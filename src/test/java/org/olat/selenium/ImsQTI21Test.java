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
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.qti.QTI21ConfigurationCEPage;
import org.olat.selenium.page.qti.QTI21EditorPage;
import org.olat.selenium.page.qti.QTI21Page;
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
	public void qti21TestInCourse(@InitialPage LoginPage authorLoginPage)
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
	public void qti21Test_lmsHidden_results(@InitialPage LoginPage authorLoginPage)
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
	public void qti21GraphicInteractionTest(@InitialPage LoginPage authorLoginPage)
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
			.saveAnswer().nextAnswer()
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
}
