package ec.edu.espe.banquito.switchpagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import ec.edu.espe.banquito.switchpagos.config.LocalPostgresDatabaseInitializer;
import ec.edu.espe.banquito.switchpagos.config.ValidationRulesProperties;

@SpringBootApplication
@EnableConfigurationProperties(ValidationRulesProperties.class)
public class SwitchPagosApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SwitchPagosApplication.class);
        application.addInitializers(new LocalPostgresDatabaseInitializer());
        application.run(args);
    }

}
