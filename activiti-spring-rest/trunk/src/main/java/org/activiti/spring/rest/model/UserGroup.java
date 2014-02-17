package org.activiti.spring.rest.model;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;

import org.activiti.engine.identity.Group;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
@RooSerializable
public class UserGroup {

    /**
     */
    private String id;

    /**
     */
    private String name;

    /**
     */
    private String type;

    public UserGroup() {
        super();
    }

    public UserGroup(String id, String name, String type) {
        this();
        setId(id);
        setName(name);
        setType(type);
    }

    /**
     */
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<UserRecord> users = new HashSet<UserRecord>();

    public UserGroup(Group group) {
        this();
        setId(group.getId());
        setName(group.getName());
        setType(group.getType());
    }
}
