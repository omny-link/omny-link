package org.activiti.spring.rest.model;
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

}
