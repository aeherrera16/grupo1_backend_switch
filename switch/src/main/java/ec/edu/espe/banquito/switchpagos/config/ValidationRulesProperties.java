package ec.edu.espe.banquito.switchpagos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.validation")
@Validated
public class ValidationRulesProperties {

    @Min(1)
    @Max(365)
    private int duplicateWindowDays = 30;

    @DecimalMin(value = "0.01")
    private BigDecimal maxDetailAmount = new BigDecimal("5000.00");

    public int getDuplicateWindowDays() {
        return duplicateWindowDays;
    }

    public void setDuplicateWindowDays(int duplicateWindowDays) {
        this.duplicateWindowDays = duplicateWindowDays;
    }

    public BigDecimal getMaxDetailAmount() {
        return maxDetailAmount;
    }

    public void setMaxDetailAmount(BigDecimal maxDetailAmount) {
        this.maxDetailAmount = maxDetailAmount;
    }
}
