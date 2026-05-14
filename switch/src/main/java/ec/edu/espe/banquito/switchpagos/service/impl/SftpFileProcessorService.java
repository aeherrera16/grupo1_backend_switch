package ec.edu.espe.banquito.switchpagos.service.impl;

import java.io.InputStream;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChannelEnum;
import ec.edu.espe.banquito.switchpagos.model.FileValidation;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser.CsvParseResult;
import ec.edu.espe.banquito.switchpagos.service.IFileValidationService;
import ec.edu.espe.banquito.switchpagos.service.ISftpFileProcessorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SftpFileProcessorService implements ISftpFileProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(SftpFileProcessorService.class);

    private final IFileValidationService fileValidationService;

    @Autowired
    public SftpFileProcessorService(IFileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    @Override
    public FileValidation processSftpCsv(InputStream inputStream, String fileName, long fileSize) {
        ChannelEnum channel = ChannelEnum.SFTP;
        try {
            CsvParseResult parseResult = CsvBatchParser.parseCsvFile(inputStream, fileName, fileSize);
            // CsvParseResult doesn't have success/errorMessage fields
            var batch = parseResult.getBatch();
            batch.setChannel(channel);
            batch.setReceivedAt(LocalDateTime.now());
            batch.setStatus(BatchStatusEnum.RECEIVED);
            
            logger.info("Lote creado - RUC: {}, Hash: {}, Total: {}", 
                       batch.getRuc(), batch.getFileHash(), batch.getHeaderTotalAmount());

            fileValidationService.validateEarlyRejection(parseResult);

            return fileValidationService.validateBatch(batch, parseResult.getDetails());
        } catch (Exception e) {
            throw new RuntimeException("Error procesando archivo SFTP: " + e.getMessage(), e);
        }
    }
}