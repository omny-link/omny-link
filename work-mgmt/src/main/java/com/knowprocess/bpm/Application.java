package com.knowprocess.bpm;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.knowprocess.bpm.api.ActivitiApplicationSecurity;
import com.knowprocess.bpm.impl.JsonManager;

@Configuration
@ComponentScan(basePackages = { "com.knowprocess.bpm",
        "com.knowprocess.decisions", "link.omny.acctmgmt" })
@EnableAutoConfiguration
@EntityScan({ "com.knowprocess.bpm", "link.omny.acctmgmt.model" })
@EnableJpaRepositories({ "com.knowprocess.decisions.repositories",
        "com.knowprocess.bpm.repositories", "link.omny.acctmgmt.repositories" })
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

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
