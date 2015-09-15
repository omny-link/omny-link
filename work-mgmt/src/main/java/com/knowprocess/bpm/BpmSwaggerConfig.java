package com.knowprocess.bpm;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import com.google.common.base.Predicate;

@Component
public class BpmSwaggerConfig {

    @Bean
    public Docket oneDecisionApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("bpm-api")
                .select().paths(publicPaths())
                .build();
    }

    /**
     * 
     * @return public API.
     */
    @SuppressWarnings("unchecked")
    private Predicate<String> publicPaths() {
        return or(regex("/.*/deployments.*"), regex("/msg.*"),
                regex("/.*/process-definitions.*"),
                regex("/.*/process-instances.*"), regex("/.*/task.*"),
                regex("/.*/tasks.*"), regex("/users.*"));
    }
}
