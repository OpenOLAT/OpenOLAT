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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_AFTER_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_AFTER_END;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_BEFORE_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_BEFORE_END;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_SAME_DAY_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_SAME_DAY_END;

import java.util.Date;

import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.modules.todo.ToDoDateUnit;

/**
 * Initial date: 8 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumElementToDoProviderRelativeDateTest {

	private static final long DELTA_MS = 2000L;

	private final Date begin = DateUtils.addDays(new Date(), 10);
	private final Date end = DateUtils.addDays(begin, 30);

	@Test
	public void shouldReturnNull_whenRefIsNull() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(null, ToDoDateUnit.DAYS, 2, begin, end)).isNull();
	}

	@Test
	public void shouldReturnNull_whenUnitIsNull() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, null, 2, begin, end)).isNull();
	}

	@Test
	public void shouldReturnNull_whenRefIsUnknown() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate("UNKNOWN", ToDoDateUnit.DAYS, 2, begin, end)).isNull();
	}

	@Test
	public void shouldReturnNull_whenBeginRefAndBeginDateIsNull() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.DAYS, 2, null, end)).isNull();
	}

	@Test
	public void shouldReturnNull_whenEndRefAndEndDateIsNull() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_END, ToDoDateUnit.DAYS, 2, begin, null)).isNull();
	}

	@Test
	public void shouldReturnNull_whenValueIsNullAndUnitIsNotSameDay() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.DAYS, null, begin, end)).isNull();
	}

	@Test
	public void shouldReturnBeginDate_forSameDayBegin() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_SAME_DAY_BEGIN, ToDoDateUnit.SAME_DAY, null, begin, end)).isEqualTo(begin);
	}

	@Test
	public void shouldReturnEndDate_forSameDayEnd() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_SAME_DAY_END, ToDoDateUnit.SAME_DAY, null, begin, end)).isEqualTo(end);
	}

	@Test
	public void shouldReturnRefDate_whenUnitIsSameDay_regardlessOfRef() {
		assertThat(CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_BEFORE_BEGIN, ToDoDateUnit.SAME_DAY, null, begin, end)).isEqualTo(begin);
	}

	@Test
	public void shouldAddDays_forAfterBegin() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.DAYS, 3, begin, end);
		assertThat(result).isCloseTo(DateUtils.addDays(begin, 3), DELTA_MS);
	}

	@Test
	public void shouldSubtractDays_forBeforeBegin() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_BEFORE_BEGIN, ToDoDateUnit.DAYS, 3, begin, end);
		assertThat(result).isCloseTo(DateUtils.addDays(begin, -3), DELTA_MS);
	}

	@Test
	public void shouldAddDays_forAfterEnd() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_END, ToDoDateUnit.DAYS, 3, begin, end);
		assertThat(result).isCloseTo(DateUtils.addDays(end, 3), DELTA_MS);
	}

	@Test
	public void shouldSubtractDays_forBeforeEnd() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_BEFORE_END, ToDoDateUnit.DAYS, 3, begin, end);
		assertThat(result).isCloseTo(DateUtils.addDays(end, -3), DELTA_MS);
	}

	@Test
	public void shouldUseWeeks() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.WEEKS, 2, begin, end);
		assertThat(result).isCloseTo(DateUtils.addWeeks(begin, 2), DELTA_MS);
	}

	@Test
	public void shouldUseMonths() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.MONTHS, 1, begin, end);
		assertThat(result).isCloseTo(DateUtils.addMonth(begin, 1), DELTA_MS);
	}

	@Test
	public void shouldUseYears() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.YEARS, 1, begin, end);
		assertThat(result).isCloseTo(DateUtils.addYears(begin, 1), DELTA_MS);
	}

	@Test
	public void shouldHandleZeroValue() {
		Date result = CurriculumElementToDoProvider.computeRelativeDate(DATE_REF_AFTER_BEGIN, ToDoDateUnit.DAYS, 0, begin, end);
		assertThat(result).isCloseTo(begin, DELTA_MS);
	}

}
