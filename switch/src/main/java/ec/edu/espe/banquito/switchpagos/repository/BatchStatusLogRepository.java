package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.BatchStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchStatusLogRepository extends JpaRepository<BatchStatusLog, Integer> {

    // Returns batch status history.
    List<BatchStatusLog> findByPaymentBatchIdOrderByChangedAtAsc(Integer paymentBatchId);
}
