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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils.Usage;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageBody;
import org.olat.modules.ceditor.PageReference;
import org.olat.modules.ceditor.manager.PageDAO;
import org.olat.modules.ceditor.manager.PageReferenceDAO;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLog;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.MediaVersionMetadata;
import org.olat.modules.cemedia.handler.ImageHandler;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaVersionImpl;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.TextHandler;
import org.olat.modules.video.VideoFormat;
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
	private PageDAO pageDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private ImageHandler imageHandler;
	@Autowired
	private PageReferenceDAO pageReferenceDao;
	@Autowired
	private PortfolioService portfolioService;
	
	@Test
	public void createMediaAndVersion() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-1");
		Media media = mediaDao.createMediaAndVersion("Media", "Media description", "Alt-text", "Media content", "Forum", "[Media:0]", null, 10, id);
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
	public void createMediaAndVersionWithoutBusinessPath() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-2");
		Media media = mediaDao.createMediaAndVersion("Media", null, null, null, "Forum", null, null, 10, id);
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
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Hello", "/fx/", "root.xml");
		media = mediaWithVersion.media();
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
	public void createImageMedia() throws URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-14");
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageFile = new File(imageUrl.toURI());
		Media media = imageHandler.createMedia("Image", null, null, imageFile, imageFile.getName(), "[Image:0]",
				id, MediaLog.Action.UPLOAD);
		dbInstance.commitAndCloseSession();
		
		Media realodedMedia = mediaDao.loadByKey(media.getKey());
		Assert.assertNotNull(realodedMedia);
		List<MediaVersion> mediaVersions = realodedMedia.getVersions();
		Assert.assertNotNull(mediaVersions);
		Assert.assertEquals(1, mediaVersions.size());
		
		MediaVersion mediaVersion = mediaVersions.get(0);
		Assert.assertNotNull(mediaVersion);
		Assert.assertNotNull(mediaVersion.getRootFilename());
		Assert.assertNotNull(mediaVersion.getStoragePath());
		Assert.assertNotNull(mediaVersion.getVersionChecksum());
		Assert.assertNotNull(mediaVersion.getVersionUuid());
		Assert.assertNotNull(mediaVersion.getMetadata());
		Assert.assertEquals(media, mediaVersion.getMedia());
	}

	@Test
	public void createMediaWithVersionMetadata() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-15");
		Media media = mediaDao.createMedia("Media", null, null, null,
				"video-via-url", null, null, 10, id);
		MediaVersionMetadata versionMetadata = mediaDao.createVersionMetadata();
		String youtubeUrl = "https://www.youtube.com/watch?v=-abcd1234";
		versionMetadata.setUrl(youtubeUrl);
		versionMetadata.setLength("1:23");
		versionMetadata.setWidth(1280);
		versionMetadata.setHeight(720);
		versionMetadata.setFormat(VideoFormat.youtube.name());
		mediaDao.update(versionMetadata);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), versionMetadata);
		dbInstance.commit();

		Assert.assertNotNull(media);
		Assert.assertNotNull(media.getKey());
		Assert.assertNotNull(media.getCreationDate());
		Assert.assertNotNull(media.getCollectionDate());
		Assert.assertEquals(id, media.getAuthor());

		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
		Assert.assertEquals("Media", reloadedMedia.getTitle());
		Assert.assertNull(reloadedMedia.getBusinessPath());
		Assert.assertEquals(id, reloadedMedia.getAuthor());
		Assert.assertEquals("video-via-url", reloadedMedia.getType());

		List<MediaVersion> versions = reloadedMedia.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertEquals(1, versions.size());
		MediaVersion reloadedVersion = versions.get(0);
		Assert.assertEquals(mediaWithVersion.version(), reloadedVersion);
		MediaVersionMetadata reloadedVersionMetadata = reloadedVersion.getVersionMetadata();
		Assert.assertNotNull(reloadedVersionMetadata);
		Assert.assertEquals(VideoFormat.youtube, VideoFormat.valueOf(reloadedVersionMetadata.getFormat()));
		Assert.assertEquals(youtubeUrl, reloadedVersionMetadata.getUrl());
		Assert.assertEquals(versionMetadata.getWidth(), reloadedVersionMetadata.getWidth());
		Assert.assertEquals(versionMetadata.getHeight(), reloadedVersionMetadata.getHeight());
		Assert.assertEquals("1:23", reloadedVersionMetadata.getLength());
	}
	
	@Test
	public void addVersion() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-4");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Hello", "/fx/", "root.xml");
		media = mediaWithVersion.media();
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
	public void addVersionWithVersionMetadata() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-16");
		Media media = mediaDao.createMedia("Media", null, null, null,
				"video-via-url", null, null, 10, id);
		MediaVersionMetadata versionMetadata = mediaDao.createVersionMetadata();
		String youtubeUrl = "https://www.youtube.com/watch?v=bcde2345";
		versionMetadata.setUrl(youtubeUrl);
		versionMetadata.setLength("2:34");
		versionMetadata.setWidth(1920);
		versionMetadata.setHeight(1080);
		versionMetadata.setFormat(VideoFormat.youtube.name());
		mediaDao.update(versionMetadata);
		mediaDao.createVersion(media, new Date(), versionMetadata);
		dbInstance.commitAndCloseSession();

		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		MediaVersionMetadata addedVersionMetadata = mediaDao.createVersionMetadata();
		String addedYoutubeUrl = "https://www.youtube.com/watch?v=cdef3456";
		addedVersionMetadata.setUrl(addedYoutubeUrl);
		addedVersionMetadata.setLength("3:45");
		addedVersionMetadata.setWidth(1280);
		addedVersionMetadata.setHeight(720);
		addedVersionMetadata.setFormat(VideoFormat.youtube.name());
		mediaDao.update(addedVersionMetadata);
		mediaDao.addVersion(reloadedMedia, new Date(), addedVersionMetadata);
		dbInstance.commitAndCloseSession();

		Media versionedMedia = mediaDao.loadByKey(media.getKey());
		List<MediaVersion> versions = versionedMedia.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertEquals(2, versions.size());

		MediaVersion versionOne = versions.get(0);
		MediaVersionMetadata versionOneMetadata = versionOne.getVersionMetadata();
		Assert.assertNotNull(versionOneMetadata);
		Assert.assertEquals(addedVersionMetadata, versionOneMetadata);
		Assert.assertEquals(addedYoutubeUrl, versionOneMetadata.getUrl());
		Assert.assertEquals(addedVersionMetadata.getLength(), versionOneMetadata.getLength());
		Assert.assertEquals(addedVersionMetadata.getWidth(), versionOneMetadata.getWidth());
		Assert.assertEquals(addedVersionMetadata.getHeight(), versionOneMetadata.getHeight());
		Assert.assertEquals(addedVersionMetadata.getFormat(), versionOneMetadata.getFormat());

		MediaVersion versionTwo = versions.get(1);
		MediaVersionMetadata versionTwoMetadata = versionTwo.getVersionMetadata();
		Assert.assertNotNull(versionTwoMetadata);
		Assert.assertEquals(versionMetadata, versionTwoMetadata);
		Assert.assertEquals(youtubeUrl, versionTwoMetadata.getUrl());
		Assert.assertEquals(versionMetadata.getLength(), versionTwoMetadata.getLength());
		Assert.assertEquals(versionMetadata.getWidth(), versionTwoMetadata.getWidth());
		Assert.assertEquals(versionMetadata.getHeight(), versionTwoMetadata.getHeight());
		Assert.assertEquals(versionMetadata.getFormat(), versionTwoMetadata.getFormat());
	}
	
	@Test
	public void addVersions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-4");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Hello", "/fx/", "root.xml");
		media = mediaWithVersion.media();
		dbInstance.commitAndCloseSession();
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		mediaDao.addVersion(reloadedMedia, new Date(), "World", null, null);
		dbInstance.commitAndCloseSession();
		
		reloadedMedia = mediaDao.loadByKey(media.getKey());
		mediaDao.addVersion(reloadedMedia, new Date(), "!", null, null);
		dbInstance.commitAndCloseSession();
		
		Media versionedMedia = mediaDao.loadByKey(media.getKey());
		List<MediaVersion> versions = versionedMedia.getVersions();
		Assert.assertNotNull(versions);
		Assert.assertEquals(3, versions.size());
		//Check content
		Assert.assertEquals("!", versions.get(0).getContent());
		Assert.assertEquals("World", versions.get(1).getContent());
		Assert.assertEquals("Hello", versions.get(2).getContent());
		//Check position
		Assert.assertEquals(0l, ((MediaVersionImpl)versions.get(0)).getPos());
		Assert.assertEquals(1l, ((MediaVersionImpl)versions.get(1)).getPos());
		Assert.assertEquals(2l, ((MediaVersionImpl)versions.get(2)).getPos());
		//Check versio name
		Assert.assertEquals("0", versions.get(0).getVersionName());
		Assert.assertEquals("2", versions.get(1).getVersionName());
		Assert.assertEquals("1", versions.get(2).getVersionName());
	}
	
	@Test
	public void getVersions() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-5");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Mercury", "/fx/", "root.xml");
		media = mediaWithVersion.media();
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
	public void loadVersionByKey() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-5");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Mercury", "/fx/", "root.xml");
		media = mediaWithVersion.media();
		dbInstance.commitAndCloseSession();
		
		Media reloadedMedia = mediaDao.loadByKey(media.getKey());
		mediaDao.addVersion(reloadedMedia, new Date(), "Venus", null, null);
		dbInstance.commitAndCloseSession();
		
		// load the current version
		MediaVersion version = mediaDao.loadVersionByKey(mediaWithVersion.version().getKey());
		Assert.assertNotNull(version);
		Assert.assertEquals("Venus", version.getContent());
	}
	
	@Test
	public void loadByUuid() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-11");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Mercury", "/fx/", "root.xml");
		media = mediaWithVersion.media();
		dbInstance.commitAndCloseSession();
		String uuid = media.getUuid();
		
		Media reloadedMedia = mediaDao.loadByUuid(uuid);
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
	}
	
	@Test
	public void loadByMetadata() throws URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-11");
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageFile = new File(imageUrl.toURI());
		Media media = imageHandler.createMedia("Image", null, null, imageFile, imageFile.getName(), "[Image:0]", id, MediaLog.Action.UPLOAD);
		dbInstance.commit();
		
		Assert.assertEquals(1, media.getVersions().size());
		MediaVersion currentVersion = media.getVersions().get(0);
		VFSMetadata metadata = currentVersion.getMetadata();
		Assert.assertNotNull(metadata);

		Media reloadedMedia = mediaDao.loadByMetadata(metadata.getKey());
		Assert.assertNotNull(reloadedMedia);
		Assert.assertEquals(media, reloadedMedia);
	}
	
	@Test
	public void filterOwnedDeletableMedias() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-12");
		Media media = mediaDao.createMedia("Media", null, null, null, "Forum", null, null, 10, id);
		MediaWithVersion mediaWithVersion = mediaDao.createVersion(media, new Date(), null, "Mercury", "/fx/", "root.xml");
		media = mediaWithVersion.media();
		dbInstance.commitAndCloseSession();
		
		List<Long> deletableKeys = mediaDao.filterOwnedDeletableMedias(id, List.of(media.getKey(), 1234l));
		assertThat(deletableKeys)
			.hasSize(1)
			.containsExactly(media.getKey());
	}

	@Test
	public void load() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-18");
		Media media = mediaDao.createMediaAndVersion("Media 18", "The media theory", null, "Media theory is very important subject", "Forum", "[Media:0]", null, 10, author);
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
		Media media = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = MediaPart.valueOf(author, media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		List<MediaUsage> usages = mediaDao.getUsages(media);
		assertThat(usages)
			.hasSize(1);
	}
	
	@Test
	public void countUsages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-8");
		Page page = pageDao.createAndPersist("Page 8", "A page with usages.", null, null, true, null, null);
		Media media = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = MediaPart.valueOf(author, media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		//reload
		long usages = mediaDao.countUsages(List.of(media));
		Assert.assertEquals(1l, usages);
	}
	
	@Test
	public void getFileUsageByIdentity() throws URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-21");
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1483.png");
		File imageFile = new File(imageUrl.toURI());
		Media media = imageHandler.createMedia("Image", null, null, imageFile, imageFile.getName(), "[Image:0]",
				id, MediaLog.Action.UPLOAD);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(media);

		Usage usage = mediaDao.getFileUsage(id);
		Assert.assertEquals(imageFile.length(), usage.getSize());
	}
	
	@Test
	public void getFileUsageByPath() throws URISyntaxException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-22");
		URL imageUrl = JunitTestHelper.class.getResource("file_resources/IMG_1484.jpg");
		File imageFile = new File(imageUrl.toURI());
		Media media = imageHandler.createMedia("Image", null, null, imageFile, imageFile.getName(), "[Image:0]",
				id, MediaLog.Action.UPLOAD);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(media);

		String relPath = "/HomeSite/" + id.getKey() + "/MediaCenter/0/My/0";
		Usage usage = mediaDao.getFileUsage(relPath);
		Assert.assertNotNull(usage);
		Assert.assertEquals(imageFile.length(), usage.getSize());
	}
	
	@Test
	public void isUsedInPage() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-9");
		Page page = pageDao.createAndPersist("Page 1", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMediaAndVersion("Media", "Binder", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commitAndCloseSession();

		MediaPart mediaPart = MediaPart.valueOf(author, media);
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
		Media media = mediaDao.createMediaAndVersion("Media", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();

		MediaPart mediaPart = MediaPart.valueOf(author, media);
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
		Media media = mediaDao.createMediaAndVersion("Media 20", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(author, media);
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
		Assert.assertTrue(mediaUsage.revoked());
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
		Media media = mediaDao.createMediaAndVersion("Media 20", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		MediaPart mediaPart = MediaPart.valueOf(author, media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(reference);
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPageUsages(author, media);
		Assert.assertNotNull( mediaUsages);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertFalse(mediaUsage.revoked());
	}
	
	@Test
	public void getPortfolioUsages() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-21");
		Page page = pageDao.createAndPersist("Page 21", "A page with content.", null, null, true, null, null);
		Media media = mediaDao.createMediaAndVersion("Media 21", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(author, media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPortfolioUsages(author, author, media);
		Assert.assertNotNull( mediaUsages);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertEquals("Page 21", mediaUsage.pageTitle());
		Assert.assertEquals(media.getKey(), mediaUsage.mediaKey());
		Assert.assertEquals(mediaPart.getMediaVersion().getKey(), mediaUsage.mediaVersionKey());
		Assert.assertEquals("0", mediaUsage.mediaVersionName());
		Assert.assertTrue(mediaUsage.revoked());
	}
	
	@Test
	public void getPortfolioUsagesWithOwnership() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-21-1");
		Page page = portfolioService.appendNewPage(author, "Page 21-1", "A page with content.", null, null, null);
		Media media = mediaDao.createMediaAndVersion("Media 21-1", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		MediaPart mediaPart = MediaPart.valueOf(author, media);
		PageBody reloadedBody = pageDao.loadPageBodyByKey(page.getBody().getKey());
		pageDao.persistPart(reloadedBody, mediaPart);
		dbInstance.commitAndCloseSession();
		
		List<MediaUsageWithStatus> mediaUsages = mediaDao.getPortfolioUsages(author, author, media);
		Assert.assertEquals(1, mediaUsages.size());
		
		MediaUsageWithStatus mediaUsage = mediaUsages.get(0);
		Assert.assertEquals(page.getKey(), mediaUsage.pageKey());
		Assert.assertFalse(mediaUsage.revoked());
	}
	
	@Test
	public void isEditableByAuthor() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-22");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-23");
		Media media = mediaDao.createMediaAndVersion("Media 21", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		
		boolean editable = mediaDao.isShared(author, media, Boolean.TRUE);
		Assert.assertTrue(editable);
		boolean notEditable = mediaDao.isShared(id, media, Boolean.TRUE);
		Assert.assertFalse(notEditable);
	}
	
	@Test
	public void isEditableShared() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-24");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-25");
		Media media = mediaDao.createMediaAndVersion("Media 23", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		MediaToGroupRelation relation = mediaService.addRelation(media, true, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);
		
		boolean editable = mediaDao.isShared(author, media, Boolean.TRUE);
		Assert.assertTrue(editable);
		boolean editableToo = mediaDao.isShared(id, media, Boolean.TRUE);
		Assert.assertTrue(editableToo);
	}
	
	
	@Test
	public void isEditableSharedNotEditable() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-26");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-27");
		Media media = mediaDao.createMediaAndVersion("Media 24", "Alone", null, "Une citation sur les classeurs", TextHandler.TEXT_MEDIA, "[Media:0]", null, 10, author);
		dbInstance.commit();
		MediaToGroupRelation relation = mediaService.addRelation(media, false, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(relation);

		boolean cannotEdit = mediaDao.isShared(id, media, Boolean.TRUE);
		Assert.assertFalse(cannotEdit);
		boolean notEditable = mediaDao.isShared(id, media, Boolean.FALSE);
		Assert.assertTrue(notEditable);
		boolean shared = mediaDao.isShared(id, media, null);
		Assert.assertTrue(shared);
	}
	
}