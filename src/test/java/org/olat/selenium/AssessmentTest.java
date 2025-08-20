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
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.util.CodeHelper;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.AssessmentCEConfigurationPage;
import org.olat.selenium.page.course.AssessmentInspectionConfigurationPage;
import org.olat.selenium.page.course.AssessmentInspectionPage;
import org.olat.selenium.page.course.AssessmentModePage;
import org.olat.selenium.page.course.AssessmentPage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.BadgeClassesPage;
import org.olat.selenium.page.course.BadgesAdminPage;
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
import org.olat.selenium.page.qti.QTI21ConfigurationCEPage;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class AssessmentTest extends Deployments {

	private WebDriver browser = getWebDriver(0);
	@ArquillianResource
	private URL deploymentUrl;

	
	/**
	 * An author upload a SCORM resource, create a course and use the
	 * SCORM within. It publish the course, add a participant to the
	 * course. The participant log in, select the course above, run
	 * the SCORM and finish it.<br>
	 * At the end, the author go to the assessment tool and check that
	 * the participant has successfully passed the test.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void scormCourseWithAssessment()
	throws IOException, URISyntaxException {
		WebDriver ryomouBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a SCORM package
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
			.selectTabScormContent()
			.chooseScorm(scormTitle);

		//publish the course
		courseEditor
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.cleanBlueBox()
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
			.loginAs(ryomou.getLogin(), ryomou.getPassword());
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//open the course and see the test start page
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		ryomouCourse
			.tree()
			.assertWithTitleSelected(scormNodeTitle);
		
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
			.openAuthoringEnvironment()
			.openResource(courseTitle);
		
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
	 * An author create a course with a test. It prepares a template
	 * configuration for an assessment inspection.<br>
	 * A participant makes the test. The author prepare the inspection
	 * with a the right time period.<br>
	 * The participant log in again and see the results, and close the
	 * inspection.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void assessmentInspection()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
			
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Janis");
		UserVO author = new UserRestClient(deploymentUrl).createAuthor("Rim");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Inspection 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course inspection " + UUID.randomUUID();
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
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the test start page
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.tree()
			//check that the title of the start page of test is correct
			.assertWithTitleSelected(testNodeTitle);
		
		// Add the participant
		MembersPage members = courseRuntime.members();
		members.quickAdd(participant);
		
		// Prepare an assessment inspection template
		String inspectionName = "View " + UUID.randomUUID();
		courseRuntime
			.assessmentInspectionConfiguration()
			.createAssessmentInspection(inspectionName)
			.assertOnAssessmentInspectionInList(inspectionName);
		
		// Participant log in 
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		// Go to the test
		CoursePageFragment participantTestCourse = new CoursePageFragment(participantBrowser);
		participantTestCourse
			.tree()
			.assertWithTitleSelected(testNodeTitle);
		// Pass the test
		QTI21Page.getQTI21Page(participantBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		// Log out
		new UserToolsPage(participantBrowser)
			.logout();
		
		// Author prepare an assessment inspection window
		AssessmentInspectionConfigurationPage configurationPage = courseRuntime
			.assessmentTool()
			.assertOnInspectionsMenuItem()
			.selectAssessmentInspections();
		
		LocalDateTime now = LocalDateTime.now().minusMinutes(2);
		int hour = now.getHour();
		int minute = now.getMinute();
		
		// Authorize an inspection for the participant
		configurationPage
			.addMember()
			.selectCourseElement(testNodeTitle)
			.selectUser(participant)
			.configuration(hour, minute)
			.contact();
		configurationPage
			.assertActiveOnParticipantInList(participant);
		
		// Participant log in 
		participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		AssessmentInspectionPage inspectionPage = new AssessmentInspectionPage(participantBrowser);
		inspectionPage
			.assertOnStartInspection(testNodeTitle)
			.startInspection();
		
		// Results
		new QTI21Page(participantBrowser)
			.assertOnAssessmentResults();
		
		inspectionPage
			.closeInspection()
			.confirmCloseInspection();
		
		// Land on "My courses"
		By myEntriesBy = By.cssSelector("#o_main.o_sel_my_repository_entries");
		OOGraphene.waitElement(myEntriesBy, participantBrowser);
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
	public void assessmentModeManual()
	throws IOException, URISyntaxException {
		WebDriver ryomouBrowser = getWebDriver(1);
		WebDriver kanuBrowser = getWebDriver(2);
			
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
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the test start page
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.tree()
			//check that the title of the start page of test is correct
			.assertWithTitleSelected(testNodeTitle);
		
		//add Ryomou and Kanu as a course member
		MembersPage members = courseRuntime.members();
		members.quickAdd(ryomou);
		members.quickAdd(kanu);
		
		//Kanu log in 
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword());
		
		// prepare and start an assessment
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 2);
		Date begin = cal.getTime();
		cal.add(Calendar.MINUTE, 5);
		Date end = cal.getTime();
		String assessmentName = "Assessment-" + UUID.randomUUID();
		courseRuntime
			.assessmentConfiguration()
			.createAssessmentMode()
			.editAssessment(assessmentName, begin, end, 0, true)
			.saveGeneral(assessmentName)
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
			.assertOnStartAssessment()
			.startAssessment();
		//go to the test
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.tree()
			.assertWithTitleSelected(testNodeTitle);
		//pass the test
		QTI21Page.getQTI21Page(ryomouBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
	
		//Kanu makes the test
		AssessmentModePage kanuAssessment = new AssessmentModePage(kanuBrowser)
			.assertOnStartAssessment()
			.startAssessment();
		//go to the test
		CoursePageFragment kanuTestCourse = new CoursePageFragment(kanuBrowser);
		kanuTestCourse
			.tree()
			.assertWithTitleSelected(testNodeTitle);
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
		kanuAssessment.backToOpenOlat();
		ryomouAssessment.backToOpenOlat();
		kanuAssessment.assertGuardDisappears();
		ryomouAssessment.assertGuardDisappears();
		
		//Author check if they pass the test
		navBar
			.openAuthoringEnvironment()
			.openResource(courseTitle);	
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
	 * An author upload a test, create a course with a test course element,
	 * publish the course, add a participant to the course, configure an
	 * assessment with one minute follow-up.<br />
	 * The participant logs in before the assessment started. It pass the test.
	 * The author ends the assessment. The participant waits the end of the
	 * assessment and go back to normal activities. The author checks that it
	 * pass the test in the assessment tool.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void assessmentModeManualWithFollowUp()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
			
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "QTI Test 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course With Follow-Up " + UUID.randomUUID();
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
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the test start page
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.tree()
			//check that the title of the start page of test is correct
			.assertWithTitleSelected(testNodeTitle);
		
		// Add the participant
		MembersPage members = courseRuntime.members();
		members.quickAdd(participant);
		
		// Participant log in 
		LoginPage participantLogin = LoginPage.load(participantBrowser, deploymentUrl);
		participantLogin
			.loginAs(participant.getLogin(), participant.getPassword());
		
		// prepare and start an assessment
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 2);
		Date begin = cal.getTime();
		cal.add(Calendar.MINUTE, 5);
		Date end = cal.getTime();
		String assessmentName = "Assessment-" + UUID.randomUUID();
		courseRuntime
			.assessmentConfiguration()
			.createAssessmentMode()
			.editAssessment(assessmentName, begin, end, 1, true)
			.saveGeneral(assessmentName)
			.clickToolbarBack()
			.assertAssessmentModeList()
			.start(assessmentName)
			.confirmStart();
	
		// Participant makes the test
		AssessmentModePage participantAssessment = new AssessmentModePage(participantBrowser)
			.assertOnStartAssessment()
			.startAssessment();
		// Go to the test
		CoursePageFragment participantTestCourse = new CoursePageFragment(participantBrowser);
		participantTestCourse
			.tree()
			.assertWithTitleSelected(testNodeTitle);
		// Pass the test
		QTI21Page.getQTI21Page(participantBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		// Author ends the test
		courseRuntime
			.assessmentConfiguration()
			.stop(assessmentName)
			.confirmStop();
		
		participantAssessment
			.waitBackToOpenOlat(180)
			.backToOpenOlat()
			.assertGuardDisappears();
		
		// Author check if they pass the test
		navBar
			.openAuthoringEnvironment()
			.openResource(courseTitle);
		// Open the assessment tool
		AssessmentToolPage assessmentTool = new CoursePageFragment(browser)
			.assessmentTool();		
		assessmentTool
			.users()
		// Check that the participant has passed the test
			.assertOnUsers(participant)
			.selectUser(participant)
			.assertPassed(participant);
	}
	
	
	/**
	 * An author upload a test, create a course with a test course element,
	 * publish the course, add a participant to the course, configure a first
	 * assessment.<br />
	 * The participant logs in before the assessment started. It pass the test.
	 * The author ends the assessment. The participant waits the end of the
	 * assessment. The author configures a second assessment and starts it.
	 * The participant want to go back to OpenOlat but it is catched in the
	 * second assessment mode. It waits patiently the author ends the second
	 * mode and goes back to a normal activity.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void assessmentModesConsecutives()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
			
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "QTI Test 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course With Follow-Up " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type test with the QTI 2.1 test that we upload above
		String testNodeOneTitle = "Test one QTI";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeOneTitle)
			.selectTabVisibility()
			.setAssessmentMode()
			.save();
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);
		
		String testNodeTwoTitle = "Test two QTI";
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTwoTitle)
			.selectTabVisibility()
			.setAssessmentMode()
			.save();
		
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the test start page
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.tree()
			//check that the title of the start page of test is correct
			.assertWithTitleSelected(testNodeOneTitle);
		
		// Add the participant
		MembersPage members = courseRuntime.members();
		members.quickAdd(participant);
		
		// Participant log in 
		LoginPage participantLogin = LoginPage.load(participantBrowser, deploymentUrl);
		participantLogin
			.loginAs(participant.getLogin(), participant.getPassword());
		
		// prepare and start an assessment
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 2);
		Date begin = cal.getTime();
		cal.add(Calendar.MINUTE, 5);
		Date end = cal.getTime();
		String assessmentOneName = "First one " + UUID.randomUUID();
		courseRuntime
			.assessmentConfiguration()
			.createAssessmentMode()
			.editAssessment(assessmentOneName, begin, end, 0, true)
			.saveGeneral(assessmentOneName)
			.editRestrictions(testNodeOneTitle)
			.saveRestrictions(assessmentOneName)
			.clickToolbarBack()
			.assertAssessmentModeList()
			.start(assessmentOneName)
			.confirmStart();
	
		// Participant makes the test
		AssessmentModePage participantAssessment = new AssessmentModePage(participantBrowser)
			.assertOnStartAssessment()
			.startAssessment();
		// Go to the test
		CoursePageFragment participantTestCourse = new CoursePageFragment(participantBrowser);
		participantTestCourse
			.tree()
			.assertWithTitleSelected(testNodeOneTitle)
			.assertTitleNotExists(testNodeTwoTitle);
		// Pass the test
		QTI21Page.getQTI21Page(participantBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		// Author ends the test
		AssessmentModePage assessmentModes = courseRuntime
			.assessmentConfiguration()
			.stop(assessmentOneName)
			.confirmStop();
		
		// Participant waits
		participantAssessment
			.waitBackToOpenOlat();
		
		// Author prepare and start a second assessment
		cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 3);
		Date beginAgain = cal.getTime();
		cal.add(Calendar.MINUTE, 6);
		Date endAgain = cal.getTime();
		String assessmentSecondName = "Second mode " + UUID.randomUUID();
		courseRuntime
			.assessmentConfiguration()
			.createAssessmentMode()
			.editAssessment(assessmentSecondName, beginAgain, endAgain, 0, true)
			.saveGeneral(assessmentSecondName)
			.editRestrictions(testNodeTwoTitle)
			.saveRestrictions(assessmentSecondName)
			.clickToolbarBack()
			.assertAssessmentModeList()
			.start(assessmentSecondName)
			.confirmStart();
		
		OOGraphene.waitingLong();
		
		// Go out of the first assessment mode
		participantAssessment
			.backToOpenOlat();
		// Start the second assessment mode
		participantAssessment
			.assertOnStartAssessment()
			.startAssessment();
		
		participantTestCourse
			.tree()
			.assertWithTitleSelected(testNodeTwoTitle)
			.assertTitleNotExists(testNodeOneTitle);
		// Pass the test
		QTI21Page.getQTI21Page(participantBrowser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		// Author stops the second assessment mode
		assessmentModes
			.stop(assessmentSecondName)
			.confirmStop();
		
		// Participant waits
		participantAssessment
			.waitBackToOpenOlat()
			.backToOpenOlat()
			.assertGuardDisappears();
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
	public void certificatesManuallyGenerated()
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
			.enableValidity()
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
		LoginPage reiLoginPage = LoginPage.load(browser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword());
				
		//open the efficiency statements
		UserToolsPage reiUserTools = new UserToolsPage(browser);
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
	public void certificatesGeneratedByTest()
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
		CourseEditorPageFragment editor = courseRuntime
			.edit()
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(testTitle);
		
		editor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
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
			.enableValidity()
			.save();
		courseSetting
			.clickToolbarBack();
		
		//Participant log in
		LoginPage reiLoginPage = LoginPage.load(browser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword());
		
		//open the course
		NavigationPage reiNavBar = NavigationPage.load(browser);
		reiNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the test
		CoursePageFragment reiTestCourse = new CoursePageFragment(browser);
		reiTestCourse
			.tree()
			.assertWithTitleSelected(testNodeTitle);
		//pass the test
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		//open the efficiency statements
		UserToolsPage reiUserTools = new UserToolsPage(browser);
		reiUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnCertificateAndStatements(courseTitle)
			.selectStatement(courseTitle)
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
			.addMember()
			.importList()
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
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		
		course
			.changeStatus(RepositoryEntryStatusEnum.published);
	
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant log in and go directly to the first test
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		
		participantLoginPage
			.loginAs(participant1.getLogin(), participant1.getPassword());
		
		//open the course
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(zipTitle);
		
		//go to the test
		CoursePageFragment certificationCourse = new CoursePageFragment(browser);
		certificationCourse
			.tree()
			.assertWithTitleSelected("Test 1");
		//pass the test
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		//open the efficiency statements
		String certificateTitle = zipTitle;
		UserToolsPage participantUserTools = new UserToolsPage(browser);
		participantUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnCertificateAndStatements(certificateTitle)
			.selectStatement(certificateTitle)
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
			.loginAs(participant2.getLogin(), participant2.getPassword());
		
		//open the course
		NavigationPage participant2NavBar = NavigationPage.load(browser);
		participant2NavBar
			.openMyCourses()
			.select(zipTitle);
		
		//go to the test
		CoursePageFragment certification2Course = new CoursePageFragment(browser);
		certification2Course
			.tree()
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
			.assertOnCourseDetails("Certificates", true)
			.assertOnCourseDetails("Struktur 3", true)
			.assertOnCourseDetails("Test 3", true);
	}
	

	/**
	 * An author create a course, configure a badge and give
	 * a badge to a participant. The participant opens
	 * the course and see its badge.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void createBadgeManually()
			throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Jeremy");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		//create a course
		String courseTitle = ("Badges " + UUID.randomUUID()).substring(0, 23);
		CoursePageFragment course = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		CourseSettingsPage courseSetting = course
			.settings();
		courseSetting
			.certificates()
			.enableBadges();
		courseSetting
			.clickToolbarBack();
		
		// Add a participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		course
			.changeStatus(RepositoryEntryStatusEnum.published);

		String badgeClassName = "Star on shield";
		String badgeDescription = "You pass a selenium test.";
		String criteriaSummary = "Pass selenium test";
		
		BadgeClassesPage badges = course
			.badgesAdministration()
			.createBadgeClass()
			.selectClass(badgeClassName)
			.nextToCustomization()
			.customize("Selenium the test")
			.nextToCriteria()
			.criteria(criteriaSummary)
			.nextToDetails()
			.details(badgeDescription)
			.nextToSummary()
			.assertOnSummary(badgeClassName)
			.assertOnSummary(badgeDescription)
			.assertOnSummary(criteriaSummary)
			.nextToRecipients()
			.finish();
		badges
			.assertOnTable(badgeClassName);
		
		//open the assessment tool
		course
			.assessmentTool()
			.users()
			.assertOnUsers(participant)
			.selectUser(participant)
			.awardBadge()
			.assertOnBadge(badgeClassName)
			.clickToolbarRootCrumb();
		
		// Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment badgeCourse = new CoursePageFragment(browser);
		badgeCourse
			.myBadges()
			.assertOnBadge(badgeClassName);
	}
	

	/**
	 * An author create a course with a test ands configure a badge to be
	 * given if the course is passed. The participant opens
	 * the course, pass the test and sees its badge.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void createBadgeAuto()
			throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Jeremy");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = ("Badges " + UUID.randomUUID()).substring(0, 23);
		CoursePageFragment course = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type test with the QTI 2.1 test that we upload above
		String testNodeTitle = "Test QTI 2.1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		courseEditor
			.clickToolbarBack();
		
		CourseSettingsPage courseSetting = course
			.settings();
		courseSetting
			.certificates()
			.enableBadges();
		courseSetting
			.clickToolbarBack();
		
		// Add a participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();

		String badgeClassName = "Star on shield";
		String badgeDescription = "You pass a selenium test.";
		String criteriaSummary = "Pass selenium test";
		
		BadgeClassesPage badges = course
			.badgesAdministration()
			.createBadgeClass()
			.selectClass(badgeClassName)
			.nextToCustomization()
			.customize("Selenium the test")
			.nextToCriteria()
			.criteria(criteriaSummary)
			.criteriaAuto()
			.criteraCoursePassedAsFirstRule()
			.nextToDetails()
			.details(badgeDescription)
			.nextToSummary()
			.assertOnSummary(badgeClassName)
			.assertOnSummary(badgeDescription)
			.assertOnSummary(criteriaSummary)
			.nextToRecipients()
			.finish();
		badges
			.assertOnTable(badgeClassName);
		
		// Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);

		CoursePageFragment badgeCourse = new CoursePageFragment(browser);
		badgeCourse
			.assertOnLearnPathNodeInSequence(testNodeTitle);
		
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		badgeCourse
			.assertOnLearnPathNodeDone(testNodeTitle)
			.myBadges()
			.assertOnBadge(badgeClassName);
	}
	
	
	/**
	 * An author create a course with an assessment element and a test.
	 * It configures a first badge to be granted if the assessment is passed
	 * and second one if the first one is passed and the test.<br>
	 * It add a participant to the course and set the assessment as passed.<br>
	 * A participant log in and happily find a first badge in its collection.
	 * It solves successfully the test and gets its second badge.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void createBadgeAuto2Levels()
			throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Jeremy");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = ("Badges " + UUID.randomUUID()).substring(0, 23);
		CoursePageFragment course = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		// Create an assessment course element
		String assessmentNodeTitle = "Assessment for Badge";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("ms")
			.nodeTitle(assessmentNodeTitle);
		
		//configure assessment
		AssessmentCEConfigurationPage assessmentConfig = new AssessmentCEConfigurationPage(browser);
		assessmentConfig
			.selectConfigurationWithRubric()
			.setScore(0.1f, 10.0f, 5.0f)
			.save();
		
		//Create a course element of type test with the QTI 2.1 test that we upload above
		String testNodeTitle = "Badge QTI 2.1";
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		courseEditor
			.clickToolbarBack();
		
		CourseSettingsPage courseSetting = course
			.settings();
		courseSetting
			.certificates()
			.enableBadges();
		courseSetting
			.clickToolbarBack();

		String firstBadgeClassName = "Star on shield";
		String badgeDescription = "You pass a selenium test.";
		String criteriaSummary = "Pass selenium test";
		
		// Add a first badge base on the assessment node
		BadgeClassesPage badges = course
			.badgesAdministration()
			.createBadgeClass()
			.selectClass(firstBadgeClassName)
			.nextToCustomization()
			.customize("Selenium the assessment")
			.nextToCriteria()
			.criteria(criteriaSummary)
			.criteriaAuto()
			.criteriaPassedCourseElementAsFirstRule(assessmentNodeTitle)
			.nextToDetails()
			.details(badgeDescription)
			.nextToSummary()
			.assertOnSummary(firstBadgeClassName)
			.assertOnSummary(badgeDescription)
			.assertOnSummary(criteriaSummary)
			.finish();
		badges
			.assertOnTable(firstBadgeClassName);
		
		// Add a second badge dependent of the first one and the QTI test
		String secondBadgeClassName = "Cup on circle";
		badges = course
			.badgesAdministration()
			.createBadgeClass()
			.startingWithNewBadgeClass()
			.nextToClasses()
			.selectClass(secondBadgeClassName)
			.nextToCustomization()
			.nextToCriteria()
			.criteria(criteriaSummary)
			.criteriaAuto()
			.criteriaPassedCourseElementAsFirstRule(testNodeTitle)
			.criteriaPassedBadgeAsAdditionalRule(firstBadgeClassName)
			.nextToDetails()
			.details(badgeDescription)
			.nextToSummary()
			.assertOnSummary(secondBadgeClassName)
			.assertOnSummary(badgeDescription)
			.assertOnSummary(criteriaSummary)
			.finish();
		badges
			.assertOnTable(secondBadgeClassName);
		
		// Add a participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		// Make it passed the assessment course element
		members
			.clickToolbarBack()
			.assessmentTool()
			.users()
			.assertOnUsers(participant)
			.selectUser(participant)
			.selectUsersCourseNode(assessmentNodeTitle)
			.setAssessmentScore(8.0f)
			.closeAndPublishAssessment()
			.assertUserPassedCourseNode(assessmentNodeTitle);
		
		// Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);

		CoursePageFragment badgeCourse = new CoursePageFragment(browser);
		badgeCourse
			.assertOnLearnPathNodeDone(assessmentNodeTitle)
			.myBadges()
			.assertOnBadge(firstBadgeClassName)
			.assertNotOnBadge(secondBadgeClassName)
			.clickToolbarBack();
		
		badgeCourse
			.tree()
			.selectWithTitle(testNodeTitle);
		
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		badgeCourse
			.assertOnLearnPathNodeDone(testNodeTitle)
			.myBadges()
			.assertOnBadge(firstBadgeClassName)
			.assertOnBadge(secondBadgeClassName);
	}
	
	
	/**
	 * An administrator creates a global badge, awards the badge
	 * to a recipient and the participant log in and goes
	 * to its user tools to see the badge.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void createBadgeGlobalManually()
			throws IOException, URISyntaxException {
		UserVO admin = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Adelaide");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(admin.getLogin(), admin.getPassword())
			.resume();
		
		BadgesAdminPage adminPage = NavigationPage.load(browser)
			.openAdministration()
			.openBadges()
			.openGlobalBadges();
		
		String badgeClassName = "Cup on shield";
		String nameSuffix = " " + CodeHelper.getUniqueID();
		String badgeFullName = badgeClassName + nameSuffix;
		String badgeDescription = "You become a selenium crack at " + UUID.randomUUID();
		String criteriaSummary = "Global selenium badge";
		
		// Create a new class
		BadgeClassesPage badgesPage = adminPage.createBadgeClass()
			.selectClass(badgeClassName)
			.nextToCustomization()
			.customize("Selenium the test")
			.nextToCriteria()
			.criteria(criteriaSummary)
			//Manual already selected
			.nextToDetails()
			.details(nameSuffix, badgeDescription)
			.nextToSummary()
			.assertOnSummary(badgeClassName)
			.assertOnSummary(badgeDescription)
			.assertOnSummary(criteriaSummary)
			.finish();
		
		badgesPage
			.assertOnTable(badgeClassName);
		
		adminPage
			.awardNewBadge(badgeFullName)
			.searchRecipient(participant)
			.nextConfirmation()
			.finish();
		
		adminPage
			.openIssuedBadges()
			.assertIssuedBadge(badgeFullName, participant);
		
		// Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		new UserToolsPage(browser)
			.openUserToolsMenu()
			.openBadges()
			.assertOnBadge(badgeClassName);
	}
	

	/**
	 * An administrator creates a course (learn path) with a test, add a
	 * participant to the course. Than it creates a global badge with a
	 * rule based on the course (passed).<br>
	 * The participant log in, passes the course successfully and goes
	 * to its user tools to see the badge.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void createBadgeGlobalAuto()
			throws IOException, URISyntaxException {
		UserVO admin = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Adelaide");
		
		//deploy the test
		URL testUrl = ArquillianDeployments.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		String testTitle = "E4Test-" + UUID.randomUUID();
		new RepositoryRestClient(deploymentUrl, admin).deployResource(new File(testUrl.toURI()), "-", testTitle);
		
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage.loginAs(admin.getLogin(), admin.getPassword())
			.resume();
		
		//create a course
		String courseTitle = "GBadge " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//create a course element of type test with the QTI 2.1 test that we upload above
		String testNodeTitle = "Test-QTI-2.1";
		CoursePageFragment courseRuntime = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = courseRuntime
			.edit()
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(testTitle);
		
		//publish the course
		editor
			.publish()
			.quickPublish();
		editor
			.clickToolbarBack();
		
		CourseSettingsPage courseSetting = courseRuntime
			.settings();
		courseSetting
			.certificates()
			.enableBadges()
			// Certificate
			.enableCertificates(true)
			.enableValidity()
			.save();
		courseSetting
			.clickToolbarBack();
		
		//add a participant to the course
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		// Make a global badge
		BadgesAdminPage adminPage = NavigationPage.load(browser)
			.openAdministration()
			.openBadges()
			.openGlobalBadges();
		
		String badgeClassName = "Cup on shield";
		String nameSuffix = " " + CodeHelper.getUniqueID();
		String badgeDescription = "You become an OpenOlat expert at " + UUID.randomUUID();
		String criteriaSummary = "Global OpenOlat badge";
		
		// Create a new class
		BadgeClassesPage badgesPage = adminPage.createBadgeClass()
			.selectClass(badgeClassName)
			.nextToCustomization()
			.customize("Selenium the test")
			.nextToCriteria()
			.criteria(criteriaSummary)
			.criteriaGlobalAuto()
			.criteriaGlobalPassedCourseAsFirstRule(courseTitle)
			.nextToDetails()
			.details(nameSuffix, badgeDescription)
			.nextToSummary()
			.assertOnSummary(badgeClassName)
			.assertOnSummary(badgeDescription)
			.assertOnSummary(criteriaSummary)
			.nextToRecipients()
			.finish();
		
		badgesPage
			.assertOnTable(badgeClassName);
		
		// Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);

		CoursePageFragment badgeCourse = new CoursePageFragment(browser);
		badgeCourse
			.assertOnLearnPathNodeInSequence(testNodeTitle);
		
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		new UserToolsPage(browser)
			.openUserToolsMenu()
			.openBadges()
			.assertOnBadge(badgeClassName);
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
	public void assessmentCourseElement()
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
			.setScore(0.1f, 10.0f, 5.0f)
			.save();
		//set the score / passed calculation in root node and publish
		CourseSettingsPage courseSettings = courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings();
		courseSettings
			.accessConfiguration()
			.setAccessToRegisteredUser();
		courseSettings
			.certificates()
			.enableCertificates(true)
			.enableValidity()
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
			.closeAndPublishAssessment()
			.assertUserPassedCourseNode(assessmentNodeTitle);
		
		//Ryomou login
		LoginPage ryomouLoginPage = LoginPage.load(browser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword());
		
		//see its beautiful efficiency statement
		UserToolsPage ryomouUserTools = new UserToolsPage(browser);
		ryomouUserTools
			.openUserToolsMenu()
			.openMyEfficiencyStatement()
			.assertOnEfficiencyStatmentPage()
			.assertOnStatement(courseTitle, true)
			.selectStatement(courseTitle)
			.assertOnCourseDetails(assessmentNodeTitle, true);
	}
	

	/**
	 * An author makes a course with an assessment course element. It
	 * selects the swiss grade system. It assess a student and gives a score,
	 * modify again the score. The student login and checks her grade.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void assessmentCourseElementWithGrades()
	throws IOException, URISyntaxException {
	
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-Assessment-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
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
			.setScore(0.0f, 10.0f, 5.0f)
			.enableGrade(true)
			.save()
			.editGradingScale()
			.selectSwissGradeSystem()
			.assertOnSwissNumericalGradeScale()
			.saveConfiguration();
		assessmentConfig
			.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.save();
		
		//go to members management
		CoursePageFragment courseRuntime = courseEditor.clickToolbarBack();
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(student, true)
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
			.assertOnUsers(student)
			.selectUser(student)
			.selectUsersCourseNode(assessmentNodeTitle)
			.setAssessmentScore(8.0f)
			.closeAndPublishAssessment()
			.assertUserSwissGradeCourseNode(assessmentNodeTitle, "5")
			.assertUserPassedCourseNode(assessmentNodeTitle)
			.selectUsersCourseNode(assessmentNodeTitle)
			.reopenAssessment()
			.updateAssessmentScore(10.0f)
			.closeAndPublishAssessment()
			.assertUserSwissGradeCourseNode(assessmentNodeTitle, "6")
			.assertUserPassedCourseNode(assessmentNodeTitle);
		
		// Student login
		LoginPage studentLoginPage = LoginPage.load(browser, deploymentUrl);
		studentLoginPage
			.loginAs(student.getLogin(), student.getPassword());
		
		NavigationPage studentNavBar = NavigationPage.load(browser);
		studentNavBar
			.openMyCourses()
			.select(courseTitle);

		// Go to the course element and check the 
		CoursePageFragment studentCourse = new CoursePageFragment(browser);
		studentCourse
			.tree()
			.assertWithTitleSelected(assessmentNodeTitle);
		
		new AssessmentPage(browser)
			.assertOnPassed()
			.assertOnSwissGrade("6");
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
	public void taskWithGroupsAndStandardSettings()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
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
			.createBusinessGroup("Group to task - 1,Group to task - 2,Group to task - 3")
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
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(participantBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(participantBrowser);
		ryomouTestCourse
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(participantBrowser);
		ryomouTask
			.assertAssignmentAvailable()
			.selectTask(taskName2)
			.assertSubmissionAvailable();
		
		//Participant 2 log in
		LoginPage kanuLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu);
		
		//open the course
		NavigationPage kanuNavBar = NavigationPage.load(participantBrowser);
		kanuNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment kanuTestCourse = new CoursePageFragment(participantBrowser);
		kanuTestCourse
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_1.txt");
		File submit1File = new File(submit1Url.toURI());
		String submittedFilename = "my_solution.html";
		String submittedText = "This is my solution";
		GroupTaskPage kanuTask = new GroupTaskPage(participantBrowser);
		kanuTask
			.assertTask(taskName2)
			.assertSubmissionAvailable()
			.submitFile(submit1File)
			.submitText(submittedFilename, submittedText)
			.submitDocuments();
		
		//back to author
		coursePage
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		GroupTaskToCoachPage groupToCoach = new GroupTaskToCoachPage(browser);
		groupToCoach
			.selectGroupsToCoach()
			.selectBusinessGroupToCoach("Group to task - 1")
			.assertSubmittedDocument("my_solution.html")
			.assertSubmittedDocument("submit_1.txt")
			.reviewed()
			.openGroupAssessment()
			.groupAssessment(Boolean.TRUE, null);
		
		//participant check if they passed
		kanuTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		kanuTask.assertPassed();
		
		ryomouTestCourse
			.tree()
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
	public void taskWithIndividualScoreAndRevision()
	throws IOException, URISyntaxException {
		WebDriver ryomouBrowser = getWebDriver(1);
		
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
			.addMember()
			.importList()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		
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
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("personal_solution.html")
			.assertSubmittedDocument("submit_2.txt")
			.uploadCorrection(correctionFile)
			.needRevision();
		
		//participant add a revised document
		URL revisionUrl = JunitTestHelper.class.getResource("file_resources/submit_3.txt");
		File revisionFile = new File(revisionUrl.toURI());
		ryomouTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.submitRevisedFile(revisionFile)
			.submitRevision();
		
		//back to author
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertRevision("submit_3.txt")
			.closeRevisions()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.tree()
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
	public void taskWithoutAssignment()
	throws IOException, URISyntaxException {
		WebDriver ryomouBrowser = getWebDriver(1);
		
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
			.addMember()
			.importList()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		
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
			.tree()
			.selectWithTitle(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("personal_solution.html")
			.assertSubmittedDocument("submit_2.txt")
			.uploadCorrection(correctionFile)
			.needRevision();
		
		//participant add a revised document
		URL revisionUrl = JunitTestHelper.class.getResource("file_resources/submit_3.txt");
		File revisionFile = new File(revisionUrl.toURI());
		ryomouTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.submitRevisedFile(revisionFile)
			.submitRevision();
		
		//back to author
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertRevision("submit_3.txt")
			.closeRevisions()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.tree()
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
	public void taskWithIndividualScoreNoRevision()
	throws IOException, URISyntaxException {
		WebDriver ryomouBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
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
			.addMember()
			.importList()
			.setMembers(ryomou)
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
			.loginAs(ryomou);
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the task which is automatically assigned
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.tree()
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
			.tree()
			.selectWithTitle(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		participantToCoach
			.assertOnVisitGreen()
			.selectIdentitiesToCoach()
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
	public void bulkAssessment()
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
			.setScore(0.1f, 10.0f, 5.0f)
			.save();
		//set the score / passed calculation in root node and publish
		courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save();
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.publish();// publish the course
		
		//go to members management
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
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
		LoginPage ryomouLoginPage = LoginPage.load(browser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouCourse = new CoursePageFragment(browser);
		ryomouCourse
			.tree()
			.assertWithTitleSelected(assessmentNodeTitle);
		
		//Ryomou -> passed
		By passedBy = By.cssSelector("div.o_state.o_passed");
		WebElement passedEl = OOGraphene.waitElement(passedBy, browser);
		Assert.assertTrue(passedEl.isDisplayed());
		
		//Second login
		LoginPage kanuLoginPage = LoginPage.load(browser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu);
		
		NavigationPage kanuNavBar = NavigationPage.load(browser);
		kanuNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment kanuCourse = new CoursePageFragment(browser);
		kanuCourse
			.tree()
			.assertWithTitleSelected(assessmentNodeTitle);
		
		//Kanu -> failed
		By failedBy = By.cssSelector("div.o_state.o_failed");
		WebElement failedEl = OOGraphene.waitElement(failedBy, browser);
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
	public void taskOptionalWithIndividualScore()
	throws IOException, URISyntaxException {
		WebDriver ryomouBrowser = getWebDriver(1);
		
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
			.addMember()
			.importList()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
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
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("submit_2.txt")
			.reviewed()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//participant checks she passed the task
		ryomouTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		ryomouTask
			.assertPassed()
			.openSolutions()
			.assertSolution("solution_1.txt");
	}
	

	/**
	 * An author create a course with an heavy customized task
	 * course element. Assignment, solutions and grading are
	 * disabled. The task is optional. The participant upload
	 * a document, the author / coach marks the task as needing
	 * some revisions, student uploads a revision, author reviews
	 * it and accept it.<br>
	 * The participant checks if the taks is done.
	 *  successfully passed the task
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskOptionalWithoutAssignment()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-opt-task-3-" + UUID.randomUUID();
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
			.optional(true)
			.enableAssignment(false)
			.enableSolution(false)
			.enableGrading(false)
			.saveWorkflow();
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
		
		membersPage
			.addMember()
			.importList()
			.setMembers(ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		//Participant log in
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(ryomou);
		
		//open the course
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment participantTestCourse = new CoursePageFragment(participantBrowser);
		participantTestCourse
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		
		GroupTaskPage participantTask = new GroupTaskPage(participantBrowser);
		participantTask
			.openSubmission()
			.assertSubmissionAvailable();
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_1.txt");
		File submit1File = new File(submit1Url.toURI());
		URL submit2Url = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submit2File = new File(submit2Url.toURI());
		participantTask
			.submitFile(submit1File)
			.submitFile(submit2File)
			.submitDocuments();
		
		//back to author
		coursePage
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		
		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertSubmittedDocument("submit_1.txt")
			.assertSubmittedDocument("submit_2.txt")
			.uploadCorrection(correctionFile)
			.needRevision();
		
		//participant add a revised document
		URL revisionUrl = JunitTestHelper.class.getResource("file_resources/revision_1.txt");
		File revisionFile = new File(revisionUrl.toURI());
		participantTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		participantTask
			.submitRevisedFile(revisionFile)
			.submitRevision();
		
		//back to author
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.assertRevision("revision_1.txt")
			.closeRevisions();
		
		//participant checks she passed the task
		participantTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		participantTask
			.assertRevisionDone();
	}
	
	/**
	 * An author create a course with an heavy customized task
	 * course element. Assignment, solutions and grading are
	 * disabled. The task is optional. It switches to participant
	 * and uploads two documents and finally checks that the
	 * submission is done.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskOptionalWithoutAssignmentRoleSwitch()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-opt-task-4-" + UUID.randomUUID();
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
			.optional(true)
			.enableAssignment(false)
			.enableSolution(false)
			.enableGrading(false)
			.saveWorkflow();
		
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		// Set the author as participant too
		CoursePageFragment coursePage = courseEditor.clickToolbarBack();
		coursePage
			.members()
			.selectMembers()
			.openMembership(author.getFirstName())
			.editRepositoryMembership(Boolean.TRUE)
			.saveMembership()
			.clickToolbarBack();

		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		// switch
		coursePage
			.switchRole(Role.participant);

		GroupTaskPage participantTask = new GroupTaskPage(browser);
		participantTask
			.openSubmission()
			.assertSubmissionAvailable();
		
		URL submit1Url = JunitTestHelper.class.getResource("file_resources/submit_1.txt");
		File submit1File = new File(submit1Url.toURI());
		URL submit2Url = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submit2File = new File(submit2Url.toURI());
		participantTask
			.submitFile(submit1File)
			.submitFile(submit2File)
			.submitDocuments()
			.assertSubmissionDone();
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
	public void taskWithoutSubmission()
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
			.addMember()
			.importList()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);

		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.uploadCorrection(correctionFile)
			.reviewed()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(browser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(browser);
		ryomouTestCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(browser);
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
	public void taskOptionalWithoutSubmission()
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
			.addMember()
			.importList()
			.setMembers(kanu, ryomou)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//go to the course
		CoursePageFragment coursePage = membersPage
			.clickToolbarBack();
		coursePage
			.tree()
			.selectWithTitle(gtaNodeTitle);

		URL correctionUrl = JunitTestHelper.class.getResource("file_resources/correction_1.txt");
		File correctionFile = new File(correctionUrl.toURI());
		GroupTaskToCoachPage participantToCoach = new GroupTaskToCoachPage(browser);
		participantToCoach
			.selectIdentitiesToCoach()
			.selectIdentityToCoach(ryomou)
			.openRevisionsStep()
			.uploadCorrection(correctionFile)
			.reviewed()
			.openIndividualAssessment()
			.individualAssessment(null, 5.5f)
			.assertPassed();
		
		//Participant log in
		LoginPage ryomouLoginPage = LoginPage.load(browser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou);
		
		//open the course
		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(browser);
		ryomouTestCourse
			.tree()
			.assertWithTitleSelected(gtaNodeTitle);
		
		GroupTaskPage ryomouTask = new GroupTaskPage(browser);
		//participant checks she passed the task
		ryomouTask
			.assertPassed()
			.openSolutions()
			.assertSolution("solution_1.txt");
	}
}
