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
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationSearchParams {
	
	private Collection<Long> topicKeys;
	private RepositoryEntry entry;
	private String subIdent;
	private Collection<Long> identityKeys;
	private Date createdAfter;
	private Collection<Long> participationKeys;
	private Collection<Long> appointmentKeys;
	private Date startAfter;
	private Appointment.Status status;
	private Date statusModifiedAfter;
	private Identity organizer;
	private boolean fetchTopics;
	private boolean fetchAppointments;
	private boolean fetchIdentities;
	private boolean fetchUser;
	
	public Collection<Long> getTopicKeys() {
		return topicKeys;
	}
	
	public void setTopic(TopicRef topicRef) {
		this.topicKeys = Collections.singleton(topicRef.getKey());
	}
	
	public void setTopics(Collection<? extends TopicRef> topics) {
		this.topicKeys = topics.stream()
				.map(TopicRef::getKey)
				.collect(Collectors.toSet());
	}
	
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

	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}

	public void setIdentityKeys(Collection<Long> identityKeys) {
		this.identityKeys = identityKeys;
	}
	
	public void setIdentities(Collection<? extends IdentityRef> identities) {
		this.identityKeys = identities.stream()
				.map(IdentityRef::getKey)
				.collect(Collectors.toSet());
	}

	public void setIdentity(IdentityRef identity) {
		this.identityKeys = Collections.singletonList(identity.getKey());
	}
	
	public Date getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}

	public Collection<Long> getParticipationKeys() {
		return participationKeys;
	}
	
	public void setParticipation(Participation participation) {
		this.participationKeys = Collections.singletonList(participation.getKey());
	}
	
	public void setParticipations(Collection<? extends ParticipationRef> participations) {
		this.participationKeys = participations.stream()
				.map(ParticipationRef::getKey)
				.collect(Collectors.toSet());
	}

	public Collection<Long> getAppointmentKeys() {
		return appointmentKeys;
	}
	
	public void setAppointmentKeys(Collection<Long> appointmentKeys) {
		this.appointmentKeys = appointmentKeys;
	}

	public void setAppointments(Collection<? extends AppointmentRef> appointments) {
		this.appointmentKeys = appointments.stream()
				.map(AppointmentRef::getKey)
				.collect(Collectors.toSet());
	}
	
	public void setAppointment(AppointmentRef appointment) {
		this.appointmentKeys = Collections.singletonList(appointment.getKey());
	}

	public Date getStartAfter() {
		return startAfter;
	}

	public void setStartAfter(Date startAfter) {
		this.startAfter = startAfter;
	}
	
	public Appointment.Status getStatus() {
		return status;
	}
	
	public void setStatus(Appointment.Status status) {
		this.status = status;
	}

	public Date getStatusModifiedAfter() {
		return statusModifiedAfter;
	}

	public void setStatusModifiedAfter(Date statusModifiedAfter) {
		this.statusModifiedAfter = statusModifiedAfter;
	}

	public Identity getOrganizer() {
		return organizer;
	}

	public void setOrganizer(Identity organizer) {
		this.organizer = organizer;
	}

	public boolean isFetchTopics() {
		return fetchTopics;
	}

	public void setFetchTopics(boolean fetchTopics) {
		this.fetchTopics = fetchTopics;
	}

	public boolean isFetchAppointments() {
		return fetchAppointments;
	}

	public void setFetchAppointments(boolean fetchAppointments) {
		this.fetchAppointments = fetchAppointments;
	}

	public boolean isFetchIdentities() {
		return fetchIdentities;
	}

	public void setFetchIdentities(boolean fetchIdentities) {
		this.fetchIdentities = fetchIdentities;
	}

	public boolean isFetchUser() {
		return fetchUser;
	}

	public void setFetchUser(boolean fetchUser) {
		this.fetchUser = fetchUser;
	}

}
