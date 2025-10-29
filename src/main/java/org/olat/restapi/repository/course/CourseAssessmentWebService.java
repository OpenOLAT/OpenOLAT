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
package org.olat.restapi.repository.course;

import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Objects;

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
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.AssessableResultsVO;
import org.olat.util.logging.activity.LoggingResourceable;
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
 * Description:<br>
 * Retrieve and import course assessments
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{courseId}/assessments")
public class CourseAssessmentWebService {
	
	private static final String VERSION  = "1.0";

	@Autowired
	private GradeService gradeService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	/**
	 * Retrieves the version of the Course Assessment Web Service.
	 * 
	 * @return
	 */
	@GET
	@Path("version")
	@Operation(summary = "Retireves the version of the Course Assessment Web Service", description = "Retireves the version of the Course Assessment Web Service")
	@ApiResponse(responseCode = "200", description = "The version of this specific Web Service")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVersion() {
		return Response.ok(VERSION).build();
	}
	
	/**
	 * Returns the results of the course (the root node).
	 * 
	 * @param courseId The course resourceable's id
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Operation(summary = "Returns the results of the course (the root node)", description = "Returns the results of the course (the root node)")
	@ApiResponse(responseCode = "200", description = "Array of results all participants of the course", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AssessableResultsVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AssessableResultsVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseRootResults(@PathParam("courseId") Long courseId, @Context HttpServletRequest httpRequest, @Context Request request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		List<Identity> courseUsers = loadAllParticipants(course);
		int i=0;
		
		Date lastModified = null;
		AssessableResultsVO[] results = new AssessableResultsVO[courseUsers.size()];
		for(Identity courseUser:courseUsers) {
			AssessableResultsVO result = getRootResult(courseUser, course);
			if(lastModified == null || (result.getLastModifiedDate() != null && lastModified.before(result.getLastModifiedDate()))) {
				lastModified = result.getLastModifiedDate();
			}
			results[i++] = result;
		}
		
		return Response.ok(results).build();
	}
	
	/**
	 * Returns the results of the course.
	 * 
	 * @param courseId The course resourceable's id
	 * @param identityKey The id of the user
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Path("users/{identityKey}")
	@Operation(summary = "Returns the results of the course by participant id", description = "Returns the results of the course by participant id")
	@ApiResponse(responseCode = "200", description = "The result of the course for the specified participant", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = AssessableResultsVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = AssessableResultsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseRootResultsOf(@PathParam("courseId") Long courseId, @PathParam("identityKey") Long identityKey, @Context HttpServletRequest httpRequest, @Context Request request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		Identity userIdentity = securityManager.loadIdentityByKey(identityKey, false);
		if(userIdentity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
			
		AssessableResultsVO results = getRootResult(userIdentity, course);
		return Response.ok(results).build();
	}
	
	/**
	 * Exports results for an assessable course node for all students.
	 * 
	 * @param courseId The course resourceable's id
	 * @param nodeId The id of the course building block
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Path("{nodeId}")
	@Operation(summary = "Export results", description = "Exports results for an assessable course node for all students")
	@ApiResponse(responseCode = "200", description = "Export all results of all user of the course", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = AssessableResultsVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = AssessableResultsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getCourseNodeResults(@PathParam("courseId") Long courseId, @PathParam("nodeId") Long nodeId,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if (!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<Identity> courseUsers = loadAllParticipants(course);
		int i=0;
		Date lastModified = null;
		AssessableResultsVO[] results = new AssessableResultsVO[courseUsers.size()];
		for(Identity courseUser:courseUsers) {
			AssessableResultsVO result = getNodeResult(courseUser, course, nodeId);
			if(lastModified == null || (result.getLastModifiedDate() != null && lastModified.before(result.getLastModifiedDate()))) {
				lastModified = result.getLastModifiedDate();
			}
			results[i++] = result;
		}
		
		return Response.ok(results).build();
	}
	
	/**
	 * Imports results for an assessable course node for the authenticated student.
	 * 
	 * @param courseId The resourceable id of the course
	 * @param nodeId The id of the course building block
	 * @param resultsVO The results
	 * @param request The HTTP request
	 * @return
	 */
	@POST
	@Path("{nodeId}")
	@Operation(summary = "Import results use with caution", description = "Imports results for an assessable course node for the specified student. The end point respect the setting of the element for the grades if configured.")
	@ApiResponse(responseCode = "200", description = "A result to import", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = AssessableResultsVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = AssessableResultsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response postAssessableResults(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			AssessableResultsVO resultsVO, @Context HttpServletRequest request) {
		return attachAssessableResults(courseId, nodeId, resultsVO, request);
	}
	
	@PUT
	@Path("{nodeId}")
	@Operation(summary = "Import results, use with caution", description = "Imports results for an assessable course node for the specified student. The end point respect the setting of the element for the grades if configured.")
	@ApiResponse(responseCode = "200", description = "A result to import", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = AssessableResultsVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = AssessableResultsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response putAssessableResults(@PathParam("courseId") Long courseId, @PathParam("nodeId") String nodeId,
			AssessableResultsVO resultsVO, @Context HttpServletRequest request) {
		return attachAssessableResults(courseId, nodeId, resultsVO, request);
	}
	
	private Response attachAssessableResults(Long courseId, String nodeId, AssessableResultsVO resultsVO, HttpServletRequest request) {
		Identity identity = getUserRequest(request).getIdentity();
		if(identity == null) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		attachAssessableResults(course, nodeId, identity, resultsVO);
		return Response.ok().build();
	}
	
	private void attachAssessableResults(ICourse course, String nodeKey, Identity requestIdentity, AssessableResultsVO resultsVO) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CourseNode courseNode = getCourseNode(course, nodeKey);
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(course));
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(courseNode));
		
		Identity userIdentity = securityManager.loadIdentityByKey(resultsVO.getIdentityKey());
		// Initialize the user course environment of the assessed identity
		UserCourseEnvironment userCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(userIdentity, course.getCourseEnvironment());
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);

		AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		scoreEval = scoreEval == null ? AssessmentEvaluation.EMPTY_EVAL : scoreEval;

		BigDecimal scoreScale = ScoreScalingHelper.getScoreScale(courseNode);
		
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		
		Float score = null;
		Float weightedScore = null;
		if(hasScore) {
			score = resultsVO.getScore();
			
			Float min = assessmentConfig.getMinScore();
			Float max = assessmentConfig.getMaxScore();
			if(score != null && min != null && score.floatValue() < min.floatValue()) {
				score = min;
			}
			if(score != null && max != null && score.floatValue() > max.floatValue()) {
				score = max;
			}
			weightedScore = ScoreScalingHelper.getWeightedFloatScore(score, scoreScale);	
		}
		
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		Boolean passed = null;
		if(hasPassed) {
			passed = resultsVO.getPassed() == null
					? scoreEval.getPassed()
					: resultsVO.getPassed();
		}

		Boolean userVisible = resultsVO.getUserVisible() == null
				? scoreEval.getUserVisible()
				: resultsVO.getUserVisible();
		AssessmentEntryStatus status = AssessmentEntryStatus.isValueOf(resultsVO.getAssessmentStatus())
				? AssessmentEntryStatus.valueOf(resultsVO.getAssessmentStatus())
				: scoreEval.getAssessmentStatus();
		
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		if(hasScore && score != null) {
			if(assessmentConfig.hasGrade()) {
				if(assessmentConfig.isAutoGrade()) {
					GradeScale gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
					NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, Locale.ENGLISH);
					GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
					grade = gradeScoreRange.getGrade();
					gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
					performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
					passed = gradeScoreRange.getPassed();
				} else {
					grade = StringHelper.containsNonWhitespace(resultsVO.getGrade())
							? resultsVO.getGrade()
							: scoreEval.getGrade();
				}
			} else if(assessmentConfig.getCutValue() != null) {
				Float cutConfig = assessmentConfig.getCutValue();
				if (cutConfig != null) {
					boolean aboveCutValue = score.floatValue() >= cutConfig.floatValue();
					passed = Boolean.valueOf(aboveCutValue);
				}
			}
		}
		
		ScoreEvaluation restScoreEval = new ScoreEvaluation(score, weightedScore, scoreScale,
				grade, gradeSystemIdent, performanceClassIdent,
				passed, status, userVisible, scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), null);
		courseAssessmentService.updateScoreEvaluation(courseNode, restScoreEval, userCourseEnv, requestIdentity, false, Role.coach);
		
		if(resultsVO.getAttempts() != null && !Objects.equals(resultsVO.getAttempts(), scoreEval.getAttempts())) {
			Integer attempts = resultsVO.getAttempts();
			courseAssessmentService.updateAttempts(courseNode, attempts, null, userCourseEnv, requestIdentity, Role.coach);
		}
		
		if(resultsVO.getCompletion() != null && !Objects.equals(resultsVO.getCompletion(), scoreEval.getCompletion())) {
			courseAssessmentService.updateCompletion(courseNode, userCourseEnv, resultsVO.getCompletion(), status, Role.coach);
		}
		
		if(resultsVO.getFullyAssessed() != null && !Objects.equals(resultsVO.getFullyAssessed(), scoreEval.getFullyAssessed())
				&& LearningPathNodeAccessProvider.TYPE.equals(userCourseEnv.getCourseEnvironment().getCourseConfig().getNodeAccessType().getType())) {
			courseAssessmentService.updateFullyAssessed(courseNode, userCourseEnv, resultsVO.getFullyAssessed(), status);
		}
	}


	private CourseNode getCourseNode(ICourse course, String parentNodeId) {
		if (parentNodeId == null) {
			return course.getRunStructure().getRootNode();
		} 
		return course.getEditorTreeModel().getCourseNode(parentNodeId);
	}

	/**
	 * Returns the results of a student at a specific assessable node
	 * 
	 * @param courseId The course resourceable's id
	 * @param nodeId The ident of the course building block
	 * @param identityKey The id of the user
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return
	 */
	@GET
	@Path("{nodeId}/users/{identityKey}")
	@Operation(summary = "Return results", description = "Returns the results of a student at a specific assessable node")
	@ApiResponse(responseCode = "200", description = "The result of a user at a specific node", content = {
		@Content(mediaType = "application/json", schema = @Schema(implementation = AssessableResultsVO.class)),
		@Content(mediaType = "application/xml", schema = @Schema(implementation = AssessableResultsVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCourseNodeResultsOf(@PathParam("courseId") Long courseId, @PathParam("nodeId") Long nodeId, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest, @Context Request request) {
		ICourse course = CoursesWebService.loadCourse(courseId);
		if(course == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!isAuthorEditor(course, httpRequest)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		Identity userIdentity = securityManager.loadIdentityByKey(identityKey, false);
		if(userIdentity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		AssessableResultsVO results = getNodeResult(userIdentity, course, nodeId);
		return Response.ok(results).build();
	}
	
	private AssessableResultsVO getRootResult(Identity identity, ICourse course) {
		CourseNode rootNode = course.getRunStructure().getRootNode();
		return getResults(identity, course, rootNode);
	}
	
	private AssessableResultsVO getNodeResult(Identity identity, ICourse course, Long nodeId) {
		CourseNode courseNode = course.getRunStructure().getNode(nodeId.toString());
		return getResults(identity, course, courseNode);
	}
	
	private AssessableResultsVO getResults(Identity identity, ICourse course, CourseNode courseNode) {
		AssessableResultsVO results = new AssessableResultsVO();
		results.setIdentityKey(identity.getKey());
		results.setIdentityExternalId(identity.getExternalId());
		results.setNodeIdent(courseNode.getIdent());
		
		// create an identenv with no roles, no attributes, no locale
		UserCourseEnvironment userCourseEnvironment = AssessmentHelper.createInitAndUpdateUserCourseEnvironment(identity, course);
		// Fetch all score and passed and calculate score accounting for the entire course
		ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
	
		AssessmentEvaluation scoreEval = scoreAccounting.evalCourseNode(courseNode);
		results.setScore(scoreEval.getScore());
		results.setMaxScore(scoreEval.getMaxScore());
		results.setWeightedScore(scoreEval.getWeightedScore());
		results.setWeightedMaxScore(scoreEval.getWeightedMaxScore());
		results.setPassed(scoreEval.getPassed());
		results.setGrade(scoreEval.getGrade());
		results.setPerformanceClassIdent(scoreEval.getPerformanceClassIdent());
		results.setCompletion(scoreEval.getCompletion());
		results.setAttempts(scoreEval.getAttempts());
		results.setUserVisible(scoreEval.getUserVisible());

		if(scoreEval.getAssessmentStatus() != null) {
			results.setAssessmentStatus(scoreEval.getAssessmentStatus().name());
		}
		
		results.setLastModifiedDate(getLastModificationDate(identity, course, courseNode));
		results.setLastUserModified(scoreEval.getLastUserModified());
		results.setLastCoachModified(scoreEval.getLastCoachModified());
		results.setFirstVisit(scoreEval.getFirstVisit());
		results.setLastVisit(scoreEval.getLastVisit());
		results.setAssessmentDone(scoreEval.getAssessmentDone());
		
		results.setFullyAssessed(scoreEval.getFullyAssessed());
		results.setFullyAssessedDate(scoreEval.getFullyAssessedDate());
		
		return results;
	}
	
	private Date getLastModificationDate(Identity assessedIdentity, ICourse course, CourseNode courseNode) {
		AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
		return am.getScoreLastModifiedDate(courseNode, assessedIdentity);
	}

	private List<Identity> loadAllParticipants(ICourse course) {
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Identity> participants = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
		return new ArrayList<>(new HashSet<>(participants));
	}
	
	private boolean isAuthorEditor(ICourse course, HttpServletRequest request) {
		try {
			Identity identity = getUserRequest(request).getIdentity();
			CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
			return repositoryService.hasRoleExpanded(identity, cgm.getCourseEntry(),
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
					GroupRoles.owner.name()) || cgm.hasRight(identity, CourseRights.RIGHT_ASSESSMENT, null);
		} catch (Exception e) {
			return false;
		}
	}
}