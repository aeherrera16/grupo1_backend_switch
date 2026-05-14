package com.banquito.core.repository;

import com.banquito.core.model.TransactionSubtype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionSubtypeRepository extends JpaRepository<TransactionSubtype, Integer> {
    Optional<TransactionSubtype> findByCode(String code);
}
