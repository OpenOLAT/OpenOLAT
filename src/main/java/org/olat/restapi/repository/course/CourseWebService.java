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
package org.olat.restapi.repository.course;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.Logger;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.restapi.CalWebService;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.gotomeeting.restapi.GoToTrainingWebService;
import org.olat.modules.lecture.restapi.LectureBlocksWebService;
import org.olat.modules.reminder.restapi.RemindersWebService;
import org.olat.modules.vitero.restapi.ViteroBookingWebService;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.OlatResourceVO;
import org.olat.restapi.support.vo.RepositoryEntryAccessVO;
import org.olat.user.restapi.OrganisationVO;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * This web service will handle the functionality related to <code>Course</code>
 * and its contents.
 * 
 * <P>
 * Initial Date:  27 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseWebService {

	private static final Logger log = Tracing.createLoggerFor(CourseWebService.class);
	private static final XStream myXStream = XStreamHelper.createXStreamInstance();

	
	public static final CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	
	private final ICourse course;
	private final OLATResource courseOres;
	
	public CourseWebService(OLATResource courseOres, ICourse course) {
		this.course = course;
		this.courseOres = courseOres;
	}
	
	/**
	 * To access the group web-service for the specified course.
	 * 
	 * @return The group web service
	 */
	@Path("groups")
	public CourseGroupWebService getCourseGroupWebService() {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseOres, false);
		return new CourseGroupWebService(re, courseOres);
	}
	
	/**
	 * To access the calendar web-service of the specified course.
	 * 
	 * @param request The HTTP request
	 * @return The calendar web service
	 */
	@Path("calendar")
	public CalWebService getCourseCalendarWebService(@Context HttpServletRequest request) {
		if(calendarModule.isEnabled()
				&& (calendarModule.isEnableCourseToolCalendar() || calendarModule.isEnableCourseElementCalendar())
				&& course.getCourseConfig().isCalendarEnabled()) {
			UserRequest ureq = getUserRequest(request);
			Roles roles = ureq.getUserSession().getRoles();
			
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(ureq.getIdentity());
			ienv.setRoles(roles);
			
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq.getIdentity(), roles, courseEntry);

			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment(), null,
					null, null, null,
					reSecurity.isCoach(), reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach(), reSecurity.isParticipant(),
					reSecurity.isReadOnly() || reSecurity.isOnlyPrincipal() || reSecurity.isOnlyMasterCoach());
			KalendarRenderWrapper wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, userCourseEnv, null);
			return new CalWebService(wrapper);
		}
		return null;
	}
	
	/**
	 * To access the vitero booking web-service of the specified course
	 * and course element.
	 * 
	 * @param subIdentifier The identifier of the course element
	 * @return The vitero booking web-service
	 */
	@Path("vitero/{subIdentifier}")
	public ViteroBookingWebService getViteroWebService(@PathParam("subIdentifier") String subIdentifier) {
		ViteroBookingWebService service = new ViteroBookingWebService(courseOres, subIdentifier);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
	
	/**
	 * To get the GoToMeeting web service of the specified course
	 * and course element.
	 * 
	 * @param subIdentifier The identifier of the course element
	 * @return The GoToMeeting web service
	 */
	@Path("gotomeeting/{subIdentifier}")
	public GoToTrainingWebService getGoToMeetingWebService(@PathParam("subIdentifier") String subIdentifier) {
		RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		GoToTrainingWebService service = new GoToTrainingWebService(courseRe, subIdentifier);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
	
	/**
	 * To get the web service for the lecture blocks of a specific course.
	 * 
	 * @return The web service for lecture blocks.
	 */
	@Path("lectureblocks")
	public LectureBlocksWebService getLectureBlocksWebService(@Context HttpServletRequest request) {
		RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		boolean administrator = isManagerWithLectures(request);
		LectureBlocksWebService service = new LectureBlocksWebService(courseRe, administrator);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
	
	/**
	 * To get the web service for the reminders of a specific course.
	 * 
	 * @return The web service for reminders.
	 */
	@Path("reminders")
	public RemindersWebService getRemindersWebService(@Context HttpServletRequest request) {
		RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		boolean administrator = isManager(request);
		RemindersWebService service = new RemindersWebService(courseRe, administrator);
		CoreSpringFactory.autowireObject(service);
		return service;
	}

	/**
	 * Publish the course.
	 * 
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The metadatas of the created course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param locale The course locale
	 * @param request The HTTP request
	 * @return It returns the metadatas of the published course.
	 */
	@POST
	@Path("publish")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response publishCourse(@QueryParam("locale") Locale locale,
			@QueryParam("access") Integer access, @QueryParam("membersOnly") Boolean membersOnly,
			@QueryParam("status") String status, @QueryParam("allUsers") Boolean allUsers, @QueryParam("guests") Boolean guests,
			@Context HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		if (!isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		boolean accessGuests = false;
		boolean accessAllUsers = false;
		RepositoryEntryStatusEnum accessStatus = RepositoryEntryStatusEnum.preparation;
		if(RepositoryEntryStatusEnum.isValid(status)) {
			accessStatus = RepositoryEntryStatusEnum.valueOf(status);
			accessAllUsers = allUsers != null && allUsers.booleanValue();
			accessGuests = guests != null && guests.booleanValue();
		} else if(access != null) {
			boolean members = membersOnly != null && membersOnly.booleanValue();
			accessStatus = RestSecurityHelper.convertToEntryStatus(access.intValue(), members);
			accessAllUsers = access.longValue() >= 3;
			accessGuests = access.longValue() >= 4;
		}

		CourseFactory.publishCourse(course, accessStatus, accessAllUsers, accessGuests, ureq.getIdentity(), locale);
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}
	
	/**
	 * Get the access configuration of the course by id.
	 * 
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryAccessVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The access configuration of the course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOACCESS}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the <code>RepositoryEntryAccessVO</code> object representing the access configuration of the course.
	 */
	@GET
	@Path("access")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAccess(@Context HttpServletRequest request) {
		if(!isAuthor(request) && !isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEntryAccessVO accessVo = RepositoryEntryAccessVO.valueOf(entry);
		return Response.ok(accessVo).build();
	}

	/**
	 * Get the metadatas of the course by id
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The metadatas of the created course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response findById(@Context HttpServletRequest request) {
		if (!isCourseAccessible(course, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}
	

	/**
	 * Get the OLAT resource of the course specified by its id.
	 * 
	 * @response.representation.200.qname {http://www.example.com}olatResourceVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The OLAT resource of the course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_OLATRESOURCEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Path("resource")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOlatResource(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		OlatResourceVO vo = new OlatResourceVO(course);
		return Response.ok(vo).build();
	}
	
	/**
	 * Export the course
	 * @response.representation.200.mediaType application/zip
	 * @response.representation.200.doc The course as a ZIP file
	 * @response.representation.401.doc Not authorized to export the course
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Path("file")
	@Produces({ "application/zip", MediaType.APPLICATION_OCTET_STREAM })
	public Response getRepoFileById(@Context HttpServletRequest request) {
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if (re == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		RepositoryHandler typeToDownload = repositoryHandlerFactory.getRepositoryHandler(re);
		if (typeToDownload == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(request);
		boolean canDownload = re.getCanDownload() && typeToDownload.supportsDownload();
		if (isManager(request)) {
			canDownload = true;
		} else if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		if(!canDownload) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		OLATResource ores = resourceManager.findResourceable(re.getOlatResource());
		if (ores == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		boolean isAlreadyLocked = typeToDownload.isLocked(ores);
		LockResult lockResult = null;
		try {
			lockResult = typeToDownload.acquireLock(ores, identity);
			if (lockResult == null || (lockResult.isSuccess() && !isAlreadyLocked)) {
				MediaResource mr = typeToDownload.getAsMediaResource(ores);
				if (mr != null) {
					repositoryService.incrementDownloadCounter(re);
					if(mr instanceof StreamingOutput) {
						return Response.ok(mr).cacheControl(cc).build(); // success
					} else {
					
						return Response.ok(mr.getInputStream()).cacheControl(cc).build(); // success
					}
				} else {
					return Response.serverError().status(Status.NO_CONTENT).build();
				}
			} else {
				return Response.serverError().status(Status.CONFLICT).build();
			}
		} finally {
			if ((lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
				typeToDownload.releaseLock(lockResult);
			}
		}
	}

	/**
	 * Delete a course by id.
	 * 
	 * @response.representation.200.doc The metadatas of the deleted course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@DELETE
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteCourse(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		UserRequest ureq = getUserRequest(request);
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		ErrorList errors = repositoryService.deletePermanently(re, ureq.getIdentity(), ureq.getUserSession().getRoles(), ureq.getLocale());
		if(errors.hasErrors()) {
			return Response.serverError().status(500).build();
		}
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_DELETE, getClass(),
				LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		return Response.ok().build();
	}
	
	/**
	 * Change the status of a course by id. The possible status are:
	 * <ul>
	 * 	<li>closed</li>
	 * 	<li>unclosed</li>
	 * 	<li>unpublished</li>
	 * 	<li>deleted</li>
	 * 	<li>restored</li>
	 * </ul>
	 * 
	 * @response.representation.200.doc The metadatas of the deleted course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@POST
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("status")
	public Response deleteCoursePermanently(@FormParam("newStatus") String newStatus, @Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if("closed".equals(newStatus)) {
			repositoryService.closeRepositoryEntry(re, null, false);
			log.info(Tracing.M_AUDIT, "REST closing course: " + re.getDisplayname() + " [" + re.getKey() + "]");
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if("unclosed".equals(newStatus)) {
			repositoryService.uncloseRepositoryEntry(re);
			log.info(Tracing.M_AUDIT, "REST unclosing course: " + re.getDisplayname() + " [" + re.getKey() + "]");
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_UPDATE, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if("deleted".equals(newStatus)) {
			Identity identity = getIdentity(request);
			repositoryService.deleteSoftly(re, identity, true, false);
			log.info(Tracing.M_AUDIT, "REST deleting (soft) course: " + re.getDisplayname() + " [" + re.getKey() + "]");
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_TRASH, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if("restored".equals(newStatus)) {
			repositoryService.restoreRepositoryEntry(re);
			log.info(Tracing.M_AUDIT, "REST restoring course: " + re.getDisplayname() + " [" + re.getKey() + "]");
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_RESTORE, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		}
		return Response.ok().build();
	}
	
	/**
	 * Get the configuration of the course
	 * @response.representation.200.qname {http://www.example.com}courseConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The configuration of the course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSECONFIGVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@GET
	@Path("configuration")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getConfiguration(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		CourseConfigVO vo = ObjectFactory.getConfig(course);
		return Response.ok(vo).build();
	}
	
	/**
	 * Update the course configuration
	 * @response.representation.200.qname {http://www.example.com}courseConfigVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The metadatas of the created course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSECONFIGVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param courseId The course resourceable's id
	 * @param calendar Enable/disable the calendar (value: true/false) (optional)
	 * @param chat Enable/disable the chat (value: true/false) (optional)
	 * @param cssLayoutRef Set the custom CSS file for the layout (optional)
	 * @param efficencyStatement Enable/disable the efficencyStatement (value: true/false) (optional)
	 * @param glossarySoftkey Set the glossary (optional)
	 * @param sharedFolderSoftkey Set the shared folder (optional)
	 * @param request The HTTP request
	 * @return It returns the XML/Json representation of the <code>CourseConfig</code>
	 *         object representing the course configuration.
	 */
	@POST
	@Path("configuration")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateConfiguration(@PathParam("courseId") Long courseId,
			@FormParam("calendar") Boolean calendar, @FormParam("chat") Boolean chat,
			@FormParam("cssLayoutRef") String cssLayoutRef, @FormParam("efficencyStatement") Boolean efficencyStatement,
			@FormParam("glossarySoftkey") String glossarySoftkey, @FormParam("sharedFolderSoftkey") String sharedFolderSoftkey,
			@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		ICourse editedCourse = CourseFactory.openCourseEditSession(courseId);
		//change course config
		CourseConfig courseConfig = editedCourse.getCourseEnvironment().getCourseConfig();
		if(calendar != null) {
			courseConfig.setCalendarEnabled(calendar.booleanValue());
		}
		if(chat != null) {
			courseConfig.setChatIsEnabled(chat.booleanValue());
		}
		if(StringHelper.containsNonWhitespace(cssLayoutRef)) {
			courseConfig.setCssLayoutRef(cssLayoutRef);
		}
		if(efficencyStatement != null) {
			courseConfig.setEfficencyStatementIsEnabled(efficencyStatement.booleanValue());
		}
		if(StringHelper.containsNonWhitespace(glossarySoftkey)) {
			courseConfig.setGlossarySoftKey(glossarySoftkey);
		}
		if(StringHelper.containsNonWhitespace(sharedFolderSoftkey)) {
			courseConfig.setSharedFolderSoftkey(sharedFolderSoftkey);
		}

		CourseFactory.setCourseConfig(editedCourse.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(editedCourse.getResourceableId(),true);
		
		CourseConfigVO vo = ObjectFactory.getConfig(editedCourse);
		return Response.ok(vo).build();
	}
	
	/**
	 * Get the runstructure of the course by id
	 * @response.representation.200.mediaType application/xml
	 * @response.representation.200.doc The run structure of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@GET
	@Path("runstructure")
	@Produces(MediaType.APPLICATION_XML)
	public Response findRunStructureById(@Context HttpServletRequest httpRequest, @Context Request request) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		VFSItem runStructureItem = course.getCourseBaseContainer().resolve("runstructure.xml");
		Date lastModified = new Date(runStructureItem.getLastModified());
		Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
		if(response == null) {
			return Response.ok(myXStream.toXML(course.getRunStructure())).build();
		}
		return response.build();	
	}
	
	/**
	 * Get the editor tree model of the course by id
	 * @response.representation.200.mediaType application/xml
	 * @response.representation.200.doc The editor tree model of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return It returns the XML representation of the <code>Editor model</code>
	 *         object representing the course.
	 */
	@GET
	@Path("editortreemodel")
	@Produces(MediaType.APPLICATION_XML)
	public Response findEditorTreeModelById(@Context HttpServletRequest httpRequest, @Context Request request) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		VFSItem editorModelItem = course.getCourseBaseContainer().resolve("editortreemodel.xml");
		Date lastModified = new Date(editorModelItem.getLastModified());
		
		Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
		if(response == null) {
			return Response.ok(myXStream.toXML(course.getEditorTreeModel())).build();
		}
		return response.build();
	}

	@GET
	@Path("organisations")
	public Response getOrganisations(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Organisation> organisations = repositoryService.getOrganisations(repositoryEntry);
		OrganisationVO[] orgVoes = new OrganisationVO[organisations.size()];
		for(int i=organisations.size(); i-->0; ) {
			orgVoes[i] = OrganisationVO.valueOf(organisations.get(i));
		}
		return Response.ok(orgVoes).build();
	}
	
	@PUT
	@Path("organisations/{organisationKey}")
	public Response addOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Organisation> organisations = repositoryService.getOrganisations(repositoryEntry);
		for(Organisation organisation:organisations) {
			if(organisation.getKey().equals(organisationKey)) {
				return Response.ok().status(Status.NOT_MODIFIED).build();
			}
		}
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.ok().status(Status.NOT_FOUND).build();
		}
		repositoryService.addOrganisation(repositoryEntry, organisation);
		return Response.ok().build();
	}
	
	@DELETE
	@Path("organisations/{organisationKey}")
	public Response removeOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation != null) {
			repositoryService.removeOrganisation(repositoryEntry, organisation);
		}
		return Response.ok().build();
	}
	
	/**
	 * Get all owners and authors of the course
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of authors
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("authors")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthors(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> owners = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		
		int count = 0;
		UserVO[] authors = new UserVO[owners.size()];
		for(Identity owner:owners) {
			authors[count++] = UserVOFactory.get(owner);
		}
		return Response.ok(authors).build();
	}
	
	/**
	 * Get all coaches of the course (don't follow the groups)
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of coaches
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("tutors")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTutors(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> coachList = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		
		int count = 0;
		UserVO[] coaches = new UserVO[coachList.size()];
		for(Identity coach:coachList) {
			coaches[count++] = UserVOFactory.get(coach);
		}
		return Response.ok(coaches).build();
	}
	
	/**
	 * Get all participants of the course (don't follow the groups)
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of participants
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("participants")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipants(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> participantList = repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
		
		int count = 0;
		UserVO[] participants = new UserVO[participantList.size()];
		for(Identity participant:participantList) {
			participants[count++] = UserVOFactory.get(participant);
		}
		return Response.ok(participants).build();
	}
	
	/**
	 * Get this specific author and owner of the course
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The author
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found or the user is not an onwer or author of the course
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns an <code>UserVO</code>
	 */
	@GET
	@Path("authors/{identityKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthor(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity author = securityManager.loadIdentityByKey(identityKey, false);
		if(repositoryService.hasRole(author, repositoryEntry, GroupRoles.owner.name()) &&
				organisationService.hasRole(author, OrganisationRoles.author)) {
			UserVO vo = UserVOFactory.get(author);
			return Response.ok(vo).build();
		}
		return Response.ok(author).build();
	}
	
	/**
	 * Add an owner and author to the course
	 * @response.representation.200.doc The user is an author and owner of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as owner and author of the course
	 */
	@PUT
	@Path("authors/{identityKey}")
	public Response addAuthor(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		Identity author = securityManager.loadIdentityByKey(identityKey, false);
		if(author == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(httpRequest);
		boolean hasBeenAuthor = organisationService.hasRole(author, OrganisationRoles.author);
		if(!hasBeenAuthor) {
			//not an author already, add this identity to the security group "authors"
			organisationService.addMember(author, OrganisationRoles.author);
			log.info(Tracing.M_AUDIT, "User::" + identity.getKey() + " added system role::" + OrganisationRoles.author + " to user::" + author.getKey() + " via addAuthor method in course REST API");
		}
		
		//add the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> authors = Collections.singletonList(author);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		repositoryManager.addOwners(identity, identitiesAddedEvent, repositoryEntry, new MailPackage(false));
		
		return Response.ok().build();
	}
	
	@PUT
	@Path("authors")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addAuthors(UserVO[] authors, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		List<Identity> authorList = loadIdentities(authors);
		Identity identity = getIdentity(httpRequest);

		for(Identity author:authorList) {
			boolean hasBeenAuthor = organisationService.hasRole(author, OrganisationRoles.author);
			if(!hasBeenAuthor) {
				//not an author already, add this identity to the security group "authors"
				organisationService.addMember(author, OrganisationRoles.author);
				log.info(Tracing.M_AUDIT, "User::" + identity.getKey() + " added system role::" + OrganisationRoles.author + " to user::" + author.getKey() + " via addAuthor method in course REST API");
			}
		}
		
		//add the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authorList);
		repositoryManager.addOwners(identity, identitiesAddedEvent, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Remove an owner and author to the course
	 * @response.representation.200.doc The user was successfully removed as owner of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as owner of the course
	 */
	@DELETE
	@Path("authors/{identityKey}")
	public Response removeAuthor(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity author = securityManager.loadIdentityByKey(identityKey, false);
		if(author == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(httpRequest);
		
		//remove the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> authors = Collections.singletonList(author);
		repositoryManager.removeOwners(identity, authors, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Add a coach to the course
	 * @response.representation.200.doc The user is a coach of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as coach of the course
	 */
	@PUT
	@Path("tutors/{identityKey}")
	public Response addCoach(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		Identity tutor = securityManager.loadIdentityByKey(identityKey, false);
		if(tutor == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(httpRequest);
		UserRequest ureq = getUserRequest(httpRequest);
		
		//add the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> tutors = Collections.singletonList(tutor);
		IdentitiesAddEvent iae = new IdentitiesAddEvent(tutors);
		repositoryManager.addTutors(identity, ureq.getUserSession().getRoles(), iae, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	@PUT
	@Path("tutors")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addCoaches(UserVO[] coaches, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<Identity> coachList = loadIdentities(coaches);
		Identity identity = getIdentity(httpRequest);
		UserRequest ureq = getUserRequest(httpRequest);
		
		//add the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		IdentitiesAddEvent iae = new IdentitiesAddEvent(coachList);
		repositoryManager.addTutors(identity, ureq.getUserSession().getRoles(), iae, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Remove a coach from the course
	 * @response.representation.200.doc The user was successfully removed as coach of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as coach of the course
	 */
	@DELETE
	@Path("tutors/{identityKey}")
	public Response removeCoach(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity coach = securityManager.loadIdentityByKey(identityKey, false);
		if(coach == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(httpRequest);
		
		//remove the user as coach of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> coaches = Collections.singletonList(coach);
		repositoryManager.removeTutors(identity, coaches, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Add an participant to the course
	 * @response.representation.200.doc The user is a participant of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as owner and author of the course
	 */
	@PUT
	@Path("participants/{identityKey}")
	public Response addParticipant(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		Identity participant = securityManager.loadIdentityByKey(identityKey, false);
		if(participant == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(httpRequest);
		UserRequest ureq = getUserRequest(httpRequest);
		
		//add the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> participants = Collections.singletonList(participant);
		IdentitiesAddEvent iae = new IdentitiesAddEvent(participants);
		repositoryManager.addParticipants(identity, ureq.getUserSession().getRoles(), iae, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Add an participant to the course
	 * @response.representation.200.doc The user is a participant of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as owner and author of the course
	 */
	@PUT
	@Path("participants")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addParticipants(UserVO[] participants,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		List<Identity> participantList = loadIdentities(participants);
		Identity identity = getIdentity(httpRequest);
		UserRequest ureq = getUserRequest(httpRequest);
		
		//add the participants to the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		IdentitiesAddEvent iae = new IdentitiesAddEvent(participantList);
		repositoryManager.addParticipants(identity, ureq.getUserSession().getRoles(), iae, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Remove a participant from the course
	 * @response.representation.200.doc The user was successfully removed as participant of the course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as participant of the course
	 */
	@DELETE
	@Path("participants/{identityKey}")
	public Response removeParticipant(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity participant = securityManager.loadIdentityByKey(identityKey, false);
		if(participant == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity identity = getIdentity(httpRequest);
		
		//remove the user as participant of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> participants = Collections.singletonList(participant);
		repositoryManager.removeParticipants(identity, participants, repositoryEntry, new MailPackage(false), false);
		return Response.ok().build();
	}
	
	private List<Identity> loadIdentities(UserVO[] users) {
		List<Long> identityKeys = new ArrayList<>();
		for(UserVO user:users) {
			identityKeys.add(user.getKey());
		}
		return securityManager.loadIdentityByKeys(identityKeys);
	}
	
	private boolean isAuthor(HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		Identity identity = ureq.getIdentity();
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		return repositoryService.hasRoleExpanded(identity, entry, OrganisationRoles.author.name());
	}
	
	private boolean isManager(HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		Identity identity = ureq.getIdentity();
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		return repositoryService.hasRoleExpanded(identity, entry,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				GroupRoles.owner.name());
	}
	
	private boolean isManagerWithLectures(HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		Identity identity = ureq.getIdentity();
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		return repositoryService.hasRoleExpanded(identity, entry,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				OrganisationRoles.lecturemanager.name(), GroupRoles.owner.name());
	}
	
	public static boolean isCourseAccessible(ICourse course, HttpServletRequest request) {
		Identity identity = getIdentity(request);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		boolean manager = repositoryService.hasRoleExpanded(identity, entry,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				GroupRoles.owner.name());
		if(manager) {
			return true;
		}

		ACService acManager = CoreSpringFactory.getImpl(ACService.class);
		AccessResult result = acManager.isAccessible(entry, identity, false);
		return result.isAccessible();
	}
}
