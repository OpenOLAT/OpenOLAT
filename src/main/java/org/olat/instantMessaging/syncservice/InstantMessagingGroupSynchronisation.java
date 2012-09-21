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
package org.olat.instantMessaging.syncservice;

import java.util.Collection;


public interface InstantMessagingGroupSynchronisation {

	/**
	 * Creates a shared buddy group where all members see each other on their
	 * rosters
	 * 
	 * @param groupId olat ressource id
	 * @param displayName name shown in the roster
	 * @param initialMembers array with olat usernames
	 */
	public boolean createSharedGroup(String groupId, String displayName);
	
	/**
	 * Rename shared buddy group on the IM server
	 * 
	 * @param groupId
	 * @param displayName
	 */
	public boolean renameSharedGroup(String groupId, String displayName);

	/**
	 * @param groupId
	 */
	public boolean deleteSharedGroup(String groupId);

	/**
	 * 
	 * @param groupId
	 * @param user
	 */
	public boolean addUserToSharedGroup(String groupId, Collection<String> users);

	/**
	 * @param groupId
	 * @param username
	 */
	public boolean removeUserFromSharedGroup(String groupId, String username);

	/**
	 * @param groupId
	 * @param users
	 */
	public boolean removeUsersFromSharedGroup(String groupId, Collection<String> users);

}