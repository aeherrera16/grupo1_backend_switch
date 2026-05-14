package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BatchSummaryDTO {

    private Integer batchId;
    private String fileName;
    private String ruc;
    private String status;
    private Integer totalRecords;
    private BigDecimal totalAmount;
    private Integer successfulRecords;
    private Integer rejectedRecords;
    private LocalDateTime receivedAt;
    private BigDecimal commissionSubtotal;
    private BigDecimal vatAmount;
    private BigDecimal totalCharge;
    private String chargeStatus;
    private LocalDateTime chargedAt;

    public BatchSummaryDTO() {
    }

    // --- GETTERS Y SETTERS MANUALES ---

    public Integer getBatchId() {
        return batchId;
    }

    public void setBatchId(Integer batchId) {
        this.batchId = batchId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getSuccessfulRecords() {
        return successfulRecords;
    }

    public void setSuccessfulRecords(Integer successfulRecords) {
        this.successfulRecords = successfulRecords;
    }

    public Integer getRejectedRecords() {
        return rejectedRecords;
    }

    public void setRejectedRecords(Integer rejectedRecords) {
        this.rejectedRecords = rejectedRecords;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public BigDecimal getCommissionSubtotal() {
        return commissionSubtotal;
    }

    public void setCommissionSubtotal(BigDecimal commissionSubtotal) {
        this.commissionSubtotal = commissionSubtotal;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(BigDecimal totalCharge) {
        this.totalCharge = totalCharge;
    }

    public String getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(String chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public LocalDateTime getChargedAt() {
        return chargedAt;
    }

    public void setChargedAt(LocalDateTime chargedAt) {
        this.chargedAt = chargedAt;
    }

    @Override
    public String toString() {
        return "BatchSummaryDTO{" +
                "batchId=" + batchId +
                ", fileName='" + fileName + '\'' +
                ", status='" + status + '\'' +
                ", successfulRecords=" + successfulRecords +
                ", rejectedRecords=" + rejectedRecords +
                ", totalCharge=" + totalCharge +
                ", chargeStatus='" + chargeStatus + '\'' +
                '}';
    }
}
