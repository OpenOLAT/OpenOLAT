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
package org.olat.course.nodes.appointments;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.nodes.AppointmentsCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.Organizer;

/**
 * 
 * Initial date: 13 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsSecurityCallbackFactory {
	
	public static final AppointmentsSecurityCallback create(ModuleConfiguration config,
			UserCourseEnvironment userCourseEnv) {
		return new UserAppointmentsSecurityCallback(config, userCourseEnv);
	}
	
	private static class UserAppointmentsSecurityCallback implements AppointmentsSecurityCallback {

		private final Identity identity;
		private final boolean admin;
		private final boolean coach;
		private final boolean coachCanEditTopic;
		private final boolean coachCanEditAppointment;
		private final boolean participant;
		private final boolean readOnly;

		public UserAppointmentsSecurityCallback(ModuleConfiguration config, UserCourseEnvironment userCourseEnv) {
			this.identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			this.admin = userCourseEnv.isAdmin();
			this.coach = userCourseEnv.isCoach();
			this.participant = userCourseEnv.isParticipant();
			this.readOnly = userCourseEnv.isCourseReadOnly();
			
			this.coachCanEditTopic = coach && config.getBooleanSafe(AppointmentsCourseNode.CONFIG_COACH_EDIT_TOPIC);
			this.coachCanEditAppointment = coach && config.getBooleanSafe(AppointmentsCourseNode.CONFIG_COACH_EDIT_APPOINTMENT);
		}

		@Override
		public boolean canEditTopics() {
			return admin;
		}

		@Override
		public boolean canCreateTopic() {
			if (readOnly) return false;
			
			return admin || coachCanEditTopic;
		}

		@Override
		public Identity getDefaultOrganizer() {
			return coachCanEditTopic? identity: null;
		}

		@Override
		public boolean canEditTopic(List<Organizer> organizers) {
			if (readOnly) return false;
			
			return admin || (coachCanEditTopic && isOrganizer(organizers));
		}

		@Override
		public boolean canViewAppointment(List<Organizer> organizers) {
			return admin || (coach && isOrganizer(organizers));
		}

		@Override
		public boolean canEditAppointment(List<Organizer> organizers) {
			return admin || (coachCanEditAppointment && isOrganizer(organizers));
		}

		@Override
		public boolean canSelectAppointments() {
			if (readOnly) return false;
			
			return participant;
		}
		
		private boolean isOrganizer(List<Organizer> organizers) {
			return organizers.stream()
					.anyMatch(o -> o.getIdentity().getKey().equals(identity.getKey()));
		}
		
	}

}
