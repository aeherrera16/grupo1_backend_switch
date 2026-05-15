package ec.edu.espe.banquito.switchpagos.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import ec.edu.espe.banquito.switchpagos.dto.CoreParameterResponseDTO;
import ec.edu.espe.banquito.switchpagos.dto.TransferResponseDTO;
import ec.edu.espe.banquito.switchpagos.service.ICoreBankingClient;

@Service("coreBankingClientImpl")
public class CoreBankingClient implements ICoreBankingClient {

    private final RestClient restClient;

    public CoreBankingClient(@Value("${app.core.base-url}") String coreBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(coreBaseUrl)
                .build();
    }

    @Override
    // Executes Core transfer (origin/destination checks plus debit/credit liquidation).
    public TransferResponseDTO transfer(String origin, String destination, String beneficiaryIdentification,
                                        BigDecimal amount, String uuid) {
        return restClient.post()
                .uri("/core/v1/integration/transfer")
                .body(Map.of(
                        "originAccountNumber", origin,
                        "destinationAccountNumber", destination,
                        "beneficiaryIdentification", beneficiaryIdentification,
                        "amount", amount,
                        "transactionUuid", uuid))
                .retrieve()
                .body(TransferResponseDTO.class);
    }

        @Override
        public CoreParameterResponseDTO getParameter(String code) {
                return restClient.get()
                                .uri("/core/v1/integration/parameter/{code}", code)
                                .retrieve()
                                .body(CoreParameterResponseDTO.class);
        }
}
