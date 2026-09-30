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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.restapi.LectureBlockVO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 13 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlocksRootTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	/**
	 * Only administrator and lecture managers have access to this REST API
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_administrator()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-all");
		
		RepositoryEntry entry = deployCourseWithLecturesEnabled(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, author);
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		Assert.assertFalse(voList.isEmpty());
		
		LectureBlockVO lectureBlockVo = null;
		for(LectureBlockVO vo:voList) {
			if(vo.getKey().equals(block.getKey())) {
				lectureBlockVo = vo;
			}
		}
		
		Assert.assertNotNull(lectureBlockVo);
		Assert.assertEquals(block.getKey(), lectureBlockVo.getKey());
		Assert.assertEquals(entry.getKey(), lectureBlockVo.getRepoEntryKey());
	}
	
	/**
	 * Only administrator and lecture managers have access to this REST API
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_permissionDenied()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-all");
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndAuthor("lect-root-hacker");
		
		RepositoryEntry entry = deployCourseWithLecturesEnabled(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, author);
		dbInstance.commit();

		RestConnection conn = new RestConnection(user);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());
	}
	
	@Test
	public void getLecturesBlock_date()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-1");
		RepositoryEntry entry = deployCourseWithLecturesEnabled(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, author);
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");

		String date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").format(new Date());
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").queryParam("date", date).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		Assert.assertFalse(voList.isEmpty());
		
		LectureBlockVO lectureBlockVo = null;
		for(LectureBlockVO vo:voList) {
			if(vo.getKey().equals(block.getKey())) {
				lectureBlockVo = vo;
			}
		}
		
		Assert.assertNotNull(lectureBlockVo);
		Assert.assertEquals(block.getKey(), lectureBlockVo.getKey());
		Assert.assertEquals(entry.getKey(), lectureBlockVo.getRepoEntryKey());
	}
	
	/**
	 * OO-9734, finding 2: a lecture block on a curriculum element without a course must be returned
	 * too, not just lecture blocks tied to a repository entry.
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_curriculumElementWithoutCourse()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("lect-root-cur", "Lecture root curriculum REST", "",
				false, JunitTestHelper.getDefaultOrganisation());
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement("lect-root-cur-el",
				"Lecture root curriculum element", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		LectureBlock block = createLectureBlock(curriculumElement);
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);

		LectureBlockVO lectureBlockVo = null;
		for(LectureBlockVO vo:voList) {
			if(vo.getKey().equals(block.getKey())) {
				lectureBlockVo = vo;
			}
		}

		Assert.assertNotNull("Lecture block on a curriculum element without a course must be returned", lectureBlockVo);
		Assert.assertEquals(block.getKey(), lectureBlockVo.getKey());
		Assert.assertEquals(curriculumElement.getKey(), lectureBlockVo.getCurriculumElementKey());
	}

	/**
	 * A lecture manager sees the lecture blocks of the curricula of his organisation.
	 */
	@Test
	public void getLecturesBlock_lectureManager_curriculumOrganisation()
	throws IOException, URISyntaxException {
		Organisation organisation = createOrganisation(null);
		LectureBlock block = createLectureBlock(createCurriculumElement(organisation));
		IdentityWithLogin manager = createLectureManager(organisation, GroupMembershipInheritance.none);

		List<Long> keys = getLectureBlockKeys(new RestConnection(manager), null);
		Assert.assertTrue(keys.contains(block.getKey()));
	}

	/**
	 * A lecture manager doesn't see the lecture blocks of the curricula of other organisations.
	 */
	@Test
	public void getLecturesBlock_lectureManager_otherOrganisation()
	throws IOException, URISyntaxException {
		Organisation organisation = createOrganisation(null);
		Organisation otherOrganisation = createOrganisation(null);
		LectureBlock block = createLectureBlock(createCurriculumElement(organisation));
		IdentityWithLogin manager = createLectureManager(otherOrganisation, GroupMembershipInheritance.root);

		List<Long> keys = getLectureBlockKeys(new RestConnection(manager), null);
		Assert.assertFalse(keys.contains(block.getKey()));
	}

	/**
	 * A lecture manager of the parent organisation sees the lecture blocks of the curricula
	 * of the child organisation.
	 */
	@Test
	public void getLecturesBlock_lectureManager_parentOrganisation()
	throws IOException, URISyntaxException {
		Organisation parentOrganisation = createOrganisation(null);
		Organisation childOrganisation = createOrganisation(parentOrganisation);
		LectureBlock block = createLectureBlock(createCurriculumElement(childOrganisation));
		IdentityWithLogin manager = createLectureManager(parentOrganisation, GroupMembershipInheritance.root);

		List<Long> keys = getLectureBlockKeys(new RestConnection(manager), null);
		Assert.assertTrue(keys.contains(block.getKey()));
	}

	/**
	 * The lecture blocks of a course with lectures disabled are not returned.
	 */
	@Test
	public void getLecturesBlock_courseWithLecturesDisabled()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-dis");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		List<Long> keys = getLectureBlockKeys(new RestConnection("administrator", "openolat"), null);
		Assert.assertFalse(keys.contains(block.getKey()));
	}

	/**
	 * The lecture blocks of a deleted curriculum element are not returned.
	 */
	@Test
	public void getLecturesBlock_deletedCurriculumElement()
	throws IOException, URISyntaxException {
		CurriculumElement element = createCurriculumElement(JunitTestHelper.getDefaultOrganisation());
		LectureBlock block = createLectureBlock(element);
		curriculumService.updateCurriculumElementStatus(JunitTestHelper.getDefaultActor(), element,
				CurriculumElementStatus.deleted, false, null);
		dbInstance.commitAndCloseSession();

		List<Long> keys = getLectureBlockKeys(new RestConnection("administrator", "openolat"), null);
		Assert.assertFalse(keys.contains(block.getKey()));
	}

	/**
	 * The date parameter accepts an ISO date without time.
	 */
	@Test
	public void getLecturesBlock_isoDate()
	throws IOException, URISyntaxException {
		LectureBlock block = createLectureBlock(createCurriculumElement(JunitTestHelper.getDefaultOrganisation()));
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		List<Long> keys = getLectureBlockKeys(conn, LocalDate.now().toString());
		Assert.assertTrue(keys.contains(block.getKey()));

		List<Long> keysOtherDay = getLectureBlockKeys(conn, LocalDate.now().plusDays(3).toString());
		Assert.assertFalse(keysOtherDay.contains(block.getKey()));
	}

	private List<Long> getLectureBlockKeys(RestConnection conn, String date)
	throws IOException, URISyntaxException {
		UriBuilder builder = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures");
		if(date != null) {
			builder = builder.queryParam("date", date);
		}
		HttpGet method = conn.createGet(builder.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		return voList.stream().map(LectureBlockVO::getKey).toList();
	}

	private Organisation createOrganisation(Organisation parent) {
		String identifier = "lect-root-org-" + UUID.randomUUID();
		Organisation organisation = organisationService.createOrganisation(identifier, identifier, null, parent, null,
				JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		return organisation;
	}

	private IdentityWithLogin createLectureManager(Organisation organisation, GroupMembershipInheritance inheritance) {
		IdentityWithLogin manager = JunitTestHelper.createAndPersistRndUser("lect-root-mgr");
		organisationService.addMember(organisation, manager.getIdentity(), OrganisationRoles.lecturemanager, inheritance,
				JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		return manager;
	}

	private CurriculumElement createCurriculumElement(Organisation organisation) {
		Curriculum curriculum = curriculumService.createCurriculum("lect-root-cur", "Lecture root curriculum", "",
				false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("lect-root-cur-el",
				"Lecture root curriculum element", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.enabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commitAndCloseSession();
		return element;
	}

	private RepositoryEntry deployCourseWithLecturesEnabled(Identity author) {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);
		dbInstance.commit();
		return entry;
	}
	
	private LectureBlock createLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
	private LectureBlock createLectureBlock(CurriculumElement curriculumElement) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(curriculumElement, null);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello curriculum lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}

	protected List<LectureBlockVO> parseLectureBlockArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<LectureBlockVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
