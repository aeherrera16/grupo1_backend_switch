package com.banquito.core.model;

import com.banquito.core.enums.CustomerSubtypeStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "CUSTOMER_SUBTYPE")
public class CustomerSubtype {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "CUSTOMER_TYPE", nullable = false, length = 15)
    private String customerType;

    @Column(name = "NAME", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private CustomerSubtypeStatusEnum status;

    @Column(name = "OBSERVATIONS", length = 255)
    private String observations;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public CustomerSubtype() {}

    public CustomerSubtype(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerSubtype)) return false;
        CustomerSubtype that = (CustomerSubtype) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "CustomerSubtype{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
