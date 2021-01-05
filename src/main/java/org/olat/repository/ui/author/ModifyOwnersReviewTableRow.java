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
package org.olat.repository.ui.author;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.id.Identity;

/**
 * Initial date: Jan 4, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersReviewTableRow implements FlexiTreeTableNode {
	
	private ModifyOwnersReviewTableRow parent;
	private AuthoringEntryRow resource;
	private Identity identity;
	private ModifyOwnersReviewState state;
	private boolean hasChildren;
	private int addedOwners = 0;
	private int removedOwners = 0;
	
	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}
	
	public void setParent(ModifyOwnersReviewTableRow parent) {
		this.parent = parent;
	}
	
	public AuthoringEntryRow getResource() {
		return resource;
	}
	
	public void setResource(AuthoringEntryRow resource) {
		this.resource = resource;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public ModifyOwnersReviewState getState() {
		return state;
	}
	
	public void setState(ModifyOwnersReviewState state) {
		this.state = state;
	}
	
	public String getResourceOrIdentity() {
		if (parent != null && identity != null) {
			 return identity.getUser().getFirstName() + " " + identity.getUser().getLastName();
		} else if (parent == null && resource != null) {
			return resource.getDisplayname();
		}
		
		return "ERROR";
	}
	
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}

	@Override
	public String getCrump() {
		return null;
	}  
	
	public int getRemovedOwners() {
		return removedOwners;
	}
	
	public void increaseRemovedOwners() {
		removedOwners++;
	}
	
	public int getAddedOwners() {
		return addedOwners;
	}
	
	public void increaseAddedOwners() {
		addedOwners++;
	}
	
	
	
	/**
	 * Used to show the state of change in the review step 
	 * Granted 	=> User was owner before and stays owner, no change
	 * Denied 	=> User was not owner before and is not granted ownership now, no change
	 * Added 	=> User was not owner before and is granted ownership now, change
	 * Removed 	=> User was owner before and is now removed, change
	 * Resource => Shows the state of changes for a resource (e.g 2 added, 1 removed or No change)
	 */
	public enum ModifyOwnersReviewState {
		granted, 
		denied,
		added, 
		removed,
		resource;
	}
}
