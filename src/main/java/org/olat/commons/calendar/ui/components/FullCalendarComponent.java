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

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarComponent extends AbstractComponent implements Disposable {
	
	private static final Logger log = Tracing.createLoggerFor(FullCalendarComponent.class);
	private static final FullCalendarComponentRenderer RENDERER = new FullCalendarComponentRenderer();
	private static final SimpleDateFormat occurenceDateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
	
	public static final String RECURRENCE_ID_SEP = "_xRecOOceRx_";
	public static final String OCCURRENCE_ID_SEP = "_xOccOOccOx_";

	private List<KalendarRenderWrapper> alwaysVisibleCalendars;
	private List<KalendarRenderWrapper> calendars = new ArrayList<>();
	private Date currentDate;
	private String viewName = FullCalendarViews.dayGridMonth.name();
	private boolean configurationEnabled;
	private boolean aggregatedFeedEnabled;
	private boolean differentiateManagedEvents;
	private boolean differentiateLiveStreams;
	
	private final MapperKey mapperKey;
	private final FullCalendarElement calendarEl;
	
	/**
	 * 
	 * @param name
	 * @param calendarWrappers
	 * @param viewStartHour
	 * @param translator
	 * @param eventAlwaysVisible  When true, the 'isVis()' check is disabled and events will be displayed always.
	 */
	FullCalendarComponent(UserRequest ureq, FullCalendarElement calendarEl, String name,
			Collection<KalendarRenderWrapper> calendarWrappers, Translator translator) {
		super(name, translator);
		setCurrentDate(new Date());
		calendars = new ArrayList<>(calendarWrappers);
		this.calendarEl = calendarEl;
		
		MapperService mapper = CoreSpringFactory.getImpl(MapperService.class);
		mapperKey = mapper.register(ureq.getUserSession(), new FullCalendarMapper(this));
	}
	
	@Override
	public void dispose() {
		CoreSpringFactory.getImpl(MapperService.class).cleanUp(List.of(mapperKey));
	}
	
	protected String getMapperUrl() {
		return mapperKey.getUrl();
	}
	
	protected FullCalendarElement getCalendarElement() {
		return calendarEl;
	}

	public String getViewName() {
		return viewName == null ? FullCalendarViews.dayGridMonth.name() : viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public boolean isConfigurationEnabled() {
		return configurationEnabled;
	}

	public void setConfigurationEnabled(boolean configurationEnabled) {
		this.configurationEnabled = configurationEnabled;
	}

	public boolean isAggregatedFeedEnabled() {
		return aggregatedFeedEnabled;
	}

	public void setAggregatedFeedEnabled(boolean aggregatedFeedEnabled) {
		this.aggregatedFeedEnabled = aggregatedFeedEnabled;
	}

	public boolean isDifferentiateManagedEvents() {
		return differentiateManagedEvents;
	}

	public void setDifferentiateManagedEvents(boolean differentiateManagedEvents) {
		this.differentiateManagedEvents = differentiateManagedEvents;
	}

	public boolean isDifferentiateLiveStreams() {
		return differentiateLiveStreams;
	}

	public void setDifferentiateLiveStreams(boolean differentiateLiveStreams) {
		this.differentiateLiveStreams = differentiateLiveStreams;
	}

	public List<KalendarRenderWrapper> getAlwaysVisibleCalendars() {
		return alwaysVisibleCalendars;
	}

	public void setAlwaysVisibleCalendars(List<KalendarRenderWrapper> alwaysVisibleCalendars) {
		if(alwaysVisibleCalendars == null) {
			this.alwaysVisibleCalendars = new ArrayList<>(1);
		} else {
			this.alwaysVisibleCalendars = new ArrayList<>(alwaysVisibleCalendars);
		}
	}
	
	public boolean isCalendarVisible(KalendarRenderWrapper calendar) {
		boolean alwaysVisible = calendar.isVisible();
		
		if(!alwaysVisible && alwaysVisibleCalendars != null && !alwaysVisibleCalendars.isEmpty()) {
			for(KalendarRenderWrapper alwaysVisibleCalendar:alwaysVisibleCalendars) {
				if(alwaysVisibleCalendar.getKalendar().getType().equals(calendar.getKalendar().getType())
						&& alwaysVisibleCalendar.getKalendar().getCalendarID().equals(calendar.getKalendar().getCalendarID())) {
					alwaysVisible = true;
				}
			}
		}
		
		return alwaysVisible;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		if(Settings.isDebuging()) {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/fullcalendar/main.js");
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/fullcalendar/locales-all.js");
		} else {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/fullcalendar/main.min.js");
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/fullcalendar/locales-all.min.js");
		}
	}
	
	public boolean isOccurenceOfCalendarEvent(String eventId) {
		return eventId != null && eventId.indexOf(OCCURRENCE_ID_SEP) > 0;
	}
	
	public boolean isReccurenceOfCalendarEvent(String eventId) {
		return eventId != null && eventId.indexOf(RECURRENCE_ID_SEP) > 0;
	}
	
	public String getCalendarEventUid(String eventId) {
		int occIndex = eventId.indexOf(OCCURRENCE_ID_SEP);
		if(occIndex > 0) {
			return eventId.substring(0, occIndex);
		}
		int recIndex = eventId.indexOf(RECURRENCE_ID_SEP);
		if(recIndex > 0) {
			return eventId.substring(0, recIndex);
		}
		return eventId;
	}
	
	public Date getCalendarEventOccurenceDate(String eventId) {
		Date startDate = null;
		int occIndex = eventId.indexOf(OCCURRENCE_ID_SEP);
		if(occIndex > 0) {
			String dateStr = eventId.substring(occIndex + OCCURRENCE_ID_SEP.length());
			try {
				synchronized(occurenceDateFormat) {
					startDate = occurenceDateFormat.parse(dateStr);
				}
			} catch (ParseException e) {
				log.error("Cannot parse start date of occurence: {}", dateStr, e);
			}
		}
		return startDate;
	}
	
	public String getCalendarEventOccurenceId(String eventId) {
		int occIndex = eventId.indexOf(OCCURRENCE_ID_SEP);
		if(occIndex > 0) {
			return eventId.substring(occIndex + OCCURRENCE_ID_SEP.length());
		}
		return null;
	}
	
	public KalendarEvent getCalendarEvent(String id) {
		for(KalendarRenderWrapper cal:calendars) {
			for(KalendarEvent event:cal.getKalendar().getEvents()) {
				if(id.equals(normalizeId(event))) {
					return event;
				}
			}
		}
		return null;
	}
	
	public KalendarRenderWrapper getCalendarByNormalizedId(String id) {
		for(KalendarRenderWrapper cal:calendars) {
			for(KalendarEvent event:cal.getKalendar().getEvents()) {
				if(id.equals(normalizeId(event))) {
					return cal;
				}
			}
		}
		return null;
	}
	
	public KalendarRenderWrapper getCalendarById(String id) {
		for(KalendarRenderWrapper cal:calendars) {
			for(KalendarEvent event:cal.getKalendar().getEvents()) {
				if(id.equals(normalizeId(event.getID()))) {
					return cal;
				}
			}
		}
		return null;
	}
	
	public List<KalendarEvent> getCalendarRenderWrapper(Date from, Date to) {
		List<KalendarEvent> events = new ArrayList<>();
		
		for(KalendarRenderWrapper cal:calendars) {
			for(KalendarEvent event:cal.getKalendar().getEvents()) {
				Date end = event.getEnd();
				Date begin = event.getBegin();
				if(from.compareTo(begin) <= 0 && to.compareTo(end) >= 0) {
					events.add(event);
				}
			}
		}
		
		return events;
	}

	public KalendarRenderWrapper getCalendar(String calendarID) {
		if(calendarID == null) return null;
		
		for(KalendarRenderWrapper cal:calendars) {
			if(calendarID.equals(cal.getKalendar().getCalendarID())) {
				return cal;
			}
		}
		return null;
	}
	
	public List<KalendarRenderWrapper> getCalendars() {
		return calendars;
	}

	public void setCalendars(List<KalendarRenderWrapper> calendarWrappers) {
		calendars = new ArrayList<>(calendarWrappers);
		setDirty(true);
	}
	
	public void addCalendar(KalendarRenderWrapper calendarWrapper) {
		calendars.add(calendarWrapper);
		setDirty(true);
	}
	
	protected static final String normalizeId(KalendarEvent kEvent) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(normalizeId(kEvent.getID()));
		if(kEvent.getRecurrenceID() != null) {
			sb.append(RECURRENCE_ID_SEP);
			sb.append(normalizeId(kEvent.getRecurrenceID()));
		} else if(kEvent instanceof KalendarRecurEvent) {
			sb.append(OCCURRENCE_ID_SEP);
			String subIdent;
			synchronized(occurenceDateFormat) {
				subIdent = occurenceDateFormat.format(kEvent.getBegin());
			}
			sb.append(normalizeId(subIdent));
		}
		return sb.toString();
	}
	
	protected static final String normalizeId(String id) {
		return Normalizer.normalize(id, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+","")
				.replaceAll("\\W+", "");
	}
}
