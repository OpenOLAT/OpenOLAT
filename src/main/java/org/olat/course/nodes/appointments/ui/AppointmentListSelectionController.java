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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.Appointment.Status;
import org.olat.course.nodes.appointments.AppointmentSearchParams;
import org.olat.course.nodes.appointments.AppointmentsSecurityCallback;
import org.olat.course.nodes.appointments.Participation;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.Topic;

/**
 * 
 * Initial date: 12 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentListSelectionController extends AppointmentListController {

	protected AppointmentListSelectionController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback, Configuration config) {
		super(ureq, wControl, topic, secCallback, config);
	}

	@Override
	protected boolean canSelect() {
		return true;
	}

	@Override
	protected boolean canEdit() {
		return false;
	}

	@Override
	protected String getTableCssClass() {
		return "o_selection";
	}

	@Override
	protected String getPersistedPreferencesId() {
		return "ap-appointment-selection";
	}

	@Override
	protected List<AppointmentRow> loadModel() {
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setTopic(topic);
		List<Appointment> appointments = appointmentsService.getAppointments(aParams);
		
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setTopic(topic);
		Map<Long, List<Participation>> appointmentKeyToParticipation = appointmentsService
				.getParticipations(pParams).stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		appointments.sort((a1, a2) -> a1.getStart().compareTo(a2.getStart()));
		List<AppointmentRow> rows = new ArrayList<>(appointments.size());
		for (Appointment appointment : appointments) {
			List<Participation> participations = appointmentKeyToParticipation.getOrDefault(appointment.getKey(), emptyList());
			AppointmentRow row = getWrappedAppointment(appointment, participations);
			if (row != null) {
				rows.add(row);
			}
		}
		return rows;
	}

	private AppointmentRow getWrappedAppointment(Appointment appointment, List<Participation> participations) {
		Optional<Participation> myParticipation = participations.stream()
				.filter(p -> p.getIdentity().getKey().equals(getIdentity().getKey()))
				.findFirst();
		boolean selected = myParticipation.isPresent();
		boolean confirmed = Status.confirmed == appointment.getStatus();
		if (confirmed && !selected) {
			return null;
		}
		
		AppointmentRow row = new AppointmentRow(appointment);
		if (myParticipation.isPresent()) {
			row.setParticipation(myParticipation.get());
		}
		forgeAppointmentView(row, appointment);
	
		if (selected) {
			row.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
			row.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
		}
		
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
		row.setParticipants(participants);
		
		Integer numberOfParticipations = Integer.valueOf(participations.size());
		row.setNumberOfParticipations(numberOfParticipations);
		Integer maxParticipations = appointment.getMaxParticipations();
		Integer freeParticipations = maxParticipations != null
				? maxParticipations.intValue() - participations.size()
				: null;
		row.setFreeParticipations(freeParticipations);

		boolean selectable = false;
		if (Appointment.Status.planned == appointment.getStatus()) {
			selectable = freeParticipations == null // no limit
					|| freeParticipations.intValue() > 0;
		}
		
		boolean unselectable = selected && Appointment.Status.planned == appointment.getStatus();
		
		forgeSelectLink(row, selected, selectable, unselectable);
		
		return row;
	}

}
