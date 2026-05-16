package ec.edu.espe.banquito.switchpagos.service;

import java.util.List;
import java.util.Optional;

import ec.edu.espe.banquito.switchpagos.dto.BatchSummaryDTO;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;

/**
 * RF-06: Billing service contract.
 */
public interface IBillingService {
    
    /**
     * Counts successful details.
     */
    Integer countSuccess(List<PaymentDetail> details);
    
    /**
     * Returns the unit fee.
     */
    java.math.BigDecimal getFee(Integer successful);
    
    /**
     * RF-06/RF-07: Generates and settles the charge.
     */
    void generateCharge(PaymentBatch batch, List<PaymentDetail> details);
    
    /**
     * Returns the batch summary.
     */
    BatchSummaryDTO getBatchSummary(Integer batchId);
    
    /**
     * Returns batch details.
     */
    List<PaymentDetail> getBatchDetails(Integer batchId);
    
    /**
     * Returns the batch charge.
     */
    Optional<ServiceCharge> getServiceCharge(Integer batchId);
    
    /**
     * Returns all charges.
     */
    List<ServiceCharge> getAllCharges();
    
    /**
     * Returns a company account by parameter code.
     */
    String getCompanyAccount(String paramCode);
    
    /**
     * Returns the default company account.
     */
    String getDefaultCompanyAccount();
}
