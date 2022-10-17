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
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleManagedFlag;
import org.olat.basesecurity.RelationRoleToRight;
import org.olat.core.commons.persistence.DB;
import org.olat.course.groupsandrights.CourseRightsEnum;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.RelationRoleVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 31 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RelationRolesWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private IdentityRelationshipService identityRelationshipService;
	
	@Test
	public void getRelationRoles()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		List<RelationRight> rights = identityRelationshipService.getAvailableRights();
		RelationRole relationRole = identityRelationshipService.createRole(role, rights);
		dbInstance.commit();
		Assert.assertNotNull(relationRole);

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("relations").path("roles").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RelationRoleVO> relationRoleVoes = parseRelationRoleArray(response.getEntity());
		Assert.assertNotNull(relationRoleVoes);
		Assert.assertFalse(relationRoleVoes.isEmpty());
		
		boolean found = false;
		for(RelationRoleVO relationRoleVo:relationRoleVoes) {
			if(role.equals(relationRoleVo.getRole())) {
				found = true;
			}
		}

		Assert.assertTrue(found);
		
		conn.shutdown();
	}
	
	@Test
	public void createRelationRole()
	throws IOException, URISyntaxException {
		RelationRoleVO vo = new RelationRoleVO();
		vo.setRole("REST role");
		vo.setExternalId("REST-RO-1");
		vo.setExternalRef("REST-ID-CEL-1");
		vo.setManagedFlags("delete");
		vo.setRights(Collections.singletonList(CourseRightsEnum.viewCourseCalendar.name()));

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("relations").path("roles").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		RelationRoleVO savedVo = conn.parse(response, RelationRoleVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertEquals("REST role", savedVo.getRole());
		Assert.assertEquals("REST-RO-1", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-CEL-1", savedVo.getExternalRef());
		Assert.assertEquals("delete", savedVo.getManagedFlags());
		
		List<String> rights = savedVo.getRights();
		Assert.assertNotNull(rights);
		Assert.assertEquals(1, rights.size());
		Assert.assertEquals(CourseRightsEnum.viewCourseCalendar.name(), rights.get(0));
		
		// checked database
		RelationRole relationRole = identityRelationshipService.getRole(savedVo.getKey());
		Assert.assertNotNull(relationRole);
		Assert.assertEquals("REST role", relationRole.getRole());
		Assert.assertEquals("REST-RO-1", relationRole.getExternalId());
		Assert.assertEquals("REST-ID-CEL-1", relationRole.getExternalRef());
		Assert.assertNotNull(relationRole.getManagedFlags());
		Assert.assertEquals(1, relationRole.getManagedFlags().length);
		Assert.assertEquals(RelationRoleManagedFlag.delete, relationRole.getManagedFlags()[0]);
		
		Set<RelationRoleToRight> roleToRights = relationRole.getRights();
		Assert.assertNotNull(roleToRights);
		Assert.assertEquals(1, roleToRights.size());
		Assert.assertEquals(CourseRightsEnum.viewCourseCalendar.name(), roleToRights.iterator().next().getRelationRight().getRight());
		
		conn.shutdown();
	}
	
	@Test
	public void updateRelationRole() 
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		List<RelationRight> rights = identityRelationshipService.getAvailableRights();
		RelationRole relationRole = identityRelationshipService.createRole(role, rights);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relationRole);
		
		RelationRoleVO vo = new RelationRoleVO();
		vo.setKey(relationRole.getKey());
		vo.setRole("REST update role");
		vo.setExternalId("REST-UPRO-1");
		vo.setExternalRef("REST-ID-UPCEL-1");
		vo.setManagedFlags("all");
		vo.setRights(Collections.singletonList(CourseRightsEnum.viewEfficiencyStatement.name()));

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("relations")
				.path("roles").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		// checked VO
		RelationRoleVO savedVo = conn.parse(response, RelationRoleVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertEquals("REST update role", savedVo.getRole());
		Assert.assertEquals("REST-UPRO-1", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-UPCEL-1", savedVo.getExternalRef());
		Assert.assertEquals("all", savedVo.getManagedFlags());
		
		List<String> savedRights = savedVo.getRights();
		Assert.assertNotNull(savedRights);
		Assert.assertEquals(1, savedRights.size());
		Assert.assertEquals(CourseRightsEnum.viewEfficiencyStatement.name(), savedRights.get(0));
		
		// checked database
		RelationRole persistedRelationRole = identityRelationshipService.getRole(savedVo.getKey());
		Assert.assertNotNull(persistedRelationRole);
		Assert.assertEquals("REST update role", persistedRelationRole.getRole());
		Assert.assertEquals("REST-UPRO-1", persistedRelationRole.getExternalId());
		Assert.assertEquals("REST-ID-UPCEL-1", persistedRelationRole.getExternalRef());
		Assert.assertNotNull(persistedRelationRole.getManagedFlags());
		Assert.assertEquals(1, persistedRelationRole.getManagedFlags().length);
		Assert.assertEquals(RelationRoleManagedFlag.all, persistedRelationRole.getManagedFlags()[0]);
		
		Set<RelationRoleToRight> roleToRights = persistedRelationRole.getRights();
		Assert.assertNotNull(roleToRights);
		Assert.assertEquals(1, roleToRights.size());
		Assert.assertEquals(CourseRightsEnum.viewEfficiencyStatement.name(), roleToRights.iterator().next().getRelationRight().getRight());
		
		conn.shutdown();
	}
	
	@Test
	public void updateRelationRole_withKey() 
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		List<RelationRight> rights = identityRelationshipService.getAvailableRights();
		RelationRole relationRole = identityRelationshipService.createRole(role, rights);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relationRole);
		
		RelationRoleVO vo = new RelationRoleVO();
		vo.setRole("REST update role");
		vo.setExternalId("REST-UPRO-1");
		vo.setExternalRef("REST-ID-UPCEL-1");
		vo.setManagedFlags("all");
		vo.setRights(Collections.singletonList(CourseRightsEnum.viewEfficiencyStatement.name()));

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("relations")
				.path("roles").path(relationRole.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		RelationRoleVO savedVo = conn.parse(response, RelationRoleVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertEquals("REST update role", savedVo.getRole());
		Assert.assertEquals("REST-UPRO-1", savedVo.getExternalId());
		Assert.assertEquals("REST-ID-UPCEL-1", savedVo.getExternalRef());
		Assert.assertEquals("all", savedVo.getManagedFlags());
		
		List<String> savedRights = savedVo.getRights();
		Assert.assertNotNull(savedRights);
		Assert.assertEquals(1, savedRights.size());
		Assert.assertEquals(CourseRightsEnum.viewEfficiencyStatement.name(), savedRights.get(0));
		
		// checked database
		RelationRole persistedRelationRole = identityRelationshipService.getRole(savedVo.getKey());
		Assert.assertNotNull(persistedRelationRole);
		Assert.assertEquals("REST update role", persistedRelationRole.getRole());
		Assert.assertEquals("REST-UPRO-1", persistedRelationRole.getExternalId());
		Assert.assertEquals("REST-ID-UPCEL-1", persistedRelationRole.getExternalRef());
		Assert.assertNotNull(persistedRelationRole.getManagedFlags());
		Assert.assertEquals(1, persistedRelationRole.getManagedFlags().length);
		Assert.assertEquals(RelationRoleManagedFlag.all, persistedRelationRole.getManagedFlags()[0]);
		
		Set<RelationRoleToRight> roleToRights = persistedRelationRole.getRights();
		Assert.assertNotNull(roleToRights);
		Assert.assertEquals(1, roleToRights.size());
		Assert.assertEquals(CourseRightsEnum.viewEfficiencyStatement.name(), roleToRights.iterator().next().getRelationRight().getRight());
		
		conn.shutdown();
	}
	
	@Test
	public void deleteRelationRole()
	throws IOException, URISyntaxException {
		String role = UUID.randomUUID().toString();
		List<RelationRight> rights = identityRelationshipService.getAvailableRights();
		RelationRole relationRole = identityRelationshipService.createRole(role, rights);
		dbInstance.commit();
		Assert.assertNotNull(relationRole);
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("relations")
				.path("roles").path(relationRole.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RelationRole deletedRole = identityRelationshipService.getRole(relationRole.getKey());
		Assert.assertNull(deletedRole);
		
		conn.shutdown();
	}
	
	protected List<RelationRoleVO> parseRelationRoleArray(HttpEntity body) {
		try(InputStream in = body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RelationRoleVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
