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
 * Servicio scheduler para la integración SFTP
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
     * Tarea programada para procesar archivos SFTP
     */
    @Scheduled(fixedRateString = "${sftp.scheduler.interval:60000}")
    public void processSftpFiles() {
        if (!schedulerEnabled) {
            LOG.debug("Scheduler SFTP deshabilitado");
            return;
        }
        
        LOG.info("Iniciando ciclo de procesamiento de archivos SFTP (intervalo: {})", schedulerInterval);
        
        try {
            Path uploadPath = Paths.get(localDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                LOG.info("Directorio SFTP creado: {}", localDirectory);
            }
            
            File[] files = uploadPath.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            List<String> processedFiles = new ArrayList<>();
            
            if (files == null || files.length == 0) {
                LOG.info("No hay archivos CSV en el directorio SFTP para procesar");
            } else {
                LOG.info("Encontrados {} archivos CSV para procesar", files.length);
                
                for (File file : files) {
                    try {
                        LOG.info("Procesando archivo: {}", file.getName());
                        boolean sentToSwitch = switchApiClient.sendFileToSwitch(file);
                        
                        if (sentToSwitch) {
                            processedFiles.add(file.getName());
                            LOG.info("Archivo {} enviado al Switch exitosamente", file.getName());
                            
                            // Mover archivo a procesados
                            Path processedDir = uploadPath.resolve("procesados");
                            if (!Files.exists(processedDir)) {
                                Files.createDirectories(processedDir);
                            }
                            Files.move(file.toPath(), processedDir.resolve(file.getName()), 
                                     StandardCopyOption.REPLACE_EXISTING);
                            LOG.info("Archivo movido a procesados: {}", file.getName());
                        } else {
                            LOG.error("Error enviando archivo {} al Switch", file.getName());
                        }
                    } catch (java.io.IOException e) {
                        LOG.error("Error procesando archivo {}: {}", file.getName(), e.getMessage());
                    }
                }
                
                if (!processedFiles.isEmpty()) {
                    LOG.info("Archivos procesados exitosamente: {}", processedFiles);
                }
            }
        } catch (java.io.IOException e) {
            LOG.error("Error en procesamiento de archivos SFTP: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verifica la salud de la integración SFTP
     */
    public boolean isHealthy() {
        return schedulerEnabled && switchApiClient.isSwitchAvailable();
    }
    
    /**
     * Obtiene información del scheduler
     */
    public String getSchedulerInfo() {
        return String.format("SftpScheduler[enabled=%s, interval=%s, directory=%s]",
                           schedulerEnabled, schedulerInterval, localDirectory);
    }
}
