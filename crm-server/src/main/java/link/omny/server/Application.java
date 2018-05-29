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
package link.omny.server;

import java.util.List;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.Link;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.catalog.CatalogConfig;
import link.omny.custmgmt.CustMgmtConfig;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.server.model.mixins.DocumentMixIn;
import link.omny.server.model.mixins.LinkMixIn;
import link.omny.server.model.mixins.NoteMixIn;
import link.omny.supportservices.SupportServicesConfig;

@Configuration
@EnableAutoConfiguration
@Import({ CustMgmtConfig.class, CatalogConfig.class,
        SupportServicesConfig.class })
@ComponentScan(basePackages = { "link.omny.acctmgmt",
        "link.omny.catalog", "link.omny.custmgmt", "link.omny.server" })
public class Application extends WebMvcConfigurerAdapter {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Application.class);

    @Value("${omny.tomcat.connector2.enabled:false}")
    protected boolean tomcatConnector2Enabled;

    @Value("${omny.tomcat.connector2.port:8080}")
    protected int connector2Port;

    @Value("${omny.tomcat.connector2.scheme:http}")
    protected String connector2Scheme;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
        // #405 migration
        registry.addRedirectViewController("/login", "/");
    }

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .mixIn(Document.class, DocumentMixIn.class)
                .mixIn(Link.class, LinkMixIn.class)
                .mixIn(Note.class, NoteMixIn.class)
                .build();
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        super.configureMessageConverters(converters);
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        if (tomcatConnector2Enabled) {
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            connector.setScheme(connector2Scheme);
            connector.setSecure(false);
            connector.setPort(connector2Port);
            protocol.setSSLEnabled(false);

            LOGGER.info("Enabled secondary connector:");
            LOGGER.info("  port: {}", connector2Port);
            LOGGER.info("  scheme: {}", connector2Scheme);
            tomcat.addAdditionalTomcatConnectors(connector);
        } else {
            LOGGER.info("No secondary connector configured, set omny.tomcat.* to enable");
        }

        return tomcat;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
