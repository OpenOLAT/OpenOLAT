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

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.RepositoryEntryVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntriesTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntriesTest.class);

	@Autowired
	private DB dbInstance;
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
		
		conn.shutdown();
	}
	
	@Test
	public void testImportTest() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("qti21-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", cp, ContentType.APPLICATION_OCTET_STREAM, cp.getName())
				.addTextBody("filename", "qti21-demo.zip")
				.addTextBody("resourcename", "QTI 2.1 demo")
				.addTextBody("displayname", "QTI 2.1 demo")
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
		assertEquals("QTI 2.1 demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		
		conn.shutdown();
	}
	
	@Test
	public void testImportWikiWithMetadata() throws IOException, URISyntaxException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("wiki-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());
		
		String softKey = UUID.randomUUID().toString().substring(0, 30);
		String externalId = softKey + "-Ext-ID";
		String externalRef = softKey + "Ext-Ref";

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
				.addTextBody("externalId", externalId)
				.addTextBody("externalRef", externalRef)
				.addTextBody("softkey", softKey)
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertEquals("Wiki demo", re.getDisplayname());
		Assert.assertEquals(externalId, re.getExternalId());
		Assert.assertEquals(externalRef, re.getExternalRef());
		Assert.assertEquals(softKey, re.getSoftkey());
		
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
	

	private List<RepositoryEntryVO> parseRepoArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RepositoryEntryVO>>(){/* */});
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
		RepositoryEntry d = repositoryService.create(null, displayName, "-", displayName, "Repo entry",
				r, RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		return d;
	}
}