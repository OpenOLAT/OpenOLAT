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
package org.olat.restapi.group;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.GroupInfoVO;
import org.olat.restapi.support.vo.GroupInfoVOes;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.GroupVOes;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  18 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MyGroupWebService {
	
	private final Identity retrievedUser;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public MyGroupWebService(Identity retrievedUser) {
		this.retrievedUser = retrievedUser;
	}
	
	/**
	 * Return all groups of a user where the user is coach or participant.
	 * 
	 * @param start The first result
	 * @param limit The maximum results
	 * @param externalId Search with an external ID
	 * @param managed (true / false) Search only managed / not managed groups 
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of groups informations
	 */
	@GET
	@Operation(summary = "Return all groups of a user where the user is coach or participant", description = "Return all groups of a user where the user is coach or participant")
	@ApiResponse(responseCode = "200", description = "The groups of the user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))) })
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getUserGroupList(@QueryParam("start") @Parameter(description = "The first result") @DefaultValue("0") Integer start, @QueryParam("limit") @Parameter(description = "The maximum results")  @DefaultValue("25") Integer limit,
			@QueryParam("externalId") @Parameter(description = "Search with an external ID") String externalId, @QueryParam("managed") @Parameter(description = "(true / false) Search only managed / not managed groups") Boolean managed,
			@Context HttpServletRequest httpRequest, @Context Request request) {

		return getGroupList(start, limit, externalId, managed, true, true, httpRequest, request);
	}
	
	/**
	 * Return all groups of a user where the user is coach/owner.
	 * 
	 * @param start The first result
	 * @param limit The maximum results
	 * @param externalId Search with an external ID
	 * @param managed (true / false) Search only managed / not managed groups 
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of groups
	 */
	@GET
	@Path("owner")
	@Operation(summary = "Return all groups of a user where the user is coach/owner", description = "Return all groups of a user where the user is coach/owner")
	@ApiResponse(responseCode = "200", description = "The groups of the user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))) })
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOwnedGroupList(@QueryParam("start") @Parameter(description = "The first result") @DefaultValue("0") Integer start, @QueryParam("limit") @Parameter(description = "The maximum results") @DefaultValue("25") Integer limit,
			@QueryParam("externalId") @Parameter(description = "Search with an external ID") String externalId, @QueryParam("managed") @Parameter(description = "(true / false) Search only managed / not managed groups") Boolean managed,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		return getGroupList(start, limit, externalId, managed, true, false, httpRequest, request);
	}
	
	/**
	 * Return all groups of a user where the user is participant.
	 * 
	 * @param start The first result
	 * @param limit The maximum results
	 * @param externalId Search with an external ID
	 * @param managed (true / false) Search only managed / not managed groups 
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of groups
	 */
	@GET
	@Path("participant")
	@Operation(summary = "Return all groups of a user where the user is participant", description = "Return all groups of a user where the user is participant")
	@ApiResponse(responseCode = "200", description = "The groups of the user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupVO.class))) })
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipatingGroupList(@QueryParam("start") @DefaultValue("0") Integer start, @QueryParam("limit") @DefaultValue("25") Integer limit,
			@QueryParam("externalId") String externalId, @QueryParam("managed") Boolean managed,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		return getGroupList(start, limit, externalId, managed, false, true, httpRequest, request);
	}
	
	private Response getGroupList(Integer start, Integer limit, String externalId, Boolean managed,
			boolean owner, boolean participant, HttpServletRequest httpRequest, Request request) {
		
		if(!hasAccess(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(retrievedUser, owner, participant);
		if(StringHelper.containsNonWhitespace(externalId)) {
			params.setExternalId(externalId);
		}
		params.setManaged(managed);
		
		List<BusinessGroup> groups;
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = businessGroupService.countBusinessGroups(params, null);
			groups = businessGroupService.findBusinessGroups(params, null, start, limit);
			
			int count = 0;
			GroupVO[] groupVOs = new GroupVO[groups.size()];
			for(BusinessGroup group:groups) {
				groupVOs[count++] = GroupVO.valueOf(group);
			}
			GroupVOes voes = new GroupVOes();
			voes.setGroups(groupVOs);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
			
			int count = 0;
			GroupVO[] groupVOs = new GroupVO[groups.size()];
			for(BusinessGroup group:groups) {
				groupVOs[count++] = GroupVO.valueOf(group);
			}
			return Response.ok(groupVOs).build();
		}
	}
	
	
	/**
	 * Return all groups with information of a user. Paging is mandatory!
	 * 
	 * @param start The first result
	 * @param limit The maximum results
	 * @param externalId Search with an external ID
	 * @param managed (true / false) Search only managed / not managed groups 
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of groups with additional informations
	 */
	@GET
	@Path("infos")
	@Operation(summary = "Return all groups with information of a user", description = "Return all groups with information of a user. Paging is mandatory")
	@ApiResponse(responseCode = "200", description = "The groups of the user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GroupInfoVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GroupInfoVO.class))) })
	@ApiResponse(responseCode = "406", description = "The request hasn't paging information")	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getUserGroupInfosList(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit,
			@QueryParam("externalId") String externalId, @QueryParam("managed") Boolean managed,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		
		if(!hasAccess(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		SearchBusinessGroupParams params = new SearchBusinessGroupParams(retrievedUser, true, true);
		if(StringHelper.containsNonWhitespace(externalId)) {
			params.setExternalId(externalId);
		}
		params.setManaged(managed);
		
		List<BusinessGroup> groups;
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = businessGroupService.countBusinessGroups(params, null);
			groups = businessGroupService.findBusinessGroups(params, null, start, limit);
			
			int count = 0;
			GroupInfoVO[] groupVOs = new GroupInfoVO[groups.size()];
			for(BusinessGroup group:groups) {
				groupVOs[count++] = ObjectFactory.getInformation(retrievedUser, group);
			}
			GroupInfoVOes voes = new GroupInfoVOes();
			voes.setGroups(groupVOs);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
	}
	
	private boolean hasAccess(@Context HttpServletRequest httpRequest) {
		Identity identity = getIdentity(httpRequest);
		if(identity.getKey().equals(retrievedUser.getKey())) {
			return true;
		}
		
		Roles managerRoles = RestSecurityHelper.getRoles(httpRequest);
		if(managerRoles.isGroupManager()) {
			return true;
		}
		
		Roles identityRoles = securityManager.getRoles(retrievedUser);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.rolesmanager, identityRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.usermanager, identityRoles);
	}
}
