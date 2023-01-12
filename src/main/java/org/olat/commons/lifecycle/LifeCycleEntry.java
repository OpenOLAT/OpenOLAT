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

package org.olat.commons.lifecycle;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.core.logging.AssertException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

/**
 * An entry in LifeCycle-table
 *
 * @author Christian Guretzki
 */
@Entity
@Table(name="o_lifecycle")
public class LifeCycleEntry implements Persistable, CreateInfo {

	private static final long serialVersionUID = -2919077675588017564L;
	
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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Version
	private int version = 0;

	@Column(name="persistenttypename", nullable=false, insertable=true, updatable=false)
	private String persistentTypeName;
	@Column(name="persistentref", nullable=false, insertable=true, updatable=false)
	private Long persistentRef;
	@Column(name="action", nullable=false, insertable=true, updatable=false)
	private String action;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lctimestamp", nullable=false, insertable=true, updatable=false)
	private Date   lcTimestamp;
	@Column(name="uservalue", nullable=true, insertable=true, updatable=false)
	private String userValue;
	

	protected static final int PERSISTENTTYPENAME_MAXLENGTH = 50;

	LifeCycleEntry() {
		//
	}

	LifeCycleEntry(Date lifeCycleDate, String persistentObjectTypeName, Long persistentObjectRef) { 
		setPersistentTypeName(persistentObjectTypeName);
		setPersistentRef(persistentObjectRef);
		if (lifeCycleDate == null) {
			lcTimestamp = new Date();
		} else {
			lcTimestamp = lifeCycleDate;
		}
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
	
	/**
	 * @return Returns the lcTimestamp.
	 */
	public Date getLcTimestamp() {
		return lcTimestamp;
	}

	/**
	 * @return Returns the action.
	 */
	protected String getAction() {
		return action;
	}


	/**
	 * @param action The action to set.
	 */
	protected void setAction(String action) {
		this.action = action;
	}


	/**
	 * @return Returns the resourceTypeId.
	 */
	public Long getPersistentRef() {
		return persistentRef;
	}

	/**
	 * @param resourceTypeId The resourceTypeId to set.
	 */
	protected void setPersistentRef(Long persistentRef) {
		this.persistentRef = persistentRef;
	}

	/**
	 * @return Returns the resourceTypeName.
	 */
	protected String getPersistentTypeName() {
		return persistentTypeName;
	}

	/**
	 * @param resourceTypeName The resourceTypeName to set.
	 */
	protected void setPersistentTypeName(String persistentTypeName) {
		if (persistentTypeName.length() > PERSISTENTTYPENAME_MAXLENGTH) {
			throw new AssertException("persistentTypeName in o_lifecycle too long. persistentObjectTypeName=" + persistentTypeName );
		}
		this.persistentTypeName = persistentTypeName;
	}

	/**
	 * @return Returns the userValue.
	 */
	public String getUserValue() {
		return userValue;
	}

	/**
	 * @param userValue The userValue to set.
	 */
	protected void setUserValue(String userValue) {
		this.userValue = userValue;
	}

	/**
	 * @param lcTimestamp The lcTimestamp to set.
	 */
	protected void setLcTimestamp(Date lcTimestamp) {
		this.lcTimestamp = lcTimestamp;
	}

	@Override
	public String toString() {
		return persistentTypeName + ":" + persistentRef + ", action=" + action + ", timestamp=" + this.lcTimestamp;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 20818 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LifeCycleEntry entry) {
			return getKey() != null && getKey().equals(entry.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
