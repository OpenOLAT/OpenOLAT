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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jcodec.common.Assert;
import org.junit.Test;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;


public class ICalFileCalendarManagerTest extends OlatTestCase {

	private static final OLog log = Tracing.createLoggerFor(ICalFileCalendarManagerTest.class);
	
	@Test
	public void testAddChangeRemoveEvent() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-1-");	

		String TEST_EVENT_ID = "id-testAddEvent";
		CalendarManager manager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		Kalendar cal = manager.getPersonalCalendar(test).getKalendar();
		// 1. Test Add Event
		KalendarEvent testEvent = new KalendarEvent(TEST_EVENT_ID, "testEvent", new Date(), 1);
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
	 * Check a NPE
	 * @throws IOException
	 */
	@Test
	public void testPersistCalendarWithoutDTEndEvent() throws IOException {
		//replace the standard calendar with a forged one
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-test-1-");
		CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
		File calendarFile = calManager.getCalendarFile("user", identity.getName());
		if(calendarFile.exists()) {
			calendarFile.delete();
		}
		File newCalendarFile = new File(calendarFile.getParentFile(), calendarFile.getName());
		InputStream in = CalendarImportTest.class.getResourceAsStream("cal_without_dtend.ics");
		FileUtils.copyInputStreamToFile(in, newCalendarFile);
		
		//load the calendar
		KalendarRenderWrapper reloadCalWrapper = calManager.getPersonalCalendar(identity);
		//check if its the right calendar
		Collection<KalendarEvent> events = reloadCalWrapper.getKalendar().getEvents();
		Assert.assertNotNull(events);
		Assert.assertEquals(1, events.size());
		
		//persist
		calManager.persistCalendar(reloadCalWrapper.getKalendar());
	}
	
	/**
	 * Test concurrent add event with two threads and code-point to control concurrency.
	 *
	 */
	@Test
	public void testConcurrentAddEvent() {
		final String TEST_EVENT_ID_1 = "id-testConcurrentAddEvent-1";
		final String TEST_EVENT_SUBJECT_1 = "testEvent1";
		final String TEST_EVENT_ID_2 = "id-testConcurrentAddEvent-2";
		final String TEST_EVENT_SUBJECT_2 = "testEvent2";
		
		final Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-2-");	
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<Boolean>(1));

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar cal = calManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					log.info("testConcurrentAddEvent thread1 addEvent1");
					calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_1,TEST_EVENT_SUBJECT_1, new Date(), 1));
					log.info("testConcurrentAddEvent thread1 addEvent1 DONE");
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
					log.info("testConcurrentAddEvent thread1 finished");
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
					
					// 3. add Event2 (breakpoint of thread1 blocks)
					log.info("testConcurrentAddEvent thread2 addEvent2");
					calManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2, new Date(), 1));
					log.info("testConcurrentAddEvent thread1 addEvent2 DONE");
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
					log.info("testConcurrentAddEvent thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
			
		thread1.start();
		thread2.start();

		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.info("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	
		log.info("testConcurrentAddEvent finish successful");
	}
	
	/**
	 * Test concurrent add/update event with two threads and code-point to control concurrency.
	 *
	 */
	@Test
	public void testConcurrentAddUpdateEvent() {
		final String TEST_EVENT_ID_1 = "id-testConcurrentAddUpdateEvent-1";
		final String TEST_EVENT_SUBJECT_1 = "testEvent1";
		final String TEST_EVENT_ID_2 = "id-testConcurrentAddUpdateEvent-2";
		final String TEST_EVENT_SUBJECT_2 = "testEvent2";
		final String TEST_EVENT_SUBJECT_2_UPDATED = "testUpdatedEvent2";
		
		final Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-3-");	
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
		log.info("testConcurrentAddUpdateEvent thread2 addEvent2 DONE");

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calendarManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					log.info("testConcurrentAddUpdateEvent thread1 addEvent1");
					calendarManager.addEventTo(currentCalendar, new KalendarEvent(TEST_EVENT_ID_1,TEST_EVENT_SUBJECT_1, new Date(), 1));
					log.info("testConcurrentAddUpdateEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = currentCalendar.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = currentCalendar.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					log.info("testConcurrentAddUpdateEvent thread1 finished");
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
					CalendarManager calendarManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 3. add Event2 (breakpoint of thread1 blocks)
					log.info("testConcurrentAddUpdateEvent thread2 updateEvent2");
					calendarManager.updateEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
					log.info("testConcurrentAddUpdateEvent thread1 updateEvent2 DONE");
					// 4. check event2 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = calendar.getEvent(TEST_EVENT_ID_2);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, updatedEvent);
					assertEquals("Wrong calendar-event subject",updatedEvent.getSubject(), TEST_EVENT_SUBJECT_2_UPDATED);
					// 5. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// Delete Event
					boolean removed = calendarManager.removeEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
					assertTrue(removed);
					statusList.add(Boolean.TRUE);
					log.info("testConcurrentAddUpdateEvent thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};
			
		thread1.start();
		thread2.start();
	
		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.info("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	

		log.info("testConcurrentAddUpdateEvent finish successful");
	}
	
	/**
	 * Test concurrent add/delete event with two threads and code-point to control concurrency.
	 *
	 */
	@Test
	public void testConcurrentAddRemoveEvent() {
		final String TEST_EVENT_ID_1 = "id-testConcurrentAddRemoveEvent-1";
		final String TEST_EVENT_SUBJECT_1 = "testEvent1";
		final String TEST_EVENT_ID_2 = "id-testConcurrentAddRemoveEvent-2";
		final String TEST_EVENT_SUBJECT_2 = "testEvent2";
		
		final Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-1-");
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
		log.info("testConcurrentAddRemoveEvent thread2 addEvent2 DONE");

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			public void run() {
				try {
					// 1. load calendar
					CalendarManager calendarManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					log.info("testConcurrentAddRemoveEvent thread1 addEvent1");
					calendarManager.addEventTo(calendar, new KalendarEvent(TEST_EVENT_ID_1,TEST_EVENT_SUBJECT_1, new Date(), 1));
					log.info("testConcurrentAddRemoveEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = calendar.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					log.info("testConcurrentAddRemoveEvent thread1 finished");
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
					CalendarManager calendarManager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
					Kalendar calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 3. add Event2 (breakpoint of thread1 blocks)
					log.info("testConcurrentAddRemoveEvent thread2 removeEvent2");
					boolean removed = calendarManager.removeEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2,TEST_EVENT_SUBJECT_2, new Date(), 1));
					assertTrue(removed);
					log.info("testConcurrentAddRemoveEvent thread1 removeEvent2 DONE");
					// 4. check event2 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = calendar.getEvent(TEST_EVENT_ID_2);
					assertNull("Still found deleted event with id=" + TEST_EVENT_ID_2, updatedEvent);
					// 5. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					statusList.add(Boolean.TRUE);
					log.info("testConcurrentAddRemoveEvent thread2 finished");
				} catch (Exception ex) {
					exceptionHolder.add(ex);// no exception should happen
				} finally {
					doneSignal.countDown();
					DBFactory.getInstance().commitAndCloseSession();
				}
			}};

		thread1.start();
		thread2.start();
		
		try {
			boolean interrupt = doneSignal.await(10, TimeUnit.SECONDS);
			assertTrue("Test takes too long (more than 10s)", interrupt);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}

		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.info("exception: "+exception.getMessage());
			exception.printStackTrace();
		}

		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	
		log.info("testConcurrentAddRemoveEvent finish successful");
	}
}