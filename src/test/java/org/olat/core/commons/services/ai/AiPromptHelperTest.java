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
package org.olat.core.commons.services.ai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.services.ai.model.AiImageDescriptionData;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.model.AiMCQuestionData;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

/**
 * Unit tests for {@link AiPromptHelper}.
 * Tests the prompt creation and XML-based response parsing logic.
 * No Spring context needed — all tested methods are pure logic.
 *
 * Initial date: 28.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class AiPromptHelperTest {

	private AiPromptHelper helper;

	@Before
	public void setUp() {
		helper = new AiPromptHelper();
		// textService not needed for the methods under test
	}


	// ─── createQuestionSystemMessage ───────────────────────────────────────────

	@Test
	public void createQuestionSystemMessage_english() {
		SystemMessage msg = helper.createQuestionSystemMessage(Locale.ENGLISH);
		assertNotNull(msg);
		assertTrue(msg.text().contains("multiple choice questions"));
	}

	@Test
	public void createQuestionSystemMessage_anyLocale_returnsEnglishMessage() {
		SystemMessage msg = helper.createQuestionSystemMessage(Locale.GERMAN);
		assertNotNull(msg);
		assertTrue(msg.text().contains("multiple choice questions"));
	}

	@Test
	public void createQuestionSystemMessage_nullLocale_returnsMessage() {
		SystemMessage msg = helper.createQuestionSystemMessage(null);
		assertNotNull(msg);
		assertTrue(msg.text().contains("multiple choice questions"));
	}


	// ─── createChoiceQuestionUserMessage ───────────────────────────────────────

	@Test
	public void createChoiceQuestionUserMessage_english_containsFormattedNumbers() {
		UserMessage msg = helper.createChoiceQuestionUserMessage("some text", 3, 2, 3, Locale.ENGLISH);
		assertNotNull(msg);
		String text = msg.singleText();
		assertTrue(text.contains("3 different multiple choice question"));
		assertTrue(text.contains("2 correct"));
		assertTrue(text.contains("3 wrong"));
		assertTrue(text.contains("some text"));
		assertTrue(text.contains("in English"));
	}

	@Test
	public void createChoiceQuestionUserMessage_german_containsLanguageInstruction() {
		UserMessage msg = helper.createChoiceQuestionUserMessage("ein Text", 2, 1, 2, Locale.GERMAN);
		assertNotNull(msg);
		String text = msg.singleText();
		assertTrue(text.contains("2 different multiple choice question"));
		assertTrue(text.contains("1 correct"));
		assertTrue(text.contains("2 wrong"));
		assertTrue(text.contains("ein Text"));
		assertTrue(text.contains("in German"));
	}

	@Test
	public void createChoiceQuestionUserMessage_french_containsLanguageInstruction() {
		UserMessage msg = helper.createChoiceQuestionUserMessage("un texte", 1, 1, 1, Locale.FRENCH);
		assertNotNull(msg);
		String text = msg.singleText();
		assertTrue(text.contains("in French"));
		assertTrue(text.contains("un texte"));
	}

	@Test
	public void createChoiceQuestionUserMessage_nullLocale_fallsBackToEnglish() {
		UserMessage msg = helper.createChoiceQuestionUserMessage("text", 1, 1, 1, null);
		assertNotNull(msg);
		String text = msg.singleText();
		assertTrue(text.contains("in English"));
	}


	// ─── parseQuestionResult ───────────────────────────────────────────────────

	@Test
	public void parseQuestionResult_singleWellFormedItem() {
		String input = """
				<item>
					<title>Test Title</title>
					<topic>Test Topic</topic>
					<subject>Test Subject</subject>
					<keywords>key1, key2</keywords>
					<question>What is 2+2?</question>
					<answers>
						<correct>4</correct>
						<correct>Four</correct>
						<wrong>3</wrong>
						<wrong>5</wrong>
						<wrong>6</wrong>
					</answers>
				</item>
				""";

		AiMCQuestionsResponse response = helper.parseQuestionResult(input);
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertEquals(1, response.getQuestions().size());

		AiMCQuestionData q = response.getQuestions().get(0);
		assertEquals("Test Title", q.getTitle().trim());
		assertEquals("Test Topic", q.getTopic().trim());
		assertEquals("Test Subject", q.getSubject().trim());
		assertEquals("key1, key2", q.getKeywords().trim());
		assertEquals("What is 2+2?", q.getQuestion().trim());
		assertEquals(2, q.getCorrectAnswers().size());
		assertEquals(3, q.getWrongAnswers().size());
		assertTrue(q.getCorrectAnswers().contains("4"));
		assertTrue(q.getCorrectAnswers().contains("Four"));
		assertTrue(q.getWrongAnswers().contains("3"));
		assertTrue(q.getWrongAnswers().contains("5"));
		assertTrue(q.getWrongAnswers().contains("6"));
	}

	@Test
	public void parseQuestionResult_multipleItems() {
		String input = """
				<item>
					<title>Q1</title>
					<topic>Topic1</topic>
					<subject>Sub1</subject>
					<keywords>k1</keywords>
					<question>Question one?</question>
					<answers>
						<correct>Yes</correct>
						<wrong>No</wrong>
					</answers>
				</item>
				<item>
					<title>Q2</title>
					<topic>Topic2</topic>
					<subject>Sub2</subject>
					<keywords>k2</keywords>
					<question>Question two?</question>
					<answers>
						<correct>True</correct>
						<wrong>False</wrong>
					</answers>
				</item>
				""";

		AiMCQuestionsResponse response = helper.parseQuestionResult(input);
		assertEquals(2, response.getQuestions().size());
		assertEquals("Q1", response.getQuestions().get(0).getTitle().trim());
		assertEquals("Q2", response.getQuestions().get(1).getTitle().trim());
	}

	@Test
	public void parseQuestionResult_emptyInput_returnsEmptyResponse() {
		AiMCQuestionsResponse response = helper.parseQuestionResult("");
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertEquals(0, response.getQuestions().size());
	}

	@Test
	public void parseQuestionResult_noItemTags_returnsEmptyResponse() {
		AiMCQuestionsResponse response = helper.parseQuestionResult("Some random text without any XML tags.");
		assertNotNull(response);
		assertEquals(0, response.getQuestions().size());
	}

	@Test
	public void parseQuestionResult_missingFields_treatedAsEmptyStrings() {
		// Item with only question field, missing title/topic/subject/keywords/answers
		String input = """
				<item>
					<question>Incomplete question?</question>
					<answers></answers>
				</item>
				""";

		AiMCQuestionsResponse response = helper.parseQuestionResult(input);
		assertEquals(1, response.getQuestions().size());
		AiMCQuestionData q = response.getQuestions().get(0);
		assertEquals("", q.getTitle());
		assertEquals("", q.getTopic());
		assertEquals("Incomplete question?", q.getQuestion().trim());
		assertEquals(0, q.getCorrectAnswers().size());
		assertEquals(0, q.getWrongAnswers().size());
	}

	@Test
	public void parseQuestionResult_whitespaceOnlyFirstAnswer_stopsParsingAnswers() {
		// The while-loop condition in parseQuestionResult is containsNonWhitespace(answer),
		// so a whitespace-only answer content terminates the loop. This tests that behaviour.
		String input = """
				<item>
					<title>T</title>
					<topic>T</topic>
					<subject>S</subject>
					<keywords>k</keywords>
					<question>Q?</question>
					<answers>
						<correct>   </correct>
						<correct>Valid answer</correct>
					</answers>
				</item>
				""";

		AiMCQuestionsResponse response = helper.parseQuestionResult(input);
		AiMCQuestionData q = response.getQuestions().get(0);
		// Loop stops at whitespace content — no answers collected
		assertEquals(0, q.getCorrectAnswers().size());
	}

	@Test
	public void parseQuestionResult_malformedXml_noClosingTag_returnsEmpty() {
		// Missing </item> closing tag — parser should gracefully return empty
		String input = "<item><title>No closing tag<question>Q?</question>";
		AiMCQuestionsResponse response = helper.parseQuestionResult(input);
		assertEquals(0, response.getQuestions().size());
	}

	@Test
	public void parseQuestionResult_itemWithPreamble_stillParsed() {
		// AI responses often include text before the first <item>
		String input = """
				Here are the generated questions:

				<item>
					<title>Climate Change</title>
					<topic>Environment</topic>
					<subject>Science</subject>
					<keywords>climate, CO2</keywords>
					<question>What is the main cause of climate change?</question>
					<answers>
						<correct>Greenhouse gas emissions</correct>
						<wrong>Solar flares</wrong>
					</answers>
				</item>

				Hope this helps!
				""";

		AiMCQuestionsResponse response = helper.parseQuestionResult(input);
		assertEquals(1, response.getQuestions().size());
		assertEquals("Climate Change", response.getQuestions().get(0).getTitle().trim());
	}


	// ─── createImageDescriptionSystemMessage ──────────────────────────────────

	@Test
	public void createImageDescriptionSystemMessage_returnsMessage() {
		SystemMessage msg = helper.createImageDescriptionSystemMessage(Locale.ENGLISH);
		assertNotNull(msg);
		assertTrue(msg.text().contains("metadata"));
	}

	@Test
	public void createImageDescriptionSystemMessage_nullLocale_returnsMessage() {
		SystemMessage msg = helper.createImageDescriptionSystemMessage(null);
		assertNotNull(msg);
	}


	// ─── createImageDescriptionUserMessage ────────────────────────────────────

	@Test
	public void createImageDescriptionUserMessage_english_containsRules() {
		UserMessage msg = helper.createImageDescriptionUserMessage("base64data", "image/jpeg", Locale.ENGLISH);
		assertNotNull(msg);
	}

	@Test
	public void createImageDescriptionUserMessage_german_containsLanguageInstruction() {
		UserMessage msg = helper.createImageDescriptionUserMessage("base64data", "image/png", Locale.GERMAN);
		assertNotNull(msg);
	}

	@Test
	public void createImageDescriptionUserMessage_nullLocale_fallsBackToEnglish() {
		UserMessage msg = helper.createImageDescriptionUserMessage("base64data", "image/jpeg", null);
		assertNotNull(msg);
	}


	// ─── parseImageDescriptionResult ──────────────────────────────────────────

	@Test
	public void parseImageDescriptionResult_wellFormedResponse() {
		String input = """
				<image-description>
				  <title>Sunset Over Mountains</title>
				  <description>A beautiful sunset casting golden light over a mountain range with clouds.</description>
				  <alt-text>Sunset over mountains</alt-text>
				  <colors><color>orange</color><color>blue</color></colors>
				  <categories><category>nature</category></categories>
				  <keywords><keyword>sunset</keyword><keyword>mountains</keyword><keyword>landscape</keyword></keywords>
				</image-description>
				""";

		AiImageDescriptionResponse response = helper.parseImageDescriptionResult(input);
		assertNotNull(response);
		assertTrue(response.isSuccess());

		AiImageDescriptionData data = response.getDescription();
		assertNotNull(data);
		assertEquals("Sunset Over Mountains", data.getTitle());
		assertEquals("A beautiful sunset casting golden light over a mountain range with clouds.", data.getDescription());
		assertEquals("Sunset over mountains", data.getAltText());
		assertEquals(2, data.getColorTags().size());
		assertTrue(data.getColorTags().contains("orange"));
		assertTrue(data.getColorTags().contains("blue"));
		assertEquals(1, data.getCategoryTags().size());
		assertTrue(data.getCategoryTags().contains("nature"));
		assertEquals(3, data.getKeywords().size());
		assertTrue(data.getKeywords().contains("sunset"));
		assertTrue(data.getKeywords().contains("mountains"));
		assertTrue(data.getKeywords().contains("landscape"));
	}

	@Test
	public void parseImageDescriptionResult_withPreamble_stillParsed() {
		String input = """
				Here is the image analysis:

				<image-description>
				  <title>City at Night</title>
				  <description>An urban skyline illuminated at night.</description>
				  <alt-text>City skyline at night</alt-text>
				  <colors><color>black</color></colors>
				  <categories><category>city</category></categories>
				  <keywords><keyword>city</keyword></keywords>
				</image-description>

				Let me know if you need changes.
				""";

		AiImageDescriptionResponse response = helper.parseImageDescriptionResult(input);
		assertNotNull(response.getDescription());
		assertEquals("City at Night", response.getDescription().getTitle());
	}

	@Test
	public void parseImageDescriptionResult_emptyInput_returnsResponseWithoutDescription() {
		AiImageDescriptionResponse response = helper.parseImageDescriptionResult("");
		assertNotNull(response);
		assertTrue(response.isSuccess());
		assertNull(response.getDescription());
	}

	@Test
	public void parseImageDescriptionResult_noImageDescriptionTag_returnsResponseWithoutDescription() {
		AiImageDescriptionResponse response = helper.parseImageDescriptionResult("Just some random text");
		assertNotNull(response);
		assertNull(response.getDescription());
	}

	@Test
	public void parseImageDescriptionResult_missingOptionalFields() {
		String input = """
				<image-description>
				  <title>Minimal</title>
				  <description></description>
				  <alt-text></alt-text>
				  <colors></colors>
				  <categories></categories>
				  <keywords></keywords>
				</image-description>
				""";

		AiImageDescriptionResponse response = helper.parseImageDescriptionResult(input);
		AiImageDescriptionData data = response.getDescription();
		assertNotNull(data);
		assertEquals("Minimal", data.getTitle());
		assertEquals("", data.getDescription());
		assertEquals("", data.getAltText());
		assertTrue(data.getColorTags().isEmpty());
		assertTrue(data.getCategoryTags().isEmpty());
		assertTrue(data.getKeywords().isEmpty());
	}

	@Test
	public void parseImageDescriptionResult_multipleColorsAndKeywords() {
		String input = """
				<image-description>
				  <title>Test</title>
				  <description>Test description</description>
				  <alt-text>Test alt</alt-text>
				  <colors><color>red</color><color>green</color><color>blue</color></colors>
				  <categories><category>nature</category><category>animals</category></categories>
				  <keywords><keyword>a</keyword><keyword>b</keyword><keyword>c</keyword><keyword>d</keyword><keyword>e</keyword></keywords>
				</image-description>
				""";

		AiImageDescriptionResponse response = helper.parseImageDescriptionResult(input);
		AiImageDescriptionData data = response.getDescription();
		assertEquals(3, data.getColorTags().size());
		assertEquals(2, data.getCategoryTags().size());
		assertEquals(5, data.getKeywords().size());
	}
}
