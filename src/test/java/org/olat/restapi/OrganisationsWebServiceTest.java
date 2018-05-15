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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.restapi.OrganisationVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationsWebServiceTest extends OlatJerseyTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void getOrganisations()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("REST Organisation", "REST-organisation", "", null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		List<OrganisationVO> organisationVoes = parseOrganisationArray(body);
		
		boolean found = false;
		for(OrganisationVO organisationVo:organisationVoes) {
			if(organisationVo.getKey().equals(organisation.getKey())) {
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void getOrganisation()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("REST Organisation 5", "REST-5-organisation", "", null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		OrganisationVO organisationVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(organisationVo);
		Assert.assertEquals(organisation.getKey(), organisationVo.getKey());
		Assert.assertEquals("REST-5-organisation", organisationVo.getIdentifier());
	}
	
	@Test
	public void createOrganisation()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation parentOrganisation = organisationService.createOrganisation("REST Parent Organisation", "REST-p-organisation", "", null, null);
		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		dbInstance.commitAndCloseSession();
		
		OrganisationVO vo = new OrganisationVO();
		vo.setCssClass("o_icon_rest");
		vo.setDescription("REST created organization");
		vo.setDisplayName("REST Organisation");
		vo.setExternalId("REST1");
		vo.setIdentifier("REST-ID-1");
		vo.setManagedFlagsString("delete");
		vo.setOrganisationTypeKey(type.getKey());
		vo.setParentOrganisationKey(parentOrganisation.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		OrganisationVO savedVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_rest", savedVo.getCssClass());
		Assert.assertEquals("REST created organization", savedVo.getDescription());
		Assert.assertEquals("REST Organisation", savedVo.getDisplayName());
		Assert.assertEquals("REST1", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-1", savedVo.getIdentifier());
		Assert.assertEquals("delete", savedVo.getManagedFlagsString());
		Assert.assertEquals(parentOrganisation.getKey(), savedVo.getParentOrganisationKey());
		Assert.assertEquals(parentOrganisation.getKey(), savedVo.getRootOrganisationKey());
		Assert.assertEquals(type.getKey(), savedVo.getOrganisationTypeKey());
		
		// checked database
		Organisation savedOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisation);
		Assert.assertEquals(savedVo.getKey(), savedOrganisation.getKey());
		Assert.assertEquals("o_icon_rest", savedOrganisation.getCssClass());
		Assert.assertEquals("REST created organization", savedOrganisation.getDescription());
		Assert.assertEquals("REST Organisation", savedOrganisation.getDisplayName());
		Assert.assertEquals("REST1", savedOrganisation.getExternalId());
		Assert.assertEquals("REST-ID-1", savedOrganisation.getIdentifier());
		Assert.assertNotNull(savedOrganisation.getManagedFlags());
		Assert.assertEquals(1, savedOrganisation.getManagedFlags().length);
		Assert.assertEquals(OrganisationManagedFlag.delete, savedOrganisation.getManagedFlags()[0]);
		Assert.assertEquals(parentOrganisation, savedOrganisation.getParent());
		Assert.assertEquals(parentOrganisation, savedOrganisation.getRoot());
		Assert.assertEquals(type, savedOrganisation.getType());
	}
	
	@Test
	public void updateOrganisation()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation parentOrganisation = organisationService.createOrganisation("REST Parent Organisation 2 ", "REST-p-2-organisation", "", null, null);
		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 3", "REST-p-3-organisation", "", parentOrganisation, type);
		dbInstance.commitAndCloseSession();
		
		OrganisationVO vo = new OrganisationVO();
		vo.setKey(organisation.getKey());
		vo.setCssClass("o_icon_restful");
		vo.setDescription("Via REST updated organization");
		vo.setDisplayName("REST updated organisation");
		vo.setExternalId("REST2");
		vo.setIdentifier("REST-ID-2");
		vo.setManagedFlagsString("delete,all");
		vo.setOrganisationTypeKey(type.getKey());
		vo.setParentOrganisationKey(parentOrganisation.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		OrganisationVO savedVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_restful", savedVo.getCssClass());
		Assert.assertEquals("Via REST updated organization", savedVo.getDescription());
		Assert.assertEquals("REST updated organisation", savedVo.getDisplayName());
		Assert.assertEquals("REST2", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-2", savedVo.getIdentifier());
		Assert.assertEquals("delete,all", savedVo.getManagedFlagsString());
		Assert.assertEquals(parentOrganisation.getKey(), savedVo.getParentOrganisationKey());
		Assert.assertEquals(parentOrganisation.getKey(), savedVo.getRootOrganisationKey());
		Assert.assertEquals(type.getKey(), savedVo.getOrganisationTypeKey());
		
		// checked database
		Organisation savedOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisation);
		Assert.assertEquals(savedVo.getKey(), savedOrganisation.getKey());
		Assert.assertEquals("o_icon_restful", savedOrganisation.getCssClass());
		Assert.assertEquals("Via REST updated organization", savedOrganisation.getDescription());
		Assert.assertEquals("REST updated organisation", savedOrganisation.getDisplayName());
		Assert.assertEquals("REST2", savedOrganisation.getExternalId());
		Assert.assertEquals("REST-ID-2", savedOrganisation.getIdentifier());
		Assert.assertNotNull(savedOrganisation.getManagedFlags());
		Assert.assertEquals(2, savedOrganisation.getManagedFlags().length);
		Assert.assertEquals(parentOrganisation, savedOrganisation.getParent());
		Assert.assertEquals(parentOrganisation, savedOrganisation.getRoot());
		Assert.assertEquals(type, savedOrganisation.getType());
	}
	
	@Test
	public void updateOrganisationWithKey()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation parentOrganisation = organisationService.createOrganisation("REST Parent Organisation 6", "REST-p-6-organisation", "", null, null);
		Organisation organisation = organisationService.createOrganisation("REST Organisation 7", "REST-p-7-organisation", "", parentOrganisation, null);
		dbInstance.commitAndCloseSession();
		
		OrganisationVO vo = OrganisationVO.valueOf(organisation);
		vo.setCssClass("o_icon_restfully");
		vo.setExternalId("REST7");
		vo.setIdentifier("REST-ID-7");
		vo.setManagedFlagsString("move");

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		OrganisationVO savedVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals("o_icon_restfully", savedVo.getCssClass());
		Assert.assertEquals("REST7", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-7", savedVo.getIdentifier());
		Assert.assertEquals("move", savedVo.getManagedFlagsString());
		
		// checked database
		Organisation savedOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisation);
		Assert.assertEquals(savedVo.getKey(), savedOrganisation.getKey());
		Assert.assertEquals("o_icon_restfully", savedOrganisation.getCssClass());
		Assert.assertEquals("REST7", savedOrganisation.getExternalId());
		Assert.assertEquals("REST-ID-7", savedOrganisation.getIdentifier());
		Assert.assertNotNull(savedOrganisation.getManagedFlags());
		Assert.assertEquals(1, savedOrganisation.getManagedFlags().length);
		Assert.assertEquals(OrganisationManagedFlag.move, savedOrganisation.getManagedFlags()[0]);
	}
	
	@Test
	public void updateAndMoveOrganisation()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation parentOrganisation = organisationService.createOrganisation("REST Parent Organisation 4 ", "REST-p-4-organisation", "", null, null);
		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		Organisation organisation1 = organisationService.createOrganisation("REST Organisation 5", "REST-p-5-organisation", "", parentOrganisation, type);
		Organisation organisation2 = organisationService.createOrganisation("REST Organisation 6", "REST-p-6-organisation", "", parentOrganisation, type);
		dbInstance.commitAndCloseSession();
		
		OrganisationVO vo = OrganisationVO.valueOf(organisation1);
		vo.setParentOrganisationKey(organisation2.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		OrganisationVO savedVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals(organisation2.getKey(), savedVo.getParentOrganisationKey());
		Assert.assertEquals(parentOrganisation.getKey(), savedVo.getRootOrganisationKey());
		
		// checked database
		Organisation savedOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisation);
		Assert.assertEquals(savedVo.getKey(), savedOrganisation.getKey());
		Assert.assertEquals(organisation2, savedOrganisation.getParent());
		Assert.assertEquals(parentOrganisation, savedOrganisation.getRoot());
	}
	
	
	
	protected List<OrganisationVO> parseOrganisationArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<OrganisationVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
