// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import org.activiti.spring.rest.model.UserInfo;
import org.activiti.spring.rest.model.UserRecord;

privileged aspect UserInfo_Roo_JavaBean {
    
    public String UserInfo.getKey() {
        return this.key;
    }
    
    public void UserInfo.setKey(String key) {
        this.key = key;
    }
    
    public String UserInfo.getValue() {
        return this.value;
    }
    
    public void UserInfo.setValue(String value) {
        this.value = value;
    }
    
    public UserRecord UserInfo.getUserRecord() {
        return this.userRecord;
    }
    
    public void UserInfo.setUserRecord(UserRecord userRecord) {
        this.userRecord = userRecord;
    }
    
}
