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
 * <p>
 */

package org.olat.commons.calendar.ui.components;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.DateUtils;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

public class WeeklyCalendarComponentRenderer implements ComponentRenderer {

	
	/* This is fix (see calendar.css) */
	private int days = 7;
	private int viewStartHour = 7;
	/* This is fix (and depends on settings in calendar.css) */
	private static int dayEventHeightPixels = 20;
	private static int halfHourHeightPixels = 20;
	private static float dayGridRelativeStart = 9;
	private static float dayRelativeWidth = 13;
	private DateFormat dmyDateFormat = new SimpleDateFormat("ddMMyyyyHHmm");

	private boolean isIframePostEnabled = false;
	
	public WeeklyCalendarComponentRenderer(int viewStartHour) {
		this.viewStartHour = viewStartHour;
	}
	
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		WeeklyCalendarComponent calendarComponent = (WeeklyCalendarComponent)source;
		days = calendarComponent.getDisplayDays();
		int year = calendarComponent.getYear();
		int weekOfYear = calendarComponent.getWeekOfYear();
		isIframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();

		// get all events from all calendars within the week's view
		// sort into standard events and allday events
		// sort both arrays
		Calendar cal = CalendarUtils.getStartOfWeekCalendar(calendarComponent.getYear(), calendarComponent.getWeekOfYear(), translator.getLocale());
		Date weekStart = cal.getTime();
		cal.add(Calendar.DAY_OF_YEAR, days);
		Date weekEnd = cal.getTime();
		List<KalendarEventRenderWrapper> sortedEventsWithinWeek = new ArrayList<KalendarEventRenderWrapper>();
		List<KalendarEventRenderWrapper> sortedAllDayEventsWithinWeek = new ArrayList<KalendarEventRenderWrapper>();
		boolean hasWriteableCalendar = false;
		for (Iterator kalendarWrappers = calendarComponent.getKalendars().iterator(); kalendarWrappers.hasNext();) {
			KalendarRenderWrapper calendarWrapper = (KalendarRenderWrapper) kalendarWrappers.next();
			// check if calendar is writeable
			if (calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE)
				hasWriteableCalendar = true;
			// skip if not selected for display
			if (!calendarComponent.isEventAlwaysVisible() && !calendarWrapper.getKalendarConfig().isVis()) continue;
			boolean readOnlyCalendar = calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
			boolean isImportedCalendar = calendarWrapper.isImported();
			List events = CalendarUtils.listEventsForPeriod(calendarWrapper.getKalendar(), weekStart, weekEnd);
			for (Iterator iter = events.iterator(); iter.hasNext();) {
				KalendarEvent event = (KalendarEvent) iter.next();
				// filter out private events on readonly calendars
				if (readOnlyCalendar && !isImportedCalendar && event.getClassification() == KalendarEvent.CLASS_PRIVATE) continue;
				if (event.isAllDayEvent()) {
					sortedAllDayEventsWithinWeek.add(new KalendarEventRenderWrapper(event, calendarWrapper));
				} else {
					sortedEventsWithinWeek.add(new KalendarEventRenderWrapper(event, calendarWrapper));
				}
			}
		}
		Collections.sort(sortedEventsWithinWeek, KalendarEventDateComparator.getInstance());
		Collections.sort(sortedAllDayEventsWithinWeek, KalendarEventDateComparator.getInstance());

		Locale locale = translator.getLocale();
		
		// open grid divs
		sb.append("\n<div class=\"o_cal_wv\" id=\"o_cal_wv\">");		
		// render the grid
		sb.append("<div id=\"o_cal_wv_header_wrapper\">");
		renderWeekHeader(year, weekOfYear, hasWriteableCalendar, sb, ubu, locale);
		renderAllDayGrid(year, weekOfYear, sortedAllDayEventsWithinWeek, sb, ubu, translator);
		sb.append("</div><div class=\"o_cal_wv_grid_wrapper\"><div class=\"o_cal_wv_grid\" id=\"o_cal_wv_grid\">");
		renderWeekGrid(year, weekOfYear, hasWriteableCalendar, sb, ubu, locale);
		// add events to grid, absolute positioned
		renderEvents(year, weekOfYear, sortedEventsWithinWeek, sb, ubu, locale);
		// close grid divs
		sb.append("</div></div></div>");
		
		// optimize grid height and position scrollbar using ext.onready
		sb.append("<script type=\"text/javascript\">\n/* <![CDATA[ */\n");
		sb.append("Ext.onReady(function(){\n");
		sb.append("B_AjaxLogger.logDebug('onready','calendar');");
		sb.append("o_adjustCalendarHeight(\"o_cal_wv_grid\", 960);\n");
		sb.append("window.onresize = function() {o_adjustCalendarHeight(\"o_cal_wv_grid\", 960) };\n");
		sb.append("o_positionScrollbar(\"o_cal_wv_grid\", ").append(viewStartHour * 2 * halfHourHeightPixels).append(");\n");
		sb.append("o_init_event_tooltips();");
		sb.append("o_mark_event_box_overflow();");
		sb.append("\n});");
		sb.append("B_AjaxLogger.logDebug('right away','calendar');");
		sb.append("\n/* ]]> */\n</script>");

