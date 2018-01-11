/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package com.knowprocess.identity;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

public class IdentityTask implements JavaDelegate {
    public static final String VAR_USERNAME = "username";
    public static final String VAR_EMAIL = "email";
    public static final String VAR_FIRST_NAME = "firstName";
    public static final String VAR_LAST_NAME = "lastName";
    public static final String VAR_GROUPS = "groups";
    public static final String VAR_PASSWORD = "password";

    // private static final String DELIMITER = "__";

    // public static void createUser(User user, String... groups) {
    // createUser(user.getId(), user.getFirstName(), user.getLastName(),
    // user.getEmail(), groups);
    // }

    public static User createUser(IdentityService identityService,
            String username, String password, String givenName,
            String familyName, String email, String[] groups) {
        // long start = LogUtil.enter(IdentityHelper.class, "createUser");

        // LogUtil.debug(IdentityHelper.class, "found identity service in "
        // + (new Date().getTime() - start));

        // identityService2;
        // Note that jBPM does not protect against creation of non-unique ids
        // after that the database has to be cleaned manually!!!!!!!!!!!!!
        // You have been warned.
        // TODO check if this is still true of Activiti
        User newUser = null;
        // if (identityService.createUserQuery().userId(username).singleResult()
        // == null) {
        try {
            newUser = identityService.newUser(username);
            newUser.setEmail(email);
            newUser.setFirstName(givenName);
            newUser.setLastName(familyName);
            newUser.setPassword(password);
            identityService.saveUser(newUser);
        // }
        } catch (Exception e) {
            throw new ActivitiException(String.format(
                    "Username '%1$s' is already taken", username), e);
        }
        addGroups(username, identityService, groups);
        // LogUtil.exit(IdentityHelper.class, "createUser()", start);
        return newUser;
    }

    public static void addGroups(String username, String... groups) {
        // long start = LogUtil.enter(
        // IdentityHelper.class,
        // String.format("addGroups('%1$s', '%2$s')", username,
        // Arrays.asList(groups)));

        IdentityService identityService = ProcessEngines
                .getDefaultProcessEngine().getIdentityService();
        addGroups(username, identityService, groups);

        // LogUtil.exit(IdentityHelper.class, "addGroups", start);
    }

    private static void addGroups(String username,
            IdentityService identityService, String... groups) {
        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("username may not be null");
        }
        // allow for creating a user with no groups
        if (groups == null) {
            groups = new String[0];
        }

        // test if user already has the membership ...
        for (String group : groups) {
            String groupId = normalize(username, group);
            List<Group> existingMemberships = identityService
                    .createGroupQuery().groupMember(username).list();
            boolean found = false;
            for (Group group2 : existingMemberships) {
                if (group2.getId().equals(groupId)) {
                    found = true;
                }
            }

            // ... user not yet a member of this group, so add membership
            if (!found) {
                // jBPM API does not provide for listing all groups. This is a
                // way to work around that trying to create a membership and if
                // it fails assuming that is because the group does not exist so
                // creating it.
                // Activiti does allow listing of all Groups but this is can
                // still be useful.
                try {
                    identityService.createMembership(username, groupId);
                } catch (Exception e) {
                    // TODO
                    e.printStackTrace();
                    // LogUtil.warn(
                    // IdentityHelper.class,
                    // "Exception: "
                    // + e.getClass()
                    // + ":"
                    // + e.getMessage()
                    // +
                    // ". Assuming group does not exist and attempt to create.");
                    // TODO check actual exception is the expected one 'group x
                    // doesn't exist'
                    Group g = identityService.newGroup(groupId);
                    g.setName(group);
                    identityService.saveGroup(g);
                    identityService.createMembership(username, groupId);
                }
            }
        }
    }

    /**
     * 
     * @param username
     * @param groupName
     *            Name chosen by user as pail name.
     * @return groupId derived from the groupName. //
     */
    private static String normalize(String username, String groupName) {
        String groupId = groupName.toLowerCase().replace(' ', '_');
        return groupId;
    }

    public static void unsubscribeGroups(IdentityService identityService,
            String username, String[] groups) {
        for (String groupId : groups) {
            identityService.deleteMembership(username, groupId);
            if (identityService.createUserQuery().memberOfGroup(groupId)
                    .count() == 0) {
                // LogUtil.warn(IdentityHelper.class, String.format(
                // "Removing memberless group '%1$s'", groupId));
                identityService.deleteGroup(groupId);
            }
        }
    }

    public void execute(DelegateExecution execution) {
        String username = (String) execution.getVariable(VAR_USERNAME);
        String email = (String) execution.getVariable(VAR_EMAIL);
        String firstName = (String) execution.getVariable(VAR_FIRST_NAME);
        String lastName = (String) execution.getVariable(VAR_LAST_NAME);
        String password = (String) execution.getVariable(VAR_PASSWORD);
        if (username == null) {
            username = email;
        }
        IdentityService svc = execution.getEngineServices()
                .getIdentityService();

        String[] groups = (String[]) execution.getVariable(VAR_GROUPS);

        createUser(svc, username, password, firstName, lastName, email, groups);
    }
}
