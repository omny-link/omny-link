// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;
import org.activiti.spring.rest.model.Execution;

privileged aspect Execution_Roo_Jpa_Entity {
    
    declare @type: Execution: @Entity;
    
    @Version
    @Column(name = "version")
    private Integer Execution.version;
    
    public Integer Execution.getVersion() {
        return this.version;
    }
    
    public void Execution.setVersion(Integer version) {
        this.version = version;
    }
    
}
