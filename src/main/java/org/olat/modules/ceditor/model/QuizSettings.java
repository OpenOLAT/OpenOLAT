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
package org.olat.modules.ceditor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizSettings {

	private String title;
	private String description;
	private List<QuizQuestion> questions;
	private BlockLayoutSettings layoutSettings;
	private AlertBoxSettings alertBoxSettings;

	public QuizSettings() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BlockLayoutSettings getLayoutSettings() {
		return layoutSettings;
	}

	public void setLayoutSettings(BlockLayoutSettings layoutSettings) {
		this.layoutSettings = layoutSettings;
	}

	public AlertBoxSettings getAlertBoxSettings() {
		return alertBoxSettings;
	}

	public List<QuizQuestion> getQuestions() {
		if (questions == null) {
			questions = new ArrayList<>();
		}
		return questions;
	}

	public void setQuestions(List<QuizQuestion> questions) {
		this.questions = questions;
	}

	public void setAlertBoxSettings(AlertBoxSettings alertBoxSettings) {
		this.alertBoxSettings = alertBoxSettings;
	}
}
