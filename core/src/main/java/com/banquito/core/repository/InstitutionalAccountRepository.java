package com.banquito.core.repository;

import com.banquito.core.model.InstitutionalAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionalAccountRepository extends JpaRepository<InstitutionalAccount, Integer> {
    Optional<InstitutionalAccount> findByAccountNumber(String accountNumber);
}
