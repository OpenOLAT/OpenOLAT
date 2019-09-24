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
import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.MultipartReader;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.CourseVOes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 *
 * Description:<br>
 * This web service handles the courses.
 *
 * <P>
 * Initial Date:  27 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses")
public class CoursesWebService {

	private static final Logger log = Tracing.createLoggerFor(CoursesWebService.class);

	private static final String VERSION = "1.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService ;
	@Autowired
	private OLATResourceManager olatResourceManager;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;


	/**
	 * The version of the Course Web Service
	 * @response.representation.200.mediaType text/plain
	 * @response.representation.200.doc The version of this specific Web Service
	 * @response.representation.200.example 1.0
	 * @return
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}

	/**
	 * Get all courses viewable by the authenticated user
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json, application/json;pagingspec=1.0
	 * @response.representation.200.doc List of visible courses
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVOes}
	 * @param start
	 * @param limit
	 * @param externalId Search with an external ID
	 * @param externalRef Search with an external reference
	 * @param managed (true / false) Search only managed / not managed groups
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseList(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit,
			@QueryParam("managed") Boolean managed,
			@QueryParam("externalId") String externalId, @QueryParam("externalRef") String externalRef,
			@QueryParam("repositoryEntryKey") String repositoryEntryKey,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		Roles roles = getRoles(httpRequest);
		Identity identity = getIdentity(httpRequest);
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(identity, roles, CourseModule.getCourseTypeName());
		params.setManaged(managed);

		if(StringHelper.containsNonWhitespace(externalId)) {
			params.setExternalId(externalId);
		}
		if(StringHelper.containsNonWhitespace(externalRef)) {
			params.setExternalRef(externalRef);
		}
		if(StringHelper.containsNonWhitespace(repositoryEntryKey) && StringHelper.isLong(repositoryEntryKey)) {
			try {
				params.setRepositoryEntryKeys(Collections.singletonList(Long.valueOf(repositoryEntryKey)));
			} catch (NumberFormatException e) {
				log.error("Cannot parse the following repository entry key: " + repositoryEntryKey);
			}
		}

		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = repositoryManager.countGenericANDQueryWithRolesRestriction(params);
			List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, start, limit, true);
			CourseVO[] vos = toCourseVo(repoEntries);
			CourseVOes voes = new CourseVOes();
			voes.setCourses(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);
			CourseVO[] vos = toCourseVo(repoEntries);
			return Response.ok(vos).build();
		}
	}

	public CourseVO[] toCourseVo(List<RepositoryEntry> repoEntries) {
		List<CourseVO> voList = new ArrayList<>();

		int count=0;
		for (RepositoryEntry repoEntry : repoEntries) {
			try {
				ICourse course = loadCourse(repoEntry.getOlatResource().getResourceableId());
				voList.add(ObjectFactory.get(repoEntry, course));
				if(count++ % 33 == 0) {
					dbInstance.commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("Cannot load the course with this repository entry: " + repoEntry, e);
			}
		}

		CourseVO[] vos = new CourseVO[voList.size()];
		voList.toArray(vos);
		return vos;
	}

	@Path("{courseId}")
	public CourseWebService getCourse(@PathParam("courseId") Long courseId)
	throws WebApplicationException {
		ICourse course = loadCourse(courseId);
		if(course == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		OLATResource ores = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		CourseWebService ws = new CourseWebService(ores, course);
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}

	/**
	 * Creates an empty course, or a copy from a course if the parameter copyFrom is set.
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The metadatas of the created course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param shortTitle The short title
	 * @param title The title
	 * @param sharedFolderSoftKey The repository entry key of a shared folder (optional)
	 * @param copyFrom The course primary key key to make a copy from (optional)
	 * @param initialAuthor The primary key of the initial author (optional)
	 * @param noAuthor True to create a course without the author
	 * @param request The HTTP request
	 * @return It returns the id of the newly created Course
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createEmptyCourse(@QueryParam("shortTitle") String shortTitle, @QueryParam("title") String title,
			@QueryParam("displayName") String displayName, @QueryParam("description") String description,
			@QueryParam("objectives") String objectives, @QueryParam("requirements") String requirements,
			@QueryParam("credits") String credits, @QueryParam("expenditureOfWork") String expenditureOfWork,
			@QueryParam("softKey") String softKey, @QueryParam("status") String status,
			@QueryParam("allUsers") Boolean allUsers, @QueryParam("guests") Boolean guests,
			@QueryParam("access") Integer access, @QueryParam("membersOnly") Boolean membersOnly,
			@QueryParam("externalId") String externalId, @QueryParam("externalRef") String externalRef,
			@QueryParam("authors") String authors, @QueryParam("location") String location,
			@QueryParam("managedFlags") String managedFlags, @QueryParam("sharedFolderSoftKey") String sharedFolderSoftKey,
			@QueryParam("copyFrom") Long copyFrom, @QueryParam("initialAuthor") Long initialAuthor,
			@QueryParam("setAuthor")  @DefaultValue("true") Boolean setAuthor,
			@QueryParam("organisationKey") Long organisationKey, @Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		CourseConfigVO configVO = new CourseConfigVO();
		configVO.setSharedFolderSoftKey(sharedFolderSoftKey);
		
		boolean accessGuests = false;
		boolean accessAllUsers = false;
		RepositoryEntryStatusEnum accessStatus = RepositoryEntryStatusEnum.preparation;
		if(StringHelper.containsNonWhitespace(status) && RepositoryEntryStatusEnum.isValid(status)) {
			accessStatus = RepositoryEntryStatusEnum.valueOf(status);
			accessAllUsers = allUsers != null && allUsers.booleanValue();
			accessGuests = guests != null && guests.booleanValue();
		} else if(access != null) {
			boolean accessMembersOnly = membersOnly != null && membersOnly.booleanValue();
			accessStatus = RestSecurityHelper.convertToEntryStatus(access.intValue(), accessMembersOnly);
			accessAllUsers = access.intValue() >= 3;
			accessGuests = access.intValue() >= 4;
		}
		
		if(!StringHelper.containsNonWhitespace(displayName)) {
			displayName = shortTitle;
		}

		ICourse course;
		UserRequest ureq = getUserRequest(request);
		Identity id = null;
		if(setAuthor != null && setAuthor.booleanValue()) {
			if (initialAuthor != null) {
				id = securityManager.loadIdentityByKey(initialAuthor);
			}
			if (id == null) {
				id = ureq.getIdentity();
			}
		}

		if(copyFrom != null) {
			course = copyCourse(copyFrom, ureq, id, shortTitle, title, displayName, description,
					objectives, requirements, credits, expenditureOfWork,
					softKey, accessStatus, accessAllUsers, accessGuests, organisationKey,
					authors, location, externalId, externalRef, managedFlags, configVO);
		} else {
			course = createEmptyCourse(id, shortTitle, title, displayName, description,
					objectives, requirements, credits, expenditureOfWork,
					softKey, accessStatus, accessAllUsers, accessGuests, organisationKey,
					authors, location, externalId, externalRef, managedFlags, configVO);
		}
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}

	/**
	 * Creates an empty course
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The metadatas of the created course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param courseVo The course
	 * @param request The HTTP request
	 * @return It returns the newly created course
	 */
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createEmptyCourse(CourseVO courseVo, @Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		UserRequest ureq = getUserRequest(request);

		CourseConfigVO configVO = new CourseConfigVO();
		ICourse course = createEmptyCourse(ureq.getIdentity(),
				courseVo.getTitle(), courseVo.getTitle(), courseVo.getTitle(), courseVo.getDescription(), null, null, null, null,
				courseVo.getSoftKey(), RepositoryEntryStatusEnum.preparation, false, false, courseVo.getOrganisationKey(),
				courseVo.getAuthors(), courseVo.getLocation(),
				courseVo.getExternalId(), courseVo.getExternalRef(), courseVo.getManagedFlags(),
				configVO);
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}

