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

import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.util.ArrayList;
import java.util.List;

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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Path("organisations")
public class OrganisationsWebService {
	
	private static final String VERSION = "1.0";
	
	/**
	 * The version of the User Web Service
	 * @response.representation.200.mediaType text/plain
 	 * @response.representation.200.doc The version of this specific Web Service
 	 * @response.representation.200.example 1.0
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	
	/**
	 * List of organizations flat.
	 * 
	 * @response.representation.200.qname {http://www.example.com}organisationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of all organization in the OpenOLAT system
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest The HTTP request
	 * @return An array of organizations
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisations(@Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		List<Organisation> organisations = organisationService.getOrganisations();
		OrganisationVO[] organisationVOes = toArrayOfVOes(organisations);
		return Response.ok(organisationVOes).build();
	}

	/**
	 * Creates and persists a new organization entity.
	 * 
	 * @response.representation.qname {http://www.example.com}organisationVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The organization to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted organization
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param organisation The organization to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>organization</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putOrganisation(OrganisationVO organisation, @Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Organisation savedOrganisation = saveOrganisation(organisation);
		return Response.ok(OrganisationVO.valueOf(savedOrganisation)).build();
	}
	
	/**
	 * Updates a new organization entity.
	 * 
	 * @response.representation.qname {http://www.example.com}organisationVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The organization to update
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged organization
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param organisation The organization to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization</code>
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisation(OrganisationVO organisation, @Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Organisation savedOrganisation = saveOrganisation(organisation);
		return Response.ok(OrganisationVO.valueOf(savedOrganisation)).build();
	}
	
	/**
	 * Get a specific organization.
	 * 
	 * @response.representation.200.qname {http://www.example.com}organisationVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of all organization in the OpenOLAT system
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param organisationKey The organization primary key
	 * @param httpRequest The HTTP request
	 * @return The organization
	 */
	@GET
	@Path("{organisationKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		OrganisationVO organisationVo = OrganisationVO.valueOf(organisation);
		return Response.ok(organisationVo).build();
	}
	
