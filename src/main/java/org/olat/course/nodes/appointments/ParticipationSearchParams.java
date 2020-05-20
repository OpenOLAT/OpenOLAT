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
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationSearchParams {
	
	private Topic topic;
	private RepositoryEntry entry;
	private String subIdent;
	private Identity identity;
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
	
	public Topic getTopic() {
		return topic;
	}
	
	public void setTopic(Topic topic) {
		this.topic = topic;
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

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
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
