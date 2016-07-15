package com.knowprocess.bpm.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import lombok.Data;
import lombok.ToString;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Data
@ToString(exclude = { "groups", "info" })
@Component
public class UserRecord implements Serializable, Principal, User, UserDetails {

    private static final long serialVersionUID = 6444591690674352972L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UserRecord.class);

    // private static final String[] JSON_FIELDS = { "assignee", "createTime",
    // "id", "name", "owner", "parentUserId", "priority",
    // "processDefinitionId", "suspended", "identityDefinitionKey",
    // "groups", "info" };

    private static ProcessEngine processEngine;

    /**
     */
    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String commsPreference;

    private String omnyBarPosition;

    private String tenant;

    private String userPicture;

    /**
     */
    @OneToMany(cascade = CascadeType.ALL)
    private Set<UserInfo> info = new HashSet<UserInfo>();

    /**
     */
    @ManyToMany(cascade = CascadeType.ALL)
    private Set<UserGroup> groups = new HashSet<UserGroup>();

    private String password;

    private String confirmPassword;

    private Collection<GrantedAuthority> authorities;

    public UserRecord() {
        super();
    }

    public UserRecord(org.activiti.engine.identity.User u) {
        this();
        LOGGER.info("Wrapping user: " + u);
        setId(u.getId());
        setFirstName(u.getFirstName());
        setLastName(u.getLastName());
        setEmail(u.getEmail());
        setPassword(u.getPassword());
    }

    public UserRecord(String username) {
        this();
        LOGGER.info("Stub User from username: " + username);
        setId(username);
    }

    // Autowiring static fields is obviously dangerous, but should be ok in this
    // case as PE is thread safe.
    @Autowired(required = true)
    public void setProcessEngine(ProcessEngine pe) {
        LOGGER.debug("UserRecord.setProcessEngine:" + pe);
        UserRecord.processEngine = pe;
    }

    public static long countUserRecords() {
        return processEngine.getIdentityService().createUserQuery().count();
    }

    public static List<UserRecord> findAllUserRecords() {
        return wrap(processEngine.getIdentityService().createUserQuery().list());
    }

    public static UserRecord findUserRecord(String id) {
        LOGGER.info(String.format("findUserRecord with id: %1$s", id));
        try {
            IdentityService svc = processEngine.getIdentityService();
            User user = svc.createUserQuery().userId(id).singleResult();
            if (user == null) {
                throw new ActivitiObjectNotFoundException("User with id:" + id,
                        User.class);
            }

            return augment(new UserRecord(user));
        } catch (ActivitiObjectNotFoundException e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static UserRecord findUserRecordByNames(String first,
            String last) {
        LOGGER.info(String
                .format("findUserRecordByNames with first name: %1$s and last name: %2$s",
                        first, last));
        try {
            IdentityService svc = processEngine.getIdentityService();
            List<User> users = svc.createUserQuery().userFirstNameLike(first).userLastNameLike(last).list();
            if (users.size() == 0) {
                throw new ActivitiObjectNotFoundException(String.format("User with first name: %1$s and last name %2$s", first, last),
                        User.class);
            }

            return augment(new UserRecord(users.get(0)));
        } catch (ActivitiObjectNotFoundException e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected static UserRecord augment(UserRecord wrappedUser) {
        IdentityService svc = processEngine.getIdentityService();
        String username = wrappedUser.getId();
        List<Group> list = svc.createGroupQuery().groupMember(username).list();
        LOGGER.debug(String.format("Found %1$d groups", list.size()));
        for (Group group : list) {
            wrappedUser.getGroups().add(new UserGroup(group));
        }

        List<String> userInfoKeys = svc.getUserInfoKeys(username);
        LOGGER.debug(String.format("Found %1$d userInfo records",
                userInfoKeys.size()));
        for (String key : userInfoKeys) {
            if (UserInfoKeys.PHONE.toString().equals(key)) {
                wrappedUser.setPhone(svc.getUserInfo(username,
                        UserInfoKeys.PHONE.toString()));
            } else if (UserInfoKeys.COMMS_PREFERENCE.toString().equals(key)) {
                wrappedUser.setCommsPreference(svc.getUserInfo(username,
                        UserInfoKeys.COMMS_PREFERENCE.toString()));
            } else if (UserInfoKeys.OMNY_BAR_POSITION.toString().equals(key)) {
                wrappedUser.setOmnyBarPosition(svc.getUserInfo(username,
                        UserInfoKeys.OMNY_BAR_POSITION.toString()));
            } else if (UserInfoKeys.TENANT.toString().equals(key)) {
                wrappedUser.setTenant(svc.getUserInfo(username,
                        UserInfoKeys.TENANT.toString()));
            } else {
                wrappedUser.getInfo().add(
                        new UserInfo(key, svc.getUserInfo(
                                username, key)));
            }
        }
        return wrappedUser;
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

    @Transactional
    public void remove() {
        processEngine.getIdentityService().deleteUser(id);
    }

    // public String toJson() {
    // return toJson(JSON_FIELDS);
    // }
    //
    // public String toJson(String[] fields) {
    // return new JSONSerializer().include(fields).exclude("*.class")
    // .exclude("*.password").exclude("*.processEngine")
    // // .transform(new UserInfoTransformer(), "info")
    // .serialize(this);
    // }

    // public static String toJsonArray(Collection<UserRecord> collection) {
    // return toJsonArray(collection, JSON_FIELDS);
    // }
    //
    // public static String toJsonArray(Collection<UserRecord> collection,
    // String[] fields) {
    // System.out.println("toJsonArray....");
    // return new JSONSerializer().exclude("*.class").exclude("*.password")
    // .exclude("*.processEngine").include(fields)
    // .serialize(collection);
    // }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String pwd) {
        this.password = pwd;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new LinkedList<GrantedAuthority>();
            for (UserGroup group : getGroups()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_"
                        + group.getId()));
            }
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return getId();
    }

    public String getName() {
        return getId();
    }

    public String getFullName() {
        return String.format("%1$s %2$s", getFirstName(), getLastName());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isPictureSet() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * When setting the password it's ok to leave it empty (say to support
     * external authentication such as OAuth) but otherwise they must match.
     * 
     * @return true if password is set legitimately.
     */
    public boolean isPasswordSetOk() {
        if (password == null || password.equals(confirmPassword)) {
            return true;
        } else {
            return false;
        }
    }
}
