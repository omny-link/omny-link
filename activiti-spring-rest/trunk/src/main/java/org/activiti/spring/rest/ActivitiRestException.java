package org.activiti.spring.rest;

import org.activiti.engine.ActivitiException;

public class ActivitiRestException extends ActivitiException {

    public ActivitiRestException(String message) {
        super(message);
    }

}
