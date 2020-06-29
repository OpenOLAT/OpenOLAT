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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentRef;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.model.AppointmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.04.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganizerDAO organizerDao;
	@Autowired
	private TopicDAO topicDao;
	@Autowired
	private AppointmentDAO appointmentDao;
	
	@Autowired
	private ParticipationDAO sut;
	
	@Test
	public void shouldCreateParticipation() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity caoch = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = appointmentDao.createAppointment(topic);
		appointment = appointmentDao.loadByKey(appointment.getKey());
		Participation participation = sut.createParticipation(appointment, identity, caoch);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(participation).isNotNull();
		softly.assertThat(participation.getKey()).isNotNull();
		softly.assertThat(participation.getCreationDate()).isNotNull();
		softly.assertThat(participation.getLastModified()).isNotNull();
		softly.assertThat(participation.getAppointment()).isEqualTo(appointment);
		softly.assertThat(participation.getIdentity()).isEqualTo(identity);
		softly.assertThat(participation.getCreatedBy()).isEqualTo(caoch);
		softly.assertAll();
	}

	@Test
	public void shouldUpdateParticipation() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = appointmentDao.createAppointment(topic);
		Participation participation = sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		sut.updateParticipation(participation);
		dbInstance.commitAndCloseSession();
		
		participation = sut.loadByKey(participation.getKey());
		
		SoftAssertions softly = new SoftAssertions();
		//no updateable fields yet
		softly.assertAll();
	}
	
	@Test
	public void shouldDelete() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = appointmentDao.createAppointment(topic);
		Participation participation = sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		sut.delete(participation);
		dbInstance.commitAndCloseSession();
		
		Participation reloadedParticipation = sut.loadByKey(participation.getKey());
		assertThat(reloadedParticipation).isNull();
	}
	
	@Test
	public void shouldDeleteByTopic() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		String subIdent = JunitTestHelper.random();
		Topic topic1 = topicDao.createTopic(entry, subIdent);
		Topic topic2 = topicDao.createTopic(entry, subIdent);
		Appointment appointment1 = appointmentDao.createAppointment(topic1);
		Appointment appointment2 = appointmentDao.createAppointment(topic2);
		Participation participation11 = sut.createParticipation(appointment1, identity, identity);
		Participation participation12 = sut.createParticipation(appointment1, identity, identity);
		Participation participation21 = sut.createParticipation(appointment2, identity, identity);
		dbInstance.commitAndCloseSession();
		
		sut.delete(topic1);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		List<Participation> participations = sut.loadParticipations(params);
		assertThat(participations)
				.containsExactlyInAnyOrder(participation21)
				.doesNotContain(participation11, participation12);
	}
	
	@Test
	public void shouldDeleteByEntry() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		String subIdent = JunitTestHelper.random();
		Topic topic1 = topicDao.createTopic(entry1, random());
		Topic topic2 = topicDao.createTopic(entry1, subIdent);
		Topic topic3 = topicDao.createTopic(entry2, random());
		Appointment appointment1 = appointmentDao.createAppointment(topic1);
		Appointment appointment2 = appointmentDao.createAppointment(topic2);
		Appointment appointment3 = appointmentDao.createAppointment(topic3);
		Participation participation1 = sut.createParticipation(appointment1, identity, identity);
		Participation participation2 = sut.createParticipation(appointment2, identity, identity);
		Participation participation3 = sut.createParticipation(appointment3, identity, identity);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry1, subIdent);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setIdentity(identity);
		List<Participation> participations = sut.loadParticipations(params);
		assertThat(participations)
				.containsExactlyInAnyOrder(participation1, participation3)
				.doesNotContain(participation2);
	}
	
	@Test
	public void shouldLoadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = appointmentDao.createAppointment(topic);
		Participation participation = sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		Participation reloadedParticipation = sut.loadByKey(participation.getKey());
		
		assertThat(reloadedParticipation).isEqualTo(participation);
	}
	
	@Test
	public void shouldLoadCount() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent1 = JunitTestHelper.random();
		String subIdent2 = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry1, subIdent1);
		Topic topicB = topicDao.createTopic(entry1, subIdent1);
		Topic topicD = topicDao.createTopic(entry1, subIdent2);
		Appointment appointmentA1 = appointmentDao.createAppointment(topicA);
		Appointment appointmentA2 = appointmentDao.createAppointment(topicA);
		Appointment appointmentB1 = appointmentDao.createAppointment(topicB);
		Appointment appointmentD1 = appointmentDao.createAppointment(topicD);
		appointmentDao.updateStatus(appointmentA1, Status.confirmed);
		appointmentDao.updateStatus(appointmentA2, Status.confirmed);
		appointmentDao.updateStatus(appointmentB1, Status.confirmed);
		appointmentDao.updateStatus(appointmentD1, Status.planned);
		sut.createParticipation(appointmentA1, identity1, identity1);
		sut.createParticipation(appointmentA1, identity2, identity2);
		sut.createParticipation(appointmentA2, identity1, identity1);
		sut.createParticipation(appointmentB1, identity1, identity1);
		sut.createParticipation(appointmentB1, identity2, identity2);
		sut.createParticipation(appointmentD1 , identity1, identity1);
		dbInstance.commitAndCloseSession();
		
		Collection<AppointmentRef> appointments = Arrays.asList(appointmentA1, appointmentA2, appointmentB1,
				appointmentD1);
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(appointments);
		params.setStatus(Status.confirmed);
		Long count = sut.loadParticipationCount(params);
		
		assertThat(count).isEqualTo(5);
	}
	
	@Test
	public void shouldLoadByTopicAndIdentity() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic1 = topicDao.createTopic(entry, JunitTestHelper.random());
		Topic topic2 = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment11 = appointmentDao.createAppointment(topic1);
		Appointment appointment12 = appointmentDao.createAppointment(topic1);
		Appointment appointment21 = appointmentDao.createAppointment(topic2);
		Participation participation11 = sut.createParticipation(appointment11, identity1, identity1);
		Participation participation12 = sut.createParticipation(appointment12, identity1, identity1);
		Participation participationOtherIdentity = sut.createParticipation(appointment11, identity2, identity2);
		Participation participationOtherTopic = sut.createParticipation(appointment21, identity1, identity1);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setTopic(topic1);
		params.setIdentity(identity1);
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(participation11, participation12)
				.doesNotContain(participationOtherTopic, participationOtherIdentity);
	}
	
	@Test
	public void shouldLoadByEmptyTopics() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = appointmentDao.createAppointment(topic);
		sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setTopics(Collections.emptyList());
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations).isEmpty();
	}
	
	@Test
	public void shouldLoadByRepositoryAndIdentity() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent1 = JunitTestHelper.random();
		String subIdent2 = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic1 = topicDao.createTopic(entry1, subIdent1);
		Topic topic2 = topicDao.createTopic(entry1, subIdent1);
		Topic topicOtherEntry = topicDao.createTopic(entry2, subIdent1);
		Topic topicOtherSubIdent = topicDao.createTopic(entry1, subIdent2);
		Appointment appointment11 = appointmentDao.createAppointment(topic1);
		Appointment appointment12 = appointmentDao.createAppointment(topic1);
		Appointment appointment21 = appointmentDao.createAppointment(topic2);
		Appointment appointmentOtherEntry = appointmentDao.createAppointment(topicOtherEntry);
		Appointment appointmentOtherSubIdent = appointmentDao.createAppointment(topicOtherSubIdent);
		Participation participation11 = sut.createParticipation(appointment11, identity1, identity1);
		Participation participation12 = sut.createParticipation(appointment12, identity1, identity1);
		Participation participation21 = sut.createParticipation(appointment21, identity1, identity1);
		Participation participationOtherEntry = sut.createParticipation(appointmentOtherEntry, identity1, identity1);
		Participation participationOtherSubIdent = sut.createParticipation(appointmentOtherSubIdent, identity1, identity1);
		Participation participationOtherIdentity = sut.createParticipation(appointment11, identity2, identity2);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setEntry(entry1);
		params.setSubIdent(subIdent1);
		params.setIdentity(identity1);
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(
						participation11,
						participation12,
						participation21)
				.doesNotContain(
						participationOtherEntry,
						participationOtherSubIdent,
						participationOtherIdentity);
	}
	
	@Test
	public void shouldByCreatedAfter() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry, subIdent);
		Appointment appointment = appointmentDao.createAppointment(topicA);
		Participation participation = sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		//Is just a syntax check. The appointment key is the limiting clause.
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(asList(appointment));
		params.setCreatedAfter(new GregorianCalendar(2000, 1, 1).getTime());
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations).containsExactlyInAnyOrder(participation);
	}
	
	@Test
	public void shouldLoadByParticipations() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic1 = topicDao.createTopic(entry, JunitTestHelper.random());
		Topic topic2 = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment11 = appointmentDao.createAppointment(topic1);
		Appointment appointment12 = appointmentDao.createAppointment(topic1);
		Appointment appointment21 = appointmentDao.createAppointment(topic2);
		Participation participation111 = sut.createParticipation(appointment11, identity1, identity1);
		Participation participation121 = sut.createParticipation(appointment12, identity1, identity1);
		Participation participation112 = sut.createParticipation(appointment11, identity2, identity2);
		Participation participation211 = sut.createParticipation(appointment21, identity1, identity1);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		Collection<Participation> asList = Arrays.asList(participation112, participation211);
		params.setParticipations(asList);
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(participation112, participation211)
				.doesNotContain(participation111, participation121);
	}
	
	@Test
	public void shouldLoadByEmptyParticipations() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, JunitTestHelper.random());
		Appointment appointment = appointmentDao.createAppointment(topic);
		Participation participation = sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		Collection<Participation> participationList = Collections.singletonList(participation);
		params.setParticipations(participationList);
		List<Participation> participations = sut.loadParticipations(params);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(participations).hasSize(1);
		
		ParticipationSearchParams paramsEmpty = new ParticipationSearchParams();
		paramsEmpty.setParticipations(Collections.emptyList());
		List<Participation> participationsEmpty = sut.loadParticipations(paramsEmpty);
		softly.assertThat(participationsEmpty).isEmpty();
		
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadByAppointments() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent1 = JunitTestHelper.random();
		String subIdent2 = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry1, subIdent1);
		Topic topicB = topicDao.createTopic(entry1, subIdent1);
		Topic topicC = topicDao.createTopic(entry2, subIdent1);
		Topic topicD = topicDao.createTopic(entry1, subIdent2);
		Appointment appointmentA1 = appointmentDao.createAppointment(topicA);
		Appointment appointmentA2 = appointmentDao.createAppointment(topicA);
		Appointment appointmentB1 = appointmentDao.createAppointment(topicB);
		Appointment appointmentC1 = appointmentDao.createAppointment(topicC);
		Appointment appointmentD1 = appointmentDao.createAppointment(topicD);
		Participation participationA11 = sut.createParticipation(appointmentA1, identity1, identity1);
		Participation participationA12 = sut.createParticipation(appointmentA1, identity2, identity2);
		Participation participationA21 = sut.createParticipation(appointmentA2, identity1, identity1);
		Participation participationB11 = sut.createParticipation(appointmentB1, identity1, identity1);
		Participation participationB12 = sut.createParticipation(appointmentB1, identity2, identity2);
		Participation participationC11 = sut.createParticipation(appointmentC1, identity1, identity1);
		Participation participationC12 = sut.createParticipation(appointmentC1, identity2, identity2);
		Participation participationD11 = sut.createParticipation(appointmentD1 , identity1, identity1);
		dbInstance.commitAndCloseSession();
		
		Collection<Appointment> appointments = Arrays.asList(appointmentA1, appointmentA2, appointmentC1);
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(appointments);
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(
						participationA11,
						participationA12,
						participationA21,
						participationC11,
						participationC12
						)
				.doesNotContain(
						participationB11,
						participationB12,
						participationD11);
	}
	
	@Test
	public void shouldLoadByStartAfter() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent1 = JunitTestHelper.random();
		String subIdent2 = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry1, subIdent1);
		Topic topicB = topicDao.createTopic(entry1, subIdent1);
		Topic topicD = topicDao.createTopic(entry1, subIdent2);
		Appointment appointmentA1 = appointmentDao.createAppointment(topicA);
		Appointment appointmentA2 = appointmentDao.createAppointment(topicA);
		Appointment appointmentB1 = appointmentDao.createAppointment(topicB);
		Appointment appointmentD1 = appointmentDao.createAppointment(topicD);
		appointmentA1.setStart(new GregorianCalendar(2030, 1, 1).getTime());
		appointmentA2.setStart(new GregorianCalendar(2028, 1, 1).getTime());
		appointmentB1.setStart(new GregorianCalendar(2029, 1, 1).getTime());
		appointmentD1.setStart(new GregorianCalendar(2010, 1, 1).getTime());
		appointmentA1 = appointmentDao.saveAppointment(appointmentA1);
		appointmentA2 = appointmentDao.saveAppointment(appointmentA2);
		appointmentB1 = appointmentDao.saveAppointment(appointmentB1);
		appointmentD1 = appointmentDao.saveAppointment(appointmentD1);
		Participation participationA11 = sut.createParticipation(appointmentA1, identity1, identity1);
		Participation participationA12 = sut.createParticipation(appointmentA1, identity2, identity2);
		Participation participationA21 = sut.createParticipation(appointmentA2, identity1, identity1);
		Participation participationB11 = sut.createParticipation(appointmentB1, identity1, identity1);
		Participation participationB12 = sut.createParticipation(appointmentB1, identity2, identity2);
		Participation participationD11 = sut.createParticipation(appointmentD1 , identity1, identity1);
		dbInstance.commitAndCloseSession();
		
		Collection<Appointment> appointments = Arrays.asList(appointmentA1, appointmentA2, appointmentB1,
				appointmentD1);
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(appointments);
		params.setStartAfter(new GregorianCalendar(2020, 1, 1).getTime());
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(
						participationA11,
						participationA12,
						participationB11,
						participationB12,
						participationA21
						)
				.doesNotContain(
						participationD11
						);
	}
	
	@Test
	public void shouldLoadByAppointmentStatus() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent1 = JunitTestHelper.random();
		String subIdent2 = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry1, subIdent1);
		Topic topicB = topicDao.createTopic(entry1, subIdent1);
		Topic topicD = topicDao.createTopic(entry1, subIdent2);
		Appointment appointmentA1 = appointmentDao.createAppointment(topicA);
		Appointment appointmentA2 = appointmentDao.createAppointment(topicA);
		Appointment appointmentB1 = appointmentDao.createAppointment(topicB);
		Appointment appointmentD1 = appointmentDao.createAppointment(topicD);
		appointmentDao.updateStatus(appointmentA1, Status.confirmed);
		appointmentDao.updateStatus(appointmentA2, Status.confirmed);
		appointmentDao.updateStatus(appointmentB1, Status.confirmed);
		appointmentDao.updateStatus(appointmentD1, Status.planned);
		Participation participationA11 = sut.createParticipation(appointmentA1, identity1, identity1);
		Participation participationA12 = sut.createParticipation(appointmentA1, identity2, identity2);
		Participation participationA21 = sut.createParticipation(appointmentA2, identity1, identity1);
		Participation participationB11 = sut.createParticipation(appointmentB1, identity1, identity1);
		Participation participationB12 = sut.createParticipation(appointmentB1, identity2, identity2);
		Participation participationD11 = sut.createParticipation(appointmentD1 , identity1, identity1);
		dbInstance.commitAndCloseSession();
		
		Collection<Appointment> appointments = Arrays.asList(appointmentA1, appointmentA2, appointmentB1,
				appointmentD1);
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(appointments);
		params.setStatus(Status.confirmed);
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(
						participationA11,
						participationA12,
						participationB11,
						participationB12,
						participationA21
						)
				.doesNotContain(
						participationD11
						);
	}
	
	@Test
	public void shouldLoadByAppointmentStatusModifiedAfter() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry, subIdent);
		Topic topicB = topicDao.createTopic(entry, subIdent);
		Topic topicC = topicDao.createTopic(entry, subIdent);
		Appointment appointmentA1 = appointmentDao.createAppointment(topicA);
		Appointment appointmentA2 = appointmentDao.createAppointment(topicA);
		Appointment appointmentB1 = appointmentDao.createAppointment(topicB);
		Appointment appointmentD1 = appointmentDao.createAppointment(topicC);
		appointmentDao.saveAppointment(setStatusModified(appointmentA1, 2020, 2, 2, 10, 0, 0));
		appointmentDao.saveAppointment(setStatusModified(appointmentA2, 2020, 3, 1, 10, 0, 0));
		appointmentDao.saveAppointment(setStatusModified(appointmentB1, 2020, 3, 1, 15, 0, 0));
		appointmentDao.saveAppointment(setStatusModified(appointmentD1, 2021, 2, 2, 10, 0, 0));
		Participation participationA11 = sut.createParticipation(appointmentA1, identity, identity);
		Participation participationA21 = sut.createParticipation(appointmentA2, identity, identity);
		Participation participationB11 = sut.createParticipation(appointmentB1, identity, identity);
		Participation participationD11 = sut.createParticipation(appointmentD1 , identity, identity);
		dbInstance.commitAndCloseSession();
		
		Collection<Appointment> appointments = Arrays.asList(appointmentA1, appointmentA2, appointmentB1, appointmentD1);
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setAppointments(appointments);
		params.setStatusModifiedAfter(new GregorianCalendar(2020, 3, 1, 12, 30, 0).getTime());
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
		.containsExactlyInAnyOrder(
				participationB11,
				participationD11
				)
		.doesNotContain(
				participationA11,
				participationA21
				);
	}

	private Appointment setStatusModified(Appointment appointment, int year, int month, int dayOfMonth, int hourOfDay,
			int minute, int second) {
		Date lastModified = new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second).getTime();
		if (appointment instanceof AppointmentImpl) {
			((AppointmentImpl)appointment).setStatusModified(lastModified);
		}
		return appointment;
	}
	
	@Test
	public void shouldLoadByOrganizer() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity organizer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topicA = topicDao.createTopic(entry, subIdent);
		Topic topicB = topicDao.createTopic(entry, subIdent);
		organizerDao.createOrganizer(topicA, organizer);
		Appointment appointmentA1 = appointmentDao.createAppointment(topicA);
		Appointment appointmentA2 = appointmentDao.createAppointment(topicA);
		Appointment appointmentB1 = appointmentDao.createAppointment(topicB);
		Participation participationA12 = sut.createParticipation(appointmentA1, identity2, identity2);
		Participation participationA21 = sut.createParticipation(appointmentA2, identity1, identity1);
		Participation participationB11 = sut.createParticipation(appointmentB1, identity1, identity1);
		Participation participationB12 = sut.createParticipation(appointmentB1, identity2, identity2);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setOrganizer(organizer);
		List<Participation> participations = sut.loadParticipations(params);
		
		assertThat(participations)
				.containsExactlyInAnyOrder(
						participationA21,
						participationA12
						)
				.doesNotContain(
						participationB11,
						participationB12
						);
	}
	
	@Test
	public void shouldFetch() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = JunitTestHelper.random();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Topic topic = topicDao.createTopic(entry, subIdent);
		Appointment appointment = appointmentDao.createAppointment(topic);
		sut.createParticipation(appointment, identity, identity);
		dbInstance.commitAndCloseSession();
		
		ParticipationSearchParams params = new ParticipationSearchParams();
		params.setFetchAppointments(true);
		params.setFetchIdentities(true);
		params.setFetchTopics(true);
		params.setFetchUser(true);
		sut.loadParticipations(params);
		
		// Only a syntax check
	}

}
