package com.knowprocess.activiti.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;

@XmlRootElement(name = "group")
@XmlType(propOrder = { "id", "name", "type" })
public class GroupVO implements Serializable, Group {

    /**
     * 
     */
    private static final long serialVersionUID = 6582035529129318429L;
    private Group group;

    public GroupVO() {

    }

	public GroupVO(String id) {
		this.group = new GroupEntity(id);
	}

    public GroupVO setGroup(Group group) {
        this.group = group;
        return this;
    }

    public void setId(String id) {
        ;
    }

    public String getId() {
        return group.getId();
    }

    public void setName(String name) {
        ;
    }

    public String getName() {
        return group.getName();
    }

	public String getType() {
		return group.getType();
	}

	public void setType(String type) {
		group.setType(type);
	}

    public static List<GroupVO> asList(List<Group> groups) {
        List<GroupVO> list = new ArrayList<GroupVO>();
        for (Group group : groups) {
            list.add(new GroupVO().setGroup(group));
        }
        return list;
    }
}