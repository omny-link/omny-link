package link.omny.acctmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import io.onedecision.engine.OneDecisionConfig;
import link.omny.custmgmt.CustMgmtConfig;

@EnableAutoConfiguration
@Configuration
@ComponentScan
@Import({ AcctMgmtConfig.class, CustMgmtConfig.class, OneDecisionConfig.class })
public class Application extends WebMvcConfigurerAdapter {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/loginError").setViewName("loginError");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // @Bean
    // public DataSource database() {
    // return dataSource;
    // return DataSourceBuilder.create()
    // .url("jdbc:mysql://127.0.0.1:3306/activiti-spring-boot?characterEncoding=UTF-8")
    // .username("alfresco")
    // .password("alfresco")
    // .driverClassName("com.mysql.jdbc.Driver")
    // .build();
    // }
}
