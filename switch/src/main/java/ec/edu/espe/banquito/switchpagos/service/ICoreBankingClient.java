package ec.edu.espe.banquito.switchpagos.service;

import java.math.BigDecimal;

import ec.edu.espe.banquito.switchpagos.dto.CoreParameterResponseDTO;
import ec.edu.espe.banquito.switchpagos.dto.TransferResponseDTO;

public interface ICoreBankingClient {

    TransferResponseDTO transfer(String origin, String destination, String beneficiaryIdentification,
                                 BigDecimal amount, String uuid);

    CoreParameterResponseDTO getParameter(String code);
}
