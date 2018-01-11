/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
