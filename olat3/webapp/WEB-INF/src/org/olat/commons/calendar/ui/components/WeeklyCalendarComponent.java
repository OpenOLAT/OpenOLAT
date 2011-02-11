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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIEditEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;

public class WeeklyCalendarComponent extends Component {

	public static final String ID_CMD = "cmd";
	public static final String ID_PARAM = "p";
	public static final String ID_PARAM_SEPARATOR = "§";
	
	public static final String CMD_ADD = "add";
	public static final String CMD_EDIT = "edt";
	public static final String CMD_ADD_ALLDAY = "add_allday";

	private Map kalendars = new HashMap();
	private int year;
	private int weekOfYear;
	private int displayDays = 7;
	private int viewStartHour = 7;
	private boolean eventAlwaysVisible;
	
	/**
	 * 
	 * @param name
	 * @param calendarWrappers
	 * @param viewStartHour
	 * @param translator
	 * @param eventAlwaysVisible  When true, the 'isVis()' check is disabled and events will be displayed always.
	 */
	public WeeklyCalendarComponent(String name, Collection calendarWrappers, int viewStartHour, Translator translator, Boolean eventAlwaysVisible) {
		super(name, translator);
		this.viewStartHour = viewStartHour;
		this.eventAlwaysVisible = eventAlwaysVisible;
		setDate(new Date());
		setKalendars(calendarWrappers);
	}

	/**
	 * Set this calendars focus to year/weekOfYear.
	 * 
	 * @param year
	 * @param weekOfYear
	 */
	public void setFocus(int year, int weekOfYear) {
		this.year = year;
		this.weekOfYear = weekOfYear;
		setDirty(true);
	}
	
