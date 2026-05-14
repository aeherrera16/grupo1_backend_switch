package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;

public class PaymentResponseDTO {
    private String paymentId;
    private String status;
    private String message;
    private BigDecimal amount;

    public String getPaymentId() {
        return paymentId;
    }
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    @Override
    public String toString() {
        return "PaymentResponseDTO{" +
                "paymentId='" + paymentId + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", amount=" + amount +
                '}';
    }
}
