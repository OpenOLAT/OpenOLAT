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
package org.olat.instantMessaging;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.manager.InstantMessageDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessageDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InstantMessageDAO imDao;
	
	@Test
	public void testCreateMessage() {
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-test-1", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-1-");
		InstantMessage msg = imDao.createMessage(id, id.getName(), false, "Hello world", chatResources);
		Assert.assertNotNull(msg);
		Assert.assertNotNull(msg.getKey());
		Assert.assertNotNull(msg.getCreationDate());
		Assert.assertEquals("Hello world", msg.getBody());
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testLoadMessage_byId() {
		//create a message
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-test-2-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-2-");
		InstantMessage msg = imDao.createMessage(id, id.getName(), false, "Hello load by id", chatResources);
		Assert.assertNotNull(msg);
		dbInstance.commitAndCloseSession();
		
		//load the message
		InstantMessage reloadedMsg = imDao.loadMessageById(msg.getKey());
		Assert.assertNotNull(reloadedMsg);
		Assert.assertEquals(msg.getKey(), reloadedMsg.getKey());
		Assert.assertEquals("Hello load by id", reloadedMsg.getBody());
	}
	
	@Test
	public void testLoadMessage_byResource() {
		//create a message
		OLATResourceable chatResources = OresHelper.createOLATResourceableInstance("unit-test-3-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-3-");
		InstantMessage msg = imDao.createMessage(id, id.getName(), false, "Hello load by resource", chatResources);
		Assert.assertNotNull(msg);
		dbInstance.commitAndCloseSession();
		
		//load the message
		List<InstantMessage> messageList = imDao.getMessages(chatResources, null, 0, -1);
		Assert.assertNotNull(messageList);
		Assert.assertEquals(1, messageList.size());
		Assert.assertEquals(msg.getKey(), messageList.get(0).getKey());
		Assert.assertEquals("Hello load by resource", messageList.get(0).getBody());
	}
	
	@Test
	public void testCreateNotification() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-4", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-3-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-9-");
		InstantMessageNotification notification = imDao.createNotification(id2.getKey(), id.getKey(), chatResource);
		Assert.assertNotNull(notification);
		Assert.assertNotNull(notification.getKey());
		Assert.assertNotNull(notification.getCreationDate());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteNotification() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-5", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-3-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-7-");
		InstantMessageNotification notification = imDao.createNotification(id2.getKey(), id.getKey(), chatResource);
		Assert.assertNotNull(notification);
		dbInstance.commitAndCloseSession();
		
		imDao.deleteNotification(notification.getKey());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteNotification_ByIdentity() {
		OLATResourceable chatResource1 = OresHelper.createOLATResourceableInstance("unit-test-6", System.currentTimeMillis());
		OLATResourceable chatResource2 = OresHelper.createOLATResourceableInstance("unit-test-7", System.currentTimeMillis());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-5-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-6-");
		InstantMessageNotification notification_1_1 = imDao.createNotification(id2.getKey(), id1.getKey(), chatResource1);
		InstantMessageNotification notification_1_2 = imDao.createNotification(id2.getKey(), id1.getKey(), chatResource2);
		InstantMessageNotification notification_2_1 = imDao.createNotification(id1.getKey(), id2.getKey(), chatResource1);
		InstantMessageNotification notification_2_2 = imDao.createNotification(id1.getKey(), id2.getKey(), chatResource2);
		dbInstance.commitAndCloseSession();
		
		//delete notifications 1 - 1
		imDao.deleteNotification(id1, chatResource1);
		dbInstance.commitAndCloseSession();
		
		//check the rest
		List<InstantMessageNotification> notifications_1 = imDao.getNotifications(id1);
		Assert.assertNotNull(notifications_1);
		Assert.assertEquals(1, notifications_1.size());
		Assert.assertFalse(notifications_1.contains(notification_1_1));
		Assert.assertTrue(notifications_1.contains(notification_1_2));
		
		List<InstantMessageNotification> notifications_2 = imDao.getNotifications(id2);
		Assert.assertNotNull(notifications_2);
		Assert.assertEquals(2, notifications_2.size());
		Assert.assertTrue(notifications_2.contains(notification_2_1));
		Assert.assertTrue(notifications_2.contains(notification_2_2));
	}
	
	@Test
	public void testLoadNotificationByIdentity() {
		OLATResourceable chatResource1 = OresHelper.createOLATResourceableInstance("unit-test-4", System.currentTimeMillis());
		OLATResourceable chatResource2 = OresHelper.createOLATResourceableInstance("unit-test-5", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-4-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-10-");
		InstantMessageNotification notification1 = imDao.createNotification(id2.getKey(), id.getKey(), chatResource1);
		InstantMessageNotification notification2 = imDao.createNotification(id2.getKey(),id.getKey(), chatResource2);
		dbInstance.commitAndCloseSession();
		
		List<InstantMessageNotification> notifications = imDao.getNotifications(id);
		Assert.assertNotNull(notifications);
		Assert.assertEquals(2, notifications.size());
		Assert.assertTrue(notifications.contains(notification1));
		Assert.assertTrue(notifications.contains(notification2));
	}
}
