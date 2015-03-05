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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.AssessmentModePage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PublisherPageFragment.Access;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.qti.QTI12Page;
import org.olat.selenium.page.repository.ScormPage;
import org.olat.selenium.page.repository.RepositoryAccessPage.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
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
 * Initial date: 11.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class AssessmentTest {

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
	 * An author upload a test, create a course with a test course
	 * element, publish the course and do and pass the test.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti12Test(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		//File upload only work with Firefox
		Assume.assumeTrue(browser instanceof FirefoxDriver);
				
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "QTI-Test-1.2-" + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/e4_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-1.2-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String testNodeTitle = "Test-QTI-1.2";
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
		courseEditor
			.clickToolbarBack()
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_titled_wrapper.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		//start the test
		QTI12Page testPage = QTI12Page.getQTI12Page(browser);
		testPage
			.start()
			.selectItem(0)
			.answerSingleChoice(0)
			.saveAnswer()
			.selectItem(1)
			.answerMultipleChoice(0, 2)
			.saveAnswer()
			.selectItem(2)
			.answerKPrim(true, false, true, false)
			.saveAnswer()
			.selectItem(3)
			.answerFillin("not")
			.saveAnswer();
		testPage
			.endTest();
		
		//check results page
		By resultsBy = By.id("o_qti_results");
		OOGraphene.waitElement(resultsBy, browser);
		WebElement resultsEl = browser.findElement(resultsBy);
		Assert.assertTrue(resultsEl.getText().contains(author.getFirstName()));
		//close the test
		testPage
			.closeTest();
		//all answers are correct -> passed
		WebElement passedEl = browser.findElement(By.cssSelector("tr.o_state.o_passed"));
		Assert.assertTrue(passedEl.isDisplayed());
	}
	

	/**
	 * An author upload a test, create a course with a test course
	 * element, publish the course, assign the course to a student.
	 * The student come to pass the test, logout after passing it.
	 * The author check if the test of the student is passed in the
	 * assessment tool.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti12CourseWithAssessment(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		//File upload only work with Firefox
		Assume.assumeTrue(browser instanceof FirefoxDriver);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//upload a test
		String qtiTestTitle = "QTI-Test-1.2-" + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/e4_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-1.2-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type CP with the CP that we create above
		String testNodeTitle = "Test-QTI-1.2";
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
			.quickPublish(Access.membersOnly);
		
		//open the course and see the test start page
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_titled_wrapper.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		//add Ryomou as a course member
		courseRuntime
			.members()
			.addMember()
			.searchMember(ryomou, true)
			.next().next().next().finish();
		
		//Ryomou open the course
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = new NavigationPage(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the test
		CoursePageFragment ryomouTestCourse = new CoursePageFragment(ryomouBrowser);
		ryomouTestCourse
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//start the test
		QTI12Page testPage = QTI12Page.getQTI12Page(ryomouBrowser);
		testPage
			.start()
			.selectItem(0)
			.answerSingleChoice(0)
			.saveAnswer()
			.selectItem(1)
			.answerMultipleChoice(0, 2)
			.saveAnswer()
			.selectItem(2)
			.answerKPrim(true, false, true, false)
			.saveAnswer()
			.selectItem(3)
			.answerFillin("not")
			.saveAnswer();
		testPage
			.endTest();
		
		//check results page
		By resultsBy = By.id("o_qti_results");
		OOGraphene.waitElement(resultsBy, ryomouBrowser);
		WebElement resultsEl = ryomouBrowser.findElement(resultsBy);
		Assert.assertTrue(resultsEl.getText().contains(ryomou.getFirstName()));
		//close the test
		testPage
			.closeTest();
		//all answers are correct -> passed
		WebElement passedEl = ryomouBrowser.findElement(By.cssSelector("tr.o_state.o_passed"));
		Assert.assertTrue(passedEl.isDisplayed());
		
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
	public void scormCourseWithAssessment(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		//File upload only work with Firefox
		Assume.assumeTrue(browser instanceof FirefoxDriver);
				
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//upload a test
		String scormTitle = "SCORM - " + UUID.randomUUID();
		URL scormUrl = JunitTestHelper.class.getResource("file_resources/very_simple_scorm.zip");
		File scormFile = new File(scormUrl.toURI());
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
		String scormNodeTitle = "SCORM";
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
				.accessConfiguration()
				.setUserAccess(UserAccess.registred)
				.clickToolbarBack();
		
		CoursePageFragment courseRuntime = new CoursePageFragment(browser);
		//add Ryomou as a course member
		courseRuntime
			.members()
			.addMember()
			.searchMember(ryomou, true)
			.next().next().next().finish();
		
		//Ryomou open the course
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		//open the course
		NavigationPage ryomouNavBar = new NavigationPage(ryomouBrowser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//open the course and see the test start page
		CoursePageFragment ryomouCourse = new CoursePageFragment(ryomouBrowser);
		ryomouCourse
			.clickTree()
			.selectWithTitle(scormNodeTitle);
		
		By scormH2By = By.cssSelector("div.o_titled_wrapper.o_course_run h2");
		WebElement scormH2 = ryomouBrowser.findElement(scormH2By);
		Assert.assertEquals(scormNodeTitle, scormH2.getText().trim());
		
		//scorm
		ScormPage scorm = ScormPage.getScormPage(ryomouBrowser);
		scorm
			.start()
			.passVerySimpleScorm()
			.back();
		
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
	public void assessmentMode_manual(@InitialPage LoginPage authorLoginPage,
			@Drone @Student WebDriver ryomouBrowser, @Drone @Participant WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		//File upload only work with Firefox
		Assume.assumeTrue(browser instanceof FirefoxDriver);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		
		//upload a test
		String qtiTestTitle = "QTI-Test-1.2-" + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/e4_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-1.2-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type CP with the CP that we create above
		String testNodeTitle = "Test-QTI-1.2";
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
		OOGraphene.closeBlueMessageWindow(browser);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_titled_wrapper.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		//add Ryomou and Kanu as a course member
		courseRuntime.members().quickAdd(ryomou);
		courseRuntime.members().quickAdd(kanu);
		
		//Kanu log in 
		LoginPage kanuLoginPage = LoginPage.getLoginPage(kanuBrowser, deploymentUrl);
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
			.start(assessmentName)
			.confirmStart();

		//Ryomou opens the course
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
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
		QTI12Page.getQTI12Page(ryomouBrowser).passE4(ryomou);

		
		//Kanu makes the test
		AssessmentModePage kanuAssessment = new AssessmentModePage(kanuBrowser)
			.startAssessment(true);
		//go to the test
		CoursePageFragment kanuTestCourse = new CoursePageFragment(kanuBrowser);
		kanuTestCourse
			.clickTree()
			.selectWithTitle(testNodeTitle);
		//pass the test
		QTI12Page.getQTI12Page(kanuBrowser).passE4(kanu);

		
		//Author ends the test
		courseRuntime
			.assessmentConfiguration()
			.stop(assessmentName)
			.confirmStop();
		
		By continueBy = By.className("o_sel_assessment_continue");
		OOGraphene.waitElement(continueBy, 10, ryomouBrowser);
		OOGraphene.waitElement(continueBy, 10, kanuBrowser);
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
	public void certificatesManuallyGenerated(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver reiBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		
		//create a course
		String courseTitle = "Course-With-Certificates-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type CP with the CP that we create above
		CoursePageFragment courseRuntime = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("info")
			.autoPublish();
		
		//add a participant to the course
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(rei, true)
			.next().next().next().finish();
		// return to course
		courseRuntime = members
				.clickToolbarBack()
				.efficiencyStatementConfiguration()
				.clickToolbarBack()
				.efficiencyStatementConfiguration()
				.enableCertificates(false)
				.enableRecertification()
				.save()
				.clickToolbarBack();
		//create a certificate
		courseRuntime
			.assessmentTool()
			.users()
			.selectUser(rei)
			.generateCertificate();
		
		//Participant log in
		LoginPage reiLoginPage = LoginPage.getLoginPage(reiBrowser, deploymentUrl);
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

}
