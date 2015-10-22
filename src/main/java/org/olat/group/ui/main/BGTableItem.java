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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupView;
import org.olat.group.model.BGRepositoryEntryRelation;
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
public class BGTableItem {
	private final BusinessGroupMembership member;
	private boolean marked;
	private final FormLink markLink;
	private FormLink allResourcesLink;
	private FormLink accessLink;
	
	private final Boolean allowLeave;
	private final Boolean allowDelete;
	private final String businessGroupDescription;
	private final Date businessGroupLastUsage;
	
	private final BGShort businessGroup;
	private List<RepositoryEntryShort> relations;
	private List<PriceMethodBundle> access;
	
	public BGTableItem(BusinessGroup businessGroup, boolean marked, BusinessGroupMembership member, Boolean allowLeave, Boolean allowDelete, List<PriceMethodBundle> access) {
		this.businessGroup = new BGShort(businessGroup);
		this.businessGroupDescription = businessGroup.getDescription();
		this.businessGroupLastUsage = businessGroup.getLastUsage();
		this.marked = marked;
		this.markLink = null;
		this.member = member;
		this.allowLeave = allowLeave;
		this.allowDelete = allowDelete;
		this.access = access;
	}
	
	public BGTableItem(BusinessGroupView businessGroup, boolean marked, BusinessGroupMembership member, Boolean allowLeave, Boolean allowDelete, List<PriceMethodBundle> access) {
		this.businessGroup = new BGShort(businessGroup);
		this.businessGroupDescription = businessGroup.getDescription();
		this.businessGroupLastUsage = businessGroup.getLastUsage();
		this.marked = marked;
		this.markLink = null;
		this.member = member;
		this.allowLeave = allowLeave;
		this.allowDelete = allowDelete;
		this.access = access;
	}
	
	public BGTableItem(BusinessGroupView businessGroup, FormLink markLink, boolean marked, BusinessGroupMembership member, Boolean allowLeave, Boolean allowDelete, List<PriceMethodBundle> access) {
		this.businessGroup = new BGShort(businessGroup);
		this.businessGroupDescription = businessGroup.getDescription();
		this.businessGroupLastUsage = businessGroup.getLastUsage();
		this.marked = marked;
		this.markLink = markLink;
		this.member = member;
		this.allowLeave = allowLeave;
		this.allowDelete = allowDelete;
		this.access = access;
	}

	public Long getBusinessGroupKey() {
		return businessGroup.getKey();
	}
	
	public String getBusinessGroupExternalId() {
		return businessGroup.getExternalId();
	}
	
	public String getBusinessGroupName() {
		return businessGroup.getName();
	}
	
	public long getNumOfParticipants() {
		return businessGroup.getNumOfParticipants();
	}
	
	public long getNumOfPendings() {
		return businessGroup.getNumOfPendings();
	}
	
	public long getNumOfOwners() {
		return businessGroup.getNumOfOwners();
	}
	
	public long getNumWaiting() {
		return businessGroup.getNumWaiting();
	}
	
	public Integer getMaxParticipants() {
		return businessGroup.getMaxParticipants();
	}
	
	public boolean isWaitingListEnabled() {
		return businessGroup.isWaitingListEnabled();
	}
	
