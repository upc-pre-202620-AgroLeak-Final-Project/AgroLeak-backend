package pe.edu.upc.agroleak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pe.edu.upc.agroleak.monitoring.infrastructure.config.DetectionProperties;

@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(DetectionProperties.class)
public class AgroLeakApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgroLeakApplication.class, args);
    }
}
