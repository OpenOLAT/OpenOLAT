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
package org.olat.modules.selectus.model;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PersonGender {
	
	unspecified("-", "edit.application.gender.unspecified"),
	male("m", "edit.application.gender.male"),
	female("f", "edit.application.gender.female"),
	other("o", "edit.application.gender.other"),
	transgender("t", "edit.application.gender.transgender"),
	nonbinary("n", "edit.application.gender.nonbinary"),
	
	;
	
	private final String gender;
	private final String i18nKey;
	
	private PersonGender(String gender, String i18nKey) {
		this.gender = gender;
		this.i18nKey = i18nKey;
	}
	
	public String gender() {
		return gender;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static boolean hasValue(PersonGender[] genders, PersonGender val) {
		boolean has = false;
		if(genders != null) {
			for(PersonGender pg:genders) {
				if(val == pg) {
					has = true;
				}
			}
		}
		return has;
	}
	
	public static PersonGender genderOf(Person person) {
		return genderOf(person.getGender());
	}
	
	public static PersonGender genderOf(String gender) {
		PersonGender g;
		if(!StringHelper.containsNonWhitespace(gender)) {
			g = PersonGender.unspecified;
		} else if("male".equals(gender) || "m".equals(gender)) {
			g = PersonGender.male;
		} else if("female".equals(gender) || "f".equals(gender)) {
			g = PersonGender.female;
		} else if(gender.startsWith("o") || gender.startsWith("a")) {
			g = PersonGender.other;
		} else if(gender.startsWith("transgender") || gender.startsWith("t")) {
			g = PersonGender.transgender;
		} else if(gender.startsWith("nonbinary") || gender.startsWith("n")) {
			g = PersonGender.nonbinary;
		} else {
			g = PersonGender.unspecified;
		}
		return g;
	}
}
