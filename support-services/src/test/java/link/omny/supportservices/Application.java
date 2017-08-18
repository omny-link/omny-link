package link.omny.supportservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableAutoConfiguration
@Configuration
@Import({ SupportServicesConfig.class })
@ComponentScan(basePackages = { "link.omny.supportservices" })
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
