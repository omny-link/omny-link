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
package link.omny.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.catalog.CatalogConfig;
import link.omny.custmgmt.CustMgmtConfig;
import link.omny.supportservices.SupportServicesConfig;

@SpringBootApplication
@Import({ CustMgmtConfig.class, CatalogConfig.class,
        SupportServicesConfig.class })
public class Application {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Application.class);

    @Autowired
    protected CrmCorsProperies corsProps;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public WebMvcConfigurer webConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                LOGGER.info("CORS configuration:");
                LOGGER.info("  allowed origins: {}", corsProps.getOrigins());
                LOGGER.info("  allowed methods: {}", corsProps.getMethods());
                LOGGER.info("  allowed headers: {}", corsProps.getAllowedHeaders());
                LOGGER.info("  exposed headers: {}", corsProps.getAllowedHeaders());
                LOGGER.info("  allow credentials: {}", corsProps.isAllowCredentials());
                CorsRegistration reg = registry.addMapping("/**");
                reg.allowedOrigins(corsProps.getOrigins().split(","));
                reg.allowedMethods(corsProps.getMethods().split(","));
                reg.allowedHeaders(corsProps.getAllowedHeaders().split(","));
                reg.exposedHeaders(corsProps.getAllowedHeaders().split(","));
                reg.allowCredentials(corsProps.isAllowCredentials());
            }

            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // String clientContext = systemConfig().getClientContext();
                String clientContext = "";
                LOGGER.debug("client context set to: " + clientContext);
                // Allegedly sets welcome page though does not appear to be working
                registry.addViewController(clientContext + "/").setViewName("index");
                registry.addViewController("/").setViewName("index.html");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
