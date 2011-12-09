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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.RepositoryEntryVOes;
import org.olat.test.OlatJerseyTestCase;

public class RepositoryEntriesTest extends OlatJerseyTestCase {
	
	public RepositoryEntriesTest() {
		super();
  }
	
	@Before @Override
	public void setUp() throws Exception {
		super.setUp();
		DBFactory.getInstance().intermediateCommit();
	}

	@After @Override
	public void tearDown() throws Exception {
		super.tearDown();
		DBFactory.getInstance().commitAndCloseSession();
	}

	@Test
	public void testGetEntries() throws HttpException, IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("repo/entries", MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		
		List<RepositoryEntryVO> entryVoes = parseRepoArray(body);
		assertNotNull(entryVoes);
	}
	
	@Test
	public void testGetEntriesWithPaging() throws HttpException, IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.queryParam("start", "0").queryParam("limit", "25").build();
		
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		RepositoryEntryVOes entryVoes = parse(body, RepositoryEntryVOes.class);
		method.releaseConnection();

		assertNotNull(entryVoes);
		assertNotNull(entryVoes.getRepositoryEntries());
		assertTrue(entryVoes.getRepositoryEntries().length <= 25);
		assertTrue(entryVoes.getTotalCount() >= entryVoes.getRepositoryEntries().length);
	}
	
	@Test
	public void testGetEntry() throws HttpException, IOException {
		RepositoryEntry re = createRepository("Test GET repo entry");
		
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("repo/entries/" + re.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		
		RepositoryEntryVO entryVo = parse(body, RepositoryEntryVO.class);
		assertNotNull(entryVo);
	}
	
	@Test
	public void testImportCp() throws HttpException, IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("cp-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		HttpClient c = loginWithCookie("administrator", "olat");
		PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", cp),
				new StringPart("filename","cp-demo.zip"),
				new StringPart("resourcename","CP demo"),
				new StringPart("displayname","CP demo")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("CP demo", re.getDisplayname());
	}
	
	@Test
	public void testImportTest() throws HttpException, IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("qti-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		HttpClient c = loginWithCookie("administrator", "olat");
		PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", cp),
				new StringPart("filename","qti-demo.zip"),
				new StringPart("resourcename","QTI demo"),
				new StringPart("displayname","QTI demo")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
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
	public void testImportQuestionnaire() throws HttpException, IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("questionnaire-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		HttpClient c = loginWithCookie("administrator", "olat");
		PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", cp),
				new StringPart("filename","questionnaire-demo.zip"),
				new StringPart("resourcename","Questionnaire demo"),
				new StringPart("displayname","Questionnaire demo")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
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
	public void testImportWiki() throws HttpException, IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("wiki-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		HttpClient c = loginWithCookie("administrator", "olat");
		PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", cp),
				new StringPart("filename","wiki-demo.zip"),
				new StringPart("resourcename","Wiki demo"),
				new StringPart("displayname","Wiki demo")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
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
	public void testImportBlog() throws HttpException, IOException, URISyntaxException {
		Logger log = Logger.getLogger(getClass().getName());
		URL cpUrl = RepositoryEntriesTest.class.getResource("blog-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		HttpClient c = loginWithCookie("administrator", "olat");
		PutMethod method = createPut("repo/entries", MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", cp),
				new StringPart("filename","blog-demo.zip"),
				new StringPart("resourcename","Blog demo"),
				new StringPart("displayname","Blog demo")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		RepositoryEntryVO vo = parse(body, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOwnerGroup());
		assertNotNull(re.getOlatResource());
		assertEquals("Blog demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
	}
	
	protected List<RepositoryEntryVO> parseRepoArray(String body) {
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