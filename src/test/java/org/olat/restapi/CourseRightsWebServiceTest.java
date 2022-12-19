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


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.olat.restapi.repository.course.CourseRightsWebService.geRightsListWithRole;
import static org.olat.restapi.repository.course.CourseRightsWebService.getRightsVOWithRole;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRights;
import org.olat.group.right.BGRightsRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.RightsVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 19 déc. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseRightsWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CourseRightsWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	/**
	 * Load the rights link to coaches and participants of the course without any permissions set.
	 */
	@Test
	public void getRightsByCourseEmpty()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rights-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an contact node
		URI rightsUri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(entry.getOlatResource().getResourceableId().toString())
				.path("rights").path("course")
				.build();

		HttpGet method = conn.createGet(rightsUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		List<RightsVO> rights = parseUserArray(response.getEntity());
		assertThat(rights)
			.isNotNull()
			.hasSize(2)
			.map(RightsVO::getRole)
			.containsExactlyInAnyOrder(BGRightsRole.tutor.name(), BGRightsRole.participant.name());
		
		RightsVO coachRights = getRightsVOWithRole(BGRightsRole.tutor, rights);
		Assert.assertNotNull(coachRights);
		Assert.assertNotNull(coachRights.getRights());
		Assert.assertTrue(coachRights.getRights().isEmpty());
		
		RightsVO participantsRights = getRightsVOWithRole(BGRightsRole.participant, rights);
		Assert.assertNotNull(participantsRights);
		Assert.assertNotNull(participantsRights.getRights());
		Assert.assertTrue(participantsRights.getRights().isEmpty());
		
		conn.shutdown();
	}
	
	/**
	 * Load the rights link to coaches and participants of the course with permissions.
	 */
	@Test
	public void getRightsByCourse()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rights-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		Group defGroup = repositoryService.getDefaultGroup(entry);
		rightManager.addBGRight(CourseRights.RIGHT_COURSEEDITOR, defGroup, entry.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, defGroup, entry.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_DB, defGroup, entry.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an contact node
		URI rightsUri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(entry.getOlatResource().getResourceableId().toString())
				.path("rights").path("course")
				.build();

		HttpGet method = conn.createGet(rightsUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RightsVO> rights = parseUserArray(response.getEntity());
		assertThat(rights)
			.isNotNull()
			.hasSize(2)
			.map(RightsVO::getRole)
			.containsExactlyInAnyOrder(BGRightsRole.tutor.name(), BGRightsRole.participant.name());
		
		RightsVO coachRights = getRightsVOWithRole(BGRightsRole.tutor, rights);
		assertThat(coachRights.getRights())
			.isNotNull()
			.hasSize(1)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_COURSEEDITOR);
		
		RightsVO participantsRights = getRightsVOWithRole(BGRightsRole.participant, rights);
		assertThat(participantsRights.getRights())
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_ARCHIVING, CourseRights.RIGHT_DB);

		conn.shutdown();
	}
	
	/**
	 * Update the rights link to coaches and participants of the course (at course level).
	 */
	@Test
	public void addRightsByCourse()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rights-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		Group defGroup = repositoryService.getDefaultGroup(entry);
		rightManager.addBGRight(CourseRights.RIGHT_DB, defGroup, entry.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, defGroup, entry.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_DB, defGroup, entry.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		RightsVO newCoachRights = new RightsVO();
		newCoachRights.setRole(BGRightsRole.tutor.name());
		newCoachRights.setRights(List.of(CourseRights.RIGHT_ASSESSMENT, CourseRights.RIGHT_ASSESSMENT_MODE));
		
		RightsVO newParticipantsRights = new RightsVO();
		newParticipantsRights.setRole(BGRightsRole.participant.name());
		newParticipantsRights.setRights(List.of(CourseRights.RIGHT_DB, CourseRights.RIGHT_GLOSSARY));

		//create an contact node
		URI rightsUri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(entry.getOlatResource().getResourceableId().toString())
				.path("rights").path("course")
				.build();

		HttpPost method = conn.createPost(rightsUri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, new RightsVO[] { newCoachRights, newParticipantsRights });

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// Check the database
		List<BGRights> currentRights = rightManager.findBGRights(List.of(defGroup), entry.getOlatResource());
		assertThat(currentRights)
			.isNotNull()
			.hasSize(2)
			.map(BGRights::getRole)
			.containsExactlyInAnyOrder(BGRightsRole.tutor, BGRightsRole.participant);
		
		List<String> coachRightsStrings = geRightsListWithRole(BGRightsRole.tutor, currentRights);
		assertThat(coachRightsStrings)
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_ASSESSMENT, CourseRights.RIGHT_ASSESSMENT_MODE);
		
		List<String> participantRightsStrings = geRightsListWithRole(BGRightsRole.participant, currentRights);
		assertThat(participantRightsStrings)
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_DB, CourseRights.RIGHT_GLOSSARY);

		conn.shutdown();
	}
	
	/**
	 * Load the rights link to coaches and participants of a course with business group permissions.
	 */
	@Test
	public void getRightsByBusinessGroup()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rights-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);

		BusinessGroup group1 = businessGroupService.createBusinessGroup(author, "Rights-3-1", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(author, "Rights-3-2", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		Assert.assertNotNull(group2);

		Group baseGroup1 = group1.getBaseGroup();
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, baseGroup1, entry.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight(CourseRights.RIGHT_ARCHIVING, baseGroup1, entry.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_DB, baseGroup1, entry.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an contact node
		URI rightsUri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(entry.getOlatResource().getResourceableId().toString())
				.path("rights").path("group").path(group1.getKey().toString())
				.build();

		HttpGet method = conn.createGet(rightsUri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RightsVO> rights = parseUserArray(response.getEntity());
		assertThat(rights)
			.isNotNull()
			.hasSize(2)
			.map(RightsVO::getRole)
			.containsExactlyInAnyOrder(BGRightsRole.tutor.name(), BGRightsRole.participant.name());
		
		RightsVO coachRights = getRightsVOWithRole(BGRightsRole.tutor, rights);
		assertThat(coachRights.getRights())
			.isNotNull()
			.hasSize(1)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_ARCHIVING);
		
		RightsVO participantsRights = getRightsVOWithRole(BGRightsRole.participant, rights);
		assertThat(participantsRights.getRights())
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_ARCHIVING, CourseRights.RIGHT_DB);

		conn.shutdown();
	}
	
	/**
	 * Update the rights link to coaches and participants of the course via business group.
	 */
	@Test
	public void updateRightsByBusinessGroup()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rights-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		BusinessGroup group1 = businessGroupService.createBusinessGroup(author, "Rights-4-1", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(author, "Rights-4-2", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry);
		Assert.assertNotNull(group2);

		Group baseGroup1 = group1.getBaseGroup();
		rightManager.addBGRight(CourseRights.RIGHT_ASSESSMENT, baseGroup1, entry.getOlatResource(), BGRightsRole.tutor);
		rightManager.addBGRight(CourseRights.RIGHT_GLOSSARY, baseGroup1, entry.getOlatResource(), BGRightsRole.participant);
		rightManager.addBGRight(CourseRights.RIGHT_DB, baseGroup1, entry.getOlatResource(), BGRightsRole.participant);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		RightsVO newCoachRights = new RightsVO();
		newCoachRights.setRole(BGRightsRole.tutor.name());
		newCoachRights.setRights(List.of(CourseRights.RIGHT_ASSESSMENT, CourseRights.RIGHT_ASSESSMENT_MODE));
		
		RightsVO newParticipantsRights = new RightsVO();
		newParticipantsRights.setRole(BGRightsRole.participant.name());
		newParticipantsRights.setRights(List.of(CourseRights.RIGHT_GROUPMANAGEMENT, CourseRights.RIGHT_MEMBERMANAGEMENT));

		//create an contact node
		URI rightsUri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(entry.getOlatResource().getResourceableId().toString())
				.path("rights").path("group").path(group1.getKey().toString())
				.build();

		HttpPost method = conn.createPost(rightsUri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, new RightsVO[] { newCoachRights, newParticipantsRights });

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// Check the database
		List<BGRights> currentRights = rightManager.findBGRights(List.of(group1.getBaseGroup()), entry.getOlatResource());
		assertThat(currentRights)
			.isNotNull()
			.hasSize(2)
			.map(BGRights::getRole)
			.containsExactlyInAnyOrder(BGRightsRole.tutor, BGRightsRole.participant);
		
		List<String> coachRightsStrings = geRightsListWithRole(BGRightsRole.tutor, currentRights);
		assertThat(coachRightsStrings)
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_ASSESSMENT, CourseRights.RIGHT_ASSESSMENT_MODE);
		
		List<String> participantRightsStrings = geRightsListWithRole(BGRightsRole.participant, currentRights);
		assertThat(participantRightsStrings)
			.isNotNull()
			.hasSize(2)
			.containsExactlyInAnyOrder(CourseRights.RIGHT_GROUPMANAGEMENT, CourseRights.RIGHT_MEMBERMANAGEMENT);

		conn.shutdown();
	}
	
	/**
	 * Check that the endpoint returns a "Not found" if someone try to change
	 * the permission via a business group which is not in relation to the course.
	 */
	@Test
	public void updateRightsByWrongBusinessGroup()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rights-4");
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author);
		
		BusinessGroup group1 = businessGroupService.createBusinessGroup(author, "Rights-4-1", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry2);
		BusinessGroup group2 = businessGroupService.createBusinessGroup(author, "Rights-4-2", "", BusinessGroup.BUSINESS_TYPE,
				null, null, null, null, false, false, entry2);
		Assert.assertNotNull(group1);

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		RightsVO newCoachRights = new RightsVO();
		newCoachRights.setRole(BGRightsRole.tutor.name());
		newCoachRights.setRights(List.of(CourseRights.RIGHT_ASSESSMENT, CourseRights.RIGHT_ASSESSMENT_MODE));
		
		//create an contact node
		URI rightsUri = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(entry1.getOlatResource().getResourceableId().toString())
				.path("rights").path("group").path(group2.getKey().toString())
				.build();

		HttpPost method = conn.createPost(rightsUri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, new RightsVO[] { newCoachRights });

		HttpResponse response = conn.execute(method);
		EntityUtils.consume(response.getEntity());
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		
		conn.shutdown();
	}

	protected List<RightsVO> parseUserArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RightsVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
