package com.banquito.core.model;

import com.banquito.core.enums.AccountStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "ACCOUNT_NUMBER", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "BRANCH_ID", nullable = false)
    private Branch branch;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_SUBTYPE_ID", nullable = false)
    private AccountSubtype accountSubtype;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private AccountStatusEnum status;

    @Column(name = "ACCOUNTING_BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal accountingBalance;

    @Column(name = "AVAILABLE_BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "IS_FAVORITE", nullable = false)
    private Boolean isFavorite;

    @Column(name = "OPENING_DATE", nullable = false)
    private LocalDateTime openingDate;

    @Column(name = "LAST_UPDATE")
    private LocalDateTime lastUpdate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Account() {}

    public Account(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", status=" + status +
                ", availableBalance=" + availableBalance +
                '}';
    }
}
