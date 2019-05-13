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

package org.olat.upgrade.model;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.resource.OLATResource;

/**
 * Description: <br>
 * POJO designed class <br>
 * Implementation for the Interface BusinessGroup. <br>
 * Initial Date: Jul 27, 2004
 * 
 * @author patrick
 */

public class BusinessGroupUpgrade extends PersistentObject implements BusinessGroup {

	private static final long serialVersionUID = -6977108696910447781L;
	private static final Logger log = Tracing.createLoggerFor(BusinessGroupUpgrade.class);
	
	private String description;
	private String name;
	private String externalId;
	private String managedFlagsString;
	private Integer minParticipants;
	private Integer maxParticipants;
	private OLATResource resource;
	private SecurityGroup ownerGroup;
	private SecurityGroup partipiciantGroup;
	private SecurityGroup waitingGroup;
	private Group baseGroup;
	
	private Date lastUsage;
	private Long groupContextKey;
	private Boolean waitingListEnabled;
	private Boolean autoCloseRanksEnabled;
	private Date lastModified;
	private boolean ownersVisibleIntern;
	private boolean participantsVisibleIntern;
	private boolean waitingListVisibleIntern;
	private boolean ownersVisiblePublic;
	private boolean participantsVisiblePublic;
	private boolean waitingListVisiblePublic;
	private boolean downloadMembersLists;
	private boolean allowToLeave;

	/**
	 * constructs an unitialised BusinessGroup, use setXXX for setting attributes
	 */
	public BusinessGroupUpgrade() {
	// used by spring
	}

	/**
	 * @param partipiciantGroupP
	 */
	public void setPartipiciantGroup(SecurityGroup partipiciantGroupP) {
		this.partipiciantGroup = partipiciantGroupP;
	}

	/**
	 * @param ownerGroupP
	 */
	public void setOwnerGroup(SecurityGroup ownerGroupP) {
		this.ownerGroup = ownerGroupP;
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

	public boolean isAllowToLeave() {
		return allowToLeave;
	}

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
		baseGroup = group;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getOwnerGroup()
	 */
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getPartipiciantGroup()
	 */
	public SecurityGroup getPartipiciantGroup() {
		return partipiciantGroup;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getWaitingGroup()
	 */
	public SecurityGroup getWaitingGroup() {
		return waitingGroup;
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
	 * @see org.olat.group.BusinessGroup#getGroupContext()
	 */
	public Long getGroupContextKey() {
		return groupContextKey;
	}

	/**
	 * @see org.olat.group.BusinessGroup#setGroupContext(org.olat.group.context.BGContext)
	 */
	public void setGroupContextKey(Long groupContextKey) {
		this.groupContextKey = groupContextKey;
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

	/**
	 * @see org.olat.group.BusinessGroup#getMinParticipants()
	 */
	public Integer getMinParticipants() {
		return minParticipants;
	}

	/**
	 * @see org.olat.group.BusinessGroup#setMinParticipants(java.lang.Integer)
	 */
	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "name=" + name + "::" + super.toString();
	}

	public void setWaitingGroup(SecurityGroup waitingGroup) {
		this.waitingGroup = waitingGroup;
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
	public int hashCode() {
		return getKey() == null ? 2901 : getKey().hashCode();
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