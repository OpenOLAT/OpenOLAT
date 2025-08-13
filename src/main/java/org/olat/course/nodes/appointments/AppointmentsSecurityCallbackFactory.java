/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.appointments;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.nodes.AppointmentsCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.OrganizerCandidateSupplier;
import org.olat.modules.appointments.Participation;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.teams.TeamsMeeting;

/**
 * 
 * Initial date: 13 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AppointmentsSecurityCallbackFactory {
	
	public static final AppointmentsSecurityCallback create(ModuleConfiguration config,
			UserCourseEnvironment userCourseEnv, OrganizerCandidateSupplier organizerCandidateSupplier) {
		return new UserAppointmentsSecurityCallback(config, userCourseEnv, organizerCandidateSupplier);
	}
	
	private static class UserAppointmentsSecurityCallback implements AppointmentsSecurityCallback {

		private final Identity identity;
		private final boolean admin;
		private final boolean coach;
		private final boolean coachCanEditTopic;
		private final boolean coachCanEditAppointment;
		private final boolean participant;
		private final boolean readOnly;
		private final boolean organizerCandidate;
		private final boolean sendParticipationNotificationToOrganizers;
		private final boolean participantCanComment;

		public UserAppointmentsSecurityCallback(ModuleConfiguration config, UserCourseEnvironment userCourseEnv,
				OrganizerCandidateSupplier organizerCandidateSupplier) {
			this.identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			this.admin = userCourseEnv.isAdmin();
			this.coach = userCourseEnv.isCoach();
			this.participant = userCourseEnv.isParticipant();
			this.readOnly = userCourseEnv.isCourseReadOnly();
			this.organizerCandidate = organizerCandidateSupplier.getOrganizerCandidates().contains(this.identity);
			
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			this.coachCanEditTopic = nodeRightService.isGranted(config, userCourseEnv, AppointmentsCourseNode.EDIT_TOPIC);
			this.coachCanEditAppointment = nodeRightService.isGranted(config, userCourseEnv, AppointmentsCourseNode.EDIT_APPOINTMENT);
			this.sendParticipationNotificationToOrganizers = config.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_NOTIFICATION_FOR_ORGANIZERS);
			this.participantCanComment = config.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_PARTICIPANT_COMMENT);
		}

		@Override
		public boolean canCreateTopic() {
			if (readOnly) return false;
			
			return admin || coachCanEditTopic;
		}

		@Override
		public boolean canEditTopic(Collection<Organizer> organizers) {
			if (readOnly) return false;
			
			return admin || (coachCanEditTopic && isOrganizer(organizers));
		}

		@Override
		public boolean canViewAppointment(Collection<Organizer> organizers) {
			return admin || (coach && isOrganizer(organizers));
		}

		@Override
		public boolean canEditAppointment(Collection<Organizer> organizers) {
			if (readOnly) return false;
			
			return admin || (coachCanEditAppointment && isOrganizer(organizers));
		}

		@Override
		public boolean canSelectAppointments() {
			if (readOnly) return false;
			
			return participant;
		}

		@Override
		public boolean canJoinBBBMeeting(Appointment appointment, Collection<Organizer> organizers, Collection<Participation> participations) {
			if (readOnly 
					|| appointment == null
					|| appointment.getBBBMeeting() == null
					|| Appointment.Status.confirmed != appointment.getStatus()) {
				return false;
			}
			
			BigBlueButtonMeeting meeting = appointment.getBBBMeeting();
			boolean participation = isParticipation(participations);
			boolean organizer = isOrganizer(organizers);
			if (participation || organizer) {
				Date end = meeting.getEndWithFollowupTime();
				return end == null || end.compareTo(new Date()) >= 0;
			}
			return false;
		}

		@Override
		public boolean canJoinOtherMeeting(Appointment appointment, Collection<Organizer> organizers, Collection<Participation> participations) {
			if (readOnly
					|| appointment == null
					|| !StringHelper.containsNonWhitespace(appointment.getMeetingUrl())
					|| Appointment.Status.confirmed != appointment.getStatus()) {
				return false;
			}

			boolean participation = isParticipation(participations);
			boolean organizer = isOrganizer(organizers);
			if (participation || organizer) {
				Date end = appointment.getEnd();
				return end == null || end.compareTo(new Date()) >= 0;
			}
			return false;
		}

		@Override
		public boolean isBBBMeetingOpen(Appointment appointment, Collection<Organizer> organizers) {
			if (readOnly 
					|| appointment == null
					|| appointment.getBBBMeeting() == null
					|| Appointment.Status.confirmed != appointment.getStatus()) {
				return false;
			}
			
			BigBlueButtonMeeting meeting = appointment.getBBBMeeting();
			Date now = new Date();
			Date start = isOrganizer(organizers) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
			Date end = meeting.getEndWithFollowupTime();
			return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
		}

		@Override
		public boolean canWatchRecording(Collection<Organizer> organizers, Collection<Participation> participations) {
			return isOrganizer(organizers) || isParticipation(participations);
		}

		@Override
		public boolean canJoinTeamsMeeting(Appointment appointment, Collection<Organizer> organizers, List<Participation> participations) {
			if (readOnly 
					|| appointment == null
					|| appointment.getTeamsMeeting() == null
					|| Appointment.Status.confirmed != appointment.getStatus()) {
				return false;
			}
			
			TeamsMeeting meeting = appointment.getTeamsMeeting();
			boolean participation = isParticipation(participations);
			boolean organizer = isOrganizer(organizers);
			if (participation || organizer) {
				Date end = meeting.getEndWithFollowupTime();
				return end == null || end.compareTo(new Date()) >= 0;
			}
			return false;
		}

		@Override
		public boolean isTeamsMeetingOpen(Appointment appointment, Collection<Organizer> organizers) {
			if (readOnly 
					|| appointment == null
					|| appointment.getTeamsMeeting() == null
					|| Appointment.Status.confirmed != appointment.getStatus()) {
				return false;
			}
			
			TeamsMeeting meeting = appointment.getTeamsMeeting();
			Date now = new Date();
			Date start = isOrganizer(organizers) ? meeting.getStartWithLeadTime() : meeting.getStartDate();
			Date end = meeting.getEndWithFollowupTime();
			return !((start != null && start.compareTo(now) >= 0) || (end != null && end.compareTo(now) <= 0));
		}

		@Override
		public boolean canSubscribe(Collection<Organizer> organizers) {
			if (readOnly) return false;
			
			return organizerCandidate || isOrganizer(organizers);
		}
		
		private boolean isOrganizer(Collection<Organizer> organizers) {
			return organizers != null && organizers.stream()
					.anyMatch(o -> o.getIdentity().getKey().equals(identity.getKey()));
		}
		
		private boolean isParticipation(Collection<Participation> participations) {
			return participations != null && participations.stream()
					.anyMatch(p -> p.getIdentity().getKey().equals(identity.getKey()));
		}

		@Override
		public boolean isSendParticipationNotificationToOrganizers() {
			return sendParticipationNotificationToOrganizers;
		}

		@Override
		public boolean isParticipantCanComment() {
			return participantCanComment;
		}

		@Override
		public GroupRoles searchMemberAs() {
			if(admin) {
				return GroupRoles.owner;
			}
			if(coach) {
				return GroupRoles.coach;
			}
			return GroupRoles.participant;
		}
	}
}
