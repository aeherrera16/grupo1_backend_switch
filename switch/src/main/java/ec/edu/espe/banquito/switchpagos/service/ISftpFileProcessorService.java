package ec.edu.espe.banquito.switchpagos.service;

import java.io.InputStream;

import ec.edu.espe.banquito.switchpagos.model.FileValidation;

public interface ISftpFileProcessorService {
    FileValidation processSftpCsv(InputStream inputStream, String fileName, long fileSize);
}
