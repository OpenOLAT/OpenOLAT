/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.PageLoadWait;
import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.components.ChatComponent;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.group.GroupManagement;
import org.olat.test.util.selenium.olatapi.group.RightsManagement;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;

import com.thoughtworks.selenium.Selenium;

/**
 * OLAT abstraction for the Course Run.
 * @author Lavinia Dumitrescu
 *
 */
public class CourseRun extends OLATSeleniumWrapper {

	
	
	/**
	 * @param selenium
	 */
	public CourseRun(Selenium selenium) {
		super(selenium);	
		
		try {
			//delay at load course since 23.06.09
			Thread.sleep(3000);
		} catch (Exception e) {			
		}
		//Check that we're on the right place
		if(!selenium.isElementPresent("ui=course::toolbox_generalTools_detailedView()")) {
			throw new IllegalStateException("This is not the - Course run - page");
		}
	}

	/**
	 * Selects root, it must start with: title. 
	 * (Warning: the titles are truncated! if they are longer than 22 chars)
	 * @param title
	 * @return
	 */
	public StructureElement selectRoot(String title) {
	  String truncatedTitle = title;
	  if(title.length()>22) {
	    truncatedTitle = title.substring(0, 22);
	  } 
      selenium.click("ui=course::menu_root(nameOfCourse=" + truncatedTitle + ")");
	  selenium.waitForPageToLoad("30000");
	  return new StructureElement(selenium);
	}
	
	/**
	 * Tries to select a course element than is no more visible, so it gets the root.
	 * @param title
	 * @return
	 */
	public StructureElement selectAnyButGetToRoot(String title) {
		return selectAnyButGetToRoot(title, PageLoadWait.DEFAULT);
	}
	
	public StructureElement selectAnyButGetToRoot(String title, PageLoadWait wait) {
		selectCourseElement(title, wait);
		return new StructureElement(selenium);
	}
	
	/**
	 * Selects course element. CourseRun page still valid (returns void).
	 * Use this if the type of the selected element doesn't matter.
	 * @param title
	 */
	public void selectCourseElement(String title) {
		selectCourseElement(title, PageLoadWait.DEFAULT);
	}
	
	public void selectCourseElement(String title, PageLoadWait wait){
		selenium.click("ui=course::menu_link(link=" + title + ")");
		if(wait != PageLoadWait.NO_WAIT){
			selenium.waitForPageToLoad(wait.getMs());
		}
	}
	
	/**
	 * Selects course element, identified by "position" (root's child).
	 *  
	 * @param position starts from 1.
	 */
	public boolean selectCourseElement(int position) {
		if(selenium.isElementPresent("ui=course::menu_positionedCourseElem(index=" + position + ")")) {
		  selenium.click("ui=course::menu_positionedCourseElem(index=" + position + ")");
		  selenium.waitForPageToLoad("30000");
		  return true;
		} else if(position==1){
		  System.out.println("NO COURSE ELEMENT FOUND TO BE SELECTED. IS THIS NOT STRANGE???");
		  //throw new IllegalStateException("NO COURSE ELEMENT FOUND TO BE SELECTED! Is this really an empty course?");
		}
		return false;
	}
	
	/**
	 * 
	 * @param title
	 * @return Returns an EnrolmentRun element.
	 */
	public EnrolmentRun selectEnrolment(String title) {
		selectCourseElement(title);		
		return new EnrolmentRun(selenium);		
	}
	
	/**
	 * Selects the forum element with the given title.
	 * @param title
	 * @return
	 */
	public Forum selectForum(String title) {
		selectCourseElement(title);
		if(selenium.isElementPresent("ui=course::content_forum_newTopic()") || selenium.isElementPresent("ui=course::content_forum_displayForum()")) {
			return new Forum(selenium);
		}
		throw new IllegalStateException("This is not the - Forum - page");
	}
	
	public TopicAssignmentRun selectTopicAssignment(String  title) {
		selectCourseElement(title);
		//TODO: LD: add check - is this a project broker element?
		return new TopicAssignmentRun(selenium);
	}
	
	/**
	 * Selects and returns the SCORM element with the given title.
	 * @param title
	 * @return
	 */
	public SCORM selectSCORM(String title) {
		selectCourseElement(title);
		if(selenium.isElementPresent("ui=course::content_scorm_scormPreview()")) {
			return new SCORM(selenium);
		}
		throw new IllegalStateException("This is not the - SCORM - page");
	}
	
	/**
	 * Selects and returns the Wiki element with the given title.
	 * 
	 * @param title
	 * @return
	 */
	public WikiRun selectWiki(String title) {
		//selectCourseElement(title);
		//replace selectCourseElement call, no waitForPageToLoad needed
		selenium.click("ui=course::menu_link(link=" + title + ")");
		try {
			Thread.sleep(3000);
		} catch (Exception e) {			
		}
		if(selenium.isElementPresent("ui=wiki::sideNavigation_index()")) {
			return new WikiRun(selenium);
		}
		throw new IllegalStateException("This is not the - Wiki - page");
	}
	
	/**
	 * Selects and returns an Assessment element with the given title.
	 * @param title
	 * @return
	 */
	public AssessmentElement selectAssessmentElement(String title) {
		selectCourseElement(title);
		return new AssessmentElement(selenium);
	}
	
