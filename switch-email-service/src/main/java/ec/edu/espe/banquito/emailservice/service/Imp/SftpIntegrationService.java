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
            LOG.info("SFTP integration disabled");
            return processedFiles;
        }

        LOG.info("Starting SFTP file processing");

        try {
            if (!sftpClientService.connect()) {
                LOG.error("Could not connect to SFTP server");
                return processedFiles;
            }

            List<String> downloadedFiles = downloadSftpFiles();

            for (String filePath : downloadedFiles) {
                if (processDownloadedFile(filePath)) {
                    processedFiles.add(filePath);
                }
            }

            LOG.info("SFTP processing completed: {} files processed", processedFiles.size());

        } catch (RuntimeException e) {
            LOG.error("Error during SFTP processing: {}", e.getMessage(), e);
        } finally {
            sftpClientService.disconnect();
        }

        return processedFiles;
    }

    private List<String> downloadSftpFiles() {
        List<String> downloadedFiles = new ArrayList<>();

        try {
            Files.createDirectories(Paths.get(sftpLocalDirectory));

            List<String> csvFiles = sftpClientService.listCsvFiles("/upload");

            for (String csvFile : csvFiles) {
                String remotePath = "/upload/" + csvFile;
                String localPath = sftpLocalDirectory + "/" + csvFile;

                if (sftpClientService.downloadFile(remotePath, localPath)) {
                    downloadedFiles.add(localPath);

                    sftpClientService.deleteRemoteFile(remotePath);
                    LOG.info("File {} downloaded and deleted from server", csvFile);
                }
            }

        } catch (java.io.IOException e) {
            LOG.error("Error downloading SFTP files: {}", e.getMessage());
        }

        return downloadedFiles;
    }

    private boolean processDownloadedFile(String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists() || !file.canRead()) {
                LOG.warn("Invalid or unreadable file: {}", filePath);
                return false;
            }

            LOG.info("Processing downloaded file: {}", file.getName());

            boolean success = switchApiClient.sendFileToSwitch(file, null);

            if (success) {
                moveToProcessed(file);
                LOG.info("File sent to switch successfully: {}", file.getName());
                return true;
            } else {
                moveToError(file);
                LOG.warn("Error sending file to switch: {}", file.getName());
                return false;
            }

        } catch (java.io.IOException | RuntimeException e) {
            LOG.error("Error processing downloaded file {}: {}", filePath, e.getMessage());

            try {
                moveToError(new File(filePath));
            } catch (java.io.IOException moveError) {
                LOG.error("Error moving file to error directory: {}", moveError.getMessage());
            }

            return false;
        }
    }

    private void moveToProcessed(File file) throws java.io.IOException {
        Path processedDir = Paths.get(sftpLocalDirectory, "processed");
        Files.createDirectories(processedDir);

        Path target = processedDir.resolve(file.getName());
        Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        LOG.debug("File moved to processed directory: {}", target);
    }

    private void moveToError(File file) throws java.io.IOException {
        Path errorDir = Paths.get(sftpLocalDirectory, "errors");
        Files.createDirectories(errorDir);

        Path target = errorDir.resolve(file.getName());
        Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

        LOG.debug("File moved to error directory: {}", target);
    }

    @Override
    public boolean isIntegrationHealthy() {
        if (!integrationEnabled) {
            return true;
        }

        try {
            boolean connected = sftpClientService.connect();
            if (connected) {
                sftpClientService.disconnect();
            }
            return connected;
        } catch (RuntimeException e) {
            LOG.warn("SFTP health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getIntegrationInfo() {
        return String.format("SftpIntegration[enabled=%s, %s, localDir=%s]",
                           integrationEnabled, sftpClientService.getServerInfo(), sftpLocalDirectory);
    }
}
