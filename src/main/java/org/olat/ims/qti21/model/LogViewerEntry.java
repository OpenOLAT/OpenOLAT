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
package org.olat.ims.qti21.model;

import java.util.Date;
import java.util.List;

import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 * 
 * Initial date: 24 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogViewerEntry {
	
	private final Date date;
	
	private boolean outcomes;
	
	private CandidateTestEventType testEventType;
	private CandidateItemEventType itemEventType;
	private QTI21QuestionType questionType;
	private AssessmentItem assessmentItem;
	
	private String assessmentItemTitle;
	private String assessmentItemId;
	private String testPlanNodeId;
	
	private Double minScore;
	private Double maxScore;
	private Double score;
	private Boolean passed;
	
	private Answers answers;
	
	public LogViewerEntry(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public boolean isOutcomes() {
		return outcomes;
	}

	public void setOutcomes(boolean outcomes) {
		this.outcomes = outcomes;
	}

	public CandidateTestEventType getTestEventType() {
		return testEventType;
	}

	public void setTestEventType(CandidateTestEventType testEventType) {
		this.testEventType = testEventType;
	}

	public CandidateItemEventType getItemEventType() {
		return itemEventType;
	}

	public void setItemEventType(CandidateItemEventType itemEventType) {
		this.itemEventType = itemEventType;
	}

	public String getAssessmentItemTitle() {
		return assessmentItemTitle;
	}

	public void setAssessmentItemTitle(String assessmentItemTitle) {
		this.assessmentItemTitle = assessmentItemTitle;
	}

	public String getAssessmentItemId() {
		return assessmentItemId;
	}

	public void setAssessmentItemId(String assessmentItemId) {
		this.assessmentItemId = assessmentItemId;
	}

	public String getTestPlanNodeId() {
		return testPlanNodeId;
	}

	public void setTestPlanNodeId(String testPlanNodeId) {
		this.testPlanNodeId = testPlanNodeId;
	}

	public QTI21QuestionType getQuestionType() {
		return questionType;
	}

	public void setQuestionType(QTI21QuestionType questionType) {
		this.questionType = questionType;
	}

	public Double getMinScore() {
		return minScore;
	}

	public void setMinScore(Double minScore) {
		this.minScore = minScore;
	}

	public Double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}
	
	public Answers getAnswers() {
		return answers;
	}

	public void setAnswers(Answers answers) {
		this.answers = answers;
	}

	public AssessmentItem getAssessmentItem() {
		return assessmentItem;
	}

	public void setAssessmentItem(AssessmentItem assessmentItem) {
		this.assessmentItem = assessmentItem;
	}
	
	public record Answers(List<Answer> answers) {
		//
	}

	public record Answer(List<String> ids, List<String> values) {
		//
	}
}
