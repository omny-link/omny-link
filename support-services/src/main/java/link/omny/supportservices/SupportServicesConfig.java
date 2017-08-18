package link.omny.supportservices;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@ComponentScan(basePackages = { "link.omny.supportservices" })
@EntityScan({ "link.omny.supportservices.model" })
@EnableJpaRepositories({ "link.omny.supportservices.repositories" })
public class SupportServicesConfig extends RepositoryRestMvcConfiguration {

}

