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
package org.olat.modules.todo.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;

enum ToDoDueFilter {
	overdue {
		@Override
		public DateRange getDateRange(Date now) {
			return new DateRange(
					DateUtils.addYears(now, -100),
					DateUtils.setTime(now, 0, 0, 0));
		}
	},
	today {
		@Override
		public DateRange getDateRange(Date now) {
			return new DateRange(
					DateUtils.setTime(now, 0, 0, 0),
					DateUtils.setTime(DateUtils.addDays(now, 1), 0, 0, 0));
		}
	},
	thisWeek {
		@Override
		public DateRange getDateRange(Date now) {
			Calendar lastMonday = new GregorianCalendar();
			lastMonday.setTime(now);
			lastMonday.add(Calendar.DAY_OF_WEEK, lastMonday.getFirstDayOfWeek() - lastMonday.get(Calendar.DAY_OF_WEEK));
			
			return new DateRange(
					DateUtils.setTime(lastMonday.getTime(), 0, 0, 0),
					DateUtils.setTime(DateUtils.addDays(lastMonday.getTime(), 8), 0, 0, 0));
		}
	},
	nextWeek {
		@Override
		public DateRange getDateRange(Date now) {
			Calendar lastMonday = new GregorianCalendar();
			lastMonday.setTime(now);
			lastMonday.add(Calendar.DAY_OF_WEEK, lastMonday.getFirstDayOfWeek() - lastMonday.get(Calendar.DAY_OF_WEEK));
			
			return new DateRange(
					DateUtils.setTime(DateUtils.addDays(lastMonday.getTime(), 7), 0, 0, 0),
					DateUtils.setTime(DateUtils.addDays(lastMonday.getTime(), 14), 0, 0, 0));
		}
	},
	next2Weeks {
		@Override
		public DateRange getDateRange(Date now) {
			Calendar lastMonday = new GregorianCalendar();
			lastMonday.setTime(now);
			lastMonday.add(Calendar.DAY_OF_WEEK, lastMonday.getFirstDayOfWeek() - lastMonday.get(Calendar.DAY_OF_WEEK));
			
			return new DateRange(
					DateUtils.setTime(DateUtils.addDays(lastMonday.getTime(), 7), 0, 0, 0),
					DateUtils.setTime(DateUtils.addDays(lastMonday.getTime(), 21), 0, 0, 0));
		}
	},
	future {
		@Override
		public DateRange getDateRange(Date now) {
			return new DateRange(
					DateUtils.setTime(DateUtils.addDays(now, 1), 0, 0, 0),
					DateUtils.addYears(now, 100));
		}
	},
	noDueDate {
		@Override
		public DateRange getDateRange(Date now) {
			return null;
		}
	};

	public abstract DateRange getDateRange(Date now);

}