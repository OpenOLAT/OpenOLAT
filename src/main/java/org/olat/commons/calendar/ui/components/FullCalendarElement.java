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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIMoveEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIPrintEvent;
import org.olat.commons.calendar.ui.events.KalendarGUISelectEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;


/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarElement extends FormItemImpl {

	private FullCalendarComponent component;

	public FullCalendarElement(UserRequest ureq, String name, List<KalendarRenderWrapper> calendarWrappers,
			Translator translator, Boolean eventAlwaysVisible) {
		super(name);
		
		component = new FullCalendarComponent(ureq, this, name, calendarWrappers, translator, eventAlwaysVisible);
	}
	
	public String getMapperUrl() {
		return component.getMapperUrl();
	}

	public boolean isEventAlwaysVisible() {
		return component.isEventAlwaysVisible();
	}
	
	public Date getFocusDate() {
		return component.getCurrentDate();
	}
	
	public void setFocusDate(Date date) {
		component.setCurrentDate(date);
	}
	
	public KalendarRenderWrapper getKalendarRenderWrapper(String calendarID) {
		return component.getKalendarRenderWrapper(calendarID);
	}

	public void setKalendars(List<KalendarRenderWrapper> calendarWrappers) {
		component.setKalendars(calendarWrappers);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String selectedEventId = getRootForm().getRequestParameter("evSelect");
		String addEventMarker = getRootForm().getRequestParameter("evAdd");
		String movedEventId = getRootForm().getRequestParameter("evMove");
		String changeViewName = getRootForm().getRequestParameter("evChangeView");
		String print = getRootForm().getRequestParameter("print");
		
		String dispatchuri = getRootForm().getRequestParameter("dispatchuri");
		if("undefined".equals(dispatchuri)) {
			//to nothing
		} else if(StringHelper.containsNonWhitespace(print)) {
			String targetDomId = "fc_p" + component.getDispatchID();
			getRootForm().fireFormEvent(ureq, new KalendarGUIPrintEvent(targetDomId));
		} else if(StringHelper.containsNonWhitespace(selectedEventId)) {
			String targetDomId = getRootForm().getRequestParameter("evDomId");
			doSelect(ureq, selectedEventId, targetDomId);
		} else if(StringHelper.containsNonWhitespace(addEventMarker)) {
			String start = getRootForm().getRequestParameter("start");
			String end = getRootForm().getRequestParameter("end");
			String allDay = getRootForm().getRequestParameter("allDay");
			doAdd(ureq, start, end, allDay);
		} else if(StringHelper.containsNonWhitespace(movedEventId)) {
			String dayDelta = getRootForm().getRequestParameter("dayDelta");
			String minuteDelta = getRootForm().getRequestParameter("minuteDelta");
			String allDay = getRootForm().getRequestParameter("allDay");
			doMove(ureq, movedEventId, dayDelta, minuteDelta, allDay);
		} else if(StringHelper.containsNonWhitespace(changeViewName)) {
			String start = getRootForm().getRequestParameter("start");
			doChangeView(changeViewName, start);
		}
	}
	
	protected void doChangeView(String viewName, String start) {
		if(StringHelper.isLong(start)) {
			long startTime = Long.parseLong(start);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(startTime);
			component.setCurrentDate(cal.getTime());
		}

		if("month".equals(viewName) || "agendaWeek".equals(viewName)
				|| "agendaDay".equals(viewName) || "basicWeek".equals(viewName)
				|| "basicDay".equals(viewName)) {
			component.setViewName(viewName);
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
		
		KalendarEvent event = component.getKalendarEvent(eventId);
		KalendarRenderWrapper calWrapper = component.getKalendarRenderWrapperOf(eventId);
		getRootForm().fireFormEvent(ureq, new KalendarGUIMoveEvent(this, event, calWrapper, day, minute, allDay));
	}
	
	protected void doAdd(UserRequest ureq, String start, String end, String allDay) {
		long startTime = -1;
		if(StringHelper.isLong(start)) {
			startTime = Long.parseLong(start);
		}
		long endTime = -1;
		if(StringHelper.isLong(end)) {
			endTime = Long.parseLong(end);
		}
		boolean allDayEvent = "true".equalsIgnoreCase(allDay);
		getRootForm().fireFormEvent(ureq, new KalendarGUIAddEvent(this, null, new Date(startTime), new Date(endTime), allDayEvent));
	}
	
	protected void doSelect(UserRequest ureq, String eventId, String targetDomId) {
		KalendarEvent event = component.getKalendarEvent(eventId);
		KalendarRenderWrapper calWrapper = component.getKalendarRenderWrapperOf(eventId);
		getRootForm().fireFormEvent(ureq, new KalendarGUISelectEvent(this, event, calWrapper, targetDomId));
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

	protected FullCalendarComponent getFormItemComponent() {
		return component;
	}
}
