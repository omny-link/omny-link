package link.omny.acctmgmt;

import link.omny.acctmgmt.model.TenantConfig;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "link.omny.acctmgmt" })
@EntityScan({ "link.omny.acctmgmt.model" })
@EnableJpaRepositories({ "link.omny.acctmgmt.repositories" })
public class AcctMgmtConfig extends RepositoryRestMvcConfiguration {

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(TenantConfig.class);
    }

}

