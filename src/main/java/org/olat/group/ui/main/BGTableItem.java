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
package org.olat.group.ui.main;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.model.BusinessGroupRow;
import org.olat.repository.RepositoryEntryShort;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;

/**
 * 
 * Description:<br>
 * A wrapper class for the list of business group
 * 
 * <P>
 * Initial Date:  7 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGTableItem extends BusinessGroupRow {
	private BusinessGroupMembership member;
	private FormLink markLink;
	private FormLink allResourcesLink;
	private FormLink accessLink;
	
	private final Boolean allowLeave;
	private final Boolean allowDelete;
	
	private long numOfParticipants;
	private long numOfPendings;
	private long numOfOwners;
	private long numWaiting;
	
	public BGTableItem(BusinessGroupRow businessGroup, FormLink markLink, Boolean allowLeave, Boolean allowDelete) {
		super(businessGroup);
		this.markLink = markLink;
		if(markLink != null) {
			markLink.setUserObject(this);
		}
		this.member = businessGroup.getMember();
		setResources(businessGroup.getResources());
		setBundles(businessGroup.getBundles());
		this.allowLeave = allowLeave;
		this.allowDelete = allowDelete;
	}

	public Long getBusinessGroupKey() {
		return getKey();
	}
	
	public String getBusinessGroupExternalId() {
		return getExternalId();
	}
	
	public String getBusinessGroupName() {
		return getName();
	}
	
	public long getNumOfParticipants() {
		return numOfParticipants;
	}
	
	public long getNumOfPendings() {
		return numOfPendings;
	}
	
	public long getNumOfOwners() {
		return numOfOwners;
	}
	
	public long getNumWaiting() {
		return numWaiting;
	}

	public void setNumWaiting(long numWaiting) {
		this.numWaiting = numWaiting;
	}

	public void setNumOfParticipants(long numOfParticipants) {
		this.numOfParticipants = numOfParticipants;
	}

	public void setNumOfPendings(long numOfPendings) {
		this.numOfPendings = numOfPendings;
	}

	public void setNumOfOwners(long numOfOwners) {
		this.numOfOwners = numOfOwners;
	}

	public boolean isFull() {
		Integer maxParticipants = getMaxParticipants();
		if(maxParticipants == null || maxParticipants.intValue() < 0) {
			return false;
		}
		if(maxParticipants.intValue() == 0) {
			return true;
		}
		if(maxParticipants.intValue() <= (getNumOfPendings() + getNumOfParticipants())) {
			return true;
		}
		return false;
	}

	public String getBusinessGroupDescription() {
		return getDescription();
	}

	public Date getBusinessGroupLastUsage() {
		return getLastUsage();
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public FormLink getAllResourcesLink() {
		return allResourcesLink;
	}

	public void setAllResourcesLink(FormLink allResourcesLink) {
		this.allResourcesLink = allResourcesLink;
	}
	
	public List<RepositoryEntryShort> getRelations() {
		return super.getResources();
	}

	public FormLink getAccessLink() {
		return accessLink;
	}

	public void setAccessLink(FormLink accessLink) {
		this.accessLink = accessLink;
	}

	public BusinessGroupMembership getMembership() {
		return member;
	}
	
	public boolean isAccessControl() {
		return getBundles() != null && !getBundles().isEmpty();
	}

	public List<PriceMethodBundle> getAccessTypes() {
		return getBundles();
	}

	public Boolean getAllowLeave() {
		return allowLeave;
	}

	public Boolean getAllowDelete() {
		return allowDelete;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGTableItem) {
			BGTableItem item = (BGTableItem)obj;
			return getKey() != null && getKey().equals(item.getKey());
		}
		return false;
	}
}