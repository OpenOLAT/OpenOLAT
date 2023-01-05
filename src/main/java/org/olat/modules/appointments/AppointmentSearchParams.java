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

import org.olat.core.id.Identity;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentSearchParams {
	
	private Long appointmentKey;
	private Collection<Long> topicKeys;
	private RepositoryEntry entry;
	private String subIdent;
	private Identity organizer;
	private Date startAfter;
	private Appointment.Status status;
	private BigBlueButtonMeeting bbbMeeting;
	private TeamsMeeting teamsMeeting;
	private boolean hasMeeting;
	private boolean withMaxParticipations;
	private boolean fetchTopic;
	private boolean fetchEntry;
	private boolean fetchMeetings;

	public Long getAppointmentKey() {
		return appointmentKey;
	}

	public void setAppointment(AppointmentRef appointment) {
		this.appointmentKey = appointment.getKey();
	}

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
	
	public Identity getOrganizer() {
		return organizer;
	}

	public void setOrganizer(Identity organizer) {
		this.organizer = organizer;
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

	public BigBlueButtonMeeting getBBBMeeting() {
		return bbbMeeting;
	}

	public void setBBBMeeting(BigBlueButtonMeeting bbbMeeting) {
		this.bbbMeeting = bbbMeeting;
	}

	public TeamsMeeting getTeamsMeeting() {
		return teamsMeeting;
	}

	public void setTeamsMeeting(TeamsMeeting teamsMeeting) {
		this.teamsMeeting = teamsMeeting;
	}

	public boolean hasMeeting() {
		return hasMeeting;
	}

	public void setHasMeeting(boolean hasMeeting) {
		this.hasMeeting = hasMeeting;
	}

	public boolean isWithMaxParticipants() {
		return withMaxParticipations;
	}

	public void setWithMaxParticipants(boolean withMaxParticipations) {
		this.withMaxParticipations = withMaxParticipations;
	}

	public boolean isFetchTopic() {
		return fetchTopic;
	}

	public void setFetchTopic(boolean fetchTopic) {
		this.fetchTopic = fetchTopic;
	}

	public boolean isFetchEntry() {
		return fetchEntry;
	}

	public void setFetchEntry(boolean fetchEntry) {
		this.fetchEntry = fetchEntry;
	}

	public boolean isFetchMeetings() {
		return fetchMeetings;
	}

	public void setFetchMeetings(boolean fetchMeetings) {
		this.fetchMeetings = fetchMeetings;
	}

}
