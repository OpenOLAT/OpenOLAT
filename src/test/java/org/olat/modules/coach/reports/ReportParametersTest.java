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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.DateUtils;

/**
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportParametersTest {

	private static final List<ReportParameterDefinition> BOOKINGS = List.of(
			ReportParameterDefinition.timeRange(ReportTimeRange.all),
			ReportParameterDefinition.changedSince());
	private static final List<ReportParameterDefinition> ABSENCES = List.of(
			ReportParameterDefinition.timeRange(ReportTimeRange.last4Weeks));

	private final Date now = DateUtils.toDate(LocalDateTime.of(2026, 11, 25, 14, 30));

	@Test
	public void defaults() {
		ReportParameters params = ReportParameters.defaults(ABSENCES);
		Assert.assertEquals(ReportTimeRange.last4Weeks, params.getTimeRange());
		Assert.assertNull(params.getChangedSince());
		Assert.assertTrue(params.validate(ABSENCES, now).isEmpty());
	}

	@Test
	public void timeRangeMissing() {
		ReportParameters params = new ReportParameters();
		Assert.assertEquals(List.of(ReportParameterName.timeRange), params.validate(ABSENCES, now));
	}

	@Test
	public void customRange() {
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.custom);
		params.setFrom(date(2026, 1, 1));
		params.setTo(date(2026, 1, 1));
		Assert.assertTrue(params.validate(ABSENCES, now).isEmpty());
	}

	@Test
	public void customRangeToBeforeFrom() {
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.custom);
		params.setFrom(date(2026, 2, 1));
		params.setTo(date(2026, 1, 31));
		Assert.assertEquals(List.of(ReportParameterName.timeRange), params.validate(ABSENCES, now));
	}

	@Test
	public void customRangeMissingDate() {
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.custom);
		params.setFrom(date(2026, 2, 1));
		Assert.assertEquals(List.of(ReportParameterName.timeRange), params.validate(ABSENCES, now));
	}

	@Test
	public void timePeriodMissingKey() {
		ReportParameters params = new ReportParameters();
		params.setTimeRange(ReportTimeRange.timePeriod);
		Assert.assertEquals(List.of(ReportParameterName.timeRange), params.validate(ABSENCES, now));

		params.setTimePeriodKey(Long.valueOf(42));
		Assert.assertTrue(params.validate(ABSENCES, now).isEmpty());
	}

	@Test
	public void changedSinceToday() {
		ReportParameters params = ReportParameters.defaults(BOOKINGS);
		params.setChangedSince(date(2026, 11, 25));
		Assert.assertTrue(params.validate(BOOKINGS, now).isEmpty());
	}

	@Test
	public void changedSinceInFuture() {
		ReportParameters params = ReportParameters.defaults(BOOKINGS);
		params.setChangedSince(date(2026, 11, 26));
		Assert.assertEquals(List.of(ReportParameterName.changedSince), params.validate(BOOKINGS, now));
	}

	@Test
	public void changedSinceNotSupported() {
		ReportParameters params = ReportParameters.defaults(ABSENCES);
		params.setChangedSince(date(2026, 11, 1));
		Assert.assertEquals(List.of(ReportParameterName.changedSince), params.validate(ABSENCES, now));
	}

	private static Date date(int year, int month, int day) {
		return DateUtils.toDate(LocalDate.of(year, month, day));
	}
}
