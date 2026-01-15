/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;

import java.util.List;

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

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsModule;
import org.olat.modules.teams.TeamsService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 14 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryTeamsMeetingWebService {
	
	private final RepositoryEntry entry;

	@Autowired
	private TeamsModule teamsModule;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private RepositoryService repositoryService;

	public  RepositoryEntryTeamsMeetingWebService(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	@GET
	@Path("tool")
	@Operation(summary = "Return the Teams meetings of the course's tool",
			description = "Return the Teams meetings of the course's tool")
	@ApiResponse(responseCode = "200", description = "The Teams meeting", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TeamsMeetingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = TeamsMeetingVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "409", description = "Teams is disabled")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTeamsMeetings(@Context HttpServletRequest request) {
		if(!teamsModule.isEnabled() || !teamsModule.isCoursesEnabled()) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		if(!isAuthorEditor(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<TeamsMeeting> meetings = teamsService.getMeetings(entry, null, null);
		TeamsMeetingVO[] meetingsVOes = new TeamsMeetingVO[meetings.size()];
		for(int i=meetings.size(); i-->0; ) {
			meetingsVOes[i] = TeamsMeetingVO.valueOf(meetings.get(i));
		}
		return Response.ok(meetingsVOes).build();
	}
	
	@GET
	@Path("tool/{meetingKey}")
	@Operation(summary = "Return the Teams meetings of the course's tool",
			description = "Return the Teams meetings of the course's tool")
	@ApiResponse(responseCode = "200", description = "The Teams meeting", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TeamsMeetingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = TeamsMeetingVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@ApiResponse(responseCode = "409", description = "The data doesn't match, teams is disabled, edit meeting in the wrong course...")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTeamsMeeting(@PathParam("meetingKey") Long meetingKey, @Context HttpServletRequest request) {
		if(!teamsModule.isEnabled() || !teamsModule.isCoursesEnabled()) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		if(!isAuthorEditor(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		TeamsMeeting meeting = teamsService.getMeetingByKey(meetingKey);
		if(meeting == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!entry.equals(meeting.getEntry()) || StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		return Response.ok(TeamsMeetingVO.valueOf(meeting)).build();
	}
	
	@PUT
	@Path("tool")
	@Operation(summary = "Create or update a Teams meeting", description = "Create or update a Teams meeting")
	@ApiResponse(responseCode = "200", description = "The updated online meeting",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = TeamsMeetingVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = TeamsMeetingVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@ApiResponse(responseCode = "409", description = "The data doesn't match, teams is disabled, edit meeting in the wrong course...")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putTeamsMeeting(TeamsMeetingVO meeting, @Context HttpServletRequest httpRequest) {
		return saveTeamsMeeting(meeting, httpRequest);
	}
	
	/**
	 * Create or update a Teams meeting.
	 * 
	 * @param block The meeting
	 * @return The updated / created meeting.
	 */
	@POST
	@Path("tool")
	@Operation(summary = "Create or update a Teams meeting", description = "Create or update a Teams meeting")
	@ApiResponse(responseCode = "200", description = "The updated online meeting",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = TeamsMeetingVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = TeamsMeetingVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@ApiResponse(responseCode = "409", description = "The data doesn't match, teams is disabled, edit meeting in the wrong course...")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postTeamsMeeting(TeamsMeetingVO meeting, @Context HttpServletRequest httpRequest) {
		return saveTeamsMeeting(meeting, httpRequest);
	}
	
	private Response saveTeamsMeeting(TeamsMeetingVO meetingVo, HttpServletRequest request) {
		if(!teamsModule.isEnabled() || !teamsModule.isCoursesEnabled()) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		if(!isAuthorEditor(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		TeamsMeeting meeting;
		if(meetingVo.getKey() != null) {
			meeting = teamsService.getMeetingByKey(meetingVo.getKey());
			if(entry.equals(meeting.getEntry())) {
				return Response.serverError().status(Status.CONFLICT).build();
			}
		} else {
			Identity doer = getIdentity(request);
			meeting = teamsService.createMeeting(meetingVo.getSubject(), meetingVo.getStartDate(), meetingVo.getEndDate(), entry, null, null, doer);
		}
		TeamsMeetingVO.transfer(meetingVo, meeting);
		meeting = teamsService.updateMeeting(meeting, false);
		TeamsMeetingVO persistedMeeting = TeamsMeetingVO.valueOf(meeting);
		return Response.ok(persistedMeeting).build();
	}
	
	@DELETE
	@Path("tool/{meetingKey}")
	@Operation(summary = "Delete a Teams meeting",
			description = "Delete a  Teams meetings of the course's tool")
	@ApiResponse(responseCode = "200", description = "The Teams meeting", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TeamsMeetingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = TeamsMeetingVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@ApiResponse(responseCode = "409", description = "The data doesn't match, teams is disabled, edit meeting in the wrong course...")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteTeamsMeetings(@PathParam("meetingKey") Long meetingKey, @Context HttpServletRequest request) {
		if(!teamsModule.isEnabled() || !teamsModule.isCoursesEnabled()) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		if(!isAuthorEditor(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		TeamsMeeting meeting = teamsService.getMeetingByKey(meetingKey);
		if(meeting == null) {
			return Response.serverError().status(Status.NOT_MODIFIED).build();
		}
		if(!entry.equals(meeting.getEntry()) || StringHelper.containsNonWhitespace(meeting.getSubIdent())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		teamsService.deleteMeeting(meeting);
		return Response.ok().build();
	}
	
	private boolean isAuthorEditor(HttpServletRequest request) {
		try {
			Identity identity = getUserRequest(request).getIdentity();
			return repositoryService.hasRoleExpanded(identity, entry, OrganisationRoles.administrator.name(),
					OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name());
		} catch (Exception e) {
			return false;
		}
	}
}
