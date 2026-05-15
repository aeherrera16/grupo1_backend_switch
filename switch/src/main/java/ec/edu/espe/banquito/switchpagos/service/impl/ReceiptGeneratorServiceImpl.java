package ec.edu.espe.banquito.switchpagos.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.repository.ServiceChargeRepository;
import ec.edu.espe.banquito.switchpagos.service.IReceiptGeneratorService;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.awt.Color;
import java.util.Map;

@Service
public class ReceiptGeneratorServiceImpl implements IReceiptGeneratorService {

    private final PaymentDetailRepository detailRepository;
    private final BillingService billingService;

    public ReceiptGeneratorServiceImpl(
            PaymentBatchRepository paymentBatchRepository,
            SettlementServiceImpl settlementService,
            PaymentDetailRepository detailRepository,
            ServiceChargeRepository chargeRepository,
            BillingService billingService
    ) {
        this.detailRepository = detailRepository;
        this.billingService = billingService;
    }

    @Override
    public byte[] generateReceipt(Integer batchId) {

        try {

            Map<String, Object> receipt =
                    billingService.generarComprobanteLiquidacion(batchId);

            List<PaymentDetail> details =
                    detailRepository.findByPaymentBatchId(batchId);

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4, 40, 40, 60, 40);

            PdfWriter writer =
                    PdfWriter.getInstance(document, baos);

            document.open();

            Color primary =
                    new Color(0, 51, 102);

            Color light =
                    new Color(230, 230, 230);

            Color white =
                    Color.WHITE;

            // ===== FUENTES =====

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            18,
                            white
                    );

