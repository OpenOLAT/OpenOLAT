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
import java.util.Collections;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberPermissionChangeEvent extends RepositoryEntryPermissionChangeEvent {
	private static final long serialVersionUID = 8499004967313689825L;

	private List<RepositoryEntryPermissionChangeEvent> repositoryChanges;
	private List<BusinessGroupMembershipChange> groupChanges;
	private List<CurriculumElementMembershipChange> curriculumChanges;
	
	public MemberPermissionChangeEvent(Identity member) {
		super(member);
	}
	
	public List<BusinessGroupShort> getGroups() {
		List<BusinessGroupShort> groups = new ArrayList<>();
		if(groupChanges != null && !groupChanges.isEmpty()) {
			for(BusinessGroupMembershipChange change:groupChanges) {
				BusinessGroupShort group = change.getGroup();
				if(!groups.contains(group)) {
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	public List<RepositoryEntryPermissionChangeEvent> getRepoChanges() {
		return repositoryChanges;
	}
	
	public void setRepoChanges(List<RepositoryEntryPermissionChangeEvent> repoPermissionChanges) {
		repositoryChanges = repoPermissionChanges;
	}
	
	public List<BusinessGroupMembershipChange> getGroupChanges() {
		return groupChanges;
	}

	public void setGroupChanges(List<BusinessGroupMembershipChange> changes) {
		this.groupChanges = changes;
	}
	

	public List<CurriculumElementMembershipChange> getCurriculumChanges() {
		return curriculumChanges;
	}

	public void setCurriculumChanges(List<CurriculumElementMembershipChange> curriculumChanges) {
		this.curriculumChanges = curriculumChanges;
	}

	@Override
	public int size() {
		return (groupChanges == null ? 0 : groupChanges.size())
				+ (curriculumChanges == null ? 0 : curriculumChanges.size())
				+ (repositoryChanges == null ? 0 : repositoryChanges.size())
				+ super.size();
	}
	
	public List<RepositoryEntryPermissionChangeEvent> generateRepositoryChanges(List<Identity> members) {
		if(members == null || members.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<RepositoryEntryPermissionChangeEvent> repoChanges = getRepoChanges();
		if (repoChanges == null || repoChanges.isEmpty()) {
			// Fallback solution
			List<RepositoryEntryPermissionChangeEvent> changes = new ArrayList<>();
			for (Identity member : members) {
				changes.add(new RepositoryEntryPermissionChangeEvent(member, this));
			}
			
			return changes;
		}
		
		List<RepositoryEntryPermissionChangeEvent> allModifications = new ArrayList<>();
		for (RepositoryEntryPermissionChangeEvent repoChange : repoChanges) {
			for(Identity member:members) {
				allModifications.add(new RepositoryEntryPermissionChangeEvent(member, repoChange));
			}
		}
		
		return allModifications;
	}
	
	public List<BusinessGroupMembershipChange> generateBusinessGroupMembershipChange(List<Identity> members) {
		if(members == null || members.isEmpty()) {
			return Collections.emptyList();
		}
		List<BusinessGroupMembershipChange> grChanges = getGroupChanges();
		if(grChanges == null || grChanges.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<BusinessGroupMembershipChange> allModifications = new ArrayList<>();
		for(BusinessGroupMembershipChange grChange:grChanges) {
			for(Identity member:members) {
				allModifications.add(new BusinessGroupMembershipChange(member, grChange));
			}
		}
		return allModifications;
	}
	
	public List<CurriculumElementMembershipChange> generateCurriculumElementMembershipChange(List<Identity> members) {
		if(members == null || members.isEmpty()) {
			return Collections.emptyList();
		}
		List<CurriculumElementMembershipChange> elementChanges = getCurriculumChanges();
		if(elementChanges == null || elementChanges.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<CurriculumElementMembershipChange> allModifications = new ArrayList<>();
		for(CurriculumElementMembershipChange grChange:elementChanges) {
			for(Identity member:members) {
				allModifications.add(new CurriculumElementMembershipChange(member, grChange));
			}
		}
		return allModifications;
	}
	
	/**
	 * @return The first curriculum element with the shortest path.
	 */
	public CurriculumElement getRootCurriculumElement() {
		if(curriculumChanges == null || curriculumChanges.isEmpty()) return null;

		int numOfSegments = -1;
		CurriculumElement root = null; 
		
		for(CurriculumElementMembershipChange change:curriculumChanges) {
			int segments = change.numOfSegments();
			if(root == null || segments < numOfSegments) {
				root = change.getElement();
				numOfSegments = segments;
			}
		}
		
		return root;
	}
	

}