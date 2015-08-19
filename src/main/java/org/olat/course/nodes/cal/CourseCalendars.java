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
package org.olat.course.nodes.cal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.run.calendar.CourseCalendarSubscription;
import org.olat.course.run.calendar.CourseLinkProviderController;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryManager;

public class CourseCalendars {

	private KalendarRenderWrapper courseKalendarWrapper;
	private List<KalendarRenderWrapper> calendars;

	public CourseCalendars(KalendarRenderWrapper courseKalendarWrapper, List<KalendarRenderWrapper> calendars) {
		this.courseKalendarWrapper = courseKalendarWrapper;
		this.calendars = calendars;
	}

	public List<KalendarRenderWrapper> getCalendars() {
		return calendars;
	}

	public void setCalendars(List<KalendarRenderWrapper> calendars) {
		this.calendars = calendars;
	}

	public KalendarRenderWrapper getCourseKalendarWrapper() {
		return courseKalendarWrapper;
	}

	public void setCourseKalendarWrapper(KalendarRenderWrapper courseKalendarWrapper) {
		this.courseKalendarWrapper = courseKalendarWrapper;
	}

	public Kalendar getKalendar() {
		return courseKalendarWrapper.getKalendar();
	}

	public CourseCalendarSubscription createSubscription2(UserRequest ureq) {
		CourseCalendarSubscription calSubscription = new CourseCalendarSubscription(getKalendar(), ureq.getUserSession().getGuiPreferences());
		return calSubscription;
	}
	
	/**
	 * Return only the course calendar without any group calendar
	 * @param ureq
	 * @param wControl
	 * @param ores
	 * @param ne
	 * @return
	 */
	public static KalendarRenderWrapper getCourseCalendarWrapper(UserRequest ureq, OLATResourceable ores, NodeEvaluation ne) {
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		// add course calendar
		ICourse course = CourseFactory.loadCourse(ores);
		KalendarRenderWrapper courseKalendarWrapper = calendarManager.getCourseCalendar(course);
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		Identity identity = ureq.getIdentity();
		Roles roles = ureq.getUserSession().getRoles();
		boolean isPrivileged = roles.isOLATAdmin() || cgm.isIdentityCourseAdministrator(identity)
				|| (ne != null && ne.isCapabilityAccessible(CalCourseNode.EDIT_CONDITION_ID))
				|| RepositoryManager.getInstance().isInstitutionalRessourceManagerFor(identity, roles, cgm.getCourseEntry());
		
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
		return courseKalendarWrapper;
	}

	public static CourseCalendars createCourseCalendarsWrapper(UserRequest ureq, WindowControl wControl, OLATResourceable ores, NodeEvaluation ne) {
		List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
		KalendarRenderWrapper courseKalendarWrapper = getCourseCalendarWrapper(ureq, ores, ne);
		// add link provider
		ICourse course = CourseFactory.loadCourse(ores);
		CourseLinkProviderController clpc = new CourseLinkProviderController(course, Collections.singletonList(course), ureq, wControl);
		courseKalendarWrapper.setLinkProvider(clpc);
		calendars.add(courseKalendarWrapper);
		
		Identity identity = ureq.getIdentity();
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();

		// add course group calendars
		boolean isGroupManager = ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isGroupManager()
				|| cgm.isIdentityCourseAdministrator(identity) || cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT);
		if (isGroupManager) {
			// learning groups
			List<BusinessGroup> allGroups = cgm.getAllBusinessGroups();
			addCalendars(ureq, allGroups, true, clpc, calendars);

		} else {
			// learning groups
			List<BusinessGroup> ownerGroups = cgm.getOwnedBusinessGroups(identity);
			addCalendars(ureq, ownerGroups, true, clpc, calendars);
			List<BusinessGroup> attendedGroups = cgm.getParticipatingBusinessGroups(identity);
			for (BusinessGroup ownerGroup : ownerGroups) {
				if (attendedGroups.contains(ownerGroup)) {
					attendedGroups.remove(ownerGroup);
				}
			}
			addCalendars(ureq, attendedGroups, false, clpc, calendars);
		}
		return new CourseCalendars(courseKalendarWrapper, calendars);
	}

	private static void addCalendars(UserRequest ureq, List<BusinessGroup> groups, boolean isOwner, LinkProvider linkProvider,
			List<KalendarRenderWrapper> calendars) {
		CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		for (BusinessGroup bGroup : groups) {
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
}
