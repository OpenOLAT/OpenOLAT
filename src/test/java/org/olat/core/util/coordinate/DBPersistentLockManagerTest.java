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
package org.olat.core.util.coordinate;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DBPersistentLockManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private DBPersistentLockManager lockManager;
	
	@Test
	public void aquirePersistentLock() {
		String type = UUID.randomUUID().toString();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(type, 25l);
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-1");
		String locksubkey = UUID.randomUUID().toString();
		
		// acquire lock
		LockResult lock = lockManager.aquirePersistentLock(ores, owner, locksubkey);
		dbInstance.commit();
		//check
		Assert.assertNotNull(lock);
		Assert.assertEquals(owner, lock.getOwner());
		Assert.assertTrue(lock.isSuccess());
	}
	
	@Test
	public void releasePersistentLock() {
		String type = UUID.randomUUID().toString();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(type, 25l);
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-1");
		String locksubkey = UUID.randomUUID().toString();
		
		// acquire lock
		LockResult lock = lockManager.aquirePersistentLock(ores, owner, locksubkey);
		dbInstance.commit();
		Assert.assertTrue(lock.isSuccess());
		
		//release
		lockManager.releasePersistentLock(lock);
		dbInstance.commit();
	}
	
	@Test
	public void deleteUserData() {
		String type = UUID.randomUUID().toString();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(type, 25l);
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-1");
		Identity nextIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("lock-2");
		String locksubkey = UUID.randomUUID().toString();
		
		// acquire lock
		LockResult lock = lockManager.aquirePersistentLock(ores, owner, locksubkey);
		dbInstance.commit();
		Assert.assertTrue(lock.isSuccess());
		
		//delete the owner
		lockManager.deleteUserData(owner, "");
		dbInstance.commit();
		
		//next can acquire the lock
		LockResult nextLock = lockManager.aquirePersistentLock(ores, nextIdentity, locksubkey);
		dbInstance.commit();
		Assert.assertTrue(nextLock.isSuccess());
	}

}
