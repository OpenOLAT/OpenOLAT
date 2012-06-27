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
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.RepositoryEntryVOes;
import org.olat.test.OlatJerseyTestCase;

public class RepositoryEntriesTest extends OlatJerseyTestCase {
	
	private RestConnection conn;
	
	@Before @Override
	public void setUp() throws Exception {
		super.setUp();
		conn = new RestConnection();
		DBFactory.getInstance().intermediateCommit();
	}

  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
      e.printStackTrace();
      throw e;
		}
	}

	@Test
	public void testGetEntries() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		
		
		List<RepositoryEntryVO> entryVoes = parseRepoArray(body);
		assertNotNull(entryVoes);
	}
	
	@Test
	public void testGetEntriesWithPaging() throws IOException, URISyntaxException {
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
	}
	
	@Test
	public void testGetEntry() throws IOException, URISyntaxException {
		RepositoryEntry re = createRepository("Test GET repo entry");
		
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries/" + re.getKey()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryVO entryVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(entryVo);
	}
	
	@Test
	public void testImportCp() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("cp-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("cp-demo.zip"));
		entity.addPart("resourcename", new StringBody("CP demo"));
		entity.addPart("displayname", new StringBody("CP demo"));
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("CP demo", re.getDisplayname());
	}
	
	@Test
	public void testImportTest() throws IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("qti-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("qti-demo.zip"));
		entity.addPart("resourcename", new StringBody("QTI demo"));
		entity.addPart("displayname", new StringBody("QTI demo"));
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("QTI demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
	}
	
	@Test
	public void testImportQuestionnaire() throws IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("questionnaire-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("questionnaire-demo.zip"));
		entity.addPart("resourcename", new StringBody("Questionnaire demo"));
		entity.addPart("displayname", new StringBody("Questionnaire demo"));
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("Questionnaire demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
	}
	
	@Test
	public void testImportWiki() throws IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("wiki-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("wiki-demo.zip"));
		entity.addPart("resourcename", new StringBody("Wiki demo"));
		entity.addPart("displayname", new StringBody("Wiki demo"));
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("Wiki demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
	}
	
	@Test
	public void testImportBlog() throws IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("blog-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		method.addHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		entity.addPart("file", new FileBody(cp));
		entity.addPart("filename", new StringBody("blog-demo.zip"));
		entity.addPart("resourcename", new StringBody("Blog demo"));
		entity.addPart("displayname", new StringBody("Blog demo"));
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("Blog demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
	}
	
	protected List<RepositoryEntryVO> parseRepoArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<RepositoryEntryVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private RepositoryEntry createRepository(String name) {
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		
		OLATResource r =  rm.createOLATResourceInstance("DummyType");
		DBFactory.getInstance().saveObject(r);
		DBFactory.getInstance().intermediateCommit();

		RepositoryEntry d = RepositoryManager.getInstance().createRepositoryEntryInstance("Stéphane Rossé", name, "Repo entry");
		d.setOlatResource(r);
		d.setDisplayname(name);
		DBFactory.getInstance().saveObject(d);
		DBFactory.getInstance().intermediateCommit();
		return d;
	}
}