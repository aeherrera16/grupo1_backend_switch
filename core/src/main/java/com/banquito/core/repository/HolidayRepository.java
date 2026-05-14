package com.banquito.core.repository;

import com.banquito.core.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {
    Optional<Holiday> findByHolidayDate(LocalDate holidayDate);
}
