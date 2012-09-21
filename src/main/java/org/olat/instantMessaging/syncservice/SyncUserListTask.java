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
package org.olat.instantMessaging.syncservice;

import java.util.HashSet;
import java.util.Set;

import org.olat.group.BusinessGroup;
import org.olat.instantMessaging.InstantMessagingModule;

/**
 * A task to sync a list of users with a roster
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SyncUserListTask implements Runnable {
	
	private final String groupId;
	private final String groupDisplayName;
	private final Set<String> userToAdd = new HashSet<String>();
	private final Set<String> userToRemove = new HashSet<String>();
	
	/**
	 * 
	 * @param group
	 */
	public SyncUserListTask(BusinessGroup group) {
		groupId = InstantMessagingModule.getAdapter().createChatRoomString(group);
		groupDisplayName = group.getName();
	}
	
	public Set<String> getUserToAdd() {
		return userToAdd;
	}

	public void addUserToAdd(String username) {
		userToAdd.add(username);
	}

	public Set<String> getUserToRemove() {
		return userToRemove;
	}
	
	public void addUserToRemove(String username) {
		userToRemove.add(username);
	}
	
	public boolean isEmpty() {
		return userToRemove.isEmpty() && userToAdd.isEmpty();
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		InstantMessagingModule.getAdapter().syncFriendsRoster(groupId, groupDisplayName, userToAdd, userToRemove);
	}

}
