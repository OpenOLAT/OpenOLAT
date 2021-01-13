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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.User;
import org.olat.core.logging.AssertException;


/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class IdentityImpl implements Identity, IdentityRef, CreateInfo, Persistable, Serializable {

	private static final long serialVersionUID = 1762176135363569542L;

	private Long key;
	
	@SuppressWarnings("unused")
	private int version = 0;
	private Date creationDate;
	private Date lastLogin;
	private String name;
	private String externalId;
	private User user;
	/** status=[activ|deleted|permanent] */
	private int status;

	private Date inactivationDate;
	private Date inactivationEmailDate;
	private Date reactivationDate;
	private Date expirationDate;
	private Date expirationEmailDate;
	
	private Date deletedDate;
	private String deletedBy;
	private String deletedRoles;
	private Date deletionEmailDate;
	
	/**
	 * Maximum length of an identity's name.
	 */
	public static final int NAME_MAXLENGTH = 128;

	public IdentityImpl() {
		//  
	}

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
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
	
	/**
	 * Set new last login value
	 * 
	 * @param newLastLogin  The new last login date
	 */
	public void setLastLogin(Date newLastLogin) {
		this.lastLogin = newLastLogin;
	}

	@Override
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
	public void setName(String name) {
		if (name.length() > NAME_MAXLENGTH) {
			throw new AssertException("field name of table o_bs_identity too long");
		}
		this.name = name;
	}

	/**
	 * for hibernate only Sets the user.
	 * 
	 * @param user The user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Status can be [activ|deleted|permanent].
	 * @return Returns the status.
	 */
	@Override
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(Integer status) {
		this.status = status == null ? 0 : status.intValue();
	}

	public Date getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	public String getDeletedRoles() {
		return deletedRoles;
	}

	public void setDeletedRoles(String deletedRoles) {
		this.deletedRoles = deletedRoles;
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}

	public Date getDeletionEmailDate() {
		return deletionEmailDate;
	}

	public void setDeletionEmailDate(Date deletionEmailDate) {
		this.deletionEmailDate = deletionEmailDate;
	}

	@Override
	public Date getInactivationDate() {
		return inactivationDate;
	}

	public void setInactivationDate(Date inactivationDate) {
		this.inactivationDate = inactivationDate;
	}

	public Date getInactivationEmailDate() {
		return inactivationEmailDate;
	}

	public void setInactivationEmailDate(Date inactivationEmailDate) {
		this.inactivationEmailDate = inactivationEmailDate;
	}

	@Override
	public Date getReactivationDate() {
		return reactivationDate;
	}

	public void setReactivationDate(Date reactivationDate) {
		this.reactivationDate = reactivationDate;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public Date getExpirationEmailDate() {
		return expirationEmailDate;
	}

	public void setExpirationEmailDate(Date expirationEmailDate) {
		this.expirationEmailDate = expirationEmailDate;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash;
		hash = 31 * hash + (null == this.getName() ? 0 : this.getName().hashCode());
		return hash;
	}
	
	/**
	 * Compares the usernames.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof IdentityImpl) {
			IdentityImpl identity = (IdentityImpl)obj;
			return getName().equals(identity.getName());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		return "Identity[key=" + key + "], " + super.toString();
	}
}