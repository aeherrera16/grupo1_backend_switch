package ec.edu.espe.banquito.switchpagos.client;

import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente para consultar al core (tabla HOLIDAY) si un día es hábil.
 */
@Component
public class CoreCalendarClient {

    private static final Logger LOG = LoggerFactory.getLogger(CoreCalendarClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.core.base-url}")
    private String coreBaseUrl;

    @Value("${app.core.calendar.is-business-day-endpoint}")
    private String isBusinessDayEndpoint;

    /**
     * @return true si el core responde que es día hábil; null si no se pudo consultar.
     */
    public Boolean isBusinessDay(LocalDate date) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(coreBaseUrl + isBusinessDayEndpoint + "?date=" + date, Map.class);
            if (response == null) {
                return null;
            }
            Object val = response.get("businessDay");
            if (val instanceof Boolean b) {
                return b;
            }
            if (val instanceof String s) {
                return Boolean.valueOf(s);
            }
            return null;
        } catch (RestClientException | IllegalArgumentException e) {
            LOG.warn("No se pudo consultar el core para día hábil", e);
            return null;
        }
    }
}

