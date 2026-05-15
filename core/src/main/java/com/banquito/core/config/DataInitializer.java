package com.banquito.core.config;

import com.banquito.core.enums.AccountStatusEnum;
import com.banquito.core.enums.CommonStatusEnum;
import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerSubtypeStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import com.banquito.core.model.*;
import com.banquito.core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        initCustomerSubtypes();
        initBranches();
        initAccountSubtypes();
        initTransactionSubtypes();
        initCoreParameters();
        initInstitutionalAccounts();

        if (coreUserRepository.count() == 0) {
            initCoreUsers();
        }

        initMassiveCustomers();
        initMassiveAccounts();

        log.info("Datos de prueba cargados correctamente");
    }

    private void initCustomerSubtypes() {
        if (customerSubtypeRepository.findAll().stream()
                .noneMatch(s -> "PERSONAL".equals(s.getName()))) {
            CustomerSubtype personal = new CustomerSubtype();
            personal.setCustomerType("NATURAL");
            personal.setName("PERSONAL");
            personal.setDescription("Clientes personas naturales");
            personal.setStatus(CustomerSubtypeStatusEnum.ACTIVO);
            customerSubtypeRepository.save(personal);
        }

        if (customerSubtypeRepository.findAll().stream()
                .noneMatch(s -> "EMPRESA_PAGOS_MASIVOS".equals(s.getName()))) {
            CustomerSubtype empresaPagosMasivos = new CustomerSubtype();
            empresaPagosMasivos.setCustomerType("JURIDICO");
            empresaPagosMasivos.setName("EMPRESA_PAGOS_MASIVOS");
            empresaPagosMasivos.setDescription("Empresa con servicio Pagos Masivos Switch activo");
            empresaPagosMasivos.setStatus(CustomerSubtypeStatusEnum.ACTIVO);
            customerSubtypeRepository.save(empresaPagosMasivos);
        }

        log.info("CustomerSubtypes creados o verificados");
    }

    private void initBranches() {
        if (branchRepository.findByBranchCode("001").isEmpty()) {
            Branch norte = new Branch();
            norte.setBranchCode("001");
            norte.setName("Sucursal Norte");
            norte.setCity("Quito");
            norte.setCreationDate(LocalDateTime.now());
            branchRepository.save(norte);
        }

        if (branchRepository.findByBranchCode("002").isEmpty()) {
            Branch sur = new Branch();
            sur.setBranchCode("002");
            sur.setName("Sucursal Sur");
            sur.setCity("Quito");
            sur.setCreationDate(LocalDateTime.now());
            branchRepository.save(sur);
        }

        if (branchRepository.findByBranchCode("003").isEmpty()) {
            Branch centro = new Branch();
            centro.setBranchCode("003");
            centro.setName("Sucursal Centro");
            centro.setCity("Quito");
            centro.setCreationDate(LocalDateTime.now());
            branchRepository.save(centro);
        }

        if (branchRepository.findByBranchCode("004").isEmpty()) {
            Branch valles = new Branch();
            valles.setBranchCode("004");
            valles.setName("Sucursal Valles");
            valles.setCity("Quito");
            valles.setCreationDate(LocalDateTime.now());
            branchRepository.save(valles);
        }

        if (branchRepository.findByBranchCode("005").isEmpty()) {
            Branch digital = new Branch();
            digital.setBranchCode("005");
            digital.setName("Sucursal Digital");
            digital.setCity("Digital");
            digital.setCreationDate(LocalDateTime.now());
            branchRepository.save(digital);
        }

        log.info("Branches creadas o verificadas");
    }

    private void initAccountSubtypes() {
        if (accountSubtypeRepository.findAll().stream()
                .noneMatch(s -> "AHO".equals(s.getCode()))) {
            AccountSubtype ahorros = new AccountSubtype();
            ahorros.setSuperType("PASIVO");
            ahorros.setCode("AHO");
            ahorros.setName("Ahorros");
            ahorros.setDescription("Cuenta de Ahorros");
            ahorros.setStatus(CommonStatusEnum.ACTIVO);
            accountSubtypeRepository.save(ahorros);
        }

        if (accountSubtypeRepository.findAll().stream()
                .noneMatch(s -> "CTE".equals(s.getCode()))) {
            AccountSubtype corriente = new AccountSubtype();
            corriente.setSuperType("PASIVO");
            corriente.setCode("CTE");
            corriente.setName("Corriente");
            corriente.setDescription("Cuenta Corriente");
            corriente.setStatus(CommonStatusEnum.ACTIVO);
            accountSubtypeRepository.save(corriente);
        }

        if (accountSubtypeRepository.findAll().stream()
                .noneMatch(s -> "NOM".equals(s.getCode()))) {
            AccountSubtype nomina = new AccountSubtype();
            nomina.setSuperType("PASIVO");
            nomina.setCode("NOM");
            nomina.setName("Nómina");
            nomina.setDescription("Cuenta de Nómina");
            nomina.setStatus(CommonStatusEnum.ACTIVO);
            accountSubtypeRepository.save(nomina);
        }
        log.info("AccountSubtypes creados o verificados");
    }

    private void initTransactionSubtypes() {
        if (transactionSubtypeRepository.findByCode("TRN-GEN").isEmpty()) {
            TransactionSubtype general = new TransactionSubtype();
            general.setCode("TRN-GEN");
            general.setName("Transaccion General");
            general.setDescription("Movimiento general de cuenta");
            general.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(general);
        }

        if (transactionSubtypeRepository.findByCode("TRANSFER").isEmpty()) {
            TransactionSubtype transfer = new TransactionSubtype();
            transfer.setCode("TRANSFER");
            transfer.setName("Transferencia entre cuentas");
            transfer.setDescription("Transferencia entre cuentas bancarias");
            transfer.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(transfer);
        }

        if (transactionSubtypeRepository.findByCode("MASS_PAYMENT").isEmpty()) {
            TransactionSubtype massPayment = new TransactionSubtype();
            massPayment.setCode("MASS_PAYMENT");
            massPayment.setName("Pago masivo");
            massPayment.setDescription("Debito por procesamiento de pago masivo");
            massPayment.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(massPayment);
        }

        if (transactionSubtypeRepository.findByCode("ATM_WITHDRAW").isEmpty()) {
            TransactionSubtype atmWithdraw = new TransactionSubtype();
            atmWithdraw.setCode("ATM_WITHDRAW");
            atmWithdraw.setName("Retiro por cajero");
            atmWithdraw.setDescription("Retiro de efectivo por cajero automatico");
            atmWithdraw.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(atmWithdraw);
        }

        if (transactionSubtypeRepository.findByCode("PURCHASE").isEmpty()) {
            TransactionSubtype purchase = new TransactionSubtype();
            purchase.setCode("PURCHASE");
            purchase.setName("Compra en comercio");
            purchase.setDescription("Debito por compra en comercio");
            purchase.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(purchase);
        }

        if (transactionSubtypeRepository.findByCode("COMISION").isEmpty()) {
            TransactionSubtype commission = new TransactionSubtype();
            commission.setCode("COMISION");
            commission.setName("Cobro de comision");
            commission.setDescription("Debito por cobro de comision bancaria");
            commission.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(commission);
        }

        if (transactionSubtypeRepository.findByCode("TAX_PAYMENT").isEmpty()) {
            TransactionSubtype taxPayment = new TransactionSubtype();
            taxPayment.setCode("TAX_PAYMENT");
            taxPayment.setName("Pago de impuestos");
            taxPayment.setDescription("Debito por pago de impuestos");
            taxPayment.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(taxPayment);
        }

        if (transactionSubtypeRepository.findByCode("PAYROLL").isEmpty()) {
            TransactionSubtype payroll = new TransactionSubtype();
            payroll.setCode("PAYROLL");
            payroll.setName("Abono de nomina");
            payroll.setDescription("Credito por abono de nomina");
            payroll.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(payroll);
        }

        if (transactionSubtypeRepository.findByCode("DEPOSIT").isEmpty()) {
            TransactionSubtype deposit = new TransactionSubtype();
            deposit.setCode("DEPOSIT");
            deposit.setName("Deposito por ventanilla");
            deposit.setDescription("Credito por deposito realizado en ventanilla");
            deposit.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(deposit);
        }

        if (transactionSubtypeRepository.findByCode("TRANSFER_IN").isEmpty()) {
            TransactionSubtype transferIn = new TransactionSubtype();
            transferIn.setCode("TRANSFER_IN");
            transferIn.setName("Transferencia recibida");
            transferIn.setDescription("Credito por transferencia recibida");
            transferIn.setStatus(CommonStatusEnum.ACTIVO);
            transactionSubtypeRepository.save(transferIn);
        }

        log.info("TransactionSubtypes creados o verificados");
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

        log.info("CoreParameters creados o verificados");
    }

    private void initInstitutionalAccounts() {
        if (institutionalAccountRepository.findByAccountNumber("9000000001").isEmpty()) {
            InstitutionalAccount ingresos = new InstitutionalAccount();
            ingresos.setAccountNumber("9000000001");
            ingresos.setName("INGRESOS_SERVICIOS_MASIVOS");
            ingresos.setCode("MASS_SERVICE_INCOME");
            ingresos.setDescription("Cuenta institucional para registrar ingresos por servicios masivos");
            ingresos.setAccountingBalance(BigDecimal.ZERO);
            ingresos.setBalance(BigDecimal.ZERO);
            ingresos.setStatus(CommonStatusEnum.ACTIVO);
            ingresos.setCreationDate(LocalDateTime.now());
            institutionalAccountRepository.save(ingresos);
        }

        if (institutionalAccountRepository.findByAccountNumber("9000000002").isEmpty()) {
            InstitutionalAccount iva = new InstitutionalAccount();
            iva.setAccountNumber("9000000002");
            iva.setName("PASIVOS_IVA_RETENIDO");
            iva.setCode("VAT_PAYABLE");
            iva.setDescription("Cuenta institucional para registrar IVA retenido");
            iva.setAccountingBalance(BigDecimal.ZERO);
            iva.setBalance(BigDecimal.ZERO);
            iva.setStatus(CommonStatusEnum.ACTIVO);
            iva.setCreationDate(LocalDateTime.now());
            institutionalAccountRepository.save(iva);
        }

        log.info("InstitutionalAccounts creadas o verificadas");
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

    private void initMassiveCustomers() {
        CustomerSubtype personal = customerSubtypeRepository.findAll().stream()
                .filter(s -> "PERSONAL".equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Subtype PERSONAL no encontrado"));

        CustomerSubtype empresaPagosMasivosSubtype = customerSubtypeRepository.findAll().stream()
                .filter(s -> "EMPRESA_PAGOS_MASIVOS".equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Subtype EMPRESA_PAGOS_MASIVOS no encontrado"));

        String[] nombres = {
                "Juan", "Maria", "Carlos", "Ana", "Luis", "Gabriela", "Pedro", "Daniela",
                "Jorge", "Paola", "Andres", "Camila", "Diego", "Valeria", "Fernando",
                "Sofia", "Mateo", "Carolina", "Ricardo", "Isabel", "Sebastian", "Diana",
                "Esteban", "Fernanda", "Cristian", "Andrea", "Mauricio", "Karla"
        };

        String[] apellidos = {
                "Perez", "Garcia", "Morales", "Vera", "Cevallos", "Mendoza", "Castro",
                "Zambrano", "Rojas", "Sanchez", "Ortiz", "Torres", "Salazar", "Guerrero",
                "Navarrete", "Paredes", "Espinoza", "Romero", "Alvarez", "Delgado",
                "Molina", "Quintero", "Benitez", "Cabrera", "Vargas", "Acosta"
        };

        long naturalCount = customerRepository.findAll().stream()
                .filter(c -> CustomerTypeEnum.NATURAL.equals(c.getCustomerType()))
                .count();

        int naturalIndex = 1;
        while (naturalCount < 500) {
            String cedula = generateEcuadorianCedula(naturalIndex);

            if (customerRepository.findByIdentificationTypeAndIdentification("CEDULA", cedula).isEmpty()) {
                String nombre = nombres[naturalIndex % nombres.length];
                String apellidoPaterno = apellidos[naturalIndex % apellidos.length];
                String apellidoMaterno = apellidos[(naturalIndex + 9) % apellidos.length];
                String apellidosCompletos = apellidoPaterno + " " + apellidoMaterno;

                Customer customer = new Customer();
                customer.setCustomerSubtype(personal);
                customer.setCustomerType(CustomerTypeEnum.NATURAL);
                customer.setIdentificationType("CEDULA");
                customer.setIdentification(cedula);
                customer.setFirstName(nombre);
                customer.setLastName(apellidosCompletos);
                customer.setBirthDate(generateAdultBirthDate(naturalIndex));
                customer.setEmail(
                        nombre.toLowerCase() + "."
                                + apellidoPaterno.toLowerCase() + "."
                                + apellidoMaterno.toLowerCase()
                                + naturalIndex + "@banquito.com"
                );
                customer.setMobilePhone("09" + String.format("%08d", naturalIndex));
                customer.setAddress("Quito, sector " + ((naturalIndex % 5) + 1));
                customer.setStatus(CustomerStatusEnum.ACTIVO);

                customerRepository.save(customer);
                naturalCount++;
            }

            naturalIndex++;
        }

        List<Customer> naturalRepresentatives = customerRepository.findAll().stream()
                .filter(c -> CustomerTypeEnum.NATURAL.equals(c.getCustomerType()))
                .toList();

        if (naturalRepresentatives.isEmpty()) {
            throw new IllegalStateException("No existen clientes naturales para asignar representantes legales");
        }

        String[] nombresEmpresas = {
                "Andes", "Pacifico", "Equinoccio", "Pichincha", "Amazonas",
                "Sierra", "Condor", "Galapagos", "Capital", "Libertad"
        };

        String[] actividadesEmpresas = {
                "Servicios Corporativos", "Soluciones Financieras", "Comercializadora",
                "Logistica Empresarial", "Consultoria Integral", "Tecnologia Aplicada",
                "Gestion Administrativa", "Servicios Industriales"
        };

        long corporateCount = customerRepository.findAll().stream()
                .filter(c -> CustomerTypeEnum.JURIDICO.equals(c.getCustomerType()))
                .count();

        int corporateIndex = 1;
        while (corporateCount < 50) {
            String ruc = generateCompanyRuc(corporateIndex);

            if (customerRepository.findByIdentificationTypeAndIdentification("RUC", ruc).isEmpty()) {
                String legalName = nombresEmpresas[corporateIndex % nombresEmpresas.length] + " "
                        + actividadesEmpresas[corporateIndex % actividadesEmpresas.length] + " "
                        + String.format("%03d", corporateIndex) + " S.A.";

                Customer company = new Customer();
                company.setCustomerSubtype(empresaPagosMasivosSubtype);
                company.setCustomerType(CustomerTypeEnum.JURIDICO);
                company.setIdentificationType("RUC");
                company.setIdentification(ruc);
                company.setLegalName(legalName);
                company.setConstitutionDate(LocalDate.of(2000, 1, 1).plusDays(corporateIndex * 93L));
                company.setLegalRepresentative(
                        naturalRepresentatives.get(corporateIndex % naturalRepresentatives.size())
                );
                company.setEmail("contacto.empresa" + corporateIndex + "@banquito.com");
                company.setMobilePhone("02" + String.format("%07d", corporateIndex));
                company.setAddress("Quito, oficina corporativa " + corporateIndex);
                company.setStatus(CustomerStatusEnum.ACTIVO);

                customerRepository.save(company);
                corporateCount++;
            }

            corporateIndex++;
        }

        log.info("Clientes masivos creados o verificados");
    }

    private void initMassiveAccounts() {
        if (accountRepository.count() >= 1500) {
            log.info("Cuentas masivas ya existen");
            return;
        }

        List<Branch> branches = branchRepository.findAll();
        if (branches.isEmpty()) {
            throw new IllegalStateException("No existen sucursales para crear cuentas");
        }

        AccountSubtype ahorros = accountSubtypeRepository.findAll().stream()
                .filter(s -> "AHO".equals(s.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Subtipo AHO no encontrado"));

        AccountSubtype corriente = accountSubtypeRepository.findAll().stream()
                .filter(s -> "CTE".equals(s.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Subtipo CTE no encontrado"));

        List<Customer> naturalCustomers = customerRepository.findAll().stream()
                .filter(c -> CustomerTypeEnum.NATURAL.equals(c.getCustomerType()))
                .toList();

        List<Customer> corporateCustomers = customerRepository.findAll().stream()
                .filter(c -> CustomerTypeEnum.JURIDICO.equals(c.getCustomerType()))
                .toList();

        int accountSequence = 1;

        for (int i = 0; i < naturalCustomers.size(); i++) {
            Customer customer = naturalCustomers.get(i);
            List<Account> customerAccounts = accountRepository.findByCustomer_Id(customer.getId());

            if (customerAccounts.isEmpty()) {
                Branch branch = branches.get(i % branches.size());
                createSeedAccount(customer, branch, ahorros, accountSequence++);
            }
        }

        int customersWithTwoAccounts = Math.min(100, naturalCustomers.size());

        for (int i = 0; i < customersWithTwoAccounts; i++) {
            Customer customer = naturalCustomers.get(i);
            List<Account> customerAccounts = accountRepository.findByCustomer_Id(customer.getId());

            if (customerAccounts.size() < 2) {
                Branch branch = branches.get(i % branches.size());
                createSeedAccount(customer, branch, corriente, accountSequence++);
            }
        }

        for (int i = 0; i < corporateCustomers.size(); i++) {
            Customer company = corporateCustomers.get(i);

            while (accountRepository.findByCustomer_Id(company.getId()).size() < 3) {
                Branch branch = branches.get(i % branches.size());
                AccountSubtype subtype = accountRepository.findByCustomer_Id(company.getId()).size() % 2 == 0
                        ? corriente
                        : ahorros;

                createSeedAccount(company, branch, subtype, accountSequence++);
            }
        }

        int corporateIndex = 0;

        while (accountRepository.count() < 1500) {
            Customer company = corporateCustomers.get(corporateIndex % corporateCustomers.size());
            Branch branch = branches.get(corporateIndex % branches.size());
            AccountSubtype subtype = corporateIndex % 2 == 0 ? ahorros : corriente;

            createSeedAccount(company, branch, subtype, accountSequence++);
            corporateIndex++;
        }

        log.info("Cuentas masivas creadas o verificadas");
    }

    private void createSeedAccount(Customer customer, Branch branch, AccountSubtype subtype, int sequence) {
        String accountNumber = generateSeedAccountNumber(branch, sequence);

        while (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            sequence++;
            accountNumber = generateSeedAccountNumber(branch, sequence);
        }

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomer(customer);
        account.setBranch(branch);
        account.setAccountSubtype(subtype);
        account.setStatus(AccountStatusEnum.ACTIVO);
        account.setAccountingBalance(new BigDecimal("1000.00"));
        account.setAvailableBalance(new BigDecimal("1000.00"));
        account.setIsFavorite(false);
        account.setOpeningDate(LocalDateTime.now());
        account.setLastUpdate(LocalDateTime.now());

        accountRepository.save(account);
    }

    private String generateSeedAccountNumber(Branch branch, int sequence) {
        return branch.getBranchCode() + "-" + String.format("%07d", sequence);
    }

    private String generateEcuadorianCedula(int index) {
        int province = (index % 24) + 1;
        String provinceCode = String.format("%02d", province);

        int sequence = 100000 + index;
        String base = provinceCode + String.format("%06d", sequence).substring(0, 6) + "0";

        int[] coefficients = {2, 1, 2, 1, 2, 1, 2, 1, 2};
        int total = 0;

        for (int i = 0; i < coefficients.length; i++) {
            int value = Character.getNumericValue(base.charAt(i)) * coefficients[i];
            if (value >= 10) {
                value -= 9;
            }
            total += value;
        }

        int verifier = total % 10 == 0 ? 0 : 10 - (total % 10);
        return base + verifier;
    }

    private LocalDate generateAdultBirthDate(int index) {
        int year = 1970 + (index % 35);
        int month = (index % 12) + 1;
        int day = (index % 28) + 1;

        return LocalDate.of(year, month, day);
    }

    private String generateCompanyRuc(int index) {
        return "179" + String.format("%07d", index) + "001";
    }
}