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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.restapi.CurriculumElementVO;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementsWebServiceTest extends OlatJerseyTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void getCurriculumElements()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-1", "Element 1", null, null, null, null, curriculum);
		CurriculumElement element1_1 = curriculumService.createCurriculumElement("Element-1.1", "Element 1.1", null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		List<CurriculumElementVO> elementVoes = parseCurriculumElementArray(body);
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
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-1", "Element 1", null, null, null, null, curriculum);
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
	public void createCurriculumElement()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation organisation = organisationService.createOrganisation("Curriculum org.", "curr-org", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-3", "Element 3", null, null, null, null, curriculum);
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
		Assert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
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
		
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "Element 5", null, null, null, null, curriculum);
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
	public void updateCurriculumElementWithKey()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-6", "Element 6", null, null, null, null, curriculum);
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
	public void updateAndMoveOrganisation()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-8", "Element 8", null, null, null, null, curriculum);
		CurriculumElement element1_1 = curriculumService.createCurriculumElement("Element-8.1", "Element 8.1", null, null, element1, null, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-9", "Element 9", null, null, null, null, curriculum);
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
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation organisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", null, null);
		Curriculum curriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-10", "Element 10", null, null, null, null, curriculum);
		Curriculum otherCurriculum = curriculumService.createCurriculum("REST-Curriculum-elements", "REST Curriculum", "A curriculum accessible by REST API for elemets", organisation);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementVO vo = CurriculumElementVO.valueOf(element);
		vo.setExternalId("REST-CEL-10");

		//try to update an element under the false curriculum
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path(otherCurriculum.getKey().toString())
				.path("elements").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	
	protected List<CurriculumElementVO> parseCurriculumElementArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<CurriculumElementVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
