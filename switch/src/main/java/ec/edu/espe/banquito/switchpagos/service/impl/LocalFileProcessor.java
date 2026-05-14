package ec.edu.espe.banquito.switchpagos.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChannelEnum;
import ec.edu.espe.banquito.switchpagos.model.FileValidation;
import ec.edu.espe.banquito.switchpagos.model.PaymentBatch;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser.CsvParseResult;
import ec.edu.espe.banquito.switchpagos.config.EnumUtils;
import ec.edu.espe.banquito.switchpagos.service.IFileValidationService;
import ec.edu.espe.banquito.switchpagos.service.ILocalFileProcessor;

@Service
@EnableScheduling
public class LocalFileProcessor implements ILocalFileProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileProcessor.class);
    
    @Value("${app.local.input-directory:./input}")
    private String inputDirectory;
    
    @Value("${app.local.processed-directory:./processed}")
    private String processedDirectory;
    
    private final IFileValidationService fileValidationService;
    
    @Autowired
    public LocalFileProcessor(IFileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }
    
    @Override
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void processLocalFiles() {
        logger.info("Starting local file processing");
        
        try {
            Path inputPath = Paths.get(inputDirectory);
            if (!Files.exists(inputPath)) {
                logger.warn("Input directory does not exist: {}", inputDirectory);
                return;
            }
            
            // Process CSV files
            try (Stream<Path> files = Files.list(inputPath)) {
                files.filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                     .filter(path -> Files.isRegularFile(path))
                     .forEach(this::processFile);
            }
            
        } catch (Exception e) {
            logger.error("Error processing local files: {}", e.getMessage(), e);
        }
    }
    
    private void processFile(Path filePath) {
        try {
            logger.info("Processing file: {}", filePath.getFileName());
            
            // Read and process file
            CsvParseResult parseResult = CsvBatchParser.parseCsvFile(
                Files.newInputStream(filePath), 
                filePath.getFileName().toString(), 
                Files.size(filePath)
            );
            
            // Process batch (CsvParseResult doesn't have success/errorMessage fields)
            PaymentBatch batch = parseResult.getBatch();
            batch.setChannel(ChannelEnum.SFTP);
            batch.setReceivedAt(LocalDateTime.now());
            batch.setStatus(BatchStatusEnum.RECEIVED);
            
            // Validate and process
            fileValidationService.validateEarlyRejection(parseResult);
            FileValidation validation = fileValidationService.validateBatch(batch, parseResult.getDetails());
            
            // Move file to processed directory
            moveToProcessedDirectory(filePath);
            
            logger.info("File processed successfully: {}", filePath.getFileName());
            
        } catch (Exception e) {
            logger.error("Error processing file {}: {}", filePath.getFileName(), e.getMessage(), e);
        }
    }
    
    private void moveToProcessedDirectory(Path filePath) throws IOException {
        Path processedPath = Paths.get(processedDirectory);
        if (!Files.exists(processedPath)) {
            Files.createDirectories(processedPath);
        }
        
        Path destination = processedPath.resolve(filePath.getFileName());
        Files.move(filePath, destination, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Moved file to processed directory: {}", destination);
    }
}