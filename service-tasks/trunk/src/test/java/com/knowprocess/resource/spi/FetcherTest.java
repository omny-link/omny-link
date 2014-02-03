package com.knowprocess.resource.spi;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;

import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.repository.Deployment;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.resource.internal.gdrive.GDriveConfigurationException;
import com.knowprocess.test.activiti.ExtendedRule;

public class FetcherTest {
	@Rule
	public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

	private Fetcher svc;
	private String resourceUrl = "http://farm4.staticflickr.com/3140/3094868910_41c19ce2a3_b_d.jpg";
	private String resourceUrl2 = "http://farm4.staticflickr.com/3025/3094867738_1300826ed5_q_d.jpg";
	private String repoUri = "https://docs.google.com/tux-collage.jpg";
	private String repoUri2 = "https://docs.google.com/sub-folder/java-collage.jpg";

	@Before
	public void setUp() throws Exception {
		svc = new Fetcher();
	}

	@Test
	public void testUploadImageToRootFolder() {
		try {
			svc.fetchToRepo(resourceUrl, repoUri);
		} catch (GDriveConfigurationException e) {
			System.err
					.println("Skipping test as GDrive not configured, did you supply a secret?");
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	/*
	 * Does not work (uploads to root folder)
	 * 
	 * @Test public void testUploadImageToSubFolder() { try {
	 * svc.fetchToRepo(resourceUrl2, repoUri2); } catch (IOException e) {
	 * e.printStackTrace(); fail(); } }
	 */

	@Test
	public void testClasspathResourceToString() {
		try {
			String bpmn = svc
					.fetchToString("classpath:///process/com/knowprocess/deployment/DeploymentProcess.bpmn");
			System.out.println("BPMN: " + bpmn);
			assertNotNull(bpmn);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDeploymentResourceToString() {
		try {
			Deployment deployment = activitiRule.getRepositoryService()
					.createDeployment().name("TestMailshotDeployment")
					.addClasspathResource("process/TestMailshot.bpmn")
					.addClasspathResource("templates/Mailshot.txt").deploy();
			assertNotNull(deployment);
			System.out.println("deployment id: " + deployment.getId());

			String resource = new String(((DeploymentEntity) deployment)
					.getResource("templates/Mailshot.txt").getBytes());
			System.out.println("resource: " + resource);
			assertNotNull(resource);
			assertTrue(resource.length() > 0);

			svc.setRepositoryService(activitiRule.getRepositoryService());
			String txt = svc
					.fetchToString("activiti://TestMailshot/templates/Mailshot.txt");
			System.out.println("TEXT: " + txt);
			assertNotNull(txt);
			assertTrue(txt.contains("Kind regards,"));
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testUrlPdfResourceToBlob() {
		FileWriter writer = null;
		try {
			byte[] pdf = svc
					.fetchToByteArray("https://www.gov.uk/government/uploads/system/uploads/attachment_data/file/85869/release-carbon-footprint-dec2012.pdf");
			System.out.println("PDF: " + pdf);
			assertNotNull(pdf);

			File file = new File("testUrlPdfResourceToBlob.pdf");
			OutputStream pdfout = new FileOutputStream(file);
			pdfout.write(pdf);
			System.out.println("Wrote pdf to: " + file);

		} catch (UnknownHostException e) {
			Assume.assumeNoException(
					String.format(
							"Test failed to fetch resource from '%1$s'. Assume because we are running test whilst offline",
							e.getMessage()), e);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}
	}
}
