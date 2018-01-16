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
package link.omny.custmgmt;

import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.subethamail.wiser.Wiser;

import link.omny.custmgmt.internal.JsonPopulatorFactoryBean;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.MemoDistribution;

@Configuration
@ComponentScan(basePackages = { "io.onedecision.engine.decisions",
        "io.onedecision.engine.domain",
        "link.omny.custmgmt" })
@EntityScan({ "io.onedecision.engine.decisions.model",
        "io.onedecision.engine.domain.model",
        "link.omny.custmgmt.model" })
@EnableJpaRepositories({ "io.onedecision.engine.decisions.repositories",
        "io.onedecision.engine.domain.repositories",
        "link.omny.custmgmt.repositories" })
public class CustMgmtConfig extends RepositoryRestMvcConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CustMgmtConfig.class);

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Value("${omny.populator.skip:true}")
    protected boolean skipPopulator;

    @Value("${omny.mock.smtp:true}")
    protected boolean mockSmtpServer;

    @Bean
    public Object wiser() {
        if (mockSmtpServer) {
            LOGGER.warn("Starting mock SMTP server on port 2525, disable with omny.mock.smtp=false");
            try {
                Wiser wiser = new Wiser();
                wiser.setPort(2525); // Default is 25
                wiser.start();
                return wiser;
            } catch (NoClassDefFoundError e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(Contact.class, Account.class,
                MemoDistribution.class);
    }

    @Bean
    public JsonPopulatorFactoryBean repositoryPopulator() {
        JsonPopulatorFactoryBean factory = new JsonPopulatorFactoryBean();
        // Set a custom ObjectMapper if Jackson customization is needed
        // factory.setObjectMapper(…);
        if (skipPopulator) {
            LOGGER.warn("Configured to skip repository population, change this by setting populator.skip=false in application.properties");
            factory.setResources(new Resource[0]);
        } else {
            factory.setResources(new Resource[] { new ClassPathResource(
                    "data.json") });
        }

        return factory;
    }

}

