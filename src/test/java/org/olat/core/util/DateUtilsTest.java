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
package org.olat.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.core.util.DateUtils.toDate;

import java.time.LocalDate;
import java.util.Date;

import org.junit.Test;

/**
 * 
 * Initial date: 20 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DateUtilsTest {
	
	@Test
	public void shouldGetLaterIfFirstIsLater() {
		Date date1 = toDate(LocalDate.of(2011, 10, 12));
		Date date2 = toDate(LocalDate.of(2011, 9, 12));
		assertGetLater(date1, date2, date1);
	}
	
	@Test
	public void shouldGetLaterIfSecondIsLater() {
		Date date1 = toDate(LocalDate.of(2011, 9, 12));
		Date date2 = toDate(LocalDate.of(2011, 10, 12));
		assertGetLater(date1, date2, date2);
	}
	
	@Test
	public void shouldGetLaterIfFirstIsNull() {
		Date date1 = null;
		Date date2 = toDate(LocalDate.of(2011, 9, 12));
		assertGetLater(date1, date2, date2);
	}
	
	@Test
	public void shouldGetLaterIfSecondIsNull() {
		Date date1 = toDate(LocalDate.of(2011, 9, 12));
		Date date2 = null;
		assertGetLater(date1, date2, date1);
	}
	
	private void assertGetLater(Date date1, Date date2, Date expected) {
		Date later = DateUtils.getLater(date1, date2);
		
		assertThat(later).isEqualTo(expected);
	}


}
