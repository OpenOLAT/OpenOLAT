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
package org.olat.modules.project.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAppointmentDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjAppointmentDAO sut;
	
	@Test
	public void shouldCreateAppointment() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjArtefact artefact = artefactDao.create(ProjAppointment.TYPE, project, creator);
		dbInstance.commitAndCloseSession();
		
		Date startDate = DateUtils.addDays(new Date(), 1);
		Date endDate = DateUtils.addDays(new Date(), 2);
		ProjAppointment appointment = sut.create(artefact, startDate, endDate);
		dbInstance.commitAndCloseSession();
		
		assertThat(appointment).isNotNull();
		assertThat(appointment.getCreationDate()).isNotNull();
		assertThat(appointment.getLastModified()).isNotNull();
		assertThat(appointment.getIdentifier()).isNotNull();
		assertThat(appointment.getEventId()).isNotNull();
		assertThat(appointment.getStartDate()).isCloseTo(startDate, 1000);
		assertThat(appointment.getEndDate()).isCloseTo(endDate, 1000);
		assertThat(appointment.isAllDay()).isFalse();
		assertThat(appointment.getArtefact()).isEqualTo(artefact);
	}
	
	@Test
	public void shouldSaveAppointment() {
		ProjAppointment appointment = createRandomAppointment();
		dbInstance.commitAndCloseSession();
		
		String recurrenceId = random();
		appointment.setRecurrenceId(recurrenceId);
		Date startDate = DateUtils.addDays(new Date(), 1);
		appointment.setStartDate(startDate);
		Date endDate  = DateUtils.addDays(new Date(), 1);
		appointment.setEndDate(endDate);
		String subject = random();
		appointment.setSubject(subject);
		String description = random();
		appointment.setDescription(description);
		String location = random();
		appointment.setLocation(location);
		String color =  random();
		appointment.setColor(color);
		appointment.setAllDay(true);
		String recurrenceRule = "a rule";
		appointment.setRecurrenceRule(recurrenceRule);
		String recurrenceExclusion = random();
		appointment.setRecurrenceExclusion(recurrenceExclusion);
		sut.save(appointment);
		dbInstance.commitAndCloseSession();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment));
		appointment = sut.loadAppointments(params).get(0);
		
		assertThat(appointment.getRecurrenceId()).isEqualTo(recurrenceId);
		assertThat(appointment.getStartDate()).isCloseTo(startDate, 1000);
		assertThat(appointment.getEndDate()).isCloseTo(endDate, 1000);
		assertThat(appointment.getSubject()).isEqualTo(subject);
		assertThat(appointment.getDescription()).isEqualTo(description);
		assertThat(appointment.getLocation()).isEqualTo(location);
		assertThat(appointment.getColor()).isEqualTo(color);
		assertThat(appointment.isAllDay()).isTrue();
		assertThat(appointment.getRecurrenceRule()).isEqualTo(recurrenceRule);
		assertThat(appointment.getRecurrenceExclusion()).isEqualTo(recurrenceExclusion);
	}
	
	@Test
	public void shouldDelete() {
		ProjAppointment appointment = createRandomAppointment();
		
		sut.delete(appointment);
		dbInstance.commitAndCloseSession();
		
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setProject(appointment.getArtefact().getProject());
		List<ProjAppointment> appointments = sut.loadAppointments(searchParams);
		assertThat(appointments).isEmpty();
	}
	
	@Test
	public void shouldCount() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment1, appointment2));
		long count = sut.loadAppointmentsCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjAppointment appointment = createRandomAppointment();
		createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setProject(appointment.getArtefact().getProject());
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment);
	}
	
	@Test
	public void shouldLoad_filter_appointmentKeys() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment1, appointment2));
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment1, appointment2);
	}
	
	@Test
	public void shouldLoad_filter_identifiers() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setIdentifiers(List.of(appointment1.getIdentifier(), appointment2.getIdentifier()));
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment1, appointment2);
	}
	
	@Test
	public void shouldLoad_filter_eventIds() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setEventIds(List.of(appointment1.getEventId(), appointment2.getEventId()));
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment1, appointment2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setArtefacts(List.of(appointment1.getArtefact(), appointment2.getArtefact()));
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment1, appointment2);
	}
	
	@Test
	public void shouldLoad_filter_recurrenceIdAvailable() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		appointment2.setRecurrenceId(random());
		appointment2 = sut.save(appointment2);
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment1, appointment2));
		
		// All
		assertThat(sut.loadAppointments(params)).containsExactlyInAnyOrder(appointment1, appointment2);
		
		// With recurrence id
		params.setRecurrenceIdAvailable(Boolean.TRUE);
		assertThat(sut.loadAppointments(params)).containsExactlyInAnyOrder(appointment2);
		
		// Without recurrence id
		params.setRecurrenceIdAvailable(Boolean.FALSE);
		assertThat(sut.loadAppointments(params)).containsExactlyInAnyOrder(appointment1);
	}
	
	@Test
	public void shouldLoad_filter_status() {
		ProjAppointment appointment1 = createRandomAppointment();
		projectService.deleteAppointmentSoftly(appointment1.getArtefact().getCreator(), appointment1);
		ProjAppointment appointment2 = createRandomAppointment();
		projectService.deleteAppointmentSoftly(appointment2.getArtefact().getCreator(), appointment2);
		ProjAppointment appointment3 = createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment1, appointment2, appointment3));
		params.setStatus(List.of(ProjectStatus.deleted));
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		
		assertThat(appointments).containsExactlyInAnyOrder(appointment1, appointment2);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ProjAppointment appointment = createRandomAppointment();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment));
		params.setCreatedAfter(new Date());
		sut.loadAppointments(params);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_filter_datesNull() {
		ProjAppointment appointment1 = createRandomAppointment();
		ProjAppointment appointment2 = createRandomAppointment();
		appointment2.setStartDate(null);
		sut.save(appointment2);
		ProjAppointment appointment3 = createRandomAppointment();
		appointment3.setEndDate(null);
		sut.save(appointment3);
		ProjAppointment appointment4 = createRandomAppointment();
		appointment4.setStartDate(null);
		appointment4.setEndDate(null);
		sut.save(appointment4);
		dbInstance.commitAndCloseSession();
		
		ProjAppointmentSearchParams params = new ProjAppointmentSearchParams();
		params.setAppointments(List.of(appointment1, appointment2, appointment3, appointment4));
		List<ProjAppointment> appointments = sut.loadAppointments(params);
		assertThat(appointments).containsExactlyInAnyOrder(appointment1, appointment2, appointment3, appointment4);
		
		params.setDatesNull(Boolean.TRUE);
		appointments = sut.loadAppointments(params);
		assertThat(appointments).containsExactlyInAnyOrder(appointment2, appointment3, appointment4);
		
		params.setDatesNull(Boolean.FALSE);
		appointments = sut.loadAppointments(params);
		assertThat(appointments).containsExactlyInAnyOrder(appointment1);
	}
	
	private ProjAppointment createRandomAppointment() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomAppointment(creator);
	}

	private ProjAppointment createRandomAppointment(Identity creator) {
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjArtefact artefact = artefactDao.create(ProjAppointment.TYPE, project, creator);
		ProjAppointment appointment = sut.create(artefact, DateUtils.addDays(new Date(), 1), DateUtils.addDays(new Date(), 2));
		dbInstance.commitAndCloseSession();
		return appointment;
	}

}
