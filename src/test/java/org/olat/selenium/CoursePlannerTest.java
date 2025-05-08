/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.restapi.CurriculumElementTypeVO;
import org.olat.modules.curriculum.ui.member.ConfirmationMembershipEnum;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.curriculum.CoursePlannerPage;
import org.olat.selenium.page.curriculum.CurriculumComposerPage;
import org.olat.selenium.page.curriculum.CurriculumElementMembersPage;
import org.olat.selenium.page.curriculum.CurriculumElementPage;
import org.olat.selenium.page.curriculum.CurriculumPage;
import org.olat.selenium.page.repository.RepositoryEditDescriptionPage;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.CurriculumRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 8 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class CoursePlannerTest extends Deployments {
	
	private WebDriver browser = getWebDriver(0);
	@ArquillianResource
	private URL deploymentUrl;
	
	/**
	 * A curriculum manager creates a curriculum, an element with a type
	 * single course implementation. It imports a course, publish it, add
	 * the course to the curriculum element. It add a participant to the 
	 * element and set the element status to confirmed.<br>
	 * The participant logs in and select the course in "My courses".
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createCurriculum()
	throws IOException, URISyntaxException {
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Charlie");
		
		UserVO manager = new UserRestClient(deploymentUrl).createCurriculumManager("Sammy");
		String typeName = "Single course impl.";
		CurriculumElementTypeVO type = new CurriculumRestClient(deploymentUrl)
				.createSingleCourseImplementationType(typeName, "C-CUR-1");
		
		LoginPage managerLoginPage = LoginPage.load(browser, deploymentUrl);
		managerLoginPage
			.loginAs(manager.getLogin(), manager.getPassword());
		NavigationPage navBar = NavigationPage.load(browser);

		String courseTitle = "Course-cur " + UUID.randomUUID();
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/curriculum_course.zip");
		File courseFile = new File(courseUrl.toURI());
		
		navBar
			.openAuthoringEnvironment()
			.uploadResource(courseTitle, courseFile);
		// Publish the course
		new RepositoryEditDescriptionPage(browser)
			.clickToolbarBack();
		CoursePageFragment.getCourse(browser)
			.edit()
			.autoPublish()
			.changeStatus(RepositoryEntryStatusEnum.published);
		
		CoursePlannerPage coursePlannerPage = navBar
			.openCoursePlanner();
		
		String id = UUID.randomUUID().toString();
		String curriculumName = "Curriculum 1 " + id;
		String curriculumRef = "CUR-1 " + id;
		CurriculumPage curriculumPage = coursePlannerPage
			.openCurriculumBrowser()
			.addCurriculum(curriculumName, curriculumRef)
			.assertOnCurriculumInTable(curriculumName)
			.openCurriculum(curriculumName);

		String eid = UUID.randomUUID().toString();
		String elementName = "Element of course 1 " + eid;
		String elementIdentifier = "ELC-1 " + eid;
		CurriculumComposerPage curriculumComposer = curriculumPage
			.openImplementationsTab()
			.addCurriculumElement(elementName, elementIdentifier, type.getDisplayName())
			.assertOnCurriculumElementInTable(elementName);
		
		CurriculumElementPage courseElementPage = curriculumComposer
			.selectCurriculumElementInTable(elementName)
			.assertOnImplementationDetails();
		
		// Add a course
		courseElementPage
			.openResourcesTab()
			.assertOnTemplatesList()
			.selectCourse(courseTitle)
			.assertOnCourseInResourcesList(courseTitle);
		
		// Add a member
		CurriculumElementMembersPage elementMembersPage = courseElementPage
			.openMembersTab();
		elementMembersPage
			.addMember()
			.searchMember(participant, true)
			.membership(ConfirmationMembershipEnum.WITHOUT)
			.confirmation(participant)
			.notification();
		elementMembersPage
			.assertOnMemberInList(participant);
		
		courseElementPage
			.changeStatus(CurriculumElementStatus.confirmed);
		
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		NavigationPage.load(browser)
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment participantCourse = CoursePageFragment.getCourse(browser);
		participantCourse
			.assertOnLearnPathNodeDone("Participant list");
	}

}
