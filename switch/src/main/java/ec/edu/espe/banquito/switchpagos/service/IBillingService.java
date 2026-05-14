package ec.edu.espe.banquito.switchpagos.service;

import java.util.List;
import java.util.Optional;

import ec.edu.espe.banquito.switchpagos.dto.BatchSummaryDTO;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;

/**
 * Interface for billing and commission services.
 * Provides methods for calculating and managing service charges.
 */
public interface IBillingService {
    
    /**
     * Counts the number of successful transactions in a list of details.
     *
     * @param details List of PaymentDetail from the batch
     * @return Number of transactions with status SUCCESS
     */
    Integer countSuccess(List<PaymentDetail> details);
    
    /**
     * Gets the unit fee based on the number of successful transactions.
     * Queries the SERVICE_FEE_RULE table to find the applicable range.
     *
     * @param successful Number of successful transactions
     * @return Unit fee per transaction (BigDecimal)
     * @throws IllegalStateException if no applicable fee rule is found
     */
    java.math.BigDecimal getFee(Integer successful);
    
    /**
     * RF-06: Generates commission charge for a processed batch.
     * This method is called by Johan (PaymentProcessor) after processing completion.
     *
     * Steps:
     * 1. Count successful transactions
     * 2. Get applicable fee
     * 3. Calculate: subtotal = fee * successful, vat = subtotal * 0.15, total = subtotal + vat
     * 4. Create and save ServiceCharge
     * 5. Call coreFacade.chargeCommission(...)
     * 6. Update successful_records and rejected_records of batch
     *
     * @param batch   The processed payment batch
     * @param details List of batch details with their final states
     */
    void generateCharge(PaymentBatch batch, List<PaymentDetail> details);
    
    /**
     * Gets the summary of a processed batch as DTO.
     *
     * @param batchId Batch ID
     * @return BatchSummaryDTO with consolidated batch and commission information
     * @throws ec.edu.espe.banquito.switchpagos.exception.ResourceNotFoundException if the batch does not exist
     */
    BatchSummaryDTO getBatchSummary(Integer batchId);
    
    /**
     * Gets all payment details of a batch.
     *
     * @param batchId Batch ID
     * @return List of PaymentDetail from the batch
     * @throws ec.edu.espe.banquito.switchpagos.exception.ResourceNotFoundException if the batch does not exist
     */
    List<PaymentDetail> getBatchDetails(Integer batchId);
    
    /**
     * Gets the service charge for a batch.
     *
     * @param batchId Batch ID
     * @return Optional with ServiceCharge if it exists, empty if not
     * @throws ec.edu.espe.banquito.switchpagos.exception.ResourceNotFoundException if the batch does not exist
     */
    Optional<ServiceCharge> getServiceCharge(Integer batchId);
    
    /**
     * Gets all registered service charges.
     *
     * @return List of all ServiceCharge
     */
    List<ServiceCharge> getAllCharges();
    
    /**
     * Gets the company account from SwitchParameter.
     * Looks for the parameter with code "EMPRESA_ACCOUNT" by default.
     *
     * @param paramCode Parameter code (e.g: "EMPRESA_ACCOUNT")
     * @return Company account number
     * @throws ec.edu.espe.banquito.switchpagos.exception.ResourceNotFoundException if the parameter does not exist
     */
    String getCompanyAccount(String paramCode);
    
    /**
     * Gets the company account with default code "EMPRESA_ACCOUNT".
     *
     * @return Company account number
     */
    String getDefaultCompanyAccount();
}
