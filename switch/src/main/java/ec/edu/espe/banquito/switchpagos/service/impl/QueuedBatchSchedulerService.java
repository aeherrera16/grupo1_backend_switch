package ec.edu.espe.banquito.switchpagos.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.util.DateTimeProvider;

@Service
public class QueuedBatchSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(QueuedBatchSchedulerService.class);

    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentBatchProcessingService paymentBatchProcessingService;
    private final BusinessDayService businessDayService;
    private final DateTimeProvider dateTimeProvider;

    public QueuedBatchSchedulerService(PaymentBatchRepository paymentBatchRepository,
                                       PaymentDetailRepository paymentDetailRepository,
                                       PaymentBatchProcessingService paymentBatchProcessingService,
                                       BusinessDayService businessDayService,
                                       DateTimeProvider dateTimeProvider) {
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.paymentBatchProcessingService = paymentBatchProcessingService;
        this.businessDayService = businessDayService;
        this.dateTimeProvider = dateTimeProvider;
    }

    // RF-01: Processes queued batches on business days.
    @Scheduled(cron = "${app.queue.processing.cron:0 1 0 * * *}")
    public void processQueuedBatches() {
        LocalDate today = dateTimeProvider.today();
        if (!businessDayService.isBusinessDay(today)) {
            LOG.info("Today is not a business day ({}). Queued batch processing is skipped.", today);
            return;
        }

        List<PaymentBatch> queued = paymentBatchRepository.findByStatusOrderByReceivedAtAsc(BatchStatusEnum.ENCOLADO);
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
}