		// render print view
		List<KalendarEventRenderWrapper> allEventsOfWeek = new ArrayList<KalendarEventRenderWrapper>(sortedAllDayEventsWithinWeek);
		allEventsOfWeek.addAll(sortedEventsWithinWeek);
		Collections.sort(allEventsOfWeek, KalendarEventDateComparator.getInstance());
		renderEventsListForPrint(year, weekOfYear, allEventsOfWeek, sb, locale);
	}

	private void renderEventsListForPrint(int year, int weekOfYear, List<KalendarEventRenderWrapper> sortedEventsWithinWeek, StringOutput sb,
			Locale locale) {
		sb.append("<div class=\"o_cal_wv_print\">\n");
		sb.append("<ul class=\"o_cal_wv_list\">\n");
		// render day events which are positioned absolute
		Calendar cal = CalendarUtils.getStartOfWeekCalendar(year, weekOfYear, locale);
		Date periodStart = cal.getTime();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date periodEnd = cal.getTime();
		// itterate over days
		for (int dayCounter = 0; dayCounter < days; dayCounter++) {
			// collect all events for actual day
			List<KalendarEventRenderWrapper> sortedEventsOfDay = new ArrayList<KalendarEventRenderWrapper>();
			for (KalendarEventRenderWrapper eventWrapper : sortedEventsWithinWeek) {
				KalendarEvent nextEvent = eventWrapper.getEvent();
				if (nextEvent.getEnd().before(periodStart) || nextEvent.getBegin().after(periodEnd)) continue;
				sortedEventsOfDay.add(eventWrapper);
			}
			// note that eventWrappers may get re-used if it is a multi-day event
			renderEventsOfDayForPrint(sortedEventsOfDay, periodStart, periodEnd, sb, locale);

			// reposition start/end period
			periodStart = periodEnd;
			cal.add(Calendar.DAY_OF_YEAR, 1);
			periodEnd = cal.getTime();
		} // day iteration
		sb.append("</ul>\n");
		sb.append("<div class=\"b_clearfix\">&nbsp;</div>\n");
		sb.append("</div>\n");
	}

	private void renderEventsOfDayForPrint(List<KalendarEventRenderWrapper> sortedEvents, Date dayStart, Date dayEnd, StringOutput sb, Locale locale) {
		if (!sortedEvents.isEmpty()) {
			sb.append("<li>\n");
			sb.append("<span class=\"o_cal_date\">\n");
			sb.append(StringHelper.formatLocaleDateFull(dayStart.getTime(), locale));
			sb.append("</span>\n");
			sb.append("<ul class=\"o_cal_events\">\n");
			for (KalendarEventRenderWrapper event : sortedEvents) {
				renderEventForPrint(event, dayStart, dayEnd, sb, locale);
			}
			sb.append("</ul>\n");
			sb.append("</li>\n");
		}
	}

	private void renderEventForPrint(KalendarEventRenderWrapper eventWrapper, Date dayStart, Date dayEnd, StringOutput sb, Locale locale) {
		KalendarEvent event = eventWrapper.getEvent();
		boolean hidden = eventWrapper.getCalendarAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY
				&& !eventWrapper.getKalendarRenderWrapper().isImported() && event.getClassification() != KalendarEvent.CLASS_PUBLIC;
		String escapedSubject = Formatter.escWithBR(event.getSubject()).toString();
		escapedSubject = escapedSubject.replace('\r', ' ');
		sb.append("<li class=\"o_cal_event\">\n");
		// time
		sb.append("<div class=\"o_cal_time\"><span>\n");
		Translator translator = Util.createPackageTranslator(CalendarManager.class, locale);
		if (event.isAllDayEvent()) {
			sb.append(translator.translate("cal.form.allday"));
		} else {
			// set start and end times for events spanning more than one day
			Date begin = event.getBegin();
			Date end = event.getEnd();
			if (begin.before(dayStart)) {
				begin = dayStart;
			}
			if (end.after(dayEnd)) {
				end = dayEnd;
			}
			sb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
			sb.append(" - ");
			sb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
		}
		sb.append("</span></div>\n");
		// event name (subject)
		//fxdiff BAKS-13: firefox doesn't break lines with only <br />, we need <p>
		sb.append("<div class=\"o_cal_subject " + eventWrapper.getCssClass() + "\"><p>");
		if (hidden) {
			sb.append("-");
		} else {
			sb.append(escapedSubject.replace("<br />", "</p><p>"));
		}
		sb.append("</p></div>\n");
		// location
		if (StringHelper.containsNonWhitespace(event.getLocation())) {
			sb.append("<div class=\"o_cal_location\"><span>\n");
			sb.append(translator.translate("cal.form.location") + ": ");
			if (!hidden) {
				sb.append(event.getLocation());
			}
			sb.append("</span></div>\n");
		}
		sb.append("</li>\n");
	}
	
	private void renderAllDayGrid(int year, int weekOfYear, List sortedAllDayEventsForWeek, StringOutput sb, URLBuilder ubu, Translator translator) {
		Calendar cal = CalendarUtils.getStartOfWeekCalendar(year, weekOfYear, translator.getLocale());
		int dayToday = -1;
		Calendar calNow = CalendarUtils.createCalendarInstance(translator.getLocale());
		if ( (calNow.get(Calendar.WEEK_OF_YEAR) == weekOfYear)
				&& (calNow.get(Calendar.YEAR) == year) ) {
			// if we are within current week, adjust dayToday
			dayToday = calNow.get(Calendar.DAY_OF_WEEK);
		}
		
		StringOutput inset = new StringOutput(1024);
		int maxDayEvents = 0;
		for (int day = 1; day <= days; day++) {
			int dayOfWeekIter = cal.get(Calendar.DAY_OF_WEEK);
			Date periodBegin = cal.getTime();
			inset.append("<div class=\"o_cal_wv_dlday o_cal_wv_row");
			inset.append(day);
			if (dayOfWeekIter == dayToday) {
				// current day
				inset.append(" o_cal_wv_today");
			} else if (dayOfWeekIter == Calendar.SATURDAY || dayOfWeekIter == Calendar.SUNDAY) {
				// holiday
				inset.append(" o_cal_wv_holiday");
			}
			if (day == days) {
				// last day
				inset.append(" o_cal_wv_lastday");
			}
			inset.append("\" style=\"height: 100%;\">");

			cal.add(Calendar.DAY_OF_YEAR, 1);
			Date periodEnd = cal.getTime();
			// render daylong events
			int maxDayEventsThisDay = 0;
			for (Iterator iter = sortedAllDayEventsForWeek.iterator(); iter.hasNext();) {
				KalendarEventRenderWrapper eventWrapper = (KalendarEventRenderWrapper) iter.next();
				KalendarEvent event = eventWrapper.getEvent();
				// skip if not within period
				if (event.getEnd().compareTo(periodBegin) < 0 || event.getBegin().compareTo(periodEnd) >= 0) continue;
				// increment count of number of dayevents
				maxDayEventsThisDay++;
				boolean hideSubject = eventWrapper.getCalendarAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY && !eventWrapper.getKalendarRenderWrapper().isImported() && event.getClassification() != KalendarEvent.CLASS_PUBLIC;
				String escapedSubject = Formatter.escWithBR(event.getSubject()).toString();
				
				inset.append("<div class=\"o_cal_wv_devent_wrapper\">");
				inset.append("<div class=\"o_cal_wv_devent ").append(eventWrapper.getKalendarRenderWrapper().getKalendarConfig().getCss()).append("\">");
				inset.append("<div class=\"o_cal_wv_devent_content\">");
				inset.append("<a href=\"");
				ubu.buildURI(inset, new String[] {WeeklyCalendarComponent.ID_CMD, WeeklyCalendarComponent.ID_PARAM},
						new String[] {WeeklyCalendarComponent.CMD_EDIT, event.getCalendar().getCalendarID() + WeeklyCalendarComponent.ID_PARAM_SEPARATOR + event.getID() + WeeklyCalendarComponent.ID_PARAM_SEPARATOR + event.getBegin().getTime()},
						isIframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
				inset.append("\" ");
				if (isIframePostEnabled) {
					ubu.appendTarget(inset);
				}
				inset.append(" onclick=\"return o2cl();\">");
				if (hideSubject) {
					inset.append("<i>").append(translator.translate("cal.eventdetails.hidden")).append("</i>");
				} else {
					inset.append(escapedSubject);
				}
				inset.append("</a>");
				// append any event links
				if (!hideSubject) {
					renderEventLinks(event, inset);
				}
				// closing devent_content
				inset.append("</div>");
				
				// render event tooltip content
				renderEventTooltip(eventWrapper, escapedSubject, hideSubject, inset, ubu, translator.getLocale());
				
				// closing devent and devent_wrapper
				inset.append("</div></div>");
			} // events within day iterator
			
			inset.append("</div>");
			if (maxDayEventsThisDay > maxDayEvents) maxDayEvents = maxDayEventsThisDay;
		} // day irterator
		
		// do not render anything if we do not have any allday events
		if (maxDayEvents == 0) return;
		sb.append("\n<div id=\"o_cal_wv_daylong\" style=\"height: ").append(maxDayEvents * dayEventHeightPixels).append("px;\">");
		sb.append("<div class=\"o_cal_wv_time o_cal_wv_row0\" style=\"height: 100%;\"></div>");	
		sb.append(inset);
		sb.append("</div>");
	}
	
	private void renderEvents(int year, int weekOfYear, List sortedEventsWithinWeek, StringOutput sb, URLBuilder ubu, Locale locale) {
		// render day events which are positioned absolute
		Calendar cal = CalendarUtils.getStartOfWeekCalendar(year, weekOfYear, locale);
		Date periodStart = cal.getTime();
		cal.add(Calendar.DAY_OF_YEAR, 1);
		Date periodEnd = cal.getTime();
		// itterate over days
		for (int dayCounter = 0; dayCounter < days; dayCounter++) {
			// collect all events for actual day
			List sortedEventsOfDay = new ArrayList();
			for (Iterator iter = sortedEventsWithinWeek.iterator(); iter.hasNext();) {
				KalendarEventRenderWrapper eventWrapper = (KalendarEventRenderWrapper) iter.next();
				KalendarEvent nextEvent = eventWrapper.getEvent();
				if (nextEvent.getEnd().before(periodStart) || nextEvent.getBegin().after(periodEnd)) continue;
				sortedEventsOfDay.add(eventWrapper);
			}
			// not that eventWrappers may get re-used if it is a multi-day event
			renderEventsForDay(sortedEventsOfDay, periodStart, dayCounter, sb, ubu, locale);
			
			// reposition start/end period
			periodStart = periodEnd;
			cal.add(Calendar.DAY_OF_YEAR, 1);
			periodEnd = cal.getTime();
		} // day iteration
		
	}
	
	private void renderEventsForDay(List sortedEvents, Date dayStart, int column, StringOutput sb, URLBuilder ubu, Locale locale) {
		if (sortedEvents.isEmpty()) return;
		
		// this is the start of this day
		long startDateLong = dayStart.getTime();
		
		/**
		 * STAGE 1: assign slots
		 */
		
		// list containing slot endpoint information (Long(lastEndPointMillis))
		List slotEndPointsList = new ArrayList();
		// make at least one end point slot available
		slotEndPointsList.add(new Long(-1));
		// List of event group with equal slot widths
		List slotGroup = new ArrayList();
		// the maximum slots for this group
		int maxSlotsForGroup = 1;
		// the latest endpoint of this group (this endpoint is part of the slotEndPointsList)
		long latestEndPointForGroup = -1;
		
		// Iterate over all events
		for (int eventCounter = 0; eventCounter < sortedEvents.size(); eventCounter++) {
			KalendarEventRenderWrapper eventWrapper = (KalendarEventRenderWrapper)sortedEvents.get(eventCounter);
			KalendarEvent event = eventWrapper.getEvent();
			// get relative begin / end of event in millis
			long eventBeginRelativeMillis = event.getBegin().getTime() - startDateLong;
			// if begin is before our start period, correct
			if (eventBeginRelativeMillis < 0) eventBeginRelativeMillis = 0;
			long eventEndRelativeMillis = event.getEnd().getTime() - startDateLong;
			// if end relative greater than one day, correct
			if (eventEndRelativeMillis > (1000 * 60 * 60 * 24)) eventEndRelativeMillis = (1000 * 60 * 60 * 24);
			
			// first, cleanup slot group if we reach new, completely empty slot row
			if (eventBeginRelativeMillis > latestEndPointForGroup) {
				// reset slotGroup and slotEndPointsList
				slotGroup = new ArrayList();
				slotEndPointsList = new ArrayList();
				slotEndPointsList.add(new Long(-1));
				maxSlotsForGroup = 1;
			}
			
			// search for a slot which matches relative begin and mark it in the slot list
			boolean foundSlot = false;
			int nextFreeSlot = 0;
			for (; nextFreeSlot < slotEndPointsList.size(); nextFreeSlot++) {
				Long slotEnd = (Long)slotEndPointsList.get(nextFreeSlot);
				if (slotEnd.longValue() <= eventBeginRelativeMillis) {
					foundSlot = true;
					break;
				}
			}
			if (!foundSlot) {
				// no free slots, open a new slot
				// register end point
				slotEndPointsList.add(new Long(eventEndRelativeMillis));
				// correct max slot width for all in slot group
				maxSlotsForGroup++;
				for (Iterator iter = slotGroup.iterator(); iter.hasNext();) {
					KalendarEventRenderWrapper rw = (KalendarEventRenderWrapper) iter.next();
					rw.setMaxSlots(maxSlotsForGroup);
				}
			} else {
				// mark slot end point
				slotEndPointsList.set(nextFreeSlot, new Long(eventEndRelativeMillis));
			}

			// add to slot group
			slotGroup.add(eventWrapper);
			// mark latest end point of slot group
			if (latestEndPointForGroup < eventEndRelativeMillis)
				latestEndPointForGroup = eventEndRelativeMillis;
			// set assigned slot
			eventWrapper.setAssignedSlot(nextFreeSlot);
			// set max slots
			eventWrapper.setMaxSlots(maxSlotsForGroup);

			
			// see how much we can expand to the east
			// note that this makes it n*(n-1) loops. Remove this to have linear times
			int maxExpandToEast = maxSlotsForGroup - nextFreeSlot;
			for (Iterator iter = slotGroup.iterator(); iter.hasNext();) {
				KalendarEventRenderWrapper rw2 = (KalendarEventRenderWrapper) iter.next();
				// skip all slots which are to the west
				if (rw2.getAssignedSlot() <= eventWrapper.getAssignedSlot()) continue;
				// skip if we don't collide with the event
				if (rw2.getEvent().getEnd().before(event.getBegin())) continue;
				// now this event is limiting.... see how far we can grow to the east
				if (maxExpandToEast > rw2.getAssignedSlot())
					maxExpandToEast = rw2.getAssignedSlot();
			}
			eventWrapper.setSlotExpandToEast(maxExpandToEast);
		}
		
		/**
		 * STAGE 2: calculate final geometry
		 */
		
		int pixelsTotalPerPeriod = 24 * 2 * halfHourHeightPixels;
		int millisTotalPerPeriod = 24 * 60 * 60 * 1000;
		for (int eventCounter = 0; eventCounter < sortedEvents.size(); eventCounter++) {
			KalendarEventRenderWrapper eventWrapper = (KalendarEventRenderWrapper)sortedEvents.get(eventCounter);
			
			// calculate Y-coordinate and height ehich are already 
			// total available pixels is 24 * 2 * half hour pixels
			// get relative begin / end of event
			long eventBeginRelative = eventWrapper.getEvent().getBegin().getTime() - startDateLong;
			if (eventBeginRelative < 0) eventBeginRelative = 0;
			long eventEndRelative = eventWrapper.getEvent().getEnd().getTime() - startDateLong;
			if (eventEndRelative > (1000 * 60 * 60 * 24)) eventEndRelative = (1000 * 60 * 60 * 24);
			int yPos = (int)(pixelsTotalPerPeriod * eventBeginRelative / millisTotalPerPeriod);
			int height = (int)((eventEndRelative - eventBeginRelative) * pixelsTotalPerPeriod / millisTotalPerPeriod);
			// DST (daylight saving time) offset (Sommerzeit)
			int dstOffset = 0;
			if ( isDstStart(eventWrapper.getEvent().getBegin()) ) {
				dstOffset = 2 * halfHourHeightPixels;
			}
			if ( isDstEnd(eventWrapper.getEvent().getBegin()) ) {
				dstOffset = -2 * halfHourHeightPixels;
			}
			eventWrapper.setYPosAbsolute(yPos + dstOffset);
			eventWrapper.setHeightAbsolute(height);

			int assignedSlot = eventWrapper.getAssignedSlot();
			int slotsExpandToEast = eventWrapper.getSlotExpandToEast();
			float slotWidth = dayRelativeWidth / eventWrapper.getMaxSlots();
			eventWrapper.setXPosRelative(dayGridRelativeStart  + (dayRelativeWidth * column) + (slotWidth * assignedSlot));
			eventWrapper.setWidthRelative(slotWidth * slotsExpandToEast);
			renderSingleEvent(eventWrapper, sb, ubu, locale);
		}
	}
	
	/**
	 * Return true when the date is the DST (daylight saving time) start date.
	 * Last Sunday in March.
	 * @param date
	 * @return
	 */
	public boolean isDstStart(Date date) {
	  java.util.Calendar cal = new java.util.GregorianCalendar(Calendar.getInstance().getTimeZone());
		cal.setTime(date);
	  cal.set(Calendar.HOUR_OF_DAY, 0);
	  cal.set(Calendar.MINUTE, 30);
	  cal.set(Calendar.SECOND, 0);
	  cal.set(Calendar.MILLISECOND, 0);
	  cal.add(java.util.Calendar.HOUR_OF_DAY, 24);
	  return (1 == cal.get(java.util.Calendar.HOUR_OF_DAY));
	}
	
	/**
	 * Return true when the date is the DST (daylight saving time) end date.
	 * Last Sunday in October.
	 * @param date
	 * @return
	 */
	public boolean isDstEnd(Date date) {
	  java.util.Calendar cal = new java.util.GregorianCalendar(Calendar.getInstance().getTimeZone());
		cal.setTime(date);
	  cal.set(Calendar.HOUR_OF_DAY, 0);
	  cal.set(Calendar.MINUTE, 30);
	  cal.set(Calendar.SECOND, 0);
	  cal.set(Calendar.MILLISECOND, 0);
	  cal.add(java.util.Calendar.HOUR_OF_DAY, 24);
	  return (23 == cal.get(java.util.Calendar.HOUR_OF_DAY));
	}



	private void renderSingleEvent(KalendarEventRenderWrapper eventWrapper, StringOutput sb, URLBuilder ubu, Locale locale) {
		KalendarEvent event = eventWrapper.getEvent();
		boolean hideSubject = eventWrapper.getCalendarAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY && !eventWrapper.getKalendarRenderWrapper().isImported() && event.getClassification() != KalendarEvent.CLASS_PUBLIC;
		String escapedSubject = Formatter.escWithBR(event.getSubject()).toString();
		escapedSubject = escapedSubject.replace('\r', ' ');
		sb.append("\n<div class=\"o_cal_wv_event_wrapper\" style=\"");
		sb.append("left: ").append("" + eventWrapper.getXPosRelative());
		sb.append("%; width: ").append("" + eventWrapper.getWidthRelative());
		sb.append("%; top: ").append(eventWrapper.getYPosAbsolute() - 1 ); // fixt start position -1px for top border
		sb.append("px; height: ").append(eventWrapper.getHeightAbsolute() - 1 ); // fix height -1 px for bottom border
		sb.append("px;\"");
		sb.append(">");
		
		sb.append("<div class=\"o_cal_wv_event ").append(eventWrapper.getCssClass()).append("\" style=\"");
		sb.append("width: auto; height: 100%\">");
		
		sb.append("<div class=\"o_cal_wv_event_header\">");
		if(eventWrapper.getCalendarAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) { 
			sb.append("<a href=\"");
			ubu.buildURI(sb, new String[] {WeeklyCalendarComponent.ID_CMD, WeeklyCalendarComponent.ID_PARAM},
				new String[] {WeeklyCalendarComponent.CMD_EDIT, event.getCalendar().getCalendarID() + WeeklyCalendarComponent.ID_PARAM_SEPARATOR + event.getID() + WeeklyCalendarComponent.ID_PARAM_SEPARATOR + event.getBegin().getTime()},
				isIframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
			sb.append("\" ");
			if (isIframePostEnabled) {
				ubu.appendTarget(sb);
			}
			sb.append(" onclick=\"return o2cl();\">");
		}
		sb.append(CalendarUtils.getTimeAsString(event.getBegin(), locale));
		if(eventWrapper.getCalendarAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) { 
			sb.append("</a>");
		}
		sb.append("</div>");
		
		sb.append("<div class=\"o_cal_wv_event_content\">");
		// check if we need to hide textual information.
		// Private events on readonly kalendars are already filtered out and do not appear in the render list.
		// We thus only need to check for freebusy events in readonly calendars.
		if (!hideSubject) {
			sb.append(escapedSubject);
			// append any event links
			renderEventLinks(event, sb);
		}
		sb.append("</div>");
		// tooltip content
		renderEventTooltip(eventWrapper, escapedSubject, hideSubject, sb, ubu, locale);
		sb.append("</div></div>");
	}

	private void renderEventTooltip(KalendarEventRenderWrapper eventWrapper, String escapedSubject, boolean hideSubject, StringOutput sb, URLBuilder ubu, Locale locale) {
		KalendarEvent event = eventWrapper.getEvent();
		// Tooltip content
		sb.append("<div class=\"o_cal_wv_event_tooltip");
		if (event.isAllDayEvent()) {
			// add marker css class used in js code to identify all day events
			sb.append(" o_cal_allday");
		}
		sb.append("\">");
		// time
		sb.append("<div class=\"o_cal_time\">\n");
		Translator translator = Util.createPackageTranslator(CalendarManager.class, locale);
		Calendar cal = CalendarUtils.createCalendarInstance(locale);
		Date begin = event.getBegin();
		Date end = event.getEnd();	
		cal.setTime(begin);
		sb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
		if (!event.isAllDayEvent()) {
			sb.append("<br />").append(StringHelper.formatLocaleTime(begin.getTime(), locale));
			sb.append(" - ");
			if (!DateUtils.isSameDay(begin, end)) {
				sb.append(StringHelper.formatLocaleDateFull(end.getTime(), locale)).append(", ");
			} 
			sb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
		}
		sb.append("</div>\n");
		if (!hideSubject) {
		  sb.append("<div class=\"o_cal_wv_event_tooltip_content\">");
		  sb.append(escapedSubject);
		}

		// location
		if (StringHelper.containsNonWhitespace(event.getLocation())) {
			sb.append("<div class=\"o_cal_location\">\n");
			if (!hideSubject) {
				sb.append("<b>").append(translator.translate("cal.form.location")).append("</b>:");
				sb.append(event.getLocation());
			}
			sb.append("</div>\n");
		}
		// links
		if (!hideSubject) {
			renderEventLinks(event, sb);
		}

		if(eventWrapper.getCalendarAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) {
			// edit link
			sb.append("<div class=\"o_cal_tooltip_buttons\"><a class=\"b_button b_xsmall\" href=\"");
			ubu.buildURI(sb, new String[] {WeeklyCalendarComponent.ID_CMD, WeeklyCalendarComponent.ID_PARAM},
					new String[] {WeeklyCalendarComponent.CMD_EDIT, event.getCalendar().getCalendarID() + WeeklyCalendarComponent.ID_PARAM_SEPARATOR + event.getID() + WeeklyCalendarComponent.ID_PARAM_SEPARATOR + event.getBegin().getTime()},
					isIframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
			sb.append("\" ");
			if (isIframePostEnabled) {
				ubu.appendTarget(sb);
			}
			sb.append("><span>");
			sb.append(translator.translate("edit"));
			sb.append("</span></a></div>");
		}
		
		//tooltip_content		
		if (!hideSubject) {
		  sb.append("</div>");
		}
		//event_tooltip
		sb.append("</div>");
	}
	
	private void renderEventLinks(KalendarEvent event, StringOutput sb) {
		List kalendarEventLinks = event.getKalendarEventLinks();
		
		if (kalendarEventLinks != null) {
			sb.append("<div class=\"o_cal_links\">");
			for (Iterator iter = kalendarEventLinks.iterator(); iter.hasNext();) {
				KalendarEventLink link = (KalendarEventLink) iter.next();
				sb.append("<br /><b>");				
				//fxdiff
				String uri = link.getURI();
				String iconCssClass = link.getIconCssClass();
				if(!StringHelper.containsNonWhitespace(iconCssClass)) {
					String displayName = link.getDisplayName();
					iconCssClass = CSSHelper.createFiletypeIconCssClassFor(displayName);
				}
				
				if(uri.contains("://")) {
					sb.append("<a href=\"").append(uri).append("\" title=\"").append(StringEscapeUtils.escapeHtml(link.getDisplayName())).append("\" ");
					
					if (StringHelper.containsNonWhitespace(iconCssClass)) {
						sb.append("class=\"b_with_small_icon_left ").append(iconCssClass).append("\"");
					}
					sb.append(" target=\"_blank\">").append(link.getDisplayName()).append("</a>");
				} else {
					sb.append("<a href=\"javascript:top.o_openUriInMainWindow('").append(uri).append("')\" title=\"").append(StringEscapeUtils.escapeHtml(link.getDisplayName())).append("\" ");
					if (StringHelper.containsNonWhitespace(iconCssClass)) {
						sb.append("class=\"b_with_small_icon_left ").append(iconCssClass).append("\"");
					}
					sb.append(" onclick=\"return o2cl();\">").append(link.getDisplayName()).append("</a>");
				} 
				sb.append("</b>");
			}
			sb.append("</div>");
		}
	}
	private void renderWeekHeader(int year, int weekOfYear, boolean enableAddEvent, StringOutput sb, URLBuilder ubu, Locale locale) {
		Calendar cal = CalendarUtils.getStartOfWeekCalendar(year, weekOfYear, locale);
		Calendar calNow = CalendarUtils.createCalendarInstance(locale);
		cal.set(Calendar.HOUR_OF_DAY, calNow.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, calNow.get(Calendar.MINUTE));
		SimpleDateFormat dayMonth = new SimpleDateFormat("E dd.MM", locale);

		// render header
		sb.append("\n<div id=\"o_cal_wv_header\">");
		sb.append("<div class=\"o_cal_wv_time o_cal_wv_row0\">");
		sb.append("<div class=\"o_cal_wv_legend_hours\"><div></div></div></div>");
		int dayToday = -1;
		if ( (calNow.get(Calendar.WEEK_OF_YEAR) == weekOfYear)
				&& (calNow.get(Calendar.YEAR) == year) ) {
			// if we are within current week, adjust dayToday
			dayToday = calNow.get(Calendar.DAY_OF_WEEK);
		}
		for (int i = 1; i <= days; i++) {
			int dayOfWeekIter = cal.get(Calendar.DAY_OF_WEEK);
			sb.append("\n<div class=\"o_cal_wv_day ");
			sb.append("o_cal_wv_row");
			sb.append(i);
			sb.append(" ");
			if (i == 7) {
				// terminate last day
				sb.append("o_cal_wv_lastday ");
			}
			if (dayOfWeekIter == dayToday) {
				// current day
				sb.append("o_cal_wv_today ");
			} else if (dayOfWeekIter == Calendar.SATURDAY || dayOfWeekIter == Calendar.SUNDAY) {
				// holiday
				sb.append("o_cal_wv_holiday ");
			}			
			sb.append("\">");
			sb.append("<div class=\"o_cal_wv_legend_day\"><div>");
			if (enableAddEvent) {
				sb.append("<a href=\"");
				ubu.buildURI(sb, new String[] {WeeklyCalendarComponent.ID_CMD, WeeklyCalendarComponent.ID_PARAM},
						new String[] {WeeklyCalendarComponent.CMD_ADD_ALLDAY, dmyDateFormat.format(cal.getTime())},
						isIframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
				sb.append("\" ");
				if (isIframePostEnabled) {
					ubu.appendTarget(sb); 
				}
				sb.append(" onclick=\"return o2cl();\">");
				sb.append(dayMonth.format(cal.getTime()));
				sb.append("</a>");
			} else {
				sb.append(dayMonth.format(cal.getTime()));
			}
			sb.append("</div></div></div>");
			// add one day to calendar
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		sb.append("</div>");
	}
	
	private void renderWeekGrid(int year, int weekOfYear, boolean enableAddEvent, StringOutput sb, URLBuilder ubu, Locale locale) {
		Calendar cal = CalendarUtils.getStartOfWeekCalendar(year, weekOfYear, locale);
		int dayToday = -1;
		Calendar calNow = CalendarUtils.createCalendarInstance(locale);
		if ( (calNow.get(Calendar.WEEK_OF_YEAR) == weekOfYear)
				&& (calNow.get(Calendar.YEAR) == year) ) {
			// if we are within current week, adjust dayToday
			dayToday = calNow.get(Calendar.DAY_OF_WEEK);
		}
		
		// render day grid
		sb.append("\n<div class=\"o_cal_wv_time o_cal_wv_row0\">");
		for (int i = 0; i < 23; i++) {
			sb.append("<div class=\"o_cal_wv_legend_hour\"><div>");
			sb.append(i);
			sb.append(":00</div></div>");
		}
		sb.append("<div class=\"o_cal_wv_legend_hour o_cal_wv_lasthour\"><div>23:00</div></div></div>");
		
		// render daily grid
		// reposition calendar to start of week
		for (int i = 1; i <= 7; i++) {
			int dayOfWeekIter = cal.get(Calendar.DAY_OF_WEEK);
			sb.append("\n<div class=\"o_cal_wv_day o_cal_wv_row");
			sb.append(i);
			if (dayOfWeekIter == dayToday) {
				// current day
				sb.append(" o_cal_wv_today");
			} else if (dayOfWeekIter == Calendar.SATURDAY || dayOfWeekIter == Calendar.SUNDAY) {
				// holiday
				sb.append(" o_cal_wv_holiday");
			}
			sb.append("\">");
			
			for (int j = 0; j < 23; j++) {
				// set calendar add link to corresponding hour
				cal.set(Calendar.HOUR_OF_DAY, j);
				
				//build the add event uri
				StringOutput uri = new StringOutput();
				ubu.buildJavaScriptBgCommand(uri, new String[] {WeeklyCalendarComponent.ID_CMD, WeeklyCalendarComponent.ID_PARAM},
						new String[] {WeeklyCalendarComponent.CMD_ADD, dmyDateFormat.format(cal.getTime())},
						isIframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL, true);
				
				sb.append("<div class=\"o_cal_wv_hour\"");
				if (enableAddEvent) {
					sb.append(" onclick=\"");
					sb.append(uri);
				}
				sb.append("\"></div>");
				sb.append("<div class=\"o_cal_wv_half_hour\"");
				if (enableAddEvent) {
					sb.append(" onclick=\"");
					sb.append(uri);
				}
				sb.append("\"></div>");
			}
			// set calendar add link to last hour (23)
			cal.set(Calendar.HOUR_OF_DAY, 23);
			StringOutput uri = new StringOutput();
			ubu.buildJavaScriptBgCommand(uri, new String[] {WeeklyCalendarComponent.ID_CMD, WeeklyCalendarComponent.ID_PARAM},
					new String[] {WeeklyCalendarComponent.CMD_ADD, dmyDateFormat.format(cal.getTime())},
					isIframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL, true);
			
			sb.append("<div class=\"o_cal_wv_hour\"");
			if (enableAddEvent) {
				sb.append(" onclick=\"");
				sb.append(uri);
			}
			sb.append("\"></div>");
			sb.append("<div class=\"o_cal_wv_half_hour o_cal_wv_lasthour\"");
			if (enableAddEvent) {
				sb.append(" onclick=\"");
				sb.append(uri);
			}
			sb.append("\"></div></div>");
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}

	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		// nothing to include, css and javascript is included by calender component
	}

	/**
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		// using Ext.onReady() instead
	}

}