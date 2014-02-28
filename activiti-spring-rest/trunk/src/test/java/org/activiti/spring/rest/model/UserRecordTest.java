package org.activiti.spring.rest.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class UserRecordTest {
	private String userRecord = "{\"email\":\"tim@knowprocess.com\",\"firstName\":\"Tim\",\"groups\":[],\"id\":\"1\",\"info\":[{\"id\":\"\",\"key\":\"foo\",\"value\":\"bar\",\"version\":\"\",\"foo\":\"bar\"}],\"lastName\":\"Stephenson\",\"version\":1}";
	private String userRecord2 = "{\"email\":\"tim@knowprocess.com\",\"firstName\":\"Tim\",\"groups\":[],\"id\":\"1\",\"info\":[{\"id\":\"\",\"key\":\"foo2\",\"value\":\"bar2\",\"version\":\"\",\"foo2\":\"bar2\"},{\"id\":\"\",\"key\":\"foo\",\"value\":\"bar\",\"version\":\"\",\"foo\":\"bar\"}],\"lastName\":\"Stephenson\",\"version\":1}";
	private UserRecord user;

	@Before
	public void setUp() {
		user = new UserRecord();
		user.setEmail("tim@knowprocess.com");
		user.setFirstName("Tim");
		user.setId("1");
		user.setLastName("Stephenson");
		user.setVersion(1);
		UserInfo info = new UserInfo("foo", "bar");
		user.getInfo().add(info);
	}

	@Test
	@Ignore
	// Disabled as found diff way to do this client side only but want to
	// capture in svn history
	public void testCustomUserInfoSerialisation() {
		String json = user.toJson();
		System.out.println("JSON: " + json);
		assertEquals(userRecord, json);
	}

	@Test
	@Ignore
	// Disabled as found diff way to do this client side only but want to
	// capture in svn history
	public void testCustomMoreThanOneUserInfoSerialisation() {
		user.getInfo().add(new UserInfo("foo2", "bar2"));
		assertEquals(2, user.getInfo().size());

		String json = user.toJson();
		System.out.println("JSON: " + json);
		assertEquals(userRecord2, json);
	}

}
