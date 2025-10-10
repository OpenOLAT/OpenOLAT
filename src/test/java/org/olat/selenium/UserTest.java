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

import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.login.webauthn.PasskeyLevels;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.LoginPage.PasskeyInformations;
import org.olat.selenium.page.core.LoginPasswordForgottenPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupsPage;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.tracing.ContactTracingAdminPage;
import org.olat.selenium.page.tracing.ContactTracingPage;
import org.olat.selenium.page.user.ImportUserPage;
import org.olat.selenium.page.user.PasswordAndAuthenticationAdminPage;
import org.olat.selenium.page.user.PortalPage;
import org.olat.selenium.page.user.RegistrationPage;
import org.olat.selenium.page.user.UserAdminPage;
import org.olat.selenium.page.user.UserAttributesWizardPage;
import org.olat.selenium.page.user.UserPasswordPage;
import org.olat.selenium.page.user.UserPreferencesPageFragment;
import org.olat.selenium.page.user.UserPreferencesPageFragment.ResumeOption;
import org.olat.selenium.page.user.UserProfilePage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.selenium.page.user.UserViewPage;
import org.olat.selenium.page.user.VisitingCardPage;
import org.olat.test.rest.RepositoryRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.virtualauthenticator.VirtualAuthenticator;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class UserTest extends Deployments {

	public static final Logger log = Tracing.createLoggerFor(UserTest.class);
	
	private WebDriver browser = getWebDriver(0);
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
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//deploy a course
		CourseVO newCourse = new RepositoryRestClient(deploymentUrl).deployDemoCourse();
		
		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.searchResource(newCourse.getKey().toString())
			.openResource(newCourse.getTitle());
		
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

		//check the title of the course if any//check the title of the course if any
		CoursePageFragment.getCourse(browser).assertOnTitle(newCourse.getTitle());
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
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//deploy a course
		CourseVO newCourse = new RepositoryRestClient(deploymentUrl).deployDemoCourse();

		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.searchResource(newCourse.getKey().toString())
			.openResource(newCourse.getTitle());
		
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
		loginPage
			.assertOnResume()	
			.resume();

		//check the title of the course if any
		CoursePageFragment.getCourse(browser).assertOnTitle(newCourse.getTitle());
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
			.loginAs(user.getLogin(), user.getPassword());
		
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
	 * An administrator enable passkey. A user generate its passkey
	 * during the login process, log out and login again with passkey.
	 * The test only works with Chrome.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void passkeyRegistration()
	throws IOException, URISyntaxException {
		// Firefox doesn't implement passkey and Safari not several browsers
		Assume.assumeTrue(browser instanceof ChromeDriver);

		WebDriver userBrowser = getWebDriver(1);
		
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		PasswordAndAuthenticationAdminPage passkeyAdminPage = NavigationPage.load(browser)
				.openAdministration()
				.openPasswordAndAuthentication()
				.enablePasskey(true)
				.enablePasskeyLevel(PasskeyLevels.level2);

		// Generate the passkey
		VirtualAuthenticator authenticator = LoginPage.registerAuthenticator(userBrowser);
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.loginWithRegistrationToPasskey(user.getLogin(), user.getPassword(), authenticator);
		
		//Log out
		new UserToolsPage(userBrowser)
			.logout();
		
		// Log in with passkey
		userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.loginWithPasskey(user.getLogin());
		
		passkeyAdminPage.enablePasskey(false);
	}
	

	/**
	 * An administrator enable passkey. A user generate its passkey
	 * during the login process, save its recovery keys log out and login again
	 * but we remove the authenticator. It triggers the error and it uses
	 * the recovery key to login.
	 * The test only works with Chrome.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void passkeyRecoveryKey()
	throws IOException, URISyntaxException {
		// Firefox doesn't implement passkey and Safari not several browsers
		Assume.assumeTrue(browser instanceof ChromeDriver);

		WebDriver userBrowser = getWebDriver(1);
		
		//create a random user
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser();
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// Administrator opens the course to the public
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		PasswordAndAuthenticationAdminPage passkeyAdminPage = NavigationPage.load(browser)
				.openAdministration()
				.openPasswordAndAuthentication()
				.enablePasskey(true)
				.enablePasskeyLevel(PasskeyLevels.level2);

		// Generate the passkey
		VirtualAuthenticator authenticator = LoginPage.registerAuthenticator(userBrowser);
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		PasskeyInformations passkeyInfos = userLoginPage
			.loginWithRegistrationToPasskey(user.getLogin(), user.getPassword(), authenticator);
		
		//Log out
		new UserToolsPage(userBrowser)
			.logout();

		// Log in with recovery key (remove the virtual authenticator first)
		LoginPage.deregisterAuthenticator(userBrowser, authenticator);
		userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.loginWithPasskeyButRecovery(user.getLogin(), passkeyInfos);
		
		passkeyAdminPage.enablePasskey(false);
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
		String folderUrl = deploymentUrl.toString() + "auth/HomeSite/" + user.getKey() + "/filehub/0";
		browser.get(folderUrl);
		new UserToolsPage(browser)
			.assertOnFileHub();
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
			.loginAs(user.getLogin(), user.getPassword());
		
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
			.assertOnResume()
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
			.assertOnResume()
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
			.loginAs(user.getLogin(), user.getPassword());

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
			.assertOnResume()
			.resume()
			.assertLoggedIn(user);
	}
	
	
	/**
	 * An administrator generate a link to change the password of
	 * a user. The user uses the link to change its password.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userChangeItsPasswordWithRESTLink()
	throws IOException, URISyntaxException {
		UserRestClient userWebService = new UserRestClient(deploymentUrl);
		UserVO user = userWebService.createRandomUser();
		String pwChangeLink = userWebService.createPasswordChangeLink(user);
		
		// The default browser is probably still logged in
		WebDriver anOtherBrowser = getWebDriver(1);
		anOtherBrowser.navigate()
			.to(new URL(pwChangeLink));
		OOGraphene.waitModalDialog(anOtherBrowser);
		
		LoginPasswordForgottenPage forgottenPage = new LoginPasswordForgottenPage(anOtherBrowser)
				.assertUserIdentificationAndNext(user.getEmail());
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
		String otp = RegistrationPage.extractOtp(messages.get(0));
		log.info("Registration OTP: {}", otp);
		
		String newPassword = "Sel#17HighlySecret";
		forgottenPage
			.confirmOtp(otp)
			.newPassword(newPassword);
		
		LoginPage loginPage = LoginPage.load(anOtherBrowser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), newPassword)
			.assertLoggedInByLastName(user.getLastName());
	}
	
	
	/**
	 * A user with an actual password can call the URL url/changepw/0 and starts
	 * the change password workflow by giving its email address.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userChangeItsPasswordWithChangePwLink()
	throws IOException, URISyntaxException {
		UserRestClient userWebService = new UserRestClient(deploymentUrl);
		UserVO user = userWebService.createRandomUser();
		
		// The default browser is probably still logged in
		WebDriver anOtherBrowser = getWebDriver(1);
		anOtherBrowser.navigate()
			.to(new URL(deploymentUrl.toString() + "url/changepw/0"));
		OOGraphene.waitModalDialog(anOtherBrowser);
		
		LoginPasswordForgottenPage forgottenPage = new LoginPasswordForgottenPage(anOtherBrowser)
				.userIdentification(user.getEmail());
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
		String otp = RegistrationPage.extractOtp(messages.get(0));
		log.info("Registration OTP: {}", otp);
		
		String newPassword = "Sel#18HighlySecret";
		forgottenPage
			.confirmOtp(otp)
			.newPassword(newPassword);
		
		LoginPage loginPage = LoginPage.load(anOtherBrowser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), newPassword)
			.assertLoggedInByLastName(user.getLastName());
	}
	
	
	/**
	 * An administrator generate a link to change the password of
	 * a user. The user uses the link to change its password.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void userChangeItsPasswordWithAdminLink()
	throws IOException, URISyntaxException {
		UserRestClient userWebService = new UserRestClient(deploymentUrl);
		UserVO user = userWebService.createRandomUserWithoutPassword("Joe");
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		UserViewPage userViewPage = navBar
			.openUserManagement()
			.searchByUsername(user.getLogin())
			.selectByUsername(user.getLogin());
		String pwChangeLink = userViewPage
			.assertOnUserEditView(user.getFirstName(), user.getLastName())
			.assertOnUserEditProfil()
			.openPasswordsTab()
			.sendPasswordLink();
		
		// Log out
		new UserToolsPage(browser).logout();
		// Check the presence of an email
		List<SmtpMessage> adminMessages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, adminMessages.size());
		getSmtpServer().reset();
		
		// The user starts the password reset
		browser.navigate()
			.to(new URL(pwChangeLink));
		OOGraphene.waitModalDialog(browser);
		
		LoginPasswordForgottenPage forgottenPage = new LoginPasswordForgottenPage(browser)
				.assertUserIdentificationAndNext(user.getEmail());
		
		List<SmtpMessage> userMessages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, userMessages.size());
		String otp = RegistrationPage.extractOtp(userMessages.get(0));
		log.info("Registration OTP: {}", otp);
		
		String newPassword = "Sel#22FamousSecret";
		forgottenPage
			.confirmOtp(otp)
			.newPassword(newPassword);
		
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage
			.loginAs(user.getLogin(), newPassword)
			.assertLoggedInByLastName(user.getLastName());
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
			.loginAs(user.getLogin(), user.getPassword());
		
		String newEmail = user.getLogin() + "@openolat.com";

		UserToolsPage userTools = new UserToolsPage(browser);
		UserProfilePage profil = userTools
			.openUserToolsMenu()
			.openMyProfil()
			.changeEmail(newEmail)
			.validateEmail();
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
		String otp = profil.extractOtp(messages.get(0));
		
		profil
			.confirmEmail(otp)
			.assertOnChangedEmail(newEmail);
		
		userTools
			.logout();
		
		OOGraphene.waitingALittleBit();
	
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.assertOnResume()
			.resume();
		
		profil = new UserProfilePage(browser);
		profil
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
			.loginAs(user.getLogin(), user.getPassword());

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
		String cssClass = notesPortlet.getDomAttribute("class");
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
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
	
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(administrator)
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
	public void createUser()
	throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		String uuid = UUID.randomUUID().toString();
		String username = ("miku-" + uuid).substring(0, 32);
		String password = "MiHat#Sune#01Sama";
		UserVO userVo = UserAdminPage.createUserVO(username, "Miku", "Hatsune", username + "@openolat.com", password);

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openCreateUser();
		userAdminPage
			.fillUserForm(userVo)
			.assertOnUserEditView(userVo.getFirstName(), userVo.getLastName());
		
		userAdminPage
			.openSearchUser()
			.searchByUsername(username)
			.assertOnUserInList(username)
			.selectByUsername(username)
			.assertOnUserEditView(userVo.getFirstName(), userVo.getLastName())
			.assertOnUserEditProfil();
		
		//user log in
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(username, password)
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
	public void deleteUser()
	throws IOException, URISyntaxException {
		WebDriver userBrowser = getWebDriver(1);
		
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		String uuid = UUID.randomUUID().toString();
		String username = ("miku-" + uuid).substring(0, 32);
		String lastName = "Hatsune" + uuid;
		String password = "MiHat#Sune#02Sama";
		UserVO userVo = UserAdminPage.createUserVO(username, "Miku012_KU", lastName, username + "@openolat.com", password);

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openCreateUser();
		userAdminPage
			.fillUserForm(userVo)
			.assertOnUserEditView(userVo.getFirstName(), userVo.getLastName());
		
		//user log in
		LoginPage userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(username, password)
			.assertLoggedIn(userVo);
		//log out
		new UserToolsPage(userBrowser).logout();
		
		//admin delete
		userAdminPage
			.openSearchUser()
			.searchByUsername(username)
			.selectByUsername(username)
			.assertOnUserEditView(userVo.getFirstName(), userVo.getLastName())
			.deleteUser()
			.confirmDeleteUsers();
		
		//user try the login
		userLoginPage = LoginPage.load(userBrowser, deploymentUrl);
		userLoginPage
			.loginDenied(username, password);
		//assert on error message
		By errorMessageby = By.cssSelector("div.o_login_box div.o_login_error");
		OOGraphene.waitElement(errorMessageby, userBrowser);

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
	public void importUsers()
	throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
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
		UserVO user1 = importWizard.append(username1, "Rosario01Book1", "Moka", "Akashiya", csv);
		importWizard.append(username2, "Vampire01Book2", "Mizore", "Shirayuki", csv);
		importWizard
			.fill(csv.toString())
			.nextData() // -> preview
			.assertGreen(2)
			.nextOverview() // -> groups
			.nextGroups() // -> emails
			.finish();
		
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		
		//user log in
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(username1, "Rosario01Book1")
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
	public void importExistingUsers()
	throws IOException, URISyntaxException {

		UserVO user1 = new UserRestClient(deploymentUrl).createRandomUser("tsukune");
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();

		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
			.openUserManagement()
			.openImportUsers();
		//start import wizard
		ImportUserPage importWizard = userAdminPage.startImport();
		
		String uuid = UUID.randomUUID().toString();
		String username1 = ("moka-" + uuid).substring(0, 32);
		String password1 = "Rosario#02Book";
		String password2 = "Openolat#2Book";

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
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		//tools
		userLoginPage
			.loginAs(user1.getLogin(), password2)
			.assertLoggedInByLastName("Aono");
		
		//new user log in
		LoginPage newLoginPage = LoginPage.load(browser, deploymentUrl);
		//tools
		newLoginPage
			.loginAs(newUser.getLogin(), password1)
			.assertLoggedInByLastName("Akashiya");
	}
	
	/**
	 * The administrator modify the status of a user with the batch function.
	 * It sets it inactive and the user try unsuccessfully to log in.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void modifyUserStatusBatch()
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Katherin");
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
				.openUserManagement();
		userAdminPage
			.openSearchUser()
			.searchByUsername(user.getLogin())
			.assertOnUserInList(user.getLogin())
			.selectRowByUsername(user.getLogin())
			.modifyStatusBatch(Identity.STATUS_INACTIVE.intValue())
			.assertOnInactiveUserInList(user.getLogin());

		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage
			.loginDenied(user.getLogin(), user.getPassword());
	}
	
	

	/**
	 * The administrator modify the attributes of a user with the batch function.
	 * It modifies the first name and set the author role.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void modifyUserAttributesBatch()
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Jun");
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		//login
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		UserAdminPage userAdminPage = navBar
				.openUserManagement();
		UserAttributesWizardPage attributesWizard = userAdminPage
			.openSearchUser()
			.searchByUsername(user.getLogin())
			.assertOnUserInList(user.getLogin())
			.selectRowByUsername(user.getLogin())
			.modifyAttributesBatch();

		String newFirstName = "Jon";
		attributesWizard
			.assertOnAttributes()
			.changeAttribute("firstName", newFirstName)
			.nextToOtherSettings()
			.nextToRoles()
			.changeRoles(OrganisationRoles.author.name(), true)
			.nextToGroups()
			.nextToOverview()
			.assertOnNewAttributeOverview(newFirstName)
			.finish();
		
		userAdminPage
			.assertOnUserInList(newFirstName);
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
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(administrator)
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
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("carole");
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(administrator)
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
		tracing
			.load(url)
			.asAuthenticatedUser()
			.loginAs(user);
		
		tracing
			.send()
			.assertSent();
	}
}
