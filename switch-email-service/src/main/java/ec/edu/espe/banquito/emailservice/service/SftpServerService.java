package ec.edu.espe.banquito.emailservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.emailservice.client.SwitchApiClient;

/**
 * Servicio para gestionar el servidor SFTP embebido
 * NOTA: Este servicio requiere las dependencias de Apache SSHD para funcionar como servidor.
 * Para habilitar el servidor SFTP, instala Maven y ejecuta: mvn clean install
 */
@Service
public class SftpServerService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SftpServerService.class);
    
    private final SwitchApiClient switchApiClient;
    
    @Value("${sftp.server.upload-directory:./sftp-uploads}")
    private String uploadDirectory;
    
    @Value("${sftp.server.enabled:false}")
    private boolean sftpServerEnabled;
    
    @Autowired
    public SftpServerService(SwitchApiClient switchApiClient) {
        this.switchApiClient = switchApiClient;
    }
    
    /**
     * Procesa los archivos subidos al directorio SFTP
     */
    public void processUploadedFiles() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                LOG.warn("Directorio de uploads no existe: {}", uploadDirectory);
                return;
            }
            
            Files.list(uploadPath)
                .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                .forEach(this::processFile);
                
        } catch (IOException e) {
            LOG.error("Error procesando archivos subidos: {}", e.getMessage(), e);
        }
    }
    
    private void processFile(Path filePath) {
        try {
            LOG.info("Procesando archivo subido: {}", filePath.getFileName());
            
            boolean sentToSwitch = switchApiClient.sendFileToSwitch(filePath.toFile());
            
            if (sentToSwitch) {
                // Mover a procesados
                Path processedDir = Paths.get(uploadDirectory, "processed");
                Files.createDirectories(processedDir);
                Path target = processedDir.resolve(filePath.getFileName());
                Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("Archivo enviado al Switch y movido a procesados: {}", filePath.getFileName());
            } else {
                // Mover a errores
                Path errorDir = Paths.get(uploadDirectory, "errors");
                Files.createDirectories(errorDir);
                Path target = errorDir.resolve(filePath.getFileName());
                Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.warn("Archivo movido a errores: {}", filePath.getFileName());
            }
            
        } catch (IOException e) {
            LOG.error("Error procesando archivo {}: {}", filePath, e.getMessage(), e);
        }
    }
    
    public boolean isServerRunning() {
        return sftpServerEnabled;
    }
    
    public String getServerInfo() {
        return String.format("SFTP Server[enabled=%s, uploadDir=%s]", 
                           sftpServerEnabled, uploadDirectory);
    }
}
