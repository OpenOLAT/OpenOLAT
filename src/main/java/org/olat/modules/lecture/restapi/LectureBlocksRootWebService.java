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
import static org.olat.restapi.security.RestSecurityHelper.parseDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Repo")
@Component
@Path("repo/lectures")
public class LectureBlocksRootWebService {
	
	@Autowired
	private LectureService lectureService;
	
	
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
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response searchLectureBlocks(@QueryParam("date") @Parameter(description = "The date") String date, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isAdministrator() && !roles.isLectureManager()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		if(date != null) {
			Date d = parseDate(date, Locale.ENGLISH);
			Date startDate = CalendarUtils.removeTime(d);
			Date endDate = CalendarUtils.endOfDay(d);
			searchParams.setStartDate(startDate);
			searchParams.setEndDate(endDate);
		}
		searchParams.setManager(getIdentity(httpRequest));
		List<LectureBlock> blockList = lectureService.getLectureBlocks(searchParams);
		List<LectureBlockVO> voList = new ArrayList<>(blockList.size());
		for(LectureBlock block:blockList) {
			voList.add(new LectureBlockVO(block, block.getEntry().getKey()));
		}
		LectureBlockVO[] voes = voList.toArray(new LectureBlockVO[voList.size()]);
		return Response.ok(voes).build();
	}

	@Path("rollcalls")
	public LectureBlockRollCallWebService getLectureBlockRollCallWebService() {
		LectureBlockRollCallWebService ws = new LectureBlockRollCallWebService();
		CoreSpringFactory.autowireObject(ws);
		return ws;
	}
}