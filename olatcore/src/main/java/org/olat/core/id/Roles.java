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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.id;

import java.io.Serializable;

/**
*  Description:<br>
* @author Felix Jost
*/
public class Roles implements Serializable {
	private boolean isOLATAdmin;
	private boolean isUserManager;
	private boolean isGroupManager;
	private boolean isAuthor;
	private boolean isGuestOnly;
	private boolean isInstitutionalResourceManager;
	private boolean isInvitee;

	/**
	 * @param isOLATAdmin
	 * @param isUserManager
	 * @param isGroupManager
	 * @param isAuthor
	 * @param isGuestOnly
	 * @param isUniCourseManager
	 */
	public Roles(boolean isOLATAdmin, boolean isUserManager, boolean isGroupManager, boolean isAuthor, boolean isGuestOnly, boolean isInstitutionalResourceManager, boolean isInvitee) {
		this.isOLATAdmin = isOLATAdmin;
		this.isGroupManager = isGroupManager;
		this.isUserManager = isUserManager;
		this.isAuthor = isAuthor;
		this.isGuestOnly = isGuestOnly;
		this.isInstitutionalResourceManager = isInstitutionalResourceManager;
		this.isInvitee = isInvitee;
	}

	/**
	 * @return boolean
	 */
	public boolean isOLATAdmin() {
		return isOLATAdmin;
	}

	/**
	 * @return boolean
	 */
	public boolean isAuthor() {
		return isAuthor;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isGuestOnly() {
		return isGuestOnly;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isGroupManager() {
		return isGroupManager;
	}

	/**
	 * @return boolean
	 */
	public boolean isUserManager() {
		return isUserManager;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isInstitutionalResourceManager() {
		return isInstitutionalResourceManager;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isInvitee() {
		return isInvitee;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "admin:"+isOLATAdmin+", usermanager:"+isUserManager+", groupmanager:"+isGroupManager+", author:"+isAuthor+", guestonly:"+isGuestOnly+", isInstitutionalResourceManager:"+isInstitutionalResourceManager+", isInvitee:"+isInvitee+", "+super.toString();
	}

}
