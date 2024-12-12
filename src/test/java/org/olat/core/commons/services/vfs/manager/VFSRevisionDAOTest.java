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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.vfs.model.VFSMetadataRefImpl;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSRevisionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired 
	private VFSRevisionDAO revisionDao;
	@Autowired
	private VFSMetadataDAO vfsMetadataDao;
	
	@Test
	public void createRevision() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, 99, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(revision);
	}
	
	@Test
	public void getRevisions() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		Assert.assertEquals(revision, revisions.get(0));
	}
	
	@Test
	public void getRevisions_collection() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		List<VFSMetadataRef> metadataRefs = new ArrayList<>();
		metadataRefs.add(metadata);
		List<VFSRevision> revisions = revisionDao.getRevisions(metadataRefs);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		Assert.assertEquals(revision, revisions.get(0));
	}
	
	@Test
	public void getRevisions_more() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-3");
		List<VFSMetadata> metadatas = new ArrayList<>();
		List<VFSRevision> revisions = new ArrayList<>();
		for(int i=0; i<10; i++) {
			VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text" + i + ".txt",
				new Date(), 10l, false, "file:///text" + i + ".tx", "file", null);
			VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text" + i + ".txt", 1, null, 25l, new Date(), "A comment", metadata);
			metadatas.add(metadata);
			revisions.add(revision);
		}
		
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> loadedRevisions = revisionDao.getRevisions(new ArrayList<>(metadatas));
		Assertions.assertThat(loadedRevisions)
			.hasSize(10)
			.containsAll(revisions)
			.map(VFSRevision::getMetadata)
			.containsAll(metadatas);
	}
	
	@Test
	public void getRevisions_emptyCollection() {
		List<VFSMetadataRef> metadataRefs = new ArrayList<>();
		List<VFSRevision> revisions = revisionDao.getRevisions(metadataRefs);
		Assert.assertNotNull(revisions);
		Assert.assertTrue(revisions.isEmpty());
	}
	
	@Test
	public void getMetadataWithMoreThan() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		for(int i=1; i<=8; i++) {
			VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_" + i + "_text.txt", i, null, 25l + i, new Date(), "A comment", metadata);
			Assert.assertNotNull(revision);
		}
		dbInstance.commitAndCloseSession();
		
		List<VFSMetadataRef> metadataRefs = new ArrayList<>();
		metadataRefs.add(metadata);
		List<VFSMetadataRef> metadataWithRevs = revisionDao.getMetadataWithMoreThan(7);
		Assert.assertNotNull(metadataWithRevs);
		Assert.assertTrue(metadataWithRevs.contains(new VFSMetadataRefImpl(metadata.getKey())));
		
		for(VFSMetadataRef metadataRef:metadataWithRevs) {
			List<VFSRevision> revisions = revisionDao.getRevisions(metadataRef);
			Assert.assertTrue(revisions.size() > 7);
		}
	}
	
	@Test
	public void getMetadataKeysOfDeletedFiles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		
		VFSMetadata deletedMetadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision deletedRevision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", deletedMetadata);
		
		VFSMetadata withoutMetadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(revision);
		Assert.assertNotNull(deletedRevision);
		// mark one as deleted
		((VFSMetadataImpl)deletedMetadata).setDeleted(true);
		deletedMetadata = vfsMetadataDao.updateMetadata(deletedMetadata);
		dbInstance.commitAndCloseSession();
		
		List<Long> deletedMetadataKeys = revisionDao.getMetadataKeysOfDeletedFiles();
		Assert.assertNotNull(deletedMetadataKeys);
		Assert.assertFalse(deletedMetadataKeys.contains(metadata.getKey()));
		Assert.assertTrue(deletedMetadataKeys.contains(deletedMetadata.getKey()));
		Assert.assertFalse(deletedMetadataKeys.contains(withoutMetadata.getKey()));
	}
	
	@Test
	public void testGetRevisionsSize() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		String uuid1 = UUID.randomUUID().toString();
		String uuid2 = UUID.randomUUID().toString();
		String parentPath = "/bcroot/course/" + uuid1;
		String relativePath = parentPath + "/hello";
		String relativePathTwo = parentPath + "/helloWorld";
		String fileName1 = uuid1 + ".mp4";
		String fileName21 = uuid2 + ".mp21";
		String fileName22 = uuid2 + ".mp22";
		String fileName23 = uuid2 + ".mp23";
		
		String uri1 = "file:///Users/frentix/Documents/bcroot/course/test/" + fileName1;
		String uri21 = "file:///Users/frentix/Documents/bcroot/course/test/" + fileName21;
		String uri22 = "file:///Users/frentix/Documents/bcroot/course/test/" + fileName22;
		String uri23 = "file:///Users/frentix/Documents/bcroot/course/test/" + fileName23;
		
		List<VFSMetadata> allItemsBeforeTests = vfsMetadataDao.getMetadatas(relativePath);
		for (VFSMetadata item : allItemsBeforeTests) {
			vfsMetadataDao.removeMetadata(item);
		}
		dbInstance.commitAndCloseSession();
		
		VFSMetadata container = vfsMetadataDao.createMetadata(uuid1, "/bcroot/course", uuid1, new Date(), 0L, true, uri1, "file", null);
		VFSMetadata container1 = vfsMetadataDao.createMetadata(uuid1, parentPath, "hello", new Date(), 0L, true, uri1, "file", container);
		VFSMetadata container2 = vfsMetadataDao.createMetadata(uuid1, parentPath, "helloWorld", new Date(), 0L, true, uri1, "file", container);
		VFSMetadata metadata1 = vfsMetadataDao.createMetadata(uuid1, relativePath, fileName1, new Date(), 0L, false, uri1, "file", container1);
		revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 100l, new Date(), "A comment", metadata1);
		VFSMetadataImpl fileMetadata21Deleted = (VFSMetadataImpl)vfsMetadataDao.createMetadata(uuid1, relativePathTwo, fileName21, new Date(), 0L, false, uri21, "file", container2);
		fileMetadata21Deleted.setDeleted(true);
		vfsMetadataDao.updateMetadata(fileMetadata21Deleted);
		revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 80l, new Date(), "A comment", fileMetadata21Deleted);
		revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 30l, new Date(), "A comment", fileMetadata21Deleted);
		VFSMetadata metadata22 = vfsMetadataDao.createMetadata(uuid1, relativePathTwo, fileName22, new Date(), 0L, false, uri22, "file", container2);
		revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 100l, new Date(), "A comment", metadata22);
		VFSMetadata metadata23 = vfsMetadataDao.createMetadata(uuid1, relativePathTwo, fileName23, new Date(), 0L, false, uri23, "file", container2);
		revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 50l, new Date(), "A comment", metadata23);
		dbInstance.commitAndCloseSession();
		
		Long size = revisionDao.getRevisionsSize(container, null);
		Assert.assertEquals(Long.valueOf(360), size);
		
		size = revisionDao.getRevisionsSize(container, Boolean.TRUE);
		Assert.assertEquals(Long.valueOf(110), size);
		
		size = revisionDao.getRevisionsSize(container, Boolean.FALSE);
		Assert.assertEquals(Long.valueOf(250), size);
	}
	
	@Test
	public void calculateRevisionsSize() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision1 = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(revision1);
		
		long size = revisionDao.calculateRevisionsSize();
		Assert.assertTrue(size >= 25l);
	}
	
	@Test
	public void getLargest() {
		Date fiveMinutesInThePast = addMinutesToDate(-5, new Date());
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata1 = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text1.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision1 = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, fiveMinutesInThePast, "A comment", metadata1);
		VFSMetadata metadata2 = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text2.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision2 = revisionDao.createRevision(author, author, "._oo_vr_2_text.txt", 1, null, 25l, fiveMinutesInThePast, "A comment", metadata2);
		VFSMetadata metadata3 = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text3.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision3 = revisionDao.createRevision(author, author, "._oo_vr_3_text.txt", 1, null, 25l, fiveMinutesInThePast, "A comment", metadata3);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(revision1);
		Assert.assertNotNull(revision2);
		Assert.assertNotNull(revision3);
		
		int maxResult = 100;
		Date createdAtNewer = addMinutesToDate(-150, new Date());
		Date createdAtOlder = addMinutesToDate(5, new Date());
		Date editedAtNewer = addMinutesToDate(-150, new Date());
		Date editedAtOlder = addMinutesToDate(5, new Date());
		
		List<VFSRevision> queryResult = revisionDao.getLargest(maxResult, createdAtNewer, createdAtOlder, editedAtNewer, editedAtOlder, null, null, null, null, null, null, null);
		
		Assert.assertNotNull(queryResult);
		Assert.assertTrue(queryResult.size() > 0);
		Assert.assertTrue(queryResult.size() <= maxResult);
		for (VFSRevision vfsMetadata : queryResult) {
			Assert.assertTrue(vfsMetadata.getCreationDate().compareTo(createdAtNewer) >= 0);
			Assert.assertTrue(vfsMetadata.getCreationDate().compareTo(createdAtOlder) <= 0);
			Assert.assertTrue(vfsMetadata.getFileLastModified().compareTo(editedAtNewer) >= 0);
			Assert.assertTrue(vfsMetadata.getFileLastModified().compareTo(editedAtOlder) <= 0);
		}
	}
	
	/**
	 * Only check the query syntax
	 */
	@Test
	public void getLargest_allParameters() {
		Date now = new Date();
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text_all.txt",
				new Date(), 10000l, false, "file:///text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_all_text.txt", 1, null, 25l, now, "A comment", metadata);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(revision);

		List<VFSRevision> queryResult = revisionDao.getLargest(100, now, now, now, now, now, now,
				Boolean.TRUE, Boolean.FALSE, Integer.valueOf(25), Long.valueOf(33), Integer.valueOf(55));
		
		Assert.assertNotNull(queryResult);
	}
	
	@Test
	public void deleteRevision() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/del/revs", "text.txt",
				new Date(), 10l, false, "file:///del/text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		revisionDao.deleteRevision(revision);
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertTrue(revisions.isEmpty());
	}
	
	/**
	 * This case can happen in the folder component. The unit test
	 * check that the exception is swallowed and that the transaction
	 * is not set to "rollback" (the implementation has changed several
	 * times).
	 */
	@Test
	public void doubleDeleteRevision() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/del/revs", "text.txt",
				new Date(), 10l, false, "file:///del/text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		revisionDao.deleteRevision(revision);
		dbInstance.commitAndCloseSession();

		revisionDao.deleteRevision(revision);
		List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
		// this update produce an error because the transaction is in error status
		metadata.setCity("Biel");
		vfsMetadataDao.updateMetadata(metadata);
		dbInstance.commit();
		
		Assert.assertNotNull(revisions);
		Assert.assertTrue(revisions.isEmpty());
	}
	
	private Date addMinutesToDate(int minutes, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minutes);
		return cal.getTime();
	}
	
}
