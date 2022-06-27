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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.run.calendar;

import java.util.List;

import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.cal.CalSecurityCallback;
import org.olat.course.nodes.cal.CalSecurityCallbackFactory;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.userview.UserCourseEnvironment;

public class CourseCalendarController extends BasicController {

	private CalendarController calendarController;

	private CalSecurityCallback secCallback;
	private final UserCourseEnvironment userCourseEnv;
	
	public CourseCalendarController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		secCallback = CalSecurityCallbackFactory.createCourseCalendarCallback(userCourseEnv);

		CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, wControl, userCourseEnv, secCallback);
		List<KalendarRenderWrapper> calendars = myCal.getCalendars();
		calendarController = new WeeklyCalendarController(ureq, wControl, calendars, CalendarController.CALLER_COURSE,
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource(), false);
		calendarController.setDifferentiateManagedEvent(CourseCalendars.needToDifferentiateManagedEvents(calendars));
		listenTo(calendarController);
		putInitialPanel(calendarController.getInitialComponent());
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			CourseCalendars myCal = CourseCalendars.createCourseCalendarsWrapper(ureq, getWindowControl(), userCourseEnv, secCallback);
			List<KalendarRenderWrapper> calendars = myCal.getCalendars();
			calendarController.setCalendars(calendars);
		}
	}

}
