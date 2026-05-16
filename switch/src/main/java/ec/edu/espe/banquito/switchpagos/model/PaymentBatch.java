package ec.edu.espe.banquito.switchpagos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "PAYMENT_BATCH")
public class PaymentBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_hash", length = 255)
    private String fileHash;

    @Column(name = "ruc", length = 20)
    private String ruc;

    @Column(name = "source_account_number", length = 30)
    private String sourceAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 50)
    private ec.edu.espe.banquito.switchpagos.enums.ChannelEnum channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 50)
    private ec.edu.espe.banquito.switchpagos.enums.ServiceTypeEnum serviceType;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private BatchStatusEnum status;

    @Column(name = "header_total_records")
    private Integer headerTotalRecords;

    // Use BigDecimal for money.
    @Column(name = "header_total_amount", precision = 18, scale = 2)
    private BigDecimal headerTotalAmount;

    @Column(name = "successful_records")
    private Integer successfulRecords;

    @Column(name = "rejected_records")
    private Integer rejectedRecords;

    // JPA constructor.
    public PaymentBatch() {
    }

    // PK constructor.
    public PaymentBatch(Integer id) {
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public ec.edu.espe.banquito.switchpagos.enums.ChannelEnum getChannel() {
        return channel;
    }

    public void setChannel(ec.edu.espe.banquito.switchpagos.enums.ChannelEnum channel) {
        this.channel = channel;
    }

    public ec.edu.espe.banquito.switchpagos.enums.ServiceTypeEnum getServiceType() {
        return serviceType;
    }

    public void setServiceType(ec.edu.espe.banquito.switchpagos.enums.ServiceTypeEnum serviceType) {
        this.serviceType = serviceType;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public BatchStatusEnum getStatus() {
        return status;
    }

    public void setStatus(BatchStatusEnum status) {
        this.status = status;
    }

    public Integer getHeaderTotalRecords() {
        return headerTotalRecords;
    }

    public void setHeaderTotalRecords(Integer headerTotalRecords) {
        this.headerTotalRecords = headerTotalRecords;
    }

    public BigDecimal getHeaderTotalAmount() {
        return headerTotalAmount;
    }

    public void setHeaderTotalAmount(BigDecimal headerTotalAmount) {
        this.headerTotalAmount = headerTotalAmount;
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

    // Equality by PK.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentBatch that = (PaymentBatch) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // Debug output.
    @Override
    public String toString() {
        return "PaymentBatch{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", ruc='" + ruc + '\'' +
                ", status='" + status + '\'' +
                ", headerTotalAmount=" + headerTotalAmount +
                '}';
    }
}
