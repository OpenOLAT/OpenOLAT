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
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockInfo;
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
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private VFSLockManagerImpl lockManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
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
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// new file, no lock
		boolean locked = lockManager.isLockedForMe(file, id, null, null);
		Assert.assertFalse(locked);

		VFSMetadata metadata = metadataDao.getMetadata(relativePath, "lock.txt", false);
		boolean lockedAlt = lockManager.isLockedForMe(file, metadata, id, null, null);
		Assert.assertFalse(lockedAlt);
	}
	
	@Test
	public void canLockStandardFiles() {
    	Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-lock-10");
		VFSContainer homeFolder = VFSManager.olatRootContainer(FolderConfig.getUserHome(id), null);
		VFSContainer publicContainer = VFSManager.getOrCreateContainer(homeFolder, "public");
		
		// Some files
		VFSLeaf docLeaf = publicContainer.createChildLeaf("Bonjour.docx");
		Assert.assertTrue(lockManager.canLock(docLeaf, null));
		VFSLeaf docHelpLeaf = publicContainer.createChildLeaf("~$Bonjour.docx");
		Assert.assertTrue(lockManager.canLock(docHelpLeaf, null));
	}
		
	@Test
	public void canLockOpenOlatSystemFiles() {
    	Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-lock-11");
		VFSContainer homeFolder = VFSManager.olatRootContainer(FolderConfig.getUserHome(id), null);
		VFSContainer publicContainer = VFSManager.getOrCreateContainer(homeFolder, "public");
		
		// OpenOlat system files
		VFSLeaf openolatMetadataLeaf = publicContainer.createChildLeaf("._oo_meta_Bonjour.docx");
		Assert.assertFalse(lockManager.canLock(openolatMetadataLeaf, null));
		VFSLeaf openolatThumbnailLeaf = publicContainer.createChildLeaf("._oo_th_Bonjour.docx");
		Assert.assertFalse(lockManager.canLock(openolatThumbnailLeaf, null));
		VFSLeaf openolatVersionLeaf = publicContainer.createChildLeaf("._oo_vr_Bonjour.docx");
		Assert.assertFalse(lockManager.canLock(openolatVersionLeaf, null));
	}
	
	@Test
	public void canLockHiddenFiles() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-lock-12");
		VFSContainer homeFolder = VFSManager.olatRootContainer(FolderConfig.getUserHome(id), null);
		VFSContainer publicContainer = VFSManager.getOrCreateContainer(homeFolder, "public");
		
		// macos hidden files
		VFSLeaf dsStoreLeaf = publicContainer.createChildLeaf(".DS_Store");
		Assert.assertFalse(lockManager.canLock(dsStoreLeaf, null));
		VFSLeaf macLeaf = publicContainer.createChildLeaf("__MACOSX");
		Assert.assertFalse(lockManager.canLock(macLeaf, null));
		VFSLeaf hiddenLeaf = publicContainer.createChildLeaf(".somefile");
		Assert.assertFalse(lockManager.canLock(hiddenLeaf, null));
	}

	@Test
	public void canLockWebDAVHelpFiles() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-lock-14");
		VFSContainer homeFolder = VFSManager.olatRootContainer(FolderConfig.getUserHome(id), null);
		VFSContainer publicContainer = VFSManager.getOrCreateContainer(homeFolder, "public");
		
		// help files
		VFSLeaf wordLeaf = publicContainer.createChildLeaf(".~WRD0000");
		Assert.assertFalse(lockManager.canLock(wordLeaf, null));
		VFSLeaf macLockLeaf = publicContainer.createChildLeaf("._file");
		Assert.assertFalse(lockManager.canLock(macLockLeaf, null));
		
		// help files used by WebDAV
		VFSLeaf webDAVWordLeaf = publicContainer.createChildLeaf(".~WRD0001");
		Assert.assertTrue(lockManager.canLock(webDAVWordLeaf, VFSLockApplicationType.webdav));
		VFSLeaf webDAVMacLockLeaf = publicContainer.createChildLeaf("._leaf");
		Assert.assertTrue(lockManager.canLock(webDAVMacLockLeaf, VFSLockApplicationType.webdav));
	}
	
	/**
	 * A new file cannot be locked
	 */
	@Test
	public void lockVfs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-3");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-4");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// lock the file
		LockResult locked = lockManager.lock(file, id, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(locked.isAcquired());

		VFSMetadata metadata = metadataDao.getMetadata(relativePath, "lock.txt", false);
		boolean lockedAlt = lockManager.isLockedForMe(file, metadata, id, null, null);
		Assert.assertFalse(lockedAlt);
		
		// but other cannot
		boolean lockedOther = lockManager.isLockedForMe(file, otherId, null, null);
		Assert.assertTrue(lockedOther);
	}
	
	/**
	 * A new file cannot be locked
	 */
	@Test
	public void lockUnlockVfs() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-3");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-4");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// lock the file
		LockResult locked = lockManager.lock(file, id, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(locked.isAcquired());

		// but other cannot
		boolean lockedOther = lockManager.isLockedForMe(file, otherId, null, null);
		Assert.assertTrue(lockedOther);
		
		// first unlock it
		boolean unlocked = lockManager.unlock(file, VFSLockApplicationType.vfs);
		Assert.assertTrue(unlocked);
		
		// other can now
		boolean unlockedOther = lockManager.isLockedForMe(file, otherId, null, null);
		Assert.assertFalse(unlockedOther);
	}
	
	/**
	 * Check the locks for VFS at metadata level
	 */
	@Test
	public void lockUnlockVfs_metadata() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-3");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// First user lock via "GUI"
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setLocked(true);
		metadata.setLockedBy(id);
		metadata.setLockedDate(new Date());
		vfsRepositoryService.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		// re-lock the file
		LockResult locked = lockManager.lock(file, id, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(locked.isAcquired());

		// first unlock it
		boolean unlocked = lockManager.unlock(file, VFSLockApplicationType.vfs);
		Assert.assertTrue(unlocked);
		
		// check the metadata loose the lock informations
		VFSMetadata reloadMetadata = vfsRepositoryService.getMetadataFor(file);
		Assert.assertNull(reloadMetadata.getLockedBy());
		Assert.assertNull(reloadMetadata.getLockedDate());
	}
	
	/**
	 * A shared lock for collaboration
	 */
	@Test
	public void lockUnlockCollaboration() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-3");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-4");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// lock the file
		LockResult locked = lockManager.lock(file, id, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertTrue(locked.isAcquired());

		// but other can if the same app
		boolean lockedOther = lockManager.isLockedForMe(file, otherId, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertFalse(lockedOther);
		// but only same app
		boolean lockedOtherApp = lockManager.isLockedForMe(file, otherId, VFSLockApplicationType.collaboration, "notoo-collaboration");
		Assert.assertTrue(lockedOtherApp);
		
		// first unlock it
		boolean unlocked = lockManager.unlock(file, locked);
		Assert.assertTrue(unlocked);
		
		// other can now
		boolean unlockedOther = lockManager.isLockedForMe(file, otherId, VFSLockApplicationType.collaboration, "notoo-collaboration");
		Assert.assertFalse(unlockedOther);
	}
	
	/**
	 * A shared lock for collaboration with 3 users, 2 which coll
	 */
	@Test
	public void lockUnlockCollaboration_realCollaboration() {
		Identity collabId1 = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-col-3");
		Identity collabId2 = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-col-4");
		Identity webdavId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-dav-4");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// lock the file
		LockResult lockResult = lockManager.lock(file, collabId1, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertTrue(lockResult.isAcquired());
		// but other can if the same app
		boolean lockedOther = lockManager.isLockedForMe(file, collabId2, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertFalse(lockedOther);
		// do it
		LockResult lockOtherResult = lockManager.lock(file, collabId2, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertTrue(lockOtherResult.isAcquired());
		
		// others cannot
		boolean lockedWebdav = lockManager.isLockedForMe(file, webdavId, VFSLockApplicationType.webdav, null);
		Assert.assertTrue(lockedWebdav);
		boolean lockedVfs = lockManager.isLockedForMe(file, webdavId, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(lockedVfs);
		boolean lockedAlienCollab = lockManager.isLockedForMe(file, webdavId, VFSLockApplicationType.collaboration, "alien-collaboration");
		Assert.assertTrue(lockedAlienCollab);
		
		// first unlock the file
		boolean unlockResult = lockManager.unlock(file, lockResult);
		Assert.assertFalse(unlockResult);
		// still lock by 2
		boolean lockedRetryCollab = lockManager.isLockedForMe(file, webdavId, VFSLockApplicationType.collaboration, "alien-collaboration");
		Assert.assertTrue(lockedRetryCollab);
		
		// 2 unlock
		boolean unlock2Result = lockManager.unlock(file, lockOtherResult);
		Assert.assertTrue(unlock2Result);
		
		// it's free
		boolean lockFreeVfs = lockManager.isLockedForMe(file, webdavId, VFSLockApplicationType.vfs, null);
		Assert.assertFalse(lockFreeVfs);
	}
	
	/**
	 * A shared lock for collaboration
	 */
	@Test
	public void lockVfsAgainstCollaboration() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-5");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-6");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		
		// First user lock via "GUI"
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setLocked(true);
		metadata.setLockedBy(id);
		metadata.setLockedDate(new Date());
		vfsRepositoryService.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		// second user, try to lock the file
		LockResult locked = lockManager.lock(file, otherId, VFSLockApplicationType.vfs, null);
		Assert.assertFalse(locked.isAcquired());
		LockInfo lock = lockManager.getLock(file);
		Assert.assertNotNull(lock);
		// is locked
		boolean lockedCollaboration = lockManager.isLockedForMe(file, otherId, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertTrue(lockedCollaboration);
		// is locked
		boolean lockedVfs = lockManager.isLockedForMe(file, otherId, VFSLockApplicationType.vfs, null);
		Assert.assertTrue(lockedVfs);
	}

	/**
	 * A shared lock for collaboration with myself
	 */
	@Test
	public void lockVfsAgainstCollaborationAndMe() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-7");
		
		//create a file
		String relativePath = "lock" + UUID.randomUUID();
		VFSContainer rootTest = VFSManager.olatRootContainer("/" + relativePath, null);
		String filename = "lock.txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);

		// Locked for one app. 
		boolean lockedCollaboration = lockManager.isLockedForMe(file, id, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertFalse(lockedCollaboration);
		// Locked it
		LockResult locked = lockManager.lock(file, id, VFSLockApplicationType.collaboration, "oo-collaboration");
		Assert.assertTrue(locked.isAcquired());
		LockInfo lock = lockManager.getLock(file);
		Assert.assertNotNull(lock);
		
		// Try to lock it with an other app.
		boolean lockedOtherApp = lockManager.isLockedForMe(file, id, VFSLockApplicationType.collaboration, "other-collaboration");
		Assert.assertTrue(lockedOtherApp);
		// Try harder
		LockResult lockedOther = lockManager.lock(file, id, VFSLockApplicationType.collaboration, "other-collaboration");
		Assert.assertFalse(lockedOther.isAcquired());
	}
	
	
}
