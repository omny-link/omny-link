package com.knowprocess.bpm;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.knowprocess.bpm.api.ActivitiUserDetailsService;
import com.knowprocess.bpm.impl.CorsFilter;
import com.knowprocess.bpm.impl.JsonManager;

@Configuration
@AutoConfigureBefore(DataSourceProcessEngineAutoConfiguration.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ComponentScan
@EnableAutoConfiguration
public class Application extends WebMvcConfigurerAdapter {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MultiTenantActivitiProperties overrideProperties;

    @Bean
    public JsonManager jsonManager() {
        return new JsonManager();
    }

    /*@Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            DataSource activitiDataSource,
            PlatformTransactionManager transactionManager,
            SpringAsyncExecutor springAsyncExecutor) throws IOException {

        // setActivitiProperties(overrideProperties);

        SpringProcessEngineConfiguration config = this
                .baseSpringProcessEngineConfiguration(activitiDataSource,
                        transactionManager, springAsyncExecutor);
        config.setJpaEntityManagerFactory(entityManagerFactory);
        config.setTransactionManager(transactionManager);
        config.setJpaHandleTransaction(false);
        config.setJpaCloseEntityManager(false);

        config.setDataSource(activitiDataSource);
        config.setMailServers(overrideProperties.getServers());

        return config;
    }*/

    @Bean
    protected PropertiesFactoryBean messageAliases() {
        PropertiesFactoryBean fact = new PropertiesFactoryBean();
        fact.setLocation(new ClassPathResource("messageAliases.properties"));
        return fact;
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/login").setViewName("login");
      registry.addViewController("/loginError").setViewName("loginError");
    }

    @Bean
    public ApplicationSecurity applicationSecurity() {
      return new ApplicationSecurity();
    }

    @Bean
    public ActivitiUserDetailsService activitiUserDetailsService() {
      return new ActivitiUserDetailsService();
    }

    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    protected static class ApplicationSecurity extends
    WebSecurityConfigurerAdapter {

      @Autowired
      private DataSource dataSource;

      @Autowired
      private SecurityProperties security;

      @Autowired
      private ActivitiUserDetailsService activitiUserDetailsService;

      @Autowired
      private CorsFilter corsFilter;

      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
        .antMatchers("/css/**", "/data/**", "/fonts/**",
        "/images/**", "/js/**")
        .permitAll()
        .antMatchers(HttpMethod.OPTIONS,"/**").permitAll()
        .antMatchers("/*.html").hasRole("USER")
        .antMatchers("/admin.html", "/audit-trails/**",
        "/deployments/**", "/process-definitions/**",
        "/process-instances/**", "/tasks/**", "/users/**").hasRole("ADMIN")
        .anyRequest().authenticated()
        .and().formLogin()
        .loginPage("/login").failureUrl("/loginError")
        .defaultSuccessUrl("/").permitAll()
        .and().csrf().disable().httpBasic();
        //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        //             http.requestMatcher
        // org.apache.catalina.filters.CorsFilter corsFilter = new
        // org.apache.catalina.filters.CorsFilter();
        // corsFilter.
        http.addFilterBefore(corsFilter, BasicAuthenticationFilter.class);
      }

      @Override
      public void configure(AuthenticationManagerBuilder auth)
      throws Exception {
        auth.userDetailsService(activitiUserDetailsService);
        // auth.jdbcAuthentication().dataSource(dataSource)
        // .withDefaultSchema().withUser("user").password("password")
        // .roles("USER").and().withUser("admin").password("password")
        // .roles("USER", "ADMIN");
        // auth.inMemoryAuthentication().withUser("user").password("user")
        // .roles("USER");
      }
    }

    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
            final RuntimeService runtimeService,
            final IdentityService identityService, final TaskService taskService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                System.out.println("Number of process definitions : "
                        + repositoryService.createProcessDefinitionQuery()
                                .count());
                System.out.println("Number of users: "
                        + identityService.createUserQuery().count());
                List<User> users = identityService.createUserQuery().list();
                for (User user : users) {
                    System.out.println("... : " + user.getId() + ":"
                            + user.getPassword());
                }
            }
        };

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
