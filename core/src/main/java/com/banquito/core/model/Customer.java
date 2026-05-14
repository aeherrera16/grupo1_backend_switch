package com.banquito.core.model;

import com.banquito.core.enums.CustomerStatusEnum;
import com.banquito.core.enums.CustomerTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(
        name = "CUSTOMER",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"IDENTIFICATION_TYPE", "IDENTIFICATION"})
        }
)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_SUBTYPE_ID", nullable = false)
    private CustomerSubtype customerSubtype;

    @Enumerated(EnumType.STRING)
    @Column(name = "CUSTOMER_TYPE", nullable = false, length = 15)
    private CustomerTypeEnum customerType;

    @Column(name = "IDENTIFICATION_TYPE", nullable = false, length = 15)
    private String identificationType;

    @Column(name = "IDENTIFICATION", nullable = false, length = 20)
    private String identification;

    @Column(name = "FIRST_NAME", length = 100)
    private String firstName;

    @Column(name = "LAST_NAME", length = 100)
    private String lastName;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "LEGAL_NAME", length = 150)
    private String legalName;

    @Column(name = "CONSTITUTION_DATE")
    private LocalDate constitutionDate;

    @ManyToOne
    @JoinColumn(name = "LEGAL_REPRESENTATIVE_ID")
    private Customer legalRepresentative;

    @Column(name = "EMAIL", nullable = false, length = 100)
    private String email;

    @Column(name = "MOBILE_PHONE", nullable = false, length = 20)
    private String mobilePhone;

    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    @Column(name = "LATITUDE", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "LONGITUDE", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 15)
    private CustomerStatusEnum status;

    @Column(name = "REGISTRATION_DATE")
    private LocalDateTime registrationDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Customer() {}

    public Customer(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customerType=" + customerType +
                ", identification='" + identification + '\'' +
                ", status=" + status +
                '}';
    }
}
