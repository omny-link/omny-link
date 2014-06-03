package org.activiti.spring.rest.cors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class CorsInterceptor extends HandlerInterceptorAdapter {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CorsInterceptor.class);

    @Autowired
    protected CorsFilter corsFilter;

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        String origin = request.getHeader("Origin");
        LOGGER.debug("Checking CORS headers ...");
        if (origin == null) {
            LOGGER.debug("... No Origin header, continue as non-CORS.");
            return true;
        } else if (corsFilter.isAllowed(origin)) {
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
