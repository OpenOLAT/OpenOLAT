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
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationDestination;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationRequest;

/**
 * Unit tests for the {@link GenerationRequest} factory methods:
 * {@code forQuizPart}, {@code forPool}, and the backward-compatible
 * zero-new-arg overloads.
 *
 * Pure Java — no Spring context required.
 *
 * Initial date: 2026-05-06<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class GenerationRequestFactoryTest {

	private static final String MARKDOWN = "# Topic\nSome content here.";
	private static final Long   REPO_KEY  = 42L;
	private static final Long   PAGE_KEY  = 10L;
	private static final Long   QUIZ_KEY  = 20L;
	private static final Long   TAX_KEY   = 99L;

	// ================================================================
	// forQuizPart — extended (11-arg) factory
	// ================================================================

	@Test
	public void forQuizPart_extended_allFieldsLandOnRecord() {
		List<AiBloomLevel> bloom = List.of(AiBloomLevel.ANALYSE, AiBloomLevel.EVALUATE);
		List<String> objectives  = List.of("Compare A with B", "Evaluate trade-offs");

		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				PAGE_KEY, QUIZ_KEY,
				3, 5,
				bloom, 4, objectives);

		assertEquals(MARKDOWN, r.pageMarkdown());
		assertEquals(REPO_KEY, r.repositoryEntryKey());
		assertEquals(Locale.ENGLISH, r.language());
		assertNull(r.requester());
		assertEquals(PAGE_KEY, r.pageKey());
		assertEquals(QUIZ_KEY, r.quizPartKey());
		assertEquals(3, r.targetQuestionCount());
		assertEquals(5, r.mcQuestionCount());
		assertEquals(bloom, r.targetBloomLevels());
		assertEquals(Integer.valueOf(4), r.targetDifficulty());
		assertEquals(objectives, r.learningObjectives());
	}

	@Test
	public void forQuizPart_extended_destinationIsQuizPart() {
		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.GERMAN, null,
				PAGE_KEY, QUIZ_KEY,
				2, 3,
				List.of(AiBloomLevel.APPLY), 2, List.of());

		assertEquals(GenerationDestination.QUIZ_PART, r.destination());
	}

	@Test
	public void forQuizPart_extended_nullBloomFallsBackToUnderstandApply() {
		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				PAGE_KEY, QUIZ_KEY,
				2, 3,
				null, null, null);

		assertDefaultBloom(r.targetBloomLevels());
	}

	@Test
	public void forQuizPart_extended_emptyBloomFallsBackToUnderstandApply() {
		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				PAGE_KEY, QUIZ_KEY,
				2, 3,
				List.of(), null, null);

		assertDefaultBloom(r.targetBloomLevels());
	}

	@Test
	public void forQuizPart_extended_nullLearningObjectivesFallsBackToEmptyList() {
		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				PAGE_KEY, QUIZ_KEY,
				2, 3,
				null, null, null);

		assertNotNull(r.learningObjectives());
		assertTrue("learningObjectives should be empty", r.learningObjectives().isEmpty());
	}

	@Test
	public void forQuizPart_extended_nullDifficultyStaysNull() {
		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				PAGE_KEY, QUIZ_KEY,
				2, 3,
				null, null, null);

		assertNull("targetDifficulty should remain null when not specified", r.targetDifficulty());
	}

	// ================================================================
	// forPool — extended (10-arg) factory
	// ================================================================

	@Test
	public void forPool_extended_allFieldsLandOnRecord() {
		List<AiBloomLevel> bloom = List.of(AiBloomLevel.REMEMBER, AiBloomLevel.CREATE);
		List<String> objectives  = List.of("Recall key terms");

		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.FRENCH, null,
				4, 6, TAX_KEY,
				bloom, 3, objectives);

		assertEquals(MARKDOWN, r.pageMarkdown());
		assertEquals(REPO_KEY, r.repositoryEntryKey());
		assertEquals(Locale.FRENCH, r.language());
		assertNull(r.requester());
		assertEquals(4, r.targetQuestionCount());
		assertEquals(6, r.mcQuestionCount());
		assertEquals(TAX_KEY, r.taxonomyLevelKey());
		assertEquals(bloom, r.targetBloomLevels());
		assertEquals(Integer.valueOf(3), r.targetDifficulty());
		assertEquals(objectives, r.learningObjectives());
	}

	@Test
	public void forPool_extended_destinationIsPool() {
		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				2, 4, null,
				List.of(AiBloomLevel.APPLY), 2, List.of());

		assertEquals(GenerationDestination.POOL, r.destination());
	}

	@Test
	public void forPool_extended_nullBloomFallsBackToUnderstandApply() {
		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				2, 4, null,
				null, null, null);

		assertDefaultBloom(r.targetBloomLevels());
	}

	@Test
	public void forPool_extended_emptyBloomFallsBackToUnderstandApply() {
		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				2, 4, null,
				List.of(), null, null);

		assertDefaultBloom(r.targetBloomLevels());
	}

	@Test
	public void forPool_extended_nullLearningObjectivesFallsBackToEmptyList() {
		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				2, 4, null,
				null, null, null);

		assertNotNull(r.learningObjectives());
		assertTrue("learningObjectives should be empty", r.learningObjectives().isEmpty());
	}

	@Test
	public void forPool_extended_nullDifficultyStaysNull() {
		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				2, 4, null,
				null, null, null);

		assertNull("targetDifficulty should remain null when not specified", r.targetDifficulty());
	}

	// ================================================================
	// Backward compatibility — old (shorter) factory overloads
	// ================================================================

	@Test
	public void forQuizPart_legacy_compilesAndProducesDefaultBloom() {
		GenerationRequest r = GenerationRequest.forQuizPart(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				PAGE_KEY, QUIZ_KEY,
				2, 3);

		assertDefaultBloom(r.targetBloomLevels());
		assertNull("legacy forQuizPart should have null difficulty", r.targetDifficulty());
		assertNotNull(r.learningObjectives());
		assertTrue("legacy forQuizPart should have empty objectives", r.learningObjectives().isEmpty());
		assertEquals(GenerationDestination.QUIZ_PART, r.destination());
	}

	@Test
	public void forPool_legacy_compilesAndProducesDefaultBloom() {
		GenerationRequest r = GenerationRequest.forPool(
				MARKDOWN, REPO_KEY, Locale.ENGLISH, null,
				2, 4, TAX_KEY);

		assertDefaultBloom(r.targetBloomLevels());
		assertNull("legacy forPool should have null difficulty", r.targetDifficulty());
		assertNotNull(r.learningObjectives());
		assertTrue("legacy forPool should have empty objectives", r.learningObjectives().isEmpty());
		assertEquals(GenerationDestination.POOL, r.destination());
		assertEquals(TAX_KEY, r.taxonomyLevelKey());
	}

	// ================================================================
	// helpers
	// ================================================================

	private static void assertDefaultBloom(List<AiBloomLevel> actual) {
		assertNotNull("bloom level list must not be null", actual);
		assertEquals("expected 2 default bloom levels", 2, actual.size());
		assertTrue("default bloom must contain UNDERSTAND", actual.contains(AiBloomLevel.UNDERSTAND));
		assertTrue("default bloom must contain APPLY",      actual.contains(AiBloomLevel.APPLY));
	}
}
