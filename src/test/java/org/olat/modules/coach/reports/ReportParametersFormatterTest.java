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
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 27 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportParametersFormatterTest extends OlatTestCase {

	private static final Long TIME_PERIOD_KEY = Long.valueOf(42l);
	private static final Date REFERENCE = DateUtils.toDate(LocalDateTime.of(2026, 11, 27, 10, 30));

	@Autowired
	private ReportTemplatesRegistry reportTemplatesRegistry;

	private Formatter dateFormatter;
	private ReportParametersFormatter formatter;

	/**
	 * The translators need the Spring context, which is not ready when the fields are initialized.
	 */
	@Before
	public void createFormatters() {
		dateFormatter = Formatter.getInstance(Locale.ENGLISH);
		formatter = new ReportParametersFormatter(Locale.ENGLISH, 1, 1,
				key -> key == TIME_PERIOD_KEY.longValue() ? timePeriod("Autumn semester") : null);
	}

	@Test
	public void titleAllTime() {
		String title = formatter.formatTitle("Bookings", params(ReportTimeRange.all), REFERENCE);
		Assert.assertEquals("Bookings - All time", title);
	}

	@Test
	public void titleWithoutParameters() {
		String title = formatter.formatTitle("Bookings", null, REFERENCE);
		Assert.assertEquals("Bookings - All time", title);
	}

	@Test
	public void titlePreset() {
		String title = formatter.formatTitle("Bookings", params(ReportTimeRange.last4Weeks), REFERENCE);
		Assert.assertEquals("Bookings - Last 4 weeks", title);
	}

	@Test
	public void titleCustomRange() {
		ReportParameters params = params(ReportTimeRange.custom);
		params.setFrom(date(2026, 1, 1));
		params.setTo(date(2026, 3, 31));

		String title = formatter.formatTitle("Bookings", params, REFERENCE);
		Assert.assertEquals("Bookings - " + format(2026, 1, 1) + " to " + format(2026, 3, 31), title);
	}

	@Test
	public void titleTimePeriod() {
		ReportParameters params = params(ReportTimeRange.timePeriod);
		params.setTimePeriodKey(TIME_PERIOD_KEY);

		String title = formatter.formatTitle("Bookings", params, REFERENCE);
		Assert.assertEquals("Bookings - Autumn semester", title);
	}

	@Test
	public void titleChangedSince() {
		ReportParameters params = params(ReportTimeRange.all);
		params.setChangedSince(date(2026, 11, 1));

		String title = formatter.formatTitle("Bookings", params, REFERENCE);
		Assert.assertEquals("Bookings - All time, changed since " + format(2026, 11, 1), title);
	}

	@Test
	public void summaryFiscalYear() {
		ReportConfiguration template = reportTemplatesRegistry.getTemplate("courseplanner.bookingorders");
		String summary = formatter.formatSummary(template, params(ReportTimeRange.currentFiscalYear), REFERENCE);
		Assert.assertEquals("Booking date: Current year (" + format(2026, 1, 1) + " to " + format(2026, 12, 31) + ")", summary);
	}

	@Test
	public void summaryRelativeEndsToday() {
		ReportConfiguration template = reportTemplatesRegistry.getTemplate("coaching.absences");
		String summary = formatter.formatSummary(template, params(ReportTimeRange.last4Weeks), REFERENCE);
		Assert.assertEquals("Date of absence: Last 4 weeks (" + format(2026, 10, 30) + " to " + format(2026, 11, 27) + ")", summary);
	}

	@Test
	public void summaryTimePeriodWithChangedSince() {
		ReportConfiguration template = reportTemplatesRegistry.getTemplate("courseplanner.bookingorders");
		ReportParameters params = params(ReportTimeRange.timePeriod);
		params.setTimePeriodKey(TIME_PERIOD_KEY);
		params.setChangedSince(date(2026, 11, 1));

		String summary = formatter.formatSummary(template, params, REFERENCE);
		Assert.assertEquals("Booking date: Autumn semester (" + format(2026, 9, 1) + " to " + format(2027, 1, 31) + ")\n"
				+ "Only changes since: " + format(2026, 11, 1), summary);
	}

	@Test
	public void summaryAllTime() {
		ReportConfiguration template = reportTemplatesRegistry.getTemplate("coaching.certificates");
		String summary = formatter.formatSummary(template, params(ReportTimeRange.all), REFERENCE);
		Assert.assertEquals("Issued date: All time", summary);
	}

	@Test
	public void rangeWithDash() {
		ReportDateRange range = formatter.resolve(params(ReportTimeRange.lastFiscalYear), REFERENCE);
		Assert.assertEquals(format(2025, 1, 1) + " – " + format(2025, 12, 31), formatter.formatRange(range, true));
		Assert.assertEquals("All time", formatter.formatRange(ReportDateRange.ALL, true));
	}

	@Test(expected = ReportGenerationException.class)
	public void resolveDeletedTimePeriod() {
		ReportParameters params = params(ReportTimeRange.timePeriod);
		params.setTimePeriodKey(Long.valueOf(43l));
		formatter.resolve(params, REFERENCE);
	}

	/**
	 * The task resolves the range from the time the report was started, like
	 * the title and the description, not from the time it runs.
	 */
	@Test
	public void templateResolvesFromReferenceDate() {
		ReportParameters params = params(ReportTimeRange.last4Weeks);
		params.setReferenceDate(REFERENCE);
		ReportDateRange range = new CertificatesReportConfiguration().resolveRange(params);
		Assert.assertEquals(date(2026, 10, 30), range.from());
		Assert.assertEquals(REFERENCE, range.toExclusive());
	}

	private static ReportParameters params(ReportTimeRange timeRange) {
		ReportParameters params = new ReportParameters();
		params.setTimeRange(timeRange);
		return params;
	}

	private static RepositoryEntryLifecycle timePeriod(String label) {
		RepositoryEntryLifecycle timePeriod = new RepositoryEntryLifecycle();
		timePeriod.setLabel(label);
		timePeriod.setValidFrom(date(2026, 9, 1));
		timePeriod.setValidTo(date(2027, 1, 31));
		return timePeriod;
	}

	private static Date date(int year, int month, int day) {
		return DateUtils.toDate(LocalDate.of(year, month, day));
	}

	private String format(int year, int month, int day) {
		return dateFormatter.formatDate(date(year, month, day));
	}
}
