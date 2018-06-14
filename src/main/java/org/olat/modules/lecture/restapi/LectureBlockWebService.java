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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWebService {
	
	private static final OLog log = Tracing.createLoggerFor(LectureBlockWebService.class);
	
	private final RepositoryEntry entry;
	private final LectureBlock lectureBlock;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	public LectureBlockWebService(LectureBlock lectureBlock, RepositoryEntry entry) {
		this.entry = entry;
		this.lectureBlock = lectureBlock;
	}
	
	/**
	 * Return a specific lecture blocks.
	 * @response.representation.200.qname {http://www.example.com}lectureBlocksVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc A lecture blocks
	 * @response.representation.200.example {@link org.olat.modules.lecture.restapi.Examples#SAMPLE_LECTUREBLOCKVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @return The lecture blocks
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getLectureBlock() {
		return Response.ok(new LectureBlockVO(lectureBlock, entry.getKey())).build();
	}
	
	@POST
	@Path("entry/{repositoryEntryKey}")
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
	 * @response.representation.200.doc Lecture blocks deleted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @return Nothing
	 */
	@DELETE
	public Response deleteLectureBlock() {
		lectureService.deleteLectureBlock(lectureBlock);
		log.audit("Lecture block deleted: " + lectureBlock);
		return Response.ok().build();
	}

	/**
	 * Get all teachers of the specific lecture blocks.
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of authors
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("teachers")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTeacher() {
		List<Identity> teachers = lectureService.getTeachers(lectureBlock);
		return identitiesToResponse(teachers);
	}
	
	/**
	 * Add a teacher to the lecture block 
	 * @response.representation.200.doc The user is a teacher of the lecture block
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or the user not found
	 * @param identityKey The user identifier
	 * @return It returns 200  if the user is added as teacher of the lecture block.
	 */
	@PUT
	@Path("teachers/{identityKey}")
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
	 * @response.representation.200.doc The user was successfully removed as teacher of the lecture block
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course, the lecture block or the user not found
	 * @param identityKey The user identifier
	 * @return It returns 200  if the user is removed as teacher of the lecture block
	 */
	@DELETE
	@Path("teachers/{identityKey}")
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
	 * @response.representation.200.doc Successfully added
	 * @return 200 if all ok
	 */
	@PUT
	@Path("participants/repositoryentry")
	public Response addRepositoryEntryParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		List<Group> currentGroups = lectureService.getLectureBlockToGroups(reloadedBlock);
		if(!currentGroups.contains(defGroup)) {
			currentGroups.add(defGroup);
			reloadedBlock = lectureService.save(reloadedBlock, currentGroups);
		}
		lectureService.syncParticipantSummaries(reloadedBlock);
		return Response.ok().build();
	}
	
	/**
	 * Remove the group of the course from the lecture block participants.
	 * @response.representation.200.doc Successfully removed
	 * @return 200 if all ok
	 */
	@DELETE
	@Path("participants/repositoryentry")
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
	 * Synchronize the calendars based on the lecture block.
	 * @response.representation.200.doc The calendar is successfully synchronized
	 * @return 200 if all ok
	 */
	@POST
	@Path("sync/calendar")
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
