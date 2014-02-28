package org.activiti.spring.rest.model;

import java.util.Collection;
import java.util.Iterator;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
@RooSerializable
@RooJson
public class UserInfo {

	/**
     */
	@Id
	private String id;

	/**
     */
	private String key;

	/**
     */
	private String value;

	/**
     */
	@ManyToOne
	private UserRecord userRecord;

	public UserInfo() {
		super();
	}

	public UserInfo(UserRecord userRecord, String key, String value) {
		this();
		this.key = key;
		this.value = value;
		this.userRecord = userRecord;
	}

	public UserInfo(String key, String value) {
		this();
		setKey(key);
		setValue(value);
	}

//	public String toJson() {
//		return String
//				.format("{\"id\":\"%1$s\",\"key\":\"%1$2\",\"value\":\"%3$s\",\"%4$s\":\"%5$s\"}",
//						getId(), getKey(), getValue(), getKey(), getValue());
//	}
//
//	public static String toJsonArray(Collection<UserInfo> collection) {
//		StringBuffer sb = new StringBuffer("[");
//		for (Iterator<UserInfo> iterator = collection.iterator(); iterator
//				.hasNext();) {
//			UserInfo userInfo = iterator.next();
//			sb.append(userInfo.toJson());
//			if (iterator.hasNext()) {
//				sb.append(",");
//			}
//		}
//		sb.append("]");
//		return sb.toString();
//	}
}
