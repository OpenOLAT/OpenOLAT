/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
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
