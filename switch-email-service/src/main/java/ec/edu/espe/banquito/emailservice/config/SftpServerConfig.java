package ec.edu.espe.banquito.emailservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ec.edu.espe.banquito.emailservice.service.SftpServerService;

/**
 * Configuración del servidor SFTP embebido usando Apache SSHD
 * NOTA: Esta configuración requiere las dependencias de Apache SSHD.
 * Para habilitar el servidor SFTP, instala Maven y ejecuta:
 * mvn clean install
 */
@Configuration
public class SftpServerConfig {
    
    private static final Logger LOG = LoggerFactory.getLogger(SftpServerConfig.class);
    
    @Value("${sftp.server.enabled:false}")
    private boolean sftpServerEnabled;
    
    @Bean
    public Object sshServer(SftpServerService sftpServerService) {
        if (!sftpServerEnabled) {
            LOG.info("Servidor SFTP deshabilitado");
            return null;
        }
        
        LOG.warn("Servidor SFTP habilitado pero las dependencias de Apache SSHD no están disponibles.");
        LOG.warn("Para habilitar el servidor SFTP:");
        LOG.warn("1. Instala Maven: choco install maven -y");
        LOG.warn("2. Ejecuta: mvn clean install");
        LOG.warn("3. Reinicia la aplicación");
        
        return null;
    }
}
