package org.olat.test.sandbox;

import java.io.File;

import org.olat.test.functional.test.TestEditorCombiTest;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.AssessmentEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.EnrolmentEditor;
import org.olat.test.util.selenium.olatapi.course.editor.TestElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor.CourseElemTypes;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.course.run.QuestionnaireElement;
import org.olat.test.util.selenium.olatapi.course.run.TestElement;
import org.olat.test.util.selenium.olatapi.course.run.TestRun;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.selenium.olatapi.home.HomeConfigurator;
import org.olat.test.util.selenium.olatapi.home.MySettings;
import org.olat.test.util.selenium.olatapi.lr.CPResourceEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.lr.LearningResources.LR_Types;
import org.olat.test.util.selenium.olatapi.qti.EssayQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.FIBQuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionnaireEditor;
import org.olat.test.util.selenium.olatapi.qti.TestEditor;
import org.olat.test.util.selenium.olatapi.qti.QuestionEditor.QUESTION_TYPES;
import org.olat.test.util.selenium.olatapi.user.UserManagement;
import org.olat.test.util.selenium.olatapi.user.UserSettings;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.test.util.selenium.olatapi.course.editor.QuestionnaireElementEditor;
import org.olat.test.util.selenium.OlatLoginHelper;

/**
 * Sandbox test class.
 * Tests basic functionality.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class CreateCourseTest extends BaseSeleneseTestCase {

	private String COURSE_NAME = "AAA"+ System.currentTimeMillis();
	private String WIKI_NAME = "aWiki"+ System.currentTimeMillis();
	private String GROUP_NAME = "aGroup"+ System.currentTimeMillis();
	private String TEST_NAME = "aTest"+ System.currentTimeMillis();
	
	public void setUp() throws Exception {		
		Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);		
	}
		
	private void testUserManagement() {
		OLATWorkflowHelper workflow_A = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		/*UserManagement userManagement = workflow_A.getUserManagement();
		UserSettings userSettings = userManagement.selectUser("lavinia");
		userSettings.setFirstName("Lavinia A.", true);
		userSettings.setPassword("lavinia1");
		userSettings.setRoles(null, false, true, false, false);*/
		
		/*userManagement.createUser("lavinia3", "Lavinia", "D.", "ld3@gmail.com", "lavinia3");
		
		OLATWorkflowHelper workflow_L = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, "lavinia3", "lavinia3"));
		workflow_L.getHome().getEvidencesOfAchievement();*/
		MySettings mySettings = workflow_A.getHome().getUserSettings();
		String email = mySettings.getEmail();
		boolean isMyLastNamePresent = mySettings.isDisabledTextPresent("Administrator");
		
		//String lastname = mySettings.getDisabledText("Last name");
		System.out.println("isMyLastNamePresent: " + isMyLastNamePresent);
		//System.out.println("lastname: " + lastname);
		System.out.println("email: " + email);
		
		workflow_A.logout();
	}
	
	private void testUserManager() {
		/*OLATWorkflowHelper workflow_A = Context.getContext().getOLATWorkflowHelper(Context.getContext().getOlatLoginInfo(1, "lavinia", "lavinia1"));
		boolean cannotEditAdmin = workflow_A.getUserManagement().cannotEditUser("administrator");
		System.out.println("cannotEditAdmin: " + cannotEditAdmin);*/
		
		OLATWorkflowHelper workflow_A = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		workflow_A.getUserManagement().deleteUser("test12");
		System.out.println("user deleted");
	}
	
	/**
	 * Tests basic course functionality:
	 * - create course
	 * - insert node
	 * - publish
	 * - create learning group
	 * @throws Exception
	 */
	private void testCreateCourse() throws Exception {		
		//OlatLoginInfos loginInfos = Context.getContext().createuserIfNotExists(1, "test102", "test100", true, true, true, true, true);
		
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "bla");
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
		CourseElementEditor courseElementEditor = courseEditor.selectCourseElement("Forum");
		courseElementEditor.setDescription("NEW DESCRIPTION");
		
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		Forum forum = courseRun.selectForum("Forum");
		forum.openNewTopic("test entry concurrent edit", "forum message editing");
		
		//attach doc
		/*File doc = OlatServerSetupHelper.locateFile("org/olat/test/file_resources/Word.doc");
		String remoteDoc = Context.getContext().provideFileRemotely(doc);
		forum.attachFileToMsg(remoteDoc);
		forum.deleteAttachedFile("Word.doc");
		
		
		//create group
		GroupManagement groupManagement = courseRun.getGroupManagement();
		groupManagement.createLearningGroup("learning group selenium 5", "fifth lg", 1, false, false);
		courseRun = groupManagement.close();
		
		courseEditor = courseRun.getCourseEditor();
		EnrolmentEditor enrolmentEditor = (EnrolmentEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.ENROLMENT, true, null);
		enrolmentEditor.selectLearningGroups("learning group selenium 5");
		enrolmentEditor.changeVisibilityDependingOnGroup("learning group selenium 5");
		courseEditor.publishCourse();
		*/
						
		workflow.logout();
	}
	
	private void testCreateLearningArea() {		
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		LRDetailedView lRDetailedView1 = workflow.getLearningResources().createResource(COURSE_NAME, "bla", LR_Types.COURSE);
		CourseRun courseRun1 = lRDetailedView1.showCourseContent();	
		GroupManagement groupManagement1 = courseRun1.getGroupManagement();
		groupManagement1.createLearningArea("learning area selenium 1", null);
		workflow.logout();
	}
	
	private void testAssessmentElement() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "bla");
		AssessmentEditor assessmentEditor = (AssessmentEditor)courseEditor.insertCourseElement(CourseElemTypes.ASSESSMENT, true, null);
    //	author fills in the assessment configuration form
		assessmentEditor.configure(true, 1, 10, true, 5);
		courseEditor.publishCourse();
	}
	
	private void testRunTest() throws Exception {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent("Demo Course");
		TestElement testElement = courseRun.selectTest("Test");
		TestRun testRun = testElement.startTest();		
		testRun.finishTest(true, 0);
		testElement = courseRun.selectTest("Test");
		assertEquals("0.000", testElement.getAchievedScore());
	}
	
	private void testCreateTest() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos());
		TestEditor testEditor = workflow.getLearningResources().createTestAndStartEditing(TEST_NAME, "nonsense");
		testEditor.addQuestion(QUESTION_TYPES.GAP_TEXT,"Gap Text Question");
	// Gap text	
		FIBQuestionEditor fIBQuestionEditor = (FIBQuestionEditor)testEditor.selectQuestion("Gap Text Question");
		fIBQuestionEditor.selectQuestionAndAnswersTab();
		fIBQuestionEditor.editTextFragment(1,"Name of Kristinas boy:");
		fIBQuestionEditor.addNewBlank();
		fIBQuestionEditor.setBlankSolution("Nils", 2);
		fIBQuestionEditor.changeCapitalization(2);
		workflow.logout();
	}
	
	private void testCopyCourse() throws Exception {						
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		/*LRDetailedView lRDetailedView = workflow.getLearningResources().searchResource("Demo Course", null);
		lRDetailedView.copyLR("Copy A of Demo course", "bla");	
						
		workflow.logout();*/
	}
	
	/*public void testChangeAccess() throws Exception {		
		//OlatLoginInfos loginInfos = Context.getContext().createuserIfNotExists(1, "test102", "test100", true, true, true, true, true);
		
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "bla");
		courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.FORUM, true, null);
		CourseElementEditor courseElementEditor = courseEditor.selectCourseElement("Forum");
		
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();		
		courseRun.getGroupManagement().createGroupAndAddMembers(GROUP_NAME, null, Context.getContext().getStandardStudentOlatLoginInfos().getUsername());
		courseEditor = courseRun.getCourseEditor();
			
		//courseElementEditor.changeVisibilityDependingOnGroup("lg av 1");
		
		courseElementEditor = courseEditor.insertCourseElement(CourseElemTypes.ASSESSMENT, true, null);
		courseElementEditor.changeAccessyDependingOnGroup(GROUP_NAME);
		courseElementEditor.editVisibilityInfo("this assessment is only accessible to learning group members");
		
		courseEditor.publishCourse();
					
		workflow.logout();
	}/*
	
	/*public void testCreateWiki() throws Exception {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		LRDetailedView lRDetailedView1 = workflow.getLearningResources().createResource(WIKI_NAME, "course run test", LR_Types.WIKI);
		//select again the learningResources1
		LearningResources learningResources = workflow.getLearningResources();
		CourseEditor courseEditor1 = learningResources.createCourseAndStartEditing(COURSE_NAME, "course run test");
		
		workflow.logout();
	}*/
	
	/*public void testCreateGroup() throws Exception {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		Groups groupsTab = workflow.getGroups();
		GroupAdmin groupAdmin = groupsTab.createProjectGroup(GROUP_NAME, "bla bla");
		String[] owners = {Context.getContext().getStandardAuthorOlatLoginInfos().getUsername()};
		String[] participants = {Context.getContext().getStandardStudentOlatLoginInfos().getUsername()};
		groupAdmin.addMembers(participants, owners);
		workflow.logout();		
	}*/
	
	/*public void testGroups() throws Exception {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		Groups groups1 = workflow.getGroups();
		GroupAdmin group = groups1.createProjectGroup(GROUP_NAME, "second test");
		group.setTitleAndDescription(GROUP_NAME + "changed", "description changed");
		group.setTools(true, true, true, true, true, true);
		group.setInfo("hello everybody");
		group.selectCalendarWriteAccess("Owners and tutors respectively");
		String[] userNames = {Context.getContext().getStandardStudentOlatLoginInfos().getUsername()};
		group.addMembers(userNames, new String[0]);
		workflow.logout();
	}*/
	
	private void testCreateQuestionnaire() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos());
		
		WorkflowHelper.deleteAllLearningResourcesFromAuthor(Context.getContext().getStandardAuthorOlatLoginInfos().getUsername());
		QuestionnaireEditor qEditor = workflow.getLearningResources().createQuestionnaireAndStartEditing(TEST_NAME, "nonsense");
		EssayQuestionEditor questionEditor = (EssayQuestionEditor)qEditor.addQuestion(QUESTION_TYPES.ESSAY,"ESSAY");
		questionEditor.selectQuestionAndAnswersTab();
		questionEditor.editQuestion("please describe ...");
		questionEditor.setAnswerSize(120, 11);
		qEditor.close();
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "bla bla");
		QuestionnaireElementEditor questionnaireElementEditor= (QuestionnaireElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.QUESTIONNAIRE, true, null);
		questionnaireElementEditor.chooseMyFile("aTest1253791768388");
		questionnaireElementEditor.configureQuestionnaireLayout(true, true, true, true);
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		QuestionnaireElement questionnaireElement = courseRun.selectQuestionnaire(CourseEditor.QUESTIONNAIRE_TITLE);
		questionnaireElement.start().finish();
	}
	
	private void testLoginExpectingError() throws Exception {
		assertTrue(OlatLoginHelper.loginExpectingError(1, "test4", "test"));
	}
	
	private void testForum() {
		// author creates course with forum, opens welcome message topic			
		OLATWorkflowHelper olatWorkflow_1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAuthorOlatLoginInfos());
		CourseEditor courseEditor1 = olatWorkflow_1.getLearningResources().createCourseAndStartEditing("Concurrent_Forum_Replies_1260951770640", "selenium");
		courseEditor1.insertCourseElement(CourseElemTypes.FORUM, true, null);
		courseEditor1.publishCourse();
		LRDetailedView lRDetailedView1 = courseEditor1.closeToLRDetailedView();
		CourseRun courseRun1 = lRDetailedView1.showCourseContent();
		Forum forum1 = courseRun1.selectForum(CourseEditor.FORUM_COURSE_ELEM_TITLE);
		forum1.openNewTopic("welcome", "werdet euren senf los");
	}
	
	private void testMyGroupPortletConfig() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());		
		workflow.getHome().getHomeConfigurator().configMyGroupPortlet(99, HomeConfigurator.SORT_TYPE.ALPHABET, true);
		workflow.getHome().getHomeConfigurator().configMyBookmarkPortlet(99, HomeConfigurator.SORT_TYPE.ALPHABET, true);
	}
	
	private void testImportCP() throws Exception {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		try {
			File f = WorkflowHelper.locateFile("org/olat/test/sandbox/UserManual_6.2.zip");		
			CPResourceEditor cpEditor = workflow.getLearningResources().importCP(f, "A_B12_CP", "BLA BLA");
			
			if(cpEditor!=null) {
				cpEditor.addPageAndRename("Introduction", "Lavinia", "bau bau");
				cpEditor.copyPageAndRename("Introduction", "SecondIntroduction");
				cpEditor.copyPageAndRename("Lavinia", "Dora");
				cpEditor.copyPageAndRename("Dora", "ET");
				
				cpEditor.deletePage("Lavinia", true);
				File doc = WorkflowHelper.locateFile("org/olat/test/sandbox/4_within_subjects_vs_between_subjects_designs.zip");
				String remoteDoc = Context.getContext().provideFileRemotely(doc);
				cpEditor.importPage("Introduction", remoteDoc);
			}
		} finally {
			workflow.logout();
		}
	}
	
	private void testCreateCourseWithTest() throws Exception {				
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		CourseEditor courseEditor = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "bla");
		TestElementEditor test1 = (TestElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TEST, true, null);
		test1.setTitle("TEST_1");
		test1.configureTestLayout(true, true, true, true, 10, true);
		
		
		TestElementEditor test2 = (TestElementEditor)courseEditor.insertCourseElement(CourseEditor.CourseElemTypes.TEST, true, null);
		test2.setTitle("TEST_2");
		test2.configureTestLayout(false, false, false, false, 0, false);
		
		courseEditor.publishCourse();
		CourseRun courseRun = courseEditor.closeToLRDetailedView().showCourseContent();
		
								
		workflow.logout();
	}
	
	public void testLearningGroup() throws Exception {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		LRDetailedView lRDetailedView = workflow.getLearningResources().createCourseAndStartEditing(COURSE_NAME, "description").closeToLRDetailedView();
		GroupAdmin groupAdmin = lRDetailedView.showCourseContent().getGroupManagement().createLearningGroup(GROUP_NAME, "description", 0, false, false);
		groupAdmin.setMemberDisplayOptions(null, true, null);
		System.out.println("testLearningGroup");
	}
	
}
