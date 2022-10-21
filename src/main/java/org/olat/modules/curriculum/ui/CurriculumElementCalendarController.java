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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper.LinkProviderCreator;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.calendar.CourseLinkProviderControllerCreator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 	
 * Initial date: 4 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementCalendarController extends BasicController {

	private final WeeklyCalendarController calendarController;

	private final OLATResourceable callerOres;
	private final List<KalendarRenderWrapper> calendars;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public CurriculumElementCalendarController(UserRequest ureq, WindowControl wControl,
			CurriculumElementRef element, List<RepositoryEntry> entries, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		calendars = loadCalendars(ureq, entries);

		callerOres = OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey());
		calendarController = new WeeklyCalendarController(ureq, wControl, calendars, CalendarController.CALLER_CURRICULUM,
				callerOres, false);
		calendarController.setDifferentiateManagedEvent(CourseCalendars.needToDifferentiateManagedEvents(calendars));
		listenTo(calendarController);
		putInitialPanel(calendarController.getInitialComponent());
	}
	
	private List<KalendarRenderWrapper> loadCalendars(UserRequest ureq, List<RepositoryEntry> repoEntries) {
		List<KalendarRenderWrapper> wrappers = new ArrayList<>(repoEntries.size());

		for(RepositoryEntry entry:repoEntries) {
			ICourse course = CourseFactory.loadCourse(entry);
			if(CourseCalendars.isCourseCalendarEnabled(course)) {
				RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, entry);
				if(reSecurity.canLaunch() || secCallback.canViewAllCalendars()) {
					UserCourseEnvironmentImpl uce = UserCourseEnvironmentImpl.load(ureq, course, reSecurity, getWindowControl());
					List<KalendarRenderWrapper> courseWrappers = getListOfCalendarWrappers(ureq, uce, course);
					wrappers.addAll(courseWrappers);
				}
			}
		}

		return wrappers;
	}

	private List<KalendarRenderWrapper> getListOfCalendarWrappers(UserRequest ureq, UserCourseEnvironmentImpl userCourseEnv, ICourse course) {
		List<KalendarRenderWrapper> calendarWrappers = new ArrayList<>();
		// add course calendar
		KalendarRenderWrapper courseKalendarWrapper = calendarManager.getCourseCalendar(course);
		boolean isPrivileged = isPrivileged(userCourseEnv);
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
		LinkProviderCreator clpc = new CourseLinkProviderControllerCreator(course);
		courseKalendarWrapper.setLinkProviderCreator(clpc);
		calendarWrappers.add(courseKalendarWrapper);
		
		// add course group calendars
		
		// learning groups
		List<BusinessGroup> ownerGroups = userCourseEnv.getCoachedGroups();
		CourseCalendars.addCalendars(ureq, userCourseEnv, ownerGroups, !userCourseEnv.isCourseReadOnly(), clpc, calendarWrappers);
		List<BusinessGroup> attendedGroups = userCourseEnv.getParticipatingGroups();
		for (Iterator<BusinessGroup> ownerGroupsIterator = ownerGroups.iterator(); ownerGroupsIterator.hasNext();) {
			BusinessGroup ownerGroup = ownerGroupsIterator.next();
			if (attendedGroups.contains(ownerGroup)) {
				attendedGroups.remove(ownerGroup);
			}
		}
		CourseCalendars.addCalendars(ureq, userCourseEnv, attendedGroups, false, clpc, calendarWrappers);
		if(secCallback.canViewAllCalendars()) {
			List<BusinessGroup> allGroups = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			allGroups.removeAll(ownerGroups);
			allGroups.removeAll(attendedGroups);
			CourseCalendars.addCalendars(ureq, userCourseEnv, allGroups, false, clpc, calendarWrappers);
		}
		return calendarWrappers;
	}

	private boolean isPrivileged(UserCourseEnvironment userCourseEnv) {
		return !userCourseEnv.isCourseReadOnly() && userCourseEnv.isAdmin();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to do
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof CalendarGUIModifiedEvent) {
			calendarController.setCalendars(calendars);
		}
	}

	@Override
	protected void doDispose() {
		calendarController.dispose();
        super.doDispose();
	}
	

}