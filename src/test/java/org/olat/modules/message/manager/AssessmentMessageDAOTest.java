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
package org.olat.modules.message.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessagePublicationEnum;
import org.olat.modules.message.model.AssessmentMessageInfos;
import org.olat.modules.message.model.AssessmentMessageLogImpl;
import org.olat.modules.message.model.AssessmentMessageWithReadFlag;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentMessageDAO assessmentMessageDao;
	@Autowired
	private AssessmentMessageLogDAO assessmentMessageLogDao;
	
	@Test
	public void createMessage() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Date now = new Date();
		Date publicationDate = CalendarUtils.startOfDay(now);
		Date expirationDate = CalendarUtils.endOfDay(now);
		
		AssessmentMessage message = assessmentMessageDao.createMessage("Hello", publicationDate, expirationDate,
				AssessmentMessagePublicationEnum.asap, entry, "create-message", id);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(message);
		Assert.assertNotNull(message.getKey());
		Assert.assertNotNull(message.getCreationDate());
		Assert.assertNotNull(message.getLastModified());
		Assert.assertNotNull(message.getExpirationDate());
		Assert.assertNotNull(message.getPublicationDate());
		Assert.assertEquals("Hello", message.getMessage());
		Assert.assertEquals(AssessmentMessagePublicationEnum.asap, message.getPublicationType());
		Assert.assertEquals(entry, message.getEntry());
		Assert.assertEquals("create-message", message.getResSubPath());	
	}
	
	@Test
	public void loadByKey() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Date now = new Date();
		Date publicationDate = CalendarUtils.startOfDay(now);
		Date expirationDate = CalendarUtils.endOfDay(now);
		
		AssessmentMessage message = assessmentMessageDao.createMessage("Hello world", publicationDate, expirationDate,
				AssessmentMessagePublicationEnum.scheduled, entry, "load-by-key-message", id);
		dbInstance.commitAndCloseSession();
		
		AssessmentMessage loadedMessage = assessmentMessageDao.loadByKey(message.getKey());
		
		Assert.assertNotNull(loadedMessage);
		Assert.assertNotNull(loadedMessage.getKey());
		Assert.assertNotNull(loadedMessage.getCreationDate());
		Assert.assertNotNull(loadedMessage.getLastModified());
		Assert.assertNotNull(loadedMessage.getExpirationDate());
		Assert.assertNotNull(loadedMessage.getPublicationDate());
		Assert.assertEquals("Hello world", message.getMessage());
		Assert.assertEquals(AssessmentMessagePublicationEnum.scheduled, message.getPublicationType());
		Assert.assertEquals(entry, loadedMessage.getEntry());
		Assert.assertEquals("load-by-key-message", loadedMessage.getResSubPath());	
	}
	
	@Test
	public void getMessagesInfos() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMessage message = assessmentMessageDao.createMessage("Hello", new Date(), new Date(),
				AssessmentMessagePublicationEnum.asap, entry, "load-message-infos", id);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentMessageInfos> infos = assessmentMessageDao.getMessagesInfos(entry, "load-message-infos");
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(message, infos.get(0).getMessage());
		Assert.assertEquals(0l, infos.get(0).getNumOfRead());
	}
	
	@Test
	public void getMessageInfos() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-3-singula");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMessage message = assessmentMessageDao.createMessage("Hello", new Date(), new Date(),
				AssessmentMessagePublicationEnum.asap, entry, "load-single-message-infos", id);
		AssessmentMessageLogImpl messageLog = assessmentMessageLogDao.createMessage(message, id, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(messageLog);
		
		AssessmentMessageInfos infos = assessmentMessageDao.getMessageInfos(message.getKey());
		Assert.assertNotNull(infos);
		Assert.assertEquals(message, infos.getMessage());
		Assert.assertEquals(1l, infos.getNumOfRead());
	}
	
	@Test
	public void hasMessages() {
		Date now = new Date();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-3-bool");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		assessmentMessageDao.createMessage("Hello", DateUtils.addHours(now, -2), DateUtils.addHours(now, 2),
				AssessmentMessagePublicationEnum.asap, entry, "load-bool-message-infos", id);
		dbInstance.commitAndCloseSession();
		
		boolean hasMessages = assessmentMessageDao.hasMessages(entry, "load-bool-message-infos", now);
		Assert.assertTrue(hasMessages);
		boolean hasNoMessages = assessmentMessageDao.hasMessages(entry, "load-bool-message-no-infos", now);
		Assert.assertFalse(hasNoMessages);
	}
	
	@Test
	public void getMessagesNotRead() {
		Date now = new Date();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMessage message = assessmentMessageDao.createMessage("Do it", DateUtils.addDays(now, -1), DateUtils.addDays(now, 1),
				AssessmentMessagePublicationEnum.asap, entry, "load-message-not-read", id);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentMessageWithReadFlag> infos = assessmentMessageDao.getMessages(entry, "load-message-not-read", id, now);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(message, infos.get(0).getMessage());
		Assert.assertFalse(infos.get(0).isRead());
	}

	@Test
	public void getMessagesRead() {
		Date now = new Date();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("scheduled-msg-5");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMessage message = assessmentMessageDao.createMessage("Read it", DateUtils.addDays(now, -1), DateUtils.addDays(now, 1),
				AssessmentMessagePublicationEnum.asap, entry, "load-message-read", id);
		AssessmentMessageLogImpl messageLog = assessmentMessageLogDao.createMessage(message, id, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(messageLog);
		
		List<AssessmentMessageWithReadFlag> infos = assessmentMessageDao.getMessages(entry, "load-message-read", id, now);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(message, infos.get(0).getMessage());
		Assert.assertTrue(infos.get(0).isRead());
	}
}
