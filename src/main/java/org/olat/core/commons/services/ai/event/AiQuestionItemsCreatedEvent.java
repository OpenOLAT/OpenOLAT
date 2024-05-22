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
package org.olat.core.commons.services.ai.event;

import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.modules.qpool.QuestionItem;
/**
 * 
 * Event is fired when an AI service created one or multiple QTI question items
 * 
 * Initial date: 22.05.2024<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiQuestionItemsCreatedEvent extends Event {
	
	private static final long serialVersionUID = 5839754884543268793L;

	private List<QuestionItem> questionItems;

	/**
	 * Constructor
	 * 
	 * @param questionItems The new generated question items
	 */
	public AiQuestionItemsCreatedEvent(List<QuestionItem> questionItems) {
		super("ai.error.language.not.supported");
		this.questionItems = questionItems;
	}

	/**
	 * @return The error details or NULL if not available
	 */
	public List<QuestionItem> getQuestionItems() {
		return this.questionItems;
	}
	
}
