package ec.edu.espe.banquito.switchpagos.service.impl;

import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.service.IPaymentDetailService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentDetailServiceImpl implements IPaymentDetailService {
    private final PaymentDetailRepository paymentDetailRepository;
    public PaymentDetailServiceImpl(PaymentDetailRepository paymentDetailRepository) {
        this.paymentDetailRepository = paymentDetailRepository;
    }
    @Override
    @Transactional
    public void processBatch(Integer paymentBatchId) {
        System.out.println(
                "=== INICIANDO PROCESAMIENTO BATCH "
                        + paymentBatchId
        );
        List<PaymentDetail> details =
                paymentDetailRepository
                        .findByPaymentBatchIdOrderByLineNumberAsc(paymentBatchId);

        for (PaymentDetail detail : details) {

            try {

                // =========================
                // SIMULACIÓN CORE BANCARIO
                // =========================

                executeTransfer(
                        detail.getDestinationAccountNumber(),
                        detail.getAmount()
                );

                // =========================
                // SUCCESS
                // =========================

                detail.setStatus(PaymentDetailStatusEnum.SUCCESS);
                detail.setRejectionReason(null);
                detail.setExecutedAt(LocalDateTime.now());

            } catch (Exception e) {

                // =========================
                // ERROR POR LÍNEA
                // =========================

                detail.setStatus(PaymentDetailStatusEnum.REJECTED);
                detail.setRejectionReason(e.getMessage());

            }

            // IMPORTANTE:
            // guardar SIEMPRE cada línea

            paymentDetailRepository.save(detail);
        }
    }

    // ==========================================
    // MÉTODO QUE SIMULA LLAMADA AL CORE
    // ==========================================

    private void executeTransfer(String account, java.math.BigDecimal amount) {

        // Simulación de errores

        if (amount.doubleValue() > 1000) {
            throw new RuntimeException("Monto excede el límite permitido");
        }

        if (account == null || account.isEmpty()) {
            throw new RuntimeException("Cuenta destino inválida");
        }

        // Simulación de UUID transaccional

        UUID transactionId = UUID.randomUUID();

        System.out.println("Transferencia ejecutada: " + transactionId);
    }
}
