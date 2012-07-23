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
import java.util.List;

import org.olat.group.BusinessGroup;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.repository.RepositoryEntry;
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
	private final BGMembership member;
	private boolean marked;
	private final Boolean allowLeave;
	private final Boolean allowDelete;
	private final BusinessGroup businessGroup;
	private List<RepositoryEntry> resources;
	private List<BGRepositoryEntryRelation> relations;
	private List<PriceMethodBundle> access;
	
	public BGTableItem(BusinessGroup businessGroup, boolean marked, BGMembership member, Boolean allowLeave, Boolean allowDelete, List<PriceMethodBundle> access) {
		this.businessGroup = businessGroup;
		this.marked = marked;
		this.member = member;
		this.allowLeave = allowLeave;
		this.allowDelete = allowDelete;
		this.access = access;
	}

	public boolean isMarked() {
		return marked;
	}
	
	public void setMarked(boolean mark) {
		this.marked = mark;
	}

	public BGMembership getMembership() {
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

	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}
	
	public List<RepositoryEntry> getResources() {
		return resources;
	}

	public void setResources(List<RepositoryEntry> resources) {
		this.resources = resources;
	}

	public List<BGRepositoryEntryRelation> getRelations() {
		return relations;
	}

	public void setRelations(List<BGRepositoryEntryRelation> relations) {
		this.relations = relations;
	}
	
	/**
	 * Give the item a list of relations, it found alone which are its own
	 * @param resources
	 */
	public void setUnfilteredRelations(List<BGRepositoryEntryRelation> resources) {
		relations = new ArrayList<BGRepositoryEntryRelation>();
		for(BGRepositoryEntryRelation resource:resources) {
			if(businessGroup.getKey().equals(resource.getGroupKey())) {
				relations.add(resource);
				if(relations.size() >= 3) {
					return;
				}
			}
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
			return businessGroup != null && businessGroup.equalsByPersistableKey(item.businessGroup);
		}
		return false;
	}
}