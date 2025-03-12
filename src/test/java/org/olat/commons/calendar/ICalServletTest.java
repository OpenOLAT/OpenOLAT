/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.commons.calendar;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import jakarta.servlet.ServletException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.commons.calendar.manager.ICalFileCalendarManager;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * Initial date: 11 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ICalServletTest extends OlatTestCase {
	
	private final ICalServlet iCalServlet = new ICalServlet();

	@Autowired
	private DB dbInstance;
	@Autowired
	private ICalFileCalendarManager calendarManager;
	
	@Test
	public void loadICalFeed() throws ServletException, IOException {
		// Create a personal calendar
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-servlet-1");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// Add Event
		ZonedDateTime start = ZonedDateTime.now()
				.withHour(13)
				.withMinute(12)
				.withSecond(0)
				.with(ChronoField.MILLI_OF_SECOND, 0);
		KalendarEvent testEvent = new KalendarEvent("id-servlet", "Event for a servlet", start, 60 * 60 * 1000);// 1 hour
		calendarManager.addEventTo(cal, testEvent);
		
		// Create a configuration
		CalendarUserConfiguration config = calendarManager.createAggregatedCalendarConfig(CalendarManager.TYPE_USER_AGGREGATED, test.getKey(), test);
		dbInstance.commitAndCloseSession();
		
		String requestUri = "/" + CalendarManager.TYPE_USER_AGGREGATED + "/" + config.getKey() + "/" + config.getToken() + ".ics";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
		request.setPathInfo(requestUri);
		MockHttpServletResponse response = new MockHttpServletResponse();
		iCalServlet.service(request, response);
		
		String body = response.getContentAsString();
		Assertions.assertThat(body)
			.contains("SUMMARY:Event for a servlet")
			.contains("REFRESH-INTERVAL;VALUE=DURATION:PT15M");
	}
	
	@Test
	public void loadOutlookFeed() throws ServletException, IOException {
		// Create a personal calendar
		Identity test = JunitTestHelper.createAndPersistIdentityAsRndUser("ical-outlook-1");
		Kalendar cal = calendarManager.getPersonalCalendar(test).getKalendar();
		
		// Add Event
		ZonedDateTime start = ZonedDateTime.now()
				.withHour(13)
				.withMinute(12)
				.withSecond(0)
				.with(ChronoField.MILLI_OF_SECOND, 0)
				.withZoneSameLocal(ZoneId.of("Europe/Zurich"));
		KalendarEvent testEvent = new KalendarEvent("id-outlook", "Event for outlook", start, 60 * 60 * 1000);// 1 hour
		calendarManager.addEventTo(cal, testEvent);
		
		// Create a configuration
		CalendarUserConfiguration config = calendarManager.createAggregatedCalendarConfig(CalendarManager.TYPE_USER_AGGREGATED, test.getKey(), test);
		dbInstance.commitAndCloseSession();
		
		String requestUri = "/" + CalendarManager.TYPE_USER_AGGREGATED + "/" + config.getKey() + "/" + config.getToken() + ".ics";
		MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
		request.addHeader("User-Agent", "Microsoft Outlook");
		request.setPathInfo(requestUri);
		MockHttpServletResponse response = new MockHttpServletResponse();
		iCalServlet.service(request, response);
		
		String body = response.getContentAsString();
		Assertions.assertThat(body)
			.contains("SUMMARY:Event for outlook")
			.contains("REFRESH-INTERVAL;VALUE=DURATION:PT15M")
			.contains("DTSTART;TZID=\"Europe/Zurich\"");
	}

}
