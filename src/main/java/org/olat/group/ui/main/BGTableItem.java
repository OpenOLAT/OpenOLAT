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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
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
	private boolean accessControl;
	private final boolean member;
	private final Boolean allowLeave;
	private final Boolean allowDelete;
	private final BusinessGroup businessGroup;
	private List<RepositoryEntry> resources;
	private List<PriceMethodBundle> access;
	
	public BGTableItem(BusinessGroup businessGroup, boolean member, Boolean allowLeave, Boolean allowDelete,
			boolean accessControl, List<PriceMethodBundle> access) {
		this.accessControl = accessControl;
		this.businessGroup = businessGroup;
		this.member = member;
		this.allowLeave = allowLeave;
		this.allowDelete = allowDelete;
		this.access = access;
	}

	//fxdiff VCRP-1,2: access control of resources
	public boolean isMember() {
		return member;
	}
	//fxdiff VCRP-1,2: access control of resources
	public boolean isAccessControl() {
		return accessControl;
	}

	public List<PriceMethodBundle> getAccessTypes() {
		return access;
	}

	public void setAccessControl(boolean accessControl) {
		this.accessControl = accessControl;
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