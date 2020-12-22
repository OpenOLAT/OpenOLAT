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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.04.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TopicDAO topicDao;
	@Autowired
	private OrganizerDAO organizerDao;
	@Autowired
	private ParticipationDAO participationDao;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private TeamsService teamsService;
	
	@Autowired
	private AppointmentDAO sut;
	
	@Test
	public void shouldCreateUnsavedAppointment() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createUnsavedAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(appointment).isNotNull();
		softly.assertThat(appointment.getKey()).isNull(); // unsaved
		softly.assertThat(appointment.getCreationDate()).isNotNull();
		softly.assertThat(appointment.getLastModified()).isNotNull();
		softly.assertThat(appointment.getTopic()).isEqualTo(topic);
		softly.assertThat(appointment.getStatus()).isEqualTo(Appointment.Status.planned);
		softly.assertThat(appointment.getStatusModified()).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldCreateSavedAppointment() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(appointment).isNotNull();
		softly.assertThat(appointment.getKey()).isNotNull();
		softly.assertThat(appointment.getCreationDate()).isNotNull();
		softly.assertThat(appointment.getLastModified()).isNotNull();
		softly.assertThat(appointment.getTopic()).isEqualTo(topic);
		softly.assertThat(appointment.getStatus()).isEqualTo(Appointment.Status.planned);
		softly.assertThat(appointment.getStatusModified()).isNotNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldSaveNewAppointment() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createUnsavedAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.saveAppointment(appointment);
		
		assertThat(appointment.getKey()).isNotNull();
	}

	@Test
	public void shouldSaveAppointment() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		Date start = DateUtils.toDate(LocalDateTime.of(2020, 3, 3, 8, 0));
		appointment.setStart(start);
		Date end = DateUtils.toDate(LocalDateTime.of(2020, 3, 3, 10, 0));
		appointment.setEnd(end);
		String location = random();
		appointment.setLocation(location);
		String details = random();
		appointment.setDetails(details);
		Integer maxParticipations = 5;
		appointment.setMaxParticipations(maxParticipations);
		sut.saveAppointment(appointment);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(appointment.getStart()).isCloseTo(start, 1000);
		softly.assertThat(appointment.getEnd()).isCloseTo(end, 1000);
		softly.assertThat(appointment.getLocation()).isEqualTo(location);
		softly.assertThat(appointment.getDetails()).isEqualTo(details);
		softly.assertThat(appointment.getMaxParticipations()).isEqualTo(maxParticipations);
		softly.assertAll();
	}
	
	@Test
	public void shouldSaveMeetingRef() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getBBBMeeting()).isNull();
		
		// Add meeting
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		BigBlueButtonMeeting meeting = bigBlueButtonManager.createAndPersistMeeting(random(), entry, null, null, identity);
		appointment = sut.saveAppointment(appointment, meeting, null);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getBBBMeeting()).isEqualTo(meeting);
		
		// Remove meeting
		appointment = sut.saveAppointment(appointment, null, null);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getBBBMeeting()).isNull();
	}
	
	@Test
	public void shouldSaveTeamsMeetingRef() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getTeamsMeeting()).isNull();
		
		// Add meeting
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		TeamsMeeting meeting = teamsService.createMeeting(random(), new Date(), new Date(), entry, null, null, identity);
		appointment = sut.saveAppointment(appointment, null, meeting);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getTeamsMeeting()).isEqualTo(meeting);
		
		// Remove meeting
		appointment = sut.saveAppointment(appointment, null, null);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getTeamsMeeting()).isNull();
	}
	
	@Test
	public void shouldUpdateStatus() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		sut.updateStatus(appointment, Status.confirmed);
		dbInstance.commitAndCloseSession();
		
		appointment = sut.loadByKey(appointment.getKey());
		assertThat(appointment.getStatus()).isEqualTo(Status.confirmed);
	}
	
	@Test
	public void shouldDelete() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		sut.delete(appointment);
		
		Appointment reloadedAppointment = sut.loadByKey(appointment.getKey());
		assertThat(reloadedAppointment).isNull();
	}
	
	@Test
	public void shouldDeleteByTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Topic topic1 = topicDao.createTopic(entry, subIdent);
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Appointment appointment11 = sut.createAppointment(topic1);
		Appointment appointment12 = sut.createAppointment(topic1);
		Appointment appointment21 = sut.createAppointment(topic2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(topic1);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams searchParams = new AppointmentSearchParams();
		searchParams.setEntry(entry);
		searchParams.setSubIdent(subIdent);
		List<Appointment> reloadedAppointments = sut.loadAppointments(searchParams);
		assertThat(reloadedAppointments)
				.containsExactly(appointment21)
				.doesNotContain(appointment11, appointment12);
	}
	
	@Test
	public void shouldDeleteByEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Topic topic1 = topicDao.createTopic(entry, random());
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Topic topic3 = topicDao.createTopic(entry, random());
		Appointment appointment1 = sut.createAppointment(topic1);
		Appointment appointment2 = sut.createAppointment(topic2);
		Appointment appointment3 = sut.createAppointment(topic3);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry, subIdent);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams searchParams = new AppointmentSearchParams();
		searchParams.setEntry(entry);
		List<Appointment> reloadedAppointments = sut.loadAppointments(searchParams);
		assertThat(reloadedAppointments)
				.containsExactlyInAnyOrder(appointment1, appointment3)
				.doesNotContain(appointment2);
	}
	
	@Test
	public void shouldLoadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		Appointment reloadedAppointment = sut.loadByKey(appointment.getKey());
		
		assertThat(reloadedAppointment).isEqualTo(appointment);
	}
	
	@Test
	public void shouldLoadTopicKeyToAppointmentCount() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		String subIdent = JunitTestHelper.random();
		Topic topic1 = topicDao.createTopic(entry, subIdent);
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Appointment appointment11 = sut.createAppointment(topic1);
		sut.createAppointment(topic1);
		Appointment appointment21 = sut.createAppointment(topic2);
		appointment21.setMaxParticipations(1);
		sut.saveAppointment(appointment21);
		participationDao.createParticipation(appointment11, identity, identity);
		participationDao.createParticipation(appointment11, identity, identity);
		participationDao.createParticipation(appointment21, identity, identity);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		Map<Long, Long> appointmentCountByTopic = sut.loadTopicKeyToAppointmentCount(params, false);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(appointmentCountByTopic.get(topic1.getKey())).isEqualTo(2);
		softly.assertThat(appointmentCountByTopic.get(topic2.getKey())).isEqualTo(1);
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadTopicKeyToAppointmentCountOnlyFree() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		String subIdent = JunitTestHelper.random();
		Topic topic = topicDao.createTopic(entry, subIdent);

		Appointment noLimitNoParticipations = sut.createAppointment(topic);
		Appointment noLimitWithParticipations = sut.createAppointment(topic);
		Appointment limitNoParticipations = sut.createAppointment(topic);
		Appointment limitFull = sut.createAppointment(topic);
		Appointment limitNotFull = sut.createAppointment(topic);
		noLimitNoParticipations.setDetails("noLimitNoParticipations");
		sut.saveAppointment(noLimitNoParticipations);
		noLimitWithParticipations.setDetails("noLimitWithParticipations");
		sut.saveAppointment(noLimitWithParticipations);
		limitNoParticipations.setDetails("limitNoParticipations");
		limitNoParticipations.setMaxParticipations(2);
		sut.saveAppointment(limitNoParticipations);
		limitFull.setDetails("limitFull");
		limitFull.setMaxParticipations(1);
		sut.saveAppointment(limitFull);
		limitNotFull.setDetails("limitNotFull");
		limitNotFull.setMaxParticipations(3);
		sut.saveAppointment(limitNotFull);
		participationDao.createParticipation(noLimitWithParticipations, identity, identity);
		participationDao.createParticipation(noLimitWithParticipations, identity, identity);
		participationDao.createParticipation(limitFull, identity, identity);
		participationDao.createParticipation(limitNotFull, identity, identity);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		Map<Long, Long> appointmentCountByTopic = sut.loadTopicKeyToAppointmentCount(params, true);
		
		assertThat(appointmentCountByTopic.get(topic.getKey())).isEqualTo(4);
	}
	
	@Test
	public void shouldLoadCount() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Topic topicOther = topicDao.createTopic(entry, random());
		sut.createAppointment(topic);
		sut.createAppointment(topic);
		sut.createAppointment(topicOther);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		Long count = sut.loadAppointmentCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoadByAppointmentKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Appointment appointment = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment);
	}
	
	@Test
	public void shouldLoadByTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Topic topicOther = topicDao.createTopic(entry, random());
		Appointment appointment1 = sut.createAppointment(topic);
		Appointment appointment2 = sut.createAppointment(topic);
		Appointment appointmentOther = sut.createAppointment(topicOther);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment1, appointment2)
				.doesNotContain(appointmentOther);
	}
	
	@Test
	public void shouldLoadByRepository() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entryOther = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		String subIdentOther = random();
		Topic topic1 = topicDao.createTopic(entry, subIdent);
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Topic topicOtherEntry = topicDao.createTopic(entryOther, subIdent);
		Topic topicOtherSubIdent = topicDao.createTopic(entry, subIdentOther);
		Appointment appointment1 = sut.createAppointment(topic1);
		Appointment appointment2 = sut.createAppointment(topic1);
		Appointment appointment3 = sut.createAppointment(topic2);
		Appointment appointmentOtherEntry = sut.createAppointment(topicOtherEntry);
		Appointment appointmentOtherSubIdent = sut.createAppointment(topicOtherSubIdent);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		params.setFetchTopic(true);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment1, appointment2, appointment3)
				.doesNotContain(appointmentOtherEntry, appointmentOtherSubIdent);
	}
	
	@Test
	public void shouldLoadByOrganizer() {
		Identity organizer = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity organizerOther = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic1 = topicDao.createTopic(entry, random());
		Topic topic2 = topicDao.createTopic(entry, random());
		Topic topicNoOrganizer = topicDao.createTopic(entry, random());
		Topic topicOtherOrganizer = topicDao.createTopic(entry, random());
		organizerDao.createOrganizer(topic1, organizer);
		organizerDao.createOrganizer(topic2, organizer);
		organizerDao.createOrganizer(topicOtherOrganizer, organizerOther);
		Appointment appointment11 = sut.createAppointment(topic1);
		Appointment appointment12 = sut.createAppointment(topic1);
		Appointment appointment21 = sut.createAppointment(topic2);
		Appointment appointmentNoOrganizer = sut.createAppointment(topicNoOrganizer);
		Appointment appointmentOtherOrganizer = sut.createAppointment(topicOtherOrganizer);
		dbInstance.closeSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setOrganizer(organizer);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment11, appointment12, appointment21)
				.doesNotContain(appointmentNoOrganizer, appointmentOtherOrganizer);
	}
	
	@Test
	public void shouldLoadByStartAfter() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Appointment appointment1 = sut.createAppointment(topic);
		appointment1.setStart(new GregorianCalendar(2030, 1, 1).getTime());
		sut.saveAppointment(appointment1);
		Appointment appointment2 = sut.createAppointment(topic);
		appointment2.setStart(new GregorianCalendar(2029, 1, 1).getTime());
		sut.saveAppointment(appointment2);
		Appointment appointment3 = sut.createAppointment(topic);
		appointment3.setStart(new GregorianCalendar(2010, 1, 1).getTime());
		sut.saveAppointment(appointment3);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setStartAfter(new GregorianCalendar(2020, 1, 1).getTime());
		params.setTopic(topic);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment1, appointment2)
				.doesNotContain(appointment3);
	}
	
	@Test
	public void shouldLoadByStatus() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Appointment appointment1 = sut.createAppointment(topic);
		sut.updateStatus(appointment1, Status.confirmed);
		Appointment appointment2 = sut.createAppointment(topic);
		sut.updateStatus(appointment2, Status.confirmed);
		sut.saveAppointment(appointment2);
		Appointment appointmentPlanning = sut.createAppointment(topic);
		sut.updateStatus(appointmentPlanning, Status.planned);
		sut.saveAppointment(appointmentPlanning);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setStatus(Status.confirmed);
		params.setTopic(topic);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment1, appointment2)
				.doesNotContain(appointmentPlanning);
	}
	
	@Test
	public void shouldLoadByBBBMeeting() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		BigBlueButtonMeeting meeting1 = bigBlueButtonManager.createAndPersistMeeting(random(), entry, null, null, identity);
		BigBlueButtonMeeting meetingOther = bigBlueButtonManager.createAndPersistMeeting(random(), entry, null, null, identity);
		
		Appointment appointment = sut.createAppointment(topic);
		sut.saveAppointment(appointment, meeting1, null);
		Appointment appointmentOtherMeeting = sut.createAppointment(topic);
		sut.saveAppointment(appointmentOtherMeeting, meetingOther, null);
		Appointment appointmentNoMeeting = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setBBBMeeting(meeting1);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment)
				.doesNotContain(appointmentOtherMeeting, appointmentNoMeeting);
	}
	
	@Test
	public void shouldLoadByTeamsMeeting() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		TeamsMeeting meeting = teamsService.createMeeting(random(), new Date(), new Date(), entry, null, null, identity);
		TeamsMeeting meetingOther = teamsService.createMeeting(random(), new Date(), new Date(), entry, null, null, identity);
		
		Appointment appointment = sut.createAppointment(topic);
		sut.saveAppointment(appointment, null, meeting);
		Appointment appointmentOtherMeeting = sut.createAppointment(topic);
		sut.saveAppointment(appointmentOtherMeeting, null, meetingOther);
		Appointment appointmentNoMeeting = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTeamsMeeting(meeting);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(appointment)
				.doesNotContain(appointmentOtherMeeting, appointmentNoMeeting);
	}
	
	@Test
	public void shouldLoadByHasMeeting() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		BigBlueButtonMeeting bbbMeeting1 = bigBlueButtonManager.createAndPersistMeeting(random(), entry, null, null, identity);
		BigBlueButtonMeeting bbbMeeting2 = bigBlueButtonManager.createAndPersistMeeting(random(), entry, null, null, identity);
		TeamsMeeting teamsMeeting = teamsService.createMeeting(random(), new Date(), new Date(), entry, null, null, identity);
		
		Appointment bbbAppointment1 = sut.createAppointment(topic);
		sut.saveAppointment(bbbAppointment1, bbbMeeting1, null);
		Appointment bbbAppointment2 = sut.createAppointment(topic);
		sut.saveAppointment(bbbAppointment2, bbbMeeting2, null);
		Appointment teamsAppointment = sut.createAppointment(topic);
		sut.saveAppointment(teamsAppointment, null, teamsMeeting);
		Appointment appointmentNoMeeting = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(topic);
		params.setHasMeeting(true);
		List<Appointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments)
				.containsExactlyInAnyOrder(bbbAppointment1, bbbAppointment2, teamsAppointment)
				.doesNotContain(appointmentNoMeeting);
	}

}
