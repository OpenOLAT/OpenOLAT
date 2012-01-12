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

package org.olat.basesecurity;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.logging.AssertException;

/**
 * The object is immutable
 * @author Felix Jost
 */
public class SecurityGroupImpl extends PersistentObject implements SecurityGroup {

	/**
	 * package local
	 */
	protected SecurityGroupImpl() {
	// defined for hibernate
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return "SecGroup";
	}
	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		Long key = getKey();
		if (key == null) throw new AssertException("not persisted yet");
		return key;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 29851 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof SecurityGroup) {
			SecurityGroup sec = (SecurityGroup)obj;
			return getKey() != null && getKey().equals(sec.getKey());
		}
		return false;
	}
}