	/**
	 * Imports a course from a course archive zip file
	 * @response.representation.200.qname {http://www.example.com}courseVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The metadatas of the imported course
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_COURSEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param ownerUsername set the owner of the imported course to the user of this username.
	 * @param request The HTTP request
	 * @return It returns the imported course
	 */
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response importCourse(@QueryParam("ownerUsername") String ownerUsername, @Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		UserRequest ureq = RestSecurityHelper.getUserRequest(request);

		Identity identity = null;
		// Set the owner of the imported course to the user defined in the parameter
		if (ownerUsername != null && !ownerUsername.isEmpty() && isAuthor(request)) {
			identity = securityManager.findIdentityByName(ownerUsername);
			if(identity == null) {
				return Response.serverError().status(Status.BAD_REQUEST).build();
			}
		}
		if (identity == null) {
			identity = ureq.getIdentity();
		}

		MultipartReader partsReader = null;
		try {
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			long length = tmpFile.length();
			if(length > 0) {
				Long accessRaw = partsReader.getLongValue("access");
				String statusRaw = partsReader.getValue("status");
				String allUsersRaw = partsReader.getValue("allUsers");
				String guestsRaw = partsReader.getValue("guests");

				boolean accessGuests = false;
				boolean accessAllUsers = false;
				RepositoryEntryStatusEnum accessStatus = RepositoryEntryStatusEnum.preparation;
				if(StringHelper.containsNonWhitespace(statusRaw) && RepositoryEntryStatusEnum.isValid(statusRaw)) {
					accessStatus = RepositoryEntryStatusEnum.valueOf(statusRaw);
					accessAllUsers = "true".equals(allUsersRaw);
					accessGuests = "true".equals(guestsRaw);
				} else if(accessRaw != null) {
					String membersOnlyRaw = partsReader.getValue("membersOnly");
					boolean membersonly = "true".equals(membersOnlyRaw);
					accessStatus = RestSecurityHelper.convertToEntryStatus(accessRaw.intValue(), membersonly);
					accessAllUsers = accessRaw.intValue() >= 3;
					accessGuests = accessRaw.intValue() >= 4;
				}
			
				String softKey = partsReader.getValue("softkey");
				String displayName = partsReader.getValue("displayname");
				String organisation = partsReader.getValue("organisationkey");
				Long organisationKey = null;
				if(StringHelper.isLong(organisation)) {
					organisationKey = Long.valueOf(organisation);
				}
				
				ICourse course = importCourse(ureq, identity, tmpFile, displayName, softKey,
						accessStatus, accessAllUsers, accessGuests, organisationKey);
				CourseVO vo = ObjectFactory.get(course);
				return Response.ok(vo).build();
			}
			return Response.serverError().status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			log.error("Error while importing a file", e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
		return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	}

	public static ICourse loadCourse(Long courseId) {
		try {
			return CourseFactory.loadCourse(courseId);
		} catch(Exception ex) {
			log.error("cannot load course with id: " + courseId, ex);
			return null;
		}
	}

	private ICourse importCourse(UserRequest ureq, Identity identity, File fCourseImportZIP, String displayName,
			String softKey, RepositoryEntryStatusEnum status, boolean allUsers, boolean guests, Long organisationKey) {

		log.info("REST Import course " + displayName + " START");
		if(!StringHelper.containsNonWhitespace(displayName)) {
			displayName = "import-" + UUID.randomUUID();
		}
		
		Organisation organisation;
		if(organisationKey == null) {
			organisation = organisationService.getDefaultOrganisation();
		} else {
			organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
			
		}
		
		if(organisation != null) {
			Identity ureqIdentity = ureq.getIdentity();
			if(!organisationService.hasRole(ureqIdentity, organisation,
					OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager,
					OrganisationRoles.author)) {
				throw new WebApplicationException(Status.FORBIDDEN);
			}
		}

		RepositoryHandler handler = handlerFactory.getRepositoryHandler(CourseModule.getCourseTypeName());
		RepositoryEntry re = handler.importResource(identity, null, displayName, null, true, organisation, Locale.ENGLISH, fCourseImportZIP, null);
		if(StringHelper.containsNonWhitespace(softKey)) {
			re.setSoftkey(softKey);
			re = repositoryService.update(re);
		}
		log.info("REST Import course " + displayName + " END");

		//publish
		log.info("REST Publish course " + displayName + " START");
		ICourse course = CourseFactory.loadCourse(re);
		CourseFactory.publishCourse(course, status, allUsers, guests, identity, ureq.getLocale());
		log.info("REST Publish course " + displayName + " END");
		return course;
	}

	private ICourse copyCourse(Long copyFrom, UserRequest ureq, Identity initialAuthor, String shortTitle, String longTitle, String displayName,
			String description, String objectives, String requirements, String credits, String expenditureOfWork, String softKey,
			RepositoryEntryStatusEnum status, boolean allUsers, boolean guests, Long organisationKey,
			String authors, String location, String externalId, String externalRef, String managedFlags, CourseConfigVO courseConfigVO) {

		OLATResourceable originalOresTrans = OresHelper.createOLATResourceableInstance(CourseModule.class, copyFrom);
		RepositoryEntry src = repositoryManager.lookupRepositoryEntry(originalOresTrans, false);
		if(src == null) {
			src = repositoryManager.lookupRepositoryEntry(copyFrom, false);
		}
		if(src == null) {
			log.warn("Cannot find course to copy from: " + copyFrom);
			return null;
		}
		OLATResource originalOres = olatResourceManager.findResourceable(src.getOlatResource());
		boolean isAlreadyLocked = handlerFactory.getRepositoryHandler(src).isLocked(originalOres);
		LockResult lockResult = handlerFactory.getRepositoryHandler(src).acquireLock(originalOres, ureq.getIdentity());

		if(lockResult == null || (lockResult != null && lockResult.isSuccess()) && !isAlreadyLocked) {
			//create new repo entry
			String name;
			if(description == null || description.trim().length() == 0) {
				description = src.getDescription();
			}

			if (courseConfigVO != null && StringHelper.containsNonWhitespace(displayName)) {
				name = displayName;
			} else {
				name = "Copy of " + src.getDisplayname();
			}

			String resName = src.getResourcename();
			if (resName == null) {
				resName = "";
			}
			
			Organisation organisation;
			if(organisationKey == null) {
				organisation = organisationService.getDefaultOrganisation();
			} else {
				organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
			}

			OLATResource sourceResource = src.getOlatResource();
			OLATResource copyResource = olatResourceManager.createOLATResourceInstance(sourceResource.getResourceableTypeName());
			RepositoryEntry preparedEntry = repositoryService.create(initialAuthor, null, resName, name,
					description, copyResource, RepositoryEntryStatusEnum.preparation, organisation);

			RepositoryHandler handler = handlerFactory.getRepositoryHandler(src);
			preparedEntry = handler.copy(initialAuthor, src, preparedEntry);

			preparedEntry.setCanDownload(src.getCanDownload());
			if(StringHelper.containsNonWhitespace(softKey)) {
				preparedEntry.setSoftkey(softKey);
			}
			if(StringHelper.containsNonWhitespace(externalId)) {
				preparedEntry.setExternalId(externalId);
			}
			if(StringHelper.containsNonWhitespace(externalRef)) {
				preparedEntry.setExternalRef(externalRef);
			}
			if(StringHelper.containsNonWhitespace(authors)) {
				preparedEntry.setAuthors(authors);
			}
			if(StringHelper.containsNonWhitespace(location)) {
				preparedEntry.setLocation(location);
			}
			if(StringHelper.containsNonWhitespace(managedFlags)) {
				preparedEntry.setManagedFlagsString(managedFlags);
			}
			if(StringHelper.containsNonWhitespace(objectives)) {
				preparedEntry.setObjectives(objectives);
			}
			if(StringHelper.containsNonWhitespace(credits)) {
				preparedEntry.setCredits(credits);
			}
			if(StringHelper.containsNonWhitespace(requirements)) {
				preparedEntry.setRequirements(requirements);
			}
			if(StringHelper.containsNonWhitespace(expenditureOfWork)) {
				preparedEntry.setExpenditureOfWork(expenditureOfWork);
			}
			preparedEntry.setEntryStatus(status);
			preparedEntry.setAllUsers(allUsers);
			preparedEntry.setGuests(guests);
			preparedEntry.setAllowToLeaveOption(src.getAllowToLeaveOption());
			preparedEntry = repositoryService.update(preparedEntry);

			// copy image if available
			repositoryManager.copyImage(src, preparedEntry);
			ICourse course = prepareCourse(preparedEntry, shortTitle, longTitle, courseConfigVO);
			handlerFactory.getRepositoryHandler(src).releaseLock(lockResult);
			return course;
		} else {
			log.info("Course locked");
		}

		return null;
	}

	/**
	 * Create an empty course with some settings
	 * @param initialAuthor
	 * @param shortTitle
	 * @param longTitle
	 * @param softKey
	 * @param externalId
	 * @param externalRef
	 * @param managedFlags
	 * @param courseConfigVO
	 * @return
	 */
	private ICourse createEmptyCourse(Identity initialAuthor, String shortTitle, String longTitle, String reDisplayName,
			String description, String objectives, String requirements, String credits, String expenditureOfWork, String softKey,
			RepositoryEntryStatusEnum status, boolean allUsers, boolean guests,
			Long organisationKey, String authors, String location, String externalId, String externalRef,
			String managedFlags, CourseConfigVO courseConfigVO) {

		if(!StringHelper.containsNonWhitespace(reDisplayName)) {
			reDisplayName = shortTitle;
		}

		try {
			Organisation organisation;
			if(organisationKey == null) {
				organisation = organisationService.getDefaultOrganisation();
			} else {
				organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
			}

			// create a repository entry
			OLATResource resource = olatResourceManager.createOLATResourceInstance(CourseModule.class);
			RepositoryEntry addedEntry = repositoryService.create(initialAuthor, null, "-", reDisplayName, null,
					resource, status, organisation);
			if(StringHelper.containsNonWhitespace(softKey) && softKey.length() <= 30) {
				addedEntry.setSoftkey(softKey);
			}
			addedEntry.setLocation(location);
			addedEntry.setAuthors(authors);
			addedEntry.setExternalId(externalId);
			addedEntry.setExternalRef(externalRef);
			addedEntry.setManagedFlagsString(managedFlags);
			addedEntry.setDescription(description);
			addedEntry.setObjectives(objectives);
			addedEntry.setRequirements(requirements);
			addedEntry.setCredits(credits);
			addedEntry.setExpenditureOfWork(expenditureOfWork);
			if(RepositoryEntryManagedFlag.isManaged(addedEntry, RepositoryEntryManagedFlag.membersmanagement)) {
				addedEntry.setAllowToLeaveOption(RepositoryEntryAllowToLeaveOptions.never);
			} else {
				addedEntry.setAllowToLeaveOption(RepositoryEntryAllowToLeaveOptions.atAnyTime);//default
			}
			addedEntry.setAllUsers(allUsers);
			addedEntry.setGuests(guests);
			addedEntry = repositoryService.update(addedEntry);

			// create an empty course
			CourseFactory.createCourse(addedEntry, shortTitle, longTitle, "");
			return prepareCourse(addedEntry, shortTitle, longTitle, courseConfigVO);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	private ICourse prepareCourse(RepositoryEntry addedEntry, String shortTitle, String longTitle, CourseConfigVO courseConfigVO) {
		// set root node title
		String courseShortTitle = addedEntry.getDisplayname();
		if(StringHelper.containsNonWhitespace(shortTitle)) {
			courseShortTitle = shortTitle;
		}
		String courseLongTitle = addedEntry.getDisplayname();
		if(StringHelper.containsNonWhitespace(longTitle)) {
			courseLongTitle = longTitle;
		}

		ICourse course = CourseFactory.openCourseEditSession(addedEntry.getOlatResource().getResourceableId());
		course.getRunStructure().getRootNode().setShortTitle(Formatter.truncate(courseShortTitle, 25));
		course.getRunStructure().getRootNode().setLongTitle(courseLongTitle);

		CourseNode rootNode = ((CourseEditorTreeNode) course.getEditorTreeModel().getRootNode()).getCourseNode();
		rootNode.setShortTitle(Formatter.truncate(courseShortTitle, 25));
		rootNode.setLongTitle(courseLongTitle);

		if(courseConfigVO != null) {
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			if(StringHelper.containsNonWhitespace(courseConfigVO.getSharedFolderSoftKey())) {
				courseConfig.setSharedFolderSoftkey(courseConfigVO.getSharedFolderSoftKey());
			}
			if(courseConfigVO.getCalendar() != null) {
				courseConfig.setCalendarEnabled(courseConfigVO.getCalendar().booleanValue());
			}
			if(courseConfigVO.getChat() != null) {
				courseConfig.setChatIsEnabled(courseConfigVO.getChat().booleanValue());
			}
			if(courseConfigVO.getEfficencyStatement() != null) {
				courseConfig.setEfficencyStatementIsEnabled(courseConfigVO.getEfficencyStatement().booleanValue());
			}
			if(StringHelper.containsNonWhitespace(courseConfigVO.getCssLayoutRef())) {
				courseConfig.setCssLayoutRef(courseConfigVO.getCssLayoutRef());
			}
			if(StringHelper.containsNonWhitespace(courseConfigVO.getGlossarySoftkey())) {
				courseConfig.setGlossarySoftKey(courseConfigVO.getGlossarySoftkey());
			}
			CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		}

		CourseFactory.saveCourse(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		return CourseFactory.loadCourse(addedEntry);
	}
	
	private boolean isAuthor(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return (roles.isAuthor() || roles.isAdministrator() || roles.isLearnResourceManager());
		} catch (Exception e) {
			return false;
		}
	}
}
