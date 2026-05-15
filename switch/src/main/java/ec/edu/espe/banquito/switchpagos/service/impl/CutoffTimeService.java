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

    @Override
    public boolean isWithinIngestionWindow(LocalTime time) {
        return time.isBefore(LocalTime.of(cutoffHour, 0));
    }

    // RF-01 queue rule: after cutoff, weekend, or holiday.
    public boolean shouldQueue() {
        LocalDate today = dateTimeProvider.today();
        if (!isWithinIngestionWindow()) {
            logger.info("Current time is outside ingestion window (cutoff: {}). Batch will be queued.", getCutoffTime());
            return true;
        }
        if (isWeekendOrHoliday(today)) {
            logger.info("Date {} is weekend or holiday. Batch will be queued.", today);
            return true;
        }
        return false;
    }

    public boolean isWeekendOrHoliday(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            logger.info("Date {} is weekend ({})", date, dow);
            return true;
        }
        boolean holiday = !businessDayService.isBusinessDay(date);
        if (holiday) {
            logger.info("Date {} is a holiday according to Core", date);
        }
        return holiday;
    }
}
