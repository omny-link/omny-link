package org.activiti.spring.rest.cors;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 
 * @author tstephen
 * @see http://www.w3.org/TR/cors/
 */
public class CorsFilter extends OncePerRequestFilter {

	private static final String CORS_PROPS = "/META-INF/cors.properties";

	protected static final String REQUEST_METHOD = "Access-Control-Request-Method";

	protected static final String MAX_AGE = "Access-Control-Max-Age";

	protected static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";

	protected static final String ALLOW_METHODS = "Access-Control-Allow-Methods";

	protected static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(CorsFilter.class);

	protected Set<String> allowedOrigins;

	protected Set<String> getAllowedOrigins() throws IOException {
		if (allowedOrigins == null) {
			LOGGER.debug(String.format(
					"Init allowed domains for CORS from %1$s...", CORS_PROPS));
			Properties prop = new Properties();

			InputStream in = CorsFilter.class.getResourceAsStream(CORS_PROPS);
			prop.load(in);
			in.close();

			allowedOrigins = new HashSet<String>(Arrays.asList(prop
					.getProperty("allowed.origins").split(",")));
		}
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
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format(
						"Handling CORS request from: %1$s to %2$s ...", origin,
						request.getMethod() + " "
								+ request.getRequestURL().toString()));
			}

			String requestMethod = request.getHeader(REQUEST_METHOD);
			if (requestMethod != null) {
				if (getAllowedOrigins().contains(origin)) {
					LOGGER.debug("... CORS allowed.");

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
								"Accept, Accept-Encoding, Accept-Language, Access-Control-Request-Headers, Access-Control-Request-Method, Authorization, Cache-Control, Connection, Content-Type, Host, Origin, Referer, User-Agent");
					}
				} else {
					LOGGER.debug("... CORS disallowed.");
				}
			} else {
				LOGGER.info(String
						.format("... CORS handling abandoned due to missing header: %1$s. This can be normal if a CORS request is forwarded internally (as in an OpenId authentication).",
								REQUEST_METHOD));
			}
		}

		filterChain.doFilter(request, response);
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