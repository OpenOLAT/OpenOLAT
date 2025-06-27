/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.notifications.manager;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherChannel;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juin 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SubscriberDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SubscriberDAO subscriberDao;
	@Autowired
	private NotificationsManager notificationsManager;
	
	@Test
	public void getSubscribersByData() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b1-");

		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("SubscriberDAOTest", Long.valueOf(123000), identifier);
		PublisherData publisherData = new PublisherData("getSubscribersByData", identifier, null);
		Publisher publisher = notificationsManager.getOrCreatePublisherWithData(context, publisherData, null, PublisherChannel.DIRECT_EMAIL);
		dbInstance.commitAndCloseSession();
		Subscriber subscriber = notificationsManager.subscribe(id, publisher);
		dbInstance.commitAndCloseSession();

		List<Subscriber> subscribers = subscriberDao.getSubscribers(publisherData, PublisherChannel.DIRECT_EMAIL);
		Assertions.assertThat(subscribers)
			.hasSize(1)
			.containsExactly(subscriber);
	}
	
	@Test
	public void getSubscriberOfRootPublisher() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b2-");

		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("SubscriberDAOTest", CodeHelper.getForeverUniqueID(), identifier);
		PublisherData publisherData = new PublisherData("PublisherRootByData", identifier, null);
		Publisher rootPublisher = notificationsManager.getOrCreatePublisherWithData(context, publisherData, null, PublisherChannel.DIRECT_EMAIL);
		PublisherData subPublisherData = new PublisherData("PublisherSubByData", identifier, null);
		Publisher subPublisher = notificationsManager.getOrCreatePublisherWithData(context, subPublisherData, rootPublisher, PublisherChannel.DIRECT_EMAIL);
		
		dbInstance.commitAndCloseSession();
		Subscriber rootSubscriber = notificationsManager.subscribe(id, rootPublisher);
		Subscriber subSubscriber = notificationsManager.subscribe(id, subPublisher);
		dbInstance.commitAndCloseSession();

		Subscriber subscriber = subscriberDao.getSubscriberOfRootPublisher(id, context);
		Assert.assertEquals(rootSubscriber, subscriber);
		Assert.assertNotEquals(subSubscriber, subscriber);
	}
	
	@Test
	public void getSubscriberWithData() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b3-");

		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("SubscriberDAOTest", CodeHelper.getForeverUniqueID(), identifier);
		PublisherData publisherData = new PublisherData("PublisherWithDataRootByData", identifier, null);
		Publisher rootPublisher = notificationsManager.getOrCreatePublisherWithData(context, publisherData, null, PublisherChannel.PULL);
		PublisherData subPublisherData = new PublisherData("PublisherWithDataSubByData", identifier, null);
		Publisher subPublisher = notificationsManager.getOrCreatePublisherWithData(context, subPublisherData, rootPublisher, PublisherChannel.PULL);
		
		dbInstance.commitAndCloseSession();
		Subscriber rootSubscriber = notificationsManager.subscribe(id, rootPublisher);
		Subscriber subSubscriber = notificationsManager.subscribe(id, subPublisher);
		dbInstance.commitAndCloseSession();

		Subscriber subscriber = subscriberDao.getSubscriber(id, context, subPublisherData);
		Assert.assertEquals(subSubscriber, subscriber);
		Assert.assertNotEquals(rootSubscriber, subscriber);
	}

}
