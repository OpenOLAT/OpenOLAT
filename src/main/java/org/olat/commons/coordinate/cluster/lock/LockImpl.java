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

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

/**
 * 
 * Description:<br>
 * implementation of the lock (object used by hibernate)
 * 
 * <P>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
@Entity
@Table(name="oc_lock")
public class LockImpl implements Persistable, CreateInfo {

	private static final long serialVersionUID = 1978265978735682673L;
	
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
	@Column(name="lock_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="asset", nullable=false, insertable=true, updatable=false)
	private String asset;
	@Transient
	private String nodeId;
	@Column(name="windowid", nullable=true, insertable=true, updatable=false)
	private String windowId;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.EAGER,optional=false)
	@JoinColumn(name="identity_fk", nullable=false, insertable=true, updatable=false)
	private Identity owner;

	/**
	* Constructor needed for Hibernate.
	*/
	LockImpl() {
		// singleton
	}

	LockImpl(String asset, Identity owner, String windowId) {
		if (asset.length() > 120) {
			throw new AssertException("asset may not exceed 120 bytes in length: asset="+asset);
		}
		this.asset = asset;
		this.owner = owner;
		this.windowId = windowId;
		setCreationDate(new Date());
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

	public String getWindowId() {
		return windowId;
	}

	public void setWindowId(String windowId) {
		this.windowId = windowId;
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
		if(obj instanceof LockImpl lock) {
			return getKey() != null && getKey().equals(lock.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
