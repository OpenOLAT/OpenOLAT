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
package org.olat.commons.calendar.ui.components;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.FullCalendarComponent.CalendarEventId;
import org.olat.commons.calendar.ui.events.CalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIFormEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIMoveEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIPrintEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIResizeEvent;
import org.olat.commons.calendar.ui.events.CalendarGUISelectEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;


/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarElement extends FormItemImpl implements Disposable {
	
	private static final Logger log = Tracing.createLoggerFor(FullCalendarElement.class);

	private final FullCalendarComponent component;

	public FullCalendarElement(UserRequest ureq, String name,
			List<KalendarRenderWrapper> calendarWrappers, Translator translator) {
		super(name);
		
		component = new FullCalendarComponent(ureq, this, name, calendarWrappers, translator);
	}
	
	@Override
	public void dispose() {
		component.dispose();
	}

	public String getMapperUrl() {
		return component.getMapperUrl();
	}
	
	public Date getFocusDate() {
		return component.getCurrentDate();
	}
	
	public void setFocusDate(Date date) {
		component.setCurrentDate(date);
	}
	
	public void setView(FullCalendarViews view) {
		component.setViewName(view.name());
	}
	
	public boolean isConfigurationEnabled() {
		return component.isConfigurationEnabled();
	}

	public void setConfigurationEnabled(boolean configurationEnabled) {
		component.setConfigurationEnabled(configurationEnabled);
	}
	
	public boolean isAggregatedFeedEnabled() {
		return component.isAggregatedFeedEnabled();
	}

	public void setAggregatedFeedEnabled(boolean aggregatedFeedEnabled) {
		component.setAggregatedFeedEnabled(aggregatedFeedEnabled);
	}
	
	public boolean isDifferentiateManagedEvents() {
		return component.isDifferentiateManagedEvents();
	}

	public void setDifferentiateManagedEvents(boolean differentiateManagedEvents) {
		component.setDifferentiateManagedEvents(differentiateManagedEvents);
	}
	
	public boolean isDifferentiateLiveStreams() {
		return component.isDifferentiateLiveStreams();
	}

	public void setDifferentiateLiveStreams(boolean differentiateLiveStreams) {
		component.setDifferentiateLiveStreams(differentiateLiveStreams);
	}
	
	public KalendarRenderWrapper getCalendar(String calendarID) {
		return component.getCalendar(calendarID);
	}

	public void setCalendars(List<KalendarRenderWrapper> calendarWrappers) {
		component.setCalendars(calendarWrappers);
	}
	
	public void addCalendar(KalendarRenderWrapper calendarWrapper) {
		component.addCalendar(calendarWrapper);
	}
	
	public List<KalendarRenderWrapper> getAlwaysVisibleCalendars() {
		return component.getAlwaysVisibleCalendars();
	}

	public void setAlwaysVisibleCalendars(List<KalendarRenderWrapper> alwaysVisibleCalendars) {
		component.setAlwaysVisibleCalendars(alwaysVisibleCalendars);
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String selectedEventId = getRootForm().getRequestParameter("evSelect");
		String addEventMarker = getRootForm().getRequestParameter("evAdd");
		String movedEventId = getRootForm().getRequestParameter("evMove");
		String resizedEventId = getRootForm().getRequestParameter("evResize");
		String changeViewName = getRootForm().getRequestParameter("evChangeView");
		String changeDates = getRootForm().getRequestParameter("evChangeDates");
		String print = getRootForm().getRequestParameter("print");
		String config = getRootForm().getRequestParameter("config");
		String aggregate = getRootForm().getRequestParameter("aggregate");
		
		String dispatchuri = getRootForm().getRequestParameter("dispatchuri");
		if("undefined".equals(dispatchuri)) {
			//to nothing
		} else if(StringHelper.containsNonWhitespace(print)) {
			String targetDomId = "fc_p" + component.getDispatchID();
			getRootForm().fireFormEvent(ureq, new CalendarGUIPrintEvent(this, targetDomId));
		} else if(StringHelper.containsNonWhitespace(config)) {
			String targetDomId = "fc_x" + component.getDispatchID();
			getRootForm().fireFormEvent(ureq, new CalendarGUIFormEvent(CalendarGUIFormEvent.CONFIGURE, this, targetDomId));
		} else if(StringHelper.containsNonWhitespace(aggregate)) {
			String targetDomId = "fc_g" + component.getDispatchID();
			getRootForm().fireFormEvent(ureq, new CalendarGUIFormEvent(CalendarGUIFormEvent.AGGREGATED_FEED, this,  targetDomId));
		} else if(StringHelper.containsNonWhitespace(selectedEventId)) {
			String targetDomId = getRootForm().getRequestParameter("evDomId");
			doSelect(ureq, selectedEventId, targetDomId);
		} else if(StringHelper.containsNonWhitespace(addEventMarker)) {
			String allDay = getRootForm().getRequestParameter("allDay");
			String start = getRootForm().getRequestParameter("start");
			doAdd(ureq, start, allDay);
		} else if(StringHelper.containsNonWhitespace(movedEventId)) {
			String dayDelta = getRootForm().getRequestParameter("dayDelta");
			String minuteDelta = getRootForm().getRequestParameter("minuteDelta");
			String allDay = getRootForm().getRequestParameter("allDay");
			doMove(ureq, movedEventId, dayDelta, minuteDelta, allDay);
		} else if(StringHelper.containsNonWhitespace(resizedEventId)) {
			String minuteDelta = getRootForm().getRequestParameter("minuteDelta");
			String allDay = getRootForm().getRequestParameter("allDay");
			doResize(ureq, resizedEventId, minuteDelta, allDay);
		} else if(StringHelper.containsNonWhitespace(changeViewName)) {
			doChangeView(changeViewName);
			String start = getRootForm().getRequestParameter("start");
			if(StringHelper.containsNonWhitespace(start)) {
				doSetCurrentDate(start);
			}
			if(StringHelper.containsNonWhitespace(changeDates)) {
				doSetCurrentDate(changeDates);
			}
		} else if(StringHelper.containsNonWhitespace(changeDates)) {
			doSetCurrentDate(changeDates);
		}
	}
	
	protected void doChangeView(String viewName) {
		if(FullCalendarViews.exists(viewName)) {
			component.setViewName(viewName);
		}
	}
	
	protected void doSetCurrentDate(String start) {
		if(StringHelper.isLong(start)) {
			long startTime = Long.parseLong(start);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(startTime);
			component.setCurrentDate(cal.getTime());
		} else if(start != null && start.indexOf('-') >= 0) {
			try {
				Date startDate = CalendarUtils.parseISO8601(start);
				component.setCurrentDate(startDate);
			} catch (ParseException e) {
				log.debug("",  e);
			}
		}
	}
	
	protected void doMove(UserRequest ureq, String eventId, String dayDelta, String minuteDelta, String allDayStr) {
		Long day = null;
		if(StringHelper.isLong(dayDelta)) {
			day = Long.parseLong(dayDelta);
		}
		Long minute = null;
		if(StringHelper.isLong(minuteDelta)) {
			minute = Long.parseLong(minuteDelta);
		}
		
		Boolean allDay = null;
		if("true".equals(allDayStr)) {
			allDay = Boolean.TRUE;
		} else if("false".equals(allDayStr)) {
			allDay = Boolean.FALSE;
		}
		
		if(component.isOccurenceOfCalendarEvent(eventId) || component.isReccurenceOfCalendarEvent(eventId)) {
			CalendarEventId uid = component.getCalendarEventUid(eventId);
			KalendarRenderWrapper cal = component.getCalendarById(uid);
			KalendarRecurEvent rEvent = getCurrenceKalendarEvent(cal, eventId);
			getRootForm().fireFormEvent(ureq, new CalendarGUIMoveEvent(this, rEvent, cal, day, minute, allDay));
		} else {
			KalendarEvent event = component.getCalendarEvent(eventId);
			KalendarRenderWrapper calWrapper = component.getCalendarByNormalizedId(eventId);
			getRootForm().fireFormEvent(ureq, new CalendarGUIMoveEvent(this, event, calWrapper, day, minute, allDay));
		}
	}
	
	protected void doResize(UserRequest ureq, String eventId, String minuteDelta, String allDayStr) {
		Long minute = null;
		if(StringHelper.isLong(minuteDelta)) {
			minute = Long.parseLong(minuteDelta);
		}
		
		Boolean allDay = null;
		if("true".equals(allDayStr)) {
			allDay = Boolean.TRUE;
		} else if("false".equals(allDayStr)) {
			allDay = Boolean.FALSE;
		}
		
		if(component.isOccurenceOfCalendarEvent(eventId) || component.isReccurenceOfCalendarEvent(eventId)) {
			CalendarEventId uid = component.getCalendarEventUid(eventId);
			KalendarRenderWrapper cal = component.getCalendarById(uid);
			KalendarRecurEvent rEvent = getCurrenceKalendarEvent(cal, eventId);
			getRootForm().fireFormEvent(ureq, new CalendarGUIResizeEvent(this, rEvent, cal, minute, allDay));
		} else {
			KalendarEvent event = component.getCalendarEvent(eventId);
			KalendarRenderWrapper calWrapper = component.getCalendarByNormalizedId(eventId);
			getRootForm().fireFormEvent(ureq, new CalendarGUIResizeEvent(this, event, calWrapper, minute, allDay));
		}
	}
	
	private void doAdd(UserRequest ureq, String start, String allDay) {
		try {
			boolean allDayEvent = "true".equalsIgnoreCase(allDay);
			
			Date startDate = null;
			if(StringHelper.containsNonWhitespace(start)) {
				startDate = CalendarUtils.parseISO8601(start);
			}
			Date endDate = null;
			if(FullCalendarViews.dayGridMonth.name().equals(component.getViewName()) || allDayEvent) {
				endDate = CalendarUtils.parseISO8601(start);
			} else {
				Calendar cal = Calendar.getInstance();
				cal.setTime(startDate);
				cal.add(Calendar.HOUR_OF_DAY, 1);
				endDate = cal.getTime();
			}
			getRootForm().fireFormEvent(ureq, new CalendarGUIAddEvent(this, null, startDate, endDate, allDayEvent));
		} catch (ParseException e) {
			log.error("", e);
		}
	}
	
	private void doSelect(UserRequest ureq, String eventId, String targetDomId) {
		KalendarEvent event = component.getCalendarEvent(eventId);
		
		if(component.isOccurenceOfCalendarEvent(eventId) || component.isReccurenceOfCalendarEvent(eventId)) {
			CalendarEventId uid = component.getCalendarEventUid(eventId);
			KalendarRenderWrapper cal = component.getCalendarById(uid);
			KalendarRecurEvent recurEvent = getCurrenceKalendarEvent(cal, eventId);
			getRootForm().fireFormEvent(ureq, new CalendarGUISelectEvent(this, recurEvent, cal, targetDomId));
		} else {
			KalendarRenderWrapper calWrapper = component.getCalendarByNormalizedId(eventId);
			getRootForm().fireFormEvent(ureq, new CalendarGUISelectEvent(this, event, calWrapper, targetDomId));
		}
	}
	
	private KalendarRecurEvent getCurrenceKalendarEvent(KalendarRenderWrapper cal, String eventId) {
		boolean privateEventsVisible = cal.isPrivateEventsVisible();
		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		Date currentDate = component.getCurrentDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.add(Calendar.MONTH, -12);
		Date from = calendar.getTime();
		calendar.add(Calendar.MONTH, +36);
		Date to = calendar.getTime();
		
		List<KalendarEvent> events = calendarManager.getEvents(cal.getKalendar(), from, to, privateEventsVisible);
		for(KalendarEvent event:events) {
			if(event instanceof KalendarRecurEvent) {
				KalendarRecurEvent kEvent = (KalendarRecurEvent)event;
				if(eventId.equals(FullCalendarComponent.normalizeId(cal, event))) {
					return kEvent;
				}
			}
		}
		return null;
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		//static text must not validate
	}

	@Override
	public void reset() {
		//
	}
	
	/**
	 * Prevent parent to be set as dirty for every request
	 */
	@Override
	public boolean isInlineEditingElement() {
		return true;
	}

	@Override
	protected void rootFormAvailable() {
		//root form not interesting for Static text
	}

	@Override
	protected FullCalendarComponent getFormItemComponent() {
		return component;
	}
}
