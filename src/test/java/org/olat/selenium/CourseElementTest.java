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
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.core.util.DateUtils;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.core.AdministrationPage;
import org.olat.selenium.page.core.ContactPage;
import org.olat.selenium.page.core.ContentViewPage;
import org.olat.selenium.page.core.FolderPage;
import org.olat.selenium.page.core.MenuTreePageFragment;
import org.olat.selenium.page.course.AppointmentPage;
import org.olat.selenium.page.course.AssessmentToolPage;
import org.olat.selenium.page.course.BigBlueButtonPage;
import org.olat.selenium.page.course.CheckListConfigPage;
import org.olat.selenium.page.course.CheckListPage;
import org.olat.selenium.page.course.ContactConfigPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CourseNodeSelectionConfigurationPage;
import org.olat.selenium.page.course.CourseNodeSelectionPage;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.DialogConfigurationPage;
import org.olat.selenium.page.course.DialogPage;
import org.olat.selenium.page.course.DocumentConfigurationPage;
import org.olat.selenium.page.course.DocumentPage;
import org.olat.selenium.page.course.ForumCEPage;
import org.olat.selenium.page.course.InfoMessageCEPage;
import org.olat.selenium.page.course.JupyterHubConfigurationPage;
import org.olat.selenium.page.course.JupyterHubPage;
import org.olat.selenium.page.course.LTIConfigurationPage;
import org.olat.selenium.page.course.LTIPage;
import org.olat.selenium.page.course.MemberListConfigurationPage;
import org.olat.selenium.page.course.MemberListPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PageElementConfigurationPage;
import org.olat.selenium.page.course.PageElementPage;
import org.olat.selenium.page.course.ParticipantFolderPage;
import org.olat.selenium.page.course.PracticeConfigurationPage;
import org.olat.selenium.page.course.PracticePage;
import org.olat.selenium.page.course.ProjectBrokerPage;
import org.olat.selenium.page.course.STConfigurationPage;
import org.olat.selenium.page.course.STConfigurationPage.DisplayType;
import org.olat.selenium.page.course.SinglePage;
import org.olat.selenium.page.course.SinglePageConfigurationPage;
import org.olat.selenium.page.course.TBrokerCoachPage;
import org.olat.selenium.page.course.TBrokerConfigurationPage;
import org.olat.selenium.page.course.TBrokerPage;
import org.olat.selenium.page.course.TUConfigurationPage;
import org.olat.selenium.page.course.TUPage;
import org.olat.selenium.page.course.TeamsPage;
import org.olat.selenium.page.course.VideoConfigurationPage;
import org.olat.selenium.page.course.VideoTaskConfigurationPage;
import org.olat.selenium.page.course.VideoTaskPage;
import org.olat.selenium.page.course.ZoomConfigurationPage;
import org.olat.selenium.page.course.ZoomPage;
import org.olat.selenium.page.forum.ForumPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.repository.AuthoringEnvPage;
import org.olat.selenium.page.repository.AuthoringEnvPage.ResourceType;
import org.olat.selenium.page.repository.FeedPage;
import org.olat.selenium.page.repository.RepositoryEditDescriptionPage;
import org.olat.selenium.page.repository.ScormPage;
import org.olat.selenium.page.repository.UserAccess;
import org.olat.selenium.page.repository.VideoEditorPage;
import org.olat.selenium.page.repository.VideoPage;
import org.olat.selenium.page.survey.EvaluationFormPage;
import org.olat.selenium.page.survey.FormPage;
import org.olat.selenium.page.survey.SurveyEditorPage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.RepositoryRestClient;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 27 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class CourseElementTest extends Deployments {

	private WebDriver browser = getWebDriver(0);
	@ArquillianResource
	private URL deploymentUrl;
	

	/**
	 * Create a course, create a CP, go the the course editor,
	 * create a course element of type CP, select the CP which just created,
	 * close the course editor and check the presence of the CP with the
	 * default title of the first page.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCP()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-CP-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a CP
		String cpTitle = "CP for a course - " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCP(cpTitle)
			.assertOnInfos();
		
		navBar.openCourse(courseTitle);
		
		String cpNodeTitle = "CPNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cp")
			.nodeTitle(cpNodeTitle)
			.selectTabCPContent()
			.chooseCP(cpTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(cpNodeTitle);
		
		//check that the default title of CP (Lorem Ipsum) is visible in the iframe
		By iframe = By.cssSelector("div.o_iframedisplay>iframe");
		OOGraphene.waitElement(iframe, browser);
		OOGraphene.waitingALittleBit();
		WebElement cpIframe = browser.findElement(iframe);
		browser.switchTo().frame(cpIframe);
		OOGraphene.waitElement(By.xpath("//h2[text()='Lorem Ipsum']"), browser);
	}
	

	/**
	 * This test an edge case where a course start automatically its first
	 *  course element, which is a structure node which start itself its first
	 *  element, which is a SCORM which launch itself automatically.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void courseWithSCORM_fullAuto()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		URL zipUrl = JunitTestHelper.class.getResource("file_resources/scorm/SCORM_course_full_auto.zip");
		File zipFile = new File(zipUrl.toURI());
		//go the authoring environment to import our course
		String zipTitle = "SCORM - " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(zipTitle, zipFile);
		
		// publish the course
		new RepositoryEditDescriptionPage(browser)
			.clickToolbarBack();
		CoursePageFragment course = CoursePageFragment.getCourse(browser)
				.edit()
				.autoPublish();
		
		//scorm is auto started -> back
		ScormPage.getScormPage(browser)
			.assertOnCoachingOverview();
		
		// make the author a participant too
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		course
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		
		course
			.changeStatus(RepositoryEntryStatusEnum.published);
		
		String courseUrl = browser.getCurrentUrl();
		if(courseUrl.indexOf("CourseNode") >= 0) {
			courseUrl = courseUrl.substring(0, courseUrl.indexOf("CourseNode"));
		}

		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant log in and go directly to the course with the SCORM
		LoginPage participantLoginPage = LoginPage.load(browser, new URL(courseUrl));		
		
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword(), By.className("o_scorm_content"));
		
		// direct jump in SCORM content
		ScormPage.getScormPage(browser)
			.passVerySimpleScorm()
			.assertOnScormPassed()
			.assertOnScormScore(33);
	}
	
	/**
	 * An author create a video course element, import the video with an URL
	 * and publishes the course. It checks the page.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithVideo()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		//create a course
		String courseTitle = "Course with video " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//Create a course element of type video
		String videoNodeTitle = "Video YT 1.0";
		String videoId = "A49N9C3YvS0";
		String youtubeUrl = "https://youtu.be/" + videoId;
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("video")
			.nodeTitle(videoNodeTitle);
		new VideoConfigurationPage(browser)
			.selectVideoConfiguration()
			.selectVideoUrl("Explanation", youtubeUrl);

		courseEditor
			.autoPublish();
		
		course
			.assertOnLearnPathLastNode(videoNodeTitle);
		
		new VideoPage(browser)
			.assertOnYoutubeVideo(videoId);
	}
	

	/**
	 * An author upload a short video, add 2 segments in the video editor.
	 * It creates a course with a video course element, starts the video
	 * and wait until the first segment appears.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithVideoAndSegments()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		//Upload a video of Big Buck Bunny (https://peach.blender.org)
		URL videoUrl = JunitTestHelper.class.getResource("file_resources/big_buck_bunny.mp4");
		File videoFile = new File(videoUrl.toURI());
		
		String videoTitle = "Big Buck " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.uploadResource(videoTitle, videoFile)
			.clickToolbarRootCrumb();
		
		VideoPage videoPage = new VideoPage(browser);
		VideoEditorPage videoEditorPage = videoPage
			.assertOnVideo()
			.edit()
			.assertOnVideoEditor()
			.waitOnVideo();
		
		videoEditorPage
			.selectSegments()
			.waitOnVideo()
			.addSegment()
			.editSegment("00:00:01", "00:00:15")
			.save()
			.assertOnVideoSegments(1)
			.assertOnVideoSegmentsInTimeline(1)
			.assertOnVideoEditor()
			.addSegment()
			.editSegment("00:00:20", "00:00:45")
			.save()
			.assertOnVideoSegments(2)
			.assertOnVideoSegmentsInTimeline(2)
			.assertOnVideoEditor();
		// Back
		videoEditorPage
			.toolbarBack()
			.assertOnVideo();
		
		String courseTitle = "Video segment - " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//Create a course element of type video
		String videoNodeTitle = "Video segmented 1.0";
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("video")
			.nodeTitle(videoNodeTitle);
		new VideoConfigurationPage(browser)
			.selectVideoConfiguration()
			.selectVideoResource(videoTitle)
			.selectSegmentsOption()
			.save();

		courseEditor
			.autoPublish()
			.assertOnLearnPathLastNode(videoNodeTitle);
		
		new VideoPage(browser)
			.assertOnVideo()
			.play()
			.assetOnSegment()
			.assetOnSegmentTooltip(15);
	}
	
	/**
	 * An author create a video with a segment. After it creates a course with a video task element,
	 * add the video, check the video task use the segments and publishes the course.<br>
	 * A participant looks at the video, click the segment after 2 seconds and the course element must
	 * be green (done).
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithVideoTask()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		//Upload a video
		URL videoUrl = JunitTestHelper.class.getResource("file_resources/big_buck_bunny.mp4");
		File videoFile = new File(videoUrl.toURI());
		
		String videoTitle = "Big Buck " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.uploadResource(videoTitle, videoFile)
			.clickToolbarRootCrumb();
		
		VideoPage videoPage = new VideoPage(browser);
		VideoEditorPage videoEditorPage = videoPage
			.assertOnVideo()
			.edit()
			.assertOnVideoEditor();
		
		videoEditorPage
			.selectSegments()
			.waitOnVideo()
			.addSegment()
			.editSegment("00:00:01", "00:00:45")
			.save();
		// Back
		videoEditorPage
			.toolbarBack()
			.assertOnVideo();
		
		String courseTitle = "Video task - " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//Create a course element of type video
		String videoNodeTitle = "Video task 1.0";
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("videotask")
			.nodeTitle(videoNodeTitle);
		new VideoTaskConfigurationPage(browser)
			.selectVideoTaskConfiguration()
			.selectVideoResource(videoTitle)
			.assertSegmentsOption()
			.save();

		courseEditor
			.publish()
			.quickPublish();
		
		course = courseEditor
			.clickToolbarBack()
			.assertOnLearnPathLastNode(videoNodeTitle);
		new VideoTaskPage(browser)
			.assertOnAssessedIdentities();
		
		//go to members management
		MembersPage members = course
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		// Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		VideoTaskPage videoTask = new VideoTaskPage(browser);
		videoTask
			.assertOnStartTask()
			.startTask()
			.assertOnVideo()
			.play();
		
		// Segment starts after 1 second
		OOGraphene.waitingLong();
		
		videoTask
			.selectFirstSegment()
			.assertOnSegmentCorrect()
			.reduceVideoWindow()
			.submitTask();
		// Return to start
		videoTask
			.assertOnStartTask();
		
		CoursePageFragment participantCourse = new CoursePageFragment(browser);
		participantCourse
			.assertOnLearnPathNodeDone(videoNodeTitle);
	}
	
	
	/**
	 * Create a course, create a wiki, go the the course editor,
	 * create a course element of type wiki, select the wiki which just created,
	 * close the course editor and select the index page of the wiki.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithWiki()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Wiki-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a CP
		String wikiTitle = "Wiki for a course - " + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createWiki(wikiTitle)
			.assertOnInfos();
		
		navBar.openCourse(courseTitle);
		
		String wikiNodeTitle = "WikiNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("wiki")
			.nodeTitle(wikiNodeTitle)
			.selectTabWikiContent()
			.chooseWiki(wikiTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(wikiNodeTitle)
			.selectWithTitle("Index");
		
		//check that the title of the index article/page is visible
		WebElement indexArticleTitle = browser.findElement(By.className("o_wikimod_heading"));
		Assert.assertEquals("Index", indexArticleTitle.getText().trim());
	}
	
	/**
	 * Create a course, create a course element of type wiki. Open
	 * the resource chooser, create a wiki, close the editor, show the 
	 * index page of the wiki.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithWiki_createInCourseEditor()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-Wiki-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String wikiNodeTitle = "WikiNode-1";
		String wikiTitle = "Wiki for a course - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("wiki")
			.nodeTitle(wikiNodeTitle)
			.selectTabWikiContent()
			.createWiki(wikiTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(wikiNodeTitle)
			// the course node select automatically the index page of the wiki
			.selectWithTitle("Index")
			.assertWithTitleSelected("Index");
		
		//check that the title of the index article/page is visible
		WebElement indexArticleTitle = browser.findElement(By.className("o_wikimod_heading"));
		Assert.assertEquals("Index", indexArticleTitle.getText().trim());
	}
	

	@Test
	@RunAsClient
	public void courseWithQTITest()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-QTI-Test-1.2-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String testNodeTitle = "QTITest-1";
		String testTitle = "Test - " + UUID.randomUUID().toString();
		
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("iqtest")
			.nodeTitle(testNodeTitle)
			.selectTabTestContent()
			.createQTITest(testTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the CP
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(testNodeTitle);
		
		//check that the title of the start page of test is correct
		WebElement testH2 = browser.findElement(By.cssSelector("div.o_course_run h2"));
		Assert.assertEquals(testNodeTitle, testH2.getText().trim());
	}
	
	
	/**
	 * An author upload a test with 15 questions, makes a course
	 * with a practice course element and use the questions for the
	 * element, set series and challenges to 1. A student practices,
	 * answers all questions correctly and the element is set to done
	 * in the learn path.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithPractice()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(author.getLogin(), author.getPassword());
		
		//deploy the test
		URL testUrl = ArquillianDeployments.class.getResource("file_resources/qti21/test_15_questions.zip");
		String testTitle = "Test-15 " + UUID.randomUUID();
		new RepositoryRestClient(deploymentUrl, author)
			.deployResource(new File(testUrl.toURI()), "-", testTitle);

		//create a course
		String courseTitle = "Practice-1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type practice with the QTI 2.1 test that we upload above
		String practiceNodeTitle = "Practice-QTI-2.1";
		CoursePageFragment courseRuntime = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = courseRuntime
			.edit()
			.createNode("practice")
			.nodeTitle(practiceNodeTitle);
		
		PracticeConfigurationPage configurationPage = new PracticeConfigurationPage(browser);
		configurationPage
			.selectConfiguration()
			.selectTest(testTitle)
			.setNumberOfSeries(1, 1)
			.saveConfiguration();

		OOGraphene.scrollTop(browser);
		
		//publish the course
		courseEditor
			.autoPublish()
			.changeStatus(RepositoryEntryStatusEnum.published)
			.members()
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//First user go to the course
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		//open the course
		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment userCourse = new CoursePageFragment(browser);
		userCourse
			.tree()
			.assertWithTitleSelected(practiceNodeTitle);
		
		PracticePage practicePage = new PracticePage(browser);
		practicePage
			.assertOnPractice()
			.startShuffled();
		
		for(int i=0; i<10; i++) {
			practicePage
				.answerSingleChoiceWithParagraph("Juste")
				.saveAnswer()
				.assertOnCorrect()
				.nextQuestion();
		}
		
		practicePage
			.assertOnResults(100)
			.backToOverview();

		userCourse
			.assertOnLearnPathNodeDone(practiceNodeTitle);
	}
	
	
	/**
	 * Create a course with a course element of type podcast. Create
	 * a podcast, publish the course, go the the course and configure
	 * the podcast to read an external feed.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithPodcast_externalFeed()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// Admin. add first the domain to the list of allowed domains
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		NavigationPage.load(browser)
			.openAdministration()
			.openMediaServer()
			.addDomain("SRF", "www.srf.ch")
			.assertOnDomain("www.srf.ch");
		
		// Author add the podcast
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		// Create a course
		String courseTitle = "Course-With-Podcast-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String podcastNodeTitle = "PodcatNode-1";
		String podcastTitle = "ThePodcast - " + UUID.randomUUID();
		String podcastUrl = "https://www.srf.ch/feed/podcast/sd/6e633013-c03d-4f49-a1b7-d5b58cfed837.xml";
		
		// Create a course element of type podcast
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("podcast")
			.nodeTitle(podcastNodeTitle)
			.selectTabFeedContent()
			.importExternalUrl(podcastTitle, podcastUrl, "FileResource.PODCAST");

		// Publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		// Open the course and see the podcast
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.assertWithTitleSelected(podcastNodeTitle);
		
		// Check that the title of the podcast is visible
		By podcastElementBy = By.xpath("//div[contains(@class,'o_course_node')]//h2[i[contains(@class,'o_podcast_icon')]]/span");
		OOGraphene.waitElement(podcastElementBy, browser);

		FeedPage.getFeedPage(browser)
			.assertOnPodcastEpisodeInClassicTable();
	}
	
	
	/**
	 * Create a course with a course element of type blog. Create
	 * a blog, publish the course, go the the course and configure
	 * the blog to read the OpenOlat RSS feed.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBlog_externalFeed()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// Admin. add first the domain to the list of allowed domains
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		NavigationPage.load(browser)
			.openAdministration()
			.openMediaServer()
			.addDomain("OpenOlat", "www.openolat.com")
			.assertOnDomain("www.openolat.com");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		// Create a course
		String courseTitle = "Course-With-Blog-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		String blogNodeTitle = "BlogNode-1";
		String blogTitle = "Blog - " + UUID.randomUUID();
		
		// Create a course element of type blog
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("blog")
			.nodeTitle(blogNodeTitle)
			.selectTabFeedContent()
			.importExternalUrl(blogTitle, "https://www.openolat.com/feed/", "FileResource.BLOG");

		// Publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		// Open the course and see the blog
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.assertWithTitleSelected(blogNodeTitle);
		
		OOGraphene.waitElement(By.xpath("//div[contains(@class,'o_course_node')]//h2[i[contains(@class,'o_blog_icon')]]/span"), browser);
		FeedPage.getFeedPage(browser)
			.assertOnBlogEpisodeInClassicTable();
	}

	/**
	 * An author create a course with a blog, open it, add a post. A student
	 * open the course, see the blog post. The author add a new post, the
	 * student must see it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBlog_multipleUsers()
	throws IOException, URISyntaxException {
		WebDriver participantDrone = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course with a blog
		String courseTitle = "Course-Blog-1-" + UUID.randomUUID().toString();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
				
		String blogNodeTitle = "BlogNode-RW-1";
		String blogTitle = "Blog - RW - " + UUID.randomUUID().toString();
				
		//create a course element of type blog with a blog
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("blog")
			.nodeTitle(blogNodeTitle)
			.selectTabFeedContent()
			.createFeed(blogTitle);
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		//open the course and see the blog
		CoursePageFragment course = courseEditor
			.clickToolbarRootCrumb();
		course
			.tree()
			.assertWithTitleSelected(blogNodeTitle);
		
		String postTitle = "BlogPost-RW-1-" + UUID.randomUUID();
		String postSummary = "Some explanations as teaser";
		String postContent = "Content of the post";
		FeedPage feed = FeedPage.getFeedPage(browser);
		feed
			.newBlogPost()
			.fillPostForm(postTitle, postSummary, postContent)
			.publishPost();

		//participant go to the blog
		participantDrone.navigate().to(deploymentUrl);
		LoginPage participantLogin = LoginPage.load(participantDrone, deploymentUrl);
		participantLogin.loginAs(participant.getLogin(), participant.getPassword());
		//search the course in "My courses"
		NavigationPage participantNavigation = NavigationPage.load(participantDrone);
		participantNavigation
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		//Navigate the course to the blog
		CoursePageFragment participantCourse = new CoursePageFragment(participantDrone);
		participantCourse
			.tree()
			.assertWithTitleSelected(blogNodeTitle);
		FeedPage participantFeed = FeedPage.getFeedPage(participantDrone);
		participantFeed
			.assertOnBlogPostInClassicTable(postTitle);
		
		//the author publish a second post in its blog
		String post2Title = "Blog-RW-2-" + UUID.randomUUID();
		String post2Summary = "Some explanations as teaser";
		String post2Content = "Content of the post";
		feed
			.assertOnBlogPostInClassicTable(postTitle)
			.addBlogPost()
			.fillPostForm(post2Title, post2Summary, post2Content)
			.publishPost();
		
		//the participant must see the new post after some click
		participantFeed
			.allTableFilter()
			.assertOnBlogPostInClassicTable(post2Title);
	}
	

	/**
     * Login, create a course, select "Messages Course", insert an info message
     * course element, publish the course, add messages, count if the messages
     * are there, show older messages, count the messages, show current messages,
     * count the messages, edit a message and delete an other, count the messages.
     * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithInfoMessages()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course Msg " + UUID.randomUUID().toString();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		String infoNodeTitle = "Infos - News";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("info")
			.nodeTitle(infoNodeTitle);
		
		//configure the info messages
		InfoMessageCEPage infoMsgConfig = new InfoMessageCEPage(browser);
		infoMsgConfig
			.selectConfiguration()
			.configure(3);
		
		//publish
		editor
			.publish()
			.quickPublish(UserAccess.registred);
		editor.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(infoNodeTitle);
		//set a message
		infoMsgConfig
			.createMessage()
			.setMessage("Information 0", "A very important info", false)
			.next()
			.finish()
			.assertOnMessageTitle("Information 0");
		
		for(int i=1; i<=3; i++) {
			infoMsgConfig.quickMessage("Information " + i, "More informations");
		}
		
		int numOfMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfMessages);
		
		// count old messages
		int numOfOldMessages = infoMsgConfig
				.oldMessages()
				.countMessages();
		Assert.assertEquals(4, numOfOldMessages);
		
		//new messages
		infoMsgConfig.newMessages();
		int numOfNewMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfNewMessages);
		
		//edit
		infoMsgConfig
			.oldMessages();
		infoMsgConfig
			.editMessage("Information 2")
			.setMessage("The latest information", "A very important info", true)
			.next()
			.finish()
			.assertOnMessageTitle("The latest information");

		//delete
		infoMsgConfig
			.deleteMessage("Information 3")
			.confirmDelete();
		
		int numOfSurvivingMessages = infoMsgConfig.countMessages();
		Assert.assertEquals(3, numOfSurvivingMessages);
	}
	
	/**
	 * An author create a course with a dialog course element. It
	 * add a participant to the course, a file to the dialog in
	 * the course element configuration and after publishing the course
	 * in the view of the dialog. It opens the forum of one of the files,
	 * create a new thread.<br>
	 * The participant log in, open the course and the dialog element. It
	 * reads the thread and make a reply. The author answers to the reply.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void courseWithDialog()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course dialog " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//add a participant
		MembersPage members = new CoursePageFragment(browser)
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();

		String dialogNodeTitle = "DialogNode";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("dialog")
			.nodeTitle(dialogNodeTitle);
		
		//upload a file in the configuration
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File imageFile = new File(imageUrl.toURI());
		DialogConfigurationPage dialogConfig = new DialogConfigurationPage(browser);
		dialogConfig
			.selectConfiguration()
			.uploadFile(imageFile);
		
		//publish and go to the course element
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		course
			.tree()
			.assertWithTitleSelected(dialogNodeTitle);
		
		// upload a second file
		URL imageRunUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageRunFile = new File(imageRunUrl.toURI());
		DialogPage dialog = new DialogPage(browser);
		dialog
			.assertOnFile(imageFile.getName())
			.uploadFile(imageRunFile)
			.assertOnFileOverview(imageRunFile.getName())
			.createNewThread("JPEG vs PNG", "Which is the best format");
		
		// The participant come in
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
			
		// The participant find the course
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.assertOnNavigationPage()
			.openMyCourses()
			.select(title);
		// And opens the dialog course element
		CoursePageFragment participantCourse = CoursePageFragment.getCourse(participantBrowser);
		participantCourse
			.tree()
			.assertWithTitleSelected(dialogNodeTitle);
		DialogPage participantDialog = new DialogPage(participantBrowser);
		participantDialog
			.assertOnFile(imageRunFile.getName())
			.openForum(imageRunFile.getName())
			.openThread("JPEG vs PNG")
			.replyToMessage("JPEG vs PNG", "PNG for sure", "Not a loosy format");
		
		//The author reload the messages
		dialog
			.back()
			.openForum(imageRunFile.getName())
			.openThread("JPEG vs PNG")
			.assertMessageBody("Not a loosy format")
			.replyToMessage("PNG for sure", "JPEG smaller", "JPEG is smaller");
		
		//The participant check the reply
		participantDialog
			.back()
			.openForum(imageRunFile.getName())
			.openThread("JPEG vs PNG")
			.assertMessageBody("JPEG is smaller");
	}
	

	/**
	 * An author create a course with a member list course element.
	 * It add two participants and a coach. It publish the course and
	 * check that it sees the authors, coaches and participants.<br>
	 * After that, it edits the course and change the settins to only
	 * show the participants. It checks that only the participants are
	 * visible.<br>
	 * At least, it changes the settings a second time to only show
	 * the course coaches.
	 * 
	 * @param authorLoginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithMemberList()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO coach = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course partilist " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		//add 2 participants
		CoursePageFragment course = new CoursePageFragment(browser);
		MembersPage members = course
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant1, participant2)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		//add a coach
		members
			.addMember()	
			.searchMember(coach, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		String memberListTitle = "MemberList";
		//open course editor
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("cmembers")
			.nodeTitle(memberListTitle);
		//publish
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(memberListTitle);
		
		//check the default configuration with authors, coaches and participants
		MemberListPage memberList = new MemberListPage(browser);
		memberList
			.assertOnOwner(author.getFirstName())
			.assertOnCoach(coach.getFirstName())
			.assertOnParticipant(participant1.getFirstName())
			.assertOnParticipant(participant2.getFirstName());
		
		//the author is not satisfied with the configuration
		editor = course
			.edit()
			.selectNode(memberListTitle);
		MemberListConfigurationPage memberListConfig = new MemberListConfigurationPage(browser);
		memberListConfig
			.selectSettings()
			.setOwners(Boolean.FALSE)
			.setCoaches(Boolean.FALSE)
			.save();
		
		//go check the results
		course = editor
			.autoPublish();
		course
			.tree()
			.assertWithTitleSelected(memberListTitle);
		
		memberList
			.assertOnMembers()
			.assertOnNotOwner(author.getFirstName())
			.assertOnNotCoach(coach.getFirstName())
			.assertOnParticipant(participant1.getFirstName())
			.assertOnParticipant(participant2.getFirstName());
		
		// perhaps only the coaches
		editor = course
			.edit()
			.selectNode(memberListTitle);
		memberListConfig = new MemberListConfigurationPage(browser);
		memberListConfig
				.selectSettings()
				.setCoaches(Boolean.TRUE)
				.setCourseCoachesOnly()
				.setParticipants(Boolean.FALSE)
				.save();
		
		//go check that we see only the coaches results
		course = editor
			.autoPublish();
		course
			.tree()
			.assertWithTitleSelected(memberListTitle);
		
		memberList
			.assertOnMembers()
			.assertOnNotOwner(author.getFirstName())
			.assertOnCoach(coach.getFirstName())
			.assertOnNotParticipant(participant1.getFirstName())
			.assertOnNotParticipant(participant2.getFirstName());
	}
	
	
	/**
	 * An author create a course with a course element
	 * to show the member list. It add coaches and
	 * participants. It navigates to the member list,
	 * switch to the table view and send and an E-mail
	 * to the participants.
	 * 
	 * @param authorLoginPage The login page
	 * @param ryomouBrowser A browser for the student
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithMemberList_sendMail()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");
		UserVO student1 = new UserRestClient(deploymentUrl).createRandomUser("student1");
		UserVO student2 = new UserRestClient(deploymentUrl).createRandomUser("student2");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-with-member-list-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//add coaches as course member
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(ryomou, kanu)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		//add participatns
		members
			.addMember()
			.importList()
			.setMembers(ryomou, student1, student2)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		getSmtpServer().reset();// reset e-mails
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Members 2";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		
		courseEditor
			.createNode("cmembers")
			.nodeTitle(nodeTitle);
		
		MemberListConfigurationPage memberListConfig = new MemberListConfigurationPage(browser);
		memberListConfig
				.selectSettings()
				.setCoaches(Boolean.TRUE)
				.setCourseCoachesOnly()
				.setParticipants(Boolean.TRUE)
				.save();
		
		courseEditor
			.selectRoot();
		
		STConfigurationPage stConfig = new STConfigurationPage(browser);
		stConfig
			.selectOverview()
			.setDisplay(DisplayType.peekview);
		
		 courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		 courseRuntime = courseEditor
			.clickToolbarBack();
		MenuTreePageFragment menuTree = courseRuntime
			.tree();

		MemberListPage memberList = new MemberListPage(browser);
		// check peek view
		memberList
			.assertOnPeekview(1, 1)
			.assertOnPeekview(2, 2)
			.assertOnPeekview(3, 2);
		
		menuTree
			.selectWithTitle(nodeTitle);
		
		memberList
			.assertOnMembers()
			.assertOnOwner(author.getFirstName())
			.assertOnCoach(kanu.getFirstName())
			.assertOnCoach(ryomou.getFirstName())
			.assertOnParticipant(student1.getFirstName())
			.assertOnParticipant(student2.getFirstName())
			.assertOnNotParticipant(ryomou.getFirstName());
		
		// switch to the table view
		memberList
			.switchToTableView()
			.assertOnTableOwner(author.getFirstName())
			.assertOnTableCoach(kanu.getFirstName())
			.assertOnTableCoach(ryomou.getFirstName())
			.assertOnTableParticipant(student1.getFirstName())
			.assertOnTableParticipant(student2.getFirstName())
			.assertOnTableNotParticipant(ryomou.getFirstName());
		
		// send mail
		memberList
			.emailAll()
			.contactAllParticipants()
			.contactExternal("openolat@frentix.com")
			.contactSubject("Hello my friends")
			.send();
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
	}
	
	
	/**
	 * An author create a course with a participant folder course
	 * element. It add a participant to the course and upload file
	 * in the return box of this participant.<br>
	 * The participant come in and open the course, see the file
	 * uploaded by the author in its return box and it uploads an
	 * image in its drop box. The author go the see the image.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithParticipantFolder()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String title = "Course partilist " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(title, false)
			.assertOnInfos()
			.clickToolbarBack();
		
		String participantFolderTitle = "ParticipantList";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(title)
			.edit()
			.createNode("pf")
			.nodeTitle(participantFolderTitle);
		//publish
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		
		//add a participant
		MembersPage members = new CoursePageFragment(browser)
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		//go to the course element
		course
			.tree()
			.selectWithTitle(participantFolderTitle);
		
		// open the return box of the participant and upload a file
		URL coachImageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File coachImageFile = new File(coachImageUrl.toURI());
		ParticipantFolderPage folder = new ParticipantFolderPage(browser);
		folder
			.assertOnParticipantsList()
			.assertOnParticipant(participant.getFirstName())
			.openParticipantFolder(participant.getFirstName());
		FolderPage directory = folder
			.openReturnBox()
			.assertOnEmptyFolderCard()
			.quickUploadFile(coachImageFile)
			.assertOnFileCard(coachImageFile.getName());
		
		// The participant come in
		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());
		
		// The participant find the course
		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.assertOnNavigationPage()
			.openMyCourses()
			.select(title);
		// And opens the participant folder
		CoursePageFragment participantCourse = CoursePageFragment.getCourse(participantBrowser);
		participantCourse
			.tree()
			.assertWithTitleSelected(participantFolderTitle);
		
		ParticipantFolderPage participantFolder = new ParticipantFolderPage(participantBrowser);
		participantFolder
			.openReturnBox()
			.assertOnFileCard(coachImageFile.getName())
			.selectRootDirectory();
		// Participant upload a file in its drop box
		URL participantImageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File participantImageFile = new File(participantImageUrl.toURI());
		participantFolder
			.openDropBox()
			.assertOnEmptyFolderCard()
			.quickUploadFile(participantImageFile)
			.assertOnFileCard(participantImageFile.getName());
		
		//Author check the image in the participant drop box
		directory
			.selectRootDirectory();
		folder.openDropBox()
			.assertOnFileCard(participantImageFile.getName());
	}
	

	/**
	 * An author creates a course with a document course element,
	 * upload a PDF in the editor, publish the course and go to
	 * the course element check if the document is there.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithDocument()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "DocCourse" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Document";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("document")
			.nodeTitle(nodeTitle);
		
		URL pdfDocumentUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfDocumentFile = new File(pdfDocumentUrl.toURI());
		
		DocumentConfigurationPage docConfig = new DocumentConfigurationPage(browser);
		docConfig
			.selectConfiguration()
			.uploadDocument(pdfDocumentFile);
		
		courseEditor
			.autoPublish();
		
		DocumentPage doc = new DocumentPage(browser);
		doc.assertPdfJs();
	}
	

	/**
	 * An author upload a PDF document as learn resource, after that she creates
	 * a course with a document course element, selects the PDF in the editor,
	 * publish the course and go to the course element see if the document is
	 * there.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithDocumentFromRepository()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		URL pdfDocumentUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfDocumentFile = new File(pdfDocumentUrl.toURI());
		
		//create a course
		String docTitle = "PDF-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(docTitle, pdfDocumentFile);

		//create a course
		String courseTitle = "PDFDoc-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "PDF";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("document")
			.nodeTitle(nodeTitle);
		
		DocumentConfigurationPage docConfig = new DocumentConfigurationPage(browser);
		docConfig
			.selectConfiguration()
			.selectDocument(docTitle);
		
		courseEditor
			.autoPublish();
		
		DocumentPage doc = new DocumentPage(browser);
		doc.assertPdfJs();
	}


	/**
	 * An author creates a course with a forum, publish it, open a new thread.
	 * A first user come to see the thread. A second come via the peekview.
	 * The three make a reply at the same time. And they check that they see
	 * the replies, and the ones of the others.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param reiBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithForum_concurrent()
	throws IOException, URISyntaxException {
		WebDriver kanuBrowser = getWebDriver(1);
		WebDriver reiBrowser = getWebDriver(2);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course FO " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
	
		//go the authoring environment to create a forum
		String foTitle = "FO - " + UUID.randomUUID();
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		
		// create forum
		courseEditor
			.createNode("fo")
			.nodeTitle(foTitle);
		
		// setup peekview
		courseEditor
			.selectRoot();
		STConfigurationPage stConfig = new STConfigurationPage(browser);
		stConfig
			.selectOverview()
			.setDisplay(DisplayType.peekview);
		
		// publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
	
		MembersPage membersPage = courseEditor		
			.clickToolbarBack()
			.members();
	
		membersPage
			.addMember()
			.importList()
			.setMembers(kanu, rei)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();

		String shortFoTitle = foTitle.substring(0, 20);
		//go to the forum
		courseEditor
			.clickToolbarBack()
			.tree()
			.selectWithTitle(shortFoTitle)
			.assertWithTitleSelected(shortFoTitle);
		
		ForumPage authorForum = ForumPage
			.getCourseForumPage(browser);
		authorForum
			.createThread("The best anime ever", "What is the best anime ever?", null);
		
		//First user go to the course
		LoginPage kanuLoginPage = LoginPage.load(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword());

		NavigationPage kanuNavBar = NavigationPage.load(kanuBrowser);
		kanuNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		//go to the forum
		new CoursePageFragment(kanuBrowser)
			.tree()
			.selectWithTitle(shortFoTitle)
			.assertWithTitleSelected(shortFoTitle);
		
		ForumPage kanuForum = ForumPage
			.getCourseForumPage(kanuBrowser)
			.openThread("The best anime ever");

		//First user go to the course
		LoginPage reiLoginPage = LoginPage.load(reiBrowser, deploymentUrl);
		reiLoginPage
			.loginAs(rei);

		NavigationPage reiNavBar = NavigationPage.load(reiBrowser);
		reiNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		//select the thread in peekview
		ForumPage reiForum = new ForumPage(reiBrowser)
			.openThreadInPeekview("The best anime ever");
		
		//concurrent reply
		String kanuReply = "Ikki Touzen";
		String reiReply = "Neon Genesis Evangelion";
		String authorReply = "Lain, serial experiment";
		
		authorForum
			.replyToMessageNoWait("The best anime ever", null, authorReply);
		reiForum
			.replyToMessageNoWait("The best anime ever", null, reiReply);
		kanuForum
			.replyToMessageNoWait("The best anime ever", null, kanuReply);
	
		//wait the responses
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitModalDialogDisappears(kanuBrowser);
		OOGraphene.waitModalDialogDisappears(reiBrowser);
		
		//check own responses
		authorForum.assertMessageBody(authorReply);
		kanuForum.assertMessageBody(kanuReply);
		reiForum.assertMessageBody(reiReply);

		//check others responses
		authorForum
			.flatView()
			.waitMessageBody(kanuReply);
		reiForum
			.flatView()
			.waitMessageBody(kanuReply);
		kanuForum
			.flatView()
			.waitMessageBody(reiReply);
	}
	

	/**
	 * An administrator create a category in catalog. It creates a new course
	 * with a forum open to guests. it publish the course in the
	 * catalog.<br>
	 * The guest find the course, create a new thread. The administrator reply
	 * to the message, the guest to its reply.<br>
	 * The administrator checks the last message in its new messages, click
	 * back, use the list of users to see the messages of the guest. It clicks
	 * back to the threads list and checks the thread has 3 messages.
	 * 
	 * @param loginPage
	 * @param guestBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithForumGuestAccess()
	throws IOException, URISyntaxException {
		WebDriver guestBrowser = getWebDriver(1);
		
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		//create a course
		String courseTitle = "Guest FO " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//go the authoring environment to create a forum
		String foTitle = "GFO - " + UUID.randomUUID();
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("fo")
			.nodeTitle(foTitle);
		//configure for guest
		ForumCEPage forumConfig = new ForumCEPage(browser);
		forumConfig
			.selectConfiguration()
			.allowGuest();
		
		//publish the course
		courseEditor
			.publish()
			.nextSelectNodes()
			.selectAccess(UserAccess.guest)
			.nextAccess()
			.finish();
		//back in course
		CoursePageFragment course = courseEditor.clickToolbarBack();
		course
			.settings()
			.accessConfiguration()
			.editAccessForGuest("It's free", true)
			.clickToolbarBack();

		// Guest go to the catalog and find the course
		LoginPage guestLogin = LoginPage.load(guestBrowser, deploymentUrl);
		guestLogin
			.asCatalog()
			.exploreOffers()
			.visitCourse(courseTitle);
		
		//go to the forum
		new CoursePageFragment(guestBrowser)
			.tree()
			.assertWithTitleSelected(foTitle.substring(0, 20));
		
		String guestAlias = "Guest-" + UUID.randomUUID();
		ForumPage guestForum = ForumPage
			.getCourseForumPage(guestBrowser)
			.createThread("Your favorite author", "Name your favorite author", guestAlias);
	
		// admin go to the forum
		new CoursePageFragment(browser)
			.tree()
			.selectWithTitle(foTitle.substring(0, 20));
		//admin reply to the thread of guest
		ForumPage adminForum = ForumPage
			.getCourseForumPage(browser)
			.openThread("Your favorite author")
			.assertOnGuestPseudonym(guestAlias)
			.newMessages()
			.assertOnGuestPseudonym(guestAlias)
			.replyToMessage("Your favorite author", "Huxley is my favorite author", "My favorite author is Huxley");
		
		//guest refresh the view and reply to admin
		guestForum
			.flatView()
			.assertMessageBody("My favorite author is Huxley")
			.replyToMessage("Huxley is my favorite author", " I prefer Orwell", "Orwell is my favorite author");

		//admin see its new messages, see the list of users, select the guest and its messages
		OOGraphene.waitingALittleLonger();//JMS message need to be delivered
		adminForum
			.newMessages()
			.assertMessageBody("Orwell is my favorite author")
			.clickBack()
			.userFilter()
			.selectFilteredUser(guestAlias)
			.assertMessageBody("Orwell is my favorite author")
			.clickBack()
			.clickBack()
			.assertThreadListOnNumber("Your favorite author", 3);
	}
	

	/**
	 * An author setup a course with a LTI course element with score enabled.
	 * A participant take the course and see the LTI content. The back channel
	 * need the url of the OpenOLAT instance which is currently difficult
	 * for a selenium test. The grading is not tested until a LTI server
	 * can be installed on localhost.
	 * 
	 * @param authorLoginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithLTI()
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");

		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-LTI-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Test with the test that we create above
		String ltiTitle = "LTI-Node";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit()
			.createNode("lti")
			.nodeTitle(ltiTitle);
		
		//configure assessment
		LTIConfigurationPage ltiConfig = new LTIConfigurationPage(browser);
		ltiConfig
			.selectConfiguration()
			.setLtiPage("http://lti.frentix.com/tool.php", "123456", "secret")
			.enableScore(10.0d, 5.0d)
			.save();
		//set the score / passed calculation in root node and publish
		courseEditor
			.selectRoot()
			.selectTabScore()
			.enableRootScoreByNodes()
			.autoPublish()
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser();
		
		//go to members management
		CoursePageFragment courseRuntime = courseEditor.clickToolbarBack();
		courseRuntime
			.publish();
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//Participant login
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle);
		
		CoursePageFragment participantCourse = new CoursePageFragment(browser);
		participantCourse
			.tree()
			.assertWithTitleSelected(ltiTitle);
		LTIPage lti = new LTIPage(browser);
		lti
			.start()
			.outcomeToolProvider();
			//.sendGrade(0.8d);
	}
	
	/**
	 * An author create a course with a course element
	 * to contact all members of the course. It add some
	 * participants. A participant log in, go to the
	 * course to use the contact form and send an E-mail.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithContact()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("ryomou");
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("student");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Contact Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//add participants
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(ryomou, student)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		getSmtpServer().reset();// reset e-mails
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "ContactNode";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("co")
			.nodeTitle(nodeTitle);
		
		ContactConfigPage contactConfig = new ContactConfigPage(browser);
		contactConfig
				.selectConfiguration()
				.wantAllOwners()
				.wantAllCoaches()
				.wantAllParticipants()
				.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant comes in
		loginPage.loginAs(ryomou.getLogin(), ryomou.getPassword());


		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);

		ContactPage contactPage = new ContactPage(browser);
		// check peek view
		contactPage
			.setContent("Hello", "Hello, are you fine?")
			.send()
			.assertSend();
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
	}
	
	/**
	 * An author creates a learn path course with a course node selection
	 * course element and two folders elements. Published the course and add
	 * a participant. The participant log in, opens the course and choose a folder
	 * to see. The author checks after what the participant has done.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCourseNodeSelection()
	throws IOException, URISyntaxException {	
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course CNS " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		// Create a course element of type "Course node selection"
		String nodeTitle = "Choose folders";
		String folderOneTitle = "Folder one";
		String folderTwoTitle = "Folder two";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("cns")
			.nodeTitle(nodeTitle);
		
		CourseNodeSelectionConfigurationPage cnsConfigurationPage = new CourseNodeSelectionConfigurationPage(browser);
		cnsConfigurationPage
			.selectConfiguration()
			.setNumOfSelections(1)
			.saveConfiguration();
		
		courseEditor
			.createNode("bc")
			.nodeTitle(folderOneTitle);
		courseEditor
			.createNode("bc")
			.nodeTitle(folderTwoTitle);
		
		courseEditor
			.moveUnder(nodeTitle);
		courseEditor
			.selectNode(folderOneTitle)
			.moveUnder(nodeTitle);
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		courseRuntime
			.settings()
			.accessConfiguration()
			.setAccessToRegisteredUser()
			.clickToolbarBack();
		courseRuntime
			.changeStatus(RepositoryEntryStatusEnum.published);
		
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();
		
		// Author log out
		new UserToolsPage(browser)
			.logout();
				
		// Participant comes in and choose the folder two
		LoginPage.load(browser, deploymentUrl)
			.loginAs(participant);
		
		NavigationPage ryomouNavBar = NavigationPage.load(browser);
		ryomouNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(browser);
		MenuTreePageFragment menuTree = course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		CourseNodeSelectionPage courseNodeSelectionPage = new CourseNodeSelectionPage(browser);
		courseNodeSelectionPage
			.assertOnCourseNodeSelection()
			.selectCourseNode(folderTwoTitle);
		
		menuTree
			.selectWithTitle(folderTwoTitle);
		course
			.assertOnTitle(folderTwoTitle)
			.assertOnLearnPathNodeDone(nodeTitle)
			.assertOnLearnPathNodeDone(folderTwoTitle);
		
		// Participant log out
		new UserToolsPage(browser)
			.logout();
				
		// Author comes in and do the self test
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author)
			.resume();
		
		new CoursePageFragment(browser)
			.tree()
			.assertWithTitleSelected(nodeTitle);

		// Open details and check that the participant chooses node two
		new CourseNodeSelectionPage(browser)
			.assertOnCourseNodeSelectedBy(participant)
			.openDetails(participant)
			.assertOnDetailsSelectedNode(participant, folderTwoTitle);
	}
	
	/**
	 * An author creates a course with a single page course element,
	 * create the HTML page with the default button, publish the
	 * course and go to the page, and edit it.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithSinglePage()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Single Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "SinglePage";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(nodeTitle);

		String content = "A new single page with some content";
		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.createEditPage("sp.html", content)
			.assertOnPreview();
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		
		courseRuntime
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		SinglePage singlePage = new SinglePage(browser);
		singlePage
			.assertInPage(content);
		
		String newContent = "Newer content in a single page for you";
		singlePage
			.edit(newContent)
			.assertInPage(newContent);
	}
	
	
	/**
	 * An author creates a course with a single page course element,
	 * upload a PDF, publish the course and go to the page to check
	 * if the file is available.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithSinglePageWithPDF()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Single PDF Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "SinglePDF";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(nodeTitle);
		
		URL pdfUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File pdfFile = new File(pdfUrl.toURI());

		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.uploadFile(pdfFile)
			.assertOnPreview();
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		
		courseRuntime
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		SinglePage singlePage = new SinglePage(browser);
		singlePage
			.assertInFile("handInTopic1.pdf");	
	}
	
	/**
	 * An author creates a course with a single page course element,
	 * create the HTML page in the course editor with the default button,
	 * add an image and publish the course and go to the page to verify
	 * if the content and the image are there.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseEditSinglePage()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Single Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "EditableSinglePage";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("sp")
			.nodeTitle(nodeTitle);

		String content = "A new single page with some content";
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageFile = new File(imageUrl.toURI());
		
		SinglePageConfigurationPage spConfiguration = new SinglePageConfigurationPage(browser);
		spConfiguration
			.selectConfiguration()
			.createEditPage("nsp.html", "")
			.editPage()
			.setContent(content)
			.uploadImage(imageFile)
			.saveContent();
		
		spConfiguration
			.assertOnPreview();
		
		CoursePageFragment courseRuntime = courseEditor
			.autoPublish();
		
		courseRuntime
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		SinglePage singlePage = new SinglePage(browser);
		singlePage
			.assertInPage(content)
			.assertImageInPage(imageFile.getName());
	}
	

	/**
	 * An author create a course with a page element. It configures
	 * the course node to allow coaches to edit the page. It add a coach
	 * to the course which add a title to the page.
	 * 
	 * @param loginPage
	 */
	@Test
	@RunAsClient
	public void courseWithPage()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO coach = new UserRestClient(deploymentUrl).createRandomUser("John");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//go to authoring
		NavigationPage navBar = NavigationPage.load(browser);
		AuthoringEnvPage authoringEnv = navBar
			.assertOnNavigationPage()
			.openAuthoringEnvironment();
		
		String courseTitle = "Page " + UUID.randomUUID();
		//create course
		authoringEnv
			.openCreateDropDown()
			.clickCreate(ResourceType.course)
			.fillCreateCourseForm(courseTitle, true)
			.assertOnInfos()
			.clickToolbarBack();
		
		//add a participant
		MembersPage members = new CoursePageFragment(browser)
			.members();
		members
			.addMember()
			.searchMember(coach, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, true, false)
			.nextPermissions()
			.finish();
		members
			.clickToolbarBack();

		String pageNodeTitle = "Page";
		//open course editor
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment editor = course
			.assertOnCoursePage()
			.assertOnTitle(courseTitle)
			.edit()
			.createNode("cepage")
			.nodeTitle(pageNodeTitle);
		
		new PageElementConfigurationPage(browser)
			.selectConfiguration()
			.enableCoachEditing();
		
		//publish and go to the course element
		editor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		editor
			.clickToolbarBack();
		course
			.tree()
			.assertWithTitleSelected(pageNodeTitle);
		
		//Coach login
		LoginPage coachLoginPage = LoginPage.load(browser, deploymentUrl);
		coachLoginPage
			.loginAs(coach.getLogin(), coach.getPassword());
		
		NavigationPage coachNavBar = NavigationPage.load(browser);
		coachNavBar
			.openCoaching()
			.openCourses()
			.filterAllCourses()
			.openCourse(courseTitle);

		// Go to the course element and check the 
		CoursePageFragment coachCourse = new CoursePageFragment(browser);
		coachCourse
			.tree()
			.assertWithTitleSelected(pageNodeTitle);

		String title = "My title " + UUID.randomUUID();
		
		PageElementPage page = new PageElementPage(browser)
			.assertOnPageElement();
			
		page.openEditor()
			.addLayout(ContainerLayout.block_1_1lcols)
			.openElementsChooser(1, 1)
			.addTitle(title)
			.setTitleSize(4, false)
			.closeEditFragmentOfPage()
			.assertOnTitle(title, 4);
		
		page.closeEditor();
		
		new ContentViewPage(browser)
			.assertOnTitle(title, 4);
	}
	
	
	/**
	 * An author creates a survey with a multiple choice
	 * and a single choice. He uses it in a course. A
	 * participant of the course participates to the
	 * survey.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithSurvey()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Maximilien");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a survey
		String surveyTitle = "Survey-1-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createSurvey(surveyTitle)
			.assertOnInfos()
			.clickToolbarBack();
		
		EvaluationFormPage survey = EvaluationFormPage
			.loadPage(browser);
		SurveyEditorPage surveyEditor = survey
			.edit();
		surveyEditor
			.addLayout(ContainerLayout.block_3rows)
			.openElementsChooser(1, 1)
			.addTitle("My survey")
			.setTitleSize(2, true)
			.closeEditFragmentOfResource()
			.assertOnTitle("My survey", 2);
		
		surveyEditor
			.openElementsChooser(1, 2)
			.addMultipleChoiceElement()
			.addMultipleChoice("Jupiter", 2)
			.addMultipleChoice("Saturn", 3)
			.closeEditFragmentOfResource();
		
		surveyEditor
			.openElementsChooser(1, 3)
			.addSingleChoiceElement()
			.addSingleChoice("Mercury", 2)
			.addSingleChoice("Venus", 3)
			.scrollTop()
			.closeEditFragmentOfResource();
		
		surveyEditor
			.close();

		//create a course
		String courseTitle = "Course-With-Survey-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		navBar.openCourse(courseTitle);
		
		String surveyNodeTitle = "SurveyNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("survey")
			.nodeTitle(surveyNodeTitle)
			.selectTabSurveyContent()
			.chooseSurvey(surveyTitle);

		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
			
		membersPage
			.addMember()
			.importList()
			.setMembers(user)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//open the course and see the survey
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(surveyNodeTitle);
		
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage
			.loginAs(user.getLogin(), user.getPassword());
		
		//open the course
		NavigationPage userNavBar = NavigationPage.load(browser);
		userNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment userCourse = new CoursePageFragment(browser);
		userCourse
			.tree()
			.assertWithTitleSelected(surveyNodeTitle);
		
		EvaluationFormPage userSurvey = EvaluationFormPage.loadPage(browser)
			.assertOnExecution();
		
		userSurvey
			.answerMultipleChoice("Saturn")
			.answerSingleChoice("Venus")
			.saveAndClose()
			.assertOnSurveyClosed();
	}
	
	
	/**
	 * An author upload a form with 2 rubrics. He uses it in a
	 * course. A participant of the course fills the form and
	 * the author checks the results.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithForm()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("Maximilien");
		LoginPage authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a survey
		//upload a SCORM package
		String formTitle = "Form - " + UUID.randomUUID();
		URL formUrl = JunitTestHelper.class.getResource("file_resources/form_with_rubrics_planets.zip");
		File formFile = new File(formUrl.toURI());
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.uploadResource(formTitle, formFile);

		//create a course
		String courseTitle = "Course-With-Form-" + UUID.randomUUID().toString();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		navBar.openCourse(courseTitle);
		
		String formNodeTitle = "FormNode-1";
		//create a course element of type CP with the CP that we create above
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("form")
			.nodeTitle(formNodeTitle)
			.selectTabFormContent()
			.chooseForm(formTitle)
			.saveConfiguration();

		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		
		MembersPage membersPage = courseEditor
			.clickToolbarBack()
			.members();
			
		membersPage
			.addMember()
			.importList()
			.setMembers(user)
			.nextUsers()
			.nextOverview()
			.nextPermissions()
			.finish();
		
		//open the course and see the survey
		CoursePageFragment course = courseEditor
			.clickToolbarBack();
		course
			.tree()
			.selectWithTitle(formNodeTitle);
		
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage
			.loginAs(user.getLogin(), user.getPassword());
		
		//open the course
		NavigationPage userNavBar = NavigationPage.load(browser);
		userNavBar
			.openMyCourses()
			.select(courseTitle);
		
		//go to the group task
		CoursePageFragment userCourse = new CoursePageFragment(browser);
		userCourse
			.tree()
			.selectWithTitle(formNodeTitle);
		
		EvaluationFormPage userSurvey = EvaluationFormPage.loadPage(browser)
			.assertOnExecution();
		
		userSurvey
			//Planets
			.answerRubric("Venus", 2)
			.answerRubric("Earth", 3)
			.answerRubric("Saturn", 4)
			.answerRubric("Neptun", 4)
			.answerRubric("Pluto", 5)
			// Asteroids
			.answerRubric("Ceres", 4)
			.answerRubric("Juno", 2)
			.answerRubric("Pallas", 4)
			.answerRubric("Kabudari", 5)
			.saveAndClose()
			.assertOnFormClosed();
		
		// Author is back
		authorLoginPage = LoginPage.load(browser, deploymentUrl);
		authorLoginPage
			.loginAs(author.getLogin(), author.getPassword())
			.assertOnResume()
			.resume();
		
		NavigationPage.load(browser)
			.openAuthoringEnvironment()
			.openResource(courseTitle);
		
		new FormPage(browser)
			.assertOnParticipantsList()
			.selectParticipant(user.getFirstName())
			.assertOnFormClosed()
			.assertAnsweredRubric("Venus", 2, true)
			.assertAnsweredRubric("Pluto", 5, true)
			.assertAnsweredRubric("Pallas", 4, true)
			.assertAnsweredRubric("Kabudari", 5, true);
	}
	
	
	/**
	 * An author creates a course with a course element of type BigBlueButton,
	 * add a meeting in the edit list, go the meetings list and goes to the page
	 * dedicated to join the meeting.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBigBlueButton()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-BBB-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "BBB Node-1";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("bigbluebutton")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		String meetingName = "Quick meeting";
		BigBlueButtonPage bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addSingleMeeting(meetingName, "Classroom")
			.assertOnList(meetingName)
			.selectMeetingsList()
			.assertOnList(meetingName)
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName)
			.assertOnJoin();
	}
	

	/**
	 * First an administrator enables a template with external URL.<br>
	 * Than an author creates a course with a course element of type BigBlueButton,
	 * add a meeting in the edit list, go the meetings list and select the meeting
	 * where she picks the external URL. Log out, navigate to the external
	 * URL, check the login button and use it.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBigBlueButtonGuestLogin()
	throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// open meeting for guest
		LoginPage adminLoginPage = LoginPage.load(browser, deploymentUrl);
		adminLoginPage
			.loginAs(administrator)
			.resume();
		
		NavigationPage.load(browser)
			.openAdministration()
			.openBigBlueButtonSettings()
			.selectTemplates()
			.editTemplate("Interview")
			.enableTemplate()
			.enableGuestLink()
			.save();

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-BBB-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "BBB Guest-2";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("bigbluebutton")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		String meetingName = "Quick meeting";
		BigBlueButtonPage bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addSingleMeeting()
			.editMeeting(meetingName, "Interview")
			.assertEditMeetingExternalUrl()
			.saveMeeting()
			.assertOnList(meetingName)
			.selectMeetingsList()
			.assertOnList(meetingName)
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName);
		
		String externalUrl = bigBlueButton.getExternalUrl();
		
		// log out
		new UserToolsPage(browser)
			.logout();
		// return to login
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage();
		
		browser.navigate().to(externalUrl);
		
		bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnWaitGuestMeeting()
			.loginToGuestJoin(author)
			.assertOnGuestJoinMeetingActive();
	}
	
	
	/**
	 * An author creates a course with a course element of type BigBlueButton,
	 * add 4 meetings planned for next month in the edit list. Than it goes the meetings list
	 * and goes to the page dedicated to join the meeting.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithBigBlueButtonMultipleMeetings()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Course-With-BBB-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "BBB Node-2";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("bigbluebutton")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.autoPublish();
		
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		String meetingName = "Recurring meeting";
		BigBlueButtonPage bigBlueButton = new BigBlueButtonPage(browser);
		bigBlueButton
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addMultipleDailyMeetings(meetingName, 5, 10, "Classroom")
			.nextToDatesList()
			.assertOnDatesList(3, 5)
			.finishRecurringMeetings()
			.assertOnList(meetingName, 3, 5)
			.selectMeetingsList()
			.selectMeeting(meetingName, 1)
			.assertOnMeeting(meetingName);
	}
	
	
	/**
	 * An author creates a course with a course element of type Microsoft Teams,
	 * add a meeting in the edit list, go the meetings list and goes to the page
	 * dedicated to the meeting. The selenium works without teams to be configured
	 * and wait for the configuration errors.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTeams()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Teams-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "Teams-1";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("msteams")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
		courseEditor
			.clickToolbarBack();
		
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		String meetingName = "Teams meeting";
		TeamsPage teams = new TeamsPage(browser);
		teams
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addSingleMeeting(meetingName)
			.assertOnList(meetingName)
			.selectMeetingsList()
			.assertOnList(meetingName)
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName);
		
		teams
			.assertOnJoinDisabled();
	}
	

	/**
	 * An author creates a course with a course element of type Teams, add a
	 * serie of weekly meetings in the edit list, then goes in the meetings list
	 * and goes to the page dedicated to join the meeting.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTeamsWeeklyMeetings()
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Teams-2-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		String nodeTitle = "Teams-2";
		//create a course element of type CP with the CP that we create above
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("msteams")
			.nodeTitle(nodeTitle);
		
		//publish the course
		courseEditor
			.autoPublish();
		
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		String meetingName = "Teams meeting";
		TeamsPage teams = new TeamsPage(browser);
		teams
			.assertOnRuntime()
			.selectEditMeetingsList()
			.addMultipleWeeklyMeetings(meetingName)
			.nextToDatesList()
			.assertOnDatesList(5)
			.finishRecurringMeetings()
			.assertOnList(meetingName, 4)
			.selectMeetingsList()
			.selectMeeting(meetingName)
			.assertOnMeeting(meetingName);
		
		teams
			.assertOnJoinDisabled();
	}
	
	
	/**
	 * An author creates a topic broker course element with a topic. Set the dates
	 * in the past and the future. It publishes the course, add two participants,
	 * the participants choose the topic, one normally, one with the highest priority.
	 * The author come back, set both dates in the past and run the enrollment. It checks
	 * the enrollment of both participants is successful. A participant checks
	 * that it's enrolled successfully too.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTopicBroker()
	throws IOException, URISyntaxException {
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("Cel");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Val");
			
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		// Create a course
		String courseTitle = "Course with topic broker " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		// Create a course element of type topic broker
		String brokerNodeTitle = "Topic broker 1.0";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("topicbroker")
			.nodeTitle(brokerNodeTitle);
		
		Date now = new Date();
		Date start = DateUtils.addDays(now, -4);
		Date end = DateUtils.addDays(now, 2);
		String topicTitle = "Selenium testing";
		String topicIdentifier = "SEL-100c";
		
		TBrokerConfigurationPage tbConfig = new TBrokerConfigurationPage(browser);
		tbConfig
			.selectConfiguration()
			.selectPeriod(start, end)
			.saveConfiguration()
			.selectConfigurationTopics()
			.addTopic(topicIdentifier, topicTitle, 1, 5);
		
		courseEditor
			.publish()
			.quickPublish();
		
		// Open the course
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.tree()
			.assertWithTitleSelected(brokerNodeTitle);
		
		// Add participants
		courseRuntime
			.members()
			.quickImport(participant1, participant2)
			.clickToolbarBack();
		
		// First participant select a topic
		LoginPage participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant1.getLogin(), participant1.getPassword());
		
		NavigationPage participant1NavBar = NavigationPage.load(browser);
		participant1NavBar
			.openMyCourses()
			.select(courseTitle);
		
		new CoursePageFragment(browser)
			.tree()
			.assertWithTitleSelected(brokerNodeTitle);
		
		new TBrokerPage(browser)
			.assertOnTopicTitle(topicTitle)
			.selectTopic(topicTitle)
			.assertOnSelectedTopicAsCard(topicTitle);
		
		// Second participant select topic with high priority
		LoginPage participant2LoginPage = LoginPage.load(browser, deploymentUrl);
		participant2LoginPage
			.loginAs(participant2.getLogin(), participant2.getPassword());
		
		NavigationPage participant2NavBar = NavigationPage.load(browser);
		participant2NavBar
			.openMyCourses()
			.select(courseTitle);
		
		new CoursePageFragment(browser)
			.tree()
			.assertWithTitleSelected(brokerNodeTitle);
		
		new TBrokerPage(browser)
			.assertOnTopicTitle(topicTitle)
			.selectTopicHighest(topicTitle)
			.assertOnSelectedTopicAsCard(topicTitle);
		
		// Author come back to change the end date of the enrollment
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		NavigationPage.load(browser)
			.openAuthoringEnvironment()
			.openResource(courseTitle);
		
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		courseEditor = course
			.assertOnTitle(brokerNodeTitle)
			.edit()
			.selectNode(brokerNodeTitle);
		
		Date newEnd = DateUtils.addDays(now, -1);
		new TBrokerConfigurationPage(browser)
			.selectConfiguration()
			.selectEndPeriod(newEnd, true)
			.saveConfiguration();
		
		courseEditor
			.autoPublish();
		
		course
			.tree()
			.assertWithTitleSelected(brokerNodeTitle);

		new TBrokerPage(browser)
			.startEnrollment()
			.confirmEnrollment(2)
			.assertEnrolledByUser(participant1, 1)
			.assertEnrolledByUser(participant2, 1);
		
		// First participant check the results
		participantLoginPage = LoginPage.load(browser, deploymentUrl);
		participantLoginPage
			.loginAs(participant1.getLogin(), participant1.getPassword())
			.assertOnResume()
			.resume();
		
		new TBrokerPage(browser)
			.assertEnrolledByTopic(topicTitle, 1);
	}
	
	
	/**
	 * An author creates a topic broker course element with a topic. Set the dates
	 * in the past and the future. It publishes the course, add two participants,
	 * and choose the topics for the participants. Update the end date and runs
	 * the enrollment. It checks that the 2 participants are enrolled successfully.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTopicBrokerByAuthorOnly()
	throws IOException, URISyntaxException {
		UserVO participant1 = new UserRestClient(deploymentUrl).createRandomUser("So");
		UserVO participant2 = new UserRestClient(deploymentUrl).createRandomUser("Mat");
			
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		// Create a course
		String courseTitle = "Course unfair broker " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//
		String brokerNodeTitle = "Topic broker 2.0";
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("topicbroker")
			.nodeTitle(brokerNodeTitle);
		
		Date now = new Date();
		Date start = DateUtils.addDays(now, -4);
		Date end = DateUtils.addDays(now, 2);
		String topicTitle = "Unfair testing";
		String topicIdentifier = "SEL-200c";
		
		TBrokerConfigurationPage tbConfig = new TBrokerConfigurationPage(browser);
		tbConfig
			.selectConfiguration()
			.selectPeriod(start, end)
			.saveConfiguration()
			.selectConfigurationTopics()
			.addTopic(topicIdentifier, topicTitle, 1, 5);
		
		courseEditor
			.publish()
			.quickPublish();
		
		// Open the course
		CoursePageFragment courseRuntime = courseEditor
			.clickToolbarBack();
		courseRuntime
			.tree()
			.assertWithTitleSelected(brokerNodeTitle);
		
		// Add participants
		courseRuntime
			.members()
			.quickImport(participant1, participant2)
			.clickToolbarBack();
		
		// Choose for the participants
		courseRuntime
			.tree()
			.selectWithTitle(brokerNodeTitle);
		
		TBrokerCoachPage brokerPage = new TBrokerCoachPage(browser);
		brokerPage
			.assertOnParticipant(participant1)
			.expandParticipantDetails(participant1)
			.selectTopic(topicTitle)
			.collapseParticipantDetails(participant1);
		
		brokerPage
			.assertOnParticipant(participant2)
			.expandParticipantDetails(participant2)
			.selectTopic(topicTitle);
		
		courseEditor = course
			.edit()
			.assertSelectedNode(brokerNodeTitle);
		
		Date newEnd = DateUtils.addDays(now, -1);
		new TBrokerConfigurationPage(browser)
			.selectConfiguration()
			.selectEndPeriod(newEnd, true)
			.saveConfiguration();
		
		courseEditor
			.autoPublish();
	
		course
			.tree()
			.assertWithTitleSelected(brokerNodeTitle);
	
		new TBrokerPage(browser)
			.startEnrollment()
			.confirmEnrollment(2)
			.assertEnrolledByUser(participant1, 1)
			.assertEnrolledByUser(participant2, 1);
	}
	
	
	/**
	 * An author create a tunnel course element, set the URL and
	 * publishes the course. It checks the page.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithTunnel()
	throws IOException, URISyntaxException {
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		LoginPage.load(browser, deploymentUrl)
			.loginAs(author.getLogin(), author.getPassword());
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		//create a course
		String courseTitle = "Course with tunnel " + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();

		//Create a course element of type tunnel
		String tunnelNodeTitle = "Tunnel 1.0";
		CoursePageFragment course = CoursePageFragment.getCourse(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("tu")
			.nodeTitle(tunnelNodeTitle);
		new TUConfigurationPage(browser)
			.selectTunnelConfiguration()
			.addURL("https://testing.frentix.com")
			.selectIframeVisible()
			.saveConfiguration();
		
		courseEditor
			.autoPublish();
		
		course
			.assertOnLearnPathLastNode(tunnelNodeTitle);
		new TUPage(browser)
			.checkPage("body #page_margins>h1");
	}

	/**
	 * Minimal testing of the JupyterHub course element. An administrator
	 * enables the feature and add in administration a new configuration.
	 * It creates a new course with a JupyterHub course element and configure
	 * the image name, publishes the course and check that the start button
	 * is there.
	 */
	@Test
	@RunAsClient
	public void courseWithJupyterLab()
	throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(administrator)
			.resume();
		
		String name = "OpenOlatLab " + UUID.randomUUID().toString();
		
		NavigationPage navBar = NavigationPage.load(browser);
		AdministrationPage administration = navBar
			.openAdministration();
		administration
			.openJupyterHubSettings(true)
			.enableJupyterLab()
			.addConfiguration(name, "https://www.openolat.org/lab/")
			.assertOnConfiguration(name);
		
		 String courseTitle = "Course with Jupyter " + UUID.randomUUID().toString();
		 navBar
		 	.openAuthoringEnvironment()
		 	.createCourse(courseTitle, true)
		 	.assertOnInfos();
		
		String nodeTitle = "Jupyter Lab";
		CoursePageFragment course = new CoursePageFragment(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("jupyterHub")
			.nodeTitle(nodeTitle);
		
		JupyterHubConfigurationPage configurationPage = new JupyterHubConfigurationPage(browser);
		configurationPage
			.selectConfiguration()
			.setImageName("OpenOlatDev")
			.saveConfiguration();

		//publish the course
		courseEditor
			.autoPublish();
		
		JupyterHubPage jupyterPage = new JupyterHubPage(browser);
		jupyterPage
			.assertOnStartButton();
	}
	

	/**
	 * The test doesn't test really Zoom itself. It enables LTI 1.3 and Zoom,
	 * create a dummy Zoom profile, create a course with a Zoom course element
	 * with the above created profile and check that the panel appears but
	 * Zoom will not accept the fake profile.
	 * 
	 */
	@Test
	public void courseWithZoom() 
	throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).getOrCreateAdministrator();
		
		String profile = UUID.randomUUID().toString();
		
		// configure the lectures module
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(administrator)
			.resume();
		NavigationPage navBar = NavigationPage.load(browser);
		AdministrationPage administration = navBar
			.openAdministration();
		administration
			.openLti13Settings()
			.enableLTI()
			.saveConfiguration();
		administration
			.openZoomSettings(false)
			.enableZoom()
			.addProfile(profile, "key-" + profile)
			.saveConfiguration();
		 
		 OOGraphene.scrollTop(browser);
		 
		 String courseTitle = "Course with Zoom " + UUID.randomUUID().toString();
		 navBar
		 	.openAuthoringEnvironment()
		 	.createCourse(courseTitle, true)
		 	.assertOnInfos();
		
		String nodeTitle = "Zoom meeting";
		CoursePageFragment course = new CoursePageFragment(browser);
		CourseEditorPageFragment courseEditor = course
			.edit();
		courseEditor
			.createNode("zoom")
			.nodeTitle(nodeTitle);
		
		ZoomConfigurationPage zoomConfiguration = new ZoomConfigurationPage(browser);
		zoomConfiguration
			.selectConfiguration()
			.selectProfile(profile)
			.saveConfiguration();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish();
		
		course = courseEditor
			.clickToolbarBack();
		
		ZoomPage zoom = new ZoomPage(browser);
		zoom
			.assertOnZoomPanel();
	}
	

	/**
	 * An author creates a course with an appointment course element, add a topic,
	 * add herself to the appointment and confirm the event.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithAppointment()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "App-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type appointment
		String nodeTitle = "App-Week";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		CoursePageFragment course = courseEditor
			.createNode("appointments")
			.nodeTitle(nodeTitle)
			.autoPublish();
		
		course
			.tree()
			.assertWithTitle(nodeTitle);
		
		String topicTitle = "Author topic";
		Date topicDate = new Date();
		int day = AppointmentPage.getDay(topicDate);
		
		AppointmentPage appointment = new AppointmentPage(browser);
		appointment
			.addTopic(topicTitle)
			.saveTopic()
			.assertOnTopic(topicTitle)
			.addUser(day)
			.searchUserByFirstName(author)
			.selectAll()
			.choose();
		
		appointment
			.assertOnConfirmAppointmentByDay(day)
			.confirmAppointmentByDay(day);
	}
	

	/**
	 * An author creates a course with an appointment course element, add a topic
	 * with multiple appointment, add herself to the appointment and confirm the event.
	 * The course has a second participant which selects an appointment, the author
	 * confirms it, and the participant checks the confirmation of the appointment.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithAppointmentRecurring()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Alfred");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "App-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type appointment
		String nodeTitle = "App-Week";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		CoursePageFragment course = courseEditor
			.createNode("appointments")
			.nodeTitle(nodeTitle)
			.autoPublish();
		course
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		//add participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(nodeTitle);
		
		String topicTitle = "Multi topic";
		
		AppointmentPage appointment = new AppointmentPage(browser);
		appointment
			.addTopic(topicTitle)
			.setRecurringTopic(1, 28, 13, 14, DayOfWeek.MONDAY)
			.saveTopic()
			.assertOnTopicMultipleMeetings(topicTitle, 3)
			.addUserToAppointment(1)
			.searchUserByFirstName(author)
			.selectAll()
			.choose();
		
		appointment
			.assertOnConfirmAppointmentByPosition(1)
			.confirmAppointmentByPosition(1);

		
		// participant comes in book an appointment
		LoginPage.load(participantBrowser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment participantCourse = new CoursePageFragment(participantBrowser);
		participantCourse
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		AppointmentPage participantAppointment = new AppointmentPage(participantBrowser);
		participantAppointment
			.assertOnTopicMultipleMeetings(topicTitle, 3)
			.assertOnSelectAppointmentByPosition(2)
			.selectAppointmentByPosition(2);
		
		// author confirm the participant's appointment
		course
			.clickTreeNode(nodeTitle)
			.assertWithTitle(nodeTitle);
		
		appointment
			.selectTopicAsCoach(topicTitle)
			.assertOnPlannedAppointmentByPosition(2)
			.confirmPlannedAppointmentByPosition(2)
			.assertOnConfirmedAppointmentByPosition(2);
		
		// participant check the confirmation
		participantCourse
			.clickTree();
		
		participantAppointment
			.selectTopicAsParticipant(topicTitle)
			.assertOnConfirmedAppointmentByPosition(2);
	}
	

	/**
	 * An author creates a course with an appointment course element, add a topic
	 * to find an hour to meet. The course has a second participant which selects
	 * the proposed appointment, the author confirms it, and the participant checks
	 * the confirmation of the appointment.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithAppointmentFinding()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Alfred");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "App-" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//create a course element of type appointment
		String nodeTitle = "App-Finding";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		CoursePageFragment course = courseEditor
			.createNode("appointments")
			.nodeTitle(nodeTitle)
			.autoPublish();
		
		course
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		//add participant
		MembersPage members = course
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		course
			.tree()
			.selectWithTitle(nodeTitle);
		
		String topicTitle = "Find topic";
		int today = AppointmentPage.getDay(new Date());
		
		AppointmentPage appointment = new AppointmentPage(browser);
		appointment
			.addTopic(topicTitle)
			.setFinding()
			.saveTopic()
			.assertOnTopic(topicTitle);
		
		// participant comes in book an appointment
		LoginPage.load(participantBrowser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment participantCourse = new CoursePageFragment(participantBrowser);
		participantCourse
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		AppointmentPage participantAppointment = new AppointmentPage(participantBrowser);
		participantAppointment
			.assertOnTopic(topicTitle)
			.selectAppointmentByDay(today);
		
		// author confirm the participant's appointment
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);
		
		appointment
			.confirmAppointmentFindingByDay(today, participant)
			.assertOnConfirmedAppointmentByDay(today);
		
		// participant check the confirmation
		participantCourse
			.tree()
			.selectWithTitle(nodeTitle);
		
		participantAppointment
			.selectTopicAsParticipant(topicTitle)
			.assertOnConfirmedAppointmentByDay(today);
	}
	
	
	/**
	 * An author create a course with a course element
	 * with one check box. It add one participant. The
	 * participant log in, go to the course to check its
	 * box and see if it has done the course with 100%.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCheckboxWithScore()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("nezuko");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Check Course" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//add participant
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "CheckNode";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("checklist")
			.nodeTitle(nodeTitle);
		
		String checkboxTitle = "Do some programming";
		
		CheckListConfigPage checkConfig = new CheckListConfigPage(browser);
		checkConfig
			.selectListConfiguration()
			.addCheckbox(checkboxTitle, 4)
			.assertOnCheckboxInList(checkboxTitle);
		
		checkConfig
			.selectAssessmentConfiguration()
			.setScoring(0, 4, 3)
			.saveAssessmentConfiguration();
		
		courseEditor
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.passed)
			.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		// participant comes in

		LoginPage participantLoginPage = LoginPage.load(participantBrowser, deploymentUrl);
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(participantBrowser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(participantBrowser);
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);

		CheckListPage checkPage = new CheckListPage(participantBrowser);
		checkPage
			.assertOnCheckbox(checkboxTitle)
			.check(checkboxTitle);
		// student has done the course
		course
			.assertOnLearnPathNodeDone(nodeTitle)
			.assertOnLearnPathPercent(100);
		
		// open the assessment tool and check the participant passed the node
		// and the course
		AssessmentToolPage assessmentTool = new CoursePageFragment(browser)
			.assessmentTool();
		assessmentTool
			.users()
			.assertOnUsers(participant)
			.selectUser(participant)
			.assertPassed(participant)
			.assertUserPassedCourseNode(nodeTitle);
	}
	
	/**
	 * An author create a course with a course element
	 * with one check box. It add one participant and he
	 * checks the box.
	 * 
	 * @param loginPage The login page
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithCheckbox()
	throws IOException, URISyntaxException {
						
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("nezuko");

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Checklist" + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		CoursePageFragment courseRuntime = navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
		
		//add participant
		MembersPage members = courseRuntime
			.members();
		members
			.addMember()
			.importList()
			.setMembers(participant)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		// back to course
		members
			.clickToolbarBack();
		
		//create a course element of type Test with the test that we create above
		String nodeTitle = "Liste de controle";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("checklist")
			.nodeTitle(nodeTitle);
		
		String checkboxTitle = "Do some stuff";
		
		CheckListConfigPage checkConfig = new CheckListConfigPage(browser);
		checkConfig
			.selectListConfiguration()
			.addCheckbox(checkboxTitle, -1)
			.assertOnCheckboxInList(checkboxTitle);
		
		checkConfig
			.selectAssessmentConfiguration()
			.disableScoring()
			.saveAssessmentConfiguration();
		
		courseEditor
			.selectTabLearnPath()
			.setCompletionCriterion(FullyAssessedTrigger.confirmed)
			.save();
		
		courseEditor
			.autoPublish()
			.publish()
			.settings()
			.accessConfiguration()
			.setAccessToMembersOnly()
			.save()
			.clickToolbarBack();
		
		//log out
		new UserToolsPage(browser)
			.logout();
		
		// participant comes in
		LoginPage.load(browser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage participantNavBar = NavigationPage.load(browser);
		participantNavBar
			.openMyCourses()
			.select(courseTitle);
		
		CoursePageFragment course = new CoursePageFragment(browser);
		course
			.tree()
			.assertWithTitleSelected(nodeTitle);

		CheckListPage checkPage = new CheckListPage(browser);
		checkPage
			.assertOnCheckbox(checkboxTitle)
			.check(checkboxTitle);
		// check doesn't influence the learn path
		course
			.assertOnLearnPathNodeReady(nodeTitle)
			.assertOnLearnPathPercent(0);
		// student has done the course
		course
			.confirmNode()
			.assertOnLearnPathNodeDone(nodeTitle)
			.assertOnLearnPathPercent(100);
	}
	

	/**
	 * An author create a course with a project broker course element.
	 * It publishes the course and jumps to the element to create a new
	 * project. A participant chooses the project and uploads a file,
	 * the author opens the project and look at the uploaded document.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void courseWithProjectBroker()
	throws IOException, URISyntaxException {
		WebDriver participantBrowser = getWebDriver(1);
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Theo");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Broker " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle, true)
			.clickToolbarBack();
	
		//go the  course editor
		String projectTitle = "Project - " + UUID.randomUUID();
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		
		//create course element project broker
		courseEditor
			.createNode("projectbroker")
			.nodeTitle(projectTitle);
		
		// publish the course
		courseEditor
			.publish()
			.quickPublish(UserAccess.membersOnly);
	
		MembersPage membersPage = courseEditor		
			.clickToolbarBack()
			.members();

		membersPage
			.addMember()
			.searchMember(participant, true)
			.nextUsers()
			.nextOverview()
			.selectRepositoryEntryRole(false, false, true)
			.nextPermissions()
			.finish();
		
		//go to the forum
		courseEditor
			.clickToolbarBack()
			.tree()
			.selectWithTitle(projectTitle.substring(0, 20));
		
		String projectName = "Moon observation";
		ProjectBrokerPage brokerPage = new ProjectBrokerPage(browser);
		brokerPage
			.assertOnProjectBrokerList()
			.createNewProject(projectName);
		
		//Participant open the course
		LoginPage.load(participantBrowser, deploymentUrl)
			.loginAs(participant.getLogin(), participant.getPassword());

		NavigationPage.load(participantBrowser)
			.openMyCourses()
			.select(courseTitle);
		
		//open the course and see the test start page
		CoursePageFragment participantCourse = new CoursePageFragment(participantBrowser);
		participantCourse
			.tree()
			.assertWithTitleSelected(projectTitle.substring(0, 20));
		
		URL submitUrl = JunitTestHelper.class.getResource("file_resources/submit_2.txt");
		File submitFile = new File(submitUrl.toURI());
		
		ProjectBrokerPage participantBrokerPage = new ProjectBrokerPage(participantBrowser);
		participantBrokerPage
			.assertOnProjectBrokerList()
			.assertOnProjectBrokerInList(projectName)
			.enrollInProject(projectName)
			.selectProject(projectName)
			.selectFolders()
			.assertOnDropbox()
			.uploadDropbox(submitFile);
		
		//Coach look at the dropbox
		brokerPage
			.assertOnProjectBrokerList()
			.assertOnProjectBrokerInList(projectName)
			.selectProject(projectName)
			.selectFolders()
			.assertOnDropbox()
			.selectFolderInDropbox(participant)
			.assertOnFileInDropbox(submitFile.getName());		
	}
}
