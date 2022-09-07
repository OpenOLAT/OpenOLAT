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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupsPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.tracing.ContactTracingAdminPage;
import org.olat.selenium.page.tracing.ContactTracingPage;
import org.olat.selenium.page.user.ImportUserPage;
import org.olat.selenium.page.user.PortalPage;
import org.olat.selenium.page.user.UserAdminPage;
import org.olat.selenium.page.user.UserPasswordPage;
import org.olat.selenium.page.user.UserPreferencesPageFragment;
import org.olat.selenium.page.user.UserPreferencesPageFragment.ResumeOption;
import org.olat.selenium.page.user.UserProfilePage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.selenium.page.user.VisitingCardPage;
import org.olat.test.rest.RepositoryRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class UserTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;

	
	/**
	 * Set the resume preferences to automatically resume the session,
	 * open a course, log out, log in and check if the course is resumed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void resumeCourseAutomatically()
	throws IOException, URISyntaxException {
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		//deploy a course
		CourseVO newCourse = new RepositoryRestClient(deploymentUrl).deployDemoCourse();
		
		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.searchResource(newCourse.getKey().toString())
			.selectResource(newCourse.getTitle());
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser();

		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		
		//set the preferences to resume automatically
		UserToolsPage userTools = new UserToolsPage(browser);
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.assertOnUserSettings()
			.openPreferences()
			.assertOnUserPreferences()
			.setResume(ResumeOption.auto);
		
		//open a course via REST url
		CoursePageFragment coursePage = CoursePageFragment.getCourse(browser, deploymentUrl, newCourse);
		coursePage
			.assertOnCoursePage()
			.clickTree();
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());

		//check the title of the course if any
		WebElement courseTitle = browser.findElement(By.tagName("h2"));
		Assert.assertNotNull(courseTitle);
		Assert.assertTrue(courseTitle.isDisplayed());
		Assert.assertTrue(courseTitle.getText().contains(newCourse.getTitle()));
	}
	
	/**
	 * Set the resume preferences to resume the session on request,
	 * open a course, log out, log in, resume the session and check
	 * if the course is resumed.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void resumeCourseOnDemand()
	throws IOException, URISyntaxException {
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		//deploy a course
		CourseVO newCourse = new RepositoryRestClient(deploymentUrl).deployDemoCourse();

		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.searchResource(newCourse.getKey().toString())
			.selectResource(newCourse.getTitle());
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser();

		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl)
				.loginAs(user.getLogin(), user.getPassword());
		
		//set the preferences to resume automatically
		UserToolsPage userTools = new UserToolsPage(browser);
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setResume(ResumeOption.ondemand);
		
		//open a course via REST url and click it
		CoursePageFragment.getCourse(browser, deploymentUrl, newCourse).clickTree();
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		//resume
		loginPage.resumeWithAssert();

		//check the title of the course if any
		WebElement courseTitle = browser.findElement(By.tagName("h2"));
		Assert.assertNotNull(courseTitle);
		Assert.assertTrue(courseTitle.isDisplayed());
		Assert.assertTrue(courseTitle.getText().contains(newCourse.getTitle()));
	}
	
	/**
	 * Disable the resume function and check that the resume
	 * popup don't stay in our way after login.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void resumeDisabled()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl)
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		//set the preferences to resume automatically
		UserToolsPage userTools = new UserToolsPage(browser);
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setResume(ResumeOption.none);
		
		//logout
		userTools.logout();
		
		//login again
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword())
			//check that we are really logged in
			.assertLoggedIn(user);
		
		//and check that we don't see the resume button
		List<WebElement> resumeButtons = browser.findElements(LoginPage.resumeButton);
		Assert.assertTrue(resumeButtons.isEmpty());
	}
	

	/**
	 * An user configures its landing page, log out
	 * and try it.
	 * 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginInHomeWithLandingPage()
	throws IOException, URISyntaxException {
		//create a random user
		UserRestClient userClient = new UserRestClient(deploymentUrl);
		UserVO user = userClient.createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());

		UserToolsPage userTools = new UserToolsPage(browser);
		userTools
				.openUserToolsMenu()
				.openMySettings()
				.openPreferences()
				.setResume(ResumeOption.none)
				.setLandingPage("/RepositorySite/0/Search/0");
		
		userTools.logout();
		
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		
		new AuthoringEnvPage(browser)
			.assertOnGenericSearch();
	}
	
	/**
	 * Jump to notifications in home, go to the courses and return
	 * to home's notification with a REST url.
	 * 
	 * @see https://jira.openolat.org/browse/OO-1962
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void loginInHomeWithRestUrl()
	throws IOException, URISyntaxException {
		//create a random user
		UserRestClient userClient = new UserRestClient(deploymentUrl);
		UserVO user = userClient.createRandomUser();

		//load dmz
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl)
				.assertOnLoginPage();
		
		String jumpToNotificationsUrl = deploymentUrl.toString() + "url/HomeSite/" + user.getKey() + "/notifications/0";
		browser.get(jumpToNotificationsUrl);
		loginPage.loginAs(user.getLogin(), user.getPassword());
		//must see the notification
		new UserToolsPage(browser).assertOnNotifications();
		
		//go to courses
		NavigationPage.load(browser)
			.openMyCourses();
		
		//use url to go to notifications
		String goToNotificationsUrl = deploymentUrl.toString() + "auth/HomeSite/" + user.getKey() + "/notifications/0";
		browser.get(goToNotificationsUrl);
		//must see the notification
		new UserToolsPage(browser).assertOnNotifications();
	}
	
	/**
	 * Check if a user can use the rest url in OpenOLAT.
	 * It jump from the static sites to its home, user tools,
	 * to the visiting card of an other user.
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
	public void restUrlAfterLogin()
	throws IOException, URISyntaxException {
		//create a random user
		UserRestClient userClient = new UserRestClient(deploymentUrl);
		UserVO user = userClient.createRandomUser("Kanu");
		UserVO ryomou = userClient.createRandomUser("Ryomou");

		//load dmz
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(user.getLogin(), user.getPassword());
		
		//go to courses
		NavigationPage navBar = NavigationPage.load(browser);
		navBar.openMyCourses();
		
		//use url to go to the other users business card
		String ryomouBusinessCardUrl = deploymentUrl.toString() + "auth/Identity/" + ryomou.getKey();
		browser.get(ryomouBusinessCardUrl);
		new VisitingCardPage(browser)
			.assertOnVisitingCard()
			.assertOnLastName(ryomou.getLastName());
		
		//return to my courses
		navBar.openMyCourses()
			.assertOnMyCourses();
		
		//go to profile by url
		String userUrl = deploymentUrl.toString() + "url/Identity/" + user.getKey();
		browser.get(userUrl);
		new UserProfilePage(browser)
			.assertOnProfile()
			.assertOnUsername(user.getLogin());
		
		//go to groups
		String groupsUrl = deploymentUrl.toString() + "url/GroupsSite/0/AllGroups/0";
		browser.get(groupsUrl);
		new GroupsPage(browser)
			.assertOnMyGroupsSelected();
		
		//go to my calendar
		String calendarUrl = deploymentUrl.toString() + "auth/HomeSite/0/calendar/0";
		browser.get(calendarUrl);
		new UserToolsPage(browser)
			.assertOnCalendar();
		
		//go to my folder
		String folderUrl = deploymentUrl.toString() + "auth/HomeSite/" + user.getKey() + "/userfolder/0";
		browser.get(folderUrl);
		new UserToolsPage(browser)
			.assertOnFolder();
	}
	
	/**
	 * Switch the language to german and after logout login to english
	 * and check every time.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userSwitchLanguageSwitchToEnglish()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		//set the languages preferences to german
		UserToolsPage userTools = new UserToolsPage(browser);
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.assertOnUserSettings()
			.openPreferences()
			.assertOnUserPreferences()
			.setLanguage("de");
		
		userTools.logout();
		
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		WebElement usernameDE = browser.findElement(LoginPage.usernameFooterBy);
		boolean de = usernameDE.getText().contains("Eingeloggt als");
		Assert.assertTrue(de);
		List<WebElement> deMarker = browser.findElements(By.className("o_lang_de"));
		Assert.assertFalse(deMarker.isEmpty());
		
		
		//set the languages preferences to english
		userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences()
			.setLanguage("en");
		
		userTools.logout();
				
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		WebElement usernameEN = browser.findElement(LoginPage.usernameFooterBy);
		boolean en = usernameEN.getText().contains("Logged in as");
		Assert.assertTrue(en);
		List<WebElement> enMarker = browser.findElements(By.className("o_lang_en"));
		Assert.assertFalse(enMarker.isEmpty());
	}
	
	/**
	 * Change the password, log out and try to log in again
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userChangeItsPassword()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();

		UserToolsPage userTools = new UserToolsPage(browser);
		userTools
			.openUserToolsMenu()
			.openPassword();
		
		String newPassword = UUID.randomUUID().toString();
		UserPasswordPage password = UserPasswordPage.getUserPasswordPage(browser);
		password.setNewPassword(user.getPassword(), newPassword);

		userTools.logout();
		
		loginPage
			.loginAs(user.getLogin(), newPassword)
			.resume()
			.assertLoggedIn(user);
	}
	

	/**
	 * Change the email, log out and check the new confirmed E-mail.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userChangeItsEmail()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		
		String newEmail = user.getLogin() + "@openolat.frentix.com";

		UserToolsPage userTools = new UserToolsPage(browser);
		UserProfilePage profil = userTools
			.openUserToolsMenu()
			.openMyProfil()
			.changeEmail(newEmail)
			.saveProfilAndConfirmEmail()
			.assertOnChangedEmail(newEmail);
		
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
		String confirmationLink = profil.extractConfirmationLink(messages.get(0));
		profil.loadConfirmationLink(confirmationLink);
		
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		userTools = new UserToolsPage(browser);
		profil = userTools
			.openUserToolsMenu()
			.openMyProfil()
			.assertOnEmail(newEmail);
	}
	
	
	/**
	 * Reset the preferences and check that a log out happens
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userResetItsPreferences()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume();

		UserToolsPage userTools = new UserToolsPage(browser);
		UserPreferencesPageFragment prefs = userTools
			.openUserToolsMenu()
			.openMySettings()
			.openPreferences();
		//reset the preferences
		prefs.resetPreferences();
		//check the user is log out
		loginPage.assertOnLoginPage();
	}
	
	/**
	 * Go in portal, edit it, deactivate the quick start portlet,
	 * finish editing, check that the quick start portlet disappears,
	 * re-edit, reactivate the quick start portlet and check it is
	 * again in the non-edit view. 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void portletDeactivateActivate()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		PortalPage portal = navBar.openPortal()
			.assertPortlet(PortalPage.quickStartBy)
			.edit()
			.disable(PortalPage.quickStartBy)
			.finishEditing()
			.assertNotPortlet(PortalPage.quickStartBy);
		
		//re-enable quickstart
		portal.edit()
			.enable(PortalPage.quickStartBy)
			.finishEditing()
			.assertPortlet(PortalPage.quickStartBy);
		
		List<WebElement> portalInactive = browser.findElements(PortalPage.inactiveBy);
		Assert.assertTrue(portalInactive.isEmpty());
	}
	
	/**
	 * Go to the portal, edit it, move the notes to the
	 * top, quit editing, check the notes are the first
	 * portlet in the view mode.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void movePortletToTheTop()
	throws IOException, URISyntaxException {
		
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword());

		NavigationPage navBar = NavigationPage.load(browser);
		PortalPage portal = navBar.openPortal()
			.assertPortlet(PortalPage.notesBy)
			.edit()
			.moveLeft(PortalPage.notesBy)
			.moveUp(PortalPage.notesBy)
			.moveUp(PortalPage.notesBy)
			.moveUp(PortalPage.notesBy)
			.moveRight(PortalPage.quickStartBy)
			.moveDown(PortalPage.quickStartBy);
		//finish editing
		portal.finishEditing();
		
		//no inactive panel -> we are in view mode
		List<WebElement> portalInactive = browser.findElements(PortalPage.inactiveBy);
		Assert.assertTrue(portalInactive.isEmpty());

		//notes must be first
		List<WebElement> portlets = browser.findElements(By.className("o_portlet"));
		Assert.assertFalse(portlets.isEmpty());	
		WebElement notesPortlet = portlets.get(0);
		String cssClass = notesPortlet.getAttribute("class");
		Assert.assertNotNull(cssClass);
		Assert.assertTrue(cssClass.contains("o_portlet_notes"));
	}
	
	/**
	 * Browse some tabs, two times back and check where I am.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void browserBack()
	throws IOException, URISyntaxException {
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();

		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openPortal()
			.assertPortlet(PortalPage.quickStartBy);
		navBar.openAuthoringEnvironment();
		navBar.openGroups(browser);
		navBar.openMyCourses();
		navBar.openUserManagement();
		navBar.openAdministration();
		
		//we are in administration
		browser.navigate().back();
		//we are in user management
		By userManagementCreateBy = By.cssSelector("ul.o_tools a.o_sel_useradmin_create");
		OOGraphene.waitElement(userManagementCreateBy, browser);
		browser.navigate().back();
		//we are in "My courses", check
		OOGraphene.waitElement(NavigationPage.myCoursesAssertBy, browser);
	}
	
	/**
	 * The administrator create an user, the user log in.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createUser(@Drone @User WebDriver userBrowser)
	throws IOException, URISyntaxException {
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs("administrator", "openolat")
			.resume();
		
		String uuid = UUID.randomUUID().toString();
		String username = ("miku-" + uuid).substring(0, 32);
		String password = "Miku#hatsune#01";
		UserVO userVo = UserAdminPage.createUserVO(username, "Miku", "Hatsune", username + "@openolat.com", password);

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openCreateUser()
			.fillUserForm(userVo)
			.assertOnUserEditView(username);
		
		userAdminPage
			.openSearchUser()
			.searchByUsername(username)
			.assertOnUserInList(username)
			.selectByUsername(username)
			.assertOnUserEditView(username);
		
		//user log in
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(username, password)
			.resume()
			.assertLoggedIn(userVo);
	}
	
	/**
	 * Test if deleted user cannot login anymore. An administrator
	 * create a user. This user log in and log out. The administrator
	 * use the direct delete workflow in user management to delete
	 * it.<br>
	 * The user try to log in again, unsuccessfully. The
	 * administrator doesn't find it anymore in the user
	 * search of the user management tab.
	 * 
	 */
	@Test
	@RunAsClient
	public void deleteUser(@Drone @User WebDriver userBrowser) {
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs("administrator", "openolat")
			.resume();
		
		String uuid = UUID.randomUUID().toString();
		String username = ("miku-" + uuid).substring(0, 32);
		String lastName = "Hatsune" + uuid;
		String password = "Miku#hatsune#02";
		UserVO userVo = UserAdminPage.createUserVO(username, "Miku", lastName, username + "@openolat.com", password);

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openCreateUser()
			.fillUserForm(userVo)
			.assertOnUserEditView(username);
		
		//user log in
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(username, password)
			.resume()
			.assertLoggedIn(userVo);
		//log out
		new UserToolsPage(userBrowser).logout();
		
		//admin delete
		userAdminPage
			.openSearchUser()
			.searchByUsername(username)
			.selectByUsername(username)
			.assertOnUserEditView(username)
			.deleteUser()
			.confirmDeleteUsers();
		
		//user try the login
		userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.loginDenied(username, password);
		//assert on error message
		By errorMessageby = By.cssSelector("div.modal-body.alert.alert-danger");
		OOGraphene.waitElement(errorMessageby, 2, userBrowser);

		// administrator search the deleted user
		userAdminPage
			.openSearchUser()
			.searchByUsername(username)
			.assertNotInUserList(username);
	}
	
	/**
	 * Import 2 new users and check if the first can log in.
	 * 
	 * @param loginPage
	 * @param userBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void importUsers(@Drone @User WebDriver userBrowser)
	throws IOException, URISyntaxException {
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs("administrator", "openolat")
			.resume();

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openImportUsers();
		//start import wizard
		ImportUserPage importWizard = userAdminPage.startImport();
		
		String uuid = UUID.randomUUID().toString();
		String username1 = ("moka-" + uuid).substring(0, 32);
		String username2 = ("mizore-" + uuid).substring(0, 32);
		
		StringBuilder csv = new StringBuilder();
		UserVO user1 = importWizard.append(username1, "rosario01", "Moka", "Akashiya", csv);
		importWizard.append(username2, "vampire01", "Mizore", "Shirayuki", csv);
		importWizard
			.fill(csv.toString())
			.nextData() // -> preview
			.assertGreen(2)
			.nextOverview() // -> groups
			.nextGroups() // -> emails
			.finish();
		
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		//user log in
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(username1, "rosario01")
			.resume()
			.assertLoggedIn(user1);
	}
	
	/**
	 * Import 1 new user and 1 existing, change the password and the last name
	 * of the existing user.
	 * 
	 * @param loginPage
	 * @param existingUserBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test 
	@RunAsClient
	public void importExistingUsers(@Drone @User WebDriver existingUserBrowser,
			@Drone @Student WebDriver newUserBrowser)
	throws IOException, URISyntaxException {

		UserVO user1 = new UserRestClient(deploymentUrl)
			.createRandomUser("tsukune");
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs("administrator", "openolat")
			.resume();

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openImportUsers();
		//start import wizard
		ImportUserPage importWizard = userAdminPage.startImport();
		
		String uuid = UUID.randomUUID().toString();
		String username1 = ("moka-" + uuid).substring(0, 32);
		String password1 = "Rosario#02";
		String password2 = "Openolat#2";

		StringBuilder csv = new StringBuilder();
		UserVO newUser = importWizard.append(username1, password1, "Moka", "Akashiya", csv);
		user1 = importWizard.append(user1, "Aono", password2, csv);
		importWizard
			.fill(csv.toString())
			.nextData() // -> preview
			.assertGreen(1)
			.assertWarn(1)
			.updatePasswords()
			.updateUsers()
			.nextOverview() // -> groups
			.nextGroups() // -> emails
			.finish();
		
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		//existing user log in with its new password and check if its name was updated
		LoginPage userLoginPage = LoginPage.load(existingUserBrowser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(user1.getLogin(), password2)
			.resume()
			.assertLoggedInByLastName("Aono");
		
		//new user log in
		LoginPage newLoginPage = LoginPage.load(newUserBrowser, deploymentUrl);
		//tools
		newLoginPage
			.loginAs(newUser.getLogin(), password1)
			.resume()
			.assertLoggedInByLastName("Akashiya");
	}
	

	/**
	 * An administrator add a location for contact tracing. Somebody
	 * use it as guest.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void contactTracingAsGuest()
	throws IOException, URISyntaxException {
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		ContactTracingAdminPage tracingAdmin = NavigationPage.load(browser)
			.openAdministration()
			.openContactTracing()
			.enableTracing()
			.selectLocations();
		String url = tracingAdmin
			.addLocation("Zurich", "Meeting-Room", "IT");
		Assert.assertNotNull(url);
		
		//log out
		new UserToolsPage(browser)
			.logout();
		
		ContactTracingPage tracing = new ContactTracingPage(browser);
		tracing.load(url)
			.asGuest()
			.fillIdentification("Jeremy K.", "Bloch")
			.fillContact("jeremy@openolat.com", "07912345678")
			.send()
			.assertSent();
	}
	

	/**
	 * An administrator add a location for contact tracing. A user
	 * use it to register itself.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void contactTracingWithLogin()
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl)
				.createRandomUser("carole");
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		ContactTracingAdminPage tracingAdmin = NavigationPage.load(browser)
			.openAdministration()
			.openContactTracing()
			.enableTracing()
			.selectLocations();
		String url = tracingAdmin
			.addLocation("Biel", "IT-Room", "Development");
		Assert.assertNotNull(url);
		
		//log out
		new UserToolsPage(browser)
			.logout();
		
		ContactTracingPage tracing = new ContactTracingPage(browser);
		tracing.load(url)
			.asAuthenticatedUser()
			.loginAs(user);
		
		tracing
			.send()
			.assertSent();
	}
}
