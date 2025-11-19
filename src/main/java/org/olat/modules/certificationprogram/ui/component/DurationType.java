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
package org.olat.modules.certificationprogram.ui.component;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 4 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum DurationType {
	day("unit.day", "unit.days", "period.day", "period.days"),
	week("unit.week", "unit.weeks", "period.week", "period.weeks"),
	month("unit.month", "unit.months", "period.month", "period.months"),
	year("unit.year", "unit.years", "period.year", "period.years");
	
	private final String i18nSingular;
	private final String i18nPlural;
	private final String i18nPeriodSingular;
	private final String i18nPeriodPlural;
	
	private DurationType(String i18nSingular, String i18nPlural, String i18nPeriodSingular, String i18nPeriodPlural) {
		this.i18nSingular = i18nSingular;
		this.i18nPlural = i18nPlural;
		this.i18nPeriodSingular = i18nPeriodSingular;
		this.i18nPeriodPlural = i18nPeriodPlural;
	}

	public String i18nSingular() {
		return i18nSingular;
	}

	public String i18nPlural() {
		return i18nPlural;
	}

	public String i18nPeriodSingular() {
		return i18nPeriodSingular;
	}

	public String i18nPeriodPlural() {
		return i18nPeriodPlural;
	}

	public Date toDate(Date reference, int time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reference);
		switch(this) {
			case day: cal.add(Calendar.DATE, time); break;
			case week: cal.add(Calendar.DATE, time * 7); break;
			case month: cal.add(Calendar.MONTH, time); break;
			case year: cal.add(Calendar.YEAR, time); break;
		}
		return cal.getTime();
	}
	
	public String toString(long value, Translator translator) {
		StringBuilder sb = new StringBuilder();
		sb.append(value).append(" ");
		if(value <= 1) {
			sb.append(translator.translate(i18nSingular()));
		} else {

			sb.append(translator.translate(i18nPlural()));
		}
		return sb.toString();
	}
}
