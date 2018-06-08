/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.catalog;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;
import link.omny.catalog.web.StockCategoryController.ShortStockCategoryResource;

@Configuration
@ComponentScan(basePackages = { "link.omny.catalog" })
@EntityScan({ "link.omny.catalog.model" })
@EnableJpaRepositories({ "link.omny.catalog.repositories" })
public class CatalogConfig extends RepositoryRestMvcConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CatalogConfig.class);

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(ShortStockCategoryResource.class, StockItem.class,
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

}
