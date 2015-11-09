package com.knowprocess.bpm.model;

import java.io.Serializable;

public enum UserInfoKeys implements Serializable {

    COMMS_PREFERENCE, PHONE, TENANT;

    public String toString() {
        return name().toLowerCase();
    }
}
