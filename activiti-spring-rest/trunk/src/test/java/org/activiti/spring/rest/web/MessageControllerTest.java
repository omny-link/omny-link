package org.activiti.spring.rest.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.rest.beans.MessageRegistry;
import org.activiti.spring.rest.cors.PreAuthenticatedAuthentication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;
import org.toxos.activiti.assertion.ProcessAssert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/spring/applicationContext-test.xml",
        "/META-INF/spring/applicationContext-activiti-spring-rest.xml" })
public class MessageControllerTest {

    private static final String BASE_URL = "http://localhost";

    private static final String MSG_NAME = "kp.testInOutMep";

    private static final String PROCESS_IN_OUT_NAME = "MessageControllerTest";
    private static final String PROCESS_IN_ONLY_NAME = "MessageControllerInOnlyTest";
    private static final String USERNAME = "tim@knowprocess.com";

    // @Rule
    // public ActivitiRule activitiRule;

    private Map<String, Object> variableMap;

    private MessageController svc;

    @Autowired
    public ProcessEngine processEngine;

    @Before
    public void setUp() {
        // activitiRule = new ActivitiRule(processEngine);
        // assertNotNull(activitiRule);
        // assertNotNull(activitiRule.getRuntimeService());
        variableMap = new HashMap<String, Object>();

        svc = new MessageController();
        svc.setProcessEngine(processEngine);
        svc.messageRegistry = new MessageRegistry();

        SecurityContextHolder.getContext().setAuthentication(
                new PreAuthenticatedAuthentication(USERNAME));
    }

    @After
    public void tearDown() {
        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.deleteUser(USERNAME);
    }

    @Test
    public void testInOnlyMep() {
        deploy(PROCESS_IN_ONLY_NAME);

        String json = "[{\"contact\":{\"firstName\":\"John\","
                + "\"email\":\"john@knowprocess.com\"}}]";
        variableMap.put(MSG_NAME, json);

        Authentication.setAuthenticatedUserId(USERNAME);
        variableMap.put("initiator", USERNAME);

        ResponseEntity<String> response = svc.doInOnlyMep(
                UriComponentsBuilder.fromHttpUrl(BASE_URL), MSG_NAME, json);
        for (Entry<String, List<String>> entry : response.getHeaders()
                .entrySet()) {
            System.out.println("response header: " + entry.getKey() + ": "
                    + entry.getValue());
        }
        assertEquals("Did not expect any body to response", null,
                response.getBody());

        // String piid = svc.parseInstanceIdFromLocation(response);
        // activitiRule.assertComplete(processInstance);

        assertEquals("Location has wrong value", "http://acme.com/foo/bar/123",
                response.getHeaders().get("Location").get(0));
    }

    @Test
    public void testInOutMep() {
        deploy(PROCESS_IN_OUT_NAME);

        String json = "[{\"contact\":{\"firstName\":\"John\","
                + "\"email\":\"john@knowprocess.com\"}}]";
        variableMap.put(MSG_NAME, json);

        Authentication.setAuthenticatedUserId(USERNAME);
        variableMap.put("initiator", USERNAME);

        ResponseEntity<String> response = svc.doInOutMep(
                UriComponentsBuilder.fromHttpUrl(BASE_URL), MSG_NAME, json);
        for (Entry<String, List<String>> entry : response.getHeaders()
                .entrySet()) {
            System.out.println("response header: " + entry.getKey() + ": "
                    + entry.getValue());
        }
        System.out.println("response body: " + response.getBody());
        assertEquals("{\"initiator\": \"" + USERNAME + "\"}",
                response.getBody());

        assertNotNull("No Location header set.",
                response.getHeaders().get("Location"));
        String piid = svc.parseInstanceIdFromLocation(response);
        ProcessAssert.assertHistoricProcessVariableLatestValueEquals(piid,
                "initiator", USERNAME);
        assertEquals("Location has wrong value",
                "http://localhost/process-instances/" + piid, response
                        .getHeaders().get("Location").get(0));

        List<ProcessInstance> instances = processEngine.getRuntimeService()
                .createProcessInstanceQuery().processInstanceId(piid).list();
        assertEquals(0, instances.size());

        HistoricProcessInstance pi = processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceId(piid)
                .singleResult();
        assertNotNull(pi);

        // TODO cannot use this assertion...
        // java.lang.NullPointerException
        // at
        // org.toxos.activiti.assertion.LogMessageProvider.loadBundle(LogMessageProvider.java:74)
        // ProcessAssert.assertProcessEnded(piid);
    }

    private void deploy(String processName) {
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment().name(processName)
                .addClasspathResource(getProcessResource(processName)).deploy();
        assertNotNull(deployment);
    }

    private String getProcessResource(String processName) {
        return "processes/" + processName + ".bpmn";
    }

}