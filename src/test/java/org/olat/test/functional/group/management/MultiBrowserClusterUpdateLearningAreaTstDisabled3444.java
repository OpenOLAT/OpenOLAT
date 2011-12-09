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
package org.olat.test.functional.group.management;

import org.junit.Ignore;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

@Ignore
public class MultiBrowserClusterUpdateLearningAreaTstDisabled3444 extends BaseSeleneseTestCase {
	
    protected com.thoughtworks.selenium.Selenium selenium1;
    protected com.thoughtworks.selenium.Selenium selenium2;

    public void testMultiBrowserClusterNewLearningArea() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos user1 = context.createuserIfNotExists(1, "mbcnla1", standardPassword, true, true, true, true, true);
		OlatLoginInfos user2 = context.createuserIfNotExists(2, "mbcnla2", standardPassword, true, true, true, true, true);

		{
			System.out.println("logging in browser 1...");
			selenium1 = context.createSeleniumAndLogin(user1);
			selenium1.click("ui=tabs::learningResources()");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=learningResources::menu_searchForm()");
			selenium1.waitForPageToLoad("30000");
			selenium1.type("ui=learningResources::content_searchForm_titleField()", "Demo course wiki");
			selenium1.click("ui=learningResources::content_searchForm_search()");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=learningResources::content_clickCourseEntry(nameOfCourse=Demo course wiki)");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=learningResources::content_showContent()");
			selenium1.waitForPageToLoad("30000");
			
			selenium1.click("ui=course::toolbox_courseTools_groupManagement()");
			selenium1.waitForPageToLoad("30000");
			
			// make sure the learning area does not exist yet - delete otherwise
			selenium1.click("ui=groupManagement::menu_allLearningAreas()");
			selenium1.waitForPageToLoad("30000");
			if (selenium1.isElementPresent("ui=groupManagement::content_learningAreaTable_deleteLearningArea(nameOfLearningArea=multibrowserclusterlearningarea)")) {
				selenium1.click("ui=groupManagement::content_learningAreaTable_deleteLearningArea(nameOfLearningArea=multibrowserclusterlearningarea)");
				selenium1.waitForPageToLoad("30000");
				selenium1.click("ui=dialog::Yes()");
				selenium1.waitForPageToLoad("30000");
			}
			
			// create the learning area
			selenium1.click("ui=groupManagement::toolbox_groupManagement_newLearningArea()");
			selenium1.waitForPageToLoad("30000");
			assertTrue(selenium1.isTextPresent("Create a new learning area"));
			selenium1.type("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_name()", "multibrowserclusterlearningarea");
			selenium1.type("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_description()", "egal oder?");
			selenium1.click("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_save()");
			selenium1.waitForPageToLoad("30000");
			
			// open the learning area in edit mode
			selenium1.click("ui=groupManagement::menu_allLearningAreas()");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=groupManagement::content_learningAreaTable_editLearningArea(nameOfLearningArea=multibrowserclusterlearningarea)");
			selenium1.waitForPageToLoad("30000");
			
			// edit the title
			selenium1.type("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_name()", "mod1");
		}
		
		{
			System.out.println("logging in browser 2...");
			selenium2 = context.createSeleniumAndLogin(user2);
			selenium2.click("ui=tabs::learningResources()");
			selenium2.waitForPageToLoad("30000");
			selenium2.click("ui=learningResources::menu_searchForm()");
			selenium2.waitForPageToLoad("30000");
			selenium2.type("ui=learningResources::content_searchForm_titleField()", "Demo course wiki");
			selenium2.click("ui=learningResources::content_searchForm_search()");
			selenium2.waitForPageToLoad("30000");
			selenium2.click("ui=learningResources::content_clickCourseEntry(nameOfCourse=Demo course wiki)");
			selenium2.waitForPageToLoad("30000");
			selenium2.click("ui=learningResources::content_showContent()");
			selenium2.waitForPageToLoad("30000");
			
			selenium2.click("ui=course::toolbox_courseTools_groupManagement()");
			selenium2.waitForPageToLoad("30000");

			// open the learning area in edit mode
			selenium2.click("ui=groupManagement::menu_allLearningAreas()");
			selenium2.waitForPageToLoad("30000");
			selenium2.click("ui=groupManagement::content_learningAreaTable_editLearningArea(nameOfLearningArea=multibrowserclusterlearningarea)");
			selenium2.waitForPageToLoad("30000");

			// edit the title
			selenium2.type("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_name()", "mod2");
		}
		
		selenium1.click("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_save()");	
		selenium2.click("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_save()");
		
		selenium1.waitForPageToLoad("30000");
		selenium2.waitForPageToLoad("30000");
		
		assertTrue("Not found in selenium 1: Edit learning area multibrowserclusterlearningarea", selenium1.isTextPresent("Edit learning area multibrowserclusterlearningarea"));
		assertTrue("Not found in selenium 2: Edit learning area multibrowserclusterlearningarea", selenium2.isTextPresent("Edit learning area multibrowserclusterlearningarea"));
	}
}
