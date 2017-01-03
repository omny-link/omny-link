package link.omny.analytics;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "link.omny.analytics" })
// @EntityScan({ "link.omny.analytics.model" })
// @EnableJpaRepositories({ "link.omny.analytics.repositories" })
public class AnalyticsConfig extends RepositoryRestMvcConfiguration {

}

