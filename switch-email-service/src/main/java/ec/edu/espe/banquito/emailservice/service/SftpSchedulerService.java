package ec.edu.espe.banquito.emailservice.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.emailservice.client.SwitchApiClient;

/**
 * Scheduler service for SFTP processing
 */
@Service
public class SftpSchedulerService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SftpSchedulerService.class);
    
    private final SwitchApiClient switchApiClient;
    
    @Value("${sftp.scheduler.enabled}")
    private boolean schedulerEnabled;
    
    @Value("${sftp.scheduler.interval}")
    private String schedulerInterval;
    
    @Value("${sftp.local.directory}")
    private String localDirectory;
    
    @Autowired
    public SftpSchedulerService(SwitchApiClient switchApiClient) {
        this.switchApiClient = switchApiClient;
    }
    
    /**
     * Processes SFTP files on a schedule
     */
    @Scheduled(fixedRateString = "${sftp.scheduler.interval:60000}")
    public void processSftpFiles() {
        if (!schedulerEnabled) {
            LOG.debug("SFTP scheduler disabled");
            return;
        }
        
        LOG.info("Starting SFTP file processing cycle (interval: {})", schedulerInterval);
        
        try {
            Path uploadPath = Paths.get(localDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                LOG.info("SFTP directory created: {}", localDirectory);
            }
            
            File[] files = uploadPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            List<String> processedFiles = new ArrayList<>();
            
            if (files == null || files.length == 0) {
                LOG.info("No CSV files found in the SFTP directory");
            } else {
                LOG.info("Found {} CSV files to process", files.length);
                
                for (File file : files) {
                    try {
                        LOG.info("Processing file: {}", file.getName());
                        boolean sentToSwitch = switchApiClient.sendFileToSwitch(file);
                        
                        if (sentToSwitch) {
                            processedFiles.add(file.getName());
                            LOG.info("File {} sent to the Switch successfully", file.getName());
                            
                            Path processedDir = uploadPath.resolve("processed");
                            if (!Files.exists(processedDir)) {
                                Files.createDirectories(processedDir);
                            }
                            Files.move(file.toPath(), processedDir.resolve(file.getName()), 
                                     StandardCopyOption.REPLACE_EXISTING);
                            LOG.info("File moved to processed directory: {}", file.getName());
                        } else {
                            LOG.error("Error sending file {} to the Switch", file.getName());
                        }
                    } catch (java.io.IOException e) {
                        LOG.error("Error processing file {}: {}", file.getName(), e.getMessage());
                    }
                }
                
                if (!processedFiles.isEmpty()) {
                    LOG.info("Files processed successfully: {}", processedFiles);
                }
            }
        } catch (java.io.IOException e) {
            LOG.error("Error processing SFTP files: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Checks SFTP integration health
     */
    public boolean isHealthy() {
        return schedulerEnabled && switchApiClient.isSwitchAvailable();
    }
    
    /**
     * Returns scheduler information
     */
    public String getSchedulerInfo() {
        return String.format("SftpScheduler[enabled=%s, interval=%s, directory=%s]",
                           schedulerEnabled, schedulerInterval, localDirectory);
    }
}
