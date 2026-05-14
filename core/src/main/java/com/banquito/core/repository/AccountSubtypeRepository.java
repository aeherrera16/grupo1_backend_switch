package com.banquito.core.repository;

import com.banquito.core.model.AccountSubtype;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountSubtypeRepository extends JpaRepository<AccountSubtype, Integer> {
    Optional<AccountSubtype> findByCode(String code);
}
