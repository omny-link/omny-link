package com.knowprocess.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class IdentityTaskTest {

    private static final String LAST_NAME = "Stephenson";
    private static final String FIRST_NAME = "Tim";
    private static final String USER_TSTEPHEN = "tstephen@knowprocess.com";
    private static final String PWD = "secure";
    private String[] groups;

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("test-activiti.cfg.xml");

    @Before
    public void setUp() throws Exception {
        groups = new String[] { "finance-reporting", "finance" };
        for (String groupId : groups) {
            Group group = activitiRule.getIdentityService().newGroup(groupId);
            activitiRule.getIdentityService().saveGroup(group);
        }
    }

    @After
    public void tearDown() throws Exception {
        for (String groupId : groups) {
            activitiRule.getIdentityService().deleteMembership(USER_TSTEPHEN,
                    groupId);
            activitiRule.getIdentityService().deleteGroup(groupId);
        }
        activitiRule.getIdentityService().deleteUser(USER_TSTEPHEN);
    }

    @Test
    public void testCreateUser() {
        User user = IdentityTask.createUser(activitiRule.getIdentityService(),
                USER_TSTEPHEN, PWD, FIRST_NAME, LAST_NAME, USER_TSTEPHEN,
                groups);
        assertNotNull(user);
        assertEquals(USER_TSTEPHEN, user.getId());

        try {
            IdentityTask.createUser(activitiRule.getIdentityService(),
                    USER_TSTEPHEN, PWD, FIRST_NAME, LAST_NAME, USER_TSTEPHEN,
                    groups);
            fail("Should have rejected username as duplicate.");
        } catch (ActivitiException e) {
            ;
        }
    }

    @Test
    public void testConfirmUser() {

    }

    @Test
    public void testResetPassword() {

    }
}
