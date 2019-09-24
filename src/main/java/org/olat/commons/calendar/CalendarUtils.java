/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.property.ExDate;

public class CalendarUtils {
	private static final Logger log = Tracing.createLoggerFor(CalendarUtils.class);
	private static final SimpleDateFormat ical4jDateFormatter = new SimpleDateFormat("yyyyMMdd");
	private static final SimpleDateFormat ical4jDateTimeFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	private static final SimpleDateFormat occurenceDateTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

	private static final DateFormat iso8601Date = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat iso8601DateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static String getTimeAsString(Date date, Locale locale) {
		return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(date);
	}
	
	public static String getDateTimeAsString(Date date, Locale locale) {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(date);
	}

	/**
	 * Create a calendar instance that uses mondays or sundays as the first day of
	 * the week depending on the given locale and sets the week number 1 to the
	 * first week in the year that has four days of january.
	 * 
	 * @param local the locale to define if a week starts on sunday or monday
	 * @return a calendar instance
	 */
	public static Calendar createCalendarInstance(Locale locale) {
		// use Calendar.getInstance(locale) that sets first day of week
		// according to locale or let user decide in GUI
		Calendar cal = Calendar.getInstance(locale);
		// manually set min days to 4 as we are used to have it
		cal.setMinimalDaysInFirstWeek(4);						
		return cal;
	}	
	
	public static Calendar getStartOfDayCalendar(Locale locale) {
		Calendar cal = createCalendarInstance(locale);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static Date startOfDay(Date date)  {
		if(date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getStartOfDay(cal).getTime();
	}
	
	public static Calendar getStartOfDay(Calendar cal)  {
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static Date endOfDay(Date date) {
		if(date == null) return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return getEndOfDay(cal).getTime();
	}
	
	public static Calendar getEndOfDay(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static String getRecurrence(String rule) {
		if (rule != null) {
			try {
				Recur recur = new Recur(rule);
				String frequency = recur.getFrequency();
				WeekDayList wdl = recur.getDayList();
				Integer interval = recur.getInterval();
				if((wdl != null && !wdl.isEmpty())) {
					// we only support one rule with daylist
					return KalendarEvent.WORKDAILY;
				} else if(interval != null && interval == 2) {
					// we only support one rule with interval
					return KalendarEvent.BIWEEKLY;
				} else {
					// native supportet rule
					return frequency;
				}
			} catch (ParseException e) {
				log.error("cannot restore recurrence rule", e);
			}
		}
		
		return null;
	}
	
	/**
	 * Create list with excluded dates based on the exclusion rule.
	 * @param recurrenceExc
	 * @return list with excluded dates
	 */
	public static List<Date> getRecurrenceExcludeDates(String recurrenceExc) {
		List<Date> recurExcDates = new ArrayList<>();
		if(recurrenceExc != null && !recurrenceExc.equals("")) {
			try {
				net.fortuna.ical4j.model.ParameterList pl = new net.fortuna.ical4j.model.ParameterList();
				ExDate exdate = new ExDate(pl, recurrenceExc);
				DateList dl = exdate.getDates();
				for( Object date : dl ) {
					Date excDate = (Date)date;
					recurExcDates.add(excDate);
				}
			} catch (ParseException e) {
				log.error("cannot restore recurrence exceptions", e);
			}
		}
		
		return recurExcDates;
	}
	
	/**
	 * Create exclusion rule based on list with dates.
	 * @param dates
	 * @return string with exclude rule
	 */
	public static String getRecurrenceExcludeRule(List<Date> dates) {
		if(dates != null && !dates.isEmpty()) {
			DateList dl = new DateList();
			for( Date date : dates ) {
				net.fortuna.ical4j.model.Date dd = CalendarUtils.createDate(date);
				dl.add(dd);
			}
			ExDate exdate = new ExDate(dl);
			return exdate.getValue();
		}
		
		return null;
	}
	
	public static net.fortuna.ical4j.model.Date createDate(Date date) {
		try {
			String toString;
			synchronized(ical4jDateFormatter) {//cluster_OK only to optimize memory/speed
				toString = ical4jDateFormatter.format(date);
			}
			return new net.fortuna.ical4j.model.Date(toString);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static net.fortuna.ical4j.model.DateTime createDateTime(Date date) {
		try {
			String toString;
			synchronized(ical4jDateTimeFormatter) {//cluster_OK only to optimize memory/speed
				toString = ical4jDateTimeFormatter.format(date);
			}
			return new net.fortuna.ical4j.model.DateTime(toString);
		} catch (ParseException e) {
			return null;
		}
	}
	
	public static String formatRecurrenceDate(Date date, boolean allDay) {
		try {
			String toString;
			if(allDay) {
				synchronized(ical4jDateFormatter) {//cluster_OK only to optimize memory/speed
					toString = ical4jDateFormatter.format(date);
				}
			} else {
				synchronized(occurenceDateTimeFormat) {//cluster_OK only to optimize memory/speed
					toString = occurenceDateTimeFormat.format(date);
				}
			}
			return toString;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * The method will guess if the specified date as string
	 * is a date alone or a date with time.
	 * 
	 * @param d The date as string in ISO 8601 format.
	 * @return The date object
	 * @throws ParseException
	 */
	public static Date parseISO8601(String d) throws ParseException {
		Date date;
		if(StringHelper.containsNonWhitespace(d)) {
			if(d.indexOf('T') >= 0) {
				date = parseISO8601Datetime(d);
			} else {
				date = parseISO8601Date(d);
			}
		} else {
			date = null;
		}
		return date;
	}
	
	/*
	 * Formats the given date with the ISO 8601 standard also known as 'date'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date
	 */
	public static String formatISO8601Date(Date d) {
		synchronized (iso8601Date) {
			return iso8601Date.format(d);
		}
	}
	
	/**
	 * Parse the given date with the ISO 8601 standard also known as 'date'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date as string to be parsed
	 * @return The date
	 */
	public static Date parseISO8601Date(String d) throws ParseException {
		synchronized (iso8601Date) {
			return iso8601Date.parse(d);
		}
	}
	
	/**
	 * Formats the given date with the ISO 8601 standard also known as 'datetime'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date to be formatted
	 * @return a String with the formatted date and time
	 */
	public static String formatISO8601Datetime(Date d) {
		synchronized (iso8601DateTime) {
			return iso8601DateTime.format(d);
		}
	}
	
	/**
	 * Parse the given date with the ISO 8601 standard also known as 'datetime'
	 * See http://www.w3.org/TR/NOTE-datetime.html for more info.
	 * 
	 * @param d the date as string to be parsed
	 * @return The date
	 */
	public static Date parseISO8601Datetime(String d) throws ParseException {
		synchronized (iso8601DateTime) {
			return iso8601DateTime.parse(d);
		}
	}
	
	public static Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);  
		cal.set(Calendar.MINUTE, 0);  
		cal.set(Calendar.SECOND, 0);  
		cal.set(Calendar.MILLISECOND, 0);  
		return cal.getTime();
	}
	
	public static boolean isToday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar now = Calendar.getInstance();
		now.setTime(new Date());
		return now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
				&& now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
	}
	
	public static boolean isSameDay(Date date, Date otherDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		Calendar otherCal = Calendar.getInstance();
		otherCal.setTime(otherDate);
		return otherCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
				&& otherCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR);
	}
}