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
import java.util.Map;

import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.CalendarKey;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.calendar.CourseCalendarSubscription;
import org.olat.course.run.calendar.CourseLinkProviderController;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;

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
		return new CourseCalendarSubscription(getKalendar(), ureq.getUserSession().getGuiPreferences());
	}
	
	/**
	 * Return only the course calendar (for course calendar element) without any group calendar.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param ores
	 * @param ne
	 * @return
	 */
	public static KalendarRenderWrapper getCourseCalendarWrapper(UserRequest ureq, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		// add course calendar
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		KalendarRenderWrapper courseKalendarWrapper = calendarManager.getCourseCalendar(course);
		boolean isPrivileged = !userCourseEnv.isCourseReadOnly() &&
				(userCourseEnv.isAdmin()
				  || (ne != null && ne.isCapabilityAccessible(CalCourseNode.EDIT_CONDITION_ID)));
		
		if (isPrivileged) {
			courseKalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			courseKalendarWrapper.setPrivateEventsVisible(true);
		} else {
			courseKalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			courseKalendarWrapper.setPrivateEventsVisible(userCourseEnv.isAdmin() || userCourseEnv.isCoach() || userCourseEnv.isParticipant());
		}
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(courseKalendarWrapper.getKalendar(), ureq.getIdentity());
		if (config != null) {
			courseKalendarWrapper.setConfiguration(config);
		}
		return courseKalendarWrapper;
	}

	public static CourseCalendars createCourseCalendarsWrapper(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		KalendarRenderWrapper courseKalendarWrapper = getCourseCalendarWrapper(ureq, userCourseEnv, ne);
		// add link provider
		
		CourseLinkProviderController clpc = new CourseLinkProviderController(course, Collections.singletonList(course), ureq, wControl);
		courseKalendarWrapper.setLinkProvider(clpc);
		calendars.add(courseKalendarWrapper);
		
		Identity identity = ureq.getIdentity();
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();

		// add course group calendars
		Roles roles = ureq.getUserSession().getRoles();
		boolean isGroupManager = roles.isGroupManager() || userCourseEnv.isAdmin()
				|| cgm.hasRight(identity, CourseRights.RIGHT_GROUPMANAGEMENT);
		boolean readOnly = userCourseEnv.isCourseReadOnly();
		
		if (isGroupManager) {
			// learning groups
			List<BusinessGroup> allGroups = cgm.getAllBusinessGroups();
			addCalendars(ureq, userCourseEnv, allGroups, !readOnly, clpc, calendars);
		} else {
			// learning groups
			List<BusinessGroup> ownerGroups = cgm.getOwnedBusinessGroups(identity);
			addCalendars(ureq, userCourseEnv, ownerGroups, !readOnly, clpc, calendars);
			List<BusinessGroup> attendedGroups = cgm.getParticipatingBusinessGroups(identity);
			for (BusinessGroup ownerGroup : ownerGroups) {
				if (attendedGroups.contains(ownerGroup)) {
					attendedGroups.remove(ownerGroup);
				}
			}
			addCalendars(ureq, userCourseEnv, attendedGroups, false, clpc, calendars);
		}
		return new CourseCalendars(courseKalendarWrapper, calendars);
	}

	public static void addCalendars(UserRequest ureq, UserCourseEnvironment courseEnv, List<BusinessGroup> groups, boolean isOwner,
			LinkProvider linkProvider, List<KalendarRenderWrapper> calendars) {
		if(groups == null || groups.isEmpty()) return;
		
		CollaborationToolsFactory collabFactory = CollaborationToolsFactory.getInstance();
		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		Map<CalendarKey, CalendarUserConfiguration> configMap = calendarManager
				.getCalendarUserConfigurationsMap(ureq.getIdentity(), CalendarManager.TYPE_GROUP);
		for (BusinessGroup bGroup : groups) {
			CollaborationTools collabTools = collabFactory.getOrCreateCollaborationTools(bGroup);
			if (!collabTools.isToolEnabled(CollaborationTools.TOOL_CALENDAR)) {
				continue;
			}
			boolean member = courseEnv.isIdentityInCourseGroup(bGroup.getKey());
			KalendarRenderWrapper groupCalendarWrapper = calendarManager.getGroupCalendar(bGroup);
			groupCalendarWrapper.setPrivateEventsVisible(member || isOwner);
			// set calendar access
			int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
			Long lCalAccess = collabTools.lookupCalendarAccess();
			if (lCalAccess != null) iCalAccess = lCalAccess.intValue();
			if (iCalAccess == CollaborationTools.CALENDAR_ACCESS_OWNERS && !isOwner) {
				groupCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
			} else {
				groupCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			}
			CalendarUserConfiguration config = configMap.get(groupCalendarWrapper.getCalendarKey());
			if (config != null) {
				groupCalendarWrapper.setConfiguration(config);
			}
			groupCalendarWrapper.setLinkProvider(linkProvider);
			calendars.add(groupCalendarWrapper);
		}
	}
	
	public static boolean needToDifferentiateManagedEvents(List<KalendarRenderWrapper> calendars) {
		boolean hasManaged = false;
		for(KalendarRenderWrapper wrapper:calendars) {
			Kalendar cal = wrapper.getKalendar();
			hasManaged |= cal.hasManagedEvents();
		}
		return hasManaged;
	}
	
	public static boolean isCourseCalendarEnabled(ICourse course) {
		if(course.getCourseConfig().isCalendarEnabled()) {
			return true;
		}
		
		CourseNode rootNode = course.getRunStructure().getRootNode();
		CalCourseNodeVisitor v = new CalCourseNodeVisitor();
		new TreeVisitor(new CalCourseNodeVisitor(), rootNode, true).visitAll();
		return v.isFound();
	}
	
	private static class CalCourseNodeVisitor implements Visitor {
		private boolean found = false;
		
		public boolean isFound() {
			return found;
		}
		
		@Override
		public void visit(INode node) {
			if(node instanceof CalCourseNode) {
				found = true;
			}
		}
	}
}
