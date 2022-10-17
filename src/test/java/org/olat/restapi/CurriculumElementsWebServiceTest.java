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
import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementToTaxonomyLevel;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.manager.CurriculumElementToTaxonomyLevelDAO;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.restapi.CurriculumElementMemberVO;
import org.olat.modules.curriculum.restapi.CurriculumElementVO;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelVO;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementsWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private OrganisationService organisationService;

	@Autowired
	private CurriculumElementToTaxonomyLevelDAO curriculumElementToTaxonomyLevelDao;
	
	@Test
	public void getCurriculumElements()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-1", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumService.createCurriculumElement("Element-1.1", "Element 1.1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementVO> elementVoes = parseCurriculumElementArray(response.getEntity());
		Assert.assertNotNull(elementVoes);
		Assert.assertEquals(2, elementVoes.size());
		
		boolean found1 = false;
		boolean found1_1 = false;
		for(CurriculumElementVO elementVo:elementVoes) {
			if(element1.getKey().equals(elementVo.getKey())) {
				found1 = true;
			} else if(element1_1.getKey().equals(elementVo.getKey())) {
				found1_1 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found1_1);
	}
	
	@Test
	public void getCurriculumElement()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		CurriculumElementVO elementVo = conn.parse(response, CurriculumElementVO.class);
		Assert.assertNotNull(elementVo);
		Assert.assertEquals(element.getKey(), elementVo.getKey());
	}
	
	@Test
	public void getCurriculumElementChildren()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element_1 = curriculumService.createCurriculumElement("Element-1.1", "Element 1.1",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element_2 = curriculumService.createCurriculumElement("Element-1.2", "Element 1.2",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element_3 = curriculumService.createCurriculumElement("Element-1.3", "Element 1.3",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("elements").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementVO> children = this.parseCurriculumElementArray(response.getEntity());
		Assert.assertNotNull(children);
		Assert.assertEquals(3, children.size());
		
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for(CurriculumElementVO elementVo:children) {
			if(elementVo.getKey().equals(element_1.getKey())) {
				found1 = true;
			} else if(elementVo.getKey().equals(element_2.getKey())) {
				found2 = true;
			} else if(elementVo.getKey().equals(element_3.getKey())) {
				found3 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
		Assert.assertTrue(found3);
	}

	@Test
	public void createCurriculumElement()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-3", "Element 3",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO vo = new CurriculumElementVO();
		vo.setDescription("REST created element");
		vo.setDisplayName("REST Curriculum element");
		vo.setExternalId("REST-CEL-1");
		vo.setIdentifier("REST-ID-CEL-1");
		vo.setManagedFlagsString("delete");
		vo.setCurriculumKey(curriculum.getKey());
		vo.setParentElementKey(element1.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		CurriculumElementVO savedVo = conn.parse(response, CurriculumElementVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("REST created element", savedVo.getDescription());
		Assert.assertEquals("REST Curriculum element", savedVo.getDisplayName());
		Assert.assertEquals("REST-CEL-1", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-CEL-1", savedVo.getIdentifier());
		Assert.assertEquals("delete", savedVo.getManagedFlagsString());
		Assert.assertEquals(element1.getKey(), savedVo.getParentElementKey());
		Assert.assertEquals(curriculum.getKey(), savedVo.getCurriculumKey());
		
		// checked database
		CurriculumElement savedElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElement);
		Assert.assertEquals(savedVo.getKey(), savedElement.getKey());
		Assert.assertEquals("REST created element", savedElement.getDescription());
		Assert.assertEquals("REST Curriculum element", savedElement.getDisplayName());
		Assert.assertEquals("REST-CEL-1", savedElement.getExternalId());
		Assert.assertEquals("REST-ID-CEL-1", savedElement.getIdentifier());
		Assert.assertNotNull(savedElement.getManagedFlags());
		Assert.assertEquals(1, savedElement.getManagedFlags().length);
		Assert.assertEquals(CurriculumElementManagedFlag.delete, savedElement.getManagedFlags()[0]);
		Assert.assertEquals(element1, savedElement.getParent());
		Assert.assertEquals(curriculum, savedElement.getCurriculum());
	}
	
	@Test
	public void updateCurriculumElement()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "Element 5",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElementType type = curriculumService.createCurriculumElementType("TYPE-2", "Type 2", "", "");
		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO vo = new CurriculumElementVO();
		vo.setKey(element.getKey());
		vo.setDescription("Via REST updated element");
		vo.setDisplayName("REST updated element");
		vo.setExternalId("REST-CEL-2");
		vo.setIdentifier("REST-ID-CEL-2");
		vo.setManagedFlagsString("delete,all");
		vo.setCurriculumKey(curriculum.getKey());
		vo.setCurriculumElementTypeKey(type.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumElementVO savedVo = conn.parse(response, CurriculumElementVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("Via REST updated element", savedVo.getDescription());
		Assert.assertEquals("REST updated element", savedVo.getDisplayName());
		Assert.assertEquals("REST-CEL-2", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-CEL-2", savedVo.getIdentifier());
		Assert.assertEquals("delete,all", savedVo.getManagedFlagsString());
		Assert.assertEquals(curriculum.getKey(), savedVo.getCurriculumKey());
		Assert.assertNull(savedVo.getParentElementKey());
		Assert.assertEquals(type.getKey(), savedVo.getCurriculumElementTypeKey());
		
		// checked database
		CurriculumElement savedElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElement);
		Assert.assertEquals(savedVo.getKey(), savedElement.getKey());
		Assert.assertEquals("Via REST updated element", savedElement.getDescription());
		Assert.assertEquals("REST updated element", savedElement.getDisplayName());
		Assert.assertEquals("REST-CEL-2", savedElement.getExternalId());
		Assert.assertEquals("REST-ID-CEL-2", savedElement.getIdentifier());
		Assert.assertNotNull(savedElement.getManagedFlags());
		Assert.assertEquals(2, savedElement.getManagedFlags().length);
		Assert.assertEquals(curriculum, savedElement.getCurriculum());
		Assert.assertEquals(type, savedElement.getType());
	}
	

	@Test
	public void updateCurriculumElement_moveToOtherCurriculum()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 25", "REST-p-25-organisation", "", defOrganisation, null);
		Curriculum sourceCurriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Source Curriculum", "A source curriculum", false, organisation);
		Curriculum targetCurriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Target Curriculum", "A target curriculum", false, organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-25", "Element2 5",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, sourceCurriculum);
		CurriculumElement element1_1 = curriculumService.createCurriculumElement("Element-25-1", "Element 25-1",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, sourceCurriculum);

		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO vo = new CurriculumElementVO();
		vo.setKey(element1.getKey());
		vo.setDisplayName("REST updated element");
		vo.setCurriculumKey(targetCurriculum.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(targetCurriculum.getKey().toString())
				.path("elements").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumElementVO savedVo = conn.parse(response, CurriculumElementVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("REST updated element", savedVo.getDisplayName());
		Assert.assertEquals(targetCurriculum.getKey(), savedVo.getCurriculumKey());
		Assert.assertNull(savedVo.getParentElementKey());
		
		// checked database
		CurriculumElement savedElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElement);
		Assert.assertEquals(savedVo.getKey(), savedElement.getKey());
		Assert.assertEquals("REST updated element", savedElement.getDisplayName());
		Assert.assertEquals(targetCurriculum, savedElement.getCurriculum());

		// check the source curriculum (low level to make sure the position are rights)
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(sourceCurriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertTrue(rootElements.isEmpty());

		// check the target curriculum (low level to make sure the position are rights)
		CurriculumImpl reloadedTargetCurriculum = (CurriculumImpl)curriculumDao.loadByKey(targetCurriculum.getKey());
		List<CurriculumElement> targetRootElements = reloadedTargetCurriculum.getRootElements();
		Assert.assertEquals(1, targetRootElements.size());
		Assert.assertTrue(targetRootElements.contains(element1));
		
		List<CurriculumElement> movedElements = curriculumElementDao.loadElements(reloadedTargetCurriculum, CurriculumElementStatus.values());
		Assert.assertTrue(movedElements.contains(element1));
		Assert.assertTrue(movedElements.contains(element1_1));
		
		List<CurriculumElement> sourceElements = curriculumElementDao.loadElements(sourceCurriculum, CurriculumElementStatus.values());
		Assert.assertTrue(sourceElements.isEmpty());
	}
	
	/**
	 * Set the parent element key as itself.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void updateCurriculumElement_conflict()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "Element 5",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element_1 = curriculumService.createCurriculumElement("Element-5.1", "Element 5.1",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO vo = new CurriculumElementVO();
		vo.setKey(element_1.getKey());
		vo.setDescription("Via REST updated element");
		vo.setDisplayName("REST updated element");
		vo.setExternalId("REST-CEL-2");
		vo.setIdentifier("REST-ID-CEL-2");
		vo.setCurriculumKey(curriculum.getKey());
		vo.setParentElementKey(element_1.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	@Test
	public void updateCurriculumElementWithKey()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-6", "Element 6",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO vo = CurriculumElementVO.valueOf(element);
		vo.setExternalId("REST-CEL-7");
		vo.setIdentifier("REST-ID-CEL-7");
		vo.setManagedFlagsString("displayName");

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumElementVO savedVo = conn.parse(response, CurriculumElementVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("REST-CEL-7", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-CEL-7", savedVo.getIdentifier());
		Assert.assertEquals("displayName", savedVo.getManagedFlagsString());
		
		// checked database
		CurriculumElement savedElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElement);
		Assert.assertEquals(savedVo.getKey(), savedElement.getKey());
		Assert.assertEquals("REST-CEL-7", savedElement.getExternalId());
		Assert.assertEquals("REST-ID-CEL-7", savedElement.getIdentifier());
		Assert.assertNotNull(savedElement.getManagedFlags());
		Assert.assertEquals(1, savedElement.getManagedFlags().length);
		Assert.assertEquals(CurriculumElementManagedFlag.displayName, savedElement.getManagedFlags()[0]);
	}
	
	@Test
	public void  reorderCurriculumElementChildren()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation organisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements-order", "REST ordered curriculum", "A curriculum", false, organisation);
		CurriculumElement rootElement = curriculumService.createCurriculumElement("Element-35", "Element35",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		List<CurriculumElement> elements = new ArrayList<>();
		for(int i=0; i<6; i++) {
			CurriculumElement element = curriculumService.createCurriculumElement("Element-35-" + i, "Element 35-" + i,
					CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
					CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
			elements.add(element);
		}
		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO[] orderArray = new CurriculumElementVO[] {
				CurriculumElementVO.valueOf(elements.get(4)),
				CurriculumElementVO.valueOf(elements.get(2)),
				CurriculumElementVO.valueOf(elements.get(5)),
				CurriculumElementVO.valueOf(elements.get(0)),
				CurriculumElementVO.valueOf(elements.get(3)),
				CurriculumElementVO.valueOf(elements.get(1))
		};
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(rootElement.getKey().toString()).path("reorder").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, orderArray);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<CurriculumElement> orderedElements = curriculumElementDao.getChildren(rootElement);
		Assert.assertEquals(elements.get(4), orderedElements.get(0));
		Assert.assertEquals(elements.get(2), orderedElements.get(1));
		Assert.assertEquals(elements.get(5), orderedElements.get(2));
		Assert.assertEquals(elements.get(0), orderedElements.get(3));
		Assert.assertEquals(elements.get(3), orderedElements.get(4));
		Assert.assertEquals(elements.get(1), orderedElements.get(5));
	}
	
	@Test
	public void updateAndMoveOrganisation()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-8", "Element 8",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumService.createCurriculumElement("Element-8.1", "Element 8.1",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-9", "Element 9",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		
		CurriculumElementVO vo = CurriculumElementVO.valueOf(element1_1);
		vo.setParentElementKey(element2.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumElementVO savedVo = conn.parse(response, CurriculumElementVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals(element2.getKey(), savedVo.getParentElementKey());
		Assert.assertEquals(curriculum.getKey(), savedVo.getCurriculumKey());
		
		// checked database
		CurriculumElement savedElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElement);
		Assert.assertEquals(savedVo.getKey(), savedElement.getKey());
		Assert.assertEquals(element2, savedElement.getParent());
		Assert.assertEquals(curriculum, savedElement.getCurriculum());
	}
	
	@Test
	public void updateCurriculumElement_notAuthorized()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-10", "Element 10",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Curriculum otherCurriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum",
				"A curriculum accessible by REST API for elements", false, organisation);
		dbInstance.commitAndCloseSession();
		
		// other administration organisation
		Organisation adminOrganisation = organisationService.createOrganisation("REST Admin Organisation", "REST-p-4admin-organisation", "", defOrganisation, null);
		IdentityWithLogin admin = JunitTestHelper.createAndPersistRndUser("p-4admin");
		organisationService.addMember(adminOrganisation, admin.getIdentity(), OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(admin));
		
		CurriculumElementVO vo = CurriculumElementVO.valueOf(element);
		vo.setExternalId("REST-CEL-10");

		//try to update an element under the false curriculum
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(otherCurriculum.getKey().toString())
				.path("elements").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	@Test
	public void getRepositoryEntriesInCurriculumElement()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 4", "REST-p-4-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-11", "Element 11",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("rest-auth-1");
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(author);
		curriculumService.addRepositoryEntry(element, course, false);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		// add the relation
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RepositoryEntryVO> entries = parseRepositoryEntryArray(response.getEntity());
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(course.getKey(), entries.get(0).getKey());
	}
	
	@Test
	public void headRepositoryEntryInCurriculumElement()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 4", "REST-p-4-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-11", "Element 11",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("rest-auth-1");
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(author);
		curriculumService.addRepositoryEntry(element, course, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// check the relation
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path(course.getKey().toString()).build();
		HttpHead method = conn.createHead(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// check a non existing repository entry
		URI notRequest = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path("32").build();
		HttpHead notMethod = conn.createHead(notRequest, MediaType.APPLICATION_JSON, true);
		HttpResponse notResponse = conn.execute(notMethod);
		Assert.assertEquals(404, notResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(notResponse.getEntity());
	}
	
	@Test
	public void getRepositoryEntryInCurriculumElement()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 4", "REST-p-4-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-11", "Element 11",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("rest-auth-1");
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(author);
		curriculumService.addRepositoryEntry(element, course, false);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		// check the relation
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path(course.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryVO entry = conn.parse(response.getEntity(), RepositoryEntryVO.class);
		Assert.assertNotNull(entry);
		Assert.assertEquals(course.getKey(), entry.getKey());
		
		// check a non existing repository entry
		URI notRequest = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path("32").build();
		HttpHead notMethod = conn.createHead(notRequest, MediaType.APPLICATION_JSON, true);
		HttpResponse notResponse = conn.execute(notMethod);
		Assert.assertEquals(404, notResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(notResponse.getEntity());
	}
	
	@Test
	public void addRepositoryEntryToCurriculumElement()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 4", "REST-p-4-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-11", "Element 11",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("rest-auth-1");
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();

		// add the relation
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path(course.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(course, entries.get(0));
		
		// very important -> not modified response if already added
		URI twiceRequest = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path(course.getKey().toString()).build();
		HttpPut twiceMethod = conn.createPut(twiceRequest, MediaType.APPLICATION_JSON, true);
		HttpResponse twiceResponse = conn.execute(twiceMethod);
		Assert.assertEquals(304, twiceResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(twiceResponse.getEntity());
	}
	
	@Test
	public void removeRepositoryEntryFromCurriculumElement()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 5", "REST-p-5-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-12", "Element 12",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("rest-auth-2");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
		Assert.assertEquals(1, entries.size());
		

		//try to delete the relation
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("entries").path(entry.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<RepositoryEntry> deletedRelations = curriculumService.getRepositoryEntries(element);
		Assert.assertNotNull(deletedRelations);
		Assert.assertTrue(deletedRelations.isEmpty());
	}
	
	@Test
	public void getMemberships()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-1");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 5", "REST-p-5-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-12", "Element 12",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("members").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementMemberVO> memberVoes = parseCurriculumElementMemberArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getIdentityKey());
		Assert.assertEquals("participant", memberVoes.get(0).getRole());
	}
	
	@Test
	public void getUsers()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-6");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 8", "REST-p-8-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-14", "Element 14",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("users").queryParam("role", "participant").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void getUsers_curriculumElementOwners()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-6");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 8", "REST-p-8-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-14", "Element 14",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.curriculumelementowner);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("users")
				.queryParam("role", "curriculumelementowner").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void getParticipants()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-7");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 9", "REST-p-9-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-15", "Element 15",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("participants").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void getCoaches()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-10");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 10", "REST-p-10-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-16", "Element 16",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("coaches").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void getMasterCoaches()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-10");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 10", "REST-p-10-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-16", "Element 16",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.mastercoach);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("mastercoaches").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		assertThat(memberVoes)
			.isNotNull()
			.size().isEqualTo(1);
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void getCurriculumOwners()
	throws IOException, URISyntaxException {
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-10");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 10", "REST-p-10-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-16", "Element 16",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		curriculumService.addMember(element, member, CurriculumRoles.curriculumelementowner);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("curriculumelementowners").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> memberVoes = parseUserArray(response.getEntity());
		
		Assert.assertNotNull(memberVoes);
		Assert.assertEquals(1, memberVoes.size());
		Assert.assertEquals(member.getKey(), memberVoes.get(0).getKey());
	}
	
	@Test
	public void addMembership()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-1");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 5", "REST-p-5-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-12", "Element 12",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CurriculumElementMemberVO membershipVo = new CurriculumElementMemberVO();
		membershipVo.setIdentityKey(member.getKey());
		membershipVo.setRole("participant");
		membershipVo.setInheritanceMode("none");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("members").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, membershipVo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		SearchMemberParameters params = new SearchMemberParameters();
		List<CurriculumMember> members = curriculumService.getMembers(element, params);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(member, members.get(0).getIdentity());
		Assert.assertEquals("participant", members.get(0).getRole());
	}
	
	@Test
	public void addParticipant()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-11");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 11", "REST-p-11-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-17", "Element 17",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("participants").path(participant.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> participants = curriculumService.getMembersIdentity(element, CurriculumRoles.participant);
		Assert.assertNotNull(participants);
		Assert.assertEquals(1, participants.size());
		Assert.assertEquals(participant, participants.get(0));
	}
	
	@Test
	public void addCoach()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-12");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 12", "REST-p-12-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-18", "Element 18",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("coaches").path(coach.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> coaches = curriculumService.getMembersIdentity(element, CurriculumRoles.coach);
		Assert.assertNotNull(coaches);
		Assert.assertEquals(1, coaches.size());
		Assert.assertEquals(coach, coaches.get(0));
	}
	
	@Test
	public void addMasterCoach()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-21");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 21", "REST-p-21-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-22", "Element 22",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("mastercoaches").path(masterCoach.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> masterCoaches = curriculumService.getMembersIdentity(element, CurriculumRoles.mastercoach);
		Assert.assertNotNull(masterCoaches);
		Assert.assertEquals(1, masterCoaches.size());
		Assert.assertEquals(masterCoach, masterCoaches.get(0));
	}
	
	@Test
	public void addParticipants()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-13");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-14");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 13", "REST-p-13-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-18", "Element 18",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		UserVO[] participants = new UserVO[] {
			UserVOFactory.get(participant1),
			UserVOFactory.get(participant2)
		};
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("participants").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, participants);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> participantList = curriculumService.getMembersIdentity(element, CurriculumRoles.participant);
		Assert.assertNotNull(participantList);
		Assert.assertEquals(2, participantList.size());
	}
	
	@Test
	public void addCoaches()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-15");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-16");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 16", "REST-p-16-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-20", "Element 20",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		UserVO[] coaches = new UserVO[] {
			UserVOFactory.get(coach1),
			UserVOFactory.get(coach2)
		};
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("coaches").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, coaches);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> coachList = curriculumService.getMembersIdentity(element, CurriculumRoles.coach);
		Assert.assertNotNull(coachList);
		Assert.assertEquals(2, coachList.size());
	}
	
	@Test
	public void addMasterCoaches()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity masterCoach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-23");
		Identity masterCoach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-24");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 25", "REST-p-25-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-26", "Element 26",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		UserVO[] coaches = new UserVO[] {
			UserVOFactory.get(masterCoach1),
			UserVOFactory.get(masterCoach2)
		};
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("mastercoaches").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, coaches);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> masterCoachList = curriculumService.getMembersIdentity(element, CurriculumRoles.mastercoach);
		assertThat(masterCoachList)
			.isNotNull()
			.containsExactlyInAnyOrder(masterCoach1, masterCoach2);
	}
	
	@Test
	public void removeMembership()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-1");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 5", "REST-p-5-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-12", "Element 12",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		curriculumService.addMember(element, member, CurriculumRoles.participant);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("members").path(member.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		SearchMemberParameters params = new SearchMemberParameters();
		List<CurriculumMember> members = curriculumService.getMembers(element, params);
		Assert.assertNotNull(members);
		Assert.assertTrue(members.isEmpty());
	}
	
	@Test
	public void removeParticipant()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-21");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-22");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 21", "REST-p-21-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-21", "Element 21",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, coach, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("participants").path(participant.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> participants = curriculumService.getMembersIdentity(element, CurriculumRoles.participant);
		Assert.assertTrue(participants.isEmpty());
		List<Identity> coaches = curriculumService.getMembersIdentity(element, CurriculumRoles.coach);
		Assert.assertEquals(1, coaches.size());
	}
	
	@Test
	public void removeCoach()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-23");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-24");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 24", "REST-p-24-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24", "Element 24",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, coach, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("coaches").path(coach.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> coaches = curriculumService.getMembersIdentity(element, CurriculumRoles.coach);
		Assert.assertTrue(coaches.isEmpty());
		List<Identity> participants = curriculumService.getMembersIdentity(element, CurriculumRoles.participant);
		Assert.assertEquals(1, participants.size());
	}
	
	@Test
	public void removeMasterCoach()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-30");
		Identity mastercoach = JunitTestHelper.createAndPersistIdentityAsRndUser("element-member-31");
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 32", "REST-p-32-organisation", "", defOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-33", "Element 33",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, mastercoach, CurriculumRoles.mastercoach);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString()).path("mastercoaches").path(mastercoach.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> masterCoaches = curriculumService.getMembersIdentity(element, CurriculumRoles.mastercoach);
		assertThat(masterCoaches)
			.isNotNull()
			.isEmpty();
		List<Identity> participants = curriculumService.getMembersIdentity(element, CurriculumRoles.participant);
		assertThat(participants)
			.isNotNull()
			.containsExactly(participant);
	}
	
	@Test
	public void getTaxonomyLevels()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24", "Element 24",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-350", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString())
				.path("taxonomy").path("levels").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyLevelVO> levelVoes = parseTaxonomyLevelArray(response.getEntity());
		Assert.assertNotNull(levelVoes);
		Assert.assertEquals(1, levelVoes.size());
	}
	
	@Test
	public void addTaxonomyLevels()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24", "Element 24",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-351", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString())
				.path("taxonomy").path("levels").path(level.getKey().toString()).build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// check that the relation is really persisted
		List<TaxonomyLevel> levels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level, levels.get(0));
	}
	
	@Test
	public void addTwiceTaxonomyLevels()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24", "Element 24",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-352", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString())
				.path("taxonomy").path("levels").path(level.getKey().toString()).build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(304, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// check that the relation is really persisted
		CurriculumElement reloadedElement = curriculumService.getCurriculumElement(element);
		
		Set<CurriculumElementToTaxonomyLevel> relationToLevels = reloadedElement.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		CurriculumElementToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
		Assert.assertEquals(element, relationToLevel.getCurriculumElement());
	}
	
	@Test
	public void deleteTaxonomyLevels()
	throws IOException, URISyntaxException {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elements", false, defOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-30", "Element 24",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-353", "Leveled taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-2", random(), "My second taxonomy level", "A basic level", null, null, null, null, taxonomy);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level1);
		curriculumElementToTaxonomyLevelDao.createRelation(element, level2);
		dbInstance.commitAndCloseSession();
		
		// make sure there is something to delete
		List<TaxonomyLevel> levels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
		Assert.assertNotNull(levels);
		Assert.assertEquals(2, levels.size());
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString())
				.path("elements").path(element.getKey().toString())
				.path("taxonomy").path("levels").path(level2.getKey().toString()).build();
		
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// check that there is only level1 left
		CurriculumElement reloadedElement = curriculumService.getCurriculumElement(element);
		
		Set<CurriculumElementToTaxonomyLevel> relationToLevels = reloadedElement.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		CurriculumElementToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level1, relationToLevel.getTaxonomyLevel());
		Assert.assertEquals(element, relationToLevel.getCurriculumElement());
	}
	
	protected List<UserVO> parseUserArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<TaxonomyLevelVO> parseTaxonomyLevelArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<TaxonomyLevelVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<RepositoryEntryVO> parseRepositoryEntryArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RepositoryEntryVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<CurriculumElementVO> parseCurriculumElementArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<CurriculumElementVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<CurriculumElementMemberVO> parseCurriculumElementMemberArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<CurriculumElementMemberVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
