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
package org.olat.modules.quality.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * 
 * Initial date: 15 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TemporalKeyComparatorTest {

	@Test
	public void shouldParseWithPart() {
		int year = 2010;
		int yearPart = 3;
		String unparsed = Integer.toString(year) + TemporalKey.DELIMITER + Integer.toString(yearPart);
		
		TemporalKey temporalKey = TemporalKey.parse(unparsed);
		
		assertThat(temporalKey.getYear()).isEqualTo(year);
		assertThat(temporalKey.getYearPart()).isEqualTo(yearPart);
	}
	
	@Test
	public void shouldParseWithoutPart() {
		int year = 2010;
		String unparsed = Integer.toString(year);
		
		TemporalKey temporalKey = TemporalKey.parse(unparsed);
		
		assertThat(temporalKey.getYear()).isEqualTo(year);
		assertThat(temporalKey.getYearPart()).isEqualTo(TemporalKey.NO_VALUE);
	}

	@Test
	public void shouldOrderAsc() {
		TemporalKey s2000 = TemporalKey.of(2000);
		TemporalKey s2000_1 = TemporalKey.of(2000, 1);
		TemporalKey s2000_2 = TemporalKey.of(2000, 2);
		TemporalKey s2001 = TemporalKey.of(2001);
		TemporalKey s2001_1 = TemporalKey.of(2001, 1);
		TemporalKey s2001_2 = TemporalKey.of(2001, 2);
		List<TemporalKey> keys = Arrays.asList(s2000, s2000_1, s2000_2, s2001, s2001_1, s2001_2);
		
		Collections.shuffle(keys);
		Collections.sort(keys);
		
		assertThat(keys).containsExactly(s2000, s2000_1, s2000_2, s2001, s2001_1, s2001_2);
	}

}
