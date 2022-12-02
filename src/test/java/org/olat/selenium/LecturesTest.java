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
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseSettingsPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.lecture.LectureRepositoryAdminListPage;
import org.olat.selenium.page.lecture.LectureRepositoryAdminPage;
import org.olat.selenium.page.lecture.LectureRepositoryParticipantsPage;
import org.olat.selenium.page.lecture.LecturesRepositoryPage;
import org.olat.selenium.page.lecture.RollCallInterceptorPage;
import org.olat.selenium.page.lecture.TeacherRollCallPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * Test suite for the lectures / absence management feature.
 * 
 * Initial date: 7 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class LecturesTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	/**
	 * An author create a course, enable the absence management,
	 * create a lecture block, add a coach and two participants.<br>
	 * The coach login in, see the interceptor to start the roll call.
	 * It starts the roll call, set an absence and close.<br>
	 * The participant with an absence log in, use the lectures user's
	 * tool to see that it has an absence.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void lecturesRollCall_authorizedAbsence(@Drone @User WebDriver coachBrowser,
			@Drone @Participant WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		 NavigationPage.load(browser)
			.openAdministration()
			.openLecturesSettings()
			.configure(true, true, true, true, false)
			.save()
			.configurePermissions(false)
			.savePermissions();
		
		// start the test with authorized absence on
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO coach = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Rymou");

		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Lecture " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//set access
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		course
			.publish();
		
		//add a coach
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(coach, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		//add the participants
		members
			.addMember()
			.importList()
			.setMembers(participant1, participant2)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();

		//enable the lectures
		CourseSettingsPage settings = course
			.settings();
		settings
			.lecturesConfiguration()
			.enableLectures()
			.overrideDefaultSettings()
			.saveSettings();
		settings
			.clickToolbarBack();
		
		LectureRepositoryAdminListPage lectureList = course
			.lecturesAdministration()
			.lectureList();
		
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int endHour = hour < 23 ? hour + 1 : hour;
		String lectureTitle = "1. Lecture";
		lectureList
			.newLectureBlock()
			.setTitle(lectureTitle)
			.setTeacher(coach)
			.setDate(today, hour, 0, endHour, 59)
			.save();
		
		//coach at work
		LoginPage coachLoginPage = LoginPage.load(coachBrowser, deploymentUrl);
		coachLoginPage
			.loginAs(coach);
		new RollCallInterceptorPage(coachBrowser)
			.start()
			.setAbsence(participant1, "1")
			.closeRollCall()
			.confirmCloseRollCall()
			.assertOnClosedTable();
		
		//participant check it roll call
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant1)
			.resume();
		UserToolsPage participantUserTools = new UserToolsPage(participantBrowser);
		participantUserTools
			.openUserToolsMenu()
			.openLectures()
			.assertDailyOverview()
			.openLecturesAndAbsences()
			.assertOnParticipantLecturesList()
			.selectCourseAsParticipant(title)
			.assertOnParticipantLectureBlocks()
			.assertOnParticipantLectureBlockAbsent(coach, lectureTitle, title);
	}
	

	/**
	 * An author create a course, enable the absence management,
	 * create a lecture block, add a coach and two participants.<br>
	 * The coach login in, see the interceptor to start the roll call
	 * version mobile.<br>
	 * It starts the roll call, set an absence and close.<br>
	 * The participant with an absence log in, use the lectures user's
	 * tool to see that it has an absence.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void lectureMobileRollCall_authorizedAbsence(@Drone @User WebDriver coachBrowser,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage.load(browser)
			.openAdministration()
			.openLecturesSettings()
			.configure(true, true, true, true, false)
			.save()
			.configurePermissions(false)
			.savePermissions();
		
		// start the test with authorized absence on
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO coach = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Rymou");
		
		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Lecture " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//set access
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		course
			.publish();
		
		//add a coach
		MembersPage members = course
			.members();
		members
			.addMember()	
			.searchMember(coach, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		//add the participants
		members
			.addMember()
			.importList()
			.setMembers(participant1, participant2)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//enable the lectures
		CourseSettingsPage settings = course
			.settings();
		settings
			.lecturesConfiguration()
			.enableLectures()
			.overrideDefaultSettings()
			.saveSettings();
		settings
			.clickToolbarBack();
		
		LectureRepositoryAdminListPage lectureList = course
			.lecturesAdministration()
			.lectureList();
		
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int endHour = hour < 23 ? hour + 1 : hour;
		String lectureTitle = "2.Lecture";
		lectureList
			.newLectureBlock()
			.setTitle(lectureTitle)
			.setTeacher(coach)
			.setDate(today, hour, 0, endHour, 59)
			.save();
		
		//coach at work
		LoginPage coachLoginPage = LoginPage.load(coachBrowser, deploymentUrl);
		coachLoginPage
			.loginAs(coach);
		new RollCallInterceptorPage(coachBrowser)
			.startMobile()
			.setAbsence("1")
			.saveAndNext()
			.setAbsence("1")
			.setAbsence("2")
			.saveAndNext()
			.closeRollCall();
		//check that a roll call at least is closed
		new TeacherRollCallPage(coachBrowser)
			.assertOnClosedTable();
		
		//participant check it roll call
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant1)
			.resume();
		UserToolsPage participantUserTools = new UserToolsPage(participantBrowser);
		participantUserTools
			.openUserToolsMenu()
			.openLectures()
			.assertDailyOverview()
			.openLecturesAndAbsences()
			.assertOnParticipantLecturesList()
			.selectCourseAsParticipant(title)
			.assertOnParticipantLectureBlocks()
			.assertOnParticipantLectureBlockAbsent(coach, lectureTitle, title);
	}
	
	
	/**
	 * An author create a course to use the absence management
	 * without authorized absence.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void lecturesRollCall()
	throws IOException, URISyntaxException {

		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage.load(browser)
			.openAdministration()
			.openLecturesSettings()
			.configure(true, true, true, false, false)
			.save();
		
		//start
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Rymou");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Lecture " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();

		CoursePageFragment course = new CoursePageFragment(browser);
		//add a coach
		MembersPage members = course
			.members();
		members
			.quickAdd(author, true, true);
		//add the participants
		members
			.quickImport(participant1, participant2);
		
		//enable the lectures
		CourseSettingsPage settings = course
			.settings();
		settings
			.lecturesConfiguration()
			.enableLectures()
			.overrideDefaultSettings()
			.saveSettings();
		settings
			.clickToolbarBack();
		
		//add a lecture
		LectureRepositoryAdminPage lecturesAdmin = course
			.lecturesAdministration();
		LectureRepositoryAdminListPage lectureList = lecturesAdmin
			.lectureList();
		
		String lectureTitle = "2.Lecture";
		lectureList
			.newLectureBlock()
			.setTitle(lectureTitle)
			.setTeacher(author)
			.setDateOneHourBefore()
			.save();
		
		//go to the lectures list as "teacher"
		LecturesRepositoryPage teachersLectures = lecturesAdmin
			.clickToolbarRootCrumb()
			.lectures();
		teachersLectures
			.openRollCall(lectureTitle)
			.setAbsence(participant1, "1")
			.setAbsence(participant1, "2")
			.setAbsence(participant1, "3")
			.closeRollCall()
			.confirmCloseRollCall()
			.assertOnClosedTable();
		
		//go to the lecture administration in the course
		lecturesAdmin = teachersLectures
			.clickToolbarRootCrumb()
			.lecturesAdministration();
		//edit the first admission to see some result
		LectureRepositoryParticipantsPage participantsAdmin = lecturesAdmin
			.participantList()
			.editParticipant(participant1)
			.firstAdmissionBack()
			.saveParticipant()
			.editParticipant(participant2)
			.firstAdmissionBack()
			.saveParticipant();
		//check
		participantsAdmin
			.assertOnParticipantLectureBlockAbsent(participant1, 3)
			.assertOnParticipantLectureBlockAbsent(participant2, 0);
	}
	
	/**
	 * An author create a course, enable the absence management,
	 * create a lecture block, add a coach and two participants.<br>
	 * The coach login in, see the interceptor to start the roll call.
	 * It starts the roll call, set an absence and close.<br>
	 * The participant with an absence log in, use the lectures user's
	 * tool to see that it has an absence.<br>
	 * The absence management has the following settings: holding partial
	 * lecture is disable, cancel status of lectures is disabled, authorized
	 * absence are enabled with them as default authorized, teachers can
	 * authorized absences.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void lecturesRollCall_defaultAuthorizedAbsence()
	throws IOException, URISyntaxException {
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		NavigationPage.load(browser)
			.openAdministration()
			.openLecturesSettings()
			.configure(true, false, false, true, true)
			.save()
			.configurePermissions(true)
			.savePermissions();
		
		// start the test with authorized absence on
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Rymou");

		LoginPage
			.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Lecture " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//set access
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		course
			.publish();
		
		//add a coach
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(author, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		//add the participants
		members
			.addMember()
			.importList()
			.setMembers(participant1, participant2)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//enable the lectures
		CourseSettingsPage settings = course
			.settings();
		settings
			.lecturesConfiguration()
			.enableLectures()
			.overrideDefaultSettings()
			.saveSettings();
		settings
			.clickToolbarBack();

		LectureRepositoryAdminListPage lectureList = course
			.lecturesAdministration()
			.lectureList();
		
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int endHour = hour < 23 ? hour + 1 : hour;
		String lectureTitle = "1. Lecture";
		lectureList
			.newLectureBlock()
			.setTitle(lectureTitle)
			.setTeacher(author)
			.setDate(today, hour, 0, endHour, 59)
			.save();
		
		//coach at work
		LoginPage coachLoginPage = LoginPage.load(browser, deploymentUrl);
		coachLoginPage
			.loginAs(author);
		new RollCallInterceptorPage(browser)
			.start()
			.setAbsence(participant1, "1")
			.closeRollCall()
			.confirmCloseRollCall()
			.assertOnClosedTable();
		
		//participant check it roll call
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant1)
			.resume();
		UserToolsPage participantUserTools = new UserToolsPage(browser);
		participantUserTools
			.openUserToolsMenu()
			.openLectures()
			.assertDailyOverview()
			.openLecturesAndAbsences()
			.assertOnParticipantLecturesList()
			.selectCourseAsParticipant(title)
			.assertOnParticipantLectureBlocks()
			.assertOnParticipantLectureBlockAuthorised(author, lectureTitle, title);
	}
	
	/**
	 * An author create a course to use the absence management
	 * and import some lectures blocks.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void importLectures()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Lecture " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		CoursePageFragment course = new CoursePageFragment(browser);
		//enable the lectures
		CourseSettingsPage settings = course
			.settings();
		settings
			.lecturesConfiguration()
			.enableLectures()
			.overrideDefaultSettings()
			.saveSettings();
		settings
			.clickToolbarBack();
		
		LectureRepositoryAdminListPage lectureList = course
			.lecturesAdministration()
			.lectureList();
		
		// import the lecture blocks
		lectureList
				.importLecturesBlocks()
				.importFile("lectures/import_lectures.txt")
				.next()
				.assertOnNumberOfNewBlocks(3)
				.finish();
		
		//check the blocks
		lectureList
			.assertOnLectureBlock("Abschlussparty")
			.assertOnLectureBlock("Excel Workshop")
			.assertOnLectureBlock("Mathematik Einf");
	}
}
