/**

 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.commons.calendar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.logging.Tracing;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RecurrenceId;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CalendarImportTest {

	private static final Logger log = Tracing.createLoggerFor(CalendarImportTest.class);
	
	@Before
	public void setUp() {
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
	}
	
	@Test
	public void testImportMonthFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_30.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
        in.close();
	}
	
	@Test
	public void testImportWeekFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_7.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
        in.close();
	}
	
	@Test
	public void testImportAllFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_Alles.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
        in.close();
	}
	
	@Test
	public void testImportOktoberFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_Okt.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
        in.close();
	}
	
	@Test
	public void testImportFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("Hoffstedde.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
        in.close();
	}
	
	/*
	 * Why is this test not reliable???
	@Test(expected = ParserException.class)
	public void testImportRefresh() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("Refresh.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}

	@Test
	public void testImportFromFGiCal() throws IOException, ParserException {
		//default settings in olat
		System.setProperty(CompatibilityHints.KEY_RELAXED_UNFOLDING, "true");
		System.setProperty(CompatibilityHints.KEY_RELAXED_PARSING, "true");
		
		InputStream in = CalendarImportTest.class.getResourceAsStream("EMAIL.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}
	*/
	
	@Test
	public void testImportRecurringCal() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("RecurringEvent.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
        in.close();
        
        VEvent rootEvent = null;
        VEvent exceptionEvent = null;
        for (Iterator<?> iter = calendar.getComponents().iterator(); iter.hasNext();) {
			Object comp = iter.next();
			if (comp instanceof VEvent) {
				VEvent vevent = (VEvent)comp;
				if(vevent.getRecurrenceId() == null) {
					rootEvent = vevent;
				} else {
					exceptionEvent = vevent;
				}
			}
		}
        assertNotNull(rootEvent);
        assertNotNull(exceptionEvent);
        
        java.util.Date startDate = CalendarUtils.getDate(2016, java.util.Calendar.OCTOBER, 10);
        DateTime start = new DateTime(startDate);
        java.util.Date endDate = CalendarUtils.getDate(2016, java.util.Calendar.NOVEMBER, 10);
        DateTime end = new DateTime(endDate);
        
        Period period = new Period(start, end);
        PeriodList pList = rootEvent.calculateRecurrenceSet(period);
        for(Object obj:pList) {
        	Period p = (Period)obj;
        	log.info("Period: " + p.getStart());
        }
        
        RecurrenceId recurrenceId = exceptionEvent.getRecurrenceId();
        Date recurrenceDate = recurrenceId.getDate();
        log.info("Recurrence: " + recurrenceDate);
        exceptionEvent.getSequence();
	}
}
