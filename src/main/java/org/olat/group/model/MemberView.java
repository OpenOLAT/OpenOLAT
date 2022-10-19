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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupShort;
import org.olat.group.ui.main.CourseMembership;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementShort;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.resource.OLATResource;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 6 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberView extends UserPropertiesRow {
	
	private RepositoryEntry repositoryEntry;
	private List<BusinessGroupShort> groups;
	private List<CurriculumElementShort> curriculumElements;
	
	private Date creationDate;
	private Date lastModified;
	
	private final Integer identityStatus;
	
	private boolean managedMembersRepo;
	private final CourseMembership membership = new CourseMembership();
	
	public MemberView(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		identityStatus = identity == null ? null : identity.getStatus();
	}
	
	public Integer getIdentityStatus() {
		return identityStatus;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		if(creationDate == null) return;
		if(this.creationDate == null || this.creationDate.compareTo(creationDate) > 0) {
			this.creationDate = creationDate;
		}
	}

	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		if(lastModified == null) return;
		if(this.lastModified == null || this.lastModified.compareTo(lastModified) < 0) {
			this.lastModified = lastModified;
		}
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntry == null ? null : repositoryEntry.getKey();
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
		if(repositoryEntry != null) {
			boolean membersManaged = RepositoryEntryManagedFlag
					.isManaged(repositoryEntry.getManagedFlags(), RepositoryEntryManagedFlag.membersmanagement);
			membership.setManagedMembersRepo(membersManaged);
		}
	}
	
	public OLATResource getOLATResource() {
		return repositoryEntry == null ? null : repositoryEntry.getOlatResource();
	}

	public String getRepositoryEntryDisplayName() {
		return repositoryEntry == null ? null : repositoryEntry.getDisplayname();
	}

	public String getRepositoryEntryExternalId() {
		return repositoryEntry == null ? null : repositoryEntry.getExternalId();
	}

	public String getRepositoryEntryExternalRef() {
		return repositoryEntry == null ? null : repositoryEntry.getExternalRef();
	}

	public RepositoryEntryManagedFlag[] getManagedFlags() {
		return repositoryEntry == null ? null : repositoryEntry.getManagedFlags();
	}

	public List<BusinessGroupShort> getGroups() {
		return groups;
	}

	public void setGroups(List<BusinessGroupShort> groups) {
		this.groups = groups;
	}
	
	public void addGroup(BusinessGroupShort group) {
		if(groups == null) {
			groups = new ArrayList<>(5);
		}
		groups.add(group);
	}

	public List<CurriculumElementShort> getCurriculumElements() {
		return curriculumElements;
	}

	public void setCurriculumElements(List<CurriculumElementShort> curriculumElements) {
		this.curriculumElements = curriculumElements;
	}
	
	public void addCurriculumElement(CurriculumElementShort curriculumElement) {
		if(curriculumElements == null) {
			curriculumElements = new ArrayList<>(3);
		}
		curriculumElements.add(curriculumElement);
	}
	
	public CourseMembership getMemberShip() {
		return membership;
	}

	public boolean isManagedMembersRepo() {
		return managedMembersRepo;
	}

	public void setManagedMembersRepo(boolean managedMembersRepo) {
		this.managedMembersRepo = managedMembersRepo;
	}

	public static class BusinessGroupShortImpl implements BusinessGroupShort {
		
		private final Long key;
		private final String name;
		private final BusinessGroupManagedFlag[] managedFlags;
		
		public BusinessGroupShortImpl(Long key, String name, String managedFlags) {
			this.key = key;
			this.name = name;
			this.managedFlags = BusinessGroupManagedFlag.toEnum(managedFlags);
		}

		@Override
		public String getResourceableTypeName() {
			return "BusinessGroup";
		}

		@Override
		public Long getResourceableId() {
			return getKey();
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public BusinessGroupManagedFlag[] getManagedFlags() {
			return managedFlags;
		}
	}
	
	public static class CurriculumElementShortImpl implements CurriculumElementShort {
		
		private final Long key;
		private final String displayName;
		private final CurriculumElementManagedFlag[] managedFlags;
		
		public CurriculumElementShortImpl(Long key, String displayName, String managedFlags) {
			this.key = key;
			this.displayName = displayName;
			this.managedFlags = CurriculumElementManagedFlag.toEnum(managedFlags);
		}
		
		public CurriculumElementShortImpl(Long key, String displayName, CurriculumElementManagedFlag[] managedFlags) {
			this.key = key;
			this.displayName = displayName;
			this.managedFlags = managedFlags;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public CurriculumElementManagedFlag[] getManagedFlags() {
			return managedFlags;
		}
	}
}
