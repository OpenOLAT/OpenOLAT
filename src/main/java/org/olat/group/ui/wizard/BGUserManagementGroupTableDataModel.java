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
package org.olat.group.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGUserManagementGroupTableDataModel extends DefaultTableDataModel<Identity> {
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<Identity> addCoachesIdentities = new ArrayList<>();
	private final List<Identity> addParticipantIdentities = new ArrayList<>();
	private final List<Identity> addToWaitingList = new ArrayList<>();
	private final List<Identity> removedIdentities = new ArrayList<>();
	
	private final List<Identity> coaches = new ArrayList<>();
	private final List<Identity> participants = new ArrayList<>();
	private final List<Identity> waitingList = new ArrayList<>();
	
	public BGUserManagementGroupTableDataModel(Locale locale, List<UserPropertyHandler> userPropertyHandlers) {
		super(new ArrayList<Identity>());
		setLocale(locale);		
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	public List<Identity> getAddCoachesIdentities() {
		return addCoachesIdentities;
	}

	public void addCoaches(List<Identity> identitiesToAdd) {
		addCoachesIdentities.addAll(identitiesToAdd);
		addToObjects(identitiesToAdd);
	}

	public List<Identity> getAddParticipantIdentities() {
		return addParticipantIdentities;
	}

	public void addParticipants(List<Identity> identitiesToAdd) {
		addParticipantIdentities.addAll(identitiesToAdd);
		addToObjects(identitiesToAdd);
	}

	public List<Identity> getRemovedIdentities() {
		return removedIdentities;
	}
	
	public void addToWaitingList(List<Identity> identitiesToAdd) {
		addToWaitingList.addAll(identitiesToAdd);
		addToObjects(identitiesToAdd);
	}
	
	public List<Identity> getAddToWaitingList() {
		return addToWaitingList;
	}

	public void remove(List<Identity> identitiesToRemove) {
		coaches.removeAll(identitiesToRemove);
		addParticipantIdentities.removeAll(identitiesToRemove);
		addToWaitingList.removeAll(identitiesToRemove);
		removedIdentities.addAll(identitiesToRemove);
	}

	public final Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		switch(col) {
			case 0: {
				if(addCoachesIdentities.contains(identity)) {
					return Status.newOwner;
				}
				if(addParticipantIdentities.contains(identity)) {
					return Status.newParticipant;
				}
				if(addToWaitingList.contains(identity)) {
					return Status.newWaiting;
				}
				if(removedIdentities.contains(identity)) {
					return Status.removed;
				}
				return Status.current;		
			}
			case 1: {
				if(coaches.contains(identity) || addCoachesIdentities.contains(identity)) {
					return GroupRoles.coach;
				}
				if(participants.contains(identity) || addParticipantIdentities.contains(identity)) {
					return GroupRoles.participant;
				}
				if(waitingList.contains(identity) || addToWaitingList.contains(identity)) {
					return GroupRoles.waiting;
				}
				return null;		
			}
			default: {
				int propertyIndex = col - 2;
				if (propertyIndex >= 0 && propertyIndex < userPropertyHandlers.size() ) {
					// get user property for this column
					UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(propertyIndex);
					String value = userPropertyHandler.getUserProperty(identity.getUser(), getLocale());
					return (value == null ? "n/a" : value);
				}
				return "error";
			}
		}
	}

	@Override
	public Object createCopyWithEmptyList() {
		return new BGUserManagementGroupTableDataModel(getLocale(), userPropertyHandlers);
	}

	public void setMembers(List<Identity> coaches,  List<Identity> participants, List<Identity> waitingList) {
		this.coaches.clear();
		this.coaches.addAll(coaches);
		addToObjects(coaches);
		this.participants.clear();
		this.participants.addAll(participants);
		addToObjects(participants);
		this.waitingList.clear();
		this.waitingList.addAll(waitingList);
		addToObjects(waitingList);
	}
	
	private void addToObjects(List<Identity> identities) {
		for(Identity identity:identities) {
			if(!getObjects().contains(identity)) {
				getObjects().add(identity);
			}
		}
	}

	public int getColumnCount() {
		return userPropertyHandlers.size() + 1;
	}
	
	public enum Status {
		newOwner,
		newParticipant,
		newWaiting,
		removed,
		current
	}


}
