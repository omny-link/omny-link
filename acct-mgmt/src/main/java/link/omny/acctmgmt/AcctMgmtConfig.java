package link.omny.acctmgmt;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.acctmgmt.model.TenantConfig;

@Configuration
@ComponentScan(basePackages = { "link.omny.acctmgmt" })
@EntityScan({ "link.omny.acctmgmt.model" })
@EnableJpaRepositories({ "link.omny.acctmgmt.repositories" })
//@Import({ AuthConfig.class, BpmConfiguration.class})
public class AcctMgmtConfig extends RepositoryRestMvcConfiguration {

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(TenantConfig.class);
    }

    // See https://github.com/spring-projects/spring-boot/issues/6529
    @Override
    @Bean
    @Primary
    public ObjectMapper halObjectMapper() {
        return super.halObjectMapper();
    }

}

