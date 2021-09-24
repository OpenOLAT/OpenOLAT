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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.group.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 09.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="businessgrouptosearch")
@Table(name="o_gp_business")
public class BusinessGroupToSearch implements Persistable {

	private static final long serialVersionUID = 1483583378938116438L;

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
	@Column(name="group_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="descr", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="groupname", nullable=true, insertable=true, updatable=true)
	private String name;
	
	@Column(name="status", nullable=false, insertable=true, updatable=true)
	private String status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="inactivationdate", nullable=true, insertable=true, updatable=true)
	private Date inactivationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="inactivationemaildate", nullable=true, insertable=true, updatable=true)
	private Date inactivationEmailDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="reactivationdate", nullable=true, insertable=true, updatable=true)
	private Date reactivationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="softdeleteemaildate", nullable=true, insertable=true, updatable=true)
	private Date softDeleteEmailDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="softdeletedate", nullable=true, insertable=true, updatable=true)
	private Date softDeleteDate;
	
	@Column(name="technical_type", nullable=true, insertable=true, updatable=false)
	private String technicalType;
	
	@Column(name="external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@Column(name="maxparticipants", nullable=true, insertable=true, updatable=true)
	private Integer maxParticipants;
	@Column(name="waitinglist_enabled", nullable=true, insertable=true, updatable=true)
	private Boolean waitingListEnabled;
	@Column(name="autocloseranks_enabled", nullable=true, insertable=true, updatable=true)
	private Boolean autoCloseRanksEnabled;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastusage", nullable=true, insertable=true, updatable=true)
	private Date lastUsage;

	@Column(name="allowtoleave", nullable=true, insertable=true, updatable=true)
	private boolean allowToLeave;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_resource", nullable=true, insertable=true, updatable=true)
	private OLATResource resource;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=false, updatable=false)
	private Group baseGroup;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public String getName() {
		return name;
	}
	
	public String getStatus() {
		return status;
	}
	
	public BusinessGroupStatusEnum getGroupStatus() {
		return BusinessGroupStatusEnum.valueOf(getStatus());
	}

	public String getTechnicalType() {
		return technicalType;
	}

	public String getDescription() {
		return description;
	}

	public String getExternalId() {
		return externalId;
	}

	public BusinessGroupManagedFlag[] getManagedFlags() {
		if(StringHelper.containsNonWhitespace(managedFlagsString)) {
			return BusinessGroupManagedFlag.toEnum(managedFlagsString);
		}
		return new BusinessGroupManagedFlag[0];
	}

	public boolean isAllowToLeave() {
		return allowToLeave;
	}

	public OLATResource getResource() {
		return resource;
	}

	public Group getBaseGroup() {
		return baseGroup;
	}

	public Date getLastUsage() {
		return lastUsage;
	}

	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public Boolean getAutoCloseRanksEnabled() {
		return autoCloseRanksEnabled;
	}

	public Boolean getWaitingListEnabled() {
		return waitingListEnabled;
	}
	
	/**
	 * Compares the keys.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof BusinessGroup) {
			BusinessGroup bg = (BusinessGroup)obj;
			return getKey() != null && getKey().equals(bg.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2901 : getKey().hashCode();
	}
}