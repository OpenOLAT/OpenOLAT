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
 * software distributed under the LICENSE is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.ceditor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.olat.core.commons.services.ai.essay.AiGradingTier;
import org.olat.core.commons.services.ai.essay.AnnotatedParagraph;
import org.olat.core.commons.services.ai.essay.AnnotatedSpan;
import org.olat.core.commons.services.ai.essay.FormativeFeedback;
import org.olat.core.commons.services.ai.essay.GradingSuggestion;
import org.olat.core.commons.services.ai.essay.LengthPreFilter;
import org.olat.core.commons.services.ai.essay.MarkKind;
import org.olat.core.commons.services.ai.essay.RejectionReason;

/**
 * Unit tests for {@link AiEssayFeedbackViewFlattener} —
 * {@code bucketKey}, {@code confidenceClass}, {@code assessmentClass},
 * {@code buildAssessmentLabel}, full {@code flatten()} paths.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AiEssayFeedbackViewFlattenerTest {

	// ---------------------------------------------------------------- bucketKey

	@Test
	public void bucketKey_negativeReturnsNull() {
		assertNull(AiEssayFeedbackViewFlattener.bucketKey(-1));
	}

	@Test
	public void bucketKey_above100ReturnsNull() {
		assertNull(AiEssayFeedbackViewFlattener.bucketKey(101));
	}

	@Test
	public void bucketKey_0IsWrong() {
		assertEquals("ai.essay.correction.assessment.wrong", AiEssayFeedbackViewFlattener.bucketKey(0));
	}

	@Test
	public void bucketKey_24IsWrong() {
		assertEquals("ai.essay.correction.assessment.wrong", AiEssayFeedbackViewFlattener.bucketKey(24));
	}

	@Test
	public void bucketKey_25IsInsufficient() {
		assertEquals("ai.essay.correction.assessment.insufficient", AiEssayFeedbackViewFlattener.bucketKey(25));
	}

	@Test
	public void bucketKey_49IsInsufficient() {
		assertEquals("ai.essay.correction.assessment.insufficient", AiEssayFeedbackViewFlattener.bucketKey(49));
	}

	@Test
	public void bucketKey_50IsMediocre() {
		assertEquals("ai.essay.correction.assessment.mediocre", AiEssayFeedbackViewFlattener.bucketKey(50));
	}

	@Test
	public void bucketKey_69IsMediocre() {
		assertEquals("ai.essay.correction.assessment.mediocre", AiEssayFeedbackViewFlattener.bucketKey(69));
	}

	@Test
	public void bucketKey_70IsGood() {
		assertEquals("ai.essay.correction.assessment.good", AiEssayFeedbackViewFlattener.bucketKey(70));
	}

	@Test
	public void bucketKey_84IsGood() {
		assertEquals("ai.essay.correction.assessment.good", AiEssayFeedbackViewFlattener.bucketKey(84));
	}

	@Test
	public void bucketKey_85IsVeryGood() {
		assertEquals("ai.essay.correction.assessment.verygood", AiEssayFeedbackViewFlattener.bucketKey(85));
	}

	@Test
	public void bucketKey_100IsVeryGood() {
		assertEquals("ai.essay.correction.assessment.verygood", AiEssayFeedbackViewFlattener.bucketKey(100));
	}

	// ---------------------------------------------------------------- assessmentClass

	@Test
	public void assessmentClass_negativeReturnsEmpty() {
		assertEquals("", AiEssayFeedbackViewFlattener.assessmentClass(-1));
	}

	@Test
	public void assessmentClass_above100ReturnsEmpty() {
		assertEquals("", AiEssayFeedbackViewFlattener.assessmentClass(101));
	}

	@Test
	public void assessmentClass_0IsDanger() {
		assertEquals("label-danger", AiEssayFeedbackViewFlattener.assessmentClass(0));
	}

	@Test
	public void assessmentClass_24IsDanger() {
		assertEquals("label-danger", AiEssayFeedbackViewFlattener.assessmentClass(24));
	}

	@Test
	public void assessmentClass_25IsWarning() {
		assertEquals("label-warning", AiEssayFeedbackViewFlattener.assessmentClass(25));
	}

	@Test
	public void assessmentClass_50IsInfo() {
		assertEquals("label-info", AiEssayFeedbackViewFlattener.assessmentClass(50));
	}

	@Test
	public void assessmentClass_84IsInfo() {
		assertEquals("label-info", AiEssayFeedbackViewFlattener.assessmentClass(84));
	}

	@Test
	public void assessmentClass_85IsSuccess() {
		assertEquals("label-success", AiEssayFeedbackViewFlattener.assessmentClass(85));
	}

	@Test
	public void assessmentClass_100IsSuccess() {
		assertEquals("label-success", AiEssayFeedbackViewFlattener.assessmentClass(100));
	}

	// ---------------------------------------------------------------- confidenceClass

	@Test
	public void confidenceClass_nullReturnsEmpty() {
		assertEquals("", AiEssayFeedbackViewFlattener.confidenceClass(null));
	}

	@Test
	public void confidenceClass_highIsInfo() {
		assertEquals("label-info", AiEssayFeedbackViewFlattener.confidenceClass(GradingSuggestion.Confidence.HIGH));
	}

	@Test
	public void confidenceClass_mediumIsDefault() {
		assertEquals("label-default", AiEssayFeedbackViewFlattener.confidenceClass(GradingSuggestion.Confidence.MEDIUM));
	}

	@Test
	public void confidenceClass_lowIsWarning() {
		assertEquals("label-warning", AiEssayFeedbackViewFlattener.confidenceClass(GradingSuggestion.Confidence.LOW));
	}

	// ---------------------------------------------------------------- buildAssessmentLabel

	@Test
	public void buildAssessmentLabel_outOfRangeReturnsEmpty() {
		assertEquals("", AiEssayFeedbackViewFlattener.buildAssessmentLabel(-1, null));
		assertEquals("", AiEssayFeedbackViewFlattener.buildAssessmentLabel(101, null));
	}

	@Test
	public void buildAssessmentLabel_nullTranslatorReturnsBucketKey() {
		String label = AiEssayFeedbackViewFlattener.buildAssessmentLabel(90, null);
		assertEquals("ai.essay.correction.assessment.verygood", label);
	}

	// ---------------------------------------------------------------- flatten — null input

	@Test
	public void flatten_nullReturnsNull() {
		assertNull(AiEssayFeedbackViewFlattener.flatten(null, null));
	}

	// ---------------------------------------------------------------- flatten — REJECTED path

	@Test
	public void flatten_rejectedTypeAndRejectionKey() {
		RejectionReason reason = new RejectionReason("ai.essay.error.empty", "answer is empty");
		FormativeFeedback fb = FormativeFeedback.rejected(reason);
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		assertNotNull(v);
		assertEquals("REJECTED", v.get("type"));
		assertEquals("ai.essay.error.empty", v.get("rejectionMessageKey"));
		// With null translator the detail is used as the message
		assertNotNull(v.get("rejectionMessage"));
	}

	@Test
	public void flatten_rejectedDoesNotContainSuggestionKeys() {
		FormativeFeedback fb = FormativeFeedback.rejected(new RejectionReason("k", "d"));
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		assertFalse(v.containsKey("overallAssessment"));
		assertFalse(v.containsKey("confidence"));
	}

	// ---------------------------------------------------------------- flatten — REFUSED_LONG path

	@Test
	public void flatten_refusedLongType() {
		RejectionReason reason = new RejectionReason(LengthPreFilter.REASON_TOO_LONG, "too long");
		FormativeFeedback fb = FormativeFeedback.refusedLong(reason);
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		assertNotNull(v);
		assertEquals("REFUSED_LONG", v.get("type"));
	}

	// ---------------------------------------------------------------- flatten — OK path

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_okPathContainsExpectedKeys() {
		GradingSuggestion suggestion = buildSuggestion(75);
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 1L);
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		assertNotNull(v);
		assertEquals("OK", v.get("type"));
		assertNotNull(v.get("overallAssessment"));
		assertNotNull(v.get("confidence"));
		assertNotNull(v.get("assessmentLabel"));
		assertNotNull(v.get("assessmentClass"));
		assertNotNull(v.get("feedbackToStudent"));
		assertNotNull(v.get("keyPointsHit"));
		assertNotNull(v.get("keyPointsMissed"));
		assertNotNull(v.get("warnings"));
	}

	@Test
	public void flatten_okWithNullSuggestionReturnsNull() {
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, null, List.of(), 1L);
		assertNull(AiEssayFeedbackViewFlattener.flatten(fb, null));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_okWarningsAreIncluded() {
		GradingSuggestion suggestion = buildSuggestion(60);
		List<RejectionReason> warnings = List.of(
				new RejectionReason("ai.essay.warn.language.mismatch", "language differs"));
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, warnings, 1L);
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		List<Map<String, String>> warnList = (List<Map<String, String>>) v.get("warnings");
		assertEquals(1, warnList.size());
		assertEquals("ai.essay.warn.language.mismatch", warnList.get(0).get("messageKey"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_keyPointsHitAndMissed() {
		GradingSuggestion.ContentSignals content = new GradingSuggestion.ContentSignals(
				List.of(new GradingSuggestion.KeyPointHit("kp1", "good evidence")),
				List.of(new GradingSuggestion.KeyPointMissed("kp2", "not covered")),
				null, null, null);
		GradingSuggestion suggestion = new GradingSuggestion(content, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.HIGH,
				new GradingSuggestion.StudentFeedback("well", "missing", "next"),
				"coach", "overall", 80, List.of());
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 1L);
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);

		List<Map<String, String>> hit = (List<Map<String, String>>) v.get("keyPointsHit");
		List<Map<String, String>> missed = (List<Map<String, String>>) v.get("keyPointsMissed");
		assertEquals(1, hit.size());
		assertEquals("kp1", hit.get(0).get("id"));
		assertEquals(1, missed.size());
		assertEquals("kp2", missed.get(0).get("id"));
	}

	// ---------------------------------------------------------------- markCssClass

	@Test
	public void markCssClass_correct() {
		assertEquals("o_ai_mark_correct", AiEssayFeedbackViewFlattener.markCssClass(MarkKind.CORRECT));
	}

	@Test
	public void markCssClass_ambiguous() {
		assertEquals("o_ai_mark_ambiguous", AiEssayFeedbackViewFlattener.markCssClass(MarkKind.AMBIGUOUS));
	}

	@Test
	public void markCssClass_wrong() {
		assertEquals("o_ai_mark_wrong", AiEssayFeedbackViewFlattener.markCssClass(MarkKind.WRONG));
	}

	@Test
	public void markCssClass_neutral() {
		assertEquals("", AiEssayFeedbackViewFlattener.markCssClass(MarkKind.NEUTRAL));
	}

	@Test
	public void markCssClass_null() {
		assertEquals("", AiEssayFeedbackViewFlattener.markCssClass(null));
	}

	// ---------------------------------------------------------------- flatten — annotatedParagraphs

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_annotatedParagraphs_twoParagraphsMixedKinds() {
		// Paragraph 1: two spans — CORRECT + NEUTRAL
		List<AnnotatedSpan> spans1 = List.of(
				new AnnotatedSpan("Paris is the capital", MarkKind.CORRECT, "Right!"),
				new AnnotatedSpan(" of France.", MarkKind.NEUTRAL, null));
		AnnotatedParagraph para1 = new AnnotatedParagraph(spans1, "Good paragraph.");

		// Paragraph 2: two spans — WRONG + AMBIGUOUS
		List<AnnotatedSpan> spans2 = List.of(
				new AnnotatedSpan("The war started in 1945", MarkKind.WRONG, "Wrong year"),
				new AnnotatedSpan(" due to complex reasons.", MarkKind.AMBIGUOUS, null));
		AnnotatedParagraph para2 = new AnnotatedParagraph(spans2, "Check your dates.");

		GradingSuggestion suggestion = new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.MEDIUM,
				new GradingSuggestion.StudentFeedback("well", "missing", "next"),
				"", "overall", 65, List.of(para1, para2));
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 1L);

		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		assertNotNull(v);

		List<Map<String, Object>> paras = (List<Map<String, Object>>) v.get("annotatedParagraphs");
		assertNotNull(paras);
		assertEquals(2, paras.size());

		// Paragraph 1
		Map<String, Object> p1 = paras.get(0);
		assertEquals("Good paragraph.", p1.get("feedback"));
		List<Map<String, String>> s1 = (List<Map<String, String>>) p1.get("spans");
		assertEquals(2, s1.size());
		assertEquals("correct", s1.get(0).get("kind"));
		assertEquals("o_ai_mark_correct", s1.get(0).get("cssClass"));
		assertEquals("Right!", s1.get(0).get("comment"));
		assertEquals("neutral", s1.get(1).get("kind"));
		assertEquals("", s1.get(1).get("cssClass"));

		// Paragraph 2
		Map<String, Object> p2 = paras.get(1);
		assertEquals("Check your dates.", p2.get("feedback"));
		List<Map<String, String>> s2 = (List<Map<String, String>>) p2.get("spans");
		assertEquals(2, s2.size());
		assertEquals("wrong", s2.get(0).get("kind"));
		assertEquals("o_ai_mark_wrong", s2.get(0).get("cssClass"));
		assertEquals("Wrong year", s2.get(0).get("comment"));
		assertEquals("ambiguous", s2.get(1).get("kind"));
		assertEquals("o_ai_mark_ambiguous", s2.get(1).get("cssClass"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_annotatedParagraphs_emptyListWhenNone() {
		GradingSuggestion suggestion = buildSuggestion(75);
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 1L);
		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);

		List<Map<String, Object>> paras = (List<Map<String, Object>>) v.get("annotatedParagraphs");
		assertNotNull(paras);
		assertTrue(paras.isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_annotatedParagraphs_failSoftFallbackNeutralSpan() {
		// Simulate the fail-soft path: a paragraph that has a single NEUTRAL span
		// (as produced by verifyOrFallback when the LLM returned bad spans)
		AnnotatedSpan fallbackSpan = new AnnotatedSpan(
				"The student wrote this entire paragraph.", MarkKind.NEUTRAL, null);
		AnnotatedParagraph fallbackPara = new AnnotatedParagraph(
				List.of(fallbackSpan), "Model spans did not match — shown as plain text.");

		GradingSuggestion suggestion = new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.LOW,
				new GradingSuggestion.StudentFeedback("ok", "improve", "next"),
				"", "overall", 40, List.of(fallbackPara));
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 2L);

		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		List<Map<String, Object>> paras = (List<Map<String, Object>>) v.get("annotatedParagraphs");

		assertEquals(1, paras.size());
		List<Map<String, String>> spans = (List<Map<String, String>>) paras.get(0).get("spans");
		assertEquals(1, spans.size());
		assertEquals("neutral", spans.get(0).get("kind"));
		assertEquals("", spans.get(0).get("cssClass"));
		assertEquals("The student wrote this entire paragraph.", spans.get(0).get("text"));
	}

	// ---------------------------------------------------------------- renderSpanText / isParagraphHtml

	@Test
	public void renderSpanText_nullReturnsEmpty() {
		assertEquals("", AiEssayFeedbackViewFlattener.renderSpanText(null, false));
		assertEquals("", AiEssayFeedbackViewFlattener.renderSpanText(null, true));
	}

	@Test
	public void renderSpanText_emptyReturnsEmpty() {
		assertEquals("", AiEssayFeedbackViewFlattener.renderSpanText("", false));
	}

	@Test
	public void renderSpanText_plainTextConvertsNewlinesToBr() {
		assertEquals("Line 1<br>Line 2", AiEssayFeedbackViewFlattener.renderSpanText("Line 1\nLine 2", false));
	}

	@Test
	public void renderSpanText_plainTextConvertsTabsToFourNbsp() {
		assertEquals("Col1&nbsp;&nbsp;&nbsp;&nbsp;Col2",
				AiEssayFeedbackViewFlattener.renderSpanText("Col1\tCol2", false));
	}

	@Test
	public void renderSpanText_plainTextNormalizesCrlfAndCr() {
		assertEquals("a<br>b<br>c", AiEssayFeedbackViewFlattener.renderSpanText("a\r\nb\rc", false));
	}

	@Test
	public void renderSpanText_plainTextEscapesHtmlChars() {
		// HTML chars in plain text input must be escaped so they render as text,
		// not be interpreted as markup by the browser.
		assertEquals("&lt;b&gt;not bold&lt;/b&gt;",
				AiEssayFeedbackViewFlattener.renderSpanText("<b>not bold</b>", false));
	}

	@Test
	public void renderSpanText_htmlIsPassedThroughVerbatim() {
		assertEquals("<b>bold</b> text",
				AiEssayFeedbackViewFlattener.renderSpanText("<b>bold</b> text", true));
	}

	@Test
	public void renderSpanText_htmlNewlinesAreNotConverted() {
		// HTML input is trusted as-is — author already controls line breaks via <br> / <p>.
		assertEquals("a\nb", AiEssayFeedbackViewFlattener.renderSpanText("a\nb", true));
	}

	@Test
	public void isParagraphHtml_nullParagraph() {
		assertFalse(AiEssayFeedbackViewFlattener.isParagraphHtml(null));
	}

	@Test
	public void isParagraphHtml_plainTextSpans() {
		AnnotatedParagraph ap = new AnnotatedParagraph(
				List.of(new AnnotatedSpan("Hello\nworld", MarkKind.NEUTRAL, null)),
				"");
		assertFalse(AiEssayFeedbackViewFlattener.isParagraphHtml(ap));
	}

	@Test
	public void isParagraphHtml_htmlSpans() {
		AnnotatedParagraph ap = new AnnotatedParagraph(
				List.of(new AnnotatedSpan("<p>Hello world</p>", MarkKind.NEUTRAL, null)),
				"");
		assertTrue(AiEssayFeedbackViewFlattener.isParagraphHtml(ap));
	}

	@Test
	public void isParagraphHtml_tagAcrossSpanBoundary() {
		// A tag straddling the boundary between two spans must still be detected
		// as HTML — that is the reason detection runs on the concatenated paragraph.
		AnnotatedParagraph ap = new AnnotatedParagraph(
				List.of(
						new AnnotatedSpan("Hello <stro", MarkKind.NEUTRAL, null),
						new AnnotatedSpan("ng>world</strong>", MarkKind.NEUTRAL, null)),
				"");
		assertTrue(AiEssayFeedbackViewFlattener.isParagraphHtml(ap));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_plainTextSpansHaveLineBreaksAsBr() {
		AnnotatedParagraph ap = new AnnotatedParagraph(
				List.of(new AnnotatedSpan("Line 1\nLine 2\tindented", MarkKind.NEUTRAL, null)),
				"");
		GradingSuggestion suggestion = new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.MEDIUM,
				new GradingSuggestion.StudentFeedback("", "", ""),
				"", "", 60, List.of(ap));
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 1L);

		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		List<Map<String, Object>> paras = (List<Map<String, Object>>) v.get("annotatedParagraphs");
		List<Map<String, String>> spans = (List<Map<String, String>>) paras.get(0).get("spans");
		assertEquals("Line 1<br>Line 2&nbsp;&nbsp;&nbsp;&nbsp;indented", spans.get(0).get("text"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void flatten_htmlSpansAreNotEscapedAndNotConverted() {
		AnnotatedParagraph ap = new AnnotatedParagraph(
				List.of(new AnnotatedSpan("<p>Hello <b>world</b></p>", MarkKind.NEUTRAL, null)),
				"");
		GradingSuggestion suggestion = new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.MEDIUM,
				new GradingSuggestion.StudentFeedback("", "", ""),
				"", "", 60, List.of(ap));
		FormativeFeedback fb = FormativeFeedback.ok(AiGradingTier.SHORT, suggestion, List.of(), 1L);

		Map<String, Object> v = AiEssayFeedbackViewFlattener.flatten(fb, null);
		List<Map<String, Object>> paras = (List<Map<String, Object>>) v.get("annotatedParagraphs");
		List<Map<String, String>> spans = (List<Map<String, String>>) paras.get(0).get("spans");
		assertEquals("<p>Hello <b>world</b></p>", spans.get(0).get("text"));
	}

	// ---------------------------------------------------------------- helpers

	private GradingSuggestion buildSuggestion(int percent) {
		return new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.HIGH,
				new GradingSuggestion.StudentFeedback("well done", "could improve", "next step"),
				"coach note", "overall assessment", percent, List.of());
	}
}
