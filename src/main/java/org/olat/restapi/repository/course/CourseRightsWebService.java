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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.Group;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.RightsVO;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 19 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Course - Rights")
public class CourseRightsWebService {
	
	private boolean admin;
	private RepositoryEntry entry;
	
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public CourseRightsWebService(RepositoryEntry entry, boolean admin) {
		this.entry = entry;
		this.admin = admin;
	}
	
	/**
	 * Returns the list of course rights granted to the course coaches and/or
	 * participants.
	 * 
	 * @return The rights for tutor/coach and participant roles
	 */
	@GET
	@Operation(summary = "Get the list of course rights granted to the course coaches and/or participants",
		description = "Get the list of course rights grnated to the course coaches and/or participants")
	@ApiResponse(responseCode = "200", description = "The list of permissions by role", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RightsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RightsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("course")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseRights() {
		if(!admin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Group defGroup = repositoryService.getDefaultGroup(entry);
		List<BGRights> currentRights = rightManager.findBGRights(List.of(defGroup), entry.getOlatResource());
		RightsVO coachRights = getRightsFrom(BGRightsRole.tutor, currentRights);
		RightsVO participantRights = getRightsFrom(BGRightsRole.participant, currentRights);
		RightsVO[] rightsVoes = new RightsVO[] { coachRights, participantRights };
		return Response.ok(rightsVoes).build();
	}
	
	/**
	 * Update the course rights granted to the course coaches and participants for the specified
	 * role. The list of rights need to be complete, rights not in the list will be removed,
	 * rights in the list will be added if missing.
	 * 
	 * @param rights The rights to update
	 * @return 200 if ok
	 */
	@POST
	@Operation(summary = "Update the list of course rights granted to the course coaches and participants",
		description = "Update the list of course rights granted to the course coaches and participants. The list of rights need to be complete, rights not in the list will be removed, rights in the list will be added if missing.")
	@ApiResponse(responseCode = "200", description = "The changes have been applied", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RightsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RightsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("course")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCourseRights(List<RightsVO> rights) {
		return updateCourseRights(rights);
	}
	
	/**
	 * Update the course rights granted to the course coaches and participants for the specified
	 * role. The list of rights need to be complete, rights not in the list will be removed,
	 * rights in the list will be added if missing.
	 * 
	 * @param rights The rights to update
	 * @return 200 if ok
	 */
	@PUT
	@Operation(summary = "Update the list of course rights granted to the course coaches and participants",
		description = "Update the list of course rights granted to the course coaches and participants. The list of rights need to be complete, rights not in the list will be removed, rights in the list will be added if missing.")
	@ApiResponse(responseCode = "200", description = "The changes have been applied", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RightsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RightsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("course")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCourseRights(List<RightsVO> rights) {
		return updateCourseRights(rights);
	}
	
	private Response updateCourseRights(List<RightsVO> rightsVoes) {
		if(!admin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		Group defGroup = repositoryService.getDefaultGroup(entry);
		List<BGRights> currentRights = rightManager.findBGRights(List.of(defGroup), entry.getOlatResource());
		update(defGroup, rightsVoes, currentRights);
		return Response.ok().build();
	}
	
	/**
	 * Returns the list of course rights granted to the coaches and/or
	 * participants of a specific business group.
	 * 
	 * @return The rights for tutor/coach and participant roles
	 */
	@GET
	@Operation(summary = "Get the list of course rights granted to the course coaches and participants",
		description = "Get the list of course rights granted to the course coaches and participants")
	@ApiResponse(responseCode = "200", description = "The list of permissions by role", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RightsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RightsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("group/{businessGroupKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getBusinessgroupRights(@PathParam("businessGroupKey") Long businessGroupKey) {
		if(!admin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setGroupKeys(List.of(businessGroupKey));
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
		if(groups.isEmpty()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(groups.size() > 1) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		Group baseGroup = groups.get(0).getBaseGroup();
		List<BGRights> currentRights = rightManager.findBGRights(List.of(baseGroup), entry.getOlatResource());
		RightsVO coachRights = getRightsFrom(BGRightsRole.tutor, currentRights);
		RightsVO participantRights = getRightsFrom(BGRightsRole.participant, currentRights);
		RightsVO[] rightsVoes = new RightsVO[] { coachRights, participantRights };
		return Response.ok(rightsVoes).build();
	}

	/**
	 * Update the course rights granted to the coaches and participants of the specified group
	 * for the specified role. The list of rights need to be complete, rights not in the list will be removed,
	 * rights in the list will be added if missing.
	 * 
	 * @param businessGroupKey The business group
	 * @param rights The rights to update
	 * @return 200 if ok
	 */
	@POST
	@Operation(summary = "Update the list of course rights granted to the course coaches and participants",
		description = "Update the list of course rights granted to the course coaches and participants. The list of rights need to be complete, rights not in the list will be removed, rights in the list will be added if missing.")
	@ApiResponse(responseCode = "200", description = "The changes have been applied", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RightsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RightsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("group/{businessGroupKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postBusinessGroupRights(@PathParam("businessGroupKey") Long businessGroupKey, List<RightsVO> rights) {
		return updateBusinessGroupRights(businessGroupKey, rights);
	}
	
	/**
	 * Update the course rights granted to the coaches and participants of the specified group
	 * for the specified role. The list of rights need to be complete, rights not in the list will be removed,
	 * rights in the list will be added if missing.
	 * 
	 * @param businessGroupKey The business group
	 * @param rights The rights to update
	 * @return 200 if ok
	 */
	@PUT
	@Operation(summary = "Update the list of course rights granted to the coaches and participants of a bsuiness group",
		description = "Update the list of course rights granted to the coaches and participants of a business group. The list of rights need to be complete, rights not in the list will be removed, rights in the list will be added if missing.")
	@ApiResponse(responseCode = "200", description = "The changes have been applied", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RightsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RightsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("group/{businessGroupKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putBusinessGroupRights(@PathParam("businessGroupKey") Long businessGroupKey, List<RightsVO> rights) {
		return updateBusinessGroupRights(businessGroupKey, rights);
	}
	
	private Response updateBusinessGroupRights(Long businessGroupKey, List<RightsVO> rightsVoes) {
		if(!admin) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setGroupKeys(List.of(businessGroupKey));
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
		if(groups.isEmpty()) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if(groups.size() > 1) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		Group baseGroup = groups.get(0).getBaseGroup();
		List<BGRights> currentRights = rightManager.findBGRights(List.of(baseGroup), entry.getOlatResource());
		update(baseGroup, rightsVoes, currentRights);
		return Response.ok().build();
	}
	
	/**
	 * The method updates the coaches/tutors and participants rights. If the REST object
	 * is not present for a specific role, the rights are not changed.
	 * 
	 * @param group The security group
	 * @param rightsVoes The list of rights pushed via REST
	 * @param currentRights The current list of granted permissions
	 */
	private void update(Group group, List<RightsVO> rightsVoes, List<BGRights> currentRights) {
		update(group, BGRightsRole.tutor, rightsVoes, currentRights);
		update(group, BGRightsRole.participant, rightsVoes, currentRights);
	}
	
	private void update(Group group, BGRightsRole role, List<RightsVO> rightsVoes, List<BGRights> currentRights) {
		RightsVO rightsVo = getRightsVOWithRole(role, rightsVoes);
		if(rightsVo != null) {
			List<String> currentRightsStrings = geRightsListWithRole(role, currentRights);
			List<String> rightsVoStrings = rightsVo.getRights() == null ? new ArrayList<>() : rightsVo.getRights();
			updateRightsStrings(group, role, rightsVoStrings, currentRightsStrings);
		}
	}

	private void updateRightsStrings(Group group, BGRightsRole role, List<String> rightsVoList, List<String> currentRightsList) {
		for(String currentRight:currentRightsList) {
			if(!rightsVoList.contains(currentRight)) {
				rightManager.removeBGRight(currentRight, group, entry.getOlatResource(), role);
			}
		}
		
		for(String rightVo:rightsVoList) {
			if(!currentRightsList.contains(rightVo)) {
				rightManager.addBGRight(rightVo, group, entry.getOlatResource(), role);
			}
		}
	}
	
	public static RightsVO getRightsVOWithRole(BGRightsRole role, List<RightsVO> rights) {
		for(RightsVO right:rights) {
			if(role.name().equals(right.getRole())) {
				return right;
			}
		}
		return null;
	}
	
	public static List<String> geRightsListWithRole(BGRightsRole role, List<BGRights> rights) {
		for(BGRights right:rights) {
			if(role == right.getRole()) {
				return right.getRights();
			}
		}
		return new ArrayList<>();
	}
	
	public static RightsVO getRightsFrom(BGRightsRole role, List<BGRights> rights) {
		List<String> rightList = null;
		
		for(BGRights right:rights) {
			if(role == right.getRole()) {
				rightList = right.getRights();
			}
		}
		
		RightsVO vo = new RightsVO();
		vo.setRole(role.name());
		vo.setRights(rightList == null ? List.of() : rightList);
		return vo;
	}
}
