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
package org.olat.modules.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 9 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ProjectServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService sut;

	@Test
	public void shouldDeleteAppointmentSeriesWithChangedOccurrence() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjectBCFactory bcFactory = new ProjectBCFactory();
		ProjProject project = sut.createProject(owner, bcFactory, owner);
		ZonedDateTime start = ZonedDateTime.of(2030, 3, 4, 9, 0, 0, 0, ZoneId.systemDefault());
		ProjAppointment series = sut.createAppointment(owner, bcFactory, project, DateUtils.toDate(start), DateUtils.toDate(start.plusHours(1)));
		sut.updateAppointment(owner, bcFactory, series, DateUtils.toDate(start), DateUtils.toDate(start.plusHours(1)), "Weekly", null, null, null,
				false, "FREQ=WEEKLY;COUNT=4");
		ZonedDateTime occurrenceStart = start.plusWeeks(1);
		String recurrenceId = CalendarUtils.formatRecurrenceDate(occurrenceStart, false);
		ProjAppointment occurrence = sut.createAppointmentOcurrence(owner, bcFactory, series.getIdentifier(), recurrenceId,
				DateUtils.toDate(occurrenceStart), DateUtils.toDate(occurrenceStart.plusHours(1)));
		dbInstance.commitAndCloseSession();

		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(project);
		searchParams.setEventIds(List.of(series.getEventId()));
		searchParams.setStatus(List.of(ProjectStatus.active));
		assertThat(sut.getAppointments(searchParams)).containsExactlyInAnyOrder(series, occurrence);

		sut.deleteAppointmentSoftly(owner, bcFactory, series.getIdentifier(), null);
		dbInstance.commitAndCloseSession();

		assertThat(sut.getAppointments(searchParams)).isEmpty();
	}

}
