package com.knowprocess.in;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.code.linkedinapi.schema.Connections;
import com.knowprocess.in.filters.IndustryInterestsFilter;

public class LinkedInTasksTest extends AbstractLinkedInTest {

	@Test
	public void testSendMessage() {
		SendLinkedInMessageTask svc = new SendLinkedInMessageTask();
		svc.setIdentityService(activitiRule.getIdentityService());
		svc.sendMessage(INITIATOR, "Hello!", "Hi\n\nLook ma no hands!",
				Collections.singletonList("~"));
	}

	@Test
	public void testSetStatus() {
		PostLinkedInStatusTask svc = new PostLinkedInStatusTask();
		svc.setIdentityService(activitiRule.getIdentityService());
		svc.postStatus(INITIATOR, "Hello!");

		svc.removeCurrentStatus(INITIATOR);
	}

	@Test
	public void testGetConnectionIds() {
		GetConnectionsTask svc = new GetConnectionsTask();
		svc.setIdentityService(activitiRule.getIdentityService());
		Connections conns = svc.getConnections(INITIATOR);
		PersonFilter filter = svc.getFilter("");
		List<String> idsOfConnections = svc.getIdsOfConnections(conns, filter);
		String connectionsJson = svc.getConnectionsAsJson(conns, filter);
		System.out.println(connectionsJson);
		assertTrue(idsOfConnections.size() >= 355);
	}

	@Test
	public void testGetNameFilteredConnectionIds() {
		GetConnectionsTask svc = new GetConnectionsTask();
		svc.setIdentityService(activitiRule.getIdentityService());
		Connections conns = svc.getConnections(INITIATOR);
		PersonFilter filter = svc.getFilter("Stephenson");
		List<String> idsOfConnections = svc.getIdsOfConnections(conns, filter);
		String connectionsJson = svc.getConnectionsAsJson(conns, filter);
		System.out.println(connectionsJson);
		assertTrue(idsOfConnections.size() >= 3);
	}

	@Test
	public void testGetIndustryFilteredConnectionIds() {
		GetConnectionsTask svc = new GetConnectionsTask();
		svc.setIdentityService(activitiRule.getIdentityService());
		Connections conns = svc.getConnections(INITIATOR);
		PersonFilter filter = new IndustryInterestsFilter("Internet");
		List<String> idsOfConnections = svc.getIdsOfConnections(conns, filter);
		String connectionsJson = svc.getConnectionsAsJson(conns, filter);
		System.out.println(connectionsJson);
		assertTrue(idsOfConnections.size() >= 1);
	}
}
