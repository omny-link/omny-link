package org.activiti.spring.rest.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.spring.rest.model.UserRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
 "/META-INF/spring/applicationContext-test.xml",
        "/META-INF/spring/applicationContext-activiti-spring-rest.xml" })
public class UserRecordControllerTest {

    private static final String USER_ID = "tim@knowprocess.com";
    private static final String NEW_KINDLE_ADDRESS = "tstephenson05@kindle.com";
    private UserRecordController svc = new UserRecordController();

    @Autowired
    public ProcessEngine processEngine;

    @Before
    public void setUp() {
        assertNotNull(processEngine);
        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USER_ID));
        // For some reason Autowired is no working in unit test environment
        new UserRecord().setProcessEngine(processEngine);
        svc.processEngine = processEngine;
    }

    @After
    public void tearDown() {
        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.deleteUser(USER_ID);
    }

    @Test
    public void testShowJsonProfile() {
        String path = "/users/" + USER_ID;
        MockHttpServletRequest req = new MockHttpServletRequest("get", path);
        req.setServletPath(path);
        // req.addParameter(name, value);
        ResponseEntity<String> entity = svc.showJson(USER_ID, req);
        String json = entity.getBody();
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonObject obj = reader.readObject();
        System.out.println("obj" + obj);
        assertEquals(USER_ID, obj.getString("id"));

    }

    @Test
    public void testUpdateProfileFromJson() {
        String kindleAddress = processEngine.getIdentityService().getUserInfo(
                USER_ID, "kindle");
        assertNull(kindleAddress);
        String json = "{\"username\":\"timatthestephensons@gmail.com\",\"firstName\":\"Tim\",\"lastName\":\"Stephenson\",\"twitter\":\"tstephen10\",\"kindle\":\""
                + NEW_KINDLE_ADDRESS
                + "\",\"sugarUsername\":\"\",\"sugarPassword\":\"\",\"sugarUrl\":\"\",\"info\":[{\"key\":\"twitter\",\"value\":\"tstephen10\"},{\"key\":\"kindle\",\"value\":\"tstephenson05@kindle.com\"},{\"key\":\"sugarUsername\",\"value\":\"\"},{\"key\":\"sugarPassword\",\"value\":\"\"},{\"key\":\"sugarUrl\",\"value\":\"\"}]}";

        ResponseEntity<String> entity = svc.updateFromJson(json, USER_ID);
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        assertEquals(NEW_KINDLE_ADDRESS, processEngine.getIdentityService()
                .getUserInfo(USER_ID, "kindle"));
    }

}
