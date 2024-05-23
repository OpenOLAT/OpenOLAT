/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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

import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * AI service API for different AI services
 * 
 * Initial date: 22.05.2024<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface AiSPI {
	
	/**
	 * @return The technical identifier of the SPI
	 */
	public String getId();

	/**
	 * @return The human readable identifier / name of the SPI, e.g the prouduct name
	 */
	public String getName();
	
	/**
	 * Factory method to create an admin interface to configure the SPI
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public Controller createAdminController(UserRequest ureq, WindowControl wControl);

	
	/**
	 * @return true: this SPI can be used to generate questions; false: the SPI can
	 *         not be used to generate questions
	 */
	public boolean isQuestionGenerationEnabled();

	/**
	 * @return The LLM used for generating questions
	 */
	public String getQuestionGenerationModel();

	
	/**
	 * Generative method to create multiple choice items from a given text input
	 * 
	 * @param input  The original input text. Make sure it is not longer than your
	 *               model supports
	 * @param number the number of questions that shall be generated
	 * @return
	 */
	public AiMCQuestionsResponse generateMCQuestionsResponse(String input, int number);

}
