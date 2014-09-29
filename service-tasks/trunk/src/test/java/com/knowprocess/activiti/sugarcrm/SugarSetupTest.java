package com.knowprocess.activiti.sugarcrm;

import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.test.activiti.ExtendedRule;

public class SugarSetupTest {

	@Rule
	public ExtendedRule activitiRule = new ExtendedRule(
			"test-activiti.cfg.xml");

	@Test
	public void testDerekSugarCredentials() {
		activitiRule.getIdentityService().deleteUserInfo("derek@trakeo.com",
				SugarTask.SUGAR_USERNAME);
		activitiRule.getIdentityService().setUserInfo("derek@trakeo.com",
				SugarTask.SUGAR_USERNAME, "admin");
	}

}
