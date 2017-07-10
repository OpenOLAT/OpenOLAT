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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.id.Roles;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlocksWebService {
	
	private final RepositoryEntry entry;
	
	@Autowired
	private LectureService lectureService;
	
	public LectureBlocksWebService(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	/**
	 * Return the lecture block.
	 * 
	 * @param httpRequest The HTTP request
	 * @return The web service for a single lecture block.
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getLectureBlocks(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<LectureBlock> blockList = lectureService.getLectureBlocks(entry);
		List<LectureBlockVO> voList = new ArrayList<>(blockList.size());
		for(LectureBlock block:blockList) {
			voList.add(new LectureBlockVO(block, entry.getKey()));
		}
		LectureBlockVO[] voes = voList.toArray(new LectureBlockVO[voList.size()]);
		return Response.ok(voes).build();
	}

	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putLectureBlocks(LectureBlockVO block, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		LectureBlock updatedBlock = saveLectureBlock(block);
		return Response.ok(new LectureBlockVO(updatedBlock, entry.getKey())).build();
	}
	
	@POST
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postLectureBlocks(LectureBlockVO block, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		LectureBlock updatedBlock = saveLectureBlock(block);
		return Response.ok(new LectureBlockVO(updatedBlock, entry.getKey())).build();
	}
	
	private LectureBlock saveLectureBlock(LectureBlockVO blockVo) {
		LectureBlock block;
		int currentPlannedLectures;
		if(blockVo.getKey() != null && blockVo.getKey() > 0) {
			block = lectureService.getLectureBlock(blockVo);
			currentPlannedLectures = block.getPlannedLecturesNumber();
		} else {
			block = lectureService.createLectureBlock(entry);
			currentPlannedLectures = -1;
		}
		
		if(blockVo.getExternalId() != null) {
			block.setExternalId(blockVo.getExternalId());
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
		LectureBlock savedLectureBlock = lectureService.save(block, null);
		if(currentPlannedLectures > 0 && currentPlannedLectures != savedLectureBlock.getPlannedLecturesNumber()) {
			lectureService.adaptRollCalls(savedLectureBlock);
		}
		return savedLectureBlock;
	}
	
	@GET
	@Path("configuration")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getConfiguration(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
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
	
	@POST
	@Path("configuration")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response updateConfiguration(RepositoryEntryLectureConfigurationVO configuration, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
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
	public LectureBlockWebService getLectureBlockWebService(@PathParam("lectureBlockKey") Long lectureBlockKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return null;
		}
		LectureBlock lectureBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(lectureBlockKey));
		if(lectureBlock == null || !lectureBlock.getEntry().equals(entry)) {
			return null;
		}
		return new LectureBlockWebService(lectureBlock, entry, lectureService);
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
		lectureService.syncCalendars(entry);
		return Response.ok().build();
	}
	
	@GET
	@Path("adaptation")
	public Response adapatation(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		lectureService.adaptAll();
		return Response.ok().build();
	}
}