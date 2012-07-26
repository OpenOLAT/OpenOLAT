/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.login;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.util.FunctionalHomeSiteUtil;
import org.olat.util.FunctionalHomeSiteUtil.HomeSiteAction;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalUtil.OlatSite;
import org.olat.util.FunctionalVOUtil;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalResumeTest {
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	FunctionalUtil functionalUtil;
	FunctionalVOUtil functionalVOUtil;
	FunctionalHomeSiteUtil functionalHomeSiteUtil;
	FunctionalRepositorySiteUtil functionalResourcesSiteUtil;
	
	@Before
	public void setup(){
		functionalUtil = new FunctionalUtil();
		functionalUtil.setDeploymentUrl(deploymentUrl.toString());
		
		functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
		functionalHomeSiteUtil = new FunctionalHomeSiteUtil(functionalUtil);
		functionalResourcesSiteUtil = new FunctionalRepositorySiteUtil(functionalUtil);
	}
	
	@Test
	@RunAsClient
	public void checkResume() throws IOException, URISyntaxException{	
		/* deploy course with rest */
		CourseVO course = functionalVOUtil.importAllElementsCourse(deploymentUrl);
		
		/* create xpath to check if course is open */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//li[@class='b_nav_tab b_nav_active b_resource_CourseModule']")
		.append("//a[@title='")
		.append(functionalVOUtil.getAllElementsCourseFilename().substring(0, functionalVOUtil.getAllElementsCourseFilename().indexOf('.')))
		.append("']");
		
		String courseXPath = selectorBuffer.toString();
		
		/* login */
		Assert.assertTrue(functionalUtil.login(browser));
		
		/* enable resume */
		functionalHomeSiteUtil.enableResume(browser);
		
		/* open course and check if it's open */
		Assert.assertTrue(functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES));
		Assert.assertTrue(functionalResourcesSiteUtil.openCourse(browser, course.getRepoEntryKey()));

		Assert.assertTrue(browser.isElementPresent(courseXPath));
		
		/* logout */
		Assert.assertTrue(functionalUtil.logout(browser));
		
		/* login */
		Assert.assertTrue(functionalUtil.login(browser, false));
		
		/* check if we are on open course tab */
		Assert.assertTrue(browser.isElementPresent(courseXPath));
		
		/* enable resume on request */
		functionalHomeSiteUtil.enableResumeOnRequest(browser);
		
		/* login without clicking away dialogs */
		Assert.assertTrue(functionalUtil.login(browser, false));
		
		/* Yes to resume last session */
		browser.click("xpath=//form//button");
		browser.waitForPageToLoad(functionalUtil.getWaitLimit());
		
		/* check if we are on open course tab */
		Assert.assertTrue(browser.isElementPresent(courseXPath));
	}
}
