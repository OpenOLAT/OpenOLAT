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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.AssessmentCEConfigurationPage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PortfolioElementPage;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.portfolio.BinderPage;
import org.olat.selenium.page.portfolio.BinderPublicationPage;
import org.olat.selenium.page.portfolio.BindersPage;
import org.olat.selenium.page.portfolio.EntriesPage;
import org.olat.selenium.page.portfolio.EntryPage;
import org.olat.selenium.page.portfolio.MediaCenterPage;
import org.olat.selenium.page.portfolio.PortfolioV2HomePage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.FeedPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.selenium.page.wiki.WikiPage;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Suite of test for the e-Portfolio version 2.0
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class PortfolioV2Test extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	
	/**
	 * A user create a simple binder with section and page.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createSimpleBinder() 
			throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("rei");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
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
	public void createTemplate(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String binderTitle = "PF-Binder-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
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
			.selectTabPortfolioContent()
			.choosePortfolio(binderTitle)
			.publish()
			.quickPublish(UserAccess.membersOnly);
	
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
	
		membersPage
			.importMembers()
			.setMembers(ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Participant log in
		LoginPage
			.load(ryomouBrowser, deploymentUrl)
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the portfolio course element
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.tree()
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
	public void collectForumMediaInCourse()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String courseTitle = "Collect-Forum-" + UUID.randomUUID();
		String forumTitle = ("Forum-" + UUID.randomUUID()).substring(0, 24);
		//go to authoring, create a course with a forum
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(courseTitle, false)
			.back();
		
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
			.tree()
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
	public void collectWikiMediaInWikiResource()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
				
		String title = "PF-Wiki-" + UUID.randomUUID();
		//create a wiki and launch it
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.wiki)
			.fillCreateForm(title)
			.assertOnInfos()
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
	public void collectBlogEntryMediaInBlogResource()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//create a course
		String courseTitle = "Course-With-Blog-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
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
			.selectTabFeedContent()
			.createFeed(blogTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(blogNodeTitle);
		
		String postTitle = "Post-EP-" + UUID.randomUUID();
		String postSummary = "Some explanations to tease people";
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
	 * The user log in, search its efficiency statement, pick it
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
	public void collectEfficiencyStatement(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-Assessment-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
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
			.selectConfigurationWithRubric()
			.setRubricScore(1.0f, 6.0f, 4.0f);
		//set the score / passed calculation in root node and publish
		courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save();
		
		//go to members management
		CoursePageFragment courseRuntime = courseEditor.clickToolbarBack();
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(ryomou, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//efficiency statement is default on
		//go to the assessment to to set the points
		members
			.clickToolbarBack()
			.assessmentTool()
			.users()
			.assertOnUsers(ryomou)
			.selectUser(ryomou)
			.selectUsersCourseNode(assessmentNodeTitle)
			.setAssessmentScore(4.5f)
			.closeAndPublishAssessment()
			.assertUserPassedCourseNode(assessmentNodeTitle);
		
		//Ryomou login
		LoginPage
			.load(ryomouBrowser, deploymentUrl)
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
	public void binderInvitation(@Drone @User WebDriver inviteeBrowser)
			throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("rei");

		LoginPage
			.load(browser, deploymentUrl)
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
			.assertOnTimeline()
			.createEntry("3. Page", 1)
			.assertOnPage("3. Page");
		
		String invitation = "c.l." + UUID.randomUUID() + "@frentix.com";
		BinderPublicationPage binderPublish = binder
			.selectPublish()
			.openAccessMenu()
			.addInvitation(invitation)
			.fillInvitation("Clara", "Vigne")
			.fillAccessRights("3. Page", Boolean.TRUE);
		String url = binderPublish.getInvitationURL();
		binderPublish
			.save();
		
		//invitee come to see the binder
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
			.assertOnTimeline()
			.assertOnPageInEntries("3. Page")
			.selectEntryInEntries("3. Page")
			.assertOnPage("3. Page");
		
		// check mail really send
		List<SmtpMessage> emails = getSmtpServer().getReceivedEmails();
		Assert.assertNotNull(emails);
		Assert.assertEquals(1, emails.size());
		SmtpMessage email = emails.get(0);
		Assert.assertNotNull(email.getHeaderValue("To"));
	}
	

	/**
	 * This is a long test. It's test the whole process to assess a binder from
	 * the template create by the author, to the assessment value saved in the
	 * assessment tool of the course.<br>
	 * The author creates a portfolio template with 2 sections and 2 assignments,
	 * it creates a course with a portfolio element and bind the template to it. It
	 * add a user as participant.<br>
	 * The participant starts the course, pick the binder and do every assignment.
	 * It edits the sharing settings to add the author as a coach.<br>
	 * The author assesses the sections and set the binder as done. Than it goes
	 * to the course, opens the assessment tool and check the participant passed
	 * the binder.
	 * 
	 * 
	 * @param loginPage
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void binderAssessment(@Drone @User WebDriver reiBrowser)
			throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("rei");

		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String binderTitle = "Binder to assess " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createPortfolioBinder(binderTitle)
			.clickToolbarBack();
		
		// create a binder template with 2 sections and
		// an assignment in each
		String section1Title = "Section 1 " + UUID.randomUUID();
		String assignment1Title = "Assignment 1 " + UUID.randomUUID();
		String section2Title = "Section 2 " + UUID.randomUUID();
		String assignment2Title = "Assignment 2 " + UUID.randomUUID();
		
		BinderPage binderTemplate = new BinderPage(browser);
		binderTemplate
			.assertOnBinder()
			.selectTableOfContent()
			.deleteSection()
			.selectEntries()
			.createSectionInEntries(section1Title)
			.createAssignmentForSection(section1Title, assignment1Title, "Write a small summary", "Your task is...")
			.assertOnAssignmentInEntries(assignment1Title)
			.createSection(section2Title)
			.createAssignmentForSection(section2Title, assignment2Title, "Second part to do", "you have to work")
			.assertOnAssignmentInEntries(assignment2Title);
		
		// create a course
		String courseTitle = "ASPF Course " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String portfolioNodeTitle = "Template-ASPF-v2";
	
		//create a course element of type portfolio and choose the one we created above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ep")
			.nodeTitle(portfolioNodeTitle)
			.selectTabPortfolioContent()
			.choosePortfolio(binderTitle);
		//configure the assessment
		AssessmentCEConfigurationPage assessmentConfig = new AssessmentCEConfigurationPage(browser);
		assessmentConfig
			.selectConfiguration()
			.setScoreAuto(0.0f, 10.0f, 5.0f)
			.saveAssessmentOptions();
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
	
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
	
		membersPage
			.importMembers()
			.setMembers(rei)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Participant log in
		LoginPage
			.load(reiBrowser, deploymentUrl)
			.loginAs(rei)
			.resume();
		
		//open the course
		NavigationPage reiNavBar = NavigationPage.load(reiBrowser);
		reiNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the portfolio course element
		CoursePageFragment reiTestCourse = new CoursePageFragment(reiBrowser);
		reiTestCourse
			.tree()
			.selectWithTitle(portfolioNodeTitle);
		PortfolioElementPage portfolioCourseEl = new PortfolioElementPage(reiBrowser);
		BinderPage reiBinder = portfolioCourseEl
				.pickPortfolio()
				.goToPortfolioV2();
		OOGraphene.waitAndCloseBlueMessageWindow(reiBrowser);

		reiBinder
			.selectEntries()
			.pickAssignment(assignment1Title)
			.publishEntry();
		reiBinder
			.selectEntries()
			.pickAssignment(assignment2Title)
			.publishEntry();
		//add the author as coach
		reiBinder
			.selectPublish()
			.openAccessMenu()
			.addMember()
			.searchMember(author, false)
			.nextUsers()
			.nextOverview()
			.fillAccessRights(binderTitle, Boolean.TRUE)
			.nextPermissions()
			.deSelectEmail()
			.finish();
		
		//the author come to see the binder
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioV2HomePage portfolio = userTools
			.openUserToolsMenu()
			.openPortfolioV2();
		portfolio
			.openSharedWithMe()
			.openSharedBindersWithMe()
			.assertOnBinder(binderTitle)
			.selectBinder(binderTitle)
			.selectAssessment()
			.passed(section1Title)
			.save()
			.close(section1Title)
			.passed(section2Title)
			.save()
			.close(section2Title)
			.done()
			.assertPassed(2);
		
		//than go to the course and check the results in the assessment tool
		//author take the lead and check the assessment tool
		navBar
			.openMyCourses()
			.select(courseTitle);
		//open the assessment tool
		AssessmentToolPage assessmentTool = new CoursePageFragment(browser)
			.assessmentTool();
		//check that rei has passed the test
		assessmentTool
			.users()
			.assertOnUsers(rei)
			.selectUser(rei)
			.assertPassed(rei);
	}
	
	/**
	 * A user create a page / entry, it edit it
	 * and add a title, an image, a document
	 * and a citation. It toggles between the editor
	 * mode and the view mode to check if the parts it
	 * add in the page are really there.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void editPage() 
			throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("rei");
		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioV2HomePage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolioV2();
		
		String pageTitle = "My page " + UUID.randomUUID();
		EntryPage entry = portfolio
				.openMyEntries()
				.newPage(pageTitle)
				.assertOnPage(pageTitle);
		// add a title
		String title = "My long title " + UUID.randomUUID();
		entry
			.openElementsChooser()
			.addTitle(title)
			.setTitleSize(4)
			.closeEditFragment()
			.assertOnTitle(title, 4);
		
		// add an image
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File imageFile = new File(imageUrl.toURI());
		entry
			.openElementsChooser()
			.addImage("Blue is the new black", imageFile)
			.assertOnImage(imageFile);
		// close the editor and check
		entry
			.toggleEditor()
			.assertOnTitle(title, 4)
			.assertOnImage(imageFile);
		
		//reopen the editor and add a document
		URL pdfUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfFile = new File(pdfUrl.toURI());
		entry
			.toggleEditor()
			.openElementsChooser()
			.addDocument("Anything about", pdfFile)
			.assertOnDocument(pdfFile);
		//and a citation
		String citation = "Close the world, open the next.";
		entry
			.openElementsChooser()
			.addCitation("Serial experiment", citation)
			.assertOnCitation(citation);
		//close the editor and check all parts
		entry.toggleEditor()
			.assertOnTitle(title, 4)
			.assertOnImage(imageFile)
			.assertOnDocument(pdfFile)
			.assertOnCitation(citation);
	}

	/**
	 * A user create a binder with a section and two pages. It deletes
	 * one, go to the trash, find the delete page, restore it and go
	 * again in the binder. It move a second time the page to the trash
	 * and delete it definitively.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void deletePage()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("rei");

		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioV2HomePage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolioV2();
		
		String binderTitle = "Binder del " + UUID.randomUUID();
		BinderPage binder = portfolio
			.openMyBinders()
			.createBinder(binderTitle, "A binder where I want to delete some pages");
		
		String sectionTitle = "Section one " + UUID.randomUUID();
		binder
			.selectEntries()
			.createSection(sectionTitle)
			.assertOnSectionTitleInEntries(sectionTitle);
		
		String pageTitle = "Page two " + UUID.randomUUID();
		String pageToDelete = "Page del " + UUID.randomUUID();
		binder
			.createEntry(pageToDelete)
			.assertOnPage(pageToDelete)
			.selectEntries()
			.createEntry(pageTitle)
			.assertOnPage(pageTitle)
			.selectTableOfContent()
			.selectEntryInToc(pageToDelete)
			.moveEntryToTrash()
			.assertOnPageInToc(pageTitle)
			.assertOnPageNotInToc(pageToDelete);
		
		EntriesPage trash = portfolio
			.clickToolbarBack()
			.clickToolbarBack()
			.clickToolbarBack()
			.openDeletedEntries();
		
		trash
			.assertOnPage(pageToDelete)
			.switchTableView()
			.restore(pageToDelete, binderTitle, sectionTitle);
		
		portfolio
			.clickToolbarBack()
			.openMyBinders()
			.selectBinder(binderTitle)
			.assertOnPageInToc(pageToDelete)
			.selectEntryInToc(pageToDelete)
			.moveEntryToTrash();
		
		trash = portfolio
			.clickToolbarBack()
			.clickToolbarBack()
			.clickToolbarBack()
			.openDeletedEntries();
		
		trash
			.switchTableView()
			.assertOnPageTableFlatView(pageToDelete)
			.switchTableView()
			.selectPageInTableFlatView(pageToDelete)
			.deleteEntry()
			.assertEmptyTableView();
	}
	
	/**
	 * A user create a binder with section and pages, move it to
	 * the trash. Then it goes to the trash restore it. Return to
	 * the list of binders, move the binder again to the trash and
	 * goes there to delete it definitively.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void deleteBinder()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("rei");

		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioV2HomePage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolioV2();
		
		String binderTitle = "Binder ephemere " + UUID.randomUUID();
		BindersPage myBinders = portfolio
			.openMyBinders();
		BinderPage binder = myBinders
			.createBinder(binderTitle, "A binder that I want to delete");
		
		String sectionTitle = "Section one " + UUID.randomUUID();
		binder
			.selectEntries()
			.createSection(sectionTitle)
			.assertOnSectionTitleInEntries(sectionTitle);
		
		for(int i=1; i<3; i++) {
			String pageTitle = "Page " + i;
			binder
				.createEntry(pageTitle)
				.assertOnPage(pageTitle)
				.selectEntries();
		}
		binder
			.selectTableOfContent()
			.selectEntryInToc("Page 1");
		
		//reload the binder
		portfolio
			.clickToolbarBack()
			.clickToolbarBack();
		myBinders
			.selectBinder(binderTitle);
		
		// move the binder to the trash
		binder
			.assertOnPageInToc("Page 1")
			.moveBinderToTrash();
		
		// go in the trash to restore it
		portfolio
			.clickToolbarBack()
			.openDeletedBinders()
			.switchDeletedBindersTableView()
			.restoreBinder(binderTitle);
		
		// move it to the trash again
		portfolio
			.clickToolbarBack()
			.openMyBinders()
			.selectBinder(binderTitle)
			.moveBinderToTrash();
		
		// go to the trash to delete it definitively
		portfolio
			.clickToolbarBack()
			.openDeletedBinders()
			.selectBinderInTableView(binderTitle)
			.assertOnPageInToc("Page 2")
			.deleteBinder()
			.assertEmptyTableView();
	}
}
