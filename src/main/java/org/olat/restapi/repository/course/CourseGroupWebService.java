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

import static org.olat.restapi.security.RestSecurityHelper.isGroupManager;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.admin.quota.QuotaConstants;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.restapi.VFSWebServiceSecurityCallback;
import org.olat.core.commons.services.vfs.restapi.VFSWebservice;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.restapi.ForumWebService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.vo.GroupVO;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Description:<br>
 * CourseGroupWebService
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Course - Groups")
public class CourseGroupWebService {
	
	private static final String VERSION = "1.0";
	
	private final OLATResource course;
	private final RepositoryEntryRef courseEntryRef;
	
	@Autowired
	private BusinessGroupService bgs;
	@Autowired
	private RepositoryService repositoryService;
	
	public CourseGroupWebService(RepositoryEntryRef courseEntryRef, OLATResource ores) {
		this.course = ores;
		this.courseEntryRef = courseEntryRef;
	}
	
	/**
	 * Retrieves the version of the Course Group Web Service.
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retrieves the version of the Course Group Web Service", description = "Retrieves the version of the Course Group Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	@Path("{groupKey}/folder")
	public VFSWebservice getFolder(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = getGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(!collabTools.isToolEnabled(CollaborationTools.TOOL_FOLDER)) {
			return null;
		}
		
		String relPath = collabTools.getFolderRelPath();
		QuotaManager qm = CoreSpringFactory.getImpl(QuotaManager.class);
		Quota folderQuota = qm.getCustomQuota(relPath);
		if (folderQuota == null) {
			Quota defQuota = qm.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_GROUPS);
			folderQuota = qm.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		SubscriptionContext subsContext = null;
		VFSWebServiceSecurityCallback secCallback = new VFSWebServiceSecurityCallback(true, true, true, folderQuota, subsContext);
		VFSContainer rootContainer = VFSManager.olatRootContainer(relPath, null);
		rootContainer.setLocalSecurityCallback(secCallback);
		return new VFSWebservice(rootContainer);
	}
	
	/**
	 * Return the Forum web service
	 * @param groupKey The key of the group
	 * @param request The HTTP Request
	 * @return
	 */
	@Path("{groupKey}/forum")
	@Operation(summary = "Return the Forum web service", description = "Return the Forum web service")
	public ForumWebService getForum(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		BusinessGroup bg = getGroup(groupKey);
		if(bg == null) {
			return null;
		}
		
		if(!isGroupManager(request)) {
			Identity identity = RestSecurityHelper.getIdentity(request);
			if(!bgs.isIdentityInBusinessGroup(identity, bg)) {
				return null;
			}
		}
		
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		if(collabTools.isToolEnabled(CollaborationTools.TOOL_FORUM)) {
			Forum forum = collabTools.getForum();
			ForumWebService ws = new ForumWebService(forum);
			CoreSpringFactory.autowireObject(ws);
			return ws;
		}
		return null;
	}
	
