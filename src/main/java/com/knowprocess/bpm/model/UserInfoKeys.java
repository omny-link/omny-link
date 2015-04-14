package com.knowprocess.bpm.model;

public enum UserInfoKeys {

    PHONE, COMMS_PREFERENCE;

    public String toString() {
        return name().toLowerCase();
    }
}
