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
package com.knowprocess.xslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;

import org.junit.Before;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;

public class TransformTaskTest {

	private static final String ACTIVITI_SUPPORT_BPMN = "classpath:///processes/miwg/activiti-support.bpmn";
    private static final String DATA_OBJECT_BPMN = "classpath:///processes/miwg/handle-invoice-with-data-objects.bpmn";
    private static final String SHORTCUT_POTENTIAL_OWNERS_BPMN = "classpath:///processes/miwg/handle-invoice-no-service-tasks.bpmn";
    private static final String JSF_FORM_KEY_BPMN = "classpath:///processes/miwg/3_trisotech_handle-invoice.bpmn";
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

            // ERROR: Timer events must specify exactly one of: timeDate, timeDuration or timeCycle for timerEventDefinition with id: invalidTimerUnderSpecified
            assertResults(result, 1, 15, 22);
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
    public void testSuppressJsfFormKey() throws IOException,
            TransformerConfigurationException {
        String bpmn = new Fetcher().fetchToString(JSF_FORM_KEY_BPMN);

        svc.setXsltResources("/xslt/ExecutableTweaker.xsl");
        System.out.println("BPMN: " + bpmn.toString());
        assertTrue(bpmn.toString().trim().endsWith(">"));
        String result = svc.transform(bpmn.toString().trim());
        System.out.println("result: " + result);
        assertNotNull(result);
        assertTrue(!result.contains(".jsf"));
    }

    @Test
    public void testConvertUnsupportedServiceTaskToUserTask()
            throws IOException, TransformerConfigurationException {
        String bpmn = new Fetcher().fetchToString(DATA_OBJECT_BPMN);

        svc.setXsltResources("/xslt/ExecutableTweaker.xsl");
        System.out.println("BPMN: " + bpmn.toString());
        assertTrue(bpmn.toString().trim().endsWith(">"));
        String result = svc.transform(bpmn.toString().trim(), Collections.singletonMap("unsupportedTasksToUserTask", "true"));
        System.out.println("result: " + result);
        assertNotNull(result);
        assertTrue(result.contains("assignee=\"${initiator}\""));
    }
}
