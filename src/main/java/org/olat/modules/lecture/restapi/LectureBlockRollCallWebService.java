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
package org.olat.modules.lecture.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 28 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallWebService {
	
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	/**
	 * Return a list lecture block roll call.
	 * 
	 * @param closed If true, the status of the block is done or the status of the roll call is closed or auto closed
	 * @param hasAbsence If true, the roll call has an absence
	 * @param hasSupervisorNotificationDate If true, the roll call has a supervisor notification date set
	 * @param httpRequest  The HTTP request
	 * @return The roll calls
	 */
	@GET
	@Path("/")
	@Operation(summary = "Return a list lecture block roll call", description = "Return a list lecture block roll call" + 
			"the user is a participant (owner, tutor or participant)")
	@ApiResponse(responseCode = "200", description = "A lecture block roll call",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LectureBlockRollCallVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LectureBlockRollCallVO.class)))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getRollCalls(@QueryParam("closed") @Parameter(description = "If true, the status of the block is done or the status of the roll call is closed or auto closed") Boolean closed, @QueryParam("hasAbsence") @Parameter(description = "If true, the roll call has an absence") Boolean hasAbsence, 
			@QueryParam("hasSupervisorNotificationDate") @Parameter(description = "If true, the roll call has a supervisor notification date set") Boolean hasSupervisorNotificationDate,
			@QueryParam("lectureBlockKey") @Parameter(description = "The lectureBlock Key") Long lectureBlockKey,
			@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isLectureManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		if(hasAbsence != null) {
			searchParams.setHasAbsence(hasAbsence);
		}
		if(hasSupervisorNotificationDate != null) {
			searchParams.setHasSupervisorNotificationDate(hasSupervisorNotificationDate);
		}
		if(closed != null) {
			searchParams.setClosed(closed);
		}
		if(lectureBlockKey != null) {
			searchParams.setLectureBlockKey(lectureBlockKey);
		}
		
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(searchParams);
		List<LectureBlockRollCallVO> voList = new ArrayList<>(rollCalls.size());
		for(LectureBlockRollCall rollCall:rollCalls) {
			voList.add(new LectureBlockRollCallVO(rollCall));
		}
		LectureBlockRollCallVO[] voes = voList.toArray(new LectureBlockRollCallVO[voList.size()]);
		return Response.ok(voes).build();
	}
	
	/**
	 * Return the lecture block roll call specified by the primary key.
	 * 
	 * @param rollCallKey The primary key of the roll call
	 * @param httpRequest  The HTTP request
	 * @return The roll call
	 */
	@GET
	@Path("{rollCallKey}")
	@Operation(summary = "Return the lecture block roll call specified by the primary key", description = "Return the lecture block roll call specified by the primary key" + 
			"the user is a participant (owner, tutor or participant)")
	@ApiResponse(responseCode = "200", description = "A lecture block roll call",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockRollCallVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockRollCallVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRollCall(@PathParam("rollCallKey") Long rollCallKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isLectureManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setRollCallKey(rollCallKey);
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(searchParams);
		if(rollCalls.size() == 1) {
			LectureBlockRollCall rollCall = rollCalls.get(0);
			if(!isManagerOf(rollCall, httpRequest)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}

			LectureBlockRollCallVO vo = new LectureBlockRollCallVO(rollCall);
			return Response.ok(vo).build();
		}
		return Response.serverError().status(Status.NOT_FOUND).build();	
	}
	
	/**
	 * Update a roll call. The absence are not updated by this method! Only the
	 * supervisor notification date, the comment and the reason. The method doesn't
	 * create a new roll call.
	 * 
	 * @param rollCallVo The roll call to update
	 * @param httpRequest  The HTTP request
	 * @return The updated roll call
	 */
	@PUT
	@Path("/")
	@Operation(summary = "Update a roll call", description = "Return the lecture block roll call specified by the primary key" + 
			"Update a roll call. The absence are not updated by this method! Only the\n" + 
			" supervisor notification date, the comment and the reason. The method doesn't\n" + 
			" create a new roll call")
	@ApiResponse(responseCode = "200", description = "A lecture block roll call",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockRollCallVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockRollCallVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The role call was not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateRollCallPut(LectureBlockRollCallVO rollCallVo, @Context HttpServletRequest httpRequest) {
		return updateLectureBlockRollCall(rollCallVo, httpRequest);
	}

	/**
	 * Update a roll call. The absence are not updated by this method! Only the
	 * supervisor notification date, the comment and the reason. The method doesn't
	 * create a new roll call.
	 * 
	 * @param rollCallVo The roll call to update
	 * @param httpRequest  The HTTP request
	 * @return The updated roll call
	 */
	@POST
	@Path("/")
	@Operation(summary = "Update a roll call", description = "Return the lecture block roll call specified by the primary key" + 
			"Update a roll call. The absence are not updated by this method! Only the\n" + 
			" supervisor notification date, the comment and the reason. The method doesn't\n" + 
			" create a new roll call")
	@ApiResponse(responseCode = "200", description = "A lecture block roll call",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockRollCallVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockRollCallVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The role call was not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateRollCall(LectureBlockRollCallVO rollCallVo, @Context HttpServletRequest httpRequest) {
		return updateLectureBlockRollCall(rollCallVo, httpRequest);
	}

	private Response updateLectureBlockRollCall(LectureBlockRollCallVO rollCallVo, HttpServletRequest httpRequest) {
		if(rollCallVo.getKey() == null) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}
		
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setRollCallKey(rollCallVo.getKey());
		List<LectureBlockRollCall> rollCalls = lectureService.getRollCalls(searchParams);
		if(rollCalls.size() == 1) {
			LectureBlockRollCall rollCall = rollCalls.get(0);
			if(!isManagerOf(rollCall, httpRequest)) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}

			rollCall.setAbsenceSupervisorNotificationDate(rollCallVo.getAbsenceSupervisorNotificationDate());
			if(rollCallVo.getAbsenceReason() != null) {
				rollCall.setAbsenceReason(rollCallVo.getAbsenceReason());
			}
			if(rollCallVo.getComment() != null) {
				rollCall.setComment(rollCallVo.getComment());
			}
			rollCall = lectureService.updateRollCall(rollCall);
			LectureBlockRollCallVO vo = new LectureBlockRollCallVO(rollCall);
			return Response.ok(vo).build();
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	private boolean isManagerOf(LectureBlockRollCall rollCall, HttpServletRequest httpRequest) {
		RepositoryEntry entry = rollCall.getLectureBlock().getEntry();
		Identity identity = RestSecurityHelper.getIdentity(httpRequest);
		return repositoryService.hasRoleExpanded(identity, entry,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
				OrganisationRoles.lecturemanager.name(), GroupRoles.owner.name());
	}

}
