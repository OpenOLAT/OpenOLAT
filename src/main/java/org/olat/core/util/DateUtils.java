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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
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
	
	public static Date toDate(ZonedDateTime zonedDateTime) {
		if (zonedDateTime == null) return null;
		return toDate(zonedDateTime, ZoneId.systemDefault());
	}
	
	public static Date toDate(ZonedDateTime zonedDateTime, ZoneId zone) {
		if (zonedDateTime == null) return null;
		return Date.from(zonedDateTime.withZoneSameInstant(zone).toInstant());
	}
 
	public static LocalDate toLocalDate(Date date) {
		if (date == null) return null;
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static LocalDate toLocalDate(ZonedDateTime dateTime) {
		if (dateTime == null) return null;
		return dateTime.withZoneSameLocal(ZoneId.systemDefault()).toLocalDate();
	}
 
	public static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) return null;
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	public static ZonedDateTime toZonedDateTime(Date date) {
		if (date == null) return null;
		return toZonedDateTime(date, ZoneId.systemDefault());
	}
	
	public static ZonedDateTime toZonedDateTime(Date date, ZoneId zone) {
		if (date == null) return null;
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), zone);
	}
	
	public static boolean isSameDate(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDate(cal1, cal2);
	}

	protected static boolean isSameDate(Calendar cal1, Calendar cal2) {
		return isSameDay(cal1, cal2) && isSameTime(cal1, cal2);
	}
	
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null && date2 == null) return true;
		if (date1 == null && date2 != null) return false;
		if (date1 != null && date2 == null) return false;
		
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
				cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}
	
	public static boolean isSameDay(ZonedDateTime date1, ZonedDateTime date2) {
		return date1.getYear() == date2.getYear()
				&& date1.getDayOfYear() == date2.getDayOfYear();
	}
	
	public static boolean isSameDay(LocalDate date1, LocalDate date2) {
		return date1.getYear() == date2.getYear()
				&& date1.getDayOfYear() == date2.getDayOfYear();
	}
	
	public static boolean isSameTime(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameTime(cal1, cal2);
	}

	public static boolean isSameTime(Calendar cal1, Calendar cal2) {
		return (cal1.get(Calendar.SECOND) == cal2.get(Calendar.SECOND) &&
				cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE) &&
				cal1.get(Calendar.HOUR) == cal2.get(Calendar.HOUR));
	}
	
	public static Date setTime(Date date, int hour, int minutes, int seconds) {
		if (date == null) {
			return null;
		}
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static ZonedDateTime getZonedDateTime(int year, int month, int days) {
		return ZonedDateTime.of(year, month, days, 0, 0, 0, 0, ZoneId.systemDefault());
	}
	
	public static ZonedDateTime setZonedTime(ZonedDateTime date, int hour, int minutes, int seconds) {
		if (date == null) {
			return null;
		}
		return date
				.withHour(hour)
				.withMinute(minutes)
				.withSecond(seconds)
				.with(ChronoField.MILLI_OF_SECOND, 0l);
	}
	
	public static ZonedDateTime setZonedTime(Date date, int hour, int minutes, int seconds) {
		if (date == null) {
			return null;
		}
		
		return DateUtils.toZonedDateTime(date)
				.withHour(hour)
				.withMinute(minutes)
				.withSecond(seconds)
				.with(ChronoField.MILLI_OF_SECOND, 0l);
	}
	
	public static Date truncateSeconds(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date truncateMilliSeconds(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	/**
	 * Round something like 10:49:45.200 to 10:50:00.000
	 * 
	 * @param The date
	 * @return The rounded date
	 */
	public static Date roundToMinute(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return roundToMinute(calendar);
	}
	
	/**
	 * Round something like 10:49:45.200 to 10:50:00.000
	 * 
	 * @param calendar
	 * @return The rounded date
	 */
	public static Date roundToMinute(Calendar calendar) {
		int seconds = calendar.get(Calendar.SECOND);
		if(seconds > 30) {
			calendar.add(Calendar.MINUTE, 1);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date getStartOfDay(Date date) {
		if (date == null) {
			return null;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date getEndOfDay(Date date) {
		if (date == null) {
			return null;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}
	
	public static LocalDateTime getEndOfDay(LocalDateTime cal) {
		return cal.withHour(23)
				  .withMinute(59)
				  .withSecond(59)
				  .withNano(0);
	}
	
	public static ZonedDateTime getEndOfDay(ZonedDateTime cal) {
		return cal.withHour(23)
				  .withMinute(59)
				  .withSecond(59)
				  .withNano(0);
	}
	
	public static Date getStartOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 1);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date getEndOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MONTH, Calendar.DECEMBER);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
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
		LocalDateTime ldtDate = toLocalDateTime(date);
		LocalDateTime ldtfrom = toLocalDateTime(from);
		LocalDateTime localDateTime = LocalDateTime.of(ldtDate.toLocalDate(), ldtfrom.toLocalTime());
		return toDate(localDateTime);
	}
	
	public static Date addYears(Date date, int years) {
		if (date == null) return null;
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, years);
		return c.getTime();
	}
	
	public static Date addMonth(Date date, int months) {
		if (date == null) return null;
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, months);
		return c.getTime();
	}

	public static Date addWeeks(Date date, int weeks) {
		if (date == null) return null;

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.WEEK_OF_YEAR, weeks);
		return c.getTime();
	}
	
	public static Date addDays(Date date, int days) {
		if (date == null) return null;
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	public static Date addHours(Date date, int hours) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, hours);
		return c.getTime();
	}
	
	public static Date addMinutes(Date date, int minutes) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, minutes);
		return c.getTime();
	}
	
	public static Date addSeconds(Date date, int seconds) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, seconds);
		return c.getTime();
	}
	
	public static Date getLater(Date date1, Date date2) {
		if (date1 == null) return date2;
		if (date2 == null) return date1;
		
		return date1.after(date2)? date1: date2;
	}
	
	public static Date getEarlier(Date date1, Date date2) {
		if (date1 == null) return date2;
		if (date2 == null) return date1;
		
		return date1.before(date2)? date1: date2;
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

	public static boolean isOverlapping(Date start1, Date end1, Date start2, Date end2) {
		if (start1.before(start2)) {
			if (end1.before(start2)) {
				return false;
			}
		} else {
			if (end2.before(start1)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isOverlapping(ZonedDateTime start1, ZonedDateTime end1, ZonedDateTime start2, ZonedDateTime end2) {
		if (start1.isBefore(start2)) {
			if (end1.isBefore(start2)) {
				return false;
			}
		} else {
			if (end2.isBefore(start1)) {
				return false;
			}
		}
		return true;
	}
	
	public static long countDays(Date date1, Date date2) {
		LocalDate lDate1 = toLocalDate(date1);
		LocalDate lDate2 = toLocalDate(date2);
		return ChronoUnit.DAYS.between(lDate1, lDate2);
	}

	/**
	 * Converts a time string in the format "Xd Yh Zm" into total minutes.
	 * The function accepts any combination of days (d), hours (h), and minutes (m),
	 * separated by spaces. The units are case-insensitive.
	 * <p>
	 * Examples:
	 * - "1d 1h 1m" → 1501 minutes (1440 + 60 + 1)
	 * - "2h 30m"   → 150 minutes
	 * - "1d"       → 1440 minutes
	 *
	 * @param timeString The input string in format "Xd Yh Zm" where X, Y, and Z are integers
	 *                   and d, h, m represent days, hours, and minutes respectively.
	 *                   Parts can be omitted and whitespace is trimmed.
	 * @return The total number of minutes as a long value.
	 * Returns 0 if the input is null or empty.
	 */
	public static long parseTimeToMinutes(String timeString) {
		if (timeString == null || timeString.trim().isEmpty()) {
			return 0;
		}

		long totalMinutes = 0;
		String[] parts = timeString.trim().split("\\s+");

		for (String part : parts) {
			part = part.trim().toLowerCase();
			if (part.isEmpty()) continue;

			int value;
			try {
				value = Integer.parseInt(part.substring(0, part.length() - 1));
			} catch (NumberFormatException e) {
				continue;
			}

			char unit = part.charAt(part.length() - 1);
			switch (unit) {
				case 'd':
					totalMinutes += (long) value * 24 * 60; // days to minutes
					break;
				case 'h':
					totalMinutes += value * 60L;      // hours to minutes
					break;
				case 'm':
					totalMinutes += value;           // already in minutes
					break;
				default:
					throw new NumberFormatException("Invalid unit: " + unit);
			}
		}

		return totalMinutes;
	}

	/**
	 * Converts a number of minutes into a formatted time string using days, hours, and minutes.
	 * The output format is "Xd Yh Zm" where parts with zero values are omitted.
	 * All values are represented as positive integers.
	 * <p>
	 * Examples:
	 * - 1501 minutes → "1d 1h 1m" (1440 + 60 + 1)
	 * - 150 minutes  → "2h 30m"
	 * - 1440 minutes → "1d"
	 * - 0 minutes    → "0m"
	 *
	 * @param minutes The number of minutes to convert
	 * @return A formatted string in the pattern "Xd Yh Zm" where X, Y, and Z are integers
	 * representing days, hours, and minutes respectively. Parts with zero values
	 * are omitted except when the total is zero, then "0m" is returned.
	 */
	public static String formatMinutesToTimeString(long minutes) {
		if (minutes == 0) {
			return "0m";
		}

		StringBuilder result = new StringBuilder();
		boolean needsSpace = false;

		// Calculate days
		long days = minutes / (24 * 60);
		if (days > 0) {
			result.append(days).append("d");
			needsSpace = true;
			minutes %= (24 * 60);
		}

		// Calculate hours
		long hours = minutes / 60;
		if (hours > 0) {
			if (needsSpace) {
				result.append(" ");
			}
			result.append(hours).append("h");
			needsSpace = true;
			minutes %= 60;
		}

		// Add remaining minutes
		if (minutes > 0) {
			if (needsSpace) {
				result.append(" ");
			}
			result.append(minutes).append("m");
		}

		return result.toString();
	}

	public static int yearFromDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(java.util.Calendar.YEAR);
	}

	public static  int monthFromDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(java.util.Calendar.MONTH) + 1;
	}

	public static  int dayFromDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.get(java.util.Calendar.DAY_OF_MONTH);
	}
}
