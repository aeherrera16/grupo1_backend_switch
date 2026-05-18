package ec.edu.espe.banquito.switchpagos.service.impl;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.client.CoreCalendarClient;

@Service
public class BusinessDayService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessDayService.class);

    private final CoreCalendarClient coreCalendarClient;

    public BusinessDayService(CoreCalendarClient coreCalendarClient) {
        this.coreCalendarClient = coreCalendarClient;
    }

    public boolean isBusinessDay(LocalDate date) {
        Boolean fromCore = coreCalendarClient.isBusinessDay(date);
        if (fromCore != null) {
            return fromCore;
        }

        LOG.warn("Could not query Core business-day status. Assuming business day: {}", date);
        return true;
    }
}
