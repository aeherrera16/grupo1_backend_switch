package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceChargeRepository extends JpaRepository<ServiceCharge, Integer> {

    // Returns the batch charge.
    Optional<ServiceCharge> findByPaymentBatchId(Integer paymentBatchId);
}
