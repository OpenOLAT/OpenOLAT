/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.model;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;

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
import jakarta.persistence.Version;

/**
 * 
 * @author srosse, stephae.rosse@frentix.com, http://www.frentix.com
 */
@Entity
@Table(name="o_om_room_reference")
public class OpenMeetingsRoomReference implements Persistable, CreateInfo, ModifiedInfo {

	private static final long serialVersionUID = -1556626893809537080L;

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
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@ManyToOne(targetEntity=BusinessGroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="businessgroup", nullable=true, insertable=true, updatable=false)
	private BusinessGroup group;

	@Column(name="resourcetypename", nullable=true, insertable=true, updatable=true)
	private String resourceTypeName;
	@Column(name="resourcetypeid", nullable=true, insertable=true, updatable=true)
	private Long resourceTypeId;
	@Column(name="ressubpath", nullable=true, insertable=true, updatable=true)
	private String subIdentifier;

	@Column(name="roomid", nullable=true, insertable=true, updatable=true)
	private Long roomId;
	@Column(name="config", nullable=true, insertable=true, updatable=true)
	private String config;
	
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public String getResourceTypeName() {
		return resourceTypeName;
	}
	
	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}
	
	public Long getResourceTypeId() {
		return resourceTypeId;
	}
	
	public void setResourceTypeId(Long resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}
	
	public String getSubIdentifier() {
		return subIdentifier;
	}
	
	public void setSubIdentifier(String subIdentifier) {
		this.subIdentifier = subIdentifier;
	}
	
	public long getRoomId() {
		return roomId;
	}
	
	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
	
	public String getConfig() {
		return config;
	}
	
	public void setConfig(String config) {
		this.config = config;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 3945 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OpenMeetingsRoomReference ref) {
			return getKey() != null && getKey().equals(ref.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}