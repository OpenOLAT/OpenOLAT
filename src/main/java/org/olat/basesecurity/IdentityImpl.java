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

import java.io.Serializable;
import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.AssertException;


/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class IdentityImpl extends PersistentObject implements Identity, IdentityRef, Serializable {

	private static final long serialVersionUID = 1762176135363569542L;
	private String name;
	private User user;
	private Date lastLogin;
	private String externalId;
	/** status=[activ|deleted|permanent] */
	private int status;
	
	/**
	 * Maximum length of an identity's name.
	 */
	public static final int NAME_MAXLENGTH = 128;

	/**
	 * both args are mandatory (in junit test you may omit the user)
	 */
	protected IdentityImpl() {
	//  
	}

	IdentityImpl(String name, User user) {
		this.name = name;
		this.user = user;
		status = Identity.STATUS_ACTIV;
		this.setLastLogin(new Date());
	}

	/**
	 * @return String
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return User
	 */
	@Override
	public User getUser() {
		return user;
	}

	/**
	 * @return lastLogin
	 */
	@Override
	public Date getLastLogin() {
		return lastLogin;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	/**
	 * for hibernate only Sets the name.
	 * 
	 * @param name The name to set
	 */
	@Override
	public void setName(String name) {
		if (name.length() > NAME_MAXLENGTH)
			throw new AssertException("field name of table o_bs_identity too long");
		this.name = name;
	}

	/**
	 * for hibernate only Sets the user.
	 * 
	 * @param user The user to set
	 */
	@SuppressWarnings("unused")
	private void setUser(User user) {
		this.user = user;
	}

	/**
	 * for hibernate only.
	 * 
	 * @param newLastLogin  The new last login date
	 */
	private void setLastLogin(Date newLastLogin) {
		this.lastLogin = newLastLogin;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Identity[name=" + name + "], " + super.toString();
	}

	/**
	 * Status can be [activ|deleted|permanent].
	 * @return Returns the status.
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	/**
	 * Compares the usernames.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		// object must be IdentityImpl at this point
		IdentityImpl identity = (IdentityImpl)obj;
		return this.getName().equals(identity.getName());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash;
		hash = 31 * hash + (null == this.getName() ? 0 : this.getName().hashCode());
		return hash;
	}
}