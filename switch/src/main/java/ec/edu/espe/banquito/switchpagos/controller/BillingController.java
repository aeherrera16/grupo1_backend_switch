package ec.edu.espe.banquito.switchpagos.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.banquito.switchpagos.dto.BatchSummaryDTO;
import ec.edu.espe.banquito.switchpagos.exception.ResourceNotFoundException;
import ec.edu.espe.banquito.switchpagos.model.BatchStatusLog;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.model.PaymentDetail;
import ec.edu.espe.banquito.switchpagos.model.DetailStatusLog;
import ec.edu.espe.banquito.switchpagos.model.ServiceCharge;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.service.impl.BillingService;

/**
 * RF-06: Billing and commission endpoints.
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
@RequestMapping("/switch/v1/billing")
public class BillingController {

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;
    private final PaymentBatchRepository paymentBatchRepository;

    @Autowired
    public BillingController(BillingService billingService,
                             PaymentBatchRepository paymentBatchRepository) {
        this.billingService = billingService;
        this.paymentBatchRepository = paymentBatchRepository;
    }

    /**
     * Returns the batch billing summary.
     */
    @GetMapping("/batches/{batchId}/summary")
    public ResponseEntity<?> obtenerResumenBatch(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/summary", batchId);

        try {
            BatchSummaryDTO resumen = billingService.getBatchSummary(batchId);
            logger.info("Resumen obtenido exitosamente para lote {}", batchId);
            return ResponseEntity.ok(resumen);

        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al obtener resumen del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener el resumen"));
        }
    }

    /**
     * Returns batch payment details.
     */
    @GetMapping("/batches/{batchId}/detail")
    public ResponseEntity<?> obtenerDetallesBatch(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/detail", batchId);

        try {
            List<PaymentDetail> detalles = billingService.getBatchDetails(batchId);
            logger.info("Se obtuvieron {} detalles para el lote {}", detalles.size(), batchId);

            return ResponseEntity.ok(Map.of(
                    "batchId", batchId,
                    "totalDetalles", detalles.size(),
                    "detalles", detalles
            ));

        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al obtener detalles del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener los detalles"));
        }
    }

    /**
     * Returns the batch service charge.
     */
    @GetMapping("/batches/{batchId}/charge")
    public ResponseEntity<?> obtenerCargoServicio(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/charge", batchId);

        try {
            ServiceCharge cargo = billingService.getServiceCharge(batchId)
                    .orElseThrow(() -> new ResourceNotFoundException("No hay cargo de servicio para el lote: " + batchId));
            logger.info("Cargo obtenido exitosamente para lote {}", batchId);
            return ResponseEntity.ok(cargo);

        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al obtener cargo del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener el cargo"));
        }
    }

    @GetMapping("/batches/{batchId}/receipt")
    public ResponseEntity<?> obtenerComprobanteLiquidacion(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/receipt", batchId);

        try {
            return ResponseEntity.ok(billingService.generateSettlementReceipt(batchId));
        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al obtener comprobante del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener el comprobante"));
        }
    }

    @GetMapping("/batches/{batchId}/history")
    public ResponseEntity<?> obtenerHistorialBatch(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/history", batchId);

        try {
            List<BatchStatusLog> historial = billingService.getBatchStatusHistory(batchId);
            return ResponseEntity.ok(Map.of(
                    "batchId", batchId,
                    "totalEventos", historial.size(),
                    "historial", historial
            ));
        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al obtener historial del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener el historial del lote"));
        }
    }

    @GetMapping("/details/{detailId}/history")
    public ResponseEntity<?> obtenerHistorialDetalle(@PathVariable Integer detailId) {
        logger.info("GET /switch/v1/billing/details/{}/history", detailId);

        try {
            List<DetailStatusLog> historial = billingService.getDetailStatusHistory(detailId);
            return ResponseEntity.ok(Map.of(
                    "detailId", detailId,
                    "totalEventos", historial.size(),
                    "historial", historial
            ));
        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al obtener historial del detalle {}: {}", detailId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener el historial del detalle"));
        }
    }

    @GetMapping(value = "/batches/{batchId}/novelties", produces = "text/csv")
    public ResponseEntity<?> descargarReporteNovedades(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/novelties", batchId);

        try {
            String csv = billingService.generateNoveltyReportCsv(batchId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"novedades_lote_" + batchId + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);
        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error al generar novedades del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "Error interno al generar novedades"));
        }
    }

    /**
     * Test endpoint for charge generation.
     */
    @PostMapping("/test/{batchId}")
    public ResponseEntity<?> forzarGenerarCobro(@PathVariable Integer batchId) {
        logger.warn("🔧 POST /switch/v1/billing/test/{} - OPERACIÓN DE TESTING", batchId);

        try {
            // Load batch.
            PaymentBatch batch = paymentBatchRepository.findById(batchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));

            logger.info("Lote encontrado: {}", batch.getFileName());

            // Load details.
            List<PaymentDetail> detalles = billingService.getBatchDetails(batchId);
            logger.info("Se obtuvieron {} detalles para procesamiento", detalles.size());

            // Generate charge.
            logger.info("Ejecutando generarCobro para lote {}", batchId);
            billingService.generateCharge(batch, detalles);

            logger.info("GenerarCobro ejecutado exitosamente");

            return ResponseEntity.ok(Map.of(
                    "mensaje", "GenerarCobro ejecutado exitosamente (TEST)",
                    "batchId", batchId,
                    "detallesProcessados", detalles.size()
            ));

        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            logger.error("Error de estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al ejecutar generarCobro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Returns all service charges.
     */
    @GetMapping("/charges")
    public ResponseEntity<?> obtenerTodosCargos() {
        logger.info("GET /switch/v1/billing/charges");

        try {
            List<ServiceCharge> cargos = billingService.getAllCharges();
            logger.info("Se obtuvieron {} cargos totales", cargos.size());

            return ResponseEntity.ok(Map.of(
                    "totalCargos", cargos.size(),
                    "cargos", cargos
            ));

        } catch (Exception e) {
            logger.error("Error al obtener todos los cargos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener los cargos"));
        }
    }

    /**
     * Returns the company account by parameter code.
     */
    @GetMapping("/empresa-account/{paramCode}")
    public ResponseEntity<?> obtenerCuentaEmpresa(@PathVariable String paramCode) {
        logger.info("GET /switch/v1/billing/empresa-account/{}", paramCode);

        try {
            String cuentaEmpresa = billingService.getCompanyAccount(paramCode);
            logger.info("Cuenta empresa obtenida: {}", cuentaEmpresa);

            return ResponseEntity.ok(Map.of(
                    "paramCode", paramCode,
                    "cuentaEmpresa", cuentaEmpresa
            ));

        } catch (ResourceNotFoundException e) {
            logger.warn("Parámetro no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al obtener cuenta empresa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener la cuenta empresa"));
        }
    }

    /**
     * Returns the default company account.
     */
    @GetMapping("/empresa-account")
    public ResponseEntity<?> obtenerCuentaEmpresaDefault() {
        logger.info("GET /switch/v1/billing/empresa-account (default)");

        try {
            String cuentaEmpresa = billingService.getDefaultCompanyAccount();
            logger.info("Cuenta empresa por defecto obtenida: {}", cuentaEmpresa);

            return ResponseEntity.ok(Map.of(
                    "cuentaEmpresa", cuentaEmpresa,
                    "paramCode", "EMPRESA_ACCOUNT"
            ));

        } catch (ResourceNotFoundException e) {
            logger.warn("Parámetro por defecto no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al obtener cuenta empresa por defecto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al obtener la cuenta empresa"));
        }
    }

    /**
     * Downloads the settlement receipt.
     */
    @GetMapping("/batches/{batchId}/download/comprobante")
    public ResponseEntity<?> descargarComprobanteLiquidacion(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/download/comprobante", batchId);

        try {
            PaymentBatch batch = paymentBatchRepository.findById(batchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));

            BatchSummaryDTO resumen = billingService.getBatchSummary(batchId);
            List<PaymentDetail> detalles = billingService.getBatchDetails(batchId);

            StringBuilder comprobante = new StringBuilder();
            comprobante.append("=".repeat(80)).append("\n");
            comprobante.append("COMPROBANTE DE LIQUIDACION - PAGOS MASIVOS\n");
            comprobante.append("=".repeat(80)).append("\n\n");

            comprobante.append("INFORMACION DEL LOTE\n");
            comprobante.append("-".repeat(80)).append("\n");
            comprobante.append(String.format("ID Lote: %d%n", batchId));
            comprobante.append(String.format("Archivo: %s%n", resumen.getFileName()));
            comprobante.append(String.format("RUC: %s%n", resumen.getRuc()));
            comprobante.append(String.format("Estado: %s%n", resumen.getStatus()));
            comprobante.append(String.format("Recibido: %s%n", resumen.getReceivedAt()));
            comprobante.append("\n");

            comprobante.append("RESUMEN FINANCIERO\n");
            comprobante.append("-".repeat(80)).append("\n");
            comprobante.append(String.format("Total Registros: %d%n", resumen.getTotalRecords()));
            comprobante.append(String.format("Registros Exitosos: %d%n", resumen.getSuccessfulRecords()));
            comprobante.append(String.format("Registros Rechazados: %d%n", resumen.getRejectedRecords()));
            comprobante.append(String.format("Monto Total Dispersado: $%.2f%n", resumen.getTotalAmount()));
            comprobante.append(String.format("Subtotal de Comision: $%.2f%n", resumen.getCommissionSubtotal()));
            comprobante.append("IVA: 15%\n");
            comprobante.append(String.format("Total a Debitar: $%.2f%n", resumen.getTotalCharge()));
            comprobante.append("\n");

            comprobante.append("DETALLE DE PAGOS\n");
            comprobante.append("-".repeat(80)).append("\n");
            comprobante.append(String.format("%-4s | %-15s | %-15s | %-10s%n",
                    "No.", "Beneficiario", "Monto", "Estado"));
            comprobante.append("-".repeat(80)).append("\n");

            for (int i = 0; i < detalles.size(); i++) {
                PaymentDetail det = detalles.get(i);
                comprobante.append(String.format("%4d | %-15s | $%13.2f | %-10s%n",
                        i + 1,
                        det.getBeneficiaryIdentification().substring(0, Math.min(15, det.getBeneficiaryIdentification().length())),
                        det.getAmount(),
                        det.getStatus() != null ? det.getStatus().toString() : "PENDIENTE"));
            }

            comprobante.append("\n");
            comprobante.append("=".repeat(80)).append("\n");
            comprobante.append("Documento generado automaticamente por el Sistema de Pagos Masivos\n");
            comprobante.append("=".repeat(80)).append("\n");

            byte[] content = comprobante.toString().getBytes();

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"comprobante_" + batchId + ".txt\"")
                    .body(content);

        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al descargar comprobante del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al descargar el comprobante"));
        }
    }

    /**
     * Downloads the rejection report.
     */
    @GetMapping("/batches/{batchId}/download/novedades")
    public ResponseEntity<?> descargarReporteNovedadesDetallado(@PathVariable Integer batchId) {
        logger.info("GET /switch/v1/billing/batches/{}/download/novedades", batchId);

        try {
            PaymentBatch batch = paymentBatchRepository.findById(batchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));

            BatchSummaryDTO resumen = billingService.getBatchSummary(batchId);
            List<PaymentDetail> detalles = billingService.getBatchDetails(batchId);

            StringBuilder reporte = new StringBuilder();
            reporte.append("REPORTE DE NOVEDADES - PAGOS MASIVOS\n");
            reporte.append(String.format("Lote ID: %d%n", batchId));
            reporte.append(String.format("Archivo: %s%n", resumen.getFileName()));
            reporte.append(String.format("RUC: %s%n", resumen.getRuc()));
            reporte.append(String.format("Fecha Generacion: %s%n", resumen.getReceivedAt()));
            reporte.append("\n");

            reporte.append("RESUMEN\n");
            reporte.append(String.format("Total Rechazos: %d%n", resumen.getRejectedRecords()));
            reporte.append("\n");

            reporte.append("DETALLE DE RECHAZOS\n");
            reporte.append("No.,Beneficiario,Cedula,Monto,Motivo Rechazo\n");

            int rechazoNum = 1;
            for (PaymentDetail det : detalles) {
                if (det.getStatus() != null && det.getStatus().toString().equals("REJECTED")) {
                    String motivo = det.getRejectionReason() != null ? det.getRejectionReason() : "Sin especificar";
                    reporte.append(String.format("%d,%s,%s,%.2f,%s%n",
                            rechazoNum++,
                            det.getBeneficiaryName(),
                            det.getBeneficiaryIdentification(),
                            det.getAmount(),
                            motivo));
                }
            }

            if (resumen.getRejectedRecords() == 0) {
                reporte.append("0,Sin rechazos en este lote,,,%n");
            }

            byte[] content = reporte.toString().getBytes();

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"novedades_" + batchId + ".csv\"")
                    .body(content);

        } catch (ResourceNotFoundException e) {
            logger.warn("Recurso no encontrado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            logger.error("Error al descargar reporte de novedades del lote {}: {}", batchId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al descargar el reporte de novedades"));
        }
    }
}
