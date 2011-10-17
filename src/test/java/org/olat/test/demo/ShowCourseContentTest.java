package org.olat.test.demo;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;


public class ShowCourseContentTest extends BaseSeleneseTestCase {
	
	public void setUp() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		//selenium = context.createSelenium();
	}
	
	/**
	 * Login, go to learning resources and show content of "Demo Course", logout.
	 * @throws Exception
	 */
	public void testShowCourseContent() throws Exception {
		
		selenium = Context.getContext().createSeleniumAndLogin(); //login as the default admin user
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::menu_courses()");
		selenium.waitForPageToLoad("30000");
		//selenium.click("ui=learningResources::content_showContentOfLearningResource(nameOfLearningResource=Demo Course)");
		selenium.click("ui=learningResources::content_clickLearningResource(nameOfLearningResource=Demo Course)");
		selenium.waitForPageToLoad("30000");
		assertTrue(selenium.isTextPresent("Demo"));
		selenium.click("ui=tabs::logOut()");
		selenium.waitForPageToLoad("30000");
	}
}
