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

import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 11 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AppointmentsService {
	
	public Topic createTopic(RepositoryEntry entry, String subIdent);
	
	public Topic updateTopic(Topic topic);
	
	public void deleteTopics(RepositoryEntry entry, String subIdent);
	
	public void deleteTopic(Topic topic);
	
	public List<Topic> getTopics(RepositoryEntryRef entryRef, String subIdent);
	
	public Organizer createOrganizer(Topic topic, Identity identity);

	public void deleteOrganizers(Topic topic, Collection<Organizer> organizers);

	public List<Organizer> getOrganizers(Topic topic);

	public List<Organizer> getOrganizers(RepositoryEntry entry, String subIdent);
	
	public Appointment createUnsavedAppointment(Topic topic);

	public Appointment saveAppointment(Appointment appointment);

	public void confirmAppointment(Appointment appointment);

	public void confirmAppointments(RepositoryEntry entry, String subIdent);

	public void unconfirmAppointment(Appointment appointment);

	public void deleteAppointment(Appointment appointment);

	public List<Appointment> getAppointments(AppointmentSearchParams params);

	public ParticipationResult createParticipation(Appointment appointment, Identity identity, boolean autoConfirmation);

	public void deleteParticipation(Participation participation);

	public Long getParticipationCount(ParticipationSearchParams params);

	public List<Participation> getParticipations(ParticipationSearchParams params);

	public PublisherData getPublisherData(RepositoryEntry entry, String subIdent);

	public SubscriptionContext getSubscriptionContext(RepositoryEntry entry, String subIdent);
	
	public String createBussinesPath(Long entryKey, String subIdent);

}

