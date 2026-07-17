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
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.restapi.BigBlueButtonMeetingVO;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.manager.LectureBlockToTaxonomyLevelDAO;
import org.olat.modules.roommanagement.Room;
import org.olat.modules.roommanagement.RoomBooking;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.olat.modules.roommanagement.RoomManagementService;
import org.olat.modules.roommanagement.RoomStatus;
import org.olat.modules.roommanagement.model.SearchRoomParameters;
import org.olat.modules.roommanagement.restapi.RoomBookingVO;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 9 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWebService {
	
	private static final Logger log = Tracing.createLoggerFor(LectureBlockWebService.class);
	
	private final RepositoryEntry entry;
	private final CurriculumElement element;
	private final boolean administrator;
	private LectureBlock lectureBlock;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private LectureBlockToTaxonomyLevelDAO lectureBlockToTaxonomyLevelDao;
	@Autowired(required = false)
	private RoomManagementModule roomManagementModule;
	@Autowired(required = false)
	private RoomManagementService roomManagementService;

	public LectureBlockWebService(LectureBlock lectureBlock, RepositoryEntry entry, CurriculumElement element, boolean administrator) {
		this.entry = entry;
		this.element = element;
		this.lectureBlock = lectureBlock;
		this.administrator = administrator;
	}
	
	/**
	 * Return a specific lecture blocks.
	 * 
	 * @return The lecture blocks
	 */
	@GET
	@Operation(summary = "Return a specific lecture block", description = "Return a specific lecture block")
	@ApiResponse(responseCode = "200", description = "The lecture block", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getLectureBlock() {
		return Response.ok(LectureBlockVO.valueOf(lectureBlock, entry, element)).build();
	}
	
	@POST
	@Path("entry/{repositoryEntryKey}")
	@Operation(summary = "Post a specific lecture block", description = "Post a specific lecture block")
	@ApiResponse(responseCode = "200", description = "The lecture block posted", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = LectureBlockVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = LectureBlockVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response moveLectureBlock(@PathParam("repositoryEntryKey") Long repositoryEntryKey) {
		RepositoryEntry newEntry = repositoryService.loadByKey(repositoryEntryKey);
		if(newEntry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		LectureBlock movedLectureBlock = lectureService.moveLectureBlock(lectureBlock, newEntry);
		return Response.ok(LectureBlockVO.valueOf(movedLectureBlock, movedLectureBlock.getEntry(), movedLectureBlock.getCurriculumElement())).build();
	}
	

	/**
	 * Delete a specific lecture blocks.
	 * 
	 * @return Nothing
	 */
	@DELETE
	@Operation(summary = "Delete a specific lecture block", description = "Delete a specific lecture block")
	@ApiResponse(responseCode = "200", description = "Lecture block deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient.")
	@ApiResponse(responseCode = "404", description = "Not found.")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteLectureBlock() {
		lectureService.deleteLectureBlock(lectureBlock, null);
		log.info(Tracing.M_AUDIT, "Lecture block deleted: {}", lectureBlock);
		return Response.ok().build();
	}

	/**
	 * Get all teachers of the specific lecture blocks.
	 * 
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("teachers")
	@Operation(summary = "Get all teachers of the specific lecture block", description = "Get all teachers of the specific lecture block")
	@ApiResponse(responseCode = "200", description = "The array of authors", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTeacher() {
		List<Identity> teachers = lectureService.getTeachers(lectureBlock);
		return identitiesToResponse(teachers);
	}
	
	/**
	 * Add a teacher to the lecture block 
	 *
	 * @param identityKey The user identifier
	 * @return It returns 200  if the user is added as teacher of the lecture block.
	 */
	@PUT
	@Path("teachers/{identityKey}")
	@Operation(summary = "Add a teacher to the lecture block ", description = "Add a teacher to the lecture block ")
	@ApiResponse(responseCode = "200", description = "The user is a teacher of the lecture block")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or the user not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response addTeacher(@PathParam("identityKey") Long identityKey) {
		Identity teacher = securityManager.loadIdentityByKey(identityKey);
		if(teacher == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		lectureService.addTeacher(lectureBlock, teacher);
		return Response.ok().build();
	}
	
	/**
	 * Remove a teacher of the lecture block
	 * 
	 * @param identityKey The user identifier
	 * @return It returns 200  if the user is removed as teacher of the lecture block
	 */
	@DELETE
	@Path("teachers/{identityKey}")
	@Operation(summary = "Remove a teacher of the lecture block", description = "Remove a teacher of the lecture block")
	@ApiResponse(responseCode = "200", description = "The user was successfully removed as teacher of the lecture block")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course, the lecture block or the user not found")
	public Response removeTeacher(@PathParam("identityKey") Long identityKey) {
		Identity teacher = securityManager.loadIdentityByKey(identityKey);
		if(teacher == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		lectureService.removeTeacher(lectureBlock, teacher);
		return Response.ok().build();
	}
	
	/**
	 * Add the group of the course to the lecture block participants list.
	 * 
	 * @return 200 if all ok
	 */
	@PUT
	@Path("participants/repositoryentry")
	@Operation(summary = "Add group", description = "Add the group of the course to the lecture block participants list")
	@ApiResponse(responseCode = "200", description = "Successfully added")
	public Response addRepositoryEntryParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		List<Group> currentGroups = lectureService.getLectureBlockToGroups(reloadedBlock);
		if(!currentGroups.contains(defGroup)) {
			currentGroups.add(defGroup);
			reloadedBlock = lectureService.save(reloadedBlock, currentGroups);
		}
		dbInstance.commit();
		lectureService.syncParticipantSummaries(reloadedBlock);
		return Response.ok().build();
	}
	
	/**
	 * Remove the group of the course from the lecture block participants.
	 * 
	 * @return 200 if all ok
	 */
	@DELETE
	@Path("participants/repositoryentry")
	@Operation(summary = "Remove group", description = "Remove the group of the course from the lecture block participants")
	@ApiResponse(responseCode = "200", description = "Successfully removed")
	public Response deleteRepositoryEntryParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		List<Group> currentGroups = lectureService.getLectureBlockToGroups(reloadedBlock);
		if(currentGroups.contains(defGroup)) {
			currentGroups.remove(defGroup);
			lectureService.save(reloadedBlock, currentGroups);
		}
		return Response.ok().build();
	}
	
	/**
	 * Sync the groups of all curriculum elements to the lecture block participants list. Add missing
	 * ones and removing groups from elements which are not longer linked to the course.
	 * 
	 * @return 200 if all ok
	 */
	@PUT
	@Path("participants/curriculum")
	@Operation(summary = "Add group", description = "Synchronize the groups of all curriculum elements to the lecture block participants list")
	@ApiResponse(responseCode = "200", description = "Successfully added")
	public Response syncCurriculumElementParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		List<Group> elementGroups = elements.stream()
				.filter(el -> el.getElementStatus() != CurriculumElementStatus.deleted)
				.map(CurriculumElement::getGroup)
				.collect(Collectors.toList());

		List<Group> allGroups = lectureService.getLectureBlockToGroups(reloadedBlock, RepositoryEntryRelationType.all);
		
		boolean changed = false;
		for(Group elementGroup:elementGroups) {
			if(!allGroups.contains(elementGroup)) {
				allGroups.add(elementGroup);
				changed = true;
			}
		}
		
		List<Group> currentElementGroups = lectureService.getLectureBlockToGroups(reloadedBlock, RepositoryEntryRelationType.curriculums);
		for(Group currentElementGroup:currentElementGroups) {
			if(!elementGroups.contains(currentElementGroup)) {
				allGroups.remove(currentElementGroup);
				changed = true;
			}
		}

		if(changed) {
			reloadedBlock = lectureService.save(reloadedBlock, allGroups);
		}
		dbInstance.commit();
		lectureService.syncParticipantSummaries(reloadedBlock);
		Status status = changed ? Status.OK : Status.NOT_MODIFIED;
		return Response.ok(status).build();
	}
	
	/**
	 * Remove the group of all curriculum elements from the lecture block participants.
	 * 
	 * @return 200 if all ok
	 */
	@DELETE
	@Path("participants/curriculum")
	@Operation(summary = "Remove group", description = "Remove the group of all curriculum elements from the lecture block participants")
	@ApiResponse(responseCode = "200", description = "Successfully removed")
	public Response deleteCurriculumElementParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(entry);
		List<Group> currentGroups = lectureService.getLectureBlockToGroups(reloadedBlock);
		
		boolean changed = false;
		for(CurriculumElement element:elements) {
			Group elementGroup = element.getGroup();
			if(currentGroups.contains(elementGroup)) {
				currentGroups.remove(elementGroup);
				changed = true;
			}
		}
		
		if(changed) {
			lectureService.save(reloadedBlock, currentGroups);
		}
		
		Status status = changed ? Status.OK : Status.NOT_MODIFIED;
		return Response.ok(status).build();
	}

	@GET
	@Path("bigbluebuttonmeeting")
	@Operation(summary = "Return the BigBlueButton meeting of the lecture block if any",
			description = "Return the BigBlueButton meeting of the lecture block if any")
	@ApiResponse(responseCode = "200", description = "The BigBlueButton meeting", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonMeetingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonMeetingVO.class)) })
	@ApiResponse(responseCode = "204", description = "The lecture block don't have an online meeting")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getBigBlueButtonMeeting() {
		BigBlueButtonMeeting meeting = lectureBlock.getBBBMeeting();
		if(meeting == null) {
			return Response.noContent().status(Status.NO_CONTENT).build();
		}
		return Response.ok(BigBlueButtonMeetingVO.valueOf(meeting)).build();
	}
	
	/**
	 * Create or update a BigBlueButton meeting.
	 * 
	 * @param block The meeting
	 * @return The updated / created meeting.
	 */
	@PUT
	@Path("bigbluebuttonmeeting")
	@Operation(summary = "Create or update a BigBlueButton meeting", description = "Create or update a BigBlueButton meeting")
	@ApiResponse(responseCode = "200", description = "The updated online meeting",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonMeetingVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonMeetingVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@ApiResponse(responseCode = "406", description = "Template is missing or slot is not available")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putBigBlueButtonMeeting(BigBlueButtonMeetingVO meeting, @Context HttpServletRequest httpRequest) {
		return saveBigBlueButtonMeeting(meeting, httpRequest);
	}
	
	/**
	 * Create or update a BigBlueButton meeting.
	 * 
	 * @param block The meeting
	 * @return The updated / created meeting.
	 */
	@POST
	@Path("bigbluebuttonmeeting")
	@Operation(summary = "Create or update a BigBlueButton meeting", description = "Create or update a BigBlueButton meeting")
	@ApiResponse(responseCode = "200", description = "The updated online meeting",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = BigBlueButtonMeetingVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = BigBlueButtonMeetingVO.class))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@ApiResponse(responseCode = "406", description = "Template is missing or slot is not available")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postBigBlueButtonMeeting(BigBlueButtonMeetingVO meeting, @Context HttpServletRequest httpRequest) {
		return saveBigBlueButtonMeeting(meeting, httpRequest);
	}
	
	private Response saveBigBlueButtonMeeting(BigBlueButtonMeetingVO meetingVo, HttpServletRequest httpRequest) {
		if(!bigBlueButtonModule.isEnabled() || !bigBlueButtonModule.isLecturesEnabled()) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		if(meetingVo.getKey() == null && meetingVo.getTemplateKey() == null) {
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		
		Identity doer = getIdentity(httpRequest);
		if(lectureBlock.getBBBMeeting() != null && lectureBlock.getBBBMeeting().getKey().equals(meetingVo.getKey())) {
			BigBlueButtonMeeting bigBlueButtonMeeting = lectureBlock.getBBBMeeting();
			BigBlueButtonMeetingVO.transfer(meetingVo, bigBlueButtonMeeting);
			lectureBlock = lectureService.save(lectureBlock, null);
			return Response.ok(BigBlueButtonMeetingVO.valueOf(lectureBlock.getBBBMeeting())).build();
		} else if(lectureBlock.getBBBMeeting() == null && meetingVo.getKey() == null) {
			BigBlueButtonMeetingTemplate template = bigBlueButtonManager.getTemplate(meetingVo.getTemplateKey());
			if(template == null) {
				return Response.serverError().status(Status.NOT_FOUND).build();
			}
			
			boolean slotAvailable = bigBlueButtonManager.isSlotAvailable(null, template,
					lectureBlock.getStartDate(), meetingVo.getLeadTime(), lectureBlock.getEndDate(), meetingVo.getFollowupTime());
			if(slotAvailable) {
				BigBlueButtonMeeting bigBlueButtonMeeting = bigBlueButtonManager.createAndPersistMeeting(meetingVo.getName(), entry, null, null, doer);
				BigBlueButtonMeetingVO.transfer(meetingVo, bigBlueButtonMeeting);
				bigBlueButtonMeeting.setTemplate(template);
				lectureBlock.setBBBMeeting(bigBlueButtonMeeting);
				lectureBlock = lectureService.save(lectureBlock, null);
				return Response.ok(BigBlueButtonMeetingVO.valueOf(lectureBlock.getBBBMeeting())).build();
			}
			return Response.serverError().status(Status.NOT_ACCEPTABLE).build();
		}
		return Response.ok().status(Status.NO_CONTENT).build();
	}
	
	@DELETE
	@Path("bigbluebuttonmeeting")
	@Operation(summary = "Delete BigBlueButton meeting", description = "Delete BigBlueButton meeting")
	@ApiResponse(responseCode = "200", description = "Meeting deleted successfully")
	@ApiResponse(responseCode = "304", description = "No meeting to delete")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	public Response deleteBigBlueButtonMeeting() {
		BigBlueButtonMeeting bigBlueButtonMeeting = lectureBlock.getBBBMeeting();
		if(bigBlueButtonMeeting != null) {
			lectureBlock.setBBBMeeting(null);
			lectureBlock = lectureBlockDao.update(lectureBlock);
			bigBlueButtonManager.deleteMeeting(bigBlueButtonMeeting, null);
			dbInstance.commit();
			return Response.ok().build();
		}
		return Response.ok(Status.NOT_MODIFIED).build();
	}
	
	@GET
	@Path("taxonomy/levels")
	@Operation(summary = "Get all levels from specific taxonomy", description = "Get all levels from specific taxonomy")
	@ApiResponse(responseCode = "200", description = "The levels", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaxonomyLevelVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = TaxonomyLevelVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTaxonomyLevels() {	
		List<TaxonomyLevel> levels = lectureBlockToTaxonomyLevelDao.getTaxonomyLevels(lectureBlock);
		TaxonomyLevelVO[] voes = new TaxonomyLevelVO[levels.size()];
		for(int i=levels.size(); i-->0; ) {
			voes[i] = TaxonomyLevelVO.valueOf(levels.get(i));
		}
		return Response.ok(voes).build();
	}
	
	@PUT
	@Path("taxonomy/levels/{taxonomyLevelKey}")
	@Operation(summary = "Put level", description = "Put level to a specific taxonomy")
	@ApiResponse(responseCode = "200", description = "The level put", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = TaxonomyLevelVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = TaxonomyLevelVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	public Response putTaxonomyLevel(@PathParam("taxonomyLevelKey") Long taxonomyLevelKey) {
		List<TaxonomyLevel> levels = lectureBlockToTaxonomyLevelDao.getTaxonomyLevels(lectureBlock);
		for(TaxonomyLevel level:levels) {
			if(level.getKey().equals(taxonomyLevelKey)) {
				return Response.ok().status(Status.NOT_MODIFIED).build();
			}
		}
		TaxonomyLevel level = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(taxonomyLevelKey));
		if(level == null) {
			return Response.ok(Status.NOT_FOUND).build();
		}
		lectureBlockToTaxonomyLevelDao.createRelation(lectureBlock, level);
		return Response.ok().build();
	}
	
	@DELETE
	@Path("taxonomy/levels/{taxonomyLevelKey}")
	@Operation(summary = "Remove level", description = "Remove level from a specific taxonomy")
	@ApiResponse(responseCode = "200", description = "Level removed successfully")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	public Response deleteTaxonomyLevel(@PathParam("taxonomyLevelKey") Long taxonomyLevelKey) {
		TaxonomyLevelRef level = new TaxonomyLevelRefImpl(taxonomyLevelKey);
		lectureBlockToTaxonomyLevelDao.deleteRelation(lectureBlock, level);
		dbInstance.commitAndCloseSession();
		return Response.ok().build();
	}
	
	/**
	 * Synchronize the calendars based on the lecture block.
	 * 
	 * @return 200 if all ok
	 */
	@POST
	@Path("sync/calendar")
	@Operation(summary = "Synchronize the calendars", description = "Synchronize the calendars based on the lecture block")
	@ApiResponse(responseCode = "200", description = "The calendar is successfully synchronized")
	public Response syncCalendar() {
		lectureService.syncCalendars(lectureBlock);
		return Response.ok().build();
	}
	
	@GET
	@Path("room")
	@Operation(summary = "Get the room booking of the lecture block", description = "Get the room booking of the lecture block, if any")
	@ApiResponse(responseCode = "200", description = "The room booking", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RoomBookingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RoomBookingVO.class)) })
	@ApiResponse(responseCode = "204", description = "The lecture block has no room booking")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRoomBooking() {
		if (roomManagementModule == null || !roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (!administrator) {
			return Response.status(Status.FORBIDDEN).build();
		}
		List<RoomBooking> bookings = roomManagementService.getBookings(lectureBlock);
		if (bookings == null || bookings.isEmpty()) {
			return Response.noContent().status(Status.NO_CONTENT).build();
		}
		return Response.ok(RoomBookingVO.valueOf(bookings.get(0))).build();
	}

	@PUT
	@Path("room")
	@Operation(summary = "Create or update the room booking of the lecture block", description = "Create or replace the room booking of the lecture block. Identifier precedence: roomKey > externalId > externalRef (when unique).")
	@ApiResponse(responseCode = "200", description = "The created or updated room booking", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = RoomBookingVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = RoomBookingVO.class)) })
	@ApiResponse(responseCode = "400", description = "No room identifier supplied")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The room was not found")
	@ApiResponse(responseCode = "409", description = "The room is inactive and cannot be booked",
			content = @Content(mediaType = "application/json",
				schema = @Schema(type = "object", example = "{\"code\":\"room.inactive\"}")))
	@ApiResponse(responseCode = "422", description = "The externalRef is ambiguous (multiple rooms match)",
			content = @Content(mediaType = "application/json",
				schema = @Schema(type = "object", example = "{\"code\":\"room.ambiguousExternalRef\",\"matches\":2}")))
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putRoomBooking(RoomBookingVO vo, @Context HttpServletRequest httpRequest) {
		if (roomManagementModule == null || !roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (!administrator) {
			return Response.status(Status.FORBIDDEN).build();
		}
		if (vo == null) {
			return Response.status(Status.BAD_REQUEST).build();
		}

		Room room;
		if (vo.getRoomKey() != null) {
			room = roomManagementService.getRoom(() -> vo.getRoomKey());
			if (room == null) {
				return Response.status(Status.NOT_FOUND).build();
			}
		} else if (vo.getExternalId() != null && !vo.getExternalId().isBlank()) {
			SearchRoomParameters idParams = new SearchRoomParameters();
			idParams.setExactExternalId(vo.getExternalId());
			idParams.setStatus(List.of(RoomStatus.active, RoomStatus.inactive));
			idParams.setIdentity(getIdentity(httpRequest));
			List<Room> byId = roomManagementService.searchRooms(idParams, getRoles(httpRequest));
			if (byId.isEmpty()) {
				return Response.status(Status.NOT_FOUND).build();
			}
			room = byId.get(0);
		} else if (vo.getExternalRef() != null && !vo.getExternalRef().isBlank()) {
			SearchRoomParameters refParams = new SearchRoomParameters();
			refParams.setExactExternalRef(vo.getExternalRef());
			refParams.setStatus(List.of(RoomStatus.active, RoomStatus.inactive));
			refParams.setIdentity(getIdentity(httpRequest));
			List<Room> byRef = roomManagementService.searchRooms(refParams, getRoles(httpRequest));
			if (byRef.isEmpty()) {
				return Response.status(Status.NOT_FOUND).build();
			}
			if (byRef.size() > 1) {
				String body = "{\"code\":\"room.ambiguousExternalRef\",\"matches\":" + byRef.size() + "}";
				return Response.status(422).entity(body).type(MediaType.APPLICATION_JSON).build();
			}
			room = byRef.get(0);
		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (room.getStatus() != RoomStatus.active) {
			String body = "{\"code\":\"room.inactive\"}";
			return Response.status(Status.CONFLICT).entity(body).type(MediaType.APPLICATION_JSON).build();
		}

		Date startDate = vo.getStartDate() != null ? vo.getStartDate() : lectureBlock.getStartDate();
		Date endDate = vo.getEndDate() != null ? vo.getEndDate() : lectureBlock.getEndDate();

		Identity doer = getIdentity(httpRequest);
		List<RoomBooking> existing = roomManagementService.getBookings(lectureBlock);
		RoomBooking booking;
		if (existing != null && !existing.isEmpty()) {
			booking = existing.get(0);
			booking.setRoom(room);
			booking.setStartDate(startDate);
			booking.setEndDate(endDate);
			booking.setBufferBefore(vo.getBufferBeforeMin());
			booking.setBufferAfter(vo.getBufferAfterMin());
			booking = roomManagementService.updateBooking(booking, doer);
		} else {
			booking = roomManagementService.bookRoom(room, lectureBlock,
					startDate, endDate,
					vo.getBufferBeforeMin(), vo.getBufferAfterMin(), doer);
		}
		return Response.ok(RoomBookingVO.valueOf(booking)).build();
	}

	@DELETE
	@Path("room")
	@Operation(summary = "Delete the room booking of the lecture block", description = "Delete the room booking of the lecture block (204 if no booking exists)")
	@ApiResponse(responseCode = "204", description = "Room booking deleted or no booking existed")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "Not found")
	public Response deleteRoomBooking(@Context HttpServletRequest httpRequest) {
		if (roomManagementModule == null || !roomManagementModule.isEnabled()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		if (!administrator) {
			return Response.status(Status.FORBIDDEN).build();
		}
		List<RoomBooking> existing = roomManagementService.getBookings(lectureBlock);
		if (existing != null && !existing.isEmpty()) {
			Identity doer = getIdentity(httpRequest);
			roomManagementService.deleteBooking(existing.get(0), doer);
		}
		return Response.noContent().build();
	}

	private Response identitiesToResponse(List<Identity> identities) {
		int count = 0;
		UserVO[] ownerVOs = new UserVO[identities.size()];
		for(int i=0; i<identities.size(); i++) {
			ownerVOs[count++] = UserVOFactory.get(identities.get(i));
		}
		return Response.ok(ownerVOs).build();
	}
}
