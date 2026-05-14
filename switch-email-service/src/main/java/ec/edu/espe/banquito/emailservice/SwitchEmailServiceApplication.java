package ec.edu.espe.banquito.emailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwitchEmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwitchEmailServiceApplication.class, args);
    }

}
