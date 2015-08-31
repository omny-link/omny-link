package com.knowprocess.xslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;

import org.junit.Before;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;

public class TransformTaskTest {

	private static final String ACTIVITI_SUPPORT_BPMN = "classpath:///process/activiti-support.bpmn";
    private static final String DATA_OBJECT_BPMN = "classpath:///process/miwg2/handle-invoice-with-data-objects.bpmn";
    private static final String SHORTCUT_POTENTIAL_OWNERS_BPMN = "classpath:///process/miwg2/handle-invoice-no-service-tasks.bpmn";
	private TransformTask svc;

	@Before
	public void setUp() throws Exception {
		svc = new TransformTask();
	}

	@Test
	public void testTransformStringToString() {
		try {
			String bpmn = new Fetcher().fetchToString(ACTIVITI_SUPPORT_BPMN);

			svc.setXsltResources("/xslt/ActivitiSupportRules.xsl");
			System.out.println("BPMN: " + bpmn.toString());
			assertTrue(bpmn.toString().trim().endsWith(">"));
			String result = svc.transform(bpmn.toString().trim());
			System.out.println("result: " + result);
			assertNotNull(result);

			assertResults(result, 2, 14, 22);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testTransformStringThru2Stylesheets() {
		try {
			String bpmn = new Fetcher().fetchToString(ACTIVITI_SUPPORT_BPMN);

			svc.setXsltResources("/xslt/ExecutableTweaker.xsl,/xslt/KpSupportRules.xsl");
			System.out.println("BPMN: " + bpmn.toString());
			assertTrue(bpmn.toString().trim().endsWith(">"));
			String result = svc.transform(bpmn.toString().trim());
			System.out.println("result: " + result);
			assertNotNull(result);

            assertResults(result, 2, 14, 22);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void assertResults(String result, int expectedErrors, int expectedIgnores, int expectedPasses) {
		String[] messages = result.split("\\n", 0);
		System.out.println("messages found: " + messages.length);
		List<String> ignored = new ArrayList<String>();
		List<String> passed = new ArrayList<String>();
		List<String> errors = new ArrayList<String>();
		for (String string : messages) {
			String msg = string.trim();
			if (msg.length() > 0
					&& msg.toUpperCase().startsWith(TransformTask.ERROR_KEY)) {
				errors.add(msg);
			} else if (msg.length() > 0
					&& msg.toUpperCase().startsWith(TransformTask.PASS_KEY)) {
				passed.add(msg);
			} else if (msg.length() > 0
					&& msg.toUpperCase().startsWith(TransformTask.IGNORED_KEY)) {
				ignored.add(msg);
			}
		}
		System.err.println("ERRORS:" + errors.size());
		System.err.println("IGNORED:" + ignored.size());
		System.err.println("PASSED:" + passed.size());
		assertEquals(expectedErrors, errors.size());
		assertEquals(expectedIgnores, ignored.size());
		assertEquals(expectedPasses, passed.size());
	}

    @Test
    public void testAddFormProperties() throws IOException,
            TransformerConfigurationException {
        String bpmn = new Fetcher().fetchToString(DATA_OBJECT_BPMN);

        svc.setXsltResources("/xslt/ExecutableTweaker.xsl");
        System.out.println("BPMN: " + bpmn.toString());
        assertTrue(bpmn.toString().trim().endsWith(">"));
        String result = svc.transform(bpmn.toString().trim());
        System.out.println("result: " + result);
        assertNotNull(result);
        Pattern formPropertyPattern = Pattern.compile("formProperty(.*?)>");
        Matcher m = formPropertyPattern.matcher(result);
        List<String> formProperties = new ArrayList<String>();
        while (m.find()) {
            String s = m.group(1);
            formProperties.add(s);
        }
        assertEquals(2, formProperties.size());
        assertTrue(formProperties.get(0).contains("type=\"boolean\""));
        assertTrue(formProperties.get(1).contains("type=\"string\""));
    }

    @Test
    public void testSuppressShortcutsWhenHavePotentialOwner()
            throws IOException, TransformerConfigurationException {
        String bpmn = new Fetcher()
                .fetchToString(SHORTCUT_POTENTIAL_OWNERS_BPMN);

        svc.setXsltResources("/xslt/ExecutableTweaker.xsl");
        System.out.println("BPMN: " + bpmn.toString());
        assertTrue(bpmn.toString().trim().endsWith(">"));
        String result = svc.transform(bpmn.toString().trim());
        System.out.println("result: " + result);
        assertNotNull(result);
        assertTrue(!result.contains("assignee"));
    }

    @Test
    public void testConvertUnsupportedServiceTaskToUserTask()
            throws IOException, TransformerConfigurationException {
        String bpmn = new Fetcher().fetchToString(DATA_OBJECT_BPMN);

        svc.setXsltResources("/xslt/ExecutableTweaker.xsl");
        System.out.println("BPMN: " + bpmn.toString());
        assertTrue(bpmn.toString().trim().endsWith(">"));
        String result = svc.transform(bpmn.toString().trim());
        System.out.println("result: " + result);
        assertNotNull(result);
        assertTrue(result.contains("assignee=\"${initiator}\""));
    }
}
