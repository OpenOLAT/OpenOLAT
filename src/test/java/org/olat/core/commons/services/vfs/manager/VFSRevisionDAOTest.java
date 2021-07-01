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
		VFSRevision revision1 = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", metadata);
		VFSRevision revision2 = revisionDao.createRevision(author, author, "._oo_vr_2_text.txt", 2, null, 26l, new Date(), "A comment", metadata);
		VFSRevision revision3 = revisionDao.createRevision(author, author, "._oo_vr_3_text.txt", 3, null, 27l, new Date(), "A comment", metadata);
		VFSRevision revision4 = revisionDao.createRevision(author, author, "._oo_vr_4_text.txt", 4, null, 28l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(revision1);
		Assert.assertNotNull(revision2);
		Assert.assertNotNull(revision3);
		Assert.assertNotNull(revision4);
		
		List<VFSMetadataRef> metadataRefs = new ArrayList<>();
		metadataRefs.add(metadata);
		List<VFSMetadataRef> metadataWithRevs = revisionDao.getMetadataWithMoreThan(3);
		Assert.assertNotNull(metadataWithRevs);
		Assert.assertTrue(metadataWithRevs.contains(new VFSMetadataRefImpl(metadata.getKey())));
		
		for(VFSMetadataRef metadataRef:metadataWithRevs) {
			List<VFSRevision> revisions = revisionDao.getRevisions(metadataRef);
			Assert.assertTrue(revisions.size() > 3);
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
	public void getMetadataOfDeletedFiles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata deletedMetadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision deletedRevision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", deletedMetadata);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(deletedRevision);
		// mark as deleted
		((VFSMetadataImpl)deletedMetadata).setDeleted(true);
		deletedMetadata = vfsMetadataDao.updateMetadata(deletedMetadata);
		dbInstance.commitAndCloseSession();
		
		List<VFSMetadataRef> deletedRefs = revisionDao.getMetadataOfDeletedFiles();
		Assert.assertNotNull(deletedRefs);
		Assert.assertTrue(deletedRefs.contains(new VFSMetadataRefImpl(deletedMetadata.getKey())));
	}
	
	@Test
	public void getRevisionsOfDeletedFiles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata deletedMetadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision deletedRevision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", deletedMetadata);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(deletedRevision);
		// mark as deleted
		((VFSMetadataImpl)deletedMetadata).setDeleted(true);
		deletedMetadata = vfsMetadataDao.updateMetadata(deletedMetadata);
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> deletedVersions = revisionDao.getRevisionsOfDeletedFiles();
		Assert.assertNotNull(deletedVersions);
		Assert.assertTrue(deletedVersions.contains(deletedRevision));
	}
	
	@Test
	public void getRevisionsSizeOfDeletedFiles() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata deletedMetadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision deletedRevision = revisionDao.createRevision(author, author, "._oo_vr_1_text.txt", 1, null, 25l, new Date(), "A comment", deletedMetadata);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(deletedRevision);
		// mark as deleted
		((VFSMetadataImpl)deletedMetadata).setDeleted(true);
		deletedMetadata = vfsMetadataDao.updateMetadata(deletedMetadata);
		dbInstance.commitAndCloseSession();
		
		long size = revisionDao.getRevisionsSizeOfDeletedFiles();
		Assert.assertTrue(size >= 25l);
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
