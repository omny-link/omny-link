package com.knowprocess.bpm.model;

public enum UserInfoKeys {

    COMMS_PREFERENCE, PHONE, TENANT;

    public String toString() {
        return name().toLowerCase();
    }
}
