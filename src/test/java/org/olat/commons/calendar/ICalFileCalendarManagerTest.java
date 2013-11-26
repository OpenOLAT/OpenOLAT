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

package org.olat.commons.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JMSCodePointServerJunitHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.testutils.codepoints.client.BreakpointStateException;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.CommunicationException;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;


/**
 * 
 */
public class ICalFileCalendarManagerTest extends OlatTestCase {

	private static OLog log = Tracing.createLoggerFor(ICalFileCalendarManagerTest.class);
	private static String  CODEPOINT_SERVER_ID = "ICalFileCalendarManagerTest";
	private static Identity test = null;


	
	@Test public void testAddChangeRemoveEvent() {
		String TEST_EVENT_ID = "id-testAddEvent";
		CalendarManager manager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		Kalendar cal = manager.getPersonalCalendar(test).getKalendar();
		// 1. Test Add Event
		KalendarEvent testEvent = new KalendarEvent(TEST_EVENT_ID,"testEvent", new Date(), 1);
		manager.addEventTo(cal, testEvent);
		// set manager null to force reload of calendar from file-system
		manager = null;
		manager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		cal = manager.getPersonalCalendar(test).getKalendar();
		KalendarEvent reloadedEvent = cal.getEvent(TEST_EVENT_ID);
		assertNotNull("Could not found added event", reloadedEvent);
		assertEquals("Added event has wrong subject", testEvent.getSubject(),reloadedEvent.getSubject());
		// 2. Test Change event
		reloadedEvent.setSubject("testEvent changed");
		manager.updateEventFrom(cal, reloadedEvent);
		// set manager null to force reload of calendar from file-system
		manager = null;
		manager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		cal = manager.getPersonalCalendar(test).getKalendar();
		KalendarEvent updatedEvent = cal.getEvent(TEST_EVENT_ID);
		assertNotNull("Could not found updated event", updatedEvent);
		assertEquals("Added event has wrong subject", reloadedEvent.getSubject(),updatedEvent.getSubject());
		// 3. Test Remove event
		manager.removeEventFrom(cal, updatedEvent);
		manager = null;
		manager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		cal = manager.getPersonalCalendar(test).getKalendar();
		KalendarEvent removedEvent = cal.getEvent(TEST_EVENT_ID);
		assertNull("Found removed event", removedEvent);
	}


