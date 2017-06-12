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
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;

/**
 * 
 * Initial date: 9 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWebService {
	
	private final RepositoryEntry entry;
	private final LectureBlock lectureBlock;
	
	private final LectureService lectureService;
	private final BaseSecurity securityManager;
	
	public LectureBlockWebService(LectureBlock lectureBlock, RepositoryEntry entry, LectureService lectureService) {
		this.entry = entry;
		this.lectureBlock = lectureBlock;
		this.lectureService = lectureService;
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
	}
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getLectureBlock() {
		return Response.ok(new LectureBlockVO(lectureBlock, entry.getKey())).build();
	}
	
	@DELETE
	public Response deleteLectureBlock() {
		lectureService.deleteLectureBlock(lectureBlock);
		return Response.ok().build();
	}

	@GET
	@Path("teachers")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTeacher() {
		List<Identity> teachers = lectureService.getTeachers(lectureBlock);
		return identitiesToResponse(teachers);
	}
	
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
	 * 
	 * @response.representation.200.doc Successfully added
	 * @return 200 if all ok
	 */
	@PUT
	@Path("participants/repositoryentry")
	public Response addRepositoryEntryParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		Group defGroup = CoreSpringFactory.getImpl(RepositoryService.class)
				.getDefaultGroup(entry);
		List<Group> currentGroups = lectureService.getLectureBlockToGroups(reloadedBlock);
		if(!currentGroups.contains(defGroup)) {
			currentGroups.add(defGroup);
			lectureService.save(reloadedBlock, currentGroups);
		}
		return Response.ok().build();
	}
	
	/**
	 * Remove the group of the course from the lecture block participants.
	 * 
	 * @response.representation.200.doc Successfully removed
	 * @return 200 if all ok
	 */
	@DELETE
	@Path("participants/repositoryentry")
	public Response deleteRepositoryEntryParticipantGroup() {
		LectureBlock reloadedBlock = lectureService.getLectureBlock(lectureBlock);
		Group defGroup = CoreSpringFactory.getImpl(RepositoryService.class)
				.getDefaultGroup(entry);
		List<Group> currentGroups = lectureService.getLectureBlockToGroups(reloadedBlock);
		if(currentGroups.contains(defGroup)) {
			currentGroups.remove(defGroup);
			lectureService.save(reloadedBlock, currentGroups);
		}
		return Response.ok().build();
	}
	
	/**
	 * Synchronize the calendars based on the lecture block.
	 * 
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
