package ec.edu.espe.banquito.emailservice.service;

import java.util.List;

public interface ISftpIntegrationService {

    List<String> processSftpFiles();

    boolean isIntegrationHealthy();

    String getIntegrationInfo();
}
