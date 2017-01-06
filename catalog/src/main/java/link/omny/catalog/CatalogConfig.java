package link.omny.catalog;

import javax.annotation.PostConstruct;

import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.web.GeoLocationService;
import link.omny.catalog.web.StockCategoryController.ShortStockCategory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = { "link.omny.catalog" })
@EntityScan({ "link.omny.catalog.model" })
@EnableJpaRepositories({ "link.omny.catalog.repositories" })
public class CatalogConfig extends RepositoryRestMvcConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CatalogConfig.class);

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(ShortStockCategory.class, StockItem.class,
                StockCategory.class);
    }
    @Autowired
    private Environment env;
    @Autowired
    private Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder;

    @Autowired
    private ObjectMapper objectMapper;



    @PostConstruct
    public void init() {
        // Somewhere between Application construction and here the application
        // properties have been overridden, set Jackson back to the configured
        // values.
        jackson2ObjectMapperBuilder.indentOutput(Boolean.parseBoolean(env
                .getProperty("spring.jackson.serialization.indent_output")));

        String propertyInclusion = env
                .getProperty("spring.jackson.default-property-inclusion");
        if (propertyInclusion != null) {
            switch (propertyInclusion.toLowerCase()) {
            case "always":
                jackson2ObjectMapperBuilder
                        .serializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS);
                break;
            case "non_null":
                jackson2ObjectMapperBuilder
                        .serializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
                break;
            case "non_absent":
                jackson2ObjectMapperBuilder
                        .serializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT);
                break;
            case "non_default":
                jackson2ObjectMapperBuilder
                        .serializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT);
                break;
            case "non_empty":
                jackson2ObjectMapperBuilder
                        .serializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
                break;
            }
        }

        jackson2ObjectMapperBuilder.configure(objectMapper);
    }

    @Bean
    public GeoLocationService geoLocationService() {
        LOGGER.info(String.format(
                "Configured catalog geo-coding cache to %1$s",
                env.getProperty("omny.catalog.geoCodeCacheSize")));

        GeoLocationService geoLocationService = new GeoLocationService(
                Integer.parseInt(env
                        .getProperty("omny.catalog.geoCodeCacheSize", "1000")));
        return geoLocationService;
    }
}
