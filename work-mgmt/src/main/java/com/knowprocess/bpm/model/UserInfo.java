package com.knowprocess.bpm.model;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(exclude = { "userRecord" })
@Component
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -4782038617402728868L;

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
