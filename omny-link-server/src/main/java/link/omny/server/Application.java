package link.omny.server;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.Link;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.auth.AuthConfig;
import com.knowprocess.bpm.BpmConfiguration;

import io.onedecision.engine.OneDecisionConfig;
import io.onedecision.engine.domain.OneDecisionDomainConfig;
import link.omny.acctmgmt.AcctMgmtConfig;
import link.omny.acctmgmt.model.SystemConfig;
import link.omny.analytics.AnalyticsConfig;
import link.omny.catalog.CatalogConfig;
import link.omny.custmgmt.CustMgmtConfig;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.server.model.mixins.DocumentMixIn;
import link.omny.server.model.mixins.LinkMixIn;
import link.omny.server.model.mixins.NoteMixIn;

@Configuration
@EnableAutoConfiguration
@Import({ AuthConfig.class, OneDecisionConfig.class, OneDecisionDomainConfig.class,
        AnalyticsConfig.class, AcctMgmtConfig.class,
        BpmConfiguration.class, CustMgmtConfig.class, CatalogConfig.class })
@ComponentScan(basePackages = { "link.omny.acctmgmt", "link.omny.analytics",
        "link.omny.catalog", "link.omny.custmgmt", "link.omny.server",
        "io.onedecision.engine" })
public class Application extends WebMvcConfigurerAdapter {

    @Bean
    public SystemConfig systemConfig() {
        return new SystemConfig();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        // #405 migration
        registry.addRedirectViewController("/login", "/");
    }

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .mixIn(Document.class, DocumentMixIn.class)
                .mixIn(Link.class, LinkMixIn.class)
                .mixIn(Note.class, NoteMixIn.class)
                .build();
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        super.configureMessageConverters(converters);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
