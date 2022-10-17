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
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeManagedFlag;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.basesecurity.model.OrganisationTypeRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.OrganisationTypeVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTypesWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void getOrganisationTypes()
	throws IOException, URISyntaxException {
		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationTypeVO> organisationTypeVoes = parseOrganisationTypeArray(response.getEntity());
		
		boolean found = false;
		for(OrganisationTypeVO organisationTypeVo:organisationTypeVoes) {
			if(organisationTypeVo.getKey().equals(type.getKey())) {
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void getOrganisationType()
	throws IOException, URISyntaxException {
		OrganisationType type = organisationService.createOrganisationType("REST Type 2", "rest-2-type", "A type for REST");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").path(type.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		OrganisationTypeVO organisationTypeVo = conn.parse(response, OrganisationTypeVO.class); 
		Assert.assertNotNull(organisationTypeVo);
		Assert.assertEquals(type.getKey(), organisationTypeVo.getKey());
		Assert.assertEquals(type.getKey(), organisationTypeVo.getKey());
		Assert.assertEquals("REST Type 2", organisationTypeVo.getDisplayName());
		Assert.assertEquals("rest-2-type", organisationTypeVo.getIdentifier());
		Assert.assertEquals("A type for REST", organisationTypeVo.getDescription());	
	}
	
	@Test
	public void createOrganisationType()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		
		OrganisationTypeVO vo = new OrganisationTypeVO();
		vo.setCssClass("o_icon_rest");
		vo.setDescription("REST created organization type");
		vo.setDisplayName("REST Organisation Type");
		vo.setExternalId("REST1TYP");
		vo.setIdentifier("REST-ID-1-TYP");
		vo.setManagedFlagsString("delete");

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		OrganisationTypeVO savedVo = conn.parse(response, OrganisationTypeVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_rest", savedVo.getCssClass());
		Assert.assertEquals("REST created organization type", savedVo.getDescription());
		Assert.assertEquals("REST Organisation Type", savedVo.getDisplayName());
		Assert.assertEquals("REST1TYP", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-1-TYP", savedVo.getIdentifier());
		Assert.assertEquals("delete", savedVo.getManagedFlagsString());
		
		// checked database
		OrganisationType savedOrganisationType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisationType);
		Assert.assertEquals(savedVo.getKey(), savedOrganisationType.getKey());
		Assert.assertEquals("o_icon_rest", savedOrganisationType.getCssClass());
		Assert.assertEquals("REST created organization type", savedOrganisationType.getDescription());
		Assert.assertEquals("REST Organisation Type", savedOrganisationType.getDisplayName());
		Assert.assertEquals("REST1TYP", savedOrganisationType.getExternalId());
		Assert.assertEquals("REST-ID-1-TYP", savedOrganisationType.getIdentifier());
		Assert.assertNotNull(savedOrganisationType.getManagedFlags());
		Assert.assertEquals(1, savedOrganisationType.getManagedFlags().length);
		Assert.assertEquals(OrganisationTypeManagedFlag.delete, savedOrganisationType.getManagedFlags()[0]);
	}
	
	@Test
	public void updateOrganisationType()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		dbInstance.commitAndCloseSession();
		
		OrganisationTypeVO vo = new OrganisationTypeVO();
		vo.setKey(type.getKey());
		vo.setCssClass("o_icon_restful");
		vo.setDescription("Via REST updated organization type");
		vo.setDisplayName("REST updated organisation type");
		vo.setExternalId("REST2TYP");
		vo.setIdentifier("REST-ID-2-TYP");
		vo.setManagedFlagsString("delete,all");

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		OrganisationTypeVO savedVo = conn.parse(response, OrganisationTypeVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_restful", savedVo.getCssClass());
		Assert.assertEquals("Via REST updated organization type", savedVo.getDescription());
		Assert.assertEquals("REST updated organisation type", savedVo.getDisplayName());
		Assert.assertEquals("REST2TYP", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-2-TYP", savedVo.getIdentifier());
		Assert.assertEquals("delete,all", savedVo.getManagedFlagsString());
		
		// checked database
		OrganisationType savedOrganisationType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisationType);
		Assert.assertEquals(savedVo.getKey(), savedOrganisationType.getKey());
		Assert.assertEquals("o_icon_restful", savedOrganisationType.getCssClass());
		Assert.assertEquals("Via REST updated organization type", savedOrganisationType.getDescription());
		Assert.assertEquals("REST updated organisation type", savedOrganisationType.getDisplayName());
		Assert.assertEquals("REST2TYP", savedOrganisationType.getExternalId());
		Assert.assertEquals("REST-ID-2-TYP", savedOrganisationType.getIdentifier());
		Assert.assertNotNull(savedOrganisationType.getManagedFlags());
		Assert.assertEquals(2, savedOrganisationType.getManagedFlags().length);
	}
	
	@Test
	public void updateOrganisationTypeWithKey()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		dbInstance.commitAndCloseSession();
		
		OrganisationTypeVO vo = OrganisationTypeVO.valueOf(type);
		vo.setCssClass("o_icon_restfully");
		vo.setExternalId("REST8");
		vo.setIdentifier("REST-ID-8");
		vo.setManagedFlagsString("displayName");

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").path(type.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		OrganisationTypeVO savedVo = conn.parse(response, OrganisationTypeVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_restfully", savedVo.getCssClass());
		Assert.assertEquals("REST8", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-8", savedVo.getIdentifier());
		Assert.assertEquals("displayName", savedVo.getManagedFlagsString());
		
		// checked database
		OrganisationType savedOrganisationType = organisationService.getOrganisationType(new OrganisationTypeRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisationType);
		Assert.assertEquals(savedVo.getKey(), savedOrganisationType.getKey());
		Assert.assertEquals("o_icon_restfully", savedOrganisationType.getCssClass());
		Assert.assertEquals("REST8", savedOrganisationType.getExternalId());
		Assert.assertEquals("REST-ID-8", savedOrganisationType.getIdentifier());
		Assert.assertNotNull(savedOrganisationType.getManagedFlags());
		Assert.assertEquals(1, savedOrganisationType.getManagedFlags().length);
		Assert.assertEquals(OrganisationTypeManagedFlag.displayName, savedOrganisationType.getManagedFlags()[0]);
	}

	@Test
	public void getOrganisationTypeAllowedSubTypes()
	throws IOException, URISyntaxException {
		OrganisationType type = organisationService.createOrganisationType("REST Type 5", "rest-type", "A type for REST");
		OrganisationType subType1 = organisationService.createOrganisationType("REST Type 5.1", "rest-type", "A type for REST");
		OrganisationType subType2 = organisationService.createOrganisationType("REST Type 5.2", "rest-type", "A type for REST");
		dbInstance.commit();
		List<OrganisationType> subTypes = new ArrayList<>(2);
		subTypes.add(subType1);
		subTypes.add(subType2);
		type = organisationService.updateOrganisationType(type, subTypes);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(type);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types")
				.path(type.getKey().toString()).path("allowedSubTypes").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationTypeVO> typeVoList = parseOrganisationTypeArray(response.getEntity());
		Assert.assertNotNull(typeVoList);
		Assert.assertEquals(2, typeVoList.size());
		
		boolean found1 = false;
		boolean found2 = false;
		for(OrganisationTypeVO typeVo:typeVoList) {
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
	public void allowOrganisationTypeSubType()
	throws IOException, URISyntaxException {
		OrganisationType type = organisationService.createOrganisationType("REST Type 6", "rest-type", "A type for REST");
		OrganisationType subType1 = organisationService.createOrganisationType("REST Type 6.1", "rest-type", "A type for REST");
		OrganisationType subType2 = organisationService.createOrganisationType("REST Type 6.2", "rest-type", "A type for REST");
		dbInstance.commit();
		type = organisationService.updateOrganisationType(type, Collections.singletonList(subType1));
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").path(type.getKey().toString())
				.path("allowedSubTypes").path(subType2.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		OrganisationType reloadedType = organisationService.getOrganisationType(type);
		Set<OrganisationTypeToType> typeToTypes = reloadedType.getAllowedSubTypes();
		Assert.assertEquals(2, typeToTypes.size());
		boolean found1 = false;
		boolean found2 = false;
		for(OrganisationTypeToType typeToType:typeToTypes) {
			OrganisationType subType = typeToType.getAllowedSubOrganisationType();
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
	public void disallowOrganisationTypeSubType()
	throws IOException, URISyntaxException {
		OrganisationType type = organisationService.createOrganisationType("REST Type 7", "rest-type", "A type for REST");
		OrganisationType subType1 = organisationService.createOrganisationType("REST Type 7.1", "rest-type", "A type for REST");
		OrganisationType subType2 = organisationService.createOrganisationType("REST Type 7.2", "rest-type", "A type for REST");
		OrganisationType subType3 = organisationService.createOrganisationType("REST Type 7.3", "rest-type", "A type for REST");
		dbInstance.commit();
		List<OrganisationType> allowedSubTypes = new ArrayList<>();
		allowedSubTypes.add(subType1);
		allowedSubTypes.add(subType2);
		allowedSubTypes.add(subType3);
		type = organisationService.updateOrganisationType(type, allowedSubTypes);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("types").path(type.getKey().toString())
				.path("allowedSubTypes").path(subType2.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		OrganisationType reloadedType = organisationService.getOrganisationType(type);
		Set<OrganisationTypeToType> typeToTypes = reloadedType.getAllowedSubTypes();
		Assert.assertEquals(2, typeToTypes.size());
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for(OrganisationTypeToType typeToType:typeToTypes) {
			OrganisationType subType = typeToType.getAllowedSubOrganisationType();
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
	
	protected List<OrganisationTypeVO> parseOrganisationTypeArray(HttpEntity entity) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(entity.getContent(), new TypeReference<List<OrganisationTypeVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
