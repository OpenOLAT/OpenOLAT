/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.cemedia.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

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
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.modules.cemedia.model.SearchMediaParameters.UsedIn;
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
 * Initial date: 15 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MediaSearchQueryTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private TagService tagService;
	@Autowired
	private MediaTagDAO mediaTagDao;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private PageReferenceDAO pageReferenceDao;
	@Autowired
	private MediaSearchQuery mediaSearchQuery;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private MediaToTaxonomyLevelDAO mediaToTaxonomyLevelDao;
	
	@Test
	public void searchByAuthor() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media1 = mediaDao.createMediaAndVersion("Media 1", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		Media media2 = mediaDao.createMediaAndVersion("Media 2", "Java", null, "One of the most widespread programming language", "Forum", "[Media:0]", null, 10, author);
		Media media3 = mediaDao.createMediaAndVersion("Media 3", "Europe", "Europa", "Un continent", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		//not owned
		Identity someoneElse = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-7");
		Media mediaAlt = mediaDao.createMediaAndVersion("Media 3", "Europe", null, "Un continent", "Forum", "[Media:0]", null, 10, someoneElse);
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters ownedParameters = new SearchMediaParameters();
		ownedParameters.setIdentity(author);
		ownedParameters.setScope(null);
		List<MediaWithVersion> ownedMedias = mediaSearchQuery.searchBy(ownedParameters);
		assertThat(ownedMedias)
			.hasSizeGreaterThanOrEqualTo(3)
			.map(media -> media.media())
			.containsAnyOf(media1, media2, media3)
			.doesNotContain(mediaAlt);

		// search medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setSearchString("Europe");
		parameters.setIdentity(author);
		ownedParameters.setScope(Scope.MY);
		List<MediaWithVersion> searchMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(searchMedias)
			.hasSize(1)
			.map(media -> media.media())
			.containsAnyOf(media3)
			.doesNotContain(media1, media2, mediaAlt);
	}

	@Test
	public void searchWithAllAttributes() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 4", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

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
		
		List<MediaWithVersion> ownedMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(ownedMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsExactlyInAnyOrder(media);
	}
	
	@Test
	public void searchWithScopeAll() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 5", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.ALL);

		List<MediaWithVersion> allMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(allMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(media);
	}
	

	@Test
	public void searchWithScopeMy() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 6", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.MY);

		List<MediaWithVersion> myMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsExactlyInAnyOrder(media);
	}
	
	@Test
	public void searchWithUsedInNotUsed() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 16", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, null, null);
		Media usedMedia = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		MediaPart usedMediaPart = MediaPart.valueOf(author, usedMedia);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, usedMediaPart);
		dbInstance.commitAndCloseSession();

		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setUsedIn(List.of(UsedIn.NOT_USED));

		List<MediaWithVersion> myMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(media)
			.doesNotContain(usedMedia);
	}
	
	@Test
	public void searchWithUsedInPortfolio() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 17", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		Page page = pageDao.createAndPersist("Page 3", "A page with content.", null, null, true, null, null);
		Media usedMedia = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		MediaPart usedMediaPart = MediaPart.valueOf(author, usedMedia);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, usedMediaPart);
		dbInstance.commitAndCloseSession();

		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setUsedIn(List.of(UsedIn.PORTFOLIO));

		List<MediaWithVersion> myMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(usedMedia)
			.doesNotContain(media);
	}
	
	@Test
	public void searchWithUsedInPage() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 17", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		Page page = pageDao.createAndPersist("Page 3", "A page with content.", null, null, true, null, null);
		Media usedMedia = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		MediaPart usedMediaPart = MediaPart.valueOf(author, usedMedia);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, usedMediaPart);
		dbInstance.commitAndCloseSession();

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-234");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);

		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setUsedIn(List.of(UsedIn.PAGE));

		List<MediaWithVersion> myMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(usedMedia)
			.doesNotContain(media);
	}
	
	@Test
	public void searchWithUsedInPageAndPortfolio() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-6");
		Media media = mediaDao.createMediaAndVersion("Media 17", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);

		Page page = pageDao.createAndPersist("Page 4", "A page with content.", null, null, true, null, null);
		Media usedMedia = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		MediaPart usedMediaPart = MediaPart.valueOf(author, usedMedia);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, usedMediaPart);
		dbInstance.commitAndCloseSession();

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		PageReference reference = pageReferenceDao.createReference(page, re, "AC-234");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);
		
		Page portfolioPage = portfolioService.appendNewPage(author, "Page 21-1", "A page with content.", null, null, null);
		Media portfolioMedia = mediaDao.createMediaAndVersion("Media 21-1", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(author, portfolioMedia);
		PageBody reloadedPortfolioBody = pageDao.loadPageBodyByKey(portfolioPage.getBody().getKey());
		pageDao.persistPart(reloadedPortfolioBody, mediaPart);
		dbInstance.commitAndCloseSession();

		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setUsedIn(List.of(UsedIn.PAGE));

		List<MediaWithVersion> myMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(usedMedia, portfolioMedia)
			.doesNotContain(media);
	}
	
	@Test
	public void searchWithSharedWithUser() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-26");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-27");

		Media notSharedMedia = mediaDao.createMediaAndVersion("Media 18", "The media not shared", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		Media sharedMedia = mediaDao.createMediaAndVersion("Media 19", "The media shared with user", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(sharedMedia, false, user);
		dbInstance.commitAndCloseSession();

		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setSharedWith(List.of(MediaToGroupRelationType.USER));

		List<MediaWithVersion> myMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(myMedias)
			.hasSize(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsExactly(sharedMedia)
			.doesNotContain(notSharedMedia);
	}
	
	@Test
	public void searchWithScopeSharedWithMe() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-16");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-17");

		Media media = mediaDao.createMediaAndVersion("Media 7", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(media, false, user);
		
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(user);
		parameters.setScope(Scope.SHARED_WITH_ME);

		List<MediaWithVersion> sharedMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(sharedMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(media);
	}
	
	@Test
	public void searchWithScopeSharedByMe() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-18");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-19");

		Media sharedMedia = mediaDao.createMediaAndVersion("Media shared", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(sharedMedia, false, user);
		Media privateMedia = mediaDao.createMediaAndVersion("Media private", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setIdentity(author);
		parameters.setScope(Scope.SHARED_BY_ME);

		List<MediaWithVersion> sharedMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(sharedMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(sharedMedia)
			.doesNotContain(privateMedia);
	}
	
	@Test
	public void searchWithScopeSharedWithEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-20");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Media sharedMedia = mediaDao.createMediaAndVersion("Media shared with repo", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		mediaService.addRelation(sharedMedia, false, entry);
		Media privateMedia = mediaDao.createMediaAndVersion("Media private", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		// search owned medias
		SearchMediaParameters parameters = new SearchMediaParameters();
		parameters.setRepositoryEntry(entry);
		parameters.setScope(Scope.SHARED_WITH_ENTRY);

		List<MediaWithVersion> sharedMedias = mediaSearchQuery.searchBy(parameters);
		assertThat(sharedMedias)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(mediaWithVersion -> mediaWithVersion.media())
			.containsAnyOf(sharedMedia)
			.doesNotContain(privateMedia);
	}
}
