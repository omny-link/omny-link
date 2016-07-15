package link.omny.server;

import io.onedecision.engine.OneDecisionConfig;
import link.omny.acctmgmt.AcctMgmtConfig;
import link.omny.acctmgmt.model.SystemConfig;
import link.omny.catalog.CatalogConfig;
import link.omny.custmgmt.CustMgmtConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.knowprocess.bpm.BpmConfiguration;
import com.knowprocess.bpm.api.ActivitiApplicationSecurity;

@Configuration
@Import({ OneDecisionConfig.class, AcctMgmtConfig.class,
        BpmConfiguration.class, CustMgmtConfig.class, CatalogConfig.class })
@ComponentScan(basePackages = { "link.omny.acctmgmt", "link.omny.catalog",
        "link.omny.custmgmt", "link.omny.server", "io.onedecision.engine" })
public class Application extends WebMvcConfigurerAdapter {

    @Bean
    public SystemConfig systemConfig() {
        return new SystemConfig();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
    }

    @Bean
    public WebSecurityConfigurerAdapter applicationSecurity() {
        return new ActivitiApplicationSecurity();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
