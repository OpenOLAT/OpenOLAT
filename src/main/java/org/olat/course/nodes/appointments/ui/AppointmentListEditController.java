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
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.olat.course.nodes.appointments.Topic.Type;

/**
 * 
 * Initial date: 12 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentListEditController extends AppointmentListController {
	
	private final static List<String> FILTERS = singletonList(AppointmentDataModel.FILTER_FUTURE);
	private final static List<String> FILTERS_DEFAULT = singletonList(AppointmentDataModel.FILTER_ALL);

	protected AppointmentListEditController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl, topic, secCallback);
	}

	@Override
	protected boolean canSelect() {
		return false;
	}

	@Override
	protected boolean canEdit() {
		return true;
	}

	@Override
	protected String getTableCssClass() {
		return "o_edit";
	}

	@Override
	protected List<String> getFilters() {
		return FILTERS;
	}

	@Override
	protected List<String> getDefaultFilters() {
		return FILTERS_DEFAULT;
	}

	@Override
	protected String getPersistedPreferencesId() {
		return "ap-appointment-edit";
	}
	
	@Override
	protected List<AppointmentRow> loadModel() {
		AppointmentSearchParams searchParams = new AppointmentSearchParams();
		searchParams.setTopic(topic);
		searchParams.setFetchTopic(true);
		
		List<Appointment> appointments = appointmentsService.getAppointments(searchParams);
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setAppointments(appointments);
		Map<Long, List<Participation>> appointmentKeyToParticipations = appointmentsService
				.getParticipations(pParams).stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		
		boolean anyConfirmed = appointments.stream()
				.anyMatch(a -> Status.confirmed == a.getStatus());
		
		List<AppointmentRow> rows = new ArrayList<>(appointments.size());
		for (Appointment appointment : appointments) {
			List<Participation> participations = appointmentKeyToParticipations.getOrDefault(appointment.getKey(), emptyList());
			AppointmentRow row = createRow(appointment, participations, !anyConfirmed);
			if (row != null) {
				rows.add(row);
			}
		}
		return rows;
	}
	
	private AppointmentRow createRow(Appointment appointment, List<Participation> participations, boolean noAppointmentConfirmed) {
		AppointmentRow row = new AppointmentRow(appointment);
		
		forgeAppointmentView(row, appointment);
		
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
		
		boolean showStatus = Type.finding == topic.getType()
				? Status.confirmed == appointment.getStatus()
				: participations.size() > 0;
		if (showStatus) {
			row.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
			row.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
		}
		
		boolean removeUser = Type.finding == topic.getType()
				? participations.size() > 0 && Status.confirmed == appointment.getStatus()
				: participations.size() > 0;
		if (removeUser) {
			forgeRemoveUserLink(row);
		}
		
		if (Type.finding == topic.getType()) {
			if (noAppointmentConfirmed) {
				forgeConfirmLink(row, true);
			} else if (Appointment.Status.confirmed == appointment.getStatus()) {
				forgeConfirmLink(row, false);
			}
		} else if (!topic.isAutoConfirmation()) {
			boolean confirmable = Type.finding == topic.getType()
					? Appointment.Status.planned == appointment.getStatus()
					: Appointment.Status.planned == appointment.getStatus() && participations.size() > 0;
			boolean unconfirmable = Appointment.Status.confirmed == appointment.getStatus();
			if (confirmable || unconfirmable) {
				forgeConfirmLink(row, confirmable);
			}
		}
		if (freeParticipations == null || freeParticipations.intValue() > 0) {
			forgeAddUserLink(row);
		}
		forgeDeleteLink(row);
		forgeEditLink(row);
		
		return row;
	}

}
