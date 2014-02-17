package org.activiti.spring.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

import flexjson.JSONSerializer;

import org.activiti.engine.ActivitiObjectNotFoundException;


@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
@RooSerializable
@RooJson
public class UserRecord {

	private static final String[] JSON_FIELDS = { "assignee", "createTime",
			"id", "name", "owner", "parentUserId", "priority",
			"processDefinitionId", "suspended", "identityDefinitionKey" };

	private static ProcessEngine processEngine;

	/**
     */
	private String id;

	/**
     */
	private String firstName;

	/**
     */
	private String lastName;

	/**
     */
	private String email;

	/**
     */
	@OneToMany(cascade = CascadeType.ALL)
	private Set<UserInfo> userInfos = new HashSet<UserInfo>();

	/**
     */
	@ManyToMany(cascade = CascadeType.ALL)
	private Set<UserGroup> userGroups = new HashSet<UserGroup>();

	public UserRecord() {
		super();
	}

	public UserRecord(org.activiti.engine.identity.User u) {
		this();
		System.out.println("Wrapping user: " + u);
		setId(u.getId());
		setFirstName(u.getFirstName());
		setLastName(u.getLastName());
		setEmail(u.getEmail());
	}

	// Autowiring static fields is obviously dangerous, but should be ok in this
	// case as PE is thread safe.
	@Autowired(required = true)
	public void setProcessEngine(ProcessEngine pe) {
		System.out.println("XXX UserRecord.setProcessEngine:" + pe);
		UserRecord.processEngine = pe;
	}

	public static long countUserRecords() {
		return processEngine.getIdentityService().createUserQuery().count();
	}

	public static List<UserRecord> findAllUserRecords() {
		return wrap(processEngine.getIdentityService().createUserQuery().list());
	}

	public static UserRecord findUserRecord(String id) {
		System.out.println("Find user with id: " + id);
		try {
			IdentityService svc = processEngine.getIdentityService();
			User user = svc.createUserQuery().userId(id)
					.singleResult();
			if (user==null) { 
				throw new ActivitiObjectNotFoundException("User with id:"+id, User.class);
			}
			
			UserRecord wrappedUser = new UserRecord(user);
			String username = wrappedUser.getId();
			List<Group> list = svc.createGroupQuery().groupMember(username)
					.list();
			for (Group group : list) {
				wrappedUser.getUserGroups().add(new UserGroup(group));
			}

			List<String> userInfoKeys = svc.getUserInfoKeys(username);
			for (String key : userInfoKeys) {
				wrappedUser.getUserInfos().add(
						new UserInfo(wrappedUser, key, svc.getUserInfo(
								username, key)));
			}
			return wrappedUser;
		} catch (ActivitiObjectNotFoundException e) {
			System.out.println(e.getMessage()); 
			throw e; 
		} catch (Exception e) {
			System.out.println(e.getClass().getName()+":"+e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static List<UserRecord> findUserRecordEntries(int firstResult,
			int maxResults) {
		return wrap(processEngine.getIdentityService().createUserQuery()
				.listPage(firstResult, maxResults));
	}

	public static List<UserRecord> findUserRecordEntries(int firstResult,
			int maxResults, String sortFieldName, String sortOrder) {
		// TODO honour sort order
		return wrap(processEngine.getIdentityService().createUserQuery()
				.listPage(firstResult, maxResults));
	}

	public static List<UserRecord> findAllUserRecords(String sortFieldName,
			String sortOrder) {
		System.out.println("pe: " + processEngine);
		// TODO honour sort order
		return wrap(processEngine.getIdentityService().createUserQuery().list());
	}

	private static List<UserRecord> wrap(
			final List<org.activiti.engine.identity.User> list) {
		ArrayList<UserRecord> list2 = new ArrayList<UserRecord>();
		for (org.activiti.engine.identity.User instance : list) {
			list2.add(new UserRecord(instance));
		}
		return list2;
	}

	public String toJson() {
		return toJson(JSON_FIELDS);
	}

	public String toJson(String[] fields) {
		return new JSONSerializer().include(fields).exclude("*.class")
				.exclude("*.processEngine").serialize(this);
	}

	public static String toJsonArray(Collection<UserRecord> collection) {

		return toJsonArray(collection, JSON_FIELDS);
	}

	public static String toJsonArray(Collection<UserRecord> collection,
			String[] fields) {
		System.out.println("toJsonArray....");
		return new JSONSerializer().exclude("*.class")
				.exclude("*.processEngine").include(fields)
				.serialize(collection);
	}
}
