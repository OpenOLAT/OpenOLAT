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
package org.olat.course.assessment;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentHandler {
	
	public boolean isAssessedBusinessGroups();
	
	/**
	 * @return Returns the maximal score that can be achieved on this node. Throws 
	 * an OLATRuntimeException if hasScore set to false, maxScore is undefined in this case
	 */
	public Float getMaxScoreConfiguration();

	/**
	 * @return Returns the minimal score that can be achieved on this node. Throws 
	 * an OLATRuntimeException if hasScore set to false, maxScore is undefined in this case
	 */
	public Float getMinScoreConfiguration();

	/**
	 * @return Returns the passed cut value or null if no such value is defined. A null
	 * value means that no cut value is defined and therefore the node can be passed having any 
	 * score or no score at all. Throws an OLATRuntimeException if hasPassed is set to false, 
	 * cutValue is undefined in this case
	 */
	public Float getCutValueConfiguration();
	
	/**
	 * @return True if this course node produces a score variable for the learner
	 */
	public boolean hasScoreConfigured();
	
	/**
	 * @return True if this course node produces a passed variable for the learner
	 */
	public boolean hasPassedConfigured();
	
	/**
	 * @return True if this course node produces a comment variable for the learner
	 */
	public boolean hasCommentConfigured();
	
	/**
	 * @return True if this course node produces an attempts variable for the learner
	 */
	public boolean hasAttemptsConfigured();
	
	/**
	 * 
	 * @return True if this course node can hold some documents about the assessment of the learner
	 */
	public boolean hasIndividualAsssessmentDocuments();
	
	
	/**
	 * @return True if this course node can produces a completion variable for the learner
	 */
	public boolean hasCompletion();
	
	/**
	 * @return True if this course node has additional details to be edited / viewed
	 */
	public boolean hasDetails();

	/**
	 * @return True if score, passed, attempts and comment are editable by the assessment tool
	 */
	public boolean isEditableConfigured();
	
	/**
	 * @return True if this course node has additional result details.
	 */
	public boolean hasResultsDetails();
	
	/**
	 * @return True if this course node produces an status variable for the learner
	 */
	public boolean hasStatusConfigured();

	
	/**
	 * this method implementation must not cache any results!
	 * 
	 * The user has no scoring results jet (e.g. made no test yet), then the
	 * ScoreEvaluation.NA has to be returned!
	 * @param userCourseEnv
	 * @return null, if this node cannot deliver any useful scoring info (this is not the case for a test never tried or manual scoring: those have default values 0.0f / false for score/passed; currently only the STNode returns null if there are no scoring rules defined.)
	 */
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv);
	
	
	/**
	 * @param userCourseEnvironment
	 * @return the details view for this node and this user. will be displayed in 
	 * the user list. if hasDetails= false this returns null
	 */	
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment);
	/**
	 * @return the details list view header key that is used to label the table row
	 */	
	public String getDetailsListViewHeaderKey();
	/**
	 * Returns a controller to edit the node specific details
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnvironment
	 * @return a controller or null if hasDetails=false
	 */
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnvironment);
	
	public Controller getResultDetailsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv);
	
	/**
	 * Returns the controller with the list of assessed identities for
	 * a specific course node.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param courseEntry
	 * @param group
	 * @param coachCourseEnv
	 * @param toolContainer
	 * @param assessmentCallback
	 * @return
	 */
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback);


}
