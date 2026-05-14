package ec.edu.espe.banquito.switchpagos.repository;

import ec.edu.espe.banquito.switchpagos.model.SwitchParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwitchParameterRepository extends JpaRepository<SwitchParameter, String> {
    // La clave primaria aquí es String
}