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
package org.olat.modules.coach.reports;

import java.time.LocalDate;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.CurriculumModule;

/**
 * Initial date: 2025-02-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum ReportTimeUnit {
	day,
	week,
	month,
	year,
	lastFiscalYear,
	currentFiscalYear;

	/**
	 * Returns a date to use as the lower time boundary for this time unit and the given 'time' value.
	 * The 'time' value is interpreted as pointing to the past from the 'reference' date/time. So the 'time'
	 * value is expected to be positive.
	 *
	 * @param reference A date to use as a reference for the date/time computation, typically the current date/time.
	 * @param time A time value matching this time unit, typically a positive number.

	 * @return The date to use as a lower time boundary.
	 */
	public Date fromDate(Date reference, int time) {
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		return fromDate(reference, time, curriculumModule);
	}

	public Date fromDate(Date reference, int time, CurriculumModule curriculumModule) {
		return switch (this) {
			case day -> DateUtils.addDays(reference, -time);
			case week -> DateUtils.addWeeks(reference, -time);
			case month -> DateUtils.addMonth(reference, -time);
			case year -> DateUtils.addYears(reference, -time);

			// 00:00 of the first day of the fiscal year that 'reference' is in:
			case currentFiscalYear -> fiscalYearStart(reference, 
					curriculumModule.getReportsAccountingFiscalYearStartDay(), 
					curriculumModule.getReportsAccountingFiscalYearStartMonth());

			// 00:00 of the first day of the fiscal year before the fiscal year that 'reference' is in:
			case lastFiscalYear -> fiscalYearStart(DateUtils.addYears(reference, -1),
					curriculumModule.getReportsAccountingFiscalYearStartDay(),
					curriculumModule.getReportsAccountingFiscalYearStartMonth());
		};
	}

	/**
	 * Returns a date to use as the upper time boundary for this time unit.

	 * @param reference A date to use as a reference for the date/time computation, typically the current date/time.
	 * 
	 * @return The date to use as an upper time boundary.
	 */
	public Date toDate(Date reference) {
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		return toDate(reference, curriculumModule);
	}
	
	public Date toDate(Date reference, CurriculumModule curriculumModule) {
		return switch (this) {
			case day -> reference;
			case week -> reference;
			case month -> reference;
			case year -> reference;
			case currentFiscalYear -> reference;
			case lastFiscalYear -> fiscalYearStart(reference, 
					curriculumModule.getReportsAccountingFiscalYearStartDay(), 
					curriculumModule.getReportsAccountingFiscalYearStartMonth());
		};
	}
	
	/**
	 * Returns the first day of the fiscal year in which the 'reference' date is located.
	 * <p> 
	 * If the reference date is on the first day of the fiscal year, it returns the reference date.
	 *
	 * @param reference The date to calculate the fiscal year start for.
	 * @param fiscalYearStartDay The day number of the fiscal year start. From 1 to 31.
	 * @param fiscalYearStartMonth The month number of the fiscal year start. From 1 to 12.

	 * @return The day representing the start of the fiscal year that the 'reference' date is located in. Time is set to 00:00.
	 */
	public static Date fiscalYearStart(Date reference, int fiscalYearStartDay, int fiscalYearStartMonth) {
		LocalDate referenceLocalDate = DateUtils.toLocalDate(reference);

		// The start day of the fiscal year in the year of the reference date
		LocalDate referenceFiscalYearStart = LocalDate.of(referenceLocalDate.getYear(), fiscalYearStartMonth, fiscalYearStartDay);

		LocalDate lastFiscalStartDate;
		if (referenceLocalDate.isBefore(referenceFiscalYearStart)) {
			lastFiscalStartDate = referenceFiscalYearStart.minusYears(1);
		} else {
			lastFiscalStartDate = referenceFiscalYearStart;
		}
		return DateUtils.toDate(lastFiscalStartDate);
	}
}
