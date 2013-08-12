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

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.ICalFileCalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

public class CalendarUtilsTest extends OlatTestCase {

	private static final Runtime RUNTIME = Runtime.getRuntime ();
	private Kalendar kalendar;
	private static final int numEvents = 10000;
	private static final int maxEventDuratio = 1000 * 60 * 60 * 24 * 14; // maximum of 14 days duration
	private static final int oneYearSec = 60 * 60 * 24 * 365;
	private static final int goBackNumYears = 1;
	private static long kalendarStart = new Date().getTime() - (((long)goBackNumYears * oneYearSec) * 1000);
	private static Identity test;
	
	@Before
	public void setUp() throws Exception {
			test = JunitTestHelper.createAndPersistIdentityAsUser("test");
			kalendar = new Kalendar("test", CalendarManager.TYPE_USER);
	}
	@After
	public void tearDown() throws Exception {
		CalendarManagerFactory.getJUnitInstance().getCalendarManager().deletePersonalCalendar(test);
	}

	@Test
	public void testListEventsForPeriod() {
		System.out.println("*** Starting test with the following configuration:");
		System.out.println("*** Number of events: " + numEvents);
		System.out.println("*** Maximum event duration (ms): " + maxEventDuratio);
		System.out.println("*** Generate events in between "
				+ new Date(kalendarStart) + " and "
				+ new Date(kalendarStart + (1000 * ((long)goBackNumYears * oneYearSec))));

		createTestEvents(numEvents, kalendar);
		System.out.println("*** Load calendar...");
		CalendarManager manager = CalendarManagerFactory.getJUnitInstance().getCalendarManager();
		long start = System.currentTimeMillis();
		manager.getPersonalCalendar(test).getKalendar();
		long stop = System.currentTimeMillis();
		System.out.println("Duration load: " + (stop - start) + " ms.");
		
		System.out.println("*** Find events within period...");
		start = System.currentTimeMillis();
		List<KalendarEvent> events = CalendarUtils.listEventsForPeriod(kalendar, new Date(kalendarStart), new Date(kalendarStart + (1000 * ((long)(goBackNumYears * oneYearSec))) ));
		stop = System.currentTimeMillis();
		System.out.println("Duration find: " + (stop - start) + " ms.");
		System.out.println("Found " + events.size() + " events out of " + kalendar.getEvents().size() + " total events.");
		assertEquals(kalendar.getEvents().size(), events.size());
		
		System.out.println("*** Save calendar...");
		start = System.currentTimeMillis();
		((ICalFileCalendarManager)manager).persistCalendar(kalendar);
		stop = System.currentTimeMillis();
		System.out.println("Duration save: " + (stop - start) + " ms.");
		
	}

	
	/**
	 * Creates a number of events in certain calendar.
	 * @param numEvents
	 * @param cal
	 */
	private void createTestEvents(int numberOfEvents, Kalendar cal) {
		Random rand = new Random();
		long startUsed = RUNTIME.totalMemory() - RUNTIME.freeMemory();
		for (int i = 0; i < numberOfEvents; i++) {
			long begin = kalendarStart + (1000 * ((long)rand.nextInt(goBackNumYears * oneYearSec)));
			KalendarEvent event = new KalendarEvent("id" + i, "test" + i, new Date(begin), rand.nextInt(maxEventDuratio));
			cal.addEvent(event);
		}
		long stopUsed = RUNTIME.totalMemory() - RUNTIME.freeMemory();
		System.out.println("*** SETUP: Kalendar structure uses approx. " + (stopUsed - startUsed) / 1024 + " kb memory.");
		((ICalFileCalendarManager)CalendarManagerFactory.getJUnitInstance().getCalendarManager()).persistCalendar(kalendar);
	}
	

}
