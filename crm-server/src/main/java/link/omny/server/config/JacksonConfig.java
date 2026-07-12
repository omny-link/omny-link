/*******************************************************************************
 * Copyright 2015-2026 Tim Stephenson and contributors
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
package link.omny.server.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 JsonMapper configuration for the CRM application.
 *
 * <p>
 * Configures Jackson deserialization to handle null values for primitives
 * gracefully and respects Spring Boot Jackson properties.
 *
 * @author Tim Stephenson
 * @since 3.1.10
 */
@Configuration
public class JacksonConfig {

    @Autowired
    private Environment env;

    @Bean
    @Primary
    public JsonMapper objectMapper() {
        // Configure Jackson 3 JsonMapper
        JsonMapper.Builder builder = JsonMapper.builder();

        // Don't fail on null values for primitive fields during deserialization
        builder.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);

        // Respect spring.jackson.serialization.indent_output
        if (Boolean.parseBoolean(env.getProperty(
                "spring.jackson.serialization.indent_output", "false"))) {
            builder.enable(SerializationFeature.INDENT_OUTPUT);
        }

        // Configure property inclusion based on
        // spring.jackson.default-property-inclusion
        String propertyInclusion = env
                .getProperty("spring.jackson.default-property-inclusion");
        if (propertyInclusion != null) {
            JsonInclude.Include includeMode = switch (propertyInclusion
                    .toLowerCase()) {
            case "always" -> JsonInclude.Include.ALWAYS;
            case "non_null" -> JsonInclude.Include.NON_NULL;
            case "non_absent" -> JsonInclude.Include.NON_ABSENT;
            case "non_default" -> JsonInclude.Include.NON_DEFAULT;
            case "non_empty" -> JsonInclude.Include.NON_EMPTY;
            default -> JsonInclude.Include.NON_NULL;
            };
            builder.changeDefaultPropertyInclusion(
                    incl -> incl.withValueInclusion(includeMode));
        }

        return builder.build();
    }
}
