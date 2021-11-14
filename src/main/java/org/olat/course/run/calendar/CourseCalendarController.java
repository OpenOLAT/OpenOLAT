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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
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
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseCalendarController extends BasicController {

	private CalendarController calendarController;
	
	private final UserCourseEnvironment userCourseEnv;

	@Autowired
	private CalendarManager calendarManager;
	
	public CourseCalendarController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		List<KalendarRenderWrapper> calendars = getListOfCalendarWrappers(ureq);
		calendarController = new WeeklyCalendarController(ureq, wControl, calendars, CalendarController.CALLER_COURSE,
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource(), false);
		calendarController.setDifferentiateManagedEvent(CourseCalendars.needToDifferentiateManagedEvents(calendars));
		listenTo(calendarController);
		putInitialPanel(calendarController.getInitialComponent());
	}

	private List<KalendarRenderWrapper> getListOfCalendarWrappers(UserRequest ureq) {
		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		// add course calendar
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		KalendarRenderWrapper courseKalendarWrapper = calendarManager.getCourseCalendar(course);
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		boolean isPrivileged = isPrivileged();
		if (isPrivileged) {
			courseKalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			courseKalendarWrapper.setPrivateEventsVisible(true);
		} else {
			courseKalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			courseKalendarWrapper.setPrivateEventsVisible(userCourseEnv.isAdmin() || userCourseEnv.isCoach() || userCourseEnv.isMemberParticipant());
		}
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(courseKalendarWrapper.getKalendar(), getIdentity());
		if (config != null) {
			courseKalendarWrapper.setConfiguration(config);
		}
		// add link provider
		CourseLinkProviderController clpc = new CourseLinkProviderController(course, Collections.<ICourse>singletonList(course), ureq, getWindowControl());
		courseKalendarWrapper.setLinkProvider(clpc);
		calendars.add(courseKalendarWrapper);
		
		// add course group calendars
		
		// learning groups
		List<BusinessGroup> ownerGroups = cgm.getOwnedBusinessGroups(getIdentity());
		CourseCalendars.addCalendars(ureq, userCourseEnv, ownerGroups, !userCourseEnv.isCourseReadOnly(), clpc, calendars);
		List<BusinessGroup> attendedGroups = cgm.getParticipatingBusinessGroups(getIdentity());
		for (Iterator<BusinessGroup> ownerGroupsIterator = ownerGroups.iterator(); ownerGroupsIterator.hasNext();) {
			BusinessGroup ownerGroup = ownerGroupsIterator.next();
			if (attendedGroups.contains(ownerGroup))
				attendedGroups.remove(ownerGroup);
		}
		CourseCalendars.addCalendars(ureq, userCourseEnv, attendedGroups, false, clpc, calendars);

		return calendars;
	}
	
	private boolean isPrivileged() {
		return !userCourseEnv.isCourseReadOnly() && userCourseEnv.isAdmin();
	}
	

	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			List<KalendarRenderWrapper> calendars = getListOfCalendarWrappers(ureq);
			calendarController.setCalendars(calendars);
		}
	}

}
