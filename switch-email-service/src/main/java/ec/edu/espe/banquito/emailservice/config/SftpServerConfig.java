package ec.edu.espe.banquito.emailservice.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SftpServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SftpServerConfig.class);

    @Value("${sftp.server.host:0.0.0.0}")
    private String host;

    @Value("${sftp.server.port:2222}")
    private int port;

    @Value("${sftp.server.upload-directory:./sftp-uploads}")
    private String uploadDirectory;

    @Value("${switch.api.base-url:http://localhost:8081}")
    private String coreApiBaseUrl;

    private static final java.util.concurrent.ConcurrentHashMap<String, String> USERNAME_TO_RUC = new java.util.concurrent.ConcurrentHashMap<>();

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(name = "sftp.server.enabled", havingValue = "true")
    public SshServer sshServer() throws IOException {
        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        Path keyPath = uploadPath.resolveSibling(".sftp-host-keys").resolve("hostkey.ser");
        Files.createDirectories(keyPath.getParent());

        SshServer server = SshServer.setUpDefaultServer();
        server.setHost(host);
        server.setPort(port);
        server.setKeyPairProvider(hostKeyProvider(keyPath));
        server.setPasswordAuthenticator(passwordAuthenticator());
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build()));
        VirtualFileSystemFactory fileSystemFactory = new VirtualFileSystemFactory(uploadPath) {

            public Path getUserHomeDir(org.apache.sshd.server.session.ServerSession session) {
                String ruc = USERNAME_TO_RUC.get(session.getUsername());
                if (ruc == null || ruc.isEmpty()) {
                    ruc = session.getUsername();
                }
                Path userDir = uploadPath.resolve(ruc);
                try {
                    Files.createDirectories(userDir);
                } catch (IOException e) {
                    LOG.error("Could not create user directory: {}", userDir, e);
                }
                return userDir;
            }
        };
        server.setFileSystemFactory(fileSystemFactory);
        server.start();

        LOG.info("SFTP server started on {}:{} with user and root {}", host, port, uploadPath);
        return server;
    }

    private KeyPairProvider hostKeyProvider(Path keyPath) {
        return new SimpleGeneratorHostKeyProvider(keyPath);
    }

    private PasswordAuthenticator passwordAuthenticator() {
        return (providedUsername, providedPassword, session) -> {
            try {
                org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                String url = coreApiBaseUrl.replace("8081", "8080") + "/core/v1/auth/sftp/validate";
                java.util.Map<String, String> request = new java.util.HashMap<>();
                request.put("username", providedUsername);
                request.put("password", providedPassword);
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                org.springframework.http.HttpEntity<java.util.Map<String, String>> entity = new org.springframework.http.HttpEntity<>(request, headers);
                org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.postForEntity(url, entity, java.util.Map.class);
                boolean success = response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().containsKey("ruc");
                if (success) {
                    String ruc = (String) response.getBody().get("ruc");
                    if (ruc != null && !ruc.isEmpty()) {
                        USERNAME_TO_RUC.put(providedUsername, ruc);
                        LOG.info("Authenticated SFTP user {} successfully mapped to RUC {}", providedUsername, ruc);
                    }
                }
                return success;
            } catch (Exception e) {
                LOG.warn("Failed to authenticate user {} through Core API: {}", providedUsername, e.getMessage());
                return false;
            }
        };
    }
}
