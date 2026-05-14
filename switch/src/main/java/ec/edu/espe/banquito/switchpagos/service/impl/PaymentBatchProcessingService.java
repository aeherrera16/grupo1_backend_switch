package ec.edu.espe.banquito.switchpagos.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.banquito.switchpagos.config.ValidationRulesProperties;
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

@Service
public class PaymentBatchProcessingService implements IPaymentBatchProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentBatchProcessingService.class);
    
    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BatchStatusLogRepository batchStatusLogRepository;
    private final DetailStatusLogRepository detailStatusLogRepository;
    private final ICoreBankingClient coreBankingClient;
    private final ValidationRulesProperties validationRules;
    private final BillingService billingService;
    
    @Autowired
    public PaymentBatchProcessingService(PaymentBatchRepository paymentBatchRepository,
                                        PaymentDetailRepository paymentDetailRepository,
                                        BatchStatusLogRepository batchStatusLogRepository,
                                        DetailStatusLogRepository detailStatusLogRepository,
                                        ICoreBankingClient coreBankingClient,
                                        ValidationRulesProperties validationRules,
                                        BillingService billingService) {
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.batchStatusLogRepository = batchStatusLogRepository;
        this.detailStatusLogRepository = detailStatusLogRepository;
        this.coreBankingClient = coreBankingClient;
        this.validationRules = validationRules;
        this.billingService = billingService;
    }
    
    @Override
    @Transactional
    public PaymentBatch process(PaymentBatch batch, List<PaymentDetail> details) {
        logger.info("Processing batch {} with {} details", batch.getId(), details.size());
        
        try {
            // Update batch status to processing
            recordBatchStatusChange(batch, batch.getStatus(), BatchStatusEnum.PROCESSING);
            batch.setStatus(BatchStatusEnum.PROCESSING);
            // Note: setProcessedAt method may not exist, removing for now
            batch = paymentBatchRepository.save(batch);
            
            // Process each payment detail
            for (PaymentDetail detail : details) {
                try {
                    // Process individual payment
                    PaymentDetailStatusEnum previousStatus = detail.getStatus();
                    processPaymentDetail(detail);
                    detail.setStatus(PaymentDetailStatusEnum.SUCCESS);
                    detail.setExecutedAt(LocalDateTime.now());
                    recordDetailStatusChange(detail, previousStatus, PaymentDetailStatusEnum.SUCCESS, null, null);
                } catch (Exception e) {
                    logger.error("Error processing payment detail {}: {}", detail.getId(), e.getMessage());
                    PaymentDetailStatusEnum previousStatus = detail.getStatus();
                    detail.setStatus(PaymentDetailStatusEnum.REJECTED);
                    detail.setRejectionReason(e.getMessage());
                    recordDetailStatusChange(detail, previousStatus, PaymentDetailStatusEnum.REJECTED, "LINE_REJECTED", e.getMessage());
                }
                paymentDetailRepository.save(detail);
            }
            
            billingService.generarCobro(batch, details);

            // Update batch final status after financial processing and service liquidation
            recordBatchStatusChange(batch, batch.getStatus(), BatchStatusEnum.PROCESSED);
            batch.setStatus(BatchStatusEnum.PROCESSED);
            batch = paymentBatchRepository.save(batch);
            
            logger.info("Batch {} processed successfully", batch.getId());
            return batch;
            
        } catch (Exception e) {
            logger.error("Error processing batch {}: {}", batch.getId(), e.getMessage());
            recordBatchStatusChange(batch, batch.getStatus(), BatchStatusEnum.REJECTED);
            batch.setStatus(BatchStatusEnum.REJECTED); // Using REJECTED instead of FAILED
            return paymentBatchRepository.save(batch);
        }
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
    
    private void processPaymentDetail(PaymentDetail detail) {
        // Validate payment detail
        if (detail.getAmount() == null || detail.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        if (detail.getAmount().compareTo(validationRules.getMaxDetailAmount()) > 0) {
            throw new IllegalArgumentException("El monto de la línea supera el límite máximo permitido: "
                    + validationRules.getMaxDetailAmount().toPlainString());
        }
        
        if (detail.getDestinationAccountNumber() == null || detail.getDestinationAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination account is required");
        }
        
        // Send transfer to core banking
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
                    : "Transferencia rechazada por el Core";
            throw new IllegalStateException(reason);
        }
        logger.info("Transfer completed successfully for detail {}", detail.getId());
    }
}