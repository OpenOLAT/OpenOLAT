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
package org.olat.commons.calendar.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.IdentityShort;
import org.olat.collaboration.CollaborationManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Path("users/{identityKey}/calendars")
public class CalendarWebService {
	
	private static final OLog log = Tracing.createLoggerFor(CalendarWebService.class);
	
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCalendars(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if (ureq.getIdentity() == null || !ureq.getIdentity().getKey().equals(identityKey)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CollectCalendars visitor = new CollectCalendars();
		getCalendars(visitor, ureq);
		
		List<KalendarRenderWrapper> wrappers = visitor.getWrappers();
		CalendarVO[] voes = new CalendarVO[wrappers.size()];
		int count = 0;
		for(KalendarRenderWrapper wrapper:wrappers) {
			voes[count++] = new CalendarVO(wrapper);
		}
		return Response.ok(voes).build();
	}
	
	@GET
	@Path("{calendarId}/events")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEventsByCalendar(@PathParam("calendarId") String calendarId,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if (ureq.getIdentity() == null || !ureq.getIdentity().getKey().equals(identityKey)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		KalendarRenderWrapper calendar = getCalendar(ureq, calendarId);
		if(calendar == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!hasReadAccess(calendar)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<EventVO> events = new ArrayList<EventVO>();
		Collection<KalendarEvent> kalEvents = calendar.getKalendar().getEvents();
		for(KalendarEvent kalEvent:kalEvents) {
			EventVO eventVo = new EventVO(kalEvent);
			events.add(eventVo);
		}

		EventVO[] voes = new EventVO[events.size()];
		voes = events.toArray(voes);
		return Response.ok(voes).build();
	}
	
	@PUT
	@Path("{calendarId}/events")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putEventByCalendar(@PathParam("calendarId") String calendarId,
			@PathParam("identityKey") Long identityKey, EventVO event, @Context HttpServletRequest httpRequest) {
		return addEventByCalendar(calendarId, identityKey, event, httpRequest);
	}
	
	@POST
	@Path("{calendarId}/events")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postEventByCalendar(@PathParam("calendarId") String calendarId,
			@PathParam("identityKey") Long identityKey, EventVO event, @Context HttpServletRequest httpRequest) {
		return addEventByCalendar(calendarId, identityKey, event, httpRequest);
	}
	
	private Response addEventByCalendar(String calendarId, Long identityKey, EventVO event, HttpServletRequest httpRequest) {
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if (ureq.getIdentity() == null || !ureq.getIdentity().getKey().equals(identityKey)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		KalendarRenderWrapper calendar = getCalendar(ureq, calendarId);
		if(calendar == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(!hasWriteAccess(calendar)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CalendarManager calendarManager = CalendarManagerFactory.getInstance().getCalendarManager();
		if(event.getId() == null) {
			String id = UUID.randomUUID().toString();
			KalendarEvent kalEvent = new KalendarEvent(id, event.getSubject(), event.getBegin(), event.getEnd());
			calendarManager.addEventTo(calendar.getKalendar(), kalEvent);
		} else {
			KalendarEvent kalEvent = calendar.getKalendar().getEvent(event.getId());
			if(kalEvent == null) {
				kalEvent = new KalendarEvent(event.getId(), event.getSubject(), event.getBegin(), event.getEnd());
				calendarManager.addEventTo(calendar.getKalendar(), kalEvent);
			} else {
				kalEvent.setBegin(event.getBegin());
				kalEvent.setEnd(event.getEnd());
				kalEvent.setSubject(event.getSubject());
				kalEvent.setDescription(event.getDescription());
			}
		}

		return Response.ok().build();
	}
	
	
	@GET
	@Path("events")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEvents(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		UserRequest ureq = getUserRequest(httpRequest);
		if(!ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if (ureq.getIdentity() == null || !ureq.getIdentity().getKey().equals(identityKey)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CollectCalendars visitor = new CollectCalendars();
		getCalendars(visitor, ureq);
		List<KalendarRenderWrapper> wrappers = visitor.getWrappers();
		List<EventVO> events = new ArrayList<EventVO>();
		for(KalendarRenderWrapper wrapper:wrappers) {
			Collection<KalendarEvent> kalEvents = wrapper.getKalendar().getEvents();
			for(KalendarEvent kalEvent:kalEvents) {
				EventVO eventVo = new EventVO(kalEvent);
				events.add(eventVo);
			}
		}

		EventVO[] voes = new EventVO[events.size()];
		voes = events.toArray(voes);
		return Response.ok(voes).build();
	}
	
	private boolean hasReadAccess(KalendarRenderWrapper wrapper) {
		if(wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY) {
			return true;
		}
		if(wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) {
			return true;
		}
		return false;
	}
	
	private boolean hasWriteAccess(KalendarRenderWrapper wrapper) {
		if(wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE) {
			return true;
		}
		return false;
	}
	
	private KalendarRenderWrapper getCalendar(UserRequest ureq, String calendarId) {
		int typeIndex = calendarId.indexOf('_');
		if(typeIndex <= 0 || (typeIndex + 1 >= calendarId.length())) {
			return null;
		} 
		String type = calendarId.substring(0, typeIndex);
		String id = calendarId.substring(typeIndex + 1);
		
		KalendarRenderWrapper wrapper = null;
		if("group".equals(type)) {
			Long groupId = Long.parseLong(id);
			BusinessGroup group = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(groupId, false);
			if(BusinessGroupManagerImpl.getInstance().isIdentityInBusinessGroup(ureq.getIdentity(), group)) {
				CollaborationManager collaborationManager = CoreSpringFactory.getImpl(CollaborationManager.class);
				wrapper = collaborationManager.getCalendar(group, ureq, false);
			}
		} else if("course".equals(type)) {
			Long courseId = Long.parseLong(id);
			ICourse course = CourseFactory.loadCourse(courseId);
			wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, course, null);
		} else if("user".equals(type)) {
			List<String> identityName = Collections.singletonList(id);
			List<IdentityShort> shorts = BaseSecurityManager.getInstance().findShortIdentitiesByName(identityName);
			if(shorts.size() == 1 && shorts.get(0).getKey().equals(ureq.getIdentity().getKey())) {
				wrapper = getPersonalCalendar(ureq);
			}
		}
		return wrapper;
	}
	
	private KalendarRenderWrapper getPersonalCalendar(UserRequest ureq) {
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
			return calendarWrapper;
	}
	
	private void getCalendars(CalendarVisitor calVisitor, UserRequest ureq) {	
		Roles roles = ureq.getUserSession().getRoles();
		Identity retrievedUser = ureq.getIdentity();

		KalendarRenderWrapper personalWrapper = getPersonalCalendar(ureq);
		calVisitor.visit(personalWrapper);
		
		RepositoryManager rm = RepositoryManager.getInstance();
		ACFrontendManager acManager = (ACFrontendManager)CoreSpringFactory.getBean("acFrontendManager");
		SearchRepositoryEntryParameters repoParams = new SearchRepositoryEntryParameters(retrievedUser, roles, "CourseModule");
		repoParams.setOnlyExplicitMember(true);
		List<RepositoryEntry> entries = rm.genericANDQueryWithRolesRestriction(repoParams, 0, -1, true);
		for(RepositoryEntry entry:entries) {
			AccessResult result = acManager.isAccessible(entry, retrievedUser, false);
			if(result.isAccessible()) {
				try {
					final ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
					CourseConfig config = course.getCourseEnvironment().getCourseConfig();
					if(config.isCalendarEnabled()) {
						KalendarRenderWrapper wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, entry.getOlatResource(), null);
						calVisitor.visit(wrapper);
					} else {
						IdentityEnvironment ienv = new IdentityEnvironment(retrievedUser, roles);
						CalCourseNodeVisitor visitor = new CalCourseNodeVisitor();
						new CourseTreeVisitor(course, ienv).visit(visitor);
						if(visitor.isFound()) {
							KalendarRenderWrapper wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, entry.getOlatResource(), null);
							calVisitor.visit(wrapper);
						}
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		
		CollaborationManager collaborationManager = CoreSpringFactory.getImpl(CollaborationManager.class);
		
		//start found forums in groups
		BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(BusinessGroup.TYPE_BUDDYGROUP, BusinessGroup.TYPE_LEARNINGROUP, BusinessGroup.TYPE_RIGHTGROUP);
		params.addTools(CollaborationTools.TOOL_CALENDAR);
		List<BusinessGroup> groups = bgm.findBusinessGroups(params, retrievedUser, true, true, null, 0, -1);
		for(BusinessGroup group:groups) {
			KalendarRenderWrapper wrapper = collaborationManager.getCalendar(group, ureq, false);
			calVisitor.visit(wrapper);
		}
	}
	
	private static interface CalendarVisitor {
		public void visit(KalendarRenderWrapper wrapper);
	}
	
	private static class CollectCalendars implements CalendarVisitor {
		private final List<KalendarRenderWrapper> wrappers = new ArrayList<KalendarRenderWrapper>();

		public List<KalendarRenderWrapper> getWrappers() {
			return wrappers;
		}

		@Override
		public void visit(KalendarRenderWrapper wrapper) {
			wrappers.add(wrapper);
		}
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
