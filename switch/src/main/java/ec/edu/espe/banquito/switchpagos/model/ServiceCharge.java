package ec.edu.espe.banquito.switchpagos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import ec.edu.espe.banquito.switchpagos.enums.ChargeStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "SERVICE_CHARGE")
public class ServiceCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Version
    @Column(name = "version")
    private Integer version;

    // Relación hacia el Lote (Cabecera)
    @ManyToOne
    @JoinColumn(name = "payment_batch_id", referencedColumnName = "id", nullable = false)
    private PaymentBatch paymentBatch;

    // Relación hacia la Regla Tarifaria aplicada
    @ManyToOne
    @JoinColumn(name = "service_fee_rule_id", referencedColumnName = "id", nullable = false)
    private ServiceFeeRule serviceFeeRule;

    @Column(name = "successful_transactions", nullable = false)
    private Integer successfulTransactions;

    // Regla 3: Valores financieros
    @Column(name = "unit_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitFee;

    @Column(name = "commission_subtotal", nullable = false, precision = 18, scale = 2)
    private BigDecimal commissionSubtotal;

    @Column(name = "fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "vat_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "iva_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal ivaAmount;

    @Column(name = "total_charge", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCharge;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "charge_status", nullable = false, length = 30)
    private ChargeStatusEnum chargeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ChargeStatusEnum status;

    @Column(name = "charged_at")
    private LocalDateTime chargedAt;

    public ServiceCharge() {
    }

    public ServiceCharge(Integer id) {
        this.id = id;
    }

    // --- GETTERS Y SETTERS MANUALES ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public PaymentBatch getPaymentBatch() {
        return paymentBatch;
    }

    public void setPaymentBatch(PaymentBatch paymentBatch) {
        this.paymentBatch = paymentBatch;
    }

    public ServiceFeeRule getServiceFeeRule() {
        return serviceFeeRule;
    }

    public void setServiceFeeRule(ServiceFeeRule serviceFeeRule) {
        this.serviceFeeRule = serviceFeeRule;
    }

    public Integer getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(Integer successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public BigDecimal getUnitFee() {
        return unitFee;
    }

    public void setUnitFee(BigDecimal unitFee) {
        this.unitFee = unitFee;
    }

    public BigDecimal getCommissionSubtotal() {
        return commissionSubtotal;
    }

    public void setCommissionSubtotal(BigDecimal commissionSubtotal) {
        this.commissionSubtotal = commissionSubtotal;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getIvaAmount() {
        return ivaAmount;
    }

    public void setIvaAmount(BigDecimal ivaAmount) {
        this.ivaAmount = ivaAmount;
    }

    public BigDecimal getTotalCharge() {
        return totalCharge;
    }

    public void setTotalCharge(BigDecimal totalCharge) {
        this.totalCharge = totalCharge;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public ChargeStatusEnum getChargeStatus() {
        return chargeStatus;
    }

    public void setChargeStatus(ChargeStatusEnum chargeStatus) {
        this.chargeStatus = chargeStatus;
    }

    public ChargeStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ChargeStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getChargedAt() {
        return chargedAt;
    }

    public void setChargedAt(LocalDateTime chargedAt) {
        this.chargedAt = chargedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceCharge that = (ServiceCharge) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ServiceCharge{" +
                "id=" + id +
                ", successfulTransactions=" + successfulTransactions +
                ", totalCharge=" + totalCharge +
                ", chargeStatus='" + chargeStatus + '\'' +
                '}';
    }
}
