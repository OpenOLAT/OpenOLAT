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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 *
 * Result of a single AI essay correction run, tied to the learner's answer:
 * the answer text itself plus the QTI assessment item session it came from
 * (soft reference by key — item sessions are deleted on quiz reset, the
 * correction result is kept for audit).
 * <p>
 * This row is also the usage context of the AI call: each
 * {@code o_ai_usage_log} row written for this correction carries
 * {@code usageContextType = ai-essay-correction} and
 * {@code usageContextId = <this key>}. The grading-run provenance
 * ({@link #contentHashAtCall}, {@link #promptTemplateVersion},
 * {@link #tier}) lives here rather than on the log, which stays a generic
 * cost ledger.
 * <p>
 * The row is created at learner submit time, the correction itself runs as
 * a generic persisted task ({@link EssayAiCorrectionTask} in
 * {@code o_ex_task}), and the overlay UI polls this row's {@link #status}
 * until it reaches a terminal state. On {@link Status#DONE} the
 * {@link #feedbackJson} holds the serialised {@link FormativeFeedback}
 * record; on {@link Status#FAILED} or {@link Status#TIMEOUT} the
 * {@link #errorMessage} carries a short human-readable reason (full stack
 * trace only in server logs).
 *
 * Initial date: 2026-06-10<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Entity(name = "aiessaycorrection")
@Table(name = "o_ai_essay_correction")
public class EssayAiCorrection implements CreateInfo, ModifiedInfo, Persistable {

	private static final long serialVersionUID = 1L;

	public enum Status {
		PENDING,
		RUNNING,
		DONE,
		FAILED,
		TIMEOUT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastmodified", nullable = false, insertable = true, updatable = true)
	private Date lastModified;

	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_identity", nullable = false, insertable = true, updatable = false)
	private Identity identity;

	@Column(name = "a_item_session_key", nullable = true, insertable = true, updatable = false)
	private Long assessmentItemSessionKey;

	@Column(name = "a_storage_path", nullable = true, insertable = true, updatable = false, length = 1024)
	private String storagePath;

	@Column(name = "a_question_id", nullable = true, insertable = true, updatable = false, length = 64)
	private String questionId;

	// Grading-run provenance: which rubric/config content, which prompt
	// template version and which length tier this correction was graded
	// against. Written during grading (the row is inserted PENDING before
	// these are known), hence updatable. These used to live on
	// o_ai_usage_log; they belong to the correction, which is the context
	// of the AI call (the log points back here via its usage context id).
	@Column(name = "a_content_hash_at_call", nullable = true, insertable = true, updatable = true, length = 64)
	private String contentHashAtCall;

	@Column(name = "a_prompt_template_version", nullable = true, insertable = true, updatable = true, length = 40)
	private String promptTemplateVersion;

	@Enumerated(EnumType.STRING)
	@Column(name = "a_tier", nullable = true, insertable = true, updatable = true, length = 16)
	private AiGradingTier tier;

	@Lob
	@Column(name = "a_student_answer", nullable = false, insertable = true, updatable = false)
	private String studentAnswer;

	@Enumerated(EnumType.STRING)
	@Column(name = "a_status", nullable = false, insertable = true, updatable = true)
	private Status status = Status.PENDING;

	@Lob
	@Column(name = "a_feedback_json", nullable = true, insertable = true, updatable = true)
	private String feedbackJson;

	@Column(name = "a_error_message", nullable = true, insertable = true, updatable = true, length = 2048)
	private String errorMessage;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "a_completed", nullable = true, insertable = true, updatable = true)
	private Date completedDate;

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

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public Long getAssessmentItemSessionKey() {
		return assessmentItemSessionKey;
	}

	public void setAssessmentItemSessionKey(Long assessmentItemSessionKey) {
		this.assessmentItemSessionKey = assessmentItemSessionKey;
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

	public String getContentHashAtCall() {
		return contentHashAtCall;
	}

	public void setContentHashAtCall(String contentHashAtCall) {
		this.contentHashAtCall = contentHashAtCall;
	}

	public String getPromptTemplateVersion() {
		return promptTemplateVersion;
	}

	public void setPromptTemplateVersion(String promptTemplateVersion) {
		this.promptTemplateVersion = promptTemplateVersion;
	}

	public AiGradingTier getTier() {
		return tier;
	}

	public void setTier(AiGradingTier tier) {
		this.tier = tier;
	}

	public String getStudentAnswer() {
		return studentAnswer;
	}

	public void setStudentAnswer(String studentAnswer) {
		this.studentAnswer = studentAnswer;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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

	public Date getCompletedDate() {
		return completedDate;
	}

	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
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
		if (obj instanceof EssayAiCorrection correction) {
			return key != null && key.equals(correction.key);
		}
		return false;
	}
}
