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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.Appointment.Status;
import org.olat.course.nodes.appointments.AppointmentSearchParams;
import org.olat.course.nodes.appointments.Topic;
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
	public void shouldDeleteByKeys() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Topic topic = topicDao.createTopic(entry, random());
		Appointment appointment1 = sut.createAppointment(topic);
		Appointment appointment2 = sut.createAppointment(topic);
		Appointment appointment3 = sut.createAppointment(topic);
		dbInstance.commitAndCloseSession();
		
		sut.delete(asList(appointment1.getKey(), appointment2.getKey()));
		dbInstance.commitAndCloseSession();
		
		AppointmentSearchParams searchParams = new AppointmentSearchParams();
		searchParams.setTopic(topic);
		List<Appointment> reloadedAppointments = sut.loadAppointments(searchParams);
		assertThat(reloadedAppointments).containsExactly(appointment3);
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

}
