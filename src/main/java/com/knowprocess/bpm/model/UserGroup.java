package com.knowprocess.bpm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import lombok.Data;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Data
@Component
public class UserGroup implements Group {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UserGroup.class);
    private static ProcessEngine processEngine;

    /**
     */
    @Id
    private String id;

    /**
     */
    private String name;

    /**
     */
    private String type;

    // Autowiring static fields is obviously dangerous, but should be ok in this
    // case as PE is thread safe.
    @Autowired(required = true)
    public void setProcessEngine(ProcessEngine pe) {
        UserGroup.processEngine = pe;
    }

    public UserGroup() {
        super();
    }

    public UserGroup(String id, String name, String type) {
        this();
        setId(id);
        setName(name);
        setType(type);
    }

	public String getName() {
		if (name == null) {
			return id;
		} else {
			return name;
		}
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

	public static List<UserGroup> wrap(
            final List<org.activiti.engine.identity.Group> list) {
        ArrayList<UserGroup> list2 = new ArrayList<UserGroup>();
        for (org.activiti.engine.identity.Group instance : list) {
            list2.add(new UserGroup(instance));
        }
        return list2;
    }

    public static long countUserGroups() {
        return processEngine.getIdentityService().createGroupQuery().count();
    }

    public static List<UserGroup> findAllUserGroups() {
        return wrap(processEngine.getIdentityService().createGroupQuery()
                .list());
    }

    public static List<UserGroup> findAllUserGroups(String sortFieldName,
            String sortOrder) {
        GroupQuery query = null;
        if ("groupId".equals(sortOrder)) {
            query = processEngine.getIdentityService().createGroupQuery()
                    .orderByGroupId();
        } else if ("groupName".equals(sortOrder)) {
            query = processEngine.getIdentityService().createGroupQuery()
                    .orderByGroupName();
        } else if ("groupType".equals(sortOrder)) {
            query = processEngine.getIdentityService().createGroupQuery()
                    .orderByGroupType();

        } else {
            LOGGER.warn(String.format("Unable to sort by: %1$s", sortFieldName));
            query = processEngine.getIdentityService().createGroupQuery();
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            query.desc();
        } else {
            query.asc();
        }
        return wrap(query.list());
    }

    public static UserGroup findUserGroup(String id) {
        return wrap(
                processEngine.getIdentityService().createGroupQuery()
                        .groupId(id).list()).get(0);
    }

    public static List<UserGroup> findUserGroupEntries(int firstResult,
            int maxResults) {
        return wrap(processEngine.getIdentityService().createGroupQuery()
                .listPage(firstResult, maxResults));
    }

    public static List<UserGroup> findUserGroupEntries(int firstResult,
            int maxResults, String sortFieldName, String sortOrder) {
        return Collections.emptyList();
    }

    @Transactional
    public void persist() {
        processEngine.getIdentityService().saveGroup(this);
    }

    @Transactional
    public void remove() {
        processEngine.getIdentityService().deleteGroup(id);
    }

}
