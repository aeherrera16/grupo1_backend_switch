package ec.edu.espe.banquito.switchpagos.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "FILE_VALIDATION")
public class FileValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    // Regla 8: Relación de hijo a padre hacia el lote
    @ManyToOne
    @JoinColumn(name = "payment_batch_id", referencedColumnName = "id", nullable = false)
    private PaymentBatch paymentBatch;

    // Uso estricto de Wrappers (Boolean en vez de boolean)
    @Column(name = "totals_match")
    private Boolean totalsMatch;

    @Column(name = "customer_active_valid")
    private Boolean customerActiveValid;

    @Column(name = "duplicate_file_valid")
    private Boolean duplicateFileValid;

    @Column(name = "structure_valid")
    private Boolean structureValid;

    @Column(name = "validation_result", length = 50)
    private String validationResult;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    // Regla 6: Constructor vacío mandatorio para JPA
    public FileValidation() {
    }

    // Regla 6: Constructor con clave primaria
    public FileValidation(Integer id) {
        this.id = id;
    }

    // --- GETTERS Y SETTERS MANUALES ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PaymentBatch getPaymentBatch() {
        return paymentBatch;
    }

    public void setPaymentBatch(PaymentBatch paymentBatch) {
        this.paymentBatch = paymentBatch;
    }

    public Boolean getTotalsMatch() {
        return totalsMatch;
    }

    public void setTotalsMatch(Boolean totalsMatch) {
        this.totalsMatch = totalsMatch;
    }

    public Boolean getCustomerActiveValid() {
        return customerActiveValid;
    }

    public void setCustomerActiveValid(Boolean customerActiveValid) {
        this.customerActiveValid = customerActiveValid;
    }

    public Boolean getDuplicateFileValid() {
        return duplicateFileValid;
    }

    public void setDuplicateFileValid(Boolean duplicateFileValid) {
        this.duplicateFileValid = duplicateFileValid;
    }

    public Boolean getStructureValid() {
        return structureValid;
    }

    public void setStructureValid(Boolean structureValid) {
        this.structureValid = structureValid;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    // Regla 5: equals() y hashCode() SOLO de la PK
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileValidation that = (FileValidation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // Regla 7: Sobreescribir toString()
    @Override
    public String toString() {
        return "FileValidation{" +
                "id=" + id +
                ", totalsMatch=" + totalsMatch +
                ", duplicateFileValid=" + duplicateFileValid +
                ", validationResult='" + validationResult + '\'' +
                '}';
    }
}