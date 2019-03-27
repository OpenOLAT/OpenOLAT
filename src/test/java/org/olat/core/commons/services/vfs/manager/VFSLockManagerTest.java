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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockResult;
import org.olat.core.util.vfs.lock.VFSLockManagerImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test the VFS lock manager. The part for WebDAV is tested
 * by WebDAVCommandsTest.	
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSLockManagerTest extends OlatTestCase {
	
	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private VFSLockManagerImpl lockManager;
	
	/**
	 * A new file cannot be locked
	 */
	@Test
	public void isNewFileLocked() {
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// new file, no lock
		boolean locked = lockManager.isLocked(file, null, null);
		Assert.assertFalse(locked);
		
		// check if the metadata was created
		VFSMetadata metadata = metadataDao.getMetadata(relativePath, "lock.txt", false);
		Assert.assertNotNull(metadata);
	}
	
	/**
	 * A new file cannot be locked
	 */
	@Test
	public void isLockedForMe() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-2");
		Roles userRoles = Roles.userRoles();
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// new file, no lock
		boolean locked = lockManager.isLockedForMe(file, id, userRoles, null, null);
		Assert.assertFalse(locked);

		VFSMetadata metadata = metadataDao.getMetadata(relativePath, "lock.txt", false);
		boolean lockedAlt = lockManager.isLockedForMe(file, metadata, id, userRoles, null, null);
		Assert.assertFalse(lockedAlt);
	}
	
	/**
	 * A new file cannot be locked
	 */
	@Test
	public void lockVfs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-3");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-4");
		Roles userRoles = Roles.userRoles();
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// lock the file
		LockResult locked = lockManager.lock(file, id, userRoles, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(locked.isAcquired());

		VFSMetadata metadata = metadataDao.getMetadata(relativePath, "lock.txt", false);
		boolean lockedAlt = lockManager.isLockedForMe(file, metadata, id, userRoles, null, null);
		Assert.assertFalse(lockedAlt);
		
		// but other cannot
		boolean lockedOther = lockManager.isLockedForMe(file, otherId, userRoles, null, null);
		Assert.assertTrue(lockedOther);
	}
	
	/**
	 * A new file cannot be locked
	 */
	@Test
	public void lockUnlockVfs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-3");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-4");
		Roles userRoles = Roles.userRoles();
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// lock the file
		LockResult locked = lockManager.lock(file, id, userRoles, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(locked.isAcquired());

		// but other cannot
		boolean lockedOther = lockManager.isLockedForMe(file, otherId, userRoles, null, null);
		Assert.assertTrue(lockedOther);
		
		// first unlock it
		boolean unlocked = lockManager.unlock(file, id, userRoles, VFSLockApplicationType.vfs);
		Assert.assertTrue(unlocked);
		
		// other can now
		boolean unlockedOther = lockManager.isLockedForMe(file, otherId, userRoles, null, null);
		Assert.assertFalse(unlockedOther);
	}
}
