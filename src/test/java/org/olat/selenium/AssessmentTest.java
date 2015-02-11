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
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.qti.QTI12Page;
import org.olat.test.ArquillianDeployments;
import org.olat.test.JunitTestHelper;
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
		WebElement resultsEl = browser.findElement(By.id("o_qti_results"));
		Assert.assertTrue(resultsEl.getText().contains(author.getFirstName()));
		//close the test
		testPage
			.closeTest();
		//all answers are correct -> passed
		WebElement passedEl = browser.findElement(By.cssSelector("tr.o_state.o_passed"));
		Assert.assertTrue(passedEl.isDisplayed());
	}

}
