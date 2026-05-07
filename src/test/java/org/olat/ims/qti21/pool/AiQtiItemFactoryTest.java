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
package org.olat.ims.qti21.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.commons.services.ai.model.MCQuestionData.McAnswerOption;

/**
 * Unit tests for the static helpers of {@link AiQtiItemFactory}:
 * {@code stimulusToHtml}, {@code TOOL_PREFIX}, title derivation logic.
 * The full {@code buildEssayItem}/{@code buildMcItem} methods are deferred
 * to integration tests (require a live {@link org.olat.ims.qti21.QTI21Service}).
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AiQtiItemFactoryTest {

	// ---------------------------------------------------------------- TOOL_PREFIX constant

	@Test
	public void toolPrefix_isNonBlank() {
		assertNotNull(AiQtiItemFactory.TOOL_PREFIX);
		assertFalse(AiQtiItemFactory.TOOL_PREFIX.isBlank());
	}

	@Test
	public void toolPrefix_startsWithOpenOlatAi() {
		assertTrue("TOOL_PREFIX must start with 'OpenOlat.AI'",
				AiQtiItemFactory.TOOL_PREFIX.startsWith("OpenOlat.AI"));
	}

	// ---------------------------------------------------------------- stimulusToHtml

	@Test
	public void stimulusToHtml_nullReturnsEmptyParagraph() {
		String result = AiQtiItemFactory.stimulusToHtml(null);
		assertEquals("<p></p>", result);
	}

	@Test
	public void stimulusToHtml_blankReturnsEmptyParagraph() {
		assertEquals("<p></p>", AiQtiItemFactory.stimulusToHtml("   "));
	}

	@Test
	public void stimulusToHtml_singleLineWrappedInParagraph() {
		String result = AiQtiItemFactory.stimulusToHtml("Hello world");
		assertEquals("<p>Hello world</p>", result);
	}

	@Test
	public void stimulusToHtml_multipleLinesSeparatedByBlankLineProduceMultipleParagraphs() {
		String stimulus = "First paragraph.\n\nSecond paragraph.";
		String result = AiQtiItemFactory.stimulusToHtml(stimulus);
		assertTrue(result.contains("<p>First paragraph.</p>"));
		assertTrue(result.contains("<p>Second paragraph.</p>"));
	}

	@Test
	public void stimulusToHtml_xmlSpecialCharsAreEscaped() {
		String result = AiQtiItemFactory.stimulusToHtml("x < y & z > w with \"quotes\" and 'apostrophe'");
		assertTrue(result.contains("&lt;"));
		assertTrue(result.contains("&amp;"));
		assertTrue(result.contains("&gt;"));
		assertTrue(result.contains("&quot;"));
		assertTrue(result.contains("&apos;"));
	}

	@Test
	public void stimulusToHtml_singleNewlineBecomesSpace() {
		// Single newlines within a paragraph should become spaces, not line breaks
		String result = AiQtiItemFactory.stimulusToHtml("Line one\nLine two");
		// Both lines merged into one paragraph (no blank line separator)
		assertTrue(result.startsWith("<p>"));
		assertTrue(result.endsWith("</p>"));
		assertFalse("Multiple paragraphs must not be produced for single newline",
				result.indexOf("<p>") != result.lastIndexOf("<p>"));
	}

	@Test
	public void stimulusToHtml_unicodeIsPreserved() {
		String result = AiQtiItemFactory.stimulusToHtml("Ümlauts: äöü. Chinese: 中文");
		assertTrue(result.contains("äöü"));
		assertTrue(result.contains("中文"));
	}

	// ---------------------------------------------------------------- McAnswerOption shape

	@Test
	public void mcAnswerOption_noArgCtorAndSetters() {
		McAnswerOption opt = new McAnswerOption();
		opt.setText("Paris");
		opt.setFeedback("Paris is the capital of France.");
		assertEquals("Paris", opt.getText());
		assertEquals("Paris is the capital of France.", opt.getFeedback());
	}

	@Test
	public void mcAnswerOption_convenienceCtor() {
		McAnswerOption opt = new McAnswerOption("London", "London is the capital of the United Kingdom.");
		assertEquals("London", opt.getText());
		assertEquals("London is the capital of the United Kingdom.", opt.getFeedback());
	}

	@Test
	public void mcQuestionData_addCorrectAnswer_ignoresNullAndBlankText() {
		MCQuestionData data = new MCQuestionData();
		data.addCorrectAnswer(null);
		data.addCorrectAnswer(new McAnswerOption("  ", "some feedback"));
		data.addCorrectAnswer(new McAnswerOption("Valid answer", "explanation"));
		assertEquals(1, data.getCorrectAnswers().size());
		assertEquals("Valid answer", data.getCorrectAnswers().get(0).getText());
	}

	@Test
	public void mcQuestionData_addWrongAnswer_ignoresNullAndBlankText() {
		MCQuestionData data = new MCQuestionData();
		data.addWrongAnswer(null);
		data.addWrongAnswer(new McAnswerOption("", "some feedback"));
		data.addWrongAnswer(new McAnswerOption("Plausible distractor", "explanation"));
		assertEquals(1, data.getWrongAnswers().size());
		assertEquals("Plausible distractor", data.getWrongAnswers().get(0).getText());
	}

	@Test
	public void mcQuestionData_feedbackMayBeNull() {
		// The feedback field is optional — a null feedback must not break construction.
		McAnswerOption opt = new McAnswerOption("Answer", null);
		assertNotNull(opt);
		assertEquals("Answer", opt.getText());
		assertFalse("Null feedback must not be treated as non-blank",
				opt.getFeedback() != null && !opt.getFeedback().isBlank());
	}

	@Test
	public void mcQuestionData_setCorrectAndWrongAnswers_roundTrip() {
		MCQuestionData data = new MCQuestionData();
		List<McAnswerOption> correct = List.of(
				new McAnswerOption("Correct A", "Feedback A"),
				new McAnswerOption("Correct B", "Feedback B"));
		List<McAnswerOption> wrong = List.of(
				new McAnswerOption("Wrong X", "Feedback X"));
		data.setCorrectAnswers(correct);
		data.setWrongAnswers(wrong);
		assertEquals(2, data.getCorrectAnswers().size());
		assertEquals("Feedback A", data.getCorrectAnswers().get(0).getFeedback());
		assertEquals(1, data.getWrongAnswers().size());
		assertEquals("Wrong X", data.getWrongAnswers().get(0).getText());
	}
}
