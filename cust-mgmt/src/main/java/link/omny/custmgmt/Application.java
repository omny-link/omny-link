package link.omny.custmgmt;

import link.omny.custmgmt.internal.JsonPopulatorFactoryBean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableJpaRepositories("link.omny.custmgmt.repositories")
// @ImportResource("classpath:META-INF/spring/applicationContext-data.xml")
public class Application {

    @Bean
    public JsonPopulatorFactoryBean repositoryPopulator() {
        JsonPopulatorFactoryBean factory = new JsonPopulatorFactoryBean();
        // Set a custom ObjectMapper if Jackson customization is needed
        // factory.setObjectMapper(…);
        factory.setResources(new Resource[] { new ClassPathResource("data.json") });
        return factory;
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
