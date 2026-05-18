package ec.edu.espe.banquito.emailservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.emailservice.client.SwitchApiClient;

@Service
public class SftpServerService {

    private static final Logger LOG = LoggerFactory.getLogger(SftpServerService.class);

    private final SwitchApiClient switchApiClient;

    @Value("${sftp.server.upload-directory:./sftp-uploads}")
    private String uploadDirectory;

    @Value("${sftp.server.enabled:false}")
    private boolean sftpServerEnabled;

    @Autowired
    public SftpServerService(SwitchApiClient switchApiClient) {
        this.switchApiClient = switchApiClient;
    }

    public void processUploadedFiles() {
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                LOG.warn("Upload directory does not exist: {}", uploadDirectory);
                return;
            }
            // First process any CSV files placed directly in the upload directory
            try {
                Files.list(uploadPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .forEach(file -> processFile(file, null));
            } catch (IOException e) {
                LOG.warn("No regular files to process in upload directory: {}", e.getMessage());
            }

            // Then process CSV files placed under user subdirectories (legacy behavior)
            Files.list(uploadPath)
                .filter(Files::isDirectory)
                .filter(path -> !path.getFileName().toString().equals("processed") && !path.getFileName().toString().equals("errors"))
                .forEach(userDir -> {
                    try {
                        Files.list(userDir)
                            .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                            .forEach(file -> processFile(file, userDir.getFileName().toString()));
                    } catch (IOException e) {
                        LOG.error("Error processing files for user {}: {}", userDir.getFileName(), e.getMessage());
                    }
                });

        } catch (IOException e) {
            LOG.error("Error processing uploaded files: {}", e.getMessage(), e);
        }
    }

    private void processFile(Path filePath, String ruc) {
        try {
            LOG.info("Processing uploaded file: {}", filePath.getFileName());

            boolean sentToSwitch = switchApiClient.sendFileToSwitch(filePath.toFile(), ruc);

            if (sentToSwitch) {
                Path processedDir = Paths.get(uploadDirectory, "processed");
                Files.createDirectories(processedDir);
                Path target = processedDir.resolve(filePath.getFileName());
                Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.info("File sent to the Switch and moved to processed directory: {}", filePath.getFileName());
            } else {
                Path errorDir = Paths.get(uploadDirectory, "errors");
                Files.createDirectories(errorDir);
                Path target = errorDir.resolve(filePath.getFileName());
                Files.move(filePath, target, StandardCopyOption.REPLACE_EXISTING);
                LOG.warn("File moved to error directory: {}", filePath.getFileName());
            }

        } catch (IOException e) {
            LOG.error("Error processing file {}: {}", filePath, e.getMessage(), e);
        }
    }

    public boolean isServerRunning() {
        return sftpServerEnabled;
    }

    public String getServerInfo() {
        return String.format("SFTP Server[enabled=%s, uploadDir=%s]",
                           sftpServerEnabled, uploadDirectory);
    }
}
