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
*/

package org.olat.course.nodes;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;


/**
 * Initial Date:  Jun 18, 2004
 * @author gnaegi
 *
 * Comment: 
 * All course nodes that are of an assessement type must implement this 
 * interface so that the assessment results can be managed by the assessment
 * tool.
 */
public interface AssessableCourseNode extends CourseNode {
	
	/**
	 * 
	 * @return
	 */
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
	 * value means that no cut value is definied and therefor the node can be passed having any 
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
	 * @return the user comment for this user for this node, given by coach
	 */
	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * @param userCourseEnvironment The course environment of the assessed user.
	 * @return The list of assessment document associated with this user and course element.
	 */
	public List<File> getIndividualAssessmentDocuments(UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * @param userCourseEnvironment
	 * @return The coach comment for this user for this node (not visible to user)
	 */
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment);
	/**
	 * @param userCourseEnvironment
	 * @return the users log of this node
	 */
	public String getUserLog(UserCourseEnvironment userCourseEnvironment);
	/**
	 * @param userCourseEnvironment
	 * @return the users attempts of this node
	 */
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * 
	 * @param userCourseEnvironment
	 * @return The completion of its current task before being committed and official.
	 */
	public Double getUserCurrentRunCompletion(UserCourseEnvironment userCourseEnvironment);
	
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
	
	/**
	 * 
	 * @param scoreEvaluation if scoreEvaluation.getScore() != null, then the score will be updated, and/or if scoreEvaluation.getPassed() != null, then 'passed' will be updated
	 * @param userCourseEnvironment
	 * @param coachingIdentity
	 */
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, boolean incrementAttempts, Role doneBy);
	/**
	 * Updates the user comment for this node and this user. This comment is visible to the user.
	 * @param userComment
	 * @param userCourseEnvironment
	 * @param coachingIdentity
	 */
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity);
	
	/**
	 * Add if allowed a document for the assessed user.
	 * 
	 * @param document The document
	 * @param userCourseEnvironment The course environment of the assessed user
	 * @param coachingIdentity The coach who upload the document
	 */
	public void addIndividualAssessmentDocument(File document, String filename, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity);
	
	/**
	 * Remove a document
	 * 
	 * @param document The document to remove
	 * @param userCourseEnvironment The course environment of the assessed user
	 * @param coachingIdentity The coach who delete the document
	 */
	public void removeIndividualAssessmentDocument(File document, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity);
	
	/**
	 * Increments the users attempts for this node and this user + 1. 
	 * @param userCourseEnvironment
	 */
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment, Role doneBy);
	/**
	 * Updates the users attempts for this node and this user. 
	 * @param userAttempts
	 * @param userCourseEnvironment
	 * @param coachingIdentity
	 */
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role doneBy);
	
	/**
	 * 
	 * @param userCourseEnvironment The user course environment of the assessed identity
	 * @param identity The identity which do the action
	 * @param doneBy The role of the identity which do the action
	 */
	public void updateLastModifications(UserCourseEnvironment userCourseEnvironment, Identity identity, Role doneBy);
	
	/**
	 * 
	 * @param userCourseEnvironment The user course environment of the assessed identity
	 * @param identity The identity which do the action
	 * @param currentCompletion The completion of the current running task
	 * @param status The status of the current running task
	 * @param doneBy The role of the identity which do the action
	 */
	public void updateCurrentCompletion(UserCourseEnvironment userCourseEnvironment, Identity identity,
			Double currentCompletion, AssessmentRunStatus status, Role doneBy);
	
	/**
	 * Updates the coach comment for this node and this user. This comment is not visible to the user.
	 * @param coachComment
	 * @param userCourseEnvironment
	 */
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment);

	/**
	 * @return True if this course node produces an status variable for the learner
	 */
	public boolean hasStatusConfigured();

}
