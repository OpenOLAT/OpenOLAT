/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

/**
 * 
 * Initial date: 14 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumComposerConfig {
	
	private boolean flat = false;
	private boolean rootElementsOnly = false;
	private boolean defaultNumOfParticipants = false;
	
	private String title;
	private int titleSize;
	private String titleIconCssClass;
	
	public static CurriculumComposerConfig curriculumView() {
		CurriculumComposerConfig config = new CurriculumComposerConfig();
		config.setFlat(false);
		config.setRootElementsOnly(true);
		return config;
	}
	
	public static CurriculumComposerConfig implementationsView() {
		CurriculumComposerConfig config = new CurriculumComposerConfig();
		config.setFlat(true);
		config.setRootElementsOnly(true);
		return config;
	}

	public boolean isFlat() {
		return flat;
	}

	public void setFlat(boolean flat) {
		this.flat = flat;
	}

	public boolean isRootElementsOnly() {
		return rootElementsOnly;
	}

	public void setRootElementsOnly(boolean rootElementsOnly) {
		this.rootElementsOnly = rootElementsOnly;
	}

	public boolean isDefaultNumOfParticipants() {
		return defaultNumOfParticipants;
	}

	public void setDefaultNumOfParticipants(boolean defaultNumOfParticipants) {
		this.defaultNumOfParticipants = defaultNumOfParticipants;
	}

	public String getTitle() {
		return title;
	}
	
	public int getTitleSize() {
		return titleSize;
	}
	
	public String getTitleIconCssClass() {
		return titleIconCssClass;
	}

	public void setTitle(String title, int titleSize, String titleIconCssClass) {
		this.title = title;
		this.titleSize = titleSize;
		this.titleIconCssClass = titleIconCssClass;
	}
}
