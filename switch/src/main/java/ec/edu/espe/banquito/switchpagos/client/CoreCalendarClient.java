package ec.edu.espe.banquito.switchpagos.client;

import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class CoreCalendarClient {

    private static final Logger LOG = LoggerFactory.getLogger(CoreCalendarClient.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.core.base-url}")
    private String coreBaseUrl;

    @Value("${app.core.calendar.is-business-day-endpoint}")
    private String isBusinessDayEndpoint;

    public Boolean isBusinessDay(LocalDate date) {
        String url = UriComponentsBuilder.fromUriString(coreBaseUrl)
                .path(isBusinessDayEndpoint)
                .queryParam("date", date)
                .toUriString();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                LOG.warn("Core calendar returned an empty response: {}", url);
                return null;
            }

            Boolean businessDay = readBoolean(response, "businessDay");
            if (businessDay != null) {
                return businessDay;
            }
            businessDay = readBoolean(response, "isBusinessDay");
            if (businessDay != null) {
                return businessDay;
            }
            businessDay = readBoolean(response, "workingDay");
            if (businessDay != null) {
                return businessDay;
            }

            LOG.warn("Core calendar response did not include a valid business-day flag. URL: {}, response: {}", url, response);
            return null;
        } catch (RestClientException | IllegalArgumentException e) {
            LOG.warn("No se pudo consultar el Core para dia habil. URL: {}", url, e);
            return null;
        }
    }

    private Boolean readBoolean(Map<String, Object> response, String key) {
        Object val = response.get(key);
        if (val instanceof Boolean b) {
            return b;
        }
        if (val instanceof String s && !s.isBlank()) {
            return Boolean.valueOf(s);
        }
        return null;
    }
}
