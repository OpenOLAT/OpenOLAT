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
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.RelationRole;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.IdentityToIdentityRelationVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityToIdentityRelationsWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private IdentityRelationshipService identityRelationshipService;
	
	@Test
	public void getRelationsAsSource()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = identityRelationshipService.createRole(role, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityRelationshipService.addRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(idSource.getKey().toString()).path("relations").path("source").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<IdentityToIdentityRelationVO> relationVoes = parseRelationArray(response.getEntity());
		Assert.assertNotNull(relationVoes);
		Assert.assertEquals(1, relationVoes.size());
		IdentityToIdentityRelationVO relationVo = relationVoes.get(0);
		Assert.assertEquals(relation.getKey(), relationVo.getKey());
		Assert.assertEquals(idSource.getKey(), relationVo.getIdentitySourceKey());
		Assert.assertEquals(idTarget.getKey(), relationVo.getIdentityTargetKey());
		Assert.assertEquals(relationRole.getKey(), relationVo.getRelationRoleKey());
		Assert.assertEquals(relationRole.getRole(), relationVo.getRelationRole());
		
		conn.shutdown();
	}
	
	@Test
	public void getRelationsAsTarget()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = identityRelationshipService.createRole(role, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityRelationshipService.addRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(idTarget.getKey().toString()).path("relations").path("target").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<IdentityToIdentityRelationVO> relationVoes = parseRelationArray(response.getEntity());
		Assert.assertNotNull(relationVoes);
		Assert.assertEquals(1, relationVoes.size());
		IdentityToIdentityRelationVO relationVo = relationVoes.get(0);
		Assert.assertEquals(relation.getKey(), relationVo.getKey());
		Assert.assertEquals(idSource.getKey(), relationVo.getIdentitySourceKey());
		Assert.assertEquals(idTarget.getKey(), relationVo.getIdentityTargetKey());
		Assert.assertEquals(relationRole.getKey(), relationVo.getRelationRoleKey());
		Assert.assertEquals(relationRole.getRole(), relationVo.getRelationRole());
		
		conn.shutdown();
	}
	
	@Test
	public void createRelation_put()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = identityRelationshipService.createRole(role, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		dbInstance.commitAndCloseSession();
		
		IdentityToIdentityRelationVO relationVo = new IdentityToIdentityRelationVO();
		relationVo.setExternalId("PUT-1");
		relationVo.setIdentitySourceKey(idSource.getKey());
		relationVo.setIdentityTargetKey(idTarget.getKey());
		relationVo.setManagedFlagsString("all");
		relationVo.setRelationRoleKey(relationRole.getKey());
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(idTarget.getKey().toString()).path("relations").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, relationVo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		IdentityToIdentityRelationVO savedRelationVo = conn.parse(response, IdentityToIdentityRelationVO.class);
		Assert.assertNotNull(savedRelationVo);
		Assert.assertNotNull(savedRelationVo.getKey());
		Assert.assertEquals(idSource.getKey(), savedRelationVo.getIdentitySourceKey());
		Assert.assertEquals(idTarget.getKey(), savedRelationVo.getIdentityTargetKey());
		Assert.assertEquals(relationRole.getKey(), savedRelationVo.getRelationRoleKey());
		Assert.assertEquals(relationRole.getRole(), savedRelationVo.getRelationRole());
		
		conn.shutdown();
	}
	
	@Test
	public void createRelation_post()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = identityRelationshipService.createRole(role, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		dbInstance.commitAndCloseSession();
		
		IdentityToIdentityRelationVO relationVo = new IdentityToIdentityRelationVO();
		relationVo.setExternalId("PUT-1");
		relationVo.setIdentitySourceKey(idSource.getKey());
		relationVo.setIdentityTargetKey(idTarget.getKey());
		relationVo.setManagedFlagsString("all");
		relationVo.setRelationRoleKey(relationRole.getKey());
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(idTarget.getKey().toString()).path("relations").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, relationVo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		IdentityToIdentityRelationVO savedRelationVo = conn.parse(response, IdentityToIdentityRelationVO.class);
		Assert.assertNotNull(savedRelationVo);
		Assert.assertNotNull(savedRelationVo.getKey());
		Assert.assertEquals(idSource.getKey(), savedRelationVo.getIdentitySourceKey());
		Assert.assertEquals(idTarget.getKey(), savedRelationVo.getIdentityTargetKey());
		Assert.assertEquals(relationRole.getKey(), savedRelationVo.getRelationRoleKey());
		Assert.assertEquals(relationRole.getRole(), savedRelationVo.getRelationRole());
		
		conn.shutdown();
	}
	
	@Test
	public void deleteRelation()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		RelationRole relationRole = identityRelationshipService.createRole(role, null);
		Identity idSource = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-1");
		Identity idTarget = JunitTestHelper.createAndPersistIdentityAsRndUser("id-2-id-2");
		IdentityToIdentityRelation relation = identityRelationshipService.addRelation(idSource, idTarget, relationRole, null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(idTarget.getKey().toString())
				.path("relations").path(relation.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		conn.shutdown();
	}
	
	
	protected List<IdentityToIdentityRelationVO> parseRelationArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<IdentityToIdentityRelationVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
