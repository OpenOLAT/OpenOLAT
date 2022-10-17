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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumManagedFlag;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.restapi.CurriculumElementVO;
import org.olat.modules.curriculum.restapi.CurriculumVO;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.manager.LectureBlockRollCallDAO;
import org.olat.modules.lecture.manager.LectureParticipantSummaryDAO;
import org.olat.modules.lecture.manager.RepositoryEntryLectureConfigurationDAO;
import org.olat.modules.lecture.restapi.LectureBlockStatisticsVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumsWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumsWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO repositoryEntryLectureConfigurationDao;
	
	@Test
	public void getCurriculums()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum", "REST Curriculum", "A curriculum accessible by REST API", false, organisation);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumVO> curriculumVoes = parseCurriculumArray(response.getEntity());
		
		CurriculumVO foundVo = null;
		for(CurriculumVO curriculumVo:curriculumVoes) {
			if(curriculumVo.getKey().equals(curriculum.getKey())) {
				foundVo = curriculumVo;
			}
		}
		Assert.assertNotNull(foundVo);
	}
	
	@Test
	public void getCurriculum()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-2-Curriculum", "REST 2 Curriculum", "A curriculum accessible by REST API", false, organisation);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		CurriculumVO curriculumVo = conn.parse(response, CurriculumVO.class);
		Assert.assertNotNull(curriculumVo);
		Assert.assertEquals(curriculum.getKey(), curriculumVo.getKey());
		Assert.assertEquals("REST-2-Curriculum", curriculumVo.getIdentifier());
		Assert.assertEquals(organisation.getKey(), curriculumVo.getOrganisationKey());
	}
	
	@Test
	public void createCurriculum()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation", "REST-p-organisation", "", defOrganisation, null);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CurriculumVO vo = new CurriculumVO();
		vo.setDescription("REST created curriculum");
		vo.setDisplayName("REST Curriculum");
		vo.setExternalId("REST-CUR-1");
		vo.setIdentifier("REST-CUR-ID-1");
		vo.setManagedFlagsString("delete");
		vo.setDegree("High degree");
		vo.setOrganisationKey(organisation.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		CurriculumVO savedVo = conn.parse(response, CurriculumVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("REST created curriculum", savedVo.getDescription());
		Assert.assertEquals("REST Curriculum", savedVo.getDisplayName());
		Assert.assertEquals("REST-CUR-1", savedVo.getExternalId());
		Assert.assertEquals("REST-CUR-ID-1", savedVo.getIdentifier());
		Assert.assertEquals("delete", savedVo.getManagedFlagsString());
		Assert.assertEquals("High degree", savedVo.getDegree());
		Assert.assertEquals(organisation.getKey(), savedVo.getOrganisationKey());
		
		// checked database
		Curriculum savedCurriculum = curriculumService.getCurriculum(new CurriculumRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedCurriculum);
		Assert.assertEquals(savedVo.getKey(), savedCurriculum.getKey());
		Assert.assertEquals("REST created curriculum", savedCurriculum.getDescription());
		Assert.assertEquals("REST Curriculum", savedCurriculum.getDisplayName());
		Assert.assertEquals("REST-CUR-1", savedCurriculum.getExternalId());
		Assert.assertEquals("REST-CUR-ID-1", savedCurriculum.getIdentifier());
		Assert.assertEquals("High degree", savedCurriculum.getDegree());
		Assert.assertNotNull(savedCurriculum.getManagedFlags());
		Assert.assertEquals(1, savedCurriculum.getManagedFlags().length);
		Assert.assertEquals(CurriculumManagedFlag.delete, savedCurriculum.getManagedFlags()[0]);
		Assert.assertEquals(organisation, savedCurriculum.getOrganisation());
	}
	
	@Test
	public void updateCurriculum()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 3", "REST-p-3-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-4-Curriculum", "REST 4 Curriculum", "A curriculum accessible by REST API", false, organisation);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CurriculumVO vo = new CurriculumVO();
		vo.setKey(curriculum.getKey());
		vo.setDescription("Via REST updated curriculum");
		vo.setDisplayName("REST updated curriculum");
		vo.setDegree("Diploma");
		vo.setExternalId("REST4b");
		vo.setIdentifier("REST-ID-4b");
		vo.setManagedFlagsString("delete,all");
		vo.setOrganisationKey(organisation.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumVO savedVo = conn.parse(response, CurriculumVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertEquals(curriculum.getKey(), savedVo.getKey());
		Assert.assertEquals("Via REST updated curriculum", savedVo.getDescription());
		Assert.assertEquals("REST updated curriculum", savedVo.getDisplayName());
		Assert.assertEquals("REST4b", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-4b", savedVo.getIdentifier());
		Assert.assertEquals("delete,all", savedVo.getManagedFlagsString());
		Assert.assertEquals(organisation.getKey(), savedVo.getOrganisationKey());
		
		// checked database
		Curriculum savedCurriculum = curriculumService.getCurriculum(new CurriculumRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedCurriculum);
		Assert.assertEquals(savedVo.getKey(), savedCurriculum.getKey());
		Assert.assertEquals("Via REST updated curriculum", savedCurriculum.getDescription());
		Assert.assertEquals("REST updated curriculum", savedCurriculum.getDisplayName());
		Assert.assertEquals("REST4b", savedCurriculum.getExternalId());
		Assert.assertEquals("REST-ID-4b", savedCurriculum.getIdentifier());
		Assert.assertEquals("Diploma", savedCurriculum.getDegree());
		Assert.assertNotNull(savedCurriculum.getManagedFlags());
		Assert.assertEquals(2, savedCurriculum.getManagedFlags().length);
		Assert.assertEquals(organisation, savedCurriculum.getOrganisation());
	}
	
	@Test
	public void deleteCurriculum()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Del-Curriculum", "REST del Curriculum", "A curriculum deleted by REST API", false, defOrganisation);
		Curriculum notDeletedCurriculum = curriculumService.createCurriculum("REST-Not-Me-Curriculum", "REST don't delete me", "A curriculum by REST API", false, defOrganisation);
		dbInstance.commit();
		CurriculumElement element = curriculumService.createCurriculumElement("by-key", "Element will be deleted",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		Curriculum deletedCurriculum = curriculumService.getCurriculum(curriculum);
		Assert.assertNull(deletedCurriculum);
		CurriculumElement deletedElement = curriculumService.getCurriculumElement(element);
		Assert.assertNull(deletedElement);
		Curriculum survivingCurriculum = curriculumService.getCurriculum(notDeletedCurriculum);
		Assert.assertNotNull(survivingCurriculum);
	}
	
	@Test
	public void createCurriculum_notAuthorized()
	throws IOException, URISyntaxException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("rest-curriculum");
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(author));
		
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation", "REST-p-organisation", "", null, null);
		dbInstance.commitAndCloseSession();
		
		CurriculumVO vo = new CurriculumVO();
		vo.setDescription("Try to create curriculum");
		vo.setDisplayName("Authored Curriculum");
		vo.setIdentifier("AUTH-CUR-ID-1");
		vo.setOrganisationKey(organisation.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	@Test
	public void updateCurriculum_authorizedOrNot()
	throws IOException, URISyntaxException {
		IdentityWithLogin curriculumManager = JunitTestHelper.createAndPersistRndUser("rest-curriculum");
		Organisation parentOrganisation = organisationService.createOrganisation("Root curriculum organisation", "REST-curl-organisation", "", null, null);
		Organisation organisationA = organisationService.createOrganisation("Organisation A", "REST-A-organisation", "", parentOrganisation, null);
		Organisation organisationB = organisationService.createOrganisation("Organisation B", "REST-B-organisation", "", parentOrganisation, null);
		organisationService.addMember(organisationB, curriculumManager.getIdentity(), OrganisationRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		//create 2 curriculums
		Curriculum curriculumA = curriculumService.createCurriculum("REST-A-Curriculum", "REST A Curriculum", "A curriculum accessible by REST API", false, organisationA);
		Curriculum curriculumB = curriculumService.createCurriculum("REST-B-Curriculum", "REST B Curriculum", "A curriculum accessible by REST API", false, organisationB);
		dbInstance.commitAndCloseSession();
		
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(curriculumManager));

		CurriculumVO voA = CurriculumVO.valueOf(curriculumA);
		voA.setIdentifier("Take control A");
		// it cannot change something in organization A
		URI requestA = UriBuilder.fromUri(getContextURI()).path("curriculum").build();
		HttpPost methodA = conn.createPost(requestA, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(methodA, voA);
		HttpResponse responsea = conn.execute(methodA);
		Assert.assertEquals(403, responsea.getStatusLine().getStatusCode());
		EntityUtils.consume(responsea.getEntity());
		
		// but it can update a curriculum in organization B
		CurriculumVO voB = CurriculumVO.valueOf(curriculumB);
		voB.setIdentifier("Update B");
		// it cannot change something in organization A
		URI requestB = UriBuilder.fromUri(getContextURI()).path("curriculum").build();
		HttpPost methodB = conn.createPost(requestB, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(methodB, voB);
		HttpResponse responseB = conn.execute(methodB);
		Assert.assertEquals(200, responseB.getStatusLine().getStatusCode());
		// check the updated curriculum
		CurriculumVO updatedVoB = conn.parse(responseB, CurriculumVO.class);
		Assert.assertNotNull(updatedVoB);
		Assert.assertEquals(curriculumB.getKey(), updatedVoB.getKey());
		Assert.assertEquals("Update B", updatedVoB.getIdentifier());
	}
	
	@Test
	public void searchCurriculumElements_externalId()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Unkown", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		String externalId = UUID.randomUUID().toString();
		element.setExternalId(externalId);
		element = curriculumService.updateCurriculumElement(element);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// it cannot change something in organization A
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum/elements").queryParam("externalId", externalId).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementVO> elements = this.parseCurriculumElementArray(response.getEntity());
		Assert.assertNotNull(elements);
		Assert.assertEquals(1, elements.size());
		Assert.assertEquals(element.getKey(), elements.get(0).getKey());
	}
	
	@Test
	public void searchCurriculumElements_identifier()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		String identifier = UUID.randomUUID().toString();
		CurriculumElement element = curriculumService.createCurriculumElement(identifier, "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// it cannot change something in organization A
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum/elements").queryParam("identifier", identifier).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementVO> elements = this.parseCurriculumElementArray(response.getEntity());
		Assert.assertNotNull(elements);
		Assert.assertEquals(1, elements.size());
		Assert.assertEquals(element.getKey(), elements.get(0).getKey());
	}
	
	@Test
	public void searchCurriculumElements_elementKey()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("by-key", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// it cannot change something in organization A
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum/elements").queryParam("key", element.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementVO> elements = this.parseCurriculumElementArray(response.getEntity());
		Assert.assertNotNull(elements);
		Assert.assertEquals(1, elements.size());
		Assert.assertEquals(element.getKey(), elements.get(0).getKey());
	}
	
	@Test
	public void getCurriculumOwners()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		dbInstance.commit();
		
		curriculumService.addMember(curriculum, member, CurriculumRoles.curriculumowner);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("curriculumowners").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void addCurriculumOwner()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-11");
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		dbInstance.commit();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("curriculumowners").path(owner.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> managers = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.curriculumowner);
		Assert.assertNotNull(managers);
		Assert.assertEquals(1, managers.size());
		Assert.assertEquals(owner, managers.get(0));
	}
	
	@Test
	public void removeCurriculumManager()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-23");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-24");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		dbInstance.commit();
		
		curriculumService.addMember(curriculum, owner, CurriculumRoles.curriculumowner);
		curriculumService.addMember(curriculum, coach, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("curriculumowners").path(owner.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> owners = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.curriculumowner);
		Assert.assertTrue(owners.isEmpty());
		List<Identity> coaches = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.coach);
		Assert.assertEquals(1, coaches.size());
		
		conn.shutdown();
	}
	
	@Test
	public void getLecturesStatistics()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// Curriculum with 2 courses
		Organisation defOrg = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("cur-lect-1", "Curriculum for lectures", "Curriculum", false, defOrg);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-lect-1",
				"Element for lectures", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-lect-2",
				"Element for lectures", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-lect-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-lect-2");
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-lect-auth");
		RepositoryEntry entryLecture1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entryLecture2 = JunitTestHelper.createRandomRepositoryEntry(author);
		
		// Configuration with lectures enabled 
		RepositoryEntryLectureConfiguration lectureConfig1 = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture1);
		lectureConfig1.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig1);
		
		RepositoryEntryLectureConfiguration lectureConfig2 = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture2);
		lectureConfig2.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig2);
		dbInstance.commit();
		
		entryLecture1.setEntryStatus(RepositoryEntryStatusEnum.published);
		entryLecture1 = repositoryService.update(entryLecture1);
		entryLecture2.setEntryStatus(RepositoryEntryStatusEnum.published);
		entryLecture2 = repositoryService.update(entryLecture2);
		
		curriculumService.addRepositoryEntry(element1, entryLecture1, false);
		curriculumService.addRepositoryEntry(element2, entryLecture2, false);
		curriculumService.addMember(element1, participant1, CurriculumRoles.participant);
		curriculumService.addMember(element1, participant2, CurriculumRoles.participant);
		curriculumService.addMember(element2, participant2, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		lectureParticipantSummaryDao.createSummary(entryLecture1, participant1, DateUtils.addDays(new Date(), -2));
		lectureParticipantSummaryDao.createSummary(entryLecture1, participant2, DateUtils.addDays(new Date(), -2));
		lectureParticipantSummaryDao.createSummary(entryLecture2, participant2, DateUtils.addDays(new Date(), -2));
		
		LectureBlock lectureBlock1_1 = createLectureBlock(entryLecture1, element1);
		LectureBlock lectureBlock1_2 = createLectureBlock(entryLecture1, element1);
		LectureBlock lectureBlock2_1 = createLectureBlock(entryLecture2, element2);
		LectureBlock lectureBlock2_2 = createLectureBlock(entryLecture2, element2);
		
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock1_1, participant1,
				null, null, null, null, null, List.of(1));
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock1_1, participant2,
				null, null, null, null, null, Collections.emptyList());
		
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock1_2, participant1,
				null, null, null, null, null, Collections.emptyList());
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock1_2, participant2,
				null, null, null, null, null, List.of(1, 2, 3));
		
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock2_1, participant2,
				null, null, null, null, null, Collections.emptyList());
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock2_2, participant2,
				null, null, null, null, null, List.of(1, 2));
		
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("lectures").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<LectureBlockStatisticsVO> statisticsVoes = parseLectureBlockStatisticsArray(response.getEntity());
		Assert.assertNotNull(statisticsVoes);
		Assert.assertEquals(2, statisticsVoes.size());
		
		// Check statistics participant 1
		LectureBlockStatisticsVO stats1 = statisticsVoes.stream()
				.filter(stats -> participant1.getKey().equals(stats.getIdentityKey()))
				.findFirst().orElse(null);
		Assert.assertNotNull(stats1);
		Assert.assertEquals(1l, stats1.getTotalAbsentLectures());
		Assert.assertEquals(7l, stats1.getTotalAttendedLectures());
		Assert.assertEquals(8l, stats1.getTotalEffectiveLectures());
		Assert.assertEquals(8l, stats1.getTotalPersonalPlannedLectures());

		// Check statistics participant 2
		LectureBlockStatisticsVO stats2 = statisticsVoes.stream()
				.filter(stats -> participant2.getKey().equals(stats.getIdentityKey()))
				.findFirst().orElse(null);
		Assert.assertNotNull(stats2);
		Assert.assertEquals(5l, stats2.getTotalAbsentLectures());
		Assert.assertEquals(11l, stats2.getTotalAttendedLectures());
		Assert.assertEquals(16l, stats2.getTotalEffectiveLectures());
		Assert.assertEquals(16l, stats2.getTotalPersonalPlannedLectures());
		
		conn.shutdown();
	}
	
	@Test
	public void getLecturesOfParticipant()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// curriculum with 2 courses
		Organisation defOrg = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("cur-lectures-2", "Curriculum for lectures", "Curriculum", false, defOrg);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-lectures",
				"Element for checked relation", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-lectures-part");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-lectures-auth");
		RepositoryEntry entryLecture1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entryLecture2 = JunitTestHelper.createRandomRepositoryEntry(author);
		
		// Configuration with lectures enabled 
		RepositoryEntryLectureConfiguration lectureConfig1 = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture1);
		lectureConfig1.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig1);
		
		RepositoryEntryLectureConfiguration lectureConfig2 = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture2);
		lectureConfig2.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig2);
		dbInstance.commit();
		
		entryLecture1.setEntryStatus(RepositoryEntryStatusEnum.published);
		entryLecture1 = repositoryService.update(entryLecture1);
		entryLecture2.setEntryStatus(RepositoryEntryStatusEnum.published);
		entryLecture2 = repositoryService.update(entryLecture2);
		
		curriculumService.addRepositoryEntry(element, entryLecture1, false);
		curriculumService.addRepositoryEntry(element, entryLecture2, false);
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		lectureParticipantSummaryDao.createSummary(entryLecture1, participant, DateUtils.addDays(new Date(), -2));
		lectureParticipantSummaryDao.createSummary(entryLecture2, participant, DateUtils.addDays(new Date(), -2));
		
		LectureBlock lectureBlock1_1 = createLectureBlock(entryLecture1, element);
		LectureBlock lectureBlock1_2 = createLectureBlock(entryLecture1, element);
		LectureBlock lectureBlock2_1 = createLectureBlock(entryLecture2, element);
		LectureBlock lectureBlock2_2 = createLectureBlock(entryLecture2, element);
		
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock1_1, participant,
				null, null, null, null, null, Collections.emptyList());
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock1_2, participant,
				null, null, null, null, null, Collections.emptyList());
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock2_1, participant,
				null, null, null, null, null, Collections.emptyList());
		lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock2_2, participant,
				null, null, null, null, null, List.of(1, 2));
		
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("lectures").path(participant.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		LectureBlockStatisticsVO statisticsVo = conn.parse(response.getEntity(), LectureBlockStatisticsVO.class);
		Assert.assertNotNull(statisticsVo);
		Assert.assertEquals(2l, statisticsVo.getTotalAbsentLectures());
		Assert.assertEquals(14l, statisticsVo.getTotalAttendedLectures());
		Assert.assertEquals(16l, statisticsVo.getTotalEffectiveLectures());
		Assert.assertEquals(16l, statisticsVo.getTotalPersonalPlannedLectures());
		
		conn.shutdown();
	}
	
	private LectureBlock createLectureBlock(RepositoryEntry entry, CurriculumElement element) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(DateUtils.addHours(lectureBlock.getCreationDate(), -2));
		lectureBlock.setEndDate(DateUtils.addHours(lectureBlock.getCreationDate(), -1));
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setEffectiveLecturesNumber(4);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlock.setStatus(LectureBlockStatus.done);
		lectureBlock = lectureBlockDao.update(lectureBlock);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, element.getGroup());
		return lectureBlock;
	}
	
	protected List<UserVO> parseUserArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<CurriculumVO> parseCurriculumArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<CurriculumVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	protected List<CurriculumElementVO> parseCurriculumElementArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<CurriculumElementVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<LectureBlockStatisticsVO> parseLectureBlockStatisticsArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<LectureBlockStatisticsVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
