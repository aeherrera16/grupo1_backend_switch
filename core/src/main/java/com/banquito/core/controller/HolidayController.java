package com.banquito.core.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.core.repository.HolidayRepository;

/**
 * Endpoints de calendario laboral basados en la tabla HOLIDAY del core.
 */
@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayRepository holidayRepository;

    public HolidayController(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    /**
     * Retorna si una fecha es día hábil.
     *
     * Regla:
     * - No hábil si es sábado/domingo
     * - No hábil si existe en la tabla HOLIDAY (feriado o registro especial)
     */
    @GetMapping("/is-business-day")
    public Map<String, Object> isBusinessDay(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean holiday = holidayRepository.findByHolidayDate(date).isPresent();
        boolean businessDay = !(weekend || holiday);

        return Map.of(
                "date", date.toString(),
                "businessDay", businessDay,
                "weekend", weekend,
                "holiday", holiday
        );
    }
}
