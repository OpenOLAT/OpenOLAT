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
 * Initial date: 24 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementListConfig {
	
	private boolean preparationWarning;
	private boolean participantsOnly;
	
	private CurriculumElementListConfig(boolean preparationWarning, boolean participantsOnly) {
		this.preparationWarning = preparationWarning;
		this.participantsOnly = participantsOnly;
	}
	
	public static final CurriculumElementListConfig defaultConfig() {
		return new CurriculumElementListConfig(false, false);
	}
	
	public static final CurriculumElementListConfig config(boolean preparationWarning, boolean participantsOnly) {
		return new CurriculumElementListConfig(preparationWarning, participantsOnly);
	}
	
	public boolean preparationWarning() {
		return preparationWarning;
	}

	public boolean participantsOnly() {
		return participantsOnly;
	}
}
