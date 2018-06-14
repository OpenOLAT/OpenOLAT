/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.MailPackage;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.RepositoryEntryAccessVO;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.RepositoryEntryVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntriesTest extends OlatJerseyTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(RepositoryEntriesTest.class);
	private static final Roles ADMIN_ROLES = new Roles(true, false, false, false, false, false, false);

	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void testGetEntries() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<RepositoryEntryVO> entryVoes = parseRepoArray(response.getEntity());
		assertNotNull(entryVoes);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetEntriesWithPaging() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.queryParam("start", "0").queryParam("limit", "25").build();
		
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryVOes entryVoes = conn.parse(response, RepositoryEntryVOes.class);

		assertNotNull(entryVoes);
		assertNotNull(entryVoes.getRepositoryEntries());
		assertTrue(entryVoes.getRepositoryEntries().length <= 25);
		assertTrue(entryVoes.getTotalCount() >= entryVoes.getRepositoryEntries().length);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetEntry() throws IOException, URISyntaxException {
		RepositoryEntry re = createRepository("Test GET repo entry");

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries/" + re.getKey()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryVO entryVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(entryVo);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetEntry_managed() throws IOException, URISyntaxException {
		RepositoryEntry re = createRepository("Test GET repo entry");
		re.setManagedFlagsString("all");
		re = dbInstance.getCurrentEntityManager().merge(re);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.queryParam("managed", "true").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<RepositoryEntryVO> entryVoes = parseRepoArray(response.getEntity());
		Assert.assertNotNull(entryVoes);
		Assert.assertFalse(entryVoes.isEmpty());
		//only repo entries with managed flags
		for(RepositoryEntryVO entryVo:entryVoes) {
			Assert.assertNotNull(entryVo.getManagedFlags());
			Assert.assertTrue(entryVo.getManagedFlags().length() > 0);
		}
		
		conn.shutdown();
	}
	
	@Test
	public void testUpdateRepositoryEntry() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		RepositoryEntryVO repoVo = new RepositoryEntryVO();
		repoVo.setKey(re.getKey());
		repoVo.setDisplayname("New display name");
		repoVo.setExternalId("New external ID");
		repoVo.setExternalRef("New external ref");
		repoVo.setManagedFlags("booking,delete");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, repoVo);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO updatedVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(updatedVo);
		
		Assert.assertEquals("New display name", updatedVo.getDisplayname());
		Assert.assertEquals("New external ID", updatedVo.getExternalId());
		Assert.assertEquals("New external ref", updatedVo.getExternalRef());
		Assert.assertEquals("booking,delete", updatedVo.getManagedFlags());
		
		conn.shutdown();
		
		RepositoryEntry reloadedRe = repositoryManager.lookupRepositoryEntry(re.getKey());
		assertNotNull(reloadedRe);

		Assert.assertEquals("New display name", reloadedRe.getDisplayname());
		Assert.assertEquals("New external ID", reloadedRe.getExternalId());
		Assert.assertEquals("New external ref", reloadedRe.getExternalRef());
		Assert.assertEquals("booking,delete", reloadedRe.getManagedFlagsString());
	}
	
	@Test
	public void testUpdateRepositoryEntry_lifecycle() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		RepositoryEntryVO repoVo = new RepositoryEntryVO();
		repoVo.setKey(re.getKey());
		repoVo.setDisplayname("New display name bis");
		repoVo.setExternalId("New external ID bis");
		repoVo.setExternalRef("New external ref bis");
		repoVo.setManagedFlags("all");
		RepositoryEntryLifecycleVO cycleVo = new RepositoryEntryLifecycleVO();
		cycleVo.setLabel("Cycle");
		cycleVo.setSoftkey("The secret cycle");
		cycleVo.setValidFrom(ObjectFactory.formatDate(new Date()));
		cycleVo.setValidTo(ObjectFactory.formatDate(new Date()));
		repoVo.setLifecycle(cycleVo);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, repoVo);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO updatedVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(updatedVo);
		
		Assert.assertEquals("New display name bis", updatedVo.getDisplayname());
		Assert.assertEquals("New external ID bis", updatedVo.getExternalId());
		Assert.assertEquals("New external ref bis", updatedVo.getExternalRef());
		Assert.assertEquals("all", updatedVo.getManagedFlags());
		Assert.assertNotNull(updatedVo.getLifecycle());
		Assert.assertEquals("Cycle", updatedVo.getLifecycle().getLabel());
		Assert.assertEquals("The secret cycle", updatedVo.getLifecycle().getSoftkey());
		Assert.assertNotNull(updatedVo.getLifecycle().getValidFrom());
		Assert.assertNotNull(updatedVo.getLifecycle().getValidTo());
		
		conn.shutdown();
		
		RepositoryEntry reloadedRe = repositoryManager.lookupRepositoryEntry(re.getKey());
		assertNotNull(reloadedRe);

		Assert.assertEquals("New display name bis", reloadedRe.getDisplayname());
		Assert.assertEquals("New external ID bis", reloadedRe.getExternalId());
		Assert.assertEquals("New external ref bis", reloadedRe.getExternalRef());
		Assert.assertEquals("all", reloadedRe.getManagedFlagsString());
		Assert.assertNotNull(reloadedRe.getLifecycle());
		Assert.assertEquals("Cycle", reloadedRe.getLifecycle().getLabel());
		Assert.assertEquals("The secret cycle", reloadedRe.getLifecycle().getSoftKey());
		Assert.assertNotNull(reloadedRe.getLifecycle().getValidFrom());
		Assert.assertNotNull(reloadedRe.getLifecycle().getValidTo());
	}
	
	@Test
	public void testImportCp() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("cp-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
			.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
			.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
			.addTextBody("filename", "cp-demo.zip")
			.addTextBody("resourcename", "CP demo")
			.addTextBody("displayname", "CP demo")
			.addTextBody("access", "3")
			.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("CP demo", re.getDisplayname());
		assertEquals(RepositoryEntry.ACC_USERS, re.getAccess());
		
		conn.shutdown();
	}
	
	@Test
	public void testImportTest() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("qti-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
				.addTextBody("filename", "qti-demo.zip")
				.addTextBody("resourcename", "QTI demo")
				.addTextBody("displayname", "QTI demo")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("QTI demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		
		conn.shutdown();
	}
	
	@Test
	public void testImportQuestionnaire() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("questionnaire-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
				.addTextBody("filename", "questionnaire-demo.zip")
				.addTextBody("resourcename", "Questionnaire demo")
				.addTextBody("displayname", "Questionnaire demo")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("Questionnaire demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		
		conn.shutdown();
	}
	
	@Test
	public void testImportWiki() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("wiki-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
				.addTextBody("filename", "wiki-demo.zip")
				.addTextBody("resourcename", "Wiki demo")
				.addTextBody("displayname", "Wiki demo")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("Wiki demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		
		conn.shutdown();
	}
	
	@Test
	public void testImportBlog() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("blog-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
				.addTextBody("filename", "blog-demo.zip")
				.addTextBody("resourcename", "Blog demo")
				.addTextBody("displayname", "Blog demo")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("Blog demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		
		conn.shutdown();
	}
	
	@Test
	public void getOwners() throws IOException, URISyntaxException {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsAuthor("author-1-" + UUID.randomUUID().toString());
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsAuthor("author-2-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addOwners(owner1, new IdentitiesAddEvent(owner1), re, null);
		repositoryManager.addOwners(owner1, new IdentitiesAddEvent(owner2), re, null);
		dbInstance.commitAndCloseSession();

		//get the owners
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).path("owners").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());//our 2
		
		int found = 0;
		for(UserVO user:users) {
			String login = user.getLogin();
			Assert.assertNotNull(login);
			if(owner1.getName().equals(login) || owner2.getName().equals(login)) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
		
		conn.shutdown();
	}
	
	@Test
	public void addOwner() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsAuthor("author-3-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("owners").path(owner.getKey().toString())
				.build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> owners = repositoryService.getMembers(re, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(1, owners.size());
		Assert.assertTrue(owners.contains(owner));
	}
	
	@Test
	public void addOwners() throws IOException, URISyntaxException {
		Identity owner1 = JunitTestHelper.createAndPersistIdentityAsRndUser("author-3b-");
		Identity owner2 = JunitTestHelper.createAndPersistIdentityAsRndUser("author-3c-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		UserVO[] newOwners = new UserVO[2];
		newOwners[0] = UserVOFactory.get(owner1);
		newOwners[1] = UserVOFactory.get(owner2);
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("entries").path(re.getKey().toString()).path("owners").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newOwners);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> owners = repositoryService.getMembers(re, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(2, owners.size());
		Assert.assertTrue(owners.contains(owner1));
		Assert.assertTrue(owners.contains(owner2));
	}
	
	@Test
	public void removeOwner() throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsAuthor("author-4-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addOwners(owner, new IdentitiesAddEvent(owner), re, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("owners").path(owner.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		//check
		List<Identity> owners = repositoryService.getMembers(re, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(0, owners.size());
		Assert.assertFalse(owners.contains(owner));
	}
	
	@Test
	public void getCoaches() throws IOException, URISyntaxException {
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsAuthor("coach-1-" + UUID.randomUUID().toString());
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsAuthor("coach-2-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addTutors(coach1, ADMIN_ROLES, new IdentitiesAddEvent(coach1), re, new MailPackage(false));
		repositoryManager.addTutors(coach1, ADMIN_ROLES, new IdentitiesAddEvent(coach2), re, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//get the coaches
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).path("coaches").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());//our 2
		
		int found = 0;
		for(UserVO user:users) {
			String login = user.getLogin();
			Assert.assertNotNull(login);
			if(coach1.getName().equals(login) || coach2.getName().equals(login)) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
		
		conn.shutdown();
	}
	
	@Test
	public void addCoach() throws IOException, URISyntaxException {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("coach-3-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("coaches").path(coach.getKey().toString())
				.build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> coaches = repositoryService.getMembers(re, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(1, coaches.size());
		Assert.assertTrue(coaches.contains(coach));
	}
	
	@Test
	public void addCoaches() throws IOException, URISyntaxException {
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-3b-");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-3c-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		UserVO[] newCoaches = new UserVO[2];
		newCoaches[0] = UserVOFactory.get(coach1);
		newCoaches[1] = UserVOFactory.get(coach2);
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("entries").path(re.getKey().toString()).path("coaches").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newCoaches);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> coaches = repositoryService.getMembers(re, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertEquals(2, coaches.size());
		Assert.assertTrue(coaches.contains(coach1));
		Assert.assertTrue(coaches.contains(coach2));
	}
	
	@Test
	public void removeCoach() throws IOException, URISyntaxException {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsAuthor("coach-4-" + UUID.randomUUID());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addTutors(coach, ADMIN_ROLES, new IdentitiesAddEvent(coach), re, new MailPackage(false));
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("coaches").path(coach.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		//check
		List<Identity> coaches = repositoryService.getMembers(re, GroupRoles.coach.name());
		Assert.assertNotNull(coaches);
		Assert.assertTrue(coaches.isEmpty());
		Assert.assertFalse(coaches.contains(coach));
	}
	
	@Test
	public void getParticipants() throws IOException, URISyntaxException {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsAuthor("participant-1-" + UUID.randomUUID().toString());
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsAuthor("participant-2-" + UUID.randomUUID().toString());
		Roles part1Roles = securityManager.getRoles(participant1);
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addParticipants(participant1, part1Roles, new IdentitiesAddEvent(participant1), re, null);
		repositoryManager.addParticipants(participant1, part1Roles, new IdentitiesAddEvent(participant2), re, null);
		dbInstance.commitAndCloseSession();

		//get the coaches
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).path("participants").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity());
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());//our 2 
		
		int found = 0;
		for(UserVO user:users) {
			String login = user.getLogin();
			Assert.assertNotNull(login);
			if(participant1.getName().equals(login) || participant2.getName().equals(login)) {
				found++;
			}
		}
		Assert.assertEquals(2, found);
		conn.shutdown();
	}
	
	@Test
	public void addParticipant() throws IOException, URISyntaxException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsAuthor("participant-3-" + UUID.randomUUID().toString());
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("participants").path(participant.getKey().toString())
				.build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> participants = repositoryService.getMembers(re, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(1, participants.size());
		Assert.assertTrue(participants.contains(participant));
	}
	
	@Test
	public void addParticipants() throws IOException, URISyntaxException {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-3b-");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-3c-");
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//add an owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		//add the 2 participants to the course
		UserVO[] newParticipants = new UserVO[2];
		newParticipants[0] = UserVOFactory.get(participant1);
		newParticipants[1] = UserVOFactory.get(participant2);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries")
				.path(re.getKey().toString()).path("participants").build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newParticipants);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
		
		//check
		List<Identity> participants = repositoryService.getMembers(re, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(participant1));
		Assert.assertTrue(participants.contains(participant2));
	}
	
	@Test
	public void testRemoveParticipant() throws IOException, URISyntaxException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsAuthor("participant-4-" + UUID.randomUUID().toString());
		Roles partRoles = securityManager.getRoles(participant);
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryManager.addParticipants(participant, partRoles, new IdentitiesAddEvent(participant), re, null);
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("participants").path(participant.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		conn.shutdown();
		
		//check
		List<Identity> participants = repositoryService.getMembers(re, GroupRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertTrue(participants.isEmpty());
		Assert.assertFalse(participants.contains(participant));
	}
	
	@Test
	public void getAccess() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("access").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryAccessVO accessVo = conn.parse(response, RepositoryEntryAccessVO.class);
		conn.shutdown();
		
		//check
		Assert.assertNotNull(accessVo);
		Assert.assertEquals(re.getKey(), accessVo.getRepoEntryKey());
		Assert.assertEquals(re.getAccess(), accessVo.getAccess());
		Assert.assertEquals(re.isMembersOnly(), accessVo.isMembersOnly());
	}
	
	@Test
	public void updateAccess() throws IOException, URISyntaxException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry(false);
		dbInstance.commitAndCloseSession();
		Assert.assertFalse(re.isMembersOnly());

		//remove the owner
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		RepositoryEntryAccessVO accessVo = new RepositoryEntryAccessVO();
		accessVo.setAccess(RepositoryEntry.ACC_OWNERS);
		accessVo.setMembersOnly(true);
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo/entries").path(re.getKey().toString()).path("access").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, accessVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryAccessVO updatedAccessVo = conn.parse(response, RepositoryEntryAccessVO.class);
		conn.shutdown();
		
		// check return value
		Assert.assertNotNull(updatedAccessVo);
		Assert.assertEquals(re.getKey(), updatedAccessVo.getRepoEntryKey());
		Assert.assertEquals(RepositoryEntry.ACC_OWNERS, updatedAccessVo.getAccess());
		Assert.assertEquals(true, updatedAccessVo.isMembersOnly());
		
		// check database value
		RepositoryEntry updatedRe = repositoryService.loadByKey(re.getKey());
		Assert.assertEquals(RepositoryEntry.ACC_OWNERS, updatedRe.getAccess());
		Assert.assertEquals(true, updatedRe.isMembersOnly());
	}

	private List<RepositoryEntryVO> parseRepoArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RepositoryEntryVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private List<UserVO> parseUserArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private RepositoryEntry createRepository(String displayName) {
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		
		OLATResource r =  rm.createOLATResourceInstance("DummyType");
		dbInstance.saveObject(r);
		dbInstance.intermediateCommit();

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry d = repositoryService.create(null, displayName, "-", displayName, "Repo entry", r, 0, defOrganisation);
		dbInstance.commit();
		return d;
	}
}