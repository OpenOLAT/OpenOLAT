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
package org.olat.course.assessment.handler;

/**
 * 
 * Initial date: 18 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentConfig {
	
	public enum Mode {
		none,
		setByNode,
		evaluated
	}
	
	/**
	 * Real assessments are in efficiency statements and are shown in the assessment tool.
	 * 
	 * @return true if it is a real assessment.
	 */
	public boolean isAssessable();
	
	/**
	 * @return if this course node should be ignored in the cumulative assessment of
	 *         the course root node.
	 */
	public boolean ignoreInCourseAssessment();
	
	public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment);
	
	/**
	 * @return if this course node produces a score variable for the learner
	 */
	public Mode getScoreMode();
	
	/**
	 * @return Returns the maximal score that can be achieved on this node. Return
	 *         NULL if ScoreMode set to Mode.none, maxScore is undefined in this case
	 */
	public Float getMaxScore();

	/**
	 * @return Returns the minimal score that can be achieved on this node. Return
	 *         NULL if ScoreMode set to Mode.none, minScore is undefined in this case
	 */
	public Float getMinScore();
	
	/**
	 * @return if this course node produces a grade for the learner
	 */
	public boolean hasGrade();
	
	/**
	 * @return true if the grade is set when the score is set of if the coach has to
	 *         set the grade manually,
	 */
	public boolean isAutoGrade();
	
	/**
	 * @return if this course node produces a passed variable for the learner
	 */
	public Mode getPassedMode();

	/**
	 * @return Returns the passed cut value or null if no such value is defined. A null
	 * value means that no cut value is defined and therefore the node can be passed having any 
	 * score or no score at all. Throws an OLATRuntimeException if hasPassed is set to false, 
	 * cutValue is undefined in this case
	 */
	public Float getCutValue();
	
	/**
	 * @return if the produced passed can be overriden by the coach
	 */
	public boolean isPassedOverridable();
	
	/**
	 * @param done Is the assessment done?
	 * @param coachCanNotEdit Can the coach edit the user visibility?
	 * @return Returns the initial user visibility.
	 */
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit);
	
	/**
	 * @return if this course node can produces a completion variable for the learner
	 */
	public Mode getCompletionMode();
	
	/**
	 * @return True if this course node produces an attempts variable for the learner
	 */
	public boolean hasAttempts();
	
	public boolean hasMaxAttempts();
	
	public Integer getMaxAttempts();

	
	/**
	 * @return True if this course node produces a comment variable for the learner
	 */
	public boolean hasComment();
	
	/**
	 * @return True if this course node can hold some documents about the assessment of the learner
	 */
	public boolean hasIndividualAsssessmentDocuments();
	
	/**
	 * @return True if this course node produces an status variable for the learner
	 */
	public boolean hasStatus();
	
	public boolean isAssessedBusinessGroups();

	/**
	 * @return True if score, passed, attempts and comment are editable by the assessment tool
	 */
	public boolean isEditable();
	
	/**
	 * @return true if the data are editable in the bulk controller
	 */
	public boolean isBulkEditable();
	
	/**
	 * @return True if this course node has additional details to be edited.
	 */
	public boolean hasEditableDetails();
	
	/**
	 * @return True if this course node will be graded by external graders and not the coaches.
	 */
	public boolean isExternalGrading();
	
	public boolean isObligationOverridable();

}
