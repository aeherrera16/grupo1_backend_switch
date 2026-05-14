package ec.edu.espe.banquito.emailservice.service.Imp;

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
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.emailservice.client.SwitchApiClient;
import ec.edu.espe.banquito.emailservice.service.ISftpClientService;
import ec.edu.espe.banquito.emailservice.service.ISftpIntegrationService;

/**
 * Implementación del servicio de integración SFTP
 */
@Service
public class SftpIntegrationService implements ISftpIntegrationService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SftpIntegrationService.class);
    
    private final ISftpClientService sftpClientService;
    private final SwitchApiClient switchApiClient;
    
    @Value("${sftp.local.directory}")
    private String sftpLocalDirectory;
    
    @Value("${sftp.integration.enabled}")
    private boolean integrationEnabled;
    
    @Autowired
    public SftpIntegrationService(ISftpClientService sftpClientService, SwitchApiClient switchApiClient) {
        this.sftpClientService = sftpClientService;
        this.switchApiClient = switchApiClient;
    }
    
    @Override
    public List<String> processSftpFiles() {
        List<String> processedFiles = new ArrayList<>();
        
        if (!integrationEnabled) {
            LOG.info("Integracion SFTP deshabilitada");
            return processedFiles;
        }
        
        LOG.info("Iniciando procesamiento de archivos SFTP");
        
        try {
            // 1. Conectar al servidor SFTP
            if (!sftpClientService.connect()) {
                LOG.error("No se pudo conectar al servidor SFTP");
                return processedFiles;
            }
            
            // 2. Descargar archivos CSV
            List<String> downloadedFiles = downloadSftpFiles();
            
            // 3. Procesar cada archivo descargado
            for (String filePath : downloadedFiles) {
                if (processDownloadedFile(filePath)) {
                    processedFiles.add(filePath);
                }
            }
            
            LOG.info("Procesamiento SFTP completado: {} archivos procesados", processedFiles.size());
            
        } catch (RuntimeException e) {
            LOG.error("Error en procesamiento SFTP: {}", e.getMessage(), e);
        } finally {
            // 4. Desconectar del servidor SFTP
            sftpClientService.disconnect();
        }
        
        return processedFiles;
    }
    
    private List<String> downloadSftpFiles() {
        List<String> downloadedFiles = new ArrayList<>();
        
        try {
            // Crear directorio local si no existe
            Files.createDirectories(Paths.get(sftpLocalDirectory));
            
            // Listar archivos CSV en el servidor remoto
            List<String> csvFiles = sftpClientService.listCsvFiles("/upload");
            
            for (String csvFile : csvFiles) {
                String remotePath = "/upload/" + csvFile;
                String localPath = sftpLocalDirectory + "/" + csvFile;
                
                if (sftpClientService.downloadFile(remotePath, localPath)) {
                    downloadedFiles.add(localPath);
                    
                    // Eliminar archivo remoto después de descargar exitosamente
                    sftpClientService.deleteRemoteFile(remotePath);
                    LOG.info("Archivo {} descargado y eliminado del servidor", csvFile);
                }
            }
            
        } catch (java.io.IOException e) {
            LOG.error("Error descargando archivos SFTP: {}", e.getMessage());
        }
        
        return downloadedFiles;
    }
    
    private boolean processDownloadedFile(String filePath) {
        try {
            File file = new File(filePath);
            
            if (!file.exists() || !file.canRead()) {
                LOG.warn("Archivo no valido o no legible: {}", filePath);
                return false;
            }
            
            LOG.info("Procesando archivo descargado: {}", file.getName());
            
            // Enviar archivo al switch principal
            boolean success = switchApiClient.sendFileToSwitch(file);
            
            if (success) {
                // Mover archivo a procesados
                moveToProcessed(file);
                LOG.info("Archivo enviado exitosamente al switch: {}", file.getName());
                return true;
            } else {
                // Mover archivo a errores
                moveToError(file);
                LOG.warn("Error al enviar archivo al switch: {}", file.getName());
                return false;
            }
            
        } catch (java.io.IOException | RuntimeException e) {
            LOG.error("Error procesando archivo descargado {}: {}", filePath, e.getMessage());
            
            // Intentar mover a errores
            try {
                moveToError(new File(filePath));
            } catch (java.io.IOException moveError) {
                LOG.error("Error moviendo archivo a errores: {}", moveError.getMessage());
            }
            
            return false;
        }
    }
    
    private void moveToProcessed(File file) throws java.io.IOException {
        Path processedDir = Paths.get(sftpLocalDirectory, "processed");
        Files.createDirectories(processedDir);
        
        Path target = processedDir.resolve(file.getName());
        Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        
        LOG.debug("Archivo movido a procesados: {}", target);
    }
    
    private void moveToError(File file) throws java.io.IOException {
        Path errorDir = Paths.get(sftpLocalDirectory, "errors");
        Files.createDirectories(errorDir);
        
        Path target = errorDir.resolve(file.getName());
        Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        
        LOG.debug("Archivo movido a errores: {}", target);
    }
    
    @Override
    public boolean isIntegrationHealthy() {
        if (!integrationEnabled) {
            return false;
        }
        
        try {
            // Intentar conectar para verificar salud
            boolean connected = sftpClientService.connect();
            if (connected) {
                sftpClientService.disconnect();
            }
            return connected;
        } catch (RuntimeException e) {
            LOG.warn("Verificacion de salud SFTP fallo: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getIntegrationInfo() {
        return String.format("SftpIntegration[enabled=%s, %s, localDir=%s]",
                           integrationEnabled, sftpClientService.getServerInfo(), sftpLocalDirectory);
    }
}
