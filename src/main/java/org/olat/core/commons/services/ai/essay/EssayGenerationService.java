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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.ai.AiEssayGenerationService;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiMCQuestionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.content.AiContentChunker;
import org.olat.core.commons.services.ai.content.AiContentHardener;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Public entry point for AI-assisted question generation from page
 * content. Schedules a self-contained {@link QtiQuestionGenerationTask} on the
 * generic persisted task executor ({@code o_ex_task}). Despite "essay" in
 * the name the task can produce mixed essay + multiple-choice output for
 * the ceditor Markdown-import → QuizPart flow.
 * <p>
 * There is no job table and no status API: the destination sinks
 * ({@link EssayGenerationQuizPartSink}, {@link EssayGenerationPoolSink})
 * carry all user-visible outcomes, and execution state lives on the
 * executor's task row (failed tasks stay visible in the task admin list).
 * <p>
 * The {@link #runTask(QtiQuestionGenerationTask)} method is the body of the
 * LongRunnable — it is exposed on the service (not the runnable) so the
 * runnable stays serialisable and Spring-managed services can be accessed
 * via {@code CoreSpringFactory}.
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
	private AiEssayGenerationService aiEssayGenerationService;
	@Autowired
	private AiMCQuestionService aiMCQuestionService;
	@Autowired
	private AiContentChunker contentChunker;
	@Autowired
	private AiContentHardener contentHardener;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	@Autowired
	private BaseSecurity baseSecurity;
	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDao;

	private final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Schedule the generation request for asynchronous execution on the
	 * generic persisted task executor. The full request payload travels
	 * inside the serialised {@link QtiQuestionGenerationTask}; there is nothing
	 * to poll — the destination sink reports the outcome.
	 */
	public void submit(GenerationRequest request) {
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

		RepositoryEntry entry = RepositoryManager.getInstance()
				.lookupRepositoryEntry(request.repositoryEntryKey(), false);
		OLATResource resource = entry == null ? null : entry.getOlatResource();
		taskExecutorManager.execute(new QtiQuestionGenerationTask(request), request.requester(),
				resource, null, null);
	}

	/**
	 * Execute the task body. Called from {@link QtiQuestionGenerationTask#run()}
	 * but also reachable directly for synchronous execution in tests.
	 * Throws on total failure so the task executor marks the task row
	 * failed; partial successes are handed to the sink and count as done.
	 */
	public void runTask(QtiQuestionGenerationTask task) {
		if (task == null) return;
		Identity requester = task.getRequesterKey() == null ? null
				: baseSecurity.loadIdentityByKey(task.getRequesterKey());
		if (requester == null) {
			log.warn("Essay generation task has no resolvable requester (key={}) — skipping",
					task.getRequesterKey());
			return;
		}
		GenerationRequest request = task.toGenerationRequest(requester);

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
				String reason = "no content chunks produced from page markdown";
				notifySinkFailed(request, reason);
				throw new AiEssayGenerationException(reason);
			}

			// Destination-derived usage-context fields, shared by both
			// generation legs so each provider call is logged against the same
			// calling context (the feature — essay vs MC — is set separately
			// inside each service). Without this the essay leg passed a null
			// context and its log row was lost.
			// A persisted task can carry a null destination when an unknown /
			// blank value failed to deserialise (toGenerationRequest leaves it
			// null and logs a warning); fail fast with a clear reason instead
			// of letting the switch below throw a bare NPE.
			if (request.destination() == null) {
				throw new IllegalStateException(
						"generation request has no destination — cannot determine usage context");
			}
			String genContextType = switch (request.destination()) {
				case POOL -> "qpool-generate-questions";
				case QUIZ_PART -> "ceditor-quizpart-generate-questions";
				case DRAWER -> "ai-drawer-generate-questions";
			};
			String genResourceType = request.destination() == GenerationDestination.POOL
					? "PoolQPool" : "RepositoryEntry";
			long genResourceId = request.destination() == GenerationDestination.POOL
					? (request.taxonomyLevelKey() == null ? 0L : request.taxonomyLevelKey())
					: (request.repositoryEntryKey() == null ? 0L : request.repositoryEntryKey());
			Locale genLocale = request.language() == null ? Locale.ENGLISH : request.language();

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
				log.info("Essay generation skipped for requester {} — targetQuestionCount=0", requester.getKey());
			} else if (!aiEssayGenerationService.isEnabled()) {
				essayFailure = "essay generation is not configured or enabled";
				log.warn("Essay generation disabled for requester {} — skipping essay part", requester.getKey());
				// Visible trace: without this the not-configured case left no row
				// at all, so the feature looked like it was never invoked (parity
				// with EssayFormativeFeedbackService's GradingNotConfigured guard).
				aiUsageLogDao.createGuardLog(AiFeature.EssayGeneration.getType(),
						AiUsageContext.builder()
								.usageContextType(genContextType)
								.identity(request.requester())
								.locale(genLocale)
								.resourceType(genResourceType)
								.resourceId(genResourceId)
								.build(),
						"GenerationNotConfigured", essayFailure);
			} else {
				try {
					int essayCount = request.targetQuestionCount() > 0
							? request.targetQuestionCount() : DEFAULT_QUESTION_COUNT;
					AiUsageContext essayUsageContext = AiUsageContext.builder()
							.usageContextType(genContextType)
							.identity(request.requester())
							.locale(genLocale)
							.resourceType(genResourceType)
							.resourceId(genResourceId)
							.build();
					List<EssayItemDraft> drafts = aiEssayGenerationService.generateEssayQuestions(
							essayUsageContext,
							chunks,
							safeList(request.learningObjectives()),
							safeList(request.targetBloomLevels()),
							request.targetDifficulty(),
							essayCount,
							genLocale);

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
					log.error("Essay generation leg for requester {} failed — will still try MC leg",
							requester.getKey(), essayEx);
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
				log.info("MC question generation skipped for requester {} — mcQuestionCount=0", requester.getKey());
			} else if (wantsMc) {
				if (aiMCQuestionService == null || !aiMCQuestionService.isEnabled()) {
					mcFailure = "MC question generation is not configured or enabled";
					log.info("MC question generation disabled for requester {} — skipping MC part", requester.getKey());
					aiUsageLogDao.createGuardLog(AiFeature.MCQuestionGenerator.getType(),
							AiUsageContext.builder()
									.usageContextType(genContextType)
									.identity(request.requester())
									.locale(genLocale)
									.resourceType(genResourceType)
									.resourceId(genResourceId)
									.build(),
							"GenerationNotConfigured", mcFailure);
				} else {
					try {
						int mcCount = request.mcQuestionCount() > 0
								? request.mcQuestionCount() : DEFAULT_MC_QUESTION_COUNT;
						AiUsageContext usageContext = AiUsageContext.builder()
								.usageContextType(genContextType)
								.identity(request.requester())
								.locale(genLocale)
								.resourceType(genResourceType)
								.resourceId(genResourceId)
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
							log.warn("MC generation for requester {} did not succeed: {}", requester.getKey(), raw);
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
						log.error("MC generation leg for requester {} failed — continuing with essays only",
								requester.getKey(), mcEx);
					}
				}
			}

			// ------------------------------------------------------------------
			// Decide overall outcome. If we got nothing useful in either leg,
			// fail the task. Otherwise done with possibly partial results.
			// Special case: if the caller opted out of BOTH legs (counts of 0)
			// we treat that as an intentional no-op — the sink can then clear
			// the placeholder gracefully.
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
				notifySinkFailed(request, reason);
				throw new AiEssayGenerationException(reason);
			}

			log.info("Question generation for requester {} done: accepted={} rejected={} mcAccepted={} chunks={}{}{}",
					requester.getKey(), accepted, rejected, acceptedMcQuestions.size(), chunks.size(),
					essayFailure == null ? "" : " essayFailure=" + essayFailure,
					mcFailure == null ? "" : " mcFailure=" + mcFailure);
			if (!rejectionReasons.isEmpty()) {
				log.info("Question generation for requester {} rejected drafts: {}",
						requester.getKey(), rejectionReasons);
			}

			// Optional hand-off: attach accepted drafts + MC questions to the
			// destination sink (ceditor QuizPart, question pool, or no-op
			// for the legacy author-drawer flow).
			notifySinkDone(request, acceptedDrafts, draftToGrading, acceptedMcQuestions);

		} catch (AiEssayGenerationException e) {
			// Sink already notified — re-throw so the task executor marks the
			// task row failed.
			throw e;
		} catch (Exception e) {
			log.error("Question generation for requester {} failed mid-run", requester.getKey(), e);
			String shortReason = humanizeProviderError("essay", e.getMessage());
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
	 * Keywords marking a provider rejection caused by the request exceeding
	 * the model context window (input source material plus reserved output
	 * too large). Covers the common OpenAI, Anthropic and generic phrasings.
	 * Matched before the HTML check so a JSON error body with one of these
	 * codes still maps to the precise hint.
	 */
	private static final java.util.regex.Pattern CONTEXT_LENGTH =
			java.util.regex.Pattern.compile("(?is)context[ _]length|context window|prompt is too long|"
					+ "maximum context|too many tokens|reduce the length of the messages|"
					+ "input length exceeds");

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
		if (CONTEXT_LENGTH.matcher(trimmed).find()) {
			return legLabel(leg) + " schlug fehl: Das Quellmaterial ist zu gross für das Kontextfenster "
					+ "des gewählten Modells. Bitte die Auswahl reduzieren oder die Quelle aufteilen.";
		}
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
	 * {@link #runTask(QtiQuestionGenerationTask)} (value {@code -1} or unspecified paths).
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
}
