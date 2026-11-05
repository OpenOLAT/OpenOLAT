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
package org.olat.modules.topicbroker.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.topicbroker.TBTopicDate;
import org.olat.modules.topicbroker.TBTopicExecutionType;
import org.olat.modules.topicbroker.model.TBTopicDateImpl;
import org.olat.modules.topicbroker.model.TBTransientTopic;
import org.olat.test.OlatTestCase;

/**
 *
 * Initial date: 5 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBUIFactoryTest extends OlatTestCase {
	
	@Test
	public void shouldGetExecutionPeriod_datesSameDay() {
		List<TBTopicDate> dates = List.of(
				createDate(date(2026, 10, 9, 8), date(2026, 10, 9, 10)),
				createDate(date(2026, 10, 9, 14), date(2026, 10, 9, 16)));
		
		String executionPeriod = getExecutionPeriod(dates);
		
		assertThat(executionPeriod).endsWith("Mo., 09.11.2026, 08:00 - 10:00, 14:00 - 16:00");
	}
	
	@Test
	public void shouldGetExecutionPeriod_datesSeveralDays() {
		List<TBTopicDate> dates = List.of(
				createDate(date(2026, 10, 9, 8), date(2026, 10, 9, 10)),
				createDate(date(2026, 10, 16, 8), date(2026, 10, 16, 10)));
		
		String executionPeriod = getExecutionPeriod(dates);
		
		assertThat(executionPeriod).endsWith("2 Termine: Erster Mo., 09.11.2026, letzter Mo., 16.11.2026");
	}
	
	private String getExecutionPeriod(List<TBTopicDate> dates) {
		Translator translator = Util.createPackageTranslator(TBUIFactory.class, Locale.GERMAN);
		return TBUIFactory.getExecutionPeriod(translator, Formatter.getInstance(Locale.GERMAN), createDatesTopic(), dates);
	}
	
	private TBTransientTopic createDatesTopic() {
		TBTransientTopic topic = new TBTransientTopic();
		topic.setExecutionType(TBTopicExecutionType.dates);
		return topic;
	}
	
	private TBTopicDate createDate(Date startDate, Date endDate) {
		TBTopicDateImpl date = new TBTopicDateImpl();
		date.setStartDate(startDate);
		date.setEndDate(endDate);
		return date;
	}
	
	private Date date(int year, int month, int day, int hour) {
		return new GregorianCalendar(year, month, day, hour, 0).getTime();
	}
	
}
