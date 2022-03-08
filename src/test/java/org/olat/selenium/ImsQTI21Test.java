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
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.repository.model.SingleRoleRepositoryEntrySecurity.Role;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.qti.QTI21ConfigurationCEPage;
import org.olat.selenium.page.qti.QTI21CorrectionPage;
import org.olat.selenium.page.qti.QTI21GradingPage;
import org.olat.selenium.page.qti.QTI21Page;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 03.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class ImsQTI21Test extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	/**
	 * Test the flow of the simplest possible test with our
	 * optimization (jump automatically to the next question,
	 * jump automatically the close test). The test has one
	 * part and 2 questions, no feedbacks, no review allowed...
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_noParts_noFeedbacks()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_without_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Incorrect response")
			.saveAnswer()
			.assertOnAssessmentItem("Second question")
			.selectItem("First question")
			.assertOnAssessmentItem("First question")
			.answerSingleChoiceWithParagraph("Correct response")
			.saveAnswer()
			.answerMultipleChoice("Correct response")
			.saveAnswer()
			.endTest()//auto close because 1 part, no feedbacks
			.assertOnAssessmentTestTerminated();
	}
	
	/**
	 * Test the flow of a test with questions feedbacks and test
	 * feedback.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_noParts_withFeedbacks()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_with_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Wrong answer")
			.saveAnswer()
			.assertFeedback("Oooops")
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertFeedback("Well done")
			.nextAnswer()
			.assertOnAssessmentItem("Numerical entry")
			.answerGapText("69", "_RESPONSE_1")
			.saveAnswer()
			.assertFeedback("Not really")
			.answerGapText("42", "_RESPONSE_1")
			.saveAnswer()
			.assertFeedback("Ok")
			.endTest()
			.assertOnAssessmentTestFeedback("All right")
			.closeTest()
			.assertOnAssessmentTestTerminated();
	}
	
	/**
	 * A test with a single part, feedback for questions and
	 * tests and the resource options "show results at the end
	 * of the test".
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_noParts_feedbacksAndResults()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_with_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Wrong answer")
			.saveAnswer()
			.assertFeedback("Oooops")
			.nextAnswer()
			.assertOnAssessmentItem("Numerical entry")
			.answerGapText("42", "_RESPONSE_1")
			.saveAnswer()
			.assertFeedback("Ok")
			.endTest()
			.assertOnAssessmentTestFeedback("Not for the best")
			.closeTest()
			.assertOnAssessmentTestMaxScore(2)
			.assertOnAssessmentTestScore(1)
			.assertOnAssessmentTestNotPassed();
	}
	
	/**
	 * A test with a single part, feedback for questions and
	 * tests and the resource options "show results at the end
	 * of the test".
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_parts_noFeedbacksButResults()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_parts_without_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		qtiPage
			.clickToolbarBack()
			.startTestPart()
			.selectItem("First question")
			.assertOnAssessmentItem("First question")
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.assertOnAssessmentItem("Second question")
			.answerMultipleChoice("True")
			.saveAnswer()
			.endTestPart()
			.selectItem("Third question")
			.assertOnAssessmentItem("Third question")
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.answerCorrectKPrim("True", "Right")
			.answerIncorrectKPrim("Wrong", "False")
			.saveAnswer()
			.endTestPart()
			.assertOnAssessmentTestMaxScore(4)
			.assertOnAssessmentTestScore(4)
			.assertOnAssessmentTestPassed();
	}
	
	/**
	 * Test with 2 parts and test feedbacks.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_parts_feedbacks()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_with_parts_and_test_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);

		qtiPage
			.startTestPart()
			.selectItem("First question")
			.assertOnAssessmentItem("First question")
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Second question")
			.answerMultipleChoice("Valid answer")
			.saveAnswer()
			.endTestPart()
			.selectItem("Third question")
			.assertOnAssessmentItem("Third question")
			.answerSingleChoiceWithParagraph("Right")
			.saveAnswer()
			.answerSingleChoiceWithParagraph("Good")
			.saveAnswer()
			.endTestPart()
			.assertOnAssessmentTestFeedback("Well done")
			.closeTest()
			.assertOnAssessmentTestTerminated();
	}
	
	/**
	 * Test with time limit.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_timeLimits()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Timed QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_time_limits.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		//check simple time limit
		qtiPage
			.assertOnAssessmentItem("Single choice")
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Last choice")
			.answerSingleChoiceWithParagraph("True")
			.saveAnswer()
			.assertOnAssessmentTestTerminated(30);
	}
	
	/**
	 * Test with time limit and wait for the results at the end.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_timeLimits_results()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Timed QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_time_limits.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, new QTI21AssessmentResultsOptions(true, true, false, false, false))
			.save();
		
		//check simple time limit
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem("Single choice")
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Last choice")
			.answerSingleChoiceWithParagraph("True")
			.saveAnswer()
			.assertOnAssessmentResults(15)
			.assertOnAssessmentTestPassed()
			.assertOnAssessmentTestMaxScore(2)
			.assertOnAssessmentTestScore(2);
	}
	

	/**
	 * Upload a test in QTI 2.1 format, create a course, bind
	 * the test in a course element, customize the options
	 * with full window mode, show scores, assessment results
	 * and a start and end dates for the assessment.<br>
	 * Then a user will run it and check if the assessment results appears
	 * after the time reserved to the test ended.
	 * 
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21Course_dateTest(@Drone @Participant WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Hakufu");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Timed QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Test-8";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false)
			.selectLayoutConfiguration()
			.overrideConfiguration()
			.fullWindow()
			.saveLayoutConfiguration();
		
		Calendar cal = Calendar.getInstance();
		int currentSeconds = cal.get(Calendar.SECOND);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.MINUTE, -1);
		Date start = cal.getTime();
		int addMinutes = (currentSeconds < 30) ? 2 : 3;
		cal.add(Calendar.MINUTE, addMinutes);
		Date end = cal.getTime();
		configPage
			.selectConfiguration()
			.setTime(start, end)
			.showScoreOnHomepage(true)
			.showResultsOnHomepage(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.saveConfiguration();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		// add a participant
		course
			.members()
			.quickAdd(participant);
		
		//a user search the course and make the test
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		// open the course and see the test
		CoursePageFragment participantCourse = CoursePageFragment.getCourse(participantBrowser);		
		participantCourse
			.clickTree()
			.selectWithTitle(testNodeTitle);
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		qtiPage
			.assertOnStart()
			.start()
			.answerSingleChoiceWithParagraph("Right") 
			.saveAnswer()
			.assertOnAssessmentResults(180)
			.closeAssessmentResults()
			.assertOnCourseAttempts(1)
			.assertOnCourseAssessmentTestScore(1)
			.assertOnCourseAssessmentTestPassed()
			.assertOnAssessmentResults();
	}
	
	
	/**
	 * Test suspend. An author upload a test, set "enable suspend"
	 * and make the test visible to registered users. A second user
	 * open the test, does nothing, suspends and log out (check a possible red
	 * screen in the next step), log in, answer 3 questions, suspends 
	 * and log out. It log in a last time and finish the test successfully.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21TestFlow_suspend(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Suspend QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_4_no_skipping.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.enableSuspend()
			.save();
		
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.registred)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		
		//check simple time limit
		qtiPage
			.assertOnAssessmentItem("Single choice");
		
		//a user search the test
		LoginPage userLoginPage = LoginPage.load(ryomouBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(ryomouBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		QTI21Page userQtiPage = QTI21Page
				.getQTI21Page(ryomouBrowser);
		userQtiPage
			.assertOnAssessmentItem("Single choice")
			.suspendTest();
		//log out
		new UserToolsPage(ryomouBrowser)
			.logout();
		
		//log in and resume test
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		userQtiPage = QTI21Page
				.getQTI21Page(ryomouBrowser);
		userQtiPage
			.assertOnAssessmentItem("Single choice")
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.assertOnAssessmentItem("Kprim")
			.answerCorrectKPrim("True", "Right")
			.answerIncorrectKPrim("False", "Wrong")
			.saveAnswer()
			.suspendTest();
		
		//second log out
		new UserToolsPage(ryomouBrowser)
			.logout();
		
		//log in and resume test
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		userQtiPage = QTI21Page
				.getQTI21Page(ryomouBrowser);
		userQtiPage
			.assertOnAssessmentItem("Numerical input")
			.answerGapText("42", "_RESPONSE_1")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestMaxScore(4)
			.assertOnAssessmentTestScore(4)
			.assertOnAssessmentTestPassed();
	}
	
	/**
	 * Upload a test in QTI 2.1 format, create a course, bind
	 * the test in a course element, run it and check if
	 * the attempt go up.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21Course()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Simple QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Test-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false)
			.showScoreOnHomepage(true)
			.assertShowResultsOptions()
			.saveConfiguration();

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		// make the author a participant too
		course
			.members()
			.selectMembers()
			.openMembership(author.getFirstName())
			.editRepositoryMembership(Boolean.TRUE)
			.saveMembership()
			.clickToolbarBack();
		// close the course
		course
			.closeCourse();
			
		// reopen the course as participant (default)
		navBar
			.openAuthoringEnvironment()
			.selectResource(courseTitle);
		
		course
			.switchRole(Role.participant)
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.assertOnStart()
			.start()
			.answerSingleChoiceWithParagraph("Right")
			.saveAnswer()
			.endTest()
			.assertOnCourseAttempts(1)
			.assertOnCourseAssessmentTestScore(1);
	}
	

	/**
	 * Upload a test in QTI 2.1 format, create a course, bind
	 * the test in a course element, customize the options
	 * with full window mode, show scores and assessment results.
	 * Then run it and check if the assessment results appears after
	 * closing the test and on the start page of the test course element.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21Course_lmsHidden_results()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Simple QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Test-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false)
			.selectLayoutConfiguration()
			.overrideConfiguration()
			.fullWindow()
			.saveLayoutConfiguration();
		configPage
			.selectConfiguration()
			.showScoreOnHomepage(true)
			.showResultsOnHomepage(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.saveConfiguration();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		// make the author a participant too
		course
			.members()
			.selectMembers()
			.openMembership(author.getFirstName())
			.editRepositoryMembership(Boolean.TRUE)
			.saveMembership()
			.clickToolbarBack();
		// close the course
		course
			.closeCourse();
			
		// reopen the course as participant (default)
		navBar
			.openAuthoringEnvironment()
			.selectResource(courseTitle);
		
		course
			.switchRole(Role.participant)
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.assertOnStart()
			.start()
			.answerSingleChoiceWithParagraph("Right")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.closeAssessmentResults()
			.assertOnCourseAttempts(1)
			.assertOnCourseAssessmentTestScore(1)
			.assertOnCourseAssessmentTestPassed()
			.assertOnAssessmentResults();
	}

	/**
	 * Upload a test in QTI 2.1 format, create a course, bind
	 * the test in a course element, customize the options
	 * with full window mode, allow suspending the test,
	 * show scores and assessment results.<br>
	 * Then run it and at the middle of the test, suspend it, log out.
	 * Return with resume to the course and resume the test, finish it
	 * and check if the assessment results appears after
	 * closing the test and on the start page of the test course element.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21Course_suspend()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "No skipping QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_4_no_skipping.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Test-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false)
			.selectLayoutConfiguration()
			.overrideConfiguration()
			.fullWindow()
			.saveLayoutConfiguration();
		configPage
			.selectConfiguration()
			.showScoreOnHomepage(true)
			.showResultsOnHomepage(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.saveConfiguration()
			.selectLayoutConfiguration()
			.overrideConfiguration()
			.enableSuspend()
			.saveLayoutConfiguration();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		// make the author a participant too
		course
			.members()
			.selectMembers()
			.openMembership(author.getFirstName())
			.editRepositoryMembership(Boolean.TRUE)
			.saveMembership()
			.clickToolbarBack();
		// close the course
		course
			.closeCourse();
			
		// reopen the course as participant (default)
		navBar
			.openAuthoringEnvironment()
			.selectResource(courseTitle);
		
		course
			.switchRole(Role.participant)
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());

		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser);
		qtiPage
			.assertOnStart()
			.start()
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.suspendTest();
		
		//log out
		new UserToolsPage(browser)
			.logout();
		// return
		authorLoginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		course
			.switchRole(Role.participant);
	
		//resume the course, resume the test
		qtiPage = QTI21Page
				.getQTI21Page(browser);
		qtiPage
			.assertOnStart()
			.start()
			.assertOnAssessmentItem("Kprim")
			.answerCorrectKPrim("True", "Right")
			.answerIncorrectKPrim("False", "Wrong")
			.saveAnswer()
			.answerGapText("43", "_RESPONSE_1")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestMaxScore(4)
			.assertOnAssessmentTestScore(4)
			.assertOnAssessmentTestPassed()
			.closeAssessmentResults();
		//check the result on the start page
		qtiPage
			.assertOnCourseAssessmentTestScore(4)
			.assertOnCourseAttempts(1);
	}
	

	/**
	 * An author create a course with a test to overview
	 * the progress of a participant doing a test.
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21CourseTestCockpitProgress(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		//upload a test
		String qtiTestTitle = "Cockpit 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_without_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Cockpit QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Cockpit-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false)
			.showScoreOnHomepage(true)
			.assertShowResultsOptions()
			.saveConfiguration();

		//publish the course
		courseEditor
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save();
		
		//add a participant
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.publish()
			.members()
			.quickAdd(participant);
		//open the assessment tool
		AssessmentToolPage assessmentTool = courseRuntime
			.assessmentTool();
		assessmentTool
			.courseElements()
		// test cockpit
			.selectElementsCourseNode(testNodeTitle);
		
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		// open the course and see the test
		CoursePageFragment course = CoursePageFragment.getCourse(participantBrowser);		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		qtiPage
			.assertOnStart()
			.start()
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Correct response")
			.saveAnswer()
			.assertOnAssessmentItem("Second question");
		
		// author wait the progress
		assessmentTool
			.assertProgress(participant, 50);
		
		// answer the last question
		qtiPage
			.answerMultipleChoice("Correct response")
			.saveAnswer();
		
		// author wait the progress
		assessmentTool
			.assertProgress(participant, 100);
		
		// participant ends the test
		qtiPage
			.endTest()//auto close because 1 part, no feedbacks
			.assertOnCourseAssessmentTestScore(2)
			.assertOnCourseAssessmentTestPassed();
		
		// author wait the status changes
		assessmentTool
			.assertTablePassed(participant)
			.assertTableStatusDone(participant)
			.assertProgressEnded(participant);
	}
	
	/**
	 * An author create a course with a test with essay
	 * and single choice. A user make it. The author
	 * correct the test and the participant reload
	 * the page to see its result.
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21CourseTestCorrectionWorkflow(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Hakufu");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		//upload a test
		String qtiTestTitle = "Correction 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_sc_essay_mc.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Correction QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Correction-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false);
		
		//SEL cannot wait and discard the warning box on the server, simply wait it disappears
		OOGraphene.waitingTooLong();
		OOGraphene.waitingTooLong();
		
		configPage
			.showScoreOnHomepage(true)
			.assertShowResultsOptions()
			.saveConfiguration();

		//publish the course
		courseEditor
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save();
		
		//add a participant
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.publish()
			.members()
			.quickAdd(participant);
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		// open the course and see the test
		CoursePageFragment course = CoursePageFragment.getCourse(participantBrowser);		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		qtiPage
			.assertOnStart()
			.start()
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Essay")
			.answerEssay("Bla bla bla")
			.saveAnswer()
			.answerMultipleChoice("Good choice", "Bad choice")
			.saveAnswer()
			.endTest()
			.assertOnCourseAssessmentTestWaitingCorrection();
		
		//the author open the assessment tool
		AssessmentToolPage assessmentTool = courseRuntime
			.assessmentTool();
		assessmentTool
			.courseElements()
			.selectElementsCourseNode(testNodeTitle);
		// correction
		QTI21CorrectionPage correction = new QTI21CorrectionPage(browser);
		correction
			.startTestCorrection()
			.assertOnAssessmentItemError("Essay", 1)
			.selectAssessmentItem("Essay")
			.setScore("1.0")
			.save()
			.assertOnStatusOk()
			.back()
			.publishAll()
			.confirmDialog();
		//make all results visible
		assessmentTool
			.makeAllVisible();
		
		// the participant check its result
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		qtiPage = QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnCourseAssessmentTestScore(2);
	}
	
	
	/**
	 * An author create a test with essay and single choice, configures it
	 * with grading, add a grader then makes a course. A user play the test,
	 * the grader correct it, the author set the test as passed and free the
	 * results. The user reloads the results to see passed!
	 * 
	 * @param participantBrowser The participant browser
	 * @param graderBrowser The grader browser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21CourseTestGradingWorkflow(@Drone @Participant WebDriver participantBrowser,
		@Drone @User WebDriver graderBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Hakufu");
		UserVO grader = new UserRestClient(deploymentUrl).createRandomUser("Hakufu");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test and prepare the grading configuration
		String qtiTestTitle = "Correction 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_sc_essay_mc.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
			
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21GradingPage gradingPage = qtiPage
			.grading();
		gradingPage
			.settings()
			.enable()
			.setPeriods(9, 5, 7)
			.selectMailTemplate()
			.save();
		
		gradingPage
			.graders()
			.addGrader(grader)
			.assertGrader(grader);

		qtiPage
			.clickToolbarBack();
		
		//create a course
		String courseTitle = "Grading QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Grading-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, true)
			.setCorrectionMode("grading")
			.saveConfiguration();
		
		courseEditor
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save();
		
		//add a participant
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.publish()
			.members()
			.quickAdd(participant);
		
		//a user search the course and make the test
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		// open the course and see the test
		CoursePageFragment course = CoursePageFragment.getCourse(participantBrowser);		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		QTI21Page participantQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		participantQtiPage
			.assertOnStart()
			.start()
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Essay")
			.answerEssay("Bla bla bla")
			.saveAnswer()
			.answerMultipleChoice("Good choice", "Bad choice")
			.saveAnswer()
			.endTest()
			.assertOnCourseAssessmentTestWaitingCorrection();
		
		// grader assignment
		LoginPage graderLoginPage = LoginPage.load(graderBrowser, deploymentUrl);
		graderLoginPage
			.loginAs(grader.getLogin(), grader.getPassword())
			.resume();
		
		NavigationPage graderNavBar = NavigationPage.load(graderBrowser);
		graderNavBar
			.openCoaching()
			.assertOnGrading()
			.startGrading(testNodeTitle)
			.assertOnAssessmentItemNotCorrected("Essay", 0)
			.selectAssessmentItem("Essay")
			.setScore("1.0")
			.save()
			.assertOnStatusOk()
			.back()
			.publish()
			.confirmDialog();
		
		// author check the assignment
		navBar
			.openAuthoringEnvironment()
			.selectResource(qtiTestTitle);
		qtiPage
			.grading()
			.graders()
			.assertGraderAssignmentsDone(grader, 1);
		// go in assessment tool to free the results
		navBar
			.openAuthoringEnvironment()
			.selectResource(courseTitle);
		
		courseRuntime
			.assessmentTool()
			.users()
			.assertOnUsers(participant)
			.selectUser(participant)
			.selectUsersCourseNode(testNodeTitle)
			.reopenAssessment()
			.setAssessmentPassed(Boolean.TRUE)
			.setAssessmentVisibility(true)
			.closeAssessment()
			.assertUserPassedCourseNode(testNodeTitle);
		
		// participant checks its result
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		participantQtiPage
			.assertOnCourseAssessmentTestScore(2)
			.assertOnCourseAssessmentTestPassed();
	}
	

	/**
	 * An author create a test with essay and single choice, configures it
	 * with grading, forget to add the grader, then makes a course. A user
	 * play the test, the author add a grader to the assignment and the grader
	 * correct it, the author set the test as passed and free the results.
	 * The user reloads the results to see passed!
	 * 
	 * @param participantBrowser The participant browser
	 * @param graderBrowser The grader browser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21CourseTestGradingAfterwardsAssignment(@Drone @Participant WebDriver participantBrowser,
		@Drone @User WebDriver graderBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Hakufu");
		UserVO grader = new UserRestClient(deploymentUrl).createRandomUser("Hakufu");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test and prepare the grading configuration
		String qtiTestTitle = "Correction 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_sc_essay_mc.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
			
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21GradingPage gradingPage = qtiPage
			.grading();
		gradingPage
			.settings()
			.enable()
			.setPeriods(9, 5, 7)
			.selectMailTemplate()
			.save();

		qtiPage
			.clickToolbarBack();
		
		//create a course
		String courseTitle = "Grading QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTI21Grading-1";
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle);

		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, true)
			.setCorrectionMode("grading")
			.saveConfiguration();
		
		courseEditor
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save();
		
		//add a participant
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.publish()
			.members()
			.quickAdd(participant);
		
		//a user search the course and make the test
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		// open the course and see the test
		CoursePageFragment course = CoursePageFragment.getCourse(participantBrowser);		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		QTI21Page participantQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		participantQtiPage
			.assertOnStart()
			.start()
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Essay")
			.answerEssay("Bla bla bla")
			.saveAnswer()
			.answerMultipleChoice("Good choice", "Bad choice")
			.saveAnswer()
			.endTest()
			.assertOnCourseAssessmentTestWaitingCorrection();

		// author returns to test and assign the test to a new grader
		navBar
			.openAuthoringEnvironment()
			.selectResource(qtiTestTitle);
		
		gradingPage = qtiPage
			.grading();
		gradingPage
			.assignments()
			.assertAssignmentUnassigned(testNodeTitle)
			.openAssignmentUnassignedTool(testNodeTitle)
			.addGrader(grader);
		
		// grader assignment
		LoginPage graderLoginPage = LoginPage.load(graderBrowser, deploymentUrl);
		graderLoginPage
			.loginAs(grader.getLogin(), grader.getPassword())
			.resume();
		
		NavigationPage graderNavBar = NavigationPage.load(graderBrowser);
		graderNavBar
			.openCoaching()
			.assertOnGrading()
			.startGrading(testNodeTitle)
			.assertOnAssessmentItemNotCorrected("Essay", 0)
			.selectAssessmentItem("Essay")
			.setScore("1.0")
			.save()
			.assertOnStatusOk()
			.back()
			.publish()
			.confirmDialog();
		
		// author check the assignment
		navBar
			.openAuthoringEnvironment()
			.selectResource(qtiTestTitle);
		qtiPage
			.grading()
			.graders()
			.assertGraderAssignmentsDone(grader, 1);
		// go in assessment tool to free the results
		navBar
			.openAuthoringEnvironment()
			.selectResource(courseTitle);
		
		courseRuntime
			.assessmentTool()
			.users()
			.assertOnUsers(participant)
			.selectUser(participant)
			.selectUsersCourseNode(testNodeTitle)
			.reopenAssessment()
			.setAssessmentPassed(Boolean.TRUE)
			.setAssessmentVisibility(true)
			.closeAssessment()
			.assertUserPassedCourseNode(testNodeTitle);
		
		// participant checks its result
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		participantQtiPage
			.assertOnCourseAssessmentTestScore(2)
			.assertOnCourseAssessmentTestPassed();
	}
	
	/**
	 * An author create a course with a course element
	 * of type self test. It add a participant. The
	 * participant log in, go to the course to make the
	 * test.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21Course_selfTest()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Simple QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		//create a course
		String courseTitle = "Course-self-" + UUID.randomUUID();
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//add participants
		MembersPage members = courseRuntime
			.members();
		members
			.importMembers()
			.setMembers(ryomou)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Selftest";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqself")
			.nodeTitle(nodeTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectLearnContent()
			.chooseTest(qtiTestTitle, false)
			.showResultsOnHomepage(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.showScoreOnHomepage(true)
			.saveConfiguration();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.membersOnly)
			.save()
			.clickToolbarBack();
			
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant comes in and do the self test
		loginPage.loginAs(ryomou.getLogin(), ryomou.getPassword());

		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.clickTree()
			.selectWithTitle(nodeTitle);

		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser);
		qtiPage
			.assertOnStart()
			.start()
			.answerSingleChoiceWithParagraph("Right")
			.saveAnswer()
			.endTest()
			.closeAssessmentResults()
			.assertOnStart()
			.assertOnAssessmentResults();
	}
}
