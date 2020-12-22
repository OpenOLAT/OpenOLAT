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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.appointments.ParticipationResult.Status;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Jun 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private AppointmentsService sut;
	
	@Test
	public void shouldUpdateOrganizers() {
		Identity organizerDelete = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity organizerKeep = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity organizerNew1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity organizerNew2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		sut.updateOrganizers(topic, asList(organizerDelete, organizerKeep));
		dbInstance.commitAndCloseSession();
		
		sut.updateOrganizers(topic, asList(organizerKeep, organizerNew1, organizerNew2));
		dbInstance.commitAndCloseSession();
		
		List<Organizer> organizers = sut.getOrganizers(topic);
		assertThat(organizers).extracting(Organizer::getIdentity)
				.containsExactlyInAnyOrder(
						organizerKeep,
						organizerNew1,
						organizerNew2)
				.doesNotContain(
						organizerDelete);
	}
	
	@Test
	public void shouldAddBBBMeetings() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		topic = sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		Appointment appointment = sut.createUnsavedAppointment(topic);
		Date start = new GregorianCalendar(2020, 10, 4, 10, 15, 00).getTime();
		appointment.setStart(start);
		Date end = new GregorianCalendar(2020, 10, 4, 11, 30, 00).getTime();
		appointment.setEnd(end);
		appointment = sut.saveAppointment(appointment);
		appointment = sut.addBBBMeeting(appointment, identity);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		BigBlueButtonMeeting bbbMeeting = sut.getAppointments(params).get(0).getBBBMeeting();
		softly.assertThat(bbbMeeting.getName()).as("Name").isEqualTo(title);
		softly.assertThat(bbbMeeting.getDescription()).as("Description").isEqualTo(description);
		softly.assertThat(bbbMeeting.getStartDate()).as("Start").isCloseTo(start, 1000);
		softly.assertThat(bbbMeeting.getEndDate()).as("End").isCloseTo(end, 1000);
		softly.assertAll();
	}
	
	@Test
	public void shouldAddTeamsMeetings() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		topic = sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		Appointment appointment = sut.createUnsavedAppointment(topic);
		Date start = new GregorianCalendar(2020, 10, 4, 10, 15, 00).getTime();
		appointment.setStart(start);
		Date end = new GregorianCalendar(2020, 10, 4, 11, 30, 00).getTime();
		appointment.setEnd(end);
		appointment = sut.saveAppointment(appointment);
		appointment = sut.addTeamsMeeting(appointment, identity);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		TeamsMeeting teamsMeeting = sut.getAppointments(params).get(0).getTeamsMeeting();
		softly.assertThat(teamsMeeting.getSubject()).as("Subject").isEqualTo(title);
		softly.assertThat(teamsMeeting.getDescription()).as("Description").isEqualTo(description);
		softly.assertThat(teamsMeeting.getStartDate()).as("Start").isCloseTo(start, 1000);
		softly.assertThat(teamsMeeting.getEndDate()).as("End").isCloseTo(end, 1000);
		softly.assertAll();
	}
	
	@Test
	public void shouldSyncTopicToBBBMeetings() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		topic.setTitle(random());
		topic.setDescription(random());
		topic = sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		Appointment appointment = sut.createUnsavedAppointment(topic);
		appointment.setStart(new Date());
		appointment.setEnd(new Date());
		appointment = sut.saveAppointment(appointment);
		appointment = sut.addBBBMeeting(appointment, identity);
		dbInstance.commitAndCloseSession();
		
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		topic = sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		BigBlueButtonMeeting bbbMeeting = sut.getAppointments(params).get(0).getBBBMeeting();
		softly.assertThat(bbbMeeting.getName()).as("Name").isEqualTo(title);
		softly.assertThat(bbbMeeting.getDescription()).as("Description").isEqualTo(description);
		softly.assertAll();
	}
	
	@Test
	public void shouldSyncTopicToTeamsMeetings() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		topic.setTitle(random());
		topic.setDescription(random());
		topic = sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		Appointment appointment = sut.createUnsavedAppointment(topic);
		appointment.setStart(new Date());
		appointment.setEnd(new Date());
		appointment = sut.saveAppointment(appointment);
		appointment = sut.addTeamsMeeting(appointment, identity);
		dbInstance.commitAndCloseSession();
		
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		topic = sut.updateTopic(topic);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		TeamsMeeting teamsMeeting = sut.getAppointments(params).get(0).getTeamsMeeting();
		softly.assertThat(teamsMeeting.getSubject()).as("Name").isEqualTo(title);
		softly.assertThat(teamsMeeting.getDescription()).as("Description").isEqualTo(description);
		softly.assertAll();
	}
	
	@Test
	public void shouldCheckEndAfterDueDate() {
		Appointment appointment = sut.createUnsavedAppointment(null);
		appointment.setStart(new GregorianCalendar(2020, 2, 3, 10, 0, 0).getTime());
		appointment.setEnd(new GregorianCalendar(2020, 2, 3, 11, 0, 0).getTime());
		assertThat(sut.isEndAfter(appointment, new GregorianCalendar(2020, 2, 3, 12, 0, 0).getTime())).isFalse();
		
		appointment = sut.createUnsavedAppointment(null);
		appointment.setStart(new GregorianCalendar(2020, 2, 3, 10, 0, 0).getTime());
		appointment.setEnd(new GregorianCalendar(2020, 2, 3, 14, 0, 0).getTime());
		assertThat(sut.isEndAfter(appointment, new GregorianCalendar(2020, 2, 3, 12, 0, 0).getTime())).isTrue();
		
		// full day
		appointment = sut.createUnsavedAppointment(null);
		appointment.setStart(new GregorianCalendar(2020, 2, 3, 10, 0, 0).getTime());
		appointment.setEnd(new GregorianCalendar(2020, 2, 3, 10, 0, 0).getTime());
		assertThat(sut.isEndAfter(appointment, new GregorianCalendar(2020, 2, 3, 12, 0, 0).getTime())).isTrue();
	}
	
	@Test
	public void createParticipationShouldCreateParticiption() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment appointment = createRandomAppointment();
		dbInstance.commitAndCloseSession();
		
		ParticipationResult result = sut.createParticipations(appointment, asList(participant1, participant2), participant1, true, false, true);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.getStatus()).isEqualTo(Status.ok);
		softly.assertThat(result.getParticipations()).hasSize(2);
		List<Identity> identities = result.getParticipations().stream().map(Participation::getIdentity).collect(Collectors.toList());
		softly.assertThat(identities).containsExactly(participant1, participant2);
		softly.assertAll();
	}

	@Test
	public void createParticipationShouldNotCreateParticipationIfAppointmentDeleted() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment appointment = createRandomAppointment();
		dbInstance.commitAndCloseSession();
		sut.deleteAppointment(appointment);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult result = sut.createParticipations(appointment, singletonList(participant), participant, true, false, true);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.getStatus()).isEqualTo(Status.appointmentDeleted);
		softly.assertThat(result.getParticipations()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void createParticipationShouldNotCreateParticipationIfConfirmed() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment appointment = createRandomAppointment();
		dbInstance.commitAndCloseSession();
		sut.confirmAppointment(appointment);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult recetResult = sut.createParticipations(appointment, singletonList(participant), participant, true, false, true);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(recetResult.getStatus()).isEqualTo(Status.appointmentConfirmed);
		softly.assertThat(recetResult.getParticipations()).isNull();
		
		ParticipationResult createResult = sut.createParticipations(appointment, singletonList(participant), participant, true, false, false);
		softly.assertThat(createResult.getStatus()).isEqualTo(Status.ok);
		
		softly.assertAll();
	}
	
	@Test
	public void createParticipationShouldNotCreateParticipationIfNoFreePlaces() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participantA = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participantB = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment appointment = createRandomAppointment();
		appointment.setMaxParticipations(3);
		sut.saveAppointment(appointment);
		dbInstance.commitAndCloseSession();
		sut.createParticipations(appointment, singletonList(participantA), participant1, true, false, true);
		sut.createParticipations(appointment, singletonList(participantB), participant2, true, false, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult result = sut.createParticipations(appointment, asList(participant1, participant2), participant1, true, false, true);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.getStatus()).isEqualTo(Status.appointmentFull);
		softly.assertThat(result.getParticipations()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void createParticipationShouldNotCreateParticipationIfExisting() {
		Identity participantTwice = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment appointment = createRandomAppointment();
		appointment.setMaxParticipations(3);
		sut.saveAppointment(appointment);
		dbInstance.commitAndCloseSession();
		sut.createParticipations(appointment, singletonList(participantTwice), participantTwice, true, false, true);
		sut.createParticipations(appointment, singletonList(participant1), participant1, true, false, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult result = sut.createParticipations(appointment, asList(participant2, participantTwice), participantTwice, true, false, true);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(result.getStatus()).isEqualTo(Status.ok);
		softly.assertThat(result.getParticipations()).hasSize(1);
		if (!result.getParticipations().isEmpty()) {
			softly.assertThat(result.getParticipations().get(0).getIdentity()).isEqualTo(participant2);
		}
		softly.assertAll();
	}

	@Test
	public void createParticipationShouldAutoconfirm() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment appointment = createRandomAppointment();
		dbInstance.commitAndCloseSession();
		
		sut.createParticipations(appointment, singletonList(participant), participant, true, true, true);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(appointment);
		List<Appointment> appointments = sut.getAppointments(params);
		assertThat(appointments).hasSize(1)
				.element(0).extracting(Appointment::getStatus).isEqualTo(Appointment.Status.confirmed);
	}

	@Test
	public void createParticipationShouldDoSingleParticipation() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		Appointment appointment1 = createRandomAppointment(topic);
		Appointment appointment2 = createRandomAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		sut.createParticipations(appointment1, singletonList(participant), participant, false, false, true);
		dbInstance.commitAndCloseSession();
		sut.createParticipations(appointment2, singletonList(participant), participant, false, false, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setTopic(topic);
		params.setIdentity(participant);
		List<Participation> participations = sut.getParticipations(params);
		assertThat(participations).hasSize(1)
				.element(0).extracting(Participation::getAppointment).isEqualTo(appointment2);
	}

	@Test
	public void createParticipationShouldDoMultiParticipation() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Topic topic = createRandomTopic();
		Appointment appointment1 = createRandomAppointment(topic);
		Appointment appointment2 = createRandomAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		sut.createParticipations(appointment1, singletonList(participant), participant, true, true, true);
		dbInstance.commitAndCloseSession();
		sut.createParticipations(appointment2, singletonList(participant), participant, true, true, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setTopic(topic);
		params.setIdentity(participant);
		List<Participation> participations = sut.getParticipations(params);
		assertThat(participations).hasSize(2);
	}
	
	@Test
	public void rebookParticipationShouldCreateParticiption() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		Appointment rebookAppointment = createRandomAppointment();
		ParticipationResult participationResult1 = sut.createParticipations(currentAppointment, singletonList(participant1), participant1, true, false, true);
		Participation participation1 = participationResult1.getParticipations().get(0);
		ParticipationResult participationResult2 = sut.createParticipations(currentAppointment, singletonList(participant2), participant2, true, false, true);
		Participation participation2 = participationResult2.getParticipations().get(0);
		ParticipationResult participationResult3 = sut.createParticipations(currentAppointment, singletonList(participant3), participant3, true, false, true);
		Participation participation3 = participationResult3.getParticipations().get(0);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult rebooked = sut.rebookParticipations(rebookAppointment, asList(participation1, participation2), participant1, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rebooked.getStatus()).isEqualTo(Status.ok);
		softly.assertThat(rebooked.getParticipations()).hasSize(2);
		softly.assertThat(rebooked.getParticipations()).extracting(Participation::getIdentity)
				.containsExactlyInAnyOrder(participant1, participant2);
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(currentAppointment);
		List<Participation> currentParticipations = sut.getParticipations(params);
		softly.assertThat(currentParticipations).hasSize(1);
		softly.assertThat(currentParticipations.get(0)).isEqualTo(participation3);
		
		softly.assertAll();
	}

	@Test
	public void rebookParticipationShouldNotCreateParticipationIfAppointmentDeleted() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		Appointment rebookAppointment = createRandomAppointment();
		ParticipationResult participationResult = sut.createParticipations(currentAppointment, singletonList(participant), participant, true, false, true);
		Participation participation = participationResult.getParticipations().get(0);
		dbInstance.commitAndCloseSession();
		sut.deleteAppointment(rebookAppointment);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult rebooked = sut.rebookParticipations(rebookAppointment, singletonList(participation), participant, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rebooked.getStatus()).isEqualTo(Status.appointmentDeleted);
		softly.assertThat(rebooked.getParticipations()).isNull();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(currentAppointment);
		params.setIdentity(participant);
		assertThat(sut.getParticipations(params).get(0)).isEqualTo(participation);
		
		softly.assertAll();
	}

	@Test
	public void rebookParticipationShouldNotCreateParticipationIfNoParticipations() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		Appointment rebookAppointment = createRandomAppointment();
		ParticipationResult participationResult = sut.createParticipations(currentAppointment, singletonList(participant), participant, true, false, true);
		Participation participation = participationResult.getParticipations().get(0);
		dbInstance.commitAndCloseSession();
		sut.deleteParticipation(participation);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult rebooked = sut.rebookParticipations(rebookAppointment, singletonList(participation), participant, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rebooked.getStatus()).isEqualTo(Status.noParticipations);
		softly.assertThat(rebooked.getParticipations()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void rebookParticipationShouldNotCreateParticipationIfNoFreePlaces() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participantA = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participantB = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		Appointment rebookAppointment = createRandomAppointment();
		ParticipationResult participationResult = sut.createParticipations(currentAppointment, singletonList(participant), participant, true, false, true);
		Participation participation = participationResult.getParticipations().get(0);
		dbInstance.commitAndCloseSession();
		rebookAppointment.setMaxParticipations(2);
		sut.saveAppointment(rebookAppointment);
		dbInstance.commitAndCloseSession();
		sut.createParticipations(rebookAppointment, singletonList(participantA), participant, true, false, true);
		sut.createParticipations(rebookAppointment, singletonList(participantB), participant, true, false, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult rebooked = sut.rebookParticipations(rebookAppointment, singletonList(participation), participant, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rebooked.getStatus()).isEqualTo(Status.appointmentFull);
		softly.assertThat(rebooked.getParticipations()).isNull();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(currentAppointment);
		params.setIdentity(participant);
		assertThat(sut.getParticipations(params).get(0)).isEqualTo(participation);
		
		softly.assertAll();
	}
	
	@Test
	public void rebookParticipationShouldNotCreateParticipationIfExisting() {
		Identity participantTwice = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participantA = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Identity participantB = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		ParticipationResult participationResult = sut.createParticipations(currentAppointment, asList(participantA, participantTwice), participantTwice, true, false, true);
		List<Participation> toRebook = participationResult.getParticipations();
		sut.createParticipations(currentAppointment, singletonList(participantB), participantTwice, true, false, true);
		dbInstance.commitAndCloseSession();
		Appointment rebookAppointment = createRandomAppointment();
		rebookAppointment.setMaxParticipations(2);
		sut.saveAppointment(rebookAppointment);
		sut.createParticipations(rebookAppointment, singletonList(participantTwice), participantTwice, true, false, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult rebooked = sut.rebookParticipations(rebookAppointment, toRebook, participantTwice, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rebooked.getStatus()).isEqualTo(Status.ok);
		softly.assertThat(rebooked.getParticipations()).hasSize(1);
		softly.assertThat(rebooked.getParticipations()).extracting(Participation::getIdentity)
				.containsExactlyInAnyOrder(participantA);
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointment(currentAppointment);
		params.setIdentity(participantB);
		assertThat(sut.getParticipations(params).get(0).getIdentity()).isEqualTo(participantB);
		
		softly.assertAll();
	}
	
	@Test
	public void rebookParticipationShouldAutoconfirm() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		Appointment rebookAppointment = createRandomAppointment();
		ParticipationResult participationResult = sut.createParticipations(currentAppointment, singletonList(participant), participant, true, false, true);
		Participation participation = participationResult.getParticipations().get(0);
		dbInstance.commitAndCloseSession();
		
		sut.rebookParticipations(rebookAppointment, singletonList(participation), participant, true);
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setAppointment(rebookAppointment);
		List<Appointment> appointments = sut.getAppointments(params);
		assertThat(appointments).hasSize(1)
				.element(0).extracting(Appointment::getStatus).isEqualTo(Appointment.Status.confirmed);
	}
	
	@Test
	public void rebookParticipationShouldNotLeadToTwoParticipationsInTheSameAppointment() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		Appointment currentAppointment = createRandomAppointment();
		Appointment rebookAppointment = createRandomAppointment();
		ParticipationResult participationResult1 = sut.createParticipations(currentAppointment, singletonList(participant), participant, true, false, true);
		Participation participation1 = participationResult1.getParticipations().get(0);
		dbInstance.commitAndCloseSession();
		sut.createParticipations(rebookAppointment, singletonList(participant), participant, true, false, true);
		dbInstance.commitAndCloseSession();
		
		ParticipationResult rebooked = sut.rebookParticipations(rebookAppointment, asList(participation1), participant, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(rebooked.getStatus()).isEqualTo(Status.ok);
		softly.assertThat(rebooked.getParticipations()).hasSize(0);
		
		ParticipationSearchParams rebookedParams = new ParticipationSearchParams();
		rebookedParams.setAppointment(rebookAppointment);
		List<Participation> rebookedParticipations = sut.getParticipations(rebookedParams);
		softly.assertThat(rebookedParticipations).hasSize(1);
		
		ParticipationSearchParams currentParams = new ParticipationSearchParams();
		currentParams.setAppointment(currentAppointment);
		List<Participation> currentParticipations = sut.getParticipations(currentParams);
		softly.assertThat(currentParticipations).hasSize(0);
		
		softly.assertAll();
	}
	
	private Topic createRandomTopic() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Topic topic = sut.createTopic(entry, String.valueOf(new Random().nextInt()));
		topic.setTitle(random());
		return topic;
	}
	
	private Appointment createRandomAppointment() {
		Topic topic = createRandomTopic();
		return createRandomAppointment(topic);
	}
	
	private Appointment createRandomAppointment(Topic topic) {
		Appointment appointment = sut.createUnsavedAppointment(topic);
		appointment.setStart(new GregorianCalendar(2020, 7, 16, 8, 30, 0).getTime());
		appointment.setEnd(new GregorianCalendar(2020, 7, 16, 15, 30, 0).getTime());
		appointment.setLocation(random());
		appointment = sut.saveAppointment(appointment);
		return appointment;
	}
	
}
