package ec.edu.espe.banquito.emailservice.service;

import java.util.List;

public interface ISftpClientService {

    boolean connect();

    void disconnect();

    List<String> listCsvFiles(String remoteDirectory);

    boolean downloadFile(String remoteFilePath, String localFilePath);

    boolean deleteRemoteFile(String remoteFilePath);

    boolean isConnected();

    String getServerInfo();
}
