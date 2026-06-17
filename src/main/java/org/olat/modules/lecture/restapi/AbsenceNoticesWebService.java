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
package org.olat.modules.lecture.restapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeToLectureBlock;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.manager.AbsenceNoticeToLectureBlockDAO;
import org.olat.modules.lecture.manager.AbsenceNoticeToRepositoryEntryDAO;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 4 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AbsenceNoticesWebService {
	

	private final RepositoryEntry entry;
	private final boolean administrator;

	@Autowired
	private BaseSecurity baseSecurity;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private AbsenceNoticeToLectureBlockDAO absenceNoticeToLectureBlockDao;
	@Autowired
	private AbsenceNoticeToRepositoryEntryDAO absenceNoticeToRepositoryEntryDao;
	
	public AbsenceNoticesWebService(RepositoryEntry entry, boolean administrator) {
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
	@Path("{identityKey}")
	@Operation(summary = "Return the lecture blocks", description = "Return the lecture blocks of the specified course or repository entry")
	@ApiResponse(responseCode = "200", description = "An array of lecture blocks",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AbsenceNoticeVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = AbsenceNoticeVO.class)))
				})
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAbsenceNotices(@PathParam("identityKey") Long identityKey,
			@QueryParam("startDate") String startDate, @QueryParam("endDate") String endDate) {
		if(!administrator) {
			return Response.status(Status.FORBIDDEN).build();
		}
		
		Identity identity = baseSecurity.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.status(Status.NOT_FOUND).build();
		}
		
		Date start = ObjectFactory.parseDate(startDate);
		Date end = ObjectFactory.parseDate(endDate);
		
		AbsenceNoticeSearchParameters searchParams = new AbsenceNoticeSearchParameters();
		searchParams.setRepositoryEntry(entry);
		searchParams.setParticipant(identity);
		searchParams.setStartDate(start);
		searchParams.setEndDate(end);
		List<AbsenceNoticeInfos> infos = lectureService.searchAbsenceNotices(searchParams);
		List<AbsenceNoticeVO> voes = new ArrayList<>(infos.size());
		for(AbsenceNoticeInfos info:infos) {
			AbsenceNotice absenceNotice = info.getAbsenceNotice();
			
			List<Long> lectureBlockKeys = null;
			List<Long> repositoryEntriesKeys = null;
			if(absenceNotice.getNoticeTarget() == AbsenceNoticeTarget.entries) {
				repositoryEntriesKeys = absenceNoticeToRepositoryEntryDao.getRelations(absenceNotice).stream()
					.map(AbsenceNoticeToRepositoryEntry::getEntry)
					.map(RepositoryEntry::getKey)
					.toList();
			} else if(absenceNotice.getNoticeTarget() == AbsenceNoticeTarget.lectureblocks) {
				lectureBlockKeys = absenceNoticeToLectureBlockDao.getRelations(absenceNotice).stream()
					.map(AbsenceNoticeToLectureBlock::getLectureBlock)
					.map(LectureBlock::getKey)
					.toList();
			}
			
			AbsenceNoticeVO vo = AbsenceNoticeVO.valueOf(absenceNotice, lectureBlockKeys, repositoryEntriesKeys);
			voes.add(vo);
		}
		
		return Response.ok(voes.toArray(new AbsenceNoticeVO[voes.size()])).build();
	}

}
