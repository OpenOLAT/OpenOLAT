package org.olat.test.tutorial.singlenode.testng;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

@Test(groups = {"sequential"})
public class CreateUserTest extends BaseSeleneseTestCase {

	@Test
	public void testCoursePublish() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		selenium = context.createSelenium();

		OlatLoginInfos loginInfos = context.getStandardAdminOlatLoginInfos();
		OlatLoginInfos newLoginInfos =
			WorkflowHelper.createUserIfNotExists(loginInfos, "newuser3", "newpassword2", true, true, false, true, false);
		
		OlatLoginHelper.olatLogin(selenium, newLoginInfos);
		assertEquals("OLAT - Home", selenium.getTitle());
		OlatLoginHelper.olatLogout(selenium);
	}
	
}
