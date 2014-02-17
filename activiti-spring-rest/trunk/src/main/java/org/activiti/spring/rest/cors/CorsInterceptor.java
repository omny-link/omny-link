package org.activiti.spring.rest.cors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class CorsInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		System.out.println("**************** preHandle");
		String origin = request.getHeader("Origin");
		System.out.println("origin: " + origin);
		if (CorsFilter.getAllowedOrigins().contains(origin)) {
			System.out.println("already have ACAO?:"
					+ response.containsHeader("Access-Control-Allow-Origin"));
			response.addHeader("Access-Control-Allow-Origin", origin);
			System.out.println("**************** allowed");
			return true;
		} else {
			System.out.println("**************** disallowed");
			return false;
		}
	}
}
