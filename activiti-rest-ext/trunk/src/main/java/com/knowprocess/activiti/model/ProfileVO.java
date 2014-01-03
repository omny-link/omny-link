package com.knowprocess.activiti.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

//import org.activiti.engine.impl.identity.Account;

@XmlRootElement(name = "profile")
@XmlType(propOrder = { "username", "email", "firstName", "lastName", "groups",
		"info", "twitter", "linkedIn", "kindle" })
public class ProfileVO implements User, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1134195340094073693L;

	private static final String TWITTER = "twitter";

	private static final String LINKED_IN = "linkedIn";

	private static final String KINDLE = "kindle";

	private static final String SUGAR_URL = "sugarUrl";

	private static final String SUGAR_PASSWORD = "sugarPassword";

	private static final String SUGAR_USERNAME = "sugarUsername";

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private String password;

    // TODO need to set this based on the ACtiviti user info 'confirmation'
    // private boolean approved;

    private ArrayList<GroupVO> groups;

    private Map<String, String> info;

    public ProfileVO() {
        super();
        info = new HashMap<String, String>();
    }

    public ProfileVO(String username, String firstName, String lastName) {
        this();
        this.username = username;
        setFirstName(firstName);
        setLastName(lastName);
    }

    public ProfileVO setUser(User user) {
        this.username = user.getId();
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        return this;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method is not implemented and should not be used. It exists solely
     * because to read the property JUEL apparently requires both setters and
     * getters.
     * 
     * @param username
     *            the email to set as username.
     */
    public void setUsername(String username) {
		this.username = username;
		// throw new RuntimeException("TODO investigate need for setter.");
    }

    // /**
    // * @return the username
    // * @throws NoSuchAlgorithmException
    // */
    // public String getUsernameHash() throws NoSuchAlgorithmException {
    // MessageDigest md = MessageDigest.getInstance("MD5");
    // md.update(username.getBytes());
    // String d = new String(md.digest());
    // System.out.println("returning digest: " + d);
    // return d;
    // }
    //
    // /**
    // * This method is not implemented and should not be used. It exists solely
    // * because to read the property JUEL apparently requires both setters and
    // * getters.
    // *
    // * @param username
    // * the email to set as username.
    // */
    // public void setUsernameHash(String username) {
    // throw new RuntimeException("TODO investigate need for setUsernameHash.");
    // }

    /**
     * @return the email.
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addGroup(Group group) {
        getGroups().add(new GroupVO().setGroup(group));
    }

    public void setGroups(List<GroupVO> groups) {
        for (GroupVO group : groups) {
            addGroup(group);
        }
    }

    public List<GroupVO> getGroups() {
        if (groups == null) {
            groups = new ArrayList<GroupVO>();
        }
        return groups;
    }

    public Map<String, String> getInfo() {
        if (info == null) {
            info = new HashMap<String, String>();
        }
        return info;
    }

    public void setInfo(Map<String, String> info) {
        this.info = info;
    }

    public void addInfo(String key, String value) {
        info.put(key, value);
    }

    public String getTwitter() {
        String handle = getInfo().get(TWITTER);
        return handle == null ? " - " : handle;
    }

    public void setTwitter(String handle) {
        addInfo(TWITTER, handle);
    }

    public String getLinkedIn() {
        String addr = getInfo().get(LINKED_IN);
        return addr == null ? " - " : addr;
    }

    public void setLinkedIn(String url) {
        addInfo(LINKED_IN, url);
    }

	public String getKindle() {
		String addr = getInfo().get(KINDLE);
		return addr == null ? " - " : addr;
	}

	public void setKindle(String kindleAddress) {
		addInfo(KINDLE, kindleAddress);
	}

	public String getSugarUsername() {
		String handle = getInfo().get(SUGAR_USERNAME);
		return handle == null ? " - " : handle;
	}

	public void setSugarUsername(String handle) {
		addInfo(SUGAR_USERNAME, handle);
	}

	public String getSugarPassword() {
		String handle = getInfo().get(SUGAR_PASSWORD);
		return handle == null ? " - " : handle;
	}

	public void setSugarPassword(String handle) {
		addInfo(SUGAR_PASSWORD, handle);
	}

	public String getSugarUrl() {
		String handle = getInfo().get(SUGAR_URL);
		return handle == null ? " - " : handle;
	}

	public void setSugarUrl(String handle) {
		addInfo(SUGAR_URL, handle);
	}
    @XmlTransient
    public String getId() {
        return getUsername();
    }

    public void setId(String id) {
        setUsername(id);
    }

    @XmlTransient
    public String getPassword() {
        return password;
    }

    public void setPassword(String pwd) {
        this.password = pwd;
    }

    // public boolean isApproved() {
    // return approved;
    // }
    //
    // public void setApproved(boolean approved) {
    // this.approved = approved;
    // }

    public void setGroups(ArrayList<GroupVO> groups) {
        this.groups = groups;
    }

}
