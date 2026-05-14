package com.banquito.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "BRANCH")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "BRANCH_CODE", nullable = false, length = 10, unique = true)
    private String branchCode;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "CITY", nullable = false, length = 50)
    private String city;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public Branch() {}

    public Branch(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(id, branch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", branchCode='" + branchCode + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
