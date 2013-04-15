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
* <p>
*/ 

package org.olat.core.id;

import java.io.Serializable;

/**
*  Description:<br>
* @author Felix Jost
*/
public class Roles implements Serializable {
	private static final long serialVersionUID = 4726449291059674346L;
	private boolean isOLATAdmin;
	private boolean isUserManager;
	private boolean isGroupManager;
	private boolean isAuthor;
	private boolean isGuestOnly;
	private boolean isInstitutionalResourceManager;
	private boolean isPoolAdmin;
	private boolean isInvitee;

	/**
	 * @param isOLATAdmin
	 * @param isUserManager
	 * @param isGroupManager
	 * @param isAuthor
	 * @param isGuestOnly
	 * @param isUniCourseManager
	 */
	public Roles(boolean isOLATAdmin, boolean isUserManager, boolean isGroupManager, boolean isAuthor, boolean isGuestOnly,
			boolean isInstitutionalResourceManager, boolean isInvitee) {
		this(isOLATAdmin, isGroupManager, isUserManager, isAuthor, isGuestOnly, isInstitutionalResourceManager, false, isInvitee);
	}
	
	public Roles(boolean isOLATAdmin, boolean isUserManager, boolean isGroupManager, boolean isAuthor, boolean isGuestOnly,
			boolean isInstitutionalResourceManager, boolean isPoolAdmin, boolean isInvitee) {
		this.isOLATAdmin = isOLATAdmin;
		this.isGroupManager = isGroupManager;
		this.isUserManager = isUserManager;
		this.isAuthor = isAuthor;
		this.isGuestOnly = isGuestOnly;
		this.isInstitutionalResourceManager = isInstitutionalResourceManager;
		this.isPoolAdmin = isPoolAdmin;
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
	public boolean isPoolAdmin() {
		return isPoolAdmin;
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
	@Override
	public String toString() {
		return "admin:"+isOLATAdmin+", usermanager:"+isUserManager+", groupmanager:"+isGroupManager+", author:"+isAuthor+", guestonly:"+isGuestOnly+", isInstitutionalResourceManager:"+isInstitutionalResourceManager+", isInvitee:"+isInvitee+", "+super.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isAuthor ? 1231 : 1237);
		result = prime * result + (isGroupManager ? 1231 : 1237);
		result = prime * result + (isGuestOnly ? 1231 : 1237);
		result = prime * result + (isInstitutionalResourceManager ? 1231 : 1237);
		result = prime * result + (isInvitee ? 1231 : 1237);
		result = prime * result + (isOLATAdmin ? 1231 : 1237);
		result = prime * result + (isUserManager ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		Roles other = (Roles) obj;
		if (isAuthor != other.isAuthor) return false;
		if (isGroupManager != other.isGroupManager) return false;
		if (isGuestOnly != other.isGuestOnly) return false;
		if (isInstitutionalResourceManager != other.isInstitutionalResourceManager) return false;
		if (isInvitee != other.isInvitee) return false;
		if (isOLATAdmin != other.isOLATAdmin) return false;
		if (isUserManager != other.isUserManager) return false;
		return true;
	}
}
