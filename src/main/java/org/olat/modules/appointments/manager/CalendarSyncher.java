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
package org.olat.modules.appointments.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.nodes.appointments.ui.AppointmentsRunController;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicRef;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 11 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class CalendarSyncher {
	
	private static final CalendarManagedFlag[] CAL_MANAGED_FLAGS = new CalendarManagedFlag[] { CalendarManagedFlag.all };

	@Autowired
	private OrganizerDAO organizerDao;
	@Autowired
	private ParticipationDAO participationDao;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryManager repositoryManager;
	
	/**
	 * Sync the calendar events of all organizers and all participants.
	 *
	 * @param topic
	 * @param appointments
	 */
	void syncCalendars(TopicRef topic, Collection<Appointment> appointments) {
		organizerDao.loadOrganizers(topic).stream()
				.map(Organizer::getIdentity)
				.forEach(identity -> syncCalendar(appointments, identity));
	
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setAppointments(appointments);
		participationParams.setFetchIdentities(true);
		participationParams.setFetchUser(true);
		participationDao.loadParticipations(participationParams).stream()
				.map(Participation::getIdentity)
				.forEach(identity -> syncCalendar(appointments, identity));
	}
	
	void syncCalendar(Appointment appointment, Identity identity) {
		syncCalendar(Collections.singletonList(appointment), identity);
	}
		
	void syncCalendar(Collection<Appointment> appointments, Identity identity) {
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
		syncCalendar(appointments, identity, cal);
	}
	
	private void syncCalendar(Collection<Appointment> appointments, Identity identity, Kalendar cal) {
		appointments.stream().forEach(appointment -> syncEvent(appointment, identity, cal));
	}
	
	private void syncEvent(Appointment appointment, Identity identity, Kalendar cal) {
		String eventExternalId = generateExternalId(appointment);
		
		for (KalendarEvent event : cal.getEvents()) {
			if (eventExternalId.equals(event.getExternalId())) {
				updateEvent(appointment, event, identity);
				calendarManager.updateEventFrom(cal, event);
				return;
			}
		}
		
		// create new event if no existing
		KalendarEvent newEvent = createEvent(appointment, identity);
		calendarManager.addEventTo(cal, newEvent);
	}
	
	/**
	 * Unsync the calendar events of all organizers and all participants.
	 *
	 * @param topic
	 * @param appointments
	 */
	void unsyncCalendars(TopicRef topic, Collection<Appointment> appointments) {
		organizerDao.loadOrganizers(topic).stream()
				.map(Organizer::getIdentity)
				.forEach(identity -> unsyncCalendar(appointments, identity));
	
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setAppointments(appointments);
		participationParams.setFetchIdentities(true);
		participationDao.loadParticipations(participationParams).stream()
				.map(Participation::getIdentity)
				.forEach(identity -> unsyncCalendar(appointments, identity));
	}
	
	void unsyncCalendar(Appointment appointment, Identity identity) {
		unsyncCalendar(Collections.singletonList(appointment), identity);
	}
	
	void unsyncCalendar(Collection<Appointment> appointments, Identity identity) {
		Kalendar cal = calendarManager.getCalendar(CalendarManager.TYPE_USER, identity.getName());
		unsyncCalendar(appointments, cal);
	}
	
	private void unsyncCalendar(Collection<Appointment> appointments, Kalendar cal) {
		appointments.stream().forEach(appointment -> unsyncEvent(appointment, cal));
	}
	
	private void unsyncEvent(Appointment appointment, Kalendar cal) {
		String externalId = generateExternalId(appointment);
		cal.getEvents().stream()
				.filter(event -> externalId.equals(event.getExternalId()))
				.forEach(event -> calendarManager.removeEventFrom(cal, event));
	}
	
	private KalendarEvent createEvent(Appointment appointement, Identity identity) {
		String eventId = UUID.randomUUID().toString();
		String subject = getSubject(appointement, identity);
		KalendarEvent event = new KalendarEvent(eventId, null, subject, appointement.getStart(), appointement.getEnd());
		event.setExternalId(generateExternalId(appointement));
		event.setLocation(appointement.getLocation());
		updateDates(appointement, event);
		updateEventDescription(appointement, event);
		addKalendarEventLinks(appointement.getTopic(), event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
		return event;
	}
	
	private void updateEvent(Appointment appointement, KalendarEvent event, Identity identity) {
		event.setSubject(getSubject(appointement, identity));
		event.setLocation(appointement.getLocation());
		updateDates(appointement, event);
		updateEventDescription(appointement, event);
		addKalendarEventLinks(appointement.getTopic(), event);
		event.setManagedFlags(CAL_MANAGED_FLAGS);
	}

	private void updateDates(Appointment appointement, KalendarEvent event) {
		if (DateUtils.isSameDate(appointement.getStart(), appointement.getEnd())) {
			event.setAllDayEvent(true);
			event.setBegin(DateUtils.setTime(appointement.getStart(), 0, 0, 0));
			event.setEnd(DateUtils.setTime(appointement.getEnd(), 23,59,59));
		} else {
			event.setAllDayEvent(false);
			event.setBegin(appointement.getStart());
			event.setEnd(appointement.getEnd());
		}
	}
	
	private String getSubject(Appointment appointment, Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append(appointment.getTopic().getTitle());
		if (Appointment.Status.planned == appointment.getStatus()) {
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
			Translator translator = Util.createPackageTranslator(AppointmentsRunController.class, locale);
			sb.append(" (");
			sb.append(translator.translate("unconfirmed"));
			sb.append(")");
		}
		return sb.toString();
	}
	
	private void updateEventDescription(Appointment appointement, KalendarEvent event) {
		StringBuilder descr = new StringBuilder();
		Topic topic = appointement.getTopic();
		if (StringHelper.containsNonWhitespace(topic.getDescription())) {
			descr.append(topic.getDescription());
		}
		if (StringHelper.containsNonWhitespace(appointement.getDetails())) {
			if (descr.length() > 0) descr.append("\n");
			descr.append(appointement.getDetails());
		}
		event.setDescription(descr.toString());
	}
	
	private void addKalendarEventLinks(Topic topic, KalendarEvent event) {
		List<KalendarEventLink> kalendarEventLinks = event.getKalendarEventLinks();
		String id = topic.getKey().toString();
		String displayName = repositoryManager.lookupDisplayName(topic.getEntry().getKey());
		String businessPath = getBusinessPath(topic);
		KalendarEventLink link = new KalendarEventLink("appointments", id, displayName, businessPath, "o_CourseModule_icon");
		kalendarEventLinks.clear();
		kalendarEventLinks.add(link);
	}
	
	private String getBusinessPath(Topic topic) {
		String businessPath;
		if (topic.getEntry() != null) {
			businessPath = "[RepositoryEntry:" + topic.getEntry().getKey() + "]";
			if (StringHelper.containsNonWhitespace(topic.getSubIdent())) {
				businessPath += "[CourseNode:" + topic.getSubIdent() + "]";
			}
		} else {
			businessPath = "[RepositoryEntry:0]";
		}
		return BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
	}
	
	private String generateExternalId(Appointment appointment) {
		StringBuilder sb = new StringBuilder();
		sb.append("appointment-").append(appointment.getKey());
		return sb.toString();
	}

}
