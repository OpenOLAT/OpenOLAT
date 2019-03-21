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
import org.olat.core.commons.services.vfs.VFSRevision;
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
		VFSRevision revision = revisionDao.createRevision(author, "._oo_vr_1_text.txt", 1, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(revision);
	}
	
	@Test
	public void getRevisions() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("rev-1");
		VFSMetadata metadata = vfsMetadataDao.createMetadata(UUID.randomUUID().toString(), "test/revs", "text.txt",
				new Date(), 10l, false, "file:///text.tx", "file", null);
		VFSRevision revision = revisionDao.createRevision(author, "._oo_vr_1_text.txt", 1, 25l, new Date(), "A comment", metadata);
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		Assert.assertEquals(revision, revisions.get(0));
	}

}
