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

import java.util.List;
import java.util.stream.Collectors;

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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 13 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Users")
@Component
@Path("users/{identityKey}/statements")
public class UserEfficiencyStatementWebService {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	@GET
	@Path("") 
	@Operation(summary = "Get the efficiency statements of a user", description = "Get the efficiency statements of a user")
	@ApiResponse(responseCode = "200", description = "The statements", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = UserEfficiencyStatementVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = UserEfficiencyStatementVOes.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The repository entry cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEfficiencyStatement(@PathParam("identityKey") Long identityKey, @Context HttpServletRequest request) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(assessedIdentity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		List<UserEfficiencyStatementImpl> efficiencyStatements = efficiencyStatementManager.getUserEfficiencyStatementFull(assessedIdentity);
		List<UserEfficiencyStatementVO> statementVoList = efficiencyStatements.stream()
				.map(UserEfficiencyStatementVO::new)
				.collect(Collectors.toList());

		UserEfficiencyStatementVOes statementVoes = new UserEfficiencyStatementVOes();
		statementVoes.setStatements(statementVoList);
		return Response.ok(statementVoes).build();
	}
	
	/**
	 * Create a new efficiency statement.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @return Nothing special
	 */
	@PUT
	@Path("")
	@Operation(summary = "Create a new efficiency statement", description = "Create a new efficiency statement, you cannot update an existing one. If you want a standalone statement, let the course key null.")
	@ApiResponse(responseCode = "200", description = "If the statement was persisted ")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the resource cannot be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putEfficiencyStatement(@PathParam("identityKey") Long identityKey,
			UserEfficiencyStatementVO efficiencyStatementVO, @Context HttpServletRequest request) {
		return postEfficiencyStatement(identityKey, efficiencyStatementVO, request);
	}
	
	/**
	 * Create a new efficiency statement.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @return Nothing special
	 */
	@POST
	@Path("")
	@Operation(summary = "Create a new efficiency statement", description = "Create a new efficiency statement, you cannot update an existing one. If you want a standalone statement, let the course key null.")
	@ApiResponse(responseCode = "200", description = "If the statement was persisted ")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the resource cannot be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postEfficiencyStatement(@PathParam("identityKey") Long identityKey,
			UserEfficiencyStatementVO efficiencyStatementVO, @Context HttpServletRequest request) {
	
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(efficiencyStatementVO.getIdentityKey() != null && !assessedIdentity.getKey().equals(efficiencyStatementVO.getIdentityKey())) {
			return Response.serverError().status(Response.Status.CONFLICT).build();
		}
		if(!isAdminOf(assessedIdentity, request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		Long courseRepoKey = efficiencyStatementVO.getCourseRepoKey();
		if(courseRepoKey != null) {
			RepositoryEntry courseEntry = repositoryService.loadByKey(courseRepoKey);
			if(courseEntry == null) {
				createStandalone(assessedIdentity, efficiencyStatementVO);
			} else {
				UserEfficiencyStatement efficiencyStatement = efficiencyStatementManager
						.getUserEfficiencyStatementFull(new RepositoryEntryRefImpl(courseRepoKey), assessedIdentity);
				if(efficiencyStatement != null) {
					return Response.serverError().status(Response.Status.CONFLICT).build();
				}
				efficiencyStatementManager.createUserEfficiencyStatement(efficiencyStatementVO.getCreationDate(),
						efficiencyStatementVO.getScore(), efficiencyStatementVO.getGrade(),
						efficiencyStatementVO.getGradeSystemIdent(), efficiencyStatementVO.getPerfromanceClassIdent(),
						efficiencyStatementVO.getPassed(), assessedIdentity, courseEntry.getOlatResource());
			}
		} else {
			createStandalone(assessedIdentity, efficiencyStatementVO);
		}
		return Response.ok().build();
	}
	
	private void createStandalone(Identity assessedIdentity, UserEfficiencyStatementVO efficiencyStatementVO) {
		efficiencyStatementManager.createStandAloneUserEfficiencyStatement(efficiencyStatementVO.getCreationDate(),
				efficiencyStatementVO.getScore(), efficiencyStatementVO.getGrade(),
				efficiencyStatementVO.getGradeSystemIdent(), efficiencyStatementVO.getPerfromanceClassIdent(),
				efficiencyStatementVO.getPassed(), efficiencyStatementVO.getTotalNodes(),
				efficiencyStatementVO.getAttemptedNodes(), efficiencyStatementVO.getPassedNodes(),
				efficiencyStatementVO.getStatementXml(), assessedIdentity, null,
				efficiencyStatementVO.getCourseTitle());
	}
	
	private boolean isAdminOf(Identity assessedIdentity, HttpServletRequest httpRequest) {
		Roles managerRoles = getRoles(httpRequest);
		if(!managerRoles.isAdministrator()) {
			return false;
		}
		Roles identityRoles = securityManager.getRoles(assessedIdentity);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, identityRoles);
	}

}
