package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.DetailStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailStatusLogRepository extends JpaRepository<DetailStatusLog, Integer> {

    // Método para auditar el historial de una línea de pago específica
    List<DetailStatusLog> findByPaymentDetailIdOrderByChangedAtAsc(Integer paymentDetailId);
}