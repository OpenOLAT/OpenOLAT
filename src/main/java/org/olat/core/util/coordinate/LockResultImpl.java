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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.util.coordinate;

import org.olat.core.id.Identity;

/**
 * Description:<br>
 * implementation of the lockresult, does this by wrapping a lockentry
 * 
 * <P>
 * Initial Date:  19.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class LockResultImpl implements LockResult {
	private LockEntry lockEntry;
	private final boolean success;
	
	public LockResultImpl(boolean success, LockEntry lockEntry) {
		this.success = success;
		this.lockEntry = lockEntry;
	}
	
	public long getLockAquiredTime() {
		return lockEntry.getLockAquiredTime();
	}

	public Identity getOwner() {
		return lockEntry.getOwner();
	}

	public boolean isSuccess() {
		return success;
	}
	
	public LockEntry getLockEntry() {
		return lockEntry;
	}

}
