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

import java.io.File;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Asynchronous entry point for the essay AI correction flow used at
 * learner submit time in the ceditor QuizPart runtime. Persists an
 * {@link EssayAiCorrection} result row tied to the learner's answer,
 * schedules an {@link EssayAiCorrectionTask} on the generic persisted
 * task executor ({@code o_ex_task}, {@code aiInteractive} queue), and
 * exposes a status view the overlay UI polls until the result row reaches
 * a terminal state. The executor's task row is deleted on success — the
 * result row is the durable record of the correction.
 * <p>
 * The grading runs inline on the AI worker thread; the hard time bound is
 * the HTTP client timeout of the grading SPI, surfaced as
 * {@link EssayGradingTimeoutException} (→ TIMEOUT status).
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayAiCorrectionService implements UserDataDeletable {
	private static final Logger log = Tracing.createLoggerFor(EssayAiCorrectionService.class);

	private static final int ERROR_MESSAGE_MAX = 2000;

	@Autowired
	private DB dbInstance;
	@Autowired
	private EssayAiCorrectionDao correctionDao;
	@Autowired
	private EssayFormativeFeedbackService essayFormativeFeedbackService;
	@Autowired
	private EssayAiGradingFileStore essayAiGradingFileStore;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDao;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Persist a new correction result row, schedule the
	 * {@link EssayAiCorrectionTask} on the task executor, commit, and
	 * return the correction key so the overlay UI can poll for status.
	 *
	 * @param storagePath              ceditor QuizPart storage path (= the
	 *                                 directory that contains all question
	 *                                 sub-directories)
	 * @param questionId               the QTI question identifier (= the
	 *                                 sub-directory name that holds
	 *                                 {@code ai-grading.json})
	 * @param studentAnswer            the learner's free-text answer
	 * @param assessmentItemSessionKey QTI item session key the answer
	 *                                 came from; may be {@code null} for
	 *                                 an in-memory session
	 * @param identity                 the learner
	 * @return the newly persisted correction key
	 */
	public Long submit(String storagePath, String questionId, String studentAnswer,
			Long assessmentItemSessionKey, Identity identity) {
		if (identity == null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		assertWithinRateLimit(identity);
		EssayAiCorrection correction = correctionDao.create(storagePath, questionId, identity,
				assessmentItemSessionKey, studentAnswer);
		dbInstance.commit();

		try {
			// No back-pointer from (storagePath, questionId) to a RepositoryEntry —
			// pass null OlatResource. Task execution is unaffected; only some
			// auxiliary scoping/auditing in the task executor sees null here.
			taskExecutorManager.execute(new EssayAiCorrectionTask(correction.getKey()),
					identity, null, null, null);
		} catch (Exception e) {
			log.error("Failed to schedule essay AI correction {}", correction.getKey(), e);
			markFailed(correction.getKey(), "failed to schedule: " + e.getMessage());
		}
		return correction.getKey();
	}

	/**
	 * Execute the grading body for a single correction. Called from
	 * {@link EssayAiCorrectionTask#run()} but also reachable directly
	 * for synchronous execution in tests.
	 */
	public void runCorrection(Long correctionKey) {
		if (correctionKey == null) return;
		EssayAiCorrection correction = correctionDao.loadByKey(correctionKey);
		if (correction == null) {
			log.warn("Essay AI correction {} not found — skipping", correctionKey);
			return;
		}
		if (correction.getStatus() != EssayAiCorrection.Status.PENDING) {
			log.warn("Essay AI correction {} in status {} — skipping", correctionKey, correction.getStatus());
			return;
		}
		correction.setStatus(EssayAiCorrection.Status.RUNNING);
		correctionDao.update(correction);
		dbInstance.commit();

		try {
			Identity identity = correction.getIdentity();
			AssessmentItemSession itemSession = correction.getAssessmentItemSessionKey() == null ? null
					: loadItemSession(correction.getAssessmentItemSessionKey());
			Locale locale = identity != null && identity.getUser() != null
					? new Locale(identity.getUser().getPreferences() == null ? "en"
							: safeLanguage(identity))
					: Locale.ENGLISH;

			String storagePath = correction.getStoragePath();
			String questionId = correction.getQuestionId();
			if (storagePath == null || questionId == null) {
				String message = "no (storagePath, questionId) linked to correction";
				logCorrectionGuard(identity, questionId, "GradingArtefactMissing", message);
				markFailed(correctionKey, message);
				return;
			}
			EssayAiGrading grading = loadGradingFromDisk(storagePath, questionId);
			if (grading == null) {
				String message = "ai-grading.json not found for question " + questionId;
				logCorrectionGuard(identity, questionId, "GradingArtefactMissing", message);
				markFailed(correctionKey, message);
				return;
			}
			String studentAnswer = correction.getStudentAnswer();

			FormativeFeedback feedback = essayFormativeFeedbackService.grade(grading, studentAnswer,
					itemSession, identity, locale);

			EssayAiCorrection doneCorrection = correctionDao.loadByKey(correctionKey);
			doneCorrection.setStatus(EssayAiCorrection.Status.DONE);
			doneCorrection.setFeedbackJson(toJson(feedback));
			doneCorrection.setCompletedDate(new Date());
			correctionDao.update(doneCorrection);
			dbInstance.commit();

		} catch (EssayGradingTimeoutException timeout) {
			log.info("Essay AI correction {} hit the hard timeout", correctionKey);
			markTimeout(correctionKey, timeout.getMessage());
		} catch (EssayGradingIntegrityException integrity) {
			log.warn("Essay AI correction {} refused — integrity hash mismatch: {}", correctionKey,
					integrity.getMessage());
			logCorrectionGuard(correction.getIdentity(), correction.getQuestionId(),
					"IntegrityFailure", integrity.getMessage());
			markFailed(correctionKey, "integrity check failed: " + integrity.getMessage());
		} catch (RuntimeException e) {
			log.error("Essay AI correction {} failed", correctionKey, e);
			markFailed(correctionKey, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
		}
	}

	/**
	 * Write a guard log row for a correction-level refusal (missing artefact,
	 * integrity check failure) so the rate limiter and cost reports see the
	 * attempt even when no LLM call ever happened.
	 */
	private void logCorrectionGuard(Identity owner, String questionId, String errorCode, String message) {
		AiUsageContext usageContext = AiUsageContext.builder()
				.usageContextType(EssayFormativeFeedbackService.USAGE_CONTEXT_TYPE)
				.usageContextId(questionId)
				.identity(owner)
				.build();
		aiUsageLogDao.createGuardLog(AiFeature.EssayGrading.getType(), usageContext,
				errorCode, message);
	}

	/**
	 * Resolve {@code storagePath/questionId/ai-grading.json} via the
	 * ceditor's {@code ContentEditorFileStorage} (looked up lazily through
	 * {@link CoreSpringFactory} to keep the {@code ai.essay} package free
	 * of a hard ceditor compile-time dependency). Package-private so unit
	 * tests can override this hook with a fixture-backed implementation.
	 */
	EssayAiGrading loadGradingFromDisk(String storagePath, String questionId) {
		try {
			org.olat.modules.ceditor.manager.ContentEditorFileStorage storage =
					CoreSpringFactory.getImpl(org.olat.modules.ceditor.manager.ContentEditorFileStorage.class);
			if (storage == null) {
				log.warn("ContentEditorFileStorage not available — cannot load ai-grading.json");
				return null;
			}
			File questionsDir = storage.getFile(storagePath);
			if (questionsDir == null) {
				return null;
			}
			File questionDir = new File(questionsDir, questionId);
			return essayAiGradingFileStore.load(questionDir);
		} catch (Exception e) {
			log.warn("Could not load ai-grading.json for storagePath={} questionId={}: {}",
					storagePath, questionId, e.getMessage());
			return null;
		}
	}

	private AssessmentItemSession loadItemSession(final Long itemSessionKey) {
		try {
			return qtiService.getAssessmentItemSession(() -> itemSessionKey);
		} catch (Exception e) {
			log.warn("Could not load assessment item session {}: {}", itemSessionKey, e.getMessage());
			return null;
		}
	}

	private static String safeLanguage(Identity identity) {
		try {
			return identity.getUser().getPreferences().getLanguage();
		} catch (Exception e) {
			return "en";
		}
	}

	private void markFailed(Long correctionKey, String reason) {
		EssayAiCorrection correction = correctionDao.loadByKey(correctionKey);
		if (correction == null) return;
		correction.setStatus(EssayAiCorrection.Status.FAILED);
		correction.setErrorMessage(truncate(reason == null ? "unknown" : reason));
		correction.setCompletedDate(new Date());
		correctionDao.update(correction);
		dbInstance.commit();
	}

	private void markTimeout(Long correctionKey, String reason) {
		EssayAiCorrection correction = correctionDao.loadByKey(correctionKey);
		if (correction == null) return;
		correction.setStatus(EssayAiCorrection.Status.TIMEOUT);
		correction.setErrorMessage(truncate(reason == null ? "timeout" : reason));
		correction.setCompletedDate(new Date());
		correctionDao.update(correction);
		dbInstance.commit();
	}

	/**
	 * Status view returned to the overlay UI. {@code feedbackJson} is the
	 * raw serialised {@link FormativeFeedback} record — the caller parses
	 * it with {@link #parseFeedback(String)} when status is
	 * {@link EssayAiCorrection.Status#DONE}.
	 * <p>
	 * The owner check is mandatory on the public surface: the feedback JSON
	 * embeds rubric-derived signals plus evidence quotes from the original
	 * student answer, so a leak via guessed PKs would be a privacy
	 * regression. When the caller is not the correction owner the response
	 * is indistinguishable from "not found" — we never log who tried.
	 *
	 * @param correctionKey the correction primary key
	 * @param caller        the polling identity (must not be {@code null});
	 *                      when the correction exists but was created by a
	 *                      different identity the method returns the same
	 *                      "not found" view as for missing keys
	 */
	public CorrectionStatusView getStatus(Long correctionKey, Identity caller) {
		if (caller == null) {
			throw new IllegalArgumentException("caller must not be null");
		}
		EssayAiCorrection correction = correctionDao.loadByKey(correctionKey);
		if (correction == null) {
			return new CorrectionStatusView(correctionKey, null, null, null);
		}
		Long ownerKey = correction.getIdentity() == null ? null : correction.getIdentity().getKey();
		if (ownerKey == null || !ownerKey.equals(caller.getKey())) {
			// Indistinguishable from "not found". Do not log the caller — that
			// would itself reveal that the key exists.
			return new CorrectionStatusView(correctionKey, null, null, null);
		}
		return new CorrectionStatusView(correctionKey, correction.getStatus(),
				correction.getFeedbackJson(), correction.getErrorMessage());
	}

	/**
	 * Trusted internal status lookup that bypasses the owner check. Reserved
	 * for code paths that have already established ownership / admin
	 * authority (background runners, admin diagnostics). Public callers must
	 * use {@link #getStatus(Long, Identity)} instead.
	 */
	CorrectionStatusView getStatusInternal(Long correctionKey) {
		EssayAiCorrection correction = correctionDao.loadByKey(correctionKey);
		if (correction == null) {
			return new CorrectionStatusView(correctionKey, null, null, null);
		}
		return new CorrectionStatusView(correctionKey, correction.getStatus(),
				correction.getFeedbackJson(), correction.getErrorMessage());
	}

	/**
	 * Per-user rate limiter for the essay-grading submit path. Counts the
	 * number of {@link AiFeature#EssayGrading} usage log rows recorded for
	 * {@code caller} in the last 60 seconds; throws
	 * {@link AiRateLimitExceededException} once the configured threshold is
	 * reached.
	 * <p>
	 * Backed by {@link org.olat.core.commons.services.ai.AiUsageLog} — every
	 * successful provider call already writes a row, so no new
	 * infrastructure is needed. Failed provider calls are also logged and
	 * therefore also count toward the budget; this is intentional (a tight
	 * loop of failures is exactly what we want to throttle).
	 */
	private void assertWithinRateLimit(Identity caller) {
		if (caller == null || caller.getKey() == null) {
			return;
		}
		int limit = aiModule == null ? 30 : aiModule.getEssayGradingMaxCallsPerMinutePerUser();
		if (limit <= 0) {
			return;
		}
		Date since = new Date(System.currentTimeMillis() - 60_000L);
		int count = aiUsageLogDao.countByIdentityFeatureSince(caller.getKey(),
				AiFeature.EssayGrading.getType(), since);
		if (count >= limit) {
			String message = "essay grading rate limit exceeded for identity " + caller.getKey()
					+ " (" + count + " >= " + limit + " per minute)";
			// Record the refusal so it counts toward the limiter on subsequent
			// submits and shows up in cost / abuse reports.
			AiUsageContext usageContext = AiUsageContext.builder()
					.usageContextType(EssayFormativeFeedbackService.USAGE_CONTEXT_TYPE)
					.identity(caller)
					.build();
			aiUsageLogDao.createGuardLog(AiFeature.EssayGrading.getType(), usageContext,
					"RateLimited", message);
			throw new AiRateLimitExceededException(message);
		}
	}

	/**
	 * Delete all correction results of all questions under the given
	 * QuizPart storage path. Called by the ceditor when a QuizPart (or the
	 * page containing it) is deleted.
	 */
	public int deleteCorrections(String storagePath) {
		int deleted = correctionDao.deleteByStoragePath(storagePath);
		if (deleted > 0) {
			log.info("Deleted {} essay AI corrections for storagePath={}", deleted, storagePath);
		}
		return deleted;
	}

	/**
	 * Delete all correction results of a single question. Called by the
	 * ceditor when one question is removed from a QuizPart.
	 */
	public int deleteCorrections(String storagePath, String questionId) {
		int deleted = correctionDao.deleteByQuestion(storagePath, questionId);
		if (deleted > 0) {
			log.info("Deleted {} essay AI corrections for storagePath={} questionId={}",
					deleted, storagePath, questionId);
		}
		return deleted;
	}

	/**
	 * User-data-deletion lifecycle: the student answer in the correction
	 * row is personal data, delete all rows of the user.
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		int deleted = correctionDao.deleteByIdentity(identity);
		if (deleted > 0) {
			log.info(Tracing.M_AUDIT, "Deleted {} essay AI corrections of identity {}",
					deleted, identity.getKey());
		}
	}

	/**
	 * Parse the persisted feedback JSON back into a
	 * {@link FormativeFeedback} record. Returns {@code null} on parse
	 * failure (the overlay UI falls back to the error state).
	 */
	public FormativeFeedback parseFeedback(String feedbackJson) {
		if (feedbackJson == null || feedbackJson.isBlank()) return null;
		try {
			return mapper.readValue(feedbackJson, FormativeFeedback.class);
		} catch (Exception e) {
			log.warn("Failed to parse persisted feedback JSON: {}", e.getMessage());
			return null;
		}
	}

	private String toJson(Object o) {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			log.warn("Failed to serialise feedback payload: {}", e.getMessage());
			return null;
		}
	}

	private static String truncate(String s) {
		if (s == null) return null;
		if (s.length() <= ERROR_MESSAGE_MAX) return s;
		return s.substring(0, ERROR_MESSAGE_MAX - 3) + "...";
	}

	public record CorrectionStatusView(Long correctionKey, EssayAiCorrection.Status status,
			String feedbackJson, String errorMessage) { }
}
