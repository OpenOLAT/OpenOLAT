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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 09.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullCalendarComponent extends AbstractComponent {
	
	private static final FullCalendarComponentRenderer RENDERER = new FullCalendarComponentRenderer();

	private List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
	private Date currentDate;
	private String viewName = "month";
	private boolean configurationEnabled;
	private boolean aggregatedFeedEnabled;
	
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
		calendars = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		this.calendarEl = calendarEl;
		
		MapperService mapper = CoreSpringFactory.getImpl(MapperService.class);
		mapperKey = mapper.register(ureq.getUserSession(), new FullCalendarMapper(this));
	}
	
	protected String getMapperUrl() {
		return mapperKey.getUrl();
	}
	
	protected FullCalendarElement getCalendarElement() {
		return calendarEl;
	}

	public String getViewName() {
		return viewName == null ? "month" : viewName;
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

	/**
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	/**
	 * @see org.olat.core.gui.components.Component#validate(org.olat.core.gui.UserRequest, org.olat.core.gui.render.ValidationResult)
	 */
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/fullcalendar/fullcalendar.min.js");
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.dnd.min.js");
	}
	
	public KalendarEvent getCalendarEvent(String id) {
		for(KalendarRenderWrapper cal:calendars) {
			for(KalendarEvent event:cal.getKalendar().getEvents()) {
				if(id.equals(normalizeId(event.getID()))) {
					return event;
				}
			}
		}
		return null;
	}
	
	public KalendarRenderWrapper getCalendarByNormalizedId(String id) {
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
		List<KalendarEvent> events = new ArrayList<KalendarEvent>();
		
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
		calendars = new ArrayList<KalendarRenderWrapper>(calendarWrappers);
		setDirty(true);
	}
	
	public void addCalendar(KalendarRenderWrapper calendarWrapper) {
		calendars.add(calendarWrapper);
		setDirty(true);
	}
	
	protected static final String normalizeId(String id) {
		String normalizedId = Normalizer.normalize(id, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+","")
				.replaceAll("\\W+", "");
		return normalizedId;
	}
}
