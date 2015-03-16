package link.omny.custmgmt.internal;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Only allow data thru that matches the X-Tenant header.
 * 
 * @author tstephen
 */
@Component
@Order(value = 10000)
public class TenantFilter extends OncePerRequestFilter {

    protected static final String CUSTOM_HEADER = "X-Tenant";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        LOGGER.info("Found tenant header: " + request.getHeader(CUSTOM_HEADER));
        // response.getOutputStream() ;
        filterChain.doFilter(request, response);
    }

}