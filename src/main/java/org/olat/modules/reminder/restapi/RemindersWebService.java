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
package org.olat.modules.reminder.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;

import java.util.ArrayList;
import java.util.List;

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

import org.olat.core.id.Identity;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.manager.ReminderRulesXStream;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemindersWebService {
	
	private RepositoryEntry entry;
	private final boolean administrator;
	
	@Autowired	
	private ReminderService reminderService;
	
	public RemindersWebService(RepositoryEntry entry, boolean administrator) {
		this.entry = entry;
		this.administrator = administrator;
	}
	
	/**
	 * Return the reminders of the specified course or repository entry.
	 * 
	 * @return The reminders
	 */
	@GET
	@Operation(summary = "Return the reminders", description = "Return the reminders of the specified course or repository entry")
	@ApiResponse(responseCode = "200", description = "An array of lecture blocks",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReminderVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = ReminderVO.class)))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The resource not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getReminders() {
		if(!administrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<Reminder> reminders = reminderService.getReminders(entry);
		List<ReminderVO> voList = new ArrayList<>(reminders.size());
		for(Reminder reminder:reminders) {
			voList.add(ReminderVO.valueOf(reminder, entry.getKey()));
		}
		ReminderVO[] voes = voList.toArray(new ReminderVO[voList.size()]);
		return Response.ok(voes).build();
	}
	
	/**
	 * Create or update a reminder.
	 * 
	 * @param reminder The reminder
	 * @param httpRequest The HTTP request
	 * @return It returns the updated / created reminder.
	 */
	@PUT
	@Operation(summary = "Create or update a reminder", description = "Create or update a reminder")
	@ApiResponse(responseCode = "200", description = "The updated reminder", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ReminderVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ReminderVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or repository entry not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putReminder(ReminderVO reminder, @Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Reminder updatedReminder = saveReminder(reminder, httpRequest);
		return Response.ok(ReminderVO.valueOf(updatedReminder, entry.getKey())).build();
	}
	
	/**
	 * Create or update a reminder.
	 * 
	 * @param reminder The reminder
	 * @param httpRequest The HTTP request
	 * @return It returns the updated / created reminder.
	 */
	@POST
	@Operation(summary = "Create or update a reminder", description = "Create or update a reminder")
	@ApiResponse(responseCode = "200", description = "The updated reminder", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = ReminderVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = ReminderVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or repository entry not found")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postReminder(ReminderVO reminder, @Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Reminder updatedReminder = saveReminder(reminder, httpRequest);
		return Response.ok(ReminderVO.valueOf(updatedReminder, entry.getKey())).build();
	}

	private Reminder saveReminder(ReminderVO reminderVo, HttpServletRequest httpRequest) {
		Identity creator = getIdentity(httpRequest);
		
		Reminder reminder;
		if(reminderVo.getKey() == null) {
			reminder = reminderService.createReminder(entry, creator);
		} else {
			reminder = reminderService.loadByKey(reminderVo.getKey());
		}

		reminder.setDescription(reminderVo.getDescription());
		reminder.setEmailSubject(reminderVo.getEmailSubject());
		reminder.setEmailBody(reminderVo.getEmailBody());
		
		if(reminderVo.getRules() != null && !reminderVo.getRules().isEmpty()) {
			ReminderRules rules = new ReminderRules();
			for(ReminderRuleVO ruleVo:reminderVo.getRules()) {
				ReminderRuleImpl rule = new ReminderRuleImpl();
				rule.setType(ruleVo.getType());
				rule.setLeftOperand(ruleVo.getLeftOperand());
				rule.setOperator(ruleVo.getOperator());
				rule.setRightOperand(ruleVo.getRightOperand());
				rule.setRightUnit(ruleVo.getRightUnit());
				rules.getRules().add(rule);
			}
			String configuration = ReminderRulesXStream.toXML(rules);
			reminder.setConfiguration(configuration);
		}
		return reminderService.save(reminder);
	}
	
	/**
	 * Delete a specific reminder.
	 * 
	 * @param reminderKey The reminder primary key
	 * @return Nothing
	 */
	@DELETE
	@Path("{reminderKey}")
	@Operation(summary = "Delete a specific reminder", description = "Delete a specific reminder")
	@ApiResponse(responseCode = "200", description = "Reminder deleted")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course or repository entry not found")
	public Response deleteReminder(@PathParam("reminderKey") Long reminderKey) {
		if(!administrator) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Reminder reminder = reminderService.loadByKey(reminderKey);
		if(reminder == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		reminderService.delete(reminder);
		return Response.ok().build();
	}
}
