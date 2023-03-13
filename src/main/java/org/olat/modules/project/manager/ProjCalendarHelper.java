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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;

/**
 * 
 * Initial date: 15 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjCalendarHelper {

	private static final Logger log = Tracing.createLoggerFor(ProjCalendarHelper.class);
	
	private static final String APPOINTMENT_EXTERNAL_SOURCE = "project-appointment";
	private static final String MILESTONE_EXTERNAL_SOURCE = "project-milestone";

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
		String businessPath = ProjectBCFactory.getAppointmentUrl(appointment);
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
	
	@SuppressWarnings("deprecation")
	public String getExclusionRecurrenceRule(String recurrenceRule, Date exclusionDate) {
		try {
			Recur recur = new Recur(recurrenceRule);
			recur.setUntil(CalendarUtils.createDate(exclusionDate));
			RRule rrule = new RRule(recur);
			return rrule.getValue();
		} catch (ParseException e) {
			log.debug("", e);
		}
		return null;
	}
	
	public String getUpdatedOccurenceId(String appointmentRecurrendeId, boolean allDay, int beginDiff) {
		try {
			RecurrenceId currentReccurenceId = new RecurrenceId(appointmentRecurrendeId);
			Date currentRecurrenceDate = currentReccurenceId.getDate();
			java.util.Calendar calc = java.util.Calendar.getInstance();
			calc.clear();
			calc.setTime(currentRecurrenceDate);
			if (beginDiff > 0) {
				calc.add(java.util.Calendar.MILLISECOND, beginDiff);
			}
			
			Date newRecurrenceDate = calc.getTime();
			
			RecurrenceId newRecurrenceId;
			if(allDay) {
				newRecurrenceId = new RecurrenceId((CalendarUtils.createDate(newRecurrenceDate)));
			} else {
				newRecurrenceId = new RecurrenceId(CalendarUtils.formatRecurrenceDate(newRecurrenceDate, false));
			}
			
			return newRecurrenceId.getValue();
			
		} catch (ParseException e) {
			log.error("", e);
		}
		
		return null;
	}
	
	public KalendarEvent toEvent(ProjMilestone milestone) {
		KalendarEvent event = new KalendarEvent(milestone.getIdentifier(), null, milestone.getSubject(),
				milestone.getDueDate(), milestone.getDueDate());
		toEvent(milestone, event);
		return event;
	}

	private void toEvent(ProjMilestone milestone, KalendarEvent event) {
		event.setExternalId(milestone.getIdentifier());
		event.setExternalSource(MILESTONE_EXTERNAL_SOURCE);
		event.setDescription(milestone.getDescription());
		event.setColor(milestone.getColor());
		event.setAllDayEvent(true);
	}

	public void createOrUpdateEvent(ProjMilestone milestone, Collection<Identity> set) {
		if (milestone.getDueDate() == null) {
			return;
		}
		
		set.forEach(identity -> createOrUpdateEvent(milestone, identity));
	}

	private void createOrUpdateEvent(ProjMilestone milestone, Identity identity) {
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
		for (KalendarEvent event : cal.getEvents()) {
			if (milestone.getIdentifier().equals(event.getExternalId())) {
				updateEvent(milestone, event);
				calendarManager.updateEventFrom(cal, event);
				return;
			}
		}
		
		KalendarEvent newEvent = createEvent(milestone);
		calendarManager.addEventTo(cal, newEvent);
	}
	
	private KalendarEvent createEvent(ProjMilestone milestone) {
		KalendarEvent event = toEvent(milestone);
		addKalendarEventLinks(milestone, event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return event;
	}
	
	private void updateEvent(ProjMilestone milestone, KalendarEvent event) {
		toEvent(milestone, event);
		event.setSubject(milestone.getSubject());
		event.setBegin(milestone.getDueDate());
		addKalendarEventLinks(milestone, event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
	}
	
	private void addKalendarEventLinks(ProjMilestone milestone, KalendarEvent event) {
		ProjProject project = milestone.getArtefact().getProject();
		String id = project.getKey().toString();
		String displayName = project.getTitle();
		String businessPath = ProjectBCFactory.getMilestoneUrl(milestone);
		KalendarEventLink link = new KalendarEventLink("project", id, displayName, businessPath, "o_icon_proj_project");
		event.getKalendarEventLinks().clear();
		event.getKalendarEventLinks().add(link);
	}
	
	public void deleteEvent(ProjMilestone milestone, Collection<Identity> identities) {
		identities.forEach(identity -> deleteEvent(milestone, identity));
	}
	
	public void deleteEvent(ProjMilestone milestone, Identity identity) {
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
		cal.getEvents().stream()
				.filter(event -> milestone.getIdentifier().equals(event.getExternalId()))
				.forEach(event -> calendarManager.removeEventFrom(cal, event));
	}

}
