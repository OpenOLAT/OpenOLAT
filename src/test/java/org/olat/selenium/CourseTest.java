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
import java.util.List;
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
import org.olat.selenium.page.User;
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseWizardPage;
import org.olat.selenium.page.course.InfoMessageCEPage;
import org.olat.selenium.page.course.PublisherPageFragment;
import org.olat.selenium.page.course.PublisherPageFragment.Access;
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
			.loginAs("administrator", "openolat");
		new NavigationPage(administratorDrone)
			.openAdministration()
			.clearCache("FeedManager@feed");
		
		//the author publish a second post in its blog
		String post2Title = "Blog-RW-2-" + UUID.randomUUID();
		String post2Summary = "Some explantations as teaser";
		String post2Content = "Content of the post";
		feed.addPost()
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
		
		OOGraphene.closeBlueMessageWindow(browser);
		
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
}
