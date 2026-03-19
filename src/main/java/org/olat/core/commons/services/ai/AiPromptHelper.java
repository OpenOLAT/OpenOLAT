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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.model.AiImageDescriptionData;
import org.olat.core.commons.services.ai.model.AiImageDescriptionResponse;
import org.olat.core.commons.services.ai.model.AiMCQuestionData;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;

/**
 *
 * Provider-agnostic prompt helper for AI-based question generation. Uses
 * LangChain4j message types and can be shared across all AiSPI implementations.
 *
 * Initial date: 20.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class AiPromptHelper {

	@Autowired
	private TextService textService;

	/**
	 * Try to detect the language for the given input.
	 *
	 * @param input the input text for the chat
	 * @return The locale if a supported language has been detected or NULL otherwise
	 */
	public Locale detectSupportedLocale(String input) {
		Locale locale = textService.detectLocale(input);
		if (locale != null
				&& (locale.getLanguage().equals("en") || locale.getLanguage().equals("de"))) {
			return locale;
		}
		return null;
	}

	/**
	 * Create a system prompt for question generation for the given locale.
	 *
	 * @param locale
	 * @return The system message, or null if locale is unsupported
	 */
	public SystemMessage createQuestionSystemMessage(Locale locale) {
		if (locale != null && locale.getLanguage().equals("en")) {
			return SystemMessage.from("You are an assistant to create assessment questions based on text input.");
		} else if (locale != null && locale.getLanguage().equals("de")) {
			return SystemMessage.from("Du bist ein Helfer um Testfragen aus einem Text zu erstellen.");
		}
		return null;
	}

	/**
	 * Create the user prompt for the generation of a multiple choice question.
	 *
	 * @param input   The input text that serves as the knowledge base for the
	 *                question
	 * @param number  The number of questions to be generated
	 * @param correct The number of correct answers
	 * @param wrong   The number of wrong answers
	 * @param locale  The prompt locale (must be same as input)
	 * @return The user message, or null if locale is unsupported
	 */
	public UserMessage createChoiceQuestionUserMessage(String input, int number, int correct, int wrong, Locale locale) {
		if (locale != null && locale.getLanguage().equals("en")) {
			return UserMessage.from("""
						Create %s different multiple choice question with %s correct and %s wrong answers.
						Use the following format for each question:

						<item>
						 	<title>Title</title>
						 	<topic>Topic</topic>
						 	<subject>Subject area</subject>
							<keywords>Keywords</keywords>
						 	<question>Question</question>
							<answers>
								<correct>Correct answer</correct>
								<correct>Correct answer</correct>
								<wrong>Wrong answer</wrong>
								<wrong>Wrong answer</wrong>
								<wrong>Wrong answer</wrong>
							</answers>
						</item>

						-----

						Use this text for the questions:

						-----

						""".formatted(number, correct, wrong) + input);
		} else if (locale != null && locale.getLanguage().equals("de")) {
			return UserMessage.from("""
						Erstelle %s verschiedene Multiple-Choice Fragen mit %s korrekten und %s falschen Antworten.
						Benutze das folgende Format für jede einzelne Frage:

						<item>
							<title>Titel</title>
						 	<topic>Thema</topic>
						 	<subject>Fachbereich</subject>
							<keywords>Schlüsselwörter</keywords>
							<question>Frage</question>
							<answers>
								<correct>Korrekte Antwort</correct>
								<correct>Korrekte Antwort</correct>
								<wrong>Falsche Antwort</wrong>
								<wrong>Falsche Antwort</wrong>
								<wrong>Falsche Antwort</wrong>
							</answers>
						</item>

						-----

						Verwende diesen Text für die Fragen:

						-----

						""".formatted(number, correct, wrong) + input);
		}
		return null;
	}

	/**
	 * Method to parse the result of the chat that contains MC questions according
	 * to the prompt. The method tries to parse the XML like input in a fail-save
	 * manner. Invalid XML or missing elements are skipped.
	 *
	 * @param result The text response from the AI chat model
	 * @return
	 */
	public AiMCQuestionsResponse parseQuestionResult(String result) {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		String currentItem = getNextElement(result, "item");
		if (currentItem.length() > 0) {
			result = result.substring(result.indexOf("</item>") + 7);
		}

		while (StringHelper.containsNonWhitespace(currentItem)) {
			AiMCQuestionData mcData = new AiMCQuestionData();
			response.addQuestion(mcData);

			mcData.setTitle(getNextElement(currentItem, "title"));
			mcData.setTopic(getNextElement(currentItem, "topic"));
			mcData.setSubject(getNextElement(currentItem, "subject"));
			mcData.setKeywords(getNextElement(currentItem, "keywords"));
			mcData.setQuestion(getNextElement(currentItem, "question"));

			// find all correct in answers
			String answers = getNextElement(currentItem, "answers");
			String correct = getNextElement(answers, "correct");
			while (StringHelper.containsNonWhitespace(correct)) {
				mcData.addCorrectAnswer(correct);
				answers = answers.substring(answers.indexOf("</correct>") + 10);
				correct = getNextElement(answers, "correct");
			}
			// reset, find all wrong in answers
			answers = getNextElement(currentItem, "answers");
			String wrong = getNextElement(answers, "wrong");
			while (StringHelper.containsNonWhitespace(wrong)) {
				mcData.addWrongAnswer(wrong);
				answers = answers.substring(answers.indexOf("</wrong>") + 8);
				wrong = getNextElement(answers, "wrong");
			}

			currentItem = getNextElement(result, "item");
			if (currentItem.length() > 0) {
				result = result.substring(result.indexOf("</item>") + 7);
			}
		}
		return response;
	}


	/**
	 * Create a system prompt for image description generation.
	 *
	 * @param locale unused, kept for API consistency
	 * @return The system message
	 */
	public SystemMessage createImageDescriptionSystemMessage(Locale locale) {
		return SystemMessage.from("You are an assistant that analyzes images and generates structured metadata for them.");
	}

	/**
	 * Create a multimodal user message for image description generation.
	 * Uses a single English prompt with a "respond in [language]" instruction
	 * so the model produces output in the user's language.
	 *
	 * @param imageBase64 The base64 encoded image
	 * @param mimeType The MIME type of the image
	 * @param locale The locale for the response language
	 * @return The multimodal user message
	 */
	public UserMessage createImageDescriptionUserMessage(String imageBase64, String mimeType, Locale locale) {
		String langName = (locale != null) ? locale.getDisplayLanguage(Locale.ENGLISH) : "English";
		String prompt = """
				Analyze this image and generate the following metadata in XML format:

				<image-description>
				  <title>Short descriptive title</title>
				  <description>Short paragraph describing the image, suitable for search</description>
				  <alt-text>Very short alternative text</alt-text>
				  <colors><color>Color name</color></colors>
				  <categories><category>Category</category></categories>
				  <keywords><keyword>Keyword</keyword></keywords>
				</image-description>

				Rules:
				- Title: short and concise (max 10 words)
				- Description: a short paragraph (2-3 sentences) describing the image content in more detail, suitable for full-text search
				- Alt text: very short accessible text (max 10 words, e.g. "Sunset over the ocean")
				- Colors: 1-2 dominant colors in the image
				- Categories: 1-3 from this list: nature, city, portrait, architecture, food, technology, abstract, animals, sport, education
				- Keywords: 3-5 stock-photo-library-style tags
				- Respond in %s
				""".formatted(langName);

		return UserMessage.from(
				ImageContent.from(imageBase64, mimeType),
				TextContent.from(prompt)
		);
	}

	/**
	 * Parse the XML result from an image description AI response.
	 * Uses fail-safe parsing that skips invalid or missing elements.
	 *
	 * @param result The text response from the AI chat model
	 * @return The parsed response
	 */
	public AiImageDescriptionResponse parseImageDescriptionResult(String result) {
		AiImageDescriptionResponse response = new AiImageDescriptionResponse();
		String descBlock = getNextElement(result, "image-description");
		if (!StringHelper.containsNonWhitespace(descBlock)) {
			return response;
		}

		AiImageDescriptionData data = new AiImageDescriptionData();
		data.setTitle(getNextElement(descBlock, "title"));
		data.setDescription(getNextElement(descBlock, "description"));
		data.setAltText(getNextElement(descBlock, "alt-text"));

		String colorsBlock = getNextElement(descBlock, "colors");
		for (String color : parseRepeatingElements(colorsBlock, "color")) {
			data.addColorTag(color);
		}

		String categoriesBlock = getNextElement(descBlock, "categories");
		for (String category : parseRepeatingElements(categoriesBlock, "category")) {
			data.addCategoryTag(category);
		}

		String keywordsBlock = getNextElement(descBlock, "keywords");
		for (String keyword : parseRepeatingElements(keywordsBlock, "keyword")) {
			data.addKeyword(keyword);
		}

		response.setDescription(data);
		return response;
	}

	/**
	 * Parse all occurrences of a repeating element within a block of XML-like text.
	 *
	 * @param block The XML block to search in
	 * @param elementName The element name to extract
	 * @return List of element contents
	 */
	private List<String> parseRepeatingElements(String block, String elementName) {
		List<String> results = new ArrayList<>();
		if (!StringHelper.containsNonWhitespace(block)) {
			return results;
		}
		String remaining = block;
		String value = getNextElement(remaining, elementName);
		while (StringHelper.containsNonWhitespace(value)) {
			results.add(value);
			String endTag = "</" + elementName + ">";
			int endPos = remaining.indexOf(endTag);
			if (endPos < 0) break;
			remaining = remaining.substring(endPos + endTag.length());
			value = getNextElement(remaining, elementName);
		}
		return results;
	}

	/**
	 * Helper to find and extract the content of the next XML element in the given
	 * input.
	 *
	 * @param input
	 * @param elementName
	 * @return The found element content or an empty string if not found or XML
	 *         invalid
	 */
	private String getNextElement(String input, String elementName) {
		String startTag = "<" + elementName + ">";
		String endTag = "</" + elementName + ">";
		int nextStartPos = input.indexOf(startTag);
		int nextEndPos = input.indexOf(endTag);
		if (nextStartPos < 0 || nextEndPos < 0 || nextEndPos < (nextStartPos + startTag.length())) {
			return "";
		}
		return input.substring(nextStartPos + startTag.length(), nextEndPos);
	}

}
