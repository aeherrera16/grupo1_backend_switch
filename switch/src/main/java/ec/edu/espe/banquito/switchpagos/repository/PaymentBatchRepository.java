package ec.edu.espe.banquito.switchpagos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;

@Repository
public interface PaymentBatchRepository extends JpaRepository<PaymentBatch, Integer> {

    // Duplicado ya procesado exitosamente en los últimos 30 días
    Optional<PaymentBatch> findFirstByFileNameAndFileHashAndStatusAndReceivedAtAfter(
            String fileName,
            String fileHash,
            BatchStatusEnum status,
            LocalDateTime receivedAt);

    // Mismo hash activo (en proceso o recibido) sin importar cuándo
    Optional<PaymentBatch> findFirstByFileHashAndStatusIn(
            String fileHash,
            List<BatchStatusEnum> statuses);

    Optional<PaymentBatch> findFirstByFileHashAndReceivedAtAfter(
            String fileHash,
            LocalDateTime receivedAt);

    Optional<PaymentBatch> findFirstByFileHash(String fileHash);

    List<PaymentBatch> findByStatusOrderByReceivedAtAsc(BatchStatusEnum status);

    List<PaymentBatch> findByStatus(BatchStatusEnum status);

    List<PaymentBatch> findByRucOrderByReceivedAtDesc(String ruc);
}
