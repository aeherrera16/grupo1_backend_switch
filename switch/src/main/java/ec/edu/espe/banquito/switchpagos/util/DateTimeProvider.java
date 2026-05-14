package ec.edu.espe.banquito.switchpagos.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Proporciona acceso a la fecha y hora actual.
 * Permite inyección de dependencias y testing sin afectar tiempo real.
 * 
 * En producción: retorna tiempo real
 * En testing/demo: puede ser mockeado para simular diferentes horas
 */
@Component
public class DateTimeProvider {

    @Value("${app.time.mode:real}")
    private String timeMode;

    @Value("${app.time.fixed-datetime:}")
    private String fixedDateTime;

    @Value("${app.time.zone:America/Guayaquil}")
    private String timeZone;

    /**
     * Retorna la fecha y hora actual
     */
    public LocalDateTime now() {
        if (isFixedTimeEnabled()) {
            return LocalDateTime.parse(fixedDateTime);
        }

        return ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDateTime();
    }

    /**
     * Retorna la fecha actual
     */
    public LocalDate today() {
        return now().toLocalDate();
    }

    /**
     * Retorna la hora actual
     */
    public LocalTime currentTime() {
        return now().toLocalTime();
    }

    private boolean isFixedTimeEnabled() {
        return "fixed".equalsIgnoreCase(timeMode) && fixedDateTime != null && !fixedDateTime.isBlank();
    }
}
