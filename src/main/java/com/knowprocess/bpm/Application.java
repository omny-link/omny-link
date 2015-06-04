package com.knowprocess.bpm;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.google.common.base.Predicate;
import com.knowprocess.bpm.api.ActivitiUserDetailsService;
import com.knowprocess.bpm.impl.CorsFilter;
import com.knowprocess.bpm.impl.JsonManager;

@Configuration
@AutoConfigureBefore(DataSourceProcessEngineAutoConfiguration.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = { "com.knowprocess.bpm",
        "com.knowprocess.decisions" })
@EnableAutoConfiguration
@EntityScan({ "com.knowprocess.bpm", "com.knowprocess.decisions" })
@EnableJpaRepositories({ "com.knowprocess.decisions.repositories",
        "com.knowprocess.bpm.decisions.repositories",
        "com.knowprocess.bpm.domain.repositories" })
@EnableSwagger2
public class Application extends WebMvcConfigurerAdapter {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MultiTenantActivitiProperties overrideProperties;

    @Bean
    public Docket workApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
                .select()
                // Ignores controllers annotated with @CustomIgnore
                // .apis(not(withClassAnnotation(CustomIgnore.class))
                // //Selection by RequestHandler
                .paths(publicPaths()) // and by paths
                .build();
        // .apiInfo(apiInfo())
        // .securitySchemes(securitySchemes())
        // .securityContext(securityContext());
    }

    /**
     * 
     * @return public API.
     */
    private Predicate<String> publicPaths() {
        return or(regex("/.*/deployments.*"), regex("/msg.*"),
                regex("/.*/process-definitions.*"),
                regex("/.*/process-instances.*"),
                regex("/.*/task.*"),
                regex("/.*/tasks.*"), regex("/users.*"));
    }

    @Bean
    public JsonManager jsonManager() {
        return new JsonManager();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
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
        .antMatchers("/*.html").hasRole("user")
        .antMatchers("/admin.html", "/audit-trails/**",
                "/deployments/**", "/process-definitions/**",
                "/process-instances/**", "/tasks/**", "/users/**")
                .hasRole("admin")
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
