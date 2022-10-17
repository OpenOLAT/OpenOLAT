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
package org.olat.user.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.core.id.Roles;

/**
 * 
 * Initial date: 07.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rolesVO")
public class RolesVO {
	
	private boolean systemAdmin = false;
	private boolean olatAdmin = false;
	private boolean userManager = false;
	private boolean groupManager = false;
	private boolean author = false;
	private boolean guestOnly = false;
	private boolean institutionalResourceManager = false;
	private boolean poolAdmin = false;
	private boolean curriculumManager = false;
	private boolean invitee = false;

	public RolesVO() {
		//for JAXB
	}

	public RolesVO(Roles roles) {
		systemAdmin = roles.isSystemAdmin();
		olatAdmin = roles.isAdministrator();
		groupManager = roles.isGroupManager();
		userManager = roles.isUserManager();
		author = roles.isAuthor();
		guestOnly = roles.isGuestOnly();
		institutionalResourceManager = roles.isLearnResourceManager();
		poolAdmin = roles.isPoolManager();
		curriculumManager = roles.isCurriculumManager();
		invitee = roles.isInvitee();
	}

	public boolean isSystemAdmin() {
		return systemAdmin;
	}

	public void setSystemAdmin(boolean systemAdmin) {
		this.systemAdmin = systemAdmin;
	}

	public boolean isOlatAdmin() {
		return olatAdmin;
	}

	public void setOlatAdmin(boolean olatAdmin) {
		this.olatAdmin = olatAdmin;
	}

	public boolean isUserManager() {
		return userManager;
	}

	public void setUserManager(boolean userManager) {
		this.userManager = userManager;
	}

	public boolean isGroupManager() {
		return groupManager;
	}

	public void setGroupManager(boolean groupManager) {
		this.groupManager = groupManager;
	}

	public boolean isAuthor() {
		return author;
	}

	public void setAuthor(boolean author) {
		this.author = author;
	}

	public boolean isGuestOnly() {
		return guestOnly;
	}

	public void setGuestOnly(boolean guestOnly) {
		this.guestOnly = guestOnly;
	}

	public boolean isInstitutionalResourceManager() {
		return institutionalResourceManager;
	}

	public void setInstitutionalResourceManager(boolean institutionalResourceManager) {
		this.institutionalResourceManager = institutionalResourceManager;
	}

	public boolean isPoolAdmin() {
		return poolAdmin;
	}

	public void setPoolAdmin(boolean poolAdmin) {
		this.poolAdmin = poolAdmin;
	}

	public boolean isCurriculumManager() {
		return curriculumManager;
	}

	public void setCurriculumManager(boolean curriculumManager) {
		this.curriculumManager = curriculumManager;
	}

	public boolean isInvitee() {
		return invitee;
	}

	public void setInvitee(boolean invitee) {
		this.invitee = invitee;
	}

}
