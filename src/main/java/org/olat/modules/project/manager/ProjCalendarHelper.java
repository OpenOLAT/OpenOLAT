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
package org.olat.modules.project.manager;

import java.util.Collection;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjCalendarHelper {
	
	private static final String APPOINTMENT_EXTERNAL_SOURCE = "project-appointment";

	private static final CalendarManagedFlag[] CAL_MANAGED_FLAGS = new CalendarManagedFlag[] { CalendarManagedFlag.all };
	
	@Autowired
	private CalendarManager calendarManager;
	
	public KalendarEvent toEvent(ProjAppointment appointment) {
		KalendarEvent event = new KalendarEvent(appointment.getEventId(), appointment.getRecurrenceId(),
				appointment.getSubject(), appointment.getStartDate(), appointment.getEndDate());
		toEvent(appointment, event);
		return event;
	}

	private void toEvent(ProjAppointment appointment, KalendarEvent event) {
		event.setExternalId(appointment.getIdentifier());
		event.setExternalSource(APPOINTMENT_EXTERNAL_SOURCE);
		event.setDescription(appointment.getDescription());
		event.setLocation(appointment.getLocation());
		event.setColor(appointment.getColor());
		event.setAllDayEvent(appointment.isAllDay());
		event.setRecurrenceRule(appointment.getRecurrenceRule());
		event.setRecurrenceExc(appointment.getRecurrenceExclusion());
	}

	public void createOrUpdateEvent(ProjAppointment appointment, Collection<Identity> set) {
		if (appointment.getStartDate() == null) {
			return;
		}
		
		set.forEach(identity -> createOrUpdateEvent(appointment, identity));
	}

	private void createOrUpdateEvent(ProjAppointment appointment, Identity identity) {
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
		for (KalendarEvent event : cal.getEvents()) {
			if (appointment.getIdentifier().equals(event.getExternalId())) {
				updateEvent(appointment, event);
				calendarManager.updateEventFrom(cal, event);
				return;
			}
		}
		
		KalendarEvent newEvent = createEvent(appointment);
		calendarManager.addEventTo(cal, newEvent);
	}
	
	private KalendarEvent createEvent(ProjAppointment appointment) {
		KalendarEvent event = toEvent(appointment);
		event.setExternalId(appointment.getIdentifier());
		event.setExternalSource(APPOINTMENT_EXTERNAL_SOURCE);
		addKalendarEventLinks(appointment, event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return event;
	}
	
	private void updateEvent(ProjAppointment appointment, KalendarEvent event) {
		toEvent(appointment, event);
		event.setSubject(appointment.getSubject());
		event.setBegin(appointment.getStartDate());
		event.setEnd(appointment.getEndDate());
		addKalendarEventLinks(appointment, event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
	}
	
	private void addKalendarEventLinks(ProjAppointment appointment, KalendarEvent event) {
		ProjProject project = appointment.getArtefact().getProject();
		String id = project.getKey().toString();
		String displayName = project.getTitle();
		String businessPath = ProjectBCFactory.getProjectUrl(project);
		KalendarEventLink link = new KalendarEventLink("project", id, displayName, businessPath, "o_icon_proj_project");
		event.getKalendarEventLinks().clear();
		event.getKalendarEventLinks().add(link);
	}
	
	public void deleteEvent(ProjAppointment appointment, Collection<Identity> identities) {
		identities.forEach(identity -> deleteEvent(appointment, identity));
	}
	
	public void deleteEvent(ProjAppointment appointment, Identity identity) {
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
		cal.getEvents().stream()
				.filter(event -> appointment.getIdentifier().equals(event.getExternalId()))
				.forEach(event -> calendarManager.removeEventFrom(cal, event));
	}

}
