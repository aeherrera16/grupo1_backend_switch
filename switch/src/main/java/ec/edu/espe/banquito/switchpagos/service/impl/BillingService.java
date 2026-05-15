package ec.edu.espe.banquito.switchpagos.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.espe.banquito.switchpagos.dto.BatchSummaryDTO;
import ec.edu.espe.banquito.switchpagos.enums.ChargeStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import ec.edu.espe.banquito.switchpagos.exception.ResourceNotFoundException;
import ec.edu.espe.banquito.switchpagos.model.BatchStatusLog;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.model.DetailStatusLog;
import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;
import ec.edu.espe.banquito.switchpagos.model.ServiceFeeRule;
import ec.edu.espe.banquito.switchpagos.model.SwitchParameter;
import ec.edu.espe.banquito.switchpagos.repository.BatchStatusLogRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.repository.DetailStatusLogRepository;
import ec.edu.espe.banquito.switchpagos.repository.ServiceChargeRepository;
import ec.edu.espe.banquito.switchpagos.repository.ServiceFeeRuleRepository;
import ec.edu.espe.banquito.switchpagos.repository.SwitchParameterRepository;

@Service
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private static final BigDecimal IVA_RATE = new BigDecimal("0.15");

    private final ServiceFeeRuleRepository serviceFeeRuleRepository;
    private final ServiceChargeRepository serviceChargeRepository;
    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final BatchStatusLogRepository batchStatusLogRepository;
    private final DetailStatusLogRepository detailStatusLogRepository;
    private final SwitchParameterRepository switchParameterRepository;
    private final CoreFacadeService coreFacadeService;

    @Autowired
    public BillingService(ServiceFeeRuleRepository serviceFeeRuleRepository,
                          ServiceChargeRepository serviceChargeRepository,
                          PaymentBatchRepository paymentBatchRepository,
                          PaymentDetailRepository paymentDetailRepository,
                          BatchStatusLogRepository batchStatusLogRepository,
                          DetailStatusLogRepository detailStatusLogRepository,
                          SwitchParameterRepository switchParameterRepository,
                          CoreFacadeService coreFacadeService) {
        this.serviceFeeRuleRepository = serviceFeeRuleRepository;
        this.serviceChargeRepository = serviceChargeRepository;
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.batchStatusLogRepository = batchStatusLogRepository;
        this.detailStatusLogRepository = detailStatusLogRepository;
        this.switchParameterRepository = switchParameterRepository;
        this.coreFacadeService = coreFacadeService;
    }

    public Integer countSuccess(List<PaymentDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        int successful = 0;
        for (PaymentDetail detail : details) {
            if (detail.getStatus() == PaymentDetailStatusEnum.SUCCESS) {
                successful++;
            }
        }
        logger.debug("Successful transactions counted: {}/{}", successful, details.size());
        return successful;
    }

    public BigDecimal getFeeRate(Integer successful) {
        logger.info("Looking up fee rate for {} successful transactions", successful);
        Optional<ServiceFeeRule> ruleOpt = serviceFeeRuleRepository.findRuleByTransactionCount(BigDecimal.valueOf(successful));
        if (ruleOpt.isEmpty()) {
            logger.error("No fee rule found for {} transactions", successful);
            throw new IllegalStateException("No applicable fee rule found for " + successful + " successful transactions");
        }
        ServiceFeeRule rule = ruleOpt.get();
        logger.info("Fee rule found: {} (range: {}-{}, unit fee: {})",
                rule.getId(), rule.getMinAmount(), rule.getMaxAmount(), rule.getUnitFee());
        return rule.getUnitFee();
    }

    @Transactional
    // RF-06/RF-07: calculate service charge and send settlement to Core.
    public void generateCharge(PaymentBatch batch, List<PaymentDetail> details) {
        logger.info("=== START CHARGE GENERATION RF-06 === Batch ID: {}, File: {}", batch.getId(), batch.getFileName());

        int successful = countSuccess(details);
        int rejected = details != null ? details.size() - successful : 0;
        logger.info("Batch result - Successful: {}, Rejected: {}", successful, rejected);

        Optional<ServiceFeeRule> ruleOpt = serviceFeeRuleRepository.findRuleByTransactionCount(BigDecimal.valueOf(successful));
        if (ruleOpt.isEmpty()) {
            logger.error("No fee rule found for {} transactions", successful);
            throw new IllegalStateException("No applicable fee rule found for " + successful + " successful transactions");
        }

        ServiceFeeRule rule = ruleOpt.get();
        BigDecimal unitFee = rule.getUnitFee();
        logger.info("Applied unit fee: {} per transaction (Rule ID: {})", unitFee, rule.getId());

        BigDecimal subtotal = unitFee.multiply(BigDecimal.valueOf(successful)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vatAmount = subtotal.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

        logger.info("Commission breakdown - Subtotal: {}, VAT (15%): {}, Total: {}", subtotal, vatAmount, total);

        ServiceCharge charge = new ServiceCharge();
        charge.setPaymentBatch(batch);
        charge.setServiceFeeRule(rule);
        charge.setSuccessfulTransactions(successful);
        charge.setUnitFee(unitFee);
        charge.setCommissionSubtotal(subtotal);
        charge.setFeeAmount(subtotal);
        charge.setVatAmount(vatAmount);
        charge.setIvaAmount(vatAmount);
        charge.setTotalCharge(total);
        charge.setTotalAmount(total);
        charge.setChargeStatus(ChargeStatusEnum.PENDING);
        charge.setStatus(ChargeStatusEnum.PENDING);

        ServiceCharge savedCharge = serviceChargeRepository.save(charge);
        logger.info("ServiceCharge created with ID: {}", savedCharge.getId());

        String uuid = UUID.randomUUID().toString();
        String companyAccount = batch.getSourceAccountNumber();
        if (companyAccount == null || companyAccount.isBlank()) {
            throw new IllegalStateException("Batch has no source account number for commission debit");
        }

        logger.info("Sending commission to Core - Account: {}, Total: {}, UUID: {}", companyAccount, total, uuid);
        boolean chargeSuccessful = coreFacadeService.chargeCommission(companyAccount, subtotal, vatAmount, total, uuid);

        if (chargeSuccessful) {
            savedCharge.setChargeStatus(ChargeStatusEnum.CHARGED);
            savedCharge.setStatus(ChargeStatusEnum.CHARGED);
            savedCharge.setChargedAt(LocalDateTime.now());
            logger.info("Charge successful - Status updated to CHARGED");
        } else {
            savedCharge.setChargeStatus(ChargeStatusEnum.REJECTED);
            savedCharge.setStatus(ChargeStatusEnum.REJECTED);
            logger.warn("Charge rejected - Status updated to REJECTED");
        }

        serviceChargeRepository.save(savedCharge);

        batch.setSuccessfulRecords(successful);
        batch.setRejectedRecords(rejected);
        paymentBatchRepository.save(batch);

        logger.info("=== END CHARGE GENERATION RF-06 === successful_records: {}, rejected_records: {}", successful, rejected);
    }

    public BatchSummaryDTO getBatchSummary(Integer batchId) {
        logger.info("Generating summary for batch ID: {}", batchId);
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        Optional<ServiceCharge> chargeOpt = serviceChargeRepository.findByPaymentBatchId(batchId);

        BatchSummaryDTO summary = new BatchSummaryDTO();
        summary.setBatchId(batch.getId());
        summary.setFileName(batch.getFileName());
        summary.setRuc(batch.getRuc());
        summary.setStatus(batch.getStatus() != null ? batch.getStatus().name() : null);
        summary.setTotalRecords(batch.getHeaderTotalRecords());
        summary.setTotalAmount(batch.getHeaderTotalAmount());
        summary.setSuccessfulRecords(batch.getSuccessfulRecords());
        summary.setRejectedRecords(batch.getRejectedRecords());
        summary.setReceivedAt(batch.getReceivedAt());

        if (chargeOpt.isPresent()) {
            ServiceCharge charge = chargeOpt.get();
            summary.setCommissionSubtotal(charge.getCommissionSubtotal());
            summary.setVatAmount(charge.getVatAmount());
            summary.setTotalCharge(charge.getTotalCharge());
            summary.setChargeStatus(charge.getChargeStatus() != null ? charge.getChargeStatus().name() : null);
            summary.setChargedAt(charge.getChargedAt());
        }

        logger.info("Summary generated for batch: {}", summary);
        return summary;
    }

    public List<PaymentDetail> getBatchDetails(Integer batchId) {
        logger.info("Fetching details for batch ID: {}", batchId);
        paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentBatchId(batchId);
        logger.info("Found {} details for batch", details.size());
        return details;
    }

    public Optional<ServiceCharge> getServiceCharge(Integer batchId) {
        logger.info("Fetching service charge for batch ID: {}", batchId);
        paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        return serviceChargeRepository.findByPaymentBatchId(batchId);
    }

    public Map<String, Object> generateSettlementReceipt(Integer batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        ServiceCharge charge = serviceChargeRepository.findByPaymentBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("No service charge found for batch: " + batchId));
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentBatchId(batchId);

        BigDecimal dispersedAmount = details.stream()
                .filter(detail -> detail.getStatus() == PaymentDetailStatusEnum.SUCCESS)
                .map(PaymentDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("batchId", batch.getId());
        receipt.put("fileName", batch.getFileName());
        receipt.put("ruc", batch.getRuc());
        receipt.put("sourceAccountNumber", batch.getSourceAccountNumber());
        receipt.put("batchStatus", batch.getStatus() != null ? batch.getStatus().name() : null);
        receipt.put("receivedAt", batch.getReceivedAt());
        receipt.put("successfulTransactions", charge.getSuccessfulTransactions());
        receipt.put("rejectedTransactions", batch.getRejectedRecords());
        receipt.put("successfulDispersedAmount", dispersedAmount);
        receipt.put("unitFee", charge.getUnitFee());
        receipt.put("commissionSubtotal", charge.getCommissionSubtotal());
        receipt.put("vatAmount", charge.getVatAmount());
        receipt.put("totalDebitedForServices", charge.getTotalCharge());
        receipt.put("chargeStatus", charge.getChargeStatus() != null ? charge.getChargeStatus().name() : null);
        receipt.put("chargedAt", charge.getChargedAt());
        return receipt;
    }

    public String generateNoveltyReportCsv(Integer batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        List<PaymentDetail> details = paymentDetailRepository.findByPaymentBatchId(batchId);

        StringBuilder csv = new StringBuilder();
        csv.append("batch_id,file_name,line_number,beneficiary_identification,beneficiary_name,destination_account,amount,status,rejection_reason,executed_at\n");
        for (PaymentDetail detail : details) {
            csv.append(batch.getId()).append(',')
                    .append(escapeCsv(batch.getFileName())).append(',')
                    .append(detail.getLineNumber()).append(',')
                    .append(escapeCsv(detail.getBeneficiaryIdentification())).append(',')
                    .append(escapeCsv(detail.getBeneficiaryName())).append(',')
                    .append(escapeCsv(detail.getDestinationAccountNumber())).append(',')
                    .append(detail.getAmount()).append(',')
                    .append(detail.getStatus() != null ? detail.getStatus().name() : "").append(',')
                    .append(escapeCsv(detail.getRejectionReason())).append(',')
                    .append(detail.getExecutedAt() != null ? detail.getExecutedAt() : "")
                    .append('\n');
        }
        return csv.toString();
    }

    public List<BatchStatusLog> getBatchStatusHistory(Integer batchId) {
        paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        return batchStatusLogRepository.findByPaymentBatchIdOrderByChangedAtAsc(batchId);
    }

    public List<DetailStatusLog> getDetailStatusHistory(Integer detailId) {
        paymentDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Detail not found: " + detailId));
        return detailStatusLogRepository.findByPaymentDetailIdOrderByChangedAtAsc(detailId);
    }

    public List<ServiceCharge> getAllCharges() {
        logger.info("Fetching all service charges");
        return serviceChargeRepository.findAll();
    }

    public String getCompanyAccount(String paramCode) {
        logger.info("Fetching company account from parameter: {}", paramCode);
        SwitchParameter param = switchParameterRepository.findById(paramCode)
                .orElseThrow(() -> new ResourceNotFoundException("Parameter not found: " + paramCode));
        String accountNumber = param.getValueString();
        logger.info("Company account retrieved: {}", accountNumber);
        return accountNumber;
    }

    public String getDefaultCompanyAccount() {
        return getCompanyAccount("EMPRESA_ACCOUNT");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
