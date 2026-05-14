package com.banquito.core.model;

import com.banquito.core.enums.MovementTypeEnum;
import com.banquito.core.enums.TransactionStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "ACCOUNT_TRANSACTION")
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "TRANSACTION_SUBTYPE_ID", nullable = false)
    private TransactionSubtype transactionSubtype;

    @Column(name = "TRANSACTION_UUID", nullable = false, length = 36)
    private String transactionUuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "MOVEMENT_TYPE", nullable = false, length = 15)
    private MovementTypeEnum movementType;

    @Column(name = "AMOUNT", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "RESULTING_BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal resultingBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private TransactionStatusEnum status;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    private LocalDateTime transactionDate;

    public AccountTransaction() {}

    public AccountTransaction(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountTransaction that = (AccountTransaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AccountTransaction{" +
                "id=" + id +
                ", transactionUuid='" + transactionUuid + '\'' +
                ", movementType=" + movementType +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
