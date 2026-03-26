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
		return textService.detectLocale(input);
	}

	/**
	 * Create a system prompt for question generation for the given locale.
	 *
	 * @param locale
	 * @return The system message, or null if locale is unsupported
	 */
	public SystemMessage createQuestionSystemMessage(Locale locale) {
		return SystemMessage.from(
				"You are an expert assessment designer. You create challenging multiple choice questions " +
				"that test genuine understanding, not just surface recall. " +
				"You always verify that correct answers are factually accurate and that wrong answers are " +
				"genuinely incorrect. Wrong answers must be plausible distractors.");
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
		String langName = (locale != null) ? locale.getDisplayLanguage(Locale.ENGLISH) : "English";
		return UserMessage.from("""
					Create %s different multiple choice questions with %s correct and %s wrong answers each.

					Rules for answer quality:
					- CORRECT answers: Must be verifiably true based on the text. Double-check each one.
					- WRONG answers: Must be clearly incorrect but plausible. Use these strategies:
					  * Take a correct fact and introduce a subtle but meaningful change (e.g. wrong number, swapped name, changed date)
					  * Invent a plausible-sounding but fictitious detail
					  * Use a true statement from a different context that does not answer this specific question
					- Each wrong answer must be unambiguously wrong — a knowledgeable person must be able to reject it.
					- Each correct answer must be unambiguously correct — it must be fully supported by the text.

					Language and terminology rules:
					- Generate all questions, answers, and metadata in %4$s.
					- Use the exact terminology and wording from the input text — do NOT translate or rephrase the source material.
					- Questions must be self-contained. Do NOT refer to "the text", "the passage", "the article", or similar. The input is the knowledge domain, not a reading comprehension exercise.
					- XML tag names remain in English.

					Use the following XML format for each question:

					<item>
						<title>Title</title>
						<topic>Topic</topic>
						<subject>Subject area</subject>
						<keywords>Keywords</keywords>
						<question>Question</question>
						<answers>
							<correct>Correct answer</correct>
							<wrong>Wrong answer</wrong>
						</answers>
					</item>

					-----

					Use this text for the questions:

					-----

					""".formatted(number, correct, wrong, langName) + input);
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
				Analyze this image and generate structured metadata in XML format.

				<image-description>
				  <title>Short descriptive title</title>
				  <description>Short paragraph describing the image</description>
				  <alt-text>Accessible description for a blind person</alt-text>
				  <subject>Subject area</subject>
				  <orientation>horizontal</orientation>
				  <colors><color>Dominant color</color></colors>
				  <categories><category>Category</category></categories>
				  <keywords><keyword>Keyword</keyword></keywords>
				</image-description>

				Rules:
				- Title: short and concise, max 10 words.
				- Description: 2-3 sentences describing the image content in detail, suitable for full-text search.
				- Alt text: an accessible description for screen readers. It must be: \
				precise and informative, only relevant details, helpful, very short and to the point, avoids redundancy. \
				Do NOT start with "Image of" or "Picture of".
				- Subject: the academic or professional subject area the image belongs to \
				(e.g. "Biology", "Computer Science", "Marketing", "History", "Mathematics", "Medicine", "Art"). \
				Use one or two words. Use the most specific subject that fits.
				- Orientation: exactly one of "horizontal", "vertical", or "square" based on the image aspect ratio.
				- Colors: use ONLY if the image has a clearly dominant color appearance. \
				If one color dominates, output one <color>. If two colors dominate, output two. \
				If the image has no clear dominant color, output NO <color> elements at all. \
				For black-and-white or grayscale images, output <color>b&w</color>.
				- Categories: pick 1 dominant category from this list: nature, city, portrait, architecture, \
				food, technology, abstract, animals, sport, education. Add a second ONLY if clearly needed. \
				Use only 1 or 2 words per category.
				- Keywords: 1-4 descriptive tags, stock-photo-library style. Always use singular form \
				(e.g. "tree" not "trees", "person" not "people"), even when multiple instances appear in the image.
				- Respond in %s.
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
		data.setSubject(getNextElement(descBlock, "subject"));
		data.setOrientation(getNextElement(descBlock, "orientation"));

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
