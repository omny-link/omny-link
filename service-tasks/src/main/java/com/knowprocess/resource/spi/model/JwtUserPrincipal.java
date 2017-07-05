package com.knowprocess.resource.spi.model;

import java.security.Principal;

public class JwtUserPrincipal extends PasswordUserPrincipal implements Principal {

    private String jwtLoginUrl;

    public JwtUserPrincipal(String usr, String pwd, String jwtLoginUrl) {
        super(usr, pwd);
        this.jwtLoginUrl = jwtLoginUrl;
    }

    public String getJwtLoginUrl() {
        return jwtLoginUrl;
    }

}
