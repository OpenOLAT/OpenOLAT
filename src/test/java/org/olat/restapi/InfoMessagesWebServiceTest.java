/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.restapi.InfoMessageVO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.httpclient.ConnectionUtilities;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Permission checks of the info messages REST API (OO-10057).
 *
 * Initial date: 11 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class InfoMessagesWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private InfoMessageFrontendManager infoMessageManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Test
	public void getMessageCourseAsAdministrator()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-1");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "114840998525891", owner.getIdentity());
 
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(msg.getKey(), vo.getKey());
		Assert.assertEquals(msg.getTitle(), vo.getTitle());
		conn.shutdown();
	}

	@Test
	public void getMessageCourseAsOwner()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-2");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		InfoMessage msg = createMessage(courseEntry.getOlatResource(),  "114840998525891", owner.getIdentity());

		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(msg.getKey(), vo.getKey());
		conn.shutdown();
	}

	@Test
	public void getMessageCourseAsNotMember()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-3");
		IdentityWithLogin learner = JunitTestHelper.createAndPersistRndUser("info-rest-4");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "114840998525891", owner.getIdentity());

		RestConnection conn = new RestConnection(learner);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void getMessageCourseAsAuthorNotOwner()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-5");
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("info-rest-6");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "114840998525891", owner.getIdentity());

		RestConnection conn = new RestConnection(author);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	/**
	 * The REST API is restricted to the managers of the resource,
	 * participants cannot read the messages.
	 */
	@Test
	public void getMessageCourseAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-7");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-8");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "114840998525891", owner.getIdentity(), pastDate());
		
		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(msg.getKey(), vo.getKey());
		conn.shutdown();
	}
	
	@Test
	public void getMessageCourseAsParticipantButNoCourseNode()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-7b");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-8b");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "10001", owner.getIdentity(), pastDate());
		
		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		conn.shutdown();
	}
	
	/**
	 * Participants cannot read the messages of a course in preparation.
	 */
	@Test
	public void getMessageCourseInPreparationAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-35");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-36");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.preparation);
		repositoryEntryRelationDao.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "114840998525891", owner.getIdentity(), pastDate());
		
		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}
	
	@Test
	public void getMessageCourseAsCoach()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-25");
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-26");
		RepositoryEntry courseEntry = deployInfoMessagesCourse(owner.getIdentity(), RepositoryEntryStatusEnum.published);
		repositoryEntryRelationDao.addRole(coach.getIdentity(), courseEntry, GroupRoles.coach.name());
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), "114840998525891", owner.getIdentity(), pastDate());
		
		RestConnection conn = new RestConnection(coach);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}
	
	/**
	 * A message with a publish date in the future is only
	 * visible for the managers of the resource.
	 */
	@Test
	public void getMessageCourseFuturePublishDateAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-27");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-28");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		repositoryEntryRelationDao.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), owner.getIdentity(), futureDate());
		
		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}
	
	@Test
	public void getMessageCourseFuturePublishDateAsOwner()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-29");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		InfoMessage msg = createMessage(courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), owner.getIdentity(), futureDate());
		
		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void getMessageUnknownKey()
	throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpResponse<InputStream> response = getMessage(conn, Long.valueOf(-23l));
		Assert.assertEquals(404, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void createMessageCourseAsOwner()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-9");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = putMessage(conn, courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), null);
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertNotNull(vo.getKey());
		Assert.assertEquals(owner.getKey(), vo.getAuthorKey());
		conn.shutdown();

		InfoMessage msg = infoMessageManager.loadInfoMessage(vo.getKey());
		Assert.assertEquals(courseEntry.getOlatResource().getResourceableId(), msg.getResId());
		Assert.assertEquals(owner.getIdentity(), msg.getAuthor());
	}

	/**
	 * The authorKey is ignored for non-administrators, the author
	 * is the authenticated user.
	 */
	@Test
	public void createMessageCourseAsOwnerWithOtherAuthor()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-10");
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(owner);
		HttpResponse<InputStream> response = putMessage(conn, courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), admin.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(owner.getKey(), vo.getAuthorKey());
		conn.shutdown();
	}

	@Test
	public void createMessageCourseAsAdministratorWithOtherAuthor()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-11");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpResponse<InputStream> response = putMessage(conn, courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), owner.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(owner.getKey(), vo.getAuthorKey());
		conn.shutdown();
	}

	@Test
	public void createMessageCourseAsAuthorNotOwner()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-12");
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("info-rest-13");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(author);
		HttpResponse<InputStream> response = putMessage(conn, courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), null);
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();

		int count = infoMessageManager.countInfoMessageByResource(courseEntry.getOlatResource(), null, null, null, null);
		Assert.assertEquals(0, count);
	}

	@Test
	public void createMessageCourseAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("info-rest-14");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-15");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		repositoryEntryRelationDao.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = putMessage(conn, courseEntry.getOlatResource(), Long.toString(CodeHelper.getRAMUniqueID()), null);
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void createMessageMissingResource() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("info-rest-34");
		
		RestConnection conn = new RestConnection(author);
		URI uri = UriBuilder.fromUri(getContextURI()).path("infomessages")
				.queryParam("resName", "CourseModule")
				.queryParam("title", "Hello")
				.queryParam("message", "World")
				.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(400, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}
	
	@Test
	public void createMessageUnknownResource() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("info-rest-16");

		RestConnection conn = new RestConnection(author);
		URI uri = UriBuilder.fromUri(getContextURI()).path("infomessages")
				.queryParam("resName", "UnknownResource")
				.queryParam("resId", "23")
				.queryParam("title", "Hello")
				.queryParam("message", "World")
				.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void getMessageBusinessGroupAsCoach()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-17");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		InfoMessage msg = createMessage(group, "", coach.getIdentity());

		RestConnection conn = new RestConnection(coach);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(msg.getKey(), vo.getKey());
		conn.shutdown();
	}

	@Test
	public void getMessageBusinessGroupAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-18");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-19");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		businessGroupRelationDao.addRole(participant.getIdentity(), group, GroupRoles.participant.name());
		InfoMessage msg = createMessage(group, "", coach.getIdentity(), pastDate());
		
		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(msg.getKey(), vo.getKey());
		conn.shutdown();
	}
	
	@Test
	public void getMessageBusinessGroupFuturePublishDateAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-30");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-31");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		businessGroupRelationDao.addRole(participant.getIdentity(), group, GroupRoles.participant.name());
		InfoMessage msg = createMessage(group, "", coach.getIdentity(), futureDate());
		
		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}
	
	@Test
	public void getMessageBusinessGroupAsWaiting()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-32");
		IdentityWithLogin waiting = JunitTestHelper.createAndPersistRndUser("info-rest-33");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		businessGroupRelationDao.addRole(waiting.getIdentity(), group, GroupRoles.waiting.name());
		InfoMessage msg = createMessage(group, "", coach.getIdentity(), pastDate());
		
		RestConnection conn = new RestConnection(waiting);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void getMessageBusinessGroupAsNotMember()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-20");
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("info-rest-21");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		InfoMessage msg = createMessage(group, "", coach.getIdentity());

		RestConnection conn = new RestConnection(author);
		HttpResponse<InputStream> response = getMessage(conn, msg.getKey());
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();
	}

	@Test
	public void createMessageBusinessGroupAsCoach()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-22");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(coach);
		HttpResponse<InputStream> response = putMessage(conn, group, "", null);
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(coach.getKey(), vo.getAuthorKey());
		conn.shutdown();
	}

	@Test
	public void createMessageBusinessGroupAsParticipant()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("info-rest-23");
		BusinessGroup group = createGroup(participant.getIdentity(), GroupRoles.participant);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(participant);
		HttpResponse<InputStream> response = putMessage(conn, group, null, null);
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.shutdown();

		int count = infoMessageManager.countInfoMessageByResource(group, null, null, null, null);
		Assert.assertEquals(0, count);
	}

	@Test
	public void createMessageBusinessGroupAsAdministrator()
	throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin coach = JunitTestHelper.createAndPersistRndUser("info-rest-24");
		BusinessGroup group = createGroup(coach.getIdentity(), GroupRoles.coach);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		HttpResponse<InputStream> response = putMessage(conn, group, "", coach.getKey());
		Assert.assertEquals(200, response.statusCode());
		InfoMessageVO vo = conn.parse(response, InfoMessageVO.class);
		Assert.assertEquals(coach.getKey(), vo.getAuthorKey());
		conn.shutdown();
	}
	
	private RepositoryEntry deployInfoMessagesCourse(Identity owner, RepositoryEntryStatusEnum status) {
		Organisation organisation = JunitTestHelper.getDefaultOrganisation();
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/course_with_info_messages.zip");
		return JunitTestHelper.deployCourse(owner, "Infos messages", status, courseUrl, organisation);
	}

	private BusinessGroup createGroup(Identity member, GroupRoles role) {
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "info-rest-group", null,
				BusinessGroup.BUSINESS_TYPE, -1, -1, false, false, null);
		businessGroupRelationDao.addRole(member, group, role.name());
		dbInstance.commitAndCloseSession();
		return group;
	}

	private InfoMessage createMessage(OLATResourceable ores, String resSubPath, Identity author) {
		return createMessage(ores, resSubPath, author, pastDate());
	}
	
	private InfoMessage createMessage(OLATResourceable ores, String resSubPath, Identity author, Date publishDate) {
		InfoMessage msg = infoMessageManager.createInfoMessage(ores, resSubPath, null, author);
		msg.setTitle("Info REST");
		msg.setMessage("Info message for REST");
		msg.setPublishDate(publishDate);
		msg.setPublished(publishDate.before(new Date()));
		msg = infoMessageManager.saveInfoMessageAndNotify(msg);
		dbInstance.commitAndCloseSession();
		return msg;
	}

	private static Date pastDate() {
		return DateUtils.addHours(new Date(), -1);
	}
	
	private static Date futureDate() {
		return DateUtils.addDays(new Date(), 2);
	}
	
	private HttpResponse<InputStream> getMessage(RestConnection conn, Long messageKey)
	throws IOException, URISyntaxException, InterruptedException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("infomessages").path(messageKey.toString()).build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		return conn.execute(method);
	}

	private HttpResponse<InputStream> putMessage(RestConnection conn, OLATResourceable ores, String resSubPath, Long authorKey)
	throws IOException, URISyntaxException, InterruptedException {
		UriBuilder builder = UriBuilder.fromUri(getContextURI()).path("infomessages")
				.queryParam("resName", ores.getResourceableTypeName())
				.queryParam("resId", ores.getResourceableId().toString())
				.queryParam("title", "REST title")
				.queryParam("message", "REST message");
		if(resSubPath != null) {
			builder = builder.queryParam("resSubPath", resSubPath);
		}
		if(authorKey != null) {
			builder = builder.queryParam("authorKey", authorKey.toString());
		}
		HttpRequest method = conn.createPut(builder.build(), MediaType.APPLICATION_JSON);
		return conn.execute(method);
	}
}
