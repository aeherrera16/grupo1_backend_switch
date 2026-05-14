package ec.edu.espe.banquito.switchpagos.service;

import java.util.List;

import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;

public interface IPaymentBatchProcessingService {
    PaymentBatch process(PaymentBatch batch, List<PaymentDetail> details);
}
