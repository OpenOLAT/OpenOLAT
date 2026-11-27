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
package org.olat.modules.coach.reports;

import java.util.Date;

import org.olat.core.util.DateUtils;

/**
 * The time range options of a report.
 * <p>
 * Boundaries are the same for all options (server time zone):
 * <ul>
 * <li>the lower bound is 00:00 of the first day, inclusive</li>
 * <li>the upper bound is 00:00 of the day after the last day, exclusive</li>
 * <li>relative options end at the reference time (the start of the report)</li>
 * </ul>
 *
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public enum ReportTimeRange {

	all(Group.all),

	custom(Group.specific),
	timePeriod(Group.specific),
	currentFiscalYear(Group.specific),
	lastFiscalYear(Group.specific),
	last2FiscalYears(Group.specific),

	last7Days(Group.relative),
	last4Weeks(Group.relative),
	last6Months(Group.relative),
	last12Months(Group.relative),
	last24Months(Group.relative);

	private final Group group;

	private ReportTimeRange(Group group) {
		this.group = group;
	}

	public Group group() {
		return group;
	}

	/**
	 * @return true if the range needs the "from" and "to" dates chosen by the user
	 *  (custom range) or the dates of a time period
	 */
	public boolean isExplicitDates() {
		return this == custom || this == timePeriod;
	}

	/**
	 * Resolves the time range.
	 *
	 * @param reference The reference time, typically the start of the report
	 * @param from The first day of a custom range or of a time period (only used by
	 * 		{@link #custom} and {@link #timePeriod}), can be null for an open range
	 * @param to The last day (inclusive) of a custom range or of a time period (only used by
	 * 		{@link #custom} and {@link #timePeriod}), can be null for an open range
	 * @param fiscalYearStartDay The day of the start of the fiscal year (1 - 31)
	 * @param fiscalYearStartMonth The month of the start of the fiscal year (1 - 12)
	 * @return The range, never null
	 */
	public ReportDateRange resolve(Date reference, Date from, Date to, int fiscalYearStartDay, int fiscalYearStartMonth) {
		return switch (this) {
			case all -> ReportDateRange.ALL;
			case custom, timePeriod -> new ReportDateRange(startOfDay(from), startOfNextDay(to));
			case currentFiscalYear -> fiscalYears(reference, 0, 1, fiscalYearStartDay, fiscalYearStartMonth);
			case lastFiscalYear -> fiscalYears(reference, -1, 1, fiscalYearStartDay, fiscalYearStartMonth);
			case last2FiscalYears -> fiscalYears(reference, -2, 2, fiscalYearStartDay, fiscalYearStartMonth);
			case last7Days -> new ReportDateRange(startOfDay(DateUtils.addDays(reference, -7)), reference);
			case last4Weeks -> new ReportDateRange(startOfDay(DateUtils.addWeeks(reference, -4)), reference);
			case last6Months -> new ReportDateRange(startOfDay(DateUtils.addMonth(reference, -6)), reference);
			case last12Months -> new ReportDateRange(startOfDay(DateUtils.addMonth(reference, -12)), reference);
			case last24Months -> new ReportDateRange(startOfDay(DateUtils.addMonth(reference, -24)), reference);
		};
	}

	/**
	 * @param reference The reference date
	 * @param offsetYears The first fiscal year, relative to the fiscal year of the reference date
	 * @param numOfYears The number of full fiscal years
	 */
	private static ReportDateRange fiscalYears(Date reference, int offsetYears, int numOfYears,
			int fiscalYearStartDay, int fiscalYearStartMonth) {
		Date currentStart = ReportTimeUnit.fiscalYearStart(reference, fiscalYearStartDay, fiscalYearStartMonth);
		int currentYear = DateUtils.toLocalDate(currentStart).getYear();
		Date start = DateUtils.toDate(ReportTimeUnit.startDay(currentYear + offsetYears, fiscalYearStartDay, fiscalYearStartMonth));
		Date end = DateUtils.toDate(ReportTimeUnit.startDay(currentYear + offsetYears + numOfYears, fiscalYearStartDay, fiscalYearStartMonth));
		return new ReportDateRange(start, end);
	}

	private static Date startOfDay(Date date) {
		return date == null ? null : DateUtils.toDate(DateUtils.toLocalDate(date));
	}

	private static Date startOfNextDay(Date date) {
		return date == null ? null : DateUtils.toDate(DateUtils.toLocalDate(date).plusDays(1));
	}

	/**
	 * The three choices of the dialog.
	 */
	public enum Group {
		all,
		specific,
		relative
	}
}
