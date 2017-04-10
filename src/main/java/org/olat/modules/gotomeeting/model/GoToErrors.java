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
package org.olat.modules.gotomeeting.model;

/**
 * 
 * Initial date: 24.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum GoToErrors {
	Unkown(""),
	DuplicateRegistrant(""),
	OrganizerOverlap("error.organizer.overbooked"),
	TrainingOver("error.code.TrainingOver"),
	InvalidRequest("error.code.unkown"),
	InvalidToken("error.code.InvalidToken"),
	NoSuchTraining("error.code.NoSuchTraining"),
	TrainingInSession("error.code.TrainingInSession"),
	InvalidTrainingDatesMaxDuration("error.code.InvalidTrainingDatesMaxDuration");
	
	private final String i18nKey;
	
	private GoToErrors(String i18nKey) {
		this.i18nKey = i18nKey;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static final GoToErrors valueOfOrNull(String val) {
		for(GoToErrors error:GoToErrors.values()) {
			if(error.name().equals(val)) {
				return error;
			}
		}
		return null;
	}
}
