package ec.edu.espe.banquito.switchpagos.service.impl;

import ec.edu.espe.banquito.switchpagos.dto.SettlementSummaryDTO;
import ec.edu.espe.banquito.switchpagos.enums.PaymentDetailStatusEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.model.ServiceFeeRule;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.repository.ServiceFeeRuleRepository;
import ec.edu.espe.banquito.switchpagos.service.ISettlementService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SettlementServiceImpl implements ISettlementService {
    private final PaymentDetailRepository detailRepository;

    private final ServiceFeeRuleRepository ruleRepository;

    public SettlementServiceImpl(PaymentDetailRepository detailRepository, ServiceFeeRuleRepository ruleRepository) {
        this.detailRepository = detailRepository;
        this.ruleRepository = ruleRepository;
    }

    @Override
    public SettlementSummaryDTO calculateSettlement(Integer batchId) {

            List<PaymentDetail> successful =
                    detailRepository
                            .findByPaymentBatchIdAndStatus(
                                    batchId,
                                    PaymentDetailStatusEnum.SUCCESS
                            );

            Integer successfulTransactions =
                    successful.size();

            BigDecimal dispersedAmount =
                    successful.stream()
                            .map(PaymentDetail::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

        ServiceFeeRule rule =
                ruleRepository
                        .findRule(successfulTransactions)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No existe regla tarifaria para "
                                                + successfulTransactions
                                                + " transacciones"
                                )
                        );

        if (rule == null) {
            throw new RuntimeException(
                    "No existe regla tarifaria para "
                            + successfulTransactions
                            + " transacciones"
            );
        }

        BigDecimal commissionSubtotal =
                rule.getUnitFee()
                        .multiply(
                                BigDecimal.valueOf(successfulTransactions)
                        );

        BigDecimal vatAmount =
                commissionSubtotal.multiply(
                        new BigDecimal("0.15")
                );

        BigDecimal totalCharge =
                dispersedAmount
                        .add(commissionSubtotal)
                        .add(vatAmount);

        SettlementSummaryDTO dto = new SettlementSummaryDTO();

        dto.setSuccessfulTransactions(successfulTransactions);

        dto.setDispersedAmount(dispersedAmount);

        dto.setCommissionSubtotal(commissionSubtotal);

        dto.setVatAmount(vatAmount);

        dto.setTotalCharge(totalCharge);

        // RF-06: Extra settlement fields.

        dto.setUnitFee(rule.getUnitFee());

        dto.setFeeAmount(rule.getFeeAmount());

        dto.setRuleId(rule.getId());

        dto.setIvaAmount(vatAmount);

        dto.setTotalAmount(dispersedAmount);

        return dto;
    }

}
