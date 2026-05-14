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

/**
 * RF-06: Servicio de Facturaci├│n y Comisiones.
 * Responsable de calcular y cobrar las comisiones por el servicio de pagos masivos.
 * 
 * Kevin - Comisiones y Reportes
 */
@Service
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    // Tasa de IVA vigente en Ecuador (15%)
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

    /**
     * Cuenta el n├║mero de transacciones exitosas en una lista de detalles.
     *
     * @param detalles Lista de PaymentDetail del lote
     * @return N├║mero de transacciones con status SUCCESS
     */
    public Integer countSuccess(List<PaymentDetail> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return 0;
        }

        Integer exitosos = 0;
        for (PaymentDetail detalle : detalles) {
            if (detalle.getStatus() == PaymentDetailStatusEnum.SUCCESS) {
                exitosos++;
            }
        }

        logger.debug("Transacciones exitosas contadas: {}/{}", exitosos, detalles.size());
        return exitosos;
    }

    /**
     * Obtiene la tarifa unitaria seg├║n el n├║mero de transacciones exitosas.
     * Consulta la tabla SERVICE_FEE_RULE para encontrar el rango aplicable.
     *
     * @param exitosos N├║mero de transacciones exitosas
     * @return Tarifa unitaria por transacci├│n (BigDecimal)
     * @throws IllegalStateException si no se encuentra una regla tarifaria aplicable
     */
    public BigDecimal obtenerTarifa(Integer exitosos) {
        logger.info("Buscando tarifa para {} transacciones exitosas", exitosos);

        Optional<ServiceFeeRule> reglaOpt = serviceFeeRuleRepository.findRuleByTransactionCount(BigDecimal.valueOf(exitosos));

        if (reglaOpt.isEmpty()) {
            logger.error("No se encontr├│ regla tarifaria para {} transacciones", exitosos);
            throw new IllegalStateException(
                    "No se encontr├│ regla tarifaria aplicable para " + exitosos + " transacciones exitosas");
        }

        ServiceFeeRule regla = reglaOpt.get();
        logger.info("Regla tarifaria encontrada: {} (rango: {}-{}, tarifa: {})",
                regla.getId(),
                regla.getMinAmount(),
                regla.getMaxAmount(),
                regla.getUnitFee());

        return regla.getUnitFee();
    }

    /**
     * RF-06: Genera el cobro de comisi├│n para un lote procesado.
     * Este m├®todo es llamado por Johan (PaymentProcessor) al finalizar el procesamiento.
     *
     * Pasos:
     * 1. Contar transacciones exitosas
     * 2. Obtener tarifa aplicable
     * 3. Calcular: subtotal = tarifa * exitosos, iva = subtotal * 0.15, total = subtotal + iva
     * 4. Crear y guardar ServiceCharge
     * 5. Llamar a coreFacade.cobrarComision(...) usando la cuenta matriz del lote
     * 6. Actualizar successful_records y rejected_records del batch
     *
     * @param batch    El lote de pagos procesado
     * @param detalles Lista de detalles del lote con sus estados finales
     */
    @Transactional
    public void generarCobro(PaymentBatch batch, List<PaymentDetail> detalles) {
        logger.info("=== INICIO GENERACI├ôN DE COBRO RF-06 ===");
        logger.info("Lote ID: {}, Archivo: {}", batch.getId(), batch.getFileName());

        // 1. Contar transacciones exitosas y rechazadas
        Integer exitosos = countSuccess(detalles);
        Integer rechazados = detalles != null ? detalles.size() - exitosos : 0;
        
        logger.info("Resultado del lote - Exitosos: {}, Rechazados: {}", exitosos, rechazados);

        // 2. Obtener la regla tarifaria aplicable
        Optional<ServiceFeeRule> reglaOpt = serviceFeeRuleRepository.findRuleByTransactionCount(BigDecimal.valueOf(exitosos));
        
        if (reglaOpt.isEmpty()) {
            logger.error("No se encontr├│ regla tarifaria para {} transacciones", exitosos);
            throw new IllegalStateException(
                    "No se encontr├│ regla tarifaria aplicable para " + exitosos + " transacciones exitosas");
        }
        
        ServiceFeeRule regla = reglaOpt.get();
        BigDecimal tarifa = regla.getUnitFee();
        logger.info("Tarifa aplicada: {} por transacci├│n (Regla ID: {})", tarifa, regla.getId());

        // 3. Calcular montos de comisi├│n
        BigDecimal subtotal = tarifa.multiply(BigDecimal.valueOf(exitosos))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = subtotal.multiply(IVA_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(iva)
                .setScale(2, RoundingMode.HALF_UP);

        logger.info("C├ílculo de comisi├│n:");
        logger.info("  Subtotal (tarifa x exitosos): {} x {} = {}", tarifa, exitosos, subtotal);
        logger.info("  IVA (15%): {}", iva);
        logger.info("  Total: {}", total);

        // 4. Crear y guardar el registro de cargo (ServiceCharge)
        ServiceCharge cargo = new ServiceCharge();
        cargo.setPaymentBatch(batch);
        cargo.setServiceFeeRule(regla);
        cargo.setSuccessfulTransactions(exitosos);
        cargo.setUnitFee(tarifa);
        cargo.setCommissionSubtotal(subtotal);
        cargo.setFeeAmount(subtotal);
        cargo.setVatAmount(iva);
        cargo.setIvaAmount(iva);
        cargo.setTotalCharge(total);
        cargo.setTotalAmount(total);
        cargo.setChargeStatus(ChargeStatusEnum.PENDING);
        cargo.setStatus(ChargeStatusEnum.PENDING);

        ServiceCharge cargoGuardado = serviceChargeRepository.save(cargo);
        logger.info("ServiceCharge creado con ID: {}", cargoGuardado.getId());

        // 5. Llamar al Core para cobrar la comisi├│n
        String uuid = UUID.randomUUID().toString();
        String cuentaEmpresa = batch.getSourceAccountNumber();
        if (cuentaEmpresa == null || cuentaEmpresa.isBlank()) {
            throw new IllegalStateException("El lote no tiene cuenta matriz de cargo");
        }

        logger.info("Enviando cobro al Core - Cuenta: {}, Total: {}, UUID: {}",
                   cuentaEmpresa, total, uuid);

        boolean cobroExitoso = coreFacadeService.cobrarComision(cuentaEmpresa, subtotal, iva, total, uuid);

        if (cobroExitoso) {
            cargoGuardado.setChargeStatus(ChargeStatusEnum.CHARGED);
            cargoGuardado.setStatus(ChargeStatusEnum.CHARGED);
            cargoGuardado.setChargedAt(LocalDateTime.now());
            logger.info("Cobro exitoso - Status actualizado a CHARGED");
        } else {
            cargoGuardado.setChargeStatus(ChargeStatusEnum.REJECTED);
            cargoGuardado.setStatus(ChargeStatusEnum.REJECTED);
            logger.warn("Cobro rechazado - Status actualizado a REJECTED");
        }

        serviceChargeRepository.save(cargoGuardado);

        // 6. Actualizar contadores del lote
        batch.setSuccessfulRecords(exitosos);
        batch.setRejectedRecords(rechazados);
        paymentBatchRepository.save(batch);

        logger.info("Lote actualizado - successful_records: {}, rejected_records: {}", 
                   exitosos, rechazados);
        logger.info("=== FIN GENERACI├ôN DE COBRO RF-06 ===");
    }

    // ==================== M├ëTODOS DE REPORTES ====================

    /**
     * Obtiene el resumen de un lote procesado como DTO.
     *
     * @param batchId ID del lote
     * @return BatchSummaryDTO con informaci├│n consolidada del lote y su comisi├│n
     * @throws ResourceNotFoundException si el lote no existe
     */
    public BatchSummaryDTO obtenerResumenBatch(Integer batchId) {
        logger.info("Generando resumen para lote ID: {}", batchId);

        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));

        Optional<ServiceCharge> chargeOpt = serviceChargeRepository.findByPaymentBatchId(batchId);

        BatchSummaryDTO resumen = new BatchSummaryDTO();
        resumen.setBatchId(batch.getId());
        resumen.setFileName(batch.getFileName());
        resumen.setRuc(batch.getRuc());
        resumen.setStatus(batch.getStatus() != null ? batch.getStatus().name() : null);
        resumen.setTotalRecords(batch.getHeaderTotalRecords());
        resumen.setTotalAmount(batch.getHeaderTotalAmount());
        resumen.setSuccessfulRecords(batch.getSuccessfulRecords());
        resumen.setRejectedRecords(batch.getRejectedRecords());
        resumen.setReceivedAt(batch.getReceivedAt());

        if (chargeOpt.isPresent()) {
            ServiceCharge charge = chargeOpt.get();
            resumen.setCommissionSubtotal(charge.getCommissionSubtotal());
            resumen.setVatAmount(charge.getVatAmount());
            resumen.setTotalCharge(charge.getTotalCharge());
            resumen.setChargeStatus(charge.getChargeStatus() != null ? charge.getChargeStatus().name() : null);
            resumen.setChargedAt(charge.getChargedAt());
        }

        logger.info("Resumen generado para lote: {}", resumen);
        return resumen;
    }

    /**
     * Obtiene todos los detalles de pago de un lote.
     *
     * @param batchId ID del lote
     * @return Lista de PaymentDetail del lote
     * @throws ResourceNotFoundException si el lote no existe
     */
    public List<PaymentDetail> obtenerDetallesBatch(Integer batchId) {
        logger.info("Consultando detalles para lote ID: {}", batchId);

        // Verificar que el lote existe
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));

        List<PaymentDetail> detalles = paymentDetailRepository.findByPaymentBatchId(batchId);
        logger.info("Se encontraron {} detalles para el lote", detalles.size());

        return detalles;
    }

    /**
     * Obtiene el cargo de servicio para un lote.
     *
     * @param batchId ID del lote
     * @return Optional con ServiceCharge si existe, vac├¡o si no
     * @throws ResourceNotFoundException si el lote no existe
     */
    public Optional<ServiceCharge> obtenerCargoServicio(Integer batchId) {
        logger.info("Consultando cargo de servicio para lote ID: {}", batchId);

        // Verificar que el lote existe
        paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));

        return serviceChargeRepository.findByPaymentBatchId(batchId);
    }

    public Map<String, Object> generarComprobanteLiquidacion(Integer batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));
        ServiceCharge charge = serviceChargeRepository.findByPaymentBatchId(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("No hay cargo de servicio para el lote: " + batchId));
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

    public String generarReporteNovedadesCsv(Integer batchId) {
        PaymentBatch batch = paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));
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

    public List<BatchStatusLog> obtenerHistorialEstadosBatch(Integer batchId) {
        paymentBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + batchId));
        return batchStatusLogRepository.findByPaymentBatchIdOrderByChangedAtAsc(batchId);
    }

    public List<DetailStatusLog> obtenerHistorialEstadosDetalle(Integer detailId) {
        paymentDetailRepository.findById(detailId)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado: " + detailId));
        return detailStatusLogRepository.findByPaymentDetailIdOrderByChangedAtAsc(detailId);
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

    /**
     * Obtiene todos los cargos de servicio registrados.
     *
     * @return Lista de todos los ServiceCharge
     */
    public List<ServiceCharge> obtenerTodosCargos() {
        logger.info("Consultando todos los cargos de servicio");
        return serviceChargeRepository.findAll();
    }

    /**
     * Obtiene la cuenta empresa desde SwitchParameter.
     * Busca el par├ímetro con c├│digo "EMPRESA_ACCOUNT" por defecto.
     *
     * @param paramCode C├│digo del par├ímetro (ej: "EMPRESA_ACCOUNT")
     * @return N├║mero de cuenta de la empresa
     * @throws ResourceNotFoundException si el par├ímetro no existe
     */
    public String obtenerCuentaEmpresa(String paramCode) {
        logger.info("Obteniendo cuenta empresa desde par├ímetro: {}", paramCode);

        SwitchParameter param = switchParameterRepository.findById(paramCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Par├ímetro no encontrado: " + paramCode));

        String cuentaEmpresa = param.getValueString();
        logger.info("Cuenta empresa obtenida: {}", cuentaEmpresa);

        return cuentaEmpresa;
    }

    /**
     * Obtiene la cuenta empresa con c├│digo por defecto "EMPRESA_ACCOUNT".
     *
     * @return N├║mero de cuenta de la empresa
     */
    public String obtenerCuentaEmpresaDefault() {
        return obtenerCuentaEmpresa("EMPRESA_ACCOUNT");
    }
}
