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

package org.olat.commons.calendar.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.olat.commons.calendar.CalendarImportTest;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.validate.ValidationException;


public class ICalFileCalendarManagerTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(ICalFileCalendarManagerTest.class);
	
	@Autowired
	private ICalFileCalendarManager calendarManager;
	@Autowired
	private ImportCalendarManager importCalendarManager;
	
	private final void emptyCalendarCache() {
		CoordinatorManager coordinator = CoreSpringFactory.getImpl(CoordinatorManager.class);
		Cacher cacher = coordinator.getCoordinator().getCacher();
		cacher.getCacheContainer().getCache("CalendarManager-calendar").clear();
	}
	
	@Test
	public void testAddChangeRemoveEvent() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-1-");	

		String TEST_EVENT_ID = "id-testAddEvent";
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		// 1. Test Add Event
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 13);
		calendar.set(Calendar.MINUTE, 12);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();

		KalendarEvent testEvent = new KalendarEvent(TEST_EVENT_ID, "testEvent", start, 60 * 60 * 1000);// 1 hour
		calendarManager.addEventTo(cal, testEvent);
		
		// set manager null to force reload of calendar from file-system
		emptyCalendarCache();
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent reloadedEvent = cal.getEvent(TEST_EVENT_ID, null);
		Assert.assertNotNull("Could not found added event", reloadedEvent);
		Assert.assertEquals("Added event has wrong subject", testEvent.getSubject(), reloadedEvent.getSubject());
		Assert.assertEquals(start, reloadedEvent.getBegin());
		//calculate and check end date
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date end = calendar.getTime();
		Assert.assertEquals(end, reloadedEvent.getEnd());
		Assert.assertFalse(reloadedEvent.isAllDayEvent());
		Assert.assertTrue(reloadedEvent.isToday());
		
		// 2. Test Change event
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date updatedEnd = calendar.getTime();
		reloadedEvent.setSubject("testEvent changed");
		reloadedEvent.setEnd(updatedEnd);
		calendarManager.updateEventFrom(cal, reloadedEvent);
		
		// set manager null to force reload of calendar from file-system
		emptyCalendarCache();
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent updatedEvent = cal.getEvent(TEST_EVENT_ID, null);
		Assert.assertNotNull("Could not found updated event", updatedEvent);
		Assert.assertEquals("Added event has wrong subject", reloadedEvent.getSubject(), updatedEvent.getSubject());
		Assert.assertEquals(start, reloadedEvent.getBegin());
		Assert.assertEquals(updatedEnd, reloadedEvent.getEnd());
		
		// 3. Test Remove event
		calendarManager.removeEventFrom(cal, updatedEvent);
		emptyCalendarCache();
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent removedEvent = cal.getEvent(TEST_EVENT_ID, null);
		assertNull("Found removed event", removedEvent);
	}
	
	@Test
	public void testAddChangeEvent_v2() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-1-");	

		String TEST_EVENT_ID = "id-testAddEvent";
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Add Event
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date end = calendar.getTime();
		KalendarEvent testEvent = new KalendarEvent(TEST_EVENT_ID, null, "testEvent", start, end);
		calendarManager.addEventTo(cal, testEvent);
		
		//empty the cache
		emptyCalendarCache();
		
		Kalendar reloadedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent reloadedEvent = reloadedCal.getEvent(TEST_EVENT_ID, null);
		Assert.assertNotNull("Could not found added event", reloadedEvent);
		Assert.assertEquals("Added event has wrong subject", testEvent.getSubject(), reloadedEvent.getSubject());
		Assert.assertEquals(reloadedEvent.getBegin(), start);
		Assert.assertEquals(reloadedEvent.getEnd(), end);
		
		// 2. Test Change event
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date updatedEnd = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, -4);
		Date updatedStart = calendar.getTime();
		reloadedEvent.setSubject("testEvent changed");
		reloadedEvent.setBegin(updatedStart);
		reloadedEvent.setEnd(updatedEnd);
		calendarManager.updateEventFrom(cal, reloadedEvent);
		
		//empty the cache
		emptyCalendarCache();

		Kalendar updatedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent updatedEvent = updatedCal.getEvent(TEST_EVENT_ID, null);
		Assert.assertNotNull("Could not found updated event", updatedEvent);
		Assert.assertEquals("Added event has wrong subject", "testEvent changed", updatedEvent.getSubject());
		Assert.assertEquals(updatedStart, updatedEvent.getBegin());
		Assert.assertEquals(updatedEnd, updatedEvent.getEnd());
	}
	
	/**
	 * Check a NPE
	 * @throws IOException
	 */
	@Test
	public void testTodayEvent() throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-3-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Today Event
		String eventId = "today-" + UUID.randomUUID();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		Date start = calendar.getTime();
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		Date end = calendar.getTime();
		KalendarEvent testEvent = new KalendarEvent(eventId, null, "Today Event", start, end);
		calendarManager.addEventTo(cal, testEvent);
		
		//Next days event
		String nextEventId = "next-" + UUID.randomUUID();
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.add(Calendar.DATE, 3);
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		Date nextStart = calendar.getTime();
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		Date nextEnd = calendar.getTime();
		KalendarEvent nextEvent = new KalendarEvent(nextEventId, null, "Next Event", nextStart, nextEnd);
		calendarManager.addEventTo(cal, nextEvent);
		

		//2. reload and test
		emptyCalendarCache();
		KalendarEvent reloadedEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(eventId, null);
		Assert.assertNotNull(reloadedEvent);
		Assert.assertEquals("Today Event", reloadedEvent.getSubject());
		Assert.assertEquals(start, reloadedEvent.getBegin());
		Assert.assertEquals(end, reloadedEvent.getEnd());
		Assert.assertTrue(reloadedEvent.isToday());
		Assert.assertTrue(reloadedEvent.isWithinOneDay());
		Assert.assertFalse(reloadedEvent.isAllDayEvent());
		
		KalendarEvent reloadedNextEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(nextEventId, null);
		Assert.assertNotNull(reloadedNextEvent);
		Assert.assertEquals("Next Event", reloadedNextEvent.getSubject());
		Assert.assertEquals(nextStart, reloadedNextEvent.getBegin());
		Assert.assertEquals(nextEnd, reloadedNextEvent.getEnd());
		Assert.assertFalse(reloadedNextEvent.isToday());
		Assert.assertTrue(reloadedNextEvent.isWithinOneDay());
		Assert.assertFalse(reloadedNextEvent.isAllDayEvent());
	}
	
	@Test
	public void testWithinOneDay() throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-4-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Today Event
		String eventId = "short-" + UUID.randomUUID();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 14);
		Date start = calendar.getTime();
		calendar.set(Calendar.HOUR_OF_DAY, 15);
		Date end = calendar.getTime();
		KalendarEvent testEvent = new KalendarEvent(eventId, null, "Short Event", start, end);
		calendarManager.addEventTo(cal, testEvent);
		
		//Next days event
		String nextEventId = "long-" + UUID.randomUUID();
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.add(Calendar.DATE, 3);
		calendar.set(Calendar.HOUR, 8);
		Date nextStart = calendar.getTime();
		
		calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.add(Calendar.DATE, 6);
		calendar.set(Calendar.HOUR_OF_DAY, 18);
		Date nextEnd = calendar.getTime();
		KalendarEvent nextEvent = new KalendarEvent(nextEventId, null, "Long Event", nextStart, nextEnd);
		calendarManager.addEventTo(cal, nextEvent);
		

		//2. reload and test
		emptyCalendarCache();
		KalendarEvent reloadedEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(eventId, null);
		Assert.assertNotNull(reloadedEvent);
		Assert.assertEquals("Short Event", reloadedEvent.getSubject());
		Assert.assertEquals(start, reloadedEvent.getBegin());
		Assert.assertEquals(end, reloadedEvent.getEnd());
		Assert.assertTrue(reloadedEvent.isToday());
		Assert.assertTrue(reloadedEvent.isWithinOneDay());
		Assert.assertFalse(reloadedEvent.isAllDayEvent());
		
		KalendarEvent reloadedNextEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(nextEventId, null);
		Assert.assertNotNull(reloadedNextEvent);
		Assert.assertEquals("Long Event", reloadedNextEvent.getSubject());
		Assert.assertEquals(nextStart, reloadedNextEvent.getBegin());
		Assert.assertEquals(nextEnd, reloadedNextEvent.getEnd());
		Assert.assertFalse(reloadedNextEvent.isToday());
		Assert.assertFalse(reloadedNextEvent.isWithinOneDay());
		Assert.assertFalse(reloadedNextEvent.isAllDayEvent());
	}

	/**
	 * Check a NPE
	 * @throws IOException
	 */
	@Test
	public void testPersistCalendarWithoutDTEndEvent() throws IOException {
		//replace the standard calendar with a forged one
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-test-1-");
		File calendarFile = calendarManager.getCalendarFile("user", identity.getName());
		if(calendarFile.exists()) {
			calendarFile.delete();
		}
		File newCalendarFile = new File(calendarFile.getParentFile(), calendarFile.getName());
		InputStream in = CalendarImportTest.class.getResourceAsStream("cal_without_dtend.ics");
		FileUtils.copyInputStreamToFile(in, newCalendarFile);
		in.close();
		
		//to be sure
		emptyCalendarCache();
		//load the calendar
		KalendarRenderWrapper reloadCalWrapper = calendarManager.getPersonalCalendar(identity);
		//check if its the right calendar
		Collection<KalendarEvent> events = reloadCalWrapper.getKalendar().getEvents();
		Assert.assertNotNull(events);
		Assert.assertEquals(1, events.size());
		KalendarEvent event = events.iterator().next();
		Assert.assertEquals("Arbeitszeit: 1-3h", event.getSubject());
		Assert.assertNull(event.getEnd());
		Assert.assertFalse(event.isToday());
		Assert.assertTrue(event.isWithinOneDay());
		Assert.assertFalse(event.isAllDayEvent());

		//test persist
		boolean allOk = calendarManager.persistCalendar(reloadCalWrapper.getKalendar());
		Assert.assertTrue(allOk);
	}
	
	@Test
	public void testListEventsForPeriod() {
		final int numEvents = 10000;
		final int maxEventDuratio = 1000 * 60 * 60 * 24 * 14; // maximum of 14 days duration
		final int oneYearSec = 60 * 60 * 24 * 365;
		final int goBackNumYears = 1;
		final long kalendarStart = new Date().getTime() - (((long)goBackNumYears * oneYearSec) * 1000);
		
		Identity test = JunitTestHelper.createAndPersistIdentityAsUser("test");
		Kalendar kalendar = new Kalendar("test", CalendarManager.TYPE_USER);

		log.info("*** Starting test with the following configuration:");
		log.info("*** Number of events: " + numEvents);
		log.info("*** Maximum event duration (ms): " + maxEventDuratio);
		log.info("*** Generate events in between "
				+ new Date(kalendarStart) + " and "
				+ new Date(kalendarStart + (1000 * ((long)goBackNumYears * oneYearSec))));

		Random rand = new Random();
		long startUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		for (int i = 0; i < numEvents; i++) {
			long begin = kalendarStart + (1000 * ((long)rand.nextInt(goBackNumYears * oneYearSec)));
			String eventId = "id" + i;
			KalendarEvent event = new KalendarEvent(eventId, "test" + i, new Date(begin), rand.nextInt(maxEventDuratio));
			kalendar.addEvent(event);
		}
		long stopUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		log.info("*** SETUP: Kalendar structure uses approx. " + (stopUsed - startUsed) / 1024 + " kb memory.");
		calendarManager.persistCalendar(kalendar);
		
		
		log.info("*** Load calendar...");
		long start = System.currentTimeMillis();
		calendarManager.getPersonalCalendar(test).getKalendar();
		long stop = System.currentTimeMillis();
		log.info("Duration load: " + (stop - start) + " ms.");
		
		log.info("*** Find events within period...");
		start = System.currentTimeMillis();
		List<KalendarEvent> events = calendarManager.getEvents(kalendar,
				new Date(kalendarStart), new Date(kalendarStart + (1000 * ((long)(goBackNumYears * oneYearSec)))), true);
		stop = System.currentTimeMillis();
		log.info("Duration find: " + (stop - start) + " ms.");
		log.info("Found " + events.size() + " events out of " + kalendar.getEvents().size() + " total events.");
		assertEquals(kalendar.getEvents().size(), events.size());
		
		log.info("*** Save calendar...");
		start = System.currentTimeMillis();
		calendarManager.persistCalendar(kalendar);
		stop = System.currentTimeMillis();
		log.info("Duration save: " + (stop - start) + " ms.");
	}
	
	/**
	 * A recurring event with the start and end date reversed. This error
	 * in the calendar cause the whole calendar to crash.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Test
	public void testCalendarRecurringEventInversed() throws URISyntaxException, IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur1-");
		URL calendarUrl = CalendarImportTest.class.getResource("ReversedRecurringEvent.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		Calendar cal = Calendar.getInstance();
		cal.set(2018, 03, 10, 10, 00);
		Date startDate = cal.getTime();
		cal.set(2018, 03, 17);
		Date endDate = cal.getTime();
		
		List<KalendarEvent> recurringEvents = calendarManager.getEvents(importedCalendar.getKalendar(), startDate, endDate, true);
		Assert.assertEquals(0, recurringEvents.size());
	}
	
	/**
	 * A recurring event with missing end date. This error
	 * in the calendar cause the whole calendar to crash.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Test
	public void testCalendarRecurringEventMissingEndDate() throws URISyntaxException, IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur1-");
		URL calendarUrl = CalendarImportTest.class.getResource("RecurringEventMissingEnd.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		Calendar cal = Calendar.getInstance();
		cal.set(2018, 03, 10, 10, 00);
		Date startDate = cal.getTime();
		cal.set(2018, 03, 17);
		Date endDate = cal.getTime();
		
		List<KalendarEvent> recurringEvents = calendarManager.getEvents(importedCalendar.getKalendar(), startDate, endDate, true);
		Assert.assertEquals(0, recurringEvents.size());
	}
	
	/**
	 * Check a NPE
	 * @throws IOException
	 */
	@Test
	public void testListEventsForPeriodWithoutDTEndEvent() throws IOException {
		//replace the standard calendar with a forged one
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-test-1-");
		File calendarFile = calendarManager.getCalendarFile("user", identity.getName());
		if(calendarFile.exists()) {
			calendarFile.delete();
		}
		File newCalendarFile = new File(calendarFile.getParentFile(), calendarFile.getName());
		InputStream in = CalendarImportTest.class.getResourceAsStream("cal_without_dtend.ics");
		FileUtils.copyInputStreamToFile(in, newCalendarFile);
		in.close();
		
		//to be sure
		emptyCalendarCache();
		//load the calendar
		KalendarRenderWrapper reloadCalWrapper = calendarManager.getPersonalCalendar(identity);
		//check if its the right calendar
		Collection<KalendarEvent> events = reloadCalWrapper.getKalendar().getEvents();
		Assert.assertNotNull(events);
		Assert.assertEquals(1, events.size());
		KalendarEvent event = events.iterator().next();
		Assert.assertEquals("Arbeitszeit: 1-3h", event.getSubject());
		Assert.assertEquals("e73iiu9masoddi4g0vllmi2ht0@google.com", event.getID());
		Assert.assertNull(event.getEnd());

		//test persist
		boolean allOk = calendarManager.persistCalendar(reloadCalWrapper.getKalendar());
		Assert.assertTrue(allOk);
		
		//an other possible RS
		//within period
		Date periodStart = CalendarUtils.getDate(2010, Calendar.MARCH, 15);
		Date periodEnd = CalendarUtils.getDate(2010, Calendar.MARCH, 20);
		
		List<KalendarEvent> eventsForPeriod = calendarManager
				.getEvents(reloadCalWrapper.getKalendar(), periodStart, periodEnd, true);
		Assert.assertNotNull(eventsForPeriod);
		Assert.assertEquals(0, eventsForPeriod.size());
		KalendarEvent eventForPeriod = events.iterator().next();
		Assert.assertEquals("e73iiu9masoddi4g0vllmi2ht0@google.com", eventForPeriod.getID());
		
		//out of scope
		Date periodStart2 = CalendarUtils.getDate(2008, Calendar.APRIL, 15);
		Date periodEnd2 = CalendarUtils.getDate(2008, Calendar.APRIL, 17);
		List<KalendarEvent> eventsOutOfPeriod = calendarManager
				.getEvents(reloadCalWrapper.getKalendar(), periodStart2, periodEnd2, true);
		Assert.assertNotNull(eventsOutOfPeriod);
		Assert.assertTrue(eventsOutOfPeriod.isEmpty());
	}
	
	@Test
	public void synchronizeCalendarFrom() throws ValidationException, IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("s1-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();

		String eventId1 = "id-not-managed-event";
		
		// 1. Add a standard event, not managed
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date end = calendar.getTime();
		KalendarEvent notManagedEvent = new KalendarEvent(eventId1, null, "testEvent", start, end);
		calendarManager.addEventTo(cal, notManagedEvent);
		
		// 2. Synchronize with a first calendar
		Kalendar baseCalendar = calendarManager.createCalendar("user", "first-sync");
		String eventIdManaged1 = "managed-event-1";
		KalendarEvent managedEvent1 = new KalendarEvent(eventIdManaged1, null, "managedEvent", start, end);
		baseCalendar.addEvent(managedEvent1);
		String eventIdManaged2 = "managed-event-2";
		KalendarEvent managedEvent2 = new KalendarEvent(eventIdManaged2, null, "managedEvent", start, end);
		baseCalendar.addEvent(managedEvent2);
		
		ByteArrayOutputStream os1 = new ByteArrayOutputStream();
		new CalendarOutputter(false).output(calendarManager.buildCalendar(baseCalendar), os1);
		
		InputStream in1 = new ByteArrayInputStream(os1.toByteArray());
		calendarManager.synchronizeCalendarFrom(in1, "http://localhost:8080/unittest", cal);
		in1.close();
		
		// 3. Synchronize with a second calendar
		Kalendar resyncCalendar = calendarManager.createCalendar("user", "first-sync");
		KalendarEvent managedEvent1Alt = new KalendarEvent(eventIdManaged1, null, "managedEvent resync", start, end);
		resyncCalendar.addEvent(managedEvent1Alt);
		String eventIdManaged3 = "managed-event-3";
		KalendarEvent managedEvent3 = new KalendarEvent(eventIdManaged3, null, "managedEvent 3", start, end);
		resyncCalendar.addEvent(managedEvent3);
		
		ByteArrayOutputStream os2 = new ByteArrayOutputStream();
		new CalendarOutputter(false).output(calendarManager.buildCalendar(resyncCalendar), os2);
		
		InputStream in2 = new ByteArrayInputStream(os2.toByteArray());
		calendarManager.synchronizeCalendarFrom(in2, "http://localhost:8080/unittest", cal);
		in2.close();
		
		emptyCalendarCache();
		//check
		Kalendar synchedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		KalendarEvent notManagedEvent1 = synchedCal.getEvent(eventId1, null);
		Assert.assertNotNull(notManagedEvent1);
		Assert.assertEquals("testEvent", notManagedEvent1.getSubject());
		
		KalendarEvent event1 = synchedCal.getEvent(eventIdManaged1, null);
		Assert.assertNotNull(event1);
		Assert.assertEquals("managedEvent resync", event1.getSubject());

		KalendarEvent event2 = synchedCal.getEvent(eventIdManaged2, null);
		Assert.assertNull(event2);

		KalendarEvent event3 = synchedCal.getEvent(eventIdManaged3, null);
		Assert.assertNotNull(event3);
		Assert.assertEquals("managedEvent 3", event3.getSubject());
	}
	
	@Test
	public void updateCalendar() throws ValidationException, IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("u1-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();

		String eventIdMarker = "id-marker-event";
		
		// 1. Add a standard event, not managed
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		Date start = calendar.getTime();
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		Date end = calendar.getTime();
		KalendarEvent markerEvent = new KalendarEvent(eventIdMarker, null, "markerEvent", start, end);
		calendarManager.addEventTo(cal, markerEvent);
		
		// 2. Update with a first calendar
		Kalendar baseCalendar = calendarManager.createCalendar("user", "first-update");
		String eventId1 = "event-1";
		KalendarEvent event1 = new KalendarEvent(eventId1, null, "event 1", start, end);
		baseCalendar.addEvent(event1);
		String eventId2 = "event-2";
		KalendarEvent event2 = new KalendarEvent(eventId2, null, "event 2", start, end);
		baseCalendar.addEvent(event2);
		calendarManager.updateCalendar(cal, baseCalendar);
		
		// 3. Update with a second calendar
		Kalendar updateCalendar = calendarManager.createCalendar("user", "first-update");
		KalendarEvent event1alt = new KalendarEvent(eventId1, null, "event 1 alt", start, end);
		updateCalendar.addEvent(event1alt);
		String eventId3 = "event-3";
		KalendarEvent event3 = new KalendarEvent(eventId3, null, "event 3", start, end);
		updateCalendar.addEvent(event3);
		calendarManager.updateCalendar(cal, updateCalendar);
		
		//Check
		emptyCalendarCache();

		Kalendar reloadedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent marker = reloadedCal.getEvent(eventIdMarker, null);
		Assert.assertNotNull(marker);
		Assert.assertEquals("markerEvent", marker.getSubject());
		
		KalendarEvent reloaded1 = reloadedCal.getEvent(eventId1, null);
		Assert.assertNotNull(reloaded1);
		Assert.assertEquals("event 1 alt", reloaded1.getSubject());
		
		KalendarEvent reloaded2 = reloadedCal.getEvent(eventId2, null);
		Assert.assertNotNull(reloaded2);
		Assert.assertEquals("event 2", reloaded2.getSubject());

		KalendarEvent reloaded3 = reloadedCal.getEvent(eventId3, null);
		Assert.assertNotNull(reloaded3);
		Assert.assertEquals("event 3", reloaded3.getSubject());
	}
	
	@Test
	public void testImportICal_recurringEvent() throws URISyntaxException, IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur1-");
		URL calendarUrl = CalendarImportTest.class.getResource("RecurringEvent.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(2, events.size());
	}
	
	@Test
	public void testImportICal_outlookFullDay() throws URISyntaxException, IOException {
		TimeZone vmTimeZone = TimeZone.getDefault();
		TimeZone ooTimeZone = TimeZone.getTimeZone("Europe/Zurich");
		Assume.assumeTrue(vmTimeZone.getRawOffset() == ooTimeZone.getRawOffset());

		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur2-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("Fullday_outlook.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		KalendarEvent event = events.get(0);
		Assert.assertTrue(event.isAllDayEvent());
	}
	
	@Test
	public void testImportICal_icalFullDay() throws URISyntaxException, IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur3-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("Fullday_ical.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(3, events.size());
		
		// 24 hours but on 2 days
		KalendarEvent on2days = importedCalendar.getKalendar().getEvent("EFE10508-15B0-4FCE-A258-37BA642B760D", null);
		Assert.assertFalse(on2days.isAllDayEvent());
		// real all day with the iCal standard
		KalendarEvent allDay = importedCalendar.getKalendar().getEvent("14C0ACCD-AC0B-4B10-A448-0BF129492091", null);
		Assert.assertTrue(allDay.isAllDayEvent());
		// almost a full day bit it miss one minute
		KalendarEvent longDay = importedCalendar.getKalendar().getEvent("C562E736-DCFF-4002-9E5B-77D891D4A322", null);
		Assert.assertFalse(longDay.isAllDayEvent());
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
			@Override
			public void run() {
				try {
					// 1. load calendar
					Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					log.info("testConcurrentAddEvent thread1 addEvent1");
					calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, new Date(), 1));
					log.info("testConcurrentAddEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1, null);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = cal.getEvent(TEST_EVENT_ID_1, null);
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
			@Override
			public void run() {
				try {
					// 1. load calendar
					Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 3. add Event2 (breakpoint of thread1 blocks)
					log.info("testConcurrentAddEvent thread2 addEvent2");
					calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
					log.info("testConcurrentAddEvent thread1 addEvent2 DONE");
					// 4. check event2 exist
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2, null);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
					assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
					// 5. check event1 exist
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1, null);
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
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2, null);
		assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
		assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
		log.info("testConcurrentAddUpdateEvent thread2 addEvent2 DONE");

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			@Override
			public void run() {
				try {
					// 1. load calendar
					Kalendar currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					log.info("testConcurrentAddUpdateEvent thread1 addEvent1");
					calendarManager.addEventTo(currentCalendar, new KalendarEvent(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, new Date(), 1));
					log.info("testConcurrentAddUpdateEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = currentCalendar.getEvent(TEST_EVENT_ID_1, null);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = currentCalendar.getEvent(TEST_EVENT_ID_1, null);
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
			@Override
			public void run() {
				try {
					Kalendar calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 3. add Event2 (breakpoint of thread1 blocks)
					log.info("testConcurrentAddUpdateEvent thread2 updateEvent2");
					calendarManager.updateEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
					log.info("testConcurrentAddUpdateEvent thread1 updateEvent2 DONE");
					// 4. check event2 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = calendar.getEvent(TEST_EVENT_ID_2, null);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, updatedEvent);
					assertEquals("Wrong calendar-event subject",updatedEvent.getSubject(), TEST_EVENT_SUBJECT_2_UPDATED);
					// 5. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1, null);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// Delete Event
					boolean removed = calendarManager.removeEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2_UPDATED, new Date(), 1));
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
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2, null);
		assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
		assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
		log.info("testConcurrentAddRemoveEvent thread2 addEvent2 DONE");

		final CountDownLatch doneSignal = new CountDownLatch(2);

		// thread 1
		Thread thread1 = new Thread() {
			@Override
			public void run() {
				try {
					// 1. load calendar
					Kalendar calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 2. add Event1 => breakpoint hit					
					log.info("testConcurrentAddRemoveEvent thread1 addEvent1");
					calendarManager.addEventTo(calendar, new KalendarEvent(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, new Date(), 1));
					log.info("testConcurrentAddRemoveEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1, null);
					assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = calendar.getEvent(TEST_EVENT_ID_1, null);
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
			@Override
			public void run() {
				try {
					Kalendar calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					
					// 3. add Event2 (breakpoint of thread1 blocks)
					log.info("testConcurrentAddRemoveEvent thread2 removeEvent2");
					boolean removed = calendarManager.removeEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, new Date(), 1));
					assertTrue(removed);
					log.info("testConcurrentAddRemoveEvent thread1 removeEvent2 DONE");
					// 4. check event2 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = calendar.getEvent(TEST_EVENT_ID_2, null);
					assertNull("Still found deleted event with id=" + TEST_EVENT_ID_2, updatedEvent);
					// 5. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1, null);
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