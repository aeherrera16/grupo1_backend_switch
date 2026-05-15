package com.banquito.core.repository;

import com.banquito.core.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByAccountNumber(String accountNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findWithLockByAccountNumber(@Param("accountNumber") String accountNumber);

    List<Account> findByCustomer_Id(Integer customerId);
    boolean existsByAccountNumber(String accountNumber);
    Optional<Account> findByCustomer_IdAndIsFavoriteTrue(Integer customerId);
}
