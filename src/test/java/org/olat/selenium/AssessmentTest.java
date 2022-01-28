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
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.AssessmentCEConfigurationPage;
import org.olat.selenium.page.course.AssessmentModePage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.BulkAssessmentPage.BulkAssessmentData;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.CourseSettingsPage;
import org.olat.selenium.page.course.GroupTaskConfigurationPage;
import org.olat.selenium.page.course.GroupTaskPage;
import org.olat.selenium.page.course.GroupTaskToCoachPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupPage;
import org.olat.selenium.page.qti.QTI21Page;
import org.olat.selenium.page.repository.RepositoryEditDescriptionPage;
import org.olat.selenium.page.repository.ScormPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.RepositoryRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 11.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class AssessmentTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;

	
	/**
	 * An author upload a SCORM resource, create a course and use the
	 * SCORM within. It publish the course, add a participant to the
	 * course. The participant log in, select the course above, run
	 * the SCORM and finish it.<br>
	 * At the end, the author go to the assessment tool and chec that
	 * the participant has successfully passed the test.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void scormCourseWithAssessment(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String scormTitle = "SCORM - " + UUID.randomUUID();
		URL scormUrl = JunitTestHelper.class.getResource("file_resources/very_simple_scorm.zip");
		File scormFile = new File(scormUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(scormTitle, scormFile);
		
		//create a course
		String courseTitle = "Course-With-SCORM-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Scorm with the scorm that we create above
		String scormNodeTitle = "SCORM-Node";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("scorm")
			.nodeTitle(scormNodeTitle)
			.selectTabLearnContent()
			.chooseScorm(scormTitle);

		//publish the course
		courseEditor
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save()
			.clickToolbarBack();

		CoursePageFragment courseRuntime = new CoursePageFragment(browser);
		//publish the course
		courseRuntime
			.publish();
		//add Ryomou as a course member
		courseRuntime
			.members()
			.addMember()
			.searchMember(ryomou, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();

		//Ryomou open the course
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//open the course and see the test start page
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		ryomouCourse
			.clickTree()
			.selectWithTitle(scormNodeTitle);
		
		By scormH2By = By.cssSelector("div.o_course_run h2");
		WebElement scormH2 = ryomouBrowser.findElement(scormH2By);
		Assert.assertEquals(scormNodeTitle, scormH2.getText().trim());
		
		//scorm
		ScormPage scorm = ScormPage.getScormPage(ryomouBrowser);
		scorm
			.start()
			.passVerySimpleScorm()
			.assertOnScormScore(33);
		
		WebElement scormH2Back = ryomouBrowser.findElement(scormH2By);
		Assert.assertEquals(scormNodeTitle, scormH2Back.getText().trim());
		
		//log out
		UserToolsPage roymouUserTools = new UserToolsPage(ryomouBrowser);
		roymouUserTools.logout();
		
		//author take the lead and check the assessment tool
		navBar
			.openMyCourses()
			.select(courseTitle);
		
		//open the assessment tool
		AssessmentToolPage assessmentTool = new CoursePageFragment(browser)
			.assessmentTool();
		
		assessmentTool
			.users()
		//check that ryomou has passed the test
			.assertOnUsers(ryomou)
			.selectUser(ryomou)
			.assertPassed(ryomou);
	}
	
	/**
	 * An author upload a test, create a course with a test course
	 * element, publish the course, add 2 students (Ryomou and Kanu)
	 * to the course, configure an assessment.<br />
	 * A first student log in before the assessment is started by the
	 * author, the second log-in after the begin of the assessment.
	 * Both pass the test. The Author ends the assessment. The two
	 * students wait the end of the assessment and go back to normal
	 * activities. The author checks the students pass the test in the
	 * assessment tool.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void assessmentMode_manual(@Drone @Student WebDriver ryomouBrowser,
			@Drone @Participant WebDriver kanuBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "QTI-Test-1.2-" + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-2.1-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type test with the QTI 2.1 test that we upload above
		String testNodeTitle = "Test-QTI-2.1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle)
			.selectTabLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the test start page
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		//add Ryomou and Kanu as a course member
		courseRuntime.members().quickAdd(ryomou);
		courseRuntime.members().quickAdd(kanu);
		
		//Kanu log in 
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();
		
		// prepare and start an assessment
		Calendar cal = Calendar.getInstance();
		Date begin = cal.getTime();
		cal.add(Calendar.MINUTE, 5);
		Date end = cal.getTime();
		String assessmentName = "Assessment-" + UUID.randomUUID();
		courseRuntime
			.assessmentConfiguration()
			.createAssessmentMode()
			.editAssessment(assessmentName, begin, end, true)
			.save()
			.clickToolbarBack()
			.assertAssessmentModeList()
			.start(assessmentName)
			.confirmStart();

		//Ryomou opens the course
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword());
		//start the assessment
		AssessmentModePage ryomouAssessment = new AssessmentModePage(ryomouBrowser)
			.startAssessment(false);
		//go to the test
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(testNodeTitle);
		//pass the test
		QTI21Page.getQTI21Page(ryomouBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);

	
		//Kanu makes the test
		AssessmentModePage kanuAssessment = new AssessmentModePage(kanuBrowser)
			.startAssessment(true);
		//go to the test
		CoursePageFragment kanuTestCourse = new CoursePageFragment(kanuBrowser);
		kanuTestCourse
			.clickTree()
			.selectWithTitle(testNodeTitle);
		//pass the test
		QTI21Page.getQTI21Page(kanuBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);

		
		//Author ends the test
		courseRuntime
			.assessmentConfiguration()
			.stop(assessmentName)
			.confirmStop();
		
		kanuAssessment.waitBackToOpenOlat();
		ryomouAssessment.waitBackToOpenOlat();
		kanuAssessment.backToOpenOLAT();
		ryomouAssessment.backToOpenOLAT();
		
		//Author check if they pass the test
		navBar
			.openMyCourses()
			.select(courseTitle);	
		//open the assessment tool
		AssessmentToolPage assessmentTool = new CoursePageFragment(browser)
			.assessmentTool();		
		assessmentTool
			.users()
		//check that ryomou has passed the test
			.assertOnUsers(ryomou)
			.assertOnUsers(kanu)
			.selectUser(ryomou)
			.assertPassed(ryomou);
	}
	
	/**
	 * An author create a course, publish it and add a participant.
	 * It set the certificate, create one for the participant.<br>
	 * The participant logs in and look at its wonderful certificate. 
	 * 
	 * @param authorLoginPage
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void certificatesManuallyGenerated(@Drone @User WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Certificates-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type CP with the CP that we create above
		courseRuntime
			.edit()
			.createNode("info")
			.autoPublish();
		
		//add a participant to the course
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(rei, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		// return to course
		CourseSettingsPage courseSettings = members
			.clickToolbarBack()
			.settings();
		courseSettings
			.certificates()
			.enableCertificates(false)
			.enableRecertification()
			.save();
		courseSettings
			.clickToolbarBack();
		
		//publish the course
		courseRuntime
			.publish();
		//create a certificate
		courseRuntime
			.assessmentTool()
			.users()
			.selectUser(rei)
			.generateCertificate();
		
		//Participant log in
		LoginPage reiLoginPage = LoginPage.load(reiBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
				
		//open the efficiency statements
		UserToolsPage reiUserTools = new UserToolsPage(reiBrowser);
		reiUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnCertificate(courseTitle);
	}
	
	/**
	 * An author create a course, set up the root node to make efficiency statement,
	 * add a test, publish it and add a participant. It set the certificate.<br>
	 * 
	 * The participant logs in, make the test and look at its wonderful certificate
	 * and the details of its performance.
	 * 
	 * @param authorLoginPage
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void certificatesGeneratedByTest(@Drone @User WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		
		//create an author and a participant
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		//deploy the test
		URL testUrl = ArquillianDeployments.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		String testTitle = "E4Test-" + UUID.randomUUID();
		new RepositoryRestClient(deploymentUrl, author).deployResource(new File(testUrl.toURI()), "-", testTitle);

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		//create a course
		String courseTitle = "Certif-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type test with the QTI 2.1 test that we upload above
		String testNodeTitle = "Test-QTI-2.1";
		CoursePageFragment courseRuntime = CoursePageFragment.getCourse(browser);
		courseRuntime
			.edit()
			.createNode("iqtest")
			.nodeTitle(testNodeTitle)
			.selectTabLearnContent()
			.chooseTest(testTitle)
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save()
			.clickToolbarBack();
		//publish the course
		courseRuntime
			.publish();
		
		//add a participant to the course
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(rei, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		// return to course
		CourseSettingsPage courseSetting = members
			.clickToolbarBack()
			.settings();
		courseSetting
			.certificates()
			.enableCertificates(true)
			.enableRecertification()
			.save();
		courseSetting
			.clickToolbarBack();
		
		//Participant log in
		LoginPage reiLoginPage = LoginPage.load(reiBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		
		//open the course
		NavigationPage reiNavBar = NavigationPage.load(reiBrowser);
		reiNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the test
		CoursePageFragment reiTestCourse = new CoursePageFragment(reiBrowser);
		reiTestCourse
			.clickTree()
			.selectWithTitle(testNodeTitle);
		//pass the test
		QTI21Page.getQTI21Page(reiBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		OOGraphene.waitingALittleLonger();
				
		//open the efficiency statements
		UserToolsPage reiUserTools = new UserToolsPage(reiBrowser);
		reiUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnCertificateAndStatements(courseTitle)
			.selectStatement(courseTitle)
			.selectStatementSegment()
			.assertOnCourseDetails(testNodeTitle, true);
	}

	/**
	 * This tests a course with cascading rules and expert rules
	 * to calculate if the course is passed and generate a
	 * certificate. 
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void certificatesGeneratedWithCascadingRules()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Rei");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		URL zipUrl = JunitTestHelper.class.getResource("file_resources/course_certificates_exrules.zip");
		File zipFile = new File(zipUrl.toURI());
		//go the authoring environment to import our course
		String zipTitle = "Certif - " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(zipTitle, zipFile);
		
		// publish the course
		new RepositoryEditDescriptionPage(browser)
			.clickToolbarBack();
		CoursePageFragment course = CoursePageFragment.getCourse(browser)
				.edit()
				.autoPublish();
		
		// add a participant
		MembersPage members = course
			.members();
		members
			.importMembers()
			.setMembers(participant1, participant2)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		course
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.registred)
			.save()
			.clickToolbarBack();
		
		course
			.changeStatus(RepositoryEntryStatusEnum.published);
	
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant log in and go directly to the first test
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		
		participantLoginPage
			.loginAs(participant1.getLogin(), participant1.getPassword())
			.resume();
		
		//open the course
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(zipTitle);
		
		//go to the test
		CoursePageFragment certificationCourse = new CoursePageFragment(browser);
		certificationCourse
			.clickTree()
			.assertWithTitleSelected("Test 1");
		//pass the test
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		OOGraphene.waitingALittleLonger();
		
		//open the efficiency statements
		String certificateTitle = zipTitle;
		UserToolsPage participantUserTools = new UserToolsPage(browser);
		participantUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnCertificateAndStatements(certificateTitle)
			.selectStatement(certificateTitle)
			.selectStatementSegment()
			.assertOnCourseDetails("Certificates", true)
			.assertOnCourseDetails("Struktur 1", true)
			.assertOnCourseDetails("Test 1", true);
		
		OOGraphene.waitingALittleLonger();
		
		//log out
		participantUserTools
			.logout();
		
		// participant 2 log in and go directly to the second test
		LoginPage participant2LoginPage = LoginPage.load(browser, deploymentUrl);
		
		participant2LoginPage
			.loginAs(participant2.getLogin(), participant2.getPassword())
			.resume();
		
		//open the course
		NavigationPage participant2NavBar = NavigationPage.load(browser);
		participant2NavBar
			.openMyCourses()
			.select(zipTitle);
		
		//go to the test
		CoursePageFragment certification2Course = new CoursePageFragment(browser);
		certification2Course
			.clickTree()
			.selectWithTitle("Struktur 3")
			.assertWithTitleSelected("Struktur 3")
			.assertWithTitle("Test 3")
			.selectWithTitle("Test 3");
		//pass the test
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		OOGraphene.waitingALittleLonger();
		
		//open the efficiency statements
		UserToolsPage participant2UserTools = new UserToolsPage(browser);
		participant2UserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnCertificateAndStatements(certificateTitle)
			.selectStatement(certificateTitle)
			.selectStatementSegment()
			.assertOnCourseDetails("Certificates", true)
			.assertOnCourseDetails("Struktur 3", true)
			.assertOnCourseDetails("Test 3", true);
	}
	
	/**
	 * An author create a course with an assessment course element with
	 * min., max., cut value and so on. It add an user to the course,
	 * go to the assessment tool and set a score to the assessed user.<br>
	 * 
	 * The user log in, go to the efficiency statements list and check
	 * it become its statement.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void assessmentCourseElement(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-Assessment-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String assessmentNodeTitle = "Assessment CE";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("ms")
			.nodeTitle(assessmentNodeTitle);
		
		//configure assessment
		AssessmentCEConfigurationPage assessmentConfig = new AssessmentCEConfigurationPage(browser);
		assessmentConfig
			.selectConfigurationWithRubric()
			.setRubricScore(0.1f, 10.0f, 5.0f);
		//set the score / passed calculation in root node and publish
		CourseSettingsPage courseSettings = courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings();
		courseSettings
			.accessConfiguration()
			.setUserAccess(UserAccess.registred)
			.save();
		courseSettings
			.certificates()
			.enableCertificates(true)
			.enableRecertification()
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
			.setAssessmentScore(8.0f)
			.assertUserPassedCourseNode(assessmentNodeTitle);
		
		//Ryomou login
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		//see its beautiful efficiency statement
		UserToolsPage ryomouUserTools = new UserToolsPage(ryomouBrowser);
		ryomouUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnStatement(courseTitle, true)
			.selectStatement(courseTitle)
			.selectStatementSegment()
			.assertOnCourseDetails(assessmentNodeTitle, true);
	}

	/**
	 * An author create a course for a group task with the default
	 * settings, all steps are selected, grading with only passed,
	 * 3 groups, 2 tasks, 1 solution...</br>
	 * A group has 2 participants, the first select a task, the
	 * second submit 2 documents, one with the embedded editor,
	 * one with the upload mechanism.</br>
	 * The author reviews the documents, use the assessment tool
	 * for group within the course element to set passed to the
	 * group.</br>
	 * The 2 participants check if they successfully passed the task.
	 * 
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskWithGroupsAndStandardSettings(@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
			
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-group-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Group task 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("gta")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.openBusinessGroupChooser()
			.createBusinessGroup("Group to task - 1")
			.createBusinessGroup("Group to task - 2")
			.createBusinessGroup("Group to task - 3")
			.confirmBusinessGroupsSelection()
			.saveWorkflow()
			.selectAssignment();
		
		URL task1Url = JunitTestHelper.class.getResource("file_resources/task_1_a.txt");
		File task1File = new File(task1Url.toURI());
		String taskName1 = "Task-1";
		gtaConfig.uploadTask(taskName1, task1File);
		
		URL task2Url = JunitTestHelper.class.getResource("file_resources/task_1_b.txt");
		File task2File = new File(task2Url.toURI());
		String taskName2 = "Task-2-b";
		gtaConfig
			.uploadTask(taskName2, task2File)
			.saveTasks()
			.selectSolution();
		
		URL solutionUrl = JunitTestHelper.class.getResource("file_resources/solution_1.txt");
		File solutionFile = new File(solutionUrl.toURI());
		gtaConfig.uploadSolution("The Best Solution", solutionFile);
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		GroupPage groupPage = membersPage
			.selectBusinessGroups()
			.selectBusinessGroup("Group to task - 1")
			.openAdministration()
			.openAdminMembers();
		
		groupPage
			.addMember()
			.searchMember(kanu, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		groupPage
			.addMember()
			.searchMember(ryomou, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		groupPage
			.close();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.assertMembersManagement()
			.clickToolbarBack();
		coursePage
			.assertStatus(RepositoryEntryStatusEnum.published)// publish the course for the participants
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		ryomouTask
			.assertAssignmentAvailable()
			.selectTask(taskName2)
			.assertSubmissionAvailable();
		
		//Participant 2 log in
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu)
			.resume();
		
		//open the course
		NavigationPage kanuNavBar = NavigationPage.load(kanuBrowser);
		kanuNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment kanuTestCourse = new CoursePageFragment(kanuBrowser);
		kanuTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_1.txt");
		File submit1File = new File(submit1Url.toURI());
		String submittedFilename = "my_solution.html";
		String submittedText = "This is my solution";
		GroupTaskPage kanuTask = new GroupTaskPage(kanuBrowser);
		kanuTask
			.assertTask(taskName2)
			.assertSubmissionAvailable()
			.submitFile(submit1File)
			.submitText(submittedFilename, submittedText)
			.submitDocuments();
		
		//back to author
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		GroupTaskToCoachPage groupToCoach = new GroupTaskToCoachPage(browser);
		groupToCoach
			.selectBusinessGroupToCoach("Group to task - 1")
			.assertSubmittedDocument("my_solution.html")
			.assertSubmittedDocument("submit_1.txt")
			.reviewed()
			.openGroupAssessment()
			.groupAssessment(Boolean.TRUE, null);
		
		//participant check if they passed
		kanuTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		kanuTask.assertPassed();
		
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask.assertPassed();
	}
	
	/**
	 * An author create a course for a task with the some custom
	 * settings, all steps are selected, grading with score and
	 * passed automatically calculated, 2 tasks, 1 solution...</br>
	 * It had 2 participants. One of them goes through the workflow,
	 * selects a task, submits 2 documents, one with the embedded editor,
	 * one with the upload mechanism.</br>
	 * The author reviews the documents, uploads a correction and
	 * want a revision.</br>
	 * The assessed participant upload a revised document.</br>
	 * The author sees it and close the revisions process, use
	 * the assessment tool to set the score.</br>
	 * The participant checks if she successfully passed the task.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskWithIndividualScoreAndRevision(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-individual-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Individual task 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectAssignment();
		
		URL task1Url = JunitTestHelper.class.getResource("file_resources/task_1_a.txt");
		File task1File = new File(task1Url.toURI());
		gtaConfig.uploadTask("Individual Task 1 alpha", task1File);
		
		URL task2Url = JunitTestHelper.class.getResource("file_resources/task_1_b.txt");
		File task2File = new File(task2Url.toURI());
		gtaConfig
			.uploadTask("Individual Task 2 beta", task2File)
			.saveTasks()
			.selectSolution();
		
		URL solutionUrl = JunitTestHelper.class.getResource("file_resources/solution_1.txt");
		File solutionFile = new File(solutionUrl.toURI());
		gtaConfig.uploadSolution("The Best Solution", solutionFile);
		
		gtaConfig
			.selectAssessment()
			.setAssessmentOptions(0.0f, 6.0f, 4.0f)
			.saveAssessmentOptions();
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.importMembers()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		ryomouTask
			.assertAssignmentAvailable()
			.selectTask(1)
			.assertTask("Individual Task 2 beta")
			.assertSubmissionAvailable();
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submit1File = new File(submit1Url.toURI());
		String submittedFilename = "personal_solution.html";
		String submittedText = "This is my solution";
		ryomouTask
			.submitFile(submit1File)
			.submitText(submittedFilename, submittedText)
			.submitDocuments();
		
		//back to author
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("personal_solution.html")
			.assertSubmittedDocument("submit_2.txt")
			.uploadCorrection(correctionFile)
			.needRevision();
		
		//participant add a revised document
		URL revisionUrl = JunitTestHelper.class.getResource("file_resources/submit_3.txt");
		File revisionFile = new File(revisionUrl.toURI());
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.submitRevisedFile(revisionFile)
			.submitRevision();
		
		//back to author
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.assertRevision("submit_3.txt")
			.closeRevisions()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.assertPassed()
			.openSolutions()
			.assertSolution("solution_1.txt");
	}
	
	/**
	 * An author create a course for a task with some custom
	 * settings, assignment and solution steps are disabled,
	 * but grading is selected with score and
	 * passed is automatically calculated.</br>
	 * It had a participant which goes through the workflow,
	 * submits 2 documents, one with the embedded editor,
	 * one with the upload mechanism.</br>
	 * The author reviews the documents, uploads a correction and
	 * want a revision.</br>
	 * The assessed participant upload a revised document.</br>
	 * The author sees it and close the revisions process, use
	 * the assessment tool to set the score.</br>
	 * The participant checks if she successfully passed the task.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskWithoutAssignment(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-task-alt-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Individual task 2";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.enableAssignment(false)
			.enableSolution(false)
			.saveWorkflow();
		
		gtaConfig
			.selectAssessment()
			.setAssessmentOptions(0.0f, 6.0f, 4.0f)
			.saveAssessmentOptions();
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.importMembers()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		ryomouTask
			.assertSubmissionAvailable();
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submit1File = new File(submit1Url.toURI());
		String submittedFilename = "personal_solution.html";
		String submittedText = "This is my solution";
		ryomouTask
			.submitFile(submit1File)
			.submitText(submittedFilename, submittedText)
			.submitDocuments();
		
		//back to author
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("personal_solution.html")
			.assertSubmittedDocument("submit_2.txt")
			.uploadCorrection(correctionFile)
			.needRevision();
		
		//participant add a revised document
		URL revisionUrl = JunitTestHelper.class.getResource("file_resources/submit_3.txt");
		File revisionFile = new File(revisionUrl.toURI());
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.submitRevisedFile(revisionFile)
			.submitRevision();
		
		//back to author
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.assertRevision("submit_3.txt")
			.closeRevisions()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.assertPassed();
	}
	
	/**
	 * An author create a course for a task with some custom
	 * settings, the following steps are not selected: review
	 * (and revisions) and solutions. The assignment is automatic.
	 * The assessment is set to passed automatically.</br>
	 * It had a participant which see the assignment and submits
	 * 2 documents, one with the embedded editor,
	 * one with the upload mechanism.</br>
	 * The author reviews the documents and use
	 * the assessment tool to set the score.</br>
	 * The participant checks if she successfully passed the task.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskWithIndividualScoreNoRevision(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-individual-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Individual task 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.enableSolution(false)
			.enableReview(false)
			.saveWorkflow()
			.selectAssignment();
		
		URL task1Url = JunitTestHelper.class.getResource("file_resources/task_1_a.txt");
		File task1File = new File(task1Url.toURI());
		gtaConfig.uploadTask("Individual Task 1 alpha", task1File);
		
		URL task2Url = JunitTestHelper.class.getResource("file_resources/task_1_b.txt");
		File task2File = new File(task2Url.toURI());
		gtaConfig
			.uploadTask("Individual Task 2 beta", task2File)
			.enableAutoAssignment(true)
			.saveTasks();
		
		gtaConfig
			.selectAssessment()
			.setAssessmentOptions(0.0f, 6.0f, 4.0f)
			.saveAssessmentOptions();
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.importMembers()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.clickTree()
			.assertWithTitleSelected(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the task which is automatically assigned
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.assertWithTitleSelected(gtaNodeTitle);
		OOGraphene.waitAndCloseBlueMessageWindow(ryomouBrowser);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		ryomouTask
			.assertAssignmentAvailable()
			.assertTask("Individual Task")
			.assertSubmissionAvailable();
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submit1File = new File(submit1Url.toURI());
		String submittedFilename = "personal_solution.html";
		String submittedText = "This is my solution";
		ryomouTask
			.submitFile(submit1File)
			.submitText(submittedFilename, submittedText)
			.submitDocuments();
		
		//back to author
		coursePage
			.clickTree()
			.assertWithTitleSelected(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("personal_solution.html")
			.assertSubmittedDocument("submit_2.txt")
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.clickTree()
			.assertWithTitleSelected(gtaNodeTitle);
		ryomouTask
			.assertPassed();
	}
	
	/**
	 * Create an assessment course element, add two users to the course
	 * and assesses them with the bulk assessment tool. The 2 users
	 * log in and check their results.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void bulkAssessment(@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-Assessment-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String assessmentNodeTitle = "Assessment CE";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("ms")
			.nodeTitle(assessmentNodeTitle);
		
		//configure assessment
		AssessmentCEConfigurationPage assessmentConfig = new AssessmentCEConfigurationPage(browser);
		assessmentConfig
			.selectConfigurationWithRubric()
			.setRubricScore(0.1f, 10.0f, 5.0f);
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
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.publish();// publish the course
		
		//go to members management
		MembersPage members = courseRuntime
			.members();
		members
			.importMembers()
			.setMembers(ryomou, kanu)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		BulkAssessmentData[] data = new BulkAssessmentData[] {
			new BulkAssessmentData(ryomou, 8.0f, null, "Well done"),
			new BulkAssessmentData(kanu, 4.0f, null, "Need more work")
		};
		
		members
			.clickToolbarBack()
			.assessmentTool()
			.bulk()
			.data(data)
			.nextData()
			.nextColumns()
			.nextValidation()
			.finish();
		
		//Ryomou login
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		ryomouCourse
			.clickTree()
			.selectWithTitle(assessmentNodeTitle);
		
		//Second login
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu)
			.resume();
		
		NavigationPage kanuNavBar = NavigationPage.load(kanuBrowser);
		kanuNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment kanuCourse = new CoursePageFragment(kanuBrowser);
		kanuCourse
			.clickTree()
			.selectWithTitle(assessmentNodeTitle);
		
		//Ryomou -> passed
		WebElement passedEl = ryomouBrowser.findElement(By.cssSelector("tr.o_state.o_passed"));
		Assert.assertTrue(passedEl.isDisplayed());
		//Kanu -> failed
		WebElement failedEl = kanuBrowser.findElement(By.cssSelector("tr.o_state.o_failed"));
		Assert.assertTrue(failedEl.isDisplayed());
	}
	

	/**
	 * An author create a course for a task with the some custom
	 * settings, all steps are selected, grading with score and
	 * passed automatically calculated, the task is optional,
	 * automatically assigned 2 tasks, 1 solution...</br>
	 * It had 2 participants. One of them goes through the workflow,
	 * accept to become a task, submits a document with the upload
	 * mechanism.</br>
	 * The author reviews the documents and accept them, uses
	 * the assessment tool to set the score.</br>
	 * The participant checks if she successfully passed the task
	 * and if it can see the proposed solution.
	 * 
	 * @param authorLoginPage The login page
	 * @param ryomouBrowser A browser for the student
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskOptionalWithIndividualScore(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-auto-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Individual task 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		// default on root node -> select first child
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.optional(true)
			.saveWorkflow()
			.selectAssignment();
		
		URL task1Url = JunitTestHelper.class.getResource("file_resources/task_1_a.txt");
		File task1File = new File(task1Url.toURI());
		gtaConfig.uploadTask("Individual Task 1 alpha", task1File);
		
		URL task2Url = JunitTestHelper.class.getResource("file_resources/task_1_b.txt");
		File task2File = new File(task2Url.toURI());
		gtaConfig
			.uploadTask("Individual Task 2 beta", task2File)
			.enableAutoAssignment(true)
			.saveTasks();
		
		gtaConfig
			.selectAssessment()
			.setAssessmentOptions(0.0f, 6.0f, 4.0f)
			.saveAssessmentOptions();
		
		URL solutionUrl = JunitTestHelper.class.getResource("file_resources/solution_1.txt");
		File solutionFile = new File(solutionUrl.toURI());
		gtaConfig
			.selectSolution()
			.uploadSolution("A possible solution", solutionFile);
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.importMembers()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task with auto select first node
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree();
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		ryomouTask
			.assertAssignmentAvailable()
			.confirmOptionalTask()
			.assertTask("Individual Task")
			.assertSubmissionAvailable();
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submit1File = new File(submit1Url.toURI());
		ryomouTask
			.submitFile(submit1File)
			.submitDocuments();
		
		//back to author
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("submit_2.txt")
			.reviewed()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.assertPassed()
			.openSolutions()
			.assertSolution("solution_1.txt");
	}
	
	/**
	 * An author create a course with an heavy customized task
	 * course element. Assignment and submission are disabled.
	 * The participant doesn't to interact with the course, the
	 * author / coach upload a correction and marks the task as
	 * reviewed and uses the assessment tool to set the score.<br>
	 * The participant checks if she successfully passed the task
	 * and if it can see the proposed solution.
	 * 
	 * @param authorLoginPage The login page
	 * @param ryomouBrowser A browser for the student
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskWithoutSubmission(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-auto-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Individual task 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.enableAssignment(false)
			.enableSubmission(false)
			.saveWorkflow();
		
		gtaConfig
			.selectAssessment()
			.setAssessmentOptions(0.0f, 6.0f, 4.0f)
			.saveAssessmentOptions();
		
		URL solutionUrl = JunitTestHelper.class.getResource("file_resources/solution_1.txt");
		File solutionFile = new File(solutionUrl.toURI());
		gtaConfig
			.selectSolution()
			.uploadSolution("A possible solution", solutionFile);
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.importMembers()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);

		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.uploadCorrection(correctionFile)
			.reviewed()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		//participant checks she passed the task
		ryomouTask
			.assertPassed()
			.openSolutions()
			.assertSolution("solution_1.txt");
	}
	
	
	/**
	 * An author create a course with an heavy customized task
	 * course element. Assignment and submission are disabled.
	 * The task is optional. The participant doesn't need to
	 * interact with the course, the author / coach upload a
	 * correction and marks the task as reviewed and uses the
	 * assessment tool to set the score.<br>
	 * The participant checks if she successfully passed the task
	 * and if it can see the proposed solution.
	 * 
	 * @param authorLoginPage The login page
	 * @param ryomouBrowser A browser for the student
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskOptionalWithoutSubmission(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");
		
		LoginPage authorLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-auto-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Individual task 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.optional(true)
			.enableAssignment(false)
			.enableSubmission(false)
			.enableSolutionForAll(true)
			.saveWorkflow();
		
		gtaConfig
			.selectAssessment()
			.setAssessmentOptions(0.0f, 6.0f, 4.0f)
			.saveAssessmentOptions();
		
		URL solutionUrl = JunitTestHelper.class.getResource("file_resources/solution_1.txt");
		File solutionFile = new File(solutionUrl.toURI());
		gtaConfig
			.selectSolution()
			.uploadSolution("A possible solution", solutionFile);
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.importMembers()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.clickTree()
			.selectWithTitle(gtaNodeTitle);

		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		participantToCoach
			.selectIdentityToCoach(ryomou)
			.openRevisionsStep()
			.uploadCorrection(correctionFile)
			.reviewed()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou)
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(ryomouBrowser);
		//participant checks she passed the task
		ryomouTask
			.assertPassed()
			.openSolutions()
			.assertSolution("solution_1.txt");
	}
}
