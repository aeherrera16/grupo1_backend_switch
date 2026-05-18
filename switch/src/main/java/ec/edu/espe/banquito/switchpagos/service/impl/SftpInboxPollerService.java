package ec.edu.espe.banquito.switchpagos.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser.CsvParseResult;
import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChannelEnum;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.provider.DateTimeProvider;
import ec.edu.espe.banquito.switchpagos.repository.PaymentBatchRepository;
import ec.edu.espe.banquito.switchpagos.repository.PaymentDetailRepository;

@Service
@ConditionalOnProperty(name = "app.sftp.enabled", havingValue = "true", matchIfMissing = true)
public class SftpInboxPollerService {

    private static final Logger LOG = LoggerFactory.getLogger(SftpInboxPollerService.class);

    @Value("${app.sftp.inbox-dir:}")
    private String inboxDir;

    private final FileValidationService fileValidationService;
    private final PaymentBatchRepository paymentBatchRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final PaymentBatchProcessingService paymentBatchProcessingService;
    private final CoreFacadeService coreFacadeService;
    private final BusinessDayService businessDayService;
    private final CutoffTimeService cutoffTimeService;
    private final DateTimeProvider dateTimeProvider;

    @Autowired
    public SftpInboxPollerService(
            FileValidationService fileValidationService,
            PaymentBatchRepository paymentBatchRepository,
            PaymentDetailRepository paymentDetailRepository,
            PaymentBatchProcessingService paymentBatchProcessingService,
            CoreFacadeService coreFacadeService,
            BusinessDayService businessDayService,
            CutoffTimeService cutoffTimeService,
            DateTimeProvider dateTimeProvider) {
        this.fileValidationService = fileValidationService;
        this.paymentBatchRepository = paymentBatchRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.paymentBatchProcessingService = paymentBatchProcessingService;
        this.coreFacadeService = coreFacadeService;
        this.businessDayService = businessDayService;
        this.cutoffTimeService = cutoffTimeService;
        this.dateTimeProvider = dateTimeProvider;
    }

    @Scheduled(fixedDelayString = "${app.sftp.poll-interval-ms:30000}")
    public void pollInbox() {
        if (inboxDir == null || inboxDir.isBlank()) {
            return;
        }

        Path inbox = Path.of(inboxDir);
        if (!Files.isDirectory(inbox)) {
            LOG.warn("SFTP inbox directory not found: {}", inboxDir);
            return;
        }

        try (Stream<Path> entries = Files.list(inbox)) {
            entries
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".csv"))
                .forEach(this::claimAndProcess);
        } catch (IOException e) {
            LOG.error("Error scanning SFTP inbox {}: {}", inboxDir, e.getMessage(), e);
        }
    }

    private void claimAndProcess(Path csvFile) {
        String originalName = csvFile.getFileName().toString();
        Path processingFile = csvFile.resolveSibling(originalName + ".processing");

        if (!csvFile.toFile().renameTo(processingFile.toFile())) {
            LOG.debug("Could not claim {} — likely already being processed", originalName);
            return;
        }

        Path processedDir = csvFile.getParent().resolve("processed");
        Path errorsDir = csvFile.getParent().resolve("errors");

        try {
            ensureDirectory(processedDir);
            ensureDirectory(errorsDir);

            processFile(processingFile, originalName);

            Files.move(processingFile, processedDir.resolve(originalName), StandardCopyOption.REPLACE_EXISTING);
            LOG.info("SFTP file ingested and moved to processed/: {}", originalName);
        } catch (Exception e) {
            LOG.error("Failed ingesting SFTP file {}: {}", originalName, e.getMessage(), e);
            try {
                Files.move(processingFile, errorsDir.resolve(originalName), StandardCopyOption.REPLACE_EXISTING);
                LOG.info("SFTP file moved to errors/: {}", originalName);
            } catch (IOException moveEx) {
                LOG.error("Could not move {} to errors/: {}", originalName, moveEx.getMessage());
            }
        }
    }

    private void processFile(Path file, String originalName) throws IOException {
        long fileSize = Files.size(file);

        CsvParseResult parseResult;
        try (InputStream is = Files.newInputStream(file)) {
            parseResult = CsvBatchParser.parseCsvFile(is, originalName, fileSize);
        }

        PaymentBatch batch = parseResult.getBatch();
        batch.setChannel(ChannelEnum.SFTP);
        batch.setReceivedAt(dateTimeProvider.now());
        batch.setScheduledDate(dateTimeProvider.now());
        batch.setSourceAccountNumber(coreFacadeService.getFavoritePaymentAccountByRuc(batch.getRuc()));

        boolean isBusinessDay = businessDayService.isBusinessDay(dateTimeProvider.today());
        boolean withinWindow = cutoffTimeService.isWithinIngestionWindow();
        boolean shouldEnqueue = !isBusinessDay || !withinWindow;

        batch.setStatus(shouldEnqueue ? BatchStatusEnum.ENCOLADO : BatchStatusEnum.RECEIVED);

        fileValidationService.validateEarlyRejection(parseResult);

        PaymentBatch saved = paymentBatchRepository.save(batch);
        for (var detail : parseResult.getDetails()) {
            detail.setPaymentBatch(saved);
        }
        paymentDetailRepository.saveAll(parseResult.getDetails());

        var validation = fileValidationService.validateBatch(saved, parseResult.getDetails());

        if (!BatchStatusEnum.ENCOLADO.equals(saved.getStatus()) && "SUCCESS".equals(validation.getValidationResult())) {
            paymentBatchProcessingService.process(saved, parseResult.getDetails());
        }

        LOG.info("SFTP batch registered: id={}, ruc={}, status={}", saved.getId(), saved.getRuc(), saved.getStatus());
    }

    private void ensureDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}
