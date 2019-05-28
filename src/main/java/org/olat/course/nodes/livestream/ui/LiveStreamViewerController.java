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
package org.olat.course.nodes.livestream.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.LiveStreamCourseNode;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 24 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamViewerController extends BasicController {

	private final VelocityContainer mainVC;
	
	private final CourseCalendars calendars;
	private final int bufferBeforeMin;
	private final int bufferAfterMin;
	
	public LiveStreamViewerController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration,
			CourseCalendars calendars) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));
		this.calendars = calendars;
		
		bufferBeforeMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_BEFORE_MIN, 0);
		bufferAfterMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_AFTER_MIN, 0);
		
		mainVC = createVelocityContainer("viewer");
		KalendarEvent liveStreamEvent = getActiveLiveStream();
		if (liveStreamEvent == null) {
			mainVC.contextPut("noLiveStream", Boolean.TRUE);
		} else {
			mainVC.contextPut("id", CodeHelper.getRAMUniqueID());
			mainVC.contextPut("src", liveStreamEvent.getLiveStreamUrl());
			mainVC.contextPut("title", liveStreamEvent.getSubject());
			addDateToMainVC(liveStreamEvent);
			StringBuilder description = Formatter.stripTabsAndReturns(Formatter.formatURLsAsLinks(liveStreamEvent.getDescription()));
			mainVC.contextPut("description", description.toString());
			if (StringHelper.containsNonWhitespace(liveStreamEvent.getLocation())) {
				mainVC.contextPut("location", liveStreamEvent.getLocation());
			}
		}
		
		putInitialPanel(mainVC);
	}
	
	private KalendarEvent getActiveLiveStream() {
		for (KalendarRenderWrapper calendar : calendars.getCalendars()) {
			Kalendar cal = calendar.reloadKalendar();
			Collection<KalendarEvent> events = cal.getEvents();
			for (KalendarEvent event : events) {
				if (isActiveLiveStream(event)) {
					return event;
				}
			}
		}
		return null;
	}

	private boolean isActiveLiveStream(KalendarEvent event) {
		if (event.getLiveStreamUrl() == null) return false;
		
		Instant now = Instant.now();
		Instant startWithBuffer = event.getBegin().toInstant().minus(Duration.of(bufferBeforeMin, ChronoUnit.MINUTES));
		Instant endWithBuffer = event.getEnd().toInstant().plus(Duration.of(bufferAfterMin, ChronoUnit.MINUTES));
		return now.isAfter(startWithBuffer) && now.isBefore(endWithBuffer);
	}
	
	private String addDateToMainVC(KalendarEvent calEvent) {
		Locale locale = getLocale();
		Calendar cal = CalendarUtils.createCalendarInstance(locale);
		Date begin = calEvent.getBegin();
		Date end = calEvent.getEnd();	
		cal.setTime(begin);
		
		StringBuilder sb = new StringBuilder();
		sb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
		mainVC.contextPut("date", sb.toString());

		if (!calEvent.isAllDayEvent()) {
			sb = new StringBuilder();
			sb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
			sb.append(" - ");
			if (!DateUtils.isSameDay(begin, end)) {
				sb.append(StringHelper.formatLocaleDateFull(end.getTime(), locale)).append(", ");
			} 
			sb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
			mainVC.contextPut("time", sb.toString());
		}
		return sb.toString();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
