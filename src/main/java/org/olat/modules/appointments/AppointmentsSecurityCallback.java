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
package org.olat.modules.appointments;

import java.util.Collection;
import java.util.List;

/**
 * 
 * Initial date: 13 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AppointmentsSecurityCallback {
	
	public boolean canCreateTopic();
	
	public boolean canEditTopic(Collection<Organizer> organizers);
	
	public boolean canViewAppointment(Collection<Organizer> organizers);
	
	public boolean canEditAppointment(Collection<Organizer> organizers);
	
	public boolean canSelectAppointments();
	
	public boolean canJoinBBBMeeting(Appointment appointment, Collection<Organizer> organizers, Collection<Participation> participations);
	
	public boolean isBBBMeetingOpen(Appointment appointment, Collection<Organizer> organizers);
	
	public boolean canWatchRecording(Collection<Organizer> organizers, Collection<Participation> participations);

	public boolean canJoinTeamsMeeting(Appointment appointment, Collection<Organizer> organizers, List<Participation> participations);
	
	public boolean isTeamsMeetingOpen(Appointment appointment, Collection<Organizer> organizers);
	
	public boolean canSubscribe(Collection<Organizer> organizers);

	
}
