package link.omny.custmgmt;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import com.google.common.base.Predicate;

@Component
public class CustMgmtSwaggerConfig {
    @Bean
    public Docket omnyApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("public-api")
                .select()
                // Ignores controllers annotated with @CustomIgnore
                // .apis(not(withClassAnnotation(CustomIgnore.class))
                // //Selection by RequestHandler
                .paths(publicPaths()) // and by paths
                .build();
        // .apiInfo(apiInfo())
        // .securitySchemes(securitySchemes())
        // .securityContext(securityContext());
    }


    /**
     * 
     * @return public API.
     */
    @SuppressWarnings("unchecked")
    private Predicate<String> publicPaths() {
        return or(regex("/.*/accounts.*"), regex("/.*/activities.*"),
                regex("/.*/contacts.*"), regex("/.*/notes.*"));
    }
}
