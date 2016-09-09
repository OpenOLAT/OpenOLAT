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
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.portfolio.BinderPage;
import org.olat.selenium.page.portfolio.MediaCenterPage;
import org.olat.selenium.page.portfolio.PortfolioV2HomePage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.user.UserToolsPage;
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
	 * A user create a simple binder with section and page.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createSimpleBinder(@InitialPage LoginPage loginPage) 
			throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("rei");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioV2HomePage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolioV2();
		
		String binderTitle = "First binder " + UUID.randomUUID();
		BinderPage binder = portfolio
			.openMyBinders()
			.createBinder(binderTitle, "A brand new binder");
		
		String sectionTitle = "Section one " + UUID.randomUUID();
		binder
			.selectEntries()
			.createSection(sectionTitle)
			.assertOnSectionTitleInEntries(sectionTitle);
		
		String pageTitle = "Page one " + UUID.randomUUID();
		binder
			.createEntry(pageTitle)
			.assertOnPage(pageTitle);
	}
	
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
		
		BinderPage portfolio = new BinderPage(browser);
		portfolio
			.assertOnBinder()
			.selectEntries()
			.createSectionInEntries(sectionTitle)
			.createAssignmentForSection(sectionTitle, assignmentTitle, "Write a small summary", "Your task is...")
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
		BinderPage binder = portfolioCourseEl
				.pickPortfolio()
				.goToPortfolioV2();

		binder
			.selectEntries()
			.pickAssignment(assignmentTitle);
	}
	
	/**
	 * Create a course with a forum, open a new thread and pick it as
	 * a media. Go in the media center and check that the media
	 * is waiting there, click the details and check again.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void collectForumArtefactInCourse(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String courseTitle = "Collect-Forum-" + UUID.randomUUID();
		String forumTitle = ("Forum-" + UUID.randomUUID()).substring(0, 24);
		//go to authoring, create a course with a forum
		navBar
			.openAuthoringEnvironment()
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(courseTitle)
			.clickToolbarBack();
		
		//open course editor
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("fo")
			.nodeTitle(forumTitle)
			.publish()
			.quickPublish();
		courseEditor.clickToolbarBack();
		
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		course
			.clickTree()
			.selectWithTitle(forumTitle);
		
		String mediaTitle = "A post";
		
		String threadTitle = "Very interessant thread";
		ForumPage forum = ForumPage.getCourseForumPage(browser);
		forum
			.createThread(threadTitle, "With a lot of content", null)
			.addAsMedia()
			.fillForumMedia(mediaTitle, "A post I write");
		
		UserToolsPage userTools = new UserToolsPage(browser);
		MediaCenterPage mediaCenter = userTools
				.openUserToolsMenu()
				.openPortfolioV2()
				.openMediaCenter();
		mediaCenter
				.assertOnMedia(mediaTitle)
				.selectMedia(mediaTitle)
				.assertOnMediaDetails(mediaTitle);
	}

}
