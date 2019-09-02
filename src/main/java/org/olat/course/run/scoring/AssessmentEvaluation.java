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
package org.olat.course.run.scoring;

import java.util.Date;

import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;

/**
 * 
 * Initial date: 26.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEvaluation extends ScoreEvaluation {
	
	public static final AssessmentEvaluation EMPTY_EVAL = new AssessmentEvaluation((Float)null, (Boolean)null);
	
	private final Integer attempts;
	private final String comment;
	private final String coachComment;
	private final int numOfAssessmentDocs;
	
	private final Date lastModified;
	private final Date lastUserModified;
	private final Date lastCoachModified;
	private final Date assessmentDone;

	public AssessmentEvaluation(Float score, Boolean passed) {
		this(score, passed, null, null);
	}
	
	public AssessmentEvaluation(final Float score, final Boolean passed, final Boolean fullyAssessed) {
		this(score, passed, fullyAssessed, null);
	}
	
	public AssessmentEvaluation(Float score, Boolean passed, Boolean fullyAssessed, Long assessmentID) {
		this(score, passed, null, null, null, fullyAssessed, null, null, assessmentID, null, null, -1, null, null, null, null);
	}
	
	public AssessmentEvaluation(Date lastModified, Date lastUserModified, Date lastCoachModified) {
		this(null, null, null, null, null, null, null, null, null, null, null, -1, lastModified, lastUserModified, lastCoachModified, null);
	}
	
	public AssessmentEvaluation(Float score, Boolean passed, Integer attempts, AssessmentEntryStatus assessmentStatus, Boolean userVisibility,
			Boolean fullyAssessed, Double currentRunCompletion, AssessmentRunStatus runStatus, Long assessmentID,
			String comment, String coachComment, int numOfAssessmentDocs,
			Date lastModified, Date lastUserModified, Date lastCoachModified, Date assessmentDone) {
		super(score, passed, assessmentStatus, userVisibility, fullyAssessed, currentRunCompletion, runStatus, assessmentID);
		this.attempts = attempts;
		this.comment = comment;
		this.coachComment = coachComment;
		this.numOfAssessmentDocs = numOfAssessmentDocs;
		this.lastModified = lastModified;
		this.lastUserModified = lastUserModified;
		this.lastCoachModified = lastCoachModified;
		this.assessmentDone = assessmentDone;
	}
	
	/**
	 * Utility constructor to update only the status of the evaluation
	 * 
	 * @param eval
	 * @param assessmentStatus
	 */
	public AssessmentEvaluation(AssessmentEvaluation eval, AssessmentEntryStatus assessmentStatus) {
		this(eval.getScore(), eval.getPassed(), eval.getAttempts(), assessmentStatus, eval.getUserVisible(),
				eval.getFullyAssessed(), eval.getCurrentRunCompletion(), eval.getCurrentRunStatus(),  eval.getAssessmentID(),
				eval.getComment(), eval.getCoachComment(), -1,
				eval.getLastModified(), eval.getLastUserModified(), eval.getLastCoachModified(), eval.getAssessmentDone());
	}

	public Integer getAttempts() {
		return attempts;
	}

	public String getComment() {
		return comment;
	}

	public String getCoachComment() {
		return coachComment;
	}
	
	public int getNumOfAssessmentDocs() {
		return numOfAssessmentDocs;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public Date getLastUserModified() {
		return lastUserModified;
	}

	public Date getLastCoachModified() {
		return lastCoachModified;
	}

	public Date getAssessmentDone() {
		return assessmentDone;
	}

	public static final AssessmentEvaluation toAssessmentEvaluation(AssessmentEntry entry, AssessmentConfig assessmentConfig) {
		if(entry == null) {
			return AssessmentEvaluation.EMPTY_EVAL;
		}
		
		Integer attempts = null;
		if(assessmentConfig.hasAttempts()) {
			attempts = entry.getAttempts();
		}
		
		Float score = null;
		if(assessmentConfig.hasScore()) {
			score = entry.getScore() == null ? null : entry.getScore().floatValue();
		}
		
		Boolean passed = null;
		if(assessmentConfig.hasPassed()) {
			passed = entry.getPassed();
		}
		
		String comment = null;
		if(assessmentConfig.hasComment()) {
			comment = entry.getComment();
		}
		
		Double currentRunCompletion = null;
		AssessmentRunStatus runStatus = null;
		if(assessmentConfig.hasCompletion()) {
			currentRunCompletion = entry.getCurrentRunCompletion();
			runStatus = entry.getCurrentRunStatus();
		}
		
		return new AssessmentEvaluation(score, passed, attempts, entry.getAssessmentStatus(), entry.getUserVisibility(),
				entry.getFullyAssessed(), currentRunCompletion, runStatus, entry.getAssessmentId(),
				comment, entry.getCoachComment(), entry.getNumberOfAssessmentDocuments(),
				entry.getLastModified(), entry.getLastUserModified(), entry.getLastCoachModified(), entry.getAssessmentDone());
	}
}