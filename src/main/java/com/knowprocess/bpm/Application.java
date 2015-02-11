package com.knowprocess.bpm;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    protected PropertiesFactoryBean messageAliases() {
        PropertiesFactoryBean fact = new PropertiesFactoryBean();
        fact.setLocation(new ClassPathResource("messageAliases.properties"));
        return fact;
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