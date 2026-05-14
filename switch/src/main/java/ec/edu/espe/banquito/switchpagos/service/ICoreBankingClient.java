package ec.edu.espe.banquito.switchpagos.service;

import java.math.BigDecimal;

import ec.edu.espe.banquito.switchpagos.dto.TransferResponseDTO;

/**
 * Interfaz para el cliente del Core Banking.
 * Proporciona m├®todos para interactuar con el sistema bancario central.
 */
public interface ICoreBankingClient {
    
    /**
     * Realiza una transferencia entre cuentas.
     * 
     * @param origin Cuenta de origen
     * @param destination Cuenta de destino
     * @param amount Monto a transferir
     * @param uuid UUID ├║nico de la transacci├│n
     */
    TransferResponseDTO transfer(String origin, String destination, String beneficiaryIdentification,
                                 BigDecimal amount, String uuid);
}
