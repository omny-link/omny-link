// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;
import org.activiti.spring.rest.model.UserGroup;

privileged aspect UserGroup_Roo_Jpa_Entity {
    
    declare @type: UserGroup: @Entity;
    
    @Version
    @Column(name = "version")
    private Integer UserGroup.version;
    
    public Integer UserGroup.getVersion() {
        return this.version;
    }
    
    public void UserGroup.setVersion(Integer version) {
        this.version = version;
    }
    
}
