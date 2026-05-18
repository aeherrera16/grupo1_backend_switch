package ec.edu.espe.banquito.switchpagos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "SERVICE_FEE_RULE")
public class ServiceFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "service_type", nullable = false, length = 50)
    private String serviceType;

    @Column(name = "fee_type", nullable = false, length = 50)
    private String feeType;

    @Column(name = "min_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "min_successful_transactions", nullable = false)
    private Integer minSuccessfulTransactions;

    @Column(name = "max_successful_transactions")
    private Integer maxSuccessfulTransactions;

    @Column(name = "unit_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitFee;

    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeAmount;

    public ServiceFeeRule() {
    }

    public ServiceFeeRule(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Integer getMinSuccessfulTransactions() {
        return minSuccessfulTransactions;
    }

    public void setMinSuccessfulTransactions(Integer minSuccessfulTransactions) {
        this.minSuccessfulTransactions = minSuccessfulTransactions;
    }

    public Integer getMaxSuccessfulTransactions() {
        return maxSuccessfulTransactions;
    }

    public void setMaxSuccessfulTransactions(Integer maxSuccessfulTransactions) {
        this.maxSuccessfulTransactions = maxSuccessfulTransactions;
    }

    public BigDecimal getUnitFee() {
        return unitFee;
    }

    public void setUnitFee(BigDecimal unitFee) {
        this.unitFee = unitFee;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceFeeRule that = (ServiceFeeRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ServiceFeeRule{" +
                "id=" + id +
                ", serviceType='" + serviceType + '\'' +
                ", feeType='" + feeType + '\'' +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", unitFee=" + unitFee +
                ", feeAmount=" + feeAmount +
                '}';
    }
}
