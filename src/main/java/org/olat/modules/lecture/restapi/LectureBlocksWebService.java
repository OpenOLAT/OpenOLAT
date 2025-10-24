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

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.manager.LectureServiceImpl;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlocksWebService {
	
	private final RepositoryEntry entry;
	private final boolean administrator;
	
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	public LectureBlocksWebService(RepositoryEntry entry, boolean administrator) {
		this.entry = entry;
		this.administrator = administrator;
	}
	
	/**
	 * Return the lecture blocks of the specified course or repository entry.
	 * 
	 * @param httpRequest The HTTP request
	 * @return The lecture blocks
	 */
	@GET
	@Operation(summary = "Return the lecture blocks", description = "Return the lecture blocks of the specified course or repository entry")
	@ApiResponse(responseCode = "200", description = "An array of lecture blocks",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LectureBlockVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = LectureBlockVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getLectureBlocks(@Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		List<LectureBlock> blockList = lectureService.getLectureBlocks(entry);
		LectureBlockVO[] voes = new LectureBlockVO[blockList.size()];
		for(int i=blockList.size(); i-->0; ) {
			voes[i] = LectureBlockVO.valueOf(blockList.get(i), entry.getKey());
		}
		return Response.ok(voes).build();
	}

	/**
	 * Create or update a lecture block.
	 * 
	 * @param block The lecture block
	 * @return It returns the updated / created lecture block.
	 */
	@PUT
	@Operation(summary = "Create or update a lecture block", description = "Create or update a lecture block")
	@ApiResponse(responseCode = "200", description = "The updated lecture block",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putLectureBlocks(LectureBlockVO block, @Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity doer = getIdentity(httpRequest);
		LectureBlock updatedBlock = saveLectureBlock(block, doer);
		return Response.ok(LectureBlockVO.valueOf(updatedBlock, entry.getKey())).build();
	}
	
	/**
	 * Create or update a lecture block. The status of the blocks will be set to
	 * autoclose only for newly created blocks. By update, the states of the
	 * block and the roll call will not be updated.
	 * 
	 * @param block The lecture block
	 * @return It returns the updated / created lecture block.
	 */
	@POST
	@Operation(summary = "Create or update a lecture block", description = """
			Create or update a lecture block. The status of the blocks will be set to
			autoclose only for newly created blocks. By update, the states of the
			block and the roll call will not be updated""")
	@ApiResponse(responseCode = "200", description = "The updated configuration",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postLectureBlocks(LectureBlockVO block, @Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		Identity doer = getIdentity(httpRequest);
		LectureBlock updatedBlock = saveLectureBlock(block, doer);
		return Response.ok(LectureBlockVO.valueOf(updatedBlock, entry.getKey())).build();
	}
	
	private LectureBlock saveLectureBlock(LectureBlockVO blockVo, Identity doer) {
		LectureBlock block;
		int currentPlannedLectures;
		boolean autoclose = false;
		if(blockVo.getKey() != null && blockVo.getKey() > 0) {
			block = lectureService.getLectureBlock(blockVo);
			currentPlannedLectures = block.getPlannedLecturesNumber();
			if("autoclosed".equals(blockVo.getRollCallStatus()) && block.getRollCallStatus() != LectureRollCallStatus.autoclosed) {
				autoclose = true;
			}
		} else {
			block = lectureService.createLectureBlock(entry);
			currentPlannedLectures = -1;
			if("autoclosed".equals(blockVo.getRollCallStatus())) {
				autoclose = true;
			}
		}
		
		if(blockVo.getExternalId() != null) {
			block.setExternalId(blockVo.getExternalId());
		}
		if(blockVo.getExternalRef() != null) {
			block.setExternalRef(blockVo.getExternalRef());
		}
		if(blockVo.getTitle() != null) {
			block.setTitle(blockVo.getTitle());
		}
		if(blockVo.getDescription() != null) {
			block.setDescription(blockVo.getDescription());
		}
		if(blockVo.getPreparation() != null) {
			block.setPreparation(blockVo.getPreparation());
		}
		if(blockVo.getLocation() != null) {
			block.setLocation(blockVo.getLocation());
		}
		if(blockVo.getComment() != null) {
			block.setComment(blockVo.getComment());
		}
		if(blockVo.getStartDate() != null) {
			block.setStartDate(blockVo.getStartDate());
		}
		if(blockVo.getEndDate() != null) {
			block.setEndDate(blockVo.getEndDate());
		}
		if(blockVo.getCompulsory() != null) {
			block.setCompulsory(blockVo.getCompulsory().booleanValue());
		}
		if(blockVo.getManagedFlagsString() != null) {
			block.setManagedFlagsString(blockVo.getManagedFlagsString());
		}
		block.setPlannedLecturesNumber(blockVo.getPlannedLectures());
		if(blockVo.getMeetingTitle() != null) {
			block.setMeetingTitle(blockVo.getMeetingTitle());
		}
		if(blockVo.getMeetingUrl() != null) {
			block.setMeetingUrl(blockVo.getMeetingUrl());
		}
		
		if(autoclose) {
			block.setStatus(LectureBlockStatus.done);
			block.setRollCallStatus(LectureRollCallStatus.autoclosed);
			if(block.getAutoClosedDate() != null) {
				((LectureBlockImpl)block).setAutoClosedDate(new Date());
			}
			if(block.getEffectiveLecturesNumber() <= 0) {
				block.setEffectiveLecturesNumber(block.getPlannedLecturesNumber());
			}
		}

		LectureBlock savedLectureBlock = lectureService.save(block, null);
		if(currentPlannedLectures > 0 && currentPlannedLectures != savedLectureBlock.getPlannedLecturesNumber()) {
			lectureService.adaptRollCalls(savedLectureBlock);
		}
		if(autoclose) {
			lectureService.syncParticipantSummariesAndRollCalls(savedLectureBlock, LectureBlockAuditLog.Action.autoclose);
		}
		
		AssessmentMode assessmentMode = assessmentModeMgr.getAssessmentMode(savedLectureBlock);
		if(assessmentMode != null) {
			assessmentModeMgr.syncAssessmentModeToLectureBlock(assessmentMode);
			assessmentModeMgr.merge(assessmentMode, false, doer);
		}
		return savedLectureBlock;
	}
	
	/**
	 * Return the configuration of the specified course or repository entry.
	 * 
	 * @param httpRequest The HTTP request
	 * @return The configuration
	 */
	@GET
	@Path("configuration")
	@Operation(summary = "Return the configuration", description = "Return the configuration of the specified course or repository entry")
	@ApiResponse(responseCode = "200", description = "The configuration of the lecture's feature", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryEntryLectureConfigurationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryEntryLectureConfigurationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getConfiguration(@Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		RepositoryEntryLectureConfigurationVO configVo;
		if(config == null ) {
			configVo = new RepositoryEntryLectureConfigurationVO();
		} else {
			configVo = new RepositoryEntryLectureConfigurationVO(config);
		}
		return Response.ok(configVo).build();
	}
	
	/**
	 * Update the configuration of the lecture's feature of a specified
	 * course or repository entry.
	 * 
	 * @param configuration The configuration
	 * @return It returns the updated configuration.
	 */
	@POST
	@Path("configuration")
	@Operation(summary = "Update the configuration", description = "Update the configuration of the lecture's feature of a specified")
	@ApiResponse(responseCode = "200", description = "The updated configuration", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RepositoryEntryLectureConfigurationVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RepositoryEntryLectureConfigurationVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateConfiguration(RepositoryEntryLectureConfigurationVO configuration) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		if(configuration.getLectureEnabled() != null) {
			config.setLectureEnabled(configuration.getLectureEnabled());
		}
		if(configuration.getCalculateAttendanceRate() != null) {
			config.setCalculateAttendanceRate(configuration.getCalculateAttendanceRate());
		}
		if(configuration.getRequiredAttendanceRate() != null) {
			config.setRequiredAttendanceRate(configuration.getRequiredAttendanceRate());
		}
		if(configuration.getOverrideModuleDefault() != null) {
			config.setOverrideModuleDefault(configuration.getOverrideModuleDefault());
		}
		if(configuration.getCourseCalendarSyncEnabled() != null) {
			config.setCourseCalendarSyncEnabled(configuration.getCourseCalendarSyncEnabled());
		}
		if(configuration.getRollCallEnabled() != null) {
			config.setRollCallEnabled(configuration.getRollCallEnabled());
		}
		if(configuration.getTeacherCalendarSyncEnabled() != null) {
			config.setTeacherCalendarSyncEnabled(configuration.getTeacherCalendarSyncEnabled());
		}
		RepositoryEntryLectureConfiguration updatedConfig = lectureService.updateRepositoryEntryLectureConfiguration(config);
		return Response.ok(new RepositoryEntryLectureConfigurationVO(updatedConfig)).build();
	}

	/**
	 * To get the web service for a specific lecture block.
	 * 
	 * @param lectureBlockKey The primary key of the lecture block
	 * @param httpRequest The HTTP request
	 * @return The web service for a single lecture block.
	 */
	@Path("{lectureBlockKey}")
	@Operation(summary = "Get the web service for a specific lecture block", description = "Get the web service for a specific lecture block")
	@ApiResponse(responseCode = "200", description = "The web service for a single lecture block")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	public LectureBlockWebService getLectureBlockWebService(@PathParam("lectureBlockKey") Long lectureBlockKey, @Context HttpServletRequest httpRequest)
	throws WebApplicationException {
		if(!administrator) {
			throw new WebApplicationException(Status.FORBIDDEN);
		}
		LectureBlock lectureBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(lectureBlockKey));
		if(lectureBlock == null || !lectureBlock.getEntry().equals(entry)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		LectureBlockWebService ws = new LectureBlockWebService(lectureBlock, entry, administrator);
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}

	@POST
	@Path("healmoved/{originEntryKey}")
	@Operation(summary = "Post Entry", description = "Post Entry")
	@ApiResponse(responseCode = "200", description = "Entry has been posted")
	public Response healMoved(@PathParam("originEntryKey") Long originEntryKey) {
		//check the lecture summary
		
		RepositoryEntry originEntry = repositoryService.loadByKey(originEntryKey);
		if(originEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		int rows = ((LectureServiceImpl)lectureService).healMovedLectureBlocks(entry, originEntry);
		
		if(rows == 0) {
			return Response.ok().status(Status.NOT_MODIFIED).build();
		}
		return Response.ok().build();
	}
	
	/**
	 * Synchronize the calendars based on the lecture blocks.
	 * 
	 * @return 200 if the calendar is successfully synchronized
	 */
	@POST
	@Path("sync/calendar")
	@Operation(summary = "Synchronize the calendars based on the lecture blocks", description = "Synchronize the calendars based on the lecture blocks")
	@ApiResponse(responseCode = "200", description = "The calendar is successfully synchronized")
	public Response syncCalendar() {
		lectureService.syncCalendars(entry);
		return Response.ok().build();
	}
	
	/**
	 * Adapt all roll call to the effective number of lectures. Use with caution!
	 * 
	 * @param httpRequest The HTTP request
	 * @return 200 if the adaptation is successful
	 */
	@GET
	@Path("adaptation")
	@Operation(summary = "Adapt all roll call to the effective number of lectures", description = "Adapt all roll call to the effective number of lectures. Use with caution!")
	@ApiResponse(responseCode = "200", description = "The adaptation is successful")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	public Response adaptation(@Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		lectureService.adaptAll(getIdentity(httpRequest));
		return Response.ok().build();
	}
}