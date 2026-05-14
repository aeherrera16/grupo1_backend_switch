package ec.edu.espe.banquito.switchpagos.service;

import ec.edu.espe.banquito.switchpagos.dto.SettlementSummaryDTO;

public interface ISettlementService {
    SettlementSummaryDTO calculateSettlement(Integer batchId);
}
