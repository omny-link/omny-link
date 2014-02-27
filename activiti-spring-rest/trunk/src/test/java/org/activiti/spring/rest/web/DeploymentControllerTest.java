package org.activiti.spring.rest.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
		"/META-INF/spring/applicationContext-activiti-spring-rest.xml",
		"/META-INF/spring/applicationContext-test.xml" })
public class DeploymentControllerTest {

	private static final String PROCESS_RESOURCE = "/processes/DeploymentControllerTest.bpmn";
	private static final String BASE_URL = "http://localhost";
	private static final String ENDPOINT = "/deployments";
	private static final String USER_ID = "tim@knowprocess.com";
	private static final String DEPLOYMENT_NAME = "A test process";

	private DeploymentController svc = new DeploymentController();

	@Autowired
	public ProcessEngine processEngine;

	private List<Deployment> deployments;

	@Before
	public void setUp() {
		assertNotNull(processEngine);
		IdentityService idSvc = processEngine.getIdentityService();
		try {
			idSvc.saveUser(idSvc.newUser(USER_ID));
		} catch (Exception e) {
			System.err.println("Hopefully this is a temporary glitch ");
		}
		// For some reason Autowired is no working in unit test environment
		svc.processEngine = this.processEngine;
	}

	@After
	public void tearDown() {

		for (Deployment d : deployments) {
			processEngine.getRepositoryService().deleteDeployment(d.getId());
		}
		IdentityService idSvc = processEngine.getIdentityService();
		idSvc.deleteUser(USER_ID);
	}

	@Test
	public void testUploadRequest() {
		InputStream is = getClass().getResourceAsStream(
				PROCESS_RESOURCE);
		assertNotNull("Failed to find bpmn file to upload");
		try {
			MockMultipartFile multipartFile = new MockMultipartFile(
					"resourceFile",
					"DeploymentControllerTest.bpmn", "text/xml", is);

			ResponseEntity<String> entity = svc.uploadMultipleFiles(
					UriComponentsBuilder.fromHttpUrl(BASE_URL),
					DEPLOYMENT_NAME,
					multipartFile);
			System.out.println(entity);
			System.out.println(entity.getStatusCode());

			assertEquals(HttpStatus.CREATED, entity.getStatusCode());

			URI location = entity.getHeaders().getLocation();
			assertNotNull(location);
			String url = location.toURL().toExternalForm();
			System.out.println("URL: " + url);
			assertTrue(url.startsWith(BASE_URL + ENDPOINT));

			String deploymentId = url.substring(url.lastIndexOf('/') + 1);
			System.out.println("Deployment id:" + deploymentId);
			assertNotNull(deploymentId);
			deployments = processEngine.getRepositoryService()
					.createDeploymentQuery().deploymentId(deploymentId).list();
			assertEquals(1, deployments.size());

			List<ProcessDefinition> definitions = processEngine
					.getRepositoryService().createProcessDefinitionQuery()
					.deploymentId(deploymentId).list();
			for (ProcessDefinition def : definitions) {
				System.out.println(String.format("%1$s: %2$s", def.getId(),
						def.getKey()));
			}
			assertEquals(1, definitions.size());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
