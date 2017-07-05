package com.knowprocess.auth.user.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_ID_GROUP")
public class Role implements Serializable{
    private static final long serialVersionUID = 2226416538273669156L;

    @Id
    @Column(name = "ID_")
    private String id;

    @Column(name = "NAME_")
    private String name;

    @ManyToMany
    @JoinTable(name = "ACT_ID_MEMBERSHIP", joinColumns = @JoinColumn(name = "GROUP_ID_"), inverseJoinColumns = @JoinColumn(name = "USER_ID_"))
    private Set<User> users;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}