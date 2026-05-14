package ec.edu.espe.banquito.emailservice.service;

import java.util.List;

/**
 * Interfaz para el servicio de integración SFTP
 */
public interface ISftpIntegrationService {
    
    /**
     * Procesa archivos desde SFTP y los envía al switch
     */
    List<String> processSftpFiles();
    
    /**
     * Verifica el estado de la integración SFTP
     */
    boolean isIntegrationHealthy();
    
    /**
     * Obtiene información de la integración
     */
    String getIntegrationInfo();
}
