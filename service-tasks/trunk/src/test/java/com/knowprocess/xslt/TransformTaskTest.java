package com.knowprocess.xslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;

public class TransformTaskTest {

	private static final String ACTIVITI_SUPPORT_BPMN = "classpath:///process/activiti-support.bpmn";
	private TransformTask svc;

	@Before
	public void setUp() throws Exception {
		svc = new TransformTask();
	}

	@Test
	public void testTransformStringToString() {
		try {
			String bpmn = new Fetcher().fetchToString(ACTIVITI_SUPPORT_BPMN);

			svc.setXsltResource("/xslt/ActivitiSupportRules.xsl");
			System.out.println("BPMN: " + bpmn.toString());
			assertTrue(bpmn.toString().trim().endsWith(">"));
			String result = svc.transform(bpmn.toString().trim());
			System.out.println("result: " + result);
			assertNotNull(result);

			assertResults(result);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testTransformStringThru2Stylesheets() {
		try {
			String bpmn = new Fetcher().fetchToString(ACTIVITI_SUPPORT_BPMN);

			svc.setXsltResource("/xslt/ExecutableTweaker.xsl,/xslt/KpSupportRules.xsl");
			System.out.println("BPMN: " + bpmn.toString());
			assertTrue(bpmn.toString().trim().endsWith(">"));
			String result = svc.transform(bpmn.toString().trim());
			System.out.println("result: " + result);
			assertNotNull(result);

			assertResults(result);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void assertResults(String result) {
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
		assertEquals(2, errors.size());
		assertEquals(14, ignored.size());
		assertEquals(22, passed.size());
	}

}
