package org.olat.test.functional.course.run;

import java.io.File;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.course.editor.CourseElementEditor;
import org.olat.test.util.selenium.olatapi.course.editor.PodcastEditor;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.PodcastRun;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Tests the Podcast BB and Podcast Editor. Also tests issue OLAT-5767
 * <br/>
 * <p>
 * Test setup: <br/>
 * 1. import Course "Course_with_all_bb.zip" <br/>
 * 2. enter title "EditPodcastCourse"  <br/>
 * 3. enter description "Edit And Configure Podcast Test Course" <br/>
 * <p>
 * Test case: <br/>
 * *--- AS AUTHOR: ---*
 * login as author and go to tab learning resources <br/>
 * start course editor <br/>
 * click on publish course <br/>
 * select "Podcast intern" <br/>
 * click "Next" <br/>
 * set permission "All registered OLAT users" <br/>
 * click "Finish" <br/>
 * close editor <br/>
 * run course <br/>
 * select BB "Podcast intern" <br/>
 * click on "Add episode" <br/>
 * enter title "First Podcast" <br/>
 * enter description "A Podcast Entry Description" <br/>
 * click on "Select file" <br/>
 * select mp3 audio file <br/>
 * assure is visible "audio" and "A Podcast Entry Description" <br/>
 * click on "Edit episode" <br/>
 * enter description "A modified Podcast Entry Description" <br/>
 * click on "Publish" <br/>
 * assure is visible "A modified Podcast Entry Description" <br/>
 * click on "Comments (0)" for comment <br/>
 * enter comment "I really enjoyed recording this." <br/>
 * click save <br/>
 * assure visible "Comments (1)" <br/>
 * start course editor <br/>
 * select BB "Podcast intern" <br/>
 * click on access tab and uncheck "Blocked for learners" from section "Present" <br/>
 * click save <br/>
 * uncheck "Blocked for learners" from section "Read and Write" <br/>
 * click save <br/>
 * click publish <br/>
 * select "Podcast intern" <br/>
 * click "Next" <br/>
 * click "Finish" <br/>
 * close editor <br/>
 * *--- AS STUDENT: ---*
 * login as student and go to tab learning resources <br/>
 * click "Search form" <br/>
 * enter "EditPodcastCourse" <br/>
 * run course <br/>
 * select BB "Podcast intern" <br/>
 * assure is visible "A modified Podcast Entry Description" <br/>
 * click on "Add episode"
 * enter title "A Studi Podcast" <br/>
 * enter description "A Student Podcast Entry Description" <br/>
 * select mp3 audio file <br/>
 * click on "Publish" <br/>
 * assure is visible "audio" and "A Student Podcast Entry Description" <br/>
 * *--- CONTINUE AS AUTHOR: ---*
 * select BB "Podcast intern" <br/>
 * assure is visible "audio" and "A Student Podcast Entry Description" <br/>
 * click on "Add episode"
 * enter title "My Second Podcast"
 * enter desription "Can students still read this Podcast" <br/>
 * select mp3 audio file <br/>
 * click on "Publish" <br/>
 * assure is visible "Can students still read this Podcast" <br/>
 * *--- CONTINUE AS STUDENT: ---*
 * select BB "Podcast intern" <br/>
 * assure is visible "Can students still read this Podcast" <br/>
 * *--- CONTINUE AS AUTHOR: ---*
 * go to tab learning resources <br/>
 * click "Search form" <br/>
 * enter "EditBlogCourse" <br/>
 * click on "Detailed view" for "EditBlogCourse" <br/>
 * click on "Delete" <br/>
 * logout <br/>
 *   
 * </p>
 * 
 * @author Alberto Sanz
 *
 */

public class EditAndConfigurePodcastTest extends BaseSeleneseTestCase {
	
  private final String IMPORTABLE_COURSE_PATH = Context.FILE_RESOURCES_PATH + "Course_with_all_bb.zip"; 
  private final String COURSE_NAME_PREFIX = "EditPodcastCourse-";
  private final String COURSE_NAME = COURSE_NAME_PREFIX+System.currentTimeMillis();

  private final String PODCAST_INTERN = "Podcast intern";
  private final String PODCAST_EPISODE_TITLE = "First Podcast";
  private final String PODCAST_EPISODE_DESCRIPTION = "A Podcast Entry Description";
  private final String PODCAST_EPISODE_DESCRIPTION_MODIFIED = "A MODIFIED Podcast Entry Description";
  private final String AUDIO_FILE_NAME = "Mp3.mp3";
  private final String EPISODE_COMMENT = "I really enjoyed recording this this.";
  
