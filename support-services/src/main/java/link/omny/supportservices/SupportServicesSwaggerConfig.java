package link.omny.supportservices;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SupportServicesSwaggerConfig {
    @Bean
    public Docket supportServicesApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Support services")
                .select()
                .apis(RequestHandlerSelectors.basePackage("link.omny.supportservices.web"))
                .paths(PathSelectors.any())
                .build();
    }
}
