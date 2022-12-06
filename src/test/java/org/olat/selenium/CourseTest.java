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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.Author;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.core.BookingPage;
import org.olat.selenium.page.core.CalendarPage;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.course.AssessmentCEConfigurationPage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseSettingsPage;
import org.olat.selenium.page.course.CourseWizardPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PublisherPageFragment;
import org.olat.selenium.page.course.RemindersPage;
import org.olat.selenium.page.course.STConfigurationPage;
import org.olat.selenium.page.course.STConfigurationPage.DisplayType;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.CPPage;
import org.olat.selenium.page.repository.RepositoryEditDescriptionPage;
import org.olat.selenium.page.repository.RepositorySettingsPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * The test @see confirmMembershipForCourse can break others if not successful
 * as it changes the setting for confirmation for memberships and reset it at
 * the end.
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class CourseTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
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
	public void createCourse()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Create-Selen-" + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			//from description editor, back to the course
			.clickToolbarBack();
		
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit();
		
		//create a course element of type info messages
		PublisherPageFragment publisher = editor
			.assertOnEditor()
			.createNode("info")
			.publish();
		
		//publish
		publisher
			.assertOnPublisher()
			.nextSelectNodes()
			.selectAccess(UserAccess.guest)
			.nextAccess()
			.selectCatalog(false)
			.nextCatalog() // -> no problem found
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
	 * Check if we can create and open a course with this
	 * name: It's me, the "course".
	 * @see https://jira.openolat.org/browse/OO-1839
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithSpecialCharacters()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String marker = Long.toString(System.currentTimeMillis());
		String title = "It's me, the \"course\" number " + marker;
		//create course
		RepositoryEditDescriptionPage editDescription = authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos();
		
		//from description editor, back to the course
		editDescription
			.clickToolbarBack();
		
		//close the course
		navBar.closeTab();
		
		//select the authoring
		navBar
			.openAuthoringEnvironment()
			.openResource(marker);
		
		new CoursePageFragment(browser)
			.assertOnCoursePage();
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
	public void createCourse_withWizard()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
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
			.nextNodes()
			.nextCatalog()
			.finish();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		RepositorySettingsPage settings = new RepositorySettingsPage(browser);
		//from description editor, back to details and launch the course
		settings
			.assertOnInfos();
		settings	
			.back();

		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		course
			.assertOnCoursePage();
		
		//assert the 5 nodes are there and click them
		By elementsBy = By.xpath("//div[contains(@class,'o_tree')]//ul[contains(@class,' o_tree_l1')][count(li)=5]");
		OOGraphene.waitElement(elementsBy, browser);
		
		for(int i=0; i<5; i++) {
			By linkBy = By.xpath("//div[contains(@class,'o_tree')]//li[" + (i+1) + "]/div/span[contains(@class,'o_tree_link')][contains(@class,'o_tree_l1')][contains(@class,'o_tree_level_label_leaf')]/a[span]");
			OOGraphene.waitElement(linkBy, browser);
			browser.findElement(linkBy).click();
			By activeLinkBy = By.xpath("//div[contains(@class,'o_tree')]//li[" + (i+1) + "][contains(@class,'active')]/div/span[contains(@class,'o_tree_link')][contains(@class,'o_tree_l1')][contains(@class,'o_tree_level_label_leaf')]/a[span]");
			OOGraphene.waitElement(activeLinkBy, browser);
		}
	}
	
	/**
	 * An author create a course, add a second user as co-author
	 * of the course, and edit the course.<br>
	 * The co-author select the course and try to edit it, unsuccessfully.
	 * It try to edit the course directly from the author list without
	 * success.<br>
	 * The author closes the editor and the co-author is allowed to edit.
	 * The author cannot edit i anymore...
	 * 
	 * @param loginPage Login page of the author
	 * @param coAuthorBrowser the browser for the coauthor
	 */
	@Test
	@RunAsClient
	public void concurrentEditCourse(@Drone @Participant WebDriver coAuthorBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO coAuthor = new UserRestClient(deploymentUrl).createAuthor("Rei");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();

		String title = "Coedit-" + UUID.randomUUID().toString().replace("-", " ");
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		//add a second owner
		MembersPage members = new CoursePageFragment(browser)
			.members();
		members
			.addMember()
			.searchMember(coAuthor, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(true, false, false)
			.nextPermissions()
			.finish();
		//open the editor
		CoursePageFragment coursePage = members
			.clickToolbarBack();
		CourseEditorPageFragment editor = coursePage
			.edit();
		
		//the second author come in
		LoginPage coAuthroLoginPage = LoginPage.load(coAuthorBrowser, deploymentUrl);
		coAuthroLoginPage
			.loginAs(coAuthor.getLogin(), coAuthor.getPassword())
			.resume();
	
		//go to authoring
		NavigationPage coAuthorNavBar = NavigationPage.load(coAuthorBrowser);
		coAuthorNavBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment()
			.openResource(title);
		//try to edit
		CoursePageFragment coAuthorCourse = new CoursePageFragment(coAuthorBrowser);
		coAuthorCourse
			.tryToEdit()
			.assertOnWarning();
		
		//retry in list
		coAuthorNavBar
			.openAuthoringEnvironment()
			.editResource(title);
		new CourseEditorPageFragment(coAuthorBrowser)
			.assertOnWarning();
		
		//author close the course editor
		editor
			.clickToolbarBack();
		coursePage
			.assertOnCoursePage();
		
		//co-author edit the course
		CourseEditorPageFragment coAuthorEditor = coAuthorCourse
			.edit()
			.assertOnEditor();
		
		//author try
		coursePage
			.tryToEdit()
			.assertOnWarning();
		
		//co-author close the editor
		coAuthorEditor
			.clickToolbarBack()
			.assertOnCoursePage();
		
		//author reopens the editor
		coursePage
			.edit()
			.assertOnEditor();
	}
	
	/**
	 * An author create a course, a user see it.<br>
	 * The author change the course and publish it. The user
	 * must see a warning if the same node as been modified.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void concurrentVisitAndPublish(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//create a course
		String courseTitle = "Course to publish-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(courseTitle)
			.edit();
		
		//create a course element of type info messages
		String firstNodeTitle = "First node";
		String secondNodeTitle = "Second node";
		editor
			.assertOnEditor()
			.createNode("info")
			.nodeTitle(firstNodeTitle)
			.createNode("st")
			.nodeTitle(secondNodeTitle)
			.publish()
			.quickPublish(UserAccess.registred);
		
		// The user opens the course
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		MenuTreePageFragment ryomouCourseTree = ryomouCourse
			.tree()
			.selectWithTitle(firstNodeTitle);
		
		//The author make a change on node 2
		String changedNodeTitlev2 = "Changed 2 title";
		course = editor
			.selectNode(secondNodeTitle)
			.nodeTitle(changedNodeTitlev2)
			.autoPublish();
		
		//The user click the first node and the changed second node
		ryomouCourseTree
			.selectWithTitle(firstNodeTitle)
			.selectWithTitle(changedNodeTitlev2);
		ryomouCourse
			.assertOnTitle(changedNodeTitlev2);
		
		//The author changed the second node
		String changedNodeTitlev3 = "Changed 3 title";
		course = course.edit()
			.selectNode(changedNodeTitlev2)
			.nodeTitle(changedNodeTitlev3)
			.autoPublish();
		
		//The user wait the message
		ryomouCourse
			.assertOnRestart()
			.clickRestart();
		ryomouCourseTree
			.selectWithTitle(changedNodeTitlev3);
		ryomouCourse
			.assertOnTitle(changedNodeTitlev3);
	}
	
	
	/**
	 * Test that renaming the root node is reflected after
	 * publishing.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseRename()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course to rename-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(courseTitle)
			.edit()
			.assertOnEditor()
			.selectRoot();
		
		// configure the simplest overview
		STConfigurationPage stConfig = new STConfigurationPage(browser);
		stConfig
			.selectOverview()
			.setDisplay(DisplayType.system);
		
		String nodeTitle = "More informations";
		//create a course element of type info messages
		course = editor
			
			.createNode("info")
			.nodeTitle(nodeTitle)
			.autoPublish();
		//check that the root node has the name of the repository entry
		course
			.assertOnTitle(courseTitle);
		
		//rename the root node
		String newCourseName = "Renamed course";
		course = course
			.edit()
			.selectRoot()
			.nodeTitle(newCourseName)
			.autoPublish();
		
		//assert the changed name
		course
			.assertOnTitle(newCourseName);
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
	public void catalogRoundTrip()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		
		//administrator create the categories in the catalog
		LoginPage adminLogin = LoginPage.load(browser, deploymentUrl);
		adminLogin
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage adminNavBar = NavigationPage.load(browser);
		
		String node1 = "First level " + UUID.randomUUID();
		String node2_1 = "Second level first element " + UUID.randomUUID();
		String node2_2 = "Second level second element " + UUID.randomUUID();
		String node1Short = "First " + JunitTestHelper.miniRandom();
		String node2_1Short = "1.1l " + JunitTestHelper.miniRandom();
		String node2_2Short = "1.2l " + JunitTestHelper.miniRandom();
		adminNavBar
				.openCatalogAdministration()
				.addCatalogNode(node1, node1Short, "First level of the catalog")
				.selectNode(node1Short)
				.addCatalogNode(node2_1, node2_1Short, "First element of the second level")
				.addCatalogNode(node2_2, node2_2Short, "Second element of the second level");
		
		//An author create a course and publish it under a category
		//created above
		LoginPage login = LoginPage.load(browser, deploymentUrl);
		login
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		String courseTitle = "Catalog-Course-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
	
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.publish()
			.nextSelectNodes()
			.selectAccess(UserAccess.registred)
			.nextAccess()
			.selectCatalog(true)
			.selectCategory(node1, node2_2)
			//.nextCatalog() // -> no problem found
			.finish();
		
		//User logs in, go to "My courses", navigate the catalog and start
		//the course
		LoginPage userLogin = LoginPage.load(browser, deploymentUrl);
		userLogin
			.loginAs(user.getLogin(), user.getPassword())
			.resume();

		NavigationPage userNavBar = NavigationPage.load(browser);
		userNavBar
			.openMyCourses()
			.openCatalog()
			.selectCatalogEntry(node1Short)
			.selectCatalogEntry(node2_2Short)
			.select(courseTitle)//go to the details page
			.start();
		
		By courseTitleBy = By.cssSelector("div.o_course_run h2");
		OOGraphene.waitElement(courseTitleBy, browser);
		WebElement courseTitleEl = browser.findElement(courseTitleBy);
		Assert.assertTrue(courseTitleEl.getText().contains(courseTitle));
	}
	
	/**
	 * Create a course with a calendar element, add a recurring event
	 * all day, modify an occurrence to an event between 13h and 15h.
	 * Remove an other single occurrence and at the end, remove all remaining
	 * events by removing the original event and confirm that
	 * all must be deleted.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithCalendar()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-iCal-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		navBar.openCourse(courseTitle);
		
		String calendarNodeTitle = "iCalNode-1";
		//create a course element of type calendar
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cal")
			.nodeTitle(calendarNodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the calendar
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(calendarNodeTitle);
		// create a recurring event
		CalendarPage calendar = new CalendarPage(browser);
		calendar
			.assertOnCalendar()
			.addEvent(3)
			.setDescription("Eventhor", "Hammer", "Asgard")
			.setAllDay(true)
			.setRecurringEvent(KalendarEvent.WEEKLY, 28)
			.save();
		// modify an occurence
		calendar
			.openDetailsOccurence("Eventhor", 17)
			.edit()
			.setAllDay(false)
			.setBeginEnd(13, 15)
			.save()
			.confirmModifyOneOccurence();
		
		// check
		calendar
			.assertOnEvents("Eventhor", 4)
			.assertOnEventsAt("Eventhor", 1, 13);
		
		// modify all events
		calendar
			.openDetailsOccurence("Eventhor", 3)
			.edit()
			.setDescription("Eventoki", null, null)
			.save()
			.confirmModifyAllOccurences();
		// check
		calendar
			.assertOnEvents("Eventoki", 3)
			.assertOnEventsAt("Eventhor", 1, 13);
		
		// delete an occurence
		calendar
			.openDetailsOccurence("Eventoki", 10)
			.edit()
			.delete()
			.confirmDeleteOneOccurence();
		// check
		calendar
			.assertOnEvents("Eventoki", 2)
			.assertOnEventsAt("Eventhor", 1, 13);
		
		// delete all
		calendar
			.openDetailsOccurence("Eventoki", 3)
			.edit()
			.delete()
			.confirmDeleteAllOccurences();
		
		OOGraphene.waitingALittleBit();
		calendar
			.assertZeroEvent();
	}
	
	/**
	 * This is a variant of the test above based on the
	 * feedback of our beta-testerin.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithCalendar_alt()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-iCal-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		navBar.openCourse(courseTitle);
		
		// activate the calendar options
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseSettingsPage settings = course
			.settings();
		settings
			.toolbar()
			.calendar(Boolean.TRUE)
			.save();
		settings
			.clickToolbarBack();
		
		String calendarNodeTitle = "iCalNode-2";
		//create a course element of type calendar
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cal")
			.nodeTitle(calendarNodeTitle);
		
		//publish the course
		course = courseEditor
			.autoPublish();
		//open the course and see the CP
		course
			.tree()
			.selectWithTitle(calendarNodeTitle);
		
		// create a recurring event
		CalendarPage calendar = new CalendarPage(browser);
		calendar
			.addEvent(2)
			.setDescription("Repeat", "Loop", "Foreach")
			.setAllDay(false)
			.setBeginEnd(14, 18)
			.setRecurringEvent(KalendarEvent.WEEKLY, 28)
			.save()
			.assertOnEvents("Repeat", 4);
		
		//pick an occurence which is not the first and modify it
		calendar
			.openDetailsOccurence("Repeat", 16)
			.edit()
			.setBeginEnd(15, 18)
			.save()
			.confirmModifyAllOccurences()
			.assertOnEventsAt("Repeat", 4, 15);
		
		// delete futur event of the same event as above
		calendar
			.openDetailsOccurence("Repeat", 16)
			.edit()
			.delete()
			.confirmDeleteFuturOccurences()
			.assertOnEventsAt("Repeat", 2, 15);
	}
	
	/**
	 * An author create a course with a calendar. It add
	 * an event to the calendar, the event is not recurring
	 * for the moment. The author save it, edit it again
	 * and make it a recurring event. The test is there to
	 * check the transition from non-recurring to recurring
	 * works flawlessly. 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCourseWithCalendar_singleToRecurrent()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-iCal-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		navBar.openCourse(courseTitle);
		
		// activate the calendar options
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseSettingsPage settings = course
			.settings();
		settings
			.toolbar()
			.calendar(Boolean.TRUE)
			.save();
		settings
			.clickToolbarBack();
		
		String calendarNodeTitle = "iCalNode-3";
		//create a course element of type calendar
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cal")
			.nodeTitle(calendarNodeTitle);
		
		//publish the course
		course = courseEditor
			.autoPublish();
		//open the course and see the CP
		course
			.tree()
			.selectWithTitle(calendarNodeTitle);
		
		// create a recurring event
		CalendarPage calendar = new CalendarPage(browser);
		calendar
			.assertOnCalendar()
			.addEvent(2)
			.setDescription("Repeat", "Loop", "Foreach")
			.setAllDay(false)
			.setBeginEnd(14, 18)
			.save()
			.assertOnEvents("Repeat", 1);
		
		//pick an occurence which is not the first and modify it
		calendar
			.openDetails("Repeat")
			.edit()
			.setRecurringEvent(KalendarEvent.WEEKLY, 28)
			.save()
			.assertOnEventsAt("Repeat", 4, 14);
		
		//pick details of an occurent
		calendar
			.openDetailsOccurence("Repeat", 9);
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
	public void courseBooking(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Create-Selen-" + UUID.randomUUID().toString();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos();

		//open course editor
		CoursePageFragment course = new CoursePageFragment(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.selectRoot();
		// configure the peekview
		STConfigurationPage stConfig = new STConfigurationPage(browser);
		stConfig
			.selectOverview()
			.setDisplay(DisplayType.peekview);
		
		course = courseEditor
			.createNode("info")
			.autoPublish();
		course
			.settings()
			.accessConfiguration()
			//add booking by secret token
			.setAccessWithTokenBooking("secret", "The password is secret")
			.clickToolbarBack();
		// publish the course
		course
			.publish();
		
		//a user search the course
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
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
			.bookingTool();
		bookingList
			.assertFirstNameInListIsOk(ryomou);
		
		//Author go to members list
		course
			.members()
			.assertFirstNameInList(ryomou);
	}
	

	/**
	 * An author creates a course, make it visible for
	 * members and add an access control by free booking
	 * with the auto booking enabled.<br/>
	 * The user search for the course and enters it.<br/>
	 * The author checks in the list of orders if the booking
	 * of the user is there and after it checks if the user is
	 * in the member list too.
	 * 
	 * @param loginPage
	 * @param userBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseFreeBooking(@Drone @User WebDriver userBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "AutoBooking-" + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos();

		String infoMessageTitle = "Some informations";
		//open course editor
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.edit()
			.createNode("info")
			.nodeTitle(infoMessageTitle)
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessWithFreeBooking("It's free")
			.cleanBlueBox()
			.clickToolbarBack();
		course
			.publish();
		
		//a user search the course
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(userBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(title)
			.book(title);//book the course
		//check the course
		CoursePageFragment bookedCourse = CoursePageFragment.getCourse(userBrowser);
		bookedCourse
			.assertOnTitle(infoMessageTitle);
		
		//Author go in the list of bookings of the course
		BookingPage bookingList = course
			.bookingTool();
		bookingList
			.assertFirstNameInListIsOk(user);
		
		//Author go to members list
		course
			.members()
			.assertFirstNameInList(user);
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
	public void courseReminders()
	throws IOException, URISyntaxException {
		//configure at least a license
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage.load(browser)
			.openAdministration()
			.openLicenses()
			.enableForResources("all rights reserved");
		new UserToolsPage(browser)
			.logout();
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		loginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -10);
		Date validFrom = cal.getTime();
		cal.add(Calendar.DATE, 20);
		Date validTo = cal.getTime();
		
		String title = "Remind-me-" + UUID.randomUUID();
		//create course
		RepositorySettingsPage settings = authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false);
		settings
			.assertOnInfos();
		settings
			.metadata()
			.setLicense();
		settings
			.execution()
			.setLifecycle(validFrom, validTo, Locale.GERMAN)
			.save();
		settings
			.back();

		//open course editor, create a node, set access
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.edit()
			.createNode("info")
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		course
			.publish();
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
			.setTimeBasedRule("main", "RepositoryEntryLifecycleAfterValidFromRuleSPI", 5, "day")
			.addRule("RepositoryEntryRoleRuleSPI")
			.setRoleBasedRule("1", "RepositoryEntryRoleRuleSPI", "participant")
			.nextToReview()
			.assertOnReviewInList(kanu)
			.nextToEmail()
			.setSubject(reminderTitle)
			.finish()
			.assertOnRemindersList()
			.assertOnReminderInList(reminderTitle);
		//send the reminders
		reminders
			.openActionMenu(reminderTitle)
			.sendRemindersToAll();
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
			.openMoreMenu()
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
	public void coursePassword(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Password-me-" + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		String infoTitle = "Info - " + UUID.randomUUID();
		String structureTitle = "St - " + UUID.randomUUID();

		//open course editor, create a structure node
		CoursePageFragment course = new CoursePageFragment(browser);
		CourseEditorPageFragment editor = course
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
		//access settings
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		//publish
		course
			.publish();
		
		MenuTreePageFragment courseTree = course
			.tree()
			.selectWithTitle(structureTitle.substring(0, 20));
		course
			.assertOnPassword()
			.enterPassword("super secret");
		courseTree
			.selectWithTitle(infoTitle.substring(0, 20));
		course
			.assertOnTitle(infoTitle);
		
		//First user go to the course
		LoginPage kanuLoginPage = LoginPage.load(browser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();

		NavigationPage kanuNavBar = NavigationPage.load(browser);
		kanuNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(title)
			.select(title)
			.start();
		
		//go to the structure, give the password
		CoursePageFragment kanuCourse = new CoursePageFragment(browser);
		MenuTreePageFragment kanuTree = kanuCourse
			.tree()
			.selectWithTitle(structureTitle.substring(0, 20));
		kanuCourse
			.assertOnPassword()
			.enterPassword("super secret");
		kanuTree
			.selectWithTitle(infoTitle.substring(0, 20));
		kanuCourse
			.assertOnTitle(infoTitle);
		
		//Second user use the rest url
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, new URL(courseInfoUrl));
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		ryomouCourse
			.assertOnPassword()
			.enterPassword("super secret");
		//find the secret info course element
		ryomouCourse
			.tree()
			.selectWithTitle(structureTitle.substring(0, 20))
			.selectWithTitle(infoTitle.substring(0, 20));
		ryomouCourse
			.assertOnTitle(infoTitle);
	}
	
	
	/**
	 * An author create a course with a course element
	 * to contact all members of the course. It add some
	 * participants. A participant log in, go to the
	 * course to use the contact form and send an E-mail.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseDeleteCourseElement()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Contact Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "SomeNode";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("bc")
			.nodeTitle(nodeTitle);

		courseEditor
			.autoPublish();
		
		courseRuntime
			.tree()
			.assertWithTitle(nodeTitle)
			.selectWithTitle(nodeTitle);
		
		// delete the node
		courseRuntime
			.edit()
			.selectNode(nodeTitle)
			.deleteElement()
			.autoPublish();
		
		courseRuntime
			.clickTree();

		// make sure the course element is deleted
		By linkBy = By.xpath("//a[span[contains(text(),'" + nodeTitle + "')]]");
		OOGraphene.waitElementDisappears(linkBy, 5, browser);
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
	public void courseAccessRules(@Drone @Student WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("rei");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course FO " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
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
			.selectConfigurationWithRubric()
			.setScore(0.0f, 6.0f, 4.0f)
			.save();
		
		//wiki is assessment dependent
		courseEditor
			.createNode("info")
			.nodeTitle(infoTitle)
			.selectTabVisibility()
			.setAssessmentCondition(1)
			.save();
		
		OOGraphene.scrollTop(browser);
		courseEditor
			.publish()
			.nextSelectNodes()
			.selectAccess(UserAccess.membersOnly)
			.nextAccess()
			.selectCatalog(false)
			.nextCatalog() // -> no problem found
			.finish();
		courseEditor
			.clickToolbarBack()
			.assertStatus(RepositoryEntryStatusEnum.published);
		
		//add a member to the group we create above
		MembersPage members = CoursePageFragment
			.getCourse(browser)
			.members();
		members
			.addMember()
			.searchMember(rei, true)
			.nextUsers()
			.nextOverview()
			.selectGroupAsParticipant(groupName)
			.nextPermissions()
			.finish();
		
		//participant search the course
		LoginPage.load(reiBrowser, deploymentUrl)
			.loginAs(rei)
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(reiBrowser);
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
		AssessmentToolPage assessmentTool = members
			.clickToolbarBack()
			.assessmentTool();
		assessmentTool
			.users()
			.assertOnUsers(rei)
			.selectUser(rei)
			.selectUsersCourseNode(msTitle.substring(0, 20))
			.setAssessmentScore(5.5f)
			.closeAndPublishAssessment()
			.assertUserPassedCourseNode(msTitle.substring(0, 20));
		
		//student can see info
		reiTree
			.selectRoot()
			.assertWithTitle(bcTitle.substring(0, 20))
			.assertWithTitle(msTitle.substring(0, 20))
			.assertTitleNotExists(foTitle.substring(0, 20))
			.assertWithTitle(infoTitle.substring(0, 20));
		
		//author can see all
		assessmentTool
			.clickToolbarRootCrumb()
			.tree()
			.assertWithTitle(bcTitle.substring(0, 20))
			.assertWithTitle(msTitle.substring(0, 20))
			.assertWithTitle(foTitle.substring(0, 20))
			.assertWithTitle(infoTitle.substring(0, 20));
	}
	
	/**
	 *  First, an administrator make in administration part
	 * the confirmation of group's membership mandatory if
	 * the group is created by an author.<br>
	 * 
	 * An author create a course and a group and add two
	 * participants. The first user jump to the course
	 * with a rest url, log in, confirm its membership
	 * and see the course.<br>
	 * The second participant log-in, confirm its membership,
	 * go the "My courses" and visit the course.
	 * 
	 * @param loginPage
	 * @param authorBrowser
	 * @param participantBrowser
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void confirmMembershipForCourse(@Drone @Author WebDriver authorBrowser,
			@Drone @Participant WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser();
		
		//admin make the confirmation of membership mandatory
		//for groups created by standard users.
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		AdministrationPage administration = NavigationPage.load(browser)
			.openAdministration()
			.openGroupSettings()
			.setGroupConfirmationForAuthor(true);
		
		//author create a course
		String courseTitle = "Membership " + UUID.randomUUID();
		LoginPage.load(authorBrowser, deploymentUrl)
			.loginAs(author)
			.resume();
		NavigationPage authorNavBar = NavigationPage.load(authorBrowser);
		authorNavBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		String groupName = "Groupship " + UUID.randomUUID();
		MembersPage members = CoursePageFragment
			.getCourse(authorBrowser)
			.members();
		//create a group
		members
			.selectBusinessGroups()
			.createBusinessGroup(groupName, "-", 1, false, false);
		//return to course
		authorNavBar.openCourse(courseTitle);
		//add the 2 participants to the group
		members
			.selectMembers()
			.addMember()
			.searchMember(rei, true)
			.nextUsers()
			.nextOverview()
			.selectGroupAsParticipant(groupName)
			.nextPermissions()
			.finish();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.selectGroupAsParticipant(groupName)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		//set the course for members only
		CoursePageFragment course = CoursePageFragment
			.getCourse(authorBrowser);
		course
			.publish();
		course
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		
		String courseUrl = authorBrowser.getCurrentUrl();

		//rest url -> login -> accept membership
		participantBrowser.get(courseUrl);
		new LoginPage(participantBrowser)
			.loginAs(rei.getLogin(), rei.getPassword())
			.assertOnMembershipConfirmation()
			.confirmMembership();
		new CoursePageFragment(participantBrowser)
			.assertOnCoursePage()
			.assertOnTitle(courseTitle);
		
		//participant login -> accept membership -> my courses -> course
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.assertOnMembershipConfirmation()
			.confirmMembership();
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		new CoursePageFragment(participantBrowser)
			.assertOnCoursePage()
			.assertOnTitle(courseTitle);
		
		//reset the settings
		administration.setGroupConfirmationForAuthor(false);	
	}
	
	/**
	 * An author creates a CP, changes the name of a page, import 
	 * an other page, create two pages and delete one, import an
	 * image...<br>
	 * A user come to see the CP, check that the deleted page is
	 * really deleted and that the "Small HTML page" exists and
	 * can be read.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createContentPackage()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a CP
		String cpTitle = "CP " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCP(cpTitle)
			.back();
		
		String firstPage = "Page 1 " + UUID.randomUUID();
		String secondPage = "Seite 2 " + UUID.randomUUID();
		String thirdPage = "Feuillet 3 " + UUID.randomUUID();
		String deletedPage = "To delete 4 " + UUID.randomUUID();
		
		URL pageUrl = JunitTestHelper.class.getResource("file_resources/page.html");
		File pageFile = new File(pageUrl.toURI());
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1482.JPG");
		File imageFile = new File(imageUrl.toURI());
		
		CPPage cpPage = new CPPage(browser);
		cpPage
			.openEditor()
			//rename page
			.editMetadata(firstPage)
			.clickRoot()
			.newPage(secondPage)
			.clickRoot()
			.newPage(deletedPage)
			.clickRoot()
			//import a page and change the title
			.importPage(pageFile)
			.fillMetadataForm(thirdPage)
			//import an image without changing the title
			.importPage(imageFile)
			.closeMetadataForm()
			//delete page
			.selectPage(deletedPage)
			.deletePage()
			//close the editor
			.clickToolbarBack();
		
		//set access to registered members
		cpPage
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		//publish
		cpPage
			.publish();
		
		//a user search the content package
		LoginPage ryomouLoginPage = LoginPage.load(browser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(cpTitle)
			.select(cpTitle)
			.start();
		
		CPPage ryomouPage = new CPPage(browser);
		ryomouPage
			.assertPageDeleted(deletedPage)
			.assertInIFrame(By.xpath("//h1[text()[contains(.,'Small HTML page')]]"))
			.selectPage(secondPage)
			.selectPage(firstPage)
			.assertInIFrame(By.xpath("//h2[text()[contains(.,'Lorem Ipsum')]]"));
	}
	
	/**
	 * Add an owner to a course directly from the author's list, and after
	 * remove itself as an owner of the course.
	 */
	@Test
	@RunAsClient
	public void modifyOwnerCourseBatch()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Lisa");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		String title = "Owner-Course-Wizard-" + UUID.randomUUID().toString();
		navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment()
			// Create course
			.createCourse(title, true)
			.clickToolbarBack();
		
		AuthoringEnvPage authoringEnv = navBar
			.openAuthoringEnvironment();
		// First add a new owner
		authoringEnv
			.selectResource(title)
			.changeOwner()
			.nextRemoveOwners()
			.searchUserAndAdd(user, true)
			.assertAddOwner(user)
			.nextReview()
			.finish();
		
		// Remove itself
		authoringEnv
			.selectResource(title)
			.changeOwner()
			.removeOwner(author)
			.nextRemoveOwners()
			.nextAddOwners()
			.assertRemoveOwner(author)
			.nextReview()
			.finish();

		//Participant log in
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		// My course -> In preparation
		NavigationPage userNavBar = NavigationPage.load(browser);
		userNavBar
			.openMyCourses()
			.openInPreparation()
			.select(title);

		CoursePageFragment userCourse = new CoursePageFragment(browser);
		userCourse
			.assertOnCoursePage()
			.edit()
			.assertOnEditor();
	}
	
	/**
	 * Modify the status of a course directly in the author's list.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void modifyStatusCourseBatch()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		String title = "Status-Course-" + UUID.randomUUID().toString();
		navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment()
			// Create course
			.createCourse(title, true)
			.clickToolbarBack()
			.closeCourse();
		
		AuthoringEnvPage authoringEnv = navBar
			.openAuthoringEnvironment();
		// First add a new owner
		authoringEnv
			.selectResource(title)
			.modifyStatus(RepositoryEntryStatusEnum.published)
			.assertOnStatus(title, RepositoryEntryStatusEnum.published);
		
		authoringEnv
			.selectResource(title)
			.modifyStatus(RepositoryEntryStatusEnum.closed)
			.assertOnStatus(title, RepositoryEntryStatusEnum.closed)
			.openResource(title);
	
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.assertOnTitle(title)
			.assertOnMessage()
			.assertStatus(RepositoryEntryStatusEnum.closed);
	}
	
	/**
	 * Try to import a typical Windows zip with encoding issues. The goal
	 * is to check that no ugly red screen are produced.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void tryImportOfWindowsZip()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		URL zipUrl = JunitTestHelper.class.getResource("file_resources/windows_zip.zip");
		File zipFile = new File(zipUrl.toURI());
		//go the authoring environment to create a CP
		String zipTitle = "ZIP - " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(zipTitle, zipFile)
			.assertOnResourceType();
	}
	
}
