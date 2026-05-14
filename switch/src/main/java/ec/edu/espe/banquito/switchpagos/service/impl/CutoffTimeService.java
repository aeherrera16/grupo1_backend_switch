
package ec.edu.espe.banquito.switchpagos.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.service.ICutoffTimeService;
import ec.edu.espe.banquito.switchpagos.util.DateTimeProvider;

/**
 * Servicio para manejar la lógica de horarios de corte.
 */
@Service
public class CutoffTimeService implements ICutoffTimeService {

    private static final Logger logger = LoggerFactory.getLogger(CutoffTimeService.class);

    @Value("${app.ingest.cutoff-hour:18}")
    private int cutoffHour;

    private final BusinessDayService businessDayService;
    private final DateTimeProvider dateTimeProvider;

    public CutoffTimeService(BusinessDayService businessDayService, DateTimeProvider dateTimeProvider) {
        this.businessDayService = businessDayService;
        this.dateTimeProvider = dateTimeProvider;
    }

    /**
     * Verifica si el tiempo actual está dentro de la ventana de ingesta.
     * @return true si está antes de la hora de corte, false otherwise
     */
    @Override
    public boolean isWithinIngestionWindow() {
        LocalTime now = dateTimeProvider.currentTime();
        LocalTime cutoff = LocalTime.of(cutoffHour, 0);
        return now.isBefore(cutoff);
    }

    @Override
    public LocalTime getCutoffTime() {
        return LocalTime.of(cutoffHour, 0);
    }

    /**
     * Verifica si un tiempo específico está dentro de la ventana de ingesta.
     * @param time tiempo a verificar
     * @return true si está antes de la hora de corte, false otherwise
     */
    @Override
    public boolean isWithinIngestionWindow(LocalTime time) {
        return time.isBefore(LocalTime.of(cutoffHour, 0));
    }

    /**
     * Retorna true si el lote debe ser encolado en lugar de procesado inmediatamente.
     * Se encola cuando: la hora actual es >= hora de corte, O el dia es fin de semana, O es feriado.
     */
    public boolean shouldQueue() {
        LocalDate today = dateTimeProvider.today();
        if (!isWithinIngestionWindow()) {
            logger.info("Hora actual fuera de ventana de ingesta (corte: {}). Lote sera encolado.", getCutoffTime());
            return true;
        }
        if (isWeekendOrHoliday(today)) {
            logger.info("Dia {} es fin de semana o feriado. Lote sera encolado.", today);
            return true;
        }
        return false;
    }

    public boolean isWeekendOrHoliday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            logger.info("Dia {} es fin de semana ({})", date, dow);
            return true;
        }
        boolean holiday = !businessDayService.isBusinessDay(date);
        if (holiday) {
            logger.info("Dia {} es feriado segun el core", date);
        }
        return holiday;
    }
}
