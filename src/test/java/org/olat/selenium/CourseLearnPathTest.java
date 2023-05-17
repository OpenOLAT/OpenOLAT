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
import org.olat.course.CourseModule;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CourseExamWizardPage;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.GroupTaskConfigurationPage;
import org.olat.selenium.page.course.GroupTaskPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.SinglePageConfigurationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.qti.QTI21Page;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.RepositorySettingsPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.AuthoringEnvPage.Wizard;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Test specifically features of the course in learn path mode.
 * 
 * Initial date: 8 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class CourseLearnPathTest extends Deployments {
	
	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;
	

	/**
	 * This is a course with learn path, three course elements,
	 * two to confirm, one to visit. The author creates the course
	 * and a user play it until 100% done.
	 * 
	 * @param participantBrowser Browser of the participant
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void learnPathConfirmationAndVisitedFlow()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-auto-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//create a course element of type single page
		String firstNodeTitle = "First page";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(firstNodeTitle);
		
		String content = "A new single page with some content";
		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.createEditPage("lsp.html", content)
			.assertOnPreview();
		
		courseEditor
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.confirmed)
			.save();
		
		// create a second element, a forum
		String forumNodeTitle = "Forum discussion";
		courseEditor
			.createNode("fo")
			.nodeTitle(forumNodeTitle)
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.confirmed)
			.save();
		
		// create a third element, an info message
		String infosNodeTitle = "Informations";
		courseEditor
			.createNode("info")
			.nodeTitle(infosNodeTitle)
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.nodeVisited)
			.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.members()
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Participant log in
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant)
			.resume();
		
		//open the course
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment participantCourse = new CoursePageFragment(browser);
		MenuTreePageFragment menuTree = participantCourse
			.tree()
			.selectWithTitle(firstNodeTitle);
		participantCourse
			.assertOnLearnPathNodeReady(firstNodeTitle)
			.confirmNode()
			.assertOnLearnPathNodeDone(firstNodeTitle)
			.assertOnLearnPathNodeInProgress(courseTitle)
			.assertOnLearnPathNodeReady(forumNodeTitle)
			.assertOnLearnPathNodeNotAccessible(infosNodeTitle);
		
		// confirm second node
		menuTree
			.selectWithTitle(forumNodeTitle)
			.assertWithTitleSelected(forumNodeTitle);
		participantCourse
			.confirmNode()
			.assertOnLearnPathNodeDone(forumNodeTitle)
			.assertOnLearnPathNodeInProgress(courseTitle)
			.assertOnLearnPathNodeReady(infosNodeTitle);

		// see the third node
		menuTree
			.selectWithTitle(infosNodeTitle)
			.assertWithTitleSelected(infosNodeTitle);
		participantCourse
			.assertOnLearnPathNodeDone(infosNodeTitle)
			.assertOnLearnPathNodeDone(firstNodeTitle)
			.assertOnLearnPathNodeDone(forumNodeTitle)
			.assertOnLearnPathNodeDone(courseTitle)
			.assertOnLearnPathPercent(100);
	}
	

	/**
	 * This is a degenerated form of task but the case exists.
	 * An author creates a course (learn path) with a task course
	 * element. The task is configured to only show the solution,
	 * and the course element to be passed if the task is done. In
	 * this case, the participant only need to see the solution to
	 * get the node done.
	 * 
	 * @param participantBrowser Browser of the participant
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void taskLearnPathSolutionsOnly()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-auto-task-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String gtaNodeTitle = "Solution 1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("ita")
			.nodeTitle(gtaNodeTitle);
		
		courseEditor
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.statusDone)
			.save();
		
		GroupTaskConfigurationPage gtaConfig = new GroupTaskConfigurationPage(browser);
		gtaConfig
			.selectWorkflow()
			.enableAssignment(false)
			.enableSubmission(false)
			.enableReview(false)
			.enableGrading(false)
			.saveWorkflow();
		
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
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Participant log in
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant)
			.resume();
		
		//open the course
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment participantCourse = new CoursePageFragment(browser);
		participantCourse
			.tree()
			.selectWithTitle(gtaNodeTitle);
		
		GroupTaskPage participantTask = new GroupTaskPage(browser);
		participantTask
			.openSolutions()
			.assertSolution("solution_1.txt");
		// seeing the solution got the job done
		participantCourse
			.assertOnLearnPathNodeDone(gtaNodeTitle);
	}
	
	

	/**
	 * An author create a course with 2 tests with the exam wizard.
	 * It add a student and a coach and the student pass the first
	 * test.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseExamWizard()
	throws IOException, URISyntaxException {
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("Jeremy");
		UserVO coach = new UserRestClient(deploymentUrl).createRandomUser("Jessica");
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		String qtiTestTitle = "Exam 2.1 " + UUID.randomUUID();
		URL qtiTestUrl = JunitTestHelper.class.getResource("file_resources/qti21/e4_test_qti21.zip");
		File qtiTestFile = new File(qtiTestUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(qtiTestTitle, qtiTestFile);

		//go to authoring
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		// Create course with the exam wizard
		String courseTitle = "Exam " + UUID.randomUUID();
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateFormAndStartWizard(courseTitle, CourseModule.COURSE_TYPE_PATH, Wizard.exam);
		CourseExamWizardPage courseWizard = CourseExamWizardPage.getWizard(browser);
		
		String firstTry = "Exam 2.1 First try";
		String secondTry = "Exam 2.1 Second try";
		// Choose options
		courseWizard
			.setExamConfiguration(true, true, true)
			.setExamMembersConfiguration(true, true)
			.nextInfosMetadata()
			.nextDisclaimers()
			.nextTest()
			.selectTest(qtiTestTitle, firstTry)
			.nextTestConfiguration()
			.disableTestDate()
			.nextTest()
			.selectTest(qtiTestTitle, secondTry)
			.nextTestConfiguration()
			.disableTestDate()
			.nextCertificates()
			//Coaches
			.nextSearchUsers()
			.importMembers(coach)
			.nextUsersOverview()
			.assertOnOverview(coach)
			// Participants
			.nextSearchUsers()
			.importMembers(student)
			.nextUsersOverview()
			.assertOnOverview(student)
			.nextPublication()
			.publish()
			.finish();
		
		RepositorySettingsPage settings = new RepositorySettingsPage(browser);
		settings
			.assertOnInfos();
		settings	
			.back();
		
		// See the course
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		course
			.assertOnCoursePage();
		
		// Assert on the 2 tests
		By elementsBy = By.xpath("//div[contains(@class,'o_tree')]//ul[contains(@class,' o_tree_l1')][count(li)=2]");
		OOGraphene.waitElement(elementsBy, browser);
		course
			.tree()
			.assertWithTitleSelected(firstTry)
			.assertWithTitle(secondTry);
		
		//Student login
		LoginPage studentLoginPage = LoginPage.load(browser, deploymentUrl);
		studentLoginPage
			.loginAs(student)
			.resume();
		
		NavigationPage studentNavBar = NavigationPage.load(browser);
		studentNavBar
			.openMyCourses()
			.select(courseTitle);
		
		// Go to the first test
		CoursePageFragment studentCourse = new CoursePageFragment(browser);
		studentCourse
			.assertOnLearnPathNodeReady(firstTry)
			.assertOnLearnPathNodeNotAccessible(secondTry);
		
		// Pass the test
		QTI21Page.getQTI21Page(browser)
			.passE4()
			.assertOnCourseAssessmentTestScore(4);
		
		// Check the second test is read
		studentCourse
			.assertOnLearnPathNodeDone(firstTry)
			.assertOnLearnPathNodeReady(secondTry);
	}

}