	/**
	 * Closes the CourseRun tab. (Leaves the CourseRun context.) 
	 * Get to the previous selected tab
	 *
	 */
	public void close(String title) {
		selenium.click("ui=tabs::closeCourse(nameOfCourse=" + title + ")");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Selects the course tab, in case the selection changed to another tab.
	 * @param title
	 */
	public void selectCourseTab(String title) {
	  selenium.click("ui=tabs::selectCourse(nameOfCourse=" + title + ")");
    selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * It assumes that this is the only opened course.
	 */
	public void closeAny() {
		selenium.click("ui=tabs::closeAnyCourse()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @return Returns a CourseEditor object.
	 */
	public CourseEditor getCourseEditor() {
		selenium.click("ui=course::toolbox_courseTools_courseEditor()");
		selenium.waitForPageToLoad("60000");
		return new CourseEditor(selenium);
	}	
	
	/**
	 * If the course run was disposed by another process (e.g. published, or properties modified),
	 * your CourseRun object gets disposed.
	 * @return
	 */
	public DisposedCourseRun getDisposedCourseRun() {
		return new DisposedCourseRun(selenium);
	}
	
	public ChatComponent getChatComponent() {
	  return new ChatComponent(selenium);
	}
	
	/**
	 * The user tries to open the courseEditor but it is locked.
	 * The caller of this method expects to find the course locked, so it should not open the CourseEditor.
	 * 
	 * @return Returns true if the "alreadyLocked" message shows up, false otherwise.
	 */
	public boolean checkCourseLocked(String lockOwnerUsername) throws Exception {
		selenium.click("ui=course::toolbox_courseTools_courseEditor()");
		selenium.waitForPageToLoad("30000");

		// and wait until 'This course is currently edited by lockOwnerUsername and therefore locked.' appears
		for (int second = 0;; second++) {
			if (second >= 20) return false;
			try { 
				if (selenium.isTextPresent("This course is currently edited by "+lockOwnerUsername+" and therefore locked.")) 
					return true; 
			} catch (Exception e) {}

			Thread.sleep(1000);
		}
	}
	
	/**
	 * 
	 * @return Returns a LRDetailedView object.
	 */
	public LRDetailedView getDetailedView() {
		selenium.click("ui=course::toolbox_generalTools_detailedView()");
		selenium.waitForPageToLoad("30000");
		return new LRDetailedView(selenium);
	}
		
	
	public void setBookmark() {
		selenium.click("ui=course::toolbox_generalTools_setBookmark()");
		selenium.waitForPageToLoad("30000");			
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
	}
		
	
	/**
	 * Open group management from course run.
	 * @return Returns a GroupManagement object.
	 */
	public GroupManagement getGroupManagement() {
		selenium.click("ui=course::toolbox_courseTools_groupManagement()");
		selenium.waitForPageToLoad("30000");	
		return new GroupManagement(selenium);
	}
	
 	/**
	 * Open rights management from course run
	 * @return Returns a Rightsmanagement object
	 * 
	 * @author Thomas Linowsky
	 */
	public RightsManagement getRightsManagement(){
		selenium.click("ui=course::toolbox_courseTools_rightsManagement()");
		selenium.waitForPageToLoad("30000");
		return new RightsManagement(selenium);
	}
	
	/**
	 * TODO: LD: is this the right place for this method ???
	 * @param groupName
	 * @param userName
	 */
	public void removeFromTutoredGroup(String groupName, String userName) {
		selenium.click("ui=course::toolbox_myGroupsTools_tutoredGroups(nameOfGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=group::menu_administration()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=group::content_members_tabMembers()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_checkUsername(nameOfUser=" + userName + ")");
		selenium.click("ui=commons::usertable_participants_remove()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_adduser_finish()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=dialog::Yes()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @param groupName
	 * @return
	 */
	public boolean hasTutoredGroup(String groupName) {
		if(selenium.isElementPresent("ui=course::toolbox_myGroupsTools_tutoredGroups(nameOfGroup=" + groupName + ")")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if it has a group with this groupName in "My learning groups".
	 * @param groupName
	 * @return
	 */
	public boolean hasMyGroup(String groupName) {
	  if(selenium.isElementPresent("ui=course::toolbox_myGroupsTools_myLearningGroups(nameOfGroup=" + groupName + ")")) {
      return true;
    }
    return false;
	}
	
	/**
	 * 
	 * @return Returns an AssessmentTool instance
	 */
	public AssessmentTool getAssessmentTool() {
		selenium.click("ui=course::toolbox_courseTools_assessmentTool()");
		selenium.waitForPageToLoad("30000");
		return new AssessmentTool(selenium);
	}
	
	/**
	 * Selects a test element with the given title.
	 * @param title
	 * @return
	 */
	public TestElement selectTest(String title) {
		selectCourseElement(title);
		if(selenium.isElementPresent("ui=commons::start()") || selenium.isTextPresent("There are no more attempts at your disposal.")) {
			return new TestElement(selenium);
		}
		throw new IllegalStateException("This is not the - Test Element - page");
	}	
	
	public QuestionnaireElement selectQuestionnaire(String title) {
		selectCourseElement(title);
		return new QuestionnaireElement(selenium);
	}
	
	public FileDialog selectFileDialog(String title) {
		selectCourseElement(title);
		//TODO: LD: check if this a FileDialog element!!!
		return new FileDialog(selenium); 
	}
	
	public Folder selectFolder(String title) {
		selectCourseElement(title);
		//TODO: LD: check if this a Folder element!!!
		return new Folder(selenium);
	}
	
	public LTIRun selectLTI(String title) {
	  selectCourseElement(title);
	  return new LTIRun(selenium);
	}
	
	public BlogRun selectBlog(String title) {
	  selectCourseElement(title);
	  return new BlogRun(selenium);
	}
	
	public PodcastRun selectPodcast(String title) {
    selectCourseElement(title);
    return new PodcastRun(selenium);
  }
	
    public InfoMessageRun selectInfoMessage(String title) {
        selectCourseElement(title);
        return new InfoMessageRun(selenium);
    }
	
}
