package ec.edu.espe.banquito.emailservice.service;

import java.util.List;

/**
 * Interfaz para el servicio de cliente SFTP
 */
public interface ISftpClientService {
    
    /**
     * Conecta al servidor SFTP
     */
    boolean connect();
    
    /**
     * Desconecta del servidor SFTP
     */
    void disconnect();
    
    /**
     * Lista los archivos CSV en el directorio remoto
     */
    List<String> listCsvFiles(String remoteDirectory);
    
    /**
     * Descarga un archivo del servidor SFTP
     */
    boolean downloadFile(String remoteFilePath, String localFilePath);
    
    /**
     * Elimina un archivo del servidor SFTP después de descargarlo
     */
    boolean deleteRemoteFile(String remoteFilePath);
    
    /**
     * Verifica si la conexión está activa
     */
    boolean isConnected();
    
    /**
     * Obtiene información del servidor SFTP
     */
    String getServerInfo();
}
