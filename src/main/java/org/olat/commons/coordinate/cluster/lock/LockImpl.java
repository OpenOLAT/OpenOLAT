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
*/

package org.olat.commons.coordinate.cluster.lock;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;

/**
 * 
 * Description:<br>
 * implementation of the lock (object used by hibernate)
 * 
 * <P>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class LockImpl extends PersistentObject {

	private static final long serialVersionUID = 1978265978735682673L;
	private Identity owner;
	private String asset;
	private String nodeId;

	/**
	* Constructor needed for Hibernate.
	*/
	LockImpl() {
		// singleton
	}

	LockImpl(String asset, Identity owner) {
		if (asset.length() > 120) {
			throw new AssertException("asset may not exceed 120 bytes in length: asset="+asset);
		}
		this.asset = asset;
		this.owner = owner;
	}

	@Override
	public String toString() {
		return "Lock[owner="+(owner==null ? "null" : owner.getKey())+",asset="+asset+",nodeId="+nodeId+"]";
	}

	public String getAsset() {
		return asset;
	}

	/**
	 * [for hibernate]
	 * @param asset
	 */
	void setAsset(String asset) {
		this.asset = asset;
	}

	public Identity getOwner() {
		return owner;
	}

	public void setOwner(Identity owner) {
		this.owner = owner;
	}

	String getNodeId() {
		return nodeId;
	}

	void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 39746 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LockImpl) {
			LockImpl lock = (LockImpl)obj;
			return getKey() != null && getKey().equals(lock.getKey());
		}
		return false;
	}
}
