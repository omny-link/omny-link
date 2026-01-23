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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for extracting information from JWT tokens in the security
 * context.
 *
 * @author Tim Stephenson
 * @since 3.1.10
 */
public final class JwtUtil {

    private JwtUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Gets the JWT token from the current security context.
     *
     * @return the JWT token, or null if not available
     */
    public static Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        return null;
    }

    /**
     * Gets the username from the JWT token.
     *
     * @return the username (preferred_username claim), or null if not available
     */
    public static String getUsername() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("preferred_username") : null;
    }

    /**
     * Gets the email from the JWT token.
     *
     * @return the email, or null if not available
     */
    public static String getEmail() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("email") : null;
    }

    /**
     * Gets the user ID (subject) from the JWT token.
     *
     * @return the user ID (sub claim), or null if not available
     */
    public static String getUserId() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getSubject() : null;
    }

    /**
     * Gets the full name from the JWT token.
     *
     * @return the full name, or null if not available
     */
    public static String getFullName() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("name") : null;
    }

    /**
     * Gets the given name (first name) from the JWT token.
     *
     * @return the given name, or null if not available
     */
    public static String getGivenName() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("given_name") : null;
    }

    /**
     * Gets the family name (last name) from the JWT token.
     *
     * @return the family name, or null if not available
     */
    public static String getFamilyName() {
        Jwt jwt = getJwt();
        return jwt != null ? jwt.getClaimAsString("family_name") : null;
    }
}
