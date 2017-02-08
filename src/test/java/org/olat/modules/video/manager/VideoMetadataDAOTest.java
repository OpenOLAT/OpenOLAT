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
import org.olat.modules.video.VideoMeta;
import org.olat.modules.video.model.VideoMetaImpl;
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
	public void createVideoMetadata () {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		//create metadata entries
		VideoMeta meta = videoMetadataDao.createVideoMetadata(entry, 1500, "vid.mp4");
		Assert.assertNotNull(meta);
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 5500, "vid.mov");
		Assert.assertNotNull(meta1);
		dbInstance.commitAndCloseSession();
		//retrieve by olatresource
		VideoMeta meta2 = videoMetadataDao.getVideoMetadata(entry.getOlatResource());
		Assert.assertNotNull(meta2);
		Assert.assertTrue(meta2.getSize() == 1500);
		//update value
		meta2.setSize(2500);
		Assert.assertTrue(meta2.getSize() == 2500);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteVideoMetadata () {
		RepositoryEntry entry0 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry3 = JunitTestHelper.createAndPersistRepositoryEntry();
		//create metadata entries
		VideoMeta meta0 = videoMetadataDao.createVideoMetadata(entry0, 1500, "vid.mp4");
		VideoMeta meta1 = videoMetadataDao.createVideoMetadata(entry1, 1100, "vide.mp4");
		VideoMeta meta2 = videoMetadataDao.createVideoMetadata(entry2, 1200, "video.mov");
		VideoMeta meta3 = videoMetadataDao.createVideoMetadata(entry3, 4500, "videos.mp4");
		Assert.assertNotNull(meta1);
		Assert.assertNotNull(meta3);
		Assert.assertNotNull(meta2);
		Assert.assertNotNull(meta0);
		dbInstance.commitAndCloseSession();
		//retrieve list of entries
		List<VideoMetaImpl> metadata = videoMetadataDao.getAllVideoResourcesMetadata();
		Assert.assertEquals(4, metadata.size());
		dbInstance.commitAndCloseSession();
		//delete entries
		int deleted0 = videoMetadataDao.deleteVideoMetadata(entry0.getOlatResource());
		int deleted1 = videoMetadataDao.deleteVideoMetadata(entry3.getOlatResource());
		Assert.assertEquals(1, deleted0);
		Assert.assertNotEquals(0, deleted1);
		dbInstance.commitAndCloseSession();
		//retrieve new list
		List<VideoMetaImpl> metadata1 = videoMetadataDao.getAllVideoResourcesMetadata();
		Assert.assertEquals(2, metadata1.size());
		Assert.assertEquals("mov", metadata1.get(1).getFormat());
		Assert.assertEquals(1100, metadata1.get(0).getSize());
		dbInstance.commitAndCloseSession();
	}
	
}
