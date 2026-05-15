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

<<<<<<< Updated upstream
    public Boolean chargeCommission(String companyAccount, BigDecimal subtotal, BigDecimal vat, BigDecimal total, String uuid) {
        logger.info("Charging commission to Core - Account: {}, Subtotal: {}, VAT: {}, Total: {}, UUID: {}",
                companyAccount, subtotal, vat, total, uuid);
=======
    public Boolean cobrarComision(String cuentaEmpresa, BigDecimal subtotal, BigDecimal iva, BigDecimal total, String uuid) {
        logger.info("Charging commission in Core - Account: {}, Subtotal: {}, VAT: {}, Total: {}, UUID: {}",
                cuentaEmpresa, subtotal, iva, total, uuid);
>>>>>>> Stashed changes
        try {
            Map<String, Object> body = Map.of(
                    "accountNumber", companyAccount,
                    "amount", total,
                    "commissionSubtotal", subtotal,
                    "vatAmount", vat,
                    "transactionUuid", uuid,
                    "subtypeCode", "COMISION",
<<<<<<< Updated upstream
                    "description", "Commission charge for mass payment service"
=======
                    "description", "Mass-payments commission charge"
>>>>>>> Stashed changes
            );
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/commission",
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Object successBody = response.getBody() != null ? response.getBody().get("success") : null;
            boolean success = response.getStatusCode().is2xxSuccessful() && Boolean.TRUE.equals(successBody);
            logger.info("Commission charge {}: {}", success ? "successful" : "failed", uuid);
            return success;
        } catch (RestClientException e) {
            logger.error("Error charging commission in Core: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

<<<<<<< Updated upstream
    public Boolean validateCompanyAccount(String accountNumber) {
        logger.info("Validating company account in Core: {}", accountNumber);
=======
    public Boolean validarCuentaEmpresa(String cuentaEmpresa) {
        logger.info("Validating company account in Core: {}", cuentaEmpresa);
>>>>>>> Stashed changes
        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(
                    coreBaseUrl + "/core/v1/integration/account/{accountNumber}/valid",
                    Boolean.class,
                    accountNumber);
            Boolean valid = Boolean.TRUE.equals(response.getBody());
<<<<<<< Updated upstream
            logger.info("Account {} valid: {}", accountNumber, valid);
=======
            logger.info("Account {} valid: {}", cuentaEmpresa, valid);
>>>>>>> Stashed changes
            return valid;
        } catch (RestClientException e) {
            logger.error("Error validating account in Core: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

<<<<<<< Updated upstream
    public Map<String, Object> getBalance(String accountNumber) {
        logger.info("Fetching balance from Core: {}", accountNumber);
=======
    public Map<String, Object> consultarSaldo(String accountNumber) {
        logger.info("Checking balance in Core: {}", accountNumber);
>>>>>>> Stashed changes
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/integration/balance/{accountNumber}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    accountNumber);
            return response.getBody() != null ? response.getBody() : Map.of();
        } catch (RestClientException e) {
<<<<<<< Updated upstream
            logger.error("Error fetching balance from Core: {}", e.getMessage());
=======
            logger.error("Error checking balance in Core: {}", e.getMessage());
>>>>>>> Stashed changes
            return Map.of();
        }
    }

<<<<<<< Updated upstream
    public String getDefaultPaymentAccount() {
        logger.info("Fetching default payment account from Core");
=======
    public String obtenerCuentaFavoritaPagos() {
        logger.info("Fetching favorite payment account from Core");
>>>>>>> Stashed changes
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    coreBaseUrl + "/core/v1/accounts/default/favorite",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> body = response.getBody();
            Object accountNumber = body != null ? body.get("accountNumber") : null;
            if (accountNumber == null || accountNumber.toString().isBlank()) {
<<<<<<< Updated upstream
                throw new RestClientException("Core did not return a valid default payment account");
=======
                throw new RestClientException("Core did not return a valid favorite account");
>>>>>>> Stashed changes
            }
            return accountNumber.toString();
        } catch (RestClientException e) {
<<<<<<< Updated upstream
            logger.error("Error fetching default payment account from Core: {}", e.getMessage());
=======
            logger.error("Error fetching favorite account from Core: {}", e.getMessage());
>>>>>>> Stashed changes
            throw e;
        }
    }
}
