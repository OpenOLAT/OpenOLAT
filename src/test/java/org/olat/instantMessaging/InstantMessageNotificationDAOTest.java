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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.manager.InstantMessageNotificationDAO;
import org.olat.instantMessaging.model.InstantMessageNotificationTypeEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessageNotificationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InstantMessageNotificationDAO notificationDao;
	
	
	@Test
	public void testCreateNotification() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-4", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-3-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-9-");
		InstantMessageNotification notification = notificationDao.createNotification(id2.getKey(), id.getKey(),
				chatResource, null, null, InstantMessageNotificationTypeEnum.message);
		Assert.assertNotNull(notification);
		Assert.assertNotNull(notification.getKey());
		Assert.assertNotNull(notification.getCreationDate());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteNotification() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-5", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-3-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-7-");
		InstantMessageNotification notification = notificationDao.createNotification(id2.getKey(), id.getKey(),
				chatResource, null, null, InstantMessageNotificationTypeEnum.message);
		Assert.assertNotNull(notification);
		dbInstance.commitAndCloseSession();
		
		notificationDao.deleteNotification(notification.getKey());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testDeleteNotificationByIdentity() {
		OLATResourceable chatResource1 = OresHelper.createOLATResourceableInstance("unit-test-6", System.currentTimeMillis());
		OLATResourceable chatResource2 = OresHelper.createOLATResourceableInstance("unit-test-7", System.currentTimeMillis());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-5-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-6-");
		InstantMessageNotification notification_1_1 = notificationDao.createNotification(id2.getKey(), id1.getKey(),
				chatResource1, null, null, InstantMessageNotificationTypeEnum.message);
		InstantMessageNotification notification_1_2 = notificationDao.createNotification(id2.getKey(), id1.getKey(),
				chatResource2, null, null, InstantMessageNotificationTypeEnum.message);
		InstantMessageNotification notification_2_1 = notificationDao.createNotification(id1.getKey(), id2.getKey(),
				chatResource1, null, null, InstantMessageNotificationTypeEnum.message);
		InstantMessageNotification notification_2_2 = notificationDao.createNotification(id1.getKey(), id2.getKey(),
				chatResource2, null, null, InstantMessageNotificationTypeEnum.message);
		dbInstance.commitAndCloseSession();
		
		//delete notifications 1 - 1
		notificationDao.deleteNotification(id1, chatResource1, null, null);
		dbInstance.commitAndCloseSession();
		
		//check the rest
		List<InstantMessageNotification> notifications_1 = notificationDao.getPrivateNotifications(id1);
		Assert.assertNotNull(notifications_1);
		Assert.assertEquals(1, notifications_1.size());
		Assert.assertFalse(notifications_1.contains(notification_1_1));
		Assert.assertTrue(notifications_1.contains(notification_1_2));
		
		List<InstantMessageNotification> notifications_2 = notificationDao.getPrivateNotifications(id2);
		Assert.assertNotNull(notifications_2);
		Assert.assertEquals(2, notifications_2.size());
		Assert.assertTrue(notifications_2.contains(notification_2_1));
		Assert.assertTrue(notifications_2.contains(notification_2_2));
	}
	
	@Test
	public void testDeleteNotificationByResourceThanChannel() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-8", System.currentTimeMillis());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-10-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-11-");
		InstantMessageNotification notificationResource = notificationDao.createNotification(id1.getKey(), id2.getKey(),
				chatResource, null, null, InstantMessageNotificationTypeEnum.message);
		InstantMessageNotification notificationChannel = notificationDao.createNotification(id2.getKey(), id1.getKey(),
				chatResource, "sub-path", "channeled", InstantMessageNotificationTypeEnum.message);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(notificationResource);
		
		notificationDao.deleteNotification(null, chatResource, null, null);
		dbInstance.commitAndCloseSession();
		
		List<InstantMessageNotification> notificationsResource = notificationDao.getNotifications(chatResource, null, null);
		Assert.assertTrue(notificationsResource.isEmpty());
		List<InstantMessageNotification> notificationsByChannel = notificationDao.getNotifications(chatResource, "sub-path", "channeled");
		assertThat(notificationsByChannel)
			.containsExactly(notificationChannel);
		
		notificationDao.deleteNotification(null, chatResource, "sub-path", "channeled");
		dbInstance.commitAndCloseSession();
		
		List<InstantMessageNotification> deletedNotificationsByChannel = notificationDao.getNotifications(chatResource, "sub-path", "channeled");
		Assert.assertTrue(deletedNotificationsByChannel.isEmpty());
	}
	
	@Test
	public void testDeleteNotificationByChannel() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-9", System.currentTimeMillis());
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-12-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-13-");
		InstantMessageNotification notificationResource = notificationDao.createNotification(id1.getKey(), id2.getKey(),
				chatResource, null, null, InstantMessageNotificationTypeEnum.message);
		InstantMessageNotification notificationChannel = notificationDao.createNotification(id2.getKey(), id1.getKey(),
				chatResource, "sub-path", "channeled", InstantMessageNotificationTypeEnum.request);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(notificationChannel);
		
		notificationDao.deleteNotification(null, chatResource, "sub-path", "channeled");
		dbInstance.commitAndCloseSession();
		
		List<InstantMessageNotification> deletedNotificationsByChannel = notificationDao.getNotifications(chatResource, "sub-path", "channeled");
		Assert.assertTrue(deletedNotificationsByChannel.isEmpty());
		List<InstantMessageNotification> notificationsResource = notificationDao.getNotifications(chatResource, null, null);
		assertThat(notificationsResource)
			.containsExactly(notificationResource);
		
		notificationDao.deleteNotification(null, chatResource, null, null);
		dbInstance.commitAndCloseSession();
		
		List<InstantMessageNotification> deletedNotificationsResource = notificationDao.getNotifications(chatResource, null, null);
		Assert.assertTrue(deletedNotificationsResource.isEmpty());
	}
	
	@Test
	public void testLoadPrivateNotificationByIdentity() {
		OLATResourceable chatResource1 = OresHelper.createOLATResourceableInstance("unit-test-4", System.currentTimeMillis());
		OLATResourceable chatResource2 = OresHelper.createOLATResourceableInstance("unit-test-5", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-4-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-10-");
		InstantMessageNotification notification1 = notificationDao.createNotification(id2.getKey(), id.getKey(),
				chatResource1, null, null, InstantMessageNotificationTypeEnum.message);
		InstantMessageNotification notification2 = notificationDao.createNotification(id2.getKey(),id.getKey(),
				chatResource2, null, null, InstantMessageNotificationTypeEnum.message);
		dbInstance.commitAndCloseSession();
		
		List<InstantMessageNotification> notifications = notificationDao.getPrivateNotifications(id);
		Assert.assertNotNull(notifications);
		Assert.assertEquals(2, notifications.size());
		Assert.assertTrue(notifications.contains(notification1));
		Assert.assertTrue(notifications.contains(notification2));
	}
	
	@Test
	public void testLoadRequestNotificationByIdentity() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-10", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-14-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-15-");
		InstantMessageNotification notification1 = notificationDao.createNotification(id2.getKey(), id.getKey(),
				chatResource, null, null, InstantMessageNotificationTypeEnum.request);
		InstantMessageNotification notification2 = notificationDao.createNotification(id2.getKey(),id.getKey(),
				chatResource, "sub-path", "channel", InstantMessageNotificationTypeEnum.request);
		InstantMessageNotification notification3 = notificationDao.createNotification(id2.getKey(),id.getKey(),
				chatResource, "sub-path", "channel", InstantMessageNotificationTypeEnum.message);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(notification3);
		
		List<InstantMessageNotification> notifications = notificationDao.getRequestNotifications(id);
		assertThat(notifications)
			.containsExactlyInAnyOrder(notification1, notification2);
	}
	
	@Test
	public void testCountRequestNotificationByIdentity() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-11", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-16-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("im-17-");
		InstantMessageNotification notification1 = notificationDao.createNotification(id2.getKey(),id.getKey(),
				chatResource, "sub-path", "channel", InstantMessageNotificationTypeEnum.request);
		InstantMessageNotification notification2 = notificationDao.createNotification(id2.getKey(),id.getKey(),
				chatResource, "sub-path", "channel", InstantMessageNotificationTypeEnum.message);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(notification1);
		Assert.assertNotNull(notification2);
		
		long notifications = notificationDao.countRequestNotifications(id);
		Assert.assertEquals(1, notifications);
	}
}
