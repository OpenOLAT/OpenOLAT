/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.course.run.leave;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.course.run.leave.LeaveCourseParticipation.Origin;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 12.03.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class LeaveCourseEvaluatorTest {

	private final LeaveCourseEvaluator sut = new LeaveCourseEvaluator();

	private static final Date PAST = new Date(1000L);
	private static final Date FUTURE = new Date(Long.MAX_VALUE);
	private static final Date NOW = new Date(1_000_000L);

	private static final LocalDate SOME_DAY = LocalDate.of(2026, 6, 15);
	private static final Date SOME_DAY_MORNING = toDate(SOME_DAY, 8);
	private static final Date SOME_DAY_EVENING = toDate(SOME_DAY, 20);
	private static final Date NEXT_DAY_MORNING = toDate(SOME_DAY.plusDays(1), 8);

	private static Date toDate(LocalDate date, int hour) {
		return Date.from(date.atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant());
	}

	@Test
	public void evaluate_embedded_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.embedded, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_curricular_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.curricular, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_template_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.template, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_standalone_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_never_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.never,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_guest_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				true, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_noParticipation_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_assessmentMode_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, true, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_onlyCplOrigin_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(cpl()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_enrollmentGroupDelistingNotPermitted_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(enrollmentGroup(false, 1)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_groupMultipleCourses_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(group(false, 2)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_groupSingleCourse_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(group(false, 1)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_enrollmentGroupDelistingPermitted_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(enrollmentGroup(true, 1)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_directParticipant_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_cplAndDirect_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(cpl(), direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_cplAndBlockedGroup_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(cpl(), enrollmentGroup(false, 1)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_blockedGroupAndDirect_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(enrollmentGroup(false, 1), direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_enrollmentNoDelistAndEnrollmentWithDelist_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(enrollmentGroup(false, 1), enrollmentGroup(true, 1)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_allRolesBlocked_hidden() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(cpl(), enrollmentGroup(false, 1), group(false, 2)), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.HIDDEN);
	}

	@Test
	public void evaluate_atAnyTime_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.atAnyTime,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, FUTURE, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_afterEndDate_statusClosed_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.closed, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_afterEndDate_endDatePassed_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, PAST, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_afterEndDate_endDateToday_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, NOW, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_afterEndDate_endDateFuture_disabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, FUTURE, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.DISABLED);
	}

	@Test
	public void evaluate_afterEndDate_noEndDate_notClosed_disabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.DISABLED);
	}

	@Test
	public void evaluate_afterEndDate_noEndDate_closed_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.closed, null, NOW);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_afterEndDate_endDateSameDayLaterTime_enabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, SOME_DAY_EVENING, SOME_DAY_MORNING);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.ENABLED);
	}

	@Test
	public void evaluate_afterEndDate_endDateNextDayEarlierTime_disabled() {
		LeaveCourseContext ctx = ctx(RepositoryEntryRuntimeType.standalone, RepositoryEntryAllowToLeaveOptions.afterEndDate,
				false, false, List.of(direct()), RepositoryEntryStatusEnum.published, NEXT_DAY_MORNING, SOME_DAY_EVENING);
		assertThat(sut.evaluate(ctx)).isEqualTo(LeaveCourseStatus.DISABLED);
	}

	private LeaveCourseContext ctx(RepositoryEntryRuntimeType runtimeType,
			RepositoryEntryAllowToLeaveOptions allowToLeave, boolean guest, boolean assessmentMode,
			List<LeaveCourseParticipation> participations, RepositoryEntryStatusEnum entryStatus,
			Date lifecycleEndDate, Date now) {
		return new LeaveCourseContextImpl(runtimeType, allowToLeave, guest, assessmentMode, participations,
				entryStatus, lifecycleEndDate, now);
	}

	private static final class LeaveCourseContextImpl implements LeaveCourseContext {

		private final RepositoryEntryRuntimeType runtimeType;
		private final RepositoryEntryAllowToLeaveOptions allowToLeave;
		private final boolean guest;
		private final boolean assessmentMode;
		private final List<LeaveCourseParticipation> participations;
		private final RepositoryEntryStatusEnum entryStatus;
		private final Date lifecycleEndDate;
		private final Date now;

		private LeaveCourseContextImpl(RepositoryEntryRuntimeType runtimeType,
				RepositoryEntryAllowToLeaveOptions allowToLeave, boolean guest, boolean assessmentMode,
				List<LeaveCourseParticipation> participations, RepositoryEntryStatusEnum entryStatus,
				Date lifecycleEndDate, Date now) {
			this.runtimeType = runtimeType;
			this.allowToLeave = allowToLeave;
			this.guest = guest;
			this.assessmentMode = assessmentMode;
			this.participations = participations;
			this.entryStatus = entryStatus;
			this.lifecycleEndDate = lifecycleEndDate;
			this.now = now;
		}

		@Override
		public RepositoryEntryRuntimeType getRuntimeType() {
			return runtimeType;
		}

		@Override
		public RepositoryEntryAllowToLeaveOptions getAllowToLeave() {
			return allowToLeave;
		}

		@Override
		public boolean isGuest() {
			return guest;
		}

		@Override
		public boolean isAssessmentMode() {
			return assessmentMode;
		}

		@Override
		public List<LeaveCourseParticipation> getParticipations() {
			return participations;
		}

		@Override
		public RepositoryEntryStatusEnum getEntryStatus() {
			return entryStatus;
		}

		@Override
		public Date getLifecycleEndDate() {
			return lifecycleEndDate;
		}

		@Override
		public Date getNow() {
			return now;
		}
	}

	private LeaveCourseParticipation direct() {
		return new LeaveCourseParticipation(Origin.DIRECT, false, false, 1);
	}

	private LeaveCourseParticipation cpl() {
		return new LeaveCourseParticipation(Origin.CPL, false, false, 1);
	}

	private LeaveCourseParticipation enrollmentGroup(boolean delistingPermitted, int linkedCourseCount) {
		return new LeaveCourseParticipation(Origin.GROUP, true, delistingPermitted, linkedCourseCount);
	}

	private LeaveCourseParticipation group(boolean enrollmentGroup, int linkedCourseCount) {
		return new LeaveCourseParticipation(Origin.GROUP, enrollmentGroup, false, linkedCourseCount);
	}
}
