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

import dev.langchain4j.model.output.structured.Description;

/**
 *
 * Parsed data from chat response to build a question
 *
 * Initial date: 22.05.2024<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class MCQuestionData {
	@Description("Short descriptive title for the question topic, max 10 words")
	private String title;
	@Description("The specific topic within the subject area")
	private String topic;
	@Description("The broad subject area the question belongs to")
	private String subject;
	@Description("Comma-separated keywords related to the question")
	private String keywords;
	@Description("The multiple choice question text, self-contained, no reference to 'the text' or 'the passage'")
	private String question;
	@Description("List of correct answer strings, each verifiably true based on the input")
	private List<String> correctAnswers = new ArrayList<>();
	@Description("List of wrong answer strings, each plausible but unambiguously incorrect")
	private List<String> wrongAnswers = new ArrayList<>();

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
	public List<String> getCorrectAnswers() {
		return correctAnswers;
	}
	public void setCorrectAnswers(List<String> correctAnswers) {
		this.correctAnswers = correctAnswers;
	}
	public void addCorrectAnswer(String correctAnswer) {
		if (StringHelper.containsNonWhitespace(correctAnswer)) {
			this.correctAnswers.add(correctAnswer);
		}
	}
	public List<String> getWrongAnswers() {
		return wrongAnswers;
	}
	public void setWrongAnswers(List<String> wrongAnswers) {
		this.wrongAnswers = wrongAnswers;
	}
	public void addWrongAnswer(String wrongAnswer) {
		if (StringHelper.containsNonWhitespace(wrongAnswer)) {
			this.wrongAnswers.add(wrongAnswer);
		}
	}
}
