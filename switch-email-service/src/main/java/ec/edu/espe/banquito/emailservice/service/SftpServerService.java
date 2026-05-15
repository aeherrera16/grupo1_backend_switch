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
 * Service for handling files uploaded through the embedded SFTP server.
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
     * Processes files uploaded to the SFTP directory
     */
    public void processUploadedFiles() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                LOG.warn("Upload directory does not exist: {}", uploadDirectory);
                return;
            }
            
            Files.list(uploadPath)
                .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                .forEach(this::processFile);
                
        } catch (IOException e) {
            LOG.error("Error processing uploaded files: {}", e.getMessage(), e);
        }
    }
    
    private void processFile(Path filePath) {
        try {
            LOG.info("Processing uploaded file: {}", filePath.getFileName());
            
            boolean sentToSwitch = switchApiClient.sendFileToSwitch(filePath.toFile());
            
            if (sentToSwitch) {
                Path processedDir = Paths.get(uploadDirectory, "processed");
                Files.createDirectories(processedDir);
                Path target = processedDir.resolve(filePath.getFileName());
                Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("File sent to the Switch and moved to processed directory: {}", filePath.getFileName());
            } else {
                Path errorDir = Paths.get(uploadDirectory, "errors");
                Files.createDirectories(errorDir);
                Path target = errorDir.resolve(filePath.getFileName());
                Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.warn("File moved to error directory: {}", filePath.getFileName());
            }
            
        } catch (IOException e) {
            LOG.error("Error processing file {}: {}", filePath, e.getMessage(), e);
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
