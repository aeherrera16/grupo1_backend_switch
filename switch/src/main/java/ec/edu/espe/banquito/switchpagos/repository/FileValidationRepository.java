package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.FileValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileValidationRepository extends JpaRepository<FileValidation, Integer> {
    // Métodos CRUD básicos suficientes por ahora
}