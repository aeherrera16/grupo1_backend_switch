package ec.edu.espe.banquito.switchpagos.repository;

import java.util.List;

import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;

@Repository
public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Integer> {

    // Para procesar el lote iterando "línea por línea" ordenado por el número de línea
    List<PaymentDetail> findByPaymentBatchIdOrderByLineNumberAsc(Integer paymentBatchId);

    List<PaymentDetail> findByPaymentBatchId(Integer paymentBatchId);

    List<PaymentDetail> findByPaymentBatchIdAndStatus(
            Integer paymentBatchId,
            PaymentDetailStatusEnum status
    );
}
