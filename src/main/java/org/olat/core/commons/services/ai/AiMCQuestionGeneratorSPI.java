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

import java.util.List;

import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;

/**
 * Feature interface for AI services that can generate multiple choice questions.
 * Implement this interface in addition to AiSPI to provide MC question generation.
 *
 * Initial date: 25.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface AiMCQuestionGeneratorSPI {

	String FEATURE_ID = "mc-question-generator";

	/**
	 * Generative method to create multiple choice items from a given text input
	 *
	 * @param input  The original input text. Make sure it is not longer than your
	 *               model supports
	 * @param number the number of questions that shall be generated
	 * @return
	 */
	AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number);

	/**
	 * Set the language model to use for MC question generation.
	 * Only rebuilds the underlying model if the name actually changes.
	 *
	 * @param model The model name
	 */
	void setMCGeneratorModel(String model);

	/**
	 * @return The language model used for MC question generation
	 */
	String getMCGeneratorModel();

	/**
	 * @return The list of available model names for MC question generation.
	 *         Used to populate the model selection dropdown in the admin UI.
	 */
	List<String> getAvailableMCGeneratorModels();

}
