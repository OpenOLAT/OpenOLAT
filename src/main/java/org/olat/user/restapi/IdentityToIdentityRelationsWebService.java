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

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationSearchParams;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * The access permission is done by UserWebService.
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityToIdentityRelationsWebService {
	
	private final Identity identity;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private IdentityRelationshipService identityRelationshipService;
	
	public IdentityToIdentityRelationsWebService(Identity identity) {
		CoreSpringFactory.autowireObject(this);
		this.identity = identity;
	}
	
	
	/**
	 * List of relations from the specified user to others.
	 * 
	 * @param httpRequest The HTTP request
	 * @return An array of relations
	 */
	@GET
	@Path("source")
	@Operation(summary = "List of relations from the specified user to others", description = "List of relations from the specified user to others")
	@ApiResponse(responseCode = "200", description = "The list of relation from the specified user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IdentityToIdentityRelationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = IdentityToIdentityRelationVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRelationsAsSource() {
		List<IdentityToIdentityRelation> relations = identityRelationshipService.getRelationsAsSource(identity);
		IdentityToIdentityRelationVO[] relationVOes = toArrayOfVOes(relations);
		return Response.ok(relationVOes).build();
	}
	
	/**
	 * List of relations to the specified user from others.
	 * 
	 * @param httpRequest The HTTP request
	 * @return An array of relations
	 */
	@GET
	@Path("target")
	@Operation(summary = "List of relations to the specified user from others", description = "List of relations to the specified user from others")
	@ApiResponse(responseCode = "200", description = "The list of relation to the specified user", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = IdentityToIdentityRelationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = IdentityToIdentityRelationVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRelationsAsTarget() {
		RelationSearchParams searchParams = new RelationSearchParams();
		List<IdentityToIdentityRelation> relations = identityRelationshipService.getRelationsAsTarget(identity, searchParams);
		IdentityToIdentityRelationVO[] relationVOes = toArrayOfVOes(relations);
		return Response.ok(relationVOes).build();
	}
	
	/**
	 * Creates and persists a new relation entity.
	 * 
	 * @param relationRoleVo The relation to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>relation</code>
	 */
	@PUT
	@Operation(summary = "Creates and persists a new relation entity", description = "Creates and persists a new relation entity")
	@ApiResponse(responseCode = "200", description = "The persisted relation", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = IdentityToIdentityRelationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = IdentityToIdentityRelationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putRelation(IdentityToIdentityRelationVO relationRoleVo) {
		return postRelation(relationRoleVo);
	}
	
	/**
	 * Creates and persists a new relation entity.
	 * 
	 * @param relationRoleVo The relation to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>relation</code>
	 */
	@POST
	@Operation(summary = "Creates and persists a new relation entity", description = "Creates and persists a new relation entity")
	@ApiResponse(responseCode = "200", description = "The persisted relation", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = IdentityToIdentityRelationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = IdentityToIdentityRelationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postRelation(IdentityToIdentityRelationVO relationRoleVo) {
		IdentityToIdentityRelation savedRelationRole = saveRelation(relationRoleVo);
		if(savedRelationRole == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		return Response.ok(IdentityToIdentityRelationVO.valueOf(savedRelationRole)).build();
	}
	
	/**
	 * Deletes a relation entity.
	 * 
	 * @param relationKey The relation to delete
	 * @return Ok if the relation was deleted
	 */
	@DELETE
	@Path("{relationKey}")
	@Operation(summary = "Delete a relation entity", description = "Deletes a relation entity")
	@ApiResponse(responseCode = "200", description = "The relation")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public Response deleteRelation(@PathParam("relationKey") Long relationKey) {
		IdentityToIdentityRelation relation = identityRelationshipService.getRelation(relationKey);
		if(relation == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		identityRelationshipService.removeRelation(relation.getSource(), relation.getTarget(), relation.getRole());
		return Response.ok().build();
	}
	
	private IdentityToIdentityRelation saveRelation(IdentityToIdentityRelationVO relationVo) {
		IdentityToIdentityRelation relation;
		if(relationVo.getKey() == null) {
			Identity source = securityManager.loadIdentityByKey(relationVo.getIdentitySourceKey());
			Identity target = securityManager.loadIdentityByKey(relationVo.getIdentityTargetKey());
			RelationRole relationRole = identityRelationshipService.getRole(relationVo.getRelationRoleKey());
			if(source == null || target == null || relationRole == null) {
				throw new WebApplicationException(Status.NOT_FOUND);
			}
			relation = identityRelationshipService.addRelation(source, target, relationRole,
					relationVo.getExternalId(), IdentityToIdentityRelationManagedFlag.toEnum(relationVo.getManagedFlagsString()));
		} else {
			relation = null;
		}
		return relation;
	}
	
	private IdentityToIdentityRelationVO[] toArrayOfVOes(List<IdentityToIdentityRelation> relationRoles) {
		int i=0;
		IdentityToIdentityRelationVO[] roleVOes = new IdentityToIdentityRelationVO[relationRoles.size()];
		for (IdentityToIdentityRelation relationRole:relationRoles) {
			roleVOes[i++] = IdentityToIdentityRelationVO.valueOf(relationRole);
		}
		return roleVOes;
	}

}
