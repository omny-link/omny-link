/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tools.jackson.annotation.JsonInclude;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import link.omny.catalog.internal.AuditorAwareImpl;

@Configuration
@ComponentScan(basePackages = { "link.omny.catalog" })
@EntityScan({ "link.omny.catalog.model" })
@EnableJpaRepositories({ "link.omny.catalog.repositories" })
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class CatalogConfig {

    @Autowired
    private Environment env;

    @Bean
    public JsonMapper objectMapper() {
        // Configure Jackson 3 JsonMapper for backward compatibility
        JsonMapper.Builder builder = JsonMapper.builder();

        // Pretty-print if configured
        if (Boolean.parseBoolean(env.getProperty(
                "spring.jackson.serialization.indent_output", "false"))) {
            builder.enable(SerializationFeature.INDENT_OUTPUT);
        }

        // Build the mapper first, then apply global inclusion
        JsonMapper mapper = builder.build();

        String propertyInclusion = env
                .getProperty("spring.jackson.default-property-inclusion");
        if (propertyInclusion != null) {
            switch (propertyInclusion.toLowerCase()) {
            case "always":
                mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
                break;
            case "non_null":
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                break;
            case "non_absent":
                mapper.setSerializationInclusion(
                        JsonInclude.Include.NON_ABSENT);
                break;
            case "non_default":
                mapper.setSerializationInclusion(
                        JsonInclude.Include.NON_DEFAULT);
                break;
            case "non_empty":
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                break;
            default:
                // leave default behavior
                break;
            }
        }

        return mapper;
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
