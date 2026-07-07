package ec.edu.espe.banquito.switchpagos.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
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
            PaymentDetailRepository detailRepository,
            BillingService billingService
    ) {
        this.detailRepository = detailRepository;
        this.billingService = billingService;
    }

    @Override
    public byte[] generateReceipt(Integer batchId) {

        try {

            Map<String, Object> receipt =
                    billingService.generateSettlementReceipt(batchId);

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

            Color accent =
                    new Color(202, 163, 74);

            Color light =
                    new Color(241, 244, 248);

            Color border =
                    new Color(210, 218, 229);

            Color dark =
                    new Color(33, 37, 41);

            Color white =
                    Color.WHITE;

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            17,
                            white
                    );

            Font subtitleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            9,
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
                            dark
                    );

            Font boldFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10,
                            dark
                    );

            Font totalFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            12,
                            primary
                    );

            Font mutedFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            8,
                            new Color(90, 98, 110)
                    );

            PdfPTable header =
                    new PdfPTable(2);

            header.setWidthPercentage(100);
            header.setWidths(new int[]{4, 2});

            PdfPCell headerCell =
                    new PdfPCell();

            headerCell.setBackgroundColor(primary);

            headerCell.setPadding(15);

            headerCell.setBorder(Rectangle.NO_BORDER);

            Paragraph title =
                    new Paragraph(
                            "COMPROBANTE DE LIQUIDACION",
                            titleFont
                    );

            title.setSpacingAfter(4);

            headerCell.addElement(title);

            Paragraph subtitle =
                    new Paragraph(
                            "Pagos Masivos - Switch Bancario",
                            subtitleFont
                    );

            headerCell.addElement(subtitle);

            header.addCell(headerCell);

            PdfPCell statusCell =
                    new PdfPCell();
            statusCell.setBackgroundColor(primary);
            statusCell.setBorder(Rectangle.NO_BORDER);
            statusCell.setPadding(15);
            statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph status =
                    new Paragraph(
                            "LOTE " + receipt.get("batchId") + "\n" + receipt.get("batchStatus"),
                            titleFont
                    );
            status.setAlignment(Element.ALIGN_RIGHT);
            statusCell.addElement(status);
            header.addCell(statusCell);

            document.add(header);

            PdfPTable accentLine =
                    new PdfPTable(1);
            accentLine.setWidthPercentage(100);
            PdfPCell accentCell =
                    new PdfPCell(new Phrase(" "));
            accentCell.setFixedHeight(5);
            accentCell.setBorder(Rectangle.NO_BORDER);
            accentCell.setBackgroundColor(accent);
            accentLine.addCell(accentCell);
            document.add(accentLine);

            document.add(new Paragraph(" "));

            Paragraph section1 =
                    new Paragraph(
                            "Informacion general",
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
                    "ID del lote",
                    String.valueOf(receipt.get("batchId")),
                    boldFont,
                    normalFont,
                    light,
                    border
            );
            addInfoRow(
                    infoTable,
                    "Archivo",
                    String.valueOf(receipt.get("fileName")),
                    boldFont,
                    normalFont,
                    Color.WHITE,
                    border
            );
            addInfoRow(
                    infoTable,
                    "RUC",
                    String.valueOf(receipt.get("ruc")),
                    boldFont,
                    normalFont,
                    light,
                    border
            );
            addInfoRow(
                    infoTable,
                    "Cuenta origen",
                    String.valueOf(receipt.get("sourceAccountNumber")),
                    boldFont,
                    normalFont,
                    Color.WHITE,
                    border
            );

            addInfoRow(
                    infoTable,
                    "Estado",
                    String.valueOf(receipt.get("batchStatus")),
                    boldFont,
                    normalFont,
                    light,
                    border
            );

            Object receivedAt =
                    receipt.get("receivedAt");

            addInfoRow(
                    infoTable,
                    "Fecha de recepcion",
                    receivedAt != null
                            ? ((LocalDateTime) receivedAt)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            : "",
                    boldFont,
                    normalFont,
                    Color.WHITE,
                    border
            );

            document.add(infoTable);

            document.add(new Paragraph(" "));

            Paragraph section2 =
                    new Paragraph(
                            "Resumen financiero",
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
                    "Transacciones exitosas",
                    String.valueOf(receipt.get("successfulTransactions")),
                    boldFont,
                    normalFont,
                    Color.WHITE,
                    border
            );

            addMoneyRow(
                    financialTable,
                    "Transacciones rechazadas",
                    String.valueOf(receipt.get("rejectedTransactions")),
                    boldFont,
                    normalFont,
                    light,
                    border
            );

            addMoneyRow(
                    financialTable,
                    "Monto dispersado",
                    "$ " + receipt.get("successfulDispersedAmount"),
                    boldFont,
                    normalFont,
                    Color.WHITE,
                    border
            );

            addMoneyRow(
                    financialTable,
                    "Tarifa unitaria",
                    "$ " + receipt.get("unitFee"),
                    boldFont,
                    normalFont,
                    light,
                    border
            );

            addMoneyRow(
                    financialTable,
                    "Subtotal de comision",
                    "$ " + receipt.get("commissionSubtotal"),
                    boldFont,
                    normalFont,
                    Color.WHITE,
                    border
            );

            addMoneyRow(
                    financialTable,
                    "IVA (15%)",
                    "$ " + receipt.get("vatAmount"),
                    boldFont,
                    normalFont,
                    light,
                    border
            );

            PdfPCell totalLabel =
                    new PdfPCell(
                            new Phrase(
                                    "TOTAL COMISION A DEBITAR (aparte del monto dispersado)",
                                    totalFont
                            )
                    );

            totalLabel.setBackgroundColor(new Color(232, 238, 247));

            totalLabel.setBorderColor(accent);

            totalLabel.setPadding(12);

            PdfPCell totalValue =
                    new PdfPCell(
                            new Phrase(
                                    "$ " + receipt.get("totalDebitedForServices"),
                                    totalFont
                            )
                    );

            totalValue.setBackgroundColor(new Color(232, 238, 247));

            totalValue.setBorderColor(accent);

            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);

            totalValue.setPadding(12);

            financialTable.addCell(totalLabel);

            financialTable.addCell(totalValue);

            java.math.BigDecimal dispersedForGrandTotal =
                    receipt.get("successfulDispersedAmount") instanceof java.math.BigDecimal
                            ? (java.math.BigDecimal) receipt.get("successfulDispersedAmount")
                            : java.math.BigDecimal.ZERO;
            java.math.BigDecimal commissionForGrandTotal =
                    receipt.get("totalDebitedForServices") instanceof java.math.BigDecimal
                            ? (java.math.BigDecimal) receipt.get("totalDebitedForServices")
                            : java.math.BigDecimal.ZERO;
            java.math.BigDecimal grandTotal = dispersedForGrandTotal.add(commissionForGrandTotal);

            PdfPCell grandTotalLabel =
                    new PdfPCell(
                            new Phrase(
                                    "TOTAL GENERAL DEBITADO DE LA CUENTA (dispersado + comision)",
                                    totalFont
                            )
                    );
            grandTotalLabel.setBackgroundColor(accent);
            grandTotalLabel.setBorderColor(accent);
            grandTotalLabel.setPadding(12);

            PdfPCell grandTotalValue =
                    new PdfPCell(
                            new Phrase(
                                    "$ " + grandTotal,
                                    totalFont
                            )
                    );
            grandTotalValue.setBackgroundColor(accent);
            grandTotalValue.setBorderColor(accent);
            grandTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            grandTotalValue.setPadding(12);

            financialTable.addCell(grandTotalLabel);
            financialTable.addCell(grandTotalValue);

            document.add(financialTable);

            document.add(new Paragraph(" "));

            Paragraph section3 =
                    new Paragraph(
                            "Detalle de transacciones",
                            sectionFont
                    );

            section3.setSpacingAfter(10);

            document.add(section3);

            PdfPTable detailTable =
                    new PdfPTable(5);

            detailTable.setWidthPercentage(100);

            detailTable.setWidths(new int[]{1, 3, 3, 2, 2});

            addHeader(detailTable, "Linea", primary);
            addHeader(detailTable, "Beneficiario", primary);
            addHeader(detailTable, "Cuenta", primary);
            addHeader(detailTable, "Monto", primary);
            addHeader(detailTable, "Estado", primary);

            for (PaymentDetail detail : details) {
                addDetailCell(detailTable, String.valueOf(detail.getLineNumber()), normalFont, border);
                addDetailCell(detailTable, detail.getBeneficiaryName(), normalFont, border);
                addDetailCell(detailTable, detail.getDestinationAccountNumber(), normalFont, border);
                addDetailCell(detailTable, "$ " + detail.getAmount(), normalFont, border);
                addDetailCell(detailTable, detail.getStatus().toString(), normalFont, border);
            }

            document.add(detailTable);

            document.add(new Paragraph(" "));

            Paragraph footer =
                    new Paragraph(
                            "Documento generado automaticamente por SWITCH PAGOS",
                            mutedFont
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
            Font valueFont,
            Color background,
            Color border
    ) {

        PdfPCell c1 =
                new PdfPCell(
                        new Phrase(label, labelFont)
                );

        c1.setBackgroundColor(background);
        c1.setBorderColor(border);
        c1.setPadding(8);

        PdfPCell c2 =
                new PdfPCell(
                        new Phrase(value, valueFont)
                );

        c2.setBackgroundColor(background);
        c2.setBorderColor(border);
        c2.setPadding(8);

        table.addCell(c1);

        table.addCell(c2);
    }

    private void addMoneyRow(
            PdfPTable table,
            String label,
            String value,
            Font labelFont,
            Font valueFont,
            Color background,
            Color border
    ) {

        PdfPCell c1 =
                new PdfPCell(
                        new Phrase(label, labelFont)
                );

        c1.setBackgroundColor(background);
        c1.setBorderColor(border);
        c1.setPadding(8);

        PdfPCell c2 =
                new PdfPCell(
                        new Phrase(value, valueFont)
                );

        c2.setBackgroundColor(background);
        c2.setBorderColor(border);
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

    private void addDetailCell(
            PdfPTable table,
            String text,
            Font font,
            Color border
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(text != null ? text : "", font)
                );

        cell.setBorderColor(border);
        cell.setPadding(7);
        table.addCell(cell);
    }
}
