package org.activiti.spring.rest.web;

import static org.junit.Assert.assertNotNull;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.spring.rest.model.UserRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.roo.addon.test.RooIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
		"/META-INF/spring/applicationContext-activiti-spring-rest.xml",
		"/META-INF/spring/applicationContext-test.xml" })
@RooIntegrationTest(entity = UserRecord.class)
public class UserRecordIntegrationTest {

	private static final String USER_ID = "tim@knowprocess.com";
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
	}

	@After
	public void tearDown() {
		IdentityService idSvc = processEngine.getIdentityService();
		idSvc.deleteUser(USER_ID);
	}

	@Test
	public void testShowJsonProfile() {
		String path = "/users/" + USER_ID;
		MockHttpServletRequest req = new MockHttpServletRequest("get",
				path);
		req.setServletPath(path);
		// req.addParameter(name, value);
		svc.showJson(USER_ID, req);
    }
}
