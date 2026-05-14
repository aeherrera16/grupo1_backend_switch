package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;

public class TransferResponseDTO {
    private Boolean success;
    private String code;
    private String uuid;
    private String transferId;
    private String status;
    private String message;
    private BigDecimal amount;

    public String getTransferId() {
        return transferId;
    }
    public void setTransferId(String transferId) {
        this.transferId = transferId;
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
    public Boolean getSuccess() {
        return success;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    @Override
    public String toString() {
        return "TransferResponseDTO{" +
                "transferId='" + transferId + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", amount=" + amount +
                '}';
    }
}
