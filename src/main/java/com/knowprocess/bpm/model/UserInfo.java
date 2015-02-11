package com.knowprocess.bpm.model;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

import org.springframework.stereotype.Component;

@Data
@Component
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
