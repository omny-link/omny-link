package org.activiti.spring.rest.cors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.spring.auth.ExternalUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class CorsInterceptor extends HandlerInterceptorAdapter {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ExternalUserDetailsService.class);

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		String origin = request.getHeader("Origin");
		LOGGER.info("Checking CORS headers ...");
		if (origin == null) {
			LOGGER.info("... No Origin header, continue as non-CORS.");
			return true;
		} else if (CorsFilter.getAllowedOrigins().contains(origin)) {
			response.addHeader("Access-Control-Allow-Origin", origin);
			LOGGER.info(String.format("... Cross origin allowed from %1$s",
					origin));
			return true;
		} else {
			LOGGER.warn(String.format("... Cross origin disallowed from %1$s",
					origin));
			return false;
		}
	}
}
