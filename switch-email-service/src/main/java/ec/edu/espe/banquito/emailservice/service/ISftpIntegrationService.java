package ec.edu.espe.banquito.emailservice.service;

import java.util.List;

/**
 * SFTP integration service contract
 */
public interface ISftpIntegrationService {
    
    /**
     * Processes files from SFTP and sends them to the switch
     */
    List<String> processSftpFiles();
    
    /**
     * Checks the SFTP integration status
     */
    boolean isIntegrationHealthy();
    
    /**
     * Returns integration information
     */
    String getIntegrationInfo();
}
