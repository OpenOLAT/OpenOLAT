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

import static org.olat.commons.calendar.restapi.CalendarWSHelper.hasReadAccess;
import static org.olat.commons.calendar.restapi.CalendarWSHelper.hasWriteAccess;
import static org.olat.commons.calendar.restapi.CalendarWSHelper.processEvents;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.collaboration.CollaborationManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.nodes.cal.CalSecurityCallback;
import org.olat.course.nodes.cal.CalSecurityCallbackFactory;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Tag(name = "Users")
@Component
@Path("/users/{identityKey}/calendars")
public class UserCalendarWebService {
	
	private static final Logger log = Tracing.createLoggerFor(UserCalendarWebService.class);
	
	@Autowired
	private ACService acManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CollaborationManager collaborationManager;
	@Autowired
	private ACService acService;
	
	
	@GET
	@Tag(name = "Calendar")
	@Operation(summary = "List calendars of a specific user.", description = "Returns list of calendars of a specific user. Will always return the administrator's calendars for administrators.")
	@ApiResponse(responseCode = "200", description = "Request was successful.",
		content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CalendarVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CalendarVO.class)))
		}, links = {})
	@ApiResponse(responseCode = "401", description = "Not authorized.")
	@ApiResponse(responseCode = "404", description = "Not found.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCalendars(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		UserRequest ureq = getUserRequest(httpRequest);
		if(ureq.getIdentity() == null || !ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		if (!ureq.getIdentity().getKey().equals(identityKey)  && !isManager(identityKey, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CollectCalendars visitor = new CollectCalendars();
		getCalendars(visitor, ureq);
		
		List<KalendarRenderWrapper> wrappers = visitor.getWrappers();
		CalendarVO[] voes = new CalendarVO[wrappers.size()];
		int count = 0;
		for(KalendarRenderWrapper wrapper:wrappers) {
			voes[count++] = new CalendarVO(wrapper, hasWriteAccess(wrapper));
		}
		return Response.ok(voes).build();
	}
	
	@Path("{calendarId}")
	public CalWebService getCalendarWebService(@PathParam("calendarId") String calendarId,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		
		UserRequest ureq = getUserRequest(httpRequest);
		if(ureq.getIdentity() == null || !ureq.getUserSession().isAuthenticated()) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		
		Identity identity;
		if(ureq.getIdentity().getKey().equals(identityKey)) {
			identity = ureq.getIdentity();
		} else if (!ureq.getIdentity().getKey().equals(identityKey) && !isManager(identityKey, httpRequest)) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		} else {
			identity = securityManager.loadIdentityByKey(identityKey);
		}
		
		if(identity == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		
		KalendarRenderWrapper calendar = getCalendar(ureq, identity, calendarId);
		if(calendar == null) {
			throw new WebApplicationException(Response.serverError().status(Status.NOT_FOUND).build());
		}
		if (!hasReadAccess(calendar)) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		
		return new CalWebService(calendar);
	}

	@GET
	@Path("events")
	@Operation(summary = "List all events from a user.", description = "Returns list of all events in for a specific user.")
	@ApiResponse(responseCode = "200", description = "Request was successful.",
		content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EventVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = EventVO.class)))
		})
	@ApiResponse(responseCode = "401", description = "Not authorized.")
	@ApiResponse(responseCode = "404", description = "Not found.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEvents(@PathParam("identityKey") Long identityKey,
			@QueryParam("start")  @Parameter(description = "Set the date for the earliest event.")@DefaultValue("0") Integer start,
			@QueryParam("limit")  @Parameter(description = "Limit the amount of events to be returned.") @DefaultValue("25") Integer limit,
			@QueryParam("onlyFuture") @DefaultValue("false") Boolean onlyFuture,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		
		UserRequest ureq = getUserRequest(httpRequest);
		if(ureq.getIdentity() == null || !ureq.getUserSession().isAuthenticated()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		if (!ureq.getIdentity().getKey().equals(identityKey) && !isManager(identityKey, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		CollectCalendars visitor = new CollectCalendars();
		getCalendars(visitor, ureq);
		List<KalendarRenderWrapper> wrappers = visitor.getWrappers();
		List<EventVO> events = new ArrayList<>();
		for(KalendarRenderWrapper wrapper:wrappers) {
			Collection<KalendarEvent> kalEvents = wrapper.getKalendar().getEvents();
			for(KalendarEvent kalEvent:kalEvents) {
				EventVO eventVo = new EventVO(kalEvent);
				events.add(eventVo);
			}
		}

		return processEvents(events, onlyFuture, start, limit, httpRequest, request);
	}
	
	private KalendarRenderWrapper getCalendar(UserRequest ureq, Identity identity, String calendarId) {
		int typeIndex = calendarId.indexOf('_');
		if(typeIndex <= 0 || (typeIndex + 1 >= calendarId.length())) {
			return null;
		}
		
		CalendarModule calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		if(!calendarModule.isEnabled()) {
			return null;
		}
		
		String type = calendarId.substring(0, typeIndex);
		String id = calendarId.substring(typeIndex + 1);
		
		KalendarRenderWrapper wrapper = null;
		if("group".equals(type) && calendarModule.isEnableGroupCalendar()) {
			Long groupId = Long.parseLong(id);
			BusinessGroup group = businessGroupService.loadBusinessGroup(groupId);
			if(businessGroupService.isIdentityInBusinessGroup(ureq.getIdentity(), group)) {
				wrapper = collaborationManager.getCalendar(group, ureq, false, false);
			}
		} else if("course".equals(type) && (calendarModule.isEnableCourseElementCalendar() || calendarModule.isEnableCourseToolCalendar())) {
			Long courseId = Long.parseLong(id);
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(ureq.getIdentity());
			ienv.setRoles(ureq.getUserSession().getRoles());
			ICourse course = CourseFactory.loadCourse(courseId);
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
			CalSecurityCallback secCallback = CalSecurityCallbackFactory.createCourseCalendarCallback(userCourseEnv);
			wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, userCourseEnv, secCallback);
		} else if("user".equals(type) && calendarModule.isEnablePersonalCalendar()) {
			if(id.equals(identity.getName()) || id.equals(identity.getKey().toString())) {
				wrapper = getPersonalCalendar(identity);
			} else {
				log.warn("Personal calendar id {} doesn't match identity key: {}", id, identity.getKey());
			}
		}
		return wrapper;
	}
	
	private KalendarRenderWrapper getPersonalCalendar(Identity identity) {
		// get the personal calendar
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(identity);
		calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		calendarWrapper.setPrivateEventsVisible(true);
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(calendarWrapper.getKalendar(), identity);
		if (config != null) {
			calendarWrapper.setConfiguration(config);
		}
		return calendarWrapper;
	}
	
	private void getCalendars(CalendarVisitor calVisitor, UserRequest ureq) {	
		Roles roles = ureq.getUserSession().getRoles();
		Identity retrievedUser = ureq.getIdentity();

		CalendarModule calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		if(calendarModule.isEnabled()) {
			
			if(calendarModule.isEnablePersonalCalendar()) {
				KalendarRenderWrapper personalWrapper = getPersonalCalendar(ureq.getIdentity());
				calVisitor.visit(personalWrapper);
			}
			
			if(calendarModule.isEnableCourseToolCalendar() || calendarModule.isEnableCourseElementCalendar()) {
				SearchRepositoryEntryParameters repoParams = new SearchRepositoryEntryParameters(retrievedUser, roles, "CourseModule");
				repoParams.setOnlyExplicitMember(true);
				repoParams.setIdentity(retrievedUser);
				repoParams.setOfferOrganisations(acService.getOfferOrganisations(retrievedUser));
				repoParams.setOfferValidAt(new Date());
				
				IdentityEnvironment ienv = new IdentityEnvironment();
				ienv.setIdentity(retrievedUser);
				ienv.setRoles(roles);
				
				List<RepositoryEntry> entries = repositoryManager.genericANDQueryWithRolesRestriction(repoParams, 0, -1, true);
				for(RepositoryEntry entry:entries) {
					AccessResult result = acManager.isAccessible(entry, retrievedUser, null, false, false);
					if(result.isAccessible()) {
						try {
							final ICourse course = CourseFactory.loadCourse(entry);
							CourseConfig config = course.getCourseEnvironment().getCourseConfig();
							UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
							
							CalSecurityCallback secCallback = CalSecurityCallbackFactory.createCourseCalendarCallback(userCourseEnv);
							if(config.isCalendarEnabled()) {
								KalendarRenderWrapper wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, userCourseEnv, secCallback);
								calVisitor.visit(wrapper);
							} else {
								CalCourseNodeVisitor visitor = new CalCourseNodeVisitor();
								new CourseTreeVisitor(course, ienv).visit(visitor);
								if(visitor.isFound()) {
									KalendarRenderWrapper wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, userCourseEnv, secCallback);
									calVisitor.visit(wrapper);
								}
							}
						} catch (Exception e) {
							log.error("", e);
						}
					}
				}
			}
			
			if(calendarModule.isEnableGroupCalendar()) {
				//start found forums in groups
				BusinessGroupService bgm = CoreSpringFactory.getImpl(BusinessGroupService.class);
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(retrievedUser, true, true);
				params.addTools(CollaborationTools.TOOL_CALENDAR);
				List<BusinessGroup> groups = bgm.findBusinessGroups(params, null, 0, -1);
				for(BusinessGroup group:groups) {
					KalendarRenderWrapper wrapper = collaborationManager.getCalendar(group, ureq, false, false);
					calVisitor.visit(wrapper);
				}
			}
		}
	}
	
	private boolean isManager(Long identityKey, HttpServletRequest httpRequest) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		return isManager(identity, httpRequest);
	}
	
	private boolean isManager(Identity identity, HttpServletRequest httpRequest) {
		if(identity == null) return false;
		Roles managerRoles = RestSecurityHelper.getRoles(httpRequest);
		if(!managerRoles.isAdministrator() && !managerRoles.isUserManager() && !managerRoles.isRolesManager()) {
			return false;
		}

		Roles identityRoles = securityManager.getRoles(identity);
		return managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles);
		
	}
	
	private static interface CalendarVisitor {
		public void visit(KalendarRenderWrapper wrapper);
	}
	
	private static class CollectCalendars implements CalendarVisitor {
		private final List<KalendarRenderWrapper> wrappers = new ArrayList<>();

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
