/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.control.Event;

/**
 * Initial date: 2022-12-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuestionSelectedEvent extends Event {

	private static final long serialVersionUID = 1239734196124599858L;
	private static final String COMMAND = "question.selected";
	private final String questionId;
	private final long startTimeInMillis;

	public QuestionSelectedEvent(String questionId, long startTimeInMillis) {
		super(COMMAND);
		this.questionId = questionId;
		this.startTimeInMillis = startTimeInMillis;
	}

	public String getQuestionId() {
		return questionId;
	}

	public long getStartTimeInMillis() {
		return startTimeInMillis;
	}
}
