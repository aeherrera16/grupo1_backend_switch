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

/**
 * Embedded SFTP server configuration using Apache SSHD.
 */
@Configuration
public class SftpServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SftpServerConfig.class);

    @Value("${sftp.server.host:0.0.0.0}")
    private String host;

    @Value("${sftp.server.port:2222}")
    private int port;

    @Value("${sftp.server.upload-directory:./sftp-uploads}")
    private String uploadDirectory;

    @Value("${sftp.server.username:sftpuser}")
    private String username;

    @Value("${sftp.server.password:password}")
    private String password;

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
        server.setFileSystemFactory(new VirtualFileSystemFactory(uploadPath));
        server.start();

        LOG.info("SFTP server started on {}:{} with user '{}' and root {}", host, port, username, uploadPath);
        return server;
    }

    private KeyPairProvider hostKeyProvider(Path keyPath) {
        return new SimpleGeneratorHostKeyProvider(keyPath);
    }

    private PasswordAuthenticator passwordAuthenticator() {
        return (providedUsername, providedPassword, session) ->
                username.equals(providedUsername) && password.equals(providedPassword);
    }
}
