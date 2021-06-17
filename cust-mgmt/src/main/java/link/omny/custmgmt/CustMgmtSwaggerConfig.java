package link.omny.custmgmt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class CustMgmtSwaggerConfig {
    @Bean
    public Docket custMgmtApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Customer management")
                .select()
                .apis(RequestHandlerSelectors.basePackage("link.omny.custmgmt.web"))
                .paths(PathSelectors.any())
                .build();
    }
}
