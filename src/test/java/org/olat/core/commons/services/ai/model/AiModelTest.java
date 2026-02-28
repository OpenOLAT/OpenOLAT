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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the AI model classes:
 * {@link AiResponse}, {@link AiMCQuestionsResponse}, {@link AiMCQuestionData}.
 *
 * Initial date: 28.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class AiModelTest {

	// ─── AiResponse ────────────────────────────────────────────────────────────

	@Test
	public void aiResponse_noError_isSuccess() {
		AiResponse response = new AiResponse();
		assertTrue(response.isSuccess());
		assertNull(response.getError());
	}

	@Test
	public void aiResponse_withError_isNotSuccess() {
		AiResponse response = new AiResponse();
		response.setError("something went wrong");
		assertFalse(response.isSuccess());
		assertEquals("something went wrong", response.getError());
	}


	// ─── AiMCQuestionsResponse ─────────────────────────────────────────────────

	@Test
	public void aiMCQuestionsResponse_addQuestion_increasesCount() {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		assertEquals(0, response.getQuestions().size());

		response.addQuestion(new AiMCQuestionData());
		assertEquals(1, response.getQuestions().size());

		response.addQuestion(new AiMCQuestionData());
		assertEquals(2, response.getQuestions().size());
	}

	@Test
	public void aiMCQuestionsResponse_isSuccessWhenNoError() {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		assertTrue(response.isSuccess());
	}

	@Test
	public void aiMCQuestionsResponse_errorPropagates() {
		AiMCQuestionsResponse response = new AiMCQuestionsResponse();
		response.setError("API call failed");
		assertFalse(response.isSuccess());
	}


	// ─── AiMCQuestionData ──────────────────────────────────────────────────────

	@Test
	public void aiMCQuestionData_addCorrectAnswer_nonBlank_added() {
		AiMCQuestionData data = new AiMCQuestionData();
		data.addCorrectAnswer("Correct answer");
		assertEquals(1, data.getCorrectAnswers().size());
		assertEquals("Correct answer", data.getCorrectAnswers().get(0));
	}

	@Test
	public void aiMCQuestionData_addCorrectAnswer_blank_filtered() {
		AiMCQuestionData data = new AiMCQuestionData();
		data.addCorrectAnswer("   ");
		data.addCorrectAnswer("");
		data.addCorrectAnswer(null);
		assertEquals(0, data.getCorrectAnswers().size());
	}

	@Test
	public void aiMCQuestionData_addWrongAnswer_nonBlank_added() {
		AiMCQuestionData data = new AiMCQuestionData();
		data.addWrongAnswer("Wrong answer");
		assertEquals(1, data.getWrongAnswers().size());
		assertEquals("Wrong answer", data.getWrongAnswers().get(0));
	}

	@Test
	public void aiMCQuestionData_addWrongAnswer_blank_filtered() {
		AiMCQuestionData data = new AiMCQuestionData();
		data.addWrongAnswer("  ");
		data.addWrongAnswer("");
		assertEquals(0, data.getWrongAnswers().size());
	}

	@Test
	public void aiMCQuestionData_settersAndGetters() {
		AiMCQuestionData data = new AiMCQuestionData();
		data.setTitle("Title");
		data.setTopic("Topic");
		data.setSubject("Subject");
		data.setKeywords("key1, key2");
		data.setQuestion("Question?");

		assertEquals("Title", data.getTitle());
		assertEquals("Topic", data.getTopic());
		assertEquals("Subject", data.getSubject());
		assertEquals("key1, key2", data.getKeywords());
		assertEquals("Question?", data.getQuestion());
	}
}
