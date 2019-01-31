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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.basesecurity.RelationRole;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;

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
	 * @response.representation.200.qname {http://www.example.com}identityToIdentityRelationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of relation from the specified user
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_IDENTITYTOIDENTITYRELATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest The HTTP request
	 * @return An array of relations
	 */
	@GET
	@Path("source")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRelationsAsSource() {
		List<IdentityToIdentityRelation> relations = identityRelationshipService.getRelationsAsSource(identity);
		IdentityToIdentityRelationVO[] relationVOes = toArrayOfVOes(relations);
		return Response.ok(relationVOes).build();
	}
	
	/**
	 * List of relations to the specified user from others.
	 * 
	 * @response.representation.200.qname {http://www.example.com}identityToIdentityRelationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of relation to the specified user
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_IDENTITYTOIDENTITYRELATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest The HTTP request
	 * @return An array of relations
	 */
	@GET
	@Path("target")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRelationsAsTarget() {
		List<IdentityToIdentityRelation> relations = identityRelationshipService.getRelationsAsTarget(identity);
		IdentityToIdentityRelationVO[] relationVOes = toArrayOfVOes(relations);
		return Response.ok(relationVOes).build();
	}
	
	/**
	 * Creates and persists a new relation entity.
	 * 
	 * @response.representation.qname {http://www.example.com}identityToIdentityRelationVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The relation to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_IDENTITYTOIDENTITYRELATIONVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted relation
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_IDENTITYTOIDENTITYRELATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param relationRoleVo The relation to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>relation</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putRelation(IdentityToIdentityRelationVO relationRoleVo) {
		return postRelation(relationRoleVo);
	}
	
	/**
	 * Creates and persists a new relation entity.
	 * 
	 * @response.representation.qname {http://www.example.com}identityToIdentityRelationVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The relation to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_IDENTITYTOIDENTITYRELATIONVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted relation
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_IDENTITYTOIDENTITYRELATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param relationRoleVo The relation to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>relation</code>
	 */
	@POST
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
	 * @response.representation.200.doc The relation
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param relationKey The relation to delete
	 * @return Ok if the relation was deleted
	 */
	@DELETE
	@Path("{relationKey}")
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
