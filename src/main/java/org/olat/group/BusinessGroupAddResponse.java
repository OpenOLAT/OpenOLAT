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

package org.olat.group;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * POJO to pass response of BusinesGroupManager.addParticipantsAndFireEvent and
 * addOwnersAndFireEvent
 * 
 * <P>
 * Initial Date: 11.12.2006 <br>
 * 
 * @author Ch.Guretzki
 */
public class BusinessGroupAddResponse {
	/* Output-list of Identity which are added */
	private List<Identity> addedIdentities;
	/* Output-list of Identity which are already in group */
	private List<Identity> identitiesAlreadyInGroup;
	/* Output-list of Identity which are added */
	private List<Identity> identitiesWithoutPermission;

	public BusinessGroupAddResponse() {
		addedIdentities = new ArrayList<>();
		identitiesAlreadyInGroup = new ArrayList<>();
		identitiesWithoutPermission = new ArrayList<>();
	}

	/**
	 * @return List of Identity which are added.
	 */
	public List<Identity> getAddedIdentities() {
		return this.addedIdentities;
	}

	/**
	 * @return List of Identity which hd be added.
	 */
	public List<Identity> getIdentitiesAlreadyInGroup() {
		return this.identitiesAlreadyInGroup;
	}

	/**
	 * @return List of Identity which hd be added.
	 */
	public List<Identity> getIdentitiesWithoutPermission() {
		return this.identitiesWithoutPermission;
	}

	
	
}