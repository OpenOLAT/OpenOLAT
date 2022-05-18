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

import java.io.File;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeOverviewController;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AccountingEvaluators;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 20 Aug 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CourseAssessmentService {
	
	public AssessmentConfig getAssessmentConfig(RepositoryEntryRef courseEntry, CourseNode courseNode);
	
	public AccountingEvaluators getEvaluators(CourseNode courseNode, CourseConfig courseConfig);

	
	/**
	 * Returns the persisted AssessmentEntry of the user. Check
	 * AssessmentConfig.isEvaluationPersisted() before invoking this method.
	 *
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return
	 */
	public AssessmentEntry getAssessmentEntry(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * Converts the assessmentEntry to an AssessmentEvaluation in respect of the
	 * AssessmentConfig. If the assessmentEntry is null, the method returns
	 * AssessmentEvaluation.EMPTY_EVAL.
	 * 
	 * @param assessmentEntry
	 * @param assessmentConfig
	 * 
	 * @return
	 */
	public AssessmentEvaluation toAssessmentEvaluation(AssessmentEntry assessmentEntry, AssessmentConfig assessmentConfig);
	
	/**
	 * Converts the assessmentEntry to an AssessmentEvaluation in respect of the
	 * AssessmentConfig of the courseNode. If the assessmentEntry is null, the
	 * method returns AssessmentEvaluation.EMPTY_EVAL.
	 * 
	 * @param assessmentEntry
	 * @param courseNode
	 * 
	 * @return
	 */
	public AssessmentEvaluation toAssessmentEvaluation(AssessmentEntry assessmentEntry, CourseNode courseNode);
	
	/**
	 * This method implementation must not cache any results!
	 * 
	 * The user has no scoring results yet (e.g. made no test yet), then the
	 * ScoreEvaluation.NA has to be returned!
	 * 
	 * @param courseNode
	 * @param userCourseEnviornment
	 * @return null, if this node cannot deliver any useful scoring info (this is
	 *         not the case for a test never tried or manual scoring: those have
	 *         default values 0.0f / false for score/passed; currently only the
	 *         STNode returns null, if there are no scoring rules defined.
	 */
	public AssessmentEvaluation getAssessmentEvaluation(CourseNode courseNode, UserCourseEnvironment userCourseEnviornment);
	
	public void updateScoreEvaluation(CourseNode courseNode, ScoreEvaluation scoreEvaluation,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, boolean incrementAttempts, Role by);
	
	/**
	 * 
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return The completion of its current task before being committed and
	 *         official.
	 */
	public Double getCurrentRunCompletion(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);

	public void updateCurrentCompletion(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Date start, Double currentCompletion, AssessmentRunStatus status, Role by);
	
	public void updateCompletion(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Double completion, AssessmentEntryStatus status, Role by);
	
	public void updateFullyAssessed(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Boolean fullyAssessed, AssessmentEntryStatus status);
	
	/**
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return the users attempts of the node
	 */
	public Integer getAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);

	/**
	 * Increments the users attempts for this node and this user.
	 * 
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @param doneBy
	 */
	public void incrementAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Role doneBy);

	/**
	 * Updates the users attempts for this node and this user.
	 * 
	 * @param courseNode
	 * @param userAttempts
	 * @param lastAttempt 
	 * @param userCourseEnvironment
	 * @param coachingIdentity
	 * @param doneBy
	 */
	public void updateAttempts(CourseNode courseNode, Integer userAttempts,
			Date lastAttempt, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role doneBy);
	
	/**
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return the user comment for this user for this node, given by coach
	 */
	public String getUserComment(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);

	/**
	 * Updates the user comment for this node and this user. This comment is visible
	 * to the user.
	 * 
	 * @param courseNode
	 * @param userComment
	 * @param userCourseEnvironment
	 * @param coachingIdentity
	 */
	public void updatedUserComment(CourseNode courseNode, String userComment,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity);

	/**
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return The coach comment for this user for this node (not visible to user)
	 */
	public String getCoachComment(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);

	/**
	 * Updates the coach comment for this node and this user. This comment is not
	 * visible to the user.
	 * 
	 * @param courseNode
	 * @param coachComment
	 * @param userCourseEnvironment
	 */
	public void updateCoachComment(CourseNode courseNode, String coachComment,
			UserCourseEnvironment userCourseEnvironment);

	/**
	 * @param courseNode
	 * @param userCourseEnvironment The course environment of the assessed user.
	 * @return The list of assessment document associated with this user and course
	 *         element.
	 */
	public List<File> getIndividualAssessmentDocuments(CourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment);

	/**
	 * Add a document for the assessed user, if allowed.
	 * 
	 * @param courseNode
	 * @param document              The document
	 * @param userCourseEnvironment The course environment of the assessed user
	 * @param coachingIdentity      The coach who upload the document
	 */
	public void addIndividualAssessmentDocument(CourseNode courseNode, File document, String filename,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity);

	/**
	 * Remove a document.
	 * 
	 * @param courseNode
	 * @param document              The document to remove
	 * @param userCourseEnvironment The course environment of the assessed user
	 * @param coachingIdentity      The coach who delete the document
	 */
	public void removeIndividualAssessmentDocument(CourseNode courseNode, File document,
			UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity);
	
	/**
	 * 
	 * @param courseNode
	 * @param userCourseEnvironment The user course environment of the assessed
	 *                              identity
	 * @param identity              The identity which do the action
	 * @param doneBy                The role of the identity which do the action
	 */
	public void updateLastModifications(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Identity identity, Role doneBy);

	/**
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return the users log of this node
	 */
	public String getAuditLog(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * Save the users achieved ScoreEvaluation for this node. If there is already a
	 * score property available, it will be overwritten with the new value.
	 * 
	 * @param courseNode            The course element
	 * @param identity              The identity who make the changes
	 * @param scoreEvaluation       The updated score evaluation
	 * @param userCourseEnvironment The user course env. of the assessed identity
	 * @param incrementUserAttempts
	 */
	public void saveScoreEvaluation(CourseNode courseNode, Identity identity, ScoreEvaluation scoreEvaluation,
			UserCourseEnvironment userCourseEnvironment, boolean incrementUserAttempts, Role by);
	
	public Overridable<Boolean> getRootPassed(UserCourseEnvironment userCourseEnvironment);

	public Overridable<Boolean> overrideRootPassed(Identity coach, UserCourseEnvironment userCourseEnvironment,
			Boolean passed);

	public Overridable<Boolean> resetRootPassed(Identity coach, UserCourseEnvironment userCourseEnvironment);
	
	/**
	 * Returns a controller to edit the node specific details. Check
	 * AssessmentConfig.hasEditableDetails() before invoking this method.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseNode
	 * @param userCourseEnvironment
	 * @return a controller or null
	 */
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseNode courseNode, UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnvironment);
	
	/**
	 * Returns the controller with the list of assessed identities for a specific
	 * course node.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param courseNode
	 * @param courseEntry
	 * @param group
	 * @param coachCourseEnv
	 * @param toolContainer
	 * @param assessmentCallback
	 * @param showTitle 
	 * @return
	 */
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback, boolean showTitle);
	
	/**
	 * This is a a convenience method to create the AssessmentCourseNodeController inside the CourseNode run controller.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param courseNode
	 * @param courseEntry
	 * @param coachCourseEnv
	 * @param toolContainer
	 * 
	 * @return
	 */
	public AssessmentCourseNodeController getCourseNodeRunController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, UserCourseEnvironment coachCourseEnv);
	
	public AssessmentCourseNodeOverviewController getCourseNodeOverviewController(UserRequest ureq,
			WindowControl wControl, CourseNode courseNode, UserCourseEnvironment coachCourseEnv, boolean courseInfoLaunch, boolean readOnly);
	
	public ScoreAccountingTrigger createScoreAccountingTrigger(RepositoryEntry entry, String subIdent,
			ScoreAccountingTriggerData data);

	public void deleteScoreAccountingTriggers(List<ScoreAccountingTrigger> scoreAccountingTrigger);

	public void deleteScoreAccountingTriggers(RepositoryEntry entry);
	
	public List<ScoreAccountingTrigger> getScoreAccountingTriggers(RepositoryEntryRef entryRef);

	public List<RepositoryEntry> getTriggeredCourseEntries(ScoreAccountingTriggerSearchParams searchParams);
	
	/**
	 * Recalculates all AssessmentEvaluation of all users in the course.
	 *
	 * @param course
	 * @param update
	 */
	public void evaluateAll(ICourse course, boolean update);
	
	/**
	 * Recalculates all AssessmentEvaluation of all users in the course asynchronously.
	 *
	 * @param courseResId
	 * @param update 
	 */
	public void evaluateAllAsync(Long courseResId, boolean update);
	
	/**
	 * Evaluates the all assessment entries of a repository entry / user when the
	 * start date of a sub assessment entry is after the start.
	 * 
	 * @param start
	 */
	public void evaluateStartOver(Date start);

	/**
	 * Evaluate the assessment entries of users without a passed root value in
	 * learning path courses where the lifecycle is over.
	 */
	public void evaluateLifecycleOver(Date now);

}
