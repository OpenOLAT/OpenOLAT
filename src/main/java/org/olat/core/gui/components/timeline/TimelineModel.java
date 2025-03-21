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

import java.util.List;
import java.util.Objects;

/**
 * Initial date: Mar 17, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineModel {

	private TimelineModel() {}

	public static final class TimelineYear {
		private final Integer year;
		private final List<TimelineDay> days;

		public TimelineYear(Integer year, List<TimelineDay> days) {
			this.year = year;
			this.days = days;
		}

		public Integer getYear() {
			return year;
		}

		public List<TimelineDay> getDays() {
			return days;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (TimelineYear) obj;
			return Objects.equals(this.year, that.year) &&
					Objects.equals(this.days, that.days);
		}

		@Override
		public int hashCode() {
			return Objects.hash(year, days);
		}

		@Override
		public String toString() {
			return "TimelineYear[" +
					"year=" + year + ", " +
					"days=" + days + ']';
		}
	}

	public static final class TimelineDay {
		private final String monthName;
		private final String dayName;
		private final Integer dayOfMonth;
		private final List<TimelineEntry> events;

		public TimelineDay(String monthName, String dayName, Integer dayOfMonth, List<TimelineEntry> events) {
			this.monthName = monthName;
			this.dayName = dayName;
			this.dayOfMonth = dayOfMonth;
			this.events = events;
		}

		public String getMonthName() {
			return monthName;
		}

		public String getDayName() {
			return dayName;
		}

		public Integer getDayOfMonth() {
			return dayOfMonth;
		}

		public List<TimelineEntry> getEntries() {
			return events;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null || obj.getClass() != this.getClass()) return false;
			var that = (TimelineDay) obj;
			return Objects.equals(this.monthName, that.monthName) &&
					Objects.equals(this.dayName, that.dayName) &&
					Objects.equals(this.dayOfMonth, that.dayOfMonth) &&
					Objects.equals(this.events, that.events);
		}

		@Override
		public int hashCode() {
			return Objects.hash(monthName, dayName, dayOfMonth, events);
		}

		@Override
		public String toString() {
			return "TimelineDay[" +
					"monthName=" + monthName + ", " +
					"dayName=" + dayName + ", " +
					"dayOfMonth=" + dayOfMonth + ", " +
					"events=" + events + ']';
		}
	}
}

