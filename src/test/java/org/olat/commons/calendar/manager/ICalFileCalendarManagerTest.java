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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
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
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.olat.commons.calendar.CalendarImportTest;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
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
	public void addChangeRemoveEvent() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-1-");	

		String eventId = "id-testAddEvent";
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		// 1. Test Add Event
		ZonedDateTime start = ZonedDateTime.now()
				.withHour(13)
				.withMinute(12)
				.withSecond(0)
				.with(ChronoField.MILLI_OF_SECOND, 0);

		KalendarEvent testEvent = new KalendarEvent(eventId, "testEvent", start, 60 * 60 * 1000);// 1 hour
		calendarManager.addEventTo(cal, testEvent);
		
		// set manager null to force reload of calendar from file-system
		emptyCalendarCache();
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent reloadedEvent = cal.getEvent(eventId, null);
		Assert.assertNotNull("Could not found added event", reloadedEvent);
		Assert.assertEquals("Added event has wrong subject", testEvent.getSubject(), reloadedEvent.getSubject());
		Assertions.assertThat(reloadedEvent.getBegin()).isEqualTo(start);
		//calculate and check end date
		ZonedDateTime end = start.plusHours(1);
		Assertions.assertThat(reloadedEvent.getEnd()).isEqualTo(end);
		Assert.assertFalse(reloadedEvent.isAllDayEvent());
		Assert.assertTrue(reloadedEvent.isToday());
		
		// 2. Test Change event
		ZonedDateTime updatedEnd = end.plusHours(1);
		reloadedEvent.setSubject("testEvent changed");
		reloadedEvent.setEnd(updatedEnd);
		calendarManager.updateEventFrom(cal, reloadedEvent);
		
		// set manager null to force reload of calendar from file-system
		emptyCalendarCache();
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent updatedEvent = cal.getEvent(eventId, null);
		Assert.assertNotNull("Could not found updated event", updatedEvent);
		Assert.assertEquals("Added event has wrong subject", reloadedEvent.getSubject(), updatedEvent.getSubject());
		Assertions.assertThat(reloadedEvent.getBegin()).isEqualTo(start);
		Assertions.assertThat(reloadedEvent.getEnd()).isEqualTo(updatedEnd);
		
		// 3. Test Remove event
		calendarManager.removeEventFrom(cal, updatedEvent);
		emptyCalendarCache();
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent removedEvent = cal.getEvent(eventId, null);
		Assert.assertNull("Found removed event", removedEvent);
	}
	
	@Test
	public void addChangeEventV2() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-1-");	

		final String eventId = "id-testAddEvent";
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Add Event
		ZonedDateTime start = ZonedDateTime.now()
				.with(ChronoField.MILLI_OF_SECOND, 0l);
		ZonedDateTime end = start.plusHours(1);
		KalendarEvent testEvent = new KalendarEvent(eventId, null, "testEvent", start, end);
		calendarManager.addEventTo(cal, testEvent);
		
		//empty the cache
		emptyCalendarCache();
		
		Kalendar reloadedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent reloadedEvent = reloadedCal.getEvent(eventId, null);
		Assert.assertNotNull("Could not found added event", reloadedEvent);
		Assert.assertEquals("Added event has wrong subject", testEvent.getSubject(), reloadedEvent.getSubject());
		Assertions.assertThat(reloadedEvent.getBegin()).isEqualTo(start);
		Assertions.assertThat(reloadedEvent.getEnd()).isEqualTo(end);
		
		// 2. Test Change event
		ZonedDateTime updatedEnd = end.plusHours(1);
		ZonedDateTime updatedStart = updatedEnd.minusHours(4);
		reloadedEvent.setSubject("testEvent changed");
		reloadedEvent.setBegin(updatedStart);
		reloadedEvent.setEnd(updatedEnd);
		calendarManager.updateEventFrom(cal, reloadedEvent);
		
		//empty the cache
		emptyCalendarCache();

		Kalendar updatedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent updatedEvent = updatedCal.getEvent(eventId, null);
		Assert.assertNotNull("Could not found updated event", updatedEvent);
		Assert.assertEquals("Added event has wrong subject", "testEvent changed", updatedEvent.getSubject());
		Assertions.assertThat(updatedEvent.getBegin()).isEqualTo(updatedStart);
		Assertions.assertThat(updatedEvent.getEnd()).isEqualTo(updatedEnd);
	}
	
	@Test
	public void addFullDayEvent() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-19-");	

		final String eventId = "id-testAddFullDayEvent";
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Add Event
		ZonedDateTime start = ZonedDateTime.of(2025, 9, 16, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime end = ZonedDateTime.of(2025, 9, 19, 0, 0, 0, 0, ZoneId.systemDefault());
		KalendarEvent testEvent = new KalendarEvent(eventId, null, "Full day event", start, end);
		testEvent.setAllDayEvent(true);
		calendarManager.addEventTo(cal, testEvent);
		
		//empty the cache
		emptyCalendarCache();
		
		Kalendar reloadedCal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent reloadedEvent = reloadedCal.getEvent(eventId, null);
		Assert.assertTrue(DateUtils.isSameDay(start, reloadedEvent.getBegin()));
		Assert.assertTrue(DateUtils.isSameDay(end, reloadedEvent.getEnd()));
	}
	
	/**
	 * Check a NPE
	 * @throws IOException
	 */
	@Test
	public void testTodayEvent() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-3-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Today Event
		String eventId = "today-" + UUID.randomUUID();
		ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
				.withHour(8);
		ZonedDateTime end = start
				.withHour(12);
		KalendarEvent testEvent = new KalendarEvent(eventId, null, "Today Event", start, end);
		calendarManager.addEventTo(cal, testEvent);

		//2. reload and test
		emptyCalendarCache();
		KalendarEvent reloadedEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(eventId, null);
		Assert.assertNotNull(reloadedEvent);
		Assert.assertEquals("Today Event", reloadedEvent.getSubject());
		Assertions.assertThat(reloadedEvent.getBegin()).isEqualTo(start);
		Assertions.assertThat(reloadedEvent.getEnd()).isEqualTo(end);
		Assert.assertTrue(reloadedEvent.isToday());
		Assert.assertTrue(reloadedEvent.isWithinOneDay());
		Assert.assertFalse(reloadedEvent.isAllDayEvent());
	}
	
	@Test
	public void testNotTodayEvent() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-3-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();

		//Next days event
		String eventId = "next-" + UUID.randomUUID();
		ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
				.plusDays(2)
				.withHour(8);
		ZonedDateTime end = start.withHour(12);
		
		KalendarEvent nextEvent = new KalendarEvent(eventId, null, "Next Event", start, end);
		calendarManager.addEventTo(cal, nextEvent);
	
		//2. reload and test
		emptyCalendarCache();
		KalendarEvent reloadedNextEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(eventId, null);
		Assert.assertNotNull(reloadedNextEvent);
		Assert.assertEquals("Next Event", reloadedNextEvent.getSubject());
		Assertions.assertThat(reloadedNextEvent.getBegin()).isEqualTo(start);
		Assertions.assertThat(reloadedNextEvent.getEnd()).isEqualTo(end);
		Assert.assertFalse(reloadedNextEvent.isToday());
		Assert.assertTrue(reloadedNextEvent.isWithinOneDay());
		Assert.assertFalse(reloadedNextEvent.isAllDayEvent());
	}
	
	@Test
	public void testWithinOneDay() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-4-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// 1. Test Today Event
		String eventId = "short-" + UUID.randomUUID();
		ZonedDateTime start = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
				.withHour(14);
		ZonedDateTime end = start
				.withHour(15);
		KalendarEvent testEvent = new KalendarEvent(eventId, null, "Short Event", start, end);
		calendarManager.addEventTo(cal, testEvent);
		
		//Next days event
		String nextEventId = "long-" + UUID.randomUUID();
		ZonedDateTime nextStart = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
				.plusDays(3)
				.withHour(14);
		ZonedDateTime nextEnd = nextStart
				.plusDays(6)
				.withHour(18);
		KalendarEvent nextEvent = new KalendarEvent(nextEventId, null, "Long Event", nextStart, nextEnd);
		calendarManager.addEventTo(cal, nextEvent);
		
		//2. reload and test
		emptyCalendarCache();
		KalendarEvent reloadedEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(eventId, null);
		Assert.assertNotNull(reloadedEvent);
		Assert.assertEquals("Short Event", reloadedEvent.getSubject());
		Assertions.assertThat(reloadedEvent.getBegin()).isEqualTo(start);
		Assertions.assertThat(reloadedEvent.getEnd()).isEqualTo(end);
		Assert.assertTrue(reloadedEvent.isToday());
		Assert.assertTrue(reloadedEvent.isWithinOneDay());
		Assert.assertFalse(reloadedEvent.isAllDayEvent());
		
		KalendarEvent reloadedNextEvent = calendarManager.getPersonalCalendar(test).getKalendar().getEvent(nextEventId, null);
		Assert.assertNotNull(reloadedNextEvent);
		Assert.assertEquals("Long Event", reloadedNextEvent.getSubject());
		Assertions.assertThat(reloadedNextEvent.getBegin()).isEqualTo(nextStart);
		Assertions.assertThat(reloadedNextEvent.getEnd()).isEqualTo(nextEnd);
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
		log.info("*** Number of events: {}", numEvents);
		log.info("*** Maximum event duration (ms): {}", maxEventDuratio);
		log.info("*** Generate events in between {} and {}",
				new Date(kalendarStart), new Date(kalendarStart + (1000 * ((long)goBackNumYears * oneYearSec))));

		Random rand = new Random();
		long startUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		for (int i = 0; i < numEvents; i++) {
			long begin = kalendarStart + (1000 * ((long)rand.nextInt(goBackNumYears * oneYearSec)));
			String eventId = "id" + i;
			ZonedDateTime beginDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(begin), ZoneId.systemDefault());
			KalendarEvent event = new KalendarEvent(eventId, "test" + i, beginDate, rand.nextInt(maxEventDuratio));
			kalendar.addEvent(event);
		}
		long stopUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		log.info("*** SETUP: Kalendar structure uses approx. {} kb memory.", (stopUsed - startUsed) / 1024);
		calendarManager.persistCalendar(kalendar);
		
		
		log.info("*** Load calendar...");
		long start = System.currentTimeMillis();
		calendarManager.getPersonalCalendar(test).getKalendar();
		long stop = System.currentTimeMillis();
		log.info("Duration load: {} ms.", (stop - start));
		
		log.info("*** Find events within period...");
		start = System.currentTimeMillis();
		ZonedDateTime zKalendarStart = DateUtils.toZonedDateTime(new Date(kalendarStart));
		ZonedDateTime zKalendarEnd = zKalendarStart.plus((1000 * ((long)(goBackNumYears * oneYearSec))), ChronoUnit.MILLIS);
		List<KalendarEvent> events = calendarManager.getEvents(kalendar, zKalendarStart, zKalendarEnd, true);
		stop = System.currentTimeMillis();
		log.info("Duration find: {} ms.", (stop - start));
		log.info("Found {} events out of {} total events.", events.size(),  kalendar.getEvents().size());
		assertEquals(kalendar.getEvents().size(), events.size());
		
		log.info("*** Save calendar...");
		start = System.currentTimeMillis();
		calendarManager.persistCalendar(kalendar);
		stop = System.currentTimeMillis();
		log.info("Duration save: {} ms.", (stop - start));
	}

	@Test
	public void getRecurrenceEndDate() {
		String rule = "FREQ=WEEKLY;UNTIL=20250228T225959";
		ZonedDateTime dateTime = calendarManager.getRecurrenceEndDate(rule);
		Assert.assertNotNull(dateTime);
		
		ZonedDateTime refDate = ZonedDateTime.of(2025, 02, 28, 22, 59, 59, 0, ZoneId.systemDefault());
		Assertions.assertThat(dateTime).isEqualTo(refDate);
	}
	
	@Test
	public void getWeeklyRecurrenceRule() {
		Calendar cal = Calendar.getInstance();
		cal.set(2025, 1, 28, 0, 0, 0);
		Date recurrenceEnd = cal.getTime();
		String rule = calendarManager.getRecurrenceRule("WEEKLY", recurrenceEnd, false);
		Assert.assertNotNull(rule);
		Assert.assertEquals("FREQ=WEEKLY;UNTIL=20250228T000000Z;COUNT=-1", rule);
	}
	
	@Test
	public void getWeeklyRecurrenceRuleAllDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(2025, 1, 28, 0, 0, 0);
		Date recurrenceEnd = cal.getTime();
		String rule = calendarManager.getRecurrenceRule("WEEKLY", recurrenceEnd, true);
		Assert.assertNotNull(rule);
		Assert.assertEquals("FREQ=WEEKLY;UNTIL=20250228;COUNT=-1", rule);
	}
	
	@Test
	public void removeOccurenceOfEvent() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-1-");
		KalendarRenderWrapper kalWrapper = calendarManager.getPersonalCalendar(id);
		Kalendar kal = kalWrapper.getKalendar();
		
		ZonedDateTime startDate = ZonedDateTime.of(2022, 3, 3, 15, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime endDate = ZonedDateTime.of(2022, 3, 3, 16, 0, 0, 0, ZoneId.systemDefault());
		
		KalendarEvent event = new KalendarEvent("cal-1-1", null, "Recurrence", startDate, endDate);
		String rrule = calendarManager.getRecurrenceRule("WEEKLY", new Date(), false);
		event.setRecurrenceRule(rrule);
		
		boolean added = calendarManager.addEventTo(kal, event);
		Assert.assertTrue(added);
		
		ZonedDateTime windowStartDate = ZonedDateTime.of(2022, 3, 17, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime windowEndDate = ZonedDateTime.of(2022, 3, 17, 23, 59, 0, 0, ZoneId.systemDefault());
		
		List<KalendarEvent> events = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertEquals(1, events.size());
		Assert.assertTrue(events.get(0) instanceof KalendarRecurEvent);
		KalendarRecurEvent recurEvent = (KalendarRecurEvent)events.get(0);
		calendarManager.removeOccurenceOfEvent(kal, recurEvent);
		
		List<KalendarEvent> excEvents = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertTrue(excEvents.isEmpty());
	}
	
	@Test
	public void removeOccurencesOfEventTwice() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-1-");
		KalendarRenderWrapper kalWrapper = calendarManager.getPersonalCalendar(id);
		Kalendar kal = kalWrapper.getKalendar();
		
		ZonedDateTime startDate = ZonedDateTime.of(2025, 9, 29, 15, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime endDate = ZonedDateTime.of(2025, 9, 29, 16, 0, 0, 0, ZoneId.systemDefault());
		
		KalendarEvent event = new KalendarEvent("cal-1-1", null, "Recurrence", startDate, endDate);

		ZonedDateTime recurenceEndDate = ZonedDateTime.of(2025, 11, 4, 0, 0, 0, 0, ZoneId.systemDefault());
		String rrule = calendarManager.getRecurrenceRule("WEEKLY", DateUtils.toDate(recurenceEndDate), false);
		event.setRecurrenceRule(rrule);
		
		boolean added = calendarManager.addEventTo(kal, event);
		Assert.assertTrue(added);
		
		ZonedDateTime windowStartDate = ZonedDateTime.of(2025, 9, 29, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime windowEndDate = ZonedDateTime.of(2025, 11, 5, 0, 0, 0, 0, ZoneId.systemDefault());
		
		List<KalendarEvent> events = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertEquals(6, events.size());
		Assert.assertTrue(events.get(0) instanceof KalendarRecurEvent);
		KalendarRecurEvent recurEventToRemove1 = (KalendarRecurEvent)events.get(2);
		calendarManager.removeOccurenceOfEvent(kal, recurEventToRemove1);
		KalendarRecurEvent recurEventToRemove2 = (KalendarRecurEvent)events.get(3);
		calendarManager.removeOccurenceOfEvent(kal, recurEventToRemove2);

		//Check
		emptyCalendarCache();
		
		List<KalendarEvent> excEvents = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertEquals(4, excEvents.size());
	}

	@Test
	public void calendarWithExDate()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-2-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("ExDate.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		Kalendar kal = importedCalendar.getKalendar();
		ZonedDateTime windowStartDate = ZonedDateTime.of(2025, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime windowEndDate = ZonedDateTime.of(2025, 3, 1, 23, 59, 0, 0, ZoneId.systemDefault());
		List<KalendarEvent> excEvents = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertEquals(3, excEvents.size());
	}	
	
	@Test
	public void calendarWithRemovedFutureEvents()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-3-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("RRuleRemoveFutureEvents.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		Kalendar kal = importedCalendar.getKalendar();
		ZonedDateTime windowStartDate = ZonedDateTime.of(2025, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime windowEndDate = ZonedDateTime.of(2025, 3, 10, 23, 59, 0, 0, ZoneId.systemDefault());
		List<KalendarEvent> excEvents = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertEquals(2, excEvents.size());
	}
	
	/**
	 * The rule OpenOlat writes with ical4j 3 is not valid with v.4
	 * @throws IOException
	 */
	@Test
	public void calendarWithRemovedFutureEventsVersion3()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-4-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("RRuleRemoveFutureEventsV3.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		Kalendar kal = importedCalendar.getKalendar();
		ZonedDateTime windowStartDate = ZonedDateTime.of(2025, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime windowEndDate = ZonedDateTime.of(2025, 3, 10, 23, 59, 0, 0, ZoneId.systemDefault());
		List<KalendarEvent> excEvents = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertEquals(2, excEvents.size());
	}
	
	/**
	 * The recurrence ID needs an extra VALUE=DATE parameter
	 * @throws IOException
	 */
	@Test
	public void calendarWithRecurenceIdValueDate()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("cal-5-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("RecurenceIdValueDate.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> kEvents = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(2, kEvents.size());
		
		// Get the events in the window (the method enlarge the specified arguments)
		Kalendar kal = importedCalendar.getKalendar();
		ZonedDateTime windowStartDate = ZonedDateTime.of(2025, 3, 19, 0, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime windowEndDate = ZonedDateTime.of(2025, 3, 22, 23, 59, 0, 0, ZoneId.systemDefault());
		List<KalendarEvent> events = calendarManager.getEvents(kal, windowStartDate, windowEndDate, true);
		Assert.assertFalse(events.isEmpty());
		
		// We check that the 20 there is one event
		List<KalendarEvent> excEvents = events.stream()
				.filter(event -> event.getBegin().getDayOfMonth() == 20)
				.toList();
		Assert.assertEquals(1, excEvents.size());
	}
	
	
	/**
	 * A recurring event with the start and end date reversed. This error
	 * in the calendar cause the whole calendar to crash in iCal4j v3.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Test
	public void calendarRecurringEventInversed()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur1-");
		URL calendarUrl = CalendarImportTest.class.getResource("ReversedRecurringEvent.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		ZonedDateTime startDate = ZonedDateTime.of(2018, 03, 10, 10, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime endDate = ZonedDateTime.of(2018, 03, 17, 10, 0, 0, 0, ZoneId.systemDefault());
		
		List<KalendarEvent> recurringEvents = calendarManager.getEvents(importedCalendar.getKalendar(), startDate, endDate, true);
		Assert.assertEquals(0, recurringEvents.size());
	}
	
	@Test
	public void calendarRecurringEventUntilLocalDate()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur1-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("RecurenceLocalDate.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		ZonedDateTime startDate = ZonedDateTime.of(2025, 9, 10, 10, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime endDate = ZonedDateTime.of(2025, 10, 17, 10, 0, 0, 0, ZoneId.systemDefault());
		
		List<KalendarEvent> recurringEvents = calendarManager.getEvents(importedCalendar.getKalendar(), startDate, endDate, true);
		Assert.assertEquals(6, recurringEvents.size());
		
		List<KalendarEvent> recurringRecurEvents =  recurringEvents.stream()
				.filter(event -> event instanceof KalendarRecurEvent)
				.toList();
		Assert.assertEquals(6, recurringRecurEvents.size());
	}
	
	/**
	 * A recurring event with missing end date. This error
	 * in the calendar cause the whole calendar to crash.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Test
	public void testCalendarRecurringEventMissingEndDate()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur1-");
		URL calendarUrl = CalendarImportTest.class.getResource("RecurringEventMissingEnd.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		ZonedDateTime startDate = ZonedDateTime.of(2018, 03, 10, 10, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime endDate = ZonedDateTime.of(2018, 03, 17, 10, 0, 0, 0, ZoneId.systemDefault());
		
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
		ZonedDateTime periodStart = DateUtils.getZonedDateTime(2010, Calendar.MARCH, 15);
		ZonedDateTime periodEnd = DateUtils.getZonedDateTime(2010, Calendar.MARCH, 20);
		
		List<KalendarEvent> eventsForPeriod = calendarManager
				.getEvents(reloadCalWrapper.getKalendar(), periodStart, periodEnd, true);
		Assert.assertNotNull(eventsForPeriod);
		Assert.assertEquals(0, eventsForPeriod.size());
		KalendarEvent eventForPeriod = events.iterator().next();
		Assert.assertEquals("e73iiu9masoddi4g0vllmi2ht0@google.com", eventForPeriod.getID());
		
		//out of scope
		ZonedDateTime periodStart2 = DateUtils.getZonedDateTime(2008, Calendar.APRIL, 15);
		ZonedDateTime periodEnd2 = DateUtils.getZonedDateTime(2008, Calendar.APRIL, 17);
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
		ZonedDateTime start = ZonedDateTime.now().with(ChronoField.MILLI_OF_SECOND, 0);
		ZonedDateTime end = start.plusHours(1);
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
	public void updateCalendar() throws ValidationException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("u1-");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();

		String eventIdMarker = "id-marker-event";
		
		// 1. Add a standard event, not managed
		ZonedDateTime start = ZonedDateTime.now().with(ChronoField.MILLI_OF_SECOND, 0);
		ZonedDateTime end = start.plusHours(1);
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
	public void importICalRecurringEvent()
	throws IOException {
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
	public void importICalRecurringEventUnreadableRecurringId()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur4-");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("RecurringDate.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		// Try catch save the event at this specific date
		ZonedDateTime startDate = ZonedDateTime.of(2022, 03, 4, 10, 0, 0, 0, ZoneId.systemDefault());
		ZonedDateTime endDate = ZonedDateTime.of(2022, 03, 7, 10, 0, 0, 0, ZoneId.systemDefault());
		List<KalendarEvent> recurringEvents = calendarManager.getEvents(importedCalendar.getKalendar(), startDate, endDate, true);
		Assert.assertEquals(1, recurringEvents.size());
	}
	
	@Test
	public void importICalOutlookFullDay()
	throws IOException {
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
	public void importICalIcalFullDay()
	throws IOException {
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
	
	@Test
	public void importOpenOlat191FullDay()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur4");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("Fullday_openolat_191.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		KalendarEvent on3days = importedCalendar.getKalendar().getEvents().get(0);
		Assert.assertTrue(on3days.isAllDayEvent());
	}
	
	/**
	 * The full days wrongly with time saved.
	 * 
	 * @throws IOException
	 */
	@Test
	public void importOpenOlat200FullDay()
	throws IOException {
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ur5");
		URL calendarUrl = ICalFileCalendarManagerTest.class.getResource("Fullday_wrong_ical4.ics");
		File calendarFile = JunitTestHelper.tmpCopy(calendarUrl);
		String calendarName = UUID.randomUUID().toString().replace("-", "");
		
		KalendarRenderWrapper importedCalendar = importCalendarManager
				.importCalendar(test, calendarName, CalendarManager.TYPE_USER, calendarFile);
		List<KalendarEvent> events = importedCalendar.getKalendar().getEvents();
		Assert.assertEquals(1, events.size());
		
		KalendarEvent on3days = importedCalendar.getKalendar().getEvents().get(0);
		Assert.assertTrue(on3days.isAllDayEvent());
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
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<>(1));

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
					calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, ZonedDateTime.now(), 1));
					log.info("testConcurrentAddEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					Assert.assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = cal.getEvent(TEST_EVENT_ID_1, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					Assert.assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
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
					calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, ZonedDateTime.now(), 1));
					log.info("testConcurrentAddEvent thread1 addEvent2 DONE");
					// 4. check event2 exist
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
					Assert.assertEquals("Wrong calendar-event subject", event2.getSubject(), TEST_EVENT_SUBJECT_2);
					// 5. check event1 exist
					cal = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = cal.getEvent(TEST_EVENT_ID_1, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					Assert.assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
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
			log.info("exception: {}", exception.getMessage(), exception);
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
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<>(1));

		// Generate event for update
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, ZonedDateTime.now(), 1));
		cal = calendarManager.getPersonalCalendar(test).getKalendar();
		KalendarEvent event2 = cal.getEvent(TEST_EVENT_ID_2, null);
		Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, event2);
		Assert.assertEquals("Wrong calendar-event subject",event2.getSubject(), TEST_EVENT_SUBJECT_2);
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
					calendarManager.addEventTo(currentCalendar, new KalendarEvent(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, ZonedDateTime.now(), 1));
					log.info("testConcurrentAddUpdateEvent thread1 addEvent1 DONE");
					// 3. check event1 exist
					currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = currentCalendar.getEvent(TEST_EVENT_ID_1, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					Assert.assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// 4. sleep 2sec
					
					// 5. check event1 still exist (event2 added in meantime)
					currentCalendar = calendarManager.getPersonalCalendar(test).getKalendar();
					event1 = currentCalendar.getEvent(TEST_EVENT_ID_1, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					Assert.assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
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
					calendarManager.updateEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2_UPDATED, ZonedDateTime.now(), 1));
					log.info("testConcurrentAddUpdateEvent thread1 updateEvent2 DONE");
					// 4. check event2 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent updatedEvent = calendar.getEvent(TEST_EVENT_ID_2, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_2, updatedEvent);
					Assert.assertEquals("Wrong calendar-event subject",updatedEvent.getSubject(), TEST_EVENT_SUBJECT_2_UPDATED);
					// 5. check event1 exist
					calendar = calendarManager.getPersonalCalendar(test).getKalendar();
					KalendarEvent event1 = calendar.getEvent(TEST_EVENT_ID_1, null);
					Assert.assertNotNull("Did not found event with id=" + TEST_EVENT_ID_1, event1);
					Assert.assertEquals("Wrong calendar-event subject",event1.getSubject(), TEST_EVENT_SUBJECT_1);
					// Delete Event
					boolean removed = calendarManager.removeEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2_UPDATED, ZonedDateTime.now(), 1));
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
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<>(1));
		final List<Boolean> statusList = Collections.synchronizedList(new ArrayList<>(1));

		// Generate event for update
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		calendarManager.addEventTo(cal, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, ZonedDateTime.now(), 1));
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
					calendarManager.addEventTo(calendar, new KalendarEvent(TEST_EVENT_ID_1, TEST_EVENT_SUBJECT_1, ZonedDateTime.now(), 1));
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
					boolean removed = calendarManager.removeEventFrom(calendar, new KalendarEvent(TEST_EVENT_ID_2, TEST_EVENT_SUBJECT_2, ZonedDateTime.now(), 1));
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
			log.info("exception: {}", exception.getMessage(), exception);
		}

		assertTrue("It throws an exception in test => see sysout", exceptionHolder.isEmpty());	
		log.info("testConcurrentAddRemoveEvent finish successful");
	}
}