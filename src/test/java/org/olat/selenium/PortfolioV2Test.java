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
import org.olat.selenium.page.course.AssessmentCEConfigurationPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PortfolioElementPage;
import org.olat.selenium.page.course.PublisherPageFragment.Access;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.portfolio.BinderPage;
import org.olat.selenium.page.portfolio.BinderPublicationPage;
import org.olat.selenium.page.portfolio.MediaCenterPage;
import org.olat.selenium.page.portfolio.PortfolioV2HomePage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.FeedPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.RepositoryAccessPage.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.selenium.page.wiki.WikiPage;
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
	public void collectForumMediaInCourse(@InitialPage LoginPage loginPage)
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
	
	/**
	 * Create a wiki as resource, add and fill a page. The author
	 * picks the page as media and go in its media center to see it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void collectWikiMediaInWikiResource(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
				
		String title = "PF-Wiki-" + UUID.randomUUID();
		//create a wiki and launch it
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.wiki)
			.fillCreateForm(title)
			.assertOnGeneralTab()
			.clickToolbarBack();
		
		//create a page in the wiki
		String page = "LMS-" + UUID.randomUUID();
		String content = "Learning Management System";
		WikiPage wiki = WikiPage.getWiki(browser);

		//create page and add it as artefact to portfolio
		String mediaTitle = "My own wiki page";
		wiki
			.createPage(page, content)
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
	
	/**
	 * Create a blog as learn resource, create a new entry and publish it.
	 * Than pick the entry as a media and go to the media center to see it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void collectBlogEntryMediaInBlogResource(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//create a course
		String courseTitle = "Course-With-Blog-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String blogNodeTitle = "Blog-EP-1";
		String blogTitle = "Blog - EP - " + UUID.randomUUID().toString();
		
		//create a course element of type blog with a blog
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("blog")
			.nodeTitle(blogNodeTitle)
			.selectTabLearnContent()
			.createFeed(blogTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.clickTree()
			.selectWithTitle(blogNodeTitle);
		
		String postTitle = "Post-EP-" + UUID.randomUUID();
		String postSummary = "Some explantations as teaser";
		String postContent = "Content of the post";

		FeedPage feed = FeedPage.getFeedPage(browser);
		feed
			.newBlog()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost();
		
		String mediaTitle = "My very own entry";
		feed
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


	/**
	 * Create a course with an assessment course element, setup
	 * efficiency statement, add a user and assess her.
	 * The user log in, search its efficency statemet, pick it
	 * as a media for is portfolio and goes in the media center
	 * to search it and select it.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void collectEfficiencyStatement(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-Assessment-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String assessmentNodeTitle = "Efficiency PF";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("ms")
			.nodeTitle(assessmentNodeTitle);
		
		//configure assessment
		AssessmentCEConfigurationPage assessmentConfig = new AssessmentCEConfigurationPage(browser);
		assessmentConfig
			.selectConfiguration()
			.setScoreAuto(1.0f, 6.0f, 4.0f);
		//set the score / passed calculation in root node and publish
		courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.accessConfiguration()
			.setUserAccess(UserAccess.registred);
		
		//go to members management
		CoursePageFragment courseRuntime = courseEditor.clickToolbarBack();
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(ryomou, true)
			.next().next().next().finish();
		
		//efficiency statement is default on
		//go to the assessment to to set the points
		members
			.clickToolbarBack()
			.assessmentTool()
			.users()
			.assertOnUsers(ryomou)
			.selectUser(ryomou)
			.selectCourseNode(assessmentNodeTitle)
			.setAssessmentScore(4.5f)
			.assertUserPassedCourseNode(assessmentNodeTitle);
		
		//Ryomou login
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		//see its beautiful efficiency statement
		String mediaTitle = "My efficiency";
		String mediaDesc = "My efficiency statement " + UUID.randomUUID();
		
		UserToolsPage ryomouUserTools = new UserToolsPage(ryomouBrowser);
		ryomouUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnStatement(courseTitle, true)
			.addAsMediaInList(courseTitle)
			.fillEfficiencyStatementMedia(mediaTitle, mediaDesc);
		
		MediaCenterPage mediaCenter = ryomouUserTools
				.openUserToolsMenu()
				.openPortfolioV2()
				.openMediaCenter();
		mediaCenter
				.assertOnMedia(mediaTitle)
				.selectMedia(mediaTitle)
				.assertOnMediaDetails(mediaTitle);
	}
	
	/**
	 * A user create a binder with some sections and pages.
	 * It invites a second person on the last page it creates.
	 * This page is not published for the moment. The invitee
	 * follow the invitation URL and see an empty binder.<br>
	 *  The author publish the last entry. The invitee come back
	 *  to the list of entries, find the page and open it.
	 * 
	 * @param loginPage
	 * @param inviteeBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void binderInvitation(@InitialPage LoginPage loginPage,
			@Drone @User WebDriver inviteeBrowser)
			throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("rei");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioV2HomePage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolioV2();
		
		String binderTitle = "Binder on invitation " + UUID.randomUUID();
		BinderPage binder = portfolio
			.openMyBinders()
			.createBinder(binderTitle, "A brand new binder");
		
		//create 2 sections and 3 entries
		binder
			.selectEntries()
			.createSection("1. Section")
			.assertOnSectionTitleInEntries("1. Section")
			.createEntry("1. Page")
			.assertOnPage("1. Page")
			.selectEntries()
			.createSection("2. Section")
			.createEntry("2. Page")
			.assertOnPage("2. Page")
			.selectEntries()
			.createEntry("3. Page", 1)
			.assertOnPage("3. Page");
		
		BinderPublicationPage binderPublish = binder
			.selectPublish()
			.openAccessMenu()
			.addInvitation("c.l@frentix.com")
			.fillInvitation("Clara", "Vigne")
			.fillAccessRights("3. Page", Boolean.TRUE);
		String url = binderPublish.getInvitationURL();
		binderPublish
			.save();
		
		//invitee come to see the bidner
		inviteeBrowser.get(url);
		BinderPage invitee = new BinderPage(inviteeBrowser);
		invitee.assertOnBinder()
			.selectEntries()
			.assertNoPagesInEntries();
		
		//author publish an entry
		binder
			.selectTableOfContent()
			.selectEntryInToc("3. Page")
			.publishEntry();
		
		//return in entries to check the changes
		invitee
			.selectTableOfContent()
			.selectEntries()
			.assertOnPageInEntries("3. Page")
			.selectEntryInEntries("3. Page")
			.assertOnPage("3. Page");
	}
}
