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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MailUserDataManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private MailUserDataManager mailBoxExtension;
	

	@Test
	public void testDeleteUserDataGroupedMail() {
		//send a mail to three ids
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-7");
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-8");
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-9");
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-10");
		
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
		List<DBMailLight> deletedMails_1 = mailManager.getInbox(toId_1, false, false, false, null, 0, -1);
		Assert.assertNotNull(deletedMails_1);
		Assert.assertTrue(deletedMails_1.isEmpty());
		List<DBMailLight> deletedMails_2 = mailManager.getInbox(toId_2, false, false, false, null, 0, -1);
		Assert.assertNotNull(deletedMails_2);
		Assert.assertTrue(deletedMails_2.isEmpty());
		List<DBMailLight> deletedMails_3 = mailManager.getInbox(toId_3, false, false, false, null, 0, -1);
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
	public void testDeleteUserDataSeparatedMail() {
		//send a mail as separated e-mails to three ids
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-7");
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-8");
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-9");
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-10");
		
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
	public void testDeleteUserDataSeveralMails() {
		//send a mail to three ids
		String metaId = UUID.randomUUID().toString();
		Identity toId_2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-11");
		Identity toId_1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-12");
		Identity toId_3 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-13");
		Identity toId_4 = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-14");
		
		for(int i=0; i<10; i++) {
			ContactList ccs = new ContactList("unit-test-cc");
			
			Identity from;
			if(i % 2 == 0) {
				ccs.add(toId_1);
				from = toId_2;
			} else {
				ccs.add(toId_2);
				from = toId_1;
			}
			ccs.add(toId_3);
			ccs.add(toId_4);
			
			MailBundle bundle = new MailBundle();
			bundle.setFromId(from);
			bundle.setContactList(ccs);
			bundle.setMetaId(metaId);
			bundle.setContent("Hello delList", "Content of delList");
			
			MailerResult result = mailManager.sendMessage(bundle);
			Assert.assertNotNull(result);
			Assert.assertEquals(MailerResult.OK, result.getReturnCode());
			dbInstance.commitAndCloseSession();
		}

		//delete the 4 users datas
		mailBoxExtension.deleteUserData(toId_4, "lalala-14");
		dbInstance.commitAndCloseSession();
		mailBoxExtension.deleteUserData(toId_3, "lalala-13");
		dbInstance.commitAndCloseSession();
		mailBoxExtension.deleteUserData(toId_2, "lalala-12");
		dbInstance.commitAndCloseSession();
		mailBoxExtension.deleteUserData(toId_1, "lalala-11");
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteUserDataSimpleMail() throws URISyntaxException {
		//send a mail to three ids
		String metaId = UUID.randomUUID().toString();
		Identity fromId = JunitTestHelper.createAndPersistIdentityAsRndUser("mail-15");
		
		URL documentUrl = JunitTestHelper.class.getResource("file_resources/Dissertation.pdf");
		File documentFile = new File(documentUrl.toURI());
		
		MailBundle bundle = new MailBundle();
		bundle.setFromId(fromId);
		bundle.setTo("noreply@frentix.com");
		bundle.setMetaId(metaId);
		bundle.setContent("Hello noreply", "Content of without answer", documentFile);
		
		MailerResult result = mailManager.sendMessage(bundle);
		Assert.assertNotNull(result);
		Assert.assertEquals(MailerResult.OK, result.getReturnCode());
		dbInstance.commitAndCloseSession();

		//delete the 4 users datas
		mailBoxExtension.deleteUserData(fromId, "lalala-15");
		dbInstance.commitAndCloseSession();
	}
}
