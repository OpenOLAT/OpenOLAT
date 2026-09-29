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
package org.olat.repository.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 29 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoyUIFactoryTest extends OlatTestCase {
	
	@Test
	public void testFormatExecutionPeriodLong() {
		Translator de = Util.createPackageTranslator(RepositoryService.class, Locale.GERMAN);
		Translator en = Util.createPackageTranslator(RepositoryService.class, Locale.ENGLISH);
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, date(2026, 9, 21), date(2026, 9, 24), true)).isEqualTo("Mo, 21. \u2013 Do, 24. September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, date(2026, 9, 28), date(2026, 10, 1), true)).isEqualTo("Mo, 28. September \u2013 Do, 1. Oktober 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, date(2026, 12, 28), date(2027, 1, 8), true)).isEqualTo("Mo, 28. Dezember 2026 \u2013 Fr, 8. Januar 2027");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, date(2026, 9, 21), date(2026, 9, 21), true)).isEqualTo("Mo, 21. September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, date(2026, 9, 21), null, true)).isEqualTo("Ab Mo, 21. September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, null, date(2026, 9, 24), true)).isEqualTo("Bis Do, 24. September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(de, null, null, true)).isNull();
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(en, date(2026, 9, 21), date(2026, 9, 24), true)).isEqualTo("Mon 21 \u2013 Thu 24 September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(en, date(2026, 12, 28), date(2027, 1, 8), true)).isEqualTo("Mon 28 December 2026 \u2013 Fri 8 January 2027");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(en, date(2026, 9, 21), date(2026, 9, 21), true)).isEqualTo("Mon 21 September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(en, date(2026, 9, 21), null, true)).isEqualTo("From Mon 21 September 2026");
		Assertions.assertThat(RepositoyUIFactory.formatExecutionPeriod(en, null, date(2026, 9, 24), true)).isEqualTo("Until Thu 24 September 2026");
	}
	
	private Date date(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day, 12, 0, 0);
		return cal.getTime();
	}
	
}
