package ec.edu.espe.banquito.switchpagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import ec.edu.espe.banquito.switchpagos.config.ValidationRulesProperties;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(ValidationRulesProperties.class)
public class SwitchPagosApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SwitchPagosApplication.class);
        application.run(args);
    }

}
