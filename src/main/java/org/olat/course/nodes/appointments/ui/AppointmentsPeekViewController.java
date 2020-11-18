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
package org.olat.course.nodes.appointments.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.AppointmentsCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.ui.AppointmentsMainController;
import org.olat.modules.appointments.ui.AppointmentsUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsPeekViewController extends BasicController {
	
	private VelocityContainer mainVC;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;

	public AppointmentsPeekViewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			AppointmentsCourseNode courseNode) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AppointmentsMainController.class, getLocale(), getTranslator()));
		mainVC = createVelocityContainer("peekview");
		
		AppointmentWrapper appointment = null;
		if (userCourseEnv.isParticipant()) {
			appointment = loadParticpantAppointment(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
		} else if (userCourseEnv.isCoach()) {
			appointment = loadCoachAppointment(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
		}
		
		if (appointment != null) {
			mainVC.contextPut("appointment", appointment);
		} else {
			String role = getTranslatedRole(userCourseEnv);
			String noAppointments = translate("no.upcomming.appointments", new String[] { role } );
			mainVC.contextPut("noAppointments", noAppointments);
		}
		
		putInitialPanel(mainVC);
	}

	private AppointmentWrapper loadParticpantAppointment(RepositoryEntry courseEntry, String ident) {
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setEntry(courseEntry);
		params.setSubIdent(ident);
		params.setIdentity(getIdentity());
		params.setStatus(Status.confirmed);
		params.setFetchAppointments(true);
		params.setFetchTopics(true);
		List<Appointment> appointments = appointmentsService.getParticipations(params).stream()
				.map(Participation::getAppointment)
				.collect(Collectors.toList());
		Optional<Appointment> appointment = getNextAppointment(appointments);
		if (appointment.isPresent()) {
			return wrap(appointment.get());
		}
		return null;
	}

	private AppointmentWrapper loadCoachAppointment(RepositoryEntry courseEntry, String ident) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(courseEntry);
		params.setSubIdent(ident);
		params.setOrganizer(getIdentity());
		params.setStatus(Status.confirmed);
		params.setFetchTopic(true);
		List<Appointment> appointments = appointmentsService.getAppointments(params);
		Optional<Appointment> appointment = getNextAppointment(appointments);
		if (appointment.isPresent()) {
			return wrap(appointment.get());
		}
		return null;
	}

	private Optional<Appointment> getNextAppointment(List<Appointment> appointments) {
		Date now = new Date();
		Optional<Appointment> appointment = appointments.stream()
				.filter(a -> appointmentsService.isEndAfter(a, now))
				.sorted((a1, a2) -> a1.getStart().compareTo(a2.getStart()))
				.limit(1)
				.findFirst();
		return appointment;
	}

	private AppointmentWrapper wrap(Appointment appointment) {
		AppointmentWrapper wrapper = new AppointmentWrapper();
		
		wrapper.setTitle(appointment.getTopic().getTitle());
		wrapOrganizers(wrapper, appointment.getTopic());
		wrapAppointmentView(wrapper, appointment);
		
		return wrapper;
	}
	
	private void wrapOrganizers(AppointmentWrapper wrapper, Topic topic) {
		List<Organizer> organizers = appointmentsService.getOrganizers(topic);
		
		List<String> organizerNames = new ArrayList<>(organizers.size());
		for (Organizer organizer : organizers) {
			String name = userManager.getUserDisplayName(organizer.getIdentity().getKey());
			organizerNames.add(name);
		}
		organizerNames.sort(String.CASE_INSENSITIVE_ORDER);
		wrapper.setOrganizerNames(organizerNames);
	}
	
	private void wrapAppointmentView(AppointmentWrapper wrapper, Appointment appointment) {
		Locale locale = getLocale();
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		String date = null;
		String date2 = null;
		String time = null;
		
		boolean sameDay = DateUtils.isSameDay(begin, end);
		boolean sameTime = DateUtils.isSameTime(begin, end);
		String startDate = StringHelper.formatLocaleDateFull(begin.getTime(), locale);
		String startTime = StringHelper.formatLocaleTime(begin.getTime(), locale);
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), locale);
		String endTime = StringHelper.formatLocaleTime(end.getTime(), locale);
		if (sameDay) {
			StringBuilder timeSb = new StringBuilder();
			if (sameTime) {
				timeSb.append(translate("full.day"));
			} else {
				timeSb.append(startTime);
				timeSb.append(" - ");
				timeSb.append(endTime);
			}
			time = timeSb.toString();
		} else {
			StringBuilder dateSbShort1 = new StringBuilder();
			dateSbShort1.append(startDate);
			dateSbShort1.append(" ");
			dateSbShort1.append(startTime);
			dateSbShort1.append(" -");
			date = dateSbShort1.toString();
			StringBuilder dateSb2 = new StringBuilder();
			dateSb2.append(endDate);
			dateSb2.append(" ");
			dateSb2.append(endTime);
			date2 = dateSb2.toString();
		}
		
		wrapper.setDate(date);
		wrapper.setDate2(date2);
		wrapper.setTime(time);
		wrapper.setLocation(AppointmentsUIFactory.getDisplayLocation(getTranslator(), appointment));
		wrapper.setDetails(appointment.getDetails());
		
		DateComponentFactory.createDateComponentWithYear("day", appointment.getStart(), mainVC);
		
		wrapper.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
		wrapper.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
	}

	private String getTranslatedRole(UserCourseEnvironment userCourseEnv) {
		String i18nKey;
		if (userCourseEnv.isAdmin()) {
			i18nKey = "no.upcomming.appointments.owner";
		} else if (userCourseEnv.isCoach()) {
			i18nKey = "no.upcomming.appointments.coach";
		} else {
			i18nKey = "no.upcomming.appointments.participant";
		}
		return translate(i18nKey);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class AppointmentWrapper {
		
		private String title;
		private List<String> organizerNames;
		private List<String> participants;
		private String dayName;
		private String date;
		private String date2;
		private String time;
		private String location;
		private String details;
		private String translatedStatus;
		private String statusCSS;
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<String> getOrganizerNames() {
			return organizerNames;
		}
		
		public void setOrganizerNames(List<String> organizerNames) {
			this.organizerNames = organizerNames;
		}

		public List<String> getParticipants() {
			return participants;
		}

		public void setParticipants(List<String> participants) {
			this.participants = participants;
		}

		public String getDayName() {
			return dayName;
		}

		public void setDayName(String dayName) {
			this.dayName = dayName;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getDate2() {
			return date2;
		}

		public void setDate2(String date2) {
			this.date2 = date2;
		}

		public String getTime() {
			return time;
		}

		public void setTime(String time) {
			this.time = time;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public String getDetails() {
			return details;
		}

		public void setDetails(String details) {
			this.details = details;
		}

		public String getTranslatedStatus() {
			return translatedStatus;
		}

		public void setTranslatedStatus(String translatedStatus) {
			this.translatedStatus = translatedStatus;
		}

		public String getStatusCSS() {
			return statusCSS;
		}

		public void setStatusCSS(String statusCSS) {
			this.statusCSS = statusCSS;
		}
	}

}
