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
package link.omny.acctmgmt;

import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.acctmgmt.model.TenantConfig;
import link.omny.supportservices.SupportServicesConfig;

@Configuration
@ComponentScan(basePackages = { "link.omny.acctmgmt" })
@EntityScan({ "link.omny.acctmgmt.model" })
@EnableJpaRepositories({ "link.omny.acctmgmt.repositories" })
@Import({ SupportServicesConfig.class })
public class AcctMgmtConfig extends RepositoryRestMvcConfiguration {

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(TenantConfig.class);
    }

    // See https://github.com/spring-projects/spring-boot/issues/6529
    @Override
    @Bean
    @Primary
    public ObjectMapper halObjectMapper() {
        return super.halObjectMapper();
    }

}

