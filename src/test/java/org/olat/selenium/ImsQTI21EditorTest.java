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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition.Operator;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition.Variable;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.User;
import org.olat.selenium.page.qti.QTI21EditorPage;
import org.olat.selenium.page.qti.QTI21GapEntriesEditorPage;
import org.olat.selenium.page.qti.QTI21HotspotEditorPage;
import org.olat.selenium.page.qti.QTI21HottextEditorPage;
import org.olat.selenium.page.qti.QTI21KprimEditorPage;
import org.olat.selenium.page.qti.QTI21LobEditorPage;
import org.olat.selenium.page.qti.QTI21MatchEditorPage;
import org.olat.selenium.page.qti.QTI21MultipleChoiceEditorPage;
import org.olat.selenium.page.qti.QTI21OrderEditorPage;
import org.olat.selenium.page.qti.QTI21Page;
import org.olat.selenium.page.qti.QTI21Page.TrueFalse;
import org.olat.selenium.page.qti.QTI21SingleChoiceEditorPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * 
 * Initial date: 23 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class ImsQTI21EditorTest extends Deployments {
	
	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;

	/**
	 * Create a test, import the CSV example, remove the
	 * first single choice which come if someone create a
	 * test. Change the delivery settings of the test to
	 * show the detailed results.<br>
	 * Run the test and check the results. 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void importQuestionsCSV()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Excel QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();

		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
			.assertOnAssessmentItem()
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
			.selectNode("Single Choice")
			.deleteNode();
		
		// go to options and show the results
		qtiPage
			.clickToolbarBack()
			.settings()
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
			.answerSingleChoiceWithParagraph("Italien")
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
	 * Create a test, import a CSV with some match variants,
	 * remove the first single choice which come if someone
	 * create a test. Change the delivery settings of the test
	 * to show the detailled results.<br>
	 * Run the test and check the results. 
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void importQuestionsCSVMatchVariants()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "ExcelMatch QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();

		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
			.assertOnAssessmentItem()
			.edit();
		
		// import a single choice, a multiple and 2 gap texts
		qtiEditor
			.importTable()
			.importFile("qti21/import_qti21_excel_match.txt")
			.next()
			.assertOnNumberOfQuestions(3)
			.finish();
		
		//remove the single choice which come from the creation
		// of the test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		// go to options and show the results
		qtiPage
			.clickToolbarBack()
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//go to the test
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerMatch("Berlin", "Deutschland", true)
			.answerMatch("Bern", "Schweiz", true)
			.answerMatch("Paris", "Frankreich", true)
			.saveAnswer()
			.nextAnswer()
			.assertOnAssessmentItem("Afrika")
			.answerMatchDropSourceToTarget("Nairobi", "Kenia")
			.answerMatchDropSourceToTarget("Windhoek", "Namibia")
			.answerMatchDropSourceToTarget("Algier", "Algerien")
			.saveAnswer()
			.assertOnAssessmentItem("Europa")
			.answerMatch("Paris", TrueFalse.right, true)
			.answerMatch("Bern", TrueFalse.right, true)
			.answerMatch("Stockholm", TrueFalse.wrong, true)
			.saveAnswer()
			.endTest();
		
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(9)
			.assertOnAssessmentTestMaxScore(9);
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
	public void qti21EditorSingleChoices(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
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
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(ryomouBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Falsch")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerSingleChoiceWithParagraph("Almost")
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(4);// 3 points from the first question, 1 from the second
	}
	
	/**
	 * Test the conditional feedback with a condition based
	 * on attempts (and an inccorect feedback used as marker).
	 * The author use the condition attempts = 2
	 * and check it in the runtime. It's done with a single
	 * choice.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorSingleChoices_conditionalAttemptsFeedback()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		qtiEditor
			.selectNode("Single Choice")
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

		// set a conditional feedback
		scEditor
			.selectFeedbacks()
			.setIncorrectFeedback("Incorrect", "Not the right response")
			.addConditionalFeedback(1, "Attempts", "2 attempts")
			.setCondition(1, 1, Variable.attempts, Operator.equals, "2")
			.save();
		
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerSingleChoiceWithParagraph("Falsch")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.answerSingleChoiceWithParagraph("Faux")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertFeedback("Attempts")
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.assertNoFeedback()
			.endTest();
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
	public void qti21EditorMultipleChoices(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO eric = new UserRestClient(deploymentUrl).createRandomUser("Eric");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
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
		LoginPage ericLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		ericLoginPage
			.loginAs(eric.getLogin(), eric.getPassword())
			.resume();
		NavigationPage ericNavBar = NavigationPage.load(participantBrowser);
		ericNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
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
	 * Test the conditional feedback with 3 conditions based
	 * on attempts (and an incorrect feedback used as marker),
	 * on score and on response. It's done with a multiple
	 * choice with score per answer and a negative min. score.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorMultipleChoices_complexConditionalFeedback()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
	
		//add a single choice: all answers score
		QTI21MultipleChoiceEditorPage mcEditor = qtiEditor
			.addMultipleChoice();
		mcEditor
			.setAnswer(0, "Ok")
			.setCorrect(0)
			.addChoice(1)
			.setCorrect(1)
			.setAnswer(1, "Correct")
			.addChoice(2)
			.setAnswer(2, "Faux")
			.addChoice(3)
			.setAnswer(3, "Falsch")
			.save();
		
		//add negative scores to play with
		mcEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setScore("Ok", "3")
			.setScore("Correct", "1")
			.setScore("Faux", "-1")
			.setScore("Falsch", "-1")
			.setMaxScore("4")
			.save();

		// set a conditional feedback
		mcEditor
			.selectFeedbacks()
			.setIncorrectFeedback("Incorrect", "Not the right response")
			// attempts = 1 && score < 0
			.addConditionalFeedback(1, "NegativeFirstAttempts", "Negative score")
			.setCondition(1, 1, Variable.attempts, Operator.equals, "1")
			.addCondition(1, 1)
			.setCondition(1, 2, Variable.score, Operator.smaller, "0")
			// response = 'Faux'
			.addConditionalFeedback(2, "FauxAnswer", "You choose the 'Faux' answer")
			.setCondition(2, 1, Variable.response, Operator.equals, "Faux")
			// 0 < score < 3 
			.addConditionalFeedback(3, "Positive", "Score between 0 and 3")
			.setCondition(3, 1, Variable.score, Operator.biggerEquals, "0")
			.addCondition(3, 1)
			.setCondition(3, 2, Variable.score, Operator.smaller, "3")
			.save();
		
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			//1 attempt, score -2.0
			.answerMultipleChoice("Falsch", "Faux")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertFeedback("FauxAnswer")
			.assertFeedback("NegativeFirstAttempts")
			.assertNoFeedback("Positive")
			//2 attempt, score 0.0
			.deselectAnswerMultipleChoice("Faux", "Falsch")
			.answerMultipleChoice("Faux", "Correct")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertFeedback("FauxAnswer")
			.assertFeedback("Positive")
			.assertNoFeedback("NegativeFirstAttempts")
			//3 attempt
			.deselectAnswerMultipleChoice("Faux")
			.answerMultipleChoice("Ok")
			.saveAnswer()
			.assertNoFeedback()
			.endTest();
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
	public void qti21EditorKprim(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO melissa = new UserRestClient(deploymentUrl).createRandomUser("Melissa");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Kprim QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		

		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
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
		LoginPage melLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		melLoginPage
			.loginAs(melissa.getLogin(), melissa.getPassword())
			.resume();
		NavigationPage melNavBar = NavigationPage.load(participantBrowser);
		melNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
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
	 * An author make a test with 2 hotspots with the single choice cardinality,
	 * the first with the score set if all answers are correct, the second
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
	public void qti21EditorHotspot_singleChoice(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		
		String qtiTestTitle = "Hotspot QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add an hotspot: all answers score
		QTI21HotspotEditorPage hotspotEditor = qtiEditor
			.addHotspot();
		// 2 spots
		URL backgroundImageUrl = JunitTestHelper.class.getResource("file_resources/house.jpg");
		File backgroundImageFile = new File(backgroundImageUrl.toURI());
		hotspotEditor
			.updloadBackground(backgroundImageFile)
			.moveToHotspotEditor()
			.resizeCircle()
			.moveCircle(300, 120)
			.addRectangle()
			.moveRectangle(150, 150)
			.setCardinality(Cardinality.SINGLE)
			.save();
		// change max score
		hotspotEditor
			.selectScores()
			.setMaxScore("3")
			.save();
		// some feedbacks
		hotspotEditor
			.selectFeedbacks()
			.setHint("Hint", "This is only an hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//add a second hotspot: score per answer
		hotspotEditor = qtiEditor
			.addHotspot();
		hotspotEditor
			.updloadBackground(backgroundImageFile)
			.moveToHotspotEditor()
			.resizeCircle()
			.moveCircle(310, 125)
			.addRectangle()
			.moveRectangle(145, 155)
			.setCardinality(Cardinality.SINGLE)
			.save();
		// change scoring
		hotspotEditor
			.selectScores()
			.setMaxScore("2")
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setScore("1.", "2")
			.setScore("2.", "0")
			.save();
		hotspotEditor
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();


		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerHotspot("rect")
			.saveGraphicAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerHotspot("rect")
			.saveGraphicAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(3);// 3 points from the first question, 0 from the second
		

		//a second user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(5);// 3 points from the first question, 2 from the second
	}

	/**
	 * An author make a test with 2 hotspots with the multiple choice cardinality,
	 * the first with the score set if all answers are correct, the second
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
	public void qti21EditorHotspot_multipleChoice(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		
		String qtiTestTitle = "Hotspot QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add an hotspot: all answers score
		QTI21HotspotEditorPage hotspotEditor = qtiEditor
			.addHotspot();
		// 2 spots
		URL backgroundImageUrl = JunitTestHelper.class.getResource("file_resources/house.jpg");
		File backgroundImageFile = new File(backgroundImageUrl.toURI());
		hotspotEditor
			.updloadBackground(backgroundImageFile)
			.moveToHotspotEditor()
			.resizeCircle()
			.moveCircle(300, 120)
			.addRectangle()
			.moveRectangle(150, 150)
			.setCardinality(Cardinality.MULTIPLE)
			.setCorrect("Hotspot 2", true)
			.save();
		// change max score
		hotspotEditor
			.selectScores()
			.setMaxScore("3")
			.save();
		// some feedbacks
		hotspotEditor
			.selectFeedbacks()
			.setHint("Hint", "This is only an hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//add a second hotspot: score per answer
		hotspotEditor = qtiEditor
			.addHotspot();
		hotspotEditor
			.updloadBackground(backgroundImageFile)
			.moveToHotspotEditor()
			.resizeCircle()
			.moveCircle(310, 125)
			.addRectangle()
			.moveRectangle(145, 155)
			.setCardinality(Cardinality.MULTIPLE)
			.setCorrect("Hotspot 2", true)
			.save();
		// change scoring
		hotspotEditor
			.selectScores()
			.setMaxScore("3")
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setScore("1.", "2")
			.setScore("2.", "1")
			.save();
		hotspotEditor
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();


		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerHotspot("rect")
			.saveGraphicAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(5);// 3 points from the first question, 2 from the second
		

		//a second user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerHotspot("circle")
			.answerHotspot("rect")
			.saveGraphicAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerHotspot("circle")
			.answerHotspot("rect")
			.saveGraphicAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 3 points from the first question, 3 from the second
	}

	/**
	 * An author make a test with 2 questions using fill-in-blank,
	 * the first with the score set if all answers are correct, the second
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
	public void qti21EditorFib_text(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());

		String qtiTestTitle = "FIB QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a gap entry: all answers score
		QTI21GapEntriesEditorPage fibEditor = qtiEditor
			.addFib()
			.appendContent("Usefull for circles ")
			.addGapEntry("Pi", "314")
			.saveGapEntry()
			.editGapEntry("Ln", "lognat", 2)
			.saveGapEntry()
			.save();
		//set max score
		fibEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.allCorrectAnswers)
			.setMaxScore("2")
			.save();
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setHint("Hint", "This is a usefull hint")
			.setCorrectSolution("Correct solution", "This is an information about the correct solution")
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//add a gap entry: score per anser
		fibEditor = qtiEditor
			.addFib()
			.appendContent("European rocket ")
			.addGapEntry("Ariane", "ari")
			.saveGapEntry()
			.editGapEntry("Falcon9", "falc", 2)
			.saveGapEntry()
			.save();
		//set max score
		fibEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("4")
			.setScore("Ariane", "3")
			.setScore("Falcon9", "1")
			.save();
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setHint("Hint", "Think to space")
			.setCorrectSolution("Correct solution", "This is an information about the correct solution")
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();

		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// first user make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerGapTextWithPlaceholder("Log", "314")
			.answerGapTextWithPlaceholder("Sin", "lognat")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerGapTextWithPlaceholder("Pi", "314")
			.answerGapTextWithPlaceholder("Ln", "lognat")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("Saturn 5", "ari")
			.answerGapTextWithPlaceholder("Falcon9", "falc")
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(3);// 2 points from the first question, 1 from the second
		

		//a second user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test with all the correct answers
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerGapTextWithPlaceholder("Pi", "314")
			.answerGapTextWithPlaceholder("Ln", "lognat")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("Ariane", "ari")
			.answerGapTextWithPlaceholder("Falcon9", "falc")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 2 points from the first question, 4 from the second
	}

	/**
	 * An author make a test with 2 questions using numerical input,
	 * the first with the score set if all answers are correct, the second
	 * with scoring per answers. The numerical input have all the tolerance
	 * mode set to EXACT.<br>
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
	public void qti21EditorNumericalInput_exact(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());

		String qtiTestTitle = "Numerical QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a numerical input: all answers score, tolerance exact
		QTI21GapEntriesEditorPage fibEditor = qtiEditor
			.addNumerical()
			.appendContent("One plus two: ")
			.addNumericalInput("3", "three", ToleranceMode.EXACT, null, null)
			.saveNumericInput()
			.editNumericalInput("9", "nine", ToleranceMode.EXACT, null, null, 2)
			.saveNumericInput()
			.save();
		//set max score
		fibEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.allCorrectAnswers)
			.setMaxScore("2")
			.save();
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setHint("Hint", "The second is the first power two")
			.setCorrectSolution("Correct solution", "I know you know")
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//add a gap entry: score per answer, tolerance exact
		fibEditor = qtiEditor
			.addNumerical()
			.appendContent("More difficult: 34 + 23 ")
			.addNumericalInput("57", "57", ToleranceMode.EXACT, null, null)
			.saveNumericInput()
			.editNumericalInput("8", "64squareroot",ToleranceMode.EXACT, null, null, 2)
			.saveNumericInput()
			.save();
		//set max score
		fibEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("4")
			.setScore("57", "2")
			.setScore("8", "3")
			.save();
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setHint("Hint", "The second is the square root of 64")
			.setCorrectSolution("Correct solution", "This is an information about the correct solution")
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();

		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// first user make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerGapTextWithPlaceholder("2", "three")
			.answerGapTextWithPlaceholder("25", "nine")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerGapTextWithPlaceholder("3", "three")
			.answerGapTextWithPlaceholder("9", "nine")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("57", "57")
			.answerGapTextWithPlaceholder("9", "64squareroot")
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(4);// 2 points from the first question, 4 from the second
		

		//a second user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test with all the correct answers
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerGapTextWithPlaceholder("3", "three")
			.answerGapTextWithPlaceholder("9", "nine")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("57", "57")
			.answerGapTextWithPlaceholder("8", "64squareroot")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 2 points from the first question, 4 from the second
	}

	/**
	 * An author make a test with 2 questions using numerical input to
	 * test the absolute tolerance mode.<br>
	 * A first user make the test, but doesn't answer all questions
	 * correctly, log out and a second user make the perfect test but
	 * on the limit.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorNumericalInput_absolut(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());

		String qtiTestTitle = "Numerical QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a numerical input: 3.1 - 3.2
		QTI21GapEntriesEditorPage fibEditor = qtiEditor
			.addNumerical()
			.appendContent("Usefull for circles ")
			.editNumericalInput("3.1416", "pi", ToleranceMode.ABSOLUTE, "3.2", "3.1", 1)
			.saveNumericInput()
			.save();
		// use standard score setting
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Out of bounds")
			.save();
		
		//add a numerical input which represent a rounding issue
		fibEditor = qtiEditor
			.addNumerical()
			.appendContent("Check rounding issue ")
			.editNumericalInput("14.923", "rounding", ToleranceMode.ABSOLUTE, "14.925", "14.915", 1)
			.saveNumericInput()
			.save();
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//add a numerical input with negative values
		fibEditor = qtiEditor
			.addNumerical()
			.appendContent("Check rounding issue ")
			.editNumericalInput("-14.923", "negative", ToleranceMode.ABSOLUTE, "-14.921", "-14.931", 1)
			.saveNumericInput()
			.save();
		// set feedbacks
		fibEditor
			.selectFeedbacks()
			.setCorrectFeedback("Correct feedback", "Your answer is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();

		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// first user make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerGapTextWithPlaceholder("3", "pi")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.answerGapTextWithPlaceholder("3.15", "pi")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("14.914", "rounding")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.answerGapTextWithPlaceholder("14.915", "rounding")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("-14.932", "negative")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.answerGapTextWithPlaceholder("-14.920", "negative")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(2);// 1 point + 1 point + 0 point
		

		//a second user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test with all the correct answers
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerGapTextWithPlaceholder("3.2", "pi")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("14.925", "rounding")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerGapTextWithPlaceholder("-14.921", "negative")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(3); 
	}

	/**
	 * An author make a test with a question with 2 hot texts, one checked,
	 * one not. A user make the test. The test is limited because I cannot
	 * edit the text within the hot text via the web driver.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorHottext(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Hottext QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a hot text with score: all answers
		QTI21HottextEditorPage hottextEditor = qtiEditor
			.addHottext()
			.appendContent("I cannot modify the hottext ")
			.addHottext()
			.uncheck(2)
			.check(1)
			.save();
		// change max score
		hottextEditor
			.selectScores()
			.setMaxScore("3")
			.save();
		// set some feedbacks
		hottextEditor
			.selectFeedbacks()
			.setHint("Hint", "This is an hint")
			.setCorrectSolution("Correct solution", "First not, second yes")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerHottext(2)
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerHottext(1)
			.answerHottext(2)//un select it
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(3);// 3 points from the first question	
	}
	
	/**
	 * Test the order editor. An author
	 * make a test with an order interaction and feedbacks.<br>
	 * A second user make the test and check the score at the end of
	 * the test.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorOrder(@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Order QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a single choice: all answers score
		QTI21OrderEditorPage orderEditor = qtiEditor
			.addOrder();
		orderEditor
			.setAnswer(0, "Mercury")
			.addChoice(1)
			.setAnswer(1, "Venus")
			.addChoice(2)
			.setAnswer(2, "Earth")
			.addChoice(3)
			.setAnswer(3, "Mars")
			.save();
		// change max score
		orderEditor
			.selectScores()
			.setMaxScore("3")
			.save();
		// set some feedbacks
		orderEditor
			.selectFeedbacks()
			.setHint("Hint", "This is an ordered hint")
			.setCorrectSolution("Correct solution", "You need to order the solution correctly")
			.setCorrectFeedback("Correct feedback", "You ordered the solution correctly")
			.setIncorrectFeedback("Incorrect", "Your answer is shuffled")
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		

		//a user search the content package
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
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(ryomouBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.answerOrderDropItem("Mercury", true)
			.answerOrderDropItem("Venus", true)
			.answerOrderDropItem("Earth", true)
			.answerOrderDropItem("Mars", true)
			.saveAnswer()
			.assertFeedback("Incorrect")
			// try again
			.moveOrderDropItemTop("Earth", true)
			.moveOrderDropItemTop("Venus", true)
			.moveOrderDropItemTop("Mercury", true)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(3);
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
	public void qti21EditorMatch(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO melissa = new UserRestClient(deploymentUrl).createRandomUser("Melissa");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Match QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
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
		LoginPage melLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		melLoginPage
			.loginAs(melissa.getLogin(), melissa.getPassword())
			.resume();
		NavigationPage melNavBar = NavigationPage.load(participantBrowser);
		melNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
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
	 * An author make a test with 2 matches. A match with "multiple selection"
	 * and score "all answers", a second with "single selection" and score
	 * "per answers". They are distractors, the assessed user must let them blank.<br>
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
	public void qti21EditorMatch_distractors(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO melissa = new UserRestClient(deploymentUrl).createRandomUser("Melissa");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Match QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a match, multiple selection
		QTI21MatchEditorPage matchEditor = qtiEditor
			.addMatch();
		matchEditor
			.setSource(0, "Eclipse")
			.setSource(1, "nano")
			.setTarget(0, "IDE")
			.setTarget(1, "WordProcessor")
			.addColumn()
			.setTarget(2, "CAD")
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
			.setTarget(0, "Lynx")
			.setTarget(1, "Netscape")
			.addColumn()
			.setTarget(2, "Pixel")
			.save();
		// select score "per answer" and set the scores
		matchEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("6")
			.setScore(0, 0, "0.0")
			.setScore(0, 1, "0.0")
			.setScore(0, 2, "1.0")
			.setScore(1, 0, "0.0")
			.setScore(1, 1, "1.0")
			.setScore(1, 2, "0.0")
			.setScore(2, 0, "2.0")
			.setScore(2, 1, "0.0")
			.setScore(2, 2, "-0.5")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		// publish
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.answerMatch("Eclipse", "WordProcessor", true)
			.answerMatch("nano", "CAD", true)
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerMatch("nano", "CAD", false)
			.answerMatch("Eclipse", "WordProcessor", false)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatch("Java", "Pixel", true)
			.answerMatch("C", "Lynx", true)
			.answerMatch("PHP", "Pixel", true)
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore("4.5");// 4 points from the first question, 0.5 from the second
		
		//a second user search the content package
		LoginPage melLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		melLoginPage
			.loginAs(melissa.getLogin(), melissa.getPassword())
			.resume();
		NavigationPage melNavBar = NavigationPage.load(participantBrowser);
		melNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatch("Java", "Pixel", true)
			.answerMatch("C", "Pixel", true)
			.answerMatch("PHP", "Lynx", true)
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.answerMatch("Java", "Pixel", false)
			.answerMatch("C", "Pixel", false)
			.answerMatch("PHP", "Lynx", false)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(10);// 4 points from the first question, 6 from the second
	}
	
	/**
	 * An author make a test with 2 match of the drag and drop variety
	 * with feedbacks.<br>
	 * A first user make the test, check the feedbacks but make an error
	 * and score the maximum. A second user answers all the questions
	 * correctly.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorMatchDragAndDrop(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO asuka = new UserRestClient(deploymentUrl).createRandomUser("Asuka");
		UserVO chara = new UserRestClient(deploymentUrl).createRandomUser("Chara");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Match DnD QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a match, multiple selection
		QTI21MatchEditorPage matchEditor = qtiEditor
			.addMatchDragAndDrop();
		matchEditor
			.setSource(0, "Einstein")
			.setSource(1, "Planck")
			.addRow()
			.setSource(2, "Euler")
			.setTarget(0, "Physicist")
			.setTarget(1, "Mathematician")
			.setMatch(0, 0, true)
			.setMatch(1, 0, true)
			.setMatch(2, 1, true)
			.save();
		// change max score
		matchEditor
			.selectScores()
			.setMaxScore("4")
			.save();
		// set some feedbacks
		matchEditor
			.selectFeedbacks()
			.setHint("Hint", "Euler come from Switzerland")
			.setCorrectSolution("Correct solution", "The correct solution is simple")
			.setCorrectFeedback("Correct feedback", "You are right")
			.setIncorrectFeedback("Incorrect", "Your answer is not exactly correct")
			.save();
		
		// second match
		matchEditor = qtiEditor
			.addMatchDragAndDrop()
			.setSingleChoices()
			.setSource(0, "Euler")
			.setSource(1, "Broglie")
			.addRow()
			.setSource(2, "Konrad")
			.setTarget(0, "Mathematics")
			.setTarget(1, "Medicine")
			.addColumn()
			.setTarget(2, "Physics")
			.setMatch(0, 0, true)
			.setMatch(1, 2, true)
			.setMatch(2, 1, true)
			.save();
		// select score "per answer" and set the scores
		matchEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("8")
			.setScore(0, 0, "2.0")
			.setScore(0, 1, "0.0")
			.setScore(0, 2, "0.0")
			.setScore(1, 0, "0.0")
			.setScore(1, 1, "0.0")
			.setScore(1, 2, "3.0")
			.setScore(2, 0, "0.0")
			.setScore(2, 1, "2.0")
			.setScore(2, 2, "0.0")
			.save();
		matchEditor
			.selectFeedbacks()
			.setHint("Hint", "The hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//close editor
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage asukaLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		asukaLoginPage
			.loginAs(asuka.getLogin(), asuka.getPassword())
			.resume();
		NavigationPage asukaNavBar = NavigationPage.load(participantBrowser);
		asukaNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page asukaQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		asukaQtiPage
			.assertOnAssessmentItem()
			.answerMatchDropSourceToTarget("Einstein", "Physicist")
			.answerMatchDropSourceToTarget("Planck", "Mathematician")
			.answerMatchDropSourceToTarget("Euler", "Mathematician")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerMatchDropTargetToTarget("Planck", "Physicist")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatchDropSourceToTarget("Broglie", "Physics") // 2 points
			.answerMatchDropSourceToTarget("Euler", "Medicine")  // 2 points
			.answerMatchDropSourceToTarget("Konrad", "Medicine") // 3 points
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(9);
		
		//a second user search the content package
		LoginPage charaLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		charaLoginPage
			.loginAs(chara.getLogin(), chara.getPassword())
			.resume();
		NavigationPage charaNavBar = NavigationPage.load(participantBrowser);
		charaNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerMatchDropSourceToTarget("Einstein", "Physicist")
			.answerMatchDropSourceToTarget("Planck", "Physicist")
			.answerMatchDropSourceToTarget("Euler", "Mathematician")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatchDropSourceToTarget("Broglie", "Physics")   // 2 points
			.answerMatchDropSourceToTarget("Euler", "Mathematics") // 2 points
			.answerMatchDropSourceToTarget("Konrad", "Medicine")   // 3 points
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(11);// 4 points from the first question, 7 from the second	
	}
	
	/**
	 * An author make a test with 2 match of the drag and drop variety
	 * with feedbacks but as distractor. The assessed user need to let them
	 * blank to have the max. score.<br>
	 * A first user make the test, check the feedbacks but make an error
	 * and score the maximum. A second user answers all the questions
	 * correctly.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorMatchDragAndDrop_distractors(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO asuka = new UserRestClient(deploymentUrl).createRandomUser("Asuka");
		UserVO chara = new UserRestClient(deploymentUrl).createRandomUser("Chara");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Match DnD QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a match, multiple selection
		QTI21MatchEditorPage matchEditor = qtiEditor
			.addMatchDragAndDrop();
		matchEditor
			.setSource(0, "Einstein")
			.setSource(1, "Planck")
			.addRow()
			.setSource(2, "Euler")
			.setTarget(0, "Chemistry")
			.setTarget(1, "Philosophy")
			.save();
		// change max score
		matchEditor
			.selectScores()
			.setMaxScore("4")
			.save();
		// set some feedbacks
		matchEditor
			.selectFeedbacks()
			.setHint("Hint", "Euler come from Switzerland")
			.setCorrectSolution("Correct solution", "The correct solution is simple")
			.setCorrectFeedback("Correct feedback", "You are right")
			.setIncorrectFeedback("Incorrect", "Your answer is not exactly correct")
			.save();
		
		// second match
		matchEditor = qtiEditor
			.addMatchDragAndDrop()
			.setSingleChoices()
			.setSource(0, "Euler")
			.setSource(1, "Broglie")
			.addRow()
			.setSource(2, "Konrad")
			.setTarget(0, "Chemistry")
			.setTarget(1, "Biology")
			.addColumn()
			.setTarget(2, "Astrology")
			.save();
		// select score "per answer" and set the scores
		matchEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("8")
			.setScore(0, 0, "1.0")
			.setScore(0, 1, "0.0")
			.setScore(0, 2, "0.0")
			.setScore(1, 0, "0.0")
			.setScore(1, 1, "0.0")
			.setScore(1, 2, "-0.5")
			.setScore(2, 0, "0.0")
			.setScore(2, 1, "2.0")
			.setScore(2, 2, "0.0")
			.save();
		matchEditor
			.selectFeedbacks()
			.setHint("Hint", "The hint")
			.setCorrectSolution("Correct solution", "This is the correct solution")
			.setCorrectFeedback("Correct feedback", "This is correct")
			.setIncorrectFeedback("Incorrect", "Your answer is not correct")
			.save();
		
		//close editor
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage asukaLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		asukaLoginPage
			.loginAs(asuka.getLogin(), asuka.getPassword())
			.resume();
		NavigationPage asukaNavBar = NavigationPage.load(participantBrowser);
		asukaNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page asukaQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		asukaQtiPage
			.assertOnAssessmentItem()
			.answerMatchDropSourceToTarget("Einstein", "Chemistry")
			.answerMatchDropSourceToTarget("Planck", "Philosophy")
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerMatchDetarget("Planck")
			.answerMatchDetarget("Einstein")
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatchDropSourceToTarget("Broglie", "Astrology") // -0.5 points
			.answerMatchDropSourceToTarget("Euler", "Chemistry")  // 1 points
			.answerMatchDropSourceToTarget("Konrad", "Chemistry") // 0 points
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore("4.5");
		
		//a second user search the content package
		LoginPage charaLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		charaLoginPage
			.loginAs(chara.getLogin(), chara.getPassword())
			.resume();
		NavigationPage charaNavBar = NavigationPage.load(participantBrowser);
		charaNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatchDropSourceToTarget("Broglie", "Chemistry")   // 2 points
			.answerMatchDropSourceToTarget("Euler", "Astrology") // 2 points
			.answerMatchDropSourceToTarget("Konrad", "Astrology")   // 3 points
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.answerMatchDetarget("Broglie")
			.answerMatchDetarget("Euler")
			.answerMatchDetarget("Konrad")
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(12);// 4 points from the first question, 8 from the second	
	}
	

	/**
	 * An author make a test with 2 matches of the True/false variant. A match
	 * with score "all answers", a second with score "per answers".<br>
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
	public void qti21EditorMatchTrueFalse(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO asuka = new UserRestClient(deploymentUrl).createRandomUser("Asuka");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "True false QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
			.deleteNode();
		
		//add a match, score "all answers"
		QTI21MatchEditorPage matchEditor = qtiEditor
			.addMatchTrueFalse();
		matchEditor
			.setSource(0, "Eclipse is a Java IDE")
			.setSource(1, "vim is Database viewer")
			.setMatch(0, 1, true)
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
			.addMatchTrueFalse()
			.setSource(0, "Java has several IDE like Eclipse")
			.setSource(1, "C is object oriented")
			.addRow()
			.setSource(2, "What do you think of PHP?")
			.setMatch(0, 1, true)
			.setMatch(1, 2, true)
			.setMatch(2, 0, true)
			.save();
		// select score "per answer" and set the scores
		matchEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("3")
			.setScore(0, 0, "0.0")
			.setScore(0, 1, "1.0")
			.setScore(0, 2, "0.0")
			.setScore(1, 0, "0.0")
			.setScore(1, 1, "0.0")
			.setScore(1, 2, "1.0")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.answerMatch("Eclipse", TrueFalse.right, true)
			.answerMatch("vim", TrueFalse.right, true)
			.saveAnswer()
			.assertFeedback("Incorrect")
			.assertCorrectSolution("Correct solution")
			.hint()
			.assertFeedback("Hint")
			.answerMatch("vim", TrueFalse.wrong, true)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatch("Java", TrueFalse.right, true)
			.answerMatch("oriented", TrueFalse.wrong, true)
			.answerMatch("PHP", TrueFalse.wrong, true)
			.saveAnswer()
			.assertCorrectSolution("Correct solution")
			.assertFeedback("Incorrect")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(6);// 4 points from the first question, 2 from the second
		

		//a second user search the content package
		LoginPage asukaLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		asukaLoginPage
			.loginAs(asuka.getLogin(), asuka.getPassword())
			.resume();
		NavigationPage asukaNavBar = NavigationPage.load(participantBrowser);
		asukaNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page
			.getQTI21Page(participantBrowser)
			.assertOnAssessmentItem()
			.answerMatch("Eclipse", TrueFalse.right, true)
			.answerMatch("vim", TrueFalse.wrong, true)
			.saveAnswer()
			.assertFeedback("Correct feedback")
			.nextAnswer()
			.answerMatch("Java", TrueFalse.right, true)
			.answerMatch("oriented", TrueFalse.wrong, true)
			.saveAnswer()
			.endTest()
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(7);// 4 points from the first question, 6 from the second
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
	public void qti21EditorUpload(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//make a test
		String qtiTestTitle = "Upload QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
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
	public void qti21EditorEssay(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		

		//make a test
		String qtiTestTitle = "Essai QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
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
	public void qti21EditorDrawing(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		

		//make a test
		String qtiTestTitle = "Drawing QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//start a blank test
		qtiEditor
			.selectNode("Single Choice")
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
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.saveAnswer()
			.assertFeedback("Empty")
			.hint()
			.assertFeedback("Hint");

		reiQtiPage
			.answerDrawing()
			.saveAnswerMoveAndScrollTop()
			.assertFeedback("Full")
			.endTest()
			.assertOnAssessmentResults()
			.assertOnDrawing();
	}

	/**
	 * An author make a test with 2 questions and in the expert
	 * settings of the section, it hides the title. It set the
	 * access configuration.<br>
	 * A user search the test, make it, check that the sections
	 * are not visible, pass the test and check the assessment
	 * results.
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorHiddenSection(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//customize the section
		qtiEditor
			.selectSection()
			.selectExpertOptions()
			.sectionTitle(false)
			.save();
		
		//edit the default single choice
		qtiEditor
			.selectItem("Single Choice");
		QTI21SingleChoiceEditorPage scEditor = new QTI21SingleChoiceEditorPage(browser);
		scEditor
			.setAnswer(0, "Wrong")
			.addChoice(1)
			.setCorrect(1)
			.setAnswer(1, "Correct")
			.addChoice(2)
			.setAnswer(2, "Faux")
			.save();
		//add a multiple choice
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
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.assertHiddenSection()
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.answerMultipleChoice("OkToo")
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.endTest()
		//check the results
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(2)
			.assertOnAssessmentTestMaxScore(2);
	}
	
	/**
	 * An author make a test and use the negative points.<br>
	 * 3 users search the test, pass the test or not and
	 * check their results.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EditorNegativePoints(@Drone @User WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO asuka = new UserRestClient(deploymentUrl).createRandomUser("Asuka");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Choices QTI 2.1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createQTI21Test(qtiTestTitle)
			.clickToolbarBack();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser);
		QTI21EditorPage qtiEditor = qtiPage
				.assertOnAssessmentItem()
				.edit();
		//customize the section
		qtiEditor
			.selectSection()
			.selectExpertOptions()
			.sectionTitle(false)
			.save();
		
		//edit the default single choice
		qtiEditor
			.selectItem("Single Choice");
		QTI21SingleChoiceEditorPage scEditor = new QTI21SingleChoiceEditorPage(browser);
		scEditor
			.setAnswer(0, "Wrong")
			.addChoice(1)
			.setCorrect(1)
			.setAnswer(1, "Correct")
			.addChoice(2)
			.setAnswer(2, "Faux")
			.save();
		scEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("2.0")
			.setMinScore("-1.0")
			.setScore("Wrong", "-1")
			.setScore("Correct", "2")
			.setScore("Faux", "-1")
			.save();
			
		//add a multiple choice
		QTI21MultipleChoiceEditorPage mcEditor = qtiEditor
			.addMultipleChoice();
		mcEditor
			.setAnswer(0, "Correct")
			.setCorrect(0)
			.addChoice(1)
			.setCorrect(1)
			.setAnswer(1, "Ok")
			.addChoice(2)
			.setAnswer(2, "Faux")
			.addChoice(3)
			.setAnswer(3, "Falsch")
			.save();
		mcEditor.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("2.0")
			.setMinScore("-2")
			.setScore("Correct", "1")
			.setScore("Ok", "1")
			.setScore("Faux", "-2")
			.setScore("Falsch", "-2")
			.save();
		
		//add an hotspot
		QTI21HotspotEditorPage hotspotEditor = qtiEditor
			.addHotspot();
		// 2 spots
		URL backgroundImageUrl = JunitTestHelper.class.getResource("file_resources/house.jpg");
		File backgroundImageFile = new File(backgroundImageUrl.toURI());
		hotspotEditor
			.updloadBackground(backgroundImageFile)
			.moveToHotspotEditor()
			.resizeCircle()
			.moveCircle(300, 120)
			.addRectangle()
			.moveRectangle(150, 150)
			.setCardinality(Cardinality.SINGLE)
			.save();
		hotspotEditor
			.selectScores()
			.selectAssessmentMode(ScoreEvaluation.perAnswer)
			.setMaxScore("3.0")
			.setMinScore("-2")
			.setScore("1.", "3.0") //circle
			.setScore("2.", "-2")  //rectangle
			.save();
		
		qtiPage
			.clickToolbarBack();
		// access to all
		qtiPage
			.settings()
			.accessConfiguration()
			.setUserAccess(UserAccess.guest)
			.save()
			.clickToolbarBack();
		qtiPage
			.publish();
		// show results
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		
		//a user search the content package
		LoginPage userLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		userLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		NavigationPage userNavBar = NavigationPage.load(participantBrowser);
		userNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test with all correct answers
		QTI21Page ryomouQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		ryomouQtiPage
			.assertOnAssessmentItem()
			.assertHiddenSection()
			.answerSingleChoiceWithParagraph("Correct")
			.saveAnswer()
			.answerMultipleChoice("Ok")
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.endTest()
		//check the results
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(7)
			.assertOnAssessmentTestMaxScore(7);
		

		//a  second user search the content package
		LoginPage asukaLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		asukaLoginPage
			.loginAs(asuka.getLogin(), asuka.getPassword())
			.resume();
		NavigationPage asukaNavBar = NavigationPage.load(participantBrowser);
		asukaNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test with all correct answers
		QTI21Page asukaQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		asukaQtiPage
			.assertOnAssessmentItem()
			.assertHiddenSection()
			.answerSingleChoiceWithParagraph("Wrong")
			.saveAnswer()
			.answerMultipleChoice("Falsch")
			.answerMultipleChoice("Faux")
			.saveAnswer()
			.answerHotspot("rect")
			.saveGraphicAnswer()
			.endTest()
		//check the results
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(0) // -1 + -4 but never under 0
			.assertOnAssessmentTestMaxScore(7);
		
		//a third user search the content package
		LoginPage reiLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei.getLogin(), rei.getPassword())
			.resume();
		NavigationPage reiNavBar = NavigationPage.load(participantBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(qtiTestTitle)
			.select(qtiTestTitle)
			.start();
		
		// make the test with some correct answers
		QTI21Page reiQtiPage = QTI21Page
				.getQTI21Page(participantBrowser);
		reiQtiPage
			.assertOnAssessmentItem()
			.assertHiddenSection()
			.answerSingleChoiceWithParagraph("Faux")
			.saveAnswer()
			.answerMultipleChoice("Ok")
			.answerMultipleChoice("Correct")
			.saveAnswer()
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.endTest()
		//check the results
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(4) // -1 + 2 + 3 points
			.assertOnAssessmentTestMaxScore(7);
	}
}