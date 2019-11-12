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
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.qpool.QuestionPoolPage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * Test the question pool.
 * 
 * 
 * Initial date: 27 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class QuestionPoolTest extends Deployments {
	
	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	
	/**
	 * Smoke test: an author create a QTI 2.1 question,
	 * a single choice one.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void questionPool()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor("Lili");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String questionTitle = "SC-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		QuestionPoolPage questionPool = navBar.assertOnNavigationPage()
			.openQuestionPool();
		questionPool
			.selectMyQuestions()
			.newQuestion(questionTitle, QTI21QuestionType.sc)
			.clickToolbarBack()
			.assertQuestionInList(questionTitle, QTI21QuestionType.sc.name());
	}
	
	/**
	 * An administrator set up the review process. A question need
	 * 1 review with more than 2 stars. Than it checks that the taxonomy
	 * for the question pool as an "At least one" taxonomy level.<br>
	 * An author create a question. A reviewer reviews the question
	 * and give it 4 stars. The author see the result.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void reviewProcess()
	throws IOException, URISyntaxException {

		UserVO reviewer = new UserRestClient(deploymentUrl).createAuthor("Albert");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		AdministrationPage administration = NavigationPage.load(browser)
			.openAdministration();
		// configure the review process
		administration
			.openQuestionPool()
			.enableReviews()
			.reviewsConfiguration(1, 2);
		// configure the taxonomy
		administration
			.openTaxonomy()
			.selectTaxonomy("QPOOL")
			.assertOnMetadata()
			.selectTaxonomyTree()
			.atLeastOneLevel("at-least-one", "At least one")
			.selectTaxonomyLevel("at-least-one")
			.selectCompetence()
			.addCompetence(reviewer, TaxonomyCompetenceTypes.teach);
		
		new UserToolsPage(browser).logout();
		
		// author create a question
		UserVO author = new UserRestClient(deploymentUrl).createAuthor("Leila");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String questionTitle = "SC-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		QuestionPoolPage questionPool = navBar
			.assertOnNavigationPage()
			.openQuestionPool();
		questionPool
			.selectMyQuestions()
			.newQuestion(questionTitle, QTI21QuestionType.sc)
			.startReviewProcess()
			.clickToolbarBack()
			.assertQuestionInList(questionTitle, QTI21QuestionType.sc.name());
		// author log out
		new UserToolsPage(browser).logout();
		
		// reviewer make its job
		loginPage
			.loginAs(reviewer.getLogin(), reviewer.getPassword())
			.resume();
		navBar.assertOnNavigationPage()
			.openQuestionPool()
			.selectTaxonomyLevel("At least one")
			.selectQuestionInList(questionTitle)
			.assessQuestion(4)
			.assertFinalOnQuestion();

		new UserToolsPage(browser).logout();
		
		// author come to see the result
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		navBar.assertOnNavigationPage()
			.openQuestionPool()
			.selectMyQuestions()
			.assertFinalQuestionInList(questionTitle);
	}
	
	/**
	 * An author create a QTI 2.1 question, a multiple choice,
	 * and fill the general metadata. Go back in list and check
	 * the metadata are there.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void questionPoolGeneralMetadata()
	throws IOException, URISyntaxException {
		// prepare taxonomy
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		AdministrationPage administration = NavigationPage.load(browser)
			.openAdministration();
		// add a level
		administration
			.openQuestionPool()
			.selectLevels()
			.addLevel("Primary School")
			.assertLevelInList("Primary School");
		// configure the taxonomy
		administration
			.openTaxonomy()
			.selectTaxonomy("QPOOL")
			.assertOnMetadata()
			.selectTaxonomyTree()
			.atLeastOneLevel("one-fore-question-pool", "One for question pool");
		
		new UserToolsPage(browser).logout();
		
		// The author create a new question with metadata
		UserVO author = new UserRestClient(deploymentUrl).createAuthor("Lili");

		loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String questionTitle = "MetaMC-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		QuestionPoolPage questionPool = navBar.assertOnNavigationPage()
			.openQuestionPool();
		questionPool
			.selectMyQuestions()
			.newQuestion(questionTitle, QTI21QuestionType.mc)
			.metadata()
			.openGeneral()
			.setGeneralMetadata("New topic", "One for question pool", "Primary School",
					"Interessant", "Add. infos", "Wide coverage", "formative")
			.saveGeneralMetadata();
		
		// open quick view
		questionPool	
			.clickToolbarBack()
			.assertQuestionInList(questionTitle, QTI21QuestionType.mc.name())
			.openQuickView(questionTitle)
			.metadata()
			.openGeneral()
			.assertTopic("New topic")
			.assertTaxonomy("One for question pool")
			.assertLevel("Primary School")
			.assertKeywords("Interessant")
			.assertAdditionalInfos("Add. infos")
			.assertCoverage("Wide coverage")
			.assertAssessmentType("formative");
	}
	
	/**
	 * An author create a QTI 2.1 question, a fill in blank,
	 * and fill the item analyze metadata. Go back in list and check
	 * that the metadata are there.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void questionPoolItemAnalyseMetadata()
	throws IOException, URISyntaxException {
		// The author create a new question with metadata
		UserVO author = new UserRestClient(deploymentUrl).createAuthor("Lili");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		String questionTitle = "MetaMC-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		QuestionPoolPage questionPool = navBar.assertOnNavigationPage()
			.openQuestionPool();
		questionPool
			.selectMyQuestions()
			.newQuestion(questionTitle, QTI21QuestionType.fib)
			.metadata()
			.openItemAnalyse()
			.setLearningTime(1, 5, 3, 35)
			.setItemAnalyse(0.5d, 0.3d, -0.7d, 2, 3)
			.saveItemAnalyse();
		
		// open quick view
		questionPool	
			.clickToolbarBack()
			.assertQuestionInList(questionTitle, QTI21QuestionType.fib.name())
			.openQuickView(questionTitle)
			.metadata()
			.openItemAnalyse()
			.assertLearningTime(1, 5, 3, 35)
			.assertDifficulty(0.5d)
			.assertStandardDeviation(0.3d)
			.assertDiscriminationIndex(-0.7d)
			.assertDistractors(2)
			.assertUsage(3);
	}

}
