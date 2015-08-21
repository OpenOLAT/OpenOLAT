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
package org.olat.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.collaboration.CollaborationManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.calendar.CourseLinkProviderController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class HomeCalendarManager {
	
	private static final OLog log = Tracing.createLoggerFor(HomeCalendarManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public List<KalendarRenderWrapper> getListOfCalendarWrappers(UserRequest ureq, WindowControl wControl) {
		
		if(!calendarModule.isEnabled()) {
			return new ArrayList<KalendarRenderWrapper>();
		}
		
		List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
		appendPersonalCalendar(ureq, calendars);
		appendGroupCalendars(ureq, calendars);
		appendCourseCalendars(ureq, wControl, calendars);
		return calendars;
	}
	
	private void appendPersonalCalendar(UserRequest ureq, List<KalendarRenderWrapper> calendars) {
		// get the personal calendar
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		if(calendarModule.isEnablePersonalCalendar()) {
			KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(ureq.getIdentity());
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
			KalendarConfig personalKalendarConfig = calendarManager.findKalendarConfigForIdentity(calendarWrapper.getKalendar(), ureq);
			if (personalKalendarConfig != null) {
				calendarWrapper.getKalendarConfig().setCss(personalKalendarConfig.getCss());
				calendarWrapper.getKalendarConfig().setVis(personalKalendarConfig.isVis());
			}
			calendars.add(calendarWrapper);
		}
	}
	
	private void appendGroupCalendars(UserRequest ureq, List<KalendarRenderWrapper> calendars) {
		// get group calendars
		if(calendarModule.isEnableGroupCalendar()) {
			SearchBusinessGroupParams groupParams = new SearchBusinessGroupParams(ureq.getIdentity(), true, false);
			groupParams.addTools(CollaborationTools.TOOL_CALENDAR);
			List<BusinessGroup> ownerGroups = businessGroupService.findBusinessGroups(groupParams, null, 0, -1);
			addCalendars(ureq, ownerGroups, true, calendars);
			
			SearchBusinessGroupParams groupParams2 = new SearchBusinessGroupParams(ureq.getIdentity(), false, true);
			groupParams2.addTools(CollaborationTools.TOOL_CALENDAR);
			List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroups(groupParams2, null, 0, -1);
			attendedGroups.removeAll(ownerGroups);
			addCalendars(ureq, attendedGroups, false, calendars);
		}
	}

	private void appendCourseCalendars(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendars) {
		if(calendarModule.isEnableCourseElementCalendar() || calendarModule.isEnableCourseToolCalendar()) {
			CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
			
			// add course calendars
			List<Object[]> resources = getCourses(ureq.getIdentity());
			Set<OLATResource> editoredResources = getEditorGrants(ureq.getIdentity());
			
			Set<Long> duplicates = new HashSet<>();
			
			for (Object[] resource:resources) {
				RepositoryEntry courseEntry = (RepositoryEntry)resource[0];
				if(duplicates.contains(courseEntry.getKey())) {
					continue;
				}
				duplicates.add(courseEntry.getKey());
				
				String role = (String)resource[1];
				Long courseResourceableID = courseEntry.getOlatResource().getResourceableId();
				try {
					ICourse course = CourseFactory.loadCourse(courseEntry);
					if(isCourseCalendarEnabled(course)) {
						//calendar course aren't enabled per default but course node of type calendar are always possible
						//REVIEW if (!course.getCourseEnvironment().getCourseConfig().isCalendarEnabled()) continue;
						// add course calendar
						KalendarRenderWrapper courseCalendarWrapper = calendarManager.getCourseCalendar(course);
						boolean isPrivileged = GroupRoles.owner.name().equals(role) || editoredResources.contains(courseEntry.getOlatResource());
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
						courseCalendarWrapper.setLinkProvider(new CourseLinkProviderController(course, Collections.singletonList(course), ureq, wControl));
						calendars.add(courseCalendarWrapper);
					}
				} catch (CorruptedCourseException e) {
					OLATResource olatResource = courseEntry.getOlatResource();
					
					
					log.error("Corrupted course: " + olatResource.getResourceableTypeName() + " :: " + courseResourceableID, null);
				}
			}
		}
	}
	
	private boolean isCourseCalendarEnabled(ICourse course) {
		if(course.getCourseConfig().isCalendarEnabled()) {
			return true;
		}
		
		CourseNode rootNode = course.getRunStructure().getRootNode();
		CalCourseNodeVisitor v = new CalCourseNodeVisitor();
		new TreeVisitor(v, rootNode, true).visitAll();
		return v.isFound();
	}
	
	/**
	 * 
	 * @param identity
	 * @return List of array, first the repository entry, second the role
	 */
	private List<Object[]> getCourses(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v, membership.role from repositoryentry  v ")
		  .append(" inner join fetch v.olatResource as resource ")
		  .append(" inner join v.groups as retogroup")
		  .append(" inner join retogroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where v.olatResource.resName='CourseModule' and membership.identity.key=:identityKey and")
		  .append(" (")
		  .append("   (v.access=").append(RepositoryEntry.ACC_OWNERS).append(" and v.membersOnly=true and membership.role in ('").append(GroupRoles.owner.name()).append("','").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("'))")
		  .append("   or")
		  .append("   (v.access>=").append(RepositoryEntry.ACC_OWNERS).append(" and membership.role='").append(GroupRoles.owner.name()).append("')")
		  .append("   or")
		  .append("   (v.access>=").append(RepositoryEntry.ACC_USERS).append(" and membership.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("'))")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
	}
	
	private Set<OLATResource> getEditorGrants(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select grant.resource from bgrant as grant")
		  .append(" inner join grant.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and grant.permission='").append(CourseRights.RIGHT_COURSEEDITOR).append("' and membership.role=grant.role");
		List<OLATResource> resources = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResource.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return new HashSet<>(resources);
	}
	
	/**
	 * Append the calendars of a list of groups. The groups must have their calendar tool
	 * enabled, this routine doesn't check it.
	 * @param ureq
	 * @param groups
	 * @param isOwner
	 * @param calendars
	 */
	private void addCalendars(UserRequest ureq, List<BusinessGroup> groups, boolean isOwner, List<KalendarRenderWrapper> calendars) {
		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		Map<Long,Long> groupKeyToAccess = CoreSpringFactory.getImpl(CollaborationManager.class).lookupCalendarAccess(groups);
		for (BusinessGroup bGroup:groups) {
			KalendarRenderWrapper groupCalendarWrapper = calendarManager.getGroupCalendar(bGroup);
			// set calendar access
			int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
			Long lCalAccess = groupKeyToAccess.get(bGroup.getKey());
			if (lCalAccess != null) {
				iCalAccess = lCalAccess.intValue();
			}
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
	
	public List<KalendarRenderWrapper> getListOfImportedCalendarWrappers(UserRequest ureq) {
		ImportCalendarManager.reloadUrlImportedCalendars(ureq);
		return ImportCalendarManager.getImportedCalendarsForIdentity(ureq);
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
