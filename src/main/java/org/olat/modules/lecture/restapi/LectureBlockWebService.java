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

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.manager.LectureBlockToTaxonomyLevelDAO;
import org.olat.modules.taxonomy.TaxonomyLevel;
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
	private final LectureBlock lectureBlock;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private LectureBlockToTaxonomyLevelDAO lectureBlockToTaxonomyLevelDao;
	
	public LectureBlockWebService(LectureBlock lectureBlock, RepositoryEntry entry) {
		this.entry = entry;
		this.lectureBlock = lectureBlock;
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
		return Response.ok(new LectureBlockVO(lectureBlock, entry.getKey())).build();
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
		return Response.ok(new LectureBlockVO(movedLectureBlock, movedLectureBlock.getEntry().getKey())).build();
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
		TaxonomyLevel level = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(taxonomyLevelKey));
		if(level == null) {
			return Response.ok(Status.NOT_FOUND).build();
		}
		lectureBlockToTaxonomyLevelDao.deleteRelation(lectureBlock, level);
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
	
	private Response identitiesToResponse(List<Identity> identities) {
		int count = 0;
		UserVO[] ownerVOs = new UserVO[identities.size()];
		for(int i=0; i<identities.size(); i++) {
			ownerVOs[count++] = UserVOFactory.get(identities.get(i));
		}
		return Response.ok(ownerVOs).build();
	}
}
