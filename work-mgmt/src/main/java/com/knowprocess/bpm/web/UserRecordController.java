package com.knowprocess.bpm.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.UserGroup;
import com.knowprocess.bpm.model.UserInfo;
import com.knowprocess.bpm.model.UserInfoKeys;
import com.knowprocess.bpm.model.UserRecord;

@Controller
@RequestMapping("/users")
public class UserRecordController {

    protected static final String RESET_PWD_DEFINITION_KEY = "ResetPassword";

    protected static final String PATH = "/users";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UserRecordController.class);

    @Autowired(required = true)
    ProcessEngine processEngine;

	@RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody List<UserRecord> showAllJson() {
		LOGGER.info("showAllJson");

		// HttpHeaders headers = new HttpHeaders();
		// headers.add("Content-Type", "application/json; charset=utf-8");

        List<UserRecord> users = UserRecord.findAllUserRecords();
        LOGGER.info("Users: " + users.size());
        return users;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody UserRecord showJson(@PathVariable("id") String id,
			HttpServletRequest request) {
        LOGGER.info(String
                .format("%1$s %2$s/%3$s", RequestMethod.GET, PATH, id));
        // Since id will end .com (or similar TLE) need to get id from request
        // directly as Spring will truncate the extension.
        id = request.getServletPath().substring(
                request.getServletPath().lastIndexOf('/') + 1);
		LOGGER.info("Find user with id: " + id);
		// HttpHeaders headers = new HttpHeaders();
		// headers.add("Content-Type", "application/json; charset=utf-8");
        UserRecord userRecord;
        if (id.indexOf(' ') == -1) {
            userRecord = UserRecord.findUserRecord(id);
        } else {
            System.out
                    .println("TODO This is probably firstName / lastName not id");
            userRecord = UserRecord.findUserRecordByNames(
                    id.substring(0, id.indexOf(' ')),
                    id.substring(id.indexOf(' ') + 1));
        }
        // TODO unclear if the following check is redundant in all or only
        // some cases
        if (userRecord == null) {
            throw new ActivitiObjectNotFoundException(User.class);
        }
        return userRecord;
    }

	@RequestMapping(value = "/", method = RequestMethod.POST, headers = "Accept=application/json")
	public @ResponseBody UserRecord registerFromJson(
			@RequestBody UserRecord userRecord) {
		LOGGER.info(String.format("Creating profile of %1$s",
				userRecord.getEmail()));

		if (!userRecord.isPasswordSetOk()) {
			throw new IllegalArgumentException(
					"Password must be either null or match the confirmPassword field on registration");
		}
		IdentityService idSvc = processEngine.getIdentityService();
		User user = idSvc.newUser(userRecord.getId());
		idSvc.saveUser(user);
		return updateFromJson(userRecord, userRecord.getId());
	}

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public @ResponseBody UserRecord updateFromJson(
			@RequestBody UserRecord userRecord, @PathVariable("id") String id) {
        LOGGER.info(String.format("Updating profile of %1$s", id));

		// Since id will end .com (or similar TLE) need to get id from request
		// directly as Spring will truncate the extension.
		id = userRecord.getId();

		// UserRecord user = UserRecord.findUserRecord(id);
		IdentityService idSvc = processEngine.getIdentityService();
		User user = idSvc.createUserQuery().userId(id).singleResult();
		LOGGER.info("Found user to update: " + user);
        if (userRecord.getEmail() != null
                && userRecord.getEmail().trim().length() > 0) {
            user.setEmail(userRecord.getEmail());
        }
        if (userRecord.getFirstName() != null
                && userRecord.getFirstName().trim().length() > 0) {
            user.setFirstName(userRecord.getFirstName());
        }
        if (userRecord.getLastName() != null
                && userRecord.getLastName().trim().length() > 0) {
            user.setLastName(userRecord.getLastName());
        }
		// IdentityService idSvc = processEngine.getIdentityService();
        LOGGER.debug(String.format("Updating user record for %1$s...", id));
        try {
            idSvc.saveUser(user);
        } catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			// TODO saveUser is supposed to be proof against this (see JavaDoc
            // 'Saves the user. If the user already existed, the user is
            // updated.')
            LOGGER.warn("Error saving user, hope this means it's already there. NOTE that this will lose any updates to the user itself");
        }
        LOGGER.debug("... done");
        for (UserInfo info : userRecord.getInfo()) {
            String userInfo = idSvc.getUserInfo(id, info.getKey());
            System.out.println("Found user info: " + userInfo + " for key: "
                    + info.getKey() + ", setting to: " + info.getValue());
            if (userInfo == null || !userInfo.equals(info.getValue())) {
                LOGGER.debug(String.format(
                        "Updating user info record %2$s for %1$s...", id,
                        info.getKey()));
                idSvc.setUserInfo(id, info.getKey(), info.getValue());
                LOGGER.debug("... done");
            } else {
                LOGGER.warn(String
                        .format("Skipping unchanged user info record %2$s=%3$s for %1$s...",
                                id, info.getKey(), info.getValue()));
            }
        }
        if (userRecord.getPhone() != null
                && userRecord.getPhone().trim().length() > 0) {
            idSvc.setUserInfo(id, UserInfoKeys.PHONE.toString(),
                    userRecord.getPhone());
        }
        if (userRecord.getCommsPreference() != null
                && userRecord.getCommsPreference().trim().length() > 0) {
            idSvc.setUserInfo(id, UserInfoKeys.COMMS_PREFERENCE.toString(),
                    userRecord.getCommsPreference());
        }
        if (userRecord.getTenant() != null
                && userRecord.getTenant().trim().length() > 0) {
            idSvc.setUserInfo(id, UserInfoKeys.TENANT.toString(),
                    userRecord.getTenant());
        }
        return new UserRecord(user);
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public @ResponseBody void delete(
			@PathVariable("id") String id, HttpServletRequest request) {
		LOGGER.info(String.format("Deleting profile of %1$s", id));

		// Since id will end .com (or similar TLE) need to get id from request
		// directly as Spring will truncate the extension.
		id = request.getServletPath().substring(
				request.getServletPath().lastIndexOf('/') + 1);

		IdentityService idSvc = processEngine.getIdentityService();
		idSvc.deleteUser(id);

		// return id;
	}

	@RequestMapping(value = "/{id}/groups", method = RequestMethod.GET, headers = "Accept=application/json")
	public @ResponseBody List<UserGroup> listGroups(
			@PathVariable("id") String id) {
		LOGGER.info(String.format("List groups for %1$s", id));

		IdentityService idSvc = processEngine.getIdentityService();
		return UserGroup.wrap(idSvc.createGroupQuery().groupMember(id).list());
	}

	@RequestMapping(value = "/{id}/groups/{group}", method = RequestMethod.POST, headers = "Accept=application/json")
	public @ResponseBody void addGroupMembership(@PathVariable("id") String id,
			@PathVariable("group") String group) {
		LOGGER.info(String.format("Add group %2$s to profile of %1$s", id,
				group));

		IdentityService idSvc = processEngine.getIdentityService();
        if (idSvc.createGroupQuery().groupId(group.toLowerCase()).count() == 0) {
			LOGGER.info(String.format("Creating new group %1$s ", group));
            Group grp = idSvc.newGroup(group.toLowerCase());
            grp.setName(group);
            idSvc.saveGroup(grp);
		}
        idSvc.createMembership(id, group.toLowerCase());
	}
}
