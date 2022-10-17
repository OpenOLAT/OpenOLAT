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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;

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
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.restapi.CourseAssessmentModeWebService;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.cal.CalSecurityCallback;
import org.olat.course.nodes.cal.CalSecurityCallbackFactory;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.gotomeeting.restapi.GoToTrainingWebService;
import org.olat.modules.lecture.restapi.LectureBlocksWebService;
import org.olat.modules.reminder.restapi.RemindersWebService;
import org.olat.modules.vitero.restapi.ViteroBookingWebService;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.manager.RepositoryEntryEducationalTypeDAO;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.OlatResourceVO;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.olat.restapi.support.vo.RepositoryEntryMetadataVO;
import org.olat.user.restapi.OrganisationVO;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
	static {
		XStreamHelper.allowDefaultPackage(myXStream);
	}
	
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
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private RepositoryEntryEducationalTypeDAO educationalTypeDao;

	
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
		CourseGroupWebService ws = new CourseGroupWebService(re, courseOres);
		CoreSpringFactory.autowireObject(ws);
		return ws;
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
				&& (calendarModule.isEnableCourseToolCalendar() || calendarModule.isEnableCourseElementCalendar())) {
			UserRequest ureq = getUserRequest(request);
			Roles roles = ureq.getUserSession().getRoles();
			
			IdentityEnvironment ienv = new IdentityEnvironment(ureq.getIdentity(), roles);
			RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq.getIdentity(), roles, courseEntry);
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment(), null, null, null, null, reSecurity);
			CalSecurityCallback secCallback = CalSecurityCallbackFactory.createCourseCalendarCallback(userCourseEnv);
			KalendarRenderWrapper wrapper = CourseCalendars.getCourseCalendarWrapper(ureq, userCourseEnv, secCallback);
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
	 * To get the web service to manage the assessment modes of a specific course.
	 * 
	 * @param request the request
	 * @return The web service for assessment modes.
	 */
	@Path("assessmentmodes")
	public CourseAssessmentModeWebService getAssessmentModeWebService(@Context HttpServletRequest request) {
		RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		boolean administrator = isManager(request);
		CourseAssessmentModeWebService service = new CourseAssessmentModeWebService(courseRe, administrator);
		CoreSpringFactory.autowireObject(service);
		return service;
	}
	
	/**
	 * Publish the course.
	 * 
	 * @param locale The course locale
	 * @param request The HTTP request
	 * @return It returns the metadatas of the published course.
	 */
	@POST
	@Path("publish")
	@Operation(summary = "Publish the course", description = "Publish the course")
	@ApiResponse(responseCode = "200", description = "The metadatas of the created course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response publishCourse(@QueryParam("locale") Locale locale, @QueryParam("status") String status,
			@Context HttpServletRequest request) {
		UserRequest ureq = getUserRequest(request);
		if (!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		RepositoryEntryStatusEnum accessStatus = RepositoryEntryStatusEnum.preparation;
		if(RepositoryEntryStatusEnum.isValid(status)) {
			accessStatus = RepositoryEntryStatusEnum.valueOf(status);
		}

		CourseFactory.publishCourse(course, accessStatus, ureq.getIdentity(), locale);
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}
	

	/**
	 * Return metadata of the repository entry, educational type, objectives...
	 */
	@GET
	@Path("metadata")
	@Operation(summary = "Get lots of metadata of the repository entry", description = "Get lots of metadata of the repository entry from description up-to educational type and technical type")
	@ApiResponse(responseCode = "200", description = "The access configuration of the repository entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryEntryMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryEntryMetadataVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The repository entry not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMetadata(@Context HttpServletRequest request) {
		if(!isAuthor(request) && !isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEntryMetadataVO metadataVo = RepositoryEntryMetadataVO.valueOf(courseRe);
		return Response.ok(metadataVo).build();
	}
	
	/**
	 * Update the metadata of the repository entry. The NULL values will be updated as NULL.
	 * 
	 * @param metadataVo The metadata object
	 * @param request The HTTP request
	 * @return Updated metadata
	 */
	@PUT
	@Path("metadata")
	@Operation(summary = "Update lots of metadata of the repository entry", description = "Update lots of metadata of the repository entry from description up-to educational type and technical type. The NULL values will be updated as NULL values.")
	@ApiResponse(responseCode = "200", description = "The access configuration of the repository entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryEntryMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryEntryMetadataVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The repository entry not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putMetadata(RepositoryEntryMetadataVO metadataVo, @Context HttpServletRequest request) {
		return updateMetadata(metadataVo, request);
	}
	
	/**
	 * Update the metadata of the repository entry. The NULL values will be updated as NULL.
	 * 
	 * @param metadataVo The metadata object
	 * @param request The HTTP request
	 * @return Updated metadata
	 */
	@POST
	@Path("metadata")
	@Operation(summary = "Update lots of metadata of the repository entry", description = "Update lots of metadata of the repository entry from description up-to educational type and technical type. The NULL values will be updated as NULL values")
	@ApiResponse(responseCode = "200", description = "The access configuration of the repository entry", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryEntryMetadataVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryEntryMetadataVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The repository entry not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postMetadata(RepositoryEntryMetadataVO metadataVo, @Context HttpServletRequest request) {
		return updateMetadata(metadataVo, request);
	}
	
	private Response updateMetadata(RepositoryEntryMetadataVO metadataVo, @Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(metadataVo.getKey() != null && !metadataVo.getKey().equals(courseRe.getKey())) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}

		RepositoryEntryLifecycle lifecycle = updateLifecycle(metadataVo.getLifecycle());
		RepositoryEntryEducationalType educationalType = null;
		if (metadataVo.getEducationalType() != null && metadataVo.getEducationalType().getKey() != null) {
			educationalType = educationalTypeDao.loadByKey(metadataVo.getEducationalType().getKey());
		}
		RepositoryEntry reloaded = repositoryManager.setDescriptionAndName(courseRe, metadataVo.getDisplayname(), metadataVo.getExternalRef(), metadataVo.getAuthors(),
				metadataVo.getDescription(), metadataVo.getTeaser(), metadataVo.getObjectives(), metadataVo.getRequirements(), metadataVo.getCredits(),
				metadataVo.getMainLanguage(), metadataVo.getLocation(), metadataVo.getExpenditureOfWork(), lifecycle, null, null, educationalType);
		
		return Response.ok(RepositoryEntryMetadataVO.valueOf(reloaded)).build();
	}
	
	private RepositoryEntryLifecycle updateLifecycle(RepositoryEntryLifecycleVO lifecycleVo) {
		RepositoryEntryLifecycle lifecycle = null;
		if (lifecycleVo != null) {
			if (lifecycleVo.getKey() != null) {
				lifecycle = lifecycleDao.loadById(lifecycleVo.getKey());
				if (lifecycle.isPrivateCycle()) {
					// check date
					String fromStr = lifecycleVo.getValidFrom();
					String toStr = lifecycleVo.getValidTo();
					String label = lifecycleVo.getLabel();
					String softKey = lifecycleVo.getSoftkey();
					Date from = ObjectFactory.parseDate(fromStr);
					Date to = ObjectFactory.parseDate(toStr);
					lifecycle.setLabel(label);
					lifecycle.setSoftKey(softKey);
					lifecycle.setValidFrom(from);
					lifecycle.setValidTo(to);
				}
			} else {
				String fromStr = lifecycleVo.getValidFrom();
				String toStr = lifecycleVo.getValidTo();
				String label = lifecycleVo.getLabel();
				String softKey = lifecycleVo.getSoftkey();
				Date from = ObjectFactory.parseDate(fromStr);
				Date to = ObjectFactory.parseDate(toStr);
				lifecycle = lifecycleDao.create(label, softKey, true, from, to);
			}
		}
		return lifecycle;
	}

	/**
	 * Get the metadatas of the course by id.
	 * 
	 * @param request The HTTP request
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Operation(summary = "Get the metadatas of the course by id", description = "Get the metadatas of the course by id")
	@ApiResponse(responseCode = "200", description = "The metadatas of the created course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response findById(@Context HttpServletRequest request) {
		if (!isCourseAccessible(course, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}
	

	/**
	 * Get the OLAT resource of the course specified by its id.
	 * 
	 * @param request The HTTP request
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Path("resource")
	@Operation(summary = "Get the OLAT resource of the course specified by its id", description = "Get the OLAT resource of the course specified by its id")
	@ApiResponse(responseCode = "200", description = "The OLAT resource of the course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OlatResourceVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OlatResourceVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOlatResource(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		OlatResourceVO vo = new OlatResourceVO(course);
		return Response.ok(vo).build();
	}
	
	
	/**
	 * Mostly for the last modification of the image.
	 *
	 * @param httpRequest The HTTP request
	 * @return Mostly the last modification of the image
	 */
	@HEAD
	@Path("image")
	@Operation(summary = "Head the teaser image of the course", description = "Head the teaser image of the course")
	@ApiResponse(responseCode = "200", description = "The image's last modification of the course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OlatResourceVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OlatResourceVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The image or course not found")
	@Produces({MediaType.WILDCARD})
	public Response headImage(@Context HttpServletRequest httpRequest) {
		if(!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VFSLeaf leaf = repositoryManager.getImage(entry);
		if(leaf == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Date lastModified = new Date(leaf.getLastModified());
		return Response.ok().lastModified(lastModified).build();
	}
	
	/**
	 * Download the teaser image of the course.
	 * 
	 * @param httpRequest The HTTP request
	 * @param request The request
	 * @return The image
	 */
	@GET
	@Path("image")
	@Operation(summary = "Get the teaser image of the course", description = "Get the teaser image of the course")
	@ApiResponse(responseCode = "200", description = "The image of the course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OlatResourceVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OlatResourceVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or image not found")
	@Produces({MediaType.WILDCARD})
	public Response getTeaser(@Context HttpServletRequest httpRequest, @Context Request request) {
		if(!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		VFSLeaf leaf = repositoryManager.getImage(entry);
		if(leaf instanceof LocalImpl) {
			File image = ((LocalImpl)leaf).getBasefile();
			Date lastModified = new Date(image.lastModified());
			Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
			if(response == null) {
				response = Response.ok(image).lastModified(lastModified).cacheControl(cc);
			}
			return response.build();
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Upload the image of a course.
	 * 
	 * @param identityKey The user key identifier of the user being searched
	 * @param file The image
	 * @param request The REST request
	 * @return Nothing
	 */
	@POST
	@Path("image")
	@Operation(summary = "Upload the teaser image of a course", description = "Upload the teaser image of a course")
	@ApiResponse(responseCode = "200", description = "Nothing if successful")
	@ApiResponse(responseCode = "403", description = "Not authorized")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response postPortrait(@Context HttpServletRequest httpRequest) {
		MultipartReader partsReader = null;
		try {
			if(!isManager(httpRequest)) {
				return Response.serverError().status(Status.FORBIDDEN).build();
			}
			
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			partsReader = new MultipartReader(httpRequest);
			File image = partsReader.getFile();
			String filename = partsReader.getFilename();
			if(repositoryManager.setImage(image, filename, entry)) {
				return Response.ok().build();
			}
			return Response.serverError().status(Status.CONFLICT).build();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
	}

	
	/**
	 * Export the course.
	 * 
	 * @param request The HTTP request
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Path("file")
	@Operation(summary = "Export the course", description = "Export the course")
	@ApiResponse(responseCode = "200", description = "The course as a ZIP file")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
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
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		if(!canDownload) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@DELETE
	@Operation(summary = "Delete a course by id", description = "Delete a course by id")
	@ApiResponse(responseCode = "200", description = "The metadatas of the deleted course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteCourse(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@POST
	@Operation(summary = "Change the status of a course by id", description = "Change the status of a course by id. The possible status are:\n" + 
			" <ul>\n" + 
			"  <li>preparation</li>\n" + 
			"  <li>review</li>\n" + 
			"  <li>coachpublished</li>\n" + 
			"  <li>published</li>\n" + 
			"  <li>closed</li>\n" + 
			"  <li>unclosed</li>\n" + 
			"  <li>unpublished</li>\n" + 
			"  <li>deleted</li>\n" + 
			"  <li>restored</li>\n" + 
			" </ul>")
	@ApiResponse(responseCode = "200", description = "Status of the course updated")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Path("status")
	public Response postStatus(@FormParam("newStatus") String newStatus, @Context HttpServletRequest request) {
		return updateStatus(newStatus, request);
	}
	
	@PUT
	@Operation(summary = "Change the status of a course by id", description = "Change the status of a course by id. The possible status are:\n" + 
			" <ul>\n" + 
			"  <li>preparation</li>\n" + 
			"  <li>review</li>\n" + 
			"  <li>coachpublished</li>\n" + 
			"  <li>published</li>\n" + 
			"  <li>closed</li>\n" + 
			"  <li>unclosed</li>\n" + 
			"  <li>unpublished</li>\n" + 
			"  <li>deleted</li>\n" + 
			"  <li>restored</li>\n" + 
			" </ul>")
	@ApiResponse(responseCode = "200", description = "Status of the course updated")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Path("status")
	public Response putStatus(@FormParam("newStatus") String newStatus, @Context HttpServletRequest request) {
		return updateStatus(newStatus, request);
	}
	
	public Response updateStatus(String newStatus, HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(RepositoryEntryStatusEnum.closed.name().equals(newStatus)) {
			repositoryService.closeRepositoryEntry(re, null, false);
			log.info(Tracing.M_AUDIT, "REST closing course: {} [{}]", re.getDisplayname(), re.getKey());
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CLOSE, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if("unclosed".equals(newStatus)) {
			repositoryService.uncloseRepositoryEntry(re);
			log.info(Tracing.M_AUDIT, "REST unclosing course: {} [{}]", re.getDisplayname(), re.getKey());
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_UPDATE, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if(RepositoryEntryStatusEnum.deleted.name().equals(newStatus)) {
			Identity identity = getIdentity(request);
			repositoryService.deleteSoftly(re, identity, true, false);
			log.info(Tracing.M_AUDIT, "REST deleting (soft) course: {} [{}]", re.getDisplayname(), re.getKey());
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_TRASH, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if("restored".equals(newStatus)) {
			repositoryService.restoreRepositoryEntry(re);
			log.info(Tracing.M_AUDIT, "REST restoring course: {} [{}]", re.getDisplayname(), re.getKey());
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_RESTORE, getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		} else if(RepositoryEntryStatusEnum.isValid(newStatus)) {
			RepositoryEntryStatusEnum nStatus = RepositoryEntryStatusEnum.valueOf(newStatus);
			repositoryManager.setStatus(re, nStatus);
			log.info("Change status of {} to {}", re, newStatus);
			ThreadLocalUserActivityLogger.log(RepositoryEntryStatusEnum.loggingAction(nStatus), getClass(),
					LoggingResourceable.wrap(re, OlatResourceableType.genRepoEntry));
		}
		return Response.ok().build();
	}
	
	/**
	 * Get the configuration of the course.
	 * 
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@GET
	@Path("configuration")
	@Operation(summary = "Get the configuration of the course", description = "Get the configuration of the course")
	@ApiResponse(responseCode = "200", description = "The configuration of the course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseConfigVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseConfigVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getConfiguration(@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		CourseConfigVO vo = ObjectFactory.getConfig(course);
		return Response.ok(vo).build();
	}
	
	/**
	 * Update the course configuration.
	 * 
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
	@Operation(summary = "Update the course configuration", description = "Update the course configuration")
	@ApiResponse(responseCode = "200", description = "The metadatas of the created course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseConfigVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseConfigVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateConfiguration(@PathParam("courseId") Long courseId,
			@FormParam("calendar") Boolean calendar, @FormParam("chat") Boolean chat,
			@FormParam("cssLayoutRef") String cssLayoutRef, @FormParam("efficencyStatement") Boolean efficencyStatement,
			@FormParam("glossarySoftkey") String glossarySoftkey, @FormParam("sharedFolderSoftkey") String sharedFolderSoftkey,
			@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Update the course configuration.
	 * 
	 * @param courseId The course resourceable's id
	 * @param configuration The new configuration (null elements are ignored)
	 * @param request The HTTP request
	 * @return It returns the XML/Json representation of the <code>CourseConfig</code>
	 *         object representing the course configuration.
	 */
	@PUT
	@Path("configuration")
	@Operation(summary = "Update the course configuration", description = "Update the course configuration")
	@ApiResponse(responseCode = "200", description = "The metadatas of the created course", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseConfigVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseConfigVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateConfiguration(@PathParam("courseId") Long courseId, CourseConfigVO configuration,
			@Context HttpServletRequest request) {
		if(!isManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		ICourse editedCourse = CourseFactory.openCourseEditSession(courseId);
		//change course config
		CourseConfig courseConfig = editedCourse.getCourseEnvironment().getCourseConfig();
		if(configuration.getCalendar() != null) {
			courseConfig.setCalendarEnabled(configuration.getCalendar().booleanValue());
		}
		if(configuration.getChat() != null) {
			courseConfig.setChatIsEnabled(configuration.getChat().booleanValue());
		}
		if(StringHelper.containsNonWhitespace(configuration.getCssLayoutRef())) {
			courseConfig.setCssLayoutRef(configuration.getCssLayoutRef());
		}
		if(configuration.getEfficencyStatement() != null) {
			courseConfig.setEfficencyStatementIsEnabled(configuration.getEfficencyStatement().booleanValue());
		}
		if(StringHelper.containsNonWhitespace(configuration.getGlossarySoftkey())) {
			courseConfig.setGlossarySoftKey(configuration.getGlossarySoftkey());
		}
		if(StringHelper.containsNonWhitespace(configuration.getSharedFolderSoftKey())) {
			courseConfig.setSharedFolderSoftkey(configuration.getSharedFolderSoftKey());
		}

		CourseFactory.setCourseConfig(editedCourse.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(editedCourse.getResourceableId(),true);
		
		CourseConfigVO vo = ObjectFactory.getConfig(editedCourse);
		return Response.ok(vo).build();
	}
	
	/**
	 * Get the runstructure of the course by id.
	 * 
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@GET
	@Path("runstructure")
	@Operation(summary = "Get the runstructure", description = "Get the runstructure of the course by id")
	@ApiResponse(responseCode = "200", description = "The run structure of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces(MediaType.APPLICATION_XML)
	public Response findRunStructureById(@Context HttpServletRequest httpRequest, @Context Request request) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Get the editor tree model of the course by id.
	 * 
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return It returns the XML representation of the <code>Editor model</code>
	 *         object representing the course.
	 */
	@GET
	@Path("editortreemodel")
	@Operation(summary = "Get the editor tree model", description = "Get the editor tree model of the course by id")
	@ApiResponse(responseCode = "200", description = "The editor tree model of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces(MediaType.APPLICATION_XML)
	public Response findEditorTreeModelById(@Context HttpServletRequest httpRequest, @Context Request request) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	@Operation(summary = "Get all organisations", description = "Get all organisations")
	@ApiResponse(responseCode = "200", description = "The array of organisations", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	public Response getOrganisations(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	@Operation(summary = "Put Organisation", description = "Link a new organisation to the course")
	@ApiResponse(responseCode = "200", description = "Organisation was put successfully")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Organisation not found")
	public Response addOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	@Operation(summary = "Delete Organisation", description = "Remove the link to the organisation")
	@ApiResponse(responseCode = "200", description = "Organisation Deleted")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Organisation not found")
	public Response removeOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation != null) {
			repositoryService.removeOrganisation(repositoryEntry, organisation);
		}
		return Response.ok().build();
	}
	
	/**
	 * Get all owners and authors of the course.
	 * 
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("authors")
	@Operation(summary = "Get all owners and authors of the course", description = "Get all owners and authors of the course")
	@ApiResponse(responseCode = "200", description = "The array of authors", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthors(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Get all coaches of the course (don't follow the groups).
	 * 
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("tutors")
	@Operation(summary = "Get all coaches of the course", description = "Get all coaches of the course (don't follow the groups or curriculums)")
	@ApiResponse(responseCode = "200", description = "The array of coaches", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTutors(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Get all participants of the course (don't follow the groups).
	 * 
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("participants")
	@Operation(summary = "Get all participants of the course", description = "Get all participants of the course (don't follow the groups or curriculums)")
	@ApiResponse(responseCode = "200", description = "The array of participants", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipants(@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Get this specific author and owner of the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns an <code>UserVO</code>
	 */
	@GET
	@Path("authors/{identityKey}")
	@Operation(summary = "Get this specific author and owner of the course", description = "Get this specific author and owner of the course")
	@ApiResponse(responseCode = "200", description = "The author", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found or the user is not an onwer or author of the course")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthor(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Add an owner and author to the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as owner and author of the course
	 */
	@PUT
	@Path("authors/{identityKey}")
	@Operation(summary = "Add an owner and author to the course", description = "Add an owner and author to the course")
	@ApiResponse(responseCode = "200", description = "The user is an author and owner of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	public Response addAuthor(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
			log.info(Tracing.M_AUDIT, "User::{} added system role::{} to user::{} via addAuthor method in course REST API",
					identity.getKey(), OrganisationRoles.author, author.getKey());
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
	@Operation(summary = "Add authors to the course", description = "Add authors to the course")
	@ApiResponse(responseCode = "200", description = "The authors have been added")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addAuthors(UserVO[] authors, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		List<Identity> authorList = loadIdentities(authors);
		Identity identity = getIdentity(httpRequest);

		for(Identity author:authorList) {
			boolean hasBeenAuthor = organisationService.hasRole(author, OrganisationRoles.author);
			if(!hasBeenAuthor) {
				//not an author already, add this identity to the security group "authors"
				organisationService.addMember(author, OrganisationRoles.author);
				log.info(Tracing.M_AUDIT, "User::{} added system role::{} to user::{} via addAuthor method in course REST API",
						identity.getKey(), OrganisationRoles.author, author.getKey());
			}
		}
		
		//add the author as owner of the course
		RepositoryEntry repositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authorList);
		repositoryManager.addOwners(identity, identitiesAddedEvent, repositoryEntry, new MailPackage(false));
		return Response.ok().build();
	}
	
	/**
	 * Remove an owner and author to the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as owner of the course
	 */
	@DELETE
	@Path("authors/{identityKey}")
	@Operation(summary = "Remove an owner and author to the course", description = "Remove an owner and author to the course")
	@ApiResponse(responseCode = "200", description = "The user was successfully removed as owner of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	public Response removeAuthor(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Add a coach to the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as coach of the course
	 */
	@PUT
	@Path("tutors/{identityKey}")
	@Operation(summary = "Add a coach to the course", description = "Add a coach to the course")
	@ApiResponse(responseCode = "200", description = "The user is a coach of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	public Response addCoach(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	@Operation(summary = "Add tutors to the course", description = "Add tutors to the course")
	@ApiResponse(responseCode = "200", description = "The tutors have been added")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addCoaches(UserVO[] coaches, @Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Remove a coach from the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as coach of the course
	 */
	@DELETE
	@Path("tutors/{identityKey}")
	@Operation(summary = "Remove a coach from the course", description = "Remove a coach from the course")
	@ApiResponse(responseCode = "200", description = "The user was successfully removed as coach of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	public Response removeCoach(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Add an participant to the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as owner and author of the course
	 */
	@PUT
	@Path("participants/{identityKey}")
	@Operation(summary = "Add an participant to the course", description = "Add an participant to the course")
	@ApiResponse(responseCode = "200", description = "The user is a participant of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	public Response addParticipant(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Add an participant to the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as owner and author of the course
	 */
	@PUT
	@Path("participants")
	@Operation(summary = "Add an participant to the course", description = "Add an participant to the course")
	@ApiResponse(responseCode = "200", description = "The user is a participant of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addParticipants(UserVO[] participants,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * Remove a participant from the course.
	 * 
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as participant of the course
	 */
	@DELETE
	@Path("participants/{identityKey}")
	@Operation(summary = "Remove a participant from the course", description = "Remove a participant from the course")
	@ApiResponse(responseCode = "200", description = "The user was successfully removed as participant of the course")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	public Response removeParticipant(@PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if (!isManager(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
		AccessResult result = acManager.isAccessible(entry, identity, null, false, false);
		return result.isAccessible();
	}
}
