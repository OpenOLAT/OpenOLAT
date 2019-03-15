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
package org.olat.core.util.vfs;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.lock.LockInfo;

/**
 * The manager which locks / unlokcs the files
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VFSLockManager {
	
	/**
	 * @param item
	 * @return True if the item has a VFS or WebDAv lock
	 */
	public boolean isLocked(VFSItem item);
	
	/**
	 * This method is used as an optimization of the isLocked() to prevent
	 * loading several times the same metadata in a list.
	 * 
	 * @param item The file to check
	 * @param loadedInfo The up-to-date meta info
	 * @return
	 */
	public boolean isLocked(VFSItem item, VFSMetadata loadedInfo);
	
	/**
	 * 
	 * @param item
	 * @param me
	 * @param roles
	 * @return true if there is a lock owned by someone else, or there is a WebDAV lock on the item
	 */
	public boolean isLockedForMe(VFSItem item, Identity me, Roles roles);
	
	/**
	 * 
	 * @param item
	 * @param loadedInfo
	 * @param me
	 * @param roles
	 * @return true if there is a lock owned by someone else, or there is a WebDAV lock on the item
	 */
	public boolean isLockedForMe(VFSItem item, VFSMetadata loadedInfo, Identity me, Roles roles);
	
	public LockInfo getLock(VFSItem item);
	
	public boolean lock(VFSItem item, Identity identity, Roles roles);
	
	/**
	 * 
	 * Unlock the VFS lock only. It doesn't change the WebdAV lock.
	 * 
	 * @param item
	 * @param identity
	 * @param roles
	 * @return True if and only if the VFS lock was unlocked and there isn't any WedDAV lock
	 */
	public boolean unlock(VFSItem item, Identity identity, Roles roles);
	
	/**
	 * Method the generate the Lock-Token
	 */
	public String generateLockToken(LockInfo lock, Identity identity);
}
