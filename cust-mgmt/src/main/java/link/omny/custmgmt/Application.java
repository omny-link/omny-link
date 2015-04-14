package link.omny.custmgmt;

import javax.sql.DataSource;

import link.omny.custmgmt.internal.JsonPopulatorFactoryBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.knowprocess.bpm.api.ActivitiUserDetailsService;
import com.knowprocess.bpm.impl.CorsFilter;
import com.knowprocess.bpm.impl.JsonManager;

@Configuration
@ComponentScan(basePackages = { "link.omny.custmgmt", "com.knowprocess.bpm",
        "com.knowprocess.decisions" })
@EnableAutoConfiguration
@EntityScan({ "link.omny.custmgmt.model", "com.knowprocess.bpm",
        "com.knowprocess.decisions" })
@EnableJpaRepositories({ "link.omny.custmgmt.repositories",
        "com.knowprocess.bpm.decisions.repositories",
        "com.knowprocess.decisions.repositories" })
// @ImportResource("classpath:META-INF/spring/applicationContext-data.xml")
public class Application extends WebMvcConfigurerAdapter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Application.class);


    @Value("${omny.populator.skip}")
    protected boolean skipPopulator;

    @Bean
    public JsonManager jsonManager() {
        return new JsonManager();
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

    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        // builder.indentOutput(true).dateFormat(
        // new SimpleDateFormat("yyyy-MM-dd"));
        builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
        // builder.deserializerByType(Contact.class, new
        // JsonContactDeserializer());
        // builder.serializerByType(Extension.class, new
        // JsonExtensionSerializer());

        return builder;
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
                    .antMatchers("/*.html", "/process-instances/**",
                            "/tasks/**", "/users/**")
                    .hasRole("user")
                    .antMatchers("/admin.html", "/deployments/**",
                            "/process-definitions/**")
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

    public static void main(String[] args) {
        // ConfigurableApplicationContext context =
        SpringApplication.run(Application.class, args);

        // ModelRepository repository = context.getBean(ModelRepository.class);
        // save a couple of models
        // repository.save(new Model("foo", "http://knowprocess.com/foo",
        // "<definitions/>"));
    }
}
