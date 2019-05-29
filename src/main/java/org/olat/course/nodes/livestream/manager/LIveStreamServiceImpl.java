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
package org.olat.course.nodes.livestream.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamEvent;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.model.LiveStreamEventImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LIveStreamServiceImpl implements LiveStreamService {
	
	@Autowired
	private CalendarManager calendarManager;

	@Override
	public List<? extends LiveStreamEvent> getRunningEvents(CourseCalendars calendars, int bufferBeforeMin,
			int bufferAfterMin) {
		Date now = new Date();
		Calendar cFrom = Calendar.getInstance();
		cFrom.setTime(now);
		cFrom.add(Calendar.MINUTE, -bufferAfterMin);
		Date from = cFrom.getTime();
		Calendar cTo = Calendar.getInstance();
		cTo.setTime(now);
		cTo.add(Calendar.MINUTE, bufferBeforeMin);
		Date to = cTo.getTime();
		return getLiveStreamEvents(calendars, from, to);
	}
	
	@Override
	public List<? extends LiveStreamEvent> getUpcomingEvents(CourseCalendars calendars, int bufferBeforeMin) {
		Date now = new Date();
		Calendar cFrom = Calendar.getInstance();
		cFrom.setTime(now);
		cFrom.add(Calendar.MINUTE, bufferBeforeMin);
		Date from = cFrom.getTime();
		Calendar cTo = Calendar.getInstance();
		cTo.setTime(now);
		cTo.add(Calendar.YEAR, 10);
		Date to = cTo.getTime();
		
		return getLiveStreamEvents(calendars, from, to).stream()
				.filter(notStartedFilter(from))
				.collect(Collectors.toList());
	}

	private Predicate<LiveStreamEvent> notStartedFilter(Date from) {
		return (LiveStreamEvent e) -> {
			return !e.getBegin().before(from);
			};
	}

	private List<? extends LiveStreamEvent> getLiveStreamEvents(CourseCalendars calendars, Date from, Date to) {
		List<LiveStreamEvent> liveStreamEvents = new ArrayList<>();
		for (KalendarRenderWrapper cal : calendars.getCalendars()) {
			if(cal != null) {
				boolean privateEventsVisible = cal.isPrivateEventsVisible();
				List<KalendarEvent> events = calendarManager.getEvents(cal.getKalendar(), from, to, privateEventsVisible);
				for(KalendarEvent event:events) {
					if(!privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_PRIVATE) {
						continue;
					}
					
					if (isLiveStream(event)) {
						boolean timeOnly = !privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_X_FREEBUSY;
						LiveStreamEventImpl liveStreamEvent = toLiveStreamEvent(event, timeOnly);
						liveStreamEvents.add(liveStreamEvent);
					};
				}
			}
		}
		return liveStreamEvents;
	}
	
	private boolean isLiveStream(KalendarEvent event) {
		return event.getLiveStreamUrl() != null;
	}

	private LiveStreamEventImpl toLiveStreamEvent(KalendarEvent event, boolean timeOnly) {
		LiveStreamEventImpl liveStreamEvent = new LiveStreamEventImpl();
		liveStreamEvent.setId(event.getID());
		liveStreamEvent.setBegin(event.getBegin());
		liveStreamEvent.setEnd(event.getEnd());
		liveStreamEvent.setAllDayEvent(event.isAllDayEvent());
		liveStreamEvent.setLiveStreamUrl(event.getLiveStreamUrl());
		if (!timeOnly) {
			liveStreamEvent.setSubject(event.getSubject());
			liveStreamEvent.setDescription(event.getDescription());
			liveStreamEvent.setLocation(event.getLocation());
		}
		return liveStreamEvent;
	}

}
