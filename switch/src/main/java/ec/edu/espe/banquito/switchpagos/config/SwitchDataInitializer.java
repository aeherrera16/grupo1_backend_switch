package ec.edu.espe.banquito.switchpagos.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ec.edu.espe.banquito.switchpagos.model.ServiceFeeRule;
import ec.edu.espe.banquito.switchpagos.repository.ServiceFeeRuleRepository;

@Component
public class SwitchDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SwitchDataInitializer.class);

    private final ServiceFeeRuleRepository serviceFeeRuleRepository;

    public SwitchDataInitializer(ServiceFeeRuleRepository serviceFeeRuleRepository) {
        this.serviceFeeRuleRepository = serviceFeeRuleRepository;
    }

    @Override
    public void run(String... args) {
        upsertFeeRule(1, 10, "0.50");
        upsertFeeRule(11, 100, "0.40");
        upsertFeeRule(101, 500, "0.30");
        upsertFeeRule(501, 1000, "0.20");
        upsertFeeRule(1001, 10000, "0.10");
        upsertFeeRule(10001, null, "0.05");

        logger.info("Reglas tarifarias SERVICE_FEE_RULE verificadas correctamente");
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

    private Optional<ServiceFeeRule> findExistingRule(BigDecimal fee) {
        List<ServiceFeeRule> rules = serviceFeeRuleRepository.findAll();
        return rules.stream()
                .filter(rule -> "PAGOS_MASIVOS".equals(rule.getServiceType()))
                .filter(rule -> "UNIT_FEE".equals(rule.getFeeType()))
                .filter(rule -> rule.getFeeAmount() != null && rule.getFeeAmount().compareTo(fee) == 0)
                .findFirst();
    }
}
