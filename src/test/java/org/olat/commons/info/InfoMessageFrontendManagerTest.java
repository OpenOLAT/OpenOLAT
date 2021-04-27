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
package org.olat.commons.info;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 20.03.2017
 * 
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class InfoMessageFrontendManagerTest extends OlatTestCase {

	@Autowired
	private NotificationsManager notificationManager;
	@Autowired
	private InfoMessageFrontendManager infoManager;
	@Autowired
	private BusinessGroupService groupService;
	@Autowired
	private DB dbInstance;

	@Test
	public void createSaveLoadAndCountInfoMessage() {
		// same methods as already tested @InfoManagerTest
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-3");
		final String resName = UUID.randomUUID().toString();
		Random random = new Random();
		final InfoOLATResourceable ores = new InfoOLATResourceable(random.nextLong(), resName);
		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		assertNotNull(msg1);
		infoManager.saveInfoMessage(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id3);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		assertNotNull(msg2);
		infoManager.saveInfoMessage(msg2);
		// create, not save
		InfoMessage msg3 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id1);
		msg3.setTitle("title-3");
		msg3.setMessage("message-3");
		assertNotNull(msg3);
		infoManager.saveInfoMessage(msg3);

		dbInstance.commitAndCloseSession();

		// load by key
		InfoMessage loadedMsg1 = infoManager.loadInfoMessage(msg1.getKey());
		assertNotNull(loadedMsg1);
		InfoMessage loadedMsg2 = infoManager.loadInfoMessage(msg2.getKey());
		assertNotNull(loadedMsg2);
		InfoMessage loadedMsg3 = infoManager.loadInfoMessage(msg3.getKey());
		assertNotNull(loadedMsg3);

		// load by resource
		List<InfoMessage> loadedMessages = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		assertNotNull(loadedMessages);
		Assert.assertEquals(3, loadedMessages.size());
		Assert.assertTrue(loadedMessages.contains(msg1));
		Assert.assertTrue(loadedMessages.contains(msg2));
		Assert.assertTrue(loadedMessages.contains(msg3));

		// count info messages
		int count = infoManager.countInfoMessageByResource(ores, null, null, null, null);
		Assert.assertEquals(3, count);
	}

	@Test
	public void deleteInfoMessage() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		final String resName = UUID.randomUUID().toString();
		final InfoOLATResourceable ores = new InfoOLATResourceable(5l, resName);
		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		assertNotNull(msg1);
		infoManager.saveInfoMessage(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id1);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		assertNotNull(msg2);
		infoManager.saveInfoMessage(msg2);

		dbInstance.commitAndCloseSession();

		infoManager.deleteInfoMessage(msg1);
		dbInstance.commitAndCloseSession();

		InfoMessage loadedMsg1 = infoManager.loadInfoMessage(msg1.getKey());
		Assert.assertNull(loadedMsg1);
		InfoMessage loadedMsg2 = infoManager.loadInfoMessage(msg2.getKey());
		assertNotNull(loadedMsg2);
	}

	@Test
	public void deleteInfoMessagesOfIdentity() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup businessGroup = groupService.createBusinessGroup(null, "gdao1", "gdao1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		final OLATResourceable ores = new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return businessGroup.getResourceableTypeName();
			}

			@Override
			public Long getResourceableId() {
				return businessGroup.getResourceableId();
			}
		};

		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		assertNotNull(msg1);
		infoManager.saveInfoMessage(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id1);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		assertNotNull(msg2);
		infoManager.saveInfoMessage(msg2);
		// create, save
		InfoMessage msg3 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id1);
		msg3.setTitle("title-3");
		msg3.setMessage("message-3");
		assertNotNull(msg3);
		infoManager.saveInfoMessage(msg3);

		dbInstance.commitAndCloseSession();

		infoManager.updateInfoMessagesOfIdentity(businessGroup, id1);
		dbInstance.commitAndCloseSession();

		// load messages after deletion
		List<InfoMessage> loadedMessages = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		Assert.assertEquals(1, loadedMessages.size());
		Assert.assertTrue(loadedMessages.contains(msg1));
		Assert.assertFalse(loadedMessages.contains(msg2));
		Assert.assertFalse(loadedMessages.contains(msg3));
	}

	@Test
	public void removeInfoMessagesAndSubscriptionContext() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup businessGroup = groupService.createBusinessGroup(null, "gdao1", "gdao1-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		final OLATResourceable ores = new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return businessGroup.getResourceableTypeName();
			}

			@Override
			public Long getResourceableId() {
				return businessGroup.getResourceableId();
			}
		};		
		// create, save
		InfoMessage msg1 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id2);
		msg1.setTitle("title-1");
		msg1.setMessage("message-1");
		assertNotNull(msg1);
		infoManager.saveInfoMessage(msg1);
		// create, save
		InfoMessage msg2 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id1);
		msg2.setTitle("title-2");
		msg2.setMessage("message-2");
		assertNotNull(msg2);
		infoManager.saveInfoMessage(msg2);
		// create, save
		InfoMessage msg3 = infoManager.createInfoMessage(ores, InfoMessageFrontendManager.businessGroupResSubPath, null,
				id1);
		msg3.setTitle("title-3");
		msg3.setMessage("message-3");
		assertNotNull(msg3);
		infoManager.saveInfoMessage(msg3);

		dbInstance.commitAndCloseSession();
		
		SubscriptionContext sc = new SubscriptionContext(businessGroup.getResourceableTypeName(),
				businessGroup.getResourceableId(), InfoMessageFrontendManager.businessGroupResSubPath);
		PublisherData pd = new PublisherData("InfoMessage", "e.g. infoMessage=anyMessage", null);
		// subscribe
		notificationManager.subscribe(id1, sc, pd);
		notificationManager.subscribe(id2, sc, pd);
		dbInstance.closeSession();
		
		// check if publisher was created
		Publisher p = notificationManager.getPublisher(sc);
		assertNotNull(p);
		
		// check before message deletion
		List<InfoMessage> loadedMessages1 = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		Assert.assertEquals(3, loadedMessages1.size());
		Assert.assertTrue(loadedMessages1.contains(msg1));
		Assert.assertTrue(loadedMessages1.contains(msg2));
		Assert.assertTrue(loadedMessages1.contains(msg3));
		// delete
		infoManager.removeInfoMessagesAndSubscriptionContext(businessGroup);
		dbInstance.commitAndCloseSession();
		// check if messages are deleted
		List<InfoMessage> loadedMessages2 = infoManager.loadInfoMessageByResource(ores,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		Assert.assertEquals(0, loadedMessages2.size());
		Assert.assertFalse(loadedMessages2.contains(msg1));
		Assert.assertFalse(loadedMessages2.contains(msg2));
		Assert.assertFalse(loadedMessages2.contains(msg3));
		// check if pubisher is deleted
		Publisher p2 = notificationManager.getPublisher(sc);
		assertNull("publisher marked deleted should not be found", p2);

	}

	@Test
	public void getInfoSubscribers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("info-2");
		RepositoryEntry resource = JunitTestHelper.createAndPersistRepositoryEntry();
		BusinessGroup businessGroup = groupService.createBusinessGroup(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, resource);
		final OLATResourceable ores = new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return businessGroup.getResourceableTypeName();
			}

			@Override
			public Long getResourceableId() {
				return businessGroup.getResourceableId();
			}
		};
		// create publisher data
		String identifier = InfoMessageFrontendManager.businessGroupResSubPath;
		SubscriptionContext context = new SubscriptionContext(businessGroup.getResourceableTypeName(),
				businessGroup.getResourceableId(), identifier);
		PublisherData publisherData = new PublisherData("testGetSubscriberIdentities", "e.g. data=infomessage", null);
		dbInstance.commitAndCloseSession();

		// add subscribers
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();

		// get identities
		List<Identity> identities = infoManager.getInfoSubscribers(ores, identifier);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}

	private class InfoOLATResourceable implements OLATResourceable {
		private final Long resId;
		private final String resName;

		public InfoOLATResourceable(Long resId, String resName) {
			this.resId = resId;
			this.resName = resName;
		}

		@Override
		public String getResourceableTypeName() {
			return resName;
		}

		@Override
		public Long getResourceableId() {
			return resId;
		}
	}
}
