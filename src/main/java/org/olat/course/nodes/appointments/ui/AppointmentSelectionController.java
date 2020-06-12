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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.Appointment.Status;
import org.olat.course.nodes.appointments.AppointmentSearchParams;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Participation;
import org.olat.course.nodes.appointments.ParticipationResult;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.Topic;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentSelectionController extends BasicController {

	private static final String CMD_SELECT = "select";

	private final VelocityContainer mainVC;

	private DialogBoxController confirmParticipationCrtl;

	private final Topic topic;
	private final Configuration config;
	private List<AppointmentWrapper> appointmentWrappers;
	private int counter;
	
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private UserManager userManager;

	public AppointmentSelectionController(UserRequest ureq, WindowControl wControl, Topic topic, Configuration config) {
		super(ureq, wControl);
		this.topic = topic;
		this.config = config;
		
		mainVC = createVelocityContainer("appointment_selection2");
		refresh();
		putInitialPanel(mainVC);
	}
	
	private void refresh() {
		mainVC.clear();
		appointmentWrappers = loadAppointmentsWrappers();
		mainVC.contextPut("appointments", appointmentWrappers);
	}

	private List<AppointmentWrapper> loadAppointmentsWrappers() {
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setTopic(topic);
		List<Appointment> appointments = appointmentsService.getAppointments(aParams);
		
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setTopic(topic);
		Map<Long, List<Participation>> appointmentKeyToParticipation = appointmentsService
				.getParticipations(pParams).stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		appointments.sort((a1, a2) -> a1.getStart().compareTo(a2.getStart()));
		List<AppointmentWrapper> wrappers = new ArrayList<>(appointments.size());
		for (Appointment appointment : appointments) {
			List<Participation> participations = appointmentKeyToParticipation.getOrDefault(appointment.getKey(), emptyList());
			AppointmentWrapper wrapper = getWrappedAppointment(appointment, participations);
			if (wrapper != null) {
				wrappers.add(wrapper);
			}
		}
		return wrappers;
	}

	private AppointmentWrapper getWrappedAppointment(Appointment appointment, List<Participation> participations) {
		Optional<Participation> myParticipation = participations.stream()
				.filter(p -> p.getIdentity().getKey().equals(getIdentity().getKey()))
				.findFirst();
		boolean selected = myParticipation.isPresent();
		boolean confirmed = Status.confirmed == appointment.getStatus();
		if (confirmed && !selected) {
			return null;
		}

		AppointmentWrapper wrapper = new AppointmentWrapper(appointment);
		if (myParticipation.isPresent()) {
			wrapper.setParticipation(myParticipation.get());
		}

		Locale locale = getLocale();
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		String date = null;
		String dateLong = null;
		String dateShort1 = null;
		String dateShort2 = null;
		String time = null;

		boolean sameDay = DateUtils.isSameDay(begin, end);
		boolean sameTime = org.olat.core.util.DateUtils.isSameTime(begin, end);
		String startDate = StringHelper.formatLocaleDateFull(begin.getTime(), locale);
		String startTime = StringHelper.formatLocaleTime(begin.getTime(), locale);
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), locale);
		String endTime = StringHelper.formatLocaleTime(end.getTime(), locale);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(startDate);
			date = dateSb.toString();
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
			StringBuilder dateSbLong = new StringBuilder();
			dateSbLong.append(startDate);
			dateSbLong.append(" ");
			dateSbLong.append(startTime);
			dateSbLong.append(" - ");
			dateSbLong.append(endDate);
			dateSbLong.append(" ");
			dateSbLong.append(endTime);
			dateLong = dateSbLong.toString();
			StringBuilder dateSbShort1 = new StringBuilder();
			dateSbShort1.append(startDate);
			dateSbShort1.append(" ");
			dateSbShort1.append(startTime);
			dateSbShort1.append(" -");
			dateShort1 = dateSbShort1.toString();
			StringBuilder dateSbShort2 = new StringBuilder();
			dateSbShort2.append(endDate);
			dateSbShort2.append(" ");
			dateSbShort2.append(endTime);
			dateShort2 = dateSbShort2.toString();
		}
		
		wrapDay(wrapper, appointment.getStart());
		wrapper.setDate(date);
		wrapper.setDateLong(dateLong);
		wrapper.setDateShort1(dateShort1);
		wrapper.setDateShort2(dateShort2);
		wrapper.setTime(time);
		wrapper.setLocation(appointment.getLocation());
		wrapper.setDetails(appointment.getDetails());
	
		if (selected) {
			wrapper.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
			wrapper.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
		}
		
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
		wrapper.setParticipants(participants);
		
		boolean selectable = false;
		if (Appointment.Status.planned == appointment.getStatus()) {
			Integer maxParticipations = appointment.getMaxParticipations();
			Integer freeParticipations = maxParticipations != null
					? maxParticipations.intValue() - participations.size()
					: null;
			wrapper.setMaxParticipations(maxParticipations);
			wrapper.setFreeParticipations(freeParticipations);
			
			selectable = freeParticipations == null // no limit
					|| freeParticipations.intValue() > 0;
		}
		
		boolean unselectable = selected && Appointment.Status.planned == appointment.getStatus();
		
		wrapSelect(wrapper, selected, selectable, unselectable);
		
		return wrapper;
	}

	private void wrapDay(AppointmentWrapper wrapper, Date date) {
		String dayElName = "day_" + counter++;
		DateComponentFactory.createDateComponentWithYear(dayElName, date, mainVC);
		wrapper.setDayElName(dayElName);
	}

	private void wrapSelect(AppointmentWrapper wrapper, boolean selected, boolean selectable, boolean unselectable) {
		String selectionCSS;
		if (selected && unselectable) {
			selectionCSS = "o_ap_planned";
		} else if (selected) {
			selectionCSS = "o_ap_confirmed";
		} else {
			selectionCSS = "o_ap_selectable";
		}
		wrapper.setSelectionCSS(selectionCSS);
		
		boolean enabled = selectable || unselectable;
		boolean visible = selectable || unselectable || selected;
		String i18n = selected? "appointment.selected": "appointment.select";
		Link selectLink = LinkFactory.createCustomLink("select" + counter++, CMD_SELECT, i18n, Link.LINK, mainVC, this);
		selectLink.setUserObject(wrapper);
		if (selected) {
			selectLink.setIconLeftCSS("o_icon o_icon_lg o_icon_selected");
		} else {
			selectLink.setIconLeftCSS("o_icon o_icon_lg o_icon_unselected");
		}
		selectLink.setEnabled(enabled);
		selectLink.setVisible(visible);
		wrapper.setSelectLinkName(selectLink.getComponentName());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if (CMD_SELECT.equals(cmd)) {
				AppointmentWrapper wrapper = (AppointmentWrapper)link.getUserObject();
				doToggleParticipation(ureq, wrapper);
				refresh();
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmParticipationCrtl) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				Appointment appointment = (Appointment)confirmParticipationCrtl.getUserObject();
				doCreateParticipation(appointment);
				refresh();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doToggleParticipation(UserRequest ureq, AppointmentWrapper wrapper) {
		if (wrapper.getParticipation() == null) {
			if (config.isConfirmation()) {
				doCreateParticipation(wrapper.getAppointment());
			} else {
				doSelfConfirmParticipation(ureq, wrapper.getAppointment());
			}
		} else {
			appointmentsService.deleteParticipation(wrapper.getParticipation());
		}
	}

	private void doSelfConfirmParticipation(UserRequest ureq, Appointment appointment) {
		String title = translate("confirm.participation.self.title");
		String text = translate("confirm.participation.self");
		confirmParticipationCrtl = activateYesNoDialog(ureq, title, text, confirmParticipationCrtl);
		confirmParticipationCrtl.setUserObject(appointment);
	}

	private void doCreateParticipation(Appointment appointment) {
		ParticipationResult participationResult = appointmentsService.createParticipation(appointment, getIdentity(),
				config.isMultiParticipations(), !config.isConfirmation());
		if (ParticipationResult.Status.ok != participationResult.getStatus()) {
			showWarning("participation.not.created");
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public static final class AppointmentWrapper {

		private final Appointment appointment;
		private List<String> participants;
		private Participation participation;
		private String date;
		private String dateLong;
		private String dateShort1;
		private String dateShort2;
		private String time;
		private String location;
		private String details;
		private String translatedStatus;
		private String statusCSS;
		private Integer freeParticipations;
		private Integer maxParticipations;
		private String dayElName;
		private String selectionCSS;
		private String selectLinkName;

		public AppointmentWrapper(Appointment appointment) {
			this.appointment = appointment;
		}

		public Appointment getAppointment() {
			return appointment;
		}

		public List<String> getParticipants() {
			return participants;
		}

		public void setParticipants(List<String> participants) {
			this.participants = participants;
		}

		public Participation getParticipation() {
			return participation;
		}

		public void setParticipation(Participation participation) {
			this.participation = participation;
		}
		
		public boolean isSelected() {
			return participation != null;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getDateLong() {
			return dateLong;
		}

		public void setDateLong(String dateLong) {
			this.dateLong = dateLong;
		}

		public String getDateShort1() {
			return dateShort1;
		}

		public void setDateShort1(String dateShort1) {
			this.dateShort1 = dateShort1;
		}

		public String getDateShort2() {
			return dateShort2;
		}

		public void setDateShort2(String dateShort2) {
			this.dateShort2 = dateShort2;
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

		public Integer getFreeParticipations() {
			return freeParticipations;
		}

		public void setFreeParticipations(Integer freeParticipations) {
			this.freeParticipations = freeParticipations;
		}

		public Integer getMaxParticipations() {
			return maxParticipations;
		}

		public void setMaxParticipations(Integer maxParticipations) {
			this.maxParticipations = maxParticipations;
		}

		public String getDayElName() {
			return dayElName;
		}

		public void setDayElName(String dayElName) {
			this.dayElName = dayElName;
		}

		public String getSelectionCSS() {
			return selectionCSS;
		}

		public void setSelectionCSS(String selectionCSS) {
			this.selectionCSS = selectionCSS;
		}

		public String getSelectLinkName() {
			return selectLinkName;
		}

		public void setSelectLinkName(String selectLinkName) {
			this.selectLinkName = selectLinkName;
		}
		
	}

}
