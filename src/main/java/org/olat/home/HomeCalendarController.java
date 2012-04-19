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

package org.olat.home;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.run.calendar.CourseCalendarSubscription;
import org.olat.course.run.calendar.CourseLinkProviderController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

public class HomeCalendarController extends BasicController implements Activateable, Activateable2, GenericEventListener {

	private static final OLog log = Tracing.createLoggerFor(HomeCalendarController.class);
	
	private UserSession userSession;
	private CalendarController calendarController;
	
	public HomeCalendarController(UserRequest ureq, WindowControl windowControl) {
		super(ureq, windowControl);
		this.userSession = ureq.getUserSession();
		
		userSession.getSingleUserEventCenter().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarManager.class));
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), OresHelper.lookupType(CalendarManager.class));
		
		List<KalendarRenderWrapper> calendars = getListOfCalendarWrappers(ureq, windowControl);
		List<KalendarRenderWrapper> importedCalendars = getListOfImportedCalendarWrappers(ureq);
		calendarController = new WeeklyCalendarController(ureq, windowControl, calendars, importedCalendars, WeeklyCalendarController.CALLER_HOME, false);
		listenTo(calendarController);
		
		putInitialPanel(calendarController.getInitialComponent());
	}

	public void activate(UserRequest ureq, String viewIdentifier) {
		String[] splitted = viewIdentifier.split("\\.");
		if (splitted.length != 3) {
			// do nothing for user, just ignore it maybe this is a javascript
			// problem of the browser. However, log the problem
			log.warn("Can't parse date from user request: " + viewIdentifier);
			return;
		}
		String year = splitted[0];
		String month = splitted[1];
		String day = splitted[2];
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
		calendarController.setFocus(cal.getTime());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries != null && !entries.isEmpty() && calendarController instanceof Activateable2) {
			((Activateable2)calendarController).activate(ureq, entries, state);
		}
	}

	public static List<KalendarRenderWrapper> getListOfCalendarWrappers(UserRequest ureq, WindowControl wControl) {
		List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
		
		// get the personal calendar
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(ureq.getIdentity());
		calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		KalendarConfig personalKalendarConfig = calendarManager.findKalendarConfigForIdentity(
				calendarWrapper.getKalendar(), ureq);
		if (personalKalendarConfig != null) {
			calendarWrapper.getKalendarConfig().setCss(personalKalendarConfig.getCss());
			calendarWrapper.getKalendarConfig().setVis(personalKalendarConfig.isVis());
		}
		calendars.add(calendarWrapper);
		
		// get group calendars
		BusinessGroupManager bgManager = BusinessGroupManagerImpl.getInstance();

		SearchBusinessGroupParams groupParams = new SearchBusinessGroupParams();
		groupParams.addTypes(BusinessGroup.TYPE_BUDDYGROUP, BusinessGroup.TYPE_LEARNINGROUP, BusinessGroup.TYPE_RIGHTGROUP);
		groupParams.addTools(CollaborationTools.TOOL_CALENDAR);
		List<BusinessGroup> ownerGroups = bgManager.findBusinessGroups(groupParams, ureq.getIdentity(), true, false, null, 0, -1);
		addCalendars(ureq, ownerGroups, true, calendars);
		List<BusinessGroup> attendedGroups = bgManager.findBusinessGroups(groupParams, ureq.getIdentity(), false, true, null, 0, -1);
		for (Iterator<BusinessGroup> ownerGroupsIterator = ownerGroups.iterator(); ownerGroupsIterator.hasNext();) {
			BusinessGroup ownerGroup = ownerGroupsIterator.next();
			if (attendedGroups.contains(ownerGroup))
				attendedGroups.remove(ownerGroup);
		}
		addCalendars(ureq, attendedGroups, false, calendars);
		
		// add course calendars
		List<String> subscribedCourseCalendarIDs = CourseCalendarSubscription.getSubscribedCourseCalendarIDs(
				ureq.getUserSession().getGuiPreferences());
		
		RepositoryManager repoManager = RepositoryManager.getInstance();
		List<String> calendarIDsToBeRemoved = new ArrayList<String>();
		for (Iterator<String> iter = subscribedCourseCalendarIDs.iterator(); iter.hasNext();) {
			String courseCalendarID = iter.next();
			final long courseResourceableID = Long.parseLong(courseCalendarID);
			
			RepositoryEntry repoEntry = repoManager.lookupRepositoryEntry(new OLATResourceable() {

				public Long getResourceableId() { return new Long(courseResourceableID); }
				public String getResourceableTypeName() { return CourseModule.getCourseTypeName(); }
			}, false);
			if (repoEntry == null) {
				// mark calendar ID for cleanup
				calendarIDsToBeRemoved.add(courseCalendarID);
				continue;
			}
			try {
				ICourse course = CourseFactory.loadCourse(new Long(courseResourceableID));
				//calendar course aren't enabled per default but course node of type calendar are always possible
				//REVIEW if (!course.getCourseEnvironment().getCourseConfig().isCalendarEnabled()) continue;
				// add course calendar
				KalendarRenderWrapper courseCalendarWrapper = calendarManager.getCourseCalendar(course);
				CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
				boolean isPrivileged = cgm.isIdentityCourseAdministrator(ureq.getIdentity())
					|| cgm.hasRight(ureq.getIdentity(), CourseRights.RIGHT_COURSEEDITOR);
				if (isPrivileged) {
					courseCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
				} else {
					courseCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
				}
				KalendarConfig courseKalendarConfig = calendarManager.findKalendarConfigForIdentity(courseCalendarWrapper.getKalendar(), ureq);
				if (courseKalendarConfig != null) {
					courseCalendarWrapper.getKalendarConfig().setCss(courseKalendarConfig.getCss());
					courseCalendarWrapper.getKalendarConfig().setVis(courseKalendarConfig.isVis());
				}
				courseCalendarWrapper.setLinkProvider(new CourseLinkProviderController(course, ureq, wControl));
				calendars.add(courseCalendarWrapper);
			} catch (CorruptedCourseException e) {
				log.error("Corrupted course: " + courseResourceableID, null);
			}
		}

		// do calendar ID cleanup
		if (!calendarIDsToBeRemoved.isEmpty()) {
			subscribedCourseCalendarIDs = CourseCalendarSubscription.getSubscribedCourseCalendarIDs(
					ureq.getUserSession().getGuiPreferences());
			for (Iterator<String> iter = calendarIDsToBeRemoved.iterator(); iter.hasNext();) {
				subscribedCourseCalendarIDs.remove(iter.next());
			}
			CourseCalendarSubscription.persistSubscribedCalendarIDs(subscribedCourseCalendarIDs, ureq.getUserSession().getGuiPreferences());
		}
		return calendars;
	}
	
	public static List<KalendarRenderWrapper> getListOfImportedCalendarWrappers(UserRequest ureq) {
		ImportCalendarManager.reloadUrlImportedCalendars(ureq);
		return ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
	}
	
	private static void addCalendars(UserRequest ureq, List<BusinessGroup> groups, boolean isOwner, List<KalendarRenderWrapper> calendars) {
		CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		for (Iterator<BusinessGroup> iter = groups.iterator(); iter.hasNext();) {
			BusinessGroup bGroup = iter.next();
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
			KalendarConfig groupKalendarConfig = calendarManager.findKalendarConfigForIdentity(groupCalendarWrapper.getKalendar(), ureq);
			if (groupKalendarConfig != null) {
				groupCalendarWrapper.getKalendarConfig().setCss(groupKalendarConfig.getCss());
				groupCalendarWrapper.getKalendarConfig().setVis(groupKalendarConfig.isVis());
			}
			calendars.add(groupCalendarWrapper);
		}
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof KalendarModifiedEvent) {
			List<KalendarRenderWrapper> calendars = getListOfCalendarWrappers(ureq, getWindowControl());
			List<KalendarRenderWrapper> importedCalendars = getListOfImportedCalendarWrappers(ureq);
			calendarController.setCalendars(calendars, importedCalendars);
		}
	}

	protected void doDispose() {
		// remove from event bus
		userSession.getSingleUserEventCenter().deregisterFor(this, OresHelper.lookupType(CalendarManager.class));
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, OresHelper.lookupType(CalendarManager.class)); 
	}

	public void event(Event event) {
		if (event instanceof KalendarModifiedEvent) {
			if (calendarController != null) {
				// could theoretically be disposed 
				calendarController.setDirty();
			}
		}
	}
	
}
