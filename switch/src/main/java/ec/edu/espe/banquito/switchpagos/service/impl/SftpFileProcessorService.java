package ec.edu.espe.banquito.switchpagos.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser;
import ec.edu.espe.banquito.switchpagos.config.CsvBatchParser.CsvParseResult;
import ec.edu.espe.banquito.switchpagos.enums.BatchStatusEnum;
import ec.edu.espe.banquito.switchpagos.enums.ChannelEnum;
import ec.edu.espe.banquito.switchpagos.model.FileValidation;
import ec.edu.espe.banquito.switchpagos.service.ISftpFileProcessorService;

@Service
public class SftpFileProcessorService implements ISftpFileProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(SftpFileProcessorService.class);

    private final FileValidationService fileValidationService;

    @Autowired
    public SftpFileProcessorService(FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    @Override
    public FileValidation processSftpCsv(InputStream inputStream, String fileName, long fileSize) {
        ChannelEnum channel = ChannelEnum.SFTP;
        try {
            CsvParseResult parseResult = CsvBatchParser.parseCsvFile(inputStream, fileName, fileSize);
            // RF-02: Parse SFTP CSV payload.
            var batch = parseResult.getBatch();
            batch.setChannel(channel);
            batch.setReceivedAt(LocalDateTime.now());
            batch.setStatus(BatchStatusEnum.RECEIVED);
            
            logger.info("Batch created - RUC: {}, Hash: {}, Total: {}", 
                       batch.getRuc(), batch.getFileHash(), batch.getHeaderTotalAmount());

            fileValidationService.validateEarlyRejection(parseResult);

            return fileValidationService.validateBatch(batch, parseResult.getDetails());
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo archivo SFTP: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Archivo SFTP inválido: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error procesando archivo SFTP: " + e.getMessage(), e);
        }
    }
}
