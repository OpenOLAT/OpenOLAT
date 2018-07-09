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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.QualityReminder;
import org.olat.modules.quality.QualityReminderTo;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualitReminderDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private QualityReminderDAO sut;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}
	
	@Test
	public void shouldCreateReminder() {
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminder reminder = sut.create(dataCollectionRef);
		dbInstance.commitAndCloseSession();
		
		assertThat(reminder).isNotNull();
		assertThat(reminder.getKey()).isNotNull();
		assertThat(reminder.getCreationDate()).isNotNull();
		assertThat(reminder.getLastModified()).isNotNull();
		assertThat(reminder.isSent()).isFalse();
		assertThat(reminder.getDataCollection().getKey()).isEqualTo(dataCollectionRef.getKey());
	}

	@Test
	public void shouldUpdateReminder() {
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminder reminder = sut.create(dataCollectionRef);
		dbInstance.commitAndCloseSession();
		
		Boolean sent = Boolean.TRUE;
		reminder.setSent(sent);
		Date sendDate = new Date();
		reminder.setSendDate(sendDate);
		QualityReminderTo to = QualityReminderTo.PENDING;
		reminder.setTo(to);
		String subject = "New survey for you";
		reminder.setSubject(subject);
		String body = "body";
		reminder.setBody(body);
		
		reminder = sut.save(reminder);
		
		assertThat(reminder.isSent()).isEqualTo(sent);
		assertThat(reminder.getSendDate()).isEqualTo(sendDate);
		assertThat(reminder.getTo()).isEqualTo(to);
		assertThat(reminder.getSubject()).isEqualTo(subject);
		assertThat(reminder.getBody()).isEqualTo(body);
	}
	
	@Test
	public void shouldLoadByDataCollection() {
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminder reminderFuture = sut.create(dataCollectionRef);
		reminderFuture.setSendDate(getDateInFuture());
		sut.save(reminderFuture);
		QualityReminder reminderPast = sut.create(dataCollectionRef);
		reminderPast.setSendDate(getDateInPast());
		sut.save(reminderPast);
		QualityDataCollectionRef otherDataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminder otherReminder = sut.create(otherDataCollectionRef);
		dbInstance.commitAndCloseSession();
		
		List<QualityReminder> reminders = sut.loadByDataCollection(dataCollectionRef);

		assertThat(reminders)
				.containsExactly(reminderPast, reminderFuture)
				.doesNotContain(otherReminder);
	}
	
	@Test
	public void shouldDeleteReminder() {
		QualityDataCollectionRef dataCollectionRef = qualityTestHelper.createDataCollection();
		QualityReminder reminder = sut.create(dataCollectionRef);
		dbInstance.commitAndCloseSession();
		
		sut.delete(reminder);
		
		List<QualityReminder> reminders = sut.loadByDataCollection(dataCollectionRef);
		assertThat(reminders).isEmpty();
	}

	private Date getDateInPast() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		return calendar.getTime();
	}

	private Date getDateInFuture() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		return calendar.getTime();
	}

}
