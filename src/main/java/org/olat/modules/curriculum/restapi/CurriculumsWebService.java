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
package org.olat.modules.curriculum.restapi;


import static org.olat.restapi.security.RestSecurityHelper.getRoles;
import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Curriculum")
@Component
@Path("curriculum")
public class CurriculumsWebService {
	
	private static final String VERSION = "1.0";
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
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
	 * Return the curriculums a manager user is allowed to see.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc An array of curriculums
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest  The HTTP request
	 * @return An array of curriculums
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculums(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		CurriculumSearchParameters params = new CurriculumSearchParameters();
		List<OrganisationRef> organisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator, OrganisationRoles.curriculummanager);
		if(organisations.isEmpty()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		params.setOrganisations(organisations);
		List<Curriculum> curriculums = curriculumService.getCurriculums(params);
		List<CurriculumVO> voes = new ArrayList<>(curriculums.size());
		for(Curriculum curriculum:curriculums) {
			voes.add(CurriculumVO.valueOf(curriculum));
		}
		return Response.ok(voes.toArray(new CurriculumVO[voes.size()])).build();
	}
	

	public CurriculumElementTypesWebService getCurriculumElementTypesWebService(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
		}
		return new CurriculumElementTypesWebService();
	}

	/**
	 * Creates and persists a new curriculum.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum to persist
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted curriculum
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculum The curriculum to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>curriculum</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCurriculum(CurriculumVO curriculum, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Curriculum savedCurriculum = saveCurriculum(curriculum, httpRequest);
		return Response.ok(CurriculumVO.valueOf(savedCurriculum)).build();
	}
	
	/**
	 * Updates a curriculum entity.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum to update
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged curriculum
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculum The curriculum to merge
	 * @param request The HTTP request
	 * @return The merged <code>curriculum</code>
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculum(CurriculumVO curriculum, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Curriculum savedCurriculum = saveCurriculum(curriculum, httpRequest);
		return Response.ok(CurriculumVO.valueOf(savedCurriculum)).build();
	}
	
	/**
	 * Get a specific curriculum.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The curriculum
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param curriculumKey The curriculum primary key
	 * @param httpRequest The HTTP request
	 * @return The curriculum
	 */
	@GET
	@Path("{curriculumKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculum(@PathParam("curriculumKey") Long curriculumKey, @Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		CurriculumVO curriculumVo = CurriculumVO.valueOf(curriculum);
		return Response.ok(curriculumVo).build();
	}

	@Path("{curriculumKey}/elements")
	public CurriculumElementsWebService getCurriculumElementWebService(@PathParam("curriculumKey") Long curriculumKey,
			@Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if(!isManager(curriculum, httpRequest)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
		return new CurriculumElementsWebService(curriculum);
	}
	
	/**
	 * Updates a curriculum entity. The primary key is taken from
	 * the URL. The curriculum object can be "primary key free".
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum to update
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged curriculum
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumKey The curriculum primary key
	 * @param curriculum The curriculum to merge
	 * @param request The HTTP request
	 * @return The merged <code>curriculum</code>
	 */
	@POST
	@Path("{curriculumKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculum(@PathParam("curriculumKey") Long curriculumKey, CurriculumVO curriculum, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		if(curriculum.getKey() == null) {
			curriculum.setKey(curriculumKey);
		} else if(!curriculumKey.equals(curriculum.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		Curriculum savedCurriculum = saveCurriculum(curriculum, httpRequest);
		return Response.ok(CurriculumVO.valueOf(savedCurriculum)).build();
	}
	
	private Curriculum saveCurriculum(CurriculumVO curriculum, HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		
		Curriculum curriculumToSave = null;
		Organisation organisation = null;
		if(curriculum.getOrganisationKey() != null) {
			organisation = organisationService.getOrganisation(new OrganisationRefImpl(curriculum.getOrganisationKey()));
			allowedOrganisation(organisation, roles);//check if the user can manage this organisation's curriculum
		}

		if(curriculum.getKey() == null) {
			curriculumToSave = curriculumService.createCurriculum(curriculum.getIdentifier(), curriculum.getDisplayName(),
					curriculum.getDescription(), organisation);
		} else {
			curriculumToSave = curriculumService.getCurriculum(new CurriculumRefImpl(curriculum.getKey()));
			if(!isManager(curriculumToSave, httpRequest)) {
				throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
			}
			
			curriculumToSave.setDisplayName(curriculum.getDisplayName());
			curriculumToSave.setIdentifier(curriculum.getIdentifier());
			curriculumToSave.setDescription(curriculum.getDescription());
			curriculumToSave.setOrganisation(organisation);
		}
		
		curriculumToSave.setExternalId(curriculum.getExternalId());
		curriculumToSave.setManagedFlags(CurriculumManagedFlag.toEnum(curriculum.getManagedFlagsString()));
		curriculumToSave.setStatus(curriculum.getStatus());
		curriculumToSave.setDegree(curriculum.getDegree());
		return curriculumService.updateCurriculum(curriculumToSave);
	}
	
	private void allowedOrganisation(Organisation organisation, Roles roles) {
		if(roles.isAdministrator() || organisation == null) return;

		List<OrganisationRef> managedOrganisations = roles.getOrganisationsWithRole(OrganisationRoles.curriculummanager);
		for(OrganisationRef managedOrganisation:managedOrganisations) {
			if(managedOrganisation.getKey().equals(organisation.getKey())) {
				return;
			}
		}

		throw new WebApplicationException(Response.serverError().status(Status.UNAUTHORIZED).build());
	}
	
	@GET
	@Path("elements")
	public Response searchCurriculumElement(@QueryParam("externalId") String externalId, @QueryParam("identifier") String identifier,
			@QueryParam("key") Long key, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<CurriculumElement> elements = curriculumService.searchCurriculumElements(externalId, identifier, key);
		CurriculumElementVO[] voes = new CurriculumElementVO[elements.size()];
		for(int i=elements.size(); i-->0; ) {
			voes[i] = CurriculumElementVO.valueOf(elements.get(i));
		}
		return Response.ok(voes).build();
	}
	
	/**
	 * Get all curriculum managers of the specified curriculum.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of curriculum managers
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element not found
	 * @param curriculumKey The curriculum primary key
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumKey}/curriculumowners")
	public Response getCurriculumManagers(@PathParam("curriculumKey") Long curriculumKey, @Context HttpServletRequest httpRequest) {
		return getMembers(curriculumKey, CurriculumRoles.curriculumowner, httpRequest);
	}

	private Response getMembers(Long curriculumKey, CurriculumRoles role, HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<Identity> members = curriculumService.getMembersIdentity(curriculum, role);
		List<UserVO> voList = new ArrayList<>(members.size());
		for(Identity member:members) {
			voList.add(UserVOFactory.get(member));
		}
		return Response.ok(voList.toArray(new UserVO[voList.size()])).build();
	}
	
	/**
	 * Make the specified user a curriculum manager of the curriculum.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumKey The curriculum primary key
	 * @param identityKey The member to make a curriculum manager of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumKey}/curriculumowners/{identityKey}")
	public Response putCurriculumOwner(@PathParam("curriculumKey") Long curriculumKey,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		return putMember(curriculumKey, identityKey, CurriculumRoles.curriculumowner, httpRequest);
	}
	
	private Response putMember(Long curriculumKey, Long identityKey, CurriculumRoles role, HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		curriculumService.addMember(curriculum, identity, role);
		return Response.ok().build();
	}
	
	/**
	 * Remove the curriculum manager membership of the identity from the specified curriculum .
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum or the identity was not found
	 * @param curriculumElementKey The curriculum primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumKey}/curriculumowners/{identityKey}")
	public Response deleteCurriculumManager(@PathParam("curriculumKey") Long curriculumKey,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		return deleteMember(curriculumKey, identityKey, CurriculumRoles.curriculumowner, httpRequest);
	}
	
	private Response deleteMember(Long curriculumKey, Long identityKey, CurriculumRoles role, HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		curriculumService.removeMember(curriculum, identity, role);
		return Response.ok().build();
	}
	
	private boolean isManager(Curriculum curriculum, HttpServletRequest httpRequest) {
		Identity identity = getIdentity(httpRequest);
		return curriculumService.hasRoleExpanded(curriculum, identity,
				OrganisationRoles.administrator.name(), OrganisationRoles.curriculummanager.name(),
				CurriculumRoles.curriculummanager.name(), CurriculumRoles.owner.name());
	}
}
