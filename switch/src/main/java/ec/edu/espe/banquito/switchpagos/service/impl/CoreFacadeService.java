package ec.edu.espe.banquito.switchpagos.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class CoreFacadeService {

    private static final Logger logger = LoggerFactory.getLogger(CoreFacadeService.class);

    private final RestTemplate restTemplate;

    @Value("${app.core.base-url}")
    private String coreBaseUrl;

    public CoreFacadeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * {@code true} si el cliente RUC existe en Core con servicio de pagos masivos activo ({@code EMPRESA_PAGOS_MASIVOS}),
     * {@code false} si no coincide; lanzamiento de {@link RestClientException} ante fallos de comunicación HTTP.
     */
    public Boolean isMassPaymentsActiveForRuc(String ruc) throws RestClientException {
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
                coreBaseUrl + "/core/v1/integration/customer/mass-payments/{ruc}/active",
                Boolean.class,
                ruc);
        return Boolean.TRUE.equals(response.getBody()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Boolean cobrarComision(String cuentaEmpresa, BigDecimal subtotal, BigDecimal iva, BigDecimal total, String uuid) {
        logger.info("Cobro de comision al Core - Cuenta: {}, Subtotal: {}, IVA: {}, Total: {}, UUID: {}",
                cuentaEmpresa, subtotal, iva, total, uuid);
        try {
            Map<String, Object> body = Map.of(
                    "accountNumber", cuentaEmpresa,
                    "amount", total,
                    "commissionSubtotal", subtotal,
                    "vatAmount", iva,
                    "transactionUuid", uuid,
                    "subtypeCode", "COMISION",
                    "description", "Cobro de comision por pagos masivos"
            );
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/commission",
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Object successBody = response.getBody() != null ? response.getBody().get("success") : null;
            boolean success = response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(successBody);
            logger.info("Cobro de comision {}: {}", success ? "exitoso" : "fallido", uuid);
            return success;
        } catch (RestClientException e) {
            logger.error("Error al cobrar comision en el Core: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public Boolean validarCuentaEmpresa(String cuentaEmpresa) {
        logger.info("Validando cuenta empresa en Core: {}", cuentaEmpresa);
        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(
                    coreBaseUrl + "/core/v1/integration/account/{accountNumber}/valid",
                    Boolean.class,
                    cuentaEmpresa);
            Boolean valid = Boolean.TRUE.equals(response.getBody());
            logger.info("Cuenta {} valida: {}", cuentaEmpresa, valid);
            return valid;
        } catch (RestClientException e) {
            logger.error("Error validando cuenta en el Core: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public Map<String, Object> consultarSaldo(String accountNumber) {
        logger.info("Consultando saldo en Core: {}", accountNumber);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/balance/{accountNumber}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    },
                    accountNumber);
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (RestClientException e) {
            logger.error("Error consultando saldo en el Core: {}", e.getMessage());
            return Map.of();
        }
    }

    public String obtenerCuentaFavoritaPagos() {
        logger.info("Consultando cuenta favorita de pagos en Core");
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/accounts/default/favorite",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = response.getBody();
            Object accountNumber = body != null ? body.get("accountNumber") : null;
            if (accountNumber == null || accountNumber.toString().isBlank()) {
                throw new RestClientException("El Core no devolvio una cuenta favorita valida");
            }

            return accountNumber.toString();
        } catch (RestClientException e) {
            logger.error("Error consultando cuenta favorita en el Core: {}", e.getMessage());
            throw e;
        }
    }
}