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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(FullCalendarMapper.class);
	private static final DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private final FullCalendarComponent fcC;
	private final CalendarManager calendarManager;
	
	public FullCalendarMapper(FullCalendarComponent fcC) {
		this.fcC = fcC;
		calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
	}

	/**
	 * [{
	 * 	"id":111,
	 * 	"title":"Event1",
	 *  "start":"2013-03-10",
	 *  "url":"http:\/\/yahoo.com\/"
	 * },{
	 *  "id":222,
	 *  "title":"Event2",
	 *  "start":"2013-03-20",
	 *  "end":"2013-03-22",
	 *  "url":"http:\/\/yahoo.com\/"
	 * }]
	 */
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		try {
			JSONArray ja = new JSONArray();
			
			String calendarId = getCalendarID(request);
			String start = request.getParameter("start");
			String end = request.getParameter("end");
			
			Date startDate = null;
			if(StringHelper.isLong(start)) {
				long startTime = Long.parseLong(start);
				startDate = new Date(startTime * 1000);
			} else if(StringHelper.containsNonWhitespace(start)) {
				startDate = parseDate(start);
			}
			Date endDate = null;
			if(StringHelper.isLong(end)) {
				long time = Long.parseLong(end);
				endDate = new Date(time * 1000);
			} else if(StringHelper.containsNonWhitespace(end)) {
				endDate = parseDate(end);
			}
			
			collectKalendarEvents(ja, calendarId, startDate, endDate);
			return new JSONMediaResource(ja, "UTF-8");
		} catch (JSONException e) {
			log.error("", e);
			return null;
		}
	}
	
	private String getCalendarID(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String calendarId = uri;
		int index = calendarId.lastIndexOf('/');
		if(index > 0) {
			calendarId = calendarId.substring(index + 1);
		}
		if(calendarId.endsWith(".json")) {
			calendarId = calendarId.substring(0, calendarId.length() - 5);
		}
		return calendarId;
	}
	
	private void collectKalendarEvents(JSONArray ja, String calendarId, Date from, Date to) throws JSONException {
		KalendarRenderWrapper cal =  fcC.getCalendar(calendarId);
		if(cal != null) {
			boolean privateEventsVisible = cal.isPrivateEventsVisible();
			List<KalendarEvent> events = calendarManager.getEvents(cal.getKalendar(), from, to, privateEventsVisible);

			for(KalendarEvent event:events) {
				if(!privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_PRIVATE) {
					continue;
				}
				
				boolean timeOnly = !privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_X_FREEBUSY;
				JSONObject jsonEvent = getJSONEvent(event, cal, timeOnly);
				ja.put(jsonEvent);
			}
		}
	}
	
	private JSONObject getJSONEvent(KalendarEvent event, KalendarRenderWrapper cal, boolean timeOnly)
	throws JSONException {
		JSONObject jsonEvent = new JSONObject();
		String id = FullCalendarComponent.normalizeId(event);
		jsonEvent.put("id", id);
		if(timeOnly) {
			jsonEvent.put("title", "");
		} else {
			jsonEvent.put("title", event.getSubject());
		}
		boolean allDay = event.isAllDayEvent();
		jsonEvent.put("allDay", Boolean.valueOf(allDay));
		
		if(fcC.isDifferentiateManagedEvents()) {
			applyManagedClassNames(jsonEvent, event, cal);
		} else if(StringHelper.containsNonWhitespace(cal.getCssClass())) {
			applyClassNames(jsonEvent, cal);
		}
		if(fcC.isDifferentiateLiveStreams()) {
			applyLiveStreamClass(jsonEvent, event);
		}
		
		Boolean editable = Boolean.valueOf(cal.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE)
				&& !CalendarManagedFlag.isManaged(event, CalendarManagedFlag.dates);
		jsonEvent.put("editable", editable);
		
		if(event.getBegin() != null) {
			if(allDay) {
				jsonEvent.put("start", formatDate(event.getBegin()));
			} else {
				jsonEvent.put("start", formatDateTime(event.getBegin()));
			}
		}
		if(event.getEnd() != null) {
			if(allDay) {
				Calendar calEnd = Calendar.getInstance();
				calEnd.setTime(event.getEnd());
				calEnd.add(Calendar.DATE, 1);
				jsonEvent.put("end", formatDate(calEnd.getTime()));
			} else {
				jsonEvent.put("end", formatDateTime(event.getEnd()));
			}
		}
		if(event.getLocation() != null) {
			jsonEvent.put("location", event.getLocation());
		}
		
		return jsonEvent;
	}
	
	private void applyClassNames(JSONObject jsonEvent, KalendarRenderWrapper cal)
	throws JSONException {
		jsonEvent.put("className", cal.getCssClass());
	}
	
	private void applyManagedClassNames(JSONObject jsonEvent, KalendarEvent event, KalendarRenderWrapper cal)
	throws JSONException {
		StringBuilder classNames = new StringBuilder(32);
		if(StringHelper.containsNonWhitespace(cal.getCssClass())) {
			if(cal.getKalendar().hasManagedEvents() && !event.isManaged()) {
				// Overrides calendar color
				jsonEvent.put("color", "steelblue");
			}
		} 
		
		classNames.append(cal.getCssClass());
		if(event.isManaged()) {
			classNames.append(" o_cal_event_managed");
		} else {
			classNames.append(" o_cal_event_not_managed");
		}
		jsonEvent.put("className", classNames.toString());
	}

	private void applyLiveStreamClass(JSONObject jsonEvent, KalendarEvent event) throws JSONException {
		if(StringHelper.containsNonWhitespace(event.getLiveStreamUrl())) {
			Object classNamesObj = jsonEvent.get("className");
			if (classNamesObj instanceof String) {
				String className = (String) classNamesObj;
				jsonEvent.put("className", className + " o_cal_event_livestream");
			} else {
				jsonEvent.put("className", "o_cal_event_livestream");
			}
		}
	}
	
	private String formatDateTime(Date date) {
		synchronized(formatDateTime) {
			return formatDateTime.format(date);
		}
	}
	
	private String formatDate(Date date) {
		synchronized(formatDate) {
			return formatDate.format(date);
		}
	}
	
	private Date parseDate(String date) {
		try {
			synchronized(formatDate) {
				return formatDate.parse(date);
			}
		} catch (ParseException e) {
			log.error("Cannot parse Fullcalendar date: {}", date, e);
			return null;
		}
	}
}
