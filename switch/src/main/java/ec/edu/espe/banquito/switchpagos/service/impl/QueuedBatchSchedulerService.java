package ec.edu.espe.banquito.switchpagos.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.BatchStatusLog;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.BatchStatusLogRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.provider.DateTimeProvider;

@Service
public class QueuedBatchSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(QueuedBatchSchedulerService.class);

    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentBatchProcessingService paymentBatchProcessingService;
    private final BusinessDayService businessDayService;
    private final DateTimeProvider dateTimeProvider;
    private final BatchStatusLogRepository batchStatusLogRepository;
    private final BillingService billingService;

    public QueuedBatchSchedulerService(PaymentBatchRepository paymentBatchRepository,
                                       PaymentDetailRepository paymentDetailRepository,
                                       PaymentBatchProcessingService paymentBatchProcessingService,
                                       BusinessDayService businessDayService,
                                       DateTimeProvider dateTimeProvider,
                                       BatchStatusLogRepository batchStatusLogRepository,
                                       BillingService billingService) {
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.paymentBatchProcessingService = paymentBatchProcessingService;
        this.businessDayService = businessDayService;
        this.dateTimeProvider = dateTimeProvider;
        this.batchStatusLogRepository = batchStatusLogRepository;
        this.billingService = billingService;
    }

    @Scheduled(cron = "${app.queue.processing.cron:0 * * * * *}")
    public void processQueuedBatches() {
        LocalDateTime now = dateTimeProvider.now();
        LocalDate today = now.toLocalDate();
        if (!businessDayService.isBusinessDay(today)) {
            return;
        }

        List<PaymentBatch> queued = paymentBatchRepository.findByStatusOrderByReceivedAtAsc(BatchStatusEnum.ENCOLADO);
        List<PaymentBatch> scheduled = paymentBatchRepository.findByStatusOrderByReceivedAtAsc(BatchStatusEnum.PROGRAMADO);

        List<PaymentBatch> toProcess = new java.util.ArrayList<>(queued);

        for (PaymentBatch s : scheduled) {
            if (s.getScheduledDate() == null || !s.getScheduledDate().isAfter(now)) {
                toProcess.add(s);
            }
        }

        queued = toProcess;

        if (queued.isEmpty()) {
            LOG.info("No queued batches to process.");
            return;
        }

        LOG.info("Processing {} queued batch(es)...", queued.size());
        for (PaymentBatch batch : queued) {
            try {
                var details = paymentDetailRepository.findByPaymentBatchIdOrderByLineNumberAsc(batch.getId());
                if (details == null || details.isEmpty()) {
                    LOG.warn("Queued batch {} has no details. Skipping.", batch.getId());
                    continue;
                }

                paymentBatchProcessingService.process(batch, details);
                LOG.info("Batch {} processed.", batch.getId());
            } catch (Exception e) {
                LOG.error("Error processing queued batch {}: {}", batch.getId(), e.getMessage(), e);
            }
        }
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void recoverStuckProcessingBatches() {
        LocalDateTime stuckThreshold = dateTimeProvider.now().minusMinutes(20);
        List<PaymentBatch> processing = paymentBatchRepository.findByStatus(BatchStatusEnum.PROCESSING);

        for (PaymentBatch batch : processing) {
            try {
                Optional<BatchStatusLog> lastProcessingLog = batchStatusLogRepository
                        .findTopByPaymentBatchIdAndNewStatusOrderByChangedAtDesc(batch.getId(), "PROCESSING");

                LocalDateTime processingStart = lastProcessingLog.isPresent()
                        ? lastProcessingLog.get().getChangedAt()
                        : batch.getReceivedAt();

                if (processingStart == null || processingStart.isAfter(stuckThreshold)) {
                    continue;
                }

                LOG.warn("Recovering stuck batch {} (PROCESSING since {})", batch.getId(), processingStart);

                List<PaymentDetail> details = paymentDetailRepository.findByPaymentBatchIdOrderByLineNumberAsc(batch.getId());
                boolean allPending = details.stream().allMatch(d -> d.getStatus() == PaymentDetailStatusEnum.PENDING);
                boolean hasPending = details.stream().anyMatch(d ->
                        d.getStatus() != PaymentDetailStatusEnum.SUCCESS
                        && d.getStatus() != PaymentDetailStatusEnum.REJECTED);

                if (allPending) {
                    LOG.warn("Batch {} never processed — resetting to ENCOLADO for retry", batch.getId());
                    batch.setStatus(BatchStatusEnum.ENCOLADO);
                    paymentBatchRepository.save(batch);
                } else if (hasPending) {
                    LOG.warn("Batch {} has pending details — marking REJECTED", batch.getId());
                    batch.setStatus(BatchStatusEnum.REJECTED);
                    paymentBatchRepository.save(batch);
                } else {
                    LOG.info("Batch {} has all details resolved — completing with billing", batch.getId());
                    billingService.generateCharge(batch, details);
                    batch.setStatus(BatchStatusEnum.PROCESSED);
                    paymentBatchRepository.save(batch);
                    LOG.info("Batch {} recovered to PROCESSED", batch.getId());
                }
            } catch (Exception e) {
                LOG.error("Error recovering stuck batch {}: {}", batch.getId(), e.getMessage(), e);
                try {
                    batch.setStatus(BatchStatusEnum.REJECTED);
                    paymentBatchRepository.save(batch);
                } catch (Exception saveEx) {
                    LOG.error("Could not mark batch {} as REJECTED: {}", batch.getId(), saveEx.getMessage());
                }
            }
        }
    }
}
