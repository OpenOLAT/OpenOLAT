package org.olat.modules.cemedia.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.manager.PageDAO;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
public class MediaToPagePartDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private MediaToPagePartDAO mediaToPagePartDao;

	@Test
	public void testCreateRelation() {
		// Arrange
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-1");
		Media media = mediaDao.createMediaAndVersion("Media 1", "Media 1 description", null,
				"Media 1 content", "Image", "[Media:1]", null, 123, id);
		PageBody pageBody = createBodyWithContainerPart("Page 1", "Page 1 summary");
		PagePart containerPart = pageBody.getParts().get(0);

		// Act
		MediaToPagePart relation = mediaToPagePartDao.createRelation(media, containerPart);
		dbInstance.commitAndCloseSession();

		// Assert
		List<PagePart> pageParts = mediaToPagePartDao.loadPageParts(media);
		Assert.assertEquals(1, pageParts.size());
		Assert.assertEquals(containerPart, pageParts.get(0));

		List<MediaToPagePart> relations = mediaToPagePartDao.loadRelations(media);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(relation, relations.get(0));
	}

	private PageBody createBodyWithContainerPart(String pageTitle, String pageSummary) {
		Page page = pageDao.createAndPersist(pageTitle, pageSummary, null, null,
				true, null, null);
		dbInstance.commitAndCloseSession();
		ContainerPart containerPart = new ContainerPart();
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		PageBody pageBody = pageDao.persistPart(reloadedBody, containerPart);
		dbInstance.commitAndCloseSession();
		return pageBody;
	}

	@Test
	public void testLoadPageParts() {
		// Arrange
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-1");
		Media media = mediaDao.createMediaAndVersion("Media 1", "Media 1 description", null,
				"Media 1 content", "Image", "[Media:1]", null, 123, id);
		PageBody page1Body = createBodyWithContainerPart("Page 1", "Page 1 summary");
		PageBody page2Body = createBodyWithContainerPart("Page 2", "Page 2 summary");
		PagePart pagePart1 = page1Body.getParts().get(0);
		PagePart pagePart2 = page2Body.getParts().get(0);
		mediaToPagePartDao.createRelation(media, pagePart1);
		mediaToPagePartDao.createRelation(media, pagePart2);
		dbInstance.commitAndCloseSession();

		// Act
		List<PagePart> pageParts = mediaToPagePartDao.loadPageParts(media);
		dbInstance.commitAndCloseSession();

		// Assert
		Assert.assertEquals(2, pageParts.size());
		Set<PagePart> expectedPageParts = Set.of(pagePart1, pagePart2);
		Set<PagePart> loadedPageParts = new HashSet<>(pageParts);
		Assert.assertEquals(expectedPageParts, loadedPageParts);
	}

	@Test
	public void testLoadRelations() {
		// Arrange
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-1");
		Media media = mediaDao.createMediaAndVersion("Media 1", "Media 1 description", null,
				"Media 1 content", "Image", "[Media:1]", null, 123, id);
		PageBody page1Body = createBodyWithContainerPart("Page 1", "Page 1 summary");
		PageBody page2Body = createBodyWithContainerPart("Page 2", "Page 2 summary");
		PagePart pagePart1 = page1Body.getParts().get(0);
		PagePart pagePart2 = page2Body.getParts().get(0);
		mediaToPagePartDao.createRelation(media, pagePart1);
		mediaToPagePartDao.createRelation(media, pagePart2);
		dbInstance.commitAndCloseSession();

		// Act
		List<MediaToPagePart> relations = mediaToPagePartDao.loadRelations(media);

		// Assert
		Assert.assertEquals(2, relations.size());
		List<Media> mediaItems = relations.stream().map(MediaToPagePart::getMedia).filter(m -> m.equals(media)).toList();
		Set<PagePart> loadedPageParts = relations.stream().map(MediaToPagePart::getPagePart).collect(Collectors.toSet());
		Assert.assertEquals(2, mediaItems.size());
		Set<PagePart> expectedPageParts = Set.of(pagePart1, pagePart2);
		Assert.assertEquals(expectedPageParts, loadedPageParts);
	}

	@Test
	public void testDeleteRelations() {
		// Arrange
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("media-1");
		Media media1 = mediaDao.createMediaAndVersion("Media 1", "Media 1 description", null,
				"Media 1 content", "Image", "[Media:1]", null, 123, id1);
		PageBody page1Body = createBodyWithContainerPart("Page 1", "Page 1 summary");
		PageBody page2Body = createBodyWithContainerPart("Page 2", "Page 2 summary");
		PagePart pagePart1 = page1Body.getParts().get(0);
		PagePart pagePart2 = page2Body.getParts().get(0);
		mediaToPagePartDao.createRelation(media1, pagePart1);
		mediaToPagePartDao.createRelation(media1, pagePart2);

		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("media-2");
		Media media2 = mediaDao.createMediaAndVersion("Media 2", "Media 2 description", null,
				"Media 2 content", "Image", "[Media:2]", null, 234, id2);
		mediaToPagePartDao.createRelation(media2, pagePart1);
		mediaToPagePartDao.createRelation(media2, pagePart2);
		dbInstance.commitAndCloseSession();

		// Act
		int nbDeleted = mediaToPagePartDao.deleteRelations(media1);
		dbInstance.commitAndCloseSession();

		// Assert
		Assert.assertEquals(2, nbDeleted);
		List<PagePart> loadedPageParts1 = mediaToPagePartDao.loadPageParts(media1);
		List<MediaToPagePart> loadedRelations1 = mediaToPagePartDao.loadRelations(media1);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(loadedPageParts1.isEmpty());
		Assert.assertTrue(loadedRelations1.isEmpty());

		List<PagePart> loadedPageParts2 = mediaToPagePartDao.loadPageParts(media2);
		List<MediaToPagePart> loadedRelations2 = mediaToPagePartDao.loadRelations(media2);
		Assert.assertEquals(2, loadedPageParts2.size());
		Assert.assertEquals(2, loadedRelations2.size());
		Set<PagePart> expectedPageParts = Set.of(pagePart1, pagePart2);
		Set<PagePart> loadedPageParts = loadedRelations2.stream().map(MediaToPagePart::getPagePart).collect(Collectors.toSet());
		Assert.assertEquals(expectedPageParts, loadedPageParts);
	}

	@Test
	public void testDeleteRelation() {
		// Arrange
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-1");
		Media media = mediaDao.createMediaAndVersion("Media 1", "Media 1 description", null,
				"Media 1 content", "Image", "[Media:1]", null, 123, id);
		PageBody page1Body = createBodyWithContainerPart("Page 1", "Page 1 summary");
		PageBody page2Body = createBodyWithContainerPart("Page 2", "Page 2 summary");
		PagePart pagePart1 = page1Body.getParts().get(0);
		PagePart pagePart2 = page2Body.getParts().get(0);
		MediaToPagePart relation1 = mediaToPagePartDao.createRelation(media, pagePart1);
		MediaToPagePart relation2 = mediaToPagePartDao.createRelation(media, pagePart2);
		dbInstance.commitAndCloseSession();

		// Act
		MediaToPagePart reloadedRelation1 = mediaToPagePartDao.loadRelations(media).stream().filter(r -> r.equals(relation1)).findFirst().get();
		mediaToPagePartDao.deleteRelation(reloadedRelation1);
		dbInstance.commitAndCloseSession();

		// Assert
		List<MediaToPagePart> relations = mediaToPagePartDao.loadRelations(media);
		Assert.assertFalse(relations.contains(relation1));
		Assert.assertTrue(relations.contains(relation2));
	}
}