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
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PortfolioElementPage;
import org.olat.selenium.page.course.PublisherPageFragment.Access;
import org.olat.selenium.page.portfolio.PortfolioV2Page;
import org.olat.test.ArquillianDeployments;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Suite of test for the e-Portfolio version 2.0
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class PortfolioV2Test {
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	@Page
	private NavigationPage navBar;
	
	/**
	 * Create a portfolio, a course with a portoflio course element,
	 * publish it, ad a participant. The participant log in, search
	 * the course and pick the portfolio.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createTemplate(@InitialPage LoginPage loginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String binderTitle = "PF-Binder-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createPortfolioBinder(binderTitle)
			.clickToolbarBack();
		
		String sectionTitle = "Section 1 " + UUID.randomUUID();
		String assignmentTitle = "Assignment 1 " + UUID.randomUUID();
		
		PortfolioV2Page portfolio = new PortfolioV2Page(browser);
		portfolio
			.assertOnBinder()
			.selectEntries()
			.createSectionInEntries(sectionTitle)
			.createAssignmentForSection(sectionTitle, assignmentTitle, "Write a small text")
			.assertOnAssignmentInEntries(assignmentTitle);
		
		String courseTitle = "PF Course " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String portfolioNodeTitle = "Template-EP-v2";
	
		//create a course element of type portfolio and choose the one we created above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ep")
			.nodeTitle(portfolioNodeTitle)
			.selectTabLearnContent()
			.choosePortfolio(binderTitle)
			.publish()
			.quickPublish(Access.membersOnly);
	
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
	
		membersPage
			.importMembers()
			.setMembers(ryomou)
			.next().next().next().finish();
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = new NavigationPage(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the portfolio course element
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(portfolioNodeTitle);
		PortfolioElementPage portfolioCourseEl = new PortfolioElementPage(ryomouBrowser);
		portfolioCourseEl
			.pickPortfolio()
			.goToPortfolioV2();
	}

}
