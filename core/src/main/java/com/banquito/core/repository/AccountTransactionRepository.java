package com.banquito.core.repository;

import com.banquito.core.model.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    Optional<AccountTransaction> findByTransactionUuid(String transactionUuid);

    boolean existsByTransactionUuid(String transactionUuid);

    boolean existsByTransactionUuidAndTransactionDateBetween(
            String transactionUuid, LocalDateTime start, LocalDateTime end);

    boolean existsByAccount_IdAndTransactionUuidAndTransactionDateBetween(
            Integer accountId, String transactionUuid, LocalDateTime start, LocalDateTime end);

    List<AccountTransaction> findTop10ByAccount_IdOrderByTransactionDateDesc(Integer accountId);

    List<AccountTransaction> findTop10ByAccount_Customer_IdOrderByTransactionDateDesc(Integer customerId);
}
