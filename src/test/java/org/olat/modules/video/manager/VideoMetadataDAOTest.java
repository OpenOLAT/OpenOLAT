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
package org.olat.modules.video.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.VideoMetadataSearchParams;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: January 2017<br>
 * @author fkiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class VideoMetadataDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoMetadataDAO videoMetadataDao;
		
	@Test 
	public void createVideoMetadata() {
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//create metadata entries
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 1500, null, VideoFormat.mp4);
		Assert.assertNotNull(meta1);
		VideoMeta meta2 = videoMetadataDao.createVideoMetadata(entry2, 5500, null, VideoFormat.mp4);
		Assert.assertNotNull(meta2);
		dbInstance.commitAndCloseSession();
		
		//retrieve by olat resource
		VideoMeta reloadMeta1 = videoMetadataDao.getVideoMetadata(entry1.getOlatResource());
		Assert.assertNotNull(reloadMeta1);
		Assert.assertEquals(1500, reloadMeta1.getSize());
	}
	
	@Test 
	public void createVideoMetadata_url() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();

		//create metadata entries
		VideoMeta meta = videoMetadataDao.createVideoMetadata(entry, -1l, "https://frentix.com/video.mp4", null);
		Assert.assertNotNull(meta);
		dbInstance.commitAndCloseSession();
		
		//retrieve by olat resource
		VideoMeta reloadMeta = videoMetadataDao.getVideoMetadata(entry.getOlatResource());
		Assert.assertNotNull(reloadMeta);
		Assert.assertEquals("https://frentix.com/video.mp4", reloadMeta.getUrl());
	}
	
	@Test
	public void shouldFilterMetadataByUrlNull() {
		RepositoryEntry entry0 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		VideoMeta meta0 = videoMetadataDao.createVideoMetadata(entry0, 1500, null, VideoFormat.mp4);
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 1100, null, VideoFormat.mp4);
		VideoMeta meta2 = videoMetadataDao.createVideoMetadata(entry2, 1200, random(), VideoFormat.mp4);
		
		VideoMetadataSearchParams searchParams = new VideoMetadataSearchParams();
		searchParams.setUrlNull(Boolean.TRUE);
		List<VideoMeta> metadata = videoMetadataDao.getVideoMetadata(searchParams);
		
		assertThat(metadata)
				.contains(meta0, meta1)
				.doesNotContain(meta2);
	}
	
	@Test
	public void shouldFilterMetadataByUrlNotNull() {
		RepositoryEntry entry0 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		VideoMeta meta0 = videoMetadataDao.createVideoMetadata(entry0, 1500, random(), VideoFormat.mp4);
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 1100, random(), VideoFormat.mp4);
		VideoMeta meta2 = videoMetadataDao.createVideoMetadata(entry2, 1200, null, VideoFormat.mp4);
		
		VideoMetadataSearchParams searchParams = new VideoMetadataSearchParams();
		searchParams.setUrlNull(Boolean.FALSE);
		List<VideoMeta> metadata = videoMetadataDao.getVideoMetadata(searchParams);
		
		assertThat(metadata)
				.contains(meta0, meta1)
				.doesNotContain(meta2);
	}
	
	@Test
	public void shouldFilterMetadataByMinHight() {
		RepositoryEntry entry0 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry3 = JunitTestHelper.createAndPersistRepositoryEntry();
		VideoMeta meta0 = videoMetadataDao.createVideoMetadata(entry0, 1500, null, VideoFormat.mp4);
		meta0.setHeight(1000);
		meta0 = videoMetadataDao.updateVideoMetadata(meta0);
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 1500, null, VideoFormat.mp4);
		meta1.setHeight(2000);
		meta1 = videoMetadataDao.updateVideoMetadata(meta1);
		VideoMeta meta2 = videoMetadataDao.createVideoMetadata(entry2, 1500, null, VideoFormat.mp4);
		meta2.setHeight(500);
		meta2 = videoMetadataDao.updateVideoMetadata(meta2);
		VideoMeta meta3 = videoMetadataDao.createVideoMetadata(entry3, 1500, null, VideoFormat.mp4);
		
		VideoMetadataSearchParams searchParams = new VideoMetadataSearchParams();
		searchParams.setMinHeight(1000);
		List<VideoMeta> metadata = videoMetadataDao.getVideoMetadata(searchParams);
		
		assertThat(metadata)
				.contains(meta0, meta1)
				.doesNotContain(meta2, meta3);
	}
	
	@Test
	public void deleteVideoMetadata () {
		RepositoryEntry entry0 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry3 = JunitTestHelper.createAndPersistRepositoryEntry();
		
		//create metadata entries
		VideoMeta meta0 = videoMetadataDao.createVideoMetadata(entry0, 1500, null, VideoFormat.mp4);
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 1100, null, VideoFormat.mp4);
		VideoMeta meta2 = videoMetadataDao.createVideoMetadata(entry2, 1200, null, VideoFormat.mp4);
		VideoMeta meta3 = videoMetadataDao.createVideoMetadata(entry3, 4500, null, VideoFormat.mp4);
		Assert.assertNotNull(meta1);
		Assert.assertNotNull(meta3);
		Assert.assertNotNull(meta2);
		Assert.assertNotNull(meta0);
		dbInstance.commitAndCloseSession();
		
		//retrieve list of entries
		List<VideoMeta> metadata = videoMetadataDao.getVideoMetadata(new VideoMetadataSearchParams());
		Assert.assertTrue(metadata.contains(meta0));
		Assert.assertTrue(metadata.contains(meta1));
		Assert.assertTrue(metadata.contains(meta2));
		Assert.assertTrue(metadata.contains(meta3));

		//delete entries
		int deleted0 = videoMetadataDao.deleteVideoMetadata(entry0.getOlatResource());
		int deleted3 = videoMetadataDao.deleteVideoMetadata(entry3.getOlatResource());
		Assert.assertEquals(1, deleted0);
		Assert.assertNotEquals(0, deleted3);
		dbInstance.commitAndCloseSession();
		
		//retrieve new list
		List<VideoMeta> deleteMetadata = videoMetadataDao.getVideoMetadata(new VideoMetadataSearchParams());
		Assert.assertFalse(deleteMetadata.contains(meta0));
		Assert.assertTrue(deleteMetadata.contains(meta1));
		Assert.assertTrue(deleteMetadata.contains(meta2));
		Assert.assertFalse(deleteMetadata.contains(meta3));
	}
	
}
