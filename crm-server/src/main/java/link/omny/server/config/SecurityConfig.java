/*******************************************************************************
 * Copyright 2015-2026 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import link.omny.server.security.JwtAccessDeniedHandler;
import link.omny.server.security.JwtAuthenticationEntryPoint;
import link.omny.server.security.KeycloakJwtAuthenticationConverter;

/**
 * Spring Security configuration for JWT authentication with Keycloak.
 *
 * <p>
 * Secures all endpoints except actuator, swagger, and API docs using JWT tokens
 * issued by Keycloak. Implements role-based access control by extracting roles
 * from JWT claims.
 *
 * @author Tim Stephenson
 * @since 3.1.10
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            KeycloakJwtAuthenticationConverter jwtAuthConverter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler) throws Exception {

        LOGGER.info("Configuring Spring Security with JWT authentication");

        http
                // Use lambda DSL (Spring Security 7.0 requirement)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/actuator/**", "/swagger-ui/**",
                                "/swagger-ui.html", "/v3/api-docs/**",
                                "/api-docs/**", "/js/env.js")
                        .permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                // Configure OAuth2 Resource Server with JWT
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                // Disable CSRF for stateless JWT authentication
                .csrf(csrf -> csrf.disable())
                // Stateless session management
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS));

        LOGGER.info(
                "Security configuration complete - JWT authentication enabled");
        return http.build();
    }
}
