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
import static org.olat.restapi.security.RestSecurityHelper.isAuthor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.repository.controllers.RepositoryEntryImageController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.CourseVOes;

/**
 * 
 * Description:<br>
 * This web service handles the courses.
 * 
 * <P>
 * Initial Date:  27 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Path("repo/courses")
public class CoursesWebService {
	
	private static final OLog log = Tracing.createLoggerFor(CoursesWebService.class);
	
	private static final String VERSION = "1.0";
	
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
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseList(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @Context HttpServletRequest httpRequest,
			@Context Request request) {
		RepositoryManager rm = RepositoryManager.getInstance();

		//fxdiff VCRP-1,2: access control of resources
		Roles roles = getRoles(httpRequest);
		Identity identity = getIdentity(httpRequest);
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(identity, roles, CourseModule.getCourseTypeName());
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = rm.countGenericANDQueryWithRolesRestriction(params, true);
			List<RepositoryEntry> repoEntries = rm.genericANDQueryWithRolesRestriction(params, start, limit, true);
			CourseVO[] vos = toCourseVo(repoEntries);
			CourseVOes voes = new CourseVOes();
			voes.setCourses(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<RepositoryEntry> repoEntries = rm.genericANDQueryWithRolesRestriction(params, 0, -1, false);
			CourseVO[] vos = toCourseVo(repoEntries);
			return Response.ok(vos).build();
		}
	}
	
	private CourseVO[] toCourseVo(List<RepositoryEntry> repoEntries) {
		List<CourseVO> voList = new ArrayList<CourseVO>();
		
		int count=0;
		for (RepositoryEntry repoEntry : repoEntries) {
			try {
				ICourse course = CourseFactory.loadCourse(repoEntry.getOlatResource().getResourceableId());
				voList.add(ObjectFactory.get(repoEntry, course));
				if(count % 33 == 0) {
					DBFactory.getInstance().commitAndCloseSession();
				}
			} catch (Exception e) {
				log.error("Cannot load the course with this repository entry: " + repoEntry, e);
			}
		}
		
		CourseVO[] vos = new CourseVO[voList.size()];
		voList.toArray(vos);
		return vos;
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
   * @param copyFrom The cours key to make a copy from (optional)
   * @param request The HTTP request
	 * @return It returns the id of the newly created Course
	 */
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createEmptyCourse(@QueryParam("shortTitle") String shortTitle,
			@QueryParam("title") String title,
			@QueryParam("sharedFolderSoftKey") String sharedFolderSoftKey,
			@QueryParam("copyFrom") Long copyFrom,
			@Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		CourseConfigVO configVO = new CourseConfigVO();
		configVO.setSharedFolderSoftKey(sharedFolderSoftKey);
		
		ICourse course;
		UserRequest ureq = getUserRequest(request);
		if(copyFrom != null) {
			course = copyCourse(copyFrom, ureq, shortTitle, title, configVO);
		} else {
			course = createEmptyCourse(ureq.getIdentity(), shortTitle, title, configVO);
		}
		CourseVO vo = ObjectFactory.get(course);
		return Response.ok(vo).build();
	}
	
	public static ICourse copyCourse(Long copyFrom, UserRequest ureq, String name, String longTitle, CourseConfigVO courseConfigVO) {
		String shortTitle = name;
		//String learningObjectives = name + " (Example of creating a new course)";
		
		OLATResourceable originalOresTrans = OresHelper.createOLATResourceableInstance(CourseModule.class, copyFrom);
		RepositoryEntry src = RepositoryManager.getInstance().lookupRepositoryEntry(originalOresTrans, false);
		OLATResource originalOres = OLATResourceManager.getInstance().findResourceable(src.getOlatResource());
		boolean isAlreadyLocked = RepositoryHandlerFactory.getInstance().getRepositoryHandler(src).isLocked(originalOres);
		LockResult lockResult = RepositoryHandlerFactory.getInstance().getRepositoryHandler(src).acquireLock(originalOres, ureq.getIdentity());
		
		if(lockResult == null || (lockResult != null && lockResult.isSuccess()) && !isAlreadyLocked) {

			//create new repo entry
			RepositoryEntry preparedEntry = RepositoryManager.getInstance().createRepositoryEntryInstance(ureq.getIdentity().getName());
			preparedEntry.setCanDownload(src.getCanDownload());
			preparedEntry.setCanLaunch(src.getCanLaunch());
			
			if (courseConfigVO != null && StringHelper.containsNonWhitespace(shortTitle)) {
				preparedEntry.setDisplayname(shortTitle);
			} else {
				preparedEntry.setDisplayname("Copy of " + src.getDisplayname());
			}
			preparedEntry.setDescription(src.getDescription());

			String resName = src.getResourcename();
			if (resName == null) {
				resName = "";
			}
			preparedEntry.setResourcename(resName);
			RepositoryHandler typeToCopy = RepositoryHandlerFactory.getInstance().getRepositoryHandler(src);			
			OLATResourceable newResourceable = typeToCopy.createCopy(src.getOlatResource(), ureq);
			if (newResourceable == null) {
				return null;
			}
					
			OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(newResourceable);
			preparedEntry.setOlatResource(ores);
			// create security group
			prepareSecurityGroup(ureq.getIdentity(), preparedEntry);
			// copy image if available
			RepositoryEntryImageController.copyImage(src, preparedEntry);
			
			return prepareCourse(preparedEntry, courseConfigVO);
		}
		
		return null;
	}
	
	/**
	 * Create an empty course with some defaults settings
	 * @param identity
	 * @param name
	 * @param longTitle
	 * @param courseConfigVO
	 * @return
	 */
	public static ICourse createEmptyCourse(Identity identity, String name, String longTitle, CourseConfigVO courseConfigVO) {
		String shortTitle = name;
		String learningObjectives = name + " (Example of creating a new course)";

		try {
			// create the course resource
			OLATResourceable oresable = OLATResourceManager.getInstance().createOLATResourceInstance(CourseModule.class);

			// create a repository entry
			RepositoryEntry addedEntry = RepositoryManager.getInstance().createRepositoryEntryInstance(identity.getName());
			addedEntry.setCanDownload(false);
			addedEntry.setCanLaunch(true);
			addedEntry.setDisplayname(shortTitle);
			addedEntry.setResourcename("-");
			// Do set access for owner at the end, because unfinished course should be
			// invisible
			// addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
			addedEntry.setAccess(0);// Access for nobody

			// Set the resource on the repository entry and save the entry.
			// bind resource and repository entry
			OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(oresable);
			addedEntry.setOlatResource(ores);

			// create an empty course
			ICourse course = CourseFactory.createEmptyCourse(oresable, shortTitle, longTitle, learningObjectives);
			// initialize course group management
			CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
			cgm.createCourseGroupmanagement(course.getResourceableId().toString());
			prepareSecurityGroup(identity, addedEntry);
			return prepareCourse(addedEntry, courseConfigVO);
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}
	
	private static void prepareSecurityGroup(Identity identity, RepositoryEntry addedEntry) {
		// create security group
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		SecurityGroup newGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
		// members of this group are always authors also
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);

		securityManager.addIdentityToSecurityGroup(identity, newGroup);
		addedEntry.setOwnerGroup(newGroup);
			
		//fxdiff VCRP-1,2: access control of resources
		// security group for tutors / coaches
		SecurityGroup tutorGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_ACCESS, addedEntry.getOlatResource());
		// members of this group are always tutors also
		securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_TUTOR);
		addedEntry.setTutorGroup(tutorGroup);
			
		// security group for participants
		SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_ACCESS, addedEntry.getOlatResource());
		// members of this group are always participants also
		securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_PARTICIPANT);
		addedEntry.setParticipantGroup(participantGroup);
		// Do set access for owner at the end, because unfinished course should be invisible
		addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
		RepositoryManager.getInstance().saveRepositoryEntry(addedEntry);
	}
	
	private static ICourse prepareCourse(RepositoryEntry addedEntry, CourseConfigVO courseConfigVO) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		securityManager.createAndPersistPolicy(addedEntry.getOwnerGroup(), Constants.PERMISSION_ADMIN, addedEntry.getOlatResource());

		// set root node title
		ICourse course = CourseFactory.openCourseEditSession(addedEntry.getOlatResource().getResourceableId());
		String displayName = addedEntry.getDisplayname();
		course.getRunStructure().getRootNode().setShortTitle(Formatter.truncate(displayName, 25));
		course.getRunStructure().getRootNode().setLongTitle(displayName);

		CourseNode rootNode = ((CourseEditorTreeNode) course.getEditorTreeModel().getRootNode()).getCourseNode();
		rootNode.setShortTitle(Formatter.truncate(displayName, 25));
		rootNode.setLongTitle(displayName);
		
		if(courseConfigVO != null) {
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			if(StringHelper.containsNonWhitespace(courseConfigVO.getSharedFolderSoftKey())) {
				courseConfig.setSharedFolderSoftkey(courseConfigVO.getSharedFolderSoftKey());
			}
		}
		RepositoryManager.getInstance().updateRepositoryEntry(addedEntry);

		CourseFactory.saveCourse(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		course = CourseFactory.loadCourse(course.getResourceableId());
		return course;
	}
}
