package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;

public class TransferRequestDTO {
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String reference;

    public String getFromAccountId() {
        return fromAccountId;
    }
    public void setFromAccountId(String fromAccountId) {
        this.fromAccountId = fromAccountId;
    }
    public String getToAccountId() {
        return toAccountId;
    }
    public void setToAccountId(String toAccountId) {
        this.toAccountId = toAccountId;
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
        return "TransferRequestDTO{" +
                "fromAccountId='" + fromAccountId + '\'' +
                ", toAccountId='" + toAccountId + '\'' +
                ", amount=" + amount +
                ", reference='" + reference + '\'' +
                '}';
    }
}
