package com.knowprocess.bpm.impl;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsfrFilter extends OncePerRequestFilter {

    private static final String CSFR_COOKIE = "XSRF-TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class
                .getName());
        if (csrf != null) {
            Cookie cookie = new Cookie(CSFR_COOKIE, csrf.getToken());
            cookie.setPath("/");
            response.addCookie(cookie);
            // response.setHeader(CSFR_COOKIE, csrf.getToken());
        }

        filterChain.doFilter(request, response);
    }
};
