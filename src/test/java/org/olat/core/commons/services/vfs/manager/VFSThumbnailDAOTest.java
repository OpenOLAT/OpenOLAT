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
package org.olat.core.commons.services.vfs.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSThumbnailDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO vfsMetadataDao;
	@Autowired
	private VFSThumbnailDAO vfsThumbnailDao;
	
	@Test
	public void createMetadata() {
		String uuid = UUID.randomUUID().toString();
		String filename = uuid.concat(".jpg");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, "vfsrepo2test/world/", filename, new Date(), 1l, false, "file:///uri", "file", null);
		VFSThumbnailMetadata thumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._oo_th_", 1l, true, 200, 150, 120, 80);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(thumbnail);
		Assert.assertNotNull(thumbnail.getKey());
		Assert.assertNotNull(thumbnail.getCreationDate());
		Assert.assertNotNull(thumbnail.getLastModified());
		Assert.assertTrue(thumbnail.isFill());
		Assert.assertEquals(200, thumbnail.getMaxWidth());
		Assert.assertEquals(150, thumbnail.getMaxHeight());
		Assert.assertEquals(120, thumbnail.getFinalWidth());
		Assert.assertEquals(80, thumbnail.getFinalHeight());
	}
	
	@Test
	public void loadByKey() {
		String uuid = UUID.randomUUID().toString();
		String filename = uuid.concat(".jpg");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, "vfsrepo2test/world/", filename, new Date(), 1l, false, "file:///uri", "file", null);
		VFSThumbnailMetadata thumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._ooth_", 1l, true, 200, 150, 120, 80);
		dbInstance.commitAndCloseSession();
		
		VFSThumbnailMetadata reloadedThumbnail = vfsThumbnailDao.loadByKey(thumbnail.getKey());
		
		Assert.assertNotNull(reloadedThumbnail);
		Assert.assertNotNull(reloadedThumbnail.getKey());
		Assert.assertNotNull(reloadedThumbnail.getCreationDate());
		Assert.assertNotNull(reloadedThumbnail.getLastModified());
		Assert.assertTrue(reloadedThumbnail.isFill());
		Assert.assertEquals(thumbnail, reloadedThumbnail);
		Assert.assertEquals(metadata, reloadedThumbnail.getOwner());
		Assert.assertEquals("._ooth_", reloadedThumbnail.getFilename());
		Assert.assertEquals(200, reloadedThumbnail.getMaxWidth());
		Assert.assertEquals(150, reloadedThumbnail.getMaxHeight());
		Assert.assertEquals(120, reloadedThumbnail.getFinalWidth());
		Assert.assertEquals(80, reloadedThumbnail.getFinalHeight());
	}
	
	@Test
	public void loadByMetadata() {
		String uuid = UUID.randomUUID().toString();
		String filename = uuid.concat(".png");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, "vfsrepo2test/world/", filename, new Date(), 1l, false, "file:///uri", "file", null);
		VFSThumbnailMetadata thumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._ooth_", 1l, true, 200, 150, 120, 80);
		dbInstance.commitAndCloseSession();
		
		List<VFSThumbnailMetadata> thumbnails = vfsThumbnailDao.loadByMetadata(metadata);

		Assert.assertNotNull(thumbnails);
		Assert.assertEquals(1, thumbnails.size());
		Assert.assertEquals(thumbnail, thumbnails.get(0));
	}
	
	@Test
	public void findThumbnail() {
		String uuid = UUID.randomUUID().toString();
		String filename = uuid.concat(".jpg");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, "vfsrepo2test/world/", filename, new Date(), 1l, false, "file:///uri", "file", null);
		VFSThumbnailMetadata thumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._ooth_", 1l, true, 200, 150, 120, 80);
		dbInstance.commitAndCloseSession();
		
		VFSThumbnailMetadata reloadedThumbnail = vfsThumbnailDao.findThumbnail("vfsrepo2test/world/", filename, true, 200, 150);

		Assert.assertNotNull(reloadedThumbnail);
		Assert.assertNotNull(reloadedThumbnail.getKey());
		Assert.assertNotNull(reloadedThumbnail.getCreationDate());
		Assert.assertNotNull(reloadedThumbnail.getLastModified());
		Assert.assertEquals(thumbnail, reloadedThumbnail);
		Assert.assertTrue(reloadedThumbnail.isFill());
		Assert.assertEquals(metadata, reloadedThumbnail.getOwner());
		Assert.assertEquals("._ooth_", reloadedThumbnail.getFilename());
		Assert.assertEquals(200, reloadedThumbnail.getMaxWidth());
		Assert.assertEquals(150, reloadedThumbnail.getMaxHeight());
		Assert.assertEquals(120, reloadedThumbnail.getFinalWidth());
		Assert.assertEquals(80, reloadedThumbnail.getFinalHeight());
	}
	
	@Test
	public void findThumbnails() {
		String uuid = UUID.randomUUID().toString();
		String filename = uuid.concat(".jpg");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, "vfsrepo2test/world/", filename, new Date(), 1l, false, "file:///uri", "file", null);
		VFSThumbnailMetadata thumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._ooth_", 1l, true, 200, 150, 120, 80);
		dbInstance.commitAndCloseSession();
		
		List<VFSThumbnailMetadata> thumbnails = vfsThumbnailDao.findThumbnails("vfsrepo2test/world/", filename);

		Assert.assertNotNull(thumbnails);
		Assert.assertEquals(1, thumbnails.size());
		Assert.assertEquals(thumbnail, thumbnails.get(0));
	}
	
	@Test
	public void findThumbnail_byMetadata() {
		String uuid = UUID.randomUUID().toString();
		String filename = uuid.concat(".jpg");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, "vfsrepo2test/world/", filename, new Date(), 1l, false, "file:///uri", "file", null);
		VFSThumbnailMetadata thumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._ooth_", 1l, true, 200, 150, 120, 80);
		VFSThumbnailMetadata largeThumbnail = vfsThumbnailDao.createThumbnailMetadata(metadata, "._ooth_", 1l, true, 400, 300, 120, 80);
		dbInstance.commitAndCloseSession();
		
		VFSThumbnailMetadata reloadedThumbnail = vfsThumbnailDao.findThumbnail(metadata, true, 200, 150);

		Assert.assertNotNull(reloadedThumbnail);
		Assert.assertEquals(thumbnail, reloadedThumbnail);
		Assert.assertNotEquals(largeThumbnail, reloadedThumbnail);
	}
}
