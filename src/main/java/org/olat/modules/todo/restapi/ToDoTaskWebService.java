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
package org.olat.modules.todo.restapi;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 30 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "ToDoTask")
@Component
@Path("todotasks")
public class ToDoTaskWebService {
	
	@Autowired
	private ToDoService toDoService;

	/**
	 * Get the to-do tasks of the user.
	 * 
	 * @param status 
	 * @param dueDateFrom 
	 * @param dueDateTo 
	 * @param request The request
	 * @return The to-to tasks
	 */
	@GET
	@Path("my")
	@Operation(summary = "Get the to-do tasks of the authenticated user", description = "Get the to-do tasks of the authenticated user")
	@ApiResponse(responseCode = "200", description = "The to-dos of the user", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ToDoTaskVOes.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ToDoTaskVOes.class)) })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getToDos(
			@QueryParam("status") @Parameter(description = "Filter by status") String status,
			@QueryParam("dueDateFrom") @Parameter(description = "Filter to-do tasks whose due date is after this date. Format: yyyy-MM-ddThh:mm:ss") String dueDateFromStr,
			@QueryParam("dueDateTo") @Parameter(description = "Filter to-do tasks whose due date is before this date. Format: yyyy-MM-ddThh:mm:ss") String dueDateToStr,
			@Context HttpServletRequest request) {
		Identity identity = RestSecurityHelper.getIdentity(request);
		if (identity == null) {
			return Response.serverError().status(Response.Status.UNAUTHORIZED).build();
		}
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginDeleted(Boolean.FALSE);
		if (StringHelper.containsNonWhitespace(status) && ToDoStatus.OPEN_TO_DONE_NAMES.contains(status)) {
			searchParams.setStatus(List.of(ToDoStatus.valueOf(status)));
		} else {
			searchParams.setStatus(ToDoStatus.OPEN_TO_DONE);
		}
		
		Date dueDateFrom = StringHelper.containsNonWhitespace(dueDateFromStr)
				? ObjectFactory.parseDate(dueDateFromStr)
				: null;
		Date dueDateTo = StringHelper.containsNonWhitespace(dueDateToStr)
				? ObjectFactory.parseDate(dueDateToStr)
				: null;
		if (dueDateFrom != null || dueDateTo != null) {
			DateRange dueDateRange = new DateRange(
					dueDateFrom != null? dueDateFrom: DateUtils.addYears(new Date(), -10),
					dueDateTo != null? dueDateTo: DateUtils.addYears(new Date(), 10));
			searchParams.setDueDateRanges(List.of(dueDateRange));
		}
		
		searchParams.setAssigneeOrDelegatee(identity);
		
		List<ToDoTaskVO> toDoTaskVOlist = toDoService.getToDoTasks(searchParams).stream()
				.map(ToDoTaskVO::valueOf)
				.toList();
		ToDoTaskVOes toDoTaskVOes = new ToDoTaskVOes();
		toDoTaskVOes.setToDoTasks(toDoTaskVOlist);
		return Response.ok(toDoTaskVOes).build();
	}
	
	/**
	 * Change the status of a to-do task.
	 * 
	 * @param toDoTaskKey The key of the to-do task
	 * @return
	 */
	@POST
	@Path("{toDoTaskKey}/status")
	@Operation(summary = "Change the status of a to-do", description = "Change the status of a to-do. Available status: open, inProgress, done. Only the status of my to-dos can be changed.")
	@ApiResponse(responseCode = "200", description = "The status was successfully changed")
	@ApiResponse(responseCode = "400", description = "The status is not available")
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response postToDoTaskStatus(
			@PathParam("toDoTaskKey") Long toDoTaskKey,
			ToDoStatusVO statusVO,
			@Context HttpServletRequest request) {
		Identity identity = RestSecurityHelper.getIdentity(request);
		if (identity == null) {
			return Response.serverError().status(Response.Status.UNAUTHORIZED).build();
		}
		
		String requestedStatusStr = statusVO.getStatus();
		if (!StringHelper.containsNonWhitespace(requestedStatusStr) || !ToDoStatus.OPEN_TO_DONE_NAMES.contains(requestedStatusStr)) {
			return Response.serverError().status(Response.Status.BAD_REQUEST).build();
		}
		ToDoStatus status = ToDoStatus.valueOf(requestedStatusStr);
		
		// Currently only the status of MY to-dos can be changed.
		// My be have to extended in future e.g. managers
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setStatus(ToDoStatus.OPEN_TO_DONE);
		searchParams.setOriginDeleted(Boolean.FALSE);
		searchParams.setAssigneeOrDelegatee(identity);
		searchParams.setToDoTasks(List.of(() -> toDoTaskKey));
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		if (toDoTasks.isEmpty()) {
			return Response.serverError().status(Response.Status.NOT_FOUND).build();
		}
		
		ToDoTask toDoTask = toDoTasks.get(0);
		if (status != toDoTask.getStatus()) {
			ToDoProvider provider = toDoService.getProvider(toDoTask.getType());
			if (provider != null) {
				provider.upateStatus(identity, toDoTask, toDoTask.getOriginId(), toDoTask.getOriginSubPath(), status);
			}
		}
		
		return Response.ok().build();
	}
	
}