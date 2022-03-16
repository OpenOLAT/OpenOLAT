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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.EfficiencyStatementVO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 17.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Repo")
@Component
@Path("repo/courses/{resourceKey}/statements")
public class EfficiencyStatementWebService {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	@GET
	@Path("{identityKey}") 
	@Operation(summary = "Get statement", description = "Get statemenet")
	@ApiResponse(responseCode = "200", description = "The statement", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = EfficiencyStatementVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = EfficiencyStatementVO.class)) })
	@ApiResponse(responseCode = "404", description = "The repository entry cannot be found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getEfficiencyStatement(@PathParam("identityKey") Long identityKey, @PathParam("resourceKey") Long resourceKey,
			@Context HttpServletRequest request) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(assessedIdentity, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		UserEfficiencyStatement efficiencyStatement = efficiencyStatementManager.getUserEfficiencyStatementLightByResource(resourceKey, assessedIdentity);
		if(efficiencyStatement == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		
		EfficiencyStatementVO statementVO = new EfficiencyStatementVO(efficiencyStatement);
		return Response.ok(statementVO).build();
	}
	
	/**
	 * Create a new efficiency statement.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @return Nothing special
	 */
	@PUT
	@Operation(summary = "Create a new efficiency statement", description = "Create a new efficiency statement")
	@ApiResponse(responseCode = "200", description = "If the statement was persisted ")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the resource cannot be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putEfficiencyStatement(@PathParam("resourceKey") Long resourceKey,
						EfficiencyStatementVO efficiencyStatementVO, @Context HttpServletRequest request) {
		return putEfficiencyStatement(efficiencyStatementVO.getIdentityKey(), resourceKey, efficiencyStatementVO, request);
	}

	/**
	 * Create a new efficiency statement.
	 * 
	 * @param identityKey The owner of the certificate
	 * @param resourceKey The primary key of the resource of the repository entry of the course.
	 * @return Nothing special
	 */
	@PUT
	@Path("{identityKey}")
	@Operation(summary = "Create a new efficiency statement", description = "Create a new efficiency statement")
	@ApiResponse(responseCode = "200", description = "If the statement was persisted ")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity or the resource cannot be found")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putEfficiencyStatement(@PathParam("identityKey") Long identityKey, @PathParam("resourceKey") Long resourceKey,
						EfficiencyStatementVO efficiencyStatementVO, @Context HttpServletRequest request) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(identityKey);
		if(assessedIdentity == null) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		if(!isAdminOf(assessedIdentity, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		EfficiencyStatement efficiencyStatement = efficiencyStatementManager.getUserEfficiencyStatementByResourceKey(resourceKey, assessedIdentity);
		if(efficiencyStatement != null) {
			return Response.serverError().status(Response.Status.CONFLICT).build();
		}
		
		Date creationDate = efficiencyStatementVO.getCreationDate();
		Float score = efficiencyStatementVO.getScore();
		String grade = efficiencyStatementVO.getGrade();
		String performanceClassIdent = efficiencyStatementVO.getPerformanceClassIdent();
		Boolean passed = efficiencyStatementVO.getPassed();

		OLATResource resource = resourceManager.findResourceById(resourceKey);
		if(resource == null) {
			String courseTitle = efficiencyStatementVO.getCourseTitle();
			efficiencyStatementManager.createStandAloneUserEfficiencyStatement(creationDate, score, grade,
					performanceClassIdent, passed, null, null, null, null, assessedIdentity, resourceKey, courseTitle);
		} else {
			efficiencyStatementManager.createUserEfficiencyStatement(creationDate, score, grade, performanceClassIdent, passed, assessedIdentity, resource);
		}
		return Response.ok().build();
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
