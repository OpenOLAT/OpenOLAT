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
package org.olat.user.restapi;

import java.util.Collections;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.core.id.Roles;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Users")
@Component
@Path("users/relations")
public class RelationRolesWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private IdentityRelationshipService identityRelationshipService;
	
	/**
	 * The version of the Web Service
	 * @response.representation.200.mediaType text/plain
 	 * @response.representation.200.doc The version of this specific Web Service
 	 * @response.representation.200.example 1.0
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the Web Service", description = "The version of the Web Service")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The version of this specific Web Service") })	
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * List of relation roles.
	 * 
	 * @response.representation.200.qname {http://www.example.com}relationRoleVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of all relation roles in the OpenOLAT system
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest The HTTP request
	 * @return An array of organizations
	 */
	@GET
	@Path("roles")
	@Operation(summary = "List of relation roles", description = "List of relation roles")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The list of all relation roles in the OpenOLAT system", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RelationRoleVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RelationRoleVO.class))) }),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient") })	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRoles(@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<RelationRole> relationRoles = identityRelationshipService.getAvailableRoles();
		RelationRoleVO[] relationRoleVOes = toArrayOfVOes(relationRoles);
		return Response.ok(relationRoleVOes).build();
	}
	
	/**
	 * Creates and persists a new relation role entity.
	 * 
	 * @response.representation.qname {http://www.example.com}relationRoleVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The relation role to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted relation role
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param relationRoleVo The relation role to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>relation role</code>
	 */
	@PUT
	@Path("roles")
	@Operation(summary = "Creates and persists a new relation role entity", description = "Creates and persists a new relation role entity")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The persisted relation role", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RelationRoleVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RelationRoleVO.class)) }),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient"),
			@ApiResponse(responseCode = "406", description = "application/xml, application/json")})	
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putRelationRole(RelationRoleVO relationRoleVo, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		RelationRole savedRelationRole = saveRelationRole(relationRoleVo);
		return Response.ok(RelationRoleVO.valueOf(savedRelationRole)).build();
	}
	
	/**
	 * Updates a relation role entity.
	 * 
	 * @response.representation.qname {http://www.example.com}relationRoleVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The relation role to update
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged relation role
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param relationRoleVo The relation role to merge
	 * @param request The HTTP request
	 * @return The merged <code>relation role</code>
	 */
	@POST
	@Path("roles")
	@Operation(summary = "Update a relation role entity", description = "Updates a relation role entity")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The merged relation role", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RelationRoleVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RelationRoleVO.class)) }),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient"),
			@ApiResponse(responseCode = "406", description = "application/xml, application/json")})	
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postRelationRole(RelationRoleVO relationRoleVo, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		RelationRole savedRelationRole = saveRelationRole(relationRoleVo);
		return Response.ok(RelationRoleVO.valueOf(savedRelationRole)).build();
	}
	
	/**
	 * Updates a relation role entity.
	 * 
	 * @response.representation.qname {http://www.example.com}relationRoleVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The relation role to update
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged relation role
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_RELATIONROLEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param relationRoleVo The relation role to merge
	 * @param request The HTTP request
	 * @return The merged <code>relation role</code>
	 */
	@POST
	@Path("roles/{relationRoleKey}")
	@Operation(summary = "Update a relation role entity", description = "Updates a relation role entity")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The merged relation role", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = RelationRoleVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = RelationRoleVO.class)) }),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient"),
			@ApiResponse(responseCode = "406", description = "application/xml, application/json")})	
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postRelationRole(RelationRoleVO relationRoleVo, @PathParam("relationRoleKey") Long relationRoleKey,
			@Context HttpServletRequest httpRequest) {
		if(relationRoleVo.getKey() == null) {
			relationRoleVo.setKey(relationRoleKey);
		} else if(!relationRoleKey.equals(relationRoleVo.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		return postRelationRole(relationRoleVo, httpRequest);
	}
	
	private RelationRole saveRelationRole(RelationRoleVO relationRoleVo) {
		List<RelationRight> rights;
		if(relationRoleVo.getRights() != null && !relationRoleVo.getRights().isEmpty()) {
			List<RelationRight> availableRights = identityRelationshipService.getAvailableRights();
			rights = availableRights.stream()
					.filter(r -> relationRoleVo.getRights().contains(r.getRight()))
					.collect(Collectors.toList());
		} else {
			rights = Collections.emptyList();
		}
		
		RelationRole relationRole;
		if(relationRoleVo.getKey() == null) {
			relationRole = identityRelationshipService.createRole(relationRoleVo.getRole(), relationRoleVo.getExternalId(), relationRoleVo.getExternalRef(),
					RelationRoleManagedFlag.toEnum(relationRoleVo.getManagedFlags()), rights);
		} else {
			relationRole = identityRelationshipService.getRole(relationRoleVo.getKey());
			relationRole.setRole(relationRoleVo.getRole());
			relationRole.setExternalId(relationRoleVo.getExternalId());
			relationRole.setExternalRef(relationRoleVo.getExternalRef());
			relationRole.setManagedFlags(RelationRoleManagedFlag.toEnum(relationRoleVo.getManagedFlags()));
			relationRole = identityRelationshipService.updateRole(relationRole, rights);
		}
		return relationRole;
	}
	
	@DELETE
	@Path("roles/{relationRoleKey}")
	@Operation(summary = "Remove role", description = "Remove a role")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "The role has been removed"),
			@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient"),
			@ApiResponse(responseCode = "404", description = "Role not found")})	
	public Response deleteRelationRole(@PathParam("relationRoleKey") Long relationRoleKey, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RelationRole role = identityRelationshipService.getRole(relationRoleKey);
		if(role == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		identityRelationshipService.deleteRole(role);
		return Response.serverError().status(Status.OK).build();
	}
	
	private RelationRoleVO[] toArrayOfVOes(List<RelationRole> relationRoles) {
		int i=0;
		RelationRoleVO[] roleVOes = new RelationRoleVO[relationRoles.size()];
		for (RelationRole relationRole:relationRoles) {
			roleVOes[i++] = RelationRoleVO.valueOf(relationRole);
		}
		return roleVOes;
	}
	
	private boolean isAdministrator(HttpServletRequest request) {
		try {
			Roles roles = RestSecurityHelper.getRoles(request);
			return roles.isAdministrator() || roles.isSystemAdmin();
		} catch (Exception e) {
			return false;
		}
	}
}
