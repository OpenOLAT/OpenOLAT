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
import static org.olat.restapi.security.RestSecurityHelper.parseDate;
import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	 * @response.representation.200.qname {http://www.example.com}lectureBlocksVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc An array of lecture blocks
	 * @response.representation.200.example {@link org.olat.modules.lecture.restapi.Examples#SAMPLE_LECTUREBLOCKVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @return The lecture blocks
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response searchLectureBlocks(@QueryParam("date") String date, @Context HttpServletRequest httpRequest) {
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