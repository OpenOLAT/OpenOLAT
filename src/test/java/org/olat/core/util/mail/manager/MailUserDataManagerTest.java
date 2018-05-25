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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MailUserDataManagerTest {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private MailUserDataManager mailBoxExtension;
	

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

}
