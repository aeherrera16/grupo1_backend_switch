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

/**
 * Procesa lotes ENCOLADOS a las 00:01 del siguiente día hábil.
 *
 * Nota: el día hábil se valida contra el core (tabla HOLIDAY). Si el core no está disponible,
 * se aplica fallback lunes-viernes.
 */
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

    /**
     * Por defecto corre todos los días a las 00:01.
     * (Spring usa formato cron con segundos: sec min hora día mes díaSemana)
     */
    @Scheduled(cron = "${app.queue.processing.cron:0 1 0 * * *}")
    public void processQueuedBatches() {
        LocalDate today = dateTimeProvider.today();
        if (!businessDayService.isBusinessDay(today)) {
            LOG.info("Hoy no es día hábil ({}). Se omite el procesamiento de lotes encolados.", today);
            return;
        }

        List<PaymentBatch> queued = paymentBatchRepository.findByStatusOrderByReceivedAtAsc(BatchStatusEnum.ENCOLADO);
        if (queued.isEmpty()) {
            LOG.info("No hay lotes ENCOLADOS para procesar.");
            return;
        }

        LOG.info("Procesando {} lote(s) ENCOLADO(s)...", queued.size());
        for (PaymentBatch batch : queued) {
            try {
                var details = paymentDetailRepository.findByPaymentBatchIdOrderByLineNumberAsc(batch.getId());
                if (details == null || details.isEmpty()) {
                    LOG.warn("Lote {} ENCOLADO sin detalles. Se omite.", batch.getId());
                    continue;
                }

                paymentBatchProcessingService.process(batch, details);
                LOG.info("Lote {} procesado.", batch.getId());
            } catch (Exception e) {
                LOG.error("Error procesando lote ENCOLADO {}: {}", batch.getId(), e.getMessage(), e);
            }
        }
    }
}
