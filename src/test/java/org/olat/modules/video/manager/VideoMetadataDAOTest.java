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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.video.VideoFormat;
import org.olat.modules.video.VideoMeta;
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
		List<VideoMeta> metadata = videoMetadataDao.getAllVideoResourcesMetadata();
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
		List<VideoMeta> deleteMetadata = videoMetadataDao.getAllVideoResourcesMetadata();
		Assert.assertFalse(deleteMetadata.contains(meta0));
		Assert.assertTrue(deleteMetadata.contains(meta1));
		Assert.assertTrue(deleteMetadata.contains(meta2));
		Assert.assertFalse(deleteMetadata.contains(meta3));
	}
	
}
