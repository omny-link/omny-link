package com.knowprocess.auth.user.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ACT_ID_USER")
public class User implements Serializable {
    private static final long serialVersionUID = 6681537789718268379L;

    @Id
    @Column(name="ID_")
    private String username;

    @Column(name = "PWD_")
    private String password;

    @ManyToMany
    @JoinTable(name = "ACT_ID_MEMBERSHIP", joinColumns = @JoinColumn(name = "USER_ID_"), inverseJoinColumns = @JoinColumn(name = "GROUP_ID_"))
    private List<Role> roles;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
