// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import org.activiti.spring.rest.model.UserRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

privileged aspect UserRecord_Roo_Equals {
    
    public boolean UserRecord.equals(Object obj) {
        if (!(obj instanceof UserRecord)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        UserRecord rhs = (UserRecord) obj;
        return new EqualsBuilder().append(email, rhs.email).append(firstName, rhs.firstName).append(id, rhs.id).append(lastName, rhs.lastName).append(pwd, rhs.pwd).isEquals();
    }
    
    public int UserRecord.hashCode() {
        return new HashCodeBuilder().append(email).append(firstName).append(id).append(lastName).append(pwd).toHashCode();
    }
    
}
