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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseModule;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.CatalogEntryVO;
import org.olat.restapi.support.vo.CatalogEntryVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Description:<br>
 * Test the catalog RESt API
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CatalogTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CatalogTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private OLATResourceManager orm;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	
	private Identity admin, id1;
	private IdentityWithLogin id2;
	private CatalogEntry root1, entry1, entry2, subEntry11, subEntry12;
	private CatalogEntry entryToMove1, entryToMove2, subEntry13move;
	
	@Before
	public void setUp() throws Exception {
		id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-catalog-one");
		id2 = JunitTestHelper.createAndPersistRndUser("rest-catalog-two");
		admin = JunitTestHelper.findIdentityByLogin("administrator");

		//create a catalog
		root1 = catalogManager.getRootCatalogEntries().get(0);
		
		entry1 = catalogManager.createCatalogEntry();
		entry1.setType(CatalogEntry.TYPE_NODE);
		entry1.setName("Entry-1");
		entry1.setDescription("Entry-description-1");
		catalogManager.addCatalogEntry(root1, entry1);
		
		dbInstance.intermediateCommit();
		entry1 = catalogManager.loadCatalogEntry(entry1);
		securityGroupDao.addIdentityToSecurityGroup(admin, entry1.getOwnerGroup());
		
		subEntry11 = catalogManager.createCatalogEntry();
		subEntry11.setType(CatalogEntry.TYPE_NODE);
		subEntry11.setName("Sub-entry-11");
		subEntry11.setDescription("Sub-entry-description-11");
		catalogManager.addCatalogEntry(entry1, subEntry11);
		
		subEntry12 = catalogManager.createCatalogEntry();
		subEntry12.setType(CatalogEntry.TYPE_NODE);
		subEntry12.setName("Sub-entry-12");
		subEntry12.setDescription("Sub-entry-description-12");
		catalogManager.addCatalogEntry(entry1, subEntry12);
		
		entry2 = catalogManager.createCatalogEntry();
		entry2.setType(CatalogEntry.TYPE_NODE);
		entry2.setName("Entry-2");
		entry2.setDescription("Entry-description-2");
		catalogManager.addCatalogEntry(root1, entry2);
		
		entryToMove1 = catalogManager.createCatalogEntry();
		entryToMove1.setType(CatalogEntry.TYPE_NODE);
		entryToMove1.setName("Entry-1-to-move");
		entryToMove1.setDescription("Entry-description-1-to-move");
		catalogManager.addCatalogEntry(root1, entryToMove1);
		
		entryToMove2 = catalogManager.createCatalogEntry();
		entryToMove2.setType(CatalogEntry.TYPE_NODE);
		entryToMove2.setName("Entry-2-to-move");
		entryToMove2.setDescription("Entry-description-2-to-move");
		catalogManager.addCatalogEntry(root1, entryToMove2);
		
		subEntry13move = catalogManager.createCatalogEntry();
		subEntry13move.setType(CatalogEntry.TYPE_NODE);
		subEntry13move.setName("Sub-entry-13-move target");
		subEntry13move.setDescription("Sub-entry-description-13-move target");
		catalogManager.addCatalogEntry(root1, subEntry13move);
		
		dbInstance.intermediateCommit();
	}

	@Test
	public void testGetRoots() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CatalogEntryVO> vos = parseEntryArray(response.getEntity());
		assertNotNull(vos);
		assertEquals(1, vos.size());//Root-1
		
		conn.shutdown();
	}
	
	@Test
	public void testGetRootsWithPaging() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVOes vos = conn.parse(response, CatalogEntryVOes.class);

		assertNotNull(vos);
		assertNotNull(vos.getCatalogEntries());
		assertEquals(1, vos.getCatalogEntries().length);//Root-1
		assertEquals(1, vos.getTotalCount());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetChild() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		assertEquals(entry1.getName(), vo.getName());
		assertEquals(entry1.getDescription(), vo.getDescription());

		conn.shutdown();
	}
	
	@Test
	public void testGetChildren() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(root1.getKey().toString()).path("children").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<CatalogEntryVO> vos = parseEntryArray(response.getEntity());
		assertNotNull(vos);
		assertTrue(vos.size() >= 2);

		conn.shutdown();
	}
	
	@Test
	public void testGetChildrenWithPaging() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(root1.getKey().toString()).path("children")
				.queryParam("start", "0").queryParam("limit", "2").build();
		
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVOes vos = conn.parse(response, CatalogEntryVOes.class);
		assertNotNull(vos);
		assertNotNull(vos.getCatalogEntries());
		assertTrue(vos.getCatalogEntries().length <= 2);
		assertTrue(vos.getTotalCount() >= 2);

		conn.shutdown();
	}
	
	@Test
	public void testPutCategoryJson() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CatalogEntryVO subEntry = new CatalogEntryVO();
		subEntry.setName("Sub-entry-1");
		subEntry.setDescription("Sub-entry-description-1");
		subEntry.setType(CatalogEntry.TYPE_NODE);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, subEntry);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		boolean saved = false;
		for(CatalogEntry child:children) {
			if(vo.getKey().equals(child.getKey())) {
				saved = true;
				break;
			}
		}
		
		assertTrue(saved);

		conn.shutdown();
	}
	
	@Test
	public void testPutCategoryQuery() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
				.queryParam("name", "Sub-entry-2")
				.queryParam("description", "Sub-entry-description-2")
				.queryParam("type", String.valueOf(CatalogEntry.TYPE_NODE)).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		boolean saved = false;
		for(CatalogEntry child:children) {
			if(vo.getKey().equals(child.getKey())) {
				saved = true;
				break;
			}
		}
		
		assertTrue(saved);

		conn.shutdown();
	}
	
	@Test
	public void testPutCatalogEntryJson() throws IOException, URISyntaxException {
		RepositoryEntry re = createRepository("put-cat-entry-json", 6458438l);

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CatalogEntryVO subEntry = new CatalogEntryVO();
		subEntry.setName("Sub-entry-1");
		subEntry.setDescription("Sub-entry-description-1");
		subEntry.setType(CatalogEntry.TYPE_NODE);
		subEntry.setRepositoryEntryKey(re.getKey());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, subEntry);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		CatalogEntry ce = null;
		for(CatalogEntry child:children) {
			if(vo.getKey().equals(child.getKey())) {
				ce = child;
				break;
			}
		}
		
		assertNotNull(ce);
		assertNotNull(ce.getRepositoryEntry());
		assertEquals(re.getKey(), ce.getRepositoryEntry().getKey());

		conn.shutdown();
	}
	
	@Test
	public void testPutCatalogEntryQuery() throws IOException, URISyntaxException {
		RepositoryEntry re = createRepository("put-cat-entry-query", 6458439l);

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
				.queryParam("name", "Sub-entry-2")
				.queryParam("description", "Sub-entry-description-2")
				.queryParam("type", String.valueOf(CatalogEntry.TYPE_NODE))
				.queryParam("repoEntryKey", re.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		CatalogEntry ce = null;
		for(CatalogEntry child:children) {
			if(vo.getKey().equals(child.getKey())) {
				ce = child;
				break;
			}
		}
		
		assertNotNull(ce);
		assertNotNull(ce.getRepositoryEntry());
		assertEquals(re.getKey(), ce.getRepositoryEntry().getKey());

		conn.shutdown();
	}
	
	@Test
	public void testUpdateCatalogEntryJson() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CatalogEntryVO entry = new CatalogEntryVO();
		entry.setName("Entry-1-b");
		entry.setDescription("Entry-description-1-b");
		entry.setType(CatalogEntry.TYPE_NODE);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		method.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, entry);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entry1);
		assertEquals("Entry-1-b", updatedEntry.getName());
		assertEquals("Entry-description-1-b", updatedEntry.getDescription());

		conn.shutdown();
	}
	
	@Test
	public void testUpdateAndMoveCatalogEntryJson() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		CatalogEntryVO entry = new CatalogEntryVO();
		entry.setName("Entry-2-moved-down");
		entry.setDescription("Entry-description-2-moved-down");
		entry.setType(CatalogEntry.TYPE_NODE);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entryToMove2.getKey().toString()).queryParam("newParentKey", subEntry13move.getKey().toString()).build();

		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		method.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, entry);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entryToMove2);
		assertEquals("Entry-2-moved-down", updatedEntry.getName());
		assertEquals("Entry-description-2-moved-down", updatedEntry.getDescription());
		assertNotNull(updatedEntry.getParent());
		assertTrue(updatedEntry.getParent().equalsByPersistableKey(subEntry13move));

		conn.shutdown();
	}
	
	@Test
	public void testUpdateCatalogEntryQuery() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addEntity(method, new BasicNameValuePair("name", "Entry-2-b"),
				new BasicNameValuePair("description", "Entry-description-2-b"),
				new BasicNameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)));

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entry2);
		assertEquals("Entry-2-b", updatedEntry.getName());
		assertEquals("Entry-description-2-b", updatedEntry.getDescription());

		conn.shutdown();
	}
	
	@Test
	public void testUpdateCatalogEntryForm() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addEntity(method, new BasicNameValuePair("name", "Entry-2-c"),
				new BasicNameValuePair("description", "Entry-description-2-c"),
				new BasicNameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)));

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entry2);
		assertEquals("Entry-2-c", updatedEntry.getName());
		assertEquals("Entry-description-2-c", updatedEntry.getDescription());

		conn.shutdown();
	}
	
	@Test
	public void testMoveCatalogEntryForm() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entryToMove1.getKey().toString()).build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addEntity(method, new BasicNameValuePair("newParentKey", subEntry13move.getKey().toString()));

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CatalogEntryVO vo = conn.parse(response, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entryToMove1);
		assertEquals("Entry-1-to-move", updatedEntry.getName());
		assertEquals("Entry-description-1-to-move", updatedEntry.getDescription());
		assertNotNull(updatedEntry.getParent());
		assertTrue(updatedEntry.getParent().equalsByPersistableKey(subEntry13move));

		conn.shutdown();
	}
	
	@Test
	public void testDeleteCatalogEntry() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<CatalogEntry> entries = catalogManager.getChildrenOf(root1);
		for(CatalogEntry entry:entries) {
			assertFalse(entry.getKey().equals(entry2.getKey()));
		}

		conn.shutdown();
	}
	
	@Test
	public void testGetOwners() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> voes = parseUserArray(response.getEntity());
		assertNotNull(voes);
		
		CatalogEntry entry = catalogManager.loadCatalogEntry(entry1.getKey());
		List<Identity> identities = securityGroupDao.getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
		assertNotNull(identities);
		assertEquals(identities.size(), voes.size());

		conn.shutdown();
	}
	
	@Test
	public void testGetOwner() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		//admin is owner
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(admin.getKey().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		UserVO vo = conn.parse(response, UserVO.class);
		assertNotNull(vo);
		
		//id1 is not owner
		uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(id1.getKey().toString()).build();
		method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);

		response = conn.execute(method);
		assertEquals(404, response.getStatusLine().getStatusCode());

		conn.shutdown();
	}
	
	@Test
	public void testAddOwner() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(id1.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		CatalogEntry entry = catalogManager.loadCatalogEntry(entry1.getKey());
		List<Identity> identities = securityGroupDao.getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
		boolean found = false;
		for(Identity identity:identities) {
			if(identity.getKey().equals(id1.getKey())) {
				found = true;
			}
		}
		assertTrue(found);

		conn.shutdown();
	}
	
	@Test
	public void testRemoveOwner() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(id1.getUser().getKey().toString()).build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
	
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		CatalogEntry entry = catalogManager.loadCatalogEntry(entry1.getKey());
		List<Identity> identities = securityGroupDao.getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
		boolean found = false;
		for(Identity identity:identities) {
			if(identity.getKey().equals(id1.getKey())) {
				found = true;
			}
		}
		assertFalse(found);

		conn.shutdown();
	}
	
	@Test
	public void testBasicSecurityPutCall() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id2));

		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
				.queryParam("name", "Not-sub-entry-3")
				.queryParam("description", "Not-sub-entry-description-3")
				.queryParam("type", String.valueOf(CatalogEntry.TYPE_NODE))
				.build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);

		HttpResponse response = conn.execute(method);
		assertEquals(401, response.getStatusLine().getStatusCode());
		
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		boolean saved = false;
		for(CatalogEntry child:children) {
			if("Not-sub-entry-3".equals(child.getName())) {
				saved = true;
				break;
			}
		}
		
		assertFalse(saved);

		conn.shutdown();
	}
	
	protected List<CatalogEntryVO> parseEntryArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<CatalogEntryVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<UserVO> parseUserArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	
	private RepositoryEntry createRepository(String displayName, final Long resourceableId) {
		OLATResourceable resourceable = new OLATResourceable() {
			public String getResourceableTypeName() {	return CourseModule.ORES_TYPE_COURSE;}
			public Long getResourceableId() {return resourceableId;}
		};

		// create course and persist as OLATResourceImpl

		OLATResource r = orm.findResourceable(resourceable);
		if(r == null) {
			r = orm.createOLATResourceInstance(resourceable);
		}
		dbInstance.saveObject(r);
		dbInstance.intermediateCommit();
		
		RepositoryEntry d = repositoryManager.lookupRepositoryEntry(resourceable, false);
		if(d == null) {
			Organisation defOrganisation = organisationService.getDefaultOrganisation();
			d = repositoryService.create(null, "Rei Ayanami", "-", displayName, "Repo entry",
					r, RepositoryEntryStatusEnum.trash, defOrganisation);
			dbInstance.saveObject(d);
		}
		dbInstance.intermediateCommit();
		return d;
	}
}
