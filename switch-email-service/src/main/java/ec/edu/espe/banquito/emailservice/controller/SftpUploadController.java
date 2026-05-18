package ec.edu.espe.banquito.emailservice.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ec.edu.espe.banquito.emailservice.client.SwitchApiClient;

@RestController
@RequestMapping("/api/sftp")
public class SftpUploadController {

    private static final Logger LOG = LoggerFactory.getLogger(SftpUploadController.class);

    private final SwitchApiClient switchApiClient;

    @Value("${sftp.local.directory}")
    private String localDirectory;

    @Autowired
    public SftpUploadController(SwitchApiClient switchApiClient) {
        this.switchApiClient = switchApiClient;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> receiveSftpFile(@RequestParam("file") MultipartFile file) {
        LOG.info("SFTP file received: {}", file.getOriginalFilename());
        LOG.info("Size: {} bytes", file.getSize());

        try {
            Path uploadPath = Paths.get(localDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                LOG.info("Directory created: {}", localDirectory);
            }

            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            LOG.info("File saved at: {}", filePath);

            LOG.info("Sending file to the main Switch");
            File savedFile = filePath.toFile();
            boolean sentToSwitch = switchApiClient.sendFileToSwitch(savedFile, null);

            Map<String, Object> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("size", file.getSize());
            response.put("savedPath", filePath.toString());
            response.put("sentToSwitch", sentToSwitch);
            response.put("timestamp", java.time.LocalDateTime.now());

            if (sentToSwitch) {
                Path processedPath = moveToSubdirectory(filePath, "processed");
                LOG.info("File processed and sent to the Switch successfully");
                response.put("processedPath", processedPath.toString());
                response.put("status", "SUCCESS");
                return ResponseEntity.ok(response);
            } else {
                Path errorPath = moveToSubdirectory(filePath, "errors");
                LOG.error("Error sending file to the Switch");
                response.put("errorPath", errorPath.toString());
                response.put("status", "ERROR_SENDING_TO_SWITCH");
                return ResponseEntity.internalServerError().body(response);
            }

        } catch (IOException e) {
            LOG.error("Error processing SFTP file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error processing file: " + e.getMessage(),
                "status", "ERROR_PROCESSING_FILE"
            ));
        }
    }

    private Path moveToSubdirectory(Path filePath, String subdirectory) throws IOException {
        Path targetDirectory = filePath.getParent().resolve(subdirectory);
        Files.createDirectories(targetDirectory);
        Path targetPath = targetDirectory.resolve(filePath.getFileName());
        Files.move(filePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getSftpServerStatus() {
        LOG.info("Checking SFTP server status");

        try {
            Path uploadPath = Paths.get(localDirectory);
            boolean directoryExists = Files.exists(uploadPath);
            long directorySize = 0;
            if (directoryExists) {
                try (Stream<Path> paths = Files.walk(uploadPath)) {
                    directorySize = paths
                        .filter(Files::isRegularFile)
                        .mapToLong(p -> {
                            try {
                                return Files.size(p);
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .sum();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("service", "sftp-server");
            response.put("status", "RUNNING");
            response.put("directory", localDirectory);
            response.put("directoryExists", directoryExists);
            response.put("directorySize", directorySize);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            LOG.error("Error checking SFTP status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error checking status: " + e.getMessage()
            ));
        } catch (RuntimeException e) {
            LOG.error("Error checking SFTP status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error checking status: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSftpServerStatusGet() {
        return getSftpServerStatus();
    }
}
