package link.omny.custmgmt;

import io.onedecision.engine.OneDecisionConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.knowprocess.bpm.BpmConfiguration;
import com.knowprocess.bpm.api.ActivitiApplicationSecurity;


@Configuration
@Import({ OneDecisionConfig.class, BpmConfiguration.class,
        CustMgmtConfig.class })
@ComponentScan(basePackages = { "link.omny.custmgmt", "io.onedecision.engine" })
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Allegedly sets welcome page though does not appear to be working
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
    }

    @Bean
    public ActivitiApplicationSecurity applicationSecurity() {
        return new ActivitiApplicationSecurity();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
