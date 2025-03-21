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
package org.olat.core.gui.components.timeline;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.olat.core.util.Formatter;
import org.olat.properties.LogEntry;
import org.olat.properties.LogEntryTimelineEntry;

/**
 * Initial date: Mar 17, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineBuilder {

	private static final int FIRST_ONLY_LIMIT = 5;

	private TimelineBuilder() {
	}

	public static List<TimelineModel.TimelineYear> buildLogEntriesTimeline(List<LogEntry> logEntries, Locale locale) {
		if (logEntries.isEmpty()) return Collections.emptyList();

		Formatter formatter = Formatter.getInstance(locale);

		List<LogEntry> sortedEntries = logEntries.stream()
				.sorted(Comparator.comparing(LogEntry::timestamp))
				.toList();

		List<TimelineModel.TimelineYear> years = new ArrayList<>();
		Map<Integer, Map<LocalDate, List<LogEntryTimelineEntry>>> yearDayMap = new TreeMap<>();

		for (LogEntry logEntry : sortedEntries) {
			LocalDate localDate = logEntry.timestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			int year = localDate.getYear();

			yearDayMap
					.computeIfAbsent(year, y -> new TreeMap<>())
					.computeIfAbsent(localDate, d -> new ArrayList<>())
					.add(new LogEntryTimelineEntry(logEntry, locale));
		}

		for (var yearEntry : yearDayMap.entrySet()) {
			List<TimelineModel.TimelineDay> days = new ArrayList<>();

			for (var dayEntry : yearEntry.getValue().entrySet()) {
				LocalDate date = dayEntry.getKey();
				List<TimelineEntry> dayEvents = new ArrayList<>(dayEntry.getValue());

				days.add(new TimelineModel.TimelineDay(
						date.getMonth().getDisplayName(TextStyle.SHORT, locale),
						formatter.dayOfWeekShort(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())),
						date.getDayOfMonth(),
						dayEvents
				));
			}

			years.add(new TimelineModel.TimelineYear(yearEntry.getKey(), days));
		}

		return years;
	}
}

