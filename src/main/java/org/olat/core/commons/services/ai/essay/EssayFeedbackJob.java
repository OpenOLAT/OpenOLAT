/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.essay;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 *
 * Backing row for a single asynchronous essay formative-feedback run.
 * Triggered on student submit from the ceditor QuizPart runtime, polled
 * by the overlay UI, and transitions through
 * {@link State#PENDING} &rarr; {@link State#RUNNING} &rarr;
 * {@link State#DONE} / {@link State#FAILED} / {@link State#TIMEOUT}.
 * <p>
 * The feedback payload on DONE is the serialised {@link FormativeFeedback}
 * record. On FAILED or TIMEOUT, {@link #errorMessage} carries a short
 * human-readable reason (full stack trace only in server logs).
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Entity(name = "essayfeedbackjob")
@Table(name = "o_essay_feedback_job")
public class EssayFeedbackJob implements CreateInfo, ModifiedInfo, Persistable {

	private static final long serialVersionUID = 1L;

	public enum State {
		PENDING,
		RUNNING,
		DONE,
		FAILED,
		TIMEOUT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "a_id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@Column(name = "a_storage_path", nullable = true, insertable = true, updatable = true, length = 1024)
	private String storagePath;

	@Column(name = "a_question_id", nullable = true, insertable = true, updatable = true, length = 64)
	private String questionId;

	@Column(name = "a_identity_fk", nullable = false, insertable = true, updatable = false)
	private Long identityKey;

	@Column(name = "a_assessment_item_session_key", nullable = true, insertable = true, updatable = false)
	private Long assessmentItemSessionKey;

	@Lob
	@Column(name = "a_student_answer", nullable = false, insertable = true, updatable = false)
	private String studentAnswer;

	@Enumerated(EnumType.STRING)
	@Column(name = "a_state", nullable = false, insertable = true, updatable = true)
	private State state = State.PENDING;

	@Lob
	@Column(name = "a_feedback_json", nullable = true, insertable = true, updatable = true)
	private String feedbackJson;

	@Column(name = "a_error_message", nullable = true, insertable = true, updatable = true, length = 2048)
	private String errorMessage;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_started_at", nullable = true, insertable = true, updatable = true)
	private Date startedAt;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_completed_at", nullable = true, insertable = true, updatable = true)
	private Date completedAt;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getAssessmentItemSessionKey() {
		return assessmentItemSessionKey;
	}

	public void setAssessmentItemSessionKey(Long assessmentItemSessionKey) {
		this.assessmentItemSessionKey = assessmentItemSessionKey;
	}

	public String getStudentAnswer() {
		return studentAnswer;
	}

	public void setStudentAnswer(String studentAnswer) {
		this.studentAnswer = studentAnswer;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getFeedbackJson() {
		return feedbackJson;
	}

	public void setFeedbackJson(String feedbackJson) {
		this.feedbackJson = feedbackJson;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(Date completedAt) {
		this.completedAt = completedAt;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return key == null ? -869_245_113 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof EssayFeedbackJob job) {
			return key != null && key.equals(job.key);
		}
		return false;
	}
}
