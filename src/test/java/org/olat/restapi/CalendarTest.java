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
package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.olat.test.JunitTestHelper.random;

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

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.restapi.CalendarVO;
import org.olat.commons.calendar.restapi.EventLinkVO;
import org.olat.commons.calendar.restapi.EventVO;
import org.olat.commons.calendar.restapi.EventVOes;
import org.olat.commons.calendar.ui.ExternalLinksController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CalendarTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CalendarTest.class);

	private static ICourse course1;
	private static ICourse course2;
	private static IdentityWithLogin id1;
	private static IdentityWithLogin id2;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Before
	public void startup() {
		if(id1 == null) {
			id1 = JunitTestHelper.createAndPersistRndUser("cal-1");
		}
		if(id2 == null) {
			id2 = JunitTestHelper.createAndPersistRndUser("cal-2");
		}
		
		if(course1 == null) {
			//create a course with a calendar
			RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(id1.getIdentity(),
					RepositoryEntryStatusEnum.preparation, false, false);
			course1 = CourseFactory.loadCourse(courseEntry);
			dbInstance.commit();
			
			ICourse course = CourseFactory.loadCourse(course1.getResourceableId());
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			Assert.assertTrue(courseConfig.isCalendarEnabled());
			KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course);
			
			Calendar cal = Calendar.getInstance();
			for(int i=0; i<10; i++) {
				Date begin = cal.getTime();
				cal.add(Calendar.HOUR_OF_DAY, 1);
				Date end = cal.getTime();
				KalendarEvent event = new KalendarEvent(UUID.randomUUID().toString(), null, "Unit test " + i, begin, end);
				calendarManager.addEventTo(calendarWrapper.getKalendar(), event);
				cal.add(Calendar.DATE, 1);
			}

			cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			Date begin2 = cal.getTime();
			cal.add(Calendar.HOUR_OF_DAY, 1);
			Date end2 = cal.getTime();
			KalendarEvent event2 = new KalendarEvent(UUID.randomUUID().toString(), null, "Unit test 2", begin2, end2);
			calendarManager.addEventTo(calendarWrapper.getKalendar(), event2);
			
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(course1, false);
			entry = repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.published);
			repositoryService.addRole(id1.getIdentity(), entry, GroupRoles.participant.name());
			
			dbInstance.commit();
		}
		
		if(course2 == null) {
			//create a course with a calendar
			RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(id2.getIdentity(),
					RepositoryEntryStatusEnum.preparation, false, false);
			course2 = CourseFactory.loadCourse(courseEntry);
			dbInstance.commit();

			KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course2);
			Assert.assertNotNull(calendarWrapper);

			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(course2, false);
			entry = repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.published);
			dbInstance.commit();
		}
	}

	@Test
	public void testGetCalendars() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());//course1 + personal
		
		conn.shutdown();
	}
	
	@Test
	public void testHijackCalendars() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").path("events").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<EventVO> vos = parseEventArray(response);
		assertNotNull(vos);
		assertTrue(11 <= vos.size());//Root-1
		
		conn.shutdown();
	}

	@Test
	public void testGetEvents_onlyFuture() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").path("events")
				.queryParam("onlyFuture", "true").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<EventVO> vos = parseEventArray(response);
		assertNotNull(vos);
		assertTrue(10 <= vos.size());//Root-1
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date currentDate = cal.getTime();
		
		for(EventVO event:vos) {
			assertTrue(currentDate.equals(event.getEnd()) || currentDate.before(event.getEnd()));
		}
		conn.shutdown();
	}
	
	@Test
	public void testGetEvents_paging() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").path("events")
				.queryParam("start", "0").queryParam("limit", "5").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EventVOes voes = conn.parse(response, EventVOes.class);

		assertNotNull(voes);
		assertTrue(10 <= voes.getTotalCount());
		assertNotNull(voes.getEvents());
		assertEquals(5, voes.getEvents().length);
		
		//check reliability of api
		URI uriOverflow = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").path("events")
				.queryParam("start", voes.getTotalCount()).queryParam("limit", "5").build();
		HttpGet methodOverflow = conn.createGet(uriOverflow, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse responseOverflow = conn.execute(methodOverflow);
		assertEquals(200, responseOverflow.getStatusLine().getStatusCode());
		EventVOes voesOverflow  = conn.parse(responseOverflow, EventVOes.class);
		assertNotNull(voesOverflow);
		assertNotNull(voesOverflow.getEvents());
		assertEquals(0, voesOverflow.getEvents().length);

		conn.shutdown();
	}
	
	@Test
	public void testGetCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());//course1 + personal
		CalendarVO calendar = getCourseCalendar(vos, course1);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		List<EventVO> events = parseEventArray(eventResponse);
		assertNotNull(events);
		assertEquals(11, events.size());//Root-1
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCalendarEvents_onlyFuture() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());//course1 + personal
		CalendarVO calendar = getCourseCalendar(vos, course1);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").queryParam("onlyFuture", "true").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		List<EventVO> events = parseEventArray(eventResponse);
		assertNotNull(events);
		assertEquals(10, events.size());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCalendarEvents_paging() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());//course1 + personal
		CalendarVO calendar = getCourseCalendar(vos, course1);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events")
				.queryParam("start", "0").queryParam("limit", "5").queryParam("onlyFuture", "true").build();
		
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		EventVOes events = conn.parse(eventResponse, EventVOes.class);
		assertNotNull(events);
		assertEquals(10, events.getTotalCount());
		assertNotNull(events.getEvents());
		assertEquals(5, events.getEvents().length);
		
		conn.shutdown();
	}
	
	
	@Test
	public void testOutputGetCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("calendars").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());//Root-1
		CalendarVO calendar = getCourseCalendar(vos, course1);

		//get events and output as JSON
		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		String outputJson = EntityUtils.toString(eventResponse.getEntity());
		System.out.println("*** JSON");
		System.out.println(outputJson);

		//get events and output as XML
		URI eventXmlUri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventXmlMethod = conn.createGet(eventXmlUri, MediaType.APPLICATION_XML, true);
		HttpResponse eventXmlResponse = conn.execute(eventXmlMethod);
		assertEquals(200, eventXmlResponse.getStatusLine().getStatusCode());
		String outputXml = EntityUtils.toString(eventXmlResponse.getEntity());
		System.out.println("*** XML");
		System.out.println(outputXml);

		conn.shutdown();
	}
	
	@Test
	public void putCalendarEvent() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());
		CalendarVO calendar = getCourseCalendar(vos, course2);
		Assert.assertNotNull(calendar);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendar.getId()).path("event").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course2);
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
	public void putCalendarEventWithLinks() throws IOException, URISyntaxException {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("calendar-");

		RestConnection conn = new RestConnection();
		assertTrue(conn.login(identity));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		CalendarVO calendar = getUserCalendar(vos);
		assertNotNull(calendar);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);
		
		EventLinkVO link = new EventLinkVO();
		link.setId("link-id-1");
		link.setDisplayName("OpenOlat");
		link.setUri("https://www.openolat.org");
		link.setProvider(ExternalLinksController.EXTERNAL_LINKS_PROVIDER);
		link.setIconCssClass("o_openolat");
		event.setLinks(new EventLinkVO[] { link });

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("calendars").path(calendar.getId()).path("event").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());

		//check if the link is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(identity.getIdentity());
		List<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		Assert.assertNotNull(savedEvents);
		Assert.assertEquals(1, savedEvents.size());
		
		KalendarEvent savedEvent = savedEvents.get(0);
		List<KalendarEventLink> savedLinks = savedEvent.getKalendarEventLinks();
		Assert.assertNotNull(savedLinks);
		Assert.assertEquals(1, savedLinks.size());
		
		KalendarEventLink savedLink = savedLinks.get(0);
		Assert.assertEquals(ExternalLinksController.EXTERNAL_LINKS_PROVIDER, savedLink.getProvider());
		Assert.assertEquals("link-id-1", savedLink.getId());
		Assert.assertEquals("OpenOlat", savedLink.getDisplayName());
		Assert.assertEquals("https://www.openolat.org", savedLink.getURI());
		Assert.assertEquals("o_openolat", savedLink.getIconCssClass());

		conn.shutdown();
	}
	
	@Test
	public void putCalendarEvents_forbidden() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2));
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course1);
		String calendarCourse1Id = "course_" + calendarWrapper.getKalendar().getCalendarID();

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendarCourse1Id).path("event").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(403, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());

		conn.shutdown();
	}
	
	@Test
	public void testPostCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertTrue(vos != null && !vos.isEmpty());
		CalendarVO calendar = getCourseCalendar(vos, course2);
		Assert.assertNotNull(calendar);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendar.getId()).path("event").build();
		HttpPost postEventMethod = conn.createPost(eventUri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(postEventMethod, event);
		HttpResponse postEventResponse = conn.execute(postEventMethod);
		assertEquals(200, postEventResponse.getStatusLine().getStatusCode());

		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getCourseCalendar(course2);
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
	public void testAttributeMapping() throws IOException, URISyntaxException {
		// create a user and login
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("cal-3");
		RestConnection conn = new RestConnection();
		conn.login(identity);
		
		//create a course with a calendar
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity.getIdentity(), RepositoryEntryStatusEnum.published,
				false, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		
		// load the calendar
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		List<CalendarVO> vos = parseArray(response);
		CalendarVO calendar = getCourseCalendar(vos, course);
		
		// Add an calendar event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		event.setSubject(random());
		event.setAllDayEvent(true);
		event.setDescription(random());
		event.setExternalId(random());
		event.setExternalSource(random());
		event.setLocation(random());
		event.setColor(random());
		event.setManagedFlags(CalendarManagedFlag.description.name());
		event.setLiveStreamUrl(random());

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("calendars").path(calendar.getId()).path("event").build();
		HttpPost postEventMethod = conn.createPost(eventUri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(postEventMethod, event);
		conn.execute(postEventMethod);
		
		// Load the calendar from the manager and compare the event attributes
		Kalendar kalendar = calendarManager.getCourseCalendar(course).getKalendar();
		KalendarEvent kalendarEvent = kalendar.getEvents().get(0);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(kalendarEvent.getBegin()).isEqualTo(event.getBegin());
		softly.assertThat(kalendarEvent.getEnd()).isEqualTo(event.getEnd());
		softly.assertThat(kalendarEvent.getSubject()).isEqualTo(event.getSubject());
		softly.assertThat(kalendarEvent.isAllDayEvent()).isEqualTo(event.isAllDayEvent());
		softly.assertThat(kalendarEvent.getDescription()).isEqualTo(event.getDescription());
		softly.assertThat(kalendarEvent.getExternalId()).isEqualTo(event.getExternalId());
		softly.assertThat(kalendarEvent.getLocation()).isEqualTo(event.getLocation());
		softly.assertThat(kalendarEvent.getColor()).isEqualTo(event.getColor());
		softly.assertThat(kalendarEvent.getManagedFlags()).containsExactly(CalendarManagedFlag.description);
		softly.assertThat(kalendarEvent.getLiveStreamUrl()).isEqualTo(event.getLiveStreamUrl());
		
		// Load the calendar from REST again and compare the event attributes
		eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		EventVO reloadedEvent = parseEventArray(eventResponse).get(0);
		softly.assertThat(reloadedEvent.getBegin()).isEqualTo(event.getBegin());
		softly.assertThat(reloadedEvent.getEnd()).isEqualTo(event.getEnd());
		softly.assertThat(reloadedEvent.getSubject()).isEqualTo(event.getSubject());
		softly.assertThat(reloadedEvent.isAllDayEvent()).isEqualTo(event.isAllDayEvent());
		softly.assertThat(reloadedEvent.getDescription()).isEqualTo(event.getDescription());
		softly.assertThat(reloadedEvent.getExternalId()).isEqualTo(event.getExternalId());
		softly.assertThat(reloadedEvent.getLocation()).isEqualTo(event.getLocation());
		softly.assertThat(reloadedEvent.getManagedFlags()).isEqualTo(event.getManagedFlags());
		softly.assertThat(reloadedEvent.getLiveStreamUrl()).isEqualTo(event.getLiveStreamUrl());
		softly.assertAll();
	}
	
	@Test
	public void testGetPersonalCalendarEventWithLink() throws IOException, URISyntaxException {
		IdentityWithLogin identity = JunitTestHelper.createAndPersistRndUser("cal-perso");
		
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(identity.getIdentity());
		KalendarEvent event = new KalendarEvent(UUID.randomUUID().toString(), null, "Unit with links" , new Date(), DateUtils.addDays(new Date(), 1));
		KalendarEventLink link = new KalendarEventLink("appointments", "app-01", "Termin", "https://www.openolat.org", "o_icon");
		event.setKalendarEventLinks(List.of(link));
		calendarManager.addEventTo(calendarWrapper.getKalendar(), event);
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(identity));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		List<CalendarVO> vos = parseArray(response);
		CalendarVO calendar = getUserCalendar(vos);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("calendars").path(calendar.getId()).path("events").build();
		HttpGet eventMethod = conn.createGet(eventUri, MediaType.APPLICATION_JSON, true);
		HttpResponse eventResponse = conn.execute(eventMethod);
		assertEquals(200, eventResponse.getStatusLine().getStatusCode());
		
		List<EventVO> events = parseEventArray(eventResponse);
		assertNotNull(events);
		assertEquals(1, events.size());
		
		EventVO eventVo = events.get(0);
		EventLinkVO[] links = eventVo.getLinks();
		assertNotNull(links);
		assertEquals(1, links.length);
		
		EventLinkVO linkVo = links[0];
		assertEquals("appointments", linkVo.getProvider());
		assertEquals("app-01", linkVo.getId());
		assertEquals("Termin", linkVo.getDisplayName());
		assertEquals("https://www.openolat.org", linkVo.getUri());
		assertEquals("o_icon", linkVo.getIconCssClass());
		
		conn.shutdown();
	}
	
	@Test
	public void testPutPersonalCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2));
		
		URI calUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("calendars").build();
		HttpGet calMethod = conn.createGet(calUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(calMethod);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CalendarVO> vos = parseArray(response);
		assertNotNull(vos);
		assertTrue(2 <= vos.size());
		CalendarVO calendar = getUserCalendar(vos);
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path(calendar.getId()).path("event").build();
		HttpPut putEventMethod = conn.createPut(eventUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putEventMethod, event);
		HttpResponse putEventResponse = conn.execute(putEventMethod);
		assertEquals(200, putEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putEventResponse.getEntity());
		
		
		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(id2.getIdentity());
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
	public void testDeletePersonalCalendarEvents() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2));
		
		//check if the event is saved
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(id2.getIdentity());
		KalendarEvent kalEvent = new KalendarEvent(UUID.randomUUID().toString(), null, "Rendez-vous", new Date(), new Date());
		calendarManager.addEventTo(calendarWrapper.getKalendar(), kalEvent);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString())
				.path("calendars").path("user_" + calendarWrapper.getKalendar().getCalendarID())
				.path("events").path(kalEvent.getID()).build();
		HttpDelete delEventMethod = conn.createDelete(eventUri, MediaType.APPLICATION_JSON);
		HttpResponse delEventResponse = conn.execute(delEventMethod);
		assertEquals(200, delEventResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(delEventResponse.getEntity());

		conn.shutdown();
		
		//check if the event is saved
		Collection<KalendarEvent> savedEvents = calendarWrapper.getKalendar().getEvents();
		for(KalendarEvent savedEvent:savedEvents) {
			Assert.assertFalse(savedEvent.getID().equals(kalEvent.getID()));
		}
	}
	
	protected CalendarVO getCourseCalendar(List<CalendarVO> vos, ICourse course) {
		for(CalendarVO vo:vos) {
			if(vo.getId().startsWith("course") && vo.getId().endsWith(course.getResourceableId().toString())) {
				return vo;
			}
		}
		return null;
	}
	
	protected CalendarVO getUserCalendar(List<CalendarVO> vos) {
		for(CalendarVO vo:vos) {
			if(vo.getId().startsWith("user")) {
				return vo;
			}
		}
		return null;
	}
	
	protected List<CalendarVO> parseArray(HttpResponse response) {
		try(InputStream body = response.getEntity().getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<CalendarVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<EventVO> parseEventArray(HttpResponse response) {
		try(InputStream body = response.getEntity().getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<EventVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

}
