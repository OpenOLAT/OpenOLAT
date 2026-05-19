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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiEssayGenerationService;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiMCQuestionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.content.AiContentChunker;
import org.olat.core.commons.services.ai.content.AiContentHardener;
import org.olat.core.commons.services.ai.essay.EssayGenerationJob.State;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Public entry point for AI-assisted question generation from page
 * content. Persists an {@link EssayGenerationJob} row (the row name is
 * kept for schema stability; despite "essay" in the name the job can
 * produce mixed essay + multiple-choice output for the ceditor
 * Markdown-import → QuizPart flow), schedules an
 * {@link EssayGenerationLongRunnable} on the task executor and exposes
 * status + draft lookups for the drawer UI.
 * <p>
 * The {@link #runJob(Long)} method is the body of the LongRunnable —
 * it is exposed on the service (not the runnable) so the runnable stays
 * serialisable, Spring-managed services can be accessed via
 * {@code CoreSpringFactory}, and re-runs (if ever needed) can reuse the
 * same code path directly.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayGenerationService {
	private static final Logger log = Tracing.createLoggerFor(EssayGenerationService.class);

	/**
	 * Default number of essay drafts requested when the caller does not
	 * specify one. The Markdown-import → QuizPart flow uses this value
	 * for essays and combines it 1:1 with {@link #DEFAULT_MC_QUESTION_COUNT}
	 * MC questions, producing a MC-essay-MC-essay interleave.
	 */
	public static final int DEFAULT_QUESTION_COUNT = 2;

	/**
	 * Default number of AI-generated multiple-choice questions attached
	 * to a generated QuizPart in the Markdown-import flow.
	 */
	public static final int DEFAULT_MC_QUESTION_COUNT = 2;

	/**
	 * Generic usage-context type for guard log rows written before the
	 * destination-specific per-leg context is known (e.g. rate-limit
	 * refusals at submit time).
	 */
	private static final String USAGE_CONTEXT_TYPE = "essay-generation-submit";

	@Autowired
	private DB dbInstance;
	@Autowired
	private EssayGenerationJobDao generationJobDao;
	@Autowired
	private AiEssayGenerationService aiEssayGenerationService;
	@Autowired
	private AiMCQuestionService aiMCQuestionService;
	@Autowired
	private AiContentChunker contentChunker;
	@Autowired
	private AiContentHardener contentHardener;
	@Autowired
	private EssayGenerationJobPayloadStore payloadStore;
	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDao;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Persist a new job, schedule it for asynchronous execution, and
	 * return the job key so the caller (drawer UI) can poll status.
	 */
	public Long submit(GenerationRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request must not be null");
		}
		if (request.requester() == null) {
			throw new IllegalArgumentException("request.requester must not be null");
		}
		if (request.pageMarkdown() == null || request.pageMarkdown().isBlank()) {
			throw new IllegalArgumentException("request.pageMarkdown must not be blank");
		}
		assertWithinRateLimit(request.requester());
		EssayGenerationJob job = generationJobDao.create(request.requester());
		payloadStore.store(job.getKey(), request);
		dbInstance.commit();

		try {
			RepositoryEntry entry = RepositoryManager.getInstance()
					.lookupRepositoryEntry(request.repositoryEntryKey(), false);
			if (entry != null) {
				org.olat.core.commons.services.taskexecutor.TaskExecutorManager tem =
						org.olat.core.CoreSpringFactory.getImpl(
								org.olat.core.commons.services.taskexecutor.TaskExecutorManager.class);
				tem.execute(new EssayGenerationLongRunnable(job.getKey()), request.requester(),
						entry.getOlatResource(), null, null);
			} else {
				log.warn("Essay generation job {} has no resolvable repository entry — running inline",
						job.getKey());
				runJob(job.getKey());
			}
		} catch (Exception e) {
			log.error("Failed to schedule essay generation job {}", job.getKey(), e);
			markFailed(job.getKey(), e.getMessage());
		}
		return job.getKey();
	}

	/**
	 * Execute the job body. Called from
	 * {@link EssayGenerationLongRunnable#run()} but also reachable
	 * directly for synchronous execution in tests.
	 */
	public void runJob(Long jobKey) {
		if (jobKey == null) return;
		EssayGenerationJob job = generationJobDao.loadByKey(jobKey);
		if (job == null) {
			log.warn("Essay generation job {} not found — skipping", jobKey);
			return;
		}
		if (job.getState() != State.PENDING) {
			log.warn("Essay generation job {} in state {} — skipping", jobKey, job.getState());
			return;
		}
		GenerationRequest request = payloadStore.take(jobKey);
		if (request == null) {
			markFailed(jobKey, "generation request payload missing");
			return;
		}
		job.setState(State.RUNNING);
		generationJobDao.update(job);
		dbInstance.commit();

		int accepted = 0;
		int rejected = 0;
		List<String> rejectionReasons = new ArrayList<>();
		List<EssayItemDraft> acceptedDrafts = new ArrayList<>();
		java.util.Map<EssayItemDraft, EssayAiGrading> draftToGrading = new java.util.IdentityHashMap<>();
		List<MCQuestionData> acceptedMcQuestions = new ArrayList<>();
		boolean isQuizPartFlow = request.destination() == GenerationDestination.QUIZ_PART
				&& request.pageKey() != null && request.quizPartKey() != null;
		boolean isPoolFlow = request.destination() == GenerationDestination.POOL;
		boolean wantsMc = isQuizPartFlow || isPoolFlow;

		try {
			String scrubbedMarkdown = contentHardener.harden(request.pageMarkdown()).text();
			List<org.olat.core.commons.services.ai.essay.AiContentChunk> chunks =
					contentChunker.chunk(scrubbedMarkdown, request.language());
			if (chunks.isEmpty()) {
				markFailed(jobKey, "no content chunks produced from page markdown");
				notifyQuizPartSinkFailed(request, "no content chunks produced from page markdown");
				return;
			}

			// ------------------------------------------------------------------
			// Essay generation — the original feature. Failure here is not yet
			// fatal for the QuizPart flow: we still try MC generation below so
			// the learner at least gets MC questions (graceful degradation).
			// When the caller explicitly asks for zero essay questions we skip
			// the leg entirely (no failure — intentional opt-out from the form).
			// ------------------------------------------------------------------
			String essayFailure = null;
			boolean essaySkippedByRequest = request.targetQuestionCount() == 0;
			if (essaySkippedByRequest) {
				log.info("Essay generation skipped for job {} — targetQuestionCount=0", jobKey);
			} else if (!aiEssayGenerationService.isEnabled()) {
				essayFailure = "essay generation is not configured or enabled";
				log.warn("Essay generation disabled for job {} — skipping essay part", jobKey);
			} else {
				try {
					int essayCount = request.targetQuestionCount() > 0
							? request.targetQuestionCount() : DEFAULT_QUESTION_COUNT;
					List<EssayItemDraft> drafts = aiEssayGenerationService.generateEssayQuestions(
							null,
							chunks,
							safeList(request.learningObjectives()),
							safeList(request.targetBloomLevels()),
							request.targetDifficulty(),
							essayCount,
							request.language() == null ? Locale.ENGLISH : request.language());

					String generatorSpiId = aiEssayGenerationService.getConfiguredSpiId();
					String generatorModel = aiEssayGenerationService.getConfiguredModel();
					for (EssayItemDraft draft : drafts) {
						List<GeneratedItemValidator.ValidationIssue> issues =
								GeneratedItemValidator.validate(draft);
						if (issues.isEmpty()) {
							EssayAiGrading grading = buildAcceptedGrading(request, draft,
									generatorSpiId, generatorModel);
							acceptedDrafts.add(draft);
							if (grading != null) {
								draftToGrading.put(draft, grading);
							}
							accepted++;
						} else {
							rejected++;
							for (var issue : issues) {
								rejectionReasons.add(issue.fieldPath() + ": " + issue.reason());
							}
						}
					}
				} catch (Exception essayEx) {
					String raw = essayEx.getMessage() == null
							? essayEx.getClass().getSimpleName() : essayEx.getMessage();
					essayFailure = humanizeProviderError("essay", raw);
					log.error("Essay generation leg of job {} failed — will still try MC leg", jobKey, essayEx);
				}
			}

			// ------------------------------------------------------------------
			// MC generation leg (Markdown-import → QuizPart flow and pool flow).
			// The standalone author-drawer flow only needs essays; skip MC there.
			// When the caller explicitly requests zero MC questions we also skip
			// the leg entirely (intentional opt-out from the form).
			// ------------------------------------------------------------------
			String mcFailure = null;
			boolean mcSkippedByRequest = request.mcQuestionCount() == 0;
			if (wantsMc && mcSkippedByRequest) {
				log.info("MC question generation skipped for job {} — mcQuestionCount=0", jobKey);
			} else if (wantsMc) {
				if (aiMCQuestionService == null || !aiMCQuestionService.isEnabled()) {
					mcFailure = "MC question generation is not configured or enabled";
					log.info("MC question generation disabled for job {} — skipping MC part", jobKey);
				} else {
					try {
						int mcCount = request.mcQuestionCount() > 0
								? request.mcQuestionCount() : DEFAULT_MC_QUESTION_COUNT;
						String contextType = switch (request.destination()) {
							case POOL -> "qpool-generate-questions";
							case QUIZ_PART -> "ceditor-quizpart-generate-questions";
							case DRAWER -> "ai-drawer-generate-questions";
						};
						String resourceType = request.destination() == GenerationDestination.POOL
								? "PoolQPool" : "RepositoryEntry";
						long resourceId = request.destination() == GenerationDestination.POOL
								? (request.taxonomyLevelKey() == null ? 0L : request.taxonomyLevelKey())
								: (request.repositoryEntryKey() == null ? 0L : request.repositoryEntryKey());
						AiUsageContext usageContext = AiUsageContext.builder()
								.usageContextType(contextType)
								.identity(request.requester())
								.locale(request.language() == null ? Locale.ENGLISH : request.language())
								.resourceType(resourceType)
								.resourceId(resourceId)
								.build();
						AiMCQuestionsResponse mcResponse = aiMCQuestionService
								.generateMCQuestionsResponse(usageContext, scrubbedMarkdown, mcCount,
										safeList(request.targetBloomLevels()),
										request.targetDifficulty(),
										safeList(request.learningObjectives()));
						if (mcResponse == null || !mcResponse.isSuccess()) {
							String raw = mcResponse == null ? "no response" : mcResponse.getError();
							// Log the full raw body for developers, but surface a
							// short human message via mcFailure for the UI.
							log.warn("MC generation for job {} did not succeed: {}", jobKey, raw);
							mcFailure = humanizeProviderError("mc", raw);
						} else if (mcResponse.getQuestions() != null) {
							for (MCQuestionData q : mcResponse.getQuestions()) {
								if (q != null && q.getQuestion() != null
										&& !q.getCorrectAnswers().isEmpty()) {
									acceptedMcQuestions.add(q);
								}
							}
						}
					} catch (Exception mcEx) {
						String raw = mcEx.getMessage() == null
								? mcEx.getClass().getSimpleName() : mcEx.getMessage();
						mcFailure = humanizeProviderError("mc", raw);
						log.error("MC generation leg of job {} failed — continuing with essays only", jobKey, mcEx);
					}
				}
			}

			// ------------------------------------------------------------------
			// Decide overall outcome. If we got nothing useful in either leg,
			// mark FAILED. Otherwise DONE with possibly partial results.
			// Special case: if the caller opted out of BOTH legs (counts of 0)
			// we treat that as an intentional no-op and mark DONE — the sink
			// can then clear the placeholder gracefully.
			// ------------------------------------------------------------------
			boolean bothLegsSkippedByRequest = essaySkippedByRequest
					&& (wantsMc ? mcSkippedByRequest : true);
			boolean everythingFailed = !bothLegsSkippedByRequest
					&& acceptedDrafts.isEmpty() && acceptedMcQuestions.isEmpty()
					&& (essayFailure != null || !wantsMc)
					&& (!wantsMc || mcFailure != null);
			if (everythingFailed) {
				String reason = "essay=" + (essayFailure == null ? "none" : essayFailure)
						+ "; mc=" + (mcFailure == null ? "n/a" : mcFailure);
				markFailed(jobKey, reason);
				notifySinkFailed(request, reason);
				return;
			}

			job = generationJobDao.loadByKey(jobKey);
			job.setState(State.DONE);
			Map<String, Object> progress = new java.util.HashMap<>();
			progress.put("accepted", accepted);
			progress.put("rejected", rejected);
			progress.put("rejectionReasons", rejectionReasons);
			progress.put("chunkCount", chunks.size());
			progress.put("mcAccepted", acceptedMcQuestions.size());
			if (essayFailure != null) {
				progress.put("essayFailure", essayFailure);
			}
			if (mcFailure != null) {
				progress.put("mcFailure", mcFailure);
			}
			job.setProgressJson(toJson(progress));
			generationJobDao.update(job);
			dbInstance.commit();

			// Optional hand-off: attach accepted drafts + MC questions to the
			// destination sink (ceditor QuizPart, question pool, or no-op
			// for the legacy author-drawer flow).
			notifySinkDone(request, acceptedDrafts, draftToGrading, acceptedMcQuestions);

		} catch (Exception e) {
			log.error("Question generation job {} failed mid-run", jobKey, e);
			String shortReason = humanizeProviderError("essay", e.getMessage());
			markFailed(jobKey, shortReason);
			notifySinkFailed(request, shortReason);
			throw e;
		}
	}

	/**
	 * Dispatch to the right sink based on the request destination. {@link
	 * GenerationDestination#QUIZ_PART} delegates to the existing ceditor
	 * sink, {@link GenerationDestination#POOL} hands off to the
	 * question-pool sink, {@code DRAWER} is a silent no-op (drafts persisted
	 * as {@link EssayAiGrading} rows by the caller).
	 */
	private void notifySinkDone(GenerationRequest request, List<EssayItemDraft> acceptedDrafts,
			java.util.Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> acceptedMcQuestions) {
		if (request == null) return;
		switch (request.destination()) {
			case QUIZ_PART -> notifyQuizPartSinkDone(request, acceptedDrafts, draftToGrading, acceptedMcQuestions);
			case POOL -> notifyPoolSinkDone(request, acceptedDrafts, draftToGrading, acceptedMcQuestions);
			case DRAWER -> { /* no-op — author drawer flow */ }
		}
	}

	/**
	 * Dispatch the failed-sink notification to the right destination.
	 */
	private void notifySinkFailed(GenerationRequest request, String reason) {
		if (request == null) return;
		switch (request.destination()) {
			case QUIZ_PART -> notifyQuizPartSinkFailed(request, reason);
			case POOL -> notifyPoolSinkFailed(request, reason);
			case DRAWER -> { /* no-op — author drawer flow */ }
		}
	}

	/**
	 * Hand off accepted drafts and MC questions to the
	 * {@link EssayGenerationPoolSink}. Silent no-op when the bean is missing
	 * (e.g. unit tests without the qpool module on the classpath). Errors
	 * are logged and swallowed to avoid taking the background job down.
	 */
	private void notifyPoolSinkDone(GenerationRequest request, List<EssayItemDraft> acceptedDrafts,
			java.util.Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> acceptedMcQuestions) {
		try {
			EssayGenerationPoolSink sink = org.olat.core.CoreSpringFactory.getImpl(EssayGenerationPoolSink.class);
			if (sink != null) {
				sink.persistGeneratedItems(request.requester(), acceptedDrafts,
						draftToGrading == null ? java.util.Map.of() : draftToGrading,
						acceptedMcQuestions,
						request.language() == null ? Locale.ENGLISH : request.language(),
						request.taxonomyLevelKey());
			}
		} catch (Exception e) {
			log.warn("Pool sink notify (done) failed for requester={}: {}",
					request.requester() == null ? null : request.requester().getKey(), e.getMessage());
		}
	}

	/**
	 * Pool sink does not currently surface placeholder items, so the failure
	 * notification is logged for diagnostics only. Kept symmetrical with
	 * {@link #notifyQuizPartSinkFailed(GenerationRequest, String)} so future
	 * placeholder-style UX (e.g. a flash bar in the pool) has a hook.
	 */
	private void notifyPoolSinkFailed(GenerationRequest request, String reason) {
		log.info("Pool generation job for requester={} failed: {}",
				request.requester() == null ? null : request.requester().getKey(), reason);
	}

	/**
	 * If the request originated from the Markdown-import → QuizPart flow,
	 * look up the optional ceditor-side sink bean and hand off the accepted
	 * essay drafts and MC questions. Missing sink or missing keys are silent
	 * no-ops. The sink interleaves the two lists into a MC-essay-MC-essay
	 * sequence in the QuizPart's question list.
	 */
	private void notifyQuizPartSinkDone(GenerationRequest request, List<EssayItemDraft> acceptedDrafts,
			java.util.Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> acceptedMcQuestions) {
		if (request == null || request.pageKey() == null || request.quizPartKey() == null) {
			return;
		}
		try {
			EssayGenerationQuizPartSink sink = org.olat.core.CoreSpringFactory
					.getImpl(EssayGenerationQuizPartSink.class);
			if (sink != null) {
				sink.attachDraftsAsEssayItems(request.pageKey(), request.quizPartKey(),
						acceptedDrafts,
						draftToGrading == null ? java.util.Map.of() : draftToGrading,
						acceptedMcQuestions,
						request.language() == null ? Locale.ENGLISH : request.language());
			}
		} catch (Exception e) {
			log.warn("QuizPart sink notify (done) failed for pageKey={} quizPartKey={}: {}",
					request.pageKey(), request.quizPartKey(), e.getMessage());
		}
	}

	private void notifyQuizPartSinkFailed(GenerationRequest request, String reason) {
		if (request == null || request.pageKey() == null || request.quizPartKey() == null) {
			return;
		}
		try {
			EssayGenerationQuizPartSink sink = org.olat.core.CoreSpringFactory
					.getImpl(EssayGenerationQuizPartSink.class);
			if (sink != null) {
				sink.markGenerationFailed(request.pageKey(), request.quizPartKey(),
						reason == null ? "unknown" : reason);
			}
		} catch (Exception e) {
			log.warn("QuizPart sink notify (failed) for pageKey={} quizPartKey={}: {}",
					request.pageKey(), request.quizPartKey(), e.getMessage());
		}
	}

	/**
	 * Build an {@link EssayAiGrading} POJO for the accepted draft. The
	 * {@code assessmentItemIdentifier} is left {@code null} — the ceditor
	 * sink fills it in (= question directory name) before writing
	 * {@code ai-grading.json} next to the QTI item XML. The
	 * {@code contentHash} is recomputed by the sink right before the file
	 * is written so the integrity check at grading time succeeds.
	 */
	private EssayAiGrading buildAcceptedGrading(GenerationRequest request, EssayItemDraft draft,
			String generatorSpiId, String generatorModel) {
		EssayAiGrading grading = new EssayAiGrading();
		grading.setLanguage(request.language() == null ? "en" : request.language().getLanguage());
		grading.setReferenceExcerpt(nullToEmpty(draft.referenceExcerpt()));
		grading.setModelAnswer(nullToEmpty(draft.modelAnswer()));
		grading.setKeyPointsJson(toJson(draft.keyPoints()));
		grading.setRubricCriteriaJson(toJson(draft.rubricCriteria()));
		grading.setBloomLevel(draft.bloomLevel() == null ? null : draft.bloomLevel().name());
		grading.setLearningObjective(draft.learningObjective());
		grading.setTokenEstimate(draft.tokenEstimate());
		grading.setGradingHints(nullToEmpty(draft.gradingHints()));
		grading.setDifficulty(draft.difficulty() == 0 ? null : draft.difficulty());
		grading.setCommonMisconceptionsJson(toJson(draft.commonMisconceptions()));
		grading.setGeneratorSpi(generatorSpiId);
		grading.setGeneratorModel(generatorModel);
		grading.setContentHash(EssayFormativeFeedbackService.computeContentHash(grading));
		return grading;
	}

	private void markFailed(Long jobKey, String reason) {
		EssayGenerationJob job = generationJobDao.loadByKey(jobKey);
		if (job == null) return;
		job.setState(State.FAILED);
		String safeReason = reason == null ? "unknown" : truncate(reason, PROGRESS_ERROR_MAX);
		job.setErrorJson(toJson(Map.of("reason", safeReason)));
		generationJobDao.update(job);
		dbInstance.commit();
	}

	/**
	 * Status view returned to the drawer / pool import poller. The owner
	 * check is mandatory: the progress JSON contains generated essay drafts
	 * (model answers, key points, rubric) until they are detached, so a leak
	 * via guessed PKs would expose another author's in-flight work and burn
	 * the same content twice. When the caller is not the job owner the
	 * response is indistinguishable from "not found".
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
		EssayGenerationJob job = generationJobDao.loadByKey(jobKey);
		if (job == null) {
			return new JobStatusView(jobKey, null, null, null);
		}
		Long ownerKey = job.getCreatedBy() == null ? null : job.getCreatedBy().getKey();
		if (ownerKey == null || !ownerKey.equals(caller.getKey())) {
			// Indistinguishable from "not found". Do not log the caller.
			return new JobStatusView(jobKey, null, null, null);
		}
		return new JobStatusView(jobKey, job.getState(), job.getProgressJson(), job.getErrorJson());
	}

	/**
	 * Trusted internal status lookup that bypasses the owner check. Reserved
	 * for code paths that have already established ownership / admin
	 * authority (background runners, admin diagnostics). Public callers must
	 * use {@link #getStatus(Long, Identity)} instead.
	 */
	JobStatusView getStatusInternal(Long jobKey) {
		EssayGenerationJob job = generationJobDao.loadByKey(jobKey);
		if (job == null) {
			return new JobStatusView(jobKey, null, null, null);
		}
		return new JobStatusView(jobKey, job.getState(), job.getProgressJson(), job.getErrorJson());
	}

	/**
	 * Per-user rate limiter for the AI question-generation submit path.
	 * Counts {@link AiFeature#EssayGeneration} usage log rows recorded for
	 * {@code caller} in the last 60 seconds; throws
	 * {@link AiRateLimitExceededException} once the configured threshold is
	 * reached. Each generation call drives multiple expensive provider
	 * invocations, so the limit is significantly tighter than the grading
	 * one.
	 */
	private void assertWithinRateLimit(Identity caller) {
		if (caller == null || caller.getKey() == null) {
			return;
		}
		int limit = aiModule == null ? 10 : aiModule.getEssayGenerationMaxCallsPerMinutePerUser();
		if (limit <= 0) {
			return;
		}
		Date since = new Date(System.currentTimeMillis() - 60_000L);
		int count = aiUsageLogDao.countByIdentityFeatureSince(caller.getKey(),
				AiFeature.EssayGeneration.getType(), since);
		if (count >= limit) {
			String message = "essay generation rate limit exceeded for identity " + caller.getKey()
					+ " (" + count + " >= " + limit + " per minute)";
			// Record the refusal so it counts toward the limiter on subsequent
			// submits and shows up in cost / abuse reports.
			AiUsageContext usageContext = AiUsageContext.builder()
					.usageContextType(USAGE_CONTEXT_TYPE)
					.identity(caller)
					.build();
			aiUsageLogDao.createGuardLog(AiFeature.EssayGeneration.getType(), usageContext,
					"RateLimited", message);
			throw new AiRateLimitExceededException(message);
		}
	}

	private static <T> List<T> safeList(List<T> list) {
		return list == null ? Collections.emptyList() : list;
	}

	private String toJson(Object o) {
		try {
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			log.warn("Failed to serialise object for essay generation job: {}", e.getMessage());
			return "{}";
		}
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

	/**
	 * Maximum length for user-visible error messages in the progress JSON. Raw
	 * provider bodies (HTML pages, stack traces, multi-line JSON) are truncated
	 * with an ellipsis so the drawer UI stays readable. The untruncated body is
	 * still available in the server logs via {@code log.error}.
	 */
	static final int PROGRESS_ERROR_MAX = 500;

	/**
	 * Regex matching HTML-ish error bodies (nginx 4xx/5xx pages, Cloudflare
	 * blocks, SSO redirects) that leak into exception messages when the MC /
	 * essay HTTP call to the provider is rejected upstream.
	 */
	private static final java.util.regex.Pattern HTML_BODY =
			java.util.regex.Pattern.compile("(?is)^\\s*<(?:!doctype|html|head|body)\\b|<title[^>]*>");

	/**
	 * Regex extracting the first HTTP-ish status code from an HTML error body
	 * (e.g. {@code <title>403 Forbidden</title>} or
	 * {@code <h1>502 Bad Gateway</h1>}). Only 4xx/5xx are matched — anything
	 * else falls back to the generic upstream-rejection message.
	 */
	private static final java.util.regex.Pattern HTML_STATUS =
			java.util.regex.Pattern.compile("\\b([45]\\d{2})\\s+([A-Za-z][A-Za-z0-9 \\-]{2,40})");

	/**
	 * Turn a raw provider error body into a short human-readable message for
	 * the progress JSON. If {@code raw} looks like an HTML page we extract the
	 * status code (if any) and replace the body with a concise operator hint;
	 * otherwise we just truncate at {@link #PROGRESS_ERROR_MAX} characters.
	 * The full body still lives in the server logs via the {@code log.error}
	 * call at the catch site — this method only shapes the UI-bound copy.
	 *
	 * @param leg  {@code "essay"} or {@code "mc"} — used to pick the config
	 *             key name mentioned in the hint
	 * @param raw  raw exception message / response body (may be {@code null})
	 */
	static String humanizeProviderError(String leg, String raw) {
		if (raw == null || raw.isBlank()) {
			return "unknown";
		}
		String trimmed = raw.trim();
		if (HTML_BODY.matcher(trimmed).find()) {
			String configKey = "mc".equals(leg)
					? "ai.feature.mc.question.generator"
					: "ai.feature.essay.question.generator";
			java.util.regex.Matcher m = HTML_STATUS.matcher(trimmed);
			if (m.find()) {
				String code = m.group(1);
				String text = m.group(2).trim();
				return legLabel(leg) + " schlug fehl: Provider gab " + code + " " + text
						+ " zur\u00FCck. Bitte SPI-Konfiguration f\u00FCr "
						+ configKey + " pr\u00FCfen.";
			}
			return legLabel(leg) + " schlug fehl: Provider gab eine HTML-Fehlerseite zur\u00FCck. "
					+ "Bitte SPI-Konfiguration f\u00FCr " + configKey + " pr\u00FCfen.";
		}
		return truncate(trimmed, PROGRESS_ERROR_MAX);
	}

	private static String legLabel(String leg) {
		return "mc".equals(leg) ? "MC-Frage-Generierung" : "Essay-Frage-Generierung";
	}

	private static String truncate(String s, int max) {
		if (s == null) return null;
		if (s.length() <= max) return s;
		return s.substring(0, max - 3) + "...";
	}

	/**
	 * Where the generated questions should land. {@link #QUIZ_PART} attaches
	 * them to a ceditor QuizPart on a page, {@link #POOL} stores them in the
	 * question pool as standalone items. {@link #DRAWER} is the legacy author
	 * drawer flow that only produces essays and leaves them as
	 * {@link EssayAiGrading} rows.
	 */
	public enum GenerationDestination {
		DRAWER, QUIZ_PART, POOL
	}

	/**
	 * Generator request. {@code targetQuestionCount <= 0} means "do not
	 * generate essay questions" (opt-out). For legacy callers that don't set
	 * a count, {@link #DEFAULT_QUESTION_COUNT} is used as a fallback inside
	 * {@link #runJob(Long)} (value {@code -1} or unspecified paths).
	 * <p>
	 * {@code mcQuestionCount} behaves the same way for the MC generation leg
	 * of the Markdown-import → QuizPart flow: {@code 0} means "skip MC",
	 * {@link #DEFAULT_MC_QUESTION_COUNT} is the fallback for unspecified /
	 * legacy callers (negative values).
	 * <p>
	 * {@code pageKey} and {@code quizPartKey} are optional hand-off hints for
	 * the ceditor Markdown-import flow: when both are set the completion hook
	 * attaches accepted drafts as QTI essay items on the given QuizPart. When
	 * either is {@code null} the drafts are persisted as {@link EssayAiGrading}
	 * rows only (default author-drawer flow).
	 * <p>
	 * {@code destination} chooses the completion-hook sink. {@link
	 * GenerationDestination#QUIZ_PART} and {@link GenerationDestination#POOL}
	 * dispatch to two different Spring beans
	 * ({@link EssayGenerationQuizPartSink} and {@link EssayGenerationPoolSink}).
	 * {@code DRAWER} is the legacy default — no item persistence.
	 * <p>
	 * {@code taxonomyLevelKey} is consumed by the pool sink to stamp the
	 * created question items with a taxonomy. {@code null} means "no
	 * taxonomy". For the QuizPart and DRAWER destinations the field is
	 * ignored.
	 */
	public record GenerationRequest(
			String pageMarkdown,
			Long repositoryEntryKey,
			int targetQuestionCount,
			int mcQuestionCount,
			List<AiBloomLevel> targetBloomLevels,
			Integer targetDifficulty,
			List<String> learningObjectives,
			Locale language,
			Identity requester,
			Long pageKey,
			Long quizPartKey,
			GenerationDestination destination,
			Long taxonomyLevelKey) {

		/**
		 * Compact constructor — back-fills {@code destination} so existing
		 * callers passing the legacy 10-arg form (via reflection, payload
		 * deserialisation, or explicit {@code new GenerationRequest(...)})
		 * keep working. {@code destination == null} falls back to
		 * {@code QUIZ_PART} when both keys are set, otherwise {@code DRAWER}.
		 */
		public GenerationRequest {
			if (destination == null) {
				destination = (pageKey != null && quizPartKey != null)
						? GenerationDestination.QUIZ_PART : GenerationDestination.DRAWER;
			}
		}

		/** Convenience factory used by simple callers in the author UI. */
		public static GenerationRequest of(String pageMarkdown, Long repositoryEntryKey,
				Locale language, Identity requester) {
			return new GenerationRequest(pageMarkdown, repositoryEntryKey,
					DEFAULT_QUESTION_COUNT, 0,
					Arrays.asList(AiBloomLevel.UNDERSTAND, AiBloomLevel.APPLY),
					null, List.of(), language, requester, null, null,
					GenerationDestination.DRAWER, null);
		}

		/**
		 * Convenience factory for the Markdown-import → QuizPart flow. The
		 * completion hook will attach accepted drafts as QTI essay items to
		 * the given QuizPart.
		 * <p>
		 * Pass {@code 0} for either count to opt out of that leg. Negative
		 * values are treated as "unspecified" and fall back to the
		 * {@code DEFAULT_*_QUESTION_COUNT} constants at run time.
		 */
		public static GenerationRequest forQuizPart(String pageMarkdown, Long repositoryEntryKey,
				Locale language, Identity requester, Long pageKey, Long quizPartKey,
				int essayQuestionCount, int mcQuestionCount) {
			return forQuizPart(pageMarkdown, repositoryEntryKey, language, requester,
					pageKey, quizPartKey, essayQuestionCount, mcQuestionCount,
					null, null, null);
		}

		/**
		 * Extended factory for the Markdown-import → QuizPart flow with
		 * author-controllable Bloom levels, target difficulty, and learning
		 * objectives.
		 * <p>
		 * When {@code targetBloomLevels} is {@code null} or empty the default
		 * {@code [UNDERSTAND, APPLY]} is used. When {@code learningObjectives}
		 * is {@code null} an empty list is used. {@code targetDifficulty} may
		 * be {@code null} (LLM self-assigns difficulty).
		 */
		public static GenerationRequest forQuizPart(String pageMarkdown, Long repositoryEntryKey,
				Locale language, Identity requester, Long pageKey, Long quizPartKey,
				int essayQuestionCount, int mcQuestionCount,
				List<AiBloomLevel> targetBloomLevels, Integer targetDifficulty,
				List<String> learningObjectives) {
			int essayCount = essayQuestionCount < 0 ? 0 : essayQuestionCount;
			int mcCount = mcQuestionCount < 0 ? 0 : mcQuestionCount;
			List<AiBloomLevel> bloomLevels = (targetBloomLevels == null || targetBloomLevels.isEmpty())
					? Arrays.asList(AiBloomLevel.UNDERSTAND, AiBloomLevel.APPLY)
					: targetBloomLevels;
			List<String> objectives = learningObjectives == null ? List.of() : learningObjectives;
			return new GenerationRequest(pageMarkdown, repositoryEntryKey,
					essayCount, mcCount,
					bloomLevels, targetDifficulty, objectives,
					language, requester, pageKey, quizPartKey,
					GenerationDestination.QUIZ_PART, null);
		}

		/**
		 * Convenience factory for the question-pool import flow. The
		 * completion hook persists accepted drafts and MC questions as
		 * fresh question items owned by {@code requester}.
		 * <p>
		 * Pass {@code 0} for either count to opt out of that leg. Negative
		 * values are treated as "unspecified" and fall back to the
		 * {@code DEFAULT_*_QUESTION_COUNT} constants at run time.
		 *
		 * @param taxonomyLevelKey optional taxonomy level the items should
		 *                         be stamped with; may be {@code null}
		 */
		public static GenerationRequest forPool(String sourceText, Long repositoryEntryKey,
				Locale language, Identity requester,
				int essayQuestionCount, int mcQuestionCount, Long taxonomyLevelKey) {
			return forPool(sourceText, repositoryEntryKey, language, requester,
					essayQuestionCount, mcQuestionCount, taxonomyLevelKey,
					null, null, null);
		}

		/**
		 * Extended factory for the question-pool import flow with
		 * author-controllable Bloom levels, target difficulty, and learning
		 * objectives.
		 * <p>
		 * When {@code targetBloomLevels} is {@code null} or empty the default
		 * {@code [UNDERSTAND, APPLY]} is used. When {@code learningObjectives}
		 * is {@code null} an empty list is used. {@code targetDifficulty} may
		 * be {@code null} (LLM self-assigns difficulty).
		 *
		 * @param taxonomyLevelKey optional taxonomy level the items should
		 *                         be stamped with; may be {@code null}
		 */
		public static GenerationRequest forPool(String sourceText, Long repositoryEntryKey,
				Locale language, Identity requester,
				int essayQuestionCount, int mcQuestionCount, Long taxonomyLevelKey,
				List<AiBloomLevel> targetBloomLevels, Integer targetDifficulty,
				List<String> learningObjectives) {
			int essayCount = essayQuestionCount < 0 ? 0 : essayQuestionCount;
			int mcCount = mcQuestionCount < 0 ? 0 : mcQuestionCount;
			List<AiBloomLevel> bloomLevels = (targetBloomLevels == null || targetBloomLevels.isEmpty())
					? Arrays.asList(AiBloomLevel.UNDERSTAND, AiBloomLevel.APPLY)
					: targetBloomLevels;
			List<String> objectives = learningObjectives == null ? List.of() : learningObjectives;
			return new GenerationRequest(sourceText, repositoryEntryKey,
					essayCount, mcCount,
					bloomLevels, targetDifficulty, objectives,
					language, requester, null, null,
					GenerationDestination.POOL, taxonomyLevelKey);
		}
	}

	/** Status view returned to the drawer UI — JSON blobs are returned raw. */
	public record JobStatusView(Long jobKey, EssayGenerationJob.State state,
			String progressJson, String errorJson) { }
}
