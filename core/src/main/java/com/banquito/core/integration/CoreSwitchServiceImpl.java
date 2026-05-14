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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CoreSwitchServiceImpl implements CoreSwitchService {

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
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada: " + accountNumber));

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
                    "Transferencia entre cuentas"
            );

            return TransferResultDTO.ok(
                    "Transferencia procesada correctamente",
                    uuid
            );
        } catch (Exception e) {
            return TransferResultDTO.rejected(
                    "TRANSFER_ERROR",
                    e.getMessage(),
                    uuid
            );
        }
    }

    private void validateDestinationOwnership(String destinationAccount, String beneficiaryIdentification) {
        if (beneficiaryIdentification == null || beneficiaryIdentification.isBlank()) {
            throw new IllegalArgumentException("Identificacion del beneficiario es obligatoria");
        }
        Account account = accountRepository.findByAccountNumber(destinationAccount)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada: " + destinationAccount));
        Customer customer = account.getCustomer();
        if (customer == null || customer.getIdentification() == null
                || !customer.getIdentification().equals(beneficiaryIdentification.trim())) {
            throw new IllegalArgumentException(
                    "La cuenta destino no pertenece a la identificacion indicada en el archivo");
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
            validatePositive(commissionSubtotal, "Subtotal de comision");
            validatePositive(vatAmount, "Monto IVA");
            validatePositive(totalAmount, "Total de comision");
            if (commissionSubtotal.add(vatAmount).compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException("Subtotal + IVA no coincide con total de comision");
            }

            Account companyAccount = accountRepository.findWithLockByAccountNumber(companyAccountNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Cuenta matriz no encontrada: " + companyAccountNumber));
            validateActiveCompanyAccount(companyAccount, companyAccountNumber);

            TransactionSubtype subtype = getActiveSubtype("COMISION");

            companyAccount.setAvailableBalance(companyAccount.getAvailableBalance().subtract(totalAmount));
            companyAccount.setAccountingBalance(companyAccount.getAccountingBalance().subtract(totalAmount));
            companyAccount.setLastUpdate(LocalDateTime.now());
            accountRepository.save(companyAccount);
            registerCompanyMovement(companyAccount, totalAmount, uuid, subtype,
                    "Debito global por servicio de pagos masivos");

            creditInstitutionalAccount(MASS_SERVICE_INCOME_ACCOUNT, commissionSubtotal);
            creditInstitutionalAccount(VAT_PAYABLE_ACCOUNT, vatAmount);

            return TransferResultDTO.ok(
                    "Comision liquidada correctamente",
                    uuid
            );
        } catch (Exception e) {
            return TransferResultDTO.rejected(
                    "COMMISSION_ERROR",
                    e.getMessage(),
                    uuid
            );
        }
    }

    private void validatePositive(BigDecimal amount, String fieldName) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser mayor a cero");
        }
    }

    private void validateActiveCompanyAccount(Account account, String accountNumber) {
        if (account.getStatus() != AccountStatusEnum.ACTIVO) {
            throw new IllegalArgumentException("Cuenta matriz no permite debitos: " + accountNumber);
        }
    }

    private TransactionSubtype getActiveSubtype(String subtypeCode) {
        TransactionSubtype subtype = subtypeRepository.findByCode(subtypeCode)
                .orElseThrow(() -> new IllegalStateException("Subtipo de transaccion no configurado: " + subtypeCode));
        if (subtype.getStatus() != CommonStatusEnum.ACTIVO) {
            throw new IllegalStateException("Subtipo de transaccion inactivo: " + subtypeCode);
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
                .orElseThrow(() -> new IllegalStateException("Cuenta contable no configurada: " + accountNumber));
        if (account.getStatus() != CommonStatusEnum.ACTIVO) {
            throw new IllegalStateException("Cuenta contable inactiva: " + accountNumber);
        }
        account.setAccountingBalance(account.getAccountingBalance().add(amount));
        institutionalAccountRepository.save(account);
    }
}
