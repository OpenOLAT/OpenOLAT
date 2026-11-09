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
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.fileresource.types.ImageFileResource;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.restapi.support.vo.RepositoryEntryVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntriesTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntriesTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;

	@Test
	public void getEntries() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<RepositoryEntryVO> entryVoes = conn.parseList(response, RepositoryEntryVO.class);
		assertNotNull(entryVoes);
		conn.close();

	}
	
	@Test
	public void getEntriesWithPaging() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.queryParam("start", "0").queryParam("limit", "25").build();
		
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RepositoryEntryVOes entryVoes = conn.parse(response, RepositoryEntryVOes.class);

		assertNotNull(entryVoes);
		assertNotNull(entryVoes.getRepositoryEntries());
		assertTrue(entryVoes.getRepositoryEntries().length <= 25);
		assertTrue(entryVoes.getTotalCount() >= entryVoes.getRepositoryEntries().length);
		conn.close();

	}
	
	@Test
	public void getEntriesByTaxonomyLevel() throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry(false);
		dbInstance.commit();
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-search-repo", "Search by taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(entry, level);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.queryParam("taxonomyLevelKey", level.getKey()).build();
		
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<RepositoryEntryVO> entryVoes = conn.parseList(response, RepositoryEntryVO.class);

		Assert.assertNotNull(entryVoes);
		Assert.assertEquals(1, entryVoes.size());
		Assert.assertEquals(entry.getKey(), entryVoes.get(0).getKey());
		conn.close();

	}
	
	@Test
	public void getEntry() throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntry re = createRepository(null, "Test GET repo entry");

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries/" + re.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RepositoryEntryVO entryVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(entryVo);
		conn.close();

	}
	
	@Test
	public void getEntryAuthor() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("rest-author-1");
		RepositoryEntry re = createRepository(author.getIdentity(), "Test GET repo entry");

		RestConnection conn = new RestConnection(author);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries/" + re.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RepositoryEntryVO entryVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(entryVo);
		conn.close();

	}
	
	@Test
	public void getEntryAuthors() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin author1 = JunitTestHelper.createAndPersistRndAuthor("rest-author-2");
		IdentityWithLogin author2 = JunitTestHelper.createAndPersistRndAuthor("rest-author-3");
		RepositoryEntry re = createRepository(author1.getIdentity(), "Test GET repo entry");

		RestConnection conn = new RestConnection(author2);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries/" + re.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(403, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
	
	@Test
	public void getEntry_managed() throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntry re = createRepository(null, "Test GET repo entry");
		re.setManagedFlagsString("all");
		re = dbInstance.getCurrentEntityManager().merge(re);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.queryParam("managed", "true").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<RepositoryEntryVO> entryVoes = conn.parseList(response, RepositoryEntryVO.class);
		Assert.assertNotNull(entryVoes);
		Assert.assertFalse(entryVoes.isEmpty());
		//only repo entries with managed flags
		for(RepositoryEntryVO entryVo:entryVoes) {
			Assert.assertNotNull(entryVo.getManagedFlags());
			Assert.assertTrue(entryVo.getManagedFlags().length() > 0);
		}
		conn.close();

	}
	
	@Test
	public void updateRepositoryEntry() throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		RepositoryEntryVO repoVo = new RepositoryEntryVO();
		repoVo.setKey(re.getKey());
		repoVo.setDisplayname("New display name");
		repoVo.setExternalId("New external ID");
		repoVo.setExternalRef("New external ref");
		repoVo.setManagedFlags("booking,delete");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").path(re.getKey().toString()).build();
		HttpRequest method = conn.createPost(request, repoVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		RepositoryEntryVO updatedVo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(updatedVo);
		
		Assert.assertEquals("New display name", updatedVo.getDisplayname());
		Assert.assertEquals("New external ID", updatedVo.getExternalId());
		Assert.assertEquals("New external ref", updatedVo.getExternalRef());
		Assert.assertEquals("booking,delete", updatedVo.getManagedFlags());

		
		RepositoryEntry reloadedRe = repositoryManager.lookupRepositoryEntry(re.getKey());
		assertNotNull(reloadedRe);

		Assert.assertEquals("New display name", reloadedRe.getDisplayname());
		Assert.assertEquals("New external ID", reloadedRe.getExternalId());
		Assert.assertEquals("New external ref", reloadedRe.getExternalRef());
		Assert.assertEquals("booking,delete", reloadedRe.getManagedFlagsString());
		conn.close();
	}
	
	@Test
	public void updateRepositoryEntry_lifecycle() throws IOException, URISyntaxException, InterruptedException {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
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
		HttpRequest method = conn.createPost(request, repoVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
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
		conn.close();
	}
	
	@Test
	public void importCp() throws IOException, URISyntaxException, InterruptedException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("cp-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("resourcename", "CP demo"),
				new NameValuePair("displayname", "CP demo"));
		HttpRequest method = conn.createPut(request, cp, "cp-demo.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("CP demo", re.getDisplayname());
		conn.close();

	}
	
	@Test
	public void importTest() throws IOException, URISyntaxException, InterruptedException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("qti21-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("resourcename", "QTI 2.1 demo"),
				new NameValuePair("displayname", "QTI 2.1 demo"));
		HttpRequest method = conn.createPut(request, cp, "qti21-demo.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("QTI 2.1 demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		conn.close();

	}
	
	@Test
	public void importWikiWithMetadata() throws IOException, URISyntaxException, InterruptedException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("wiki-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());
		
		String softKey = UUID.randomUUID().toString().substring(0, 30);
		String externalId = softKey + "-Ext-ID";
		String externalRef = softKey + "Ext-Ref";

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("resourcename", "Wiki demo"),
				new NameValuePair("displayname", "Wiki demo"),
				new NameValuePair("externalId", externalId),
				new NameValuePair("externalRef", externalRef),
				new NameValuePair("softkey", softKey));
		HttpRequest method = conn.createPut(request, cp, "wiki-demo.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
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
		conn.close();
	}
	
	@Test
	public void importBlog() throws IOException, URISyntaxException, InterruptedException {
		URL cpUrl = RepositoryEntriesTest.class.getResource("blog-demo.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/entries").build();
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("resourcename", "Blog demo"),
				new NameValuePair("displayname", "Blog demo"));
		HttpRequest method = conn.createPut(request, cp, "blog-demo.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		RepositoryEntryVO vo = conn.parse(response, RepositoryEntryVO.class);
		assertNotNull(vo);
		
		Long key = vo.getKey();
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(key);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("Blog demo", re.getDisplayname());
		log.info(re.getOlatResource().getResourceableTypeName());
		conn.close();
	}
	
	private RepositoryEntry createRepository(Identity author, String displayName) {
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		
		OLATResource r =  rm.createOLATResourceInstance(ImageFileResource.TYPE_NAME);
		dbInstance.saveObject(r);
		dbInstance.intermediateCommit();

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry d = repositoryService.create(author, displayName, "-", displayName, "Repo entry",
				r, RepositoryEntryStatusEnum.preparation, RepositoryEntryRuntimeType.embedded, defOrganisation);
		dbInstance.commit();
		return d;
	}
}