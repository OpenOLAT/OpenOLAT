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
package org.olat.modules.invitation.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * 
 * Initial date: 16 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag (name = "Invitations")
public class BusinessGroupInvitationsWebService {

	private BusinessGroup businessGroup;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private InvitationModule invitationModule;
	
	public BusinessGroupInvitationsWebService(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}
	
	/**
	 * Create a new invitation.
	 * 
	 * @param firstName The first name
	 * @param lastName The last name
	 * @param email The email
	 * @param registrationRequiered If login is mandatory
	 * @param request The HTTP request
	 * @return The response
	 */
	@POST
	@Operation(summary = "Creates an invitation", description = "Creates an invitation for a business group")
	@ApiResponse(responseCode = "200", description = "The invitation object", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postInvitationForRepositoryEntry(@QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName, @QueryParam("email") String email,
			@QueryParam("registrationRequired") @DefaultValue("true") Boolean registrationRequiered,
			@Context HttpServletRequest request) {
		return createInvitation(firstName, lastName, email, registrationRequiered, request);
	}
	
	/**
	 * Creates a new invitation.
	 * 
	 * @param firstName The first name
	 * @param lastName The last name
	 * @param email The email
	 * @param registrationRequiered If login is mandatory
	 * @param request The HTTP request
	 * @return The invitation
	 */
	@PUT
	@Operation(summary = "Creates an invitation", description = "Creates an invitation for a business group")
	@ApiResponse(responseCode = "200", description = "The invitation object", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putInvitationForRepositoryEntry(@QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName, @QueryParam("email") String email,
			@QueryParam("registrationRequired") @DefaultValue("true") Boolean registrationRequiered,
			@Context HttpServletRequest request) {
		return createInvitation(firstName, lastName, email, registrationRequiered, request);
	}
	
	private Response createInvitation(String firstName, String lastName, String email,
			Boolean registrationRequiered, HttpServletRequest request) {
	
		if(!invitationModule.isBusinessGroupInvitationEnabled()) {
			return Response.serverError().status(Status.METHOD_NOT_ALLOWED).build();	
		}
		if(!hasPermission(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		Identity doer = getIdentity(request);
		UserRequest ureq = getUserRequest(request);

		List<String> roles = List.of(GroupRoles.participant.name());
		Invitation invitation = invitationService.createInvitation(InvitationTypeEnum.businessGroup);
		invitation.setFirstName(firstName);
		invitation.setLastName(lastName);
		invitation.setMail(email);
		invitation.setRegistration(registrationRequiered == null || registrationRequiered.booleanValue());
		invitation.setRoleList(roles);
		
		invitation = createOrUpdateTemporaryInvitation(invitation, ureq.getLocale(), doer);
		
		String url = invitationService.toUrl(invitation, businessGroup);
		InvitationVO vo = InvitationVO.valueOf(invitation, url);
		return Response.ok(vo).build();
	}
	
	private Invitation createOrUpdateTemporaryInvitation(Invitation invitation, Locale locale, Identity doer) {
		Group group = businessGroup.getBaseGroup();
		Invitation similarInvitation = invitationService.findSimilarInvitation(InvitationTypeEnum.businessGroup,
				invitation.getMail(), invitation.getRoleList(), group);
		if(similarInvitation == null) {
			invitationService.getOrCreateIdentityAndPersistInvitation(invitation, group, locale, doer);
		} else {
			if(similarInvitation.getStatus() != InvitationStatusEnum.active) {
				similarInvitation.setStatus(InvitationStatusEnum.active);
				similarInvitation = invitationService.update(similarInvitation);
			}
			invitation = similarInvitation;
		}
		return invitation;
	}
	
	/**
	 * Create or update an invitation for an external user to a business group.
	 * 
	 * @param invitation The invitation
	 * @param request The HTTP request
	 * @return The invitation
	 */
	@PUT
	@Operation(summary = "Creates or update an invitation", description = "Creates or update an invitation for a business group")
	@ApiResponse(responseCode = "200", description = "The invitation object", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createOrUpdateInvitationForRepositoryEntryPut(InvitationVO invitation, @Context HttpServletRequest request) {
		return saveOrUpdate(invitation, request);
	}
	
	@POST
	@Operation(summary = "Creates or update an invitation", description = "Creates or update an invitation for a business group")
	@ApiResponse(responseCode = "200", description = "The invitation object", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response createOrUpdateInvitationForRepositoryEntryPost(InvitationVO invitation, @Context HttpServletRequest request) {
		return saveOrUpdate(invitation, request);
	}
	
	private Response saveOrUpdate(InvitationVO invitationVo, @Context HttpServletRequest request) {
		if(!invitationModule.isBusinessGroupInvitationEnabled()) {
			return Response.serverError().status(Status.METHOD_NOT_ALLOWED).build();	
		}
		if(!hasPermission(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity doer = getIdentity(request);
		UserRequest ureq = getUserRequest(request);
		
		Invitation invitation;
		if(invitationVo.getKey() == null) {
			List<String> roles = List.of(GroupRoles.participant.name());
			invitation = invitationService.createInvitation(InvitationTypeEnum.businessGroup);

			invitation.setRegistration(invitationVo.getRegistration());
			invitation.setFirstName(invitationVo.getFirstName());
			invitation.setLastName(invitationVo.getLastName());
			invitation.setMail(invitationVo.getEmail());
			invitation.setRegistration(invitationVo.getRegistration() == null || invitationVo.getRegistration());
			invitation.setRoleList(roles);
			
			invitation = createOrUpdateTemporaryInvitation(invitation, ureq.getLocale(), doer);	
		} else {
			invitation = invitationService.getInvitationByKey(invitationVo.getKey());
			if(invitationVo.getRegistration() != null) {
				invitation.setRegistration(invitationVo.getRegistration());
			}
			if(StringHelper.containsNonWhitespace(invitationVo.getStatus())) {
				invitation.setStatus(InvitationStatusEnum.valueOf(invitationVo.getStatus()));
			}
			invitation = invitationService.update(invitation, invitationVo.getFirstName(), invitationVo.getLastName(), invitationVo.getEmail());
		}
		
		String url = invitationService.toUrl(invitation, businessGroup);
		InvitationVO updatedInvitationVo = InvitationVO.valueOf(invitation, url);
		dbInstance.commitAndCloseSession();
		return Response.ok(updatedInvitationVo).build();
	}
	
	/**
	 * Retrieve the list of invitations of the specified business group.
	 */
	@GET
	@Operation(summary = "Get the list invitations in the specified group",
		description = "Get the list invitations in the specified group")
	@ApiResponse(responseCode = "200", description = "The list of invitations", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getInvitations(@Context HttpServletRequest request) {
		if(!invitationModule.isBusinessGroupInvitationEnabled()) {
			return Response.serverError().status(Status.METHOD_NOT_ALLOWED).build();	
		}
		if(!hasPermission(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		SearchInvitationParameters params = new SearchInvitationParameters();
		List<Invitation> invitations = invitationService.findInvitations(businessGroup, params);
		InvitationVO[] voes = new InvitationVO[invitations.size()];
		for(int i=invitations.size(); i-->0; ) {
			Invitation invitation = invitations.get(i);
			String url = invitationService.toUrl(invitation, businessGroup);
			voes[i] = InvitationVO.valueOf(invitation, url);
		}
		return Response.ok(voes).build();
	}
	
	/**
	 * Retrieve an invitation by its primary key.
	 * 
	 * @param invitationKey The invitation key
	 * @param request The HTTP request
	 * @return The invitation
	 */
	@GET
	@Path("{invitationKey}")
	@Operation(summary = "Get an invitation in the specified business group by its primary key",
		description = "Get an invitation in the specified business group by its primary key")
	@ApiResponse(responseCode = "200", description = "The list of invitations", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getInvitationByKey(@PathParam("invitationKey") Long invitationKey,
			@Context HttpServletRequest request) {
		if(!invitationModule.isBusinessGroupInvitationEnabled()) {
			return Response.serverError().status(Status.METHOD_NOT_ALLOWED).build();	
		}
		if(!hasPermission(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Invitation invitation = invitationService.getInvitationByKey(invitationKey);
		if(invitation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Group baseGroup = businessGroup.getBaseGroup();
		if(!baseGroup.equals(invitation.getBaseGroup())) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		String url = invitationService.toUrl(invitation, businessGroup);
		InvitationVO invitationVo = InvitationVO.valueOf(invitation, url);
		return Response.ok(invitationVo).build();
	}
	
	/**
	 * Delete the invitation of a specific business group.
	 */
	@DELETE
	@Path("{invitationKey}")
	@Operation(summary = "Delete an invitation in the specified business group by its primary key",
		description = "Delete an invitation in the specified business group by its primary key")
	@ApiResponse(responseCode = "200", description = "The list of invitations", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = InvitationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = InvitationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The business group was not found")
	@ApiResponse(responseCode = "405", description = "The feature is not enabled")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteInvitationByKey(@PathParam("invitationKey") Long invitationKey,
			@Context HttpServletRequest request) {
		if(!invitationModule.isBusinessGroupInvitationEnabled()) {
			return Response.serverError().status(Status.METHOD_NOT_ALLOWED).build();	
		}
		if(!hasPermission(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Invitation invitation = invitationService.getInvitationByKey(invitationKey);
		if(invitation == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		Group baseGroup = businessGroup.getBaseGroup();
		if(!baseGroup.equals(invitation.getBaseGroup())) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		invitationService.deleteInvitation(invitation);
		return Response.ok().build();
	}
	
	private boolean hasPermission(HttpServletRequest request) {
		try {
			Identity identity = getUserRequest(request).getIdentity();
			return repositoryService.hasRoleExpanded(identity, OrganisationRoles.administrator.name());
		} catch (Exception e) {
			return false;
		}
	}
}
