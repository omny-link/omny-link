package com.knowprocess.resource.spi.model;

import java.security.Principal;

public class PasswordUserPrincipal implements Principal {

    private String username;
    private String password;

    public PasswordUserPrincipal(String usr, String pwd) {
        username = usr;
        password = pwd;
    }

    @Override
    public String getName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
