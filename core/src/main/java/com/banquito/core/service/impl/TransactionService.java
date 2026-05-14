package com.banquito.core.service.impl;

import com.banquito.core.dto.TransactionResponseDTO;
import com.banquito.core.enums.AccountStatusEnum;
import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.enums.MovementTypeEnum;
import com.banquito.core.enums.TransactionStatusEnum;
import com.banquito.core.exception.InactiveAccountException;
import com.banquito.core.exception.AccountNotFoundException;
import com.banquito.core.exception.InsufficientBalanceException;
import com.banquito.core.exception.DuplicateTransactionException;
import com.banquito.core.model.Account;
import com.banquito.core.model.AccountTransaction;
import com.banquito.core.model.TransactionSubtype;
import com.banquito.core.repository.AccountRepository;
import com.banquito.core.repository.AccountTransactionRepository;
import com.banquito.core.repository.TransactionSubtypeRepository;
import com.banquito.core.service.ITransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService implements ITransactionService {

    private static final String DEFAULT_TRANSFER_SUBTYPE = "TRANSFER";

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final TransactionSubtypeRepository subtypeRepository;

    @Override
    @Transactional
    public TransactionResponseDTO debit(String accountNumber, BigDecimal amount, String uuid,
                                          String subtypeCode, String description) {
        validateIdempotency(uuid);
        validatePositiveAmount(amount);

        Account account = getActiveAccountWithLock(accountNumber);
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(accountNumber);
        }

        subtract(account, amount);
        AccountTransaction transaction = registerMovement(
                account, amount, MovementTypeEnum.DEBITO, uuid, subtypeCode, description);
        log.info("Debito exitoso: Cuenta {}, Monto {}, UUID {}", accountNumber, amount, uuid);
        return toResponse(transaction, "Debito procesado correctamente");
    }

    @Override
    @Transactional
    public TransactionResponseDTO credit(String accountNumber, BigDecimal amount, String uuid,
                                            String subtypeCode, String description) {
        validateIdempotency(uuid);
        validatePositiveAmount(amount);

        Account account = getActiveAccountWithLock(accountNumber);
        add(account, amount);
        AccountTransaction transaction = registerMovement(
                account, amount, MovementTypeEnum.CREDITO, uuid, subtypeCode, description);
        log.info("Credito exitoso: Cuenta {}, Monto {}, UUID {}", accountNumber, amount, uuid);
        return toResponse(transaction, "Credito procesado correctamente");
    }

    @Override
    @Transactional
    public TransactionResponseDTO transfer(String originAccountNumber, String destinationAccountNumber,
                                             BigDecimal amount, String uuid, String subtypeCode, String description) {
        validateIdempotency(uuid);
        validatePositiveAmount(amount);
        if (originAccountNumber.equals(destinationAccountNumber)) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser la misma");
        }

        Account firstLock = lockFirst(originAccountNumber, destinationAccountNumber);
        Account secondLock = lockSecond(originAccountNumber, destinationAccountNumber);
        Account origin = originAccountNumber.equals(firstLock.getAccountNumber()) ? firstLock : secondLock;
        Account destination = destinationAccountNumber.equals(firstLock.getAccountNumber()) ? firstLock : secondLock;

        validateActive(origin, originAccountNumber);
        validateActive(destination, destinationAccountNumber);
        if (origin.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(originAccountNumber);
        }

        subtract(origin, amount);
        add(destination, amount);

        String effectiveSubtype = subtypeCode != null && !subtypeCode.isBlank() ? subtypeCode : DEFAULT_TRANSFER_SUBTYPE;
        AccountTransaction debit = registerMovement(
                origin, amount, MovementTypeEnum.DEBITO, uuid, effectiveSubtype, description);
        registerMovement(destination, amount, MovementTypeEnum.CREDITO, uuid, effectiveSubtype, description);

        log.info("Transferencia exitosa: Origen {}, Destino {}, Monto {}, UUID {}",
                originAccountNumber, destinationAccountNumber, amount, uuid);
        return toResponse(debit, "Transferencia procesada correctamente");
    }

    private void validateIdempotency(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("TRANSACTION_UUID es obligatorio");
        }
        if (transactionRepository.existsByTransactionUuid(uuid)) {
            throw new DuplicateTransactionException(uuid);
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private Account getActiveAccountWithLock(String accountNumber) {
        Account account = accountRepository.findWithLockByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        validateActive(account, accountNumber);
        return account;
    }

    private void validateActive(Account account, String accountNumber) {
        if (account.getStatus() != AccountStatusEnum.ACTIVO) {
            throw new InactiveAccountException(accountNumber);
        }
    }

    private Account lockFirst(String originAccountNumber, String destinationAccountNumber) {
        String first = originAccountNumber.compareTo(destinationAccountNumber) < 0
                ? originAccountNumber
                : destinationAccountNumber;
        return accountRepository.findWithLockByAccountNumber(first)
                .orElseThrow(() -> new AccountNotFoundException(first));
    }

    private Account lockSecond(String originAccountNumber, String destinationAccountNumber) {
        String second = originAccountNumber.compareTo(destinationAccountNumber) < 0
                ? destinationAccountNumber
                : originAccountNumber;
        return accountRepository.findWithLockByAccountNumber(second)
                .orElseThrow(() -> new AccountNotFoundException(second));
    }

    private void subtract(Account account, BigDecimal amount) {
        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setAccountingBalance(account.getAccountingBalance().subtract(amount));
        account.setLastUpdate(LocalDateTime.now());
        accountRepository.save(account);
    }

    private void add(Account account, BigDecimal amount) {
        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setAccountingBalance(account.getAccountingBalance().add(amount));
        account.setLastUpdate(LocalDateTime.now());
        accountRepository.save(account);
    }

    private AccountTransaction registerMovement(Account account, BigDecimal amount, MovementTypeEnum type,
                                                String uuid, String subtypeCode, String description) {
        TransactionSubtype subtype = subtypeRepository.findByCode(subtypeCode)
                .orElseThrow(() -> new RuntimeException("Subtipo de transaccion no configurado: " + subtypeCode));
        if (subtype.getStatus() != CommonStatusEnum.ACTIVO) {
            throw new IllegalStateException("Subtipo de transaccion inactivo: " + subtypeCode);
        }

        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setTransactionSubtype(subtype);
        transaction.setTransactionUuid(uuid);
        transaction.setMovementType(type);
        transaction.setAmount(amount);
        transaction.setResultingBalance(account.getAccountingBalance());
        transaction.setStatus(TransactionStatusEnum.COMPLETADA);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    private TransactionResponseDTO toResponse(AccountTransaction transaction, String message) {
        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAccount().getAccountNumber(),
                transaction.getMovementType(),
                transaction.getAmount(),
                transaction.getResultingBalance(),
                transaction.getTransactionDate(),
                transaction.getTransactionUuid(),
                transaction.getStatus(),
                message
        );
    }
}