	public boolean isAutoCloseRanksEnabled() {
		return businessGroup.isAutoCloseRanksEnabled();
	}
	
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return businessGroup.getManagedFlags();
	}
	
	public boolean isFull() {
		Integer maxParticipants = businessGroup.getMaxParticipants();
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
		return businessGroupDescription;
	}

	public Date getBusinessGroupLastUsage() {
		return businessGroupLastUsage;
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

	public FormLink getAccessLink() {
		return accessLink;
	}

	public void setAccessLink(FormLink accessLink) {
		this.accessLink = accessLink;
	}

	public boolean isMarked() {
		return marked;
	}
	
	public void setMarked(boolean mark) {
		this.marked = mark;
	}

	public BusinessGroupMembership getMembership() {
		return member;
	}
	
	public boolean isAccessControl() {
		return access != null;
	}

	public List<PriceMethodBundle> getAccessTypes() {
		return access;
	}

	public Boolean getAllowLeave() {
		return allowLeave;
	}

	public Boolean getAllowDelete() {
		return allowDelete;
	}

	public BusinessGroupShort getBusinessGroup() {
		return businessGroup;
	}
	
	public List<RepositoryEntryShort> getRelations() {
		return relations;
	}
	
	public void addRelation(BGRepositoryEntryRelation resource) {
		if(resource == null) return;
		if(relations == null) {
			relations = new ArrayList<RepositoryEntryShort>(3);
		}
		if(relations.size() < 3) {
			relations.add(new REShort(resource));
		}
	}

	@Override
	public int hashCode() {
		return businessGroup.getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGTableItem) {
			BGTableItem item = (BGTableItem)obj;
			return businessGroup != null && businessGroup.equals(item.businessGroup);
		}
		return false;
	}
	
	private static class BGShort implements BusinessGroupShort {
		private final Long key;
		private final String name;
		private final String externalId;
		private final Integer maxParticipants;
		private long numWaiting;
		private long numOfOwners;
		private long numOfParticipants;
		private long numOfPendings;
		private final boolean waitingListEnabled;
		private final boolean autoCloseRanksEnabled;
		private final BusinessGroupManagedFlag[] managedflags;
		
		public BGShort(BusinessGroup group) {
			key = group.getKey();
			name = group.getName();
			maxParticipants = group.getMaxParticipants();
			waitingListEnabled = group.getWaitingListEnabled() == null ? false : group.getWaitingListEnabled().booleanValue();
			autoCloseRanksEnabled = group.getAutoCloseRanksEnabled() == null ? false : group.getAutoCloseRanksEnabled().booleanValue();
			managedflags = group.getManagedFlags();
			externalId = group.getExternalId();
		}
		
		public BGShort(BusinessGroupView group) {
			key = group.getKey();
			name = group.getName();
			maxParticipants = group.getMaxParticipants();
			numWaiting = group.getNumWaiting();
			numOfOwners = group.getNumOfOwners();
			numOfParticipants = group.getNumOfParticipants();
			numOfPendings = group.getNumOfPendings();
			waitingListEnabled = group.getWaitingListEnabled() == null ? false : group.getWaitingListEnabled().booleanValue();
			autoCloseRanksEnabled = group.getAutoCloseRanksEnabled() == null ? false : group.getAutoCloseRanksEnabled().booleanValue();
			managedflags = group.getManagedFlags();
			externalId = group.getExternalId();
		}

		@Override
		public String getResourceableTypeName() {
			return "BusinessGroup";
		}

		@Override
		public Long getResourceableId() {
			return key;
		}

		@Override
		public Long getKey() {
			return key;
		}

		public String getExternalId() {
			return externalId;
		}

		@Override
		public String getName() {
			return name;
		}

		public Integer getMaxParticipants() {
			return maxParticipants;
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
		
		public boolean isWaitingListEnabled() {
			return waitingListEnabled;
		}

		public boolean isAutoCloseRanksEnabled() {
			return autoCloseRanksEnabled;
		}

		@Override
		public BusinessGroupManagedFlag[] getManagedFlags() {
			return managedflags;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof BGShort) {
				BGShort sh = (BGShort)obj;
				return key != null && key.equals(sh.key);
			}
			return false;
		}
	}
	
	private static class REShort implements RepositoryEntryShort {
		private final Long key;
		private final String displayname;
		public REShort(BGRepositoryEntryRelation rel) {
			this.key = rel.getRepositoryEntryKey();
			this.displayname = rel.getRepositoryEntryDisplayName();
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getDisplayname() {
			return displayname;
		}

		@Override
		public String getResourceType() {
			return "CourseModule";
		}

		@Override
		public int getStatusCode() {
			return 0;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof REShort) {
				REShort re = (REShort)obj;
				return key != null && key.equals(re.key);
			}
			return false;
		}
	}
}