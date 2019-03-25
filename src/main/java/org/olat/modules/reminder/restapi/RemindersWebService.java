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
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.manager.ReminderRulesXStream;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemindersWebService {
	
	private RepositoryEntry entry;
	
	@Autowired	
	private ReminderService reminderService;
	
	public RemindersWebService(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	/**
	 * Return the reminders of the specified course or repository entry.
	 * @response.representation.200.qname {http://www.example.com}reminderVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc An array of lecture blocks
	 * @response.representation.200.example {@link org.olat.modules.reminder.restapi.Examples#SAMPLE_REMINDERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The resource not found
	 * @param httpRequest The HTTP request
	 * @return The reminders
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getReminders(@Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
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
	 * @response.representation.200.qname {http://www.example.com}reminderVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The updated reminder
	 * @response.representation.200.example {@link org.olat.modules.reminder.restapi.Examples#SAMPLE_REMINDERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or repository entry not found
	 * @param reminder The reminder
	 * @param httpRequest The HTTP request
	 * @return It returns the updated / created reminder.
	 */
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putReminder(ReminderVO reminder, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Reminder updatedReminder = saveReminder(reminder, httpRequest);
		return Response.ok(ReminderVO.valueOf(updatedReminder, entry.getKey())).build();
	}
	
	/**
	 * Create or update a reminder.
	 * 
	 * @response.representation.200.qname {http://www.example.com}reminderVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The updated reminder
	 * @response.representation.200.example {@link org.olat.modules.lecture.restapi.Examples#SAMPLE_REMINDERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or repository entry not found
	 * @param reminder The reminder
	 * @param httpRequest The HTTP request
	 * @return It returns the updated / created reminder.
	 */
	@POST
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postReminder(ReminderVO reminder, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
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
	 * @response.representation.200.doc Reminder deleted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course or repository entry not found
	 * @param reminderKey The reminder primary key
	 * @param httpRequest The HTTP request
	 * @return Nothing
	 */
	@DELETE
	@Path("{reminderKey}")
	public Response deleteReminder(@PathParam("reminderKey") Long reminderKey, @Context HttpServletRequest httpRequest) {
		Roles roles = getRoles(httpRequest);
		if(!roles.isOLATAdmin()) {
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
