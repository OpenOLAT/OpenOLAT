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
package org.olat.core.commons.services.ai.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.output.structured.Description;

/**
 *
 * Parsed data from chat response to build a question.
 *
 * Initial date: 22.05.2024<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class MCQuestionData {
	@JsonProperty(required = true)
	@Description("Short descriptive title for the question topic, max 10 words")
	private String title;
	@JsonProperty(required = true)
	@Description("The specific topic within the subject area")
	private String topic;
	@JsonProperty(required = true)
	@Description("The broad subject area the question belongs to")
	private String subject;
	@JsonProperty(required = true)
	@Description("Comma-separated keywords related to the question")
	private String keywords;
	@JsonProperty(required = true)
	@Description("The multiple choice question text, self-contained, no reference to 'the text' or 'the passage'")
	private String question;
	@JsonProperty(required = true)
	@Description("List of correct answer options, each with a text and a one-sentence feedback explaining why this answer is correct")
	private List<McAnswerOption> correctAnswers = new ArrayList<>();
	@JsonProperty(required = true)
	@Description("List of wrong answer options, each with a text and a one-sentence feedback explaining why this answer is incorrect")
	private List<McAnswerOption> wrongAnswers = new ArrayList<>();

	/**
	 * A single answer option with the displayed text and a per-option feedback
	 * sentence shown as a modal feedback when the learner selects this choice.
	 * <p>
	 * Jackson requires a no-arg constructor and standard getters/setters so that
	 * LangChain4j can deserialise the structured AI response.
	 */
	public static class McAnswerOption {
		@JsonProperty(required = true)
		@Description("The answer text shown to the learner.")
		private String text;
		@JsonProperty(required = true)
		@Description("One-sentence explanation of why this option is correct or wrong, shown as modal feedback when the learner selects this option.")
		private String feedback;

		/** No-arg constructor required by Jackson / LangChain4j deserialisation. */
		public McAnswerOption() {
			//
		}

		public McAnswerOption(String text, String feedback) {
			this.text = text;
			this.feedback = feedback;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getFeedback() {
			return feedback;
		}

		public void setFeedback(String feedback) {
			this.feedback = feedback;
		}
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<McAnswerOption> getCorrectAnswers() {
		return correctAnswers;
	}
	public void setCorrectAnswers(List<McAnswerOption> correctAnswers) {
		this.correctAnswers = correctAnswers;
	}
	public void addCorrectAnswer(McAnswerOption correctAnswer) {
		if (correctAnswer != null && StringHelper.containsNonWhitespace(correctAnswer.getText())) {
			this.correctAnswers.add(correctAnswer);
		}
	}
	public List<McAnswerOption> getWrongAnswers() {
		return wrongAnswers;
	}
	public void setWrongAnswers(List<McAnswerOption> wrongAnswers) {
		this.wrongAnswers = wrongAnswers;
	}
	public void addWrongAnswer(McAnswerOption wrongAnswer) {
		if (wrongAnswer != null && StringHelper.containsNonWhitespace(wrongAnswer.getText())) {
			this.wrongAnswers.add(wrongAnswer);
		}
	}
}
