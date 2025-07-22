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
package org.olat.modules.creditpoint;

/**
 * 
 * Initial date: 17 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CreditPointExpirationType {
	
	DEFAULT(null, null),
	DAY("expiration.unit.day", "expiration.unit.days"),
	MONTH("expiration.unit.month", "expiration.unit.months"),
	YEAR("expiration.unit.year", "expiration.unit.years");
	
	private final String i18nSingular;
	private final String i18nPlural;
	
	private CreditPointExpirationType(String i18nSingular, String i18nPlural) {
		this.i18nSingular = i18nSingular;
		this.i18nPlural = i18nPlural;
	}
	
	public String i18n(Integer val) {
		if(val == null || val.intValue() <= 1) {
			return i18nSingular();
		}
		return i18nPlural();
	}

	public final String i18nSingular() {
		return i18nSingular;
	}

	public final String i18nPlural() {
		return i18nPlural;
	}
}
