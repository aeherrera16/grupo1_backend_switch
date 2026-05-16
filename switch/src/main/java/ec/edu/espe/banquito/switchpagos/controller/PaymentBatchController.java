package ec.edu.espe.banquito.switchpagos.controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
import ec.edu.espe.banquito.switchpagos.service.impl.NoveltyReportServiceImpl;
import ec.edu.espe.banquito.switchpagos.service.impl.PaymentBatchProcessingService;
import ec.edu.espe.banquito.switchpagos.service.impl.ReceiptGeneratorServiceImpl;
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
    // Report services.
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

        this.receiptGeneratorServiceImpl = receiptGeneratorServiceImpl;
        this.noveltyReportServiceImpl = noveltyReportServiceImpl;
        this.dateTimeProvider = dateTimeProvider;
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(paymentBatchRepository.findAll());
    }

    // RF-01/RF-02: Manual and SFTP upload entrypoint.
    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file,
                                       @RequestParam("channel") ChannelEnum channel) {
        logger.info("New CSV upload request");
        logger.info("File: {}, Size: {} bytes, Channel: {}",
                file.getOriginalFilename(), file.getSize(), channel);

        try {
            final CsvParseResult parseResult;
            try {
                parseResult = CsvBatchParser.parseCsvFile(
                        file.getInputStream(),
                        file.getOriginalFilename(),
                        file.getSize());
            } catch (IllegalArgumentException e) {
                logger.warn("File rejected (parse/structure): {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", e.getMessage(),
                        "rejectedEarly", true));
            } catch (IOException e) {
                logger.warn("Could not read file: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", "Could not read file: " + e.getMessage(),
                        "rejectedEarly", true));
            }
            logger.info("CSV parsed successfully - {} detail rows", parseResult.getDetails().size());

            PaymentBatch batch = parseResult.getBatch();
            batch.setChannel(channel);
            batch.setReceivedAt(dateTimeProvider.now());

            if (ChannelEnum.SFTP.equals(channel)) {
                batch.setSourceAccountNumber(coreFacadeService.getFavoritePaymentAccountByRuc(batch.getRuc()));
            }

            boolean isBusinessDay = businessDayService.isBusinessDay(dateTimeProvider.today());
            boolean withinIngestionWindow = cutoffTimeService.isWithinIngestionWindow();
            boolean shouldEnqueue = !isBusinessDay || !withinIngestionWindow;

            if (shouldEnqueue) {
                batch.setStatus(BatchStatusEnum.ENCOLADO);
                logger.warn("Batch queued due to cutoff/business-day rule. Cutoff: {}", cutoffTimeService.getCutoffTime());
            } else {
                batch.setStatus(BatchStatusEnum.RECEIVED);
                logger.info("Within ingestion window and business day. Processing immediately.");
            }

            logger.info("Starting early validation");
            try {
                fileValidationService.validateEarlyRejection(parseResult);
                logger.info("Early validation passed");
            } catch (IllegalArgumentException e) {
                logger.error("Early validation rejected: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "error", e.getMessage(),
                        "rejectedEarly", true
                ));
            }

            PaymentBatch persistedBatch = paymentBatchRepository.save(batch);
            for (var detail : parseResult.getDetails()) {
                detail.setPaymentBatch(persistedBatch);
            }
            paymentDetailRepository.saveAll(parseResult.getDetails());

            logger.info("Starting full validation");
            FileValidation validation = fileValidationService.validateBatch(persistedBatch, parseResult.getDetails());

            PaymentBatch finalBatch = persistedBatch;
            if (!BatchStatusEnum.ENCOLADO.equals(batch.getStatus()) && "SUCCESS".equals(validation.getValidationResult())) {
                finalBatch = paymentBatchProcessingService.process(persistedBatch, parseResult.getDetails());
            }

            logger.info("Process completed - Result: {}, Status: {}",
                    validation.getValidationResult(), finalBatch.getStatus());

            return ResponseEntity.ok(Map.of(
                    "validationResult", validation.getValidationResult(),
                    "isSuccess", "SUCCESS".equals(validation.getValidationResult()),
                    "encolado", BatchStatusEnum.ENCOLADO.equals(finalBatch.getStatus()),
                    "batchStatus", finalBatch.getStatus().getDisplayName(),
                    "fileValidation", validation
            ));
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload-from-sftp-buzon")
    public ResponseEntity<?> uploadFromSftpBuzon(@RequestParam("file") MultipartFile file) {
        return uploadCsv(file, ChannelEnum.SFTP);
    }

    @PostMapping("/upload-from-sftp-mailbox")
    public ResponseEntity<?> uploadFromSftpMailbox(@RequestParam("file") MultipartFile file) {
        return uploadCsv(file, ChannelEnum.SFTP);
    }

    // Reports.


    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Integer id) {
        byte[] pdf = receiptGeneratorServiceImpl.generateReceipt(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<?> processBatch(@PathVariable Integer id) {
        var batch = paymentBatchRepository.findById(id);
        if (batch.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Lote no encontrado: " + id));
        }

        var details = paymentDetailRepository.findByPaymentBatchIdOrderByLineNumberAsc(id);
        return ResponseEntity.ok(paymentBatchProcessingService.process(batch.get(), details));
    }

    @GetMapping("/{id}/novelties")
    public ResponseEntity<byte[]> downloadNovelties(@PathVariable Integer id) {
        byte[] csv = noveltyReportServiceImpl.generateReport(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=novelties.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
