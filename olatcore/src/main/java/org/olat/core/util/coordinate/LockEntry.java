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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 
package org.olat.core.util.coordinate;

import org.olat.core.id.Identity;
/**
 * Description: <br>
 * a helper class used by singleVM and cluster locker to represent a lock entry.
 * 
 * @author Felix Jost
 */
public class LockEntry {
	private long lockAquiredTime;
	private Identity owner;
	private final String key;

	/**
	 * @param key
	 * @param lockAquiredTime
	 * @param owner
	 */
	public LockEntry(String key, long lockAquiredTime, Identity owner) {
		this.key = key;
		this.lockAquiredTime = lockAquiredTime;
		this.owner = owner;
	}

	/**
	 * @return timestamp of acquisition time.
	 */
	public long getLockAquiredTime() {
		return lockAquiredTime;
	}

	/**
	 * @return owner of this lock
	 */
	public Identity getOwner() {
		return owner;
	}

	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.getKey().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LockEntry other = (LockEntry) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
	

}