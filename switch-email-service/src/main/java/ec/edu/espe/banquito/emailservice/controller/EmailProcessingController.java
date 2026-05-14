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
 * Controlador para monitoreo y administración del procesamiento de emails
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
     * Endpoint para verificar el estado del sistema
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
            LOG.error("Error obteniendo estado del sistema", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error obteniendo estado: " + e.getMessage()
            ));
        }
    }

    
    /**
     * Endpoint para obtener información detallada del sistema
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getSystemInfo() {
        try {
            String integrationInfo = sftpIntegrationService.getIntegrationInfo();
            String schedulerInfo = sftpSchedulerService.getSchedulerInfo();
            String systemInfo = "Integration: " + integrationInfo + " | Scheduler: " + schedulerInfo;
            return ResponseEntity.ok(Map.of("info", systemInfo));
        } catch (Exception e) {
            LOG.error("Error obteniendo información del sistema", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error obteniendo información: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint de health check específico para el procesador
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

    // ===== ENDPOINTS SFTP =====

    /**
     * Endpoint para procesar archivos SFTP manualmente
     */
    @PostMapping("/sftp/process")
    public ResponseEntity<Map<String, Object>> processSftpFiles() {
        try {
            LOG.info("Iniciando procesamiento manual de archivos SFTP");
            sftpSchedulerService.processSftpFiles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("processedFiles", "Procesamiento iniciado - revisa logs para detalles");
            response.put("count", 0);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LOG.error("Error procesando archivos SFTP manualmente", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error procesando archivos SFTP: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint para verificar el estado específico de SFTP
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
            LOG.error("Error obteniendo estado SFTP", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error obteniendo estado SFTP: " + e.getMessage()
            ));
        }
    }

    /**
     * Endpoint de health check específico para SFTP
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
            LOG.error("Error en health check SFTP", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error en health check SFTP: " + e.getMessage()
            ));
        }
    }
}
