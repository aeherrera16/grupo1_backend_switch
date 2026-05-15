package com.banquito.core.service.impl;

import com.banquito.core.dto.AccountRequestDTO;
import com.banquito.core.dto.AccountResponseDTO;
import com.banquito.core.dto.BalanceDTO;
import com.banquito.core.dto.TransactionResponseDTO;
import com.banquito.core.enums.AccountStatusEnum;
import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.enums.MovementTypeEnum;
import com.banquito.core.enums.TransactionStatusEnum;
import com.banquito.core.exception.AccountNotFoundException;
import com.banquito.core.exception.DuplicateTransactionException;
import com.banquito.core.exception.InactiveAccountException;
import com.banquito.core.exception.InsufficientBalanceException;
import com.banquito.core.model.Account;
import com.banquito.core.model.AccountSubtype;
import com.banquito.core.model.AccountTransaction;
import com.banquito.core.model.Branch;
import com.banquito.core.model.Customer;
import com.banquito.core.repository.AccountRepository;
import com.banquito.core.repository.AccountSubtypeRepository;
import com.banquito.core.repository.AccountTransactionRepository;
import com.banquito.core.repository.BranchRepository;
import com.banquito.core.repository.CustomerRepository;
import com.banquito.core.repository.TransactionSubtypeRepository;
import com.banquito.core.service.IAccountService;
import com.banquito.core.service.IAuthenticationService;
import com.banquito.core.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final AccountSubtypeRepository accountSubtypeRepository;
    private final AccountTransactionRepository transactionRepository;
    private final TransactionSubtypeRepository transactionSubtypeRepository;
    private final IAuthenticationService authenticationService;
    private final IEmailService emailService;

    @Transactional(readOnly = true)
    @Override
    public AccountResponseDTO findByAccountNumber(String accountNumber, Integer coreUserId) {
        authenticationService.validateActiveCoreUser(coreUserId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AccountResponseDTO> findByCustomerId(Integer customerId, Integer coreUserId) {
        authenticationService.validateActiveCoreUser(coreUserId);
        return accountRepository.findByCustomer_Id(customerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionResponseDTO> findTransactionsByCustomerId(Integer customerId, Integer coreUserId) {
        authenticationService.validateActiveCoreUser(coreUserId);
        return transactionRepository.findTop10ByAccount_Customer_IdOrderByTransactionDateDesc(customerId)
                .stream()
                .map(transaction -> toTransactionResponse(
                        transaction,
                        transaction.getAccount().getAccountNumber(),
                        transaction.getDescription()))
                .collect(Collectors.toList());
    }

     public AccountResponseDTO create(AccountRequestDTO request, Integer coreUserId) {
        authenticationService.validateActiveCoreUser(coreUserId);
        validateAccountRequest(request);

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + request.getCustomerId()));
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada: " + request.getBranchId()));
        AccountSubtype subtype = accountSubtypeRepository.findById(request.getAccountSubtypeId())
                .orElseThrow(() -> new RuntimeException("Subtipo no encontrado: " + request.getAccountSubtypeId()));

        BigDecimal initialBalance = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }

        if (Boolean.TRUE.equals(request.getIsFavorite())) {
            accountRepository.findByCustomer_IdAndIsFavoriteTrue(customer.getId())
                    .ifPresent(acc -> {
                        acc.setIsFavorite(false);
                        accountRepository.save(acc);
                    });
        }


        LocalDateTime now = LocalDateTime.now();
        Account account = new Account();
        account.setAccountNumber(resolveAccountNumber(request.getAccountNumber(), branch));
        account.setCustomer(customer);
        account.setBranch(branch);
        account.setAccountSubtype(subtype);
        account.setStatus(AccountStatusEnum.ACTIVO);
        account.setAccountingBalance(initialBalance);
        account.setAvailableBalance(initialBalance);
        account.setIsFavorite(Boolean.TRUE.equals(request.getIsFavorite()));
        account.setOpeningDate(now);
        account.setLastUpdate(now);

        log.info("CoreUser {} crea cuenta {}", coreUserId, account.getAccountNumber());
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    @Override
    public AccountResponseDTO inactivate(String accountNumber, Integer coreUserId) {
        return changeStatus(accountNumber, AccountStatusEnum.INACTIVO, coreUserId);
    }

    @Transactional
    @Override
    public AccountResponseDTO block(String accountNumber, Integer coreUserId) {
        return changeStatus(accountNumber, AccountStatusEnum.BLOQUEADO, coreUserId);
    }

    @Transactional
    @Override
    public AccountResponseDTO suspend(String accountNumber, Integer coreUserId) {
        return changeStatus(accountNumber, AccountStatusEnum.SUSPENDIDO, coreUserId);
    }

    @Transactional
    @Override
    public AccountResponseDTO activate(String accountNumber, Integer coreUserId) {
        return changeStatus(accountNumber, AccountStatusEnum.ACTIVO, coreUserId);
    }

    private AccountResponseDTO changeStatus(String accountNumber, AccountStatusEnum status, Integer coreUserId) {
        authenticationService.validateActiveCoreUser(coreUserId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.setStatus(status);
        account.setLastUpdate(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);
        log.info("CoreUser {} cambia cuenta {} a {}", coreUserId, accountNumber, status);

        if (status == AccountStatusEnum.BLOQUEADO || status == AccountStatusEnum.SUSPENDIDO) {
            String email = savedAccount.getCustomer().getEmail();
            if (email != null && !email.isBlank()) {
                emailService.sendStatusChangeEmail(email, savedAccount.getAccountNumber(), status.name());
            } else {
                log.warn("El cliente de la cuenta {} no tiene un email registrado para notificar.", accountNumber);
            }
        }

        return toResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    @Override
    public BalanceDTO getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        return new BalanceDTO(
                account.getAccountNumber(),
                account.getAccountingBalance(),
                account.getAvailableBalance(),
                account.getStatus()
        );
    }

    @Transactional
    @Override
    public TransactionResponseDTO debit(String accountNumber, BigDecimal amount) {
        validateAmount(amount);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getStatus() != AccountStatusEnum.ACTIVO) {
            throw new InactiveAccountException(accountNumber);
        }
        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(accountNumber);
        }

        account.setAvailableBalance(account.getAvailableBalance().subtract(amount));
        account.setAccountingBalance(account.getAccountingBalance().subtract(amount));
        account.setLastUpdate(LocalDateTime.now());
        accountRepository.save(account);

        String uuid = generateTransactionUuid();
        AccountTransaction transaction = registerTransaction(account, amount, MovementTypeEnum.DEBITO, account.getAvailableBalance(), uuid, "ATM_WITHDRAW");
        return toTransactionResponse(transaction, accountNumber, "Debito realizado exitosamente");
    }

    @Transactional
    @Override
    public TransactionResponseDTO credit(String accountNumber, BigDecimal amount) {
        validateAmount(amount);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (account.getStatus() == AccountStatusEnum.SUSPENDIDO) {
            throw new InactiveAccountException(accountNumber);
        }

        account.setAvailableBalance(account.getAvailableBalance().add(amount));
        account.setAccountingBalance(account.getAccountingBalance().add(amount));
        account.setLastUpdate(LocalDateTime.now());
        accountRepository.save(account);

        String uuid = generateTransactionUuid();
        AccountTransaction transaction = registerTransaction(account, amount, MovementTypeEnum.CREDITO, account.getAvailableBalance(), uuid, "DEPOSIT");
        return toTransactionResponse(transaction, accountNumber, "Credito realizado exitosamente");
    }

    @Transactional
    @Override
    public TransactionResponseDTO transfer(String origin, String destination, BigDecimal amount, String uuid) {
        validateAmount(amount);
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()) {
            throw new IllegalArgumentException("Las cuentas origen y destino son obligatorias");
        }
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser iguales");
        }

        Account originAccount = accountRepository.findByAccountNumber(origin)
                .orElseThrow(() -> new AccountNotFoundException(origin));
        Account destinationAccount = accountRepository.findByAccountNumber(destination)
                .orElseThrow(() -> new AccountNotFoundException(destination));

        validateIdempotency(originAccount, uuid);

        if (originAccount.getStatus() != AccountStatusEnum.ACTIVO) {
            throw new InactiveAccountException(origin);
        }
        if (destinationAccount.getStatus() == AccountStatusEnum.SUSPENDIDO) {
            throw new InactiveAccountException(destination);
        }
        if (originAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(origin);
        }

        originAccount.setAvailableBalance(originAccount.getAvailableBalance().subtract(amount));
        originAccount.setAccountingBalance(originAccount.getAccountingBalance().subtract(amount));
        originAccount.setLastUpdate(LocalDateTime.now());
        accountRepository.save(originAccount);

        destinationAccount.setAvailableBalance(destinationAccount.getAvailableBalance().add(amount));
        destinationAccount.setAccountingBalance(destinationAccount.getAccountingBalance().add(amount));
        destinationAccount.setLastUpdate(LocalDateTime.now());
        accountRepository.save(destinationAccount);

        AccountTransaction originTransaction = registerTransaction(originAccount, amount, MovementTypeEnum.DEBITO, originAccount.getAvailableBalance(), uuid, "TRANSFER");
        registerTransaction(destinationAccount, amount, MovementTypeEnum.CREDITO, destinationAccount.getAvailableBalance(), uuid, "TRANSFER");

        return toTransactionResponse(originTransaction, origin, "Transferencia realizada exitosamente");
    }

    private AccountTransaction registerTransaction(Account account, BigDecimal amount, MovementTypeEnum type,
                                                   BigDecimal resultingBalance, String uuid, String subtypeCode) {
        validateUuid(uuid);
        if (subtypeCode == null || subtypeCode.isBlank()) {
            throw new IllegalArgumentException("El subtipo de transaccion es obligatorio");
        }
        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setMovementType(type);
        transaction.setAmount(amount);
        transaction.setResultingBalance(resultingBalance);
        transaction.setTransactionUuid(uuid);
        transaction.setStatus(TransactionStatusEnum.COMPLETADA);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionSubtype(transactionSubtypeRepository.findByCode(subtypeCode)
                .orElseThrow(() -> new RuntimeException("Subtipo de transaccion no configurado: " + subtypeCode)));
        if (transaction.getTransactionSubtype().getStatus() != CommonStatusEnum.ACTIVO) {
            throw new IllegalArgumentException("El subtipo de transaccion no esta activo");
        }
        transaction.setDescription(transaction.getTransactionSubtype().getName());
        return transactionRepository.save(transaction);
    }

    private TransactionResponseDTO toTransactionResponse(AccountTransaction transaction, String accountNumber,
                                                         String message) {
        return new TransactionResponseDTO(
                transaction.getId(),
                accountNumber,
                transaction.getMovementType(),
                transaction.getAmount(),
                transaction.getResultingBalance(),
                transaction.getTransactionDate(),
                transaction.getTransactionUuid(),
                transaction.getStatus(),
                message
        );
    }

    private void validateAccountRequest(AccountRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud de cuenta es obligatoria");
        }
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("El titular de la cuenta es obligatorio");
        }
        if (request.getBranchId() == null) {
            throw new IllegalArgumentException("La sucursal de la cuenta es obligatoria");
        }
        if (request.getAccountSubtypeId() == null) {
            throw new IllegalArgumentException("El subtipo de cuenta es obligatorio");
        }
        if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo inicial no puede ser negativo");
        }
    }

    private void validateAccountSubtype(AccountSubtype subtype) {
        if (!"ACTIVO".equals(subtype.getStatus())) {
            throw new IllegalArgumentException("El subtipo de cuenta no esta activo");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private void validateIdempotency(Account account, String uuid) {
        validateUuid(uuid);
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        if (transactionRepository.existsByAccount_IdAndTransactionUuidAndTransactionDateBetween(
                account.getId(), uuid, startOfDay, endOfDay)) {
            throw new DuplicateTransactionException(uuid);
        }
    }

    private String resolveAccountNumber(String requestedAccountNumber, Branch branch) {
        String accountNumber;

        do {
            accountNumber = branch.getBranchCode() + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }

    private void validateUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("El UUID de transaccion es obligatorio");
        }
        if (uuid.length() > 35) {
            throw new IllegalArgumentException("El UUID de transaccion no debe superar 35 caracteres");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public AccountResponseDTO getFavoriteAccount(Integer customerId) {
        Account account = accountRepository.findByCustomer_IdAndIsFavoriteTrue(customerId)
                .orElseThrow(() -> new AccountNotFoundException("No se encontró cuenta favorita para el cliente ID: " + customerId));
        return toResponse(account);
    }

    @Transactional
    @Override
    public AccountResponseDTO updateFavoriteAccount(String accountNumber, Integer customerId) {
        Account newFavorite = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (!newFavorite.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("La cuenta no pertenece al cliente especificado.");
        }

        if (newFavorite.getStatus() != com.banquito.core.enums.AccountStatusEnum.ACTIVO) {
            throw new IllegalStateException("Solo se puede marcar como favorita una cuenta en estado ACTIVO.");
        }

        accountRepository.findByCustomer_IdAndIsFavoriteTrue(customerId)
                .ifPresent(acc -> {
                    acc.setIsFavorite(false);
                    accountRepository.save(acc);
                });

        newFavorite.setIsFavorite(true);
        log.info("Cliente {} cambió su cuenta favorita a {}", customerId, accountNumber);
        return toResponse(accountRepository.save(newFavorite));
    }

    private String generateTransactionUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private AccountResponseDTO toResponse(Account account) {
        String customerName = resolveCustomerName(account.getCustomer());
        return new AccountResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                customerName,
                account.getBranch().getName(),
                account.getAccountSubtype().getDescription(),
                account.getStatus(),
                account.getAccountingBalance(),
                account.getAvailableBalance(),
                account.getIsFavorite(),
                account.getOpeningDate()
        );
    }

    private String resolveCustomerName(Customer customer) {
        if (customer.getLegalName() != null && !customer.getLegalName().isBlank()) {
            return customer.getLegalName();
        }
        return ((customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                (customer.getLastName() != null ? customer.getLastName() : "")).trim();
    }
}