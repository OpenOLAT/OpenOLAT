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
package org.olat.modules.bigbluebutton.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.id.Roles;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
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
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Virtual class rooms")
@Component
@Path("bigbluebutton/templates")
public class BigBlueButtonTemplatesWebService {
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	/**
	 * Return the curriculums a manager user is allowed to see.
	 * 
	 * @param httpRequest  The HTTP request
	 * @return An entity with some statistics about templates
	 */
	@GET
	@Operation(summary = "Return some statistics about used templates",
		description = "Return some statistics about used templates")
	@ApiResponse(responseCode = "200", description = "An array of BigBlueButton servers",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BigBlueButtonServerVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = BigBlueButtonServerVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Path("statistics")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatistics(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		int rooms = 0;
		int maxParticipants = 0;
		int roomsWithRecord = 0;
		int maxParticipantsWithRecord = 0;
		int roomsWithAutoStartRecording = 0;
		int maxParticipantsWithAutoStartRecording = 0;
		int roomsWithBreakout = 0;
		int maxParticipantsWithBreakout = 0;
		int roomsWithWebcamsOnlyForModerator = 0;
		int maxParticipantsWithWebcamsOnlyForModerator = 0;
		
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		for(BigBlueButtonMeetingTemplate template:templates) {
			if(!template.isEnabled()) {
				continue;
			}
	
			int mMeetings = template.getMaxConcurrentMeetings() == null ? 65535 : template.getMaxConcurrentMeetings().intValue();
			int mParticipants = template.getMaxParticipants() == null ? 65535 : template.getMaxParticipants().intValue();
			
			rooms += mMeetings;
			maxParticipants += mParticipants;
			// record
			if(template.getRecord() != null && template.getRecord().booleanValue()) {
				roomsWithRecord += mMeetings;
				maxParticipantsWithRecord += (mMeetings * mParticipants);
			}
			// auto start recording
			if(template.getAutoStartRecording() != null && template.getAutoStartRecording().booleanValue()) {
				roomsWithAutoStartRecording += mMeetings;
				maxParticipantsWithAutoStartRecording += (mMeetings * mParticipants);
			}
			// breakout rooms
			if(template.getBreakoutRoomsEnabled() != null && template.getBreakoutRoomsEnabled().booleanValue()) {
				roomsWithBreakout += mMeetings;
				maxParticipantsWithBreakout += (mMeetings * mParticipants);
			}
			// webcams only for moderator
			if(template.getWebcamsOnlyForModerator() != null && template.getWebcamsOnlyForModerator().booleanValue()) {
				roomsWithWebcamsOnlyForModerator += mMeetings;
				maxParticipantsWithWebcamsOnlyForModerator += (mMeetings * mParticipants);
			}	
		}

		BigBlueButtonTemplatesStatisticsVO vo = new BigBlueButtonTemplatesStatisticsVO();
		vo.setRooms(rooms);
		vo.setMaxParticipants(maxParticipants);
		vo.setRoomsWithRecord(roomsWithRecord);
		vo.setMaxParticipantsWithRecord(maxParticipantsWithRecord);
		vo.setRoomsWithAutoStartRecording(roomsWithAutoStartRecording);
		vo.setMaxParticipantsWithAutoStartRecording(maxParticipantsWithAutoStartRecording);
		vo.setRoomsWithBreakout(roomsWithBreakout);
		vo.setMaxParticipantsWithBreakout(maxParticipantsWithBreakout);
		vo.setRoomsWithWebcamsOnlyForModerator(roomsWithWebcamsOnlyForModerator);
		vo.setMaxParticipantsWithWebcamsOnlyForModerator(maxParticipantsWithWebcamsOnlyForModerator);
		return Response.ok(vo).build();
	}
}
