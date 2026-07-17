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
import java.util.Optional;

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
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 17 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementLectureBlocksWebService {
	
	private final boolean administrator;
	private final CurriculumElement element;
	
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	public CurriculumElementLectureBlocksWebService(CurriculumElement element, boolean administrator) {
		this.element = element;
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
		
		List<LectureBlock> blockList = lectureService.getLectureBlocks(element, false);
		LectureBlockVO[] voes = new LectureBlockVO[blockList.size()];
		for(int i=blockList.size(); i-->0; ) {
			LectureBlock block = blockList.get(i);
			voes[i] = LectureBlockVO.valueOf(block, block.getEntry(), block.getCurriculumElement());
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
		return saveLectureBlock(block, doer);
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
		return saveLectureBlock(block, doer);
	}
	
	private Response saveLectureBlock(LectureBlockVO blockVo, Identity doer) {
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
			List<RepositoryEntry> curriculumElementEntries = curriculumService.getRepositoryEntries(element);
			if(blockVo.getRepoEntryKey() != null) {
				Optional<RepositoryEntry> entry = curriculumElementEntries.stream()
						.filter(v -> blockVo.getRepoEntryKey().equals(v.getKey()))
						.findFirst();
				if(entry.isEmpty()) {
					return Response.status(Status.CONFLICT).build();
				}
				block = lectureService.createLectureBlock(element, entry.get());
			} else if(curriculumElementEntries.isEmpty()) {
				block = lectureService.createLectureBlock(element, null);
			} else if(curriculumElementEntries.size() == 1) {
				block = lectureService.createLectureBlock(element, curriculumElementEntries.get(0));
			} else {
				return Response.status(Status.CONFLICT).build();
			}

			currentPlannedLectures = -1;
			if("autoclosed".equals(blockVo.getRollCallStatus())) {
				autoclose = true;
			}
		}
		
		blockVo.transferTo(block);
		
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

		return Response.ok(LectureBlockVO.valueOf(savedLectureBlock, savedLectureBlock.getEntry(), savedLectureBlock.getCurriculumElement()))
				.build();
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
		if(lectureBlock == null || !element.equals(lectureBlock.getCurriculumElement())) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		LectureBlockWebService ws = new LectureBlockWebService(lectureBlock,
				lectureBlock.getEntry(), element, administrator);
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}
}