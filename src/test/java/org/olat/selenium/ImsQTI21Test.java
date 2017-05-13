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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.User;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.qti.QTI21ConfigurationCEPage;
import org.olat.selenium.page.qti.QTI21EditorPage;
import org.olat.selenium.page.qti.QTI21KprimEditorPage;
import org.olat.selenium.page.qti.QTI21LobEditorPage;
import org.olat.selenium.page.qti.QTI21MatchEditorPage;
import org.olat.selenium.page.qti.QTI21MultipleChoiceEditorPage;
import org.olat.selenium.page.qti.QTI21Page;
import org.olat.selenium.page.qti.QTI21SingleChoiceEditorPage;
import org.olat.selenium.page.repository.RepositoryAccessPage.UserAccess;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
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
public class ImsQTI21Test {
	
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
	public void qti21TestFlow_noParts_noFeedbacks(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_without_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.assertOnAssessmentItem()
			.answerSingleChoice("Incorrect response")
			.saveAnswer()
			.assertOnAssessmentItem("Second question")
			.selectItem("First question")
			.assertOnAssessmentItem("First question")
			.answerSingleChoice("Correct response")
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
	public void qti21TestFlow_noParts_withFeedbacks(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_with_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.assertOnAssessmentItem()
			.answerSingleChoice("Wrong answer")
			.saveAnswer()
			.assertFeedback("Oooops")
			.answerSingleChoice("Correct answer")
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
	public void qti21TestFlow_noParts_feedbacksAndResults(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_with_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.clickToolbarBack()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerSingleChoice("Wrong answer")
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
	public void qti21TestFlow_parts_noFeedbacksButResults(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_parts_without_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.clickToolbarBack()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		qtiPage
			.clickToolbarBack()
			.startTestPart()
			.selectItem("First question")
			.assertOnAssessmentItem("First question")
			.answerSingleChoice("Correct")
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
	public void qti21TestFlow_parts_feedbacks(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "With parts QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_with_parts_and_test_feedbacks.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);

		qtiPage
			.startTestPart()
			.selectItem("First question")
			.assertOnAssessmentItem("First question")
			.answerSingleChoice("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Second question")
			.answerMultipleChoice("Valid answer")
			.saveAnswer()
			.endTestPart()
			.selectItem("Third question")
			.assertOnAssessmentItem("Third question")
			.answerSingleChoice("Right")
			.saveAnswer()
			.answerSingleChoice("Good")
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
	public void qti21TestFlow_timeLimits(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Timed QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_time_limits.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		//check simple time limit
		qtiPage
			.assertOnAssessmentItem("Single choice")
			.answerSingleChoice("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Last choice")
			.answerSingleChoice("True")
			.saveAnswer()
			.assertOnAssessmentTestTerminated(15);
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
	public void qti21TestFlow_timeLimits_results(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Timed QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_time_limits.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.options()
			.showResults(Boolean.TRUE, new QTI21AssessmentResultsOptions(true, true, false, false, false, false))
			.save();
		
		//check simple time limit
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem("Single choice")
			.answerSingleChoice("Correct answer")
			.saveAnswer()
			.assertOnAssessmentItem("Last choice")
			.answerSingleChoice("True")
			.saveAnswer()
			.assertOnAssessmentResults(15)
			.assertOnAssessmentTestPassed()
			.assertOnAssessmentTestMaxScore(2)
			.assertOnAssessmentTestScore(2);
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
	public void qti21TestFlow_suspend(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Suspend QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_4_no_skipping.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.enableSuspend()
			.save();
		
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.registred)
			.clickToolbarBack();
		
		//check simple time limit
		qtiPage
			.assertOnAssessmentItem("Single choice");
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = new NavigationPage(ryomouBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		QTI21Page userQtiPage = QTI21Page
				.getQTI12Page(ryomouBrowser);
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
				.getQTI12Page(ryomouBrowser);
		userQtiPage
			.assertOnAssessmentItem("Single choice")
			.answerSingleChoice("Correct")
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
				.getQTI12Page(ryomouBrowser);
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
	public void qti21Course(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Simple QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
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
			.nodeTitle(testNodeTitle)
			.selectTabLearnContent()
			.chooseTest(qtiTestTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
			.selectConfiguration()
			.showScoreOnHomepage(true)
			.saveConfiguration();

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.start()
			.answerSingleChoice("Right")
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
	public void qti21Course_lmsHidden_results(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Simple QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_test.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
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
			.nodeTitle(testNodeTitle)
			.selectTabLearnContent()
			.chooseTest(qtiTestTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
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
		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.start()
			.answerSingleChoice("Right")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.closeAssessmentResults()
			.assertOnCourseAttempts(1)
			.assertOnCourseAssessmentTestScore(1)
			.showAssessmentResults()
			.assertOnAssessmentResults();
	}
	
	/**
	 * Check if the hotspot interaction send a "correct" feedback.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21GraphicInteraction(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Simple QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_hotspot.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerHotspot("circle")
			.saveAnswer()
			.assertFeedback("Correct!")
			.endTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(1)
			.assertOnAssessmentTestMaxScore(1);
	}
	
	/**
	 * Create a test, import the CSV example, remove the
	 * first single choice which come if someone create a
	 * test. Change the delivery settings of the test to
	 * show the detailled results.<br>
	 * Run the test and check the results. 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void importQuestionsCSV(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Excel QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();

		QTI21Page qtiPage = QTI21Page
			.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
			.edit();
		
		// import a single choice, a multiple and 2 gap texts
		qtiEditor
			.importTable()
			.importFile("qti21/import_qti21_excel.txt")
			.next()
			.assertOnNumberOfQuestions(5)
			.finish();
		
		//remove the single choice which come from the creation
		// of the test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		// go to options and show the results
		qtiPage
			.clickToolbarBack()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//go to the test
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerGapText("verbannen", "_RESPONSE_1")
			.saveAnswer()
			.nextAnswer()
			.answerGapText(",", "_RESPONSE_1")
			.answerGapText("", "_RESPONSE_2")
			.answerGapText("", "_RESPONSE_3")
			.saveAnswer()
			.answerMultipleChoice("Deutschland", "Brasilien", "S\u00FCdafrika")
			.saveAnswer()
			.answerSingleChoice("Italien")
			.saveAnswer()
			.answerCorrectKPrim("Deutschland", "Uruguay")
			.answerIncorrectKPrim("Frankreich", "Spanien")
			.saveAnswer()
			.endTest();
		
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(9)
			.assertOnAssessmentTestMaxScore(9);
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
	public void qti21Course_suspend(@InitialPage LoginPage authorLoginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "No skipping QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/test_4_no_skipping.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
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
			.nodeTitle(testNodeTitle)
			.selectTabLearnContent()
			.chooseTest(qtiTestTitle);
		
		QTI21ConfigurationCEPage configPage = new QTI21ConfigurationCEPage(browser);
		configPage
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
		
		course
			.clickTree()
			.selectWithTitle(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
			.start()
			.answerSingleChoice("Correct")
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
		//resume the course, resume the test
		qtiPage = QTI21Page
				.getQTI12Page(browser);
		qtiPage
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
	 * Test different settings in the single choice editor. An author
	 * make a test with 2 single choices, one with score all answer correct,
	 * the second with score per answer and feedbacks.<br>
	 * A second user make the test and check the score at the end of
	 * the test.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorSingleChoices(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add a single choice: all answers score
		QTI21SingleChoiceEditorPage scEditor = qtiEditor
			.addSingleChoice();
		scEditor
			.setAnswer(0, "Wrong")
			.addChoice(1)
			.setCorrect(1)
			.setAnswer(1, "Correct")
			.addChoice(2)
			.setAnswer(2, "Faux")
			.addChoice(3)
			.setAnswer(3, "Falsch")
			.save();
		// change max score
		scEditor
			.selectScores()
			.setMaxScore("3")
			.save();
		// set some feedbacks
		scEditor
			.selectFeedbacks()
			.setHint("Hint", "This is only an hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//score per answers
		scEditor = qtiEditor
			.addSingleChoice()
			.setAnswer(0, "AlmostRight")
			.addChoice(1)
			.setAnswer(1, "NotRight")
			.addChoice(2)
			.setCorrect(2)
			.setAnswer(2, "RightAnswer")
			.addChoice(3)
			.setAnswer(3, "TheWrongOne")
			.save();
		scEditor
			.selectScores()
			.setMaxScore("2")
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setScore("Almost", "1")
			.setScore("NotRight", "0")
			.setScore("RightAnswer", "2")
			.setScore("TheWrongOne", "0")
			.save();
		scEditor
			.selectFeedbacks()
			.setHint("Hint", "The hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = new NavigationPage(ryomouBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI12Page(ryomouBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerSingleChoice("Falsch")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerSingleChoice("Correct")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerSingleChoice("Almost")
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(4);// 3 points from the first question, 1 from the second
	}
	
	/**
	 * An author make a test with 2 multiple choices, the first
	 * with the score set if all answers are correct, the second
	 * with scoring per answers.<br>
	 * A first user make the test, but doesn't answer all questions
	 * correctly, log out and a second user make the perfect test.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorMultipleChoices(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO eric = new UserRestClient(deploymentUrl).createRandomUser("Eric");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add a single choice: all answers score
		QTI21MultipleChoiceEditorPage mcEditor = qtiEditor
			.addMultipleChoice();
		mcEditor
			.setAnswer(0, "Correct")
			.setCorrect(0)
			.addChoice(1)
			.setCorrect(1)
			.setAnswer(1, "OkToo")
			.addChoice(2)
			.setAnswer(2, "Faux")
			.addChoice(3)
			.setAnswer(3, "Falsch")
			.save();
		// change max score
		mcEditor
			.selectScores()
			.setMaxScore("3")
			.save();
		// set some feedbacks
		mcEditor
			.selectFeedbacks()
			.setHint("Hint", "This is only an hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//score per answers
		mcEditor = qtiEditor
			.addMultipleChoice()
			.setCorrect(0)
			.setAnswer(0, "AlmostRight")
			.addChoice(1)
			.setAnswer(1, "NotRight")
			.addChoice(2)
			.setCorrect(2)
			.setAnswer(2, "RightAnswer")
			.addChoice(3)
			.setAnswer(3, "TheWrongOne")
			.save();
		mcEditor
			.selectScores()
			.setMaxScore("3")
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setScore("AlmostRight", "1")
			.setScore("NotRight", "0")
			.setScore("RightAnswer", "2")
			.setScore("TheWrongOne", "0")
			.save();
		mcEditor
			.selectFeedbacks()
			.setHint("Hint", "The hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = new NavigationPage(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI12Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerMultipleChoice("Falsch")
			.answerMultipleChoice("OkToo")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerMultipleChoice("Falsch")
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMultipleChoice("AlmostRight")
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(4);// 3 points from the first question, 1 from the second
		

		//a second user search the content package
		LoginPage ericLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		ericLoginPage
			.loginAs(eric.getLogin(), eric.getPassword())
			.resume();
		NavigationPage ericNavBar = new NavigationPage(participantBrowser);
		ericNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI12Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerMultipleChoice("Correct", "OkToo")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMultipleChoice("AlmostRight", "RightAnswer")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 3 points from the first question, 3 from the second
	}

	/**
	 * An author make a test with 2 kprims.<br>
	 * A first user make the test, but doesn't answer all questions
	 * correctly, log out and a second user make the perfect test.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorKprim(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO melissa = new UserRestClient(deploymentUrl).createRandomUser("Melissa");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Kprim QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add a kprim
		QTI21KprimEditorPage kprimEditor = qtiEditor
			.addKprim();
		kprimEditor
			.setAnswer(0, "Correct")
			.setCorrect(0, true)
			.setAnswer(1, "OkToo")
			.setCorrect(1, true)
			.setAnswer(2, "Faux")
			.setCorrect(2, false)
			.setAnswer(3, "Falsch")
			.setCorrect(3, false)
			.save();
		// change max score
		kprimEditor
			.selectScores()
			.setMaxScore("4")
			.save();
		// set some feedbacks
		kprimEditor
			.selectFeedbacks()
			.setHint("Hint", "This is only an hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		// second kprim
		kprimEditor = qtiEditor
			.addKprim()
			.setAnswer(0, "OnlyRight")
			.setCorrect(0, true)
			.setAnswer(1, "NotRight")
			.setCorrect(1, false)
			.setAnswer(2, "NotAnswer")
			.setCorrect(2, false)
			.setAnswer(3, "TheWrongOne")
			.setCorrect(3, false)
			.save();
		kprimEditor
			.selectScores()
			.setMaxScore("2")
			.save();
		kprimEditor
			.selectFeedbacks()
			.setHint("Hint", "The hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		

		//a user search the content package
		LoginPage reiLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = new NavigationPage(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI12Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.answerCorrectKPrim("Correct", "OkToo", "Faux")
			.answerIncorrectKPrim("Falsch")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerCorrectKPrim("Correct", "OkToo")
			.answerIncorrectKPrim("Falsch", "Faux")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerIncorrectKPrim("OnlyRight", "NotRight", "NotAnswer", "TheWrongOne")
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(5);// 4 points from the first question, 1 from the second
		

		//a second user search the content package
		LoginPage melLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		melLoginPage
			.loginAs(melissa.getLogin(), melissa.getPassword())
			.resume();
		NavigationPage melNavBar = new NavigationPage(participantBrowser);
		melNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI12Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerCorrectKPrim("Correct", "OkToo")
			.answerIncorrectKPrim("Faux", "Falsch")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerCorrectKPrim("OnlyRight")
			.answerIncorrectKPrim("NotRight", "NotAnswer", "TheWrongOne")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 3 points from the first question, 3 from the second
	}
	
	/**
	 * An author make a test with 2 matches. A match with "multiple selection"
	 * and score "all answers", a second with "single selection" and score
	 * "per answers".<br>
	 * A first user make the test, but doesn't answer all questions
	 * correctly, log out and a second user make the perfect test.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorMatch(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO melissa = new UserRestClient(deploymentUrl).createRandomUser("Melissa");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Match QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add a match, multiple selection
		QTI21MatchEditorPage matchEditor = qtiEditor
			.addMatch();
		matchEditor
			.setSource(0, "Eclipse")
			.setSource(1, "vim")
			.setTarget(0, "IDE")
			.setTarget(1, "TextProcessor")
			.addColumn()
			.setTarget(2, "TextEditor")
			.setMatch(0, 0, true)
			.setMatch(1, 2, true)
			.save();
		// change max score
		matchEditor
			.selectScores()
			.setMaxScore("4")
			.save();
		// set some feedbacks
		matchEditor
			.selectFeedbacks()
			.setHint("Hint", "This is only an hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		// second match
		matchEditor = qtiEditor
			.addMatch()
			.setSingleChoices()
			.setSource(0, "Java")
			.setSource(1, "C")
			.addRow()
			.setSource(2, "PHP")
			.setTarget(0, "CodeIgniter")
			.setTarget(1, "VisualStudio")
			.addColumn()
			.setTarget(2, "Eclipse")
			.setMatch(0, 2, true)
			.setMatch(1, 1, true)
			.setMatch(2, 0, true)
			.save();
		// select score "per answer" and set the scores
		matchEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("6")
			.setScore(0, 0, "0.0")
			.setScore(0, 1, "0.0")
			.setScore(0, 2, "2.0")
			.setScore(1, 0, "0.0")
			.setScore(1, 1, "3.0")
			.setScore(1, 2, "0.0")
			.setScore(2, 0, "1.0")
			.setScore(2, 1, "0.0")
			.setScore(2, 2, "0.0")
			.save();
		matchEditor
			.selectFeedbacks()
			.setHint("Hint", "The hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = new NavigationPage(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI12Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.answerMatch("Eclipse", "IDE", true)
			.answerMatch("vim", "IDE", true)
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerMatch("vim", "IDE", false)
			.answerMatch("vim", "TextEditor", true)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatch("Java", "Eclipse", true)
			.answerMatch("C", "CodeIgniter", true)
			.answerMatch("PHP", "VisualStudio", true)
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 4 points from the first question, 2 from the second
		
		//a second user search the content package
		LoginPage melLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		melLoginPage
			.loginAs(melissa.getLogin(), melissa.getPassword())
			.resume();
		NavigationPage melNavBar = new NavigationPage(participantBrowser);
		melNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI12Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerMatch("Eclipse", "IDE", true)
			.answerMatch("vim", "TextEditor", true)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatch("Java", "Eclipse", true)
			.answerMatch("C", "CodeIgniter", true)
			.answerMatch("PHP", "VisualStudio", true)
			.saveAnswer()
			.answerMatch("C", "CodeIgniter", false)
			.answerMatch("PHP", "VisualStudio", false)
			.answerMatch("C", "VisualStudio", true)
			.answerMatch("PHP", "CodeIgniter", true)
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(10);// 4 points from the first question, 6 from the second
	}
	
	/**
	 * An author make a test with 1 upload and feedbacks.<br>
	 * A user make the test, test hint and upload the file.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorUpload(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//make a test
		String qtiTestTitle = "Upload QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add an upload interaction
		QTI21LobEditorPage uploadEditor = qtiEditor
			.addUpload();
		uploadEditor
			.setQuestion("Upload a file")
			.save()
			.selectScores()
			.setMaxScore("2.0")
			.save();
		uploadEditor
			.selectFeedbacks()
			.setHint("Hint", "Need a little help.")
			.setCorrectSolution("Correct solution", "Only for Word")
			.setAnsweredFeedback("Full", "You upload something")
			.setEmpytFeedback("Empty", "You do not upload anything")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = new NavigationPage(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI12Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.saveAnswer()
			.assertFeedback("Empty")
			.hint()
			.assertFeedback("Hint");
		
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1482.JPG");
		File imageFile = new File(imageUrl.toURI());
		reiQtiPage
			.answerUpload(imageFile)
			.saveAnswer()
			.assertFeedback("Full")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentResultUpload("IMG_1482");
	}
	
	/**
	 * An author make a test with an essai and its special feedback.<br>
	 * A user make the test and check the feedback.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorEssay(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		

		//make a test
		String qtiTestTitle = "Essai QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add an essay interaction
		QTI21LobEditorPage essayEditor = qtiEditor
			.addEssay();
		essayEditor
			.setQuestion("Write a small story")
			.save()
			.selectScores()
			.setMaxScore("3.0")
			.save();
		essayEditor
			.selectFeedbacks()
			.setHint("Hint", "Did you search inspiration?")
			.setCorrectSolution("Correct solution", "It is very personal.")
			.setAnsweredFeedback("Full", "Well done")
			.setEmpytFeedback("Empty", "Please, a little effort.")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = new NavigationPage(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI12Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.saveAnswer()
			.assertFeedback("Empty")
			.hint()
			.assertFeedback("Hint");

		reiQtiPage
			.answerEssay("What can I write?")
			.saveAnswer()
			.assertFeedback("Full")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentResultEssay("What");
	}
	
	
	/**
	 * An author make a test with a drawing and its special feedback.<br>
	 * A user make the test and check the feedback.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorDrawing(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		

		//make a test
		String qtiTestTitle = "Drawing QTI 2.1 " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI12Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single choice")
			.deleteNode();
		
		//add an essay interaction
		QTI21LobEditorPage essayEditor = qtiEditor
			.addDrawing();
		
		URL backgroundImageUrl = JunitTestHelper.class.getResource("file_resources/house.jpg");
		File backgroundImageFile = new File(backgroundImageUrl.toURI());
		essayEditor
			.setQuestion("Draw an house")
			.updloadDrawingBackground(backgroundImageFile)
			.save()
			.selectScores()
			.setMaxScore("3.0")
			.save();
		essayEditor
			.selectFeedbacks()
			.setHint("Hint", "Did you search inspiration?")
			.setCorrectSolution("Correct solution", "It is very personal.")
			.setAnsweredFeedback("Full", "Well done")
			.setEmpytFeedback("Empty", "Please, a little effort.")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.clickToolbarBack();
		// show results
		qtiPage
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = new NavigationPage(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI12Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.saveAnswer()
			.assertFeedback("Empty")
			.hint()
			.assertFeedback("Hint");

		reiQtiPage
			.answerDrawing()
			.saveAnswer()
			.assertFeedback("Full")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnDrawing();
	}
}
