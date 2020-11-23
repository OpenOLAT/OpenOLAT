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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.AppointmentSearchParams;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.TopicLight;
import org.olat.modules.appointments.TopicLight.Type;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.AppointmentInput;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.DuplicationContext;
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
	
	@Test
	public void shouldCreate() {
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
		
		DuplicationContext context = new DuplicationContext();
		context.setEntry(entry);
		context.setSubIdent(subIdent);
		context.setTopic(transientTopic);
		
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
		AppointmentInput appointmentInput = new AppointmentInput(appointment, start, end);
		context.setAppointments(Collections.singletonList(appointmentInput));
		dbInstance.commitAndCloseSession();
		
		
		DuplicateTopicCallback sut = new DuplicateTopicCallback();
		sut.create(context);
		dbInstance.commitAndCloseSession();
		
		
		SoftAssertions softly = new SoftAssertions();
		Topic createdTopic = appointmentsService.getTopics(entry, subIdent).stream()
				.sorted((t1, t2) -> t2.getKey().compareTo(t1.getKey()))
				.limit(1)
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

}
