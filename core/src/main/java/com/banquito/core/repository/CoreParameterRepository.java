package com.banquito.core.repository;

import com.banquito.core.model.CoreParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CoreParameterRepository extends JpaRepository<CoreParameter, String> {
    Optional<CoreParameter> findByCode(String code);
}