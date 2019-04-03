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
package org.olat.core.util.vfs.lock;

import org.olat.core.util.vfs.VFSLockApplicationType;

/**
 * 
 * Initial date: 26 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LockResult {
	
	public static final LockResult LOCK_FAILED = new LockResult(false, null, null, null);
	
	private final boolean acquired;
	private final LockInfo lockInfo;
	private final String token;
	private final VFSLockApplicationType type;
	
	public LockResult(boolean acquired, LockInfo lockInfo, VFSLockApplicationType type, String token) {
		this.acquired = acquired;
		this.lockInfo = lockInfo;
		this.token = token;
		this.type = type;
	}

	public boolean isAcquired() {
		return acquired;
	}

	public LockInfo getLockInfo() {
		return lockInfo;
	}
	
	public String getToken() {
		return token;
	}
	
	public VFSLockApplicationType getType() {
		return type;
	}
}
