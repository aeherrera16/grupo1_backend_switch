package ec.edu.espe.banquito.switchpagos.service.impl;

import ec.edu.espe.banquito.switchpagos.dto.SettlementSummaryDTO;
import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChargeStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;
import ec.edu.espe.banquito.switchpagos.model.ServiceFeeRule;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.ServiceChargeRepository;
import ec.edu.espe.banquito.switchpagos.repository.ServiceFeeRuleRepository;
import ec.edu.espe.banquito.switchpagos.service.ICorporateClosingService;
import ec.edu.espe.banquito.switchpagos.service.ISettlementService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CorporateClosingServiceImpl implements ICorporateClosingService {
    private final PaymentBatchRepository paymentBatchRepository;

    private final ISettlementService settlementService;

    private final ServiceChargeRepository chargeRepository;

    private final ServiceFeeRuleRepository serviceFeeRuleRepository;

    public CorporateClosingServiceImpl(PaymentBatchRepository paymentBatchRepository, ISettlementService settlementService, ServiceChargeRepository chargeRepository,ServiceFeeRuleRepository serviceFeeRuleRepository) {
        this.paymentBatchRepository = paymentBatchRepository;
        this.settlementService = settlementService;
        this.chargeRepository = chargeRepository;
        this.serviceFeeRuleRepository = serviceFeeRuleRepository;
    }

    @Override
    public void closeBatch(Integer batchId) {

        PaymentBatch batch =
                paymentBatchRepository.findById(batchId)
                        .orElseThrow(()-> new RuntimeException("Batch not found"));

        SettlementSummaryDTO settlement =
                settlementService.calculateSettlement(batchId);

        ServiceFeeRule rule =
                serviceFeeRuleRepository.findById(
                        settlement.getRuleId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Service fee rule not found"
                        )
                );

        ServiceCharge charge = new ServiceCharge();

        charge.setPaymentBatch(batch);

        charge.setSuccessfulTransactions(
                settlement.getSuccessfulTransactions()
        );

        charge.setCommissionSubtotal(
                settlement.getCommissionSubtotal()
        );

        charge.setVatAmount(
                settlement.getVatAmount()
        );

        charge.setTotalCharge(
                settlement.getTotalCharge()
        );

        charge.setTotalAmount(
                settlement.getDispersedAmount()
        );

        charge.setUnitFee(
                settlement.getUnitFee()
        );

        charge.setFeeAmount(
                settlement.getFeeAmount()
        );

        charge.setChargeStatus(
                ChargeStatusEnum.CHARGED
        );

        charge.setChargedAt(
                LocalDateTime.now()
        );

        charge.setIvaAmount(
                settlement.getVatAmount()
        );

        charge.setServiceFeeRule(
                new ServiceFeeRule(
                        settlement.getRuleId()
                )
        );

        charge.setStatus(ChargeStatusEnum.PENDING);
        batch.setStatus(BatchStatusEnum.PROCESSED);

        chargeRepository.save(charge);



        paymentBatchRepository.save(batch);
    }
}
