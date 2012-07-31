/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.group.model;

import java.util.Date;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupView;
import org.olat.resource.OLATResource;


public class BusinessGroupViewImpl extends PersistentObject implements BusinessGroupView {

	private static final long serialVersionUID = -9042740930754224954L;
	
	private Long identityKey;
	private String description;
	private String name;
	private Integer minParticipants;
	private Integer maxParticipants;
	private OLATResource resource;
	private SecurityGroup ownerGroup;
	private SecurityGroup partipiciantGroup;
	private SecurityGroup waitingGroup;
	private Date lastUsage;
	private Boolean waitingListEnabled;
	private Boolean autoCloseRanksEnabled;
	private Date lastModified;
	
	private int numOfParticipants;
	private int numOfRelations;
	private int numOfOffers;
	private int numOfValidOffers;

	@Override
	public int getNumOfRelations() {
		return numOfRelations;
	}

	public void setNumOfRelations(int numOfRelations) {
		this.numOfRelations = numOfRelations;
	}

	@Override
	public int getNumOfParticipants() {
		return numOfParticipants;
	}

	public void setNumOfParticipants(int numOfParticipants) {
		this.numOfParticipants = numOfParticipants;
	}


	@Override
	public int getNumOfOffers() {
		return numOfOffers;
	}
	
	public void setNumOfOffers(int numOfOffers) {
		this.numOfOffers = numOfOffers;
	}

	@Override
	public int getNumOfValidOffers() {
		return numOfValidOffers;
	}

	public void setNumOfValidOffers(int numOfValidOffers) {
		this.numOfValidOffers = numOfValidOffers;
	}


	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public Date getLastUsage() {
		return lastUsage;
	}

	public void setLastUsage(Date lastUsage) {
		this.lastUsage = lastUsage;
	}

	@Override
	public OLATResource getResource() {
		return resource;
	}
	

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	@Override
	public SecurityGroup getPartipiciantGroup() {
		return partipiciantGroup;
	}

	public void setPartipiciantGroup(SecurityGroup partipiciantGroup) {
		this.partipiciantGroup = partipiciantGroup;
	}

	@Override
	public SecurityGroup getWaitingGroup() {
		return waitingGroup;
	}
	
	public void setWaitingGroup(SecurityGroup waitingGroup) {
		this.waitingGroup = waitingGroup;
	}

	@Override
	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	@Override
	public Integer getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}

	@Override
	public Boolean getAutoCloseRanksEnabled() {
		return autoCloseRanksEnabled;
	}

	public void setAutoCloseRanksEnabled(Boolean autoCloseRanksEnabled) {
		this.autoCloseRanksEnabled = autoCloseRanksEnabled;
	}

	@Override
	public Boolean getWaitingListEnabled() {
		return waitingListEnabled;
	}

	public void setWaitingListEnabled(Boolean waitingListEnabled) {
		this.waitingListEnabled = waitingListEnabled;
	}

	@Override
	public int hashCode() {
		return (getKey() == null ? 2634 : getKey().hashCode())
			+ (identityKey == null ? -24 :identityKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BusinessGroupViewImpl) {
			BusinessGroupViewImpl centric = (BusinessGroupViewImpl)obj;
			return getKey() != null && getKey().equals(centric.getKey())
					&& identityKey != null && identityKey.equals(centric.getKey());
		}
		return false;
	}
}