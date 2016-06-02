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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.User;
import org.olat.core.logging.AssertException;
import org.olat.user.UserImpl;


/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
@Entity
@Table(name="o_bs_identity")
public class IdentityImpl implements Identity, IdentityRef, CreateInfo, Persistable, Serializable {

	private static final long serialVersionUID = 1762176135363569542L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastlogin", nullable=false, insertable=true, updatable=true)
	private Date lastLogin;
	
	@Column(name="name", nullable=true, insertable=true, updatable=false)
	private String name;
	@Column(name="external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	
	@OneToOne(targetEntity=UserImpl.class)
	@JoinColumn (name="fk_user_id")
	private User user;
	
	
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
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Identity[name=" + name + "], " + super.toString();
	}
}