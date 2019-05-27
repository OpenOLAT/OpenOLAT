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

/**
 * 
 * @author Felix Jost
 */
public class NamedGroupImpl extends PersistentObject implements NamedGroup {
	private String groupName;
	private SecurityGroup securityGroup;

	/**
	 * for hibernate only
	 */
	public NamedGroupImpl() {
		//  
	}

	public NamedGroupImpl(String groupName, SecurityGroup securityGroup) {
		this.groupName = groupName;
		this.securityGroup = securityGroup;
	}

	/**
	 * @return String
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @return SecurityGroup
	 */
	public SecurityGroup getSecurityGroup() {
		return securityGroup;
	}

	/**
	 * for hibernate only
	 * 
	 * @param groupName
	 */
	private void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * for hibernate only
	 * 
	 * @param securityGroup
	 */
	private void setSecurityGroup(SecurityGroup securityGroup) {
		this.securityGroup = securityGroup;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#toString()
	 */
	public String toString() {
		return "groupname:" + groupName + ", " + super.toString();
	}

}