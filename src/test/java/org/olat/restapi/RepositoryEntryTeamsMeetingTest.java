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
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.core.util.httpclient.ConnectionUtilities;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.restapi.TeamsMeetingVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.graph.models.OnlineMeetingPresenters;

/**
 * 
 * Initial date: 14 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryTeamsMeetingTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private OrganisationService organisationService;
	
	
	private static Organisation defaultUnitTestOrganisation;
	private static IdentityWithLogin defaultUnitTestAdministrator;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Teams-course-unit-test", "Teams-course-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper
					.createAndPersistRndAdmin("Teams-Elem-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void getMeetingsAsTool()
	throws IOException, URISyntaxException, InterruptedException {
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-teams-0");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Teams-Course-1", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		Date start = DateUtils.addHours(new Date(), 3);
		Date end = DateUtils.addHours(new Date(), 4);
		
		TeamsMeeting teamsMeeting = teamsService.createMeeting("Hello Team", start, end, courseEntry, null, null, auth);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(courseEntry.getKey().toString()).path("teams").path("tool").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);

		Assert.assertEquals(200, response.statusCode());
		List<TeamsMeetingVO> voList = conn.parseList(response, TeamsMeetingVO.class);
		
		Assertions.assertThat(voList)
			.hasSize(1)
			.map(TeamsMeetingVO::getKey)
			.containsExactly(teamsMeeting.getKey());
		
		TeamsMeetingVO meetingVo = voList.get(0);
		Assert.assertEquals("Hello Team", meetingVo.getSubject());
		Assert.assertNotNull(meetingVo.getStartDate());
		Assert.assertNotNull(meetingVo.getEndDate());
		conn.close();
	}
	
	@Test
	public void addMeetingAsTool()
	throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Teams-Course-2", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		Date start = DateUtils.addHours(new Date(), 3);
		Date end = DateUtils.addHours(new Date(), 4);

		TeamsMeetingVO meetingVo = new TeamsMeetingVO();
		meetingVo.setSubject("Hello friends of REST");
		meetingVo.setDescription("This is a small society about REST");
		meetingVo.setMainPresenter("-");
		meetingVo.setStartDate(start);
		meetingVo.setEndDate(end);
		meetingVo.setLeadTime(5);
		meetingVo.setFollowupTime(10);
		meetingVo.setPermanent(Boolean.TRUE);
		meetingVo.setAllowedPresenters("organizer");
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(courseEntry.getKey().toString()).path("teams").path("tool").build();
		HttpRequest method = conn.createPut(uri, meetingVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);

		Assert.assertEquals(200, response.statusCode());
		TeamsMeetingVO newMeetingVo = conn.parse(response, TeamsMeetingVO.class);
		
		// Check response
		Assert.assertNotNull(newMeetingVo.getKey());
		Assert.assertEquals("Hello friends of REST", newMeetingVo.getSubject());
		Assert.assertEquals("This is a small society about REST", newMeetingVo.getDescription());
		Assert.assertEquals("-", newMeetingVo.getMainPresenter());
		Assert.assertNotNull(newMeetingVo.getStartDate());
		Assert.assertEquals(5, newMeetingVo.getLeadTime());
		Assert.assertNotNull(newMeetingVo.getEndDate());
		Assert.assertEquals(10, newMeetingVo.getFollowupTime());
		Assert.assertEquals(Boolean.TRUE, newMeetingVo.getPermanent());
		Assert.assertEquals(OnlineMeetingPresenters.Organizer.name(), newMeetingVo.getAllowedPresenters());
		
		// Load the meeting and check
		TeamsMeeting meeting = teamsService.getMeetingByKey(newMeetingVo.getKey());
		Assert.assertEquals("Hello friends of REST", meeting.getSubject());
		Assert.assertEquals("This is a small society about REST", meeting.getDescription());
		Assert.assertEquals("-", meeting.getMainPresenter());
		Assert.assertNotNull(meeting.getStartDate());
		Assert.assertEquals(5, meeting.getLeadTime());
		Assert.assertNotNull(meeting.getEndDate());
		Assert.assertEquals(10, meeting.getFollowupTime());
		Assert.assertTrue(meeting.isPermanent());
		Assert.assertEquals(OnlineMeetingPresenters.Organizer, meeting.getAllowedPresentersEnum());
		conn.close();
	}
	
	@Test
	public void getMeetingAsTool()
	throws IOException, URISyntaxException, InterruptedException {
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-teams-3");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Teams-Course-3", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		Date start = DateUtils.addHours(new Date(), 3);
		Date end = DateUtils.addHours(new Date(), 4);
		
		TeamsMeeting meeting = teamsService.createMeeting("Hello specific Team", start, end, courseEntry, null, null, auth);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(courseEntry.getKey().toString()).path("teams").path("tool").path(meeting.getKey().toString()).build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);

		Assert.assertEquals(200, response.statusCode());
		TeamsMeetingVO meetingVo = conn.parse(response, TeamsMeetingVO.class);

		Assert.assertEquals(meeting.getKey(), meetingVo.getKey());
		Assert.assertEquals("Hello specific Team", meetingVo.getSubject());
		Assert.assertNotNull(meetingVo.getStartDate());
		Assert.assertNotNull(meetingVo.getEndDate());
		conn.close();
	}
	
	@Test
	public void deleteMeetingAsTool()
	throws IOException, URISyntaxException, InterruptedException {
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-teams-5");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Teams-Course-5", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		Date start = DateUtils.addHours(new Date(), 3);
		Date end = DateUtils.addHours(new Date(), 4);
		
		TeamsMeeting meeting = teamsService.createMeeting("Hello specific Team", start, end, courseEntry, null, null, auth);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(courseEntry.getKey().toString()).path("teams").path("tool").path(meeting.getKey().toString()).build();
		HttpRequest method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);

		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		TeamsMeeting deletedMeeting = teamsService.getMeeting(meeting);
		Assert.assertNull(deletedMeeting);
		conn.close();
	}
	
	/**
	 * An owner of course B cannot update a meeting of course A through
	 * course B.
	 * @throws InterruptedException 
	 */
	@Test
	public void updateMeetingOfOtherCourse()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin ownerB = JunitTestHelper.createAndPersistRndAuthor("rest-teams-10");
		RepositoryEntry courseEntryA = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Teams-Course-10", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		RepositoryEntry courseEntryB = JunitTestHelper.deployBasicCourse(ownerB.getIdentity());
		TeamsMeeting meetingA = createMeeting("Meeting of A", courseEntryA, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(ownerB);
		HttpResponse<InputStream> response = postMeeting(conn, courseEntryB, meetingA.getKey(), "Hacked");
		Assert.assertEquals(404, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
		
		TeamsMeeting unchangedMeeting = teamsService.getMeetingByKey(meetingA.getKey());
		Assert.assertEquals("Meeting of A", unchangedMeeting.getSubject());
		Assert.assertEquals(courseEntryA, unchangedMeeting.getEntry());
	}
	
	/**
	 * An owner can update a meeting of its own course (the check was
	 * inverted before OO-10072).
	 * @throws InterruptedException 
	 */
	@Test
	public void updateOwnMeetingAsOwner()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-teams-11");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		TeamsMeeting meeting = createMeeting("My meeting", courseEntry, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = postMeeting(conn, courseEntry, meeting.getKey(), "My updated meeting");
		Assert.assertEquals(200, response.statusCode());
		TeamsMeetingVO updatedMeetingVo = conn.parse(response, TeamsMeetingVO.class);
		Assert.assertEquals(meeting.getKey(), updatedMeetingVo.getKey());
		Assert.assertEquals("My updated meeting", updatedMeetingVo.getSubject());
		conn.close();
		
		TeamsMeeting updatedMeeting = teamsService.getMeetingByKey(meeting.getKey());
		Assert.assertEquals("My updated meeting", updatedMeeting.getSubject());
	}
	
	/**
	 * A meeting of a course element cannot be updated through the tool.
	 * @throws InterruptedException 
	 */
	@Test
	public void updateCourseElementMeetingAsTool()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-teams-12");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		TeamsMeeting meeting = createMeeting("Element meeting", courseEntry, "123456789");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = postMeeting(conn, courseEntry, meeting.getKey(), "Changed");
		Assert.assertEquals(409, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
		
		TeamsMeeting unchangedMeeting = teamsService.getMeetingByKey(meeting.getKey());
		Assert.assertEquals("Element meeting", unchangedMeeting.getSubject());
	}
	
	/**
	 * Update a meeting with an unknown key.
	 * @throws InterruptedException 
	 */
	@Test
	public void updateMeetingUnknownKey()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-teams-13");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = postMeeting(conn, courseEntry, Long.valueOf(-27l), "Unknown");
		Assert.assertEquals(404, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
	}
	
	private TeamsMeeting createMeeting(String subject, RepositoryEntry courseEntry, String subIdent) {
		Date start = DateUtils.addHours(new Date(), 3);
		Date end = DateUtils.addHours(new Date(), 4);
		return teamsService.createMeeting(subject, start, end, courseEntry, subIdent, null,
				defaultUnitTestAdministrator.getIdentity());
	}
	
	private HttpResponse<InputStream> postMeeting(RestConnection conn, RepositoryEntry courseEntry, Long meetingKey, String subject)
	throws IOException, URISyntaxException, InterruptedException {
		TeamsMeetingVO meetingVo = new TeamsMeetingVO();
		meetingVo.setKey(meetingKey);
		meetingVo.setSubject(subject);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(courseEntry.getKey().toString()).path("teams").path("tool").build();
		HttpRequest method = conn.createPost(uri, meetingVo, MediaType.APPLICATION_JSON);
		return conn.execute(method);
	}
}
