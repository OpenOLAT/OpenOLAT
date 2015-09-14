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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;

public class CalendarUtils {
	private static final OLog log = Tracing.createLoggerFor(CalendarUtils.class);
	private static final SimpleDateFormat ical4jFormatter = new SimpleDateFormat("yyyyMMdd");

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
	
	public static DateList getRecurringsInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent) {
		DateList recurDates = null;
			String recurrenceRule = kEvent.getRecurrenceRule();
			if(recurrenceRule != null && !recurrenceRule.equals("")) {
				try {
					Recur recur = new Recur(recurrenceRule);
					net.fortuna.ical4j.model.Date periodStartDate = CalendarUtils.createDate(periodStart);
					net.fortuna.ical4j.model.Date periodEndDate = CalendarUtils.createDate(periodEnd);
					net.fortuna.ical4j.model.Date eventStartDate = CalendarUtils.createDate(kEvent.getBegin());
					recurDates = recur.getDates(eventStartDate, periodStartDate, periodEndDate, Value.DATE);
				} catch (ParseException e) {
					log.error("cannot restore recurrence rule: " + recurrenceRule, e);
				}
				
				String recurrenceExc = kEvent.getRecurrenceExc();
				if(recurrenceExc != null && !recurrenceExc.equals("")) {
					try {
						ExDate exdate = new ExDate();
						// expected date+time format: 
						// 20100730T100000
						// unexpected all-day format:
						// 20100730
						// see OLAT-5645
						if (recurrenceExc.length()>8) {
							exdate.setValue(recurrenceExc);
						} else {
							exdate.getParameters().replace(Value.DATE);
							exdate.setValue(recurrenceExc);
						}
						for( Object date : exdate.getDates() ) {
							if(recurDates.contains(date)) recurDates.remove(date);
						}
					} catch (ParseException e) {
						log.error("cannot restore excluded dates for this recurrence: " + recurrenceExc, e);
					}
				}
			}
		
		return recurDates;
	}
	
	/**
	 * @see org.olat.commons.calendar.CalendarManager#getRecurringDatesInPeriod(java.util.Date, java.util.Date, org.olat.commons.calendar.model.KalendarEvent)
	 */
	public static List<KalendarRecurEvent> getRecurringDatesInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent, TimeZone userTz) {
		List<KalendarRecurEvent> lstDates = new ArrayList<KalendarRecurEvent>();
		DateList recurDates = getRecurringsInPeriod(periodStart, periodEnd, kEvent);
		if(recurDates == null) return lstDates;
		
		for( Object obj : recurDates ) {
			net.fortuna.ical4j.model.Date date = (net.fortuna.ical4j.model.Date)obj;
			
			KalendarRecurEvent recurEvent;
			
			java.util.Calendar eventStartCal = java.util.Calendar.getInstance();
			eventStartCal.clear();
			eventStartCal.setTime(kEvent.getBegin());
			
			java.util.Calendar eventEndCal = java.util.Calendar.getInstance();
			eventEndCal.clear();
			eventEndCal.setTime(kEvent.getEnd());
			
			java.util.Calendar recurStartCal = java.util.Calendar.getInstance();
			recurStartCal.clear();
			if(userTz == null) {
				recurStartCal.setTimeInMillis(date.getTime());
			} else {
				recurStartCal.setTimeInMillis(date.getTime() - userTz.getOffset(date.getTime()));
			}
			long duration = kEvent.getEnd().getTime() - kEvent.getBegin().getTime();

			java.util.Calendar beginCal = java.util.Calendar.getInstance();
			beginCal.clear();
			beginCal.set(
					recurStartCal.get(java.util.Calendar.YEAR), 
					recurStartCal.get(java.util.Calendar.MONTH), 
					recurStartCal.get(java.util.Calendar.DATE), 
					eventStartCal.get(java.util.Calendar.HOUR_OF_DAY), 
					eventStartCal.get(java.util.Calendar.MINUTE), 
					eventStartCal.get(java.util.Calendar.SECOND)
					);
			
			java.util.Calendar endCal = java.util.Calendar.getInstance();
			endCal.clear();
			endCal.setTimeInMillis(beginCal.getTimeInMillis() + duration);
			if(kEvent.getBegin().compareTo(beginCal.getTime()) == 0) continue; //prevent doubled events
			Date recurrenceEnd = CalendarUtils.getRecurrenceEndDate(kEvent.getRecurrenceRule());
			if(kEvent.isAllDayEvent() && recurrenceEnd != null && recurStartCal.getTime().after(recurrenceEnd)) continue; //workaround for ical4j-bug in all day events
			recurEvent = new KalendarRecurEvent(kEvent.getID(), kEvent.getSubject(), new Date(beginCal.getTimeInMillis()), new Date(endCal.getTimeInMillis()));
			recurEvent.setSourceEvent(kEvent);
			lstDates.add(recurEvent);
		}
		return lstDates;
	}
	
	public static List<KalendarEvent> listEventsForPeriod(Kalendar calendar, Date periodStart, Date periodEnd) {
		List<KalendarEvent> periodEvents = new ArrayList<KalendarEvent>();
		CalendarManager cm = CoreSpringFactory.getImpl(CalendarManager.class);
		Collection<KalendarEvent> events = calendar.getEvents();
		for (Iterator<KalendarEvent> iter = events.iterator(); iter.hasNext();) {
			KalendarEvent event = iter.next();
			List<KalendarRecurEvent> lstEvnt = cm.getRecurringDatesInPeriod(periodStart, periodEnd, event);
			for ( KalendarRecurEvent recurEvent : lstEvnt ) {
				periodEvents.add(recurEvent);
			}
			if ((event.getEnd() != null && event.getEnd().before(periodStart)) || event.getBegin().after(periodEnd)) {
				continue;
			}
			periodEvents.add(event);
		}
		return periodEvents;
	}
	
	public static String getRecurrence(String rule) {
		if (rule != null) {
			try {
				Recur recur = new Recur(rule);
				String frequency = recur.getFrequency();
				WeekDayList wdl = recur.getDayList();
				Integer interval = recur.getInterval();
				if((wdl != null && wdl.size() > 0)) {
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
	 * 
	 * @param rule
	 * @return date of recurrence end
	 */
	public static Date getRecurrenceEndDate(String rule) {
		TimeZone tz = ((CalendarModule)CoreSpringFactory.getBean("calendarModule")).getDefaultTimeZone();
		if (rule != null) {
			try {
				Recur recur = new Recur(rule);
				Date dUntil = recur.getUntil();
				DateTime dtUntil = dUntil == null ? null : new DateTime(dUntil.getTime());
				if(dtUntil != null) {
					if(tz != null) {
						dtUntil.setTimeZone(tz);
					}
					return dtUntil;
				}
			} catch (ParseException e) {
				log.error("cannot restore recurrence rule", e);
			}
		}
		
		return null;
	}
	
	/**
	 * Build iCalendar-compliant recurrence rule
	 * @param recurrence
	 * @param recurrenceEnd
	 * @return rrule
	 */
	public static String getRecurrenceRule(String recurrence, Date recurrenceEnd) {
		TimeZone tz = ((CalendarModule)CoreSpringFactory.getBean("calendarModule")).getDefaultTimeZone();
		
		if (recurrence != null) { // recurrence available
			// create recurrence rule
			StringBuilder sb = new StringBuilder();
			sb.append("FREQ=");
			if(recurrence.equals(KalendarEvent.WORKDAILY)) {
				// build rule for monday to friday
				sb.append(KalendarEvent.DAILY);
				sb.append(";");
				sb.append("BYDAY=MO,TU,WE,TH,FR");
			} else if(recurrence.equals(KalendarEvent.BIWEEKLY)) {
				// build rule for biweekly
				sb.append(KalendarEvent.WEEKLY);
				sb.append(";");
				sb.append("INTERVAL=2");
			} else {
				// normal supported recurrence
				sb.append(recurrence);
			}
			if(recurrenceEnd != null) {
				DateTime recurEndDT = new DateTime(recurrenceEnd.getTime());
				if(tz != null) {
					recurEndDT.setTimeZone(tz);
				}
				sb.append(";");
				sb.append(KalendarEvent.UNTIL);
				sb.append("=");
				sb.append(recurEndDT.toString());
			}
			try {
				Recur recur = new Recur(sb.toString());
				RRule rrule = new RRule(recur);
				return rrule.getValue();
			} catch (ParseException e) {
				log.error("cannot create recurrence rule: " + recurrence.toString(), e);
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
		List<Date> recurExcDates = new ArrayList<Date>();
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
		if(dates != null && dates.size() > 0) {
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
			synchronized(ical4jFormatter) {//cluster_OK only to optimize memory/speed
				toString = ical4jFormatter.format(date);
			}
			return new net.fortuna.ical4j.model.Date(toString);
		} catch (ParseException e) {
			return null;
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
}