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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiEssayGradingService;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.manager.AiEssayGradingServiceImpl;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Synchronous entry point for the formative-feedback flow. Loads the
 * persisted {@link EssayAiGrading}, runs the pre-filters, invokes the
 * grader SPI, sanitises the student-facing free-form text, and persists
 * the grading-run provenance on the {@link EssayAiCorrection} row.
 * <p>
 * The grading call runs inline on the calling thread (normally an
 * {@code aiInteractive} task-executor worker). The hard time bound is the
 * HTTP client timeout configured in {@link AiEssayGradingServiceImpl} —
 * timeout causes are mapped to {@link EssayGradingTimeoutException} so the
 * correction row gets the TIMEOUT status.
 * <p>
 * Call sequence:
 * <ol>
 *   <li>Load grading + verify content hash.</li>
 *   <li>{@link LengthPreFilter} → {@link GibberishPreFilter} → {@link LanguagePreFilter}.</li>
 *   <li>Tier classify; Lang tier refused inline.</li>
 *   <li>Router → {@link AiEssayGradingSPI}.</li>
 *   <li>SPI call via {@link AiEssayGradingServiceImpl}, bounded by the HTTP timeout.</li>
 *   <li>Free-form text sanitised via OpenOlat's XSS filter.</li>
 *   <li>Grading-run provenance written to the {@code EssayAiCorrection} row.</li>
 * </ol>
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayFormativeFeedbackService {
	private static final Logger log = Tracing.createLoggerFor(EssayFormativeFeedbackService.class);

	/**
	 * Usage-context type stamped on the {@code AiUsageLog} row. The matching
	 * {@code usageContextId} is the {@link EssayAiCorrection} key — the
	 * correction is the context of the AI call, so the log points back to it
	 * via this (type, id) pair instead of carrying essay-specific columns.
	 */
	static final String USAGE_CONTEXT_TYPE = "ai-essay-correction";

	@Autowired
	private AiEssayGradingService aiEssayGradingService;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;
	@Autowired
	private EssayAiCorrectionDao correctionDao;

	/**
	 * Grade a student essay answer synchronously against a pre-loaded
	 * {@link EssayAiGrading} POJO (loaded from {@code ai-grading.json} by
	 * the caller via {@link EssayAiGradingFileStore#load(java.io.File)}).
	 * <p>
	 * {@code session} may be {@code null} when the call is triggered
	 * outside an assessment run (preview, unit test). {@code correctionKey}
	 * is the {@link EssayAiCorrection} this grading belongs to; it is used as
	 * the usage-context id on the log row and as the target for the
	 * grading-run provenance (content hash, prompt template version, tier).
	 * It may be {@code null} for preview / unit-test calls that have no
	 * persisted correction row.
	 */
	public FormativeFeedback grade(Long correctionKey, EssayAiGrading grading, String studentAnswer,
			AssessmentItemSession session, Identity student, Locale locale) {
		if (grading == null) {
			throw new IllegalArgumentException("grading must not be null");
		}
		verifyContentHash(grading);

		Locale effectiveLocale = locale != null ? locale : Locale.ENGLISH;

		// Build the usage context up-front so pre-filter rejections can be
		// attributed to the right identity and resource in o_ai_usage_log.
		// (G3) Pre-filter refusals must be visible to the rate limiter, which
		// counts log rows for this identity + feature.
		AiUsageContext.Builder usageContextBuilder = AiUsageContext.builder()
				.usageContextType(USAGE_CONTEXT_TYPE)
				.usageContextId(correctionKey == null ? null : correctionKey.toString())
				.identity(student)
				.locale(effectiveLocale);
		if (session != null) {
			AssessmentTestSession testSession = session.getAssessmentTestSession();
			RepositoryEntry resourceEntry = testSession == null ? null
					: testSession.getRepositoryEntry();
			if (resourceEntry != null && resourceEntry.getKey() != null) {
				usageContextBuilder
						.resourceType("RepositoryEntry")
						.resourceId(resourceEntry.getKey());
			}
		}
		AiUsageContext usageContext = usageContextBuilder.build();

		// 1. Length pre-filter. Long tier refused with inline error (no LLM call).
		Optional<RejectionReason> lengthReject = LengthPreFilter.check(studentAnswer);
		if (lengthReject.isPresent()) {
			RejectionReason reason = lengthReject.get();
			logPreFilterRejection(usageContext, reason);
			if (LengthPreFilter.REASON_TOO_LONG.equals(reason.messageKey())) {
				return FormativeFeedback.refusedLong(reason);
			}
			return FormativeFeedback.rejected(reason);
		}

		// 2. Gibberish pre-filter.
		Optional<RejectionReason> gibberishReject = GibberishPreFilter.check(studentAnswer);
		if (gibberishReject.isPresent()) {
			RejectionReason reason = gibberishReject.get();
			logPreFilterRejection(usageContext, reason);
			return FormativeFeedback.rejected(reason);
		}

		// 3. Language warning (non-fatal).
		List<RejectionReason> warnings = new ArrayList<>();
		LanguagePreFilter.check(studentAnswer, grading.getLanguage()).ifPresent(warnings::add);

		// 4. Tier.
		AiGradingTier tier = AiGradingTier.classify(studentAnswer);
		if (tier == AiGradingTier.LONG) {
			// Defensive — LengthPreFilter should already have caught this.
			RejectionReason reason = new RejectionReason(
					LengthPreFilter.REASON_TOO_LONG, "tier classified as LONG");
			logPreFilterRejection(usageContext, reason);
			return FormativeFeedback.refusedLong(reason);
		}

		// 5. Precondition: grading must be configured.
		if (!aiEssayGradingService.isEnabled()) {
			aiUsageLogDAO.createGuardLog(AiFeature.EssayGrading.getType(), usageContext,
					"GradingNotConfigured", "Essay grading is not configured or not enabled.");
			throw new AiEssayGradingException("Essay grading is not configured or not enabled.");
		}

		// Grading-run provenance on the correction row (the context of the AI
		// call). Written before the SPI call so it is recorded for every
		// outcome — success, truncated response, timeout or error.
		recordProvenance(correctionKey, grading, tier);

		// 6. Service call, bounded by the HTTP client timeout of the SPI.
		AiEssayGradingService.GradingRun run;
		try {
			run = aiEssayGradingService.gradeWithLog(
					usageContext, grading, studentAnswer, effectiveLocale, tier);
		} catch (AiEssayResponseTruncatedException e) {
			// The LLM reply was cut off (typically max_tokens) and Jackson failed
			// to parse it. Surface this as a graceful rejection card instead of
			// letting the raw Jackson stack trace bubble up to the job error.
			log.info("Essay grading response truncated for tier {}: {}", tier, e.getMessage());
			// Provenance was already recorded on the correction row before the
			// call, so the truncated-response case is just as analysable as a
			// successful one.
			return FormativeFeedback.rejected(new RejectionReason(
					"ai.essay.error.response.truncated",
					"AI response truncated (tier=" + tier + ")"));
		} catch (AiEssayGradingException e) {
			if (isTimeoutCause(e)) {
				throw new EssayGradingTimeoutException(
						"Essay grading hit the provider HTTP timeout", e);
			}
			throw e;
		}

		// 7. Sanitise free-form student feedback strings.
		GradingSuggestion sanitised = sanitiseForStudent(run.suggestion());

		if (log.isDebugEnabled()) {
			log.debug("Essay graded via spi={} model={} tier={}",
					aiEssayGradingService.getConfiguredSpiId(),
					aiEssayGradingService.getConfiguredModel(), tier);
		}

		return FormativeFeedback.ok(tier, sanitised, warnings, run.usageLogKey());
	}

	/**
	 * Verify that the grading POJO's {@code contentHash} matches a fresh
	 * SHA-256 over the live grading-relevant fields. A mismatch means the
	 * on-disk {@code ai-grading.json} was tampered with or corrupted —
	 * refuse to grade.
	 */
	private void verifyContentHash(EssayAiGrading grading) {
		String fresh = computeContentHash(grading);
		String stored = grading.getContentHash();
		if (stored == null || stored.isBlank() || fresh == null || !stored.equals(fresh)) {
			throw new EssayGradingIntegrityException(
					"Content hash mismatch for grading " + grading.getAssessmentItemIdentifier()
							+ " — refusing to grade");
		}
	}

	/**
	 * Canonical SHA-256 over the grading-relevant fields. Must match the
	 * hash computed at generation time. For MVP we hash the simple
	 * pipe-joined canonicalisation; the senior dev replaces this with the
	 * full canonical JSON path in Phase 2 if the field set grows.
	 */
	public static String computeContentHash(EssayAiGrading grading) {
		String canonical = nullToEmpty(grading.getReferenceExcerpt())
				+ "|" + nullToEmpty(grading.getModelAnswer())
				+ "|" + nullToEmpty(grading.getKeyPointsJson())
				+ "|" + nullToEmpty(grading.getRubricCriteriaJson());
		return sha256Hex16(canonical);
	}

	/**
	 * Stable SHA-256 hex prefix (first 16 chars) of the input. Used for the
	 * grading integrity content hash.
	 */
	public static String sha256Hex16(String input) {
		if (input == null) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				hex.append(String.format(Locale.ROOT, "%02x", b));
			}
			return hex.substring(0, 16);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable on this JVM", e);
		}
	}

	/**
	 * Write a guard log row for a pre-filter rejection so the rate limiter,
	 * cost reporting and abuse-detection queries see every refused submission
	 * — even those that never reached the LLM.
	 */
	private void logPreFilterRejection(AiUsageContext usageContext, RejectionReason reason) {
		String messageKey = reason == null ? null : reason.messageKey();
		String detail = reason == null ? null : reason.detail();
		aiUsageLogDAO.createGuardLog(AiFeature.EssayGrading.getType(), usageContext,
				"PreFilterRejection",
				messageKey == null ? detail : (detail == null ? messageKey : messageKey + ": " + detail));
	}

	/**
	 * Persist the grading-run provenance (content hash, prompt template
	 * version, tier) onto the {@link EssayAiCorrection} row. Best-effort: a
	 * failure here must not abort the grading itself. No-op for preview /
	 * unit-test calls that have no persisted correction ({@code correctionKey}
	 * is {@code null}).
	 */
	private void recordProvenance(Long correctionKey, EssayAiGrading grading, AiGradingTier tier) {
		if (correctionKey == null) {
			return;
		}
		try {
			EssayAiCorrection correction = correctionDao.loadByKey(correctionKey);
			if (correction == null) {
				return;
			}
			correction.setContentHashAtCall(grading.getContentHash());
			correction.setPromptTemplateVersion(aiEssayGradingService.getPromptTemplateVersion());
			correction.setTier(tier);
			correctionDao.update(correction);
		} catch (Exception e) {
			log.warn("Failed to record essay grading provenance on correction {}: {}",
					correctionKey, e.getMessage());
		}
	}

	/**
	 * Walk the cause chain looking for a network/HTTP timeout. The SPI's
	 * HTTP client enforces the hard time bound on the provider call; the
	 * timeout surfaces wrapped in an {@link AiEssayGradingException}, and
	 * this check lets the caller map it to
	 * {@link EssayGradingTimeoutException} (→ TIMEOUT status on the
	 * correction row instead of generic FAILED).
	 */
	private static boolean isTimeoutCause(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof java.net.http.HttpTimeoutException
					|| c instanceof java.net.SocketTimeoutException
					|| c instanceof java.util.concurrent.TimeoutException) {
				return true;
			}
			String message = c.getMessage();
			if (message != null && message.toLowerCase(Locale.ROOT).contains("timed out")) {
				return true;
			}
			if (c == c.getCause()) {
				break;
			}
		}
		return false;
	}

	/**
	 * Whitelist-sanitise every free-form string bound for the student:
	 * allow paragraphs and emphasis ({@code <em>}, {@code <strong>}),
	 * strip everything else. Non-free-form fields (ids, enum values,
	 * numeric signals) are preserved as-is.
	 */
	public static GradingSuggestion sanitiseForStudent(GradingSuggestion in) {
		if (in == null) return null;
		GradingSuggestion.StudentFeedback fb = in.feedbackToStudent();
		GradingSuggestion.StudentFeedback safeFb = fb == null ? null
				: new GradingSuggestion.StudentFeedback(
						stripToSafeHtml(fb.whatWentWell()),
						stripToSafeHtml(fb.whatIsMissing()),
						stripToSafeHtml(fb.nextStep()));
		List<AnnotatedParagraph> sanitisedParagraphs = sanitiseAnnotatedParagraphs(in.annotatedParagraphs());
		return new GradingSuggestion(in.contentSignals(), in.languageSignals(), in.offTopicFlag(),
				in.confidence(), safeFb, stripToSafeHtml(in.feedbackToCoach()),
				stripToSafeHtml(in.overallAssessment()), in.estimatedScorePercent(), sanitisedParagraphs);
	}

	/**
	 * Sanitise the annotated-paragraphs list. Each {@link AnnotatedSpan#text()},
	 * {@link AnnotatedSpan#comment()}, and {@link AnnotatedParagraph#paragraphFeedback()}
	 * is run through the same XSS filter + tag-whitelist pipeline as the other
	 * free-form student-facing strings.
	 * <p>
	 * Returns an empty list when {@code paragraphs} is {@code null}.
	 */
	static List<AnnotatedParagraph> sanitiseAnnotatedParagraphs(List<AnnotatedParagraph> paragraphs) {
		if (paragraphs == null || paragraphs.isEmpty()) {
			return List.of();
		}
		List<AnnotatedParagraph> result = new ArrayList<>(paragraphs.size());
		for (AnnotatedParagraph para : paragraphs) {
			if (para == null) {
				continue;
			}
			List<AnnotatedSpan> cleanSpans;
			if (para.spans() == null || para.spans().isEmpty()) {
				cleanSpans = List.of();
			} else {
				cleanSpans = new ArrayList<>(para.spans().size());
				for (AnnotatedSpan span : para.spans()) {
					if (span == null) {
						continue;
					}
					// text must stay plain — strip all HTML including allowed tags
					// because it will be placed inside a <span> by the template
					String cleanText = stripToPlainText(span.text());
					String cleanComment = stripToPlainText(span.comment());
					cleanSpans.add(new AnnotatedSpan(cleanText, span.kind(), cleanComment));
				}
			}
			String cleanFeedback = stripToSafeHtml(para.paragraphFeedback());
			result.add(new AnnotatedParagraph(cleanSpans, cleanFeedback));
		}
		return result;
	}

	/**
	 * Verify that spans in {@code para} concatenate back to {@code originalParagraph}.
	 * Comparison is case-insensitive and whitespace-collapsed.
	 * <p>
	 * Returns the paragraph unchanged when the invariant holds. When it is
	 * violated, logs a {@code WARN} and returns a single {@link MarkKind#NEUTRAL}
	 * span containing the original paragraph text, so the grading run is not
	 * aborted.
	 */
	public static AnnotatedParagraph verifyOrFallback(AnnotatedParagraph para, String originalParagraph) {
		if (para == null) {
			String safe = originalParagraph == null ? "" : originalParagraph;
			return new AnnotatedParagraph(
					List.of(new AnnotatedSpan(safe, MarkKind.NEUTRAL, null)), "");
		}
		List<AnnotatedSpan> spans = para.spans();
		if (spans == null || spans.isEmpty()) {
			// empty spans for an empty paragraph is acceptable
			if (originalParagraph == null || originalParagraph.isEmpty()) {
				return para;
			}
			log.warn("annotatedParagraphs span integrity failure: spans list is empty but original paragraph is not; falling back to NEUTRAL span");
			return new AnnotatedParagraph(
					List.of(new AnnotatedSpan(originalParagraph, MarkKind.NEUTRAL, null)),
					para.paragraphFeedback());
		}
		StringBuilder concat = new StringBuilder();
		for (AnnotatedSpan s : spans) {
			if (s != null && s.text() != null) {
				concat.append(s.text());
			}
		}
		String collapsed = collapseWhitespace(concat.toString());
		String expected = collapseWhitespace(originalParagraph == null ? "" : originalParagraph);
		if (collapsed.equalsIgnoreCase(expected)) {
			return para;
		}
		log.warn("annotatedParagraphs span integrity failure: concatenated spans do not match original paragraph; falling back to NEUTRAL span. expected='{}' got='{}'",
				expected, collapsed);
		return new AnnotatedParagraph(
				List.of(new AnnotatedSpan(
						originalParagraph == null ? "" : originalParagraph,
						MarkKind.NEUTRAL, null)),
				para.paragraphFeedback());
	}

	/** Collapse all whitespace sequences (including newlines) to a single space and trim. */
	static String collapseWhitespace(String s) {
		if (s == null || s.isEmpty()) return "";
		return s.replaceAll("\\s+", " ").trim();
	}

	/**
	 * Strip ALL HTML tags, leaving only text content. Used for span text and
	 * comment fields which will be placed verbatim inside element attributes or
	 * inline text nodes by the Velocity template.
	 */
	private static String stripToPlainText(String in) {
		if (in == null || in.isEmpty()) return in;
		String xssClean = FilterFactory.getXSSFilter().filter(in);
		if (xssClean == null) return "";
		// Strip every tag (including allowed ones — these fields must be plain text)
		return xssClean.replaceAll("(?is)<[^>]*>", "");
	}

	/**
	 * Strip to a safe sub-HTML allow-list (paragraphs and basic emphasis).
	 * We use OpenOlat's XSS filter as a first pass and then reduce to the
	 * minimal tag set.
	 */
	private static String stripToSafeHtml(String in) {
		if (in == null || in.isEmpty()) return in;
		String xssClean = FilterFactory.getXSSFilter().filter(in);
		if (xssClean == null) return "";
		// Keep <em>, <strong>, <p>, <br> — strip anything else.
		String cleaned = xssClean.replaceAll("(?is)</?(?!(?:em|strong|p|br)\\b)[a-z][^>]*>", "");
		return cleaned;
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}
}
