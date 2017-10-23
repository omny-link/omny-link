package link.omny.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import link.omny.acctmgmt.AcctMgmtConfig;
import link.omny.catalog.CatalogConfig;
import link.omny.custmgmt.CustMgmtConfig;

@Configuration
@Import({ AcctMgmtConfig.class, CatalogConfig.class, CustMgmtConfig.class })
@ComponentScan(basePackages = { "link.omny.analytics", "link.omny.custmgmt",
        "io.onedecision.engine" })
public class TestApplication extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
