package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.DetailStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailStatusLogRepository extends JpaRepository<DetailStatusLog, Integer> {

    List<DetailStatusLog> findByPaymentDetailIdOrderByChangedAtAsc(Integer paymentDetailId);
}
