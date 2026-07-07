package ec.edu.espe.banquito.switchpagos.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import ec.edu.espe.banquito.switchpagos.dto.CoreParameterResponseDTO;
import ec.edu.espe.banquito.switchpagos.dto.PaymentSuccessNotificationRequestDTO;
import ec.edu.espe.banquito.switchpagos.dto.TransferResponseDTO;
import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.BatchStatusLog;
import ec.edu.espe.banquito.switchpagos.model.DetailStatusLog;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.BatchStatusLogRepository;
import ec.edu.espe.banquito.switchpagos.repository.DetailStatusLogRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.service.ICoreBankingClient;
import ec.edu.espe.banquito.switchpagos.service.IPaymentBatchProcessingService;
import ec.edu.espe.banquito.switchpagos.service.IPaymentNotificationClient;

@Service
public class PaymentBatchProcessingService implements IPaymentBatchProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentBatchProcessingService.class);

    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BatchStatusLogRepository batchStatusLogRepository;
    private final DetailStatusLogRepository detailStatusLogRepository;
    private final ICoreBankingClient coreBankingClient;
    private final IPaymentNotificationClient paymentNotificationClient;
    private final BillingService billingService;

    @Autowired
    public PaymentBatchProcessingService(PaymentBatchRepository paymentBatchRepository,
                                         PaymentDetailRepository paymentDetailRepository,
                                         BatchStatusLogRepository batchStatusLogRepository,
                                         DetailStatusLogRepository detailStatusLogRepository,
                                         ICoreBankingClient coreBankingClient,
                                         IPaymentNotificationClient paymentNotificationClient,
                                         BillingService billingService) {
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.batchStatusLogRepository = batchStatusLogRepository;
        this.detailStatusLogRepository = detailStatusLogRepository;
        this.coreBankingClient = coreBankingClient;
        this.paymentNotificationClient = paymentNotificationClient;
        this.billingService = billingService;
    }

    @Override
    @Async
    public void process(PaymentBatch batch, List<PaymentDetail> details) {
        logger.info("Processing batch {} with {} details", batch.getId(), details.size());
        Integer batchId = batch.getId();

        try {
            batch = updateBatchStatusWithRetry(batchId, BatchStatusEnum.PROCESSING);

            BigDecimal maxAmount = resolveMaxAmountForTransfer(batch);
            String companyName = resolveCompanyName(batch);

            for (PaymentDetail detail : details) {
                try {
                    PaymentDetailStatusEnum previousStatus = detail.getStatus();
                    processPaymentDetail(detail, maxAmount);
                    detail.setStatus(PaymentDetailStatusEnum.SUCCESS);
                    detail.setExecutedAt(LocalDateTime.now());
                    recordDetailStatusChange(detail, previousStatus, PaymentDetailStatusEnum.SUCCESS, null, null);
                    notifySuccessfulPayment(batch, detail, companyName);
                } catch (Exception e) {
                    logger.error("Error processing payment detail {}: {}", detail.getId(), e.getMessage());
                    PaymentDetailStatusEnum previousStatus = detail.getStatus();
                    detail.setStatus(PaymentDetailStatusEnum.REJECTED);
                    detail.setRejectionReason(truncate(e.getMessage(), 255));
                    recordDetailStatusChange(detail, previousStatus, PaymentDetailStatusEnum.REJECTED, "LINE_REJECTED", e.getMessage());
                }
                paymentDetailRepository.save(detail);
            }

            billingService.generateCharge(batch, details);

            // generateCharge() commits its own updates to the batch row in a separate
            // transaction, so the in-memory instance held here may now be behind the
            // database version. Re-fetching with retry avoids an OptimisticLockException
            // if another transaction bumped the row's version concurrently.
            updateBatchStatusWithRetry(batchId, BatchStatusEnum.PROCESSED);

            logger.info("Batch {} processed successfully", batchId);

        } catch (Exception e) {
            logger.error("Error processing batch {}: {}", batchId, e.getMessage());
            try {
                updateBatchStatusWithRetry(batchId, BatchStatusEnum.REJECTED);
            } catch (Exception saveEx) {
                logger.error("Could not persist REJECTED status for batch {}: {}", batchId, saveEx.getMessage());
            }
        }
    }

    private PaymentBatch updateBatchStatusWithRetry(Integer batchId, BatchStatusEnum newStatus) {
        final int maxAttempts = 5;
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                PaymentBatch fresh = paymentBatchRepository.findById(batchId)
                        .orElseThrow(() -> new IllegalStateException("Batch not found: " + batchId));
                recordBatchStatusChange(fresh, fresh.getStatus(), newStatus);
                fresh.setStatus(newStatus);
                return paymentBatchRepository.save(fresh);
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                lastFailure = e;
                logger.warn("Optimistic lock conflict updating batch {} to {} (attempt {}/{}), retrying",
                        batchId, newStatus, attempt, maxAttempts);
            }
        }
        throw lastFailure != null ? lastFailure
                : new IllegalStateException("Could not update batch " + batchId + " to " + newStatus);
    }

    private void recordBatchStatusChange(PaymentBatch batch, BatchStatusEnum previousStatus, BatchStatusEnum newStatus) {
        BatchStatusLog log = new BatchStatusLog();
        log.setPaymentBatch(batch);
        log.setPreviousStatus(previousStatus != null ? previousStatus.name() : null);
        log.setNewStatus(newStatus != null ? newStatus.name() : null);
        log.setChangedAt(LocalDateTime.now());
        batchStatusLogRepository.save(log);
    }

    private void recordDetailStatusChange(PaymentDetail detail,
                                          PaymentDetailStatusEnum previousStatus,
                                          PaymentDetailStatusEnum newStatus,
                                          String errorCode,
                                          String errorDescription) {
        DetailStatusLog log = new DetailStatusLog();
        log.setPaymentDetail(detail);
        log.setPreviousStatus(previousStatus != null ? previousStatus.name() : null);
        log.setNewStatus(newStatus != null ? newStatus.name() : null);
        log.setErrorCode(errorCode);
        log.setErrorDescription(errorDescription);
        log.setChangedAt(LocalDateTime.now());
        detailStatusLogRepository.save(log);
    }

    private void processPaymentDetail(PaymentDetail detail, BigDecimal maxAmount) {
        if (detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        if (detail.getAmount().compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("The line amount exceeds the maximum allowed limit: "
                    + maxAmount.toPlainString());
        }

        if (detail.getDestinationAccountNumber() == null || detail.getDestinationAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination account is required");
        }

        String originAccount = detail.getPaymentBatch().getSourceAccountNumber();
        String destinationAccount = detail.getDestinationAccountNumber();
        BigDecimal amount = detail.getAmount();
        String uuid = UUID.randomUUID().toString();

        logger.info("Sending transfer to core: {} -> {} amount: {}", originAccount, destinationAccount, amount);
        TransferResponseDTO response = coreBankingClient.transfer(
                originAccount,
                destinationAccount,
                detail.getBeneficiaryIdentification(),
                amount,
                uuid);
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            String reason = response != null && response.getMessage() != null
                    ? response.getMessage()
                    : "Transfer rejected by Core";
            throw new IllegalStateException(reason);
        }
        logger.info("Transfer completed successfully for detail {}", detail.getId());
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private void notifySuccessfulPayment(PaymentBatch batch, PaymentDetail detail, String companyName) {
        if ("SENT".equals(detail.getNotificationStatus())) {
            return;
        }
        detail.setNotificationStatus("PENDING");

        if (!StringUtils.hasText(detail.getBeneficiaryEmail())) {
            logger.warn("Notification not sent for detail {} because email is missing", detail.getId());
            detail.setNotificationStatus("SIN_CORREO");
            return;
        }

        if (!StringUtils.hasText(companyName)) {
            logger.warn("Notification not sent for detail {} because Core did not return company name", detail.getId());
            detail.setNotificationStatus("SIN_EMPRESA");
            return;
        }

        PaymentSuccessNotificationRequestDTO request = new PaymentSuccessNotificationRequestDTO();
        request.setPaymentDetailId(detail.getId());
        request.setBeneficiaryEmail(detail.getBeneficiaryEmail());
        request.setBeneficiaryName(detail.getBeneficiaryName());
        request.setAmount(detail.getAmount());
        request.setConcept(StringUtils.hasText(detail.getReference()) ? detail.getReference() : "Mass payment");
        request.setCompanyName(companyName);

        boolean sent = paymentNotificationClient.sendPaymentSuccessNotification(request);
        detail.setNotificationStatus(sent ? "SENT" : "FAILED");
    }

    private String resolveCompanyName(PaymentBatch batch) {
        if (batch == null || !StringUtils.hasText(batch.getRuc())) {
            return null;
        }

        String parameterCode = "EMPRESA_" + batch.getRuc().trim() + "_NAME";
        try {
            CoreParameterResponseDTO parameter = coreBankingClient.getParameter(parameterCode);
            if (parameter == null || !StringUtils.hasText(parameter.getValueString())) {
                return null;
            }
            return parameter.getValueString().trim();
        } catch (RestClientException e) {
            logger.warn("Could not fetch sender company {} from Core: {}", parameterCode, e.getMessage());
            return null;
        }
    }

    private BigDecimal resolveMaxAmountForTransfer(PaymentBatch batch) {
        if (batch == null || batch.getServiceType() == null) {
            return new BigDecimal("999999.99");
        }

        String serviceSpecificCode = "MAX_TRANSFER_" + batch.getServiceType().name();
        String[] candidateCodes = {serviceSpecificCode, "MAX_TRANSFER_AMOUNT"};

        for (String candidateCode : candidateCodes) {
            try {
                CoreParameterResponseDTO parameter = coreBankingClient.getParameter(candidateCode);
                if (parameter == null || parameter.getValueString() == null || parameter.getValueString().isBlank()) {
                    continue;
                }
                return new BigDecimal(parameter.getValueString().trim());
            } catch (RestClientException e) {
                logger.warn("Could not fetch parameter {} from Core: {}", candidateCode, e.getMessage());
            } catch (NumberFormatException e) {
                throw new IllegalStateException(
                        "The max limit returned by Core is not a valid numeric format for " + candidateCode,
                        e);
            }
        }

        logger.warn("Core did not return max limit for {}, using fallback 999999.99", batch.getServiceType().name());
        return new BigDecimal("999999.99");
    }
}
