package link.omny.catalog;

import link.omny.catalog.model.StockItem;
import link.omny.catalog.model.StockCategory;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "link.omny.catalog" })
@EntityScan({ "link.omny.catalog.model" })
@EnableJpaRepositories({ "link.omny.catalog.repositories" })
public class CatalogConfig extends RepositoryRestMvcConfiguration {

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(StockItem.class, StockCategory.class);
    }

}
