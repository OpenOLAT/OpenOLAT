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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the static helpers of {@link EssayFormativeFeedbackService}:
 * {@code computeContentHash} determinism and field-sensitivity,
 * {@code sha256Hex16} correctness,
 * {@code sanitiseForStudent} XSS defenses and safe-tag allowlist.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class EssayFormativeFeedbackServiceStaticsTest {

	// ---------------------------------------------------------------- sha256Hex16

	@Test
	public void sha256Hex16_nullReturnsNull() {
		assertNull(EssayFormativeFeedbackService.sha256Hex16(null));
	}

	@Test
	public void sha256Hex16_lengthIs16() {
		String hash = EssayFormativeFeedbackService.sha256Hex16("hello");
		assertNotNull(hash);
		assertEquals("sha256Hex16 must be exactly 16 chars", 16, hash.length());
	}

	@Test
	public void sha256Hex16_deterministic() {
		String a = EssayFormativeFeedbackService.sha256Hex16("test input");
		String b = EssayFormativeFeedbackService.sha256Hex16("test input");
		assertEquals(a, b);
	}

	@Test
	public void sha256Hex16_differentInputsDifferentHashes() {
		String a = EssayFormativeFeedbackService.sha256Hex16("input-a");
		String b = EssayFormativeFeedbackService.sha256Hex16("input-b");
		assertNotNull(a);
		assertNotNull(b);
		assert !a.equals(b) : "Different inputs must produce different hashes";
	}

	@Test
	public void sha256Hex16_emptyStringReturns16Chars() {
		String hash = EssayFormativeFeedbackService.sha256Hex16("");
		assertEquals(16, hash.length());
	}

	// ---------------------------------------------------------------- computeContentHash

	@Test
	public void computeContentHash_deterministic() {
		EssayAiGrading g = buildGrading("ref", "model", "[kp1]", "[rc1]");
		String h1 = EssayFormativeFeedbackService.computeContentHash(g);
		String h2 = EssayFormativeFeedbackService.computeContentHash(g);
		assertEquals("contentHash must be deterministic", h1, h2);
	}

	@Test
	public void computeContentHash_changesWhenReferenceExcerptChanges() {
		EssayAiGrading g1 = buildGrading("ref-v1", "model", "[kp1]", "[rc1]");
		EssayAiGrading g2 = buildGrading("ref-v2", "model", "[kp1]", "[rc1]");
		String h1 = EssayFormativeFeedbackService.computeContentHash(g1);
		String h2 = EssayFormativeFeedbackService.computeContentHash(g2);
		assert !h1.equals(h2) : "Hash must change when referenceExcerpt changes";
	}

	@Test
	public void computeContentHash_changesWhenModelAnswerChanges() {
		EssayAiGrading g1 = buildGrading("ref", "model-v1", "[kp1]", "[rc1]");
		EssayAiGrading g2 = buildGrading("ref", "model-v2", "[kp1]", "[rc1]");
		String h1 = EssayFormativeFeedbackService.computeContentHash(g1);
		String h2 = EssayFormativeFeedbackService.computeContentHash(g2);
		assert !h1.equals(h2) : "Hash must change when modelAnswer changes";
	}

	@Test
	public void computeContentHash_changesWhenKeyPointsChange() {
		EssayAiGrading g1 = buildGrading("ref", "model", "[{\"id\":\"kp1\"}]", "[rc1]");
		EssayAiGrading g2 = buildGrading("ref", "model", "[{\"id\":\"kp2\"}]", "[rc1]");
		String h1 = EssayFormativeFeedbackService.computeContentHash(g1);
		String h2 = EssayFormativeFeedbackService.computeContentHash(g2);
		assert !h1.equals(h2) : "Hash must change when keyPointsJson changes";
	}

	@Test
	public void computeContentHash_changesWhenRubricChanges() {
		EssayAiGrading g1 = buildGrading("ref", "model", "[kp1]", "[{\"weight\":0.7}]");
		EssayAiGrading g2 = buildGrading("ref", "model", "[kp1]", "[{\"weight\":0.6}]");
		String h1 = EssayFormativeFeedbackService.computeContentHash(g1);
		String h2 = EssayFormativeFeedbackService.computeContentHash(g2);
		assert !h1.equals(h2) : "Hash must change when rubricCriteriaJson changes";
	}

	@Test
	public void computeContentHash_nullFieldsTreatedAsEmpty() {
		EssayAiGrading g = new EssayAiGrading();
		// All fields null — must not throw
		String hash = EssayFormativeFeedbackService.computeContentHash(g);
		assertNotNull(hash);
		assertEquals(16, hash.length());
	}

	// ---------------------------------------------------------------- sanitiseForStudent

	@Test
	public void sanitiseForStudent_nullReturnsNull() {
		assertNull(EssayFormativeFeedbackService.sanitiseForStudent(null));
	}

	@Test
	public void sanitiseForStudent_scriptTagStripped() {
		GradingSuggestion s = buildSuggestion("<script>alert(1)</script>Good answer");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		assertNotNull(sanitised);
		String whatWentWell = sanitised.feedbackToStudent().whatWentWell();
		assert !whatWentWell.contains("<script>") : "script tag must be stripped";
		assert !whatWentWell.contains("alert(1)") : "script content must be stripped";
	}

	@Test
	public void sanitiseForStudent_emTagPreserved() {
		GradingSuggestion s = buildSuggestion("<em>emphasis</em> is good");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		assertNotNull(sanitised);
		// <em> is in the safe allowlist
		assert sanitised.feedbackToStudent().whatWentWell().contains("<em>emphasis</em>")
				: "em tag must be preserved; got: " + sanitised.feedbackToStudent().whatWentWell();
	}

	@Test
	public void sanitiseForStudent_strongTagPreserved() {
		GradingSuggestion s = buildSuggestion("<strong>bold</strong> text");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		assert sanitised.feedbackToStudent().whatWentWell().contains("<strong>")
				: "strong tag must be preserved";
	}

	@Test
	public void sanitiseForStudent_pTagPreserved() {
		GradingSuggestion s = buildSuggestion("<p>paragraph</p>");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		assert sanitised.feedbackToStudent().whatWentWell().contains("<p>")
				: "p tag must be preserved";
	}

	@Test
	public void sanitiseForStudent_brTagPreserved() {
		GradingSuggestion s = buildSuggestion("line one<br>line two");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		String out = sanitised.feedbackToStudent().whatWentWell();
		// OWASP sanitiser may normalise <br> → <br /> (XHTML-style); both are valid
		boolean hasBr = out.contains("<br>") || out.contains("<br/>") || out.contains("<br />");
		assert hasBr : "br tag must be preserved in some form; got: " + out;
	}

	@Test
	public void sanitiseForStudent_divTagStripped() {
		GradingSuggestion s = buildSuggestion("<div class='evil'>content</div>");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		assertNotNull(sanitised);
		String out = sanitised.feedbackToStudent().whatWentWell();
		assert !out.contains("<div") : "div tag must be stripped";
		// The text content "content" should survive the tag-stripping
		assert out.contains("content") : "Text content inside stripped tag must survive";
	}

	@Test
	public void sanitiseForStudent_nonFreeFormFieldsPreserved() {
		GradingSuggestion s = new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.HIGH,
				new GradingSuggestion.StudentFeedback("well", "missing", "next"),
				"coach note", "overall", 75);
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		assertEquals(GradingSuggestion.OffTopicFlag.NONE, sanitised.offTopicFlag());
		assertEquals(GradingSuggestion.Confidence.HIGH, sanitised.confidence());
		assertEquals(75, sanitised.estimatedScorePercent());
	}

	@Test
	public void sanitiseForStudent_unicodeContentPreserved() {
		GradingSuggestion s = buildSuggestion("Gute Antwort: äöü ß. 中文. Emoji: 🎉");
		GradingSuggestion sanitised = EssayFormativeFeedbackService.sanitiseForStudent(s);
		String out = sanitised.feedbackToStudent().whatWentWell();
		assert out.contains("äöü") : "German umlauts must survive sanitisation";
		assert out.contains("中文") : "Chinese characters must survive sanitisation";
	}

	// ---------------------------------------------------------------- helpers

	private EssayAiGrading buildGrading(String ref, String model, String kpJson, String rcJson) {
		EssayAiGrading g = new EssayAiGrading();
		g.setReferenceExcerpt(ref);
		g.setModelAnswer(model);
		g.setKeyPointsJson(kpJson);
		g.setRubricCriteriaJson(rcJson);
		return g;
	}

	private GradingSuggestion buildSuggestion(String whatWentWell) {
		return new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.HIGH,
				new GradingSuggestion.StudentFeedback(whatWentWell, "missing", "next step"),
				"coach", "overall assessment", 80);
	}
}
