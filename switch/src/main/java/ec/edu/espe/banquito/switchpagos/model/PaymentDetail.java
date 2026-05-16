package ec.edu.espe.banquito.switchpagos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
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
@Table(name = "PAYMENT_DETAIL")
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Version
    @Column(name = "version")
    private Integer version;

    // Child-to-parent relation.
    @ManyToOne
    @JoinColumn(name = "payment_batch_id", referencedColumnName = "id", nullable = false)
    private PaymentBatch paymentBatch;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "beneficiary_identification", nullable = false, length = 20)
    private String beneficiaryIdentification;

    @Column(name = "beneficiary_name", nullable = false, length = 255)
    private String beneficiaryName;

    @Column(name = "destination_account_number", nullable = false, length = 30)
    private String destinationAccountNumber;

    // Use BigDecimal for money.
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference", length = 255)
    private String reference;

    @Column(name = "beneficiary_email", length = 255)
    private String beneficiaryEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentDetailStatusEnum status;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "notification_status", length = 30)
    private String notificationStatus;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    // JPA constructor.
    public PaymentDetail() {
    }

    // PK constructor.
    public PaymentDetail(Integer id) {
        this.id = id;
    }

    // Accessors.

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

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getBeneficiaryIdentification() {
        return beneficiaryIdentification;
    }

    public void setBeneficiaryIdentification(String beneficiaryIdentification) {
        this.beneficiaryIdentification = beneficiaryIdentification;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
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

    public String getBeneficiaryEmail() {
        return beneficiaryEmail;
    }

    public void setBeneficiaryEmail(String beneficiaryEmail) {
        this.beneficiaryEmail = beneficiaryEmail;
    }

    public PaymentDetailStatusEnum getStatus() {
        return status;
    }

    public void setStatus(PaymentDetailStatusEnum status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    // Equality by PK.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentDetail that = (PaymentDetail) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // Debug output.
    @Override
    public String toString() {
        return "PaymentDetail{" +
                "id=" + id +
                ", lineNumber=" + lineNumber +
                ", destinationAccountNumber='" + destinationAccountNumber + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}
