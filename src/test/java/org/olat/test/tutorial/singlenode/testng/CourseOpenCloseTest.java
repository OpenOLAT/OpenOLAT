package org.olat.test.tutorial.singlenode.testng;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.Test;

@Test(groups = {"sequential"}, enabled=true  )
public class CourseOpenCloseTest extends BaseSeleneseTestCase {

	@Test
	public void testCourseOpenClose() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		selenium = context.createSeleniumAndLogin();
		
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::menu_searchForm()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=learningResources::content_searchForm_titleField()", "Demo course wiki");
		selenium.click("ui=learningResources::content_searchForm_search()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::content_clickCourseEntry(nameOfCourse=Demo course wiki)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::content_showContent()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=tabs::closeCourse(nameOfCourse=Demo course wiki)");
	}
}
