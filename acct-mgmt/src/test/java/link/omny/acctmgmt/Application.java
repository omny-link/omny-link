package link.omny.acctmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import link.omny.catalog.CatalogConfig;
import link.omny.custmgmt.CustMgmtConfig;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Import({ AcctMgmtConfig.class, CatalogConfig.class, CustMgmtConfig.class })
public class Application extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/loginError").setViewName("loginError");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
