package com.knowprocess.bpm.api;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.knowprocess.bpm.impl.CorsFilter;

@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class ActivitiApplicationSecurity extends WebSecurityConfigurerAdapter {

    @Value("${omny.security.loginPage:/login}")
    protected String loginPage;

    @Value("${omny.security.loginFailureUrl:/login?error}")
    protected String loginFailureUrl;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SecurityProperties security;

    @Autowired
    private ActivitiUserDetailsService activitiUserDetailsService;

    @Autowired
    private CorsFilter corsFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                .antMatchers(HttpMethod.GET, "/tenants/*.json")
                        .permitAll()
                .antMatchers("/*.html", "/css/**", "/data/**", "/fonts/**",
                        "/images/**", "/js/**", "/msg/**", "/partials/**",
                        "/public/**", "/sdu/**", "/webjars/**")
                        .permitAll()
                .antMatchers(/* "/*.html", */"/process-instances/**",
                        "/tasks/**", "/tenants/**", "/users/**")
                        .hasRole("user")
                .antMatchers("/admin.html", "/deployments/**",
                        "/process-definitions/**")
                        .hasRole("admin")
                .antMatchers("/admin/**")
                        .hasRole("super_admin")
                .anyRequest().authenticated().and()
                .formLogin().loginPage(loginPage).failureUrl(loginFailureUrl)
                .successHandler(getSuccessHandler()).permitAll().and().csrf()
                .disable().httpBasic();

        // .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        http.addFilterBefore(corsFilter, BasicAuthenticationFilter.class);
    }

    private AuthenticationSuccessHandler getSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler successHandler = new SimpleUrlAuthenticationSuccessHandler(
                "/");
        successHandler.setTargetUrlParameter("redirect");
        return successHandler;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(activitiUserDetailsService);
        // auth.jdbcAuthentication().dataSource(dataSource)
        // .withDefaultSchema().withUser("user").password("password")
        // .roles("USER").and().withUser("admin").password("password")
        // .roles("USER", "ADMIN");
        // auth.inMemoryAuthentication().withUser("user").password("user")
        // .roles("USER");
    }

}
