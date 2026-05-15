package ec.edu.espe.banquito.emailservice.service;

import java.util.List;

/**
 * SFTP client service contract
 */
public interface ISftpClientService {
    
    /**
     * Connects to the SFTP server.
     */
    boolean connect();
    
    /**
     * Disconnects from the SFTP server.
     */
    void disconnect();
    
    /**
     * Lists CSV files in the remote directory
     */
    List<String> listCsvFiles(String remoteDirectory);
    
    /**
     * Downloads a file from the SFTP server.
     */
    boolean downloadFile(String remoteFilePath, String localFilePath);
    
    /**
     * Deletes a file from the SFTP server after download.
     */
    boolean deleteRemoteFile(String remoteFilePath);
    
    /**
     * Checks whether the SFTP connection is active
     */
    boolean isConnected();
    
    /**
     * Returns SFTP server information.
     */
    String getServerInfo();
}
