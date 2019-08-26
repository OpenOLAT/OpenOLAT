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
package org.olat.modules.portfolio.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.handler.TextHandler;
import org.olat.modules.portfolio.model.BinderPageUsage;
import org.olat.modules.portfolio.model.MediaPart;
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
	private BinderDAO binderDao;
	@Autowired
	private PortfolioService portfolioService;
	
	@Test
	public void createMedia() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1");
		Media media = mediaDao.createMedia("Media", "Media description", "Media content", "Forum", "[Media:0]", null, 10, id);
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
		Assert.assertEquals("[Media:0]", reloadedMedia.getBusinessPath());
		Assert.assertEquals(id, reloadedMedia.getAuthor());
	}
	
	@Test
	public void createMedia_withoutBusinessPath() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1null");
		Media media = mediaDao.createMedia("Media", null, null, "Forum", null, null, 10, id);
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
	public void searchByAuthor() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-2");
		Media media1 = mediaDao.createMedia("Media 1", "The media theory", "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
		Media media2 = mediaDao.createMedia("Media 2", "Java", "One of the most widespread programming language", "Forum", "[Media:0]", null, 10, author);
		Media media3 = mediaDao.createMedia("Media 3", "Europe", "Un continent", "Forum", "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		//not owned
		Identity someoneElse = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-2");
		Media mediaAlt = mediaDao.createMedia("Media 3", "Europe", "Un continent", "Forum", "[Media:0]", null, 10, someoneElse);
		dbInstance.commit();
		
		// search owned medias
		List<MediaLight> ownedMedias = mediaDao.searchByAuthor(author, null, null);
		Assert.assertNotNull(ownedMedias);
		Assert.assertEquals(3, ownedMedias.size());
		Assert.assertTrue(ownedMedias.contains(media1));
		Assert.assertTrue(ownedMedias.contains(media2));
		Assert.assertTrue(ownedMedias.contains(media3));
		Assert.assertFalse(ownedMedias.contains(mediaAlt));
		
		// search medias
		List<MediaLight> searchMedias = mediaDao.searchByAuthor(author, "Europe", null);
		Assert.assertNotNull(searchMedias);
		Assert.assertEquals(1, searchMedias.size());
		Assert.assertFalse(searchMedias.contains(media1));
		Assert.assertFalse(searchMedias.contains(media2));
		Assert.assertTrue(searchMedias.contains(media3));
		Assert.assertFalse(searchMedias.contains(mediaAlt));
	}
	
	@Test
	public void usedInBinders() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-2");
		Binder binder = portfolioService.createNewBinder("Binder p2", "A binder with 2 page", null, author);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, reloadedSection, null);
		Media media = mediaDao.createMedia("Media", "Binder", "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = new MediaPart();
		mediaPart.setMedia(media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		List<BinderPageUsage> binders = mediaDao.usedInBinders(media);
		Assert.assertNotNull(binders);
		Assert.assertEquals(1, binders.size());
		Assert.assertTrue(binders.get(0).getBinderKey().equals(binder.getKey()));
	}
}