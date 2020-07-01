/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.util.coordinate;

import java.util.List;

import org.olat.core.gui.components.Window;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * Interface to acquire Locks 
 * (short time locks for gui locking (e.g. i am currently administrating this group) 
 * and 
 * long term locks for resource locking (e.g. i am editing a qti test)
 * 
 * <P>
 * Initial Date:  19.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface Locker {
	
	/**
	 * @param ores the OLATResourceable to lock upon, e.g a repositoryentry or such
	 * @param identity the identity who tries to acquire the lock, not null
	 * @param locksubkey null or any string to lock finer upon the resource (e.g. "authors", or "write", ...)  
	 * @return lock result
	 */
	public LockResult acquireLock(OLATResourceable ores, Identity identity, String locksubkey, Window window);
	
	/**
	 * releases the lock. can also be called if the lock was not successfully
	 * acquired
	 * 
	 * @param le the LockResult received when locking
	 */
	public void releaseLock(LockResult le);
	
	/**
	 * @param ores
	 * @param locksubkey
	 * @return if the olatresourceable with the subkey is already locked by
	 *         someone (returns true even if locked by "myself")
	 */
	public boolean isLocked(OLATResourceable ores, String locksubkey);
	
	/**
	 * 
	 * @param ores
	 * @param locksubkey
	 * @return The identity which lock the resource or null.
	 */
	public Identity getLockedBy(OLATResourceable ores, String locksubkey);
		
	
	/**
	 * 
	 * acquires a persistent lock.
	 * 
	 * @param ores
	 * @param ident
	 * @param locksubkey may not be longer than 30 chars
	 * @return the LockResult of this lock trial.
	 */
	//public LockResult aquirePersistentLock(OLATResourceable ores, Identity ident, String locksubkey);
	
	
	/**
	 * releases a persistent lock.
	 * 
	 * @param le the LockResult which stems from the lock acquired previously
	 */
	//public void releasePersistentLock(LockResult le);

	/**
	 * for admin purposes only.
	 * @return a list of lockentries.
	 */
	public List<LockEntry> adminOnlyGetLockEntries();

	/**
	 * for admin purposes only. Release a lockentry.
	 * @param lock  release this lockentry 
	 */
	public void releaseLockEntry(LockEntry lock);
	
	
}
