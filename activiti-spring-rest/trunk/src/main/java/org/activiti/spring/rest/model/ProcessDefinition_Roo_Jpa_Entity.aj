// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;
import org.activiti.spring.rest.model.ProcessDefinition;

privileged aspect ProcessDefinition_Roo_Jpa_Entity {
    
    declare @type: ProcessDefinition: @Entity;
    
    @Version
    @Column(name = "version_")
    private Integer ProcessDefinition.version_;
    
    public Integer ProcessDefinition.getVersion_() {
        return this.version_;
    }
    
    public void ProcessDefinition.setVersion_(Integer version) {
        this.version_ = version;
    }
    
}
