package com.knowprocess.bpmn;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

public class BpmnHelperTest {

	@Test
	public void testSplitItpCollaboration() {
		BpmnHelper helper = new BpmnHelper();
		File outputDir = new File(new File("target"), "itp");
		outputDir.mkdirs();
		helper.splitCollaboration(
				getClass().getResourceAsStream(
						"/process/miwg2/itp/TechnicalSupport.bpmn"),
				outputDir);
	}

	@Test
	@Ignore
	// Fails to find collaboration section (note that it uses qualified element
	// names unlike ITP.
	public void testSplitTrisotechCollaboration() {
		BpmnHelper helper = new BpmnHelper();
		File outputDir = new File(new File("target"), "trisotech");
		outputDir.mkdirs();
		helper.splitCollaboration(
				getClass().getResourceAsStream(
						"/process/miwg2/trisotech/TechnicalSupport.bpmn"),
				outputDir);
	}
}
