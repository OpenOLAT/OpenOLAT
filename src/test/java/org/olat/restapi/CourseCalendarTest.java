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


package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.restapi.EventVO;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 08.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseCalendarTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CourseCalendarTest.class);
	
	private IdentityWithLogin auth1;
	private ICourse course1;

	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarManager calendarManager;
	
	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() throws Exception {
		try {
			// create course and persist as OLATResourceImpl
			auth1 = JunitTestHelper.createAndPersistRndUser("rest-course-cal-one");
			CourseConfigVO config = new CourseConfigVO();
			config.setCalendar(Boolean.TRUE);
			RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(auth1.getIdentity(),
					RepositoryEntryStatusEnum.preparation, false, false);
			course1 = CourseFactory.loadCourse(courseEntry);
			dbInstance.commit();
			
			ICourse course = CourseFactory.loadCourse(course1.getResourceableId());
			Assert.assertTrue(course.getCourseConfig().isCalendarEnabled());
			
			CalendarManager calManager = CoreSpringFactory.getImpl(CalendarManager.class);
			KalendarRenderWrapper calendarWrapper = calManager.getCourseCalendar(course);
			
			Calendar cal = Calendar.getInstance();
			for(int i=0; i<2; i++) {
				Date begin = cal.getTime();
				cal.add(Calendar.HOUR_OF_DAY, 1);
				Date end = cal.getTime();
				String eventId = UUID.randomUUID().toString();
				KalendarEvent event = new KalendarEvent(eventId, null, "Unit test " + i, begin, end);
				calManager.addEventTo(calendarWrapper.getKalendar(), event);
				cal.add(Calendar.DATE, 1);
			}
			
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	@Test
	public void getCalendarEvents()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(auth1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course1.getResourceableId().toString()).path("calendar").path("events").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<EventVO> vos = parseEventArray(response.getEntity().getContent());
		assertNotNull(vos);
		assertTrue(2 <= vos.size());
		
		conn.shutdown();
	}
	
	@Test
	public void putCalendarEvent() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(auth1));

		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course1.getResourceableId().toString()).path("calendar").path("event").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course1);
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		
		boolean found = false;
		for(KalendarEvent savedEvent:savedEvents) {
			if(subject.equals(savedEvent.getSubject())) {
				found = true;
			}
		}
		Assert.assertTrue(found);

		conn.shutdown();
	}
	
	@Test
	public void putVisibleCalendarEvent() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(auth1));

		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);
		event.setClassification(KalendarEvent.CLASS_PUBLIC);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course1.getResourceableId().toString()).path("calendar").path("event").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course1);
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		
		KalendarEvent savedEvent = savedEvents.stream()
				.filter(e -> subject.equals(e.getSubject()))
				.findFirst().get();
		
		Assert.assertEquals(KalendarEvent.CLASS_PUBLIC, savedEvent.getClassification());

		conn.shutdown();
	}
	
	@Test
	public void putCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");

		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin,
				RepositoryEntryStatusEnum.preparation, false, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		//create an event
		EventVO event1 = new EventVO();
		Calendar cal = Calendar.getInstance();
		event1.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event1.setEnd(cal.getTime());
		String subject1 = UUID.randomUUID().toString();
		event1.setSubject(subject1);

		EventVO event2 = new EventVO();
		event2.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event2.setEnd(cal.getTime());
		String subject2 = UUID.randomUUID().toString();
		event2.setSubject(subject2);
		
		EventVO[] newEvents = new EventVO[2];
		newEvents[0] = event1;
		newEvents[1] = event2;

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("calendar").path("events").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, newEvents);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course);
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		
		boolean found1 = false;
		boolean found2 = false;
		for(KalendarEvent savedEvent:savedEvents) {
			if(subject1.equals(savedEvent.getSubject())) {
				found1 = true;
			} else if(subject2.equals(savedEvent.getSubject())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);

		conn.shutdown();
	}
	
	@Test
	public void deleteCalendarEvent() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(auth1));
		
		//create an event if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course1);
		
		Calendar cal = Calendar.getInstance();
		Date begin = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, 1);
		String id = UUID.randomUUID().toString();
		KalendarEvent kalEvent = new KalendarEvent(id, null, "Subject (" + id + ")", begin, cal.getTime());
		calendarManager.addEventTo(calendarWrapper.getKalendar(), kalEvent);

		//check if the event exists
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course1.getResourceableId().toString()).path("calendar").path("events").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<EventVO> vos = parseEventArray(response.getEntity().getContent());
		assertNotNull(vos);
		boolean found = false;
		for(EventVO vo:vos) {
			if(id.equals(vo.getId())) {
				found = true;
			}
		}
		assertTrue(found);
		
		//delete the event
		URI eventUri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course1.getResourceableId().toString()).path("calendar").path("events")
				.path(kalEvent.getID()).build();
		HttpDelete delEventMethod = conn.createDelete(eventUri, MediaType.APPLICATION_JSON);
		HttpResponse delEventResponse = conn.execute(delEventMethod);
		assertEquals(200, delEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(delEventResponse.getEntity());

		conn.shutdown();
		
		//check if the event is really deleted
		KalendarRenderWrapper reloadedCalendarWrapper = calendarManager.getCourseCalendar(course1);
		Collection<KalendarEvent> savedEvents = reloadedCalendarWrapper.getKalendar().getEvents();
		for(KalendarEvent savedEvent:savedEvents) {
			Assert.assertFalse(savedEvent.getID().equals(kalEvent.getID()));
		}
	}
	
	protected List<EventVO> parseEventArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<EventVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}