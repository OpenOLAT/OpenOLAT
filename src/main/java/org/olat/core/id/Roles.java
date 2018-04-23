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
	private boolean isSystemAdmin;
	private boolean isOLATAdmin;
	private boolean isUserManager;
	private boolean isGroupManager;
	private boolean isAuthor;
	private boolean isGuestOnly;
	private boolean isInstitutionalResourceManager;
	private boolean isPoolAdmin;
	private boolean isCurriculumManager;
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
		this(false, isOLATAdmin, isGroupManager, isUserManager, isAuthor, isGuestOnly, isInstitutionalResourceManager, false,  false, isInvitee);
	}
	
	public Roles(boolean isSystemAdmin, boolean isOLATAdmin, boolean isUserManager, boolean isGroupManager, boolean isAuthor, boolean isGuestOnly,
			boolean isInstitutionalResourceManager, boolean isPoolAdmin, boolean isCurriculumManager, boolean isInvitee) {
		this.isSystemAdmin = isSystemAdmin;
		this.isOLATAdmin = isOLATAdmin;
		this.isGroupManager = isGroupManager;
		this.isUserManager = isUserManager;
		this.isAuthor = isAuthor;
		this.isGuestOnly = isGuestOnly;
		this.isInstitutionalResourceManager = isInstitutionalResourceManager;
		this.isPoolAdmin = isPoolAdmin;
		this.isCurriculumManager = isCurriculumManager;
		this.isInvitee = isInvitee;
	}
	
	/**
	 * The roles of a standard user without special permissions.
	 * 
	 * @return The roles object
	 */
	public static final Roles userRoles() {
		return new Roles(false, false, false, false, false, false, false, false, false, false);
	}
	
	public boolean isSystemAdmin() {
		return isSystemAdmin;
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
	
	public boolean isCurriculumManager() {
		return isCurriculumManager;
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
		result = prime * result + (isPoolAdmin ? 1231 : 1237);
		result = prime * result + (isCurriculumManager ? 1231 : 1237);
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
		return isOLATAdmin == other.isOLATAdmin
				&& isUserManager == other.isUserManager
				&& isGroupManager == other.isGroupManager
				&& isAuthor == other.isAuthor
				&& isGuestOnly == other.isGuestOnly
				&& isInstitutionalResourceManager == other.isInstitutionalResourceManager
				&& isPoolAdmin == other.isPoolAdmin
				&& isCurriculumManager == other.isCurriculumManager
				&& isInvitee == other.isInvitee;
	}
}
