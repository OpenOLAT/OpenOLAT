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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.catalog.CatalogEntry;
import org.olat.catalog.CatalogManager;
import org.olat.catalog.restapi.CatalogEntryVO;
import org.olat.catalog.restapi.CatalogEntryVOes;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.restapi.UserVO;

/**
 * 
 * Description:<br>
 * Test the catalog RESt API
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CatalogTest extends OlatJerseyTestCase {
	
	private Identity admin, id1;
	private CatalogEntry root1, entry1, entry2, subEntry11, subEntry12;
	//fxdiff FXOLAT-122: course management
	private CatalogEntry entryToMove1, entryToMove2, subEntry13move;
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-catalog-one");
		JunitTestHelper.createAndPersistIdentityAsUser("rest-catalog-two");
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		admin = securityManager.findIdentityByName("administrator");

		//create a catalog
		CatalogManager catalogManager = CatalogManager.getInstance();
		root1 = (CatalogEntry)catalogManager.getRootCatalogEntries().get(0);
		
		entry1 = catalogManager.createCatalogEntry();
		entry1.setType(CatalogEntry.TYPE_NODE);
		entry1.setName("Entry-1");
		entry1.setDescription("Entry-description-1");
		entry1.setOwnerGroup(securityManager.createAndPersistSecurityGroup());
		catalogManager.addCatalogEntry(root1, entry1);
		
		DBFactory.getInstance().intermediateCommit();
		entry1 = catalogManager.loadCatalogEntry(entry1);
		securityManager.addIdentityToSecurityGroup(admin, entry1.getOwnerGroup());
		
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
		
		DBFactory.getInstance().intermediateCommit();
	}
	
	@After @Override
	public void tearDown() throws Exception {
		super.tearDown();
		DBFactory.getInstance().closeSession();
	}
	
	@Test
	public void testGetRoots() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String body = EntityUtils.toString(response.getEntity());
		List<CatalogEntryVO> vos = parseEntryArray(body);
		assertNotNull(vos);
		assertEquals(1, vos.size());//Root-1
		
		conn.shutdown();
	}
	
	@Test
	public void testGetRootsWithPaging() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		CatalogEntryVOes vos = parse(body, CatalogEntryVOes.class);
		method.releaseConnection();
		assertNotNull(vos);
		assertNotNull(vos.getCatalogEntries());
		assertEquals(1, vos.getCatalogEntries().length);//Root-1
		assertEquals(1, vos.getTotalCount());
	}
	
	@Test
	public void testGetChild() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		assertEquals(entry1.getName(), vo.getName());
		assertEquals(entry1.getDescription(), vo.getDescription());
	}
	
	@Test
	public void testGetChildren() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(root1.getKey().toString()).path("children").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<CatalogEntryVO> vos = parseEntryArray(body);
		assertNotNull(vos);
		assertTrue(vos.size() >= 2);
	}
	
	@Test
	public void testGetChildrenWithPaging() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(root1.getKey().toString()).path("children")
				.queryParam("start", "0").queryParam("limit", "2").build();
		
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		CatalogEntryVOes vos = parse(body, CatalogEntryVOes.class);
		method.releaseConnection();
		assertNotNull(vos);
		assertNotNull(vos.getCatalogEntries());
		assertTrue(vos.getCatalogEntries().length <= 2);
		assertTrue(vos.getTotalCount() >= 2);
	}
	
	@Test
	public void testPutCategoryJson() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		CatalogEntryVO subEntry = new CatalogEntryVO();
		subEntry.setName("Sub-entry-1");
		subEntry.setDescription("Sub-entry-description-1");
		subEntry.setType(CatalogEntry.TYPE_NODE);
		String entity = stringuified(subEntry);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
    RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
		method.setRequestEntity(requestEntity);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		boolean saved = false;
		for(CatalogEntry child:children) {
			if(vo.getKey().equals(child.getKey())) {
				saved = true;
				break;
			}
		}
		
		assertTrue(saved);
	}
	
	@Test
	public void testPutCategoryQuery() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[] {
				new NameValuePair("name", "Sub-entry-2"),
				new NameValuePair("description", "Sub-entry-description-2"),
				new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE))
		});

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		boolean saved = false;
		for(CatalogEntry child:children) {
			if(vo.getKey().equals(child.getKey())) {
				saved = true;
				break;
			}
		}
		
		assertTrue(saved);
	}
	
	@Test
	public void testPutCatalogEntryJson() throws IOException {
		RepositoryEntry re = createRepository("put-cat-entry-json", 6458438l);
		
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		CatalogEntryVO subEntry = new CatalogEntryVO();
		subEntry.setName("Sub-entry-1");
		subEntry.setDescription("Sub-entry-description-1");
		subEntry.setType(CatalogEntry.TYPE_NODE);
		subEntry.setRepositoryEntryKey(re.getKey());
		String entity = stringuified(subEntry);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
    RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
		method.setRequestEntity(requestEntity);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
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
	}
	
	@Test
	public void testPutCatalogEntryQuery() throws IOException {
		RepositoryEntry re = createRepository("put-cat-entry-query", 6458439l);
		
		HttpClient c = loginWithCookie("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[] {
				new NameValuePair("name", "Sub-entry-2"),
				new NameValuePair("description", "Sub-entry-description-2"),
				new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE)),
				new NameValuePair("repoEntryKey", re.getKey().toString())
		});

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
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
	}
	
	@Test
	public void testUpdateCatalogEntryJson() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		CatalogEntryVO entry = new CatalogEntryVO();
		entry.setName("Entry-1-b");
		entry.setDescription("Entry-description-1-b");
		entry.setType(CatalogEntry.TYPE_NODE);
		String entity = stringuified(entry);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).build();
		PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
    RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
		method.setRequestEntity(requestEntity);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entry1);
		assertEquals("Entry-1-b", updatedEntry.getName());
		assertEquals("Entry-description-1-b", updatedEntry.getDescription());
	}
	
	@Test
	public void testUpdateAndMoveCatalogEntryJson() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		CatalogEntryVO entry = new CatalogEntryVO();
		entry.setName("Entry-2-moved-down");
		entry.setDescription("Entry-description-2-moved-down");
		entry.setType(CatalogEntry.TYPE_NODE);
		String entity = stringuified(entry);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entryToMove2.getKey().toString()).queryParam("newParentKey", subEntry13move.getKey().toString()).build();

		PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
    RequestEntity requestEntity = new StringRequestEntity(entity, MediaType.APPLICATION_JSON, "UTF-8");
		method.setRequestEntity(requestEntity);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entryToMove2);
		assertEquals("Entry-2-moved-down", updatedEntry.getName());
		assertEquals("Entry-description-2-moved-down", updatedEntry.getDescription());
		assertNotNull(updatedEntry.getParent());
		assertTrue(updatedEntry.getParent().equalsByPersistableKey(subEntry13move));
	}
	
	@Test
	public void testUpdateCatalogEntryQuery() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
		PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[] {
				new NameValuePair("name", "Entry-2-b"),
				new NameValuePair("description", "Entry-description-2-b"),
				new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE))
		});

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entry2);
		assertEquals("Entry-2-b", updatedEntry.getName());
		assertEquals("Entry-description-2-b", updatedEntry.getDescription());
	}
	
	@Test
	public void testUpdateCatalogEntryForm() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
		PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
		method.addParameters(new NameValuePair[] {
				new NameValuePair("name", "Entry-2-c"),
				new NameValuePair("description", "Entry-description-2-c"),
				new NameValuePair("type", String.valueOf(CatalogEntry.TYPE_NODE))
		});

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entry2);
		assertEquals("Entry-2-c", updatedEntry.getName());
		assertEquals("Entry-description-2-c", updatedEntry.getDescription());
	}
	
	@Test
	public void testMoveCatalogEntryForm() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entryToMove1.getKey().toString()).build();
		PostMethod method = createPost(uri, MediaType.APPLICATION_JSON, true);
		method.addParameters(new NameValuePair[] {
				new NameValuePair("newParentKey", subEntry13move.getKey().toString())
		});

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		CatalogEntryVO vo = parse(body, CatalogEntryVO.class);
		assertNotNull(vo);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry updatedEntry = catalogManager.loadCatalogEntry(entryToMove1);
		assertEquals("Entry-1-to-move", updatedEntry.getName());
		assertEquals("Entry-description-1-to-move", updatedEntry.getDescription());
		assertNotNull(updatedEntry.getParent());
		assertTrue(updatedEntry.getParent().equalsByPersistableKey(subEntry13move));
	}
	
	@Test
	public void testDeleteCatalogEntry() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry2.getKey().toString()).build();
		DeleteMethod method = createDelete(uri, MediaType.APPLICATION_JSON, true);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		method.releaseConnection();
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		List<CatalogEntry> entries = catalogManager.getChildrenOf(root1);
		for(CatalogEntry entry:entries) {
			assertFalse(entry.getKey().equals(entry2.getKey()));
		}
	}
	
	@Test
	public void testGetOwners() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString()).path("owners").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<UserVO> voes = parseUserArray(body);
		assertNotNull(voes);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry entry = catalogManager.loadCatalogEntry(entry1.getKey());
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
		assertNotNull(identities);
		assertEquals(identities.size(), voes.size());
	}
	
	@Test
	public void testGetOwner() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		//admin is owner
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(admin.getKey().toString()).build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);

		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO vo = parse(body, UserVO.class);
		assertNotNull(vo);
		
		//id1 is not owner
		uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(id1.getKey().toString()).build();
		method = createGet(uri, MediaType.APPLICATION_JSON, true);

		code = c.executeMethod(method);
		assertEquals(404, code);
	}
	
	@Test
	public void testAddOwner() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(id1.getKey().toString()).build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);

		int code = c.executeMethod(method);
		method.releaseConnection();
		assertEquals(200, code);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry entry = catalogManager.loadCatalogEntry(entry1.getKey());
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
		boolean found = false;
		for(Identity identity:identities) {
			if(identity.getKey().equals(id1.getKey())) {
				found = true;
			}
		}
		assertTrue(found);
	}
	
	@Test
	public void testRemoveOwner() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
			.path("owners").path(id1.getUser().getKey().toString()).build();
		DeleteMethod method = createDelete(uri, MediaType.APPLICATION_JSON, true);
	
		int code = c.executeMethod(method);
		method.releaseConnection();
		assertEquals(200, code);
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		CatalogEntry entry = catalogManager.loadCatalogEntry(entry1.getKey());
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesOfSecurityGroup(entry.getOwnerGroup());
		boolean found = false;
		for(Identity identity:identities) {
			if(identity.getKey().equals(id1.getKey())) {
				found = true;
			}
		}
		assertFalse(found);
	}
	
	@Test
	public void testBasicSecurityPutCall() throws IOException {
		HttpClient c = loginWithCookie("rest-catalog-two", "A6B7C8");

		URI uri = UriBuilder.fromUri(getContextURI()).path("catalog").path(entry1.getKey().toString())
				.queryParam("name", "Not-sub-entry-3")
				.queryParam("description", "Not-sub-entry-description-3")
				.queryParam("type", String.valueOf(CatalogEntry.TYPE_NODE))
				.build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);

		int code = c.executeMethod(method);
		assertEquals(401, code);
		method.releaseConnection();
		
		CatalogManager catalogManager = CatalogManager.getInstance();
		List<CatalogEntry> children = catalogManager.getChildrenOf(entry1);
		boolean saved = false;
		for(CatalogEntry child:children) {
			if("Not-sub-entry-3".equals(child.getName())) {
				saved = true;
				break;
			}
		}
		
		assertFalse(saved);
	}
	
	protected List<CatalogEntryVO> parseEntryArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<CatalogEntryVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<UserVO> parseUserArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private RepositoryEntry createRepository(String name, final Long resourceableId) {
		OLATResourceable resourceable = new OLATResourceable() {
			public String getResourceableTypeName() {	return CourseModule.ORES_TYPE_COURSE;}
			public Long getResourceableId() {return resourceableId;}
		};

		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl

		OLATResource r = rm.findResourceable(resourceable);
		if(r == null) {
			r = rm.createOLATResourceInstance(resourceable);
		}
		DBFactory.getInstance().saveObject(r);
		DBFactory.getInstance().intermediateCommit();
		
		RepositoryEntry d = RepositoryManager.getInstance().lookupRepositoryEntry(resourceable, false);
		if(d == null) {
			d = RepositoryManager.getInstance().createRepositoryEntryInstance("Stéphane Rossé", name, "Repo entry");
			d.setOlatResource(r);
			d.setDisplayname(name);
			DBFactory.getInstance().saveObject(d);
		}
		DBFactory.getInstance().intermediateCommit();
		return d;
	}
}
