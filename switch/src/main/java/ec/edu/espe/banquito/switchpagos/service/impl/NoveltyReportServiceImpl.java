package ec.edu.espe.banquito.switchpagos.service.impl;

import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.service.INoveltyReportService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class NoveltyReportServiceImpl implements INoveltyReportService {
    private final PaymentDetailRepository paymentDetailRepository;

    public NoveltyReportServiceImpl(PaymentDetailRepository paymentDetailRepository) {
        this.paymentDetailRepository = paymentDetailRepository;
    }

    @Override
    public byte[] generateReport(Integer batchId) {

        List<PaymentDetail> details =
                paymentDetailRepository.findByPaymentBatchId(batchId);

        StringBuilder csv = new StringBuilder();

        csv.append(
                "LINE,BENEFICIARY,ACCOUNT,AMOUNT,STATUS,REASON\n"
        );

        for (PaymentDetail detail : details) {

            csv.append(detail.getLineNumber())
                    .append(",");

            csv.append(detail.getBeneficiaryName())
                    .append(",");

            csv.append(detail.getDestinationAccountNumber())
                    .append(",");

            csv.append(detail.getAmount())
                    .append(",");

            csv.append(detail.getStatus())
                    .append(",");

            csv.append(detail.getRejectionReason())
                    .append("\n");
        }

        return csv.toString()
                .getBytes(StandardCharsets.UTF_8);
    }
}
