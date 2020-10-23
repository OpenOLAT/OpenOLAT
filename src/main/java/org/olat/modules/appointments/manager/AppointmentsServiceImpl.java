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
package org.olat.modules.appointments.manager;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentRef;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationRef;
import org.olat.modules.appointments.ParticipationResult;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.appointments.TopicRef;
import org.olat.modules.appointments.TopicToGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingDeletionHandler;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonRecording;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.model.BigBlueButtonError;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrorCodes;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonRecordingWithReference;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AppointmentsServiceImpl implements AppointmentsService, BigBlueButtonMeetingDeletionHandler {
	
	private static final String TOPIC_USER_RESTRICTION_ROLE = GroupRoles.participant.name();

	@Autowired
	private TopicDAO topicDao;
	@Autowired
	private OrganizerDAO organizerDao;
	@Autowired
	private TopicToGroupDAO topicToGroupDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private AppointmentDAO appointmentDao;
	@Autowired
	private ParticipationDAO participationDao;
	@Autowired
	private AppointmentsMailing appointmentsMailing;
	@Autowired
	private CalendarSyncher calendarSyncher;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;

	@Override
	public Topic createTopic(RepositoryEntry entry, String subIdent) {
		return topicDao.createTopic(entry, subIdent);
	}

	@Override
	public Topic updateTopic(Topic topic) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setFetchTopic(true);
		if (Type.finding == topic.getType()) {
			params.setStatus(Status.confirmed);
		}
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		
		if (!appointments.isEmpty()) {
			calendarSyncher.syncCalendars(topic, appointments);
		}
		
		return topicDao.updateTopic(topic);
	}

	@Override
	public List<Topic> getTopics(RepositoryEntryRef entryRef, String subIdent) {
		return topicDao.loadTopics(entryRef, subIdent);
	}
	
	@Override
	public List<Topic> getRestictedTopic(RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		return topicToGroupDao.loadRestrictedTopics(entry, subIdent, identity);
	}

	@Override
	public void deleteTopics(RepositoryEntry entry, String subIdent) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		if (StringHelper.containsNonWhitespace(subIdent)) {
			params.setSubIdent(subIdent);
		}
		params.setStartAfter(new Date());
		params.setFetchTopic(true);
		params.setFetchMeetings(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		
		if (!appointments.isEmpty()) {
			Map<Topic, List<Appointment>> topicToAppointments = appointments.stream()
					.collect(Collectors.groupingBy(Appointment::getTopic));
			for (Entry<Topic, List<Appointment>> topicAppointments : topicToAppointments.entrySet()) {
				calendarSyncher.unsyncCalendars(topicAppointments.getKey(), topicAppointments.getValue());
			}	
			
			List<Appointment> confimredAppointments = appointments.stream()
					.filter(a -> a.getStatus() == Status.confirmed)
					.collect(Collectors.toList());
			appointmentsMailing.sendAppointmentDeleted(confimredAppointments);
			List<Organizer> organizers = organizerDao.loadOrganizers(entry, subIdent);
			appointmentsMailing.sendAppointmentsDeleted(confimredAppointments, organizers);
		}
		
		List<Topic> topics = topicDao.loadTopics(entry, subIdent);
		for (Topic topic : topics) {
			deleteTopicGroup(topic);
		}
		participationDao.delete(entry, subIdent);
		deleteMeetings(appointments);
		appointmentDao.delete(entry, subIdent);
		topicToGroupDao.delete(entry, subIdent);
		organizerDao.delete(entry, subIdent);
		topicDao.delete(entry, subIdent);
	}

	@Override
	public void deleteTopic(TopicRef topicRef) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topicRef);
		params.setStartAfter(new Date());
		params.setFetchTopic(true);
		params.setFetchMeetings(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		
		if (!appointments.isEmpty()) {
			List<Appointment> confimredAppointments = appointments.stream()
					.filter(a -> a.getStatus() == Status.confirmed)
					.collect(Collectors.toList());
			appointmentsMailing.sendAppointmentDeleted(confimredAppointments);
			List<Organizer> organizers = organizerDao.loadOrganizers(topicRef);
			appointmentsMailing.sendAppointmentsDeleted(confimredAppointments, organizers);
			
			calendarSyncher.unsyncCalendars(appointments.get(0).getTopic(), appointments);
		}
		
		Topic topic = topicDao.loadByKey(topicRef.getKey());
		deleteTopicGroup(topic);
		topicToGroupDao.delete(topicRef);
		participationDao.delete(topicRef);
		deleteMeetings(appointments);
		appointmentDao.delete(topicRef);
		organizerDao.delete(topicRef);
		topicDao.delete(topicRef);
	}

	private void deleteTopicGroup(Topic topic) {
		Group group = topic.getGroup();
		if (group != null) {
			groupDao.removeMemberships(group);
			groupDao.removeGroup(group);
		}
	}
	
	@Override
	public void updateOrganizers(Topic topic, Collection<Identity> identities) {
		List<Organizer> organizers = organizerDao.loadOrganizers(topic);
		List<Identity> organizersToCreate = new ArrayList<>(identities.size());
		for (Identity identity : identities) {
			boolean found = false;
			for (Organizer organizer : organizers) {
				if (organizer.getIdentity().equals(identity)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				organizersToCreate.add(identity);
			}
		}
		
		if (!organizersToCreate.isEmpty()) {
			List<Organizer> createdOrganizers = createOrganizers(topic, organizersToCreate);
			organizers.addAll(createdOrganizers);
		}
		
		ArrayList<Organizer> organizersToDelete = new ArrayList<>(organizers.size());
		for (Iterator<Organizer> organizersIt = organizers.iterator(); organizersIt.hasNext(); ) {
			Organizer organizer = organizersIt.next();
			if (!identities.contains(organizer.getIdentity())) {
				organizersToDelete.add(organizer);
			}
		}
		if (!organizersToDelete.isEmpty()) {
			deleteOrganizers(topic, organizersToDelete);
		}
		
		syncMeetingPresenters(topic, identities);
	}

	private List<Organizer> createOrganizers(Topic topic, Collection<Identity> identities) {
		List<Organizer> organizers = new ArrayList<>(identities.size());
		for (Identity identity : identities) {
			Organizer organizer = organizerDao.createOrganizer(topic, identity);
			organizers.add(organizer);
		}
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setFetchTopic(true);
		if (Type.finding == topic.getType()) {
			params.setStatus(Status.confirmed);
		}
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		
		for (Organizer organizer : organizers) {
			calendarSyncher.syncCalendar(appointments, organizer.getIdentity());
		}
		
		return organizers;
	}

	private void deleteOrganizers(TopicRef topic, Collection<Organizer> organizers) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setStatus(Status.confirmed);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		
		Collection<Long> identityKeys = organizers.stream()
				.map(organizer -> organizer.getIdentity().getKey())
				.collect(Collectors.toList());
		securityManager.loadIdentityByKeys(identityKeys).stream()
				.forEach(identity -> calendarSyncher.unsyncCalendar(appointments, identity));
			
		organizerDao.deleteOrganizers(organizers);
	}

	private void syncMeetingPresenters(Topic topic, Collection<Identity> identities) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setHasMeeting(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		if (!appointments.isEmpty()) {
			List<Long> identityKeys = identities.stream().map(Identity::getKey).collect(Collectors.toList());
			String mainPresenters = getMainPresenters(identityKeys);
			for (Appointment appointment : appointments) {
				appointment.getMeeting().setMainPresenter(mainPresenters);
				saveAppointment(appointment);
			}
		}
	}

	@Override
	public List<Organizer> getOrganizers(TopicRef topic) {
		return organizerDao.loadOrganizers(topic);
	}

	@Override
	public List<Organizer> getOrganizers(RepositoryEntry entry, String subIdent) {
		return organizerDao.loadOrganizers(entry, subIdent);
	}

	@Override
	public void restrictTopic(Topic topic, List<Group> groups) {
		List<TopicToGroup> topicToGroups = topicToGroupDao.load(topic);
		for (Group group : groups) {
			boolean found = false;
			for (TopicToGroup topicToGroup : topicToGroups) {
				if (topicToGroup.getGroup().equals(group)) {
					found = true;
					break;
				}
			}
			
			if (!found) {
				TopicToGroup topicToGroup = topicToGroupDao.create(topic, group);
				topicToGroups.add(topicToGroup);
			}
		}
		
		for (Iterator<TopicToGroup> lectureToGroupIt = topicToGroups.iterator(); lectureToGroupIt.hasNext(); ) {
			TopicToGroup topicToGroup= lectureToGroupIt.next();
			if (!groups.contains(topicToGroup.getGroup())) {
				topicToGroupDao.delete(topicToGroup);
			}
		}
	}
	
	@Override
	public boolean hasGroupRestrictions(TopicRef topic) {
		return topicToGroupDao.loadGroupCount(topic).longValue() > 0;
	}
	
	@Override
	public List<Group> getGroupRestrictions(TopicRef topic) {
		return topicToGroupDao.loadGroups(topic);
	}
	
	@Override
	public void addTopicRestriction(Topic topic, Identity identity) {
		Group group = topic.getGroup();
		if (group == null) {
			group = groupDao.createGroup();
			topicDao.setGroup(topic, group);
		}
		groupDao.addMembershipOneWay(group, identity, TOPIC_USER_RESTRICTION_ROLE);
	}

	@Override
	public void removeTopicRestriction(Topic topic, IdentityRef identity) {
		Group group = topic.getGroup();
		if (group != null) {
			groupDao.removeMembership(group, identity, TOPIC_USER_RESTRICTION_ROLE);
			if (groupDao.countMembers(group) == 0) {
				topicToGroupDao.delete(group);
				groupDao.removeGroup(group);
				topicDao.setGroup(topic, null);
			}
		}
	}
	
	@Override
	public List<Identity> getUserRestrictions(Topic topic) {
		Group group = topic.getGroup();
		if (group != null) {
			return groupDao.getMembers(group, TOPIC_USER_RESTRICTION_ROLE);
		}
		return Collections.emptyList();
	}
	
	@Override
	public List<Identity> getRestrictionMembers(TopicRef topic) {
		List<Group> groups = topicToGroupDao.loadGroups(topic);
		if (!groups.isEmpty()) {
			return groupDao.getMembers(groups, TOPIC_USER_RESTRICTION_ROLE);
		}
		return Collections.emptyList();
	}

	@Override
	public Appointment createUnsavedAppointment(Topic topic) {
		return appointmentDao.createUnsavedAppointment(topic);
	}

	@Override
	public Appointment saveAppointment(Appointment appointment) {
		if (Status.confirmed == appointment.getStatus() || Type.finding != appointment.getTopic().getType()) {
			calendarSyncher.syncCalendars(appointment.getTopic(), singletonList(appointment));
		}
		BigBlueButtonMeeting meeting = appointment.getMeeting();
		if (meeting != null) {
			meeting = bigBlueButtonManager.updateMeeting(meeting);
		}
		return appointmentDao.saveAppointment(appointment, meeting);
	}
	
	@Override
	public void confirmAppointment(Appointment appointment) {
		AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
		appointmentParams.setAppointment(appointment);
		appointmentParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(appointmentParams);
		if (!appointments.isEmpty()) {
			Appointment reloaded = appointments.get(0);
			confirmReloadedAppointment(reloaded, false);
		}
		deleteFindingMeetings(appointment);
	}

	private void confirmReloadedAppointment(Appointment appointment, boolean sendEmail) {
		if (Status.planned == appointment.getStatus()) {
			appointmentDao.updateStatus(appointment, Status.confirmed);
			calendarSyncher.syncCalendars(appointment.getTopic(), singletonList(appointment));
			if (!sendEmail) {
				appointmentsMailing.sendAppointmentConfirmed(appointment);
			}
		}
	}

	private void deleteFindingMeetings(Appointment appointment) {
		Topic topic = appointment.getTopic();
		if (Topic.Type.finding == topic.getType()) {
			AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
			appointmentParams.setTopic(topic);
			appointmentParams.setHasMeeting(true);
			List<Appointment> otherAppointments = appointmentDao.loadAppointments(appointmentParams).stream()
					.filter(a -> !a.equals(appointment))
					.collect(Collectors.toList());
			deleteMeetings(otherAppointments);
		}
	}

	@Override
	public void unconfirmAppointment(Appointment appointment) {
		AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
		appointmentParams.setAppointment(appointment);
		appointmentParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(appointmentParams);
		if (!appointments.isEmpty()) {
			Appointment reloaded = appointments.get(0);
			if (Status.confirmed == reloaded.getStatus()) {
				appointmentDao.updateStatus(reloaded, Status.planned);
				if (Type.finding == reloaded.getTopic().getType()) {
					calendarSyncher.unsyncCalendars(reloaded.getTopic(), singletonList(reloaded));
				} else {
					calendarSyncher.syncCalendars(reloaded.getTopic(), singletonList(reloaded));
				}
				appointmentsMailing.sendAppointmentUnconfirmed(reloaded);
			}
		}
	}

	@Override
	public void deleteAppointment(Appointment appointment) {
		calendarSyncher.unsyncCalendars(appointment.getTopic(), singletonList(appointment));
		if (Status.confirmed == appointment.getStatus() || Type.finding != appointment.getTopic().getType()) {
			appointmentsMailing.sendAppointmentDeleted(singletonList(appointment));
		}
		deleteMeeting(appointment);
		appointmentDao.delete(appointment);
	}

	@Override
	public boolean isEndAfter(Appointment appointment, Date dueDate) {
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		Date ajustedEnd = new Date(end.getTime());
		if (DateUtils.isSameDate(begin, end) && DateUtils.isSameTime(begin, end)) {
			ajustedEnd = DateUtils.setTime(ajustedEnd, 23, 59, 59);
		}
		return ajustedEnd.after(dueDate);
	}
	
	@Override
	public Map<Long, Long> getTopicKeyToAppointmentCount(AppointmentSearchParams params, boolean freeOnly) {
		return appointmentDao.loadTopicKeyToAppointmentCount(params, freeOnly);
	}
	
	@Override
	public Long getAppointmentCount(AppointmentSearchParams params) {
		return appointmentDao.loadAppointmentCount(params);
	}

	@Override
	public List<Appointment> getAppointments(AppointmentSearchParams params) {
		return appointmentDao.loadAppointments(params);
	}

	@Override
	public ParticipationResult createParticipations(Appointment appointment, Collection<Identity> identities,
			Identity createdBy, boolean multiParticipations, boolean autoConfirmation, boolean rejectIfConfirmed) {
		AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
		appointmentParams.setAppointment(appointment);
		appointmentParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(appointmentParams);
		if (appointments.isEmpty()) {
			return ParticipationResult.APPOINTMENT_DELETED;
		}
		
		Appointment reloadedAppointment = appointments.get(0);
		if (!autoConfirmation && rejectIfConfirmed && Status.confirmed == reloadedAppointment.getStatus()) {
			return ParticipationResult.APPOINTMENT_CONFIRMED;
		}
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(reloadedAppointment);
		params.setIdentities(identities);
		List<Participation> loadParticipations12 = participationDao.loadParticipations(params);
		List<Long> existingParticipationIdentityKeys = loadParticipations12.stream()
				.map(p -> p.getIdentity().getKey())
				.collect(Collectors.toList());
		List<Identity> identitesWithoutParticipation = new ArrayList<>(identities);
		identitesWithoutParticipation.removeIf(i -> existingParticipationIdentityKeys.contains(i.getKey()));
		
		if (reloadedAppointment.getMaxParticipations() != null) {
			ParticipationSearchParams participationParams = new ParticipationSearchParams();
			participationParams.setAppointmentKeys(singletonList(reloadedAppointment.getKey()));
			Long count = participationDao.loadParticipationCount(participationParams);
			if ((count + identitesWithoutParticipation.size()) > reloadedAppointment.getMaxParticipations().intValue()) {
				return ParticipationResult.APPOINTMENT_FULL;
			}
		}
		
		List<Participation> participations = new ArrayList<>(identitesWithoutParticipation.size());
		for (Identity identity: identitesWithoutParticipation) {
			if (!multiParticipations) {
				ParticipationSearchParams currentParticipationParams = new ParticipationSearchParams();
				currentParticipationParams.setTopic(reloadedAppointment.getTopic());
				currentParticipationParams.setIdentity(identity);
				currentParticipationParams.setFetchAppointments(true);
				currentParticipationParams.setFetchIdentities(true);
				List<Participation> loadParticipations = participationDao.loadParticipations(currentParticipationParams);
				loadParticipations.forEach(this::deleteParticipation);
			}
			
			Participation participation = participationDao.createParticipation(reloadedAppointment, identity, createdBy);
			participations.add(participation);
			if (Status.confirmed == appointment.getStatus() || Type.finding != appointment.getTopic().getType()) {
				calendarSyncher.syncCalendar(reloadedAppointment, identity);
			}
			if (Status.confirmed == appointment.getStatus()) {
				appointmentsMailing.sendParticipationCreated(participation);
			}
		}
		
		if (autoConfirmation) {
			confirmReloadedAppointment(reloadedAppointment, false);
		}
		
		markNews(reloadedAppointment.getTopic());
		
		return ParticipationResult.of(participations);
	}

	@Override
	public ParticipationResult rebookParticipations(AppointmentRef toAppointmentRef,
			Collection<? extends ParticipationRef> participationRefs, Identity rebookedBy, boolean autoConfirmation) {
		AppointmentSearchParams aParams = new AppointmentSearchParams();
		aParams.setAppointment(toAppointmentRef);
		aParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(aParams);
		if (appointments.isEmpty()) {
			return ParticipationResult.APPOINTMENT_DELETED;
		}
		Appointment toAppointment = appointments.get(0);
		
		ParticipationSearchParams pParams = new ParticipationSearchParams();
		pParams.setParticipations(participationRefs);
		pParams.setFetchIdentities(true);
		pParams.setFetchAppointments(true);
		List<Participation> fromParticipations = participationDao.loadParticipations(pParams);
		if (fromParticipations.isEmpty()) {
			return ParticipationResult.NO_PARTICIPATIONS;
		}

		List<Identity> fromIdentities = fromParticipations.stream()
				.map(Participation::getIdentity)
				.collect(Collectors.toList());
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(toAppointment);
		params.setIdentities(fromIdentities);
		List<Participation> loadParticipations12 = participationDao.loadParticipations(params);
		List<Long> existingParticipationIdentityKeys = loadParticipations12.stream()
				.map(p -> p.getIdentity().getKey())
				.collect(Collectors.toList());
		List<Participation> fromWithoutParticipation = new ArrayList<>(fromParticipations);
		fromWithoutParticipation.removeIf(p -> existingParticipationIdentityKeys.contains(p.getIdentity().getKey()));
		
		if (toAppointment.getMaxParticipations() != null) {
			ParticipationSearchParams participationParams = new ParticipationSearchParams();
			participationParams.setAppointment(toAppointment);
			Long count = participationDao.loadParticipationCount(participationParams);
			if ((count + fromWithoutParticipation.size()) > toAppointment.getMaxParticipations().intValue()) {
				return ParticipationResult.APPOINTMENT_FULL;
			}
		}
		
		List<Participation> participations = new ArrayList<>(fromWithoutParticipation.size());
		for (Participation fromParticipation : fromWithoutParticipation) {
			Identity identity = fromParticipation.getIdentity();
			Participation participation = participationDao.createParticipation(toAppointment, identity, rebookedBy);
			participations.add(participation);

			if (Status.confirmed == toAppointment.getStatus() || Type.finding != toAppointment.getTopic().getType()) {
				calendarSyncher.syncCalendar(toAppointment, identity);
			}
		}
		appointmentsMailing.sendRebook(toAppointment, fromParticipations);

		if (autoConfirmation) {
			confirmReloadedAppointment(toAppointment, false);
		}
		
		// Delete after send email to have the from participations informations in the email
		fromParticipations.forEach(this::deleteParticipation);

		markNews(toAppointment.getTopic());
		
		return ParticipationResult.of(participations);
	}
	
	@Override
	public void deleteParticipations(Collection<? extends ParticipationRef> participationRefs, boolean sendEmail) {
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setParticipations(participationRefs);
		participationParams.setFetchAppointments(true);
		participationParams.setFetchIdentities(true);
		List<Participation> loadParticipations = participationDao.loadParticipations(participationParams);
		loadParticipations.forEach(participation -> deleteParticipation(participation, sendEmail));
	}

	@Override
	public void deleteParticipation(Participation participation) {
		deleteParticipation(participation, false);
	}
	
	public void deleteParticipation(Participation participation, boolean sendEmail) {
		Appointment appointment = participation.getAppointment();
		calendarSyncher.unsyncCalendar(appointment, participation.getIdentity());
		if (sendEmail && Status.confirmed == appointment.getStatus()) {
			appointmentsMailing.sendParticipationDeleted(participation);
		}
		participationDao.delete(participation);
	}

	@Override
	public Long getParticipationCount(ParticipationSearchParams params) {
		return participationDao.loadParticipationCount(params);
	}

	@Override
	public List<Participation> getParticipations(ParticipationSearchParams params) {
		return participationDao.loadParticipations(params);
	}

	@Override
	public PublisherData getPublisherData(RepositoryEntry entry, String subIdent) {
		String businessPath = createBussinesPath(entry.getKey(), subIdent);
		return new PublisherData(AppointmentsNotificationsHandler.TYPE, "", businessPath);
	}

	@Override
	public SubscriptionContext getSubscriptionContext(RepositoryEntry entry, String subIdent) {
		ICourse course = CourseFactory.loadCourse(entry);
		Long courseResourceableId = course.getCourseEnvironment().getCourseGroupManager().getCourseResource().getResourceableId();
		return new SubscriptionContext("CourseModule", courseResourceableId, subIdent);
	}
	
	private void markNews(Topic topic) {
		SubscriptionContext markedCtxt = getSubscriptionContext(topic.getEntry(), topic.getSubIdent());
		notificationsManager.markPublisherNews(markedCtxt, null, false);	
	}

	@Override
	public String createBussinesPath(Long entryKey, String subIdent) {
		return "[RepositoryEntry:" + entryKey + "][CourseNode:" + subIdent + "]";
	}
	
	@Override
	public boolean isBigBlueButtonEnabled() {
		return bigBlueButtonModule.isEnabled() && bigBlueButtonModule.isAppointmentsEnabled();
	}

	@Override
	public List<BigBlueButtonMeetingTemplate> getBigBlueButtonTemplates(Topic topic, Identity identity, Roles roles,
			Long selectedTemplateKey) {
		RepositoryEntry entry = repositoryService.loadByKey(topic.getEntry().getKey());
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager.calculatePermissions(entry, null, identity, roles);
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		
		List<BigBlueButtonMeetingTemplate> identityTemplates = new ArrayList<>();
		for (BigBlueButtonMeetingTemplate template:templates) {
			if ((template.isEnabled() && template.availableTo(permissions)) || template.getKey().equals(selectedTemplateKey)) {
				identityTemplates.add(template);
			}
		}
		
		return identityTemplates;
	}

	@Override
	public Appointment addMeeting(Appointment appointment, Identity identity) {
		Topic topic = appointment.getTopic();
		RepositoryEntry entry = repositoryService.loadByKey(topic.getEntry().getKey());
		String name = topic.getTitle();
		String subIdent = topic.getSubIdent();
		BigBlueButtonMeeting meeting = bigBlueButtonManager.createAndPersistMeeting(name, entry, subIdent, null, identity);
		meeting.setRecordingsPublishingEnum(BigBlueButtonRecordingsPublishingEnum.auto);
		return appointmentDao.saveAppointment(appointment, meeting);
	}

	@Override
	public Appointment removeMeeting(Appointment appointment) {
		Appointment updated = appointment;
		BigBlueButtonMeeting meeting = appointment.getMeeting();
		if (meeting != null) {
			updated = appointmentDao.saveAppointment(appointment, null);
			BigBlueButtonErrors errors = new BigBlueButtonErrors();
			bigBlueButtonManager.deleteMeeting(meeting, errors);
		}
		return updated;
	}

	@Override
	public void onBeforeDelete(BigBlueButtonMeeting meeting) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setMeeting(meeting);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		if (!appointments.isEmpty()) {
			Appointment appointment = appointments.get(0);
			removeMeeting(appointment);
		}
	}
	
	private void deleteMeeting(Appointment appointment) {
		BigBlueButtonMeeting meeting = appointment.getMeeting();
		if (meeting != null) {
			BigBlueButtonErrors errors = new BigBlueButtonErrors();
			bigBlueButtonManager.deleteMeeting(meeting, errors);
		}
	}
	
	private void deleteMeetings(Collection<Appointment> appointments) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		appointments.stream()
				.map(Appointment::getMeeting)
				.filter(Objects::nonNull)
				.forEach(meeting -> bigBlueButtonManager.deleteMeeting(meeting, errors));
	}

	@Override
	public String joinMeeting(Appointment appointment, Identity identity, BigBlueButtonErrors errors) {
		// Check server
		if (!isBigBlueButtonEnabled()) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.serverDisabled));
			return null;
		}
		
		// Check access
		boolean organizer = organizerDao.loadOrganizers(appointment.getTopic()).stream()
				.anyMatch(o -> o.getIdentity().getKey().equals(identity.getKey()));
		boolean participation = false;
		if (!organizer) {
			ParticipationSearchParams params = new ParticipationSearchParams();
			params.setAppointment(appointment);
			params.setIdentity(identity);
			participation = participationDao.loadParticipationCount(params).longValue() > 0;
		}
		if (!organizer && !participation) {
			errors.append(new BigBlueButtonError(BigBlueButtonErrorCodes.noAccess));
			return null;
		}
		
		BigBlueButtonAttendeeRoles role = organizer? BigBlueButtonAttendeeRoles.moderator: BigBlueButtonAttendeeRoles.viewer;
		return bigBlueButtonManager.join(appointment.getMeeting(), identity, null, role, null, errors);
	}

	@Override
	public String getMainPresenters(Topic topic) {
		List<Long> identityKeys = getOrganizers(topic).stream()
				.map(organizer -> organizer.getIdentity().getKey())
				.collect(Collectors.toList());
		return getMainPresenters(identityKeys);
	}
	
	private String getMainPresenters(Collection<Long> identityKeys) {
		return identityKeys.stream()
				.map(key -> userManager.getUserDisplayName(key))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.joining(" / "));
	}

	@Override
	public void syncRecorings(Topic topic) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		appointmentDao.loadAppointments(params).stream()
				.map(Appointment::getMeeting)
				.filter(Objects::nonNull)
				.forEach(meeting -> bigBlueButtonManager.getRecordingAndReferences(meeting, errors));
	}

	@Override
	public Map<Long, List<BigBlueButtonRecordingReference>> getRecordingReferences(List<Appointment> appointments) {
		List<BigBlueButtonMeeting> meetings = appointments.stream()
				.map(Appointment::getMeeting)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		
		// Sync recording of running and of just finished meetings
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		Predicate<BigBlueButtonMeeting> isRecordingSyncNeeded = getIsRecordingSyncNeeded();
		meetings.stream()
				.filter(isRecordingSyncNeeded)
				.forEach(meeting -> bigBlueButtonManager.getRecordingAndReferences(meeting, errors));
		
		// Get the recordings of all meetings from database
		List<BigBlueButtonMeeting> allMeetings = meetings.stream()
				.collect(Collectors.toList());
		Map<Long, List<BigBlueButtonRecordingReference>> meetingKeyToRecordings = bigBlueButtonManager
				.getRecordingReferences(allMeetings).stream()
				.collect(Collectors.groupingBy(recording -> recording.getMeeting().getKey()));
		
		Map<Long, List<BigBlueButtonRecordingReference>> appointmentKeyToRecordings = new HashMap<>();
		for (Appointment appointment : appointments) {
			if (appointment.getMeeting() != null) {
				List<BigBlueButtonRecordingReference> recordings = meetingKeyToRecordings.get(appointment.getMeeting().getKey());
				if (recordings != null) {
					appointmentKeyToRecordings.put(appointment.getKey(), recordings);
				}
			}
		}
		 
		return appointmentKeyToRecordings;
	}

	private Predicate<BigBlueButtonMeeting> getIsRecordingSyncNeeded() {
		Date now = new Date();
		Date oneHourInPast = DateUtils.addHours(now, -1);
		// The meeting has started and has not yet or recently (not more than one hour ago) ended.
		return (BigBlueButtonMeeting meeting) -> (meeting.getStartDate().before(now) && meeting.getEndDate().after(oneHourInPast));
	}

	@Override
	public String getRecordingUrl(UserSession usess, BigBlueButtonRecordingReference recordingReference) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		Optional<BigBlueButtonRecording> recording = bigBlueButtonManager.getRecordingAndReferences(recordingReference.getMeeting(), errors).stream()
				.filter(rr -> recordingReference.getRecordingId().equals(rr.getRecording().getRecordId()))
				.map(BigBlueButtonRecordingWithReference::getRecording)
				.findFirst();
		return recording.isPresent()? bigBlueButtonManager.getRecordingUrl(usess, recording.get()): null;
	}

}
