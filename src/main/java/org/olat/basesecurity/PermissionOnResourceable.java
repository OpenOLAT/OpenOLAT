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

import org.olat.core.id.OLATResourceable;

/**
 * Initial Date:  Feb 2, 2006
 * @author gnaegi<br>
 *
 * Description:
 * This is a data container that holds a permission and an OLAT resource.
 * It can be used when searching for policies that fullfill this criteria
 * or when using for users using the powersearch.
 */
public class PermissionOnResourceable {
	private OLATResourceable olatResourceable;
	private String permission;
	
	public PermissionOnResourceable(String permission, OLATResourceable olatResourceable) {
		this.olatResourceable = olatResourceable;
		this.permission = permission;
	}

	/**
	 * @return OLATResourceable
	 */
	public OLATResourceable getOlatResourceable() {
		return olatResourceable;
	}

	/**
	 * @return String
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#toString()
	 */
	public String toString() {
		return "perm:" + permission + ", oresource: " + olatResourceable.getResourceableTypeName()
				+ ":" + olatResourceable.getResourceableId() + super.toString();
	}

}