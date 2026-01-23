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
package link.omny.server.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * Converts JWT tokens from Keycloak into Spring Security authentication
 * objects.
 *
 * <p>
 * Extracts roles from both realm-level and client-level (resource) roles in the
 * JWT token and maps them to Spring Security GrantedAuthority objects with the
 * ROLE_ prefix.
 *
 * @author Tim Stephenson
 * @since 3.1.10
 */
@Component
public class KeycloakJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;

    @Value("${jwt.auth.converter.principal-attribute}")
    private String principalAttribute;

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream
                .concat(defaultGrantedAuthoritiesConverter.convert(jwt)
                        .stream(), extractResourceRoles(jwt).stream())
                .collect(Collectors.toSet());

        String principal = jwt.getClaimAsString(principalAttribute);
        return new JwtAuthenticationToken(jwt, authorities, principal);
    }

    /**
     * Extracts roles from Keycloak JWT token.
     *
     * <p>
     * Looks for roles in two places: 1. realm_access.roles - realm-level roles
     * 2. resource_access.{resource-id}.roles - client-specific roles
     *
     * @param jwt
     *            the JWT token
     * @return collection of GrantedAuthority objects with ROLE_ prefix
     */
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        // Extract realm roles
        Collection<String> realmRoles = extractRealmRoles(jwt);

        // Extract client-specific roles
        Collection<String> resourceRoles = extractClientRoles(jwt);

        // Combine and convert to GrantedAuthority
        return Stream.concat(realmRoles.stream(), resourceRoles.stream())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    /**
     * Extracts realm-level roles from JWT token.
     *
     * @param jwt
     *            the JWT token
     * @return collection of realm role names
     */
    @SuppressWarnings("unchecked")
    private Collection<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (Collection<String>) realmAccess.get("roles");
        }
        return List.of();
    }

    /**
     * Extracts client-specific roles from JWT token.
     *
     * @param jwt
     *            the JWT token
     * @return collection of client role names
     */
    @SuppressWarnings("unchecked")
    private Collection<String> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return List.of();
        }

        Map<String, Object> resource = (Map<String, Object>) resourceAccess
                .get(resourceId);
        if (resource != null && resource.containsKey("roles")) {
            return (Collection<String>) resource.get("roles");
        }

        return List.of();
    }
}
