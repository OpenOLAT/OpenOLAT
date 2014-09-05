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

package org.olat.commons.calendar.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarImportTest;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.ICalFileCalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

public class CalendarUtilsTest extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(CalendarUtilsTest.class);

	private static final int numEvents = 10000;
	private static final int maxEventDuratio = 1000 * 60 * 60 * 24 * 14; // maximum of 14 days duration
	private static final int oneYearSec = 60 * 60 * 24 * 365;
	private static final int goBackNumYears = 1;
	private static long kalendarStart = new Date().getTime() - (((long)goBackNumYears * oneYearSec) * 1000);


	@Test
	public void testListEventsForPeriod() {
		Identity test = JunitTestHelper.createAndPersistIdentityAsUser("test");
		Kalendar kalendar = new Kalendar("test", CalendarManager.TYPE_USER);

		log.info("*** Starting test with the following configuration:");
		log.info("*** Number of events: " + numEvents);
		log.info("*** Maximum event duration (ms): " + maxEventDuratio);
		log.info("*** Generate events in between "
				+ new Date(kalendarStart) + " and "
				+ new Date(kalendarStart + (1000 * ((long)goBackNumYears * oneYearSec))));

		createTestEvents(numEvents, kalendar);
		log.info("*** Load calendar...");
		CalendarManager manager = CalendarManagerFactory.getInstance().getCalendarManager();
		long start = System.currentTimeMillis();
		manager.getPersonalCalendar(test).getKalendar();
		long stop = System.currentTimeMillis();
		log.info("Duration load: " + (stop - start) + " ms.");
		
		log.info("*** Find events within period...");
		start = System.currentTimeMillis();
		List<KalendarEvent> events = CalendarUtils.listEventsForPeriod(kalendar, new Date(kalendarStart), new Date(kalendarStart + (1000 * ((long)(goBackNumYears * oneYearSec))) ));
		stop = System.currentTimeMillis();
		log.info("Duration find: " + (stop - start) + " ms.");
		log.info("Found " + events.size() + " events out of " + kalendar.getEvents().size() + " total events.");
		assertEquals(kalendar.getEvents().size(), events.size());
		
		log.info("*** Save calendar...");
		start = System.currentTimeMillis();
		((ICalFileCalendarManager)manager).persistCalendar(kalendar);
		stop = System.currentTimeMillis();
		log.info("Duration save: " + (stop - start) + " ms.");
	}
	
	/**
	 * Creates a number of events in certain calendar.
	 * @param numEvents
	 * @param cal
	 */
	private void createTestEvents(int numberOfEvents, Kalendar cal) {
		Random rand = new Random();
		long startUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		for (int i = 0; i < numberOfEvents; i++) {
			long begin = kalendarStart + (1000 * ((long)rand.nextInt(goBackNumYears * oneYearSec)));
			KalendarEvent event = new KalendarEvent("id" + i, "test" + i, new Date(begin), rand.nextInt(maxEventDuratio));
			cal.addEvent(event);
		}
		long stopUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		log.info("*** SETUP: Kalendar structure uses approx. " + (stopUsed - startUsed) / 1024 + " kb memory.");
		((ICalFileCalendarManager)CalendarManagerFactory.getInstance().getCalendarManager()).persistCalendar(cal);
	}
	
	/**
	 * Check a NPE
	 * @throws IOException
	 */
	@Test
	public void testListEventsForPeriodWithoutDTEndEvent() throws IOException {
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
		//to be sure
		emptyCalendarCache();
		//load the calendar
		KalendarRenderWrapper reloadCalWrapper = calManager.getPersonalCalendar(identity);
		//check if its the right calendar
		Collection<KalendarEvent> events = reloadCalWrapper.getKalendar().getEvents();
		Assert.assertNotNull(events);
		Assert.assertEquals(1, events.size());
		KalendarEvent event = events.iterator().next();
		Assert.assertEquals("Arbeitszeit: 1-3h", event.getSubject());
		Assert.assertEquals("e73iiu9masoddi4g0vllmi2ht0@google.com", event.getID());
		Assert.assertNull(event.getEnd());

		
		//test persist
		boolean allOk = calManager.persistCalendar(reloadCalWrapper.getKalendar());
		Assert.assertTrue(allOk);
		
		//an other possible RS
		//within period
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2010);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DATE, 15);
		Date periodStart = cal.getTime();
		
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2010);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DATE, 17);
		Date periodEnd = cal.getTime();
		List<KalendarEvent> eventsForPeriod = CalendarUtils.listEventsForPeriod(reloadCalWrapper.getKalendar(), periodStart, periodEnd);
		Assert.assertNotNull(eventsForPeriod);
		Assert.assertEquals(1, eventsForPeriod.size());
		KalendarEvent eventForPeriod = events.iterator().next();
		Assert.assertEquals("e73iiu9masoddi4g0vllmi2ht0@google.com", eventForPeriod.getID());
		
		//out of scope
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2008);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DATE, 15);
		Date periodStart2 = cal.getTime();
		
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2008);
		cal.set(Calendar.MONTH, 3);
		cal.set(Calendar.DATE, 17);
		Date periodEnd2 = cal.getTime();
		List<KalendarEvent> eventsOutOfPeriod = CalendarUtils.listEventsForPeriod(reloadCalWrapper.getKalendar(), periodStart2, periodEnd2);
		Assert.assertNotNull(eventsOutOfPeriod);
		Assert.assertTrue(eventsOutOfPeriod.isEmpty());
	}

	private final void emptyCalendarCache() {
		CoordinatorManager coordinator = CoreSpringFactory.getImpl(CoordinatorManager.class);
		Cacher cacher = coordinator.getCoordinator().getCacher();
		EmbeddedCacheManager cm = cacher.getCacheContainer();
		cm.getCache("CalendarManager@calendar").clear();
	}
}
