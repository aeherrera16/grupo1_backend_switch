package com.banquito.core.integration;

import com.banquito.core.dto.BalanceDTO;
import com.banquito.core.dto.TransferResultDTO;
import com.banquito.core.enums.AccountStatusEnum;
import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerSubtypeStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import com.banquito.core.enums.MovementTypeEnum;
import com.banquito.core.enums.TransactionStatusEnum;
import com.banquito.core.model.Account;
import com.banquito.core.model.AccountTransaction;
import com.banquito.core.model.Customer;
import com.banquito.core.model.InstitutionalAccount;
import com.banquito.core.model.TransactionSubtype;
import com.banquito.core.repository.AccountRepository;
import com.banquito.core.repository.AccountTransactionRepository;
import com.banquito.core.repository.CustomerRepository;
import com.banquito.core.repository.InstitutionalAccountRepository;
import com.banquito.core.repository.TransactionSubtypeRepository;
import com.banquito.core.service.ITransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CoreSwitchServiceImpl implements CoreSwitchService {

    private static final Logger log = LoggerFactory.getLogger(CoreSwitchServiceImpl.class);

    public static final String MASS_PAYMENTS_SUBTYPE_NAME = "EMPRESA_PAGOS_MASIVOS";
    private static final String MASS_SERVICE_INCOME_ACCOUNT = "9000000001";
    private static final String VAT_PAYABLE_ACCOUNT = "9000000002";

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final InstitutionalAccountRepository institutionalAccountRepository;
    private final AccountTransactionRepository transactionRepository;
    private final TransactionSubtypeRepository subtypeRepository;
    private final ITransactionService transactionService;

    public CoreSwitchServiceImpl(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            InstitutionalAccountRepository institutionalAccountRepository,
            AccountTransactionRepository transactionRepository,
            TransactionSubtypeRepository subtypeRepository,
            ITransactionService transactionService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.institutionalAccountRepository = institutionalAccountRepository;
        this.transactionRepository = transactionRepository;
        this.subtypeRepository = subtypeRepository;
        this.transactionService = transactionService;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceDTO getBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
        return new BalanceDTO(
                account.getAccountNumber(),
                account.getAccountingBalance(),
                account.getAvailableBalance(),
                account.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(account -> AccountStatusEnum.ACTIVO == account.getStatus())
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMassPaymentsActiveForRuc(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            return false;
        }
        return customerRepository
                .findByIdentificationTypeAndIdentification("RUC", ruc.trim())
                .map(this::isMassPaymentsEligible)
                .orElse(false);
    }

    private boolean isMassPaymentsEligible(Customer customer) {
        if (customer.getStatus() != CustomerStatusEnum.ACTIVO) {
            return false;
        }
        if (customer.getCustomerType() != CustomerTypeEnum.JURIDICO) {
            return false;
        }
        if (customer.getCustomerSubtype() == null
                || customer.getCustomerSubtype().getStatus() != CustomerSubtypeStatusEnum.ACTIVO) {
            return false;
        }
        return MASS_PAYMENTS_SUBTYPE_NAME.equals(customer.getCustomerSubtype().getName());
    }

    @Override
    public TransferResultDTO transfer(
            String originAccount,
            String destinationAccount,
            String beneficiaryIdentification,
            BigDecimal amount,
            String uuid
    ) {
        try {
            validateDestinationOwnership(destinationAccount, beneficiaryIdentification);
            transactionService.transfer(
                    originAccount,
                    destinationAccount,
                    amount,
                    uuid,
                    "TRANSFER",
                    "Transfer between accounts"
            );
            return TransferResultDTO.ok("Transfer processed successfully", uuid);
        } catch (Exception e) {
            return TransferResultDTO.rejected("TRANSFER_ERROR", e.getMessage(), uuid);
        }
    }

    private void validateDestinationOwnership(String destinationAccount, String beneficiaryIdentification) {
        if (beneficiaryIdentification == null || beneficiaryIdentification.isBlank()) {
            throw new IllegalArgumentException("Beneficiary identification is required");
        }
        Account account = accountRepository.findByAccountNumber(destinationAccount)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + destinationAccount));
        Customer customer = account.getCustomer();
        if (customer == null || customer.getIdentification() == null
                || !customer.getIdentification().equals(beneficiaryIdentification.trim())) {
            throw new IllegalArgumentException("Destination account does not belong to the indicated beneficiary");
        }
    }

    @Override
    @Transactional
    public TransferResultDTO chargeCommission(
            String companyAccountNumber,
            BigDecimal commissionSubtotal,
            BigDecimal vatAmount,
            BigDecimal totalAmount,
            String uuid
    ) {
        try {
            validatePositive(commissionSubtotal, "Commission subtotal");
            validatePositive(vatAmount, "VAT amount");
            validatePositive(totalAmount, "Total commission");
            if (commissionSubtotal.add(vatAmount).compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException("Commission subtotal + VAT does not match total");
            }

            Account companyAccount = accountRepository.findWithLockByAccountNumber(companyAccountNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Company account not found: " + companyAccountNumber));
            validateActiveCompanyAccount(companyAccount, companyAccountNumber);
            validateCommissionIdempotency(companyAccount.getId(), uuid);

            TransactionSubtype subtype = getActiveSubtype("COMISION");

            companyAccount.setAvailableBalance(companyAccount.getAvailableBalance().subtract(totalAmount));
            companyAccount.setAccountingBalance(companyAccount.getAccountingBalance().subtract(totalAmount));
            companyAccount.setLastUpdate(LocalDateTime.now());
            accountRepository.save(companyAccount);
            registerCompanyMovement(companyAccount, totalAmount, uuid, subtype,
                    "Global debit for mass payment service");

            if (companyAccount.getAccountingBalance().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Company account {} entered overdraft after commission debit. Resulting balance: {}",
                        companyAccountNumber, companyAccount.getAccountingBalance());
            }

            creditInstitutionalAccount(MASS_SERVICE_INCOME_ACCOUNT, commissionSubtotal);
            creditInstitutionalAccount(VAT_PAYABLE_ACCOUNT, vatAmount);

            return TransferResultDTO.ok("Commission settled successfully", uuid);
        } catch (Exception e) {
            return TransferResultDTO.rejected("COMMISSION_ERROR", e.getMessage(), uuid);
        }
    }

    private void validatePositive(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    private void validateActiveCompanyAccount(Account account, String accountNumber) {
        if (account.getStatus() != AccountStatusEnum.ACTIVO) {
            throw new IllegalArgumentException("Company account does not allow debits: " + accountNumber);
        }
    }

    private void validateCommissionIdempotency(Integer accountId, String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new IllegalArgumentException("Commission UUID is required");
        }
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        if (transactionRepository.existsByAccount_IdAndTransactionUuidAndTransactionDateBetween(
                accountId, uuid, startOfDay, endOfDay)) {
            throw new IllegalArgumentException("Commission already processed for this account today");
        }
    }

    private TransactionSubtype getActiveSubtype(String subtypeCode) {
        TransactionSubtype subtype = subtypeRepository.findByCode(subtypeCode)
                .orElseThrow(() -> new IllegalStateException("Transaction subtype not configured: " + subtypeCode));
        if (subtype.getStatus() != CommonStatusEnum.ACTIVO) {
            throw new IllegalStateException("Transaction subtype is inactive: " + subtypeCode);
        }
        return subtype;
    }

    private void registerCompanyMovement(Account account, BigDecimal amount, String uuid,
                                         TransactionSubtype subtype, String description) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setTransactionSubtype(subtype);
        transaction.setTransactionUuid(uuid);
        transaction.setMovementType(MovementTypeEnum.DEBITO);
        transaction.setAmount(amount);
        transaction.setResultingBalance(account.getAccountingBalance());
        transaction.setStatus(TransactionStatusEnum.COMPLETADA);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private void creditInstitutionalAccount(String accountNumber, BigDecimal amount) {
        InstitutionalAccount account = institutionalAccountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalStateException("Institutional account not configured: " + accountNumber));
        if (account.getStatus() != CommonStatusEnum.ACTIVO) {
            throw new IllegalStateException("Institutional account is inactive: " + accountNumber);
        }
        account.setAccountingBalance(account.getAccountingBalance().add(amount));
        account.setBalance(account.getBalance().add(amount));
        institutionalAccountRepository.save(account);
        log.info("Accounting credit applied: Account {} [{}], Amount {}, Resulting balance: {}",
                accountNumber, account.getName(), amount, account.getAccountingBalance());
    }
}
