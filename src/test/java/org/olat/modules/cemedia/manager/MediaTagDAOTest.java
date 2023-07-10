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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.id.Identity;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaTag;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaTagDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private TagService tagService;
	@Autowired
	private MediaTagDAO mediaTagDao;
	
	@Test
	public void createMediaTag() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-tag-1");
		Media media = mediaDao.createMedia("Media tag 1", "Media description", null, "Media content", "Forum", "[Media:0]", null, 10, id);
		Tag tag = tagService.getOrCreateTag(random());
		MediaTag mediaTag = mediaTagDao.create(media, tag);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(mediaTag);
		Assert.assertNotNull(mediaTag.getKey());
		Assert.assertNotNull(mediaTag.getCreationDate());
		Assert.assertEquals(media, mediaTag.getMedia());
		Assert.assertEquals(tag, mediaTag.getTag());
	}
	
	@Test
	public void loadMediaTagInfosByMedia() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-tag-2");
		Media media = mediaDao.createMedia("Media tag 2", "Media description", null, "Media content", "Forum", "[Media:0]", null, 10, id);
		Tag tag = tagService.getOrCreateTag(random());
		MediaTag mediaTag = mediaTagDao.create(media, tag);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(mediaTag);
		List<TagInfo> tagInfos = mediaTagDao.loadMediaTagInfos(media, id);
		Assert.assertNotNull(tagInfos);
		Assert.assertFalse(tagInfos.isEmpty());

		boolean selected = tagInfos.stream()
				.filter(tagInfo -> tag.getKey().equals(tagInfo.getKey()))
				.findFirst().orElse(null)
				.isSelected();
		Assert.assertTrue(selected);
	}
	
	@Test
	public void loadMediaTagInfosByMediaSelected() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-tag-3");
		Media media = mediaDao.createMedia("Media tag 2 to selecr", "Media description", null, "Media content", "Forum", "[Media:0]", null, 10, id);
		Tag tag = tagService.getOrCreateTag(random());
		MediaTag mediaTag = mediaTagDao.create(media, tag);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(mediaTag);
		List<TagInfo> tagInfos = mediaTagDao.loadSelectedMediaTagInfos(media);
		Assert.assertNotNull(tagInfos);
		Assert.assertEquals(1, tagInfos.size());
		Assert.assertEquals(tag.getKey(), tagInfos.get(0).getKey());
	}
	
	@Test
	public void loadMediaTagInfosByAuthor() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-tag-2");
		Media media = mediaDao.createMedia("Media tag 2", "Media description", null, "Media content", "Forum", "[Media:0]", null, 10, id);
		Tag tag = tagService.getOrCreateTag(random());
		MediaTag mediaTag = mediaTagDao.create(media, tag);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(mediaTag);
		List<TagInfo> tagInfos = mediaTagDao.loadMediaTagInfos(null, id);
		Assert.assertNotNull(tagInfos);
		Assert.assertFalse(tagInfos.isEmpty());

		TagInfo testTagInfo = tagInfos.stream()
				.filter(tagInfo -> tag.getKey().equals(tagInfo.getKey()))
				.findFirst().orElse(null);
		Assert.assertNotNull(testTagInfo);
		Assert.assertEquals(Long.valueOf(1l), testTagInfo.getCount());
	}
	
	@Test
	public void loadMediaTagsByMedia() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-tag-3");
		Media media = mediaDao.createMedia("Media tag 3", "Media description", null, "Media content", "Forum", "[Media:0]", null, 10, id);
		Tag tag = tagService.getOrCreateTag(random());
		MediaTag mediaTag = mediaTagDao.create(media, tag);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(mediaTag);
		List<MediaTag> tagInfos = mediaTagDao.loadMediaTags(media);
		assertThat(tagInfos)
			.hasSize(1)
			.contains(mediaTag);
	}
	
	@Test
	public void loadMediaTags() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-media-tag-4");
		Media media = mediaDao.createMedia("Media tag 4", "Media description", null, "Media content", "Forum", "[Media:0]", null, 10, id);
		Tag tag = tagService.getOrCreateTag(random());
		MediaTag mediaTag = mediaTagDao.create(media, tag);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(mediaTag);
		List<MediaTag> tagInfos = mediaTagDao.loadMediaTags(id, List.of(media.getKey()));
		assertThat(tagInfos)
			.hasSize(1)
			.contains(mediaTag);
		
	}
	
}
