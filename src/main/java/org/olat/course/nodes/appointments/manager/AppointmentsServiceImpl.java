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
package org.olat.course.nodes.appointments.manager;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.Appointment.Status;
import org.olat.course.nodes.appointments.AppointmentRef;
import org.olat.course.nodes.appointments.AppointmentSearchParams;
import org.olat.course.nodes.appointments.AppointmentsService;
import org.olat.course.nodes.appointments.Organizer;
import org.olat.course.nodes.appointments.Participation;
import org.olat.course.nodes.appointments.ParticipationRef;
import org.olat.course.nodes.appointments.ParticipationResult;
import org.olat.course.nodes.appointments.ParticipationSearchParams;
import org.olat.course.nodes.appointments.Topic;
import org.olat.course.nodes.appointments.TopicRef;
import org.olat.course.nodes.appointments.TopicToGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AppointmentsServiceImpl implements AppointmentsService {
	
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
	private BaseSecurityManager securityManager;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public Topic createTopic(RepositoryEntry entry, String subIdent) {
		return topicDao.createTopic(entry, subIdent);
	}

	@Override
	public Topic updateTopic(Topic topic) {
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setStatus(Status.confirmed);
		params.setFetchTopic(true);
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
		params.setStatus(Status.confirmed);
		params.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		appointmentsMailing.sendAppointmentDeleted(appointments);
		
		List<Organizer> organizers = organizerDao.loadOrganizers(entry, subIdent);
		appointmentsMailing.sendAppointmentsDeleted(appointments, organizers);
		
		List<Topic> topics = topicDao.loadTopics(entry, subIdent);
		for (Topic topic : topics) {
			deleteTopicGroup(topic);
		}
		participationDao.delete(entry, subIdent);
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
		params.setStatus(Status.confirmed);
		params.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		appointmentsMailing.sendAppointmentDeleted(appointments);
		
		List<Organizer> organizers = organizerDao.loadOrganizers(topicRef);
		appointmentsMailing.sendAppointmentsDeleted(appointments, organizers);
		
		Topic topic = topicDao.loadByKey(topicRef.getKey());
		deleteTopicGroup(topic);
		topicToGroupDao.delete(topicRef);
		participationDao.delete(topicRef);
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
	public Organizer createOrganizer(Topic topic, Identity identity) {
		Organizer createOrganizer = organizerDao.createOrganizer(topic, identity);
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setStatus(Status.confirmed);
		params.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(params);
		calendarSyncher.syncCalendar(appointments, identity);
		
		return createOrganizer;
	}

	@Override
	public void deleteOrganizers(TopicRef topic, Collection<Organizer> organizers) {
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
		Appointment saveAppointment = appointmentDao.saveAppointment(appointment);
		calendarSyncher.syncCalendars(appointment.getTopic(), singletonList(appointment));
		return saveAppointment;
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
	}

	@Override
	public void confirmAppointments(RepositoryEntry entry, String subIdent) {
		AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
		appointmentParams.setEntry(entry);
		appointmentParams.setSubIdent(subIdent);
		appointmentParams.setStatus(Status.planned);
		appointmentParams.setFetchTopic(true);
		appointmentDao.loadAppointments(appointmentParams).stream()
				.forEach(appointment -> confirmReloadedAppointment(appointment, false));
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
				calendarSyncher.syncCalendars(reloaded.getTopic(), singletonList(reloaded));
				appointmentsMailing.sendAppointmentUnconfirmed(reloaded);
			}
		}
	}

	@Override
	public void deleteAppointment(Appointment appointment) {
		calendarSyncher.unsyncCalendars(appointment.getTopic(), singletonList(appointment));
		appointmentsMailing.sendAppointmentDeleted(singletonList(appointment));
		appointmentDao.delete(appointment);
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
			Identity createdBy, boolean multiParticipations, boolean autoConfirmation) {
		AppointmentSearchParams appointmentParams = new AppointmentSearchParams();
		appointmentParams.setAppointment(appointment);
		appointmentParams.setFetchTopic(true);
		List<Appointment> appointments = appointmentDao.loadAppointments(appointmentParams);
		if (appointments.isEmpty()) {
			return ParticipationResult.APPOINTMENT_DELETED;
		}
		
		Appointment reloadedAppointment = appointments.get(0);
		if (!autoConfirmation && Status.confirmed == reloadedAppointment.getStatus()) {
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
				loadParticipations.forEach(currentParticipation -> deleteParticipation(currentParticipation));
			}
			
			Participation participation = participationDao.createParticipation(reloadedAppointment, identity, createdBy);
			participations.add(participation);
			calendarSyncher.syncCalendar(reloadedAppointment, identity);
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
			calendarSyncher.syncCalendar(toAppointment, identity);
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
	public void deleteParticipations(Collection<? extends ParticipationRef> participationRefs) {
		ParticipationSearchParams participationParams = new ParticipationSearchParams();
		participationParams.setParticipations(participationRefs);
		participationParams.setFetchAppointments(true);
		participationParams.setFetchIdentities(true);
		List<Participation> loadParticipations = participationDao.loadParticipations(participationParams);
		loadParticipations.forEach(participation -> deleteParticipation(participation));
	}

	@Override
	public void deleteParticipation(Participation participation) {
		calendarSyncher.unsyncCalendar(participation.getAppointment(), participation.getIdentity());
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

}
