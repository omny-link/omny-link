package com.knowprocess.mail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.test.activiti.ExtendedRule;
import com.knowprocess.test.mailserver.TestMailServer;

public class TemplatedMailshotProcessTest {

    private static final String TEST_ADDRESSEE = "fred@bedrock.com";
    public static final String MSG_NAME = "messageName";
    public static final String MSG_MAILSHOT = "com.knowprocess.mail.MailData";
    private static final String TEST_SUBJECT = "Test mail";
    private static final String TEST_TEMPLATE = "http://wp.knowprocess.com/wp-content/plugins/syncapt/emails/new-registration";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    private Map<String, Object> variableMap;
    private MailData mailData;

    @Before
    public void setUp() {
        variableMap = new HashMap<String, Object>();
        String json = "{ \"subject\": \"" + TEST_SUBJECT + "\","
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

        activitiRule.assertComplete(processInstance);
        activitiRule.dumpVariables(processInstance.getId());

        checkMail();
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = "process/com/knowprocess/mail/TemplatedMailshotInternal.bpmn")
    public void testImmediateMailshotViaNoneStart() {

        variableMap.put("mailData", mailData);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("TemplatedMailshotInternal",
                        variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());

        activitiRule.assertComplete(processInstance);
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