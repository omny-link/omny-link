package link.omny.custmgmt;

import javax.persistence.EntityManagerFactory;

import link.omny.custmgmt.internal.JsonPopulatorFactoryBean;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.MemoDistribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.subethamail.wiser.Wiser;

@Configuration
@EnableAutoConfiguration
public class CustMgmtConfig extends RepositoryRestMvcConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CustMgmtConfig.class);

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Value("${omny.populator.skip:true}")
    protected boolean skipPopulator;

    @Value("${omny.mock.smtp:true}")
    protected boolean mockSmtpServer;

    @Bean
    public Object wiser() {
        if (mockSmtpServer) {
            LOGGER.warn("Starting mock SMTP server on port 2525, disable with omny.mock.smtp=false");
            try {
                Wiser wiser = new Wiser();
                wiser.setPort(2525); // Default is 25
                wiser.start();
                return wiser;
            } catch (NoClassDefFoundError e) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config) {
        config.exposeIdsFor(Contact.class, Account.class,
                MemoDistribution.class);
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

    // @Bean
    // public Jackson2ObjectMapperBuilder jacksonBuilder() {
    // Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    // // builder.indentOutput(true).dateFormat(
    // // new SimpleDateFormat("yyyy-MM-dd"));
    // builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
    // // builder.deserializerByType(Contact.class, new
    // // JsonContactDeserializer());
    // // builder.serializerByType(Extension.class, new
    // // JsonExtensionSerializer());
    //
    // return builder;
    // }

}

