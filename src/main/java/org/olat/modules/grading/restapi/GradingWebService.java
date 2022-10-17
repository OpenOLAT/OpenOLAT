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
package org.olat.modules.grading.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
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

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentRef;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.model.GradingAssignmentRefImpl;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 7 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("grading")
public class GradingWebService {
	
	@Autowired
	private GradingService gradingService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@GET
	@Path("assignments/tests")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@ApiResponse(responseCode = "200",
		description = "List all test entries with at least one assignment.",
		content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RepositoryEntryVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = RepositoryEntryVO.class)))
		})
	@Operation(summary = "List all test entries with at least one assignment",
		description = "List all entries with at least one assignment, this is test repository entries where the user has administration rights on.")
	public Response getTestWithGrading(@Context HttpServletRequest httpRequest) {
		Identity identity = getIdentity(httpRequest);
		List<RepositoryEntry> res = gradingService.getReferenceRepositoryEntriesWithGrading(identity);
		RepositoryEntryVO[] voes = toArrayOfVOes(res);
		return Response.ok(voes).build();
	}
	
	@GET
	@Path("test/{repoEntryKey}/assignments")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@ApiResponse(responseCode = "200",
	description = "List informations about assignments of the specified test repository entry.",
	content = {
		@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GradingAssignmentWithInfosVO.class))),
		@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GradingAssignmentWithInfosVO.class)))
	})
	@ApiResponse(responseCode = "400", description = "The repository entry is not a proper Long")
	@ApiResponse(responseCode = "403", description = "The user cannot access the informations")
	@ApiResponse(responseCode = "404", description = "The repository entry with the specified key cannot be found")
	@Operation(summary = "List informations about assignments of the specified test repository entry.",
	description = "List informations like assignment key or assessment entry of the specified test repository entry.")
	public Response getAssignments(@PathParam("repoEntryKey")String repoEntryKey, @Context HttpServletRequest request) {
		if(!StringHelper.isLong(repoEntryKey)) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(Long.valueOf(repoEntryKey));
		if(entry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!checkAccess(entry, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		GradingAssignmentSearchParameters searchParams = new GradingAssignmentSearchParameters();
		searchParams.setReferenceEntry(entry);
		List<GradingAssignmentWithInfos> assignmentInfos = gradingService.getGradingAssignmentsWithInfos(searchParams, I18nModule.getDefaultLocale());
		GradingAssignmentWithInfosVO[] infosVoes = toArrayOfInfoVOes(assignmentInfos);
		return Response.ok(infosVoes).build();
	}
	
	@GET
	@Path("test/{repoEntryKey}/assignments/{assignmentKey}/uservisibility")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@ApiResponse(responseCode = "200",
	description = "Limited information about the user visibility of the assessment entry of the specified assignment.",
	content = {
		@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = GradingAssignmentUserVisibilityVO.class))),
		@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = GradingAssignmentUserVisibilityVO.class)))
	})
	@ApiResponse(responseCode = "400", description = "The repository entry key or the assignment key are not proper Long")
	@ApiResponse(responseCode = "403", description = "The user cannot access the informations")
	@ApiResponse(responseCode = "404", description = "The assignment cannot be found")
	public Response getUserVisiblity(@PathParam("repoEntryKey") String repoEntryKey,
			@PathParam("assignmentKey") String assignmentKey, @Context HttpServletRequest request) {
		if(!StringHelper.isLong(repoEntryKey) || !StringHelper.containsNonWhitespace(assignmentKey)) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		GradingAssignmentRef ref = new GradingAssignmentRefImpl(Long.valueOf(assignmentKey));
		GradingAssignment assignment = gradingService.getGradingAssignment(ref);
		if(assignment == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!checkAccesAssignmentUserVisibility(assignment, Long.valueOf(repoEntryKey), request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		GradingAssignmentUserVisibilityVO userVisibility = GradingAssignmentUserVisibilityVO.valueOf(assignment);
		return Response.ok(userVisibility).build();
	}
	
	@PUT
	@Path("test/{repoEntryKey}/assignments/{assignmentKey}/uservisibility")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@ApiResponse(responseCode = "200", description = "The visibility has been changed.")
	@ApiResponse(responseCode = "400", description = "The repository entry key or the assignment ke are not proper Long")
	@ApiResponse(responseCode = "403", description = "The user cannot access the informations")
	@ApiResponse(responseCode = "404", description = "The test repository entry, the course, the course element or the assignment  cannot be found")
	public Response putUserVisiblity(@PathParam("repoEntryKey") String repoEntryKey,
			@PathParam("assignmentKey") String assignmentKey, GradingAssignmentUserVisibilityVO visibilityVo,
			@Context HttpServletRequest request) {
		return updateUserVisibility(repoEntryKey, assignmentKey, visibilityVo, request);
	}
	
	@POST
	@Path("test/{repoEntryKey}/assignments/{assignmentKey}/uservisibility")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@ApiResponse(responseCode = "200", description = "The visibility has been changed.")
	@ApiResponse(responseCode = "400", description = "The repository entry key or the assignment ke are not proper Long")
	@ApiResponse(responseCode = "403", description = "The user cannot access the informations")
	@ApiResponse(responseCode = "404", description = "The test repository entry, the course, the course element or the assignment  cannot be found")
	public Response postUserVisiblity(@PathParam("repoEntryKey") String repoEntryKey,
			@PathParam("assignmentKey") String assignmentKey, GradingAssignmentUserVisibilityVO visibilityVo,
			@Context HttpServletRequest request) {
		return updateUserVisibility(repoEntryKey, assignmentKey, visibilityVo, request);
	}
	
	private Response updateUserVisibility(String repoEntryKey, String assignmentKey,
			GradingAssignmentUserVisibilityVO visibilityVo, HttpServletRequest request) {
		if(!StringHelper.isLong(repoEntryKey) || !StringHelper.containsNonWhitespace(assignmentKey)
				|| visibilityVo == null || visibilityVo.getUserVisibility() == null) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		GradingAssignmentRef ref = new GradingAssignmentRefImpl(Long.valueOf(assignmentKey));
		GradingAssignment assignment = gradingService.getGradingAssignment(ref);
		if(!checkAccesAssignmentUserVisibility(assignment, Long.valueOf(repoEntryKey), request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		RepositoryEntry courseEntry = assignment.getAssessmentEntry().getRepositoryEntry();
		if(courseEntry == null
				|| !"CourseModule".equals(courseEntry.getOlatResource().getResourceableTypeName())
				|| assignment.getAssessmentEntry().getSubIdent() == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource());
		CourseNode courseNode = course.getRunStructure().getNode(assignment.getAssessmentEntry().getSubIdent());
		if(courseNode == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		Identity assessedIdentity = assignment.getAssessmentEntry().getIdentity();
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(assessedIdentity, course.getCourseEnvironment());
		AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);

		Boolean userVisible = visibilityVo.getUserVisibility();
		Identity doer = RestSecurityHelper.getIdentity(request);
		
		ScoreEvaluation manualScoreEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
				scoreEval.getAssessmentStatus(), userVisible, scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, manualScoreEval, assessedUserCourseEnv,
				doer, false, Role.coach);
		
		return Response.ok().build();
	}
	
	private boolean checkAccesAssignmentUserVisibility(GradingAssignment assignment, Long repositoryEntryKey,
			HttpServletRequest request) {
		RepositoryEntry referenceEntry = assignment.getReferenceEntry();
		return referenceEntry.getKey().equals(repositoryEntryKey)
				&& checkAccess(assignment.getReferenceEntry(), request);
	}

	private boolean checkAccess(RepositoryEntry entry, HttpServletRequest request) {
		Identity identity = RestSecurityHelper.getIdentity(request);
		Roles roles = RestSecurityHelper.getRoles(request);		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(identity, roles, entry);
		return reSecurity.isEntryAdmin();
	}
	
	
	private GradingAssignmentWithInfosVO[] toArrayOfInfoVOes(List<GradingAssignmentWithInfos> infos) {
		int i=0;
		GradingAssignmentWithInfosVO[] infoVOs = new GradingAssignmentWithInfosVO[infos.size()];
		for (GradingAssignmentWithInfos repoE : infos) {
			infoVOs[i++] = GradingAssignmentWithInfosVO.valueOf(repoE);
		}
		return infoVOs;
	}
	
	private RepositoryEntryVO[] toArrayOfVOes(List<RepositoryEntry> coursRepos) {
		int i=0;
		RepositoryEntryVO[] entryVOs = new RepositoryEntryVO[coursRepos.size()];
		for (RepositoryEntry repoE : coursRepos) {
			entryVOs[i++] = RepositoryEntryVO.valueOf(repoE);
		}
		return entryVOs;
	}
}
