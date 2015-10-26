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

import org.olat.course.nodes.AssessableCourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 26.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEvaluation extends ScoreEvaluation {
	
	public static final AssessmentEvaluation EMPTY_EVAL = new AssessmentEvaluation(null, null);
	
	private final Integer attempts;
	private final String comment;
	private final String coachComment;

	public AssessmentEvaluation(Float score, Boolean passed) {
		this(score, passed, null, null);
	}
	
	public AssessmentEvaluation(final Float score, final Boolean passed, final Boolean fullyAssessed) {
		this(score, passed, fullyAssessed, null);
	}
	
	public AssessmentEvaluation(Float score, Boolean passed, Boolean fullyAssessed, Long assessmentID) {
		this(score, passed, null, fullyAssessed, assessmentID);
	}
	
	public AssessmentEvaluation(Float score, Boolean passed, AssessmentEntryStatus assessmentStatus, Boolean fullyAssessed, Long assessmentID) {
		this(score, passed, null, assessmentStatus, fullyAssessed, assessmentID, null, null);
	}
	
	public AssessmentEvaluation(Float score, Boolean passed, Integer attempts, AssessmentEntryStatus assessmentStatus,
			Boolean fullyAssessed, Long assessmentID, String comment, String coachComment) {
		super(score, passed, assessmentStatus, fullyAssessed, assessmentID);
		this.attempts = attempts;
		this.comment = comment;
		this.coachComment = coachComment;
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
	
	public static final AssessmentEvaluation toAssessmentEvalutation(AssessmentEntry entry, AssessableCourseNode node) {
		if(entry == null) {
			return AssessmentEvaluation.EMPTY_EVAL;
		}
		
		Integer attempts = null;
		if(node.hasAttemptsConfigured()) {
			attempts = entry.getAttempts();
		}
		
		Float score = null;
		if(node.hasScoreConfigured()) {
			score = entry.getScore() == null ? null : entry.getScore().floatValue();
		}
		
		Boolean passed = null;
		if(node.hasPassedConfigured()) {
			passed = entry.getPassed();
		}
		
		String comment = null;
		if(node.hasCommentConfigured()) {
			comment = entry.getComment();
		}
		return new AssessmentEvaluation(score, passed, attempts, entry.getAssessmentStatus(),
				entry.getFullyAssessed(), entry.getAssessmentId(), comment, entry.getCoachComment());
	}
}