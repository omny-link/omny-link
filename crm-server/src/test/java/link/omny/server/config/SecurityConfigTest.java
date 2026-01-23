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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for JWT authentication security configuration.
 *
 * <p>
 * Tests that endpoints are properly secured and that
 * authentication/authorization works as expected.
 *
 * <p>
 * Note: In Spring Boot 4, @AutoConfigureMockMvc moved to
 * org.springframework.boot.webmvc.test.autoconfigure package in the
 * spring-boot-starter-webmvc-test module.
 *
 * @author Tim Stephenson
 * @since 3.1.10
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void actuatorHealthEndpoint_shouldBePublic() throws Exception {
        // Public endpoint - no authentication required
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void swaggerUIEndpoint_shouldBePublic() throws Exception {
        // Public endpoint - no authentication required
        // Swagger UI redirects from /swagger-ui.html to /swagger-ui/index.html
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocsEndpoint_shouldBePublic() throws Exception {
        // Public endpoint - no authentication required
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void securedEndpoint_withoutAuth_shouldReturn401() throws Exception {
        // Any other endpoint without authentication should return 401
        mockMvc.perform(get("/some-api-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void securedEndpoint_withValidJwt_shouldReturn404() throws Exception {
        // With valid JWT, endpoint is accessible (returns 404 since endpoint
        // doesn't exist)
        mockMvc.perform(get("/some-api-endpoint").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void securedEndpoint_withJwtAndRole_shouldBeAccessible() throws Exception {
        // With JWT and required role, endpoint is accessible
        mockMvc.perform(get("/some-api-endpoint").with(
                jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound()); // 404 since endpoint doesn't
                                                   // exist, not 401/403
    }

    @Test
    void actuatorEndpoints_shouldNotRequireAuth() throws Exception {
        // All actuator endpoints should be public
        mockMvc.perform(get("/actuator")).andExpect(status().isOk());
    }
}
