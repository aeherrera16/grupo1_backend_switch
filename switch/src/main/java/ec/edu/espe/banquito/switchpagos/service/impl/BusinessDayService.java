package ec.edu.espe.banquito.switchpagos.service.impl;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.client.CoreCalendarClient;

/**
 * Resuelve si una fecha es día hábil consultando al core (tabla HOLIDAY).
 * Si el core no está disponible, se asume que no es día hábil para evitar
 * procesar lotes en feriados o días no laborables por error.
 */
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

        LOG.warn("No se pudo consultar el core para validar día hábil. Se asume no hábil: {}", date);
        return false;
    }
}

