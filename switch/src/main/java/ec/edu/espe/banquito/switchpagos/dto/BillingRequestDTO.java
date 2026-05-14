package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;

public class BillingRequestDTO {
    private String customerId;
    private String serviceCode;
    private BigDecimal amount;
    private String reference;

    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    public String getServiceCode() {
        return serviceCode;
    }
    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    @Override
    public String toString() {
        return "BillingRequestDTO{" +
                "customerId='" + customerId + '\'' +
                ", serviceCode='" + serviceCode + '\'' +
                ", amount=" + amount +
                ", reference='" + reference + '\'' +
                '}';
    }
}
