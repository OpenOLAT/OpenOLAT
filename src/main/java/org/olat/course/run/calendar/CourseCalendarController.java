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

import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryManager;

public class CourseCalendarController extends BasicController {

	private CalendarController calendarController;
	private KalendarRenderWrapper courseKalendarWrapper;
	private OLATResourceable ores;
	
	public CourseCalendarController(UserRequest ureq, WindowControl wControl, OLATResourceable course) {
		super(ureq, wControl);
		this.ores = course;
		List<KalendarRenderWrapper> calendars = getListOfCalendarWrappers(ureq);
		calendarController = new WeeklyCalendarController(ureq, wControl, calendars,
				WeeklyCalendarController.CALLER_COURSE, true);
		listenTo(calendarController);
		putInitialPanel(calendarController.getInitialComponent());
	}

	private List<KalendarRenderWrapper> getListOfCalendarWrappers(UserRequest ureq) {
		List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		// add course calendar
		ICourse course = CourseFactory.loadCourse(ores);
		courseKalendarWrapper = calendarManager.getCourseCalendar(course);
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		Identity identity = ureq.getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		boolean isPrivileged = cgm.isIdentityCourseAdministrator(identity)
				|| cgm.hasRight(identity, CourseRights.RIGHT_COURSEEDITOR)
				|| RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(identity, roles, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		if (isPrivileged) {
			courseKalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		} else {
			courseKalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
		}
		KalendarConfig config = calendarManager.findKalendarConfigForIdentity(courseKalendarWrapper.getKalendar(), ureq);
		if (config != null) {
			courseKalendarWrapper.getKalendarConfig().setCss(config.getCss());
			courseKalendarWrapper.getKalendarConfig().setVis(config.isVis());
		}
		// add link provider
		CourseLinkProviderController clpc = new CourseLinkProviderController(course, Collections.<ICourse>singletonList(course), ureq, getWindowControl());
		courseKalendarWrapper.setLinkProvider(clpc);
		calendars.add(courseKalendarWrapper);
		
		// add course group calendars
		
		// learning groups
		List<BusinessGroup> ownerGroups = cgm.getOwnedBusinessGroups(identity);
		addCalendars(ureq, ownerGroups, true, clpc, calendars);
		List<BusinessGroup> attendedGroups = cgm.getParticipatingBusinessGroups(identity);
		for (Iterator<BusinessGroup> ownerGroupsIterator = ownerGroups.iterator(); ownerGroupsIterator.hasNext();) {
			BusinessGroup ownerGroup = ownerGroupsIterator.next();
			if (attendedGroups.contains(ownerGroup))
				attendedGroups.remove(ownerGroup);
		}
		addCalendars(ureq, attendedGroups, false, clpc, calendars);

		return calendars;
	}
	
	private void addCalendars(UserRequest ureq, List<BusinessGroup> groups, boolean isOwner, LinkProvider linkProvider,
			List<KalendarRenderWrapper> calendars) {
		CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		for (BusinessGroup bGroup:groups) {
			CollaborationTools collabTools = collabFactory.getOrCreateCollaborationTools(bGroup);
			if (!collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) continue;
			KalendarRenderWrapper groupCalendarWrapper = calendarManager.getGroupCalendar(bGroup);
			// set calendar access
			int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
			Long lCalAccess = collabTools.lookupCalendarAccess();
			if (lCalAccess != null) iCalAccess = lCalAccess.intValue();
			if (iCalAccess == CollaborationTools.CALENDAR_ACCESS_OWNERS && !isOwner) {
				groupCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			} else {
				groupCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			}
			KalendarConfig config = calendarManager.findKalendarConfigForIdentity(groupCalendarWrapper.getKalendar(), ureq);
			if (config != null) {
				groupCalendarWrapper.getKalendarConfig().setCss(config.getCss());
				groupCalendarWrapper.getKalendarConfig().setVis(config.isVis());
			}
			groupCalendarWrapper.setLinkProvider(linkProvider);
			calendars.add(groupCalendarWrapper);
		}
	}
	

	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof KalendarModifiedEvent) {
			List<KalendarRenderWrapper> calendars = getListOfCalendarWrappers(ureq);
			calendarController.setCalendars(calendars);
		}
	}

	protected void doDispose() {
		//
	}

}
