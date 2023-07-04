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
package org.olat.modules.cemedia.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PageReference;
import org.olat.modules.ceditor.manager.PageDAO;
import org.olat.modules.ceditor.manager.PageReferenceDAO;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.TextHandler;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private TagService tagService;
	@Autowired
	private MediaTagDAO mediaTagDao;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private PageReferenceDAO pageReferenceDao;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private MediaToTaxonomyLevelDAO mediaToTaxonomyLevelDao;
	
	@Test
	public void createMedia() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1");
		Media media = mediaDao.createMedia("Media", "Media description", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
		dbInstance.commit();
		
		Assert.assertNotNull(media);
		Assert.assertNotNull(media.getKey());
		Assert.assertNotNull(media.getCreationDate());
		Assert.assertNotNull(media.getCollectionDate());
		Assert.assertEquals(id, media.getAuthor());
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
		Assert.assertEquals(id, reloadedMedia.getAuthor());
		Assert.assertEquals("Media", reloadedMedia.getTitle());
		Assert.assertEquals("Media description", reloadedMedia.getDescription());
		Assert.assertEquals("Alt-text", reloadedMedia.getAltText());
		Assert.assertEquals("[Media:0]", reloadedMedia.getBusinessPath());
		Assert.assertEquals(id, reloadedMedia.getAuthor());
	}
	
	@Test
	public void createMedia_withoutBusinessPath() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-2");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		dbInstance.commit();
		
		Assert.assertNotNull(media);
		Assert.assertNotNull(media.getKey());
		Assert.assertNotNull(media.getCreationDate());
		Assert.assertNotNull(media.getCollectionDate());
		Assert.assertEquals(id, media.getAuthor());
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
		Assert.assertEquals(id, reloadedMedia.getAuthor());
		Assert.assertEquals("Media", reloadedMedia.getTitle());
		Assert.assertNull(reloadedMedia.getBusinessPath());
		Assert.assertEquals(id, reloadedMedia.getAuthor());
	}
	
	@Test
	public void createMediaWithStorage() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-3");
		Media media = mediaDao.createMedia("Media", null, null, "Forum", null, null, 10, id);
		media = mediaDao.createVersion(media, new Date(), "Hello", "/fx/", "root.xml");
		dbInstance.commit();
		
		Assert.assertNotNull(media);
		Assert.assertNotNull(media.getKey());
		Assert.assertNotNull(media.getCreationDate());
		Assert.assertNotNull(media.getCollectionDate());
		Assert.assertEquals(id, media.getAuthor());
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
		Assert.assertEquals(id, reloadedMedia.getAuthor());
		Assert.assertEquals("Media", reloadedMedia.getTitle());
		Assert.assertNull(reloadedMedia.getBusinessPath());
		Assert.assertEquals(id, reloadedMedia.getAuthor());
		
		List<MediaVersion> versions = reloadedMedia.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertEquals(1, versions.size());
		MediaVersion version = versions.get(0);
		Assert.assertEquals("Hello", version.getContent());
		Assert.assertEquals("/fx/", version.getStoragePath());
		Assert.assertEquals("root.xml", version.getRootFilename());
	}
	
	@Test
	public void addVersion() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-4");
		Media media = mediaDao.createMedia("Media", null, null, "Forum", null, null, 10, id);
		media = mediaDao.createVersion(media, new Date(), "Hello", "/fx/", "root.xml");
		dbInstance.commitAndCloseSession();
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		mediaDao.addVersion(reloadedMedia, new Date(), "World", null, null);
		dbInstance.commitAndCloseSession();
		
		Media versionedMedia = mediaDao.loadByKey(media.getKey());
		List<MediaVersion> versions = versionedMedia.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertEquals(2, versions.size());
		Assert.assertEquals("World", versions.get(0).getContent());
		Assert.assertEquals("Hello", versions.get(1).getContent());
	}
	
	@Test
	public void getVersions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-5");
		Media media = mediaDao.createMedia("Media", null, null, "Forum", null, null, 10, id);
		media = mediaDao.createVersion(media, new Date(), "Mercury", "/fx/", "root.xml");
		dbInstance.commitAndCloseSession();
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		mediaDao.addVersion(reloadedMedia, new Date(), "Venus", null, null);
		dbInstance.commitAndCloseSession();
		
		List<MediaVersion> versions = mediaDao.getVersions(reloadedMedia);
		Assert.assertNotNull(versions);
		Assert.assertEquals(2, versions.size());
		Assert.assertEquals("Venus", versions.get(0).getContent());
		Assert.assertEquals("Mercury", versions.get(1).getContent());
	}
	
	@Test
	public void loadByUuid() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-11");
		Media media = mediaDao.createMedia("Media", null, null, "Forum", null, null, 10, id);
		media = mediaDao.createVersion(media, new Date(), "Mercury", "/fx/", "root.xml");
		dbInstance.commitAndCloseSession();
		String uuid = media.getUuid();
		
		Media reloadedMedia = mediaDao.loadByUuid(uuid);
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
	}
	
	@Test
	public void filterOwnedDeletableMedias() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-12");
		Media media = mediaDao.createMedia("Media", null, null, "Forum", null, null, 10, id);
		media = mediaDao.createVersion(media, new Date(), "Mercury", "/fx/", "root.xml");
		dbInstance.commitAndCloseSession();
		
		List<Long> deletableKeys = mediaDao.filterOwnedDeletableMedias(id, List.of(media.getKey(), 1234l));
		assertThat(deletableKeys)
			.hasSize(1)
			.containsExactly(media.getKey());
	}
	
	@Test
	public void searchByAuthor() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media1 = mediaDao.createMedia("Media 1", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		Media media2 = mediaDao.createMedia("Media 2", "Java", null, "One of the most widespread programming language", "Forum", "[Media:0]", null, 10, author);
		Media media3 = mediaDao.createMedia("Media 3", "Europe", "Europa", "Un continent", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		//not owned
		Identity someoneElse = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-7");
		Media mediaAlt = mediaDao.createMedia("Media 3", "Europe", null, "Un continent", "Forum", "[Media:0]", null, 10, someoneElse);
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters ownedParameters = new SearchMediaParameters();
		ownedParameters.setIdentity(author);
		List<MediaWithVersion> ownedMedias = mediaDao.searchBy(ownedParameters);
		assertThat(ownedMedias)
			.hasSizeGreaterThanOrEqualTo(3)
			.map(media -> media.media())
			.containsAnyOf(media1, media2, media3)
			.doesNotContain(mediaAlt);

		// search medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setSearchString("Europe");
		parameters.setIdentity(author);
		List<MediaWithVersion> searchMedias = mediaDao.searchBy(parameters);
		assertThat(searchMedias)
			.hasSize(1)
			.map(media -> media.media())
			.containsAnyOf(media3)
			.doesNotContain(media1, media2, mediaAlt);
	}
	
	@Test
	public void searchWithAllAttributes() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMedia("Media 4", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		String tagName = random();
		Tag tag = tagService.getOrCreateTag(tagName);
		mediaTagDao.create(media, tag);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-2300", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My relation level", "A basic level", null, null, null, null, taxonomy);
		MediaToTaxonomyLevel relation = mediaToTaxonomyLevelDao.createRelation(media, level);
		
		dbInstance.commit();

		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.ALL);
		parameters.setSearchString("media");
		parameters.setTags(List.of(tag.getKey()));
		parameters.setTaxonomyLevelsRefs(List.of(relation.getTaxonomyLevel()));
		parameters.setTypes(List.of("Forum"));
		
		List<MediaWithVersion> ownedMedias = mediaDao.searchBy(parameters);
		assertThat(ownedMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsExactlyInAnyOrder(media);
	}
	
	@Test
	public void searchWithScopeAll() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMedia("Media 5", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.ALL);

		List<MediaWithVersion> allMedias = mediaDao.searchBy(parameters);
		assertThat(allMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(media);
	}
	
	@Test
	public void searchWithScopeMy() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMedia("Media 6", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.MY);

		List<MediaWithVersion> myMedias = mediaDao.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsExactlyInAnyOrder(media);
	}
	
	@Test
	public void load() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-18");
		Media media = mediaDao.createMedia("Media 18", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		List<Media> loadedMedias = mediaDao.load(author);
		assertThat(loadedMedias)
			.hasSize(1)
			.containsExactlyInAnyOrder(media);
	}
	
	@Test
	public void getUsages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-8");
		Page page = pageDao.createAndPersist("Page 4", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMedia("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		List<MediaUsage> usages = mediaDao.getUsages(media);
		assertThat(usages)
			.hasSize(1);
	}
	
	@Test
	public void isUsedInPage() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-9");
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMedia("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		boolean inUse = mediaDao.isUsed(media);
		Assert.assertTrue(inUse);
	}
	
	@Test
	public void isUsedInPageDeletedPage() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-10");
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMedia("Media", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();

		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commit();
		
		dbInstance.getCurrentEntityManager().remove(page);
		dbInstance.commitAndCloseSession();
		
		//reload
		boolean inUse = mediaDao.isUsed(media);
		Assert.assertTrue(inUse);
	}
	
	@Test
	public void getPageUsages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-20");
		Page page = pageDao.createAndPersist("Page 20", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMedia("Media 20", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-234");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPageUsages(author, media);
		Assert.assertNotNull( mediaUsages);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertEquals("Page 20", mediaUsage.pageTitle());
		Assert.assertEquals(media.getKey(), mediaUsage.mediaKey());
		Assert.assertEquals(mediaPart.getMediaVersion().getKey(), mediaUsage.mediaVersionKey());
		Assert.assertEquals("0", mediaUsage.mediaVersionName());
		Assert.assertFalse(mediaUsage.validOwnership());
		Assert.assertFalse(mediaUsage.validGroup());
	}
	
	@Test
	public void getPageUsagesAsOwner() {
		// Create a course ass owner
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-20-1");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(author);
		Page page = pageDao.createAndPersist("New referenced page", "A brand new page but with a ref.", null, null, true, null, null);
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-546");
		dbInstance.commit();
		
		// Add a media to the page
		Media media = mediaDao.createMedia("Media 20", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPageUsages(author, media);
		Assert.assertNotNull( mediaUsages);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertTrue(mediaUsage.validOwnership());
		Assert.assertFalse(mediaUsage.validGroup());
	}
	
	@Test
	public void getPortfolioUsages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-21");
		Page page = pageDao.createAndPersist("Page 21", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMedia("Media 21", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPortfolioUsages(author, media);
		Assert.assertNotNull( mediaUsages);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertEquals("Page 21", mediaUsage.pageTitle());
		Assert.assertEquals(media.getKey(), mediaUsage.mediaKey());
		Assert.assertEquals(mediaPart.getMediaVersion().getKey(), mediaUsage.mediaVersionKey());
		Assert.assertEquals("0", mediaUsage.mediaVersionName());
		Assert.assertFalse(mediaUsage.validOwnership());
		Assert.assertFalse(mediaUsage.validGroup());
	}
	
	@Test
	public void getPortfolioUsagesWithOwnership() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-21-1");
		Page page = portfolioService.appendNewPage(author, "Page 21-1", "A page with content.", null, null, null);
		Media media = mediaDao.createMedia("Media 21-1", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPortfolioUsages(author, media);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertTrue(mediaUsage.validOwnership());
		Assert.assertFalse(mediaUsage.validGroup());
	}
	
	@Test
	public void isEditableByAuthor() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-22");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-23");
		Media media = mediaDao.createMedia("Media 21", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		boolean editable = mediaDao.isEditable(author, media);
		Assert.assertTrue(editable);
		boolean notEditable = mediaDao.isEditable(id, media);
		Assert.assertFalse(notEditable);
	}
	
	@Test
	public void isEditableShared() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-24");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-25");
		Media media = mediaDao.createMedia("Media 23", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		MediaToGroupRelation relation = mediaService.addRelation(media, true, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		boolean editable = mediaDao.isEditable(author, media);
		Assert.assertTrue(editable);
		boolean editableToo = mediaDao.isEditable(id, media);
		Assert.assertTrue(editableToo);
	}
	
	
	@Test
	public void isEditableSharedNotEditable() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-26");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-27");
		Media media = mediaDao.createMedia("Media 24", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		MediaToGroupRelation relation = mediaService.addRelation(media, false, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		boolean notEditable = mediaDao.isEditable(id, media);
		Assert.assertFalse(notEditable);
	}
	
}