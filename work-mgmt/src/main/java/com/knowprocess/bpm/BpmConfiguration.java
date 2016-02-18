package com.knowprocess.bpm;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.User;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.knowprocess.bpm.api.ActivitiUserDetailsService;
import com.knowprocess.bpm.impl.JsonManager;
import com.knowprocess.bpm.impl.TaskAllocationMapper;

@Configuration
@ComponentScan(basePackages = { "com.knowprocess.bpm" })
@AutoConfigureBefore(DataSourceProcessEngineAutoConfiguration.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class BpmConfiguration extends AbstractProcessEngineAutoConfiguration {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(BpmConfiguration.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private MultiTenantActivitiProperties overrideProperties;

    @Bean
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

        Set<Class<?>> mappers = new HashSet<Class<?>>();
        mappers.add(TaskAllocationMapper.class);
        config.setCustomMybatisMappers(mappers);

        return config;
    }

    // TODO use separate datasource for activiti?
    // @Bean
    // public DataSource activitiDataSource() {
    // return DataSourceBuilder.create().url("jdbc:h2:mem:customer:H2")
    // .driverClassName("org.h2.Driver").username("sa").password("")
    // .build();
    // }

    // @Bean
    // public SpringProcessEngineConfiguration springProcessEngineConfiguration(
    // DataSource dataSource,
    // PlatformTransactionManager transactionManager,
    // SpringAsyncExecutor springAsyncExecutor) throws IOException {
    //
    // // setActivitiProperties(overrideProperties);
    //
    // SpringProcessEngineConfiguration config = this
    // .baseSpringProcessEngineConfiguration(dataSource,
    // transactionManager, springAsyncExecutor);
    // config.setJpaEntityManagerFactory(entityManagerFactory);
    // config.setTransactionManager(transactionManager);
    // config.setJpaHandleTransaction(false);
    // config.setJpaCloseEntityManager(false);
    //
    // config.setMailServers(overrideProperties.getServers());
    //
    // return config;
    // }

    @Bean
    public JsonManager jsonManager() {
        return new JsonManager();
    }

    @Bean
    public ActivitiUserDetailsService activitiUserDetailsService() {
        return new ActivitiUserDetailsService();
    }

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
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(
                            String.format("Number of process definitions: %1$d",
                            repositoryService.createProcessDefinitionQuery()
                                            .count()));
                    LOGGER.info(
                            String.format("Number of users: %1$d",
                            identityService.createUserQuery().count()));
                    if (LOGGER.isDebugEnabled()) {
                        List<User> users = identityService.createUserQuery()
                                .list();
                        for (User user : users) {
                            LOGGER.debug(String.format("... : %1$s",
                                    user.getId()));
                        }
                    }
                }
            }
        };

    }

}