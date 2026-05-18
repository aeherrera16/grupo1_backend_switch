package ec.edu.espe.banquito.switchpagos.service;

import java.util.List;
import java.util.Optional;

import ec.edu.espe.banquito.switchpagos.dto.BatchSummaryDTO;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;

public interface IBillingService {

    Integer countSuccess(List<PaymentDetail> details);

    java.math.BigDecimal getFee(Integer successful);

    void generateCharge(PaymentBatch batch, List<PaymentDetail> details);

    BatchSummaryDTO getBatchSummary(Integer batchId);

    List<PaymentDetail> getBatchDetails(Integer batchId);

    Optional<ServiceCharge> getServiceCharge(Integer batchId);

    List<ServiceCharge> getAllCharges();

    String getCompanyAccount(String paramCode);

    String getDefaultCompanyAccount();
}
