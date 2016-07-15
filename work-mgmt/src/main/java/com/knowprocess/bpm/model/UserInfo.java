package com.knowprocess.bpm.model;

import java.io.Serializable;

import javax.persistence.Id;

import lombok.Data;

import org.springframework.stereotype.Component;

@Data
@Component
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -4782038617402728868L;

    /**
     */
    @Id
    private String id;

    /**
     */
    private String key;

    /**
     */
    private String value;

    public UserInfo() {
        super();
    }

    public UserInfo(String key, String value) {
        this();
        setKey(key);
        setValue(value);
    }

}
