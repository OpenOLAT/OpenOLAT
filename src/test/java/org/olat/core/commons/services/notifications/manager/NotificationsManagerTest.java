/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.services.notifications.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherChannel;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Dec 9, 2004
 *
 * @author Felix Jost
 * 
 */
public class NotificationsManagerTest extends OlatTestCase {
	private static final Logger log = Tracing.createLoggerFor(NotificationsManagerTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private NotificationsManagerImpl notificationManager;

	@Test
	public void getUserIntervalOrDefault() {
		String defInterval = notificationManager.getDefaultNotificationInterval();
		Assert.assertNotNull(defInterval);

		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-1");
		dbInstance.commitAndCloseSession();
		String checkId = notificationManager.getUserIntervalOrDefault(id);
		Assert.assertNotNull(checkId);

		String nullInterval = notificationManager.getUserIntervalOrDefault(null);
		Assert.assertNotNull(nullInterval);
		Assert.assertEquals(defInterval, nullInterval);
	}
	
	@Test
	public void createPublisher() {
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("PS", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testPublisherSubscriber", "e.g. forumdata=keyofforum", null);
		
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		//check values
		Assert.assertNotNull(publisher);
		Assert.assertNotNull(publisher.getKey());
		Assert.assertNotNull(publisher.getCreationDate());
		Assert.assertNotNull(publisher.getLatestNewsDate());
		Assert.assertEquals("PS", publisher.getResName());
		Assert.assertEquals(Long.valueOf(123), publisher.getResId());
		Assert.assertEquals(identifier, publisher.getSubidentifier());
		
		//check if exists
		Publisher reloadedPublisher = notificationManager.getPublisher(context);
		Assert.assertNotNull(reloadedPublisher);
		Assert.assertEquals(publisher, reloadedPublisher);
	}
	
	@Test
	public void getPublisher() {
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("PS", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testPublisherSubscriber", "e.g. forumdata=keyofforum", null);
		
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		Publisher reloadedPublisher = notificationManager.getPublisher(context, publisherData);
		Assert.assertNotNull(reloadedPublisher);
		Assert.assertEquals(publisher, reloadedPublisher);
	}
	
	@Test
	public void getPublisherWithNoData() {
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("NODATA", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testPublisherWithNoData", null, null);
		
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		Publisher reloadedPublisher = notificationManager.getPublisher(context, publisherData);
		Assert.assertNotNull(reloadedPublisher);
		Assert.assertEquals(publisher, reloadedPublisher);
	}
	
	@Test
	public void updatePublisher() {
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("SIMPLE", Long.valueOf(130), identifier);
		PublisherData data = new PublisherData("testPublisherWithNoData", "one", "[Publisher:0][" + identifier + ":0]");
		Publisher publisher = notificationManager.getOrCreatePublisher(context, data);
		dbInstance.commitAndCloseSession();

		PublisherData newData = new PublisherData("completNew", "two", "[SomethingElse:0][" + identifier + ":0]");
		notificationManager.updatePublisherData(context, newData);
		dbInstance.commitAndCloseSession();
		
		Publisher updatedPublisher = notificationManager.getPublisher(context);
		Assert.assertNotNull(updatedPublisher);
		Assert.assertEquals(publisher, updatedPublisher);
	}
	
	/**
	 * @see https://track.frentix.com/issue/OO-9150
	 */
	@Test
	public void updateDoubledPublishers() {
		// Generate 2 publishers with the same context but different data
		String identifier = UUID.randomUUID().toString().replace("-", "");
		String type = "testDoublePublisher";
		String businessPath = "[Publisher:0][One:0][" + identifier + ":0]";
		
		SubscriptionContext context = new SubscriptionContext("DOUBLE", Long.valueOf(130), identifier);
		PublisherData data1 = new PublisherData(type, "one", businessPath);
		Publisher publisher1 = notificationManager.getOrCreatePublisherWithData(context, data1, null, PublisherChannel.PULL);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher1);
		
		PublisherData data2 = new PublisherData(type, "two", businessPath);
		Publisher publisher2 = notificationManager.getOrCreatePublisherWithData(context, data2, null, PublisherChannel.PULL);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher2);
		Assert.assertNotEquals(publisher1, publisher2);
		
		PublisherData newData = new PublisherData(type, "three", businessPath);
		Publisher updatedPublisher = notificationManager.updatePublisherData(context, newData);
		dbInstance.commitAndCloseSession();
		
		Publisher reloadedUpdatedPublisher = notificationManager.getPublisher(context);
		Assert.assertNotNull(reloadedUpdatedPublisher);
		Assert.assertEquals(updatedPublisher, reloadedUpdatedPublisher);
		Assert.assertEquals("three", reloadedUpdatedPublisher.getData());
		
		Publisher updatedPublisherWithData = notificationManager.getPublisher(context, newData);
		Assert.assertNotNull(updatedPublisherWithData);
		Assert.assertEquals(updatedPublisher, updatedPublisherWithData);
		Assert.assertEquals("three", updatedPublisherWithData.getData());
		
	}

	@Test
	public void updateAllSubscribers() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-2");
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("PS2", Long.valueOf(124), identifier);
		PublisherData publisherData = new PublisherData("testUpdateAllSubscribers", "e.g. forumdata=keyofforum", null);

		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);

		//subscribe
		Subscriber subscriber = notificationManager.subscribe(id, context, publisherData);
		dbInstance.commit();
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(publisher);
		Assert.assertNotNull(publisher.getKey());
		Assert.assertNotNull(publisher.getCreationDate());
		Assert.assertNotNull(publisher.getLatestNewsDate());
		Assert.assertEquals("PS2", publisher.getResName());
		Assert.assertEquals(Long.valueOf(124), publisher.getResId());
		Assert.assertNotNull(subscriber);

		notificationManager.updateAllSubscribers(publisher, false);

		//check enabled status
		Subscriber reloadedSubscriber = notificationManager.getSubscriber(subscriber.getKey());
		Assert.assertNotNull(reloadedSubscriber);
		Assert.assertEquals(subscriber, reloadedSubscriber);
		Assert.assertTrue(subscriber.isEnabled());
		Assert.assertFalse(reloadedSubscriber.isEnabled());
	}

