package ec.edu.espe.banquito.switchpagos.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import ec.edu.espe.banquito.switchpagos.dto.SettlementSummaryDTO;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.service.IReceiptGeneratorService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReceiptGeneratorServiceImpl implements IReceiptGeneratorService {
    private final PaymentBatchRepository paymentBatchRepository;
    private final SettlementServiceImpl settlementService;

    public ReceiptGeneratorServiceImpl(
            PaymentBatchRepository paymentBatchRepository,
            SettlementServiceImpl settlementService
    ) {
        this.paymentBatchRepository = paymentBatchRepository;
        this.settlementService = settlementService;
    }

    @Override
    public byte[] generateReceipt(Integer batchId) {

        PaymentBatch batch =
                paymentBatchRepository.findById(batchId)
                        .orElseThrow();

        SettlementSummaryDTO settlement =
                settlementService.calculateSettlement(batchId);

        try {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            Document document = new Document();

            PdfWriter.getInstance(document, output);

            document.open();

            document.add(
                    new Paragraph(
                            "COMPROBANTE DE LIQUIDACION"
                    )
            );

            document.add(
                    new Paragraph(
                            "Empresa: " + batch.getRuc()
                    )
            );

            document.add(
                    new Paragraph(
                            "Monto dispersado: "
                                    + settlement.getDispersedAmount()
                    )
            );

            document.add(
                    new Paragraph(
                            "Comision: "
                                    + settlement.getCommissionSubtotal()
                    )
            );

            document.add(
                    new Paragraph(
                            "IVA: "
                                    + settlement.getVatAmount()
                    )
            );

            document.add(
                    new Paragraph(
                            "Total debitado: "
                                    + settlement.getTotalCharge()
                    )
            );

            document.close();

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