  private final String PODCAST_EPISODE_2_TITLE = "A Studi Podcast";
  private final String PODCAST_EPISODE_2_DESCRIPTION = "A Student Podcast Entry Description";
  
  private final String PODCAST_EPISODE_3_TITLE = "My Second Podcast";
  private final String PODCAST_EPISODE_3_DESCRIPTION = "Can students still read this Podcast";
  
  
  
  @Override
  public void setUp() throws Exception {
    Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

    //cleanup first
    WorkflowHelper.deleteLearningResources(context.getStandardAdminOlatLoginInfos(1).getUsername(), COURSE_NAME_PREFIX);

    //import course
    File file = WorkflowHelper.locateFile(IMPORTABLE_COURSE_PATH);      
    WorkflowHelper.importCourse(file, COURSE_NAME, COURSE_NAME_PREFIX);

    //assign owner
    OLATWorkflowHelper workflowAdmin = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    workflowAdmin.getLearningResources().searchMyResource(COURSE_NAME).assignOwner(context.getStandardAuthorOlatLoginInfos(1).getUsername());
  }

  public void testEditAndConfigurePodcast() throws Exception {
    Context context = Context.getContext();

    //author
    OLATWorkflowHelper workflowAuthor = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
    CourseEditor courseEditor = workflowAuthor.getLearningResources().showCourseContent(COURSE_NAME).getCourseEditor();
    courseEditor.publishCourse();
    CourseRun courseRun = courseEditor.closeToCourseRun();
    PodcastRun podcastRun = courseRun.selectPodcast(PODCAST_INTERN);
    
    File mp3File = WorkflowHelper.locateFile(Context.FILE_RESOURCES_PATH + AUDIO_FILE_NAME);    
    String remoteFilePath = Context.getContext().provideFileRemotely(mp3File);
    podcastRun.createEpisode(PODCAST_EPISODE_TITLE, PODCAST_EPISODE_DESCRIPTION, remoteFilePath);
    assertTrue(podcastRun.isTextPresent("audio"));
    assertTrue(podcastRun.isTextPresent(PODCAST_EPISODE_DESCRIPTION));
    podcastRun.editEpisode(PODCAST_EPISODE_TITLE, PODCAST_EPISODE_DESCRIPTION_MODIFIED, null);
    assertTrue(podcastRun.isTextPresent(PODCAST_EPISODE_DESCRIPTION_MODIFIED));
    podcastRun.commentEpisode(PODCAST_EPISODE_TITLE, EPISODE_COMMENT);
    assertTrue(podcastRun.hasComments(PODCAST_EPISODE_TITLE, 1));
    
    CourseEditor courseEditor1 = courseRun.getCourseEditor();
    PodcastEditor podcastEditor = (PodcastEditor)courseEditor1.selectCourseElement(PODCAST_INTERN);
    podcastEditor.changeAccessBlockedForLearners(CourseElementEditor.ACCESS_TYPE.PRESENT);
    podcastEditor.changeAccessBlockedForLearners(CourseElementEditor.ACCESS_TYPE.READ_AND_WRITE);
    courseEditor1.publishCourse();
    CourseRun courseRun1 = courseEditor1.closeToCourseRun();

    //student
    OLATWorkflowHelper workflowStudent = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
    CourseRun courseRun2 = workflowStudent.getLearningResources().searchAndShowCourseContent(COURSE_NAME);
    PodcastRun podcastRun2 = courseRun2.selectPodcast(PODCAST_INTERN);
    assertTrue(podcastRun2.isTextPresent(PODCAST_EPISODE_DESCRIPTION_MODIFIED));
    podcastRun2.createEpisode(PODCAST_EPISODE_2_TITLE, PODCAST_EPISODE_2_DESCRIPTION, remoteFilePath);
    assertTrue(podcastRun2.isTextPresent("audio"));
    assertTrue(podcastRun2.isTextPresent(PODCAST_EPISODE_2_DESCRIPTION));

    //author
    PodcastRun podcastRun1 = courseRun1.selectPodcast(PODCAST_INTERN);    
    assertTrue(podcastRun1.isTextPresent("audio"));
    assertTrue(podcastRun1.isTextPresent(PODCAST_EPISODE_2_DESCRIPTION));
    podcastRun1.createEpisode(PODCAST_EPISODE_3_TITLE, PODCAST_EPISODE_3_DESCRIPTION, remoteFilePath);
    assertTrue(podcastRun1.isTextPresent(PODCAST_EPISODE_3_DESCRIPTION));
    
    //student
    podcastRun2 = courseRun2.selectPodcast(PODCAST_INTERN);    
    assertTrue(podcastRun2.isTextPresent(PODCAST_EPISODE_3_DESCRIPTION));
  }
}
