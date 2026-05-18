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

    public Boolean isMassPaymentsActiveForRuc(String ruc) throws RestClientException {
        ResponseEntity<Boolean> response = restTemplate.getForEntity(
                coreBaseUrl + "/core/v1/integration/customer/mass-payments/{ruc}/active",
                Boolean.class,
                ruc);
        return Boolean.TRUE.equals(response.getBody()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Boolean chargeCommission(String companyAccount, BigDecimal subtotal, BigDecimal vat, BigDecimal total, String uuid) {
        logger.info("Charging commission to Core - Account: {}, Subtotal: {}, VAT: {}, Total: {}, UUID: {}",
                companyAccount, subtotal, vat, total, uuid);
        try {
            Map<String, Object> body = Map.of(
                    "accountNumber", companyAccount,
                    "amount", total,
                    "commissionSubtotal", subtotal,
                    "vatAmount", vat,
                    "transactionUuid", uuid,
                    "subtypeCode", "COMISION",
                    "description", "Commission charge for mass payment service"
            );
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/commission",
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            boolean success = response.getStatusCode().is2xxSuccessful() && isSuccessfulCoreResponse(response.getBody());
            logger.info("Commission charge {}: {}", success ? "successful" : "failed", uuid);
            return success;
        } catch (RestClientException e) {
            logger.error("Error charging commission in Core: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    private boolean isSuccessfulCoreResponse(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return true;
        }
        Object success = body.get("success");
        if (success instanceof Boolean booleanSuccess) {
            return booleanSuccess;
        }
        if (success != null) {
            return Boolean.parseBoolean(success.toString());
        }
        Object status = body.get("status");
        if (status != null) {
            String normalizedStatus = status.toString().trim().toUpperCase();
            return "OK".equals(normalizedStatus)
                    || "SUCCESS".equals(normalizedStatus)
                    || "SUCCESSFUL".equals(normalizedStatus)
                    || "CHARGED".equals(normalizedStatus)
                    || "COMPLETED".equals(normalizedStatus);
        }
        return true;
    }

    public Boolean validateCompanyAccount(String accountNumber) {
        logger.info("Validating company account in Core: {}", accountNumber);
        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(
                    coreBaseUrl + "/core/v1/integration/account/{accountNumber}/valid",
                    Boolean.class,
                    accountNumber);
            Boolean valid = Boolean.TRUE.equals(response.getBody());
            logger.info("Account {} valid: {}", accountNumber, valid);
            return valid;
        } catch (RestClientException e) {
            logger.error("Error validating account in Core: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public Map<String, Object> getBalance(String accountNumber) {
        logger.info("Fetching balance from Core: {}", accountNumber);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/balance/{accountNumber}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    accountNumber);
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (RestClientException e) {
            logger.error("Error fetching balance from Core: {}", e.getMessage());
            return Map.of();
        }
    }

    public String getFavoritePaymentAccountByRuc(String ruc) {
        logger.info("Fetching favorite payment account from Core for RUC {}", ruc);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/customer/{ruc}/favorite-account",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    ruc);
            Map<String, Object> body = response.getBody();
            Object accountNumber = body != null ? body.get("accountNumber") : null;
            if (accountNumber == null || accountNumber.toString().isBlank()) {
                throw new RestClientException("Core did not return a valid favorite payment account for RUC " + ruc);
            }
            return accountNumber.toString();
        } catch (RestClientException e) {
            logger.error("Error fetching favorite payment account from Core for RUC {}: {}", ruc, e.getMessage());
            throw e;
        }
    }
}
