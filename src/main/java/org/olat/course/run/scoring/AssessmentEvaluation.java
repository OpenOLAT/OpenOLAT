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

import org.olat.core.id.Identity;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.model.AssessmentRunStatus;

/**
 * 
 * Initial date: 26.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEvaluation extends ScoreEvaluation {
	
	public static final AssessmentEvaluation EMPTY_EVAL = new AssessmentEvaluation((Float)null, (Boolean)null);
	
	private final Date startDate;
	private final Date endDate;
	private final Date endDateOriginal;
	private final Date endDateModificationDate;
	private final Identity endDateModificationIdentity;
	private final Integer duration;
	private final AssessmentObligation obligation;
	private final Double completion;
	private final Integer attempts;
	private final String comment;
	private final String coachComment;
	private final int numOfAssessmentDocs;
	
	private final Date lastModified;
	private final Date lastUserModified;
	private final Date lastCoachModified;
	private final Date assessmentDone;
	private final Boolean fullyAssessed;
	private final Date fullyAssessedDate;
	private final Date firstVisit;
	private final Date lastVisit;

	public AssessmentEvaluation(Float score, Boolean passed) {
		this(score, passed, null, null);
	}

	public AssessmentEvaluation(final Float score, final Boolean passed, final Boolean fullyAssessed) {
		this(score, passed, fullyAssessed, null);
	}

	public AssessmentEvaluation(Float score, Boolean passed, Boolean fullyAssessed, Long assessmentID) {
		this(score, passed, null, null, null, null, fullyAssessed, null, null, null, assessmentID, null, null, -1, null,
				null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public AssessmentEvaluation(Date lastModified, Date lastUserModified, Date lastCoachModified) {
		this(null, null, null, null, null, null, null, null, null, null, null, null, null, -1, lastModified,
				lastUserModified, lastCoachModified, null, null, null, null, null, null, null, null, null, null);
	}

	public AssessmentEvaluation(Float score, Boolean passed, Integer attempts, Double completion,
			AssessmentEntryStatus assessmentStatus, Boolean userVisibility, Boolean fullyAssessed,
			Date fullyAssessedDate, Double currentRunCompletion, AssessmentRunStatus runStatus, Long assessmentID,
			String comment, String coachComment, int numOfAssessmentDocs, Date lastModified, Date lastUserModified,
			Date lastCoachModified, Date assessmentDone, Date startDate, Date endDate, Date endDateOriginal,
			Date endDateModificationDate, Identity endDateModificationIdentity, AssessmentObligation obligation,
			Integer duration, Date firstVisit, Date lastVisit) {
		super(score, passed, assessmentStatus, userVisibility, currentRunCompletion, runStatus, assessmentID);
		this.attempts = attempts;
		this.completion = completion;
		this.comment = comment;
		this.coachComment = coachComment;
		this.numOfAssessmentDocs = numOfAssessmentDocs;
		this.lastModified = lastModified;
		this.lastUserModified = lastUserModified;
		this.lastCoachModified = lastCoachModified;
		this.assessmentDone = assessmentDone;
		this.fullyAssessed = fullyAssessed;
		this.fullyAssessedDate = fullyAssessedDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.endDateOriginal = endDateOriginal;
		this.endDateModificationDate = endDateModificationDate;
		this.endDateModificationIdentity = endDateModificationIdentity;
		this.obligation = obligation;
		this.duration = duration;
		this.firstVisit = firstVisit;
		this.lastVisit = lastVisit;
	}
	
	/**
	 * Utility constructor to update only the status of the evaluation
	 * 
	 * @param eval
	 * @param assessmentStatus
	 */
	public AssessmentEvaluation(AssessmentEvaluation eval, AssessmentEntryStatus assessmentStatus) {
		this(eval.getScore(), eval.getPassed(), eval.getAttempts(), null, assessmentStatus, eval.getUserVisible(),
				eval.getFullyAssessed(), eval.getFullyAssessedDate(), eval.getCurrentRunCompletion(),
				eval.getCurrentRunStatus(), eval.getAssessmentID(), eval.getComment(), eval.getCoachComment(), -1,
				eval.getLastModified(), eval.getLastUserModified(), eval.getLastCoachModified(),
				eval.getAssessmentDone(), eval.getStartDate(), eval.getEndDate(), eval.getEndDateOriginal(),
				eval.getEndDateModificationDate(), eval.getEndDateModificationIdentity(), eval.getObligation(),
				eval.getDuration(), eval.getFirstVisit(), eval.getLastVisit());
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public Date getEndDateOriginal() {
		return endDateOriginal;
	}

	public Date getEndDateModificationDate() {
		return endDateModificationDate;
	}

	public Identity getEndDateModificationIdentity() {
		return endDateModificationIdentity;
	}

	public Integer getDuration() {
		return duration;
	}

	public AssessmentObligation getObligation() {
		return obligation;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public Double getCompletion() {
		return completion;
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
	
	public Boolean getFullyAssessed() {
		return fullyAssessed;
	}

	public Date getFullyAssessedDate() {
		return fullyAssessedDate;
	}

	public Date getAssessmentDone() {
		return assessmentDone;
	}

	public Date getFirstVisit() {
		return firstVisit;
	}

	public Date getLastVisit() {
		return lastVisit;
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
		
		Double completion = null;
		Double currentRunCompletion = null;
		AssessmentRunStatus runStatus = null;
		if(assessmentConfig.getCompletionMode() != null && !Mode.none.equals(assessmentConfig.getCompletionMode())) {
			completion = entry.getCompletion();
			currentRunCompletion = entry.getCurrentRunCompletion();
			runStatus = entry.getCurrentRunStatus();
		}
		
		return new AssessmentEvaluation(score, passed, attempts, completion, entry.getAssessmentStatus(),
				entry.getUserVisibility(), entry.getFullyAssessed(), entry.getFullyAssessedDate(), currentRunCompletion,
				runStatus, entry.getAssessmentId(), comment, entry.getCoachComment(),
				entry.getNumberOfAssessmentDocuments(), entry.getLastModified(), entry.getLastUserModified(),
				entry.getLastCoachModified(), entry.getAssessmentDone(), entry.getStartDate(), entry.getEndDate(),
				entry.getEndDateOriginal(), entry.getEndDateModificationDate(), entry.getEndDateModificationIdentity(),
				entry.getObligation(), entry.getDuration(), entry.getFirstVisit(), entry.getLastVisit());
	}
}