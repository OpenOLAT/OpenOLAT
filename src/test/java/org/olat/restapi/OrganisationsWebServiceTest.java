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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

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
import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.OrganisationManagedFlag;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.OrganisationVO;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationsWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(OrganisationsWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private RepositoryService repositoryService;
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
		List<OrganisationVO> organisationVoes = parseOrganisationArray(response.getEntity());
		
		boolean found = false;
		for(OrganisationVO organisationVo:organisationVoes) {
			if(organisationVo.getKey().equals(organisation.getKey())) {
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void getOrganisationsByExternalId()
	throws IOException, URISyntaxException {
		String externalId = UUID.randomUUID().toString();
		Organisation organisation = organisationService.createOrganisation("REST Organisation-ext", "REST-organisation-ext", "", null, null);
		organisation.setExternalId(externalId);
		organisation = organisationService.updateOrganisation(organisation);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations")
				.queryParam("externalId", externalId).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationVO> organisationVoes = parseOrganisationArray(response.getEntity());
		
		Assert.assertNotNull(organisationVoes);
		Assert.assertEquals(1, organisationVoes.size());
		Assert.assertEquals(organisation.getKey(), organisationVoes.get(0).getKey());
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
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
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
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
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
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
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
	
	@Test
	public void updateAndMoveOrganisation_members()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation parentOrganisation = organisationService.createOrganisation("REST Parent Organisation 4", "REST-p-4-organisation", "", null, null);
		OrganisationType type = organisationService.createOrganisationType("REST Type", "rest-type", "A type for REST");
		Organisation organisation1 = organisationService.createOrganisation("REST Organisation 1", "REST-p-5-organisation", "", parentOrganisation, type);
		Organisation organisation2 = organisationService.createOrganisation("REST Organisation 2", "REST-p-6-organisation", "", parentOrganisation, type);
		dbInstance.commitAndCloseSession();

		Identity userManagerOrg1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-mgr-1");
		Identity userManagerOrg2 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-mgr-2");
		Identity user1 = JunitTestHelper.createAndPersistIdentityAsRndUser("user-1");
		Identity user2 = JunitTestHelper.createAndPersistIdentityAsRndUser("user2");
		organisationService.addMember(organisation1, userManagerOrg1, OrganisationRoles.usermanager);
		organisationService.addMember(organisation2, userManagerOrg2, OrganisationRoles.usermanager);
		organisationService.addMember(organisation1, user1, OrganisationRoles.user);
		organisationService.addMember(organisation2, user2, OrganisationRoles.user);
		dbInstance.commitAndCloseSession();
		
		OrganisationVO organisationVo2 = OrganisationVO.valueOf(organisation2);
		organisationVo2.setParentOrganisationKey(organisation1.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, organisationVo2);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		OrganisationVO savedVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals(organisation2.getKey(), savedVo.getKey());
		Assert.assertEquals(organisation1.getKey(), savedVo.getParentOrganisationKey());
		Assert.assertEquals(parentOrganisation.getKey(), savedVo.getRootOrganisationKey());
		
		// checked database
		Organisation savedOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisation);
		Assert.assertEquals(savedVo.getKey(), savedOrganisation.getKey());
		Assert.assertEquals(organisation1, savedOrganisation.getParent());
		Assert.assertEquals(parentOrganisation, savedOrganisation.getRoot());
		
		// check memberships
		
		// user manager of organization 1
		GroupMembership userManagerOrg1Membership = groupDao.getMembership(organisation1.getGroup(), userManagerOrg1, OrganisationRoles.usermanager.name());
		Assert.assertNotNull(userManagerOrg1Membership);
		Assert.assertEquals(GroupMembershipInheritance.root, userManagerOrg1Membership.getInheritanceMode());
		
		GroupMembership userManagerOrg12Membership = groupDao.getMembership(organisation2.getGroup(), userManagerOrg1, OrganisationRoles.usermanager.name());
		Assert.assertNotNull(userManagerOrg12Membership);
		Assert.assertEquals(GroupMembershipInheritance.inherited, userManagerOrg12Membership.getInheritanceMode());
		
		// user manager of organization 2
		GroupMembership userManagerOrg2Membership = groupDao.getMembership(organisation1.getGroup(), userManagerOrg2, OrganisationRoles.usermanager.name());
		Assert.assertNull(userManagerOrg2Membership);
		
		GroupMembership userManagerOrg22Membership = groupDao.getMembership(organisation2.getGroup(), userManagerOrg2, OrganisationRoles.usermanager.name());
		Assert.assertNotNull(userManagerOrg22Membership);
		Assert.assertEquals(GroupMembershipInheritance.root, userManagerOrg22Membership.getInheritanceMode());
		
		// user 1
		GroupMembership user1Membership = groupDao.getMembership(organisation1.getGroup(), user1, OrganisationRoles.user.name());
		Assert.assertNotNull(user1Membership);
		Assert.assertEquals(GroupMembershipInheritance.none, user1Membership.getInheritanceMode());
		
		GroupMembership user12Membership = groupDao.getMembership(organisation2.getGroup(), user1, OrganisationRoles.user.name());
		Assert.assertNull(user12Membership);
		
		// user 2
		GroupMembership user2Membership = groupDao.getMembership(organisation1.getGroup(), user2, OrganisationRoles.user.name());
		Assert.assertNull(user2Membership);
		
		GroupMembership user22Membership = groupDao.getMembership(organisation2.getGroup(), user2, OrganisationRoles.user.name());
		Assert.assertNotNull(user22Membership);
		Assert.assertEquals(GroupMembershipInheritance.none, user22Membership.getInheritanceMode());
	}
	
	@Test
	public void updateAndMoveRootOrganisation()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Organisation rootOrganisation = organisationService.createOrganisation("ROOT Organisation 10", "REST-p-10-organisation", "", null, null);
		Organisation rootOrganisation2 = organisationService.createOrganisation("REST Organisation 10.1", "REST-p-10-1-organisation", "", null, null);
		dbInstance.commitAndCloseSession();
		
		OrganisationVO vo = OrganisationVO.valueOf(rootOrganisation2);
		vo.setParentOrganisationKey(rootOrganisation.getKey());

		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		MatcherAssert.assertThat(response.getStatusLine().getStatusCode(), Matchers.either(Matchers.is(200)).or(Matchers.is(201)));
		
		// checked VO
		OrganisationVO savedVo = conn.parse(response, OrganisationVO.class);
		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedVo.getKey());
		Assert.assertEquals(rootOrganisation.getKey(), savedVo.getParentOrganisationKey());
		Assert.assertEquals(rootOrganisation.getKey(), savedVo.getRootOrganisationKey());
		
		// checked database
		Organisation savedOrganisation = organisationService.getOrganisation(new OrganisationRefImpl(savedVo.getKey()));
		Assert.assertNotNull(savedOrganisation);
		Assert.assertEquals(savedVo.getKey(), savedOrganisation.getKey());
		Assert.assertEquals(rootOrganisation, savedOrganisation.getParent());
		Assert.assertEquals(rootOrganisation, savedOrganisation.getRoot());
	}
	
	@Test
	public void deleteOrganisation()
	throws IOException, URISyntaxException {
		IdentityWithLogin admin = JunitTestHelper.createAndPersistRndUser("org-del-admin");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 51", "REST-51-organisation", "", null, null);
		organisationService.addMember(organisation, admin.getIdentity(), OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(admin));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Organisation deletedOrganisation = organisationService.getOrganisation(organisation);
		Assert.assertNull(deletedOrganisation);
	}
	
	@Test
	public void deleteOrganisationDefaultForbidden()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.getDefaultOrganisation();
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Organisation defOrganisation = organisationService.getOrganisation(organisation);
		Assert.assertNotNull(defOrganisation);
		Assert.assertEquals(organisation, defOrganisation);
	}
	
	@Test
	public void deleteOrganisationForbidden()
	throws IOException, URISyntaxException {
		Organisation organisation = organisationService.createOrganisation("REST Organisation 53", "REST-53-organisation", "", null, null);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Organisation deletedOrganisation = organisationService.getOrganisation(organisation);
		Assert.assertNotNull(deletedOrganisation);
	}
	
	@Test
	public void getMembers_users()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-11");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 5", "REST-p-5-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.user);
		dbInstance.commit();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString())
				.path("users").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertEquals(member.getKey(), users.get(0).getKey());
	}
	
	@Test
	public void addMember_principal()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-12");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 6", "REST-p-6-organisation", "", null, null);
		dbInstance.commit();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString())
				.path("principals").path(member.getKey().toString()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> principals = organisationService.getMembersIdentity(organisation, OrganisationRoles.principal);
		Assert.assertNotNull(principals);
		Assert.assertEquals(1, principals.size());
		Assert.assertEquals(member, principals.get(0));
	}
	
	@Test
	public void addMembers_author()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity author1 = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-14");
		Identity author2 = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-15");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 7", "REST-p-7-organisation", "", null, null);
		dbInstance.commit();
		
		
		UserVO[] authors = new UserVO[] {
				UserVOFactory.get(author1),
				UserVOFactory.get(author2)
			};
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString())
				.path("authors").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, authors);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> authorList = organisationService.getMembersIdentity(organisation, OrganisationRoles.author);
		Assert.assertNotNull(authorList);
		Assert.assertEquals(2, authorList.size());
	}
	
	@Test
	public void removeMember_administrator()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-13");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 7", "REST-p-7-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.administrator);
		dbInstance.commit();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString())
				.path("administrators").path(member.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Identity> administators = organisationService.getMembersIdentity(organisation, OrganisationRoles.administrator);
		Assert.assertNotNull(administators);
		Assert.assertTrue(administators.isEmpty());
	}
	
	@Test
	public void getRepositoryEntries()
	throws IOException, URISyntaxException {
		// prepare an organization with a course
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-13");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 8", "REST-p-8-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.administrator);
		dbInstance.commit();
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(member);
		repositoryService.addOrganisation(course, organisation);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path(organisation.getKey().toString())
				.path("entries").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<RepositoryEntryVO> entries = parseRepositoryEntryArray(response.getEntity());
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(course.getKey(), entries.get(0).getKey());
	}
	
	@Test
	public void getMemberships()
	throws IOException, URISyntaxException {
		// prepare an organization with a course
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-14");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 9", "REST-p-9-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.author);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("membership")
				.path("user").path(member.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationVO> organisations = parseOrganisationArray(response.getEntity());
		Assert.assertNotNull(organisations);
		Assert.assertEquals(1, organisations.size());
		Assert.assertEquals(organisationService.getDefaultOrganisation().getKey(), organisations.get(0).getKey());
	}
	
	/**
	 * Default is with inheritance but be sure.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getMemberships_usermanager()
	throws IOException, URISyntaxException {
		// prepare an organization with a course
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-16");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 14", "REST-p-14-organisation", "", null, null);
		Organisation subOrganisation = organisationService.createOrganisation("REST Organisation 14b", "REST-p-14-b-organisation", "", organisation, null);
		Organisation otherOrganisation = organisationService.createOrganisation("REST Organisation 15", "REST-p-16-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.rolesmanager);
		organisationService.addMember(otherOrganisation, member, OrganisationRoles.rolesmanager);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("membership")
				.path("rolesmanager").path(member.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationVO> organisations = parseOrganisationArray(response.getEntity());
		assertThat(organisations)
			.extracting(OrganisationVO::getKey)
			.containsExactlyInAnyOrder(organisation.getKey(), otherOrganisation.getKey(), subOrganisation.getKey());
	}
	
	@Test
	public void getMemberships_usermanager_withInheritance()
	throws IOException, URISyntaxException {
		// prepare an organization with a course
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-17");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 16", "REST-p-16-organisation", "", null, null);
		Organisation subOrganisation = organisationService.createOrganisation("REST Organisation 16b", "REST-p-16-b-organisation", "", organisation, null);
		Organisation otherOrganisation = organisationService.createOrganisation("REST Organisation 17", "REST-p-17-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.usermanager);
		organisationService.addMember(otherOrganisation, member, OrganisationRoles.usermanager);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("membership")
				.path("usermanager").path(member.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationVO> organisations = parseOrganisationArray(response.getEntity());
		assertThat(organisations)
			.extracting(OrganisationVO::getKey)
			.containsExactlyInAnyOrder(organisation.getKey(), otherOrganisation.getKey(), subOrganisation.getKey());
	}
	
	@Test
	public void getMemberships_usermanager_notInherited()
	throws IOException, URISyntaxException {
		// prepare an organization with a course
		Identity member = JunitTestHelper.createAndPersistIdentityAsRndUser("org-member-15");
		Organisation organisation = organisationService.createOrganisation("REST Organisation 10", "REST-p-10-organisation", "", null, null);
		Organisation subOrganisation = organisationService.createOrganisation("REST Organisation 11", "REST-p-11-organisation", "", organisation, null);
		Organisation otherOrganisation = organisationService.createOrganisation("REST Organisation 12", "REST-p-12-organisation", "", null, null);
		organisationService.addMember(organisation, member, OrganisationRoles.usermanager);
		organisationService.addMember(otherOrganisation, member, OrganisationRoles.usermanager);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("organisations").path("membership")
				.path("usermanager").path(member.getKey().toString())
				.queryParam("withInheritance", "false").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<OrganisationVO> organisations = parseOrganisationArray(response.getEntity());
		assertThat(organisations)
			.extracting(OrganisationVO::getKey)
			.containsExactlyInAnyOrder(organisation.getKey(), otherOrganisation.getKey())
			.doesNotContain(subOrganisation.getKey());
	}
	
	protected List<UserVO> parseUserArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<OrganisationVO> parseOrganisationArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<OrganisationVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<RepositoryEntryVO> parseRepositoryEntryArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RepositoryEntryVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
