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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiEssayGradingService;
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
 * grader SPI under a hard 30-second timeout, sanitises the student-facing
 * free-form text, and persists the essay-specific provenance on the usage
 * log row.
 * <p>
 * Call sequence:
 * <ol>
 *   <li>Load grading + verify content hash.</li>
 *   <li>{@link LengthPreFilter} → {@link GibberishPreFilter} → {@link LanguagePreFilter}.</li>
 *   <li>Tier classify; Lang tier refused inline.</li>
 *   <li>Router → {@link AiEssayGradingSPI}.</li>
 *   <li>SPI call via {@link AiEssayGradingServiceImpl} with 30 s hard timeout.</li>
 *   <li>Free-form text sanitised via OpenOlat's XSS filter.</li>
 *   <li>Essay provenance written to the {@code AiUsageLog} row.</li>
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

	/** Hard timeout (seconds) for the synchronous grading call. */
	public static final int GRADING_TIMEOUT_SECONDS = 30;

	/** Usage-context type stamped on the {@code AiUsageLog} row. */
	private static final String USAGE_CONTEXT_TYPE = "essay-grading";

	@Autowired
	private AiEssayGradingService aiEssayGradingService;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;

	/**
	 * Grade a student essay answer synchronously against a pre-loaded
	 * {@link EssayAiGrading} POJO (loaded from {@code ai-grading.json} by
	 * the caller via {@link EssayAiGradingFileStore#load(java.io.File)}).
	 * <p>
	 * {@code session} may be {@code null} when the call is triggered
	 * outside an assessment run (preview, unit test).
	 */
	public FormativeFeedback grade(EssayAiGrading grading, String studentAnswer,
			AssessmentItemSession session, Identity student, Locale locale) {
		if (grading == null) {
			throw new IllegalArgumentException("grading must not be null");
		}
		verifyContentHash(grading);

		Locale effectiveLocale = locale != null ? locale : Locale.ENGLISH;

		// 1. Length pre-filter. Long tier refused with inline error (no LLM call).
		Optional<RejectionReason> lengthReject = LengthPreFilter.check(studentAnswer);
		if (lengthReject.isPresent()) {
			RejectionReason reason = lengthReject.get();
			if (LengthPreFilter.REASON_TOO_LONG.equals(reason.messageKey())) {
				return FormativeFeedback.refusedLong(reason);
			}
			return FormativeFeedback.rejected(reason);
		}

		// 2. Gibberish pre-filter.
		Optional<RejectionReason> gibberishReject = GibberishPreFilter.check(studentAnswer);
		if (gibberishReject.isPresent()) {
			return FormativeFeedback.rejected(gibberishReject.get());
		}

		// 3. Language warning (non-fatal).
		List<RejectionReason> warnings = new ArrayList<>();
		LanguagePreFilter.check(studentAnswer, grading.getLanguage()).ifPresent(warnings::add);

		// 4. Tier.
		AiGradingTier tier = AiGradingTier.classify(studentAnswer);
		if (tier == AiGradingTier.LONG) {
			// Defensive — LengthPreFilter should already have caught this.
			return FormativeFeedback.refusedLong(new RejectionReason(
					LengthPreFilter.REASON_TOO_LONG, "tier classified as LONG"));
		}

		// 5. Precondition: grading must be configured.
		if (!aiEssayGradingService.isEnabled()) {
			throw new AiEssayGradingException("Essay grading is not configured or not enabled.");
		}

		AiUsageContext.Builder usageContextBuilder = AiUsageContext.builder()
				.usageContextType(USAGE_CONTEXT_TYPE)
				.usageContextId(grading.getAssessmentItemIdentifier())
				.identity(student)
				.locale(effectiveLocale);
		// Resource: the course / test the answer is being given in. Only
		// available when called from a real assessment run; absent in preview
		// and unit-test paths.
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

		// 6. Service call with 30 s hard timeout.
		AiEssayGradingService.GradingRun run;
		try {
			run = invokeWithTimeout(() -> aiEssayGradingService.gradeWithLog(
					usageContext, grading, studentAnswer, effectiveLocale, tier));
		} catch (AiEssayResponseTruncatedException e) {
			// The LLM reply was cut off (typically max_tokens) and Jackson failed
			// to parse it. Surface this as a graceful rejection card instead of
			// letting the raw Jackson stack trace bubble up to the job error.
			log.info("Essay grading response truncated for tier {}: {}", tier, e.getMessage());
			return FormativeFeedback.rejected(new RejectionReason(
					"ai.essay.error.response.truncated",
					"AI response truncated (tier=" + tier + ")"));
		}

		// 7. Sanitise free-form student feedback strings.
		GradingSuggestion sanitised = sanitiseForStudent(run.suggestion());

		// 8. Essay provenance on the usage log row.
		if (run.usageLogKey() != null) {
			try {
				aiUsageLogDAO.updateEssayFields(run.usageLogKey(),
						grading.getAssessmentItemIdentifier(),
						grading.getContentHash(),
						aiEssayGradingService.getPromptTemplateVersion(),
						tier,
						session == null ? null : session.getKey());
			} catch (Exception e) {
				log.warn("Failed to attach essay provenance to usage log {}: {}",
						run.usageLogKey(), e.getMessage());
			}
		}

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
	 * Invoke the grading service under a hard timeout.
	 * {@link EssayGradingTimeoutException} on expiry; any other failure is
	 * rewrapped as an {@link AiEssayGradingException}.
	 */
	private AiEssayGradingService.GradingRun invokeWithTimeout(
			java.util.concurrent.Callable<AiEssayGradingService.GradingRun> call) {
		CompletableFuture<AiEssayGradingService.GradingRun> future = CompletableFuture.supplyAsync(() -> {
			try {
				return call.call();
			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new AiEssayGradingException("Grading service call failed", e);
			}
		});
		try {
			return future.get(GRADING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new EssayGradingTimeoutException(
					"Essay grading exceeded " + GRADING_TIMEOUT_SECONDS + " s hard timeout", e);
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof AiEssayGradingException aege) throw aege;
			if (cause instanceof RuntimeException re) throw re;
			throw new AiEssayGradingException("Grading service call failed", cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AiEssayGradingException("Grading service call interrupted", e);
		}
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
