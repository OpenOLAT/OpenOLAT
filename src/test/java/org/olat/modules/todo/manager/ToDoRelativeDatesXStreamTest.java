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
package org.olat.modules.todo.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_AFTER_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_AFTER_END;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_BEFORE_BEGIN;
import static org.olat.modules.curriculum.manager.CurriculumElementToDoProvider.DATE_REF_BEFORE_END;

import org.junit.Test;
import org.olat.modules.todo.ToDoDateUnit;
import org.olat.modules.todo.ToDoRelativeDates;

/**
 * Initial date: 2026-05-08<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class ToDoRelativeDatesXStreamTest {

	@Test
	public void shouldReturnNullForNullOrBlankInput() {
		assertThat(ToDoRelativeDatesXStream.toXml(null)).isNull();
		assertThat(ToDoRelativeDatesXStream.fromXml(null)).isNull();
		assertThat(ToDoRelativeDatesXStream.fromXml("")).isNull();
		assertThat(ToDoRelativeDatesXStream.fromXml("   ")).isNull();
	}

	@Test
	public void shouldRoundtripFullConfig() {
		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(3);
		config.setStartUnit(ToDoDateUnit.DAYS);
		config.setStartRef(DATE_REF_BEFORE_BEGIN);
		config.setDueValue(7);
		config.setDueUnit(ToDoDateUnit.WEEKS);
		config.setDueRef(DATE_REF_AFTER_END);

		String xml = ToDoRelativeDatesXStream.toXml(config);
		ToDoRelativeDates loaded = ToDoRelativeDatesXStream.fromXml(xml);

		assertThat(loaded.getStartValue()).isEqualTo(3);
		assertThat(loaded.getStartUnit()).isEqualTo(ToDoDateUnit.DAYS);
		assertThat(loaded.getStartRef()).isEqualTo(DATE_REF_BEFORE_BEGIN);
		assertThat(loaded.getDueValue()).isEqualTo(7);
		assertThat(loaded.getDueUnit()).isEqualTo(ToDoDateUnit.WEEKS);
		assertThat(loaded.getDueRef()).isEqualTo(DATE_REF_AFTER_END);
	}

	@Test
	public void shouldRoundtripPartialStartOnlyConfig() {
		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(2);
		config.setStartUnit(ToDoDateUnit.MONTHS);
		config.setStartRef(DATE_REF_AFTER_BEGIN);

		String xml = ToDoRelativeDatesXStream.toXml(config);
		ToDoRelativeDates loaded = ToDoRelativeDatesXStream.fromXml(xml);

		assertThat(loaded.getStartValue()).isEqualTo(2);
		assertThat(loaded.getStartUnit()).isEqualTo(ToDoDateUnit.MONTHS);
		assertThat(loaded.getStartRef()).isEqualTo(DATE_REF_AFTER_BEGIN);
		assertThat(loaded.getDueValue()).isNull();
		assertThat(loaded.getDueUnit()).isNull();
		assertThat(loaded.getDueRef()).isNull();
	}

	@Test
	public void shouldRoundtripPartialDueOnlyConfig() {
		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setDueValue(1);
		config.setDueUnit(ToDoDateUnit.YEARS);
		config.setDueRef(DATE_REF_BEFORE_END);

		String xml = ToDoRelativeDatesXStream.toXml(config);
		ToDoRelativeDates loaded = ToDoRelativeDatesXStream.fromXml(xml);

		assertThat(loaded.getStartValue()).isNull();
		assertThat(loaded.getStartUnit()).isNull();
		assertThat(loaded.getStartRef()).isNull();
		assertThat(loaded.getDueValue()).isEqualTo(1);
		assertThat(loaded.getDueUnit()).isEqualTo(ToDoDateUnit.YEARS);
		assertThat(loaded.getDueRef()).isEqualTo(DATE_REF_BEFORE_END);
	}

	@Test
	public void shouldRoundtripSameDay() {
		ToDoRelativeDates config = new ToDoRelativeDates();
		config.setStartValue(0);
		config.setStartUnit(ToDoDateUnit.SAME_DAY);
		config.setStartRef(DATE_REF_AFTER_BEGIN);

		String xml = ToDoRelativeDatesXStream.toXml(config);
		ToDoRelativeDates loaded = ToDoRelativeDatesXStream.fromXml(xml);

		assertThat(loaded.getStartUnit()).isEqualTo(ToDoDateUnit.SAME_DAY);
		assertThat(loaded.getStartValue()).isEqualTo(0);
	}

	@Test
	public void shouldRoundtripAllRefs() {
		String[] refs = { DATE_REF_BEFORE_BEGIN, DATE_REF_AFTER_BEGIN, DATE_REF_BEFORE_END, DATE_REF_AFTER_END };
		for (String ref : refs) {
			ToDoRelativeDates config = new ToDoRelativeDates();
			config.setStartValue(1);
			config.setStartUnit(ToDoDateUnit.DAYS);
			config.setStartRef(ref);

			String xml = ToDoRelativeDatesXStream.toXml(config);
			ToDoRelativeDates loaded = ToDoRelativeDatesXStream.fromXml(xml);

			assertThat(loaded.getStartRef()).isEqualTo(ref);
		}
	}

	@Test
	public void shouldRoundtripAllUnits() {
		for (ToDoDateUnit unit : ToDoDateUnit.values()) {
			ToDoRelativeDates config = new ToDoRelativeDates();
			config.setStartValue(1);
			config.setStartUnit(unit);
			config.setStartRef(DATE_REF_AFTER_BEGIN);

			String xml = ToDoRelativeDatesXStream.toXml(config);
			ToDoRelativeDates loaded = ToDoRelativeDatesXStream.fromXml(xml);

			assertThat(loaded.getStartUnit()).isEqualTo(unit);
		}
	}

	@Test
	public void shouldRejectUntrustedType() {
		String maliciousXml = "<java.lang.Runtime></java.lang.Runtime>";
		assertThatThrownBy(() -> ToDoRelativeDatesXStream.fromXml(maliciousXml))
				.isInstanceOf(Exception.class);
	}

}
