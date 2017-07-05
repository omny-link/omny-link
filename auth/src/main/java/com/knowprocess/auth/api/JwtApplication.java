package com.knowprocess.auth.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot application for use in testing.
 */
@SpringBootApplication
@EnableConfigurationProperties
@EntityScan({ "com.knowprocess.auth.user.model" })
@EnableJpaRepositories({ "com.knowprocess.auth.user.repositories" })
@ComponentScan(basePackages = { "com.knowprocess.auth" })
public class JwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(JwtApplication.class, args);
	}
}
