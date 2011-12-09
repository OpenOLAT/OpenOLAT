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

package org.olat.commons.calendar.ui.components;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.core.util.Util;
import org.olat.test.OlatTestCase;



public class WeeklyCalendarComponentTest extends OlatTestCase {


	WeeklyCalendarComponent weeklyCalendarComponent, weeklyCalendarComponent_EN;
	@Before public void setup() throws Exception {
		weeklyCalendarComponent = new WeeklyCalendarComponent("test", new ArrayList(), 6, Util.createPackageTranslator(WeeklyCalendarController.class, Locale.GERMAN), true);
		weeklyCalendarComponent_EN = new WeeklyCalendarComponent("test", new ArrayList(), 6, Util.createPackageTranslator(WeeklyCalendarController.class, Locale.ENGLISH), true);
	}

	@After public void tearDown() throws Exception {
	}

	@Test public void test20072008() {
		System.out.println("test20072008 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2007, 51);
			Calendar cal = createCalendar(2007, 11, 17 + i);
			System.out.println("test20072008 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2007,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2007,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20082009() {
		System.out.println("test20082009 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2008, 51);
			Calendar cal = createCalendar(2008, 11, 15 + i);
			System.out.println("test20082009 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20092010() {
		System.out.println("test20092010 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2009, 51);
			Calendar cal = createCalendar(2009, 11, 14 + i);
			System.out.println("test20092010 cal=" + cal.getTime());
			System.out.println("DEBUG: cal=" + cal);
			System.out.println("DEBUG: cal.getFirstDayOfWeek()=" + cal.getFirstDayOfWeek());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20102011() {
		System.out.println("test20102011 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2010, 51);
			Calendar cal = createCalendar(2010, 11, 20 + i);
			System.out.println("test20102011 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20112012() {
		System.out.println("test20112012 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2011, 51);
			Calendar cal = createCalendar(2011, 11, 19 + i);
			System.out.println("test20112012 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20122013() {
		System.out.println("test20122013 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2012, 51);
			Calendar cal = createCalendar(2012, 11, 17 + i);
			System.out.println("test20122013 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20132014() {
		System.out.println("test20132014 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2013, 51);
			Calendar cal = createCalendar(2013, 11, 16 + i);
			System.out.println("test20132014 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20142015() {
		System.out.println("test20142015 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2014, 51);
			Calendar cal = createCalendar(2014, 11, 15 + i);
			System.out.println("test20142015 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void test20152016() {
		System.out.println("test20152016 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent.setFocus(2015, 51);
			Calendar cal = createCalendar(2015, 11, 14 + i);
			System.out.println("test20152016 cal=" + cal.getTime());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
			weeklyCalendarComponent.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		}
	}

	@Test public void testSetDate2008_2009() {
		// Week 51/2008
		Calendar cal = createCalendar(2008, Calendar.DECEMBER, 15);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 16);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 17);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 18);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 19);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 20);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 21);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		// Week 52/2008
		cal = createCalendar(2008, Calendar.DECEMBER, 22);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 23);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 24);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 25);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 26);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 27);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 28);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		// Week 1/2009
		cal = createCalendar(2008, Calendar.DECEMBER, 29);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 30);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2008, Calendar.DECEMBER, 31);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 1);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 2);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 3);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 4);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		// Week 2/2009
		cal = createCalendar(2009, Calendar.JANUARY, 5);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 6);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 7);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 8);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 9);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 10);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.JANUARY, 11);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent.getWeekOfYear());
	}

	@Test public void testSetDate2009_2010() {
		// Week 51/2009
		Calendar cal = createCalendar(2009, Calendar.DECEMBER, 14);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 15);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 16);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 17);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 18);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 19);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 20);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent.getWeekOfYear());
		// Week 52/2009
		cal = createCalendar(2009, Calendar.DECEMBER, 21);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 22);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 23);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 24);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 25);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 26);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 27);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		// Week 53/2009
		cal = createCalendar(2009, Calendar.DECEMBER, 28);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 29);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 30);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2009, Calendar.DECEMBER, 31);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 1);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 2);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 3);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent.getWeekOfYear());
		// Week 1/2010
		cal = createCalendar(2010, Calendar.JANUARY, 4);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 5);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 6);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 7);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 8);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 9);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.JANUARY, 10);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
	}
	
	@Test public void testSetDate2010_2011() {
		// Week 52/2010
		Calendar cal = createCalendar(2010, Calendar.DECEMBER, 27);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.DECEMBER, 28);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.DECEMBER, 29);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.DECEMBER, 30);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2010, Calendar.DECEMBER, 31);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.JANUARY, 1);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.JANUARY, 2);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		// Week 1/2011
		cal = createCalendar(2011, Calendar.JANUARY, 3);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.JANUARY, 4);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
	}

	@Test public void testSetDate2011_2012() {
		// Week 52/2011
		Calendar cal = createCalendar(2011, Calendar.DECEMBER, 26);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.DECEMBER, 27);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.DECEMBER, 28);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.DECEMBER, 29);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.DECEMBER, 30);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2011, Calendar.DECEMBER, 31);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2012, Calendar.JANUARY, 1);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent.getWeekOfYear());
		// Week 1/2012
		cal = createCalendar(2012, Calendar.JANUARY, 2);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
		cal = createCalendar(2012, Calendar.JANUARY, 3);
		weeklyCalendarComponent.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent.getWeekOfYear());
	}

	@Test public void test20072008_EN() {
		System.out.println("test20072008 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2007, 51);
			Calendar cal = createCalendar_EN(2007, 11, 17 + i);
			System.out.println("test20072008 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2007,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2007,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20082009_EN() {
		System.out.println("test20082009 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2008, 51);
			Calendar cal = createCalendar_EN(2008, 11, 15 + i);
			System.out.println("test20082009 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20092010_EN() {
		System.out.println("test20092010 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2009, 51);
			Calendar cal = createCalendar_EN(2009, 11, 14 + i);
			System.out.println("test20092010 cal=" + cal.getTime());
			System.out.println("DEBUG: cal=" + cal);
			System.out.println("DEBUG: cal.getFirstDayOfWeek()=" + cal.getFirstDayOfWeek());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",3,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20102011_EN() {
		System.out.println("test20102011 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2010, 51);
			Calendar cal = createCalendar_EN(2010, 11, 20 + i);
			System.out.println("test20102011 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20112012_EN() {
		System.out.println("test20112012 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2011, 51);
			Calendar cal = createCalendar_EN(2011, 11, 19 + i);
			System.out.println("test20112012 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20122013_EN() {
		System.out.println("test20122013 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2012, 51);
			Calendar cal = createCalendar_EN(2012, 11, 17 + i);
			System.out.println("test20122013 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20132014_EN() {
		System.out.println("test20132014 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2013, 51);
			Calendar cal = createCalendar_EN(2013, 11, 16 + i);
			System.out.println("test20132014 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2013,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20142015_EN() {
		System.out.println("test20142015 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2014, 51);
			Calendar cal = createCalendar_EN(2014, 11, 15 + i);
			System.out.println("test20142015 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2014,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void test20152016_EN() {
		System.out.println("test20152016 start...");
		// for each day of week (Monday - Sunday)
		for (int i=0; i<7; i++) {
			weeklyCalendarComponent_EN.setFocus(2015, 51);
			Calendar cal = createCalendar_EN(2015, 11, 14 + i);
			System.out.println("test20152016 cal=" + cal.getTime());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.nextWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",3,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2016,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
			weeklyCalendarComponent_EN.previousWeek(cal);
			assertEquals("Wrong year after nextWeek",2015,weeklyCalendarComponent_EN.getYear());
			assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		}
	}

	@Test public void testSetDate2008_2009_EN() {
		// Week 51/2008
		Calendar cal = createCalendar_EN(2008, Calendar.DECEMBER, 14);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 15);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 16);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 17);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 18);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 19);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 20);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 52/2008
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 21);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 22);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 23);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 24);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 25);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 26);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 27);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 53/2008
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 28);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 29);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 30);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2008, Calendar.DECEMBER, 31);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 1);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 2);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 3);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2008,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",53,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 1/2009
		cal = createCalendar_EN(2009, Calendar.JANUARY, 4);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 5);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 6);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 7);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 8);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 9);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.JANUARY, 10);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 2/2009
		cal = createCalendar_EN(2009, Calendar.JANUARY, 11);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
	}

	@Test public void testSetDate2009_2010_EN() {
		// Week 50/2009
		Calendar cal = createCalendar_EN(2009, Calendar.DECEMBER, 14);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",50,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 15);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",50,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 16);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",50,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 17);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",50,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 18);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",50,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 19);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",50,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 51/2009
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 20);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 21);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 22);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 23);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 24);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 25);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 26);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",51,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 52/2009
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 27);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 28);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 29);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 30);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2009, Calendar.DECEMBER, 31);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 1);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 2);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2009,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 1/2010
		cal = createCalendar_EN(2010, Calendar.JANUARY, 3);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 4);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 5);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 6);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 7);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 8);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.JANUARY, 9);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 2/2010
		cal = createCalendar_EN(2010, Calendar.JANUARY, 10);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",2,weeklyCalendarComponent_EN.getWeekOfYear());
	}
	
	@Test public void testSetDate2010_2011_EN() {
		// Week 52/2010
		Calendar cal = createCalendar_EN(2010, Calendar.DECEMBER, 27);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.DECEMBER, 28);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.DECEMBER, 29);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.DECEMBER, 30);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2010, Calendar.DECEMBER, 31);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.JANUARY, 1);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2010,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 1/2011
		cal = createCalendar_EN(2011, Calendar.JANUARY, 2);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.JANUARY, 3);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.JANUARY, 4);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
	}

	@Test public void testSetDate2011_2012_EN() {
		// Week 52/2011
		Calendar cal = createCalendar_EN(2011, Calendar.DECEMBER, 26);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.DECEMBER, 27);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.DECEMBER, 28);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.DECEMBER, 29);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.DECEMBER, 30);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2011, Calendar.DECEMBER, 31);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2011,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",52,weeklyCalendarComponent_EN.getWeekOfYear());
		// Week 1/2012
		cal = createCalendar_EN(2012, Calendar.JANUARY, 1);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2012, Calendar.JANUARY, 2);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
		cal = createCalendar_EN(2012, Calendar.JANUARY, 3);
		weeklyCalendarComponent_EN.setDate(cal.getTime());
		assertEquals("Wrong year after nextWeek",2012,weeklyCalendarComponent_EN.getYear());
		assertEquals("Wrong week after nextWeek",1,weeklyCalendarComponent_EN.getWeekOfYear());
	}
	
	private Calendar createCalendar(int year, int month, int date) {
		Calendar cal = CalendarUtils.createCalendarInstance(Locale.GERMAN);
		cal.set(year, month, date);
		return cal;
	}

	private Calendar createCalendar_EN(int year, int month, int date) {
		Calendar cal = CalendarUtils.createCalendarInstance(Locale.ENGLISH);
		cal.set(year, month, date);
		return cal;
	}

}
