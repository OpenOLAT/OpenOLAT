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
import java.util.HashMap;
import java.util.Map;
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
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.portfolio.ArtefactWizardPage;
import org.olat.selenium.page.portfolio.PortfolioPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.FeedPage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.selenium.page.wiki.WikiPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * 
 * Suite of test for the e-Portfolio version 1.0
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class PortfolioTestLegacy {
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		Map<String,String> propertyPortfolioV1 = new HashMap<>();
		propertyPortfolioV1.put("portfoliov2.enabled", "false");
		propertyPortfolioV1.put("portfolio.enabled", "true");
		return ArquillianDeployments.createDeployment(propertyPortfolioV1);
	}

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	@Page
	private NavigationPage navBar;

	/**
	 * Create a course with a forum, publish it.
	 * Create a map.
	 * Post in the forum, collect the artefact, bind it to the map.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void collectForumArtefactInCourse(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Map-Forum-" + UUID.randomUUID();
		String pageTitle = "Page-Forum-" + UUID.randomUUID();
		String structureElementTitle = "Struct-Forum-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Hello forum")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		
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
		
		String threadTitle = "Very interessant thread";
		ForumPage forum = ForumPage.getCourseForumPage(browser);
		ArtefactWizardPage artefactWizard = forum
			.createThread(threadTitle, "With a lot of content", null)
			.addAsArtfeact();
		
		artefactWizard
			.next()
			.tags("Forum", "Thread", "Miscellanous")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
		
		portfolio.assertArtefact(threadTitle);
	}
	
	/**
	 * Create a wiki, create a new page.
	 * Create a map.
	 * Collect the artefact, bind it to the map.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void collectWikiArtefactInWikiResource(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio();
				
		//create a map
		String mapTitle = "Map-Wiki-" + UUID.randomUUID();
		String pageTitle = "Page-Wiki-" + UUID.randomUUID();
		String structureElementTitle = "Struct-Wiki-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Hello wiki")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description about wiki and such tools")
			.createStructureElement(structureElementTitle, "Structure description");

		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
				
		String title = "EP-Wiki-" + UUID.randomUUID();
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
		ArtefactWizardPage artefactWizard = wiki
				.createPage(page, content)
				.addAsArtfeact();
			
		artefactWizard
			.next()
			.tags("Wiki", "Thread", "Miscellanous")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
		
		portfolio.assertArtefact(page);
	}
	
	/**
	 * Create a map.
	 * Create a course with a blog course element.
	 * Post a new entry in the blog.
	 * Collect the artefact, bind it to the map.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void collectBlogPostInCourse(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Map-Blog-" + UUID.randomUUID();
		String pageTitle = "Page-Blog-" + UUID.randomUUID();
		String structureElementTitle = "Struct-Blog-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Hello blog post")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		
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
		ArtefactWizardPage artefactWizard = feed
			.newBlog()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost()
			.addAsArtfeact();

		artefactWizard
			.next()
			.tags("Forum", "Thread", "Miscellanous")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
		
		portfolio.assertArtefact(postTitle);	
	}

	/**
	 * Create a map.
	 * In the artefacts view, create a new text artefact and
	 * bind it to the map.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void addTextArtefact(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Map-Text-1-" + UUID.randomUUID();
		String pageTitle = "Page-Text-1-" + UUID.randomUUID();
		String structureElementTitle = "Struct-Text-1-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Need to place a text artefact")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		//go to my artefacts
		portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio()
				.openMyArtefacts();
		
		String textTitle = "Text-1-" + UUID.randomUUID();
		//create a text artefact
		portfolio
			.addArtefact()
			.createTextArtefact()
			.fillTextArtefactContent("Content of the text artefact")
			.next()
			.fillArtefactMetadatas(textTitle, "Description")
			.next()
			.tags("Forum", "Thread", "Miscellanous")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
	}
	
	/**
	 * Create a map with a structure element and
	 * create a text artefact.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void addTextArtefact_withinMap(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Map-Text-1-" + UUID.randomUUID();
		String pageTitle = "Page-Text-1-" + UUID.randomUUID();
		String structureElementTitle = "Struct-Text-1-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Need to place a text artefact")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		//create the text artefact
		ArtefactWizardPage artefactWizard = portfolio
				.linkArtefact()
				.addArtefact()
				.createTextArtefact();
		
		String textTitle = "Text-2-" + UUID.randomUUID();
		//create a text artefact
		artefactWizard
			.fillTextArtefactContent("Content of the text artefact")
			.next()
			.fillArtefactMetadatas(textTitle, "Description")
			.next()
			.tags("LateX", "Thread", "Miscellanous")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//reopen the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
	}
	
	/**
	 * Create a map.
	 * In the artefacts view, create a new live blog
	 * (or learning journal) and bind it to the map.
	 * Check the map and post an entry in the blog.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void addLearningJournal(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Learning-Journal-1-" + UUID.randomUUID();
		String pageTitle = "Learning-Journal-Page-1-" + UUID.randomUUID();
		String structureElementTitle = "Learning-Journal-Struct-1-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Need to journal my learning feelings")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		//go to my artefacts
		portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio()
				.openMyArtefacts();
		
		String textTitle = "Journal-1-" + UUID.randomUUID();
		//create a live blog or learning journal
		portfolio
			.addArtefact()
			.createLearningJournal()
			.fillArtefactMetadatas(textTitle, "Description")
			.next()
			.tags("Journal", "Live", "Learning")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
		
		//play with the blog
		String postTitle = "Journal-EP-" + UUID.randomUUID();
		String postSummary = "Some explantations of the journal";
		String postContent = "First impression in my live blog";
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed
			.newBlog()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost();
		
		//check that we see the post
		By postTitleBy = By.cssSelector("h3.o_title>a>span");
		WebElement postTitleEl = browser.findElement(postTitleBy);
		Assert.assertTrue(postTitleEl.getText().contains(postTitle));
	}
	
	/**
	 * Create a map, create a new live blog
	 * (or learning journal), add a post.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void addLearningJournal_withinMap(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Learning-Journal-2-" + UUID.randomUUID();
		String pageTitle = "Learning-Journal-Page-2-" + UUID.randomUUID();
		String structureElementTitle = "Learning-Journal-Struct-2-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Need quickly a journal to share my learning feelings")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		//create the live blog
		ArtefactWizardPage artefactWizard = portfolio
				.linkArtefact()
				.addArtefact()
				.createLearningJournal();
		
		String textTitle = "Journal-2-" + UUID.randomUUID();
		//create a live blog or learning journal
		artefactWizard
			.fillArtefactMetadatas(textTitle, "Description")
			.next()
			.tags("Journal", "Live", "Learning")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
		
		//play with the blog
		String postTitle = "Journal-EP-" + UUID.randomUUID();
		String postSummary = "Some explantations of the journal";
		String postContent = "First impression in my live blog created in few clicks";
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed
			.newBlog()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost();
		
		//check that we see the post
		By postTitleBy = By.cssSelector("h3.o_title>a>span");
		WebElement postTitleEl = browser.findElement(postTitleBy);
		Assert.assertTrue(postTitleEl.getText().contains(postTitle));
	}
	
	/**
	 * Create a map.
	 * Go the "My artefacts" and create an artefact of type file. Bind it
	 * to the map. Check the map.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void addFileArtefact(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		//File upload only work with Firefox
		Assume.assumeTrue(browser instanceof FirefoxDriver);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Map-File-1-" + UUID.randomUUID();
		String pageTitle = "Page-File-1-" + UUID.randomUUID();
		String structureElementTitle = "Struct-File-1-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Need to upload some file")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		//go to my artefacts
		portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio()
				.openMyArtefacts();
		
		String textTitle = "File-1-" + UUID.randomUUID();
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File file = new File(courseUrl.toURI());
		//create the artefact
		portfolio
			.addArtefact()
			.createFileArtefact()
			.uploadFile(file)
			.next()
			.fillArtefactMetadatas(textTitle, "Description")
			.next()
			.tags("File", "PDF", "Learning")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);
		
		//check that we see the post
		By artefactTitleBy = By.cssSelector("div.panel-heading>h3");
		WebElement artefactTitle = browser.findElement(artefactTitleBy);
		Assert.assertTrue(artefactTitle.getText().contains(textTitle));
	}
	
	/**
	 * Create a map, create a new file artefact.
	 * Check the map and the artefact.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void addFileArtefact_withinMap(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		//File upload only work with Firefox
		Assume.assumeTrue(browser instanceof FirefoxDriver);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//open the portfolio
		UserToolsPage userTools = new UserToolsPage(browser);
		PortfolioPage portfolio = userTools
				.openUserToolsMenu()
				.openPortfolio();
		
		//create a map
		String mapTitle = "Map-File-2-" + UUID.randomUUID();
		String pageTitle = "Page-File-2-" + UUID.randomUUID();
		String structureElementTitle = "Struct-File-2-" + UUID.randomUUID();
		portfolio
			.openMyMaps()
			.createMap(mapTitle, "Need a map to upload some files quckly")
			.openEditor()
			.selectMapInEditor(mapTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		
		//create the file artefact
		ArtefactWizardPage artefactWizard = portfolio
				.linkArtefact()
				.addArtefact()
				.createFileArtefact();
		
		String textTitle = "File-2-" + UUID.randomUUID();
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File file = new File(courseUrl.toURI());
		//foolow the wizard
		artefactWizard
			.uploadFile(file)
			.next()
			.fillArtefactMetadatas(textTitle, "Description")
			.next()
			.tags("File", "Data", "Learning")
			.next()
			.selectMap(mapTitle, pageTitle, structureElementTitle)
			.finish();
		
		//open the portfolio
		portfolio = userTools
			.openUserToolsMenu()
			.openPortfolio()
			.openMyMaps()
			.openMap(mapTitle)
			.selectStructureInTOC(structureElementTitle);

		//check that we see the post
		By artefactTitleBy = By.cssSelector("div.panel-heading>h3");
		WebElement artefactTitle = browser.findElement(artefactTitleBy);
		Assert.assertTrue(artefactTitle.getText().contains(textTitle));
	}
	
	/**
	 * Create a course with a portfolio course element.
	 * Create a template from within the portfolio course
	 * element and edit it. Rename a page, add a structure
	 * element. Publish the course, go to the course
	 * and check if the link to take the map is there.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Ignore @Test
	@RunAsClient
	public void createPortfolioTemplate_inCourse(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		String courseTitle = "Course-With-Portfolio-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String portfolioNodeTitle = "Template-EP-1";
		String portfolioTitle = "Template - EP - " + UUID.randomUUID();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		PortfolioPage template = courseEditor
			.createNode("ep")
			.nodeTitle(portfolioNodeTitle)
			.selectTabLearnContent()
			.createPortfolio(portfolioTitle)
			.editPortfolio();

		String pageTitle = "Page-Template-" + UUID.randomUUID();
		String structureElementTitle = "Struct-Template-" + UUID.randomUUID();
		template
			.openResourceEditor()
			.selectMapInEditor(portfolioTitle)
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description");
		//open course
		navBar.openCourse(courseTitle);
		
		//reload editor
		courseEditor = CourseEditorPageFragment.getEditor(browser);
		courseEditor
			.publish()
			.quickPublish();
		
		//back to the course
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		//select the portfolio course element
		course
			.clickTree()
			.selectWithTitle(portfolioNodeTitle);
		
		By newMapBy = By.className("o_sel_ep_new_map_template");
		WebElement newMapButton = browser.findElement(newMapBy);
		Assert.assertTrue(newMapButton.isDisplayed());
	}
}
