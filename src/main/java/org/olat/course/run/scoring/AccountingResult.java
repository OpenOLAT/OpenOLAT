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
import java.util.Objects;

import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 16 Sep 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccountingResult extends AssessmentEvaluation {

	private final AssessmentEvaluation origin;
	private Date evaluatedStartDate;
	private Overridable<Date> evaluatedEndDate;
	private Integer evaluatedDuration;
	private ObligationOverridable evaluatedObligation;
	private Float evaluatedScore;
	private Float evaluatedMaxScore;
	private String evaluatedGrade;
	private String evaluatedGradeSystemIdent;
	private String evaluatedPerformanceClassIdent;
	private Overridable<Boolean> evaluatedPassedOverridable;
	private Boolean evaluatedUserVisible;
	private Double evaluatedCompletion;
	private AssessmentEntryStatus evaluatedStatus;
	private Boolean evaluatedFullyAssessed;
	private Date evaluatedLastUserModified;
	private Date evaluatedLastCoachModified;

	public AccountingResult(AssessmentEvaluation evaluation) {
		super(evaluation, evaluation.getAssessmentStatus());
		this.origin = evaluation;
		this.evaluatedStartDate = origin.getStartDate();
		this.evaluatedEndDate = origin.getEndDate().clone();
		this.evaluatedDuration = origin.getDuration();
		this.evaluatedObligation = origin.getObligation();
		this.evaluatedScore = origin.getScore();
		this.evaluatedMaxScore = origin.getMaxScore();
		this.evaluatedGrade = origin.getGrade();
		this.evaluatedGradeSystemIdent = origin.getGradeSystemIdent();
		this.evaluatedPerformanceClassIdent = origin.getPerformanceClassIdent();
		this.evaluatedPassedOverridable = origin.getPassedOverridable().clone();
		this.evaluatedUserVisible = origin.getUserVisible();
		this.evaluatedCompletion = origin.getCompletion();
		this.evaluatedStatus = origin.getAssessmentStatus();
		this.evaluatedFullyAssessed = origin.getFullyAssessed();
		this.evaluatedLastUserModified = origin.getLastUserModified();
		this.evaluatedLastCoachModified = origin.getLastCoachModified();
	}

	@Override
	public Date getStartDate() {
		return evaluatedStartDate;
	}

	public void setStartDate(Date startDate) {
		this.evaluatedStartDate = startDate;
	}

	@Override
	public Overridable<Date> getEndDate() {
		return evaluatedEndDate;
	}

	public void setEndDate(Overridable<Date> endDate) {
		this.evaluatedEndDate = endDate;
	}

	@Override
	public Integer getDuration() {
		return evaluatedDuration;
	}

	public void setDuration(Integer duration) {
		this.evaluatedDuration = duration;
	}

	@Override
	public ObligationOverridable getObligation() {
		return evaluatedObligation;
	}

	public void setObligation(ObligationOverridable obligation) {
		this.evaluatedObligation = obligation;
	}

	@Override
	public Float getScore() {
		return evaluatedScore;
	}

	public void setScore(Float score) {
		this.evaluatedScore = score;
	}

	@Override
	public Float getMaxScore() {
		return evaluatedMaxScore;
	}

	public void setMaxScore(Float maxMaxScore) {
		this.evaluatedMaxScore = maxMaxScore;
	}

	@Override
	public String getGrade() {
		return evaluatedGrade;
	}

	public void setGrade(String grade) {
		this.evaluatedGrade = grade;
	}

	@Override
	public String getGradeSystemIdent() {
		return evaluatedGradeSystemIdent;
	}

	public void setGradeSystemIdent(String gradeSystemIdent) {
		this.evaluatedGradeSystemIdent = gradeSystemIdent;
	}

	@Override
	public String getPerformanceClassIdent() {
		return evaluatedPerformanceClassIdent;
	}

	public void setPerformanceClassIdent(String performanceClassIdent) {
		this.evaluatedPerformanceClassIdent = performanceClassIdent;
	}

	@Override
	public Overridable<Boolean> getPassedOverridable() {
		return evaluatedPassedOverridable;
	}

	public void setPassedOverridable(Overridable<Boolean> passedOverridable) {
		this.evaluatedPassedOverridable = passedOverridable;
	}
	
	@Override
	public Boolean getPassed() {
		return evaluatedPassedOverridable.getCurrent();
	}

	@Override
	public Boolean getUserVisible() {
		return evaluatedUserVisible;
	}

	public void setUserVisible(Boolean userVisible) {
		this.evaluatedUserVisible = userVisible;
	}

	@Override
	public Double getCompletion() {
		return evaluatedCompletion;
	}

	public void setCompletion(Double completion) {
		this.evaluatedCompletion = completion;
	}

	@Override
	public AssessmentEntryStatus getAssessmentStatus() {
		return evaluatedStatus;
	}

	public void setStatus(AssessmentEntryStatus evaluatedStatus) {
		this.evaluatedStatus = evaluatedStatus;
	}

	@Override
	public Boolean getFullyAssessed() {
		return evaluatedFullyAssessed;
	}

	public void setFullyAssessed(Boolean fullyAssessed) {
		this.evaluatedFullyAssessed = fullyAssessed;
	}

	@Override
	public Date getLastUserModified() {
		return evaluatedLastUserModified;
	}

	public void setLastUserModified(Date lastUserModified) {
		this.evaluatedLastUserModified = lastUserModified;
	}

	@Override
	public Date getLastCoachModified() {
		return evaluatedLastCoachModified;
	}

	public void setLastCoachModified(Date lastCoachModified) {
		this.evaluatedLastCoachModified = lastCoachModified;
	}

	public boolean hasChanges() {
		return !Objects.equals(origin.getStartDate(), evaluatedStartDate)
				|| !Objects.equals(origin.getEndDate().getCurrent(), evaluatedEndDate.getCurrent())
				|| !Objects.equals(origin.getEndDate().getOriginal(), evaluatedEndDate.getOriginal())
				|| !Objects.equals(origin.getDuration(), evaluatedDuration)
				|| !Objects.equals(origin.getObligation().getCurrent(), evaluatedObligation.getCurrent())
				|| !Objects.equals(origin.getObligation().getInherited(), evaluatedObligation.getInherited())
				|| !Objects.equals(origin.getObligation().getEvaluated(), evaluatedObligation.getEvaluated())
				|| !Objects.equals(origin.getObligation().getConfigCurrent(), evaluatedObligation.getConfigCurrent())
				|| !Objects.equals(origin.getObligation().getConfigOriginal(), evaluatedObligation.getConfigOriginal())
				|| !Objects.equals(origin.getObligation().getModBy(), evaluatedObligation.getModBy())
				|| !Objects.equals(origin.getObligation().getModDate(), evaluatedObligation.getModDate())
				|| !Objects.equals(origin.getPassedOverridable().getCurrent(), evaluatedPassedOverridable.getCurrent())
				|| !Objects.equals(origin.getPassedOverridable().getOriginal(), evaluatedPassedOverridable.getOriginal())
				|| !Objects.equals(origin.getUserVisible(), evaluatedUserVisible)
				|| !Objects.equals(origin.getScore(), evaluatedScore)
				|| !Objects.equals(origin.getMaxScore(), evaluatedMaxScore)
				|| !Objects.equals(origin.getGrade(), evaluatedGrade)
				|| !Objects.equals(origin.getGradeSystemIdent(), evaluatedGradeSystemIdent)
				|| !Objects.equals(origin.getPerformanceClassIdent(), evaluatedPerformanceClassIdent)
				|| !Objects.equals(origin.getFullyAssessed(), evaluatedFullyAssessed)
				|| !Objects.equals(origin.getLastUserModified(), evaluatedLastUserModified)
				|| !Objects.equals(origin.getLastCoachModified(), evaluatedLastCoachModified)
				|| !Objects.equals(origin.getCompletion(), evaluatedCompletion)
				|| !Objects.equals(origin.getAssessmentStatus(), evaluatedStatus);
	}

}
