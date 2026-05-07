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

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link AnnotatedSpan}, {@link AnnotatedParagraph}, and
 * {@link MarkKind}: Jackson round-trip serialisation, missing-field tolerance,
 * and integration with {@link GradingSuggestion}.
 *
 * Initial date: 2026-05-07<br>
 *
 * @author Alan (AIT), https://www.frentix.com
 */
public class AnnotatedSpanParagraphTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	// ---------------------------------------------------------------- AnnotatedSpan round-trip

	@Test
	public void annotatedSpan_roundTrip_allFields() throws Exception {
		AnnotatedSpan original = new AnnotatedSpan("The answer is correct", MarkKind.CORRECT, "Good point");
		String json = MAPPER.writeValueAsString(original);
		AnnotatedSpan restored = MAPPER.readValue(json, AnnotatedSpan.class);

		assertEquals("The answer is correct", restored.text());
		assertEquals(MarkKind.CORRECT, restored.kind());
		assertEquals("Good point", restored.comment());
	}

	@Test
	public void annotatedSpan_roundTrip_nullComment() throws Exception {
		AnnotatedSpan original = new AnnotatedSpan("Some filler text", MarkKind.NEUTRAL, null);
		String json = MAPPER.writeValueAsString(original);
		AnnotatedSpan restored = MAPPER.readValue(json, AnnotatedSpan.class);

		assertEquals("Some filler text", restored.text());
		assertEquals(MarkKind.NEUTRAL, restored.kind());
		assertNull(restored.comment());
	}

	@Test
	public void annotatedSpan_deserialiseMissingFields_doesNotThrow() throws Exception {
		// @JsonIgnoreProperties(ignoreUnknown=true) — unknown field must be tolerated
		String json = "{\"text\":\"hello\",\"kind\":\"WRONG\",\"unknownField\":\"ignored\"}";
		AnnotatedSpan span = MAPPER.readValue(json, AnnotatedSpan.class);

		assertEquals("hello", span.text());
		assertEquals(MarkKind.WRONG, span.kind());
	}

	@Test
	public void annotatedSpan_allMarkKinds_roundTrip() throws Exception {
		for (MarkKind kind : MarkKind.values()) {
			AnnotatedSpan span = new AnnotatedSpan("text", kind, null);
			String json = MAPPER.writeValueAsString(span);
			AnnotatedSpan restored = MAPPER.readValue(json, AnnotatedSpan.class);
			assertEquals(kind, restored.kind());
		}
	}

	// ---------------------------------------------------------------- AnnotatedParagraph round-trip

	@Test
	public void annotatedParagraph_roundTrip() throws Exception {
		List<AnnotatedSpan> spans = List.of(
				new AnnotatedSpan("Paris is the capital ", MarkKind.CORRECT, "Correct"),
				new AnnotatedSpan("of Germany.", MarkKind.WRONG, "Should be France"));
		AnnotatedParagraph original = new AnnotatedParagraph(spans, "Good start, wrong country.");
		String json = MAPPER.writeValueAsString(original);
		AnnotatedParagraph restored = MAPPER.readValue(json, AnnotatedParagraph.class);

		assertNotNull(restored);
		assertEquals("Good start, wrong country.", restored.paragraphFeedback());
		assertEquals(2, restored.spans().size());
		assertEquals(MarkKind.WRONG, restored.spans().get(1).kind());
	}

	@Test
	public void annotatedParagraph_deserialiseMissingParagraphFeedback() throws Exception {
		// paragraphFeedback absent — must not throw; field will be null
		String json = "{\"spans\":[{\"text\":\"hello\",\"kind\":\"NEUTRAL\"}]}";
		AnnotatedParagraph para = MAPPER.readValue(json, AnnotatedParagraph.class);

		assertNotNull(para);
		assertEquals(1, para.spans().size());
		assertNull(para.paragraphFeedback());
	}

	@Test
	public void annotatedParagraph_deserialiseMissingSpans() throws Exception {
		// spans absent — must not throw
		String json = "{\"paragraphFeedback\":\"ok\"}";
		AnnotatedParagraph para = MAPPER.readValue(json, AnnotatedParagraph.class);

		assertNotNull(para);
		assertNull(para.spans());
	}

	@Test
	public void annotatedParagraph_unknownFieldIgnored() throws Exception {
		String json = "{\"spans\":[],\"paragraphFeedback\":\"good\",\"futureField\":42}";
		AnnotatedParagraph para = MAPPER.readValue(json, AnnotatedParagraph.class);
		assertNotNull(para);
	}

	// ---------------------------------------------------------------- GradingSuggestion integration

	@Test
	public void gradingSuggestion_annotatedParagraphsRoundTrip() throws Exception {
		List<AnnotatedParagraph> paragraphs = List.of(
				new AnnotatedParagraph(
						List.of(new AnnotatedSpan("Good text.", MarkKind.CORRECT, null)),
						"Well done."),
				new AnnotatedParagraph(
						List.of(
								new AnnotatedSpan("Some filler ", MarkKind.NEUTRAL, null),
								new AnnotatedSpan("wrong claim.", MarkKind.WRONG, "Factual error")),
						"This paragraph needs work."));
		GradingSuggestion suggestion = new GradingSuggestion(null, null,
				GradingSuggestion.OffTopicFlag.NONE, GradingSuggestion.Confidence.MEDIUM,
				new GradingSuggestion.StudentFeedback("well", "missing", "next"),
				"", "overall", 70, paragraphs);

		String json = MAPPER.writeValueAsString(suggestion);
		GradingSuggestion restored = MAPPER.readValue(json, GradingSuggestion.class);

		assertNotNull(restored);
		assertNotNull(restored.annotatedParagraphs());
		assertEquals(2, restored.annotatedParagraphs().size());

		AnnotatedParagraph p1 = restored.annotatedParagraphs().get(0);
		assertEquals(1, p1.spans().size());
		assertEquals(MarkKind.CORRECT, p1.spans().get(0).kind());
		assertEquals("Well done.", p1.paragraphFeedback());

		AnnotatedParagraph p2 = restored.annotatedParagraphs().get(1);
		assertEquals(2, p2.spans().size());
		assertEquals(MarkKind.WRONG, p2.spans().get(1).kind());
		assertEquals("Factual error", p2.spans().get(1).comment());
	}

	@Test
	public void gradingSuggestion_missingAnnotatedParagraphsDefaultsToEmpty() throws Exception {
		// Simulate old LLM response that does not include annotatedParagraphs field
		String json = "{\"contentSignals\":null,\"languageSignals\":null,"
				+ "\"offTopicFlag\":\"NONE\",\"confidence\":\"HIGH\","
				+ "\"feedbackToStudent\":{\"whatWentWell\":\"w\",\"whatIsMissing\":\"m\",\"nextStep\":\"n\"},"
				+ "\"feedbackToCoach\":\"\",\"overallAssessment\":\"ok\",\"estimatedScorePercent\":80}";
		GradingSuggestion suggestion = MAPPER.readValue(json, GradingSuggestion.class);

		assertNotNull(suggestion);
		// @JsonSetter(nulls = AS_EMPTY) must ensure the list is non-null and empty
		assertNotNull(suggestion.annotatedParagraphs());
		assertTrue(suggestion.annotatedParagraphs().isEmpty());
	}
}
