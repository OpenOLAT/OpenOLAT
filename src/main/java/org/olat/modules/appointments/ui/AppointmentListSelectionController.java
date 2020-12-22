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
package org.olat.modules.appointments.ui;

import static java.util.Collections.emptyList;
import static org.olat.modules.appointments.ui.AppointmentsUIFactory.isEndInFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;

/**
 * 
 * Initial date: 12 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentListSelectionController extends AppointmentListController {
	
	private final static List<String> FILTERS = Arrays.asList(
			AppointmentDataModel.FILTER_PARTICIPATED,
			AppointmentDataModel.FILTER_FUTURE);
	private final static List<String> FILTERS_FINDING_DEFAULT = Collections.emptyList();
	private final static List<String> FILTERS_ENROLLMENT_DEFAULT = FILTERS;

	protected AppointmentListSelectionController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl, topic, secCallback);
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
	protected boolean isParticipationVisible() {
		return topic.isParticipationVisible();
	}
	
	@Override
	protected List<String> getFilters() {
		return FILTERS;
	}

	@Override
	protected List<String> getDefaultFilters() {
		return Type.finding == topic.getType()? FILTERS_FINDING_DEFAULT: FILTERS_ENROLLMENT_DEFAULT;
	}

	@Override
	protected String getPersistedPreferencesId() {
		return "ap-appointment-selection";
	}

	@Override
	protected List<AppointmentRow> loadModel() {
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setTopic(topic);
		aParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentsService.getAppointments(aParams);
		
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setTopic(topic);
		pParams.setFetchAppointments(true);
		List<Participation> allPrticipations = appointmentsService.getParticipations(pParams);
		Map<Long, List<Participation>> appointmentKeyToParticipation = allPrticipations.stream()
				.collect(Collectors.groupingBy(p -> p.getAppointment().getKey()));
		boolean userHasNoConfirmedParticipation = !allPrticipations.stream()
				.filter(p -> p.getIdentity().getKey().equals(getIdentity().getKey()))
				.filter(p -> Appointment.Status.confirmed == p.getAppointment().getStatus())
				.findFirst()
				.isPresent();
		
		boolean noConfirmedAppointments = false;
		if (Type.finding == topic.getType()) {
			AppointmentSearchParams confirmedFindingsParams = new AppointmentSearchParams();
			confirmedFindingsParams.setTopic(topic);
			confirmedFindingsParams.setStatus(Status.confirmed);
			noConfirmedAppointments = appointmentsService.getAppointmentCount(confirmedFindingsParams) == 0;
		}
		
		Map<Long, List<BigBlueButtonRecordingReference>> appointmentKeyToRecordingReferences = appointmentsService.isBigBlueButtonEnabled()
				? appointmentsService.getBBBRecordingReferences(appointments)
				: Collections.emptyMap();
		
		Date now = new Date();
		List<AppointmentRow> rows = new ArrayList<>(appointments.size());
		for (Appointment appointment : appointments) {
			List<Participation> participations = appointmentKeyToParticipation.getOrDefault(appointment.getKey(), emptyList());
			List<BigBlueButtonRecordingReference> recordingReferences = appointmentKeyToRecordingReferences.getOrDefault(appointment.getKey(), emptyList());
			AppointmentRow row = createAppointmentRow(topic, appointment, participations, recordingReferences, userHasNoConfirmedParticipation, noConfirmedAppointments, now);
			if (row != null) {
				rows.add(row);
			}
		}
		return rows;
	}

	private AppointmentRow createAppointmentRow(Topic topic, Appointment appointment,
			List<Participation> participations, List<BigBlueButtonRecordingReference> recordingReferences,
			boolean userHasNoConfirmedParticipation, boolean noConfirmedAppointments, Date now) {
		Optional<Participation> myParticipation = participations.stream()
				.filter(p -> p.getIdentity().getKey().equals(getIdentity().getKey()))
				.findFirst();
		boolean selected = myParticipation.isPresent();
		boolean confirmedByCoach = !topic.isAutoConfirmation() && Status.confirmed == appointment.getStatus();
		Integer maxParticipations = appointment.getMaxParticipations();
		Integer freeParticipations = maxParticipations != null
				? maxParticipations.intValue() - participations.size()
				: null;
		boolean noFreePlace = freeParticipations != null && freeParticipations < 1;
		if (Type.finding != topic.getType() && !selected && (confirmedByCoach || noFreePlace)) {
			return null;
		}
		
		AppointmentRow row = new AppointmentRow(appointment);
		if (myParticipation.isPresent()) {
			row.setParticipation(myParticipation.get());
		}
		forgeAppointmentView(row, appointment);
		if (row.getParticipation() != null) {
			Participation participation = row.getParticipation();
			Identity identity = participation.getIdentity();
			Identity createdBy = participation.getCreatedBy();
			if (!identity.getKey().equals(createdBy.getKey())) {
				String createdByName = userManager.getUserDisplayName(createdBy.getKey());
				String createdByText = translate("participation.created.by", createdByName);
				createdByText = StringHelper.unescapeHtml(createdByText);
				String details = StringHelper.containsNonWhitespace(row.getDetails())
						? row.getDetails() + " " + createdByText
						: createdByText;
				row.setDetails(details);
			}
		}
	
		if (selected || confirmedByCoach) {
			row.setTranslatedStatus(translate("appointment.status." + appointment.getStatus().name()));
			row.setStatusCSS("o_ap_status_" + appointment.getStatus().name());
		}
		
		if (isParticipationVisible()) {
			forgeParticipants(row, participations);
		}
		
		Integer numberOfParticipations = Integer.valueOf(participations.size());
		row.setNumberOfParticipations(numberOfParticipations);
		
		if (Type.finding == topic.getType()) {
			if (noConfirmedAppointments && (AppointmentsUIFactory.isEndInFuture(appointment, now) || selected)) {
				forgeSelectionLink(row, selected);
			}
		} else if (topic.isMultiParticipation() || userHasNoConfirmedParticipation) {
			boolean selectable = false;
			if ((Appointment.Status.planned == appointment.getStatus() || (topic.isAutoConfirmation() && !selected)) 
					&& isEndInFuture(appointment, now)
					&& (freeParticipations == null || freeParticipations.intValue() > 0)) {
				selectable = true;
			}
			
			boolean unselectable = selected && Appointment.Status.planned == appointment.getStatus();
			if (selectable || unselectable) {
				forgeSelectionLink(row, selected);
				if (Appointment.Status.confirmed != appointment.getStatus()) {
					row.setFreeParticipations(freeParticipations);
				}
			}
		}
		
		String selectionCSS = "";
		if (selected) {
			if (Appointment.Status.planned == appointment.getStatus()) {
				selectionCSS = "o_ap_planned";
			} else {
				selectionCSS = "o_ap_confirmed";
			}
		}
		if (Type.finding == topic.getType() && Appointment.Status.confirmed == appointment.getStatus()) {
			selectionCSS = "o_ap_confirmed";
		}
		row.setSelectionCSS(selectionCSS);
		
		if (secCallback.canWatchRecording(organizers, participations)) {
			forgeRecordingReferencesLinks(row, recordingReferences);
		}
		
		return row;
	}

}