            Font sectionFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            12,
                            primary
                    );

            Font normalFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            10,
                            Color.BLACK
                    );

            Font boldFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10,
                            Color.BLACK
                    );

            PdfPTable header =
                    new PdfPTable(1);

            header.setWidthPercentage(100);

            PdfPCell headerCell =
                    new PdfPCell();

            headerCell.setBackgroundColor(primary);

            headerCell.setPadding(15);

            headerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph title =
                    new Paragraph(
                            "CORPORATE CLOSING RECEIPT",
                            titleFont
                    );

            title.setAlignment(Element.ALIGN_CENTER);

            headerCell.addElement(title);

            header.addCell(headerCell);

            document.add(header);

            document.add(new Paragraph(" "));

            Paragraph section1 =
                    new Paragraph(
                            "General Information",
                            sectionFont
                    );

            section1.setSpacingAfter(10);

            document.add(section1);

            PdfPTable infoTable =
                    new PdfPTable(2);

            infoTable.setWidthPercentage(100);

            infoTable.setWidths(new int[]{3, 5});

            addInfoRow(
                    infoTable,
                    "Batch ID",
                    String.valueOf(receipt.get("batchId")),
                    boldFont,
                    normalFont
            );
            addInfoRow(
                    infoTable,
                    "Archive",
                    String.valueOf(receipt.get("fileName")),
                    boldFont,
                    normalFont
            );
            addInfoRow(
                    infoTable,
                    "RUC",
                    String.valueOf(receipt.get("ruc")),
                    boldFont,
                    normalFont
            );
            addInfoRow(
                    infoTable,
                    "Source Account",
                    String.valueOf(receipt.get("sourceAccountNumber")),
                    boldFont,
                    normalFont
            );

            addInfoRow(
                    infoTable,
                    "State",
                    String.valueOf(receipt.get("batchStatus")),
                    boldFont,
                    normalFont
            );

            Object receivedAt =
                    receipt.get("receivedAt");

            addInfoRow(
                    infoTable,
                    "Date Received",
                    receivedAt != null
                            ? ((LocalDateTime) receivedAt)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            : "",
                    boldFont,
                    normalFont
            );

            document.add(infoTable);

            document.add(new Paragraph(" "));

            Paragraph section2 =
                    new Paragraph(
                            "Financial summary",
                            sectionFont
                    );

            section2.setSpacingAfter(10);

            document.add(section2);

            PdfPTable financialTable =
                    new PdfPTable(2);

            financialTable.setWidthPercentage(100);

            financialTable.setWidths(new int[]{5, 3});

            addMoneyRow(
                    financialTable,
                    "Successful transactions",
                    String.valueOf(receipt.get("successfulTransactions")),
                    boldFont,
                    normalFont
            );

            addMoneyRow(
                    financialTable,
                    "Rejected transactions",
                    String.valueOf(receipt.get("rejectedTransactions")),
                    boldFont,
                    normalFont
            );


            addMoneyRow(
                    financialTable,
                    "Amount disbursed",
                    "$ " + receipt.get("successfulDispersedAmount"),
                    boldFont,
                    normalFont
            );

            addMoneyRow(
                    financialTable,
                    "Unit rate",
                    "$ " + receipt.get("unitFee"),
                    boldFont,
                    normalFont
            );

            addMoneyRow(
                    financialTable,
                    "Subtotal commission",
                    "$ " + receipt.get("commissionSubtotal"),
                    boldFont,
                    normalFont
            );

            addMoneyRow(
                    financialTable,
                    "IVA",
                    "$ " + receipt.get("vatAmount"),
                    boldFont,
                    normalFont
            );


            PdfPCell totalLabel =
                    new PdfPCell(
                            new Phrase(
                                    "TOTAL CHARGED",
                                    boldFont
                            )
                    );

            totalLabel.setBackgroundColor(light);

            totalLabel.setPadding(10);

            PdfPCell totalValue =
                    new PdfPCell(
                            new Phrase(
                                    "$ " + receipt.get("totalDebitedForServices"),
                                    boldFont
                            )
                    );

            totalValue.setBackgroundColor(light);

            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalValue.setPadding(10);

            financialTable.addCell(totalLabel);

            financialTable.addCell(totalValue);

            document.add(financialTable);

            document.add(new Paragraph(" "));


            Paragraph section3 =
                    new Paragraph(
                            "Transaction details",
                            sectionFont
                    );

            section3.setSpacingAfter(10);

            document.add(section3);

            PdfPTable detailTable =
                    new PdfPTable(5);

            detailTable.setWidthPercentage(100);

            detailTable.setWidths(new int[]{1, 3, 3, 2, 2});

            addHeader(detailTable, "Line", primary);
            addHeader(detailTable, "Beneficiary", primary);
            addHeader(detailTable, "Account", primary);
            addHeader(detailTable, "Amount", primary);
            addHeader(detailTable, "State", primary);

            for (PaymentDetail detail : details) {

                detailTable.addCell(
                        new Phrase(
                                String.valueOf(detail.getLineNumber()),
                                normalFont
                        )
                );

                detailTable.addCell(
                        new Phrase(
                                detail.getBeneficiaryName(),
                                normalFont
                        )
                );

                detailTable.addCell(
                        new Phrase(
                                detail.getDestinationAccountNumber(),
                                normalFont
                        )
                );

                detailTable.addCell(
                        new Phrase(
                                "$ " + detail.getAmount(),
                                normalFont
                        )
                );

                detailTable.addCell(
                        new Phrase(
                                detail.getStatus().toString(),
                                normalFont
                        )
                );
            }

            document.add(detailTable);

            document.add(new Paragraph(" "));

            Paragraph footer =
                    new Paragraph(
                            "Document automatically generated by SWITCH PAYMENTS",
                            normalFont
                    );

            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

            writer.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void addInfoRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell c1 =
                new PdfPCell(
                        new Phrase(label, labelFont)
                );

        c1.setPadding(8);

        PdfPCell c2 =
                new PdfPCell(
                        new Phrase(value, valueFont)
                );

        c2.setPadding(8);

        table.addCell(c1);

        table.addCell(c2);
    }

    private void addMoneyRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont
    ) {

        PdfPCell c1 =
                new PdfPCell(
                        new Phrase(label, labelFont)
                );

        c1.setPadding(8);

        PdfPCell c2 =
                new PdfPCell(
                        new Phrase(value, valueFont)
                );

        c2.setPadding(8);

        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(c1);

        table.addCell(c2);
    }

    private void addHeader(
            PdfPTable table,
            String text,
            Color background
    ) {

        Font font =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10,
                        Color.WHITE
                );

        PdfPCell header =
                new PdfPCell(
                        new Phrase(text, font)
                );

        header.setBackgroundColor(background);

        header.setHorizontalAlignment(Element.ALIGN_CENTER);

        header.setPadding(8);

        table.addCell(header);
    }
}
