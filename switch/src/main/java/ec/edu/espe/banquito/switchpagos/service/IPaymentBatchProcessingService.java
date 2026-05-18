package ec.edu.espe.banquito.switchpagos.service;

import java.util.List;

import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;

public interface IPaymentBatchProcessingService {
    void process(PaymentBatch batch, List<PaymentDetail> details);
}