	/**
	 * Set how many days from the beginning of a week should
	 * be displayd (e.g. 7=sholw week; 5=MO-FI)
	 * 
	 * @param displayDays
	 */
	public void setDisplayDays(int displayDays) {
		this.displayDays = displayDays;
	}

	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		String command = ureq.getParameter(ID_CMD);
		if (command == null) return;
		else if (command.equals(CMD_EDIT)) {
			String param = ureq.getParameter(ID_PARAM);
			StringTokenizer st = new StringTokenizer(param, ID_PARAM_SEPARATOR, false);
			if (st.countTokens() != 3) return;
			String calendarID = st.nextToken();
			String eventID = st.nextToken();
			KalendarRenderWrapper kalendarWrapper = (KalendarRenderWrapper)kalendars.get(calendarID);
			KalendarEvent event = kalendarWrapper.getKalendar().getEvent(eventID);
			KalendarEvent recurEvent = null;
			CalendarManager cm = CalendarManagerFactory.getInstance().getCalendarManager();
			Long time = Long.parseLong(st.nextToken());
			Date dateStart = new Date(time);
			Calendar cal = CalendarUtils.createCalendarInstance(ureq.getLocale());
			cal.setTime(dateStart);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			Date dateEnd = cal.getTime();
			recurEvent = cm.getRecurringInPeriod(dateStart, dateEnd, event);
			setDirty(true);
			fireEvent(ureq, new KalendarGUIEditEvent(recurEvent != null ? recurEvent : event, kalendarWrapper));
		} else if ( command.equals(CMD_ADD) || command.equals(CMD_ADD_ALLDAY) ) {
			// this will get us the day of the year
			String sDate = ureq.getParameter(ID_PARAM);
			Date timeDate = new Date();
			try {
				DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmm");
				timeDate = dateFormat.parse(sDate);
			} catch (ParseException pe) {
				// ok, already initialized
			}
			// find first available writeable calendar
			// PRE: component renderer makes sure, at least one writeable calendar exists.
			KalendarRenderWrapper calendarWrapper = null;
			for (Iterator iter = getKalendars().iterator(); iter.hasNext();) {
				calendarWrapper = (KalendarRenderWrapper)iter.next();
				if (calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) break;
			}
			setDirty(true);
			fireEvent(ureq, new KalendarGUIAddEvent(calendarWrapper.getKalendar().getCalendarID(), timeDate, command.equals(CMD_ADD_ALLDAY)));
		}
	}

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	public ComponentRenderer getHTMLRendererSingleton() {
		return new WeeklyCalendarComponentRenderer(viewStartHour);
	}

	Collection getKalendars() {
		return kalendars.values();
	}

	public KalendarRenderWrapper getKalendarRenderWrapper(String calendarID) {
		return (KalendarRenderWrapper)kalendars.get(calendarID);
	}
	
	public void setKalendars(Collection kalendarRenderWrappers) {
		this.kalendars = new HashMap();
		for (Iterator iter = kalendarRenderWrappers.iterator(); iter.hasNext();) {
			KalendarRenderWrapper kalendarRenderWrapper = (KalendarRenderWrapper) iter.next();
			this.kalendars.put(kalendarRenderWrapper.getKalendar().getCalendarID(), kalendarRenderWrapper);
		}
	}

	public int getDisplayDays() {
		return displayDays;
	}

	public int getWeekOfYear() {
		return weekOfYear;
	}

	public int getYear() {
		return year;
	}


	/**
	 * @see org.olat.core.gui.components.Component#validate(org.olat.core.gui.UserRequest, org.olat.core.gui.render.ValidationResult)
	 */
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredJsFile(CalendarManager.class, "js/calendar.js");
		jsa.addRequiredCSSFile(CalendarManager.class, "css/calendar.css", false);
	}

	/**
	 * Go back to previous week.
	 */
	public void previousWeek() {
		// wrap this call because junit tests must set the calender with different values
		previousWeek(CalendarUtils.createCalendarInstance(getTranslator().getLocale()));
	}
	
	protected void previousWeek(Calendar cal) {
		cal.set(Calendar.YEAR, getYear());
		cal.set(Calendar.WEEK_OF_YEAR, getWeekOfYear());
		int lastWeekOfYear = getWeekOfYear();
		int lastYear = getYear();
		cal.add(Calendar.WEEK_OF_YEAR, -1);
		// year change must correspond with weekOfYear 
		// problem java-calendar is daily based,
		// weeks 52,53,1 can include days from old and new year
		if ( (lastWeekOfYear == 2) 
			  && (cal.get(Calendar.YEAR) != lastYear) ) {
			setFocus(lastYear, cal.get(Calendar.WEEK_OF_YEAR));
		} else if ((cal.get(Calendar.WEEK_OF_YEAR) == 53)
				|| ((cal.get(Calendar.WEEK_OF_YEAR) == 52) && (lastWeekOfYear != 53))
				&& (lastYear == cal.get(Calendar.YEAR)) ) {
			setFocus(lastYear - 1, cal.get(Calendar.WEEK_OF_YEAR));
		}	else {
			setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
		}		
	}

	/**
	 * Go back to next week.
	 */
	public void nextWeek() {
		// wrap this call because junit tests must set the calender with different values 
		nextWeek(CalendarUtils.createCalendarInstance(getTranslator().getLocale()));
	}

	protected void nextWeek(Calendar cal) {
		cal.set(Calendar.YEAR, getYear());
		cal.set(Calendar.WEEK_OF_YEAR, getWeekOfYear());
		int lastYear = getYear();
		Tracing.logInfo("nextWeek (1): getYear()=" + getYear() + "  getWeekOfYear()=" + getWeekOfYear(), this.getClass());
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		Tracing.logInfo("nextWeek (2): cal.get(Calendar.WEEK_OF_YEAR)=" + cal.get(Calendar.WEEK_OF_YEAR) + "  cal.get(Calendar.YEAR)=" + cal.get(Calendar.YEAR), this.getClass());
		
		// year change must correspond with weekOfYear 
		// problem java-calendar is daily based,
		// weeks 52,53,1 can include days from old and new year
		if ( (cal.get(Calendar.WEEK_OF_YEAR) == 1) 
			  && (cal.get(Calendar.YEAR) == lastYear) ) {
			Tracing.logInfo("nextWeek (3): case 1 setFocus(lastYear + 1, cal.get(Calendar.WEEK_OF_YEAR))", this.getClass());
			setFocus(lastYear + 1, cal.get(Calendar.WEEK_OF_YEAR));
		} else if ((cal.get(Calendar.WEEK_OF_YEAR) == 53)
				|| (cal.get(Calendar.WEEK_OF_YEAR) == 52)) {
			Tracing.logInfo("nextWeek (4): case 2 setFocus(lastYear, cal.get(Calendar.WEEK_OF_YEAR))", this.getClass());
			setFocus(lastYear, cal.get(Calendar.WEEK_OF_YEAR));
	  } else {
	  	Tracing.logInfo("nextWeek (5): case 3 setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))", this.getClass());
			setFocus(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR));
		}
	}

	/**
	 * Set focus of calendar-component to certain date.
	 * Calculate correct week-of-year and year for certain date.
	 * 
	 * @param gotoDate
	 */
	public void setDate(Date gotoDate) {
		Calendar cal = CalendarUtils.createCalendarInstance(getTranslator().getLocale());
		cal.setTime(gotoDate);
		int weekYear = cal.get(Calendar.YEAR);
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		if (week == 1) {
			// Week 1 is a special case: the date could be the last days of december, but the week is still counted as week one of the next year. Use the next year in this case to match the week number.
			if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) {
				weekYear++;
			}
		} else if (week >= 52) {
			// Opposite check: date could be first days of january, but the week is still counted as the last week of the passed year. Use the last year in this case to match the week number.
			if (cal.get(Calendar.MONTH) == Calendar.JANUARY) {
				weekYear--;
			}			
		}
 		setFocus(weekYear, week);
	}

	/**
	 * Returns true when events should be visible always (renderer does not check isVis() )
	 * @return
	 */
	public boolean isEventAlwaysVisible() {
		return eventAlwaysVisible;
	}
	
}
