package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;

public class PaymentRequestDTO {
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String description;

    public String getPayerId() {
        return payerId;
    }
    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }
    public String getPayeeId() {
        return payeeId;
    }
    public void setPayeeId(String payeeId) {
        this.payeeId = payeeId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public String toString() {
        return "PaymentRequestDTO{" +
                "payerId='" + payerId + '\'' +
                ", payeeId='" + payeeId + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}
