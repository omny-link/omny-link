// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.activiti.spring.rest.model.UserRecord;
import org.springframework.transaction.annotation.Transactional;

privileged aspect UserRecord_Roo_Jpa_ActiveRecord {
    
    @PersistenceContext
    transient EntityManager UserRecord.entityManager;
    
    public static final EntityManager UserRecord.entityManager() {
        EntityManager em = new UserRecord().entityManager;
        if (em == null) throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        return em;
    }
    
    @Transactional
    public void UserRecord.persist() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.persist(this);
    }
    
    @Transactional
    public void UserRecord.flush() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.flush();
    }
    
    @Transactional
    public void UserRecord.clear() {
        if (this.entityManager == null) this.entityManager = entityManager();
        this.entityManager.clear();
    }
    
    @Transactional
    public UserRecord UserRecord.merge() {
        if (this.entityManager == null) this.entityManager = entityManager();
        UserRecord merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }
    
}
