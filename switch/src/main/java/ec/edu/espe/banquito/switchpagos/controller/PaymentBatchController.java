package ec.edu.espe.banquito.switchpagos.controller;

import java.io.IOException;
import java.util.Map;

import ec.edu.espe.banquito.switchpagos.service.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser.CsvParseResult;
import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChannelEnum;
import ec.edu.espe.banquito.switchpagos.model.FileValidation;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;
import ec.edu.espe.banquito.switchpagos.service.impl.BusinessDayService;
import ec.edu.espe.banquito.switchpagos.service.impl.CoreFacadeService;
import ec.edu.espe.banquito.switchpagos.service.impl.CutoffTimeService;
import ec.edu.espe.banquito.switchpagos.service.impl.FileValidationService;
import ec.edu.espe.banquito.switchpagos.service.impl.PaymentBatchProcessingService;
import ec.edu.espe.banquito.switchpagos.util.DateTimeProvider;

@RestController
@RequestMapping("/switch/v1/payment-batch")
public class PaymentBatchController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentBatchController.class);

    private final FileValidationService fileValidationService;
    private final CutoffTimeService cutoffTimeService;
    private final BusinessDayService businessDayService;
    private final CoreFacadeService coreFacadeService;
    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentBatchProcessingService paymentBatchProcessingService;
    //Se inyecan servicio para repores
    private final CorporateClosingServiceImpl corporateClosingServiceImpl;
    private final ReceiptGeneratorServiceImpl receiptGeneratorServiceImpl;
    private final NoveltyReportServiceImpl noveltyReportServiceImpl;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public PaymentBatchController(FileValidationService fileValidationService,
                                  CutoffTimeService cutoffTimeService,
                                  BusinessDayService businessDayService,
                                  CoreFacadeService coreFacadeService,
                                  PaymentBatchRepository paymentBatchRepository,
                                  PaymentDetailRepository paymentDetailRepository,
                                  PaymentBatchProcessingService paymentBatchProcessingService,
                                  CorporateClosingServiceImpl corporateClosingServiceImpl,
                                  ReceiptGeneratorServiceImpl receiptGeneratorServiceImpl,
                                  NoveltyReportServiceImpl noveltyReportServiceImpl,
                                  DateTimeProvider dateTimeProvider) {
        this.fileValidationService = fileValidationService;
        this.cutoffTimeService = cutoffTimeService;
        this.businessDayService = businessDayService;
        this.coreFacadeService = coreFacadeService;
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.paymentBatchProcessingService = paymentBatchProcessingService;
        this.corporateClosingServiceImpl = corporateClosingServiceImpl;
        this.receiptGeneratorServiceImpl = receiptGeneratorServiceImpl;
        this.noveltyReportServiceImpl = noveltyReportServiceImpl;
        this.dateTimeProvider = dateTimeProvider;
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(paymentBatchRepository.findAll());
    }

    /**
     * Carga manual/portal: recibe CSV y decide:
     * - Antes de las 18:00 y d├¡a h├íbil (seg├║n core/HOLIDAY): procesa inmediato
     * - Fuera de horario / fin de semana / feriado: guarda ENCOLADO y procesa a las 00:01 del pr├│ximo d├¡a h├íbil
     */
    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file,
                                       @RequestParam("channel") ChannelEnum channel) {
        logger.info("Nueva solicitud de carga CSV");
        logger.info("Archivo: {}, Tama├▒o: {} bytes, Canal: {}",
                file.getOriginalFilename(), file.getSize(), channel);

        try {
            logger.info("Parseando archivo CSV");
            final CsvParseResult parseResult;
            try {
                parseResult = CsvBatchParser.parseCsvFile(
                        file.getInputStream(),
                        file.getOriginalFilename(),
                        file.getSize());
            } catch (IllegalArgumentException e) {
                logger.warn("Archivo rechazado (parse / estructura): {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", e.getMessage(),
                        "rejectedEarly", true));
            } catch (IOException e) {
                logger.warn("No se pudo leer el archivo: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "No se pudo leer el archivo: " + e.getMessage(),
                        "rejectedEarly", true));
            }
            logger.info("CSV parseado exitosamente - {} detalles", parseResult.getDetails().size());

            PaymentBatch batch = parseResult.getBatch();
            batch.setChannel(channel);
            batch.setReceivedAt(dateTimeProvider.now());

            if (ChannelEnum.SFTP.equals(channel)) {
                batch.setSourceAccountNumber(coreFacadeService.getDefaultPaymentAccount());
            }

            boolean isBusinessDay = businessDayService.isBusinessDay(dateTimeProvider.today());
            boolean withinIngestionWindow = cutoffTimeService.isWithinIngestionWindow();
            boolean shouldEnqueue = !isBusinessDay || !withinIngestionWindow;

            if (shouldEnqueue) {
                batch.setStatus(BatchStatusEnum.ENCOLADO);
                logger.warn("Batch encolado por fuera de horario o d├¡a no h├íbil. Corte: {}", cutoffTimeService.getCutoffTime());
            } else {
                batch.setStatus(BatchStatusEnum.RECEIVED);
                logger.info("Dentro del horario y d├¡a h├íbil. Procesamiento inmediato.");
            }

            // RF-02: validaci├│n temprana
            logger.info("Iniciando validaci├│n temprana RF-02");
            try {
                fileValidationService.validateEarlyRejection(parseResult);
                logger.info("Validaci├│n temprana exitosa");
            } catch (IllegalArgumentException e) {
                logger.error("Rechazo temprano RF-02: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "RF-02 Validaci├│n rechazada: " + e.getMessage(),
                        "rejectedEarly", true
                ));
            }

            // Persistir lote + detalles (para auditor├¡a y para procesamiento diferido)
            PaymentBatch persistedBatch = paymentBatchRepository.save(batch);
            for (var detail : parseResult.getDetails()) {
                detail.setPaymentBatch(persistedBatch);
            }
            paymentDetailRepository.saveAll(parseResult.getDetails());

            // Validaci├│n completa (guarda FILE_VALIDATION)
            logger.info("Iniciando validaci├│n completa");
            FileValidation validation = fileValidationService.validateBatch(persistedBatch, parseResult.getDetails());

            // Procesar solo si NO est├í encolado
            PaymentBatch finalBatch = persistedBatch;
            if (!BatchStatusEnum.ENCOLADO.equals(batch.getStatus()) && "SUCCESS".equals(validation.getValidationResult())) {
                finalBatch = paymentBatchProcessingService.process(persistedBatch, parseResult.getDetails());
            }

                logger.info("Proceso completado - Resultado: {}, Status: {}",
                    validation.getValidationResult(), finalBatch.getStatus());

            return ResponseEntity.ok(Map.of(
                    "validationResult", validation.getValidationResult(),
                    "isSuccess", "SUCCESS".equals(validation.getValidationResult()),
                    "encolado", BatchStatusEnum.ENCOLADO.equals(finalBatch.getStatus()),
                    "batchStatus", finalBatch.getStatus().getDisplayName(),
                    "fileValidation", validation
            ));
        } catch (Exception e) {
            logger.error("Error interno del servidor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint para carga de archivos provenientes del buz├│n (switch-email-service).
     * Mantiene el canal como SFTP.
     */
    @PostMapping("/upload-from-sftp-buzon")
    public ResponseEntity<?> uploadFromSftpBuzon(@RequestParam("file") MultipartFile file) {
        return uploadCsv(file, ChannelEnum.SFTP);
    }

    //REPORTES
    @PostMapping("/{id}/close")
    public ResponseEntity<Void> closeBatch(
            @PathVariable Integer id
    ) {

        corporateClosingServiceImpl.closeBatch(id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Integer id
    ) {

        byte[] pdf =
                receiptGeneratorServiceImpl.generateReceipt(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=receipt.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/novelties")
    public ResponseEntity<byte[]> downloadNovelties(
            @PathVariable Integer id
    ) {

        byte[] csv =
                noveltyReportServiceImpl.generateReport(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=novelties.csv"
                )
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

}

