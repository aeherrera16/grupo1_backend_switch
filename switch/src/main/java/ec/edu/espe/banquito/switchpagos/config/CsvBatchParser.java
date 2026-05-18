package ec.edu.espe.banquito.switchpagos.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ServiceTypeEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;

public final class CsvBatchParser {

    private CsvBatchParser() {
    }

    @SuppressWarnings("unused")
    public static CsvParseResult parseCsvFile(InputStream inputStream, String fileName, long fileSize)
            throws IOException {
        byte[] raw = inputStream.readAllBytes();

        String hexHash = sha256Hex(raw);

        List<String> trimmedLines;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(raw), StandardCharsets.UTF_8))) {
            trimmedLines = reader.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
        }

        if (trimmedLines.size() < 3) {
            throw new IllegalArgumentException(
                    "File must contain header, at least one detail row, and footer");
        }

        PaymentBatch batch = new PaymentBatch();
        batch.setFileName(fileName);
        batch.setFileHash(hexHash);

        parseHeader(trimmedLines.get(0), batch);

        List<PaymentDetail> details = new ArrayList<>();
        for (int i = 1; i < trimmedLines.size() - 1; i++) {
            String dl = trimmedLines.get(i);
            PaymentDetail detail = parseDetailLine(dl, i + 1, batch);
            details.add(detail);
        }

        String footerLine = trimmedLines.get(trimmedLines.size() - 1);
        String[] footerParts = footerLine.split(",", -1);
        if (footerParts.length != 3) {
            throw new IllegalArgumentException("Invalid footer: expected 3 fields [security_code,records,amount]");
        }
        String footerSecurityCode = footerParts[0].trim();
        if (footerSecurityCode.isBlank()) {
            throw new IllegalArgumentException("Footer security code is required");
        }
        int footerDeclaredRecords = parsePositiveInt(
                "footer declared records", footerParts[1].trim(), "footer");
        BigDecimal footerDeclaredAmount = parseAmount("footer declared amount", footerParts[2].trim());

        if (batch.getHeaderTotalRecords() == null || batch.getHeaderTotalAmount() == null) {
            throw new IllegalArgumentException("Header totals are missing");
        }

        return new CsvParseResult(batch, details, footerSecurityCode, footerDeclaredRecords, footerDeclaredAmount);
    }

    private static void parseHeader(String line, PaymentBatch batch) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid header: expected at least 6 fields");
        }
        batch.setRuc(parts[0].trim());
        batch.setServiceType(parseServiceType(parts[1].trim()));
        batch.setGeneratedAt(LocalDateTime.parse(parts[2].trim()));
        batch.setSourceAccountNumber(parts[3].trim());
        batch.setHeaderTotalRecords(parsePositiveInt("header record count", parts[4].trim(), "header"));
        batch.setHeaderTotalAmount(parseAmount("header total amount", parts[5].trim()));
    }

    private static PaymentDetail parseDetailLine(String line, int physicalLineNum, PaymentBatch batch) {
        String[] parts = line.split(",", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException(
                    "Invalid detail row (line " + physicalLineNum + "): expected at least 7 fields");
        }
        PaymentDetail detail = new PaymentDetail();
        detail.setLineNumber(parsePositiveInt(
                "detail line number", parts[0].trim(), physicalLine(physicalLineNum)));
        detail.setBeneficiaryIdentification(parts[1].trim());
        detail.setBeneficiaryName(parts[2].trim());
        detail.setDestinationAccountNumber(parts[3].trim());
        detail.setAmount(parseAmount("detail amount", parts[4].trim()));
        detail.setReference(parts[5].trim());
        detail.setBeneficiaryEmail(parts[6].trim());
        detail.setStatus(PaymentDetailStatusEnum.PENDING);
        detail.setPaymentBatch(batch);
        return detail;
    }

    private static String physicalLine(int n) {
        return "physical line " + n;
    }

    private static ServiceTypeEnum parseServiceType(String raw) {
        try {
            return ServiceTypeEnum.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return ServiceTypeEnum.fromDisplayName(raw);
        }
    }

    private static int parsePositiveInt(String field, String raw, String contextLabel) {
        try {
            int value = Integer.parseInt(raw.trim());
            if (value < 1) {
                throw new IllegalArgumentException(
                        field + " is invalid in " + contextLabel + ": expected a value >= 1");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " is invalid in " + contextLabel + ": " + raw.trim(), e);
        }
    }

    private static BigDecimal parseAmount(String field, String raw) {
        try {
            BigDecimal amount = new BigDecimal(raw.trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(field + " must be greater than zero");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " is unreadable: " + raw.trim(), e);
        }
    }

    private static String sha256Hex(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static final class CsvParseResult {

        private final PaymentBatch batch;
        private final List<PaymentDetail> details;
        private final String footerSecurityCode;
        private final int footerDeclaredRecords;
        private final BigDecimal footerDeclaredAmount;

        public CsvParseResult(PaymentBatch batch, List<PaymentDetail> details,
                              String footerSecurityCode, int footerDeclaredRecords, BigDecimal footerDeclaredAmount) {
            this.batch = batch;
            this.details = details;
            this.footerSecurityCode = footerSecurityCode;
            this.footerDeclaredRecords = footerDeclaredRecords;
            this.footerDeclaredAmount = footerDeclaredAmount;
        }

        public PaymentBatch getBatch() {
            return batch;
        }

        public List<PaymentDetail> getDetails() {
            return details;
        }

        public String getFooterSecurityCode() {
            return footerSecurityCode;
        }

        public int getFooterDeclaredRecords() {
            return footerDeclaredRecords;
        }

        public BigDecimal getFooterDeclaredAmount() {
            return footerDeclaredAmount;
        }
    }
}
