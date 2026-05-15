package com.banquito.core.model;

import com.banquito.core.enums.CommonStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "INSTITUTIONAL_ACCOUNT")
public class InstitutionalAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "ACCOUNT_NUMBER", nullable = false, length = 20, unique = true)
    private String accountNumber;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false, length = 150)
    private String description;

    @Column(name = "ACCOUNTING_BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal accountingBalance;

    @Column(name = "BALANCE", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private CommonStatusEnum status;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public InstitutionalAccount() {
    }

    public InstitutionalAccount(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstitutionalAccount that = (InstitutionalAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "InstitutionalAccount{" +
                "id=" + id +
                ", accountNumber='" + accountNumber + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