	@Test
	public void updateSubscriber() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-1");
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("PS2", Long.valueOf(124), identifier);
		PublisherData publisherData = new PublisherData("testUpdateSubscriber", "e.g. forumdata=keyofforum", null);

		//subscribe
		Subscriber subscriber = notificationManager.subscribe(id, context, publisherData);
		dbInstance.commit();
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(subscriber);

		notificationManager.updateSubscriber(subscriber, false);

		//check enabled status
		Subscriber reloadedSubscriber = notificationManager.getSubscriber(subscriber.getKey());
		Assert.assertNotNull(reloadedSubscriber);
		Assert.assertEquals(subscriber, reloadedSubscriber);
		Assert.assertTrue(subscriber.isEnabled());
		Assert.assertFalse(reloadedSubscriber.isEnabled());
	}
	
	@Test
	public void createUpdatePublisher() {
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("PS2", Long.valueOf(124), identifier);
		PublisherData publisherData = new PublisherData("testPublisherSubscriber", "e.g. forumdata=keyofforum", null);
		
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		//check values
		Assert.assertNotNull(publisher);
		Assert.assertNotNull(publisher.getKey());
		Assert.assertNotNull(publisher.getCreationDate());
		Assert.assertNotNull(publisher.getLatestNewsDate());
		Assert.assertEquals("PS2", publisher.getResName());
		Assert.assertEquals(Long.valueOf(124), publisher.getResId());
		
		sleep(1100);

		//update the publisher
		notificationManager.markPublisherNews(context, null, false);
		
		//check if exists and last news date is updated
		Publisher reloadedPublisher = notificationManager.getPublisher(context);
		Assert.assertNotNull(reloadedPublisher);
		Assert.assertEquals(publisher, reloadedPublisher);
		Assert.assertTrue(publisher.getLatestNewsDate().before(reloadedPublisher.getLatestNewsDate()));
	}
	
	@Test
	public void subscribe() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-3");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("All", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testAllPublishers", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		//subscribe
		notificationManager.subscribe(id, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//check 
		boolean subscribed = notificationManager.isSubscribed(id, context);
		Assert.assertTrue(subscribed);
		dbInstance.commitAndCloseSession();
		
		//double check
		Subscriber subscriber = notificationManager.getSubscriber(id, publisher);
		Assert.assertNotNull(subscriber);
		Assert.assertEquals(publisher,  subscriber.getPublisher());
		dbInstance.commitAndCloseSession();
		
		//triple check
		Subscriber reloadedSubscriber = notificationManager.getSubscriber(subscriber.getKey());
		Assert.assertNotNull(reloadedSubscriber);
		Assert.assertEquals(subscriber,  reloadedSubscriber);
	}
	
	@Test
	public void markSubscriberRead() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-4");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("All", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testAllPublishers", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commit();
		Assert.assertNotNull(publisher);
		
		//subscribe
		notificationManager.subscribe(id, context, publisherData);
		dbInstance.commit();
		//load the subscriber
		Subscriber subscriber = notificationManager.getSubscriber(id, publisher);
		Assert.assertNotNull(subscriber);
		dbInstance.commitAndCloseSession();
		
		sleep(1100);
		
		notificationManager.markSubscriberRead(id, context);
		
		//check the last modification date
		Subscriber reloadedSubscriber = notificationManager.getSubscriber(subscriber.getKey());
		Assert.assertNotNull(reloadedSubscriber);
		Assert.assertEquals(subscriber,  reloadedSubscriber);
		Assert.assertTrue(subscriber.getLastModified().before(reloadedSubscriber.getLastModified()));
	}
	
	@Test
	public void unsubscribeBySubscriber() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("unsubs-1");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("All", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testUnsubscribe", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		//subscribe
		notificationManager.subscribe(id, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//check 
		Subscriber subscriber = notificationManager.getSubscriber(id, publisher);
		Assert.assertNotNull(subscriber);
		
		//unsubscribe
		notificationManager.unsubscribe(subscriber);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean subscribed = notificationManager.isSubscribed(id, context);
		Assert.assertFalse(subscribed);
	}
	
	@Test
	public void unsubscribeByContext() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("unsubs-2");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("All", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testUnsubscribe", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		//subscribe
		notificationManager.subscribe(id, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//check 
		Subscriber subscriber = notificationManager.getSubscriber(id, publisher);
		Assert.assertNotNull(subscriber);
		
		//unsubscribe
		notificationManager.unsubscribeContext(id, context);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean subscribed = notificationManager.isSubscribed(id, context);
		Assert.assertFalse(subscribed);
	}
	
	@Test
	public void unsubscribeByContextSeveralPublishers() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("unsubs-8");
		//create 3 publishers in the same context
		Long resId = CodeHelper.getForeverUniqueID();
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("All", resId, identifier);
		PublisherData publisherData = new PublisherData("testMultiUnsubscribe", "e.g. forumdata=keyofforum", null);
		Publisher rootPublisher = notificationManager.getOrCreatePublisherWithData(context, publisherData, null, PublisherChannel.PULL);
		PublisherData subPublisher1Data = new PublisherData("testMultiUnsubscribeSub", "comment-forum-1", null);
		Publisher subPublisher1 = notificationManager.getOrCreatePublisherWithData(context, subPublisher1Data, rootPublisher, PublisherChannel.DIRECT_EMAIL);
		PublisherData subPublisher2Data = new PublisherData("testMultiUnsubscribeSub", "comment-forum-2", null);
		Publisher subPublisher2 = notificationManager.getOrCreatePublisherWithData(context, subPublisher2Data, rootPublisher, PublisherChannel.DIRECT_EMAIL);
		dbInstance.commitAndCloseSession();
		
		//subscribe
		Subscriber rootSubscriber = notificationManager.subscribe(id, rootPublisher);
		Subscriber subscriber1 = notificationManager.subscribe(id, subPublisher1);
		Subscriber subscriber2 = notificationManager.subscribe(id, subPublisher2);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rootSubscriber);
		Assert.assertNotNull(subscriber1);
		Assert.assertNotNull(subscriber2);
		
		//unsubscribe
		notificationManager.unsubscribeContext(id, context);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean subscribed = notificationManager.hasSubscribers(id, List.of(rootPublisher, subPublisher1, subPublisher2));
		Assert.assertFalse(subscribed);
	}
	
	@Test
	public void validSubscribers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1-2");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("Valid", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testValidSubscribers", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		//add subscribers
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//get valid subscribers
		List<Subscriber> subscribers = notificationManager.getValidSubscribers(id1);
		Assert.assertNotNull(subscribers);
		Assert.assertEquals(1, subscribers.size());
		Assert.assertEquals(publisher, subscribers.get(0).getPublisher());
		Assert.assertEquals(id1, subscribers.get(0).getIdentity());
	}
	
	@Test
	public void validSubscribersOf() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b-2");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("Validb", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testValidSubscribers", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		//add subscribers
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//get all subscribers of the publisher
		List<Subscriber> subscribers = notificationManager.getValidSubscribersOf(publisher);
		Assert.assertNotNull(subscribers);
		Assert.assertEquals(2, subscribers.size());
		Assert.assertEquals(publisher, subscribers.get(0).getPublisher());
		Assert.assertEquals(publisher, subscribers.get(1).getPublisher());
	}
	
	@Test
	public void getSubscriberIdentities() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("valid1b2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("disabled1b-");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testGetSubscriberIdentities", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		//add subscribers
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		notificationManager.subscribe(id3, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		notificationManager.unsubscribeContext(id3, context);
		dbInstance.commitAndCloseSession();
		
		//get identities with enabled subscribers
		List<Identity> identities = notificationManager.getSubscriberIdentities(publisher, true);
		Assertions.assertThat(identities)
			.hasSize(2)
			.containsExactlyInAnyOrder(id1, id2);
		
		//get identities with enabled subscribers
		List<Identity> allIdentities = notificationManager.getSubscriberIdentities(publisher, false);
		Assertions.assertThat(allIdentities)
			.hasSize(3)
			.containsExactlyInAnyOrder(id1, id2, id3);
	}
	
	@Test
	public void getSubscribersAll() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("type-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("type-2");
		
		//create a first publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context1 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData1 = new PublisherData("testGetSubscribersByType1", "e.g. forumdata=keyofforum", null);
		notificationManager.getOrCreatePublisher(context1, publisherData1);
		dbInstance.commitAndCloseSession();
		
		//add subscribers
		Subscriber sub1 = notificationManager.subscribe(id1, context1, publisherData1);
		Subscriber sub2 = notificationManager.subscribe(id2, context1, publisherData1);
		dbInstance.commitAndCloseSession();

		//get subscribers without types
		List<Subscriber> subscribers = notificationManager.getSubscribers(id1, null, null, true, true);
		Assertions.assertThat(subscribers)
			.containsExactly(sub1)
			.doesNotContain(sub2);
	}

	@Test
	public void getSubscribersByType() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("type1-");
		//create a first publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context1 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData1 = new PublisherData("testGetSubscribersByType1", "e.g. forumdata=keyofforum", null);
		Publisher publisher1 = notificationManager.getOrCreatePublisher(context1, publisherData1);
		dbInstance.commitAndCloseSession();
		
		String identifier2 = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context2 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier2);
		PublisherData publisherData2 = new PublisherData("testGetSubscribersByType2", "e.g. forumdata=keyofforum", null);
		Publisher publisher2 = notificationManager.getOrCreatePublisher(context2, publisherData2);
		dbInstance.commitAndCloseSession();
		
		//add subscribers
		Subscriber sub1 = notificationManager.subscribe(id, context1, publisherData1);
		Subscriber sub2 = notificationManager.subscribe(id, publisher2);
		dbInstance.commitAndCloseSession();

		//get subscribers with 1 type
		List<String> types = List.of(publisher1.getType());
		List<Subscriber> typedSubscribers = notificationManager.getSubscribers(id, types, null, true, false);
		Assertions.assertThat(typedSubscribers)
			.containsExactlyInAnyOrder(sub1)
			.doesNotContain(sub2);
	}

	@Test
	public void getSubscribersByListOfTypes() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("type1-");
		//create a first publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context1 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData1 = new PublisherData("testGetSubscribersByType1", "e.g. forumdata=keyofforum", null);
		Publisher publisher1 = notificationManager.getOrCreatePublisher(context1, publisherData1);
		dbInstance.commitAndCloseSession();
		
		String identifier2 = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context2 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier2);
		PublisherData publisherData2 = new PublisherData("testGetSubscribersByType2", "e.g. forumdata=keyofforum", null);
		Publisher publisher2 = notificationManager.getOrCreatePublisher(context2, publisherData2);
		dbInstance.commitAndCloseSession();
		
		//add subscribers
		Subscriber sub1 = notificationManager.subscribe(id, context1, publisherData1);
		Subscriber sub2 = notificationManager.subscribe(id, publisher2);
		dbInstance.commitAndCloseSession();
		
		//get subscribers with 2 types
		List<String> allTypes = List.of(publisher1.getType(), publisher2.getType());
		List<Subscriber> allSubscribers = notificationManager.getSubscribers(id, allTypes, null, true, false);
		Assertions.assertThat(allSubscribers)
			.containsExactlyInAnyOrder(sub1, sub2);
	}
	
	@Test
	public void getSubscribersWithSubs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("type1-");
		//create a first publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context1 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData1 = new PublisherData("testGetSubscribersByType1", "e.g. forumdata=keyofforum", null);
		Publisher publisher1 = notificationManager.getOrCreatePublisher(context1, publisherData1);
		dbInstance.commitAndCloseSession();
		
		String identifier2 = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context2 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier2);
		PublisherData publisherData2 = new PublisherData("testGetSubscribersByType2", "e.g. forumdata=keyofforum", null);
		Publisher publisher2 = notificationManager.getOrCreatePublisherWithData(context2, publisherData2, publisher1, PublisherChannel.DIRECT_EMAIL);
		dbInstance.commitAndCloseSession();
		
		//add subscribers
		Subscriber sub1 = notificationManager.subscribe(id, context1, publisherData1);
		Subscriber sub2 = notificationManager.subscribe(id, publisher2);
		dbInstance.commitAndCloseSession();
		
		//get subscribers with 2 types but without sub-subscribers
		List<String> allTypes = List.of(publisher1.getType(), publisher2.getType());
		List<Subscriber> withoutSubSubscribers = notificationManager.getSubscribers(id, allTypes, null, true, false);
		Assertions.assertThat(withoutSubSubscribers)
			.containsExactlyInAnyOrder(sub1);
		
		//get subscribers with 2 types but with sub-subscribers
		List<Subscriber> withSubSubscribers = notificationManager.getSubscribers(id, allTypes, null, true, true);
		Assertions.assertThat(withSubSubscribers)
			.containsExactlyInAnyOrder(sub1, sub2);
	}
	
	@Test
	public void getSubscribersByChannel() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("type1-");
		//create a first publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context1 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData1 = new PublisherData("testGetSubscribersByType1", "e.g. forumdata=keyofforum", null);
		Publisher publisher1 = notificationManager.getOrCreatePublisherWithData(context1, publisherData1, null, PublisherChannel.PULL);
		dbInstance.commitAndCloseSession();
		
		String identifier2 = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context2 = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier2);
		PublisherData publisherData2 = new PublisherData("testGetSubscribersByType2", "e.g. forumdata=keyofforum", null);
		Publisher publisher2 = notificationManager.getOrCreatePublisherWithData(context2, publisherData2, publisher1, PublisherChannel.DIRECT_EMAIL);
		dbInstance.commitAndCloseSession();
		
		//add subscribers
		Subscriber sub1 = notificationManager.subscribe(id, context1, publisherData1);
		Subscriber sub2 = notificationManager.subscribe(id, publisher2);
		dbInstance.commitAndCloseSession();
		
		//get subscribers with 2 types but only mail channel
		List<String> allTypes = List.of(publisher1.getType(), publisher2.getType());
		List<Subscriber> mailedSubscribers = notificationManager.getSubscribers(id, allTypes, PublisherChannel.DIRECT_EMAIL, true, true);
		Assertions.assertThat(mailedSubscribers)
			.containsExactlyInAnyOrder(sub2);
		
		//get subscribers with 2 types and all channels
		List<Subscriber> allSubscribers = notificationManager.getSubscribers(id, allTypes, null, true, true);
		Assertions.assertThat(allSubscribers)
			.containsExactlyInAnyOrder(sub1, sub2);
	}
	
	@Test
	public void getSubscriptionInfos() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fi1-");
		SubscriptionContext context = new SubscriptionContext("Course", Long.valueOf(789521), UUID.randomUUID().toString());
		PublisherData publisherData = new PublisherData("Forum", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		notificationManager.subscribe(id, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//get infos
		List<SubscriptionInfo> infos = notificationManager.getSubscriptionInfos(id, publisher.getType());
		Assert.assertNotNull(infos);
	}

	@Test
	public void notifyAllSubscriptions() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("fi1-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("fi2-");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("fi3-");

		SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(123), UUID.randomUUID().toString());
		PublisherData pd = new PublisherData("Forum", "e.g. forumdata=keyofforum", null);

		SubscriptionContext sc2 = new SubscriptionContext("Course2", Long.valueOf(123), UUID.randomUUID().toString());
		PublisherData pd2 = new PublisherData("Forum", "e.g. forumdata=keyofforum2", null);

		dbInstance.closeSession();
		
		notificationManager.subscribe(id1, sc, pd);
		notificationManager.subscribe(id3, sc, pd);
		notificationManager.subscribe(id2, sc2, pd2);
		notificationManager.subscribe(id1, sc2, pd2);
				
		dbInstance.closeSession();

		Publisher p = notificationManager.getPublisher(sc);
		assertNotNull(p);
		
		assertEquals(p.getResName(), sc.getResName());
		assertEquals(p.getResId(), sc.getResId());
		assertEquals(p.getSubidentifier(), sc.getSubidentifier());
		
		boolean isSub = notificationManager.isSubscribed(id1, sc);
		assertTrue("subscribed::", isSub);
		
		notificationManager.notifyAllSubscribersByEmail();
		
		dbInstance.closeSession();
		notificationManager.unsubscribeContext(id1, sc);
		dbInstance.closeSession();
		
		boolean isStillSub = notificationManager.isSubscribed(id1, sc);
		assertFalse("subscribed::", isStillSub);
		
		notificationManager.delete(sc);
		dbInstance.commitAndCloseSession();
		
		Publisher p2 = notificationManager.getPublisher(sc);
		assertNull("publisher marked deleted should not be found", p2);
	}
	
	@Test(expected=DBRuntimeException.class)
	public void duplicateSubscribers() throws Exception {
		try {
			PublisherData pd = new PublisherData("CreateSubscriber@2x", "e.g. forumdata=keyofforum", null);
			SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(1238778567), UUID.randomUUID().toString().replace("-", ""));
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fci@2x-");
			Publisher publisher = notificationManager.getOrCreatePublisher(sc, pd);
			dbInstance.commit();
			
			notificationManager.doCreateAndPersistSubscriber(publisher, id);
			dbInstance.commit();
			
			notificationManager.doCreateAndPersistSubscriber(publisher, id);
			dbInstance.commit();
		} catch (Exception e) {
			dbInstance.rollback();
			throw e;
		}
	}
	
	@Test
	public void deletePublishersOf() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-5");
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("DeletePublishers", CodeHelper.getForeverUniqueID(), identifier);
		PublisherData publisherData = new PublisherData("testAllPublishers", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);
		
		//subscribe
		notificationManager.subscribe(id, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//check that there is something
		boolean subscribed = notificationManager.isSubscribed(id, context);
		Assert.assertTrue(subscribed);
		dbInstance.commitAndCloseSession();
		
		//delete
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(publisher.getResName(), publisher.getResId());
		int rows = notificationManager.deletePublishersOf(ores);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(2, rows);
	}

	@Test
	public void createDisabledSubscriberIfAbsent() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("subs-" + UUID.randomUUID());
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("CDS", Long.valueOf(321), identifier);
		PublisherData publisherData = new PublisherData("testCreateDisabledSubscriber", "data", null);

		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);

		// Create subscriber if absent (should create new disabled one)
		Subscriber subscriber = notificationManager
				.createDisabledSubscriberIfAbsent(id, publisher);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(subscriber);
		Assert.assertFalse(subscriber.isEnabled());
		Assert.assertEquals(id, subscriber.getIdentity());
		Assert.assertEquals(publisher, subscriber.getPublisher());

		// Call again - should return same subscriber, unchanged
		Subscriber again = notificationManager
				.createDisabledSubscriberIfAbsent(id, publisher);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(subscriber, again);
	}

	/**
	 * Test creation of concurrent subscriber
	 */
	@Test
	public void concurrentCreateSubscriberWithOneIdentity() {
		final int NUM_OF_THREADS =  isOracleConfigured() ? 25 : 100;
		
		PublisherData pd = new PublisherData("CreateSubscriber", "e.g. forumdata=keyofforum", null);
		SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(1238778566), UUID.randomUUID().toString().replace("-", ""));
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fci-");
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<>(1));
		List<SubscribeThread> threads = new ArrayList<>();
		for(int i=0; i<NUM_OF_THREADS; i++) {
			SubscribeThread thread = new SubscribeThread(sc, pd, id, exceptionHolder, statusList, finishCount);
			threads.add(thread);
		}
		
		for(SubscribeThread thread:threads) {
			thread.start();
		}
		
		// sleep until threads should have terminated/excepted
		try {
			finishCount.await(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		for(Exception e:exceptionHolder) {
			log.error("Excpetion during concurrent subscription: ", e);
		}

		Assert.assertTrue("It throws an exception in test", exceptionHolder.isEmpty());	
		Assert.assertEquals("Thread(s) did not finish", NUM_OF_THREADS, statusList.size());
		Assert.assertTrue("Subscriber does not exists",  notificationManager.isSubscribed(id, sc));
	}
	
	/**
	 * Test creation of concurrent subscriber
	 */
	@Test
	public void concurrentSubscriberOperationsWithOneIdentity() {
		final int NUM_OF_THREADS = 25;
		
		PublisherData pd = new PublisherData("MPSubscriber", "e.g. forumdata=keyofforum", null);
		SubscriptionContext sc = new SubscriptionContext("MPSubscriber", Long.valueOf(1238778566), UUID.randomUUID().toString().replace("-", ""));
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fci-");
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<>(1));
		List<MPSubscriberThread> threads = new ArrayList<>();
		for(int i=0; i<NUM_OF_THREADS; i++) {
			MPSubscriberThread thread = new MPSubscriberThread(sc, pd, id, exceptionHolder, statusList, finishCount);
			threads.add(thread);
		}
		
		for(MPSubscriberThread thread:threads) {
			thread.start();
		}
		
		// sleep until threads should have terminated/excepted
		try {
			finishCount.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
			Assert.fail();
		}
		
		for(Exception e:exceptionHolder) {
			log.error("Excpetion during concurrent subscription: ", e);
		}

		assertTrue("It throws an exception in test", exceptionHolder.isEmpty());	
		assertEquals("Thread(s) did not finish", NUM_OF_THREADS, statusList.size());
		assertTrue("Subscriber does not exists",  notificationManager.isSubscribed(id, sc));
	}
	
	private class MPSubscriberThread extends Thread {
		private final SubscriptionContext sc;
		private final PublisherData pd;
		private final Identity id;

		private final List<Exception> exceptionHolder;
		private final List<Boolean> statusList;
		private final CountDownLatch countDown;

		public MPSubscriberThread(SubscriptionContext sc, PublisherData pd, Identity id,
				List<Exception> exceptionHolder, List<Boolean> statusList, CountDownLatch countDown) {
			this.sc = sc;
			this.pd = pd;
			this.id = id;
			this.exceptionHolder = exceptionHolder;
			this.statusList = statusList;
			this.countDown = countDown;
		}
		
		@Override
		public void run() {
			try {
				sleep(10);
				for(int i=5; i-->0; ) {
					//subscribe
					notificationManager.subscribe(id, sc, pd);
					
					//mark as read
					notificationManager.markSubscriberRead(id, sc);
					
					//update email date
					Publisher publisher = notificationManager.getPublisher(sc);
					Subscriber subscriber = notificationManager.getSubscriber(id, publisher);
					List<Subscriber> subscribersToUpdate = List.of(subscriber);
					notificationManager.updateSubscriberLatestEmail(subscribersToUpdate, new Date());
					
					dbInstance.closeSession();
				}
				statusList.add(Boolean.TRUE);
			} catch (Exception ex) {
				exceptionHolder.add(ex);// no exception should happen
			} finally {
				countDown.countDown();
				dbInstance.closeSession();
			}
		}
	}
	
	/**
	 * Test synchronized 'findOrCreatePublisher' triggered by method 'subscribe'. 
	 * Start 10 threads which call 'subscribe' with same SubscriptionContext.
	 */
	@Test
	public void concurrentFindOrCreatePublisher() {
		final int NUM_OF_THREADS = 10;

		PublisherData pd = new PublisherData("Forum", "e.g. forumdata=keyofforum", null );
		SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(1238778565), UUID.randomUUID().toString().replace("-", ""));
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<>(1));
		List<SubscribeThread> threads = new ArrayList<>();
		for(int i=0; i<NUM_OF_THREADS; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("fci-" + i);
			SubscribeThread thread = new SubscribeThread(sc, pd, id, exceptionHolder, statusList, finishCount);
			threads.add(thread);
		}
		
		for(SubscribeThread thread:threads) {
			thread.start();
		}
		
		// sleep until threads should have terminated/excepted
		try {
			finishCount.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
			Assert.fail();
		}

		assertTrue("It throws an exception in test", exceptionHolder.isEmpty());	
		assertEquals("Thread(s) did not finish", NUM_OF_THREADS, statusList.size());
		
		for(SubscribeThread thread:threads) {
			assertTrue("Subscriber does not exists",  notificationManager.isSubscribed(thread.getIdentity(), sc));
		}
	}
	
	private class SubscribeThread extends Thread {
		
		private final SubscriptionContext sc;
		private final PublisherData pd;
		private final Identity id;

		private final List<Exception> exceptionHolder;
		private final List<Boolean> statusList;
		private final CountDownLatch countDown;

		public SubscribeThread(SubscriptionContext sc, PublisherData pd, Identity id,
				List<Exception> exceptionHolder, List<Boolean> statusList, CountDownLatch countDown) {
			this.sc = sc;
			this.pd = pd;
			this.id = id;
			this.exceptionHolder = exceptionHolder;
			this.statusList = statusList;
			this.countDown = countDown;
		}
		
		public Identity getIdentity() {
			return id;
		}
		
		@Override
		public void run() {
			try {
				sleep(10);
				for(int i=5; i-->0; ) {
					notificationManager.subscribe(id, sc, pd);
					dbInstance.closeSession();
				}
				statusList.add(Boolean.TRUE);
			} catch (Exception ex) {
				exceptionHolder.add(ex);// no exception should happen
			} finally {
				countDown.countDown();
				dbInstance.closeSession();
			}
		}
	}
}
