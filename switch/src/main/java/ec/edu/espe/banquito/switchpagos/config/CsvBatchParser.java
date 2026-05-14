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
                    "El archivo debe contener cabecera, al menos un detalle y pie de control");
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
            throw new IllegalArgumentException("Pie inválido: se esperaban 3 campos [codigo_seguridad,registros,monto]");
        }
        String footerSecurityCode = footerParts[0].trim();
        if (footerSecurityCode.isBlank()) {
            throw new IllegalArgumentException("El código de seguridad del pie es obligatorio");
        }
        int footerDeclaredRecords = parsePositiveInt(
                "registros declarados en pie", footerParts[1].trim(), "pie del archivo");
        BigDecimal footerDeclaredAmount = parseAmount("monto declarado en pie", footerParts[2].trim());

        if (batch.getHeaderTotalRecords() == null || batch.getHeaderTotalAmount() == null) {
            throw new IllegalArgumentException("Faltan totales en cabecera");
        }

        return new CsvParseResult(batch, details, footerSecurityCode, footerDeclaredRecords, footerDeclaredAmount);
    }

    private static void parseHeader(String line, PaymentBatch batch) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Cabecera inválida: se esperaban al menos 6 campos");
        }
        batch.setRuc(parts[0].trim());
        batch.setServiceType(parseServiceType(parts[1].trim()));
        batch.setGeneratedAt(LocalDateTime.parse(parts[2].trim()));
        batch.setSourceAccountNumber(parts[3].trim());
        batch.setHeaderTotalRecords(parsePositiveInt("cantidad registros cabecera", parts[4].trim(), "cabecera"));
        batch.setHeaderTotalAmount(parseAmount("monto total cabecera", parts[5].trim()));
    }

    private static PaymentDetail parseDetailLine(String line, int physicalLineNum, PaymentBatch batch) {
        String[] parts = line.split(",", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException(
                    "Línea detalle inválida (línea " + physicalLineNum + "): se esperaban ≥7 campos");
        }
        PaymentDetail detail = new PaymentDetail();
        detail.setLineNumber(parsePositiveInt(
                "número línea detalle", parts[0].trim(), physicalLine(physicalLineNum)));
        detail.setBeneficiaryIdentification(parts[1].trim());
        detail.setBeneficiaryName(parts[2].trim());
        detail.setDestinationAccountNumber(parts[3].trim());
        detail.setAmount(parseAmount("monto detalle", parts[4].trim()));
        detail.setReference(parts[5].trim());
        detail.setBeneficiaryEmail(parts[6].trim());
        detail.setStatus(PaymentDetailStatusEnum.PENDING);
        detail.setPaymentBatch(batch);
        return detail;
    }

    private static String physicalLine(int n) {
        return "linea física " + n;
    }

    private static ServiceTypeEnum parseServiceType(String raw) {
        try {
            return ServiceTypeEnum.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return ServiceTypeEnum.fromDisplayName(raw);
        }
    }

    private static int parsePositiveInt(String campo, String raw, String contextLabel) {
        try {
            int v = Integer.parseInt(raw.trim());
            if (v < 1) {
                throw new IllegalArgumentException(
                        campo + " inválido en " + contextLabel + ": se esperaba un valor ≥ 1");
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " inválido en " + contextLabel + ": " + raw.trim(), e);
        }
    }

    private static BigDecimal parseAmount(String campo, String raw) {
        try {
            BigDecimal amt = new BigDecimal(raw.trim());
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(campo + " debe ser mayor a cero");
            }
            return amt;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " ilegible: " + raw.trim(), e);
        }
    }

    private static String sha256Hex(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
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
