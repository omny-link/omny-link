package com.knowprocess.bpm.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 
 * @author tstephen
 * @see http://www.w3.org/TR/cors/
 */
@Component
public class CorsFilter extends OncePerRequestFilter {

    // private static final String CORS_PROPS = "/META-INF/cors.properties";

    protected static final String REQUEST_METHOD = "Access-Control-Request-Method";

    protected static final String MAX_AGE = "Access-Control-Max-Age";

    protected static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";

    protected static final String ALLOW_METHODS = "Access-Control-Allow-Methods";

    protected static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CorsFilter.class);

    protected static final String EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    protected static final String ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";

    /**
     * Defaults: null,https?://localhost.*,https?://.*knowprocess
     * .com.*,chrome-extension://fdmmgilgnpjigdojojpjoooidkmcomcm
     */
    @Value("${cors.allowedOrigins}")
    protected String corsAllowedOrigins = "null,https?://localhost.*,https?://.*knowprocess.com.*,chrome-extension://fdmmgilgnpjigdojojpjoooidkmcomcm";

    protected Set<String> allowedOrigins;

    protected Set<String> getAllowedOrigins() throws IOException {
        allowedOrigins = new HashSet<String>(Arrays.asList(corsAllowedOrigins
                .split(",")));
        LOGGER.debug("... allowed: " + allowedOrigins);
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        LOGGER.debug(String.format(
                "Setting allowed domains for CORS to %1$s...", allowedOrigins));

        this.allowedOrigins = new HashSet<String>(Arrays.asList(allowedOrigins
                .split(",")));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            LOGGER.debug("CORS filter has nothing to do as Origin header is not specified.");
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format(
                        "Handling CORS request from: %1$s to %2$s ...", origin,
                        request.getMethod() + " "
                                + request.getRequestURL().toString()));
            }

            String requestMethod = request.getHeader(REQUEST_METHOD);
            if (isAllowed(origin)) {
                LOGGER.debug("... Cross-origin allowed.");

                // Spec calls for a single header and misconfiguration of
                // filters can break this...
                addOnlyOneHeader(response, ALLOW_ORIGIN, origin);

                // TODO Potentially add this here if needed.
                // If the list of exposed headers is not empty add one or
                // more
                // Access-Control-Expose-Headers headers, with as values the
                // header field names given in the list of exposed headers.

                // The following only applies to 'pre-flight' request
                // Note this is the actual pre-flight method not the
                // proposed
                // CORS method that will follow
                if ("OPTIONS".equals(request.getMethod())) {
                    // TODO parse Access-Control-Request-Headers
                    addOnlyOneHeader(response, MAX_AGE, "1800");
                    addOnlyOneHeader(response, ALLOW_METHODS,
                            "GET, POST, PUT, DELETE");
                    addOnlyOneHeader(
                            response,
                            ALLOW_HEADERS,
                            "Accept, Accept-Encoding, Accept-Language, Access-Control-Request-Headers, Access-Control-Request-Method, Authorization, Cache-Control, Connection, Content-Type, Host, Location, Origin, Referer, User-Agent");
                    addOnlyOneHeader(response, EXPOSE_HEADERS, "Location");
                    addOnlyOneHeader(response, ALLOW_CREDENTIALS_HEADER, "true");
                } else {
                    addOnlyOneHeader(response, EXPOSE_HEADERS, "Location");
                    addOnlyOneHeader(response, ALLOW_CREDENTIALS_HEADER, "true");
                }

                // TODO check spec, seems the request method is not sent at
                // least for file:// urls check FF and chromium

                // } else if (requestMethod == null) {
                // LOGGER.warn(String
                // .format("... CORS handling from %2$s to %3$s abandoned due to missing header: %1$s. This can be normal if a CORS request is forwarded internally (as in an OpenId authentication).",
                // REQUEST_METHOD, origin, request.getMethod()
                // + " "
                // + request.getRequestURL().toString()));

            } else {
                LOGGER.warn(String.format(
                        "... Cross origin disallowed from %1$s to %2$s",
                        origin, request.getMethod() + " "
                                + request.getRequestURL().toString()));
            }
        }

        filterChain.doFilter(request, response);
    }

    protected boolean isAllowed(String origin) throws IOException {
        for (String allowedOrigin : getAllowedOrigins()) {
            LOGGER.debug(String.format("Check if %1$s matches %2$s", origin,
                    allowedOrigin));
            if (origin.matches(allowedOrigin)) {
                LOGGER.info(String.format(
                        "Allowing request from %1$s as matches %2$s", origin,
                        allowedOrigin));
                return true;
            }
        }
        LOGGER.error(String.format(
                "Rejecting %1$s as there are no matches in %2$s", origin,
                allowedOrigins));
        return false;
    }

    protected void addOnlyOneHeader(HttpServletResponse response,
            String header, String value) {
        if (response.containsHeader(header)) {
            LOGGER.warn(String
                    .format("Header %1$s already specified,  check only one CORS handler implemented",
                            header));
        } else {
            response.addHeader(header, value);
        }
    }
}
