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

package org.olat.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.testutils.codepoints.client.BreakpointStateException;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Dec 9, 2004
 *
 * @author Felix Jost
 * 
 * Comment:  
 * 
 */
public class NotificationsManagerTest extends OlatTestCase {

	private static OLog log = Tracing.createLoggerFor(NotificationsManagerTest.class);
	private static final String CODEPOINT_SERVER_ID = "NotificationsManagerTest";

	private static Identity identity1, identity2, identity3;
	
	@Autowired
	private NotificationsManager notificationManager;
	@Autowired
	private DB dbInstance;
	
	


	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup() {
			// identity with null User should be ok for test case
			identity1 = JunitTestHelper.createAndPersistIdentityAsUser("fi1-" + UUID.randomUUID().toString());
			identity2 = JunitTestHelper.createAndPersistIdentityAsUser("fi2-" + UUID.randomUUID().toString());
			identity3 = JunitTestHelper.createAndPersistIdentityAsUser("fi3-" + UUID.randomUUID().toString());
			dbInstance.commit();
	}
	
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}

	@Test
	public void testSubscriptions() {
		SubscriptionContext sc = new SubscriptionContext("Course", new Long(123), "676");
		PublisherData pd = new PublisherData("Forum", "e.g. forumdata=keyofforum", null);

		SubscriptionContext sc2 = new SubscriptionContext("Course2", new Long(123), "6762");
		PublisherData pd2 = new PublisherData("Forum", "e.g. forumdata=keyofforum2", null);

		//Publisher p = nm.getPublisher(sc);
		//assertNull(p);
		DBFactory.getInstance().closeSession();
		
		notificationManager.subscribe(identity1, sc, pd);
		notificationManager.subscribe(identity3, sc, pd);
		notificationManager.subscribe(identity2, sc2, pd2);
		notificationManager.subscribe(identity1, sc2, pd2);
				
		DBFactory.getInstance().closeSession();

		Publisher p = notificationManager.getPublisher(sc);
		assertNotNull(p);
		
		assertEquals(p.getResName(), sc.getResName());
		assertEquals(p.getResId(), sc.getResId());
		assertEquals(p.getSubidentifier(), sc.getSubidentifier());
		
		boolean isSub = notificationManager.isSubscribed(identity1, sc);
		assertTrue("subscribed::", isSub);
		
		notificationManager.notifyAllSubscribersByEmail();
		
		DBFactory.getInstance().closeSession();
		notificationManager.unsubscribe(identity1, sc);
		DBFactory.getInstance().closeSession();
		
		boolean isStillSub = notificationManager.isSubscribed(identity1, sc);
		assertFalse("subscribed::", isStillSub);
		
		notificationManager.delete(sc);
		
		Publisher p2 = notificationManager.getPublisher(sc);
		assertNull("publisher marked deleted should not be found", p2);
	}
	
	/**
	 * Test synchronized 'findOrCreatePublisher' triggered by method 'subscribe'. 
	 * Start 2 threads which call 'subscribe' with same SubscriptionContext.
	 * Breakpoint at doInSync, second thread must wait until thread 1 has released the breakpoint.
	 */
	@Test
	public void testConcurrentFindOrCreatePublisher() {
		
		JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
		
		final SubscriptionContext sc = new SubscriptionContext("Course", new Long(1238778565), UUID.randomUUID().toString().replace("-", ""));
		final PublisherData pd = new PublisherData("Forum", "e.g. forumdata=keyofforum", null );

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		// enable breakpoint

		CodepointClient codepointClient = null;
		CodepointRef codepointRef = null;
		try {
			codepointClient = CodepointClientFactory.createCodepointClient("vm://localhost?broker.persistent=false", CODEPOINT_SERVER_ID);
			codepointRef = codepointClient.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.notifications.NotificationsManagerImpl.findOrCreatePublisher");
			codepointRef.enableBreakpoint();
		} catch (Exception e) {
			log.error("", e);
			fail("Could not initialize CodepointClient");
		}
		
		// thread 1
		new Thread(new Runnable() {
			public void run() {
				try {
					NotificationsManager.getInstance().subscribe(identity1, sc, pd);
					DBFactory.getInstance().closeSession();
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentFindOrCreatePublisher thread1 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				}
			}}).start();
		
		// thread 2
		new Thread(new Runnable() {
			public void run() {
				try {
					sleep(1000);
					NotificationsManager.getInstance().subscribe(identity2, sc, pd);
					DBFactory.getInstance().closeSession();
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentFindOrCreatePublisher thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				}
			}}).start();

		System.out.println("Thread point 3");
		sleep(2000);
		System.out.println("Thread point 4");
		// check thread 2 should not finished
		assertEquals("Thread already finished => synchronization did not work",0,statusList.size());
		try {
			// to see all registered code-points: comment-in next 2 lines
			// List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
			// System.out.println("codepointList=" + codepointList);
			System.out.println("testConcurrentFindOrCreatePublisher start waiting for breakpoint reached");
			TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
			assertTrue("Did not reach breakpoint", threads.length > 0);
			System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
			codepointRef.disableBreakpoint(true);
			System.out.println("testConcurrentFindOrCreatePublisher breakpoint reached => continue");
		} catch (BreakpointStateException e) {
			e.printStackTrace();
			fail("Codepoints: BreakpointStateException=" + e.getMessage());
		} catch (CommunicationException e) {
			e.printStackTrace();
			fail("Codepoints: CommunicationException=" + e.getMessage());
		}
	
		// sleep until t1 and t2 should have terminated/excepted
		int loopCount = 0;
		while ( (statusList.size()<2) && exceptionHolder.isEmpty() && (loopCount<5)) {
			sleep(1000);
			loopCount++;
		}
		assertTrue("Threads did not finish in 5sec", loopCount<5);
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: "+exception.getMessage(), exception);
		}
		assertEquals("It throws an exception in test", 0, exceptionHolder.size());	

		assertEquals("Thread(s) did not finish",2, statusList.size());
		assertTrue("Subscriber does not exists for identity=" + identity1,  NotificationsManager.getInstance().isSubscribed(identity1, sc));
		assertTrue("Subscriber does not exists for identity=" + identity2, NotificationsManager.getInstance().isSubscribed(identity2, sc));
		codepointClient.close();
		System.out.println("testConcurrentFindOrCreatePublisher finish successful");
		
		JMSCodePointServerJunitHelper.stopServer();
	}
	
	/**
	 * 
	 * @param milis the duration in miliseconds to sleep
	 */
	private void sleep(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
