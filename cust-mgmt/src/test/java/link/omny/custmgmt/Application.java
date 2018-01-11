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
package link.omny.custmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.knowprocess.auth.AuthConfig;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableAutoConfiguration
@Configuration
@Import({ AuthConfig.class, CustMgmtConfig.class })
@ComponentScan(basePackages = { "link.omny.custmgmt", "io.onedecision.engine" })
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Allegedly sets welcome page though does not appear to be working
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("login");
    }

    @Bean
    public ApplicationSecurity applicationSecurity() {
        return new ApplicationSecurity();
    }

    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class ApplicationSecurity extends
            WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers("/css/**", "/data/**", "/docs/**",
                            "/fonts/**", "/images/**", "/js/**", "/webjars/**")
                    .permitAll().antMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll().antMatchers("/*.html", "/users/**")
                    .hasRole("view").antMatchers("/admin/**").hasRole("admin")
                    .anyRequest().authenticated().and().formLogin()
                    .loginPage("/login").failureUrl("/login?error")
                    .permitAll().and()
                    .csrf().disable().httpBasic();

            // Allow frames
            // TODO really only needed for embedding notation may can tighten
            // up?
            http.headers().frameOptions().disable();
        }

        @Override
        public void configure(AuthenticationManagerBuilder auth)
                throws Exception {
            auth.inMemoryAuthentication().withUser("admin")
                    .password("onedecision")
                    .roles("view", "manage", "author", "admin");
            auth.inMemoryAuthentication().withUser("author")
                    .password("onedecision").roles("view", "manage", "author");
            auth.inMemoryAuthentication().withUser("super-user")
                    .password("onedecision").roles("view", "manage");
            auth.inMemoryAuthentication().withUser("user")
                    .password("onedecision").roles("view");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
