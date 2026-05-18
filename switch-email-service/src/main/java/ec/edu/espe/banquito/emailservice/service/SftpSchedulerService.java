package ec.edu.espe.banquito.emailservice.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ec.edu.espe.banquito.emailservice.client.SwitchApiClient;

@Service
public class SftpSchedulerService {

    private static final Logger LOG = LoggerFactory.getLogger(SftpSchedulerService.class);

    private final SwitchApiClient switchApiClient;

    @Value("${sftp.scheduler.enabled}")
    private boolean schedulerEnabled;

    @Value("${sftp.scheduler.interval}")
    private String schedulerInterval;

    // Usar el directorio real del servidor SFTP embebido, no sftp.local.directory
    @Value("${sftp.server.upload-directory:./sftp-uploads}")
    private String uploadDirectory;

    @Autowired
    public SftpSchedulerService(SwitchApiClient switchApiClient) {
        this.switchApiClient = switchApiClient;
    }

    @Scheduled(fixedRateString = "${sftp.scheduler.interval:10000}")
    public void processSftpFiles() {
        if (!schedulerEnabled) {
            LOG.debug("SFTP scheduler disabled");
            return;
        }

        LOG.info("Starting SFTP file processing cycle (interval: {})", schedulerInterval);

        try {
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                return;
            }

            // Procesar CSVs en el directorio raíz
            processFilesInDirectory(uploadPath, null);

            // Procesar CSVs en subdirectorios de usuario (estructura por RUC)
            Files.list(uploadPath)
                .filter(Files::isDirectory)
                .filter(p -> {
                    String name = p.getFileName().toString();
                    return !name.equals("processed") && !name.equals("errors");
                })
                .forEach(userDir -> {
                    String ruc = userDir.getFileName().toString();
                    processFilesInDirectory(userDir, ruc);
                });

        } catch (IOException e) {
            LOG.error("Error processing SFTP files: {}", e.getMessage(), e);
        }
    }

    private void processFilesInDirectory(Path dir, String ruc) {
        File[] files = dir.toFile().listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
        if (files == null || files.length == 0) return;

        LOG.info("Found {} CSV file(s) in {} (ruc={})", files.length, dir, ruc);

        for (File file : files) {
            try {
                String errorReason = switchApiClient.sendFileToSwitch(file, ruc);
                boolean sent = errorReason == null;

                Path destRoot = Paths.get(uploadDirectory);
                Path destDir = destRoot.resolve(sent ? "processed" : "errors");
                Files.createDirectories(destDir);
                Files.move(file.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);

                if (sent) {
                    LOG.info("File {} sent to Switch and moved to processed/", file.getName());
                } else {
                    LOG.warn("File {} failed to send ({}), moved to errors/", file.getName(), errorReason);
                    writeErrorReason(destDir, file.getName(), errorReason);
                }
            } catch (IOException e) {
                LOG.error("Error processing file {}: {}", file.getName(), e.getMessage(), e);
            }
        }
    }

    private void writeErrorReason(Path errorsDir, String csvFileName, String reason) {
        try {
            String txtName = csvFileName.replaceAll("(?i)\\.csv$", "") + ".motivo.txt";
            Path txtFile = errorsDir.resolve(txtName);
            String content = "Archivo: " + csvFileName + "\n"
                    + "Motivo de rechazo: " + reason + "\n"
                    + "Fecha: " + java.time.LocalDateTime.now() + "\n";
            Files.writeString(txtFile, content);
        } catch (IOException e) {
            LOG.warn("Could not write error reason file for {}: {}", csvFileName, e.getMessage());
        }
    }

    public boolean isHealthy() {
        return schedulerEnabled && switchApiClient.isSwitchAvailable();
    }

    public String getSchedulerInfo() {
        return String.format("SftpScheduler[enabled=%s, interval=%s, directory=%s]",
                schedulerEnabled, schedulerInterval, uploadDirectory);
    }
}
