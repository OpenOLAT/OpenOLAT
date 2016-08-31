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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBoxExtension;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.model.DBMailLight;
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
	private static final OLog log = Tracing.createLoggerFor(MailManagerTest.class);

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
		}
	}
	
	@After
	public void resetInternalInbox() {
		if(!inbox) {
			mailModule.setInterSystem(inbox);
		}
	}
	
	@Test
	public void testCreateEmail() {
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-1-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-2-" + UUID.randomUUID().toString());
		
		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setToId(toId);
		bundle.setContent("Hello", "Hello world");

		MailerResult result = mailManager.sendMessage(bundle);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
	}
	
	@Test
	public void testGetInbox() {
		//send a mail
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-3");
		Identity toId = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-4");
		
		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setToId(toId);
		bundle.setContent("Hello inbox", "Content of inbox");

		MailerResult result = mailManager.sendMessage(bundle);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of toId
		List<DBMailLight> incomingMails = mailManager.getInbox(toId, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingMails);
		Assert.assertEquals(1, incomingMails.size());
		
		DBMailLight incomingMail = incomingMails.get(0);
		Assert.assertNotNull(incomingMail);
		Assert.assertEquals("Hello inbox", incomingMail.getSubject());	
	}
	
	@Test
	public void testGetOutbox() {
		//send a mail
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-5-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-6-" + UUID.randomUUID().toString());
		
		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setToId(toId);
		bundle.setContent("Hello outbox","Content of outbox");
		
		MailerResult result = mailManager.sendMessage(bundle);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the outbox of toId (with lazy loading)
		List<DBMailLight> sendedMails = mailManager.getOutbox(fromId, 0, -1, false);
		Assert.assertNotNull(sendedMails);
		Assert.assertEquals(1, sendedMails.size());
		
		DBMailLight sendedMail = sendedMails.get(0);
		Assert.assertNotNull(sendedMail);
		Assert.assertEquals("Hello outbox", sendedMail.getSubject());
		
		dbInstance.commitAndCloseSession();
		
		//retrieve the outbox of toId (with fetch)
		List<DBMailLight> sendedMailsWithFetch = mailManager.getOutbox(fromId, 0, -1, true);
		Assert.assertNotNull(sendedMailsWithFetch);
		Assert.assertEquals(1, sendedMailsWithFetch.size());
	}
	
	@Test
	public void testGetEmailByMetaId() {
		//send a mail
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-5-" + UUID.randomUUID().toString());
		Identity toId = JunitTestHelper.createAndPersistIdentityAsUser("mail-6-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setToId(toId);
		bundle.setMetaId(metaId);
		bundle.setContent("Hello meta ID", "Meta ID");
		
		MailerResult result = mailManager.sendMessage(bundle);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of toId
		List<DBMailLight> mails = mailManager.getEmailsByMetaId(metaId);
		Assert.assertNotNull(mails);
		Assert.assertEquals(1, mails.size());
		
		DBMailLight mail = mails.get(0);
		Assert.assertNotNull(mail);
		Assert.assertEquals("Hello meta ID", mail.getSubject());	
	}
	
	
	@Test
	public void testSend_BCC() {
		//send a mail to three ids
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsUser("mail-7-" + UUID.randomUUID().toString());
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsUser("mail-8-" + UUID.randomUUID().toString());
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsUser("mail-9-" + UUID.randomUUID().toString());
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsUser("mail-10-" + UUID.randomUUID().toString());
		
		ContactList ccs = new ContactList("unit-test-cc");
		ccs.add(toId_1);
		ccs.add(toId_2);
		ccs.add(toId_3);

		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setContactList(ccs);
		bundle.setContent("Hello ccList", "Content of ccList");
		
		MailerResult result = mailManager.sendMessage(bundle);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();
		
		//retrieve the inbox of 1
		List<DBMailLight> incomingsMails = mailManager.getInbox(toId_1, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingsMails);
		Assert.assertEquals(1, incomingsMails.size());
		DBMailLight incomingMail = incomingsMails.get(0);
		Assert.assertNotNull(incomingMail);
		Assert.assertEquals("Hello ccList", incomingMail.getSubject());
		
		//retrieve the inbox of 2
		List<DBMailLight> incomingsMails_2 = mailManager.getInbox(toId_2, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
		Assert.assertNotNull(incomingsMails_2);
		Assert.assertEquals(1, incomingsMails_2.size());
		Assert.assertEquals(incomingMail, incomingsMails_2.get(0));
		
		//retrieve the inbox of 3
		List<DBMailLight> incomingsMails_3 = mailManager.getInbox(toId_2, Boolean.TRUE, Boolean.TRUE, null, 0, -1);
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
		
		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setContactList(ccs);
		bundle.setMetaId(metaId);
		bundle.setContent("Hello delList", "Content of delList");
		
		MailerResult result = mailManager.sendMessage(bundle);
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
		List<DBMailLight> deletedMails_1 = mailManager.getInbox(toId_1, null, null, null, 0, -1);
		Assert.assertNotNull(deletedMails_1);
		Assert.assertTrue(deletedMails_1.isEmpty());
		List<DBMailLight> deletedMails_2 = mailManager.getInbox(toId_2, null, null, null, 0, -1);
		Assert.assertNotNull(deletedMails_2);
		Assert.assertTrue(deletedMails_2.isEmpty());
		List<DBMailLight> deletedMails_3 = mailManager.getInbox(toId_3, null, null, null, 0, -1);
		Assert.assertNotNull(deletedMails_3);
		Assert.assertTrue(deletedMails_3.isEmpty());
		List<DBMailLight> deletedMails_4 = mailManager.getOutbox(fromId, 0, -1, true);
		Assert.assertNotNull(deletedMails_4);
		Assert.assertTrue(deletedMails_4.isEmpty());
		//check mail by meta id
		List<DBMailLight> deletedMails = mailManager.getEmailsByMetaId(metaId);
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
		
		MailBundle bundle_1 = new MailBundle();
		bundle_1.setFromId(fromId);
		bundle_1.setToId(toId_1);
		bundle_1.setMetaId(metaId);
		bundle_1.setContent("Hello ccList", "Content of ccList");
		
		MailerResult result1 = mailManager.sendMessage(bundle_1);
		Assert.assertNotNull(result1);
		Assert.assertEquals(MailerResult.OK, result1.getReturnCode());
		
		MailBundle bundle_2 = new MailBundle();
		bundle_2.setFromId(fromId);
		bundle_2.setToId(toId_2);
		bundle_2.setMetaId(metaId);
		bundle_2.setContent("Hello ccList", "Content of ccList");
		
		MailerResult result2 = mailManager.sendMessage(bundle_2);
		Assert.assertNotNull(result2);
		Assert.assertEquals(MailerResult.OK, result2.getReturnCode());
		
		MailBundle bundle_3 = new MailBundle();
		bundle_3.setFromId(fromId);
		bundle_3.setToId(toId_3);
		bundle_3.setMetaId(metaId);
		bundle_3.setContent("Hello ccList", "Content of ccList");
		
		MailerResult result3 = mailManager.sendMessage(bundle_3);
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
		List<DBMailLight> deletedMails = mailManager.getEmailsByMetaId(metaId);
		Assert.assertNotNull(deletedMails);
		Assert.assertTrue(deletedMails.isEmpty());
	}
	
	@Test
	public void testParalellSubscribers() {
		final int NUM_OF_THREADS = 10;
		final int NUM_OF_USERS = 10;
		final int NUM_OF_REDONDANCY = 50;

		List<Identity> identities = new ArrayList<>();
		for(int i=0; i<NUM_OF_USERS; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fci-" + i + "-" + UUID.randomUUID());
			for(int j=0; j<NUM_OF_REDONDANCY; j++) {
				identities.add(id);
			}
		}
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));
		List<SubscribeThread> threads = new ArrayList<SubscribeThread>();
		for(int i=0; i<NUM_OF_THREADS; i++) {
			List<Identity> ids = new ArrayList<>(identities);
			SubscribeThread thread = new SubscribeThread(ids, exceptionHolder, statusList, finishCount);
			threads.add(thread);
		}
		
		for(SubscribeThread thread:threads) {
			thread.start();
		}
		
		// sleep until threads should have terminated/excepted
		try {
			finishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
			Assert.fail();
		}

		assertTrue("It throws an exception in test", exceptionHolder.isEmpty());	
		assertEquals("Thread(s) did not finish", NUM_OF_THREADS, statusList.size());
	}

	private class SubscribeThread extends Thread {
		
		private final List<Identity> ids;
		private final List<Exception> exceptionHolder;
		private final List<Boolean> statusList;
		private final CountDownLatch countDown;

		public SubscribeThread(List<Identity> ids, List<Exception> exceptionHolder, List<Boolean> statusList, CountDownLatch countDown) {
			this.ids = ids;
			this.exceptionHolder = exceptionHolder;
			this.statusList = statusList;
			this.countDown = countDown;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(10);
				for(int i=5; i-->0; ) {
					for(Identity id:ids) {
						mailManager.subscribe(id);
					}
				}
				statusList.add(Boolean.TRUE);
			} catch (Exception ex) {
				ex.printStackTrace();
				log.error("", ex);
				exceptionHolder.add(ex);// no exception should happen
			} finally {
				countDown.countDown();
				dbInstance.closeSession();
			}
		}
	}
}
