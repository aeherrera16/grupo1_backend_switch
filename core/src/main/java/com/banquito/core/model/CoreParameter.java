package com.banquito.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "CORE_PARAMETER")
public class CoreParameter {

    @Id
    @Column(name = "CODE", nullable = false, length = 30)
    private String code;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "VALUE_STRING", nullable = false, length = 260)
    private String valueString;

    @Column(name = "DATA_TYPE", nullable = false, length = 20)
    private String dataType;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;

    @Column(name = "LAST_UPDATE")
    private LocalDateTime lastUpdate;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    public CoreParameter() {
    }

    public CoreParameter(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoreParameter that = (CoreParameter) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }

    @Override
    public String toString() {
        return "CoreParameter{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", valueString='" + valueString + '\'' +
                '}';
    }
}
