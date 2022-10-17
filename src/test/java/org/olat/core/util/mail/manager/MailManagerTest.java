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

import java.util.List;
import java.util.UUID;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SmtpMessage;

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
	public void sendExternMessage() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mailman-1");
		
		MailBundle bundle = new MailBundle();
		bundle.setToId(id);
		bundle.setContent("Hello", "Hello world");
		
		MailerResult result = new MailerResult();
		mailManager.sendExternMessage(bundle, result, false);
		
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertFalse(messages.isEmpty());
		
		SmtpMessage message = messages.get(0);
		Assert.assertEquals(id.getUser().getEmail(), message.getHeaderValue("To"));
	}

	@Test
	public void testFromHeaderOverride() throws MessagingException {
		WebappHelper.setMailConfig("mailFromDomain","internal.com");
		
		Address fromx = new InternetAddress("External Sender <test1@external.com>");
		Address fromi = new InternetAddress("Internal Sender <test1@internal.com>");

		Address gr1 = new InternetAddress("group1:;");
		Address gr2 = new InternetAddress("group2:;");

		Address tox1 = new InternetAddress("External Recipient 1 <test1@external.com>");
		Address tox2 = new InternetAddress("External Recipient 2 <test1@external.com>");
		Address toi1 = new InternetAddress("Internal Recipient 1 <test1@internal.com>");
		Address toi2 = new InternetAddress("Internal Recipient 2 <test1@internal.com>");

		Address mailFrom = new InternetAddress(WebappHelper.getMailConfig("mailFrom"));
		
		Address[] groupRecipients = {gr1, gr2};
		Address[] mixedRecipients = {tox1, tox2, toi1, toi2};
		Address[] internalRecipients = {toi1, toi2};

		MailerResult result1 = new MailerResult();
		MimeMessage msg1 = mailManager.createMimeMessage(fromx, groupRecipients, groupRecipients, mixedRecipients, "Testsubject", "Testbody", null, result1);
		Assert.assertTrue("From header is set to admin address for external maildomain in from and recipients",
				msg1.getFrom()[0].equals(mailFrom));
		Assert.assertNotNull(result1);

		MailerResult result2 = new MailerResult();
		MimeMessage msg2 = mailManager.createMimeMessage(fromi, groupRecipients, groupRecipients, mixedRecipients, "Testsubject", "Testbody", null, result2);
		Assert.assertTrue("From header is set to real user address for internal maildomain in from and external recipients",
				msg2.getFrom()[0].equals(fromi));
		Assert.assertNotNull(result2);

		MailerResult result3 = new MailerResult();
		MimeMessage msg3 = mailManager.createMimeMessage(fromx, groupRecipients, groupRecipients, internalRecipients, "Testsubject", "Testbody", null, result3);
		Assert.assertTrue("From header is set to real user address for external maildomain in from and only internal recipients",
				msg3.getFrom()[0].equals(fromx));
		Assert.assertNotNull(result3);
	}
}
