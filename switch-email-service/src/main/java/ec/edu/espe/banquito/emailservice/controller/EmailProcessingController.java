package ec.edu.espe.banquito.emailservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.espe.banquito.emailservice.service.ISftpIntegrationService;
import ec.edu.espe.banquito.emailservice.service.SftpSchedulerService;

/**
 * Controller for monitoring and managing email processing.
 */
@RestController
@RequestMapping("/api/email-processing")
public class EmailProcessingController {

    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessingController.class);

    private final ISftpIntegrationService sftpIntegrationService;
    private final SftpSchedulerService sftpSchedulerService;

    @Autowired
    public EmailProcessingController(
            ISftpIntegrationService sftpIntegrationService,
            SftpSchedulerService sftpSchedulerService) {
        this.sftpIntegrationService = sftpIntegrationService;
        this.sftpSchedulerService = sftpSchedulerService;
    }

    /**
     * Returns the system status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            boolean isHealthy = sftpIntegrationService.isIntegrationHealthy();
            String integrationInfo = sftpIntegrationService.getIntegrationInfo();
            String schedulerInfo = sftpSchedulerService.getSchedulerInfo();

            Map<String, Object> response = new HashMap<>();
            response.put("healthy", isHealthy);
            response.put("integrationInfo", integrationInfo);
            response.put("schedulerInfo", schedulerInfo);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOG.error("Error getting system status", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error getting status: " + e.getMessage()
            ));
        }
    }

    
    /**
     * Returns detailed system information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getSystemInfo() {
        try {
            String integrationInfo = sftpIntegrationService.getIntegrationInfo();
            String schedulerInfo = sftpSchedulerService.getSchedulerInfo();
            String systemInfo = "Integration: " + integrationInfo + " | Scheduler: " + schedulerInfo;
            return ResponseEntity.ok(Map.of("info", systemInfo));
        } catch (Exception e) {
            LOG.error("Error getting system information", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error getting information: " + e.getMessage()
            ));
        }
    }

    /**
     * Returns the processor health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean integrationHealthy = sftpIntegrationService.isIntegrationHealthy();
        boolean schedulerHealthy = sftpSchedulerService.isHealthy();
        
        Map<String, Object> health = new HashMap<>();
        health.put("service", "sftp-processor");
        health.put("status", (integrationHealthy && schedulerHealthy) ? "UP" : "DOWN");
        health.put("components", Map.of(
            "integration", integrationHealthy ? "UP" : "DOWN",
            "scheduler", schedulerHealthy ? "UP" : "DOWN"
        ));
        health.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(health);
    }

    /**
     * Triggers manual SFTP file processing
     */
    @PostMapping("/sftp/process")
    public ResponseEntity<Map<String, Object>> processSftpFiles() {
        try {
            LOG.info("Starting manual SFTP file processing");
            sftpSchedulerService.processSftpFiles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("processedFiles", "Processing started - check logs for details");
            response.put("count", 0);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOG.error("Error processing SFTP files manually", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error processing SFTP files: " + e.getMessage()
            ));
        }
    }

    /**
     * Returns the SFTP-specific status
     */
    @GetMapping("/sftp/status")
    public ResponseEntity<Map<String, Object>> getSftpStatus() {
        try {
            boolean isHealthy = sftpIntegrationService.isIntegrationHealthy();
            String integrationInfo = sftpIntegrationService.getIntegrationInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("service", "sftp-processor");
            response.put("healthy", isHealthy);
            response.put("integrationInfo", integrationInfo);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOG.error("Error getting SFTP status", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error getting SFTP status: " + e.getMessage()
            ));
        }
    }

    /**
     * Returns the SFTP-specific health status
     */
    @GetMapping("/sftp/health")
    public ResponseEntity<Map<String, Object>> sftpHealth() {
        try {
            boolean isHealthy = sftpIntegrationService.isIntegrationHealthy();
            
            Map<String, Object> health = new HashMap<>();
            health.put("service", "sftp-processor");
            health.put("status", isHealthy ? "UP" : "DOWN");
            health.put("components", Map.of(
                "sftp", isHealthy ? "UP" : "DOWN"
            ));
            health.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            LOG.error("Error in SFTP health check", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error in SFTP health check: " + e.getMessage()
            ));
        }
    }
}
