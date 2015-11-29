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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.Administrator;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.course.AssessmentCEConfigurationPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseWizardPage;
import org.olat.selenium.page.course.InfoMessageCEPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PublisherPageFragment;
import org.olat.selenium.page.course.RemindersPage;
import org.olat.selenium.page.course.PublisherPageFragment.Access;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.FeedPage;
import org.olat.selenium.page.repository.RepositoryAccessPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.RepositoryAccessPage.UserAccess;
import org.olat.selenium.page.repository.RepositoryEditDescriptionPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class CourseTest {
	
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
	 * An author create a course, jump to it, open the editor
	 * add an info messages course element, publish the course
	 * and view it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourse(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Create-Selen-" + UUID.randomUUID().toString();
		//create course
		RepositoryEditDescriptionPage editDescription = authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab();
		
		//from description editor, back to the course
		editDescription
			.clickToolbarBack();
		
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.openToolsMenu()
			.edit();
		
		//create a course element of type info messages
		PublisherPageFragment publisher = editor
			.assertOnEditor()
			.createNode("info")
			.publish();
		
		//publish
		publisher
			.assertOnPublisher()
			.next()
			.selectAccess(Access.guests)
			.next()
			.selectCatalog(false)
			.next() // -> no problem found
			.finish();
		
		//back to the course
		CoursePageFragment publishedCourse = editor
			.clickToolbarBack();
		
		//review the course
		publishedCourse
			.assertOnCoursePage()
			.clickTree();
	}
	
	/**
	 * Create a course, use the course wizard, select all course
	 * elements and go further with the standard settings.
	 * 
	 * Go from the description editor to the course, check
	 * that the course is automatically published and that
	 * the five course elements are there.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourse_withWizard(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Create-Course-Wizard-" + UUID.randomUUID().toString();
		//create course
		CourseWizardPage courseWizard = authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateFormAndStartWizard(title);
		
		courseWizard
			.selectAllCourseElements()
			.next()
			.next()
			.finish();
		
		RepositoryEditDescriptionPage editDescription = RepositoryEditDescriptionPage.getPage(browser);
		//from description editor, back to details and launch the course
		editDescription
			.clickToolbarBack();
		
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		course
			.assertOnCoursePage()
			.assertOnTitle(title);
		
		//assert the 5 nodes are there and click them
		By nodeBy = By.cssSelector("span.o_tree_link.o_tree_l1.o_tree_level_label_leaf>a");
		List<WebElement> nodes = browser.findElements(nodeBy);
		Assert.assertEquals(5, nodes.size());
		for(WebElement node:nodes) {
			node.click();
			OOGraphene.waitBusy(browser);
		}
	}
	
	/**
	 * Create a course, create a CP, go the the course editor,
	 * create a course element of type CP, select the CP which just created,
	 * close the course editor and check the presence of the CP with the
	 * default title of the first page.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithCP(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-CP-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a CP
		String cpTitle = "CP for a course - " + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCP(cpTitle)
			.assertOnGeneralTab();
		
		navBar.openCourse(courseTitle);
		
		String cpNodeTitle = "CP-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cp")
			.nodeTitle(cpNodeTitle)
			.selectTabLearnContent()
			.chooseCP(cpTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(cpNodeTitle);
		
		//check that the default title of CP (Lorem Ipsum) is visible in the iframe
		WebElement cpIframe = browser.findElement(By.cssSelector("div.o_iframedisplay>iframe"));
		browser.switchTo().frame(cpIframe);
		browser.findElement(By.xpath("//h2[text()='Lorem Ipsum']"));
	}
	
	
	/**
	 * Create a course, create a wiki, go the the course editor,
	 * create a course element of type wiki, select the wiki which just created,
	 * close the course editor and select the index page of the wiki.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithWiki(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Wiki-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a CP
		String wikiTitle = "Wiki for a course - " + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createWiki(wikiTitle)
			.assertOnGeneralTab();
		
		navBar.openCourse(courseTitle);
		
		String wikiNodeTitle = "Wiki-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("wiki")
			.nodeTitle(wikiNodeTitle)
			.selectTabLearnContent()
			.chooseWiki(wikiTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(wikiNodeTitle)
			.selectWithTitle("Index");
		
		//check that the title of the index article/page is visible
		WebElement indexArticleTitle = browser.findElement(By.className("o_wikimod_heading"));
		Assert.assertEquals("Index", indexArticleTitle.getText().trim());
	}
	
	/**
	 * Create a course, create a course element of type wiki. Open
	 * the resource chooser, create a wiki, close the editor, show the 
	 * index page of the wiki.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithWiki_createInCourseEditor(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Wiki-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String wikiNodeTitle = "Wiki-1";
		String wikiTitle = "Wiki for a course - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("wiki")
			.nodeTitle(wikiNodeTitle)
			.selectTabLearnContent()
			.createWiki(wikiTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(wikiNodeTitle)
			.selectWithTitle("Index");
		
		//check that the title of the index article/page is visible
		WebElement indexArticleTitle = browser.findElement(By.className("o_wikimod_heading"));
		Assert.assertEquals("Index", indexArticleTitle.getText().trim());
	}
	
	@Test
	@RunAsClient
	public void createCourseWithQTITest(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-1.2-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTITest-1";
		String testTitle = "Test - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle)
			.selectTabLearnContent()
			.createQTI12Test(testTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_titled_wrapper.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
	}
	
	/**
	 * Create a course with a course element of type podcast. Create
	 * a podcast, publish the course, go the the course and configure
	 * the podcast to read an external feed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithPodcast_externalFeed(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Podcast-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String podcastNodeTitle = "Podcats-1";
		String podcastTitle = "Podcast - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("podcast")
			.nodeTitle(podcastNodeTitle)
			.selectTabLearnContent()
			.createFeed(podcastTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.clickTree()
			.selectWithTitle(podcastNodeTitle);
		
		//check that the title of the podcast is correct
		WebElement podcastH2 = browser.findElement(By.cssSelector("div.o_podcast_info>h2"));
		Assert.assertEquals(podcastTitle, podcastH2.getText().trim());
		
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed.newExternalPodcast("http://pod.drs.ch/rock_special_mpx.xml");

		//check only that the "episodes" title is visibel
		WebElement episodeH4 = browser.findElement(By.cssSelector("div.o_podcast_episodes>h4.o_title"));
		Assert.assertNotNull(episodeH4);
	}
	
	@Test
	@RunAsClient
	public void createCourseWithBlog_externalFeed(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Blog-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String blogNodeTitle = "Blog-1";
		String blogTitle = "Blog - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
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
		
		//check that the title of the podcast is correct
		WebElement podcastH2 = browser.findElement(By.cssSelector("div.o_blog_info>h2"));
		Assert.assertEquals(blogTitle, podcastH2.getText().trim());
		
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed.newExternalBlog("http://www.openolat.com/feed/");

		//check only that the subscription link is visible
		WebElement subscriptionLink = browser.findElement(By.cssSelector("div.o_subscription>a"));
		Assert.assertTrue(subscriptionLink.isDisplayed());
	}

	/**
	 * An author create a course with a blog, open it, add a post.
	 * A student open the course, see the blog post. An administrator
	 * clears the feed cache. The author add a new post, the student
	 * must see it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void blogWithClearCache(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver participantDrone,
			@Drone @Administrator WebDriver administratorDrone)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course with a blog
		String courseTitle = "Course-Blog-1-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
				
		String blogNodeTitle = "Blog-RW-1";
		String blogTitle = "Blog - RW - " + UUID.randomUUID().toString();
				
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
		
		//open the course and see the blog
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.clickTree()
			.selectWithTitle(blogNodeTitle);
		
		String postTitle = "Blog-RW-1-" + UUID.randomUUID();
		String postSummary = "Some explantations as teaser";
		String postContent = "Content of the post";
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed
			.newBlog()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost();

		//participant go to the blog
		participantDrone.navigate().to(deploymentUrl);
		LoginPage participantLogin = LoginPage.getLoginPage(participantDrone, deploymentUrl);
		participantLogin.loginAs(participant.getLogin(), participant.getPassword());
		//search the course in "My courses"
		NavigationPage participantNavigation = new NavigationPage(participantDrone);
		participantNavigation
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		//Navigate the course to the blog
		CoursePageFragment participantCourse = new CoursePageFragment(participantDrone);
		participantCourse
			.clickTree()
			.selectWithTitle(blogNodeTitle);
		FeedPage participantFeed = FeedPage.getFeedPage(participantDrone);
		participantFeed.assertOnBlogPost(postTitle);
		
		//administrator clears the cache
		administratorDrone.navigate().to(deploymentUrl);
		LoginPage.getLoginPage(administratorDrone, deploymentUrl)
			.loginAs("administrator", "openolat")
			.resume();
		new NavigationPage(administratorDrone)
			.openAdministration()
			.clearCache("FeedManager@feed");
		
		//the author publish a second post in its blog
		String post2Title = "Blog-RW-2-" + UUID.randomUUID();
		String post2Summary = "Some explantations as teaser";
		String post2Content = "Content of the post";
		feed.addBlogPost()
			.fillPostForm(post2Title, post2Summary, post2Content)
			.publishPost();
		
		//the participant must see the new post after some click
		participantFeed
			.clickFirstMonthOfPager()
			.assertOnBlogPost(post2Title);
	}
	
	/**
	 * 
	 * Create a catalog, create a course, while publishing add the
	 * course to the catalog. Go to the catalog, find the course and
	 * open it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void catalogRoundTrip(@Drone @Administrator WebDriver adminBrowser,
			@Drone @User WebDriver userBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		
		//administrator create the categories in the catalog
		LoginPage adminLogin = LoginPage.getLoginPage(adminBrowser, deploymentUrl);
		adminLogin
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage adminNavBar = new NavigationPage(adminBrowser);
		
		String node1 = "First level " + UUID.randomUUID();
		String node2_1 = "Second level first element " + UUID.randomUUID();
		String node2_2 = "Second level second element " + UUID.randomUUID();
		adminNavBar
				.openCatalogAdministration()
				.addCatalogNode(node1, "First level of the catalog")
				.selectNode(node1)
				.addCatalogNode(node2_1, "First element of the second level")
				.addCatalogNode(node2_2, "Second element of the second level");
		
		//An author create a course and publish it under a category
		//created above
		LoginPage login = LoginPage.getLoginPage(browser, deploymentUrl);
		login
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		String courseTitle = "Catalog-Course-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
	
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.publish()
			.next()
			.selectAccess(Access.guests)
			.next()
			.selectCatalog(true)
			.selectCategory(node1, node2_2)
			.next() // -> no problem found
			.finish();
		
		//User logs in, go to "My courses", navigate the catalog and start
		//the course
		LoginPage userLogin = LoginPage.getLoginPage(userBrowser, deploymentUrl);
		userLogin
			.loginAs(user.getLogin(), user.getPassword())
			.resume();

		NavigationPage userNavBar = new NavigationPage(userBrowser);
		userNavBar
			.openMyCourses()
			.openCatalog()
			.selectCatalogEntry(node1)
			.selectCatalogEntry(node2_2)
			.select(courseTitle)//go to the details page
			.start();
		
		By courseTitleBy = By.cssSelector(".o_course_run h2");
		WebElement courseTitleEl = userBrowser.findElement(courseTitleBy);
		Assert.assertTrue(courseTitleEl.getText().contains(courseTitle));
	}
	
	/**
     * Login, create a course, select "Messages Course", insert an info message
     * course element, publish the course, add messages, count if the messages
     * are there, show older messages, count the messages, show current messages,
     * count the messages, edit a message and delete an other, count the messages.
     * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithInfoMessages(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course Msg " + UUID.randomUUID().toString();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab()
			.clickToolbarBack();
		
		String infoNodeTitle = "Infos - News";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.openToolsMenu()
			.edit()
			.createNode("info")
			.nodeTitle(infoNodeTitle);
		
		//configure the info messages
		InfoMessageCEPage infoMsgConfig = new InfoMessageCEPage(browser);
		infoMsgConfig
			.selectConfiguration()
			.configure(3);
		
		//publish
		editor
			.publish()
			.quickPublish(Access.guests);
		editor.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(infoNodeTitle);
		//set a message
		infoMsgConfig
			.createMessage()
			.setMessage("Information 0", "A very important info")
			.next()
			.finish()
			.assertOnMessageTitle("Information 0");
		
		for(int i=1; i<=3; i++) {
			infoMsgConfig.quickMessage("Information " + i, "More informations");
		}
		
		int numOfMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfMessages);
		
		//old messages
		infoMsgConfig.oldMessages();
		int numOfOldMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(4, numOfOldMessages);
		
		//new messages
		infoMsgConfig.newMessages();
		int numOfNewMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfNewMessages);
		
		//edit
		infoMsgConfig.oldMessages();
		infoMsgConfig
			.editMessage("Information 2")
			.setMessage("The latest information", "A very important info")
			.save()
			.assertOnMessageTitle("The latest information");

		//delete
		infoMsgConfig
			.deleteMessage("Information 3")
			.confirmDelete();
		
		int numOfSurvivingMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfSurvivingMessages);
	}
	
	/**
	 * An author creates a course, make it visible for
	 * members and add an access control by password.
	 * The user search for the course, books it and give
	 * the password.<br/>
	 * The author checks in the list of orders if the booking
	 * of the user is there and after it checks if the user is
	 * in the member list too.
	 * 
	 * @param loginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseBooking(@InitialPage LoginPage loginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Create-Selen-" + UUID.randomUUID().toString();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab();

		//open course editor
		CoursePageFragment course = new CoursePageFragment(browser);
		RepositoryAccessPage courseAccess = course
			.openToolsMenu()
			.edit()
			.createNode("info")
			.autoPublish()
			.accessConfiguration()
			.setUserAccess(UserAccess.registred);
		//add booking by secret token
		courseAccess
			.boooking()
			.openAddDropMenu()
			.addTokenMethod()
			.configureTokenMethod("secret", "The password is secret");
		courseAccess
			.clickToolbarBack();
		
		//a user search the course
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage ryomouNavBar = new NavigationPage(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(title)
			.book(title);
		//book the course
		BookingPage booking = new BookingPage(ryomouBrowser);
		booking
			.bookToken("secret");
		//check the course
		CoursePageFragment bookedCourse = CoursePageFragment.getCourse(ryomouBrowser);
		bookedCourse
			.assertOnTitle(title);
		
		//Author go in the list of bookings of the course
		BookingPage bookingList = course
			.openToolsMenu()
			.bookingTool();
		bookingList
			.assertFirstNameInListIsOk(ryomou);
		
		//Author go to members list
		course
			.members()
			.assertFirstNameInList(ryomou);
	}
	
	/**
	 * An author create a course, set a start and end date for life-cycle.
	 * It add a participant to the course. It creates a reminder
	 * with a rule to catch only participant, an other to send
	 * the reminder after the start of the course. It sends the reminder
	 * manually, checks the reminders send, checks the log.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseReminders(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -10);
		Date validFrom = cal.getTime();
		cal.add(Calendar.DATE, 20);
		Date validTo = cal.getTime();
		
		String title = "Remind-me-" + UUID.randomUUID().toString();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab()
			.setLifecycle(validFrom, validTo, Locale.GERMAN)
			.save();

		//open course editor, create a node, set access
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.openToolsMenu()
			.edit()
			.createNode("info")
			.autoPublish()
			.accessConfiguration()
			.setUserAccess(UserAccess.registred)
			.clickToolbarBack();
		// add a participant
		course
			.members()
			.quickAdd(kanu);
		
		//go to reminders
		RemindersPage reminders = course
				.reminders()
				.assertOnRemindersList();
		
		String reminderTitle = "REM-" + UUID.randomUUID();
		reminders
			.addReminder()
			.setDescription(reminderTitle)
			.setTimeBasedRule(1, "RepositoryEntryLifecycleAfterValidFromRuleSPI", 5, "day")
			.addRule(1)
			.setRoleBasedRule(2, "RepositoryEntryRoleRuleSPI", "participant")
			.saveReminder()
			.assertOnRemindersList()
			.assertOnReminderInList(reminderTitle);
		//send the reminders
		reminders
			.openActionMenu(reminderTitle)
			.sendReminders();
		//check the reminder is send to user
		reminders
			.openActionMenu(reminderTitle)
			.showSentReminders()
			//reminder send to user
			.assertSentRemindersList(kanu, true)
			//reminder not send to author
			.assertSentRemindersList(author, false);
		
		//open reminders log
		reminders
			.clickToolbarBack()
			.openLog()
			.assertLogList(kanu, reminderTitle, true)
			.assertLogList(author, reminderTitle, false);
	}
	
	/**
	 * An author creates a course with a structure element. The structure
	 * element is password protected. Under it, there is an info node. The
	 * course is published and a first user search the course, go to the
	 * structure element, give the password and see the info node. A second
	 * user grabs the rest url of the structure node, use it, give the password
	 * and go to the info node.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void coursePassword(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver kanuBrowser,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Password-me-" + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateForm(title)
			.assertOnGeneralTab()
			.save();
		
		String infoTitle = "Info - " + UUID.randomUUID();
		String structureTitle = "St - " + UUID.randomUUID();

		//open course editor, create a structure node
		CoursePageFragment course = new CoursePageFragment(browser);
		CourseEditorPageFragment editor = course
			.openToolsMenu()
			.edit();
		editor
			.createNode("st")
			.nodeTitle(structureTitle);
		String courseInfoUrl = editor.getRestUrl();
		editor
		//create an info node and move it under the structure node
			.createNode("info")
			.nodeTitle(infoTitle)
			.moveUnder(structureTitle)
			.selectNode(structureTitle)
		//select and set password on structure node
			.selectTabPassword()
			.setPassword("super secret")
		//publish
			.autoPublish()
			.accessConfiguration()
			.setUserAccess(UserAccess.registred)
			.clickToolbarBack();
		
		MenuTreePageFragment courseTree = course
			.clickTree()
			.selectWithTitle(structureTitle.substring(0, 20));
		course
			.assertOnPassword()
			.enterPassword("super secret");
		courseTree
			.selectWithTitle(infoTitle.substring(0, 20));
		course
			.assertOnTitle(infoTitle);
		
		//First user go to the course
		LoginPage kanuLoginPage = LoginPage.getLoginPage(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();

		NavigationPage kanuNavBar = new NavigationPage(kanuBrowser);
		kanuNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(title)
			.select(title)
			.start();
		
		//go to the structure, give the password
		CoursePageFragment kanuCourse = new CoursePageFragment(kanuBrowser);
		MenuTreePageFragment kanuTree = kanuCourse
			.clickTree()
			.selectWithTitle(structureTitle.substring(0, 20));
		kanuCourse
			.assertOnPassword()
			.enterPassword("super secret");
		kanuTree
			.selectWithTitle(infoTitle.substring(0, 20));
		kanuCourse
			.assertOnTitle(infoTitle);
		
		//Second user use the rest url
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, new URL(courseInfoUrl));
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		ryomouCourse
			.assertOnPassword()
			.enterPassword("super secret");
		//find the secret info course element
		ryomouCourse
			.clickTree()
			.selectWithTitle(structureTitle.substring(0, 20))
			.selectWithTitle(infoTitle.substring(0, 20));
		ryomouCourse
			.assertOnTitle(infoTitle);
	}
	
	/**
	 * An author creates a course with a forum, publish it, open a new thread.
	 * A first user come to see the thread. A second come via the peekview.
	 * The three make a reply at the same time. And they check that they see
	 * the replies, and the ones of the others.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void forum_concurrent(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver kanuBrowser,
			@Drone @Student WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course FO " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
	
		//go the authoring environment to create a forum
		String foTitle = "FO - " + UUID.randomUUID();
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("fo")
			.nodeTitle(foTitle)
		//publish the course
			.publish()
			.quickPublish(Access.users);
		
		//go to the forum
		courseEditor
			.clickToolbarBack()
			.clickTree()
			.selectWithTitle(foTitle.substring(0, 20));
		
		ForumPage authorForum = ForumPage
			.getCourseForumPage(browser);
		authorForum
			.createThread("The best anime ever", "What is the best anime ever?");
		
		//First user go to the course
		LoginPage kanuLoginPage = LoginPage.getLoginPage(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();

		NavigationPage kanuNavBar = new NavigationPage(kanuBrowser);
		kanuNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		
		//go to the forum
		new CoursePageFragment(kanuBrowser)
			.clickTree()
			.selectWithTitle(foTitle.substring(0, 20));
		
		ForumPage kanuForum = ForumPage
			.getCourseForumPage(kanuBrowser)
			.openThread("The best anime ever");

		
		//First user go to the course
		LoginPage reiLoginPage = LoginPage.getLoginPage(reiBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei)
			.resume();

		NavigationPage reiNavBar = new NavigationPage(reiBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		//select the thread in peekview
		ForumPage reiForum = new ForumPage(reiBrowser)
			.openThreadInPeekview("The best anime ever");
		
		//concurrent reply
		String kanuReply = "Ikki Touzen";
		String reiReply = "Neon Genesis Evangelion";
		String authorReply = "Lain, serial experiment";
		
		authorForum
			.replyToMessageNoWait("The best anime ever", null, authorReply);
		reiForum
			.replyToMessageNoWait("The best anime ever", null, reiReply);
		kanuForum
			.replyToMessageNoWait("The best anime ever", null, kanuReply);
	
		//wait the responses
		OOGraphene.waitBusy(browser);
		OOGraphene.waitBusy(kanuBrowser);
		OOGraphene.waitBusy(reiBrowser);
		
		//check own responses
		authorForum.assertMessageBody(authorReply);
		kanuForum.assertMessageBody(kanuReply);
		reiForum.assertMessageBody(reiReply);

		//check others responses
		authorForum
			.flatView()
			.waitMessageBody(kanuReply);
		reiForum
			.flatView()
			.waitMessageBody(kanuReply);
		kanuForum
			.flatView()
			.waitMessageBody(reiReply);
	}
	
	/**
	 * An author creates a course with 4 course elements. A folder
	 * which is visible to group, a forum which is visible to coaches,
	 * an assessment and an info visible to the students which passed
	 * the assessment above.<br>
	 * a student come and checks what it can see, the author make it
	 * pass the assessment and the student sees the info.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseAccessRules(@InitialPage LoginPage loginPage,
			@Drone @Student WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("rei");
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course FO " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
	
		//go the authoring environment to create a forum
		String bcTitle = "BC - " + UUID.randomUUID();
		String foTitle = "FO - " + UUID.randomUUID();
		String msTitle = "MS - " + UUID.randomUUID();
		String infoTitle = "Info - " + UUID.randomUUID();
		
		String groupName = "Students";
		
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		//folder is group protected
		courseEditor
			.createNode("bc")
			.nodeTitle(bcTitle)
			.selectTabVisibility()
			.setGroupCondition()
			.createBusinessGroup(groupName);
		//forum is coach exclusive
		courseEditor
			.createNode("fo")
			.nodeTitle(foTitle)
			.selectTabVisibility()
			.setCoachExclusive()
			.save();
		//assessment is open
		courseEditor
			.createNode("ms")
			.nodeTitle(msTitle);
		//configure assessment
		AssessmentCEConfigurationPage assessmentConfig = new AssessmentCEConfigurationPage(browser);
		assessmentConfig
			.selectConfiguration()
			.setScoreAuto(0.0f, 6.0f, 4.0f);
		
		//wiki is assessment dependent
		courseEditor
			.createNode("info")
			.nodeTitle(infoTitle)
			.selectTabVisibility()
			.setAssessmentCondition(1)
			.save();

		courseEditor
			.publish()
			.quickPublish(Access.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		//add a member to the group we create above
		MembersPage members = CoursePageFragment
			.getCourse(browser)
			.members();
		members
			.addMember()
			.searchMember(rei, true)
			.next()
			.next()
			.selectGroupAsParticipant(groupName)
			.next()
			.finish();
		
		//participant search the course
		LoginPage.getLoginPage(reiBrowser, deploymentUrl)
			.loginAs(rei)
			.resume();
		NavigationPage reiNavBar = new NavigationPage(reiBrowser);
		reiNavBar
			.openMyCourses()
			.select(courseTitle);
		
		MenuTreePageFragment reiTree = new MenuTreePageFragment(reiBrowser);
		reiTree
			.assertWithTitle(bcTitle.substring(0, 20))
			.assertWithTitle(msTitle.substring(0, 20))
			.assertTitleNotExists(foTitle.substring(0, 20))
			.assertTitleNotExists(infoTitle.substring(0, 20));
		
		//author set assessment to passed
		members
			.clickToolbarBack()
			.assessmentTool()
			.users()
			.assertOnUsers(rei)
			.selectUser(rei)
			.selectCourseNode(msTitle.substring(0, 20))
			.setAssessmentScore(5.5f)
			.assertUserPassedCourseNode(msTitle.substring(0, 20));
		
		//student can see info
		reiTree
			.selectRoot()
			.assertWithTitle(bcTitle.substring(0, 20))
			.assertWithTitle(msTitle.substring(0, 20))
			.assertTitleNotExists(foTitle.substring(0, 20))
			.assertWithTitle(infoTitle.substring(0, 20));
		
		//author can see all
		members
			.clickToolbarBack()
			.clickTree()
			.assertWithTitle(bcTitle.substring(0, 20))
			.assertWithTitle(msTitle.substring(0, 20))
			.assertWithTitle(foTitle.substring(0, 20))
			.assertWithTitle(infoTitle.substring(0, 20));
	}
}
