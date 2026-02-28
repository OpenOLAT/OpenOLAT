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
		assertTrue(msg.text().contains("assessment questions"));
	}

	@Test
	public void createQuestionSystemMessage_german() {
		SystemMessage msg = helper.createQuestionSystemMessage(Locale.GERMAN);
		assertNotNull(msg);
		assertTrue(msg.text().contains("Testfragen"));
	}

	@Test
	public void createQuestionSystemMessage_unsupportedLocale_returnsNull() {
		assertNull(helper.createQuestionSystemMessage(Locale.FRENCH));
	}

	@Test
	public void createQuestionSystemMessage_nullLocale_returnsNull() {
		assertNull(helper.createQuestionSystemMessage(null));
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
	}

	@Test
	public void createChoiceQuestionUserMessage_german_containsFormattedNumbers() {
		UserMessage msg = helper.createChoiceQuestionUserMessage("ein Text", 2, 1, 2, Locale.GERMAN);
		assertNotNull(msg);
		String text = msg.singleText();
		assertTrue(text.contains("2 verschiedene Multiple-Choice Fragen"));
		assertTrue(text.contains("1 korrekten"));
		assertTrue(text.contains("2 falschen"));
		assertTrue(text.contains("ein Text"));
	}

	@Test
	public void createChoiceQuestionUserMessage_unsupportedLocale_returnsNull() {
		assertNull(helper.createChoiceQuestionUserMessage("text", 1, 1, 1, Locale.FRENCH));
	}

	@Test
	public void createChoiceQuestionUserMessage_nullLocale_returnsNull() {
		assertNull(helper.createChoiceQuestionUserMessage("text", 1, 1, 1, null));
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
}
