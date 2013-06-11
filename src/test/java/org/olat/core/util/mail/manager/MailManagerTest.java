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
package org.olat.core.util.mail.manager;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBoxExtension;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.model.DBMail;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailManagerTest extends OlatTestCase {

	@Autowired
	private MailManager mailManager;
	@Autowired
	private MailBoxExtension mailBoxExtension;
	@Autowired
	private MailModule mailModule;
	@Autowired
	private DB dbInstance;
	
	private boolean inbox;
	
	@Before
	public void setInternalInbox() {
		inbox = mailModule.isInternSystem();
		if(!inbox) {
			mailModule.setInterSystem(true);
			sleep(500);//set of properties on module are async
		}
	}
	
	@After
	public void resetInternalInbox() {
		mailModule.setInterSystem(inbox);
	}
	
	@Test
	public void testCreateEmail() {
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-1-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-2-" + UUID.randomUUID().toString());

		MailerResult result = mailManager.sendMessage(null, fromId, null, toId, null, null, null, null, null, "Hello", "Hello world", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
	}
	
	@Test
	public void testGetInbox() {
		//send a mail
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-3-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-4-" + UUID.randomUUID().toString());
		MailerResult result = mailManager.sendMessage(null, fromId, null, toId, null, null, null, null, null, "Hello inbox", "Content of inbox", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of toId
		List<DBMail> incomingMails = mailManager.getInbox(toId, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingMails);
		Assert.assertEquals(1, incomingMails.size());
		
		DBMail incomingMail = incomingMails.get(0);
		Assert.assertNotNull(incomingMail);
		Assert.assertEquals("Hello inbox", incomingMail.getSubject());
		Assert.assertEquals("Content of inbox", incomingMail.getBody());	
	}
	
	@Test
	public void testGetOutbox() {
		//send a mail
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-5-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-6-" + UUID.randomUUID().toString());
		MailerResult result = mailManager.sendMessage(null, fromId, null, toId, null, null, null, null, null, "Hello outbox", "Content of outbox", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of toId
		List<DBMail> sendedMails = mailManager.getOutbox(fromId, 0, -1);
		Assert.assertNotNull(sendedMails);
		Assert.assertEquals(1, sendedMails.size());
		
		DBMail sendedMail = sendedMails.get(0);
		Assert.assertNotNull(sendedMail);
		Assert.assertEquals("Hello outbox", sendedMail.getSubject());
		Assert.assertEquals("Content of outbox", sendedMail.getBody());	
	}
	
	@Test
	public void testGetEmailByMetaId() {
		//send a mail
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-5-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-6-" + UUID.randomUUID().toString());
		MailerResult result = mailManager.sendMessage(null, fromId, null, toId, null, null, null, null, metaId, "Hello outbox", "Content of outbox", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of toId
		List<DBMail> mails = mailManager.getEmailsByMetaId(metaId);
		Assert.assertNotNull(mails);
		Assert.assertEquals(1, mails.size());
		
		DBMail mail = mails.get(0);
		Assert.assertNotNull(mail);
		Assert.assertEquals("Hello outbox", mail.getSubject());
		Assert.assertEquals("Content of outbox", mail.getBody());	
	}
	
	
	@Test
	public void testSendCC() {
		//send a mail to three ids
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-7-" + UUID.randomUUID().toString());
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsUser("mail-8-" + UUID.randomUUID().toString());
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsUser("mail-9-" + UUID.randomUUID().toString());
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsUser("mail-10-" + UUID.randomUUID().toString());
		
		ContactList ccs = new ContactList("unit-test-cc");
		ccs.add(toId_1);
		ccs.add(toId_2);
		ccs.add(toId_3);
		List<ContactList> ccList = Collections.singletonList(ccs);
		MailerResult result = mailManager.sendMessage(null, fromId, null, null, null, null, null, ccList, null, "Hello ccList", "Content of ccList", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of 1
		List<DBMail> incomingsMails = mailManager.getInbox(toId_1, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingsMails);
		Assert.assertEquals(1, incomingsMails.size());
		DBMail incomingMail = incomingsMails.get(0);
		Assert.assertNotNull(incomingMail);
		Assert.assertEquals("Hello ccList", incomingMail.getSubject());
		
		//retrieve the inbox of 2
		List<DBMail> incomingsMails_2 = mailManager.getInbox(toId_2, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingsMails_2);
		Assert.assertEquals(1, incomingsMails_2.size());
		Assert.assertEquals(incomingMail, incomingsMails_2.get(0));
		
		//retrieve the inbox of 3
		List<DBMail> incomingsMails_3 = mailManager.getInbox(toId_2, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingsMails_3);
		Assert.assertEquals(1, incomingsMails_3.size());
		Assert.assertEquals(incomingMail, incomingsMails_3.get(0));
	}
	
	@Test
	public void testDeleteUserData_groupedMail() {
		//send a mail to three ids
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-7-" + UUID.randomUUID().toString());
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsUser("mail-8-" + UUID.randomUUID().toString());
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsUser("mail-9-" + UUID.randomUUID().toString());
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsUser("mail-10-" + UUID.randomUUID().toString());
		
		ContactList ccs = new ContactList("unit-test-cc");
		ccs.add(toId_1);
		ccs.add(toId_2);
		ccs.add(toId_3);
		ccs.add(fromId);
		List<ContactList> ccList = Collections.singletonList(ccs);
		MailerResult result = mailManager.sendMessage(null, fromId, null, null, null, null, null, ccList, metaId, "Hello delList", "Content of delList", null);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();

		//delete the 4 users datas
		mailBoxExtension.deleteUserData(toId_1, "lalala");
		mailBoxExtension.deleteUserData(toId_2, "lalala");
		mailBoxExtension.deleteUserData(toId_3, "lalala");
		mailBoxExtension.deleteUserData(fromId, "lalala");
		dbInstance.commitAndCloseSession();
		
		//check inbox / outbox
		List<DBMail> deletedMails_1 = mailManager.getInbox(toId_1, null, null, null, 0, -1);
		Assert.assertNotNull(deletedMails_1);
		Assert.assertTrue(deletedMails_1.isEmpty());
		List<DBMail> deletedMails_2 = mailManager.getInbox(toId_2, null, null, null, 0, -1);
		Assert.assertNotNull(deletedMails_2);
		Assert.assertTrue(deletedMails_2.isEmpty());
		List<DBMail> deletedMails_3 = mailManager.getInbox(toId_3, null, null, null, 0, -1);
		Assert.assertNotNull(deletedMails_3);
		Assert.assertTrue(deletedMails_3.isEmpty());
		List<DBMail> deletedMails_4 = mailManager.getOutbox(fromId, 0, -1);
		Assert.assertNotNull(deletedMails_4);
		Assert.assertTrue(deletedMails_4.isEmpty());
		//check mail by meta id
		List<DBMail> deletedMails = mailManager.getEmailsByMetaId(metaId);
		Assert.assertNotNull(deletedMails);
		Assert.assertTrue(deletedMails.isEmpty());
	}
	
	@Test
	public void testDeleteUserData_separatedMail() {
		//send a mail as separated e-mails to three ids
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-7-" + UUID.randomUUID().toString());
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsUser("mail-8-" + UUID.randomUUID().toString());
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsUser("mail-9-" + UUID.randomUUID().toString());
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsUser("mail-10-" + UUID.randomUUID().toString());
		
		MailerResult result1 = mailManager.sendMessage(null, fromId, null, toId_1, null, null, null, null, metaId, "Hello ccList", "Content of ccList", null);
		Assert.assertNotNull(result1);
		Assert.assertEquals(MailerResult.OK, result1.getReturnCode());
		MailerResult result2 = mailManager.sendMessage(null, fromId, null, toId_2, null, null, null, null, metaId, "Hello ccList", "Content of ccList", null);
		Assert.assertNotNull(result2);
		Assert.assertEquals(MailerResult.OK, result2.getReturnCode());
		MailerResult result3 = mailManager.sendMessage(null, fromId, null, toId_3, null, null, null, null, metaId, "Hello ccList", "Content of ccList", null);
		Assert.assertNotNull(result3);
		Assert.assertEquals(MailerResult.OK, result3.getReturnCode());
		dbInstance.commitAndCloseSession();

		//delete the 4 users datas
		mailBoxExtension.deleteUserData(fromId, "lalala");
		mailBoxExtension.deleteUserData(toId_1, "lalala");
		mailBoxExtension.deleteUserData(toId_2, "lalala");
		mailBoxExtension.deleteUserData(toId_3, "lalala");
		dbInstance.commitAndCloseSession();

		//check mail by meta id
		List<DBMail> deletedMails = mailManager.getEmailsByMetaId(metaId);
		Assert.assertNotNull(deletedMails);
		Assert.assertTrue(deletedMails.isEmpty());
	}
	
	
}
