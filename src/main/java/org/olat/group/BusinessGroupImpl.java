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

package org.olat.group;

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
import javax.persistence.Version;

import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * Description: <br>
 * POJO designed class <br>
 * Implementation for the Interface BusinessGroup. <br>
 * Initial Date: Jul 27, 2004
 * 
 * @author patrick
 */
@Entity(name="businessgroup")
@Table(name="o_gp_business")
public class BusinessGroupImpl implements Persistable, ModifiedInfo, BusinessGroup {

	private static final long serialVersionUID = -6977108696910447781L;
	private static final Logger log = Tracing.createLoggerFor(BusinessGroupImpl.class);
	
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
	@Version
	private int version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="descr", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="groupname", nullable=true, insertable=true, updatable=true)
	private String name;
	
	@Column(name="external_id", nullable=true, insertable=true, updatable=true)
	private String externalId;
	@Column(name="managed_flags", nullable=true, insertable=true, updatable=true)
	private String managedFlagsString;
	
	@Column(name="minparticipants", nullable=true, insertable=true, updatable=true)
	private Integer minParticipants;
	@Column(name="maxparticipants", nullable=true, insertable=true, updatable=true)
	private Integer maxParticipants;
	@Column(name="waitinglist_enabled", nullable=true, insertable=true, updatable=true)
	private Boolean waitingListEnabled;
	@Column(name="autocloseranks_enabled", nullable=true, insertable=true, updatable=true)
	private Boolean autoCloseRanksEnabled;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastusage", nullable=true, insertable=true, updatable=true)
	private Date lastUsage;

	@Column(name="ownersintern", nullable=true, insertable=true, updatable=true)
	private boolean ownersVisibleIntern;
	@Column(name="participantsintern", nullable=true, insertable=true, updatable=true)
	private boolean participantsVisibleIntern;
	@Column(name="waitingintern", nullable=true, insertable=true, updatable=true)
	private boolean waitingListVisibleIntern;
	@Column(name="ownerspublic", nullable=true, insertable=true, updatable=true)
	private boolean ownersVisiblePublic;
	@Column(name="participantspublic", nullable=true, insertable=true, updatable=true)
	private boolean participantsVisiblePublic;
	@Column(name="waitingpublic", nullable=true, insertable=true, updatable=true)
	private boolean waitingListVisiblePublic;

	@Column(name="downloadmembers", nullable=true, insertable=true, updatable=true)
	private boolean downloadMembersLists;
	@Column(name="allowtoleave", nullable=true, insertable=true, updatable=true)
	private boolean allowToLeave;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_resource", nullable=true, insertable=true, updatable=true)
	private OLATResource resource;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;

	/**
	 * constructs an unitialised BusinessGroup, use setXXX for setting attributes
	 */
	public BusinessGroupImpl() {
	// used by spring
	}

	/**
	 * convenience constructor
	 * 
	 * @param groupName
	 * @param description
	 */
	public BusinessGroupImpl(String groupName, String description) {
		setName(groupName);
		setDescription(description);
		// per default no waiting-list
		Boolean disabled = new Boolean(false);
		setWaitingListEnabled(disabled);
		setAutoCloseRanksEnabled(disabled);
		setLastUsage(new Date());
		setLastModified(new Date());
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}


	/**
	 * @param groupName
	 */
	public void setName(String groupName) {
		this.name = groupName;

	}

	/**
	 * @see org.olat.group.BusinessGroup#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @see org.olat.group.BusinessGroup#setDescription(java.lang.String)
	 */
	public void setDescription(final String descriptionP) {
		this.description = descriptionP;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		if(StringHelper.containsNonWhitespace(managedFlagsString)) {
			return BusinessGroupManagedFlag.toEnum(managedFlagsString);
		}
		return new BusinessGroupManagedFlag[0];
	}

	@Override
	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	@Override
	public void setManagedFlagsString(String managedFlags) {
		this.managedFlagsString = managedFlags;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getName()
	 */
	public String getName() {
		return name;
	}

	public boolean isOwnersVisibleIntern() {
		return ownersVisibleIntern;
	}

	public void setOwnersVisibleIntern(boolean visible) {
		this.ownersVisibleIntern = visible;
	}

	public boolean isParticipantsVisibleIntern() {
		return participantsVisibleIntern;
	}

	public void setParticipantsVisibleIntern(boolean visible) {
		this.participantsVisibleIntern = visible;
	}

	public boolean isWaitingListVisibleIntern() {
		return waitingListVisibleIntern;
	}

	public void setWaitingListVisibleIntern(boolean visible) {
		this.waitingListVisibleIntern = visible;
	}

	public boolean isOwnersVisiblePublic() {
		return ownersVisiblePublic;
	}

	public void setOwnersVisiblePublic(boolean visible) {
		this.ownersVisiblePublic = visible;
	}

	public boolean isParticipantsVisiblePublic() {
		return participantsVisiblePublic;
	}

	public void setParticipantsVisiblePublic(boolean visible) {
		this.participantsVisiblePublic = visible;
	}

	public boolean isWaitingListVisiblePublic() {
		return waitingListVisiblePublic;
	}

	public void setWaitingListVisiblePublic(boolean visible) {
		this.waitingListVisiblePublic = visible;
	}

	public boolean isDownloadMembersLists() {
		return downloadMembersLists;
	}

	public void setDownloadMembersLists(boolean visible) {
		this.downloadMembersLists = visible;
	}

	@Override
	public boolean isAllowToLeave() {
		return allowToLeave;
	}

	@Override
	public void setAllowToLeave(boolean allow) {
		this.allowToLeave = allow;
	}

	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group group) {
		this.baseGroup = group;
	}

	/**
	 * @return Returns the lastUsage.
	 */
	public Date getLastUsage() {
		return lastUsage;
	}

	/**
	 * set last usage
	 * 
	 * @param lastUsageP
	 */
	public void setLastUsage(Date lastUsage) {
		this.lastUsage = lastUsage;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getDisplayableType(java.util.Locale)
	 */

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		return getKey();
	}

	/**
	 * @see org.olat.group.BusinessGroup#getMaxParticipants()
	 */
	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	/**
	 * @see org.olat.group.BusinessGroup#setMaxParticipants(java.lang.Integer)
	 */
	public void setMaxParticipants(Integer maxParticipants) {
		boolean maxParticipantsChanged = getMaxParticipants()!=null && !getMaxParticipants().equals(maxParticipants);
		int oldMaxParticipants = getMaxParticipants()!=null ? getMaxParticipants() : 0;
		this.maxParticipants = maxParticipants;
		if(maxParticipantsChanged) {
		  log.info(Tracing.M_AUDIT, "Max participants value changed for group " + this + " was " + oldMaxParticipants + " changed to " + maxParticipants);
		}
	}

	public Integer getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}


	public Boolean getAutoCloseRanksEnabled() {
		return autoCloseRanksEnabled;
	}

	public void setAutoCloseRanksEnabled(Boolean autoCloseRanksEnabled) {
		this.autoCloseRanksEnabled = autoCloseRanksEnabled;
	}

	public Boolean getWaitingListEnabled() {
		return waitingListEnabled;
	}

	public void setWaitingListEnabled(Boolean waitingListEnabled) {
		this.waitingListEnabled = waitingListEnabled;
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("businessGroup[key=").append(getKey() == null ? "" : getKey())
		  .append(";name=").append(getName() == null ? "" : getName()).append("]")
		  .append(super.toString());
		return sb.toString();
	}
}