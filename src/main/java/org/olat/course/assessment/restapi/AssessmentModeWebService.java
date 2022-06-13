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
package org.olat.course.assessment.restapi;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.manager.SafeExamBrowserConfigurationSerializer;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.assessment.ui.mode.AssessmentModeHelper;
import org.olat.group.BusinessGroupService;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.restapi.CurriculumElementVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.GroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 4 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Repo")
@Component
@Path("repo/assessmentmodes")
public class AssessmentModeWebService {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeWebService.class);

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	
	/**
	 * Return the reminders of the specified course or repository entry.
	 * 
	 * @return The reminders
	 */
	@GET
	@Operation(summary = "Return the assessment modes", description = "Return the assessment modes from the search results")
	@ApiResponse(responseCode = "200", description = "An array of assessment modes",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response findAssessmentModes(@QueryParam("from") @Parameter(description = "Search from the specified date") String from,
			@QueryParam("to") @Parameter(description = "Search up-to the specified date") String to,
			@QueryParam("externalId") @Parameter(description = "Exact match of the external ID, case sensitive on PostreSQL") String externalId,
			@QueryParam("managed") @Parameter(description = "All modes with a managed flags set") Boolean managed,
			@QueryParam("withExternalId") @Parameter(description = "All modes with an external ID set") Boolean withExternalId,
			@QueryParam("running") @Parameter(description = "All modes in running status (and en status but the date is not checked)") Boolean running,
			@QueryParam("repositoryEntryKey") @Parameter(description = "Limit to the specified course)") Long repositoryEntryKey, 
			@Context HttpServletRequest request) {
		if(!isAdministrator(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		SearchAssessmentModeParams params = new SearchAssessmentModeParams();
		if(StringHelper.containsNonWhitespace(from)) {
			params.setDateFrom(ObjectFactory.parseDate(from));
		}
		if(StringHelper.containsNonWhitespace(to)) {
			params.setDateTo(ObjectFactory.parseDate(to));
		}
		if(StringHelper.containsNonWhitespace(externalId)) {
			params.setExternalId(externalId);
		}
		params.setManaged(managed);
		params.setWithExternalId(withExternalId);
		params.setRunning(running);
		params.setRepositoryEntryKey(repositoryEntryKey);
		
		List<AssessmentMode> assessmentModes = assessmentModeManager.findAssessmentMode(params);

		List<AssessmentModeVO> voList = new ArrayList<>(assessmentModes.size());
		for(AssessmentMode assessmentMode:assessmentModes) {
			voList.add(AssessmentModeVO.valueOf(assessmentMode));
		}
		AssessmentModeVO[] voes = voList.toArray(new AssessmentModeVO[voList.size()]);
		return Response.ok(voes).build();
	}
	
	@GET
	@Path("{assessmentKey}")
	@Operation(summary = "Return the assessment mode", description = "Return the assessment mode with the specified primary key")
	@ApiResponse(responseCode = "200", description = "The assessment mode",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAssessmentMode(@PathParam("assessmentKey") Long assessmentKey, @Context HttpServletRequest request) {
		if(!isAdministrator(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		AssessmentMode assessmentMode = assessmentModeManager.getAssessmentModeById(assessmentKey);
		if(assessmentMode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		AssessmentModeVO vo = AssessmentModeVO.valueOf(assessmentMode);
		return Response.ok(vo).build();
	}
	
	@PUT
	@Path("")
	@Operation(summary = "Create or update an assessment mode", description = "Create or update an assessment mode")
	@ApiResponse(responseCode = "200", description = "The persisted assessment mode",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putNewAssessmentMode(AssessmentModeVO assessmentMode, @Context HttpServletRequest request) {
		return saveAssessmentMode(assessmentMode, request);
	}
	
	@POST
	@Path("")
	@Operation(summary = "Update the assessment mode", description = "Update the assessment mode")
	@ApiResponse(responseCode = "200", description = "The persisted assessment mode",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postNewAssessmentMode(AssessmentModeVO assessmentMode, @Context HttpServletRequest request) {
		return saveAssessmentMode(assessmentMode, request);
	}
	
	@PUT
	@Path("{assessmentKey}")
	@Operation(summary = "Update the assessment mode", description = "Update the assessment mode")
	@ApiResponse(responseCode = "200", description = "The persisted assessment mode",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response putAssessmentMode(AssessmentModeVO assessmentMode, @Context HttpServletRequest request) {
		return saveAssessmentMode(assessmentMode, request);
	}
	
	@POST
	@Path("{assessmentKey}")
	@Operation(summary = "Update the assessment mode", description = "Update the assessment mode")
	@ApiResponse(responseCode = "200", description = "The persisted assessment mode",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response postAssessmentMode(AssessmentModeVO assessmentMode, @Context HttpServletRequest request) {
		return saveAssessmentMode(assessmentMode, request);
	}
	

	public Response saveAssessmentMode(AssessmentModeVO assessmentModeVo, HttpServletRequest request) {
		if(!isAdministrator(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		AssessmentMode assessmentMode;
		if(assessmentModeVo.getKey() == null) {
			RepositoryEntry entry = repositoryService.loadByKey(assessmentModeVo.getRepositoryEntryKey());
			if(entry == null) {
				log.warn("Repository entry key {} for assessment mode not found", assessmentModeVo.getRepositoryEntryKey());
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			assessmentMode = assessmentModeManager.createAssessmentMode(entry);
		} else {
			assessmentMode = assessmentModeManager.getAssessmentModeById(assessmentModeVo.getKey());
		}
		return saveAssessmentMode(assessmentModeVo, assessmentMode,
				assessmentModeManager, curriculumService, businessGroupService);
	}
	
	protected static Response saveAssessmentMode(AssessmentModeVO assessmentModeVo, AssessmentMode assessmentMode,
			AssessmentModeManager assessmentModeMgr, CurriculumService curriculumService, BusinessGroupService businessGroupService) {
		if(assessmentMode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(assessmentModeVo.getName() != null) {
			assessmentMode.setName(assessmentModeVo.getName());
		}
		if(assessmentModeVo.getDescription() != null) {
			assessmentMode.setDescription(assessmentModeVo.getDescription());
		}
		if(assessmentModeVo.getExternalId() != null) {
			assessmentMode.setExternalId(assessmentModeVo.getExternalId());
		}
		if(assessmentModeVo.getManagedFlagsString() != null) {
			assessmentMode.setManagedFlagsString(assessmentModeVo.getManagedFlagsString());
		}
		
		if(assessmentModeVo.getBegin() != null) {
			assessmentMode.setBegin(assessmentModeVo.getBegin());
		}
		if(assessmentModeVo.getLeadTime() >= 0) {
			assessmentMode.setLeadTime(assessmentModeVo.getLeadTime());
		}
		if(assessmentModeVo.getEnd() != null) {
			assessmentMode.setEnd(assessmentModeVo.getEnd());
		}
		if(assessmentModeVo.getFollowupTime() >= 0) {
			assessmentMode.setFollowupTime(assessmentModeVo.getFollowupTime());
		}
		
		if(assessmentModeVo.getManualBeginEnd() != null) {
			assessmentMode.setManualBeginEnd(assessmentModeVo.getManualBeginEnd().booleanValue());
		}
		if(StringHelper.containsNonWhitespace(assessmentModeVo.getTargetAudience())) {
			assessmentMode.setTargetAudience(Target.valueOf(assessmentModeVo.getTargetAudience()));
		}
		
		if(assessmentModeVo.getRestrictAccessIps() != null) {
			assessmentMode.setRestrictAccessIps(assessmentModeVo.getRestrictAccessIps().booleanValue());
		}
		if(assessmentModeVo.getIpList() != null && assessmentMode.isRestrictAccessIps()) {
			assessmentMode.setIpList(assessmentModeVo.getIpList());
		} else if(!assessmentMode.isRestrictAccessIps()) {
			assessmentMode.setIpList(null);
		}
		
		if(assessmentModeVo.getRestrictAccessElements() != null) {
			assessmentMode.setRestrictAccessElements(assessmentModeVo.getRestrictAccessElements().booleanValue());
		}
		if(assessmentMode.isRestrictAccessElements()) {
			if(assessmentModeVo.getElementList() != null) {
				assessmentMode.setElementList(assessmentModeVo.getElementList());
			}
		} else {
			assessmentMode.setElementList(null);
		}
		if(assessmentModeVo.getStartElement() != null) {
			assessmentMode.setStartElement(assessmentModeVo.getStartElement());
		}
		
		if(assessmentModeVo.getSafeExamBrowser() != null) {
			assessmentMode.setSafeExamBrowser(assessmentModeVo.getSafeExamBrowser().booleanValue());
		}
		if(assessmentMode.isSafeExamBrowser()) {
			if(assessmentModeVo.getSafeExamBrowserKey() != null) {
				assessmentMode.setSafeExamBrowserKey(assessmentModeVo.getSafeExamBrowserKey());
			}
			if(assessmentModeVo.getSafeExamBrowserHint() != null) {
				assessmentMode.setSafeExamBrowserHint(assessmentModeVo.getSafeExamBrowserHint());
			}
			if(assessmentModeVo.getSafeExamBrowserConfigDownload() != null) {
				assessmentMode.setSafeExamBrowserConfigDownload(assessmentModeVo.getSafeExamBrowserConfigDownload().booleanValue());
			}
			if(assessmentModeVo.getSafeExamBrowserConfigXml() != null) {
				SafeExamBrowserConfiguration configuration = SafeExamBrowserConfigurationSerializer
						.fromXml(assessmentModeVo.getSafeExamBrowserConfigXml());
				assessmentMode.setSafeExamBrowserConfiguration(configuration);
			}
		} else {
			assessmentMode.setSafeExamBrowserKey(null);
			assessmentMode.setSafeExamBrowserHint(null);
			assessmentMode.setSafeExamBrowserConfigDownload(false);
			assessmentMode.setSafeExamBrowserConfiguration(null);
		}
		
		if(!StringHelper.containsNonWhitespace(assessmentMode.getName())
				|| assessmentMode.getBegin() == null || assessmentMode.getEnd() == null
				|| assessmentMode.getTargetAudience() == null) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		} else {
			if(assessmentMode.getKey() == null) {
				assessmentMode = assessmentModeMgr.persist(assessmentMode);
			}
		}
		
		CurriculumElementVO[] elements = assessmentModeVo.getCurriculumElements();
		if(elements != null) {
			List<Long> curriculumElementKeys = new ArrayList<>();
			for(CurriculumElementVO element:elements) {
				curriculumElementKeys.add(element.getKey());
			}
			AssessmentModeHelper.updateCurriculumElementsRelations(curriculumElementKeys, assessmentMode,
					assessmentMode.getTargetAudience(), assessmentModeMgr, curriculumService);
		}

		GroupVO[] businessGroups = assessmentModeVo.getBusinessGroups();
		if(businessGroups != null) {
			List<Long> businessGroupKeys = new ArrayList<>();
			for(GroupVO businessGroup:businessGroups) {
				businessGroupKeys.add(businessGroup.getKey());
			}
			AssessmentModeHelper.updateBusinessGroupRelations(businessGroupKeys, assessmentMode,
					assessmentMode.getTargetAudience(), assessmentModeMgr, businessGroupService);
		}
		
		assessmentMode = assessmentModeMgr.merge(assessmentMode, false);
		
		AssessmentModeVO vo = AssessmentModeVO.valueOf(assessmentMode);
		return Response.ok(vo).build();
	}
	
	@DELETE
	@Path("{assessmentKey}")
	@Operation(summary = "Delete the assessment mode", description = "Delete the assessment mode")
	@ApiResponse(responseCode = "200", description = "The persisted assessment mode",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessmentModeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response deleteAssessmentMode(@PathParam("assessmentKey") Long assessmentKey, @Context HttpServletRequest request) {
		if(!isAdministrator(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		AssessmentMode modeToDelete = assessmentModeManager.getAssessmentModeById(assessmentKey);
		if(modeToDelete == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		assessmentModeManager.delete(modeToDelete);
		return Response.serverError().status(Status.OK).build();
	}
	
	private boolean isAdministrator(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return (roles.isAdministrator() || roles.isLearnResourceManager());
		} catch (Exception e) {
			return false;
		}
	}
}
