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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.ui.edit;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * This event signalizes that a group has been changed. it contains the modified
 * group
 * <p>
 * Initial Date: Sep 2, 2004
 * 
 * @author gnaegi
 */
public class BusinessGroupModifiedEvent extends MultiUserEvent {

	private static final long serialVersionUID = 6234290505358324180L;
	/** event: group has been modified */
	public static final String CONFIGURATION_MODIFIED_EVENT = "configuration.modified.event";
	/** event: an identity has been added to the group */
	public static final String IDENTITY_ADDED_EVENT = "identity.added.event";
	/** event: an identity has been removed from the group */
	public static final String IDENTITY_REMOVED_EVENT = "identity.removed.event";
	/** event: an identity has been added to the group but needs a user confirmation */
	public static final String IDENTITY_ADD_PENDING_EVENT = "identity.add.pending.event";
	/** event: the associated group rights have been changed */
	public static final String GROUPRIGHTS_MODIFIED_EVENT = "grouprights.modified.event";
	/** event: an identity has been removed from the owner-group by himself */
	public static final String MYSELF_ASOWNER_REMOVED_EVENT = "myself.removed.event";

	private Long groupKey;
	private Long identityKey;
	private boolean isTutor = false;
	private Long senderKey;
	
	/**
	 * @param command one of the class constants
	 * @param group
	 * @param identity
	 */
	public BusinessGroupModifiedEvent(String command, BusinessGroup group, IdentityRef identity, IdentityRef sender) {
		super(command);
		this.groupKey = group.getKey();
		this.identityKey = (identity == null ? null : identity.getKey());
		this.senderKey = (sender == null ? null : sender.getKey());
		if (identity != null) {
			if (command.equals(MYSELF_ASOWNER_REMOVED_EVENT)) {
				isTutor = true; // Removed myself as tutor/owner from group
			} else {
				isTutor = CoreSpringFactory.getImpl(BusinessGroupService.class).hasRoles(identity, group, GroupRoles.coach.name());
			}
		} 
	}

	/**
	 * @return the key of the modified group
	 */
	public Long getModifiedGroupKey() {
		return this.groupKey;
	}
	
	public boolean isSender(IdentityRef identity) {
		return senderKey != null && identity != null && senderKey.equals(identity.getKey());
	}
	
	public Long getSenderKey() {
		return senderKey;
	}

	/**
	 * @return The key of the affected identity
	 */
	public Long getAffectedIdentityKey() {
		return this.identityKey;
	}

	/**
	 * @param identity
	 * @return
	 * @see updateBusinessGroupList(List businessGroups, Identity identity)
	 */
	public boolean wasMyselfAdded(Identity identity) {
		if(identity == null || identity.getKey() == null) {
			return false;
		}
		return getCommand().equals(IDENTITY_ADDED_EVENT) && getAffectedIdentityKey().equals(identity.getKey());
	}
	
	/**
	 * 
	 * @param identity The identity
	 * @return true if the specified identity wait to be added
	 */
	public boolean wasMyselfAddPending(Identity identity) {
		if(identity == null || identity.getKey() == null) {
			return false;
		}
		return getCommand().equals(IDENTITY_ADD_PENDING_EVENT) && getAffectedIdentityKey().equals(identity.getKey());
	}

	/**
	 * @param identity
	 * @return
	 * @see updateBusinessGroupList(List businessGroups, Identity identity)
	 */
	public boolean wasMyselfRemoved(Identity identity) {
		if(identity == null || identity.getKey() == null) {
			return false;
		}
		if (isTutor) {
			return getCommand().equals(MYSELF_ASOWNER_REMOVED_EVENT) && getAffectedIdentityKey().equals(identity.getKey());
		}
		return getCommand().equals(IDENTITY_REMOVED_EVENT) && getAffectedIdentityKey().equals(identity.getKey());
	}

	/**
	 * @param businessGroups a list of BusinessGroup objects
	 * @param identity the identity for whom the businessGroupsList was built (we
	 *          only update the list if the identity is affected)
	 * @return true if the list was modified
	 */
	public boolean updateBusinessGroupList(List<BusinessGroup> businessGroups, Identity identity) {
		boolean added = wasMyselfAdded(identity);
		boolean removed = wasMyselfRemoved(identity);
		// we are only interested in added and removed-events here
		if (!added && !removed) return false;
		Long modKey = getModifiedGroupKey();

		if (added) {
			// load the business group and add it to the groups list
			BusinessGroup nGroup = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(modKey);
			if(nGroup != null) {
				businessGroups.add(nGroup);
			}
			return true;
		}
		// else : removed
		for (Iterator<BusinessGroup> it_groups = businessGroups.iterator(); it_groups.hasNext();) {
			BusinessGroup group = it_groups.next();
			if (modKey.equals(group.getKey())) {
				// our list is affected by the modified event
				it_groups.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Fires event to all listeners of this business group and the listeners of
	 * the ressources associated with the group context of this group
	 * 
	 * @param command The event identifyer, one of CONFIGURATION_MODIFIED_EVENT,
	 *          IDENTITY_ADDED_EVENT or IDENTITY_REMOVED_EVENT
	 * @param group The group affected by the modification
	 * @param identity The identity affected by the modification
	 */
	public static void fireModifiedGroupEvents(String command, BusinessGroup group, Identity identity, IdentityRef sender) {
		BusinessGroupModifiedEvent modifiedEvent = new BusinessGroupModifiedEvent(command, group, identity, sender);
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		// 1) notify listeners of group events
		eventBus.fireEventToListenersOf(modifiedEvent, group);
		// 2) notify listeners of learning resources of this group
		BusinessGroupService bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		List<RepositoryEntry> repoEntries = bgs.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		for (RepositoryEntry entry:repoEntries) {
			eventBus.fireEventToListenersOf(modifiedEvent, entry);
		}
	}
	
	public static Deferred createDeferredEvent(String command, BusinessGroup group, Identity identity) {
		return new Deferred(command, group, identity);	
	}
	
	public static void fireDeferredEvents(List<Deferred> events) {
		if(events == null || events.isEmpty()) return;
		
		for(Deferred deferedEvent:events) {
			fireModifiedGroupEvents(deferedEvent.command, deferedEvent.group, deferedEvent.identity, null);
		}
	}
	
	@Override
	public String toString() {
		return "groupkey:"+groupKey+",identityKey:"+identityKey+", isTutor:"+isTutor+"|"+super.toString();
	}
	
	public static class Deferred {
		private final String command;
		private final Identity identity;
		private final BusinessGroup group;
		
		public Deferred(String command, BusinessGroup group, Identity identity) {
			this.command = command;
			this.group = group;
			this.identity = identity;
		}
	}
}
