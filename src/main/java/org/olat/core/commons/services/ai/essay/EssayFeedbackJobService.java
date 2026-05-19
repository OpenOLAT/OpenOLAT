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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Asynchronous entry point for the essay AI correction flow used at
 * learner submit time in the ceditor QuizPart runtime. Persists an
 * {@link EssayFeedbackJob} row, schedules an
 * {@link EssayFeedbackLongRunnable} on the low-priority task executor
 * queue, and exposes a status view the overlay UI polls until the job
 * reaches a terminal state.
 * <p>
 * The underlying {@link EssayFormativeFeedbackService#grade} call already
 * enforces a hard 30-second timeout. This service adds a defence-in-depth
 * 35-second wrapper in case the service-internal timeout is bypassed by a
 * future change.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayFeedbackJobService {
	private static final Logger log = Tracing.createLoggerFor(EssayFeedbackJobService.class);

	/**
	 * Defence-in-depth outer timeout on the grading call. One second past
	 * the inner {@link EssayFormativeFeedbackService#GRADING_TIMEOUT_SECONDS}
	 * so that the service-internal timeout has a chance to fire first and
	 * produce a TIMEOUT state before this outer cap is hit.
	 */
	public static final int OUTER_TIMEOUT_SECONDS = EssayFormativeFeedbackService.GRADING_TIMEOUT_SECONDS + 5;

	private static final int ERROR_MESSAGE_MAX = 2000;

	@Autowired
	private DB dbInstance;
	@Autowired
	private EssayFeedbackJobDao essayFeedbackJobDao;
	@Autowired
	private EssayFormativeFeedbackService essayFormativeFeedbackService;
	@Autowired
	private EssayAiGradingFileStore essayAiGradingFileStore;
	@Autowired
	private BaseSecurity baseSecurity;
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
	 * Persist a new job, schedule the {@link EssayFeedbackLongRunnable}
	 * on the task executor, commit, and return the job key so the overlay
	 * UI can poll for status.
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
	 * @return the newly persisted job key
	 */
	public Long submit(String storagePath, String questionId, String studentAnswer,
			Long assessmentItemSessionKey, Identity identity) {
		if (identity == null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		assertWithinRateLimit(identity);
		EssayFeedbackJob job = essayFeedbackJobDao.create(storagePath, questionId, identity,
				assessmentItemSessionKey, studentAnswer);
		dbInstance.commit();

		try {
			TaskExecutorManager tem = taskExecutorManager != null
					? taskExecutorManager : CoreSpringFactory.getImpl(TaskExecutorManager.class);
			// No back-pointer from (storagePath, questionId) to a RepositoryEntry —
			// pass null OlatResource. Job execution is unaffected; only some
			// auxiliary scoping/auditing in the task executor sees null here.
			tem.execute(new EssayFeedbackLongRunnable(job.getKey()), identity, null, null, null);
		} catch (Exception e) {
			log.error("Failed to schedule essay feedback job {}", job.getKey(), e);
			markFailed(job.getKey(), "failed to schedule: " + e.getMessage());
		}
		return job.getKey();
	}

	/**
	 * Execute the grading body for a single job. Called from
	 * {@link EssayFeedbackLongRunnable#run()} but also reachable directly
	 * for synchronous execution in tests.
	 */
	public void runJob(Long jobKey) {
		if (jobKey == null) return;
		EssayFeedbackJob job = essayFeedbackJobDao.loadByKey(jobKey);
		if (job == null) {
			log.warn("Essay feedback job {} not found — skipping", jobKey);
			return;
		}
		if (job.getState() != EssayFeedbackJob.State.PENDING) {
			log.warn("Essay feedback job {} in state {} — skipping", jobKey, job.getState());
			return;
		}
		job.setState(EssayFeedbackJob.State.RUNNING);
		job.setStartedAt(new Date());
		essayFeedbackJobDao.update(job);
		dbInstance.commit();

		try {
			Identity identity = job.getIdentityKey() == null ? null
					: baseSecurity.loadIdentityByKey(job.getIdentityKey());
			AssessmentItemSession itemSession = job.getAssessmentItemSessionKey() == null ? null
					: loadItemSession(job.getAssessmentItemSessionKey());
			Locale locale = identity != null && identity.getUser() != null
					? new Locale(identity.getUser().getPreferences() == null ? "en"
							: safeLanguage(identity))
					: Locale.ENGLISH;

			String storagePath = job.getStoragePath();
			String questionId = job.getQuestionId();
			if (storagePath == null || questionId == null) {
				String message = "no (storagePath, questionId) linked to job";
				logJobGuard(identity, questionId, "GradingArtefactMissing", message);
				markFailed(jobKey, message);
				return;
			}
			EssayAiGrading grading = loadGradingFromDisk(storagePath, questionId);
			if (grading == null) {
				String message = "ai-grading.json not found for question " + questionId;
				logJobGuard(identity, questionId, "GradingArtefactMissing", message);
				markFailed(jobKey, message);
				return;
			}
			String studentAnswer = job.getStudentAnswer();

			FormativeFeedback feedback = invokeWithOuterTimeout(() ->
					essayFormativeFeedbackService.grade(grading, studentAnswer,
							itemSession, identity, locale));

			EssayFeedbackJob doneJob = essayFeedbackJobDao.loadByKey(jobKey);
			doneJob.setState(EssayFeedbackJob.State.DONE);
			doneJob.setFeedbackJson(toJson(feedback));
			doneJob.setCompletedAt(new Date());
			essayFeedbackJobDao.update(doneJob);
			dbInstance.commit();

		} catch (EssayGradingTimeoutException timeout) {
			log.info("Essay feedback job {} hit the hard timeout", jobKey);
			markTimeout(jobKey, timeout.getMessage());
		} catch (EssayGradingIntegrityException integrity) {
			log.warn("Essay feedback job {} refused — integrity hash mismatch: {}", jobKey, integrity.getMessage());
			Identity owner = job.getIdentityKey() == null ? null
					: baseSecurity.loadIdentityByKey(job.getIdentityKey());
			logJobGuard(owner, job.getQuestionId(), "IntegrityFailure", integrity.getMessage());
			markFailed(jobKey, "integrity check failed: " + integrity.getMessage());
		} catch (RuntimeException e) {
			log.error("Essay feedback job {} failed", jobKey, e);
			markFailed(jobKey, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
		}
	}

	/**
	 * Write a guard log row for a job-level refusal (missing artefact,
	 * integrity check failure) so the rate limiter and cost reports see the
	 * attempt even when no LLM call ever happened.
	 */
	private void logJobGuard(Identity owner, String questionId, String errorCode, String message) {
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

	/**
	 * Defence-in-depth wrapper around
	 * {@link EssayFormativeFeedbackService#grade}. The inner call already
	 * enforces a 30-second timeout; this wrapper catches the case where
	 * the inner timeout somehow misses and prevents the task executor
	 * thread from being held for minutes.
	 */
	private FormativeFeedback invokeWithOuterTimeout(java.util.concurrent.Callable<FormativeFeedback> call) {
		CompletableFuture<FormativeFeedback> future = CompletableFuture.supplyAsync(() -> {
			try {
				return call.call();
			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new AiEssayGradingException("grade() failed", e);
			} finally {
				// The async thread has its own ThreadLocal EntityManager (DBImpl).
				// Commit and close so pre-filter guard logs, updateEssayFields,
				// and any updateAsFailed writes persist; otherwise the row is
				// silently dropped when the future returns.
				try {
					dbInstance.commitAndCloseSession();
				} catch (Exception flushError) {
					log.warn("Failed to commit/close DB session on async grading job thread: {}",
							flushError.getMessage());
				}
			}
		});
		try {
			return future.get(OUTER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new EssayGradingTimeoutException(
					"Essay grading exceeded outer " + OUTER_TIMEOUT_SECONDS + " s timeout", e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof EssayGradingTimeoutException egte) throw egte;
			if (cause instanceof EssayGradingIntegrityException egie) throw egie;
			if (cause instanceof AiEssayGradingException aege) throw aege;
			if (cause instanceof RuntimeException re) throw re;
			throw new AiEssayGradingException("grade() failed", cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AiEssayGradingException("grade() interrupted", e);
		}
	}

	private void markFailed(Long jobKey, String reason) {
		EssayFeedbackJob job = essayFeedbackJobDao.loadByKey(jobKey);
		if (job == null) return;
		job.setState(EssayFeedbackJob.State.FAILED);
		job.setErrorMessage(truncate(reason == null ? "unknown" : reason));
		job.setCompletedAt(new Date());
		essayFeedbackJobDao.update(job);
		dbInstance.commit();
	}

	private void markTimeout(Long jobKey, String reason) {
		EssayFeedbackJob job = essayFeedbackJobDao.loadByKey(jobKey);
		if (job == null) return;
		job.setState(EssayFeedbackJob.State.TIMEOUT);
		job.setErrorMessage(truncate(reason == null ? "timeout" : reason));
		job.setCompletedAt(new Date());
		essayFeedbackJobDao.update(job);
		dbInstance.commit();
	}

	/**
	 * Status view returned to the overlay UI. {@code feedbackJson} is the
	 * raw serialised {@link FormativeFeedback} record — the caller parses
	 * it with {@link #parseFeedback(String)} when state is
	 * {@link EssayFeedbackJob.State#DONE}.
	 * <p>
	 * The owner check is mandatory on the public surface: the feedback JSON
	 * embeds rubric-derived signals plus evidence quotes from the original
	 * student answer, so a leak via guessed PKs would be a privacy
	 * regression. When the caller is not the job owner the response is
	 * indistinguishable from "not found" — we never log who tried.
	 *
	 * @param jobKey the job primary key
	 * @param caller the polling identity (must not be {@code null}); when the
	 *               job exists but was created by a different identity the
	 *               method returns the same "not found" view as for missing
	 *               keys
	 */
	public JobStatusView getStatus(Long jobKey, Identity caller) {
		if (caller == null) {
			throw new IllegalArgumentException("caller must not be null");
		}
		EssayFeedbackJob job = essayFeedbackJobDao.loadByKey(jobKey);
		if (job == null) {
			return new JobStatusView(jobKey, null, null, null);
		}
		if (job.getIdentityKey() == null || !job.getIdentityKey().equals(caller.getKey())) {
			// Indistinguishable from "not found". Do not log the caller — that
			// would itself reveal that the key exists.
			return new JobStatusView(jobKey, null, null, null);
		}
		return new JobStatusView(jobKey, job.getState(), job.getFeedbackJson(), job.getErrorMessage());
	}

	/**
	 * Trusted internal status lookup that bypasses the owner check. Reserved
	 * for code paths that have already established ownership / admin
	 * authority (background runners, admin diagnostics). Public callers must
	 * use {@link #getStatus(Long, Identity)} instead.
	 */
	JobStatusView getStatusInternal(Long jobKey) {
		EssayFeedbackJob job = essayFeedbackJobDao.loadByKey(jobKey);
		if (job == null) {
			return new JobStatusView(jobKey, null, null, null);
		}
		return new JobStatusView(jobKey, job.getState(), job.getFeedbackJson(), job.getErrorMessage());
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

	public record JobStatusView(Long jobKey, EssayFeedbackJob.State state,
			String feedbackJson, String errorMessage) { }
}
