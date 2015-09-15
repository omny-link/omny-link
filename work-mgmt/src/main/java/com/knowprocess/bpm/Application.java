package com.knowprocess.bpm;

import java.util.List;

import javax.persistence.EntityManagerFactory;

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
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.knowprocess.bpm.api.ActivitiApplicationSecurity;
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
    public JsonManager jsonManager() {
        return new JsonManager();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

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
    public ActivitiApplicationSecurity applicationSecurity() {
      return new ActivitiApplicationSecurity();
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
