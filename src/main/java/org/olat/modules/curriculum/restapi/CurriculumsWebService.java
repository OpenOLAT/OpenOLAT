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


import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

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
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.IdentityRefImpl;
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
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.restapi.LectureBlockStatisticsVO;
import org.olat.modules.lecture.ui.coach.LecturesSearchFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
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
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	/**
	 * 
	 * @return The version number
	 */
	@GET
	@Path("version")
	@Operation(summary = "Get the version of the User Web Service",
		description = "Get the version of the User Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Return the curriculums a manager user is allowed to see.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An array of curriculums
	 */
	@GET
	@Operation(summary = "Return the curriculums a manager user is allowed to see",
		description = "Return the curriculums a manager user is allowed to see")
	@ApiResponse(responseCode = "200", description = "An array of curriculums",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CurriculumVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CurriculumVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculums(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		CurriculumSearchParameters params = new CurriculumSearchParameters();
		List<OrganisationRef> organisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator, OrganisationRoles.curriculummanager);
		if(organisations.isEmpty()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		params.setOrganisations(organisations);
		List<Curriculum> curriculums = curriculumService.getCurriculums(params);
		List<CurriculumVO> voes = new ArrayList<>(curriculums.size());
		for(Curriculum curriculum:curriculums) {
			voes.add(CurriculumVO.valueOf(curriculum));
		}
		return Response.ok(voes.toArray(new CurriculumVO[voes.size()])).build();
	}

	/**
	 * Creates and persists a new curriculum.
	 * 
	 * @param curriculum The curriculum to persist
	 * @param request The HTTP request
	 * @return The new persisted <code>curriculum</code>
	 */
	@PUT
	@Operation(summary = "Creates and persists a new curriculum",
		description = "Creates and persists a new curriculum")
	@ApiResponse(responseCode = "200", description = "The persisted curriculum",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCurriculum(CurriculumVO curriculum, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Curriculum savedCurriculum = saveCurriculum(curriculum, httpRequest);
		return Response.ok(CurriculumVO.valueOf(savedCurriculum)).build();
	}
	
	/**
	 * Updates a curriculum entity.
	 * 
	 * @param curriculum The curriculum to merge
	 * @param request The HTTP request
	 * @return The merged <code>curriculum</code>
	 */
	@POST
	@Operation(summary = "Updates a curriculum entity",
		description = "Updates a curriculum entity")
	@ApiResponse(responseCode = "200", description = "The curriculum to update",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculum(CurriculumVO curriculum, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Curriculum savedCurriculum = saveCurriculum(curriculum, httpRequest);
		return Response.ok(CurriculumVO.valueOf(savedCurriculum)).build();
	}
	
	/**
	 * Get a specific curriculum.
	 * 
	 * @param curriculumKey The curriculum primary key
	 * @param httpRequest The HTTP request
	 * @return The curriculum
	 */
	@GET
	@Path("{curriculumKey}")
	@Operation(summary = "Get a specific curriculum",
		description = "Get a specific curriculum")
	@ApiResponse(responseCode = "200", description = "The curriculum",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculum(@PathParam("curriculumKey") Long curriculumKey, @Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		CurriculumVO curriculumVo = CurriculumVO.valueOf(curriculum);
		return Response.ok(curriculumVo).build();
	}
	
	@DELETE
	@Path("{curriculumKey}")
	@Operation(summary = "Delete a specific curriculum",
		description = "Delete a specific curriculum")
	@ApiResponse(responseCode = "200", description = "")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteCurriculum(@PathParam("curriculumKey") Long curriculumKey, @Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		curriculumService.deleteCurriculum(curriculum);
		return Response.ok().build();
	}

	@Path("{curriculumKey}/elements")
	public CurriculumElementsWebService getCurriculumElementWebService(@PathParam("curriculumKey") Long curriculumKey,
			@Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if(!isManager(curriculum, httpRequest)) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		return new CurriculumElementsWebService(curriculum);
	}
	
	/**
	 * Updates a curriculum entity. The primary key is taken from
	 * the URL. The curriculum object can be "primary key free".
	 * 
	 * @param curriculumKey The curriculum primary key
	 * @param curriculum The curriculum to merge
	 * @param request The HTTP request
	 * @return The merged <code>curriculum</code>
	 */
	@POST
	@Path("{curriculumKey}")
	@Operation(summary = "Update a curriculum entity",
		description = "Updates a curriculum entity. The primary key is taken from\n" + 
			" the URL. The curriculum object can be \"primary key free\"")
	@ApiResponse(responseCode = "200", description = "The merged curriculum",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = CurriculumVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = CurriculumVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculum(@PathParam("curriculumKey") Long curriculumKey, CurriculumVO curriculum, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
					curriculum.getDescription(), false, organisation);
		} else {
			curriculumToSave = curriculumService.getCurriculum(new CurriculumRefImpl(curriculum.getKey()));
			if(!isManager(curriculumToSave, httpRequest)) {
				throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
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

		throw new WebApplicationException(Response.serverError().status(Status.FORBIDDEN).build());
	}
	
	@GET
	@Path("elements")
	@Operation(summary = "Get the elements of all curriculums",
	description = "Get all the elements of all curriculums")	
	@ApiResponse(responseCode = "200", description = "The elements",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CurriculumElementVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CurriculumElementVO.class)))
				} 
	)
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	public Response searchCurriculumElement(@QueryParam("externalId") String externalId, @QueryParam("identifier") String identifier,
			@QueryParam("key") Long key, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isCurriculumManager()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * @param curriculumKey The curriculum primary key
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumKey}/curriculumowners")
	@Operation(summary = "Get all curriculum managers",
		description = "Get all curriculum managers of the specified curriculum")
	@ApiResponse(responseCode = "200", description = "The array of curriculum managers",
			content = {
					@Content(mediaType = "application/json", array  = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
					@Content(mediaType = "application/xml", array  = @ArraySchema(schema = @Schema(implementation = UserVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "406", description = "application/xml, application/json")
	public Response getCurriculumManagers(@PathParam("curriculumKey") Long curriculumKey, @Context HttpServletRequest httpRequest) {
		return getMembers(curriculumKey, CurriculumRoles.curriculumowner, httpRequest);
	}

	private Response getMembers(Long curriculumKey, CurriculumRoles role, HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * @param curriculumKey The curriculum primary key
	 * @param identityKey The member to make a curriculum manager of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumKey}/curriculumowners/{identityKey}")
	@Operation(summary = "Make the specified user a curriculum manager of the curriculum",
		description = "Make the specified user a curriculum manager of the curriculum")
	@ApiResponse(responseCode = "200", description = "The membership was added")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The curriculum element or the identity was not found")
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
			return Response.serverError().status(Status.FORBIDDEN).build();
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
	 * @param curriculumElementKey The curriculum primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumKey}/curriculumowners/{identityKey}")
	@Operation(summary = "Remove the curriculum manager membership",
		description = "Remove the curriculum manager membership of the identity from the specified curriculum")
	@ApiResponse(responseCode = "200", description = "The membership was removed")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The curriculum element or the identity was not found")
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
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		curriculumService.removeMember(curriculum, identity, role);
		return Response.ok().build();
	}
	
	@GET
	@Operation(summary = "Return statistics about absences of a specific curriculun",
		description = "Return the statistics about absences of a specific curriculum")
	@ApiResponse(responseCode = "200", description = "Statistics object",
		content = {
				@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LectureBlockIdentityStatistics.class))),
				@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LectureBlockIdentityStatistics.class)))
			})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Curriculum or user data not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("{curriculumKey}/lectures/")
	public Response getLecturesStatistics(@PathParam("curriculumKey") Long curriculumKey, @Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity manager = getIdentity(httpRequest);
		List<LectureBlockIdentityStatistics> statistics = getStatisticsBy(curriculum, manager, null);
		LectureBlockStatisticsVO[] statisticsVoes = new LectureBlockStatisticsVO[statistics.size()];
		for(int i=statisticsVoes.length; i-->0; ) {
			statisticsVoes[i] = LectureBlockStatisticsVO.valueOf(statistics.get(i));
		}
		return Response.ok(statisticsVoes).build();
	}
	
	@GET
	@Operation(summary = "Return statistics about absences of a specific user",
		description = "Return the statistics about absences of a specific user in a curriculum")
	@ApiResponse(responseCode = "200", description = "Statistics object",
		content = {
				@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockIdentityStatistics.class)),
				@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockIdentityStatistics.class))
			})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Curriculum or user data not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Path("{curriculumKey}/lectures/{identityKey}")
	public Response getParticipantLecturesStatistics(@PathParam("curriculumKey") Long curriculumKey,
			@PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest) {
		Curriculum curriculum = curriculumService.getCurriculum(new CurriculumRefImpl(curriculumKey));
		if(curriculum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isManager(curriculum, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity manager = getIdentity(httpRequest);
		List<LectureBlockIdentityStatistics> statistics = getStatisticsBy(curriculum, manager, new IdentityRefImpl(identityKey));
		for(LectureBlockIdentityStatistics stats:statistics) {
			if(stats.getIdentityKey().equals(identityKey)) {
				LectureBlockStatisticsVO statsVo = LectureBlockStatisticsVO.valueOf(stats);
				return Response.ok(statsVo).build();
			}
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	private List<LectureBlockIdentityStatistics> getStatisticsBy(Curriculum curriculum, Identity identity, IdentityRef participantRef) {
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(LecturesSearchFormController.PROPS_IDENTIFIER, false);
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntriesWithLectures(curriculum, null);
		
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		params.setEntries(entries);
		if(participantRef != null) {
			params.setParticipants(List.of(participantRef));
		}
		List<LectureBlockIdentityStatistics> rawStatistics = lectureService
				.getLecturesStatistics(params, userPropertyHandlers, identity);
		return lectureService.groupByIdentity(rawStatistics);
	}
	
	private boolean isManager(Curriculum curriculum, HttpServletRequest httpRequest) {
		Identity identity = getIdentity(httpRequest);
		return curriculumService.hasRoleExpanded(curriculum, identity,
				OrganisationRoles.administrator.name(), OrganisationRoles.curriculummanager.name(),
				CurriculumRoles.curriculummanager.name(), CurriculumRoles.owner.name());
	}
}
