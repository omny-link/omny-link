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

import org.springframework.web.filter.OncePerRequestFilter;

public class CorsFilter extends OncePerRequestFilter {

	protected static Set<String> allowedOrigins;

	protected static Set<String> getAllowedOrigins() throws IOException {
		if (allowedOrigins == null) {
			System.out.println("Init CORS...");
			Properties prop = new Properties();

			InputStream in = CorsFilter.class
					.getResourceAsStream("/META-INF/cors.properties");
			prop.load(in);
			in.close();

			allowedOrigins = new HashSet<String>(Arrays.asList(prop
					.getProperty("allowed.origins").split(",")));
		}
		System.out.println("Inited: " + allowedOrigins);
		return allowedOrigins;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		System.out.println("doInternalFilter...");

		if (request.getHeader("Access-Control-Request-Method") != null
				&& "OPTIONS".equals(request.getMethod())) {
			System.out.println("... checking origin ...");
			String origin = request.getHeader("Origin");
			System.out.println("... " + origin + " ...");

			if (getAllowedOrigins().contains(origin)) {
				System.out.println("  ... YES! ...");
				response.addHeader("Access-Control-Allow-Origin", origin);

				response.addHeader("Access-Control-Allow-Methods",
						"GET, POST, PUT, DELETE");
				response.addHeader(
						"Access-Control-Allow-Headers",
						"Accept, Accept-Encoding, Accept-Language, Access-Control-Request-Headers, Access-Control-Request-Method, Authorization, Cache-Control, Connection, Content-Type, Host, Origin, Referer, User-Agent");
				response.addHeader("Access-Control-Max-Age", "1800");
			} else {
				System.out.println("... nope... ");
			}
		} else {
			System.out.println("... no CORS requested...");
		}

		System.out.println("... continuing with filter chain");
		filterChain.doFilter(request, response);
	}
}