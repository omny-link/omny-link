package com.knowprocess.bpm.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.knowprocess.bpm.Application;
import com.knowprocess.bpm.model.UserRecord;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class UserRecordControllerTest {

    @Autowired
    public ProcessEngine processEngine;

    private static final String SUFFIX = "Updated";
    private static final String LNAME = "Bear";
    private static final String FNAME = "Yogi";
    private static final String USERNAME = "yogi";
    @Autowired
    public UserRecordController svc;

    public void tearDown() {
        try {
            svc.delete(USERNAME);
            UserRecord userRecord6 = svc.showJson(USERNAME);
            assertNull(userRecord6);
        } catch (Exception e) {
            ; // ok if exception prevented test completing successfully
        }
    }

	@Test
    public void testLifecycle() {
        // CREATE
        UserRecord userRecord = new UserRecord(USERNAME);
        userRecord.setEmail("yogi@knowprocess.com");
        userRecord.setFirstName(FNAME);
        userRecord.setLastName(LNAME);
        UserRecord userRecord2 = svc.registerFromJson(userRecord);

        assertNotNull(userRecord2);
        assertEquals(USERNAME, userRecord2.getId());
        assertEquals(FNAME, userRecord2.getFirstName());
        assertEquals(LNAME, userRecord2.getLastName());

        // RETRIEVE
        UserRecord userRecord3 = svc.showJson(USERNAME);
        assertNotNull(userRecord3);
        assertEquals(USERNAME, userRecord3.getId());
        assertEquals(FNAME, userRecord3.getFirstName());
        assertEquals(LNAME, userRecord3.getLastName());

        // UPDATE
        userRecord3.setFirstName(userRecord3.getFirstName() + SUFFIX);
        userRecord3.setLastName(userRecord3.getLastName() + SUFFIX);
        UserRecord userRecord4 = svc.updateFromJson(userRecord3,
                userRecord3.getId());
        assertNotNull(userRecord4);
        assertEquals(USERNAME, userRecord4.getId());
        assertEquals(FNAME + SUFFIX, userRecord4.getFirstName());
        assertEquals(LNAME + SUFFIX, userRecord4.getLastName());

        // RESET PASSWORD
        svc.updatePassword(USERNAME, "PASS2", "PASS2");
        User user = processEngine.getIdentityService().createUserQuery()
                .userId(USERNAME).singleResult();
        assertNotNull(user);
        assertEquals("PASS2", user.getPassword());

        // DELETE
        svc.delete(USERNAME);
        try {
            svc.showJson(USERNAME);
            fail("Should not have found user record here");
        } catch (ActivitiObjectNotFoundException e) {
            ; // good
        }
	}

}
