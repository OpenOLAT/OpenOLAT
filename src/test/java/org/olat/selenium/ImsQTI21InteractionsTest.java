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
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.qti.QTI21Page;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;


/**
 * This suite tests the interactions in runtime and only in runtime. The source used
 * come from the IMS examples.
 * 
 * <ul>
 *   <li>Hotspot
 *   <li>Associate
 *   <li>Graphic associate
 *   <li>Match
 *   <li>Graphic Gap Match (with click and drop)
 *   <li>End interaction with inline and modal feedbacks (classic hint)
 *   <li>Select point interaction
 *   <li>Position object interaction
 *   <li>Order interaction
 *   <li>Graphic order interaction
 *   <li>Inline choice
 *   <li>Slider
 * </ul>
 * 
 * Initial date: 26 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class ImsQTI21InteractionsTest extends Deployments {
	
	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	/**
	 * Check if the hotspot interaction send a "correct" feedback.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21HotspotInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Hotspot QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_hotspot.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerHotspot("circle")
			.saveGraphicAnswer()
			.assertFeedback("Correct!")
			.endTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentTestScore(1)
			.assertOnAssessmentTestMaxScore(1);
	}
	
	/**
	 * Check if the associate interaction return its 4 points.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21AssociateInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Associate QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_associate_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.moveToAssociateItems()
			.answerAssociate("Antonio", 1, true)
			.answerAssociate("Prospero", 1, false)
			.answerAssociate("Capulet", 2, true)
			.answerAssociate("Montague", 2, false)
			.answerAssociate("Demetrius", 3, true)
			.answerAssociate("Lysander", 3, false)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Shakespearian Rivals", 4);
	}
	
	/**
	 * Check if the graphic associate interaction return its 2 points.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21GraphicAssociateInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Graphic associate QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_graphic_associate_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.moveToGraphicAssociateInteraction()
			.answerGraphicAssociate("B")
			.answerGraphicAssociate("C")
			.answerGraphicAssociate("C")
			.answerGraphicAssociate("D")
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Low-cost Flying", 2);
	}
	
	/**
	 * Check if the classic match interaction return its 3 points.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21MatchInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Match QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_match_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerMatch("Prospero", "Romeo and Juliet", true)
			.answerMatch("Capulet", "Romeo and Juliet", true)
			.answerMatch("Demetrius", "A Midsummer", true)
			.answerMatch("Lysander", "A Midsummer", true)
			//ooops
			.answerMatch("Prospero", "Romeo and Juliet", false)
			.answerMatch("Prospero", "The Tempest", true)
			.saveAnswer()
			.endTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Characters and Plays", 3);
	}
	
	/**
	 * Check if the order interaction return its 1 point.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21OrderInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Order QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_order_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerOrderDropItem("Jenson", false)
			.answerOrderDropItem("Rubens", false)
			.answerOrderDropItem("Michael", false)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Grand Prix of Bahrain", 1);
	}
	
	/**
	 * Check if hint with modal and inline feedbacks used
	 * with choice interaction.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21EndInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "End QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_end_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.hint()
			.assertFeedbackText("Tony lives in the United Kingdom")
			.answerSingleChoiceWithParagraph("Tony")
			.saveAnswer()
			.assertFeedbackText("No, the correct answer is Vicente Fox")
			.assertFeedbackInline("No, he is the Prime Minister of England.")
			.assertNoFeedbackText("Tony lives in the United Kingdom")
			.answerSingleChoiceWithParagraph("George")
			.saveAnswer()
			.assertFeedbackText("No, the correct answer is Vicente Fox")
			.assertFeedbackInline("No, he is the President of the USA.")
			.assertNoFeedbackText("Tony lives in the United Kingdom")
			.assertNoFeedbackInline("No, he is the Prime Minister of England.")
			.answerSingleChoiceWithParagraph("Vicente")
			.saveAnswer()
			.assertFeedbackText("Yes, that is correct")
			.assertNoFeedbackText("Tony lives in the United Kingdom")
			.assertNoFeedbackInline("No, he is the Prime Minister of England.")
			.assertNoFeedbackInline("No, he is the President of the USA.")
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Mexican President", 1);
	}
	
	/**
	 * Check if hint with modal and inline feedbacks used
	 * with choice interaction.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21GraphicGapInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Graphic Gap Match QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_graphic_gap_match_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerGraphicGapClick("GLA", "A")
			.answerGraphicGapClick("EDI", "B")
			.answerGraphicGapClick("MAN", "C")
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Airport Tags", 3);
	}
	
	/**
	 * Check if the select point interaction returns 1 point
	 * if answered correctly.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21SelectPointInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Select point QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_select_point_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerSelectPoint(100, 110)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Where is Edinburgh", 1);
	}
	
	/**
	 * Check if the graphic order interaction returns 1 point
	 * if answered correctly.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21GraphicOrderInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Graphic order QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_graphic_order_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.moveToGraphicOrderInteraction()
			.answerGraphicOrderById("A")
			.answerGraphicOrderById("D")
			.answerGraphicOrderById("C")
			.answerGraphicOrderById("B")
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Flying Home", 1);
	}
	
	/**
	 * Check if the position object interaction returns 3 points
	 * if answered correctly.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21PositionObjectInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Position object QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_position_object_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.moveToPositionObject()
			.answerPositionObject(0, 118, 184, 4)
			.answerPositionObject(1, 150, 235, 4)
			.answerPositionObject(2, 96, 114, 4)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Airport Locations", 3);
	}
	
	/**
	 * Check if the position object interaction returns 3 points
	 * if answered correctly.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21InlineChoiceInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Inline choice QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_inline_choice_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
				.getQTI21Page(browser)
				.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerInlineChoice("Y")
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Richard III", 1);
	}
	
	/**
	 * Check if the slider interaction returns 1 point
	 * if answered correctly.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21SliderInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Slider QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_slider_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser)
			.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.moveToVerticalSlider()
			.answerVerticalSlider(16)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Jedi Knights", 1);
	}
	
	/**
	 * Check if the gap match returns 3 points
	 * if answered correctly.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21GapMatchInteraction()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Gap match QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_gap_match_ims.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser)
			.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			.answerGapMatch(1, "winter", true)
			.answerGapMatch(2, "summer", true)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Richard III", 3);
	}
	
	/**
	 * This is an assessment item with severals
	 * different interactions.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void qti21MultipleInput()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//upload a test
		String qtiTestTitle = "Gap match QTI 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_multi-input.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile)
			.clickToolbarRootCrumb();
		
		QTI21Page qtiPage = QTI21Page
			.getQTI21Page(browser)
			.assertOnAssessmentItem();
		qtiPage
			.settings()
			.options()
			.showResults(Boolean.TRUE, QTI21AssessmentResultsOptions.allOptions())
			.save();
		// to the test and spot it
		qtiPage
			.clickToolbarBack()
			.assertOnAssessmentItem()
			// the single choice
			.answerSingleChoice("Some people are afraid of a woman")
			// the inline choice
			.answerInlineChoice("A2")
			// the text entry
			.answerGapText("wicked king", "RESPONSE3")
			// the gap match
			.answerGapMatch(1, "family", true)
			.answerGapMatch(2, "castle", true)
			.answerGapMatch(3, "horse", true)
			.saveAnswer()
			.endTest()
			.closeTest();
		//check the results
		qtiPage
			.assertOnAssessmentResults()
			.assertOnAssessmentItemScore("Legend", 4);
	}

}
