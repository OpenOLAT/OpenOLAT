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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.restapi.CurriculumElementTypeVO;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 16 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypesWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	@Test
	public void getCurriculumElementTypes()
	throws IOException, URISyntaxException {
		CurriculumElementType type = curriculumService.createCurriculumElementType("TYPE-2", "Type 2", "", "");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementTypeVO> typeVoes = parseCurriculumElementTypeArray(response.getEntity());
		
		CurriculumElementTypeVO foundVo = null;
		for(CurriculumElementTypeVO typeVo:typeVoes) {
			if(typeVo.getKey().equals(type.getKey())) {
				foundVo = typeVo;
			}
		}
		Assert.assertNotNull(foundVo);
	}	

	@Test
	public void getCurriculumElementType()
	throws IOException, URISyntaxException {
		CurriculumElementType type = curriculumService.createCurriculumElementType("rest-3-type", "REST Type 3", "A type for REST", "EXT-3");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").path(type.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		CurriculumElementTypeVO elementTypeVo = conn.parse(response, CurriculumElementTypeVO.class); 
		Assert.assertNotNull(elementTypeVo);
		Assert.assertEquals(type.getKey(), elementTypeVo.getKey());
		Assert.assertEquals(type.getKey(), elementTypeVo.getKey());
		Assert.assertEquals("REST Type 3", elementTypeVo.getDisplayName());
		Assert.assertEquals("rest-3-type", elementTypeVo.getIdentifier());
		Assert.assertEquals("A type for REST", elementTypeVo.getDescription());	
		Assert.assertEquals("EXT-3", elementTypeVo.getExternalId());	
	}
	
	@Test
	public void createCurriculumElementType()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CurriculumElementTypeVO vo = new CurriculumElementTypeVO();
		vo.setCssClass("o_icon_rest");
		vo.setDescription("REST created element type");
		vo.setDisplayName("REST Curriculum element type");
		vo.setExternalId("REST1CTYP");
		vo.setIdentifier("REST-ID-1-CTYP");
		vo.setManagedFlagsString("delete");

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		CurriculumElementTypeVO savedVo = conn.parse(response, CurriculumElementTypeVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_rest", savedVo.getCssClass());
		Assert.assertEquals("REST created element type", savedVo.getDescription());
		Assert.assertEquals("REST Curriculum element type", savedVo.getDisplayName());
		Assert.assertEquals("REST1CTYP", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-1-CTYP", savedVo.getIdentifier());
		Assert.assertEquals("delete", savedVo.getManagedFlagsString());
		
		// checked database
		CurriculumElementType savedElementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElementType);
		Assert.assertEquals(savedVo.getKey(), savedElementType.getKey());
		Assert.assertEquals("o_icon_rest", savedElementType.getCssClass());
		Assert.assertEquals("REST created element type", savedElementType.getDescription());
		Assert.assertEquals("REST Curriculum element type", savedElementType.getDisplayName());
		Assert.assertEquals("REST1CTYP", savedElementType.getExternalId());
		Assert.assertEquals("REST-ID-1-CTYP", savedElementType.getIdentifier());
		Assert.assertNotNull(savedElementType.getManagedFlags());
		Assert.assertEquals(1, savedElementType.getManagedFlags().length);
		Assert.assertEquals(CurriculumElementTypeManagedFlag.delete, savedElementType.getManagedFlags()[0]);
	}
	
	@Test
	public void updateCurriculumElementType()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CurriculumElementType type = curriculumService.createCurriculumElementType("rest-5-type", "REST 5 Type", "A type for REST", "EXT-5");
		dbInstance.commitAndCloseSession();
		
		CurriculumElementTypeVO vo = new CurriculumElementTypeVO();
		vo.setKey(type.getKey());
		vo.setCssClass("o_icon_restful");
		vo.setDescription("Via REST updated element type");
		vo.setDisplayName("REST updated element type");
		vo.setExternalId("REST-EXT-5");
		vo.setIdentifier("REST-ID-5-CTYP");
		vo.setManagedFlagsString("delete,all");

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumElementTypeVO savedVo = conn.parse(response, CurriculumElementTypeVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_restful", savedVo.getCssClass());
		Assert.assertEquals("Via REST updated element type", savedVo.getDescription());
		Assert.assertEquals("REST updated element type", savedVo.getDisplayName());
		Assert.assertEquals("REST-EXT-5", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-5-CTYP", savedVo.getIdentifier());
		Assert.assertEquals("delete,all", savedVo.getManagedFlagsString());
		
		// checked database
		CurriculumElementType savedElementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElementType);
		Assert.assertEquals(savedVo.getKey(), savedElementType.getKey());
		Assert.assertEquals("o_icon_restful", savedElementType.getCssClass());
		Assert.assertEquals("Via REST updated element type", savedElementType.getDescription());
		Assert.assertEquals("REST updated element type", savedElementType.getDisplayName());
		Assert.assertEquals("REST-EXT-5", savedElementType.getExternalId());
		Assert.assertEquals("REST-ID-5-CTYP", savedElementType.getIdentifier());
		Assert.assertNotNull(savedElementType.getManagedFlags());
		Assert.assertEquals(2, savedElementType.getManagedFlags().length);
	}
	
	@Test
	public void updateCurriculumElementTypeWithKey()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		CurriculumElementType type = curriculumService.createCurriculumElementType("rest-6-type", "REST 6 Type", "A type for REST", "EXT-6");
		dbInstance.commitAndCloseSession();
		
		CurriculumElementTypeVO vo = CurriculumElementTypeVO.valueOf(type);
		vo.setCssClass("o_icon_restfully");
		vo.setExternalId("REST6b");
		vo.setIdentifier("REST-ID-6b");
		vo.setManagedFlagsString("identifier");

		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").path(type.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		CurriculumElementTypeVO savedVo = conn.parse(response, CurriculumElementTypeVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_restfully", savedVo.getCssClass());
		Assert.assertEquals("REST6b", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-6b", savedVo.getIdentifier());
		Assert.assertEquals("identifier", savedVo.getManagedFlagsString());
		
		// checked database
		CurriculumElementType savedElementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedElementType);
		Assert.assertEquals(savedVo.getKey(), savedElementType.getKey());
		Assert.assertEquals("o_icon_restfully", savedElementType.getCssClass());
		Assert.assertEquals("REST6b", savedElementType.getExternalId());
		Assert.assertEquals("REST-ID-6b", savedElementType.getIdentifier());
		Assert.assertNotNull(savedElementType.getManagedFlags());
		Assert.assertEquals(1, savedElementType.getManagedFlags().length);
		Assert.assertEquals(CurriculumElementTypeManagedFlag.identifier, savedElementType.getManagedFlags()[0]);
	}

	@Test
	public void getCurriculumElementTypeAllowedSubTypes()
	throws IOException, URISyntaxException {
		CurriculumElementType type = curriculumService.createCurriculumElementType("rest-7-type", "REST Type 7", "A type for REST", "EXT-7");
		CurriculumElementType subType1 = curriculumService.createCurriculumElementType("rest-7-1-type", "REST Type 7.1", "A type for REST", "EXT-7-1");
		CurriculumElementType subType2 = curriculumService.createCurriculumElementType("rest-7-2-type", "REST Type 7.2", "A type for REST", "EXT-7-2");
		dbInstance.commit();
		List<CurriculumElementType> subTypes = new ArrayList<>(2);
		subTypes.add(subType1);
		subTypes.add(subType2);
		type = curriculumService.updateCurriculumElementType(type, subTypes);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(type);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types")
				.path(type.getKey().toString()).path("allowedSubTypes").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CurriculumElementTypeVO> typeVoList = parseCurriculumElementTypeArray(response.getEntity());
		Assert.assertNotNull(typeVoList);
		Assert.assertEquals(2, typeVoList.size());
		
		boolean found1 = false;
		boolean found2 = false;
		for(CurriculumElementTypeVO typeVo:typeVoList) {
			if(subType1.getKey().equals(typeVo.getKey())) {
				found1 = true;
			} else if(subType2.getKey().equals(typeVo.getKey())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void allowCurriculumElementTypeSubType()
	throws IOException, URISyntaxException {
		CurriculumElementType type = curriculumService.createCurriculumElementType("rest-8-type", "REST Type 8", "A type for REST", "EXT-8");
		CurriculumElementType subType1 = curriculumService.createCurriculumElementType("rest-8-1-type", "REST Type 8.1", "A type for REST", "EXT-8-1");
		CurriculumElementType subType2 = curriculumService.createCurriculumElementType("rest-8-2-type", "REST Type 8.2", "A type for REST", "EXT-8-2");
		dbInstance.commit();
		type = curriculumService.updateCurriculumElementType(type, Collections.singletonList(subType1));
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").path(type.getKey().toString())
				.path("allowedSubTypes").path(subType2.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		CurriculumElementType reloadedType = curriculumService.getCurriculumElementType(type);
		Set<CurriculumElementTypeToType> typeToTypes = reloadedType.getAllowedSubTypes();
		Assert.assertEquals(2, typeToTypes.size());
		boolean found1 = false;
		boolean found2 = false;
		for(CurriculumElementTypeToType typeToType:typeToTypes) {
			CurriculumElementType subType = typeToType.getAllowedSubType();
			if(subType1.getKey().equals(subType.getKey())) {
				found1 = true;
			} else if(subType2.getKey().equals(subType.getKey())) {
				found2 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertTrue(found2);
	}
	
	@Test
	public void disallowCurriculumElementTypeSubType()
	throws IOException, URISyntaxException {
		CurriculumElementType type = curriculumService.createCurriculumElementType("rest-9-type", "REST Type 9", "A type for REST", "EXT-9");
		CurriculumElementType subType1 = curriculumService.createCurriculumElementType("rest-9-1-type", "REST Type 9.1", "A type for REST", "EXT-9-1");
		CurriculumElementType subType2 = curriculumService.createCurriculumElementType("rest-9-2-type", "REST Type 9.2", "A type for REST", "EXT-9-2");
		CurriculumElementType subType3 = curriculumService.createCurriculumElementType("rest-9-3-type", "REST Type 9.3", "A type for REST", "EXT-9-3");
		dbInstance.commit();
		List<CurriculumElementType> allowedSubTypes = new ArrayList<>();
		allowedSubTypes.add(subType1);
		allowedSubTypes.add(subType2);
		allowedSubTypes.add(subType3);
		type = curriculumService.updateCurriculumElementType(type, allowedSubTypes);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("curriculum").path("types").path(type.getKey().toString())
				.path("allowedSubTypes").path(subType2.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		CurriculumElementType reloadedType = curriculumService.getCurriculumElementType(type);
		Set<CurriculumElementTypeToType> typeToTypes = reloadedType.getAllowedSubTypes();
		Assert.assertEquals(2, typeToTypes.size());
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for(CurriculumElementTypeToType typeToType:typeToTypes) {
			CurriculumElementType subType = typeToType.getAllowedSubType();
			if(subType1.getKey().equals(subType.getKey())) {
				found1 = true;
			} else if(subType2.getKey().equals(subType.getKey())) {
				found2 = true;
			} else if(subType3.getKey().equals(subType.getKey())) {
				found3 = true;
			}
		}
		Assert.assertTrue(found1);
		Assert.assertFalse(found2);
		Assert.assertTrue(found3);
	}

	protected List<CurriculumElementTypeVO> parseCurriculumElementTypeArray(HttpEntity entity) {
		try(InputStream in = entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<CurriculumElementTypeVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
