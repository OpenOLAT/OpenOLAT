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
package org.olat.selenium;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jcodec.common.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.user.UserPreferencesPageFragment.ResumeOption;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.rest.RepositoryRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class ResumeTest {
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	@Page
	private UserToolsPage userTools;
	
	/**
	 * Set the resume preferences to automatically resume the session,
	 * open a course, log out, log in and check if the course is resumed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void resumeCourseAutomatically(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createTestUser();
		//deploy a course
		CourseVO course = new RepositoryRestClient(deploymentUrl).deployDemoCourse();

		//login
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		
		//set the preferences to resume automatically
		userTools
			.assertOnUserTools()
			.openUserToolsMenu()
			.openMySettings()
			.assertOnUserSettings()
			.openPreferences()
			.assertOnUserPreferences()
			.setResume(ResumeOption.auto);
		
		//open a course via REST url
		CoursePageFragment coursePage = CoursePageFragment.getCourse(browser, deploymentUrl, course);
		coursePage
			.assertOnCoursePage()
			.clickTree();
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());

		//check the title of the course if any
		WebElement courseTitle = browser.findElement(By.tagName("h2"));
		Assert.assertNotNull(courseTitle);
		Assert.assertTrue(courseTitle.isDisplayed());
		Assert.assertTrue(courseTitle.getText().contains(course.getTitle()));
	}
	
	/**
	 * Set the resume preferences to resume the session on request,
	 * open a course, log out, log in, resume the session and check
	 * if the course is resumed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void resumeCourseOnDemand(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createTestUser();
		//deploy a course
		CourseVO course = new RepositoryRestClient(deploymentUrl).deployDemoCourse();

		//login
		loginPage.loginAs(user.getLogin(), user.getPassword());
		
		//set the preferences to resume automatically
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setResume(ResumeOption.ondemand);
		
		//open a course via REST url and click it
		CoursePageFragment.getCourse(browser, deploymentUrl, course).clickTree();
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		//resume
		loginPage.resumeWithAssert();

		//check the title of the course if any
		WebElement courseTitle = browser.findElement(By.tagName("h2"));
		Assert.assertNotNull(courseTitle);
		Assert.assertTrue(courseTitle.isDisplayed());
		Assert.assertTrue(courseTitle.getText().contains(course.getTitle()));
	}
}
