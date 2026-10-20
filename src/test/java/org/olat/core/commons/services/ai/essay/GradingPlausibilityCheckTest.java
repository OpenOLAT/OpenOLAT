/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.Confidence;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.ContentSignals;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.KeyPointHit;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.KeyPointMissed;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.LanguageSignals;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.OffTopicFlag;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.StudentFeedback;

/**
 * Unit tests for {@link GradingPlausibilityCheck}.
 *
 * Initial date: 2026-10-20<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public class GradingPlausibilityCheckTest {

	private static final String ANSWER = "Prokaryotic cells have no nucleus; their DNA lies free in the "
			+ "cytoplasm. Eukaryotic cells keep their DNA inside a nucleus with a double membrane.";

	private static final List<EssayItemDraft.KeyPoint> KIT = List.of(
			new EssayItemDraft.KeyPoint("kp1", "Prokaryotes have no nucleus", 0.5, true),
			new EssayItemDraft.KeyPoint("kp2", "Eukaryotic DNA is inside the nucleus", 0.5, true));

	private static GradingSuggestion suggestion(List<KeyPointHit> hits, List<KeyPointMissed> missed,
			OffTopicFlag flag, Confidence confidence, int score) {
		return new GradingSuggestion(
				new ContentSignals(hits, missed, "coherent", "sound", "on topic"),
				new LanguageSignals(List.of(), List.of()),
				flag, confidence,
				new StudentFeedback("good", "missing", "next"),
				"", "overall", score, List.of());
	}

	// ---------------------------------------------------------------- pass-through

	@Test
	public void apply_nullSuggestionReturnsNull() {
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(null, KIT, ANSWER, false);
		assertNull(result.suggestion());
		assertFalse(result.adjusted());
	}

	@Test
	public void apply_consistentResultIsReturnedUnchanged() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus"), new KeyPointHit("kp2", "DNA inside a nucleus")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 90);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertFalse(result.adjusted());
		assertSame(in, result.suggestion());
	}

	@Test
	public void apply_emptyKitSkipsIdRules() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("whatever", "have no nucleus")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 90);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, List.of(), ANSWER, false);
		assertFalse(result.adjusted());
	}

	// ---------------------------------------------------------------- key-point rules

	@Test
	public void apply_unknownKeyPointIdsAreDropped() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus"), new KeyPointHit("kp9", "have no nucleus")),
				List.of(new KeyPointMissed("kp7", "n/a")),
				OffTopicFlag.NONE, Confidence.HIGH, 80);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertTrue(result.adjusted());
		assertEquals(1, result.suggestion().contentSignals().keyPointsHit().size());
		assertEquals("kp1", result.suggestion().contentSignals().keyPointsHit().get(0).id());
		assertTrue(result.suggestion().contentSignals().keyPointsMissed().isEmpty());
		assertEquals(80, result.suggestion().estimatedScorePercent());
		assertEquals(Confidence.HIGH, result.suggestion().confidence());
	}

	@Test
	public void apply_hitWithEvidenceNotInAnswerIsDropped() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus"),
						new KeyPointHit("kp2", "mitochondria produce energy for the cell")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 90);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertTrue(result.adjusted());
		assertEquals(1, result.suggestion().contentSignals().keyPointsHit().size());
		assertEquals("kp1", result.suggestion().contentSignals().keyPointsHit().get(0).id());
	}

	@Test
	public void apply_lightlyRewordedEvidenceIsKept() {
		// Model trimmed the quote and changed punctuation; token overlap keeps it.
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp2", "Eukaryotic cells keep DNA inside the nucleus, double membrane")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 70);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertFalse(result.adjusted());
	}

	@Test
	public void apply_allHitsDroppedCapsScoreAndConfidence() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "roses are red violets are blue")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 100);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertTrue(result.adjusted());
		assertTrue(result.suggestion().contentSignals().keyPointsHit().isEmpty());
		assertEquals(GradingPlausibilityCheck.NO_HIT_SCORE_CAP, result.suggestion().estimatedScorePercent());
		assertEquals(Confidence.LOW, result.suggestion().confidence());
	}

	@Test
	public void apply_unevidencedHitsLowerConfidence() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus"), new KeyPointHit("kp2", "")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 90);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertTrue(result.adjusted());
		assertEquals(2, result.suggestion().contentSignals().keyPointsHit().size());
		assertEquals(Confidence.MEDIUM, result.suggestion().confidence());
		assertEquals(90, result.suggestion().estimatedScorePercent());
	}

	@Test
	public void apply_onlyUnevidencedHitsGiveLowConfidence() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", null)),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 90);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertEquals(Confidence.LOW, result.suggestion().confidence());
	}

	// ---------------------------------------------------------------- off-topic and language

	@Test
	public void apply_offTopicFlagCapsScoreAndConfidence() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus")),
				List.of(), OffTopicFlag.LANGUAGE_MISMATCH, Confidence.HIGH, 100);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertTrue(result.adjusted());
		assertEquals(GradingPlausibilityCheck.OFF_TOPIC_SCORE_CAP, result.suggestion().estimatedScorePercent());
		assertEquals(Confidence.LOW, result.suggestion().confidence());
		assertEquals(OffTopicFlag.LANGUAGE_MISMATCH, result.suggestion().offTopicFlag());
	}

	@Test
	public void apply_offTopicFlagWithLowScoreOnlyLowersConfidence() {
		GradingSuggestion in = suggestion(List.of(), List.of(), OffTopicFlag.GIBBERISH, Confidence.MEDIUM, 10);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertEquals(10, result.suggestion().estimatedScorePercent());
		assertEquals(Confidence.LOW, result.suggestion().confidence());
	}

	@Test
	public void apply_languageMismatchWithHighScoreCapsConfidenceAtMedium() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus"), new KeyPointHit("kp2", "DNA inside a nucleus")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 100);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, true);
		assertTrue(result.adjusted());
		assertEquals(100, result.suggestion().estimatedScorePercent());
		assertEquals(Confidence.MEDIUM, result.suggestion().confidence());
	}

	@Test
	public void apply_languageMismatchWithLowScoreChangesNothing() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 50);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, true);
		assertFalse(result.adjusted());
	}

	@Test
	public void apply_scoreOutOfRangeIsClamped() {
		GradingSuggestion in = suggestion(
				List.of(new KeyPointHit("kp1", "have no nucleus")),
				List.of(), OffTopicFlag.NONE, Confidence.HIGH, 140);
		GradingPlausibilityCheck.Result result = GradingPlausibilityCheck.apply(in, KIT, ANSWER, false);
		assertEquals(100, result.suggestion().estimatedScorePercent());
	}

	// ---------------------------------------------------------------- helpers

	@Test
	public void evidenceFoundIn_exactAndNormalisedMatch() {
		assertTrue(GradingPlausibilityCheck.evidenceFoundIn(ANSWER, "no nucleus; their DNA"));
		assertTrue(GradingPlausibilityCheck.evidenceFoundIn(ANSWER, "\"NO NUCLEUS, their dna\""));
	}

	@Test
	public void evidenceFoundIn_shortForeignSnippetFails() {
		assertFalse(GradingPlausibilityCheck.evidenceFoundIn(ANSWER, "sugar is sweet"));
	}

	@Test
	public void evidenceFoundIn_blankEvidenceCountsAsFound() {
		assertTrue(GradingPlausibilityCheck.evidenceFoundIn(ANSWER, " "));
	}

	@Test
	public void normalise_stripsPunctuationAndCase() {
		assertEquals("a b c", GradingPlausibilityCheck.normalise("  A, b... C! "));
	}
}
