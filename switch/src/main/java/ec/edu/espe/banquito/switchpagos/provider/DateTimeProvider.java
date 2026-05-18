package ec.edu.espe.banquito.switchpagos.provider;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DateTimeProvider {

    @Value("${app.time.mode:real}")
    private String timeMode;

    @Value("${app.time.fixed-datetime:}")
    private String fixedDateTime;

    @Value("${app.time.zone:America/Guayaquil}")
    private String timeZone;

    public LocalDateTime now() {
        if (isFixedTimeEnabled()) {
            return LocalDateTime.parse(fixedDateTime);
        }

        return ZonedDateTime.now(ZoneId.of(timeZone)).toLocalDateTime();
    }

    public LocalDate today() {
        return now().toLocalDate();
    }

    public LocalTime currentTime() {
        return now().toLocalTime();
    }

    private boolean isFixedTimeEnabled() {
        return "fixed".equalsIgnoreCase(timeMode) && fixedDateTime != null && !fixedDateTime.isBlank();
    }
}
