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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.DBRuntimeException;
import org.apache.logging.log4j.Logger;
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
	private NotificationsManager notificationManager;

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
	public void testCreatePublisher() {
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
	public void testCreateUpdatePublisher() {
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
		
		sleep(2000);

		//update the publisher
		notificationManager.markPublisherNews(context, null, false);
		
		//check if exists and last news date is updated
		Publisher reloadedPublisher = notificationManager.getPublisher(context);
		Assert.assertNotNull(reloadedPublisher);
		Assert.assertEquals(publisher, reloadedPublisher);
		Assert.assertTrue(publisher.getLatestNewsDate().before(reloadedPublisher.getLatestNewsDate()));
	}
	
	@Test
	public void  testAllPublishers() {
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("All", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testAllPublishers", "e.g. forumdata=keyofforum", null);
		
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(publisher);

		List<Publisher> publishers = notificationManager.getAllPublisher();
		Assert.assertNotNull(publishers);
		Assert.assertTrue(publishers.contains(publisher));
	}
	
	@Test
	public void testSubscribe() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("subs-" + UUID.randomUUID().toString());
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
	public void testMarkSubscriberRead() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("subs-" + UUID.randomUUID().toString());
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
		
		sleep(2000);
		
		notificationManager.markSubscriberRead(id, context);
		
		//check the last modification date
		Subscriber reloadedSubscriber = notificationManager.getSubscriber(subscriber.getKey());
		Assert.assertNotNull(reloadedSubscriber);
		Assert.assertEquals(subscriber,  reloadedSubscriber);
		Assert.assertTrue(subscriber.getLastModified().before(reloadedSubscriber.getLastModified()));
	}
	
	@Test
	public void testUnsubscribe_v1() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("unsubs-" + UUID.randomUUID().toString());
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
	public void testUnsubscribe_v2() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("unsubs-" + UUID.randomUUID().toString());
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
		notificationManager.unsubscribe(id, context);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean subscribed = notificationManager.isSubscribed(id, context);
		Assert.assertFalse(subscribed);
	}
	
	@Test
	public void testValidSubscribers() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("valid1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("valid1-" + UUID.randomUUID().toString());
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
	public void testValidSubscribersOf() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("valid1b-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("valid1b-" + UUID.randomUUID().toString());
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
	public void testGetSubscriberIdentities() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("valid1b-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("valid1b-" + UUID.randomUUID().toString());
		//create a publisher
		String identifier = UUID.randomUUID().toString().replace("-", "");
		SubscriptionContext context = new SubscriptionContext("Subscribers", Long.valueOf(123), identifier);
		PublisherData publisherData = new PublisherData("testGetSubscriberIdentities", "e.g. forumdata=keyofforum", null);
		Publisher publisher = notificationManager.getOrCreatePublisher(context, publisherData);
		dbInstance.commitAndCloseSession();
		//add subscribers
		notificationManager.subscribe(id1, context, publisherData);
		notificationManager.subscribe(id2, context, publisherData);
		dbInstance.commitAndCloseSession();
		
		//get identities
		List<Identity> identities = notificationManager.getSubscriberIdentities(publisher);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}
	
	@Test
	public void testGetSubscribersByTypes() {
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
		notificationManager.subscribe(id, context1, publisherData1);
		notificationManager.subscribe(id, context2, publisherData2);
		dbInstance.commitAndCloseSession();
		
		//get subscribers without types
		List<Subscriber> emptySubscribers = notificationManager.getSubscribers(id, null, true);
		Assert.assertNotNull(emptySubscribers);
		Assert.assertEquals(2, emptySubscribers.size());

		//get subscribers with 1 type
		List<String> types = Collections.singletonList(publisher1.getType());
		List<Subscriber> typedSubscribers = notificationManager.getSubscribers(id, types, true);
		Assert.assertNotNull(typedSubscribers);
		Assert.assertEquals(1, typedSubscribers.size());

		//get subscribers with 2 types
		List<String> allTypes = new ArrayList<>(2);
		allTypes.add(publisher1.getType());
		allTypes.add(publisher2.getType());
		List<Subscriber> allSubscribers = notificationManager.getSubscribers(id, allTypes, true);
		Assert.assertNotNull(allSubscribers);
		Assert.assertEquals(2, allSubscribers.size());
	}
	
	//markPublisherNews
	
	@Test
	public void testGetSubscriptionInfos() {
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
	public void testSubscriptions() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("fi1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("fi2-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("fi3-" + UUID.randomUUID().toString());

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
		notificationManager.unsubscribe(id1, sc);
		dbInstance.closeSession();
		
		boolean isStillSub = notificationManager.isSubscribed(id1, sc);
		assertFalse("subscribed::", isStillSub);
		
		notificationManager.delete(sc);
		dbInstance.commitAndCloseSession();
		
		Publisher p2 = notificationManager.getPublisher(sc);
		assertNull("publisher marked deleted should not be found", p2);
	}
	
	@Test(expected=DBRuntimeException.class)
	public void testDuplicateSubscribers() throws Exception {
		try {
			PublisherData pd = new PublisherData("CreateSubscriber@2x", "e.g. forumdata=keyofforum", null);
			SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(1238778567), UUID.randomUUID().toString().replace("-", ""));
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fci@2x-" + UUID.randomUUID().toString());
			Publisher publisher = notificationManager.getOrCreatePublisher(sc, pd);
			dbInstance.commit();
			
			((NotificationsManagerImpl)notificationManager).doCreateAndPersistSubscriber(publisher, id);
			dbInstance.commit();
			
			((NotificationsManagerImpl)notificationManager).doCreateAndPersistSubscriber(publisher, id);
			dbInstance.commit();
		} catch (Exception e) {
			dbInstance.rollback();
			throw e;
		}
	}
	
	@Test
	public void deletePublishersOf() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("subs-" + UUID.randomUUID().toString());
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
	
	/**
	 * Test creation of concurrent subscriber
	 */
	@Test
	public void testConcurrentCreateSubscriberWithOneIdentity() {
		final int NUM_OF_THREADS =  isOracleConfigured() ? 25 : 100;
		
		PublisherData pd = new PublisherData("CreateSubscriber", "e.g. forumdata=keyofforum", null);
		SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(1238778566), UUID.randomUUID().toString().replace("-", ""));
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fci-" + UUID.randomUUID().toString());
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));
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

		assertTrue("It throws an exception in test", exceptionHolder.isEmpty());	
		assertEquals("Thread(s) did not finish", NUM_OF_THREADS, statusList.size());
		assertTrue("Subscriber does not exists",  notificationManager.isSubscribed(id, sc));
	}
	
	/**
	 * Test creation of concurrent subscriber
	 */
	@Test
	public void testConcurrentSubscriberOperationsWithOneIdentity() {
		final int NUM_OF_THREADS = 100;
		
		PublisherData pd = new PublisherData("MPSubscriber", "e.g. forumdata=keyofforum", null);
		SubscriptionContext sc = new SubscriptionContext("MPSubscriber", Long.valueOf(1238778566), UUID.randomUUID().toString().replace("-", ""));
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fci-" + UUID.randomUUID().toString());
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));
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
				Thread.sleep(10);
				for(int i=5; i-->0; ) {
					//subscribe
					notificationManager.subscribe(id, sc, pd);
					
					//mark as read
					notificationManager.markSubscriberRead(id, sc);
					
					//update email date
					Publisher publisher = notificationManager.getPublisher(sc);
					Subscriber subscriber = notificationManager.getSubscriber(id, publisher);
					List<Subscriber> subscribersToUpdate = Collections.singletonList(subscriber);
					((NotificationsManagerImpl)notificationManager).updateSubscriberLatestEmail(subscribersToUpdate);
					
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
	public void testConcurrentFindOrCreatePublisher() {
		final int NUM_OF_THREADS = 10;

		PublisherData pd = new PublisherData("Forum", "e.g. forumdata=keyofforum", null );
		SubscriptionContext sc = new SubscriptionContext("Course", Long.valueOf(1238778565), UUID.randomUUID().toString().replace("-", ""));
		
		final CountDownLatch finishCount = new CountDownLatch(NUM_OF_THREADS);
		List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));
		List<SubscribeThread> threads = new ArrayList<>();
		for(int i=0; i<NUM_OF_THREADS; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fci-" + i + "-" + UUID.randomUUID().toString());
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
				Thread.sleep(10);
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
