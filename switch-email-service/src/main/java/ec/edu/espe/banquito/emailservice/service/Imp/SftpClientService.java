package ec.edu.espe.banquito.emailservice.service.Imp;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import ec.edu.espe.banquito.emailservice.service.ISftpClientService;

/**
 * Implementación del servicio de cliente SFTP para descargar archivos
 */
@Service
public class SftpClientService implements ISftpClientService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SftpClientService.class);
    
    @Value("${sftp.host}")
    private String sftpHost;
    
    @Value("${sftp.port}")
    private int sftpPort;
    
    @Value("${sftp.username}")
    private String sftpUsername;
    
    @Value("${sftp.password}")
    private String sftpPassword;
    
    @Value("${sftp.remote.directory}")
    private String sftpRemoteDirectory;
    
    @Value("${sftp.local.directory}")
    private String sftpLocalDirectory;
    
    private Session session;
    private ChannelSftp channelSftp;
    
    @Override
    public boolean connect() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);
            
            // Configurar para evitar verificación de host key (solo para desarrollo)
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            
            LOG.info("Conectado exitosamente al servidor SFTP: {}:{}", sftpHost, sftpPort);
            return true;
            
        } catch (JSchException e) {
            LOG.error("Error conectando al servidor SFTP: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public void disconnect() {
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
                LOG.info("Canal SFTP desconectado");
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
                LOG.info("Sesión SFTP desconectada");
            }
        } catch (Exception e) {
            LOG.warn("Error durante la desconexión SFTP: {}", e.getMessage());
        }
    }
    
    @Override
    public List<String> listCsvFiles(String remoteDirectory) {
        List<String> csvFiles = new ArrayList<>();
        
        if (!isConnected()) {
            LOG.warn("No hay conexión SFTP activa");
            return csvFiles;
        }
        
        try {
            channelSftp.cd(remoteDirectory);
            Vector<ChannelSftp.LsEntry> files = channelSftp.ls(".");
            
            for (ChannelSftp.LsEntry entry : files) {
                if (!entry.getAttrs().isDir() && 
                    entry.getFilename().toLowerCase().endsWith(".csv")) {
                    csvFiles.add(entry.getFilename());
                }
            }
            
            LOG.info("Encontrados {} archivos CSV en {}", csvFiles.size(), remoteDirectory);
            
        } catch (SftpException e) {
            LOG.error("Error listando archivos en {}: {}", remoteDirectory, e.getMessage());
        }
        
        return csvFiles;
    }
    
    @Override
    public boolean downloadFile(String remoteFilePath, String localFilePath) {
        if (!isConnected()) {
            LOG.warn("No hay conexión SFTP activa");
            return false;
        }
        
        try {
            // Crear directorio local si no existe
            Path localPath = Paths.get(localFilePath);
            Files.createDirectories(localPath.getParent());
            
            // Descargar archivo
            try (InputStream remoteStream = channelSftp.get(remoteFilePath);
                 OutputStream localStream = Files.newOutputStream(localPath)) {
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = remoteStream.read(buffer)) != -1) {
                    localStream.write(buffer, 0, bytesRead);
                }
            }
            
            LOG.info("Archivo descargado exitosamente: {} -> {}", remoteFilePath, localFilePath);
            return true;
            
        } catch (Exception e) {
            LOG.error("Error descargando archivo {}: {}", remoteFilePath, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteRemoteFile(String remoteFilePath) {
        if (!isConnected()) {
            LOG.warn("No hay conexión SFTP activa");
            return false;
        }
        
        try {
            channelSftp.rm(remoteFilePath);
            LOG.info("Archivo remoto eliminado: {}", remoteFilePath);
            return true;
            
        } catch (SftpException e) {
            LOG.error("Error eliminando archivo remoto {}: {}", remoteFilePath, e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isConnected() {
        return session != null && session.isConnected() && 
               channelSftp != null && channelSftp.isConnected();
    }
    
    @Override
    public String getServerInfo() {
        return String.format("SFTP[host=%s, port=%d, user=%s, remoteDir=%s, localDir=%s, connected=%s]",
                           sftpHost, sftpPort, sftpUsername, sftpRemoteDirectory, sftpLocalDirectory, isConnected());
    }
    
    /**
     * Conecta automáticamente si no está conectado
     */
    private boolean ensureConnected() {
        if (!isConnected()) {
            return connect();
        }
        return true;
    }
    
    /**
     * Método conveniente para descargar todos los archivos CSV
     */
    public List<String> downloadAllCsvFiles() {
        List<String> downloadedFiles = new ArrayList<>();
        
        if (!ensureConnected()) {
            return downloadedFiles;
        }
        
        try {
            List<String> csvFiles = listCsvFiles(sftpRemoteDirectory);
            
            for (String csvFile : csvFiles) {
                String remotePath = sftpRemoteDirectory + "/" + csvFile;
                String localPath = sftpLocalDirectory + "/" + csvFile;
                
                if (downloadFile(remotePath, localPath)) {
                    downloadedFiles.add(localPath);
                    
                    // Eliminar archivo remoto después de descargar exitosamente
                    deleteRemoteFile(remotePath);
                }
            }
            
            LOG.info("Descargados {} archivos CSV exitosamente", downloadedFiles.size());
            
        } catch (Exception e) {
            LOG.error("Error en descarga masiva de archivos: {}", e.getMessage());
        }
        
        return downloadedFiles;
    }
}
