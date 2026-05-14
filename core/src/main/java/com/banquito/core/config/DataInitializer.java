package com.banquito.core.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.banquito.core.enums.AccountStatusEnum;
import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerSubtypeStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import com.banquito.core.model.Account;
import com.banquito.core.model.AccountSubtype;
import com.banquito.core.model.Branch;
import com.banquito.core.model.CoreParameter;
import com.banquito.core.model.CoreUser;
import com.banquito.core.model.Customer;
import com.banquito.core.model.CustomerSubtype;
import com.banquito.core.model.InstitutionalAccount;
import com.banquito.core.model.TransactionSubtype;
import com.banquito.core.repository.AccountRepository;
import com.banquito.core.repository.AccountSubtypeRepository;
import com.banquito.core.repository.BranchRepository;
import com.banquito.core.repository.CoreParameterRepository;
import com.banquito.core.repository.CoreUserRepository;
import com.banquito.core.repository.CustomerRepository;
import com.banquito.core.repository.CustomerSubtypeRepository;
import com.banquito.core.repository.InstitutionalAccountRepository;
import com.banquito.core.repository.TransactionSubtypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CustomerSubtypeRepository customerSubtypeRepository;
    private final BranchRepository branchRepository;
    private final AccountSubtypeRepository accountSubtypeRepository;
    private final TransactionSubtypeRepository transactionSubtypeRepository;
    private final CoreParameterRepository coreParameterRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final InstitutionalAccountRepository institutionalAccountRepository;
    private final CoreUserRepository coreUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (customerSubtypeRepository.count() == 0) initCustomerSubtypes();
        if (branchRepository.count() == 0) initBranches();
        if (accountSubtypeRepository.count() == 0) initAccountSubtypes();
        initTransactionSubtypes();
        initCoreParameters();
        initInstitutionalAccounts();
        if (coreUserRepository.count() == 0) initCoreUsers();
        initCustomers();
        initAccounts();
        log.info("Datos de prueba cargados correctamente");
    }

    private void initCustomerSubtypes() {
        CustomerSubtype personal = new CustomerSubtype();
        personal.setCustomerType("NATURAL");
        personal.setName("PERSONAL");
        personal.setDescription("Clientes personas naturales");
        personal.setStatus(CustomerSubtypeStatusEnum.ACTIVO);
        customerSubtypeRepository.save(personal);

        CustomerSubtype empresaPagosMasivos = new CustomerSubtype();
        empresaPagosMasivos.setCustomerType("JURIDICO");
        empresaPagosMasivos.setName("EMPRESA_PAGOS_MASIVOS");
        empresaPagosMasivos.setDescription("Empresa con servicio Pagos Masivos Switch activo");
        empresaPagosMasivos.setStatus(CustomerSubtypeStatusEnum.ACTIVO);
        customerSubtypeRepository.save(empresaPagosMasivos);
        log.info("CustomerSubtypes creados");
    }

    private void initBranches() {
        Branch quito = new Branch();
        quito.setBranchCode("SUC001");
        quito.setName("Sucursal Quito Centro");
        quito.setCity("Quito");
        branchRepository.save(quito);

        Branch guayaquil = new Branch();
        guayaquil.setBranchCode("SUC002");
        guayaquil.setName("Sucursal Guayaquil Norte");
        guayaquil.setCity("Guayaquil");
        branchRepository.save(guayaquil);
        log.info("Branches creadas");
    }

    private void initAccountSubtypes() {
        AccountSubtype ahorros = new AccountSubtype();
        ahorros.setSuperType("PASIVO");
        ahorros.setCode("AHO");
        ahorros.setName("Ahorros");
        ahorros.setDescription("Cuenta de Ahorros");
        ahorros.setStatus(CommonStatusEnum.ACTIVO);
        accountSubtypeRepository.save(ahorros);

        AccountSubtype corriente = new AccountSubtype();
        corriente.setSuperType("PASIVO");
        corriente.setCode("CTE");
        corriente.setName("Corriente");
        corriente.setDescription("Cuenta Corriente");
        corriente.setStatus(CommonStatusEnum.ACTIVO);
        accountSubtypeRepository.save(corriente);
        log.info("AccountSubtypes creados");
    }

    private void initTransactionSubtypes() {
        if (transactionSubtypeRepository.findByCode("TRN-GEN").isEmpty()) {
            TransactionSubtype general = new TransactionSubtype();
            general.setCode("TRN-GEN");
            general.setName("Transaccion General");
            general.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(general);
        }

        if (transactionSubtypeRepository.findByCode("TRANSFER").isEmpty()) {
            TransactionSubtype transfer = new TransactionSubtype();
            transfer.setCode("TRANSFER");
            transfer.setName("Transferencia entre cuentas");
            transfer.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(transfer);
        }

        if (transactionSubtypeRepository.findByCode("COMISION").isEmpty()) {
            TransactionSubtype commission = new TransactionSubtype();
            commission.setCode("COMISION");
            commission.setName("Cobro servicio pagos masivos");
            commission.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(commission);
        }
        log.info("TransactionSubtypes creados");
    }

    private void initCoreParameters() {
        if (coreParameterRepository.findByCode("MAX_TRANSFER_AMOUNT").isEmpty()) {
            CoreParameter maxTransferAmount = new CoreParameter();
            maxTransferAmount.setCode("MAX_TRANSFER_AMOUNT");
            maxTransferAmount.setName("Monto maximo permitido para transferencias");
            maxTransferAmount.setValueString("99999999.99");
            maxTransferAmount.setDataType("DECIMAL");
            maxTransferAmount.setDescription("Límite máximo permitido por el banco para transferencias");
            maxTransferAmount.setLastUpdate(LocalDateTime.now());
            coreParameterRepository.save(maxTransferAmount);
        }

        if (coreParameterRepository.findByCode("MAX_TRANSFER_NOM").isEmpty()) {
            CoreParameter maxTransferNom = new CoreParameter();
            maxTransferNom.setCode("MAX_TRANSFER_NOM");
            maxTransferNom.setName("Monto maximo permitido para nómina");
            maxTransferNom.setValueString("99999999.99");
            maxTransferNom.setDataType("DECIMAL");
            maxTransferNom.setDescription("Límite máximo permitido para transferencias de nómina");
            maxTransferNom.setLastUpdate(LocalDateTime.now());
            coreParameterRepository.save(maxTransferNom);
        }

        if (coreParameterRepository.findByCode("MAX_TRANSFER_PRV").isEmpty()) {
            CoreParameter maxTransferPrv = new CoreParameter();
            maxTransferPrv.setCode("MAX_TRANSFER_PRV");
            maxTransferPrv.setName("Monto maximo permitido para proveedores");
            maxTransferPrv.setValueString("99999999.99");
            maxTransferPrv.setDataType("DECIMAL");
            maxTransferPrv.setDescription("Límite máximo permitido para transferencias a proveedores");
            maxTransferPrv.setLastUpdate(LocalDateTime.now());
            coreParameterRepository.save(maxTransferPrv);
        }

        if (coreParameterRepository.findByCode("EMPRESA_1790012345001_NAME").isEmpty()) {
            CoreParameter companyName = new CoreParameter();
            companyName.setCode("EMPRESA_1790012345001_NAME");
            companyName.setName("Nombre empresa emisora pagos masivos");
            companyName.setValueString("Pagos Masivos Demo S.A.");
            companyName.setDataType("STRING");
            companyName.setDescription("Nombre legal de la empresa emisora para notificaciones RF-05");
            companyName.setLastUpdate(LocalDateTime.now());
            coreParameterRepository.save(companyName);
        }
        log.info("CoreParameters creados");
    }

    private void initInstitutionalAccounts() {
        if (institutionalAccountRepository.findByAccountNumber("9000000001").isEmpty()) {
            InstitutionalAccount ingresos = new InstitutionalAccount();
            ingresos.setAccountNumber("9000000001");
            ingresos.setName("INGRESOS_SERVICIOS_MASIVOS");
            ingresos.setAccountingBalance(BigDecimal.ZERO);
            ingresos.setStatus(CommonStatusEnum.ACTIVO);
            ingresos.setCreationDate(LocalDateTime.now());
            institutionalAccountRepository.save(ingresos);
        }

        if (institutionalAccountRepository.findByAccountNumber("9000000002").isEmpty()) {
            InstitutionalAccount iva = new InstitutionalAccount();
            iva.setAccountNumber("9000000002");
            iva.setName("PASIVOS_IVA_RETENIDO");
            iva.setAccountingBalance(BigDecimal.ZERO);
            iva.setStatus(CommonStatusEnum.ACTIVO);
            iva.setCreationDate(LocalDateTime.now());
            institutionalAccountRepository.save(iva);
        }

        log.info("InstitutionalAccounts creadas");
    }

    private void initCoreUsers() {
        CoreUser admin = new CoreUser();
        admin.setUsername("admin.core");
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setFullName("Administrador Core");
        admin.setRole("ADMIN");
        admin.setStatus(CommonStatusEnum.ACTIVO);
        admin.setCreationDate(LocalDateTime.now());
        CoreUser saved = coreUserRepository.save(admin);
        log.info("CoreUsers creados con ID: {}", saved.getId());
    }

    private void initCustomers() {
        CustomerSubtype personal = customerSubtypeRepository.findAll().stream()
                .filter(s -> "PERSONAL".equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Subtype PERSONAL no encontrado en seed"));

        CustomerSubtype empresaPagosMasivosSubtype = customerSubtypeRepository.findAll().stream()
                .filter(s -> "EMPRESA_PAGOS_MASIVOS".equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Subtype EMPRESA_PAGOS_MASIVOS no encontrado en seed"));

        if (customerRepository.findByIdentificationTypeAndIdentification("CEDULA", "1234567890").isEmpty()) {
            Customer bryan = new Customer();
            bryan.setCustomerSubtype(personal);
            bryan.setCustomerType(CustomerTypeEnum.NATURAL);
            bryan.setIdentificationType("CEDULA");
            bryan.setIdentification("1234567890");
            bryan.setFirstName("Bryan");
            bryan.setLastName("Ortiz");
            bryan.setBirthDate(LocalDate.of(2000, 1, 15));
            bryan.setEmail("bryan@banquito.com");
            bryan.setMobilePhone("0991234567");
            bryan.setAddress("Quito, Ecuador");
            bryan.setStatus(CustomerStatusEnum.ACTIVO);
            customerRepository.save(bryan);
        }

        if (customerRepository.findByIdentificationTypeAndIdentification("CEDULA", "0987654321").isEmpty()) {
            Customer ana = new Customer();
            ana.setCustomerSubtype(personal);
            ana.setCustomerType(CustomerTypeEnum.NATURAL);
            ana.setIdentificationType("CEDULA");
            ana.setIdentification("0987654321");
            ana.setFirstName("Ana");
            ana.setLastName("Garcia");
            ana.setBirthDate(LocalDate.of(1998, 5, 20));
            ana.setEmail("ana@banquito.com");
            ana.setMobilePhone("0987654321");
            ana.setAddress("Guayaquil, Ecuador");
            ana.setStatus(CustomerStatusEnum.ACTIVO);
            customerRepository.save(ana);
        }

        Customer empresaPm = customerRepository.findByIdentificationTypeAndIdentification("RUC", "1790012345001")
                .orElseGet(Customer::new);
        empresaPm.setCustomerSubtype(empresaPagosMasivosSubtype);
        empresaPm.setCustomerType(CustomerTypeEnum.JURIDICO);
        empresaPm.setIdentificationType("RUC");
        empresaPm.setIdentification("1790012345001");
        empresaPm.setLegalName("Pagos Masivos Demo S.A.");
        empresaPm.setConstitutionDate(LocalDate.of(2015, 3, 21));
        empresaPm.setEmail("tesoreria@pagosmasivosdemo.ec");
        empresaPm.setMobilePhone("022345678");
        empresaPm.setAddress("Av. Amazonas, Quito");
        empresaPm.setStatus(CustomerStatusEnum.ACTIVO);
        customerRepository.save(empresaPm);
        log.info("Customers creados");
    }

    private void initAccounts() {
        Customer bryan = customerRepository.findByIdentificationTypeAndIdentification("CEDULA", "1234567890")
                .orElseThrow(() -> new IllegalStateException("Cliente Bryan no existe en seed"));
        Customer ana = customerRepository.findByIdentificationTypeAndIdentification("CEDULA", "0987654321")
                .orElseThrow(() -> new IllegalStateException("Cliente Ana no existe en seed"));
        Customer empresaPm = customerRepository.findByIdentificationTypeAndIdentification("RUC", "1790012345001")
                .orElseThrow(() -> new IllegalStateException("Cliente empresa PM no existe en seed"));

        Branch sucursal = branchRepository.findAll().get(0);
        AccountSubtype ahorros = accountSubtypeRepository.findAll().get(0);

        if (accountRepository.findByAccountNumber("001-00001234").isEmpty()) {
            Account cuenta1 = new Account();
            cuenta1.setAccountNumber("001-00001234");
            cuenta1.setCustomer(bryan);
            cuenta1.setBranch(sucursal);
            cuenta1.setAccountSubtype(ahorros);
            cuenta1.setStatus(AccountStatusEnum.ACTIVO);
            cuenta1.setAccountingBalance(new BigDecimal("5000.00"));
            cuenta1.setAvailableBalance(new BigDecimal("5000.00"));
            cuenta1.setIsFavorite(false);
            cuenta1.setOpeningDate(LocalDateTime.now());
            accountRepository.save(cuenta1);
        }

        if (accountRepository.findByAccountNumber("001-00005678").isEmpty()) {
            Account cuenta2 = new Account();
            cuenta2.setAccountNumber("001-00005678");
            cuenta2.setCustomer(ana);
            cuenta2.setBranch(sucursal);
            cuenta2.setAccountSubtype(ahorros);
            cuenta2.setStatus(AccountStatusEnum.ACTIVO);
            cuenta2.setAccountingBalance(new BigDecimal("2500.00"));
            cuenta2.setAvailableBalance(new BigDecimal("2500.00"));
            cuenta2.setIsFavorite(false);
            cuenta2.setOpeningDate(LocalDateTime.now());
            accountRepository.save(cuenta2);
        }

        Account cuentaEmpresaPm = accountRepository.findByAccountNumber("0050000202")
                .orElseGet(Account::new);
        cuentaEmpresaPm.setAccountNumber("0050000202");
        cuentaEmpresaPm.setCustomer(empresaPm);
        cuentaEmpresaPm.setBranch(sucursal);
        cuentaEmpresaPm.setAccountSubtype(ahorros);
        cuentaEmpresaPm.setStatus(AccountStatusEnum.ACTIVO);
        if (cuentaEmpresaPm.getAccountingBalance() == null) {
            cuentaEmpresaPm.setAccountingBalance(new BigDecimal("100000.00"));
        }
        if (cuentaEmpresaPm.getAvailableBalance() == null) {
            cuentaEmpresaPm.setAvailableBalance(new BigDecimal("100000.00"));
        }
        cuentaEmpresaPm.setIsFavorite(true);
        if (cuentaEmpresaPm.getOpeningDate() == null) {
            cuentaEmpresaPm.setOpeningDate(LocalDateTime.now());
        }
        accountRepository.save(cuentaEmpresaPm);

        log.info("Accounts creadas");
    }
}
