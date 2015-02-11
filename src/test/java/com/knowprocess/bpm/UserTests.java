package com.knowprocess.bpm;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.ProcessEngine;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class UserTests {

	@Autowired
	ProcessEngine processEngine;

	RestTemplate restTemplate = new TestRestTemplate();

	@Test
	@Ignore
	public void testUserLifecycle() {
		ResponseEntity<String> response = restTemplate.getForEntity(
				"http://localhost:8080/users/", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());

		System.out.println(response.getBody());
	}

}
