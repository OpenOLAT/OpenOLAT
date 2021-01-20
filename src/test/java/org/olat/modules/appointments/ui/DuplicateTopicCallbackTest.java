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
package org.olat.modules.appointments.ui;

import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.AppointmentInput;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.DuplicationContext;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingsPublishingEnum;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DuplicateTopicCallbackTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AppointmentsService appointmentsService;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	@Test
	public void shouldDuplicate() {
		DuplicationContext context = new DuplicationContext();
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		
		Topic topic = appointmentsService.createTopic(entry, subIdent);
		TopicLight transientTopic = DuplicateTopicCallback.toTransientTopic(topic);
		boolean autoConfirmation = true;
		transientTopic.setAutoConfirmation(autoConfirmation);
		String description = JunitTestHelper.random();
		transientTopic.setDescription(description);
		boolean multiParticipation = false;
		transientTopic.setMultiParticipation(multiParticipation);
		boolean participtionVisible = true;
		transientTopic.setParticipationVisible(participtionVisible);
		String title = random();
		transientTopic.setTitle(title);
		Type type = Type.enrollment;
		transientTopic.setType(type);
		
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setTopic(transientTopic);
		
		Identity orginizer1 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity orginizer2 = JunitTestHelper.createAndPersistIdentityAsUser(random());
		context.setOrganizers(List.of(orginizer1, orginizer2));
		
		Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
		String details = random();
		appointment.setDetails(details);
		appointment.setEnd(new Date());
		String location = random();
		appointment.setLocation(location);
		Integer maxParticipations = Integer.valueOf(3);
		appointment.setMaxParticipations(maxParticipations);
		appointment.setStart(new Date());
		appointment = appointmentsService.saveAppointment(appointment);
		
		Date start = DateUtils.addDays(new Date(), 3);
		Date end = DateUtils.addDays(new Date(), 5);
		AppointmentInput appointmentInput = new AppointmentInput(appointment, start, end, null);
		context.setAppointments(Collections.singletonList(appointmentInput));
		dbInstance.commitAndCloseSession();
		
		
		DuplicateTopicCallback sut = new DuplicateTopicCallback();
		sut.create(context);
		dbInstance.commitAndCloseSession();
		
		
		SoftAssertions softly = new SoftAssertions();
		Topic createdTopic = appointmentsService.getTopics(entry, subIdent).stream()
				.sorted((t1, t2) -> t2.getKey().compareTo(t1.getKey()))
				.findFirst()
				.get();
		softly.assertThat(createdTopic).isNotNull();
		softly.assertThat(createdTopic.getKey()).isNotNull();
		softly.assertThat(createdTopic.getEntry()).isEqualTo(entry);
		softly.assertThat(createdTopic.getSubIdent()).isEqualTo(subIdent);
		softly.assertThat(createdTopic.isAutoConfirmation()).isEqualTo(autoConfirmation);
		softly.assertThat(createdTopic.getDescription()).isEqualTo(description);
		softly.assertThat(createdTopic.isMultiParticipation()).isEqualTo(multiParticipation);
		softly.assertThat(createdTopic.isParticipationVisible()).isEqualTo(participtionVisible);
		softly.assertThat(createdTopic.getTitle()).isEqualTo(title);
		softly.assertThat(createdTopic.getType()).isEqualTo(type);
		
		List<Organizer> organizers = appointmentsService.getOrganizers(createdTopic);
		softly.assertThat(organizers)
				.extracting(Organizer::getIdentity)
				.containsExactlyInAnyOrder(orginizer1, orginizer2);
		
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setTopic(createdTopic);
		Appointment createdAppointment = appointmentsService.getAppointments(params).get(0);
		softly.assertThat(createdAppointment.getKey()).isNotNull();
		softly.assertThat(createdAppointment.getDetails()).isEqualTo(details);
		softly.assertThat(createdAppointment.getEnd()).isCloseTo(end, 1000);
		softly.assertThat(createdAppointment.getLocation()).isEqualTo(location);
		softly.assertThat(createdAppointment.getMaxParticipations()).isEqualTo(maxParticipations);
		softly.assertThat(createdAppointment.getStart()).isCloseTo(start, 1000);
		
		softly.assertAll();
	}
	
	@Test
	public void shouldDuplicateBBBMeeting() {
		DuplicationContext context = new DuplicationContext();
		
		BigBlueButtonMeetingTemplate template = bigBlueButtonManager.createAndPersistTemplate(random());
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String subIdent = random();
		
		Topic topic = appointmentsService.createTopic(entry, subIdent);
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		TopicLight transientTopic = DuplicateTopicCallback.toTransientTopic(topic);
		
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setTopic(transientTopic);
		
		Identity orginizer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		context.setOrganizers(List.of(orginizer));
		
		Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
		appointment.setEnd(new Date());
		appointment.setStart(new Date());
		appointment = appointmentsService.saveAppointment(appointment);
		
		appointment = appointmentsService.addBBBMeeting(appointment, orginizer);
		String welcome = random();
		appointment.getBBBMeeting().setWelcome(welcome);
		appointment.getBBBMeeting().setTemplate(template);
		boolean permanent = true;
		appointment.getBBBMeeting().setPermanent(permanent);
		long leadTime = 3;
		appointment.getBBBMeeting().setLeadTime(leadTime);
		long followupTime = 112;
		appointment.getBBBMeeting().setFollowupTime(followupTime);
		BigBlueButtonMeetingLayoutEnum layout = BigBlueButtonMeetingLayoutEnum.standard;
		appointment.getBBBMeeting().setMeetingLayout(layout);
		Boolean record = Boolean.FALSE;
		appointment.getBBBMeeting().setRecord(record);
		appointment.getBBBMeeting().setReadableIdentifier(random());
		
		Date start = DateUtils.addDays(new Date(), 3);
		Date end = DateUtils.addDays(new Date(), 5);
		AppointmentInput appointmentInput = new AppointmentInput(appointment, start, end, null);
		context.setAppointments(Collections.singletonList(appointmentInput));
		dbInstance.commitAndCloseSession();
		
		
		DuplicateTopicCallback sut = new DuplicateTopicCallback();
		sut.create(context);
		dbInstance.commitAndCloseSession();
		
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		BigBlueButtonMeeting meeting = appointmentsService.getAppointments(params).stream()
				.sorted((t1, t2) -> t2.getKey().compareTo(t1.getKey()))
				.findFirst()
				.get()
				.getBBBMeeting();
		softly.assertThat(meeting.getRecordingsPublishingEnum()).isEqualTo(BigBlueButtonRecordingsPublishingEnum.auto);
		softly.assertThat(meeting.getName()).as("Name").isEqualTo(title);
		softly.assertThat(meeting.getDescription()).as("Description").isEqualTo(description);
		softly.assertThat(meeting.getStartDate()).as("StartDate").isCloseTo(start, 1000);
		softly.assertThat(meeting.getEndDate()).as("EndDate").isCloseTo(end, 1000);
		softly.assertThat(meeting.getMainPresenter()).as("MainPresenter").isNotNull();
		softly.assertThat(meeting.getWelcome()).as("Welcome").isEqualTo(welcome);
		softly.assertThat(meeting.getTemplate().getKey()).as("Template").isEqualTo(template.getKey());
		softly.assertThat(meeting.isPermanent()).as("Permanent").isEqualTo(permanent);
		softly.assertThat(meeting.getLeadTime()).as("LeadTime").isEqualTo(leadTime);
		softly.assertThat(meeting.getFollowupTime()).as("FollowupTime").isEqualTo(followupTime);
		softly.assertThat(meeting.getMeetingLayout()).as("MeetingLayout").isEqualTo(layout);
		softly.assertThat(meeting.getRecord()).as("Record").isEqualTo(record);
		softly.assertThat(meeting.getReadableIdentifier()).as("ReadeableIdentifier").isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldNotDuplicateBBBMeetingIfNoSlotAvailable() {
		DuplicationContext context = new DuplicationContext();
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String subIdent = random();
		
		Topic topic = appointmentsService.createTopic(entry, subIdent);
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		TopicLight transientTopic = DuplicateTopicCallback.toTransientTopic(topic);
		
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setTopic(transientTopic);
		
		Identity orginizer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		context.setOrganizers(List.of(orginizer));
		
		Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
		appointment.setEnd(new Date());
		appointment.setStart(new Date());
		appointment = appointmentsService.saveAppointment(appointment);
		
		appointment = appointmentsService.addBBBMeeting(appointment, orginizer);
		appointment.getBBBMeeting().setTemplate(null); // no template => no slot available
		
		Date start = DateUtils.addDays(new Date(), 3);
		Date end = DateUtils.addDays(new Date(), 5);
		AppointmentInput appointmentInput = new AppointmentInput(appointment, start, end, null);
		context.setAppointments(Collections.singletonList(appointmentInput));
		dbInstance.commitAndCloseSession();
		
		
		DuplicateTopicCallback sut = new DuplicateTopicCallback();
		sut.create(context);
		dbInstance.commitAndCloseSession();
		
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		Appointment reloadedAppointment = appointmentsService.getAppointments(params).stream()
				.sorted((t1, t2) -> t2.getKey().compareTo(t1.getKey()))
				.findFirst()
				.get();
		softly.assertThat(reloadedAppointment).isNotNull();
		softly.assertThat(reloadedAppointment.getBBBMeeting()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldDuplicateTeamsMeeting() {
		DuplicationContext context = new DuplicationContext();
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("ap");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String subIdent = random();
		
		Topic topic = appointmentsService.createTopic(entry, subIdent);
		String title = random();
		topic.setTitle(title);
		String description = random();
		topic.setDescription(description);
		TopicLight transientTopic = DuplicateTopicCallback.toTransientTopic(topic);
		
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setTopic(transientTopic);
		
		Identity orginizer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		context.setOrganizers(List.of(orginizer));
		
		Appointment appointment = appointmentsService.createUnsavedAppointment(topic);
		appointment.setEnd(new Date());
		appointment.setStart(new Date());
		appointment = appointmentsService.saveAppointment(appointment);
		
		appointment = appointmentsService.addTeamsMeeting(appointment, orginizer);
		long leadTime = 45;
		appointment.getTeamsMeeting().setLeadTime(leadTime);
		long followupTime = 12;
		appointment.getTeamsMeeting().setFollowupTime(followupTime);
		boolean permanent = false;
		appointment.getTeamsMeeting().setPermanent(permanent);
		String accessLevel = random().substring(0, 10);
		appointment.getTeamsMeeting().setAccessLevel(accessLevel);
		String allowedPresenters = random().substring(0, 10);
		appointment.getTeamsMeeting().setAllowedPresenters(allowedPresenters);
		boolean entryExitAnnouncement = false;
		appointment.getTeamsMeeting().setEntryExitAnnouncement(entryExitAnnouncement);
		String scope = random().substring(0, 10);
		appointment.getTeamsMeeting().setLobbyBypassScope(scope);
		appointment.getTeamsMeeting().setReadableIdentifier(random());
		
		Date start = DateUtils.addDays(new Date(), 3);
		Date end = DateUtils.addDays(new Date(), 5);
		AppointmentInput appointmentInput = new AppointmentInput(appointment, start, end, null);
		context.setAppointments(Collections.singletonList(appointmentInput));
		dbInstance.commitAndCloseSession();
		
		
		DuplicateTopicCallback sut = new DuplicateTopicCallback();
		sut.create(context);
		dbInstance.commitAndCloseSession();
		
		
		SoftAssertions softly = new SoftAssertions();
		AppointmentSearchParams params = new AppointmentSearchParams();
		params.setEntry(entry);
		params.setSubIdent(subIdent);
		TeamsMeeting meeting = appointmentsService.getAppointments(params).stream()
				.sorted((t1, t2) -> t2.getKey().compareTo(t1.getKey()))
				.findFirst()
				.get()
				.getTeamsMeeting();
		softly.assertThat(meeting.getSubject()).isEqualTo(title);
		softly.assertThat(meeting.getDescription()).isEqualTo(description);
		softly.assertThat(meeting.getStartDate()).isCloseTo(start, 1000);
		softly.assertThat(meeting.getEndDate()).isCloseTo(end, 1000);
		softly.assertThat(meeting.getLeadTime()).isEqualTo(leadTime);
		softly.assertThat(meeting.getFollowupTime()).isEqualTo(followupTime);
		softly.assertThat(meeting.isPermanent()).isEqualTo(permanent);
		softly.assertThat(meeting.getMainPresenter()).as("MainPresenter").isNotNull();
		softly.assertThat(meeting.getAccessLevel()).isEqualTo(accessLevel);
		softly.assertThat(meeting.getAllowedPresenters()).isEqualTo(allowedPresenters);
		softly.assertThat(meeting.isEntryExitAnnouncement()).isEqualTo(entryExitAnnouncement);
		softly.assertThat(meeting.getLobbyBypassScope()).isEqualTo(scope);
		softly.assertThat(meeting.getReadableIdentifier()).isNull();
		softly.assertAll();
	}

}
