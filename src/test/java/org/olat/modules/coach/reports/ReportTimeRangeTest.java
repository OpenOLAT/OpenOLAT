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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.DateUtils;

/**
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportTimeRangeTest {

	// Fiscal year starts on 1 July
	private static final int FY_DAY = 1;
	private static final int FY_MONTH = 7;

	private final Date reference = DateUtils.toDate(LocalDateTime.of(2026, 11, 25, 14, 30));

	@Test
	public void all() {
		ReportDateRange range = ReportTimeRange.all.resolve(reference, null, null, FY_DAY, FY_MONTH);
		Assert.assertNull(range.from());
		Assert.assertNull(range.toExclusive());
	}

	@Test
	public void customToIsInclusiveUntilNextDay() {
		Date from = DateUtils.toDate(LocalDateTime.of(2026, 1, 1, 15, 12));
		Date to = DateUtils.toDate(LocalDateTime.of(2026, 6, 30, 8, 0));
		ReportDateRange range = ReportTimeRange.custom.resolve(reference, from, to, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 1, 1), range.from());
		assertDate(LocalDate.of(2026, 7, 1), range.toExclusive());
	}

	@Test
	public void customSameDay() {
		Date day = DateUtils.toDate(LocalDate.of(2026, 3, 10));
		ReportDateRange range = ReportTimeRange.custom.resolve(reference, day, day, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 3, 10), range.from());
		assertDate(LocalDate.of(2026, 3, 11), range.toExclusive());
	}

	@Test
	public void customOpenEnd() {
		Date from = DateUtils.toDate(LocalDate.of(2026, 3, 10));
		ReportDateRange range = ReportTimeRange.custom.resolve(reference, from, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 3, 10), range.from());
		Assert.assertNull(range.toExclusive());
	}

	@Test
	public void timePeriodOpenStart() {
		Date to = DateUtils.toDate(LocalDate.of(2026, 3, 10));
		ReportDateRange range = ReportTimeRange.timePeriod.resolve(reference, null, to, FY_DAY, FY_MONTH);
		Assert.assertNull(range.from());
		assertDate(LocalDate.of(2026, 3, 11), range.toExclusive());
	}

	@Test
	public void currentFiscalYear() {
		ReportDateRange range = ReportTimeRange.currentFiscalYear.resolve(reference, null, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 7, 1), range.from());
		assertDate(LocalDate.of(2027, 7, 1), range.toExclusive());
	}

	@Test
	public void currentFiscalYearOnFirstDay() {
		Date firstDay = DateUtils.toDate(LocalDateTime.of(2026, 7, 1, 0, 0));
		ReportDateRange range = ReportTimeRange.currentFiscalYear.resolve(firstDay, null, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 7, 1), range.from());
		assertDate(LocalDate.of(2027, 7, 1), range.toExclusive());
	}

	@Test
	public void lastFiscalYear() {
		ReportDateRange range = ReportTimeRange.lastFiscalYear.resolve(reference, null, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2025, 7, 1), range.from());
		assertDate(LocalDate.of(2026, 7, 1), range.toExclusive());
	}

	@Test
	public void last2FiscalYearsAreTwoFullYears() {
		ReportDateRange range = ReportTimeRange.last2FiscalYears.resolve(reference, null, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2024, 7, 1), range.from());
		assertDate(LocalDate.of(2026, 7, 1), range.toExclusive());
	}

	@Test
	public void last2FiscalYearsCalendarYear() {
		ReportDateRange range = ReportTimeRange.last2FiscalYears.resolve(reference, null, null, 1, 1);
		assertDate(LocalDate.of(2024, 1, 1), range.from());
		assertDate(LocalDate.of(2026, 1, 1), range.toExclusive());
	}

	@Test
	public void relativeRanges() {
		assertRelative(ReportTimeRange.last7Days, LocalDate.of(2026, 11, 18));
		assertRelative(ReportTimeRange.last4Weeks, LocalDate.of(2026, 10, 28));
		assertRelative(ReportTimeRange.last6Months, LocalDate.of(2026, 5, 25));
		assertRelative(ReportTimeRange.last12Months, LocalDate.of(2025, 11, 25));
		assertRelative(ReportTimeRange.last24Months, LocalDate.of(2024, 11, 25));
	}

	/**
	 * The range starts at midnight even if it crosses the change to summer time (29 March 2026).
	 */
	@Test
	public void relativeRangeOverSummerTimeChange() {
		Date afterChange = DateUtils.toDate(LocalDateTime.of(2026, 4, 5, 10, 0));
		ReportDateRange range = ReportTimeRange.last4Weeks.resolve(afterChange, null, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 3, 8), range.from());
		Assert.assertEquals(afterChange, range.toExclusive());
	}

	/**
	 * The range starts at midnight even if it crosses the change to winter time (25 October 2026).
	 */
	@Test
	public void relativeRangeOverWinterTimeChange() {
		Date afterChange = DateUtils.toDate(LocalDateTime.of(2026, 10, 30, 10, 0));
		ReportDateRange range = ReportTimeRange.last7Days.resolve(afterChange, null, null, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 10, 23), range.from());
		Assert.assertEquals(afterChange, range.toExclusive());
	}

	/**
	 * The last day of a custom range is the day of the change to winter time, which has 25 hours.
	 */
	@Test
	public void customRangeEndsOnWinterTimeChange() {
		Date from = DateUtils.toDate(LocalDate.of(2026, 10, 20));
		Date to = DateUtils.toDate(LocalDateTime.of(2026, 10, 25, 15, 0));
		ReportDateRange range = ReportTimeRange.custom.resolve(reference, from, to, FY_DAY, FY_MONTH);
		assertDate(LocalDate.of(2026, 10, 20), range.from());
		assertDate(LocalDate.of(2026, 10, 26), range.toExclusive());
	}

	/**
	 * A fiscal year which starts on 29 February starts on 28 February in the
	 * years which are not leap years.
	 */
	@Test
	public void fiscalYearStartsOn29February() {
		Date reference2027 = DateUtils.toDate(LocalDate.of(2027, 6, 1));
		ReportDateRange current = ReportTimeRange.currentFiscalYear.resolve(reference2027, null, null, 29, 2);
		assertDate(LocalDate.of(2027, 2, 28), current.from());
		assertDate(LocalDate.of(2028, 2, 29), current.toExclusive());

		ReportDateRange last = ReportTimeRange.lastFiscalYear.resolve(reference2027, null, null, 29, 2);
		assertDate(LocalDate.of(2026, 2, 28), last.from());
		assertDate(LocalDate.of(2027, 2, 28), last.toExclusive());

		Date reference2028 = DateUtils.toDate(LocalDate.of(2028, 2, 29));
		ReportDateRange leapYear = ReportTimeRange.currentFiscalYear.resolve(reference2028, null, null, 29, 2);
		assertDate(LocalDate.of(2028, 2, 29), leapYear.from());
		assertDate(LocalDate.of(2029, 2, 28), leapYear.toExclusive());

		// The day before the start in a leap year belongs to the former fiscal year
		Date dayBefore = DateUtils.toDate(LocalDate.of(2028, 2, 28));
		ReportDateRange former = ReportTimeRange.currentFiscalYear.resolve(dayBefore, null, null, 29, 2);
		assertDate(LocalDate.of(2027, 2, 28), former.from());
		assertDate(LocalDate.of(2028, 2, 29), former.toExclusive());
	}

	@Test
	public void groups() {
		Assert.assertEquals(ReportTimeRange.Group.all, ReportTimeRange.all.group());
		Assert.assertEquals(ReportTimeRange.Group.specific, ReportTimeRange.timePeriod.group());
		Assert.assertEquals(ReportTimeRange.Group.specific, ReportTimeRange.last2FiscalYears.group());
		Assert.assertEquals(ReportTimeRange.Group.relative, ReportTimeRange.last7Days.group());
	}

	private void assertRelative(ReportTimeRange timeRange, LocalDate expectedFrom) {
		ReportDateRange range = timeRange.resolve(reference, null, null, FY_DAY, FY_MONTH);
		assertDate(expectedFrom, range.from());
		Assert.assertEquals(timeRange.name(), reference, range.toExclusive());
	}

	private static void assertDate(LocalDate expected, Date actual) {
		Assert.assertNotNull(actual);
		Assert.assertEquals(DateUtils.toDate(expected), actual);
	}
}
