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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.core.CalendarPage;
import org.olat.selenium.page.core.IMPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.EnrollmentConfigurationPage;
import org.olat.selenium.page.course.EnrollmentPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupPage;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class BusinessGroupTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;

	/**
	 * Create a group, search it and delete it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createDeleteBusinessGroup()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Delete-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group to delete");
		
		//return to group list and delete it
		navBar
			.openGroups(browser)
			.deleteGroup(groupName)
			.assertDeleted(groupName);
	}

	/**
	 * An author create a group, set the visibility to
	 * show owners and participants. Add a member to the
	 * group.
	 * 
	 * The participant log in, search the group and open it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupMembersVisibility(@Drone @Participant WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Aoi");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group");
		
		MembersWizardPage members = group
			.openAdministration()
			.openAdminMembers()
			.setVisibility(true, true, false)
			.addMember();
		
		members.searchMember(participant, false)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
				.openGroups(participantBrowser)
				.selectGroup(groupName);
		
		WebElement contentEl = participantBrowser.findElement(By.id("o_main_center_content_inner"));
		String content = contentEl.getText();
		Assert.assertTrue(content.contains(groupName));
	}
	
	/**
	 * Configure group tools: create a group, go to administration > tools
	 * select the informations for members and write some message. Select
	 * all tools: contact, calendar, folder, forum, chat, wiki and portfolio.<br>
	 * 
	 * Check that all these functions are available.
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void collaborativeTools()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group");
		
		group
			.openAdministration()
			.openAdminTools()
			.enableTools();
		
		//check the news
		group
			.openNews()
			.createMessage()
			.setMessage("Information 0", "A very important info")
			.next()
			.finish()
		.	assertOnMessageTitle("Information 0");
		
		//check calendar
		group
			.openCalendar()
			.assertOnCalendar();
		
		//check members @see other selenium test dedicated to this one

		//check contact
		group
			.openContact()
			.assertOnContact();
		
		//check folder
		String directoryName = "New directory";
		group
			.openFolder()
			.assertOnFolderCmp()
			.createDirectory(directoryName)
			.assertOnDirectory(directoryName)
			.createHTMLFile("New file", "Some really cool content.")
			.assertOnFile("New file.html");
		
		//check forum
		String threadBodyMarker = UUID.randomUUID().toString();
		group
			.openForum()
			.createThread("New thread in a group", "Very interessant discussion in a group" + threadBodyMarker, null)
			.assertMessageBody(threadBodyMarker);
		
		//check chat @see other selenium test dedicated to this one
		
		//check wiki
		String wikiMarker = UUID.randomUUID().toString();
		group
			.openWiki()
			.createPage("Group page", "Content for the group's wiki " + wikiMarker)
			.assertOnContent(wikiMarker);
		
		//check portfolio
		String pageTitle = "Portfolio page " + UUID.randomUUID();
		String sectionTitle = "Section " + UUID.randomUUID();
		group
			.openPortfolio()
			.assertOnBinder()
			.selectTableOfContent()
			.selectEntries()
			.createSection(sectionTitle)
			.assertOnSectionTitleInEntries(sectionTitle)
			.createEntry(pageTitle)
			.selectEntries()
			.assertOnPageInEntries(pageTitle)
			.selectTableOfContent()
			.assertOnPageInToc(pageTitle);
	}
	
	/**
	 * An author creates a group, it opens the tab groups and then "My groups". It
	 * creates a group, enters a number of participants "1", enable the waiting
	 * list. In members visibility, it see coaches, participants and waiting
	 * list visible to members.<br>
	 * A participant and than a student come, book the group. The first enters
	 * the group, the second the waiting list.<br>
	 * The author go in the members list to check if it's in the coach list,
	 * the participant in the participants list and the student in the waiting
	 * list.
	 * 
	 * Should show group starting page, with menu items Administration and Bookings visible
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createGroupWithWaitingList(@Drone @Participant WebDriver participantBrowser,
			@Drone @Student WebDriver studentBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("Asuka");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A group with a waiting list")
			.openAdministration()
			//set waiting list and 1 participant
			.openEditDetails()
			.setMaxNumberOfParticipants(1)
			.setWaitingList()
			.saveDetails();
		
		//add booking ( token one )
		String token = "secret";
		String description = "The password is secret";
		group.openBookingConfig()
			.openAddDropMenu()
			.addTokenMethod()
			.configureTokenMethod(token, description);
		
		//members see members
		group = GroupPage.getGroup(browser)
			.openAdminMembers()
			.setVisibility(true, true, true)
			.openMembers();
		

		//participant search published groups
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		//groups
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
				.openGroups(participantBrowser)
				.publishedGroups()
				.bookGroup(groupName)
				.bookToken(token);
		//are we that we are in the right group?
		GroupPage.getGroup(participantBrowser)
			.assertOnInfosPage(groupName);
		
		
		//student search published groups
		LoginPage studentLoginPage = LoginPage.load(studentBrowser, deploymentUrl);
		//tools
		studentLoginPage
			.loginAs(student.getLogin(), student.getPassword())
			.resume();
		//groups
		NavigationPage studentNavBar = NavigationPage.load(studentBrowser);
		studentNavBar
				.openGroups(studentBrowser)
				.publishedGroups()
				.bookGroup(groupName)
				.bookToken(token);
		//are we that we are in the right group?
		GroupPage.getGroup(studentBrowser)
			.assertOnWaitingList(groupName);
		
		group = GroupPage.getGroup(browser)
				.openMembers()
				.assertMembersInOwnerList(author)
				.assertMembersInParticipantList(participant)
				.assertMembersInWaitingList(student);
	}
	
	/**
	 * First, an administrator make in administration part
	 * the confirmation of group's membership mandatory if
	 * the group is created by a standard user.<br>
	 * 
	 * A standard user create a group and add a participant.
	 * The participant log-in and confirm its membership and
	 * visit the group.<br>
	 * 
	 * A first user log in, confirm the membership and search
	 * the group.<br>
	 * 
	 * A second user log in but with a rest url to the group
	 * and jump to the group after confirming the membership.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void confirmMembershipByGroup(@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver participantBrowser,
			@Drone @Student WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
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
			.setGroupConfirmationForUser(true);
		
		//a standard user create a group
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		NavigationPage rymouNavBar = NavigationPage.load(ryomouBrowser);
		GroupPage group = rymouNavBar
			.openGroups(ryomouBrowser)
			.createGroup(groupName, "Confirmation group");
		
		String groupUrl = group
			.openAdministration()
			.getGroupURL();
		
		group.openAdminMembers()
			.addMember()
			.searchMember(participant, false)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		group.addMember()
			.searchMember(rei, false)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//participant login
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.assertOnMembershipConfirmation()
			.confirmMembership();
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openGroups(participantBrowser)
			.selectGroup(groupName)
			.assertOnInfosPage(groupName);
		
		//second participant log in with rest url
		reiBrowser.get(groupUrl);
		new LoginPage(reiBrowser)
			.loginAs(rei.getLogin(), rei.getPassword())
			.assertOnMembershipConfirmation()
			.confirmMembership();
		NavigationPage reiNavBar = NavigationPage.load(reiBrowser);
		reiNavBar
			.openGroups(reiBrowser)
			.selectGroup(groupName)
			.assertOnInfosPage(groupName);
		
		//reset the settings
		administration.setGroupConfirmationForUser(false);
	}
	
	/**
	 * An author create a group, set the visibility to true for owners
	 * and participants, enable the tools and add 2 users to it. The 2
	 * users joins the chat. All three send some messages and read them.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupChat(@Drone @Participant WebDriver kanuBrowser,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-Chat-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group to chat");
		
		group
			.openAdministration()
			.openAdminTools()
			.enableTools()
			.openAdminMembers()
			.setVisibility(true, true, false);
		//add Kanu to the group
		group
			.openAdminMembers()
			.addMember()
			.searchMember(kanu, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		//add Ryomou
		group.addMember()
			.searchMember(ryomou, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Kanu open the group
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();
		
		NavigationPage kanuNavBar = NavigationPage.load(kanuBrowser);
		GroupPage kanuGroup = kanuNavBar
			.openGroups(kanuBrowser)
			.selectGroup(groupName);
		
		//Ryomou open the group
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		IMPage ryomouIM = ryomouNavBar
			.openGroups(ryomouBrowser)
			.selectGroup(groupName)
			.openChat()
			.openGroupChat();
		
		//Author send a message to Kanu
		String msg1 = "Hello Kanu " + UUID.randomUUID();
		IMPage authorIM = group
			.openChat()
			.openGroupChat()
			.sendMessage(msg1)
			.assertOnMessage(msg1);
		
		String msg2 = "Hello dear author " + UUID.randomUUID();
		//Kanu opens her chat window
		IMPage kanuIM = kanuGroup
			.openChat()
			.openGroupChat()
			.assertOnMessage(msg1)
			.sendMessage(msg2);
		
		String msg3 = "Hello Kanu and author " + UUID.randomUUID();
		//Ryomou reads her messages
		ryomouIM
			.sendMessage(msg3)
			.assertOnMessage(msg1)
			.assertOnMessage(msg2);
		//Kanu reads her message
		kanuIM
			.assertOnMessage(msg3);
		//Author reads too
		authorIM
			.assertOnMessage(msg2)
			.assertOnMessage(msg3);
	}
	

	/**
	 * A coach create a group, enable the calendar, create an event
	 * and save it. Reopen it, edit it and save it.
	 * 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupCalendar_addEditEvent()
	throws IOException, URISyntaxException {
		UserVO coach = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(coach.getLogin(), coach.getPassword())
			.resume();
		
		//go to groups
		String groupName = "iCal-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group to delete");
		
		group
			.openAdministration()
			.openAdminTools()
			.enableCalendarTool();
		
		//add an event to the calendar
		CalendarPage calendar = group
			.openCalendar()
			.assertOnCalendar()
			.addEvent(2)
			.setDescription("Hello", "Very important event", "here or there")
			.save()
			.assertOnEvent("Hello");
		//edit the event
		calendar
			.openDetails("Hello")
			.edit()
			.setDescription("Bye", null, null)
			.save();
		//check the changes
		calendar
			.assertOnEvent("Bye");
	}
	
	/**
	 * A coach create a group, enable the calendar, create a recurring event
	 * and save it. Reopen it, edit it and save it, confirm that it will
	 * only change a single occurrence of the recurring event. After change
	 * the begin and end hour of all others events.
	 * 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupCalendar_recurringEvent()
	throws IOException, URISyntaxException {
		UserVO coach = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(coach.getLogin(), coach.getPassword())
			.resume();
		
		//go to groups
		String groupName = "iCal-2-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "Calendar with a recurring event");
		
		group
			.openAdministration()
			.openAdminTools()
			.enableCalendarTool();
		
		int startdDate = 2;
		//add an event to the calendar
		CalendarPage calendar = group
			.openCalendar()
			.assertOnCalendar()
			.addEvent(startdDate)
			.setDescription("Recurring", "Very important event 4-5 times", "In the way")
			.setAllDay(false)
			.setBeginEnd(10, 11)
			.setRecurringEvent(KalendarEvent.WEEKLY, 28)
			.save()
			.assertOnEvents("Recurring", 4);
		
		//pick an occurence of the recurring event and modify it
		calendar
			.openDetailsOccurence("Recurring", 9)
			.edit()
			.setDescription("Special", null, null)
			.save()
			.confirmModifyOneOccurence()
			.assertOnEvents("Special", 1)
			.assertOnEvents("Recurring", 3);
		
		//pick the first occurence and change all events but the modified above
		calendar
			.openDetailsOccurence("Recurring", 2)
			.edit()
			.setBeginEnd(11, 12).assertOnEvents("Special", 1)
			.save()
			.confirmModifyAllOccurences()
			.assertOnEventsAt("Recurring", 3, 11);
	}
	
	/**
	 * An author create a course, with an enrollment course element. It
	 * configure it and create a group with max. participant set to 1 and
	 * enables the waiting list.<br>
	 * 
	 * Three users goes to the course and try to enroll. One will become
	 * a participant, the 2 others land in the waiting list.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollmentWithWaitingList(@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//create a course
		String courseTitle = "Enrolment-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Enrolment
		String enNodeTitle = "Enrol-1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("en")
			.nodeTitle(enNodeTitle);
		//configure enrolment with a group that we create
		String groupName = "Enrolment group - 1 " + UUID.randomUUID();
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.createBusinessGroup(groupName, "-", 1, true, false);
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.registred);
		courseEditor.clickToolbarBack();
		
		GroupPage authorGroup = navBar
			.openGroups(browser)
			.selectGroup(groupName)
			.openAdministration()
			.openAdminMembers()
			.setVisibility(true, true, true)
			.openMembers()
			.assertParticipantList();
		
		//Rei open the course
		Enrollment[] participantDrivers = new Enrollment[]{
				new Enrollment(ryomou, ryomouBrowser),
				new Enrollment(rei, reiBrowser),
				new Enrollment(kanu, kanuBrowser)
		};
		for(Enrollment enrollment:participantDrivers) {
			WebDriver driver = enrollment.getDriver();
			LoginPage.load(driver, deploymentUrl)
				.loginAs(enrollment.getUser())
				.resume();
			
			NavigationPage participantNavBar = NavigationPage.load(driver);
			participantNavBar
				.openMyCourses()
				.openSearch()
				.extendedSearch(courseTitle)
				.select(courseTitle)
				.start();
			
			//go to the enrollment
			CoursePageFragment participantCourse = new CoursePageFragment(driver);
			participantCourse
				.clickTree()
				.selectWithTitle(enNodeTitle);
		
			EnrollmentPage enrollmentPage = new EnrollmentPage(driver);
			enrollmentPage
				.assertOnEnrolmentPage();
			enrollment.setEnrollmentPage(enrollmentPage);
		}
		
		//enroll
		for(Enrollment enrollment:participantDrivers) {
			enrollment.getEnrollmentPage().enrollNoWait();
		}
		//wait
		for(Enrollment enrollment:participantDrivers) {
			OOGraphene.waitBusy(enrollment.getDriver());
		}
		
		//author check the lists
		authorGroup.openMembers();
		//must a participant and 2 in waiting list
		int participants = 0;
		int waitingList = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(authorGroup.isInMembersParticipantList(enrollment.getUser()))  participants++;
			if(authorGroup.isInMembersInWaitingList(enrollment.getUser())) waitingList++;
		}
		Assert.assertEquals(1, participants);
		Assert.assertEquals(2, waitingList);
	}
	
	/**
	 * An author create a course, with an enrollment course element. It
	 * configure it and create 3 groups and set the maximum enrollment counter to 2<br>
	 * 
	 * One user goes to the course and enrolls in 2 of the groups. It shouldent be possible
	 * enroll in the third<br>
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollmentWithMultiEnrollment(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//create a course
		String courseTitle = "Enrolment-3-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Enrolment
		String enNodeTitle = "Enrol-3";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("en")
			.nodeTitle(enNodeTitle);
		//configure enrolment with a group that we create
		List<String> groups = new ArrayList<>();
		groups.add("Enrolment group - 3 " + UUID.randomUUID());
		groups.add("Enrolment group - 3 " + UUID.randomUUID());
		groups.add("Enrolment group - 3 " + UUID.randomUUID());
		
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.createBusinessGroup(groups.get(0), "-", 4, false, false)
			.createBusinessGroup(groups.get(1), "-", 4, false, false)
			.createBusinessGroup(groups.get(2), "-", 4, false, false)
			.selectMultipleEnrollments(2);
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.registred);
		courseEditor.clickToolbarBack();
		
		for(String groupName:groups){
			navBar
				.openGroups(browser)
				.selectGroup(groupName)
				.openAdministration()
				.openAdminMembers()
				.setVisibility(true, true, false)
				.openMembers();
		}
				
		//Ryomou open the course	
		LoginPage.load(ryomouBrowser, deploymentUrl)
			.loginAs(ryomou)
			.resume();
		
		NavigationPage participantNavBar = NavigationPage.load(ryomouBrowser);
		participantNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		
		OOGraphene.waitBusy(ryomouBrowser);
		
		//go to the enrollment
		CoursePageFragment participantCourse = new CoursePageFragment(ryomouBrowser);
		participantCourse
			.clickTree()
			.selectWithTitle(enNodeTitle);
		
		EnrollmentPage enrollmentPage = new EnrollmentPage(ryomouBrowser);
		enrollmentPage
			.assertOnEnrolmentPage()
			.multiEnroll(2);

		//assert that that no more enrollment is allowed
		enrollmentPage
			.assertNoEnrollmentAllowed();
	}
	
	
	/**
	 * An author create a course and a business group in the members
	 * management. It has max. participants set to 1 and no waiting list.
	 * Than it returns in the course editor to create an enrollment
	 * course element. It configure it and select the group created before.<br>
	 * 
	 * Three users goes to the course and try to enroll. One will become
	 * a participant, the 2 others get an error message.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollment(@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Enrollment-2-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a group in members management
		String groupName = "Enroll - " + UUID.randomUUID();
		CoursePageFragment authorCourse = CoursePageFragment.getCourse(browser);
		MembersPage membersPage = authorCourse
			.members()
			.selectBusinessGroups()
			.createBusinessGroup(groupName, "-", 1, false, false);
		//back to the members page
		navBar.openCourse(courseTitle);
		authorCourse = membersPage
			.clickToolbarBack();
		
		//create an enrollment course element
		String enNodeTitle = "Enroll - 2";
		CourseEditorPageFragment courseEditor = authorCourse
			.edit()
			.createNode("en")
			.nodeTitle(enNodeTitle);
		
		//select the group created above
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.selectBusinessGroups();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.registred);
		
		GroupPage authorGroup = navBar
			.openGroups(browser)
			.selectGroup(groupName)
			.openAdministration()
			.openAdminMembers()
			.setVisibility(false, true, false)
			.openMembers()
			.assertParticipantList();
		
		Enrollment[] participantDrivers = new Enrollment[]{
				new Enrollment(ryomou, ryomouBrowser),
				new Enrollment(rei, reiBrowser),
				new Enrollment(kanu, kanuBrowser)
		};
		for(Enrollment enrollment:participantDrivers) {
			WebDriver driver = enrollment.getDriver();
			LoginPage.load(driver, deploymentUrl)
				.loginAs(enrollment.getUser())
				.resume();
			
			NavigationPage participantNavBar = NavigationPage.load(driver);
			participantNavBar
				.openMyCourses()
				.openSearch()
				.extendedSearch(courseTitle)
				.select(courseTitle)
				.start();
			
			//go to the enrollment
			CoursePageFragment participantCourse = new CoursePageFragment(driver);
			participantCourse
				.clickTree()
				.selectWithTitle(enNodeTitle);
		
			EnrollmentPage enrollmentPage = new EnrollmentPage(driver);
			enrollmentPage
				.assertOnEnrolmentPage();
			enrollment.setEnrollmentPage(enrollmentPage);
		}
		
		//enroll
		for(Enrollment enrollment:participantDrivers) {
			enrollment.getEnrollmentPage().enrollNoWait();
		}
		//wait
		for(Enrollment enrollment:participantDrivers) {
			OOGraphene.waitBusy(enrollment.getDriver());
		}
		int errors = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(enrollment.getEnrollmentPage().hasError()) {
				errors++;
			}
		}
		
		//author check the lists
		authorGroup.openMembers();
		//must a participant and 2 in waiting list
		int participants = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(authorGroup.isInMembersParticipantList(enrollment.getUser())) {
				participants++;
			}
		}
		Assert.assertEquals(1, participants);
		Assert.assertEquals(participantDrivers.length - 1, errors);
	}
	
	/**
	 * Variant from the above test where the business group is not
	 * limited in size. This was a bug while development of the 10.3
	 * release.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollmentWithUnlimitedBusinessGroups(@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Enrollment-3-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a group in members management
		String groupName = "Enroll - " + UUID.randomUUID();
		CoursePageFragment authorCourse = CoursePageFragment.getCourse(browser);
		MembersPage membersPage = authorCourse
			.members()
			.selectBusinessGroups()
			.createBusinessGroup(groupName, "-", -1, false, false);
		//back to the members page
		navBar.openCourse(courseTitle);
		authorCourse = membersPage
			.clickToolbarBack();
		
		//create an enrollment course element
		String enNodeTitle = "Enroll - 3";
		CourseEditorPageFragment courseEditor = authorCourse
			.edit()
			.createNode("en")
			.nodeTitle(enNodeTitle);
		
		//select the group created above
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.selectBusinessGroups();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.registred);
		
		GroupPage authorGroup = navBar
			.openGroups(browser)
			.selectGroup(groupName)
			.openAdministration()
			.openAdminMembers()
			.setVisibility(false, true, false)
			.openMembers()
			.assertParticipantList();
		
		Enrollment[] participantDrivers = new Enrollment[]{
				new Enrollment(ryomou, ryomouBrowser),
				new Enrollment(rei, reiBrowser),
				new Enrollment(kanu, kanuBrowser)
		};
		for(Enrollment enrollment:participantDrivers) {
			WebDriver driver = enrollment.getDriver();
			LoginPage.load(driver, deploymentUrl)
				.loginAs(enrollment.getUser())
				.resume();
			
			NavigationPage participantNavBar = NavigationPage.load(driver);
			participantNavBar
				.openMyCourses()
				.openSearch()
				.extendedSearch(courseTitle)
				.select(courseTitle)
				.start();
			
			//go to the enrollment
			CoursePageFragment participantCourse = new CoursePageFragment(driver);
			participantCourse
				.clickTree()
				.selectWithTitle(enNodeTitle);
		
			EnrollmentPage enrollmentPage = new EnrollmentPage(driver);
			enrollmentPage
				.assertOnEnrolmentPage();
			enrollment.setEnrollmentPage(enrollmentPage);
		}
		
		//enroll
		for(Enrollment enrollment:participantDrivers) {
			enrollment.getEnrollmentPage().enrollNoWait();
		}
		//wait
		int errors = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(enrollment.getEnrollmentPage().hasError()) {
				errors++;
			}
		}
		
		//author check the lists
		authorGroup.openMembers();
		//must a participant and 2 in waiting list
		int participants = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(authorGroup.isInMembersParticipantList(enrollment.getUser())) {
				participants++;
			}
		}
		Assert.assertEquals(3, participants);
		Assert.assertEquals(0, errors);
	}
	
	public static class Enrollment {
		
		private final UserVO user;
		private final WebDriver driver;
		private EnrollmentPage enrollmentPage;
		
		public Enrollment(UserVO user, WebDriver driver) {
			this.user = user;
			this.driver = driver;
		}

		public UserVO getUser() {
			return user;
		}

		public WebDriver getDriver() {
			return driver;
		}

		public EnrollmentPage getEnrollmentPage() {
			return enrollmentPage;
		}

		public void setEnrollmentPage(EnrollmentPage enrollmentPage) {
			this.enrollmentPage = enrollmentPage;
		}
	}
}
