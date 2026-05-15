package ec.edu.espe.banquito.switchpagos.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser.CsvParseResult;
import ec.edu.espe.banquito.switchpagos.config.ValidationRulesProperties;
import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.FileValidation;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.FileValidationRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.util.DateTimeProvider;

@Service
public class FileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(FileValidationService.class);

    private final ValidationRulesProperties validationRules;
    private final FileValidationRepository fileValidationRepository;
    private final PaymentBatchRepository paymentBatchRepository;
    private final CoreFacadeService coreFacadeService;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public FileValidationService(
            ValidationRulesProperties validationRules,
            FileValidationRepository fileValidationRepository,
            PaymentBatchRepository paymentBatchRepository,
            CoreFacadeService coreFacadeService,
            DateTimeProvider dateTimeProvider) {
        this.validationRules = validationRules;
        this.fileValidationRepository = fileValidationRepository;
        this.paymentBatchRepository = paymentBatchRepository;
        this.coreFacadeService = coreFacadeService;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Transactional
    public FileValidation validateBatch(PaymentBatch batch, List<PaymentDetail> details) {
        logger.info("Validating batch {} with {} details", batch.getId(), details.size());

        FileValidation validation = new FileValidation();
        validation.setPaymentBatch(batch);
        validation.setValidatedAt(dateTimeProvider.now());
        validation.setStructureValid(true);
        validation.setTotalsMatch(true);
        validation.setDuplicateFileValid(true);
        validation.setCustomerActiveValid(true);
        validation.setValidationResult("SUCCESS");

        return fileValidationRepository.save(validation);
    }

    // RF-02: early file rejection before Core processing.
    public void validateEarlyRejection(CsvParseResult parseResult) {
        PaymentBatch batch = parseResult.getBatch();
        List<PaymentDetail> details = parseResult.getDetails();

        if (batch.getFileName() == null || batch.getFileName().isBlank()) {
            throw new IllegalArgumentException("File name is required for audit and duplicate control");
        }
        if (batch.getFileHash() == null || batch.getFileHash().isBlank()) {
            throw new IllegalArgumentException("Could not calculate the file integrity hash");
        }
        if (batch.getRuc() == null || batch.getRuc().trim().isEmpty()) {
            throw new IllegalArgumentException("Header RUC is required");
        }
        if (details.isEmpty()) {
            throw new IllegalArgumentException("The file has no detail rows");
        }
        if (parseResult.getFooterSecurityCode() == null || parseResult.getFooterSecurityCode().isBlank()) {
            throw new IllegalArgumentException("Footer security code is required");
        }

        int counted = details.size();
        BigDecimal summed = details.stream()
                .map(PaymentDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!batch.getHeaderTotalRecords().equals(counted)) {
            throw new IllegalArgumentException(String.format(
                    "Header record count (%d) does not match parsed details (%d)",
                    batch.getHeaderTotalRecords(), counted));
        }
        if (parseResult.getFooterDeclaredRecords() != counted) {
            throw new IllegalArgumentException(String.format(
                    "Footer declared records (%d) do not match parsed details (%d)",
                    parseResult.getFooterDeclaredRecords(), counted));
        }

        if (parseResult.getFooterDeclaredAmount().compareTo(batch.getHeaderTotalAmount()) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Footer amount (%s) does not match header amount (%s)",
                    parseResult.getFooterDeclaredAmount().toPlainString(),
                    batch.getHeaderTotalAmount().toPlainString()));
        }
        if (batch.getHeaderTotalAmount().compareTo(summed) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Header total amount (%s) does not match details sum (%s)",
                    batch.getHeaderTotalAmount().toPlainString(), summed.toPlainString()));
        }
        if (parseResult.getFooterDeclaredAmount().compareTo(summed) != 0) {
            throw new IllegalArgumentException(String.format(
                    "Footer declared amount (%s) does not match details sum (%s)",
                    parseResult.getFooterDeclaredAmount().toPlainString(), summed.toPlainString()));
        }

        validateNoDuplicateNominaProcessed(batch);
        validateRucClientePagosMasivos(batch.getRuc().trim());

        logger.info("RF-02 early validation passed for file {}", batch.getFileName());
    }

    private void validateNoDuplicateNominaProcessed(PaymentBatch batch) {
        LocalDateTime cutoff = (batch.getReceivedAt() != null ? batch.getReceivedAt() : dateTimeProvider.now())
                .minusDays(validationRules.getDuplicateWindowDays());

        paymentBatchRepository
                .findFirstByFileNameAndFileHashAndStatusAndReceivedAtAfter(
                        batch.getFileName(),
                        batch.getFileHash(),
                        BatchStatusEnum.PROCESSED,
                        cutoff)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(String.format(
                            "Duplicate file: '%s' with the same hash was already processed successfully in the last %d days (batch %d, received at %s)",
                            batch.getFileName(), validationRules.getDuplicateWindowDays(), existing.getId(), existing.getReceivedAt()));
                });
    }

    private void validateRucClientePagosMasivos(String ruc) {
        try {
            Boolean active = coreFacadeService.isMassPaymentsActiveForRuc(ruc);
            if (!Boolean.TRUE.equals(active)) {
                throw new IllegalArgumentException(
                        "RUC does not belong to a legal customer with active mass-payments service");
            }
        } catch (RestClientException e) {
            logger.error("Error validating mass-payments service in Core for RUC {}: {}", ruc, e.getMessage());
            throw new IllegalArgumentException(
                    "Could not validate the RUC mass-payments service against Core. Please try again later.", e);
        }
    }
}
