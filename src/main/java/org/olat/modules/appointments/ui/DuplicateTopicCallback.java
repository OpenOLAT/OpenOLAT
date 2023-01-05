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

import java.util.Collection;
import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonUIHelper;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 19 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DuplicateTopicCallback implements StepRunnerCallback {

	private static final String DUPLICATION_CONTEXT = "duplicationContext";
	
	private Identity executor;
	private AppointmentsService appointmentsService;

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		executor = ureq.getIdentity();
		DuplicationContext context = getDuplicationContext(runContext);
		
		create(context);

		return StepsMainRunController.DONE_MODIFIED;
	}

	void create(DuplicationContext context) {
		Topic topic = createTopic(context.getEntry(), context.getSubIdent(), context.getTopic());
		if (context.getOrganizers() != null) {
			getAppointmentsService().updateOrganizers(topic, context.getOrganizers());
		}
		if (context.getAppointments() != null) {
			context.getAppointments().forEach(input -> createAppointment(topic, input));
		}
	}

	private Topic createTopic(RepositoryEntry entry, String subIdent, TopicLight topicLight) {
		Topic topic = getAppointmentsService().createTopic(entry, subIdent);
		topic.setAutoConfirmation(topicLight.isAutoConfirmation());
		topic.setDescription(topicLight.getDescription());
		topic.setMultiParticipation(topicLight.isMultiParticipation());
		topic.setParticipationVisible(topicLight.isParticipationVisible());
		topic.setTitle(topicLight.getTitle());
		topic.setType(topicLight.getType());
		return getAppointmentsService().updateTopic(topic);
	}

	protected void createAppointment(Topic topic, AppointmentInput appointmentInput) {
		Appointment appointment = getAppointmentsService().createUnsavedAppointment(topic);
		appointment.setDetails(appointmentInput.getAppointment().getDetails());
		appointment.setEnd(appointmentInput.getEnd());
		appointment.setLocation(appointmentInput.getAppointment().getLocation());
		Integer maxParticipations = Type.finding == topic.getType()
				? null
				: appointmentInput.getAppointment().getMaxParticipations();
		appointment.setMaxParticipations(maxParticipations);
		appointment.setStart(appointmentInput.getStart());
		appointment = getAppointmentsService().saveAppointment(appointment);
		
		if (appointmentInput.getAppointment().getBBBMeeting() != null) {
			addBBBMeeting(appointment, appointmentInput.getAppointment().getBBBMeeting());
		} else if (appointmentInput.getAppointment().getTeamsMeeting() != null) {
			addTeamsMeeting(appointment, appointmentInput.getAppointment().getTeamsMeeting());
		}
	}
	
	private void addBBBMeeting(Appointment appointment, BigBlueButtonMeeting meetingTemplate) {
		if (!isSlotAvailable(appointment, meetingTemplate)) {
			return;
		}
		
		appointment = getAppointmentsService().addBBBMeeting(appointment, executor);
		BigBlueButtonMeeting meeting = appointment.getBBBMeeting();
		
		meeting.setMainPresenter(getAppointmentsService().getFormattedOrganizers(appointment.getTopic()));
		meeting.setWelcome(meetingTemplate.getWelcome());
		meeting.setTemplate(meetingTemplate.getTemplate());
		meeting.setPermanent(meetingTemplate.isPermanent());
		meeting.setLeadTime(meetingTemplate.getLeadTime());
		meeting.setFollowupTime(meetingTemplate.getFollowupTime());
		meeting.setMeetingLayout(meetingTemplate.getMeetingLayout());
		meeting.setRecord(meetingTemplate.getRecord());
	}
	
	private boolean isSlotAvailable(Appointment appointment, BigBlueButtonMeeting meeting) {
		return BigBlueButtonUIHelper.validateSlot(null, meeting.getTemplate(), appointment.getStart(),
				appointment.getEnd(), meeting.getLeadTime(), meeting.getFollowupTime());
	}

	private void addTeamsMeeting(Appointment appointment, TeamsMeeting meetingTemplate) {
		appointment = getAppointmentsService().addTeamsMeeting(appointment, executor);
		TeamsMeeting meeting = appointment.getTeamsMeeting();
		
		meeting.setLeadTime(meetingTemplate.getLeadTime());
		meeting.setFollowupTime(meetingTemplate.getFollowupTime());
		meeting.setPermanent(meetingTemplate.isPermanent());
		meeting.setMainPresenter(getAppointmentsService().getFormattedOrganizers(appointment.getTopic()));
		meeting.setAccessLevel(meetingTemplate.getAccessLevel());
		meeting.setAllowedPresenters(meetingTemplate.getAllowedPresenters());
		meeting.setEntryExitAnnouncement(meetingTemplate.isEntryExitAnnouncement());
		meeting.setLobbyBypassScope(meetingTemplate.getLobbyBypassScope());
	}
	
	private AppointmentsService getAppointmentsService() {
		if (appointmentsService == null) {
			appointmentsService = CoreSpringFactory.getImpl(AppointmentsService.class);
		}
		return appointmentsService;
	}
	
	public static DuplicationContext getDuplicationContext(StepsRunContext runContext) {
		if (runContext.containsKey(DUPLICATION_CONTEXT)) {
			return (DuplicationContext)runContext.get(DUPLICATION_CONTEXT);
		}
		
		DuplicationContext context = new DuplicationContext();
		runContext.put(DUPLICATION_CONTEXT, context);
		return context;
	}
	
	public static final class DuplicationContext {
		
		private RepositoryEntry entry;
		private String subIdent;
		private TopicLight topic;
		private Collection<Identity> organizers;
		private Collection<AppointmentInput> appointments;
		
		public RepositoryEntry getEntry() {
			return entry;
		}
		
		public void setEntry(RepositoryEntry entry) {
			this.entry = entry;
		}
		
		public String getSubIdent() {
			return subIdent;
		}
		
		public void setSubIdent(String subIdent) {
			this.subIdent = subIdent;
		}
		
		public TopicLight getTopic() {
			return topic;
		}
		
		public void setTopic(TopicLight topic) {
			this.topic = topic;
		}

		public Collection<Identity> getOrganizers() {
			return organizers;
		}

		public void setOrganizers(Collection<Identity> organizers) {
			this.organizers = organizers;
		}

		public Collection<AppointmentInput> getAppointments() {
			return appointments;
		}

		public void setAppointments(Collection<AppointmentInput> appointments) {
			this.appointments = appointments;
		}
		
	}
	
	public static TopicLight toTransientTopic(TopicLight topicLight) {
		TransientTopic topic = new TransientTopic();
		topic.setAutoConfirmation(topicLight.isAutoConfirmation());
		topic.setDescription(topicLight.getDescription());
		topic.setMultiParticipation(topicLight.isMultiParticipation());
		topic.setParticipationVisible(topicLight.isParticipationVisible());
		topic.setTitle(topicLight.getTitle());
		topic.setType(topicLight.getType());
		return topic;
	}

	private static final class TransientTopic implements TopicLight {
		
		private String title;
		private String description;
		private Type type;
		private boolean multiParticipation;
		private boolean autoConfirmation;
		private boolean participationVisible;
		
		@Override
		public String getTitle() {
			return title;
		}
		
		@Override
		public void setTitle(String title) {
			this.title = title;
		}
		
		@Override
		public String getDescription() {
			return description;
		}
		
		@Override
		public void setDescription(String description) {
			this.description = description;
		}
		
		@Override
		public Type getType() {
			return type;
		}
		
		@Override
		public void setType(Type type) {
			this.type = type;
		}
		
		@Override
		public boolean isMultiParticipation() {
			return multiParticipation;
		}
		
		@Override
		public void setMultiParticipation(boolean multiParticipation) {
			this.multiParticipation = multiParticipation;
		}
		
		@Override
		public boolean isAutoConfirmation() {
			return autoConfirmation;
		}
		
		@Override
		public void setAutoConfirmation(boolean autoConfirmation) {
			this.autoConfirmation = autoConfirmation;
		}
		
		@Override
		public boolean isParticipationVisible() {
			return participationVisible;
		}
		
		@Override
		public void setParticipationVisible(boolean participationVisible) {
			this.participationVisible = participationVisible;
		}
		
	}
	
	public final static class AppointmentInput {
		
		private final Appointment appointment;
		private final Date start;
		private final Date end;
		private final Boolean meetingValidation;
		
		public AppointmentInput(Appointment appointment, Date start, Date end, Boolean meetingValidation) {
			this.appointment = appointment;
			this.start = start;
			this.end = end;
			this.meetingValidation = meetingValidation;
		}

		public Appointment getAppointment() {
			return appointment;
		}

		public Date getStart() {
			return start;
		}

		public Date getEnd() {
			return end;
		}

		public Boolean getMeetingValidation() {
			return meetingValidation;
		}
		
	}
}
