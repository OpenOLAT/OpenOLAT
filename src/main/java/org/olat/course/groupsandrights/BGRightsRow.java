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
package org.olat.course.groupsandrights;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.group.right.BGRightsRole;

/**
 * 
 * Initial date: 23 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BGRightsRow {
	private final String name;
	private final Group baseGroup;
	private final BGRightsRole role;
	private final BGRightsResourceType resourceType;
	
	private List<BGRight> rightsEl;
	
	public BGRightsRow(Group baseGroup, String name, BGRightsRole role, BGRightsResourceType resourceType) {
		this.name = name;
		this.role = role;
		this.baseGroup = baseGroup;
		this.resourceType = resourceType;
	}
	
	public String getName() {
		return name;
	}
	
	public Group getBaseGroup() {
		return baseGroup;
	}
	
	public BGRightsRole getRole() {
		return role;
	}
	
	public BGRightsResourceType getResourceType() {
		return resourceType;
	}

	public List<String> getSelectedPermissions() {
		List<String> permissions = new ArrayList<String>(rightsEl.size());
		for(BGRight rightEl:rightsEl) {
			if(rightEl.getSelection().isAtLeastSelected(1)) {
				permissions.add(rightEl.getPermission());
			}	
		}
		return permissions;
	}
	
	public List<BGRight> getRightsEl() {
		return rightsEl;
	}
	
	public void setRightsEl(List<BGRight> rightsEl) {
		this.rightsEl = rightsEl;
	}
	
	public static class BGRight {
		private final String permission;
		private MultipleSelectionElement selection;
		
		public BGRight(String permission) {
			this.permission = permission;
		}

		public MultipleSelectionElement getSelection() {
			return selection;
		}

		public void setSelection(MultipleSelectionElement selection) {
			this.selection = selection;
		}

		public String getPermission() {
			return permission;
		}
	}
}
