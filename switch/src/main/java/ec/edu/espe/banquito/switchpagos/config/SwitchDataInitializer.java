package ec.edu.espe.banquito.switchpagos.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import ec.edu.espe.banquito.switchpagos.model.ServiceFeeRule;
import ec.edu.espe.banquito.switchpagos.model.SwitchParameter;
import ec.edu.espe.banquito.switchpagos.repository.ServiceFeeRuleRepository;
import ec.edu.espe.banquito.switchpagos.repository.SwitchParameterRepository;

@Component
public class SwitchDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SwitchDataInitializer.class);

    private final ServiceFeeRuleRepository serviceFeeRuleRepository;
    private final SwitchParameterRepository switchParameterRepository;
    private final JdbcTemplate jdbcTemplate;

    public SwitchDataInitializer(ServiceFeeRuleRepository serviceFeeRuleRepository,
                                 SwitchParameterRepository switchParameterRepository,
                                 JdbcTemplate jdbcTemplate) {
        this.serviceFeeRuleRepository = serviceFeeRuleRepository;
        this.switchParameterRepository = switchParameterRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureServiceFeeRuleSchema();

        upsertFeeRule(1, 10, "0.50");
        upsertFeeRule(11, 100, "0.40");
        upsertFeeRule(101, 500, "0.30");
        upsertFeeRule(501, 1000, "0.20");
        upsertFeeRule(1001, 10000, "0.10");
        upsertFeeRule(10001, null, "0.05");
        upsertSwitchParameter("EMPRESA_ACCOUNT", "0050000202", "Cuenta matriz empresarial para cargos de pagos masivos");

        logger.info("Reglas tarifarias SERVICE_FEE_RULE verificadas correctamente");
    }

    private void ensureServiceFeeRuleSchema() {
        jdbcTemplate.execute("ALTER TABLE service_fee_rule ADD COLUMN IF NOT EXISTS fee_amount NUMERIC(18,2)");
        jdbcTemplate.update("UPDATE service_fee_rule SET fee_amount = unit_fee WHERE fee_amount IS NULL");
        jdbcTemplate.execute("ALTER TABLE service_fee_rule ALTER COLUMN fee_amount SET NOT NULL");
    }

    private void upsertFeeRule(Integer min, Integer max, String unitFee) {
        BigDecimal fee = new BigDecimal(unitFee);
        Optional<ServiceFeeRule> existing = findExistingRule(fee);
        ServiceFeeRule rule = existing.orElseGet(ServiceFeeRule::new);
        rule.setServiceType("PAGOS_MASIVOS");
        rule.setFeeType("UNIT_FEE");
        rule.setMinAmount(BigDecimal.valueOf(min));
        rule.setMaxAmount(max != null ? BigDecimal.valueOf(max) : null);
        rule.setMinSuccessfulTransactions(min);
        rule.setMaxSuccessfulTransactions(max);
        rule.setUnitFee(fee);
        rule.setFeeAmount(fee);
        serviceFeeRuleRepository.save(rule);
    }

    private void upsertSwitchParameter(String code, String value, String description) {
        SwitchParameter parameter = switchParameterRepository.findById(code).orElseGet(() -> new SwitchParameter(code));
        parameter.setName(code);
        parameter.setValueString(value);
        parameter.setDataType("STRING");
        parameter.setDescription(description);
        parameter.setLastUpdate(LocalDateTime.now());
        switchParameterRepository.save(parameter);
    }

    private Optional<ServiceFeeRule> findExistingRule(BigDecimal fee) {
        List<ServiceFeeRule> rules = serviceFeeRuleRepository.findAll();
        return rules.stream()
                .filter(rule -> "PAGOS_MASIVOS".equals(rule.getServiceType()))
                .filter(rule -> "UNIT_FEE".equals(rule.getFeeType()))
                .filter(rule -> rule.getFeeAmount() != null && rule.getFeeAmount().compareTo(fee) == 0)
                .findFirst();
    }
}
