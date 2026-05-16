package ec.edu.espe.banquito.switchpagos.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class SettlementSummaryDTO {
    private Integer successfulTransactions;

    private BigDecimal dispersedAmount;

    private BigDecimal commissionSubtotal;

    private BigDecimal vatAmount;

    private BigDecimal totalCharge;

    private BigDecimal unitFee;
    private BigDecimal feeAmount;
    private BigDecimal totalAmount;
    private Integer ruleId;
    private BigDecimal ivaAmount;
    // Accessors.

    public Integer getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(Integer successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public BigDecimal getDispersedAmount() {
        return dispersedAmount;
    }

    public void setDispersedAmount(BigDecimal dispersedAmount) {
        this.dispersedAmount = dispersedAmount;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public BigDecimal getIvaAmount() {
        return ivaAmount;
    }

    public void setIvaAmount(BigDecimal ivaAmount) {
        this.ivaAmount = ivaAmount;
    }
}
