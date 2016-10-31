package com.knowprocess.mail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.bdd.test.mailserver.TestMailServer;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.test.JobTestHelper;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.toxos.activiti.assertion.ProcessAssert;


public class TemplatedMailshotProcessTest {

    private static final String TEST_ADDRESSEE = "fred@bedrock.com";
    private static final String TEST_FNAME = "Fred";
    public static final String MSG_NAME = "messageName";
    public static final String MSG_MAILSHOT = "com.knowprocess.mail.MailData";
    private static final String TEST_SUBJECT = "Test mail";
    private static final String TEST_TEMPLATE_BASE = "http://knowprocess.com/firmgains/emails";
    private static final String TEST_TEMPLATE = "/new-registration";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    private Map<String, Object> variableMap;
    private MailData mailData;

    @BeforeClass
    public static void setUpClass() {
        try {
            URL url = new URL(TEST_TEMPLATE_BASE);
            URLConnection urlConnection = url.openConnection();
            Object content = urlConnection.getContent();
            assertNotNull(content);
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            Assume.assumeTrue("No connection to test server, assume ok", false);
        }
    }

    @Before
    public void setUp() {
        variableMap = new HashMap<String, Object>();
        String json = "{ \"subject\": \"" + TEST_SUBJECT + "\","
                + "\"sendAt\": \"\","
                + "\"templateBase\": \"" + TEST_TEMPLATE_BASE + "\","
                + "\"template\": \"" + TEST_TEMPLATE + "\","
                + "\"contact\": { \"email\": \"" + TEST_ADDRESSEE + "\" } }";
        mailData = new MailData().fromJson(json);
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "process/com/knowprocess/mail/TemplatedMailshot.bpmn",
            "process/com/knowprocess/mail/TemplatedMailshotInternal.bpmn" })
    public void testImmediateMailshotViaMessageStart() {

        variableMap.put(MSG_NAME, adapt(MSG_MAILSHOT));
        variableMap.put(adapt(MSG_MAILSHOT), mailData);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        assertNotNull(runtimeService);
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByMessage(MSG_MAILSHOT, variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());

        JobTestHelper.waitForJobExecutorToProcessAllJobs(activitiRule, 2000, 1);

        ProcessAssert.assertProcessEnded(processInstance);
        activitiRule.dumpVariables(processInstance.getId());

        checkMail();
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = "process/com/knowprocess/mail/TemplatedMailshotInternal.bpmn")
    public void testImmediateMailshotViaNoneStart() {
        variableMap.put("contactEmail", TEST_ADDRESSEE);
        variableMap.put("contactFirstName", TEST_FNAME);
        variableMap.put("contactLastName", null);
        variableMap.put("sendAt", null);
        variableMap.put("subject", TEST_SUBJECT);
        variableMap.put("templateBase", TEST_TEMPLATE_BASE);
        variableMap.put("template", TEST_TEMPLATE);
        // This not needed by the process but by the templates
        variableMap.put("mailData", mailData);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("TemplatedMailshotInternal",
                        variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());

        JobTestHelper.executeJobExecutorForTime(activitiRule, 2000, 1);

        ProcessAssert.assertProcessEnded(processInstance);
        activitiRule.dumpVariables(processInstance.getId());

        checkMail();
    }

    private void checkMail() {
        try {
            mailServer.dumpMailSent();
        } catch (Exception e1) {
            e1.printStackTrace();
            fail(e1.getMessage());
        }

        String[] to = { TEST_ADDRESSEE };
        try {
            mailServer.assertEmailSend(0, true/* HTML mail */, TEST_SUBJECT,
                    "Good luck!", "donotreply@knowprocess.com",
                    Arrays.asList(to));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private String adapt(String msgName) {
        return msgName.replace('.', '_');
    }

}