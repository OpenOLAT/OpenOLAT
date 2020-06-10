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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * 
 * Initial date: 20 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DateUtils {
	
	public static Date toDate(LocalDate localDate) {
		if (localDate == null) return null;
		
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}
 
	public static Date toDate(LocalDateTime localDateTime) {
		if (localDateTime == null) return null;
		
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
 
	public static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}
 
	public static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) return null;
		
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	public static boolean isSameTime(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return (cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
				cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
				cal1.get(Calendar.HOUR) == cal2.get(Calendar.HOUR));
	}
	
	public static Date setTime(Date date, int hour, int minutes, int seconds) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);
		return calendar.getTime();
	}
	
	/**
	 * Keeps the day of the date but copies the date from the from date.
	 *
	 * @param to
	 * @param from
	 * @return
	 */
	public static Date copyTime(Date date, Date from) {
		Calendar fromCalendar = new GregorianCalendar();
		fromCalendar.setTime(from);
		
		Calendar toCalendar = new GregorianCalendar();
		toCalendar.setTime(date);
		toCalendar.set(Calendar.HOUR, fromCalendar.get(Calendar.HOUR));
		toCalendar.set(Calendar.MINUTE, fromCalendar.get(Calendar.MINUTE));
		toCalendar.set(Calendar.SECOND, fromCalendar.get(Calendar.SECOND));
		
		return toCalendar.getTime();
	}
	
	public static Date addDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}
	
	public static Date getLater(Date date1, Date date2) {
		if (date1 == null) return date2;
		if (date2 == null) return date1;
		
		return date1.after(date2)? date1: date2;
	}
	
	public static List<Date> getDaysInRange(Date start, Date end) {
		List<Date> dates = new ArrayList<>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(start);
	 
		while (calendar.getTime().before(end)) {
			Date result = calendar.getTime();
			dates.add(result);
			calendar.add(Calendar.DATE, 1);
		}
		
		return dates;
	}
	
	public static List<Date> getDaysInRange(Date start, Date end, Collection<DayOfWeek> daysOfWeek) {
		List<Date> dates = new ArrayList<>();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(start);
	 
		while (calendar.getTime().before(end)) {
			DayOfWeek dayOfWeek = toLocalDate(calendar.getTime()).getDayOfWeek();
			if (daysOfWeek.contains(dayOfWeek)) {
				Date result = calendar.getTime();
				dates.add(result);
			}
			calendar.add(Calendar.DATE, 1);
		}
		
		return dates;
	}

}
