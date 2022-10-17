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

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeManagedFlag;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.core.id.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("organisations/types")
public class OrganisationTypesWebService {
	
	@Autowired
	private OrganisationService organisationService;

	/**
	 * List of organizations types.
	 * 
	 * @param httpRequest The HTTP request
	 * @return An array of organization types
	 */
	@GET
	@Operation(summary = "List of organizations types", description = "List of organizations types")
	@ApiResponse(responseCode = "200", description = "The list of all organization types in the OpenOLAT system", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganisationTypeVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = OrganisationTypeVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisations(@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<OrganisationType> organisationtypes = organisationService.getOrganisationTypes();
		OrganisationTypeVO[] organisationTypeVOes = toArrayOfVOes(organisationtypes);
		return Response.ok(organisationTypeVOes).build();
	}
	
	/**
	 * Creates and persists a new organization type entity.
	 * 
	 * @param organisationType The organization type to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>organization type</code>
	 */
	@PUT
	@Operation(summary = "Creates and persists a new organization type entity", description = "Creates and persists a new organization type entity")
	@ApiResponse(responseCode = "200", description = "Creates and persists a new organization type entity", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationTypeVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationTypeVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putOrganisationType(OrganisationTypeVO organisationType, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		OrganisationType savedOrganisationType = saveOrganisationType(organisationType);
		return Response.ok(OrganisationTypeVO.valueOf(savedOrganisationType)).build();
	}
	
	/**
	 * Updates a new organization type entity.
	 * 
	 * @param organisationType The organization type to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization type</code>
	 */
	@POST
	@Operation(summary = "Updates a new organization type entity", description = "Updates a new organization type entity")
	@ApiResponse(responseCode = "200", description = "The merged organization type", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationTypeVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationTypeVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisationType(OrganisationTypeVO organisationType, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		OrganisationType savedOrganisationType = saveOrganisationType(organisationType);
		return Response.ok(OrganisationTypeVO.valueOf(savedOrganisationType)).build();
	}
	
	/**
	 * Updates a new organization type entity. The primary key is taken from
	 * the URL. The organization type object can be "primary key free".
	 * 
	 * @param organisationTypeKey The organization type primary key
	 * @param organisationType The organization type to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization type</code>
	 */
	@POST
	@Path("{organisationTypeKey}")
	@Operation(summary = "Updates a new organization type entity", description = "Updates a new organization type entity. The primary key is taken from\n" + 
			" the URL. The organization type object can be \"primary key free\"")
	@ApiResponse(responseCode = "200", description = "The merged type organization", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationTypeVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationTypeVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisation(@PathParam("organisationTypeKey") Long organisationTypeKey, OrganisationTypeVO organisationType,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		if(organisationType.getKey() == null) {
			organisationType.setKey(organisationTypeKey);
		} else if(!organisationTypeKey.equals(organisationType.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		OrganisationType savedOrganisationType = saveOrganisationType(organisationType);
		return Response.ok(OrganisationTypeVO.valueOf(savedOrganisationType)).build();
	}
	
	private OrganisationType saveOrganisationType(OrganisationTypeVO organisationTypeVo) {
		OrganisationType organisationType;
		if(organisationTypeVo.getKey() == null) {
			organisationType = organisationService.createOrganisationType(organisationTypeVo.getDisplayName(),
					organisationTypeVo.getIdentifier(), organisationTypeVo.getDescription());
		} else {
			organisationType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisationTypeVo.getKey()));
			organisationType.setDisplayName(organisationTypeVo.getDisplayName());
			organisationType.setIdentifier(organisationTypeVo.getIdentifier());
			organisationType.setDescription(organisationTypeVo.getDescription());
		}
		
		organisationType.setCssClass(organisationTypeVo.getCssClass());
		organisationType.setExternalId(organisationTypeVo.getExternalId());
		organisationType.setManagedFlags(OrganisationTypeManagedFlag.toEnum(organisationTypeVo.getManagedFlagsString()));
		return organisationService.updateOrganisationType(organisationType);
	}
	
	/**
	 * Get a specific organization type.
	 * 
	 * @param organisationTypeKey The organization type primary key
	 * @param httpRequest The HTTP request
	 * @return The organization
	 */
	@GET
	@Path("{organisationTypeKey}")
	@Operation(summary = "Get a specific organization type", description = "Get a specific organization type")
	@ApiResponse(responseCode = "200", description = "The organization type", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationTypeVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationTypeVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisations(@PathParam("organisationTypeKey") Long organisationTypeKey,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		OrganisationType organisationType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisationTypeKey));
		if(organisationType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(OrganisationTypeVO.valueOf(organisationType)).build();
	}
	

	/**
	 * Get the allowed sub-types of a specified organization type.
	 * 
	 * @param organisationTypeKey The organization type primary key
	 * @param httpRequest  The HTTP request
	 * @return An array of organization types
	 */
	@GET
	@Operation(summary = "Get the allowed sub-types of a specified organization type", description = "Get the allowed sub-types of a specified organization type")
	@ApiResponse(responseCode = "200", description = "An array of organization types", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationTypeVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationTypeVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The organization type was not found")
	@Path("{organisationTypeKey}/allowedSubTypes")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAllowedSubTypes(@PathParam("organisationTypeKey") Long organisationTypeKey, @Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		OrganisationType type = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisationTypeKey));
		if(type == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Set<OrganisationTypeToType> typeToTypes = type.getAllowedSubTypes();
		List<OrganisationTypeVO> subTypeVOes = new ArrayList<>(typeToTypes.size());
		for(OrganisationTypeToType typeToType:typeToTypes) {
			OrganisationType subType = typeToType.getAllowedSubOrganisationType();
			subTypeVOes.add(OrganisationTypeVO.valueOf(subType));
		}
		return Response.ok(subTypeVOes.toArray(new OrganisationTypeVO[subTypeVOes.size()])).build();
	}
	
	/**
	 * Add a sub-type to a specified organization type.
	 * 
	 * @param organisationTypeKey The type
	 * @param subTypeKey The sub type
	 * @param httpRequest  The HTTP request
	 * @return Nothing
	 */
	@PUT
	@Path("{organisationTypeKey}/allowedSubTypes/{subTypeKey}")
	@Operation(summary = "Add a sub-type", description = "Add a sub-type to a specified organization type")
	@ApiResponse(responseCode = "200", description = "The sub type was added to the allowed sub types", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = OrganisationTypeVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = OrganisationTypeVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The organization type was not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response allowSubTaxonomyLevelType(@PathParam("organisationTypeKey") Long organisationTypeKey, @PathParam("subTypeKey") Long subTypeKey,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		OrganisationType type = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisationTypeKey));
		OrganisationType subType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(subTypeKey));
		if(type == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		organisationService.allowOrganisationSubType(type, subType);
		return Response.ok().build();
	}
	
	/**
	 * Remove a sub-type to a specified organization type.
	 * 
	 * @param organisationTypeKey The type
	 * @param subTypeKey The sub type to remove
	 * @param httpRequest  The HTTP request
	 * @return Nothing
	 */
	@DELETE
	@Path("{organisationTypeKey}/allowedSubTypes/{subTypeKey}")
	@Operation(summary = "Remove a sub-type", description = "Remove a sub-type to a specified organization type")
	@ApiResponse(responseCode = "200", description = "The sub type was removed successfully")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The organization type was not found")
	public Response disalloweSubTaxonomyLevelType(@PathParam("organisationTypeKey") Long organisationTypeKey, @PathParam("subTypeKey") Long subTypeKey,
			@Context HttpServletRequest httpRequest) {
		if(!isAdministrator(httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		OrganisationType type = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisationTypeKey));
		OrganisationType subType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(subTypeKey));
		if(type == null || subType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		organisationService.disallowOrganisationSubType(type, subType);
		return Response.ok().build();
	}
	
	private OrganisationTypeVO[] toArrayOfVOes(List<OrganisationType> organisationTypes) {
		int i=0;
		OrganisationTypeVO[] entryVOs = new OrganisationTypeVO[organisationTypes.size()];
		for (OrganisationType organisationType : organisationTypes) {
			entryVOs[i++] = OrganisationTypeVO.valueOf(organisationType);
		}
		return entryVOs;
	}
	
	public static boolean isAdministrator(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return roles.isAdministrator() || roles.isSystemAdmin();
		} catch (Exception e) {
			return false;
		}
	}

}
