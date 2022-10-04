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

import java.time.ZonedDateTime;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSMetadataDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO vfsMetadataDao;
	
	@Test
	public void createMetadata() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = "image.jpg";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 10l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getCreationDate());
		Assert.assertNotNull(metadata.getLastModified());
		Assert.assertEquals(uuid, metadata.getUuid());
		Assert.assertEquals(relativePath, metadata.getRelativePath());
		Assert.assertEquals(filename, metadata.getFilename());
		Assert.assertFalse(metadata.isDirectory());
		Assert.assertEquals(uri, metadata.getUri());
		Assert.assertEquals(uriProtocol, metadata.getProtocol());
	}
	
	@Test
	public void getMetadata_uuid() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = "image.jpg";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 15l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		VFSMetadata loadedMetadata = vfsMetadataDao.getMetadata(uuid);
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(metadata.getKey(), loadedMetadata.getKey());
		Assert.assertEquals(uuid, metadata.getUuid());
		Assert.assertEquals(relativePath, metadata.getRelativePath());
		Assert.assertEquals(filename, metadata.getFilename());
		Assert.assertFalse(metadata.isDirectory());
		Assert.assertEquals(uri, metadata.getUri());
		Assert.assertEquals(uriProtocol, metadata.getProtocol());
	}
	
	@Test
	public void getMetadata_path() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = uuid + ".jpg";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 18l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		VFSMetadata loadedMetadata = vfsMetadataDao.getMetadata(relativePath, filename, false);
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(metadata.getKey(), loadedMetadata.getKey());
		Assert.assertEquals(uuid, metadata.getUuid());
		Assert.assertEquals(relativePath, metadata.getRelativePath());
		Assert.assertEquals(filename, metadata.getFilename());
		Assert.assertEquals(18l, metadata.getFileSize());
		Assert.assertFalse(metadata.isDirectory());
		Assert.assertEquals(uri, metadata.getUri());
		Assert.assertEquals(uriProtocol, metadata.getProtocol());
	}

	@Test
	public void incrementDownloadCounter() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = uuid + ".pdf";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 18l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		vfsMetadataDao.increaseDownloadCount(relativePath, filename);
		dbInstance.commitAndCloseSession();
		
		VFSMetadata loadedMetadata = vfsMetadataDao.loadMetadata(metadata.getKey());
		
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(1, loadedMetadata.getDownloadCount());
	}
	
	@Test
	public void setDownloadCounter() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = uuid + ".pdf";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image_alt.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 18l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		vfsMetadataDao.setDownloadCount(metadata, 8);
		dbInstance.commitAndCloseSession();
		
		VFSMetadata loadedMetadata = vfsMetadataDao.loadMetadata(metadata.getKey());
		
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(8, loadedMetadata.getDownloadCount());
	}
	
	@Test
	public void updateFileSize() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = uuid + ".pdf";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";
		String uriProtocol = "file";
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 18l, false, uri, uriProtocol, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		
		Identity initializedBy = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		Identity lastModifiedBy = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		vfsMetadataDao.updateMetadata(12345l, new GregorianCalendar(2019, 1, 1).getTime(), initializedBy, lastModifiedBy, relativePath, filename);
		dbInstance.commitAndCloseSession();
		
		VFSMetadata loadedMetadata = vfsMetadataDao.loadMetadata(metadata.getKey());
		
		Assert.assertEquals(metadata, loadedMetadata);
		Assert.assertEquals(12345l, loadedMetadata.getFileSize());
		Assert.assertEquals(initializedBy, loadedMetadata.getFileInitializedBy());
		Assert.assertEquals(lastModifiedBy, loadedMetadata.getFileLastModifiedBy());
	}
	
	@Test
	public void getLargest() {
		int maxResult = 100;
		Date createdAtNewer = Date.from(ZonedDateTime.now().minusMonths(5).toInstant());
		Date createdAtOlder = Date.from(ZonedDateTime.now().toInstant());
		Date editedAtNewer = Date.from(ZonedDateTime.now().minusMonths(5).toInstant());
		Date editedAtOlder = Date.from(ZonedDateTime.now().toInstant());
		
		List<VFSMetadata> queryResult = vfsMetadataDao.getLargest(maxResult, createdAtNewer, createdAtOlder, editedAtNewer, editedAtOlder, null, null, null, null, 0, Long.valueOf(0), 0);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(queryResult);
		Assert.assertTrue(queryResult.size() > 0);
		Assert.assertTrue(queryResult.size() <= maxResult);
		for (VFSMetadata vfsMetadata : queryResult) {
			Assert.assertTrue(vfsMetadata.getCreationDate().compareTo(createdAtNewer) >= 0);
			Assert.assertTrue(vfsMetadata.getCreationDate().compareTo(createdAtOlder) <= 0);
			Assert.assertTrue(vfsMetadata.getLastModified().compareTo(editedAtNewer) >= 0);
			Assert.assertTrue(vfsMetadata.getLastModified().compareTo(editedAtOlder) <= 0);
		}
	}
	
	@Test
	public void getExpiredMetadatas() {
		String uuid = UUID.randomUUID().toString();
		String relativePath = "/bcroot/hello/world/";
		String filename = uuid + ".pdf";
		String uri = "file:///Users/frentix/Documents/bcroot/hello/world/image.jpg";;
		VFSMetadata metadata = vfsMetadataDao.createMetadata(uuid, relativePath, filename, new Date(), 18l, false, uri, "file", null);
		dbInstance.commitAndCloseSession();
		metadata.setExpirationDate(DateUtils.addDays(new Date(), -1));
		Assert.assertNotNull(metadata);
		
		final Date now = new Date();
		List<VFSMetadata> expiredList = vfsMetadataDao.getExpiredMetadatas(now);
		dbInstance.commitAndCloseSession();
		
		for (VFSMetadata vfsMetadata : expiredList) {
			Assert.assertNotNull(vfsMetadata.getExpirationDate());
			Assert.assertTrue(vfsMetadata.getExpirationDate().compareTo(now) <= 0);
		}
	}

	@Test
	public void setTranscodingStatus() {
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String relativePath = "/bcroot/course/test/";
		String fileName1 = uuid1 + ".mp4";
		String fileName2 = uuid2 + ".mp3";
		String uri1 = "file:///Users/frentix/Documents/bcroot/course/test/" + fileName1;
		String uri2 = "file:///Users/frentix/Documents/bcroot/course/test/" + fileName2;

		VFSMetadata metadata1 = vfsMetadataDao.createMetadata(uuid1, relativePath, fileName1, new Date(), 100L, false, uri1, "file", null);
		VFSMetadata metadata2 = vfsMetadataDao.createMetadata(uuid1, relativePath, fileName2, new Date(), 100L, false, uri2, "file", null);

		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(metadata1);
		Assert.assertNotNull(metadata2);

		vfsMetadataDao.setTranscodingStatus(metadata1.getKey(), VFSMetadata.TRANSCODING_STATUS_WAITING);

		dbInstance.commitAndCloseSession();

		List<VFSMetadata> inNeedForTranscoding = vfsMetadataDao.getMetadatasInNeedForTranscoding();
		Assert.assertEquals(1, inNeedForTranscoding.size());
		Assert.assertEquals(metadata1, inNeedForTranscoding.get(0));

		List<VFSMetadata> allItems = vfsMetadataDao.getMetadatas(relativePath);
		Assert.assertEquals(2, allItems.size());
		Assert.assertEquals(Set.of(metadata1, metadata2), Set.of(allItems.toArray()));

		vfsMetadataDao.setTranscodingStatus(metadata1.getKey(), VFSMetadata.TRANSCODING_STATUS_DONE);
		vfsMetadataDao.setTranscodingStatus(metadata2.getKey(), VFSMetadata.TRANSCODING_STATUS_WAITING);
		dbInstance.commitAndCloseSession();

		List<VFSMetadata> inNeedForTranscodingAfterUpdate = vfsMetadataDao.getMetadatasInNeedForTranscoding();
		Assert.assertEquals(1, inNeedForTranscodingAfterUpdate.size());
		Assert.assertEquals(metadata2, inNeedForTranscodingAfterUpdate.get(0));

		List<VFSMetadata> allItemsAfterTests = vfsMetadataDao.getMetadatas(relativePath);
		for (VFSMetadata item : allItemsAfterTests) {
			vfsMetadataDao.removeMetadata(item);
		}
		dbInstance.commitAndCloseSession();

		List<VFSMetadata> inNeedForTranscodingAfterCleanup = vfsMetadataDao.getMetadatasInNeedForTranscoding();
		Assert.assertEquals(0, inNeedForTranscodingAfterCleanup.size());
	}
}
