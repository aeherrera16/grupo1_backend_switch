package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.BatchStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchStatusLogRepository extends JpaRepository<BatchStatusLog, Integer> {

    // Método para obtener todo el historial de cambios de un archivo en particular,
    // ordenado cronológicamente.
    List<BatchStatusLog> findByPaymentBatchIdOrderByChangedAtAsc(Integer paymentBatchId);
}