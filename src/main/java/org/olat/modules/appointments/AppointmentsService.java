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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.UserSession;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
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
	
	public void deleteTopic(TopicRef topic);
	
	public List<Topic> getTopics(RepositoryEntryRef entryRef, String subIdent);

	/**
	 * Get the topics to which the identity has access.
	 *
	 * @param entry
	 * @param subIdent
	 * @param identity
	 * @return
	 */
	public List<Topic> getRestictedTopic(RepositoryEntryRef entry, String subIdent, IdentityRef identity);
	
	public void updateOrganizers(Topic topic, Collection<Identity> identities);

	public List<Organizer> getOrganizers(TopicRef topic);

	public List<Organizer> getOrganizers(RepositoryEntry entry, String subIdent);
	
	public void restrictTopic(Topic topic, List<Group> groups);

	public void addTopicRestriction(Topic topic, Identity identity);
	
	public void removeTopicRestriction(Topic topic, IdentityRef identity);
	
	public boolean hasGroupRestrictions(TopicRef topic);
	
	public List<Group> getGroupRestrictions(TopicRef topic);

	/**
	 *
	 * @param topic
	 * @return the identities of the topic group.
	 */
	public List<Identity> getUserRestrictions(Topic topic);
	
	/**
	 *
	 * @param topic
	 * @return the identities of the all restriction groups.
	 */
	public List<Identity> getRestrictionMembers(TopicRef topic);
	
	public Appointment createUnsavedAppointment(Topic topic);

	public Appointment saveAppointment(Appointment appointment);

	public void confirmAppointment(Appointment appointment);

	public void unconfirmAppointment(Appointment appointment);

	public void deleteAppointment(Appointment appointment);
	
	/**
	 * Checks whether the end of the appointment is after the due date.
	 * It respects full day events.
	 *
	 * @param appointment
	 * @param dueDate
	 * @return
	 */
	public boolean isEndAfter(Appointment appointment, Date dueDate);
	
	/**
	 * Gets the key of the topic and the according count of appointments.
	 *
	 * @param params
	 * @param freeOnly counts only appointments with free participations
	 * @return the count may be null (instead of 0) if no appointments available.
	 */
	public Map<Long, Long> getTopicKeyToAppointmentCount(AppointmentSearchParams params, boolean freeOnly);

	public Long getAppointmentCount(AppointmentSearchParams params);
	
	public List<Appointment> getAppointments(AppointmentSearchParams params);
	
	public ParticipationResult createParticipations(Appointment appointment, Collection<Identity> identities,
			Identity createdBy, boolean multiParticipations, boolean autoConfirmation, boolean rejectIfConfirmed);
	
	public ParticipationResult rebookParticipations(AppointmentRef toAppointmenRef,
			Collection<? extends ParticipationRef> participationRefs, Identity rebookedBy, boolean autoConfirmation);

	public void deleteParticipation(Participation participation);
	
	public void deleteParticipations(Collection<? extends ParticipationRef> participationRefs, boolean sendEmail);

	public Long getParticipationCount(ParticipationSearchParams params);

	public List<Participation> getParticipations(ParticipationSearchParams params);

	public PublisherData getPublisherData(RepositoryEntry entry, String subIdent);

	public SubscriptionContext getSubscriptionContext(RepositoryEntry entry, String subIdent);
	
	public String createBussinesPath(Long entryKey, String subIdent);
	
	public boolean isBigBlueButtonEnabled();
	
	public List<BigBlueButtonMeetingTemplate> getBigBlueButtonTemplates(RepositoryEntryRef entryRef, Identity identity, Roles roles,
			Long selectedTemplateKey);
	
	public Appointment addMeeting(Appointment appointment, Identity identity);

	public Appointment removeMeeting(Appointment appointment);

	public String joinMeeting(Appointment appointment, Identity identity, BigBlueButtonErrors errors);

	public String getMainPresenters(Topic topic);

	public void syncRecorings(Topic topic);

	public Map<Long, List<BigBlueButtonRecordingReference>> getRecordingReferences(List<Appointment> appointments);

	public String getRecordingUrl(UserSession userSession, BigBlueButtonRecordingReference recordingReference);

}

