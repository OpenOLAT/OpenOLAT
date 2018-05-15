package org.olat.user.restapi;

import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeManagedFlag;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.core.CoreSpringFactory;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTypesWebService {
	
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
	 * List of organizations types.
	 * 
	 * @response.representation.200.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The list of all organization types in the OpenOLAT system
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest The HTTP request
	 * @return An array of organization types
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisations(@Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		List<OrganisationType> organisationtypes = organisationService.getOrganisationTypes();
		OrganisationTypeVO[] organisationTypeVOes = toArrayOfVOes(organisationtypes);
		return Response.ok(organisationTypeVOes).build();
	}
	
	/**
	 * Creates and persists a new organization type entity.
	 * 
	 * @response.representation.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The organization type to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted organization type
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param organisationType The organization type to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>organization type</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putOrganisationType(OrganisationTypeVO organisationType, @Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		OrganisationType savedOrganisationType = saveOrganisationType(organisationType);
		return Response.ok(OrganisationTypeVO.valueOf(savedOrganisationType)).build();
	}
	
	/**
	 * Updates a new organization type entity.
	 * 
	 * @response.representation.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The organization type to update
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged organization type
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param organisationType The organization type to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization type</code>
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisationType(OrganisationTypeVO organisationType, @Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		OrganisationType savedOrganisationType = saveOrganisationType(organisationType);
		return Response.ok(OrganisationTypeVO.valueOf(savedOrganisationType)).build();
	}
	
	/**
	 * Updates a new organization type entity. The primary key is taken from
	 * the URL. The organization type object can be "primary key free".
	 * 
	 * @response.representation.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The organization type to update
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged type organization
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param organisationTypeKey The organization type primary key
	 * @param organisationType The organization type to merge
	 * @param request The HTTP request
	 * @return The merged <code>organization type</code>
	 */
	@POST
	@Path("{organisationTypeKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postOrganisation(@PathParam("organisationTypeKey") Long organisationTypeKey, OrganisationTypeVO organisationType,
			@Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
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
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
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
	 * @response.representation.200.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The organization type
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param organisationTypeKey The organization type primary key
	 * @param httpRequest The HTTP request
	 * @return The organization
	 */
	@GET
	@Path("{organisationTypeKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOrganisations(@PathParam("organisationTypeKey") Long organisationTypeKey,
			@Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
		OrganisationType organisationType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(organisationTypeKey));
		if(organisationType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(OrganisationTypeVO.valueOf(organisationType)).build();
	}
	

	/**
	 * Get the allowed sub-types of a specified organization type.
	 * 
	 * @response.representation.200.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc An array of organization types
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The organization type was not found
	 * @param organisationTypeKey The organization type primary key
	 * @param httpRequest  The HTTP request
	 * @return An array of organization types
	 */
	@GET
	@Path("{organisationTypeKey}/allowedSubTypes")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAllowedSubTypes(@PathParam("organisationTypeKey") Long organisationTypeKey, @Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
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
	 * @response.representation.200.qname {http://www.example.com}organisationTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The sub type was added to the allowed sub types
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_ORGANISATIONTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The organization type was not found
	 * @param organisationTypeKey The type
	 * @param subTypeKey The sub type
	 * @param httpRequest  The HTTP request
	 * @return Nothing
	 */
	@PUT
	@Path("{organisationTypeKey}/allowedSubTypes/{subTypeKey}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response allowSubTaxonomyLevelType(@PathParam("organisationTypeKey") Long organisationTypeKey, @PathParam("subTypeKey") Long subTypeKey,
			@Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
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
	 * @response.representation.200.doc The sub type was removed successfully
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The organization type was not found
	 * @param organisationTypeKey The type
	 * @param subTypeKey The sub type to remove
	 * @param httpRequest  The HTTP request
	 * @return Nothing
	 */
	@DELETE
	@Path("{organisationTypeKey}/allowedSubTypes/{subTypeKey}")
	public Response disalloweSubTaxonomyLevelType(@PathParam("organisationTypeKey") Long organisationTypeKey, @PathParam("subTypeKey") Long subTypeKey,
			@Context HttpServletRequest httpRequest) {
		if(!isAdmin(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		OrganisationService organisationService = CoreSpringFactory.getImpl(OrganisationService.class);
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

}
