package com.knowprocess.exceptions;

import org.activiti.engine.ActivitiObjectNotFoundException;

public class UserInfoNotFoundException extends ActivitiObjectNotFoundException {

    private static final long serialVersionUID = 3333431489323116341L;

    public UserInfoNotFoundException(String message) {
        super(message);
    }

}
