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

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * Description: <br>
 * Relation between the security group and identity. The object is almost immutable
 * on the hibernate mapping.
 * 
 * 
 * @author Felix Jost
 */
public class SecurityGroupMembershipImpl extends PersistentObject implements ModifiedInfo {
	private static final long serialVersionUID = 2466302280763907357L;
	
	private Identity identity;
	private SecurityGroup securityGroup;
	private Date lastModified;

	/**
	 * package local
	 */
	protected SecurityGroupMembershipImpl() {
	//
	}

	/**
	 * @return Identity
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @return SecurityGroup
	 */
	public SecurityGroup getSecurityGroup() {
		return securityGroup;
	}

	/**
	 * Sets the identity. The identity cannot be changed and updated.
	 * The identity is only inserted to the database but never updated
	 * 
	 * @param identity The identity to set
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	/**
	 * Sets the securityGroup. The security group cannot be changed and
	 * updated. It is only inserted to the dabatase but never updated.
	 * 
	 * @param securityGroup The securityGroup to set
	 */
	public void setSecurityGroup(SecurityGroup securityGroup) {
		this.securityGroup = securityGroup;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#setLastModified(java.util.Date)
	 */
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

}