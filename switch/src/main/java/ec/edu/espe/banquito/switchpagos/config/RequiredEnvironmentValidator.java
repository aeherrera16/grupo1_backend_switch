package ec.edu.espe.banquito.switchpagos.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class RequiredEnvironmentValidator {

    private final Environment environment;

    public RequiredEnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateRequiredConfiguration() {
        validateNotBlank("spring.datasource.url");
        validateNotBlank("spring.datasource.username");
        validateNotBlank("spring.datasource.password");
    }

    private void validateNotBlank(String propertyName) {
        String value = environment.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration property: " + propertyName);
        }
    }
}
