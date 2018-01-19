/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.model.UserGroup;
import com.knowprocess.bpm.model.UserInfo;
import com.knowprocess.bpm.model.UserInfoKeys;
import com.knowprocess.bpm.model.UserRecord;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Controller
public class UserRecordController {

    protected static final String RESET_PWD_DEFINITION_KEY = "ResetPassword";

    protected static final String PATH = "/users";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UserRecordController.class);

    @Autowired(required = true)
    ProcessEngine processEngine;

    @RequestMapping(value = "/{tenantId}/users/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<UserRecord> showAllJson(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info("showAllJson");
        List<UserRecord> users
                = UserRecord.findAllUserRecords("id", "asc", tenantId);

        // HttpHeaders headers = new HttpHeaders();
        // headers.add("Content-Type", "application/json; charset=utf-8");

        LOGGER.info("Users: " + users.size());
        return users;
    }

    @RequestMapping(value = "/users/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody UserRecord showJson(
            @PathVariable("id") String id,
            HttpServletRequest request) {
        LOGGER.info(String
                .format("%1$s %2$s/%3$s", RequestMethod.GET, PATH, id));
        // Since id will end .com (or similar TLE) need to get id from request
        // directly as Spring will truncate the extension.
        id = request.getServletPath().substring(
                request.getServletPath().lastIndexOf('/') + 1);

        return wrap(showJson(id));
    }

    @RequestMapping(value = "/{tenantId}/users/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody UserRecord showJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            HttpServletRequest request) {
        return showJson(id, request);
    }

    protected @ResponseBody UserRecord showJson(
            @PathVariable("id") String id) {
        LOGGER.info("Find user with id: " + id);
        // HttpHeaders headers = new HttpHeaders();
        // headers.add("Content-Type", "application/json; charset=utf-8");
        UserRecord userRecord;
        if (id.indexOf(' ') == -1) {
            userRecord = UserRecord.findUserRecord(id);
        } else {
            LOGGER.warn("TODO This is probably firstName / lastName not id");
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

    @ResponseStatus(code = HttpStatus.CREATED)
    @RequestMapping(value = "/{tenantId}/users/", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody UserRecord registerFromJson(
            @PathVariable("tenantId") String tenantId,
            @RequestBody UserRecord userRecord) {
        LOGGER.info(String.format("Creating profile of %1$s",
                userRecord.getEmail()));

        if (!userRecord.isPasswordSetOk()) {
            throw new IllegalArgumentException(
                    "Password must be either null or match the confirmPassword field on registration");
        }
        IdentityService idSvc = processEngine.getIdentityService();
        User user = idSvc.newUser(userRecord.getId().toLowerCase().trim());
        idSvc.saveUser(user);
        return updateFromJson(tenantId, userRecord, userRecord.getId());
    }

    @RequestMapping(value = "/{tenantId}/users/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
    public @ResponseBody UserRecord updateFromJson(
            @PathVariable("tenantId") String tenantId,
            @RequestBody UserRecord userRecord, @PathVariable("id") String id) {
        LOGGER.info(String.format("Updating profile of %1$s", id));

        // Since id will end .com (or similar TLE) need to get id from request
        // directly as Spring will truncate the extension.
        id = userRecord.getId();

        // UserRecord user = UserRecord.findUserRecord(id);
        IdentityService idSvc = processEngine.getIdentityService();
        User user = idSvc.createUserQuery().userId(id).singleResult();
        if (user == null) throw new ActivitiObjectNotFoundException(
                String.format("Cannot find user with id '%1$s'", id), User.class);

        LOGGER.info("Found user to update: " + user);
        if (userRecord.getEmail() != null
                && userRecord.getEmail().trim().length() > 0) {
            user.setEmail(userRecord.getEmail().toLowerCase().trim());
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
            LOGGER.debug("Found user info: {} for key: {}, setting to: {}",
                    userInfo, info.getKey(), info.getValue());
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

    @RequestMapping(value = "/{tenantId}/users/{id}/info/{key}", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void updateInfo(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            @PathVariable("key") String key,
            @RequestBody UserInfo userInfo) {
        LOGGER.info(String.format("Updating info %2$s of %1$s", id, key));

        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.setUserInfo(id, key, userInfo.getValue());
    }

    @RequestMapping(value = "/users/{id}/reset-password", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void updatePassword(
            @PathVariable("id") String id,
            @RequestParam(name = "password") String pwd,
            @RequestParam(name = "password2") String pwd2) {
        LOGGER.info(String.format("Updating password of %1$s", id));

        if (!pwd.equals(pwd2)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        IdentityService idSvc = processEngine.getIdentityService();
        User user = idSvc.createUserQuery().userId(id).singleResult();
        user.setPassword(pwd);
        idSvc.saveUser(user);
    }

    @RequestMapping(value = "/{tenantId}/users/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public @ResponseBody void delete(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            HttpServletRequest request) {
        LOGGER.info(String.format("Deleting profile of %1$s", id));

        // Since id will end .com (or similar TLE) need to get id from request
        // directly as Spring will truncate the extension.
        id = request.getServletPath().substring(
                request.getServletPath().lastIndexOf('/') + 1);

        delete(tenantId, id);
    }

    public @ResponseBody void delete(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("Deleting profile of %1$s", id));

        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.deleteUser(id);
    }

    @RequestMapping(value = "/{tenantId}/users/{id}/groups", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<UserGroup> listGroups(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("List groups for %1$s", id));

        IdentityService idSvc = processEngine.getIdentityService();
        return UserGroup.wrap(idSvc.createGroupQuery().groupMember(id).list());
    }

    @RequestMapping(value = "/{tenantId}/users/{id}/groups/{group}", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void addGroupMembership(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
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

    private UserRecordResource wrap(UserRecord user) {
        UserRecordResource resource = new UserRecordResource();
        BeanUtils.copyProperties(user, resource);
        Link detail = new Link(String.format(
                "/%1$s/users/%2$s", user.getTenant(), user.getId()));
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    @RequestMapping(value = "/{tenantId}/users/{id}/groups/{group}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public @ResponseBody void deleteGroupMembership(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id,
            @PathVariable("group") String group) {
        LOGGER.info(String.format("Delete group %2$s to profile of %1$s", id,
                group));

        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.deleteMembership(id, group.toLowerCase());
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UserRecordResource extends UserRecord
    /* implements Identifiable <Link> */
    {
        private static final long serialVersionUID = -7032898225954476512L;
        private String selfRef;
        private List<Link> links;

        public void add(Link link) {
            if (links == null) {
                links = new ArrayList<Link>();
            }
            links.add(link);
        }
    }
}
