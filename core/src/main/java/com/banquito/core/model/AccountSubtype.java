package com.banquito.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "ACCOUNT_SUBTYPE")
public class AccountSubtype {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "SUPER_TYPE", nullable = false, length = 15)
    private String superType;

    @Column(name = "CODE", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private com.banquito.core.enums.CommonStatusEnum status;

    @Column(name = "OBSERVATIONS", length = 255)
    private String observations;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public AccountSubtype() {}

    public AccountSubtype(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountSubtype)) return false;
        AccountSubtype that = (AccountSubtype) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AccountSubtype{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
