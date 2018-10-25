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
package org.olat.modules.quality.generator.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.olat.modules.quality.generator.ProviderHelper;

/**
 * 
 * Initial date: 24.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProviderHelperTest {
	
	@Test
	public void shouldConvertToIntOrZero() {
		assertThat(ProviderHelper.toIntOrZero("22")).isEqualTo(22);
		assertThat(ProviderHelper.toIntOrZero("")).isEqualTo(0);
		assertThat(ProviderHelper.toIntOrZero(null)).isEqualTo(0);
		assertThat(ProviderHelper.toIntOrZero("ABC")).isEqualTo(0);
	}
	
	@Test
	public void shouldConvertToLongOrZero() {
		assertThat(ProviderHelper.toLongOrZero("22")).isEqualTo(22);
		assertThat(ProviderHelper.toLongOrZero("")).isEqualTo(0);
		assertThat(ProviderHelper.toLongOrZero(null)).isEqualTo(0);
		assertThat(ProviderHelper.toLongOrZero("ABC")).isEqualTo(0);
	}
	
	@Test
	public void shouldConcatAndSplitDaysOfWeek() {
		List<DayOfWeek> daysOfWeek = Arrays.asList(DayOfWeek.FRIDAY, DayOfWeek.THURSDAY);
		
		String config = ProviderHelper.concatDaysOfWeek(daysOfWeek);
		List<DayOfWeek> concatedAndSplited = ProviderHelper.splitDaysOfWeek(config);

		assertThat(concatedAndSplited).isEqualTo(daysOfWeek);
	}
	
	@Test
	public void shouldGenerateDaysInRange() {
		LocalDate start = LocalDate.of(2018, Month.AUGUST, 1);
		LocalDate day2 = LocalDate.of(2018, Month.AUGUST, 2);
		LocalDate day3 = LocalDate.of(2018, Month.AUGUST, 3);
		LocalDate end = LocalDate.of(2018, Month.AUGUST, 4);
		
		List<LocalDate> inRange = ProviderHelper.generateDaysInRange(start, end);

		assertThat(inRange).containsExactly(start, day2, day3, end);
	}

}
