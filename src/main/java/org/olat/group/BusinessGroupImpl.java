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

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.resource.OLATResource;

/**
 * Description: <br>
 * POJO designed class <br>
 * Implementation for the Interface BusinessGroup. <br>
 * Initial Date: Jul 27, 2004
 * 
 * @author patrick
 */

public class BusinessGroupImpl extends PersistentObject implements BusinessGroup {

	private static final long serialVersionUID = -6977108696910447781L;
	private static final OLog log = Tracing.createLoggerFor(BusinessGroupImpl.class);
	
	private String description;
	private String name;
	private String type;
	private String externalId;
	private String managedFlags;
	private Integer minParticipants;
	private Integer maxParticipants;
	private OLATResource resource;
	private SecurityGroup ownerGroup;
	private SecurityGroup partipiciantGroup;
	private SecurityGroup waitingGroup;
	private Date lastUsage;
	private Long groupContextKey;
	private Boolean waitingListEnabled;
	private Boolean autoCloseRanksEnabled;
	private Date lastModified;

	private static final int TYPE_MAXLENGTH = 15;

	/**
	 * constructs an unitialised BusinessGroup, use setXXX for setting attributes
	 */
	public BusinessGroupImpl() {
	// used by spring
	}

	/**
	 * convenience constructor
	 * 
	 * @param type
	 * @param groupName
	 * @param description
	 * @param ownerGroup
	 * @param partipiciantGroup
	 */
	public BusinessGroupImpl(String groupName, String description, SecurityGroup ownerGroup, SecurityGroup partipiciantGroup,
			SecurityGroup waitingGroup) {
		this.setName(groupName);
		this.setDescription(description);
		this.setOwnerGroup(ownerGroup);
		this.setPartipiciantGroup(partipiciantGroup);
		this.setWaitingGroup(waitingGroup);
		this.setType("LearningGroup");
		// per default no waiting-list
		Boolean disabled = new Boolean(false);
		this.setWaitingListEnabled(disabled);
		this.setAutoCloseRanksEnabled(disabled);
		this.setLastUsage(new Date());
		this.setLastModified(new Date());
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

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getName()
	 */
	public String getName() {
		return this.name;
	}

	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getOwnerGroup()
	 */
	public SecurityGroup getOwnerGroup() {
		return this.ownerGroup;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getPartipiciantGroup()
	 */
	public SecurityGroup getPartipiciantGroup() {
		return this.partipiciantGroup;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getWaitingGroup()
	 */
	public SecurityGroup getWaitingGroup() {
		return this.waitingGroup;
	}

	/**
	 * @return Returns the lastUsage.
	 */
	public java.util.Date getLastUsage() {
		return this.lastUsage;
	}

	/**
	 * set last usage
	 * 
	 * @param lastUsageP
	 */
	public void setLastUsage(final java.util.Date lastUsageP) {
		this.lastUsage = lastUsageP;
	}

	/**
	 * @see org.olat.group.BusinessGroup#getType()
	 */
	public String getType() {
		return this.type;// BusinessGroupImpl.class.getName();
	}

	/**
	 * @param type2
	 */
	private void setType(String type2) {
		if (type2 != null && type2.length() > TYPE_MAXLENGTH) throw new AssertException("businessgrouptype in o_bg_business too long.");
		this.type = type2;
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
		return this.groupContextKey;
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
		  log.audit("Max participants value changed for group " + this + " was " + oldMaxParticipants + " changed to " + maxParticipants);
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
		return "name=" + name + "::" + "type=" + type + "::" + super.toString();
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