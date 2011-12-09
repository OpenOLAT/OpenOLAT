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

package org.olat.test.functional.portfolio;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;
import org.olat.test.util.selenium.olatapi.course.run.StructureElement;
import org.olat.test.util.selenium.olatapi.portfolio.EPArtefactSearch;
import org.olat.test.util.selenium.olatapi.portfolio.EPCollectWizard;
import org.olat.test.util.selenium.olatapi.portfolio.EPExtensions;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * ePortfolio: testing 
 * - artefact collection 
 * - artefact creation
 * - filtering with search
 * - deleting artefacts
 * 
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class CollectOrCreateArtefactTest extends BaseSeleneseTestCase {

	private static final String FORUM_SUBJECT = "forum artefact test";

	public void setUp() throws Exception {		
		Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);				
	}
	
	/**
	 * - creates a forum post in the demo course
	 * - collect it as an artefact using the wizard
	 * - check that the artefact exists
	 * @throws Exception
	 */
	@Test
	public void testCollectForumPost() throws Exception {
    //cleanup first
    cleanUpArtefacts();
    
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
				
		CourseRun courseRun = workflow.getLearningResources().searchAndShowCourseContent(Context.DEMO_COURSE_NAME_1);
		courseRun.selectAnyButGetToRoot("Activation Interaction");
		Forum forum = courseRun.selectForum("Forum");
		forum.openNewTopic(FORUM_SUBJECT, "Forum Message to collect as artefact");
		courseRun.selectForum("Forum").viewTopic(FORUM_SUBJECT);
		
		EPCollectWizard epwizz = forum.collectAsArtefact();
		assertNotNull("forum post seems not to be collectable or ePortfolio is turned off", epwizz);
		epwizz.filloutWizardForOLATSource(FORUM_SUBJECT, "a test to collect a forum artefact");
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		assertEquals(1, epExt.getTotalArtefactAmount());
		workflow.logout();
	}
	
	private void cleanUpArtefacts() throws Exception {
	  OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
    
	  //cleanup first
	    EPExtensions epExt = workflow.getHome().getEPortfolio();
	    while(epExt.hasArtefact()) {
	      epExt.deleteFirstArtefact();
	      Thread.sleep(3000);
	    }
	}
	
	/**
	 * - create a new text-artefact
	 * - search for it by artefact-type 
	 * not functional in web 1.0 mode
	 */
	@Test(dependsOnMethods ={"testCollectForumPost"})
	public void testCreateTextArtefact() {
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		EPCollectWizard epWizz = epExt.createTextArtefactAndOpenWizard();
		epWizz.filloutWizardForTextArtefact("a great content for an artefact...", "text artefact", "how great it is");
		// filter for textartefact and get amount
		EPArtefactSearch epSearch = epExt.getArtefactSearch();
		epSearch.toggleTextArtefactFilter();
		assertEquals(1, epExt.getArtefactAmount());
		workflow.logout();
	}
	
	/**
	 * - deletes 2 artefacts (created before)
	 * - check that no more artefacts exist
	 */
	@Test(dependsOnMethods ={"testCreateTextArtefact"})
	public void testDeleteArtefacts(){
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos());
		
		EPExtensions epExt = workflow.getHome().getEPortfolio();
		assertEquals(2, epExt.getTotalArtefactAmount());
		epExt.deleteFirstArtefact();
		epExt.deleteFirstArtefact();
		assertEquals(0, epExt.getTotalArtefactAmount());
		workflow.logout();
	}
	
}