	/**
	 * Lists all learn groups of the specified course.
	 * 
	 * @param request The HTTP request
	 * @return
	 */
	@GET
	@Operation(summary = "Lists all business groups of course", description = "Lists all business groups of the specified course")
	@ApiResponse(responseCode = "200", description = "The list of all business groups of the course",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class)))
				})
	@ApiResponse(responseCode = "404", description = "The context of the group not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getGroupList() {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = bgs.findBusinessGroups(params, courseEntryRef, 0, -1);
			
		int count = 0;
		GroupVO[] vos = new GroupVO[groups.size()];
		for(BusinessGroup group:groups) {
			vos[count++] = GroupVO.valueOf(group);
		}
		return Response.ok(vos).build();
	}
	
	/**
	 * Creates a new group for the course.
	 * 
	 * @param group The group's metadatas
	 * @param request The HTTP request
	 * @return The created business group
	 */
	@PUT
	@Operation(summary = "Creates a new group", description = "Creates a new group for the course, and only create new business group.")
	@ApiResponse(responseCode = "200", description = "A group to save", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putNewBusinessGroup(GroupVO group, @Context HttpServletRequest request) {
		return createNewBusinessGroup(group, request);
	}
	
	/**
	 * Creates a new group for the course.
	 * 
	 * @param group The group's metadatas
	 * @param request The HTTP request
	 * @return The created business group
	 */
	@POST
	@Operation(summary = "Creates a new group", description = "Creates a new group for the course, and only create new business group.")
	@ApiResponse(responseCode = "200", description = "A group to save", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postNewBusinessGroup(GroupVO group, @Context HttpServletRequest request) {
		return createNewBusinessGroup(group, request);
	}
	
	private Response createNewBusinessGroup(GroupVO group, HttpServletRequest request) {
		ICourse icourse = CourseFactory.loadCourse(course.getResourceableId());
		if(!RestSecurityHelper.isGroupManager(request) && !RestSecurityHelper.isOwnerGrpManager(icourse, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		UserRequest ureq = RestSecurityHelper.getUserRequest(request);
		RepositoryEntry courseRe = repositoryService.loadByKey(courseEntryRef.getKey());
				
		BusinessGroup bg;
		if(group.getKey() != null && group.getKey() > 0) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		} else {
			Integer min = normalize(group.getMinParticipants());
			Integer max = normalize(group.getMaxParticipants());
			bg = bgs.createBusinessGroup(ureq.getIdentity(), group.getName(), group.getDescription(), BusinessGroup.BUSINESS_TYPE,
					group.getExternalId(), group.getManagedFlags(), min, max, false, false, courseRe);
		}
		GroupVO savedVO = GroupVO.valueOf(bg);
		return Response.ok(savedVO).build();
	}
	
	/**
	 * Retrieves the metadata of the specified group.
	 * 
	 * @param groupKey The group's id
	 * @param request The REST request
	 * @param httpRequest The HTTP request
	 * @return
	 */
	@GET
	@Path("{groupKey}")
	@Operation(summary = "Retrieves the metadata of the specified group", description = "Retrieves the metadata of the specified group")
	@ApiResponse(responseCode = "200", description = "This is the list of all groups in OLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class)))
	})
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	public Response getGroup(@PathParam("groupKey") Long groupKey, @Context Request request, @Context HttpServletRequest httpRequest) {
		//further security check: group is in the course
		BusinessGroup businessGroup = getGroup(groupKey);
		if(businessGroup == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		GroupVO groupVO = GroupVO.valueOf(businessGroup);
		return Response.ok(groupVO).build();
	}
	
	private BusinessGroup getGroup(Long groupKey) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = bgs.findBusinessGroups(params, courseEntryRef, 0, -1);
		for(BusinessGroup group:groups) {
			if(group.getKey().equals(groupKey)) {
				return group;
			}
		}
		return null;
	}

	/**
	 * Add a business group to the specified course.
	 * 
	 * @param groupKey The group's id
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{groupKey}")
	@Operation(summary = "Add a business group to the specified course", description = "Add a business group to the specified course")
	@ApiResponse(responseCode = "200", description = "The saved group", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	public Response postResourceTo(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		return addResourceTo(groupKey, request);
	}
	
	/**
	 * Add a business group to the specified course.
	 * 
	 * @param groupKey The group's id
	 * @param request The HTTP request
	 * @return
	 */
	@PUT
	@Path("{groupKey}")
	@Operation(summary = "Add a business group to the specified course", description = "Add a business group to the specified course")
	@ApiResponse(responseCode = "200", description = "The saved group", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = GroupVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = GroupVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	public Response putResourceTo(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		return addResourceTo(groupKey, request);
	}

	private Response addResourceTo(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		BusinessGroup group = bgs.loadBusinessGroup(groupKey);
		if(group == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		RepositoryEntry courseEntry = repositoryService.loadByKey(courseEntryRef.getKey());
		bgs.addResourceTo(group, courseEntry);
		GroupVO savedVO = GroupVO.valueOf(group);
		return Response.ok(savedVO).build();
	}
	
	/**
	 * Remove the business group from the specified course.
	 * 
	 * @param groupKey The group id
	 * @param request The HTTP request
	 * @return
	 */
	@DELETE
	@Path("{groupKey}")
	@Operation(summary = "Remove the business group from course", description = "Remove the business group from the specified course")
	@ApiResponse(responseCode = "200", description = "The business group is removed")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group cannot be found")
	public Response deleteGroup(@PathParam("groupKey") Long groupKey, @Context HttpServletRequest request) {
		if(!isGroupManager(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		BusinessGroup group = bgs.loadBusinessGroup(groupKey);
		if(group == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		RepositoryEntry courseEntry = repositoryService.loadByKey(courseEntryRef.getKey());
		bgs.removeResourceFrom(List.of(group), courseEntry);
		return Response.ok().build();
	}
	
	/**
	 * @param integer
	 * @return value bigger or equal than 0
	 */
	private int normalize(Integer integer) {
		if(integer == null) return -1;
		if(integer.intValue() < 0) return -1;
		return integer.intValue();
	}
}
