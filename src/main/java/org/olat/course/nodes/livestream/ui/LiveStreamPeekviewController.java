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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.LiveStreamCourseNode;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamEvent;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamPeekviewController extends BasicController implements Controller {
	
	private final VelocityContainer mainVC;
	private Link nodeLink;
	
	private final String nodeId;
	
	@Autowired
	private LiveStreamService liveStreamService;

	public LiveStreamPeekviewController(UserRequest ureq, WindowControl wControl, String nodeId,
			ModuleConfiguration moduleConfiguration, CourseCalendars calendars) {
		super(ureq, wControl);
		this.nodeId = nodeId;
		
		int bufferBeforeMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_BEFORE_MIN, 0);
		int bufferAfterMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_AFTER_MIN, 0);
		
		mainVC = createVelocityContainer("peekview");
		List<? extends LiveStreamEvent> runningEvents = liveStreamService.getRunningEvents(calendars, bufferBeforeMin, bufferAfterMin);
		List<EventWrapper> events;
		if (!runningEvents.isEmpty()) {
			Collections.sort(runningEvents, (e1, e2) -> e1.getBegin().compareTo(e2.getBegin()));
			events = wrapEvents(runningEvents, true);
			nodeLink = LinkFactory.createLink("peekview.open.live", mainVC, this);
			nodeLink.setIconRightCSS("o_icon o_icon_start");
			nodeLink.setElementCssClass("pull-right");
		} else {
			List<? extends LiveStreamEvent> upcomingEvents = liveStreamService.getUpcomingEvents(calendars, bufferBeforeMin);
			if (!upcomingEvents.isEmpty()) {
				Collections.sort(upcomingEvents, (e1, e2) -> e1.getBegin().compareTo(e2.getBegin()));
				events = new ArrayList<>(1);
				events.add(wrapEvent(upcomingEvents.get(0), false));
				nodeLink = LinkFactory.createLink("peekview.open.upcoming", mainVC, this);
				nodeLink.setIconRightCSS("o_icon o_icon_start");
				nodeLink.setElementCssClass("pull-right");
			} else {
				events = new ArrayList<>(0);
			}
		}
		mainVC.contextPut("events", events);
		
		putInitialPanel(mainVC);
	}

	private List<EventWrapper> wrapEvents(List<? extends LiveStreamEvent> events, boolean live) {
		List<EventWrapper> wrappedEvents = new ArrayList<>(events.size());
		for (LiveStreamEvent event : events) {
			wrappedEvents.add(wrapEvent(event, live));
		}
		return wrappedEvents;
	}

	private EventWrapper wrapEvent(LiveStreamEvent event, boolean live) {
		String titleI18n = live? "peekview.title.live": "peekview.title.upcoming";
		String title = translate(titleI18n, new String[] {event.getSubject()});
		
		Locale locale = getLocale();
		Date begin = event.getBegin();
		Date end = event.getEnd();
		String date = null;
		String time = null;
		
		boolean sameDay = DateUtils.isSameDay(begin, end);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
			date = dateSb.toString();
			if (!event.isAllDayEvent()) {
				StringBuilder timeSb = new StringBuilder();
				timeSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
				timeSb.append(" - ");
				timeSb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
				time = timeSb.toString();
			}
		} else {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
			if (!event.isAllDayEvent()) {
				dateSb.append(" ");
				dateSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
			}
			dateSb.append(" - ");
			dateSb.append(StringHelper.formatLocaleDateFull(end.getTime(), locale));
			if (!event.isAllDayEvent()) {
				dateSb.append(" ");
				dateSb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
			}
			date = dateSb.toString();
		}

		String location = event.getLocation();
		
		return new EventWrapper(title, date, time, location);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == nodeLink) {
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
		}
	}
	
	public final static class EventWrapper {
		
		private final String title;
		private final String date;
		private final String time;
		private final String location;

		private EventWrapper(String title, String date, String time, String location) {
			this.title = title;
			this.date = date;
			this.time = time;
			this.location = location;
		}

		public String getTitle() {
			return title;
		}

		public String getDate() {
			return date;
		}

		public String getTime() {
			return time;
		}

		public String getLocation() {
			return location;
		}
		
	}

}
