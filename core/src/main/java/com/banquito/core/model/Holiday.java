package com.banquito.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "HOLIDAY")
public class Holiday {

    @Id
    @Column(name = "HOLIDAY_DATE", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "IS_WEEKEND", nullable = false)
    private Boolean isWeekend;

    @Column(name = "CREATION_DATE")
    private LocalDateTime creationDate;

    public Holiday() {
    }

    public Holiday(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Holiday holiday = (Holiday) o;
        return Objects.equals(holidayDate, holiday.holidayDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(holidayDate);
    }
}
