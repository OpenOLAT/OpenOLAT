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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.basesecurity.model.SearchOrganisationParameters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Organisations")
@Component
@Path("organisations")
public class OrganisationsWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryService repositoryService;
	
	/**
	 * The version of the User Web Service
	 * 
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Operation(summary = "The version of the organisations Web Service", description = "The version of the organisations Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	
	/**
	 * List of organizations flat.
	 * 
	 * @param httpRequest The HTTP request
	 * @return An array of organizations
	 */
	@GET
	@Operation(summary = "List of organizations flat", description = "List of organizations flat")
	@ApiResponse(responseCode = "200", description = "The list of all organization in the OpenOLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")	
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisations(@QueryParam("externalId") String externalId,
			@QueryParam("identifier") String identifier, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<Organisation> organisations;
		if(StringHelper.containsNonWhitespace(identifier) || StringHelper.containsNonWhitespace(externalId)) {
			SearchOrganisationParameters params = new SearchOrganisationParameters();
			params.setExternalId(externalId);
			params.setIdentifier(identifier);
			organisations = organisationService.findOrganisations(params);
		} else {
			organisations = organisationService.getOrganisations();
		}
		OrganisationVO[] organisationVOes = toArrayOfVOes(organisations);
		return Response.ok(organisationVOes).build();
	}

	/**
	 * Creates and persists a new organization entity.
	 * 
	 * @param organisation The organization to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>organization</code>
	 */
	@PUT
	@Operation(summary = "Creates and persists a new organization entity", description = "Creates and persists a new organization entity")
	@ApiResponse(responseCode = "200", description = "The persisted organization", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putOrganisation(OrganisationVO organisation, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Organisation savedOrganisation = saveOrganisation(organisation);
		return Response.ok(OrganisationVO.valueOf(savedOrganisation)).build();
	}
	
	/**
	 * Updates a new organization entity.
	 * 
	 * @param organisation The organization to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization</code>
	 */
	@POST
	@Operation(summary = "Updates a new organization entity", description = "Updates a new organization entity")
	@ApiResponse(responseCode = "200", description = "The merged organization", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisation(OrganisationVO organisation, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Organisation savedOrganisation = saveOrganisation(organisation);
		return Response.ok(OrganisationVO.valueOf(savedOrganisation)).build();
	}
	
	@DELETE
	@Path("{organisationKey}")
	@Operation(summary = "Deletes an organization entity", description = "Deletes an new organization entity. Only administrators or "
			+ "system administrators of the organization can delete it. The users are moved to the default organisation.")
	@ApiResponse(responseCode = "200", description = "The organisation was successfully deleted", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The organization or the identity was not found")
	@ApiResponse(responseCode = "409", description = "Try to do something very dangerous")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		Roles roles = RestSecurityHelper.getRoles(httpRequest);
		OrganisationRef organisationRef = new OrganisationRefImpl(organisationKey);
		if(!roles.hasRole(organisationRef, OrganisationRoles.administrator) && !roles.hasRole(organisationRef, OrganisationRoles.sysadmin)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
			
		Organisation organisationToDelete = organisationService.getOrganisation(organisationRef);
		if(organisationToDelete == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();	
		}
		Organisation defOrganisation =  organisationService.getDefaultOrganisation();
		if(organisationToDelete.equals(defOrganisation)) {
			return Response.serverError().status(Status.CONFLICT).build();	
		}
		organisationService.deleteOrganisation(organisationToDelete, defOrganisation);
		return Response.ok().build();
	}
	
	/**
	 * Get the organizations where the specified user has the role.
	 * 
	 * @param role The role in organizations
	 * @param identityKey The user
	 * @param withInheritance With or without inheritance in the organization structure (default with)
	 * @param httpRequest The HTTP request
	 * @return The organization
	 */
	@GET
	@Path("membership/{role}/{identityKey}")
	@Operation(summary = "Get the organizations", description = "Get the organizations where the specified user has the role")
	@ApiResponse(responseCode = "200", description = "The list of organizations", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMemberships(@PathParam("role") String role, @PathParam("identityKey") Long identityKey,
			@QueryParam("withInheritance") Boolean withInheritance, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		if(!OrganisationRoles.isValue(role)) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		IdentityRef member = new IdentityRefImpl(identityKey);
		List<Organisation> organisations;
		if(withInheritance == null || withInheritance.booleanValue()) {
			organisations = organisationService.getOrganisations(member, OrganisationRoles.valueOf(role));
		} else {
			organisations = organisationService.getOrganisationsNotInherited(member, OrganisationRoles.valueOf(role));
		}
		OrganisationVO[] organisationVOes = toArrayOfVOes(organisations);
		return Response.ok(organisationVOes).build();
	}
	
	/**
	 * Get a specific organization.
	 * 
	 * @param organisationKey The organization primary key
	 * @param httpRequest The HTTP request
	 * @return The organization
	 */
	@GET
	@Path("{organisationKey}")
	@Operation(summary = "Get a specific organization", description = "Get a specific organization")
	@ApiResponse(responseCode = "200", description = "The list of all organizations in the OpenOLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = OrganisationVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		OrganisationVO organisationVo = OrganisationVO.valueOf(organisation);
		return Response.ok(organisationVo).build();
	}
	
	@GET
	@Path("{organisationKey}/entries")
	@Operation(summary = "Get entries", description = "Get entries of a specific organization")
	@ApiResponse(responseCode = "200", description = "The list of all entries in the organization system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RepositoryEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RepositoryEntryVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRepositoryEntriesInOrganisation(@PathParam("organisationKey") Long organisationKey, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		OrganisationRef organisation = new OrganisationRefImpl(organisationKey);
		List<RepositoryEntry> entries = repositoryService.getRepositoryEntryByOrganisation(organisation);
		RepositoryEntryVO[] entryVOes = new RepositoryEntryVO[entries.size()];
		for (int i=entries.size(); i-->0; ) {
			entryVOes[i] = RepositoryEntryVO.valueOf(entries.get(i));
		}
		return Response.ok(entryVOes).build();
	}
	
	/**
	 * Updates a new organization entity. the primary key is taken from
	 * the url. The organization object can be "primary key free".
	 * 
	 * @param organisationKey The organization primary key
	 * @param organisation The organization to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization</code>
	 */
	@POST
	@Path("{organisationKey}")
	@Operation(summary = "Updates a new organization entity", description = "Updates a new organization entity. the primary key is taken from\n" + 
			" the url. The organization object can be \"primary key free\"")
	@ApiResponse(responseCode = "200", description = "The merged organization", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisation(@PathParam("organisationKey") Long organisationKey, OrganisationVO organisation,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
			if((parentOrganisation != null && organisationToSave.getParent() == null)
					|| (parentOrganisation != null && organisationToSave.getParent() != null && !organisationToSave.getParent().getKey().equals(parentOrganisation.getKey()))) {
				move = true;
			}
		}
		
		organisationToSave.setCssClass(organisation.getCssClass());
		organisationToSave.setExternalId(organisation.getExternalId());
		organisationToSave.setManagedFlags(OrganisationManagedFlag.toEnum(organisation.getManagedFlagsString()));
		if(StringHelper.containsNonWhitespace(organisation.getStatus())) {
			organisationToSave.setOrganisationStatus(OrganisationStatus.valueOf(organisation.getStatus()));
		}
		
		Organisation savedOrganisation = organisationService.updateOrganisation(organisationToSave);
		if(move) {
			organisationService.moveOrganisation(savedOrganisation, parentOrganisation);
			dbInstance.commit();
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
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{organisationKey}/{role}")
	@Operation(summary = "Updates a new organization entity", description = "Updates a new organization entity. the primary key is taken from\n" + 
			" the url. The organization object can be \"primary key free\"")
	@ApiResponse(responseCode = "200", description = "The merged organization", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	public Response getMembers(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		return getMembers(organisationKey, getRoles(role));
	}
	
	private Response getMembers(Long organisationKey, OrganisationRoles role) {
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
	 * @param organisationKey The organization primary key
	 * @param role The role
	 * @param identityKey The member to make a coach of
	 * @param httpRequest The HTTP request
	 * @return Nothing
	 */
	@PUT
	@Path("{organisationKey}/{role}/{identityKey}")
	@Operation(summary = "Make the specified user a member of the specified organization", description = "Make the specified user a member of the specified organization\n" + 
			" with the specified role")
	@ApiResponse(responseCode = "200", description = "The membership was added")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The organization or the identity was not found")
	public Response putMember(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@PathParam("identityKey") Long identityKey, @QueryParam("inheritanceMode") String inheritanceMode,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		return putMember(organisationKey, identityKey, getRoles(role), inheritanceMode);
	}
	
	private Response putMember(Long organisationKey, Long identityKey, OrganisationRoles role, String inheritanceMode) {
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(role == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(GroupMembershipInheritance.isValueOf(inheritanceMode)) {
			organisationService.addMember(organisation, identity, role, GroupMembershipInheritance.valueOf(inheritanceMode));
		} else {
			organisationService.addMember(organisation, identity, role);
		}
		return Response.ok().build();
	}
	
	/**
	 * Add a membership to the specified curriculum element.
	 * 
	 * @param organisationKey The curriculum element primary key
	 * @param role The membership informations
	 * @return Nothing
	 */
	@PUT
	@Path("{organisationKey}/{role}")
	@Operation(summary = "Add a membership to the specified curriculum element", description = "Add a membership to the specified curriculum element")
	@ApiResponse(responseCode = "200", description = "The membership was persisted")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The curriculum element or the identity was not found")
	@ApiResponse(responseCode = "409", description = "The role is not allowed")
	public Response putMembers(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@QueryParam("inheritanceMode") String inheritanceMode, UserVO[] members,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(getRoles(role) == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		GroupMembershipInheritance inheritance = null;
		if(GroupMembershipInheritance.isValueOf(inheritanceMode)) {
			inheritance = GroupMembershipInheritance.valueOf(inheritanceMode);
		}

		int count = 0;
		for(UserVO member:members) {
			Identity identity = securityManager.loadIdentityByKey(member.getKey());
			if(identity != null) {
				if(inheritance == null) {
					organisationService.addMember(organisation, identity, getRoles(role));
				} else {
					organisationService.addMember(organisation, identity, getRoles(role), inheritance);
				}
			}
			if(++count % 25 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		return Response.ok().build();
	}
	
	/**
	 * Remove the membership of the identity from the specified organization and role.
	 * 
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{organisationKey}/{role}/{identityKey}")
	@Operation(summary = "Remove the membership", description = "Remove the membership of the identity from the specified organization and role")
	@ApiResponse(responseCode = "200", description = "The membership was removed")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The curriculum element or the identity was not found")
	public Response deleteMember(@PathParam("organisationKey") Long organisationKey, @PathParam("role") String role,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		return deleteMember(organisationKey, identityKey, getRoles(role));
	}
	
	private Response deleteMember(Long organisationKey, Long identityKey, OrganisationRoles role) {
		Organisation organisation = organisationService.getOrganisation(new OrganisationRefImpl(organisationKey));
		if(organisation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(role == null) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		organisationService.removeMember(organisation, identity, role, true);
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
	
	private boolean isAdministrator(HttpServletRequest request) {
		try {
			Roles roles = RestSecurityHelper.getRoles(request);
			return roles.isAdministrator() || roles.isSystemAdmin();
		} catch (Exception e) {
			return false;
		}
	}
}
