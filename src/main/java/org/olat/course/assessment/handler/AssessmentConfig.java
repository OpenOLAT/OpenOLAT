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
	
	/**
	 * Real assessments are in efficiency statements and are shown in the assessment tool.
	 * 
	 * @return true if it is a real assessment.
	 */
	public boolean isAssessable();
	
	/**
	 * @return true if the score evaluation is persisted
	 */
	public boolean isScoreEvaluationPersisted();
	
	/**
	 * @return true if the score evaluation is calculated e.g as sum of other assessments.
	 */
	public boolean isScoreEvaluationCalculated();
	
	/**
	 * @return True if this course node produces a score variable for the learner
	 */
	public boolean hasScore();
	
	/**
	 * @return Returns the maximal score that can be achieved on this node. Throws 
	 * an OLATRuntimeException if hasScore set to false, maxScore is undefined in this case
	 */
	public Float getMaxScore();

	/**
	 * @return Returns the minimal score that can be achieved on this node. Throws 
	 * an OLATRuntimeException if hasScore set to false, maxScore is undefined in this case
	 */
	public Float getMinScore();
	
	/**
	 * @return True if this course node produces a passed variable for the learner
	 */
	public boolean hasPassed();

	/**
	 * @return Returns the passed cut value or null if no such value is defined. A null
	 * value means that no cut value is defined and therefore the node can be passed having any 
	 * score or no score at all. Throws an OLATRuntimeException if hasPassed is set to false, 
	 * cutValue is undefined in this case
	 */
	public Float getCutValue();
	
	/**
	 * @return True if this course node can produces a completion variable for the learner
	 */
	public boolean hasCompletion();
	
	/**
	 * @return True if this course node produces an attempts variable for the learner
	 */
	public boolean hasAttempts();
	
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
	 * @return True if this course node has additional details to be edited.
	 */
	public boolean hasEditableDetails();

}