	/**
	 * Test concurrent add event with two threads and code-point to control concurrency.
	 *
	 */
	@Test public void testConcurrentAddEvent() {
		final String TEST_EVENT_ID_1 = "id-testConcurrentAddEvent-1";
		final String TEST_EVENT_SUBJECT_1 = "testEvent1";
		final String TEST_EVENT_ID_2 = "id-testConcurrentAddEvent-2";
		final String TEST_EVENT_SUBJECT_2 = "testEvent2";

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		// enable breakpoint

		CodepointClient codepointClient = null;
		CodepointRef codepointRef = null;
		try {
			codepointClient = CodepointClientFactory.createCodepointClient("vm://embedded?broker.persistent=false", CODEPOINT_SERVER_ID);
			codepointRef = codepointClient.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.commons.calendar.ICalFileCalendarManager.addEventTo");
			codepointRef.enableBreakpoint();
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not initialzed CodepointClient");
		}
		

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					System.out.println("testConcurrentAddEvent thread1 addEvent1");
					calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_1,TEST_EVENT_SUBJECT_1, new Date(), 1));
					System.out.println("testConcurrentAddEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					cal = calManager.getPersonalCalendar(test).getKalendar();
					event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentAddEvent thread1 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};

		// thread 2
		Thread thread2 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					// 2. sleep 1sec
					sleep(1000);
					// 3. add Event2 (breakpoint of thread1 blocks)
					System.out.println("testConcurrentAddEvent thread2 addEvent2");
					calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2, new Date(), 1));
					System.out.println("testConcurrentAddEvent thread1 addEvent2 DONE");
					// 4. check event2 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
					assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
					// 5. check event1 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentAddEvent thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
			
		thread1.start();
		thread2.start();

		sleep(2000);
		try {
			// to see all registered code-points: comment-in next 2 lines
			// List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
			// System.out.println("codepointList=" + codepointList);
			System.out.println("testConcurrentAddEvent start waiting for breakpoint reached");
			TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
			assertTrue("Did not reach breakpoint", threads.length > 0);
			System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
			codepointRef.disableBreakpoint(true);
			System.out.println("testConcurrentAddEvent breakpoint reached => continue");
		} catch (BreakpointStateException e) {
			e.printStackTrace();
			fail("Codepoints: BreakpointStateException=" + e.getMessage());
		} catch (CommunicationException e) {
			e.printStackTrace();
			fail("Codepoints: CommunicationException=" + e.getMessage());
		}
		
		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.out.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	
		codepointClient.close();
		System.out.println("testConcurrentAddEvent finish successful");
	}
	
	/**
	 * Test concurrent add/update event with two threads and code-point to control concurrency.
	 *
	 */
	@Test public void testConcurrentAddUpdateEvent() {
		final String TEST_EVENT_ID_1 = "id-testConcurrentAddUpdateEvent-1";
		final String TEST_EVENT_SUBJECT_1 = "testEvent1";
		final String TEST_EVENT_ID_2 = "id-testConcurrentAddUpdateEvent-2";
		final String TEST_EVENT_SUBJECT_2 = "testEvent2";
		final String TEST_EVENT_SUBJECT_2_UPDATED = "testUpdatedEvent2";
		

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		// Generate event for update
		CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
		calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2, new Date(), 1));
		cal = calManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2);
		assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
		assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
		System.out.println("testConcurrentAddUpdateEvent thread2 addEvent2 DONE");

		// enable breakpoint
		CodepointClient codepointClient = null;
		CodepointRef codepointRef = null;
		try {
			codepointClient = CodepointClientFactory.createCodepointClient("vm://embedded?broker.persistent=false", CODEPOINT_SERVER_ID);
			codepointRef = codepointClient.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.commons.calendar.ICalFileCalendarManager.addEventTo");
			codepointRef.enableBreakpoint();
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not initialzed CodepointClient");
		}
		

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					System.out.println("testConcurrentAddUpdateEvent thread1 addEvent1");
					calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_1,TEST_EVENT_SUBJECT_1, new Date(), 1));
					System.out.println("testConcurrentAddUpdateEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					cal = calManager.getPersonalCalendar(test).getKalendar();
					event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentAddUpdateEvent thread1 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
		
		// thread 2
		Thread thread2 = new Thread() {
			public void run() {
				try {
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					// 2. sleep 1sec
					sleep(1000);
					// 3. add Event2 (breakpoint of thread1 blocks)
					System.out.println("testConcurrentAddUpdateEvent thread2 updateEvent2");
					calManager.updateEventFrom(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
					System.out.println("testConcurrentAddUpdateEvent thread1 updateEvent2 DONE");
					// 4. check event2 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = cal.getEvent(TEST_EVENT_ID_2);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, updatedEvent);
					assertEquals("Wrong calendar-event subject",updatedEvent.getSubject(), TEST_EVENT_SUBJECT_2_UPDATED);
					// 5. check event1 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// Delete Event
					boolean removed = calManager.removeEventFrom(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
					assertTrue(removed);
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentAddUpdateEvent thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
			
		thread1.start();
		thread2.start();
		sleep(2000);
		
		try {
			// to see all registered code-points: comment-in next 2 lines
			// List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
			// System.out.println("codepointList=" + codepointList);
			System.out.println("testConcurrentAddUpdateEvent start waiting for breakpoint reached");
			TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
			assertTrue("Did not reach breakpoint", threads.length > 0);
			System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
			codepointRef.disableBreakpoint(true);
			System.out.println("testConcurrentAddUpdateEvent breakpoint reached => continue");
		} catch (BreakpointStateException e) {
			e.printStackTrace();
			fail("Codepoints: BreakpointStateException=" + e.getMessage());
		} catch (CommunicationException e) {
			e.printStackTrace();
			fail("Codepoints: CommunicationException=" + e.getMessage());
		}
	
		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.out.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	

		codepointClient.close();
		System.out.println("testConcurrentAddUpdateEvent finish successful");
	}
	
	/**
	 * Test concurrent add/delete event with two threads and code-point to control concurrency.
	 *
	 */
	@Test public void testConcurrentAddRemoveEvent() {
		final String TEST_EVENT_ID_1 = "id-testConcurrentAddRemoveEvent-1";
		final String TEST_EVENT_SUBJECT_1 = "testEvent1";
		final String TEST_EVENT_ID_2 = "id-testConcurrentAddRemoveEvent-2";
		final String TEST_EVENT_SUBJECT_2 = "testEvent2";
		

		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		// Generate event for update
		CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
		calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2, new Date(), 1));
		cal = calManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2);
		assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
		assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
		System.out.println("testConcurrentAddRemoveEvent thread2 addEvent2 DONE");

		// enable breakpoint
		CodepointClient codepointClient = null;
		CodepointRef codepointRef = null;
		try {
			codepointClient = CodepointClientFactory.createCodepointClient("vm://embedded?broker.persistent=false", CODEPOINT_SERVER_ID);
			codepointRef = codepointClient.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.commons.calendar.ICalFileCalendarManager.addEventTo");
			codepointRef.enableBreakpoint();
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not initialzed CoepointClient");
		}
		

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					System.out.println("testConcurrentAddRemoveEvent thread1 addEvent1");
					calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_1,TEST_EVENT_SUBJECT_1, new Date(), 1));
					System.out.println("testConcurrentAddRemoveEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					cal = calManager.getPersonalCalendar(test).getKalendar();
					event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentAddRemoveEvent thread1 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
		
		// thread 2
		Thread thread2 = new Thread() {
			public void run() {
				try {
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					// 2. sleep 1sec
					sleep(1000);
					// 3. add Event2 (breakpoint of thread1 blocks)
					System.out.println("testConcurrentAddRemoveEvent thread2 removeEvent2");
					boolean removed = calManager.removeEventFrom(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2, new Date(), 1));
					assertTrue(removed);
					System.out.println("testConcurrentAddRemoveEvent thread1 removeEvent2 DONE");
					// 4. check event2 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = cal.getEvent(TEST_EVENT_ID_2);
					assertNull("Still found deleted event with id=" + TEST_EVENT_ID_2, updatedEvent);
					// 5. check event1 exist
					cal = calManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					System.out.println("testConcurrentAddRemoveEvent thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};

		thread1.start();
		thread2.start();
		sleep(2000);
		
		try {
			// to see all registered code-points: comment-in next 2 lines
			// List<CodepointRef> codepointList = codepointClient.listAllCodepoints();
			// System.out.println("codepointList=" + codepointList);
			System.out.println("testConcurrentAddRemoveEvent start waiting for breakpoint reached");
			TemporaryPausedThread[] threads = codepointRef.waitForBreakpointReached(1000);
			assertTrue("Did not reach breakpoint", threads.length > 0);
			System.out.println("threads[0].getCodepointRef()=" + threads[0].getCodepointRef());
			codepointRef.disableBreakpoint(true);
			System.out.println("testConcurrentAddRemoveEvent breakpoint reached => continue");
		} catch (BreakpointStateException e) {
			e.printStackTrace();
			fail("Codepoints: BreakpointStateException=" + e.getMessage());
		} catch (CommunicationException e) {
			e.printStackTrace();
			fail("Codepoints: CommunicationException=" + e.getMessage());
		}
		
		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			System.out.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}

		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	
		codepointClient.close();
		System.out.println("testConcurrentAddRemoveEvent finish successful");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup() throws Exception {
			test = JunitTestHelper.createAndPersistIdentityAsUser("test");	
			// Setup for code-points
			JMSCodePointServerJunitHelper.startServer(CODEPOINT_SERVER_ID);
			DBFactory.getInstance().closeSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() throws Exception {
		try {
			JMSCodePointServerJunitHelper.stopServer();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}

}