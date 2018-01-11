/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package com.knowprocess.bpm;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import com.google.common.base.Predicate;

@Component
public class BpmSwaggerConfig {

    @Bean
    public Docket oneDecisionApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("bpm-api")
                .select().paths(publicPaths())
                .build();
    }

    /**
     * 
     * @return public API.
     */
    @SuppressWarnings("unchecked")
    private Predicate<String> publicPaths() {
        return or(regex("/.*/deployments.*"), regex("/msg.*"),
                regex("/.*/process-definitions.*"),
                regex("/.*/process-instances.*"), regex("/.*/task.*"),
                regex("/.*/tasks.*"), regex("/users.*"));
    }
}