	/**
	 * Updates a new organization entity. the primary key is taken from
	 * the url. The organization object can be "primary key free".
	 * 
	 * @response.representation.qname {http://www.example.com}organisationVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The organization to update
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged organization
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param organisationKey The organization primary key
	 * @param organisation The organization to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization</code>
	 */
	@POST
	@Path("{organisationKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisation(@PathParam("organisationKey") Long organisationKey, OrganisationVO organisation,
			@Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		if(organisation.getKey() == null) {
			organisation.setKey(organisationKey);
		} else if(!organisationKey.equals(organisation.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		Organisation savedOrganisation = saveOrganisation(organisation);
		return Response.ok(OrganisationVO.valueOf(savedOrganisation)).build();
	}
	
	
	private Organisation saveOrganisation(OrganisationVO organisation) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		
		Organisation organisationToSave = null;
		Organisation parentOrganisation = null;
		if(organisation.getParentOrganisationKey() != null) {
			parentOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(organisation.getParentOrganisationKey()));
		}
		OrganisationType type = null;
		if(organisation.getOrganisationTypeKey() != null) {
			type = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisation.getOrganisationTypeKey()));
		}
		
		boolean move = false;
		if(organisation.getKey() == null) {
			organisationToSave = organisationService.createOrganisation(organisation.getDisplayName(), organisation.getIdentifier(), organisation.getDescription(), parentOrganisation, type);
		} else {
			organisationToSave = organisationService.getOrganisation(new OrganisationRefImpl(organisation.getKey()));
			organisationToSave.setDisplayName(organisation.getDisplayName());
			organisationToSave.setIdentifier(organisation.getIdentifier());
			organisationToSave.setDescription(organisation.getDescription());
			organisationToSave.setType(type);
			if(parentOrganisation != null && organisationToSave.getParent() != null
					&& !organisationToSave.getParent().getKey().equals(parentOrganisation.getKey())) {
				move = true;
			}
		}
		
		organisationToSave.setCssClass(organisation.getCssClass());
		organisationToSave.setExternalId(organisation.getExternalId());
		organisationToSave.setManagedFlags(OrganisationManagedFlag.toEnum(organisation.getManagedFlagsString()));
		organisationToSave.setStatus(organisation.getStatus());
		
		Organisation savedOrganisation = organisationService.updateOrganisation(organisationToSave);
		if(move) {
			organisationService.moveOrganisation(savedOrganisation, parentOrganisation);
			CoreSpringFactory.getImpl(DB.class).commit();
			savedOrganisation = organisationService.getOrganisation(savedOrganisation);
		}
		return savedOrganisation;
	}
	
	@Path("types")
	public OrganisationTypesWebService getOrganisationTypes() {
		return new OrganisationTypesWebService();
	}
	
	/**
	 * Get all members of the specified organisation with the specified role.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of members
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The organisation was not found
	 * @response.representation.409.doc The rolle is not valid
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{organisationKey}/{role}")
	public Response getMembers(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		return getMembers(organisationKey, getRoles(role));
	}
	
	private Response getMembers(Long organisationKey, OrganisationRoles role) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(role == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		List<Identity> members = organisationService.getMembersIdentity(organisation, role);
		List<UserVO> voList = new ArrayList<>(members.size());
		for(Identity member:members) {
			voList.add(UserVOFactory.get(member));
		}
		return Response.ok(voList.toArray(new UserVO[voList.size()])).build();
	}
	
	/**
	 * Make the specified user a member of the specified organization
	 * with the specified role.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The organization or the identity was not found
	 * @param organisationKey The organization primary key
	 * @param role The role
	 * @param identityKey The member to make a coach of
	 * @param httpRequest The HTTP request
	 * @return Nothing
	 */
	@PUT
	@Path("{organisationKey}/{role}/{identityKey}")
	public Response putMember(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		return putMember(organisationKey, identityKey, getRoles(role));
	}
	
	private Response putMember(Long organisationKey, Long identityKey, OrganisationRoles role) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(role == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		organisationService.addMember(organisation, identity, role);
		return Response.ok().build();
	}
	
	/**
	 * Add a membership to the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementMemberVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTMEMBERVO}
	 * @response.representation.200.doc The membership was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param organisationKey The curriculum element primary key
	 * @param role The membership informations
	 * @return Nothing
	 */
	@PUT
	@Path("{organisationKey}/{role}")
	public Response putMembers(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			UserVO[] members, @Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
	
		if(getRoles(role) == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		for(UserVO member:members) {
			Identity identity = securityManager.loadIdentityByKey(member.getKey());
			if(identity != null) {
				organisationService.addMember(organisation, identity, getRoles(role));
			}
		}
		return Response.ok().build();
	}
	
	/**
	 * Remove the membership of the identity from the specified organization and role.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The organization or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{organisationKey}/{role}/{identityKey}")
	public Response deleteMember(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		boolean isSystemAdministrator = isAdmin(httpRequest);
		if(!isSystemAdministrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		return deleteMember(organisationKey, identityKey, getRoles(role));
	}
	
	private Response deleteMember(Long organisationKey, Long identityKey, OrganisationRoles role) {
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(role == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		organisationService.removeMember(organisation, identity, role);
		return Response.ok().build();
	}
	
	/**
	 * Convert beautified role to enum.
	 * 
	 * @param role
	 * @return The role enum or null
	 */
	private OrganisationRoles getRoles(String role) {
		if(OrganisationRoles.isValue(role)) {
			return OrganisationRoles.valueOf(role);
		} else if("coaches".equals(role)) {
			return OrganisationRoles.coach;
		}
		
		if(role.endsWith("s")) {
			role = role.substring(0, role.length() - 1);
		}
		if(OrganisationRoles.isValue(role)) {
			return OrganisationRoles.valueOf(role);
		}
		return null;	
	}
	
	
	private OrganisationVO[] toArrayOfVOes(List<Organisation> organisations) {
		int i=0;
		OrganisationVO[] entryVOs = new OrganisationVO[organisations.size()];
		for (Organisation organisation : organisations) {
			entryVOs[i++] = OrganisationVO.valueOf(organisation);
		}
		return entryVOs;
	}
}
