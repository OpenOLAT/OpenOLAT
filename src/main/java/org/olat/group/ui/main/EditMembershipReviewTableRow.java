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

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;

/**
 * Initial date: Dec 4, 2020<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class EditMembershipReviewTableRow implements FlexiTreeTableNode {

	private EditMembershipReviewTableRow parent;
	private boolean hasChildren;
	private boolean waitingListEnabled;
	
	/**
	 *	0: User and changes
	 *	1: Group, curriculum or repository entry or category to 
	 */
	private int rowMode;
	
	/**
	 * 	0: No information
	 *	1: Already added 
	 *	2: Newly added
	 *	3: Already removed
	 *	4: Newly removed
	 */
	private int participantPermissionState;
	private int tutorPermissionState;
	private int ownerPermissionState;
	private int waitingListPermissionState;
	
	private int totalAddedParticipant;
	private int totalRemovedParticipant;
	private int totalAddedTutor;
	private int totalRemovedTutor;
	private int totalAddedOwner;
	private int totalRemovedOwner;
	private int totalAddedWaitingList;
	private int totalRemovedWaitingList;
	
	/**
	 * Can be name of user, group name, curriculum name or repository entry name
	 */
	private String nameOrIdentifier;	
	
	public EditMembershipReviewTableRow(EditMembershipReviewTableRow parent, int rowMode, boolean hasChildren) {
		this.parent = parent;
		this.rowMode = rowMode;
		this.hasChildren = hasChildren;
	}
	
	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	@Override
	public String getCrump() {
		return null;
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}
	
	public int getRowMode() {
		return rowMode;
	}
	
	public int getTotalAddedParticipant() {
		return totalAddedParticipant;
	}
	
	public void increaseTotalAddedParticipant() {
		totalAddedParticipant += 1;
		
		if (parent != null) {
			parent.increaseTotalAddedParticipant();
		}
	}

	public int getTotalRemovedParticipant() {
		return totalRemovedParticipant;
	}
	
	public void increaseTotalRemovedParticipant() {
		totalRemovedParticipant += 1;
		
		if (parent != null) {
			parent.increaseTotalRemovedParticipant();
		}
	}

	public int getTotalAddedTutor() {
		return totalAddedTutor;
	}
	
	public void increaseTotalAddedPTutor() {
		totalAddedTutor += 1;
		
		if (parent != null) {
			parent.increaseTotalAddedPTutor();
		}
	}

	public int getTotalRemovedTutor() {
		return totalRemovedTutor;
	}
	
	public void increaseTotalRemovedTutor() {
		totalRemovedTutor += 1;
		
		if (parent != null) {
			parent.increaseTotalRemovedTutor();
		}
	}

	public int getTotalAddedOwner() {
		return totalAddedOwner;
	}
	
	public void increaseTotalAddedOwner() {
		totalAddedOwner += 1;
		
		if (parent != null) {
			parent.increaseTotalAddedOwner();
		}
	}

	public int getTotalRemovedOwner() {
		return totalRemovedOwner;
	}
	
	public void increaseTotalRemovedOwner() {
		totalRemovedOwner += 1;
		
		if (parent != null) {
			parent.increaseTotalRemovedOwner();
		}
	}

	public int getTotalAddedWaitingList() {
		return totalAddedWaitingList;
	}
	
	public void increaseTotalAddedWaitingList() {
		totalAddedWaitingList += 1;
		
		if (parent != null) {
			parent.increaseTotalAddedWaitingList();
		}
	}

	public int getTotalRemovedWaitingList() {
		return totalRemovedWaitingList;
	}
	
	public void increaseTotalRemovedWaitingList() {
		totalRemovedWaitingList += 1;
		
		if (parent != null) {
			parent.increaseTotalRemovedWaitingList();
		}
	}

	public int getParticipantPermissionState() {
		return participantPermissionState;
	}
	
	public void setParticipantPermissionState(int permissionState) {
		this.participantPermissionState = permissionState;
	}
	
	public int getTutorPermissionState() {
		return tutorPermissionState;
	}
	
	public void setTutorPermissionState(int tutorPermissionState) {
		this.tutorPermissionState = tutorPermissionState;
	}
	
	public int getOwnerPermissionState() {
		return ownerPermissionState;
	}
	
	public void setOwnerPermissionState(int ownerPermissionState) {
		this.ownerPermissionState = ownerPermissionState;
	} 
	
	public int getWaitingListPermissionState() {
		return waitingListPermissionState;
	}
	
	public void setWaitingListPermissionState(int waitingListPermissionState) {
		this.waitingListPermissionState = waitingListPermissionState;
	}
	
	public String getNameOrIdentifier() {
		return nameOrIdentifier;
	}
	
	public void setNameOrIdentifier(String nameOrIdentifier) {
		this.nameOrIdentifier = nameOrIdentifier;
	}
	
	public boolean isWaitingListEnabled() {
		return waitingListEnabled;
	}
	
	public void setWaitingListEnabled(boolean waitingListEnabled) {
		this.waitingListEnabled = waitingListEnabled;
	}
	
	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
