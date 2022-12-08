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
package org.olat.ldap.model;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.Attributes;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 24.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LDAPUser {
	
	private String dn;
	
	private boolean coach;
	private boolean author;
	private boolean userManager;
	private boolean groupManager;
	private boolean qpoolManager;
	private boolean curriculumManager;
	private boolean learningResourceManager;
	
	private List<String> groupIds;
	private List<String> coachedGroupIds;
	private Attributes attributes;
	private IdentityRef cachedIdentity;
	
	private List<Organisation> organisations;
	
	private boolean notFound = false;
	
	public String getDn() {
		return dn;
	}
	
	public void setDn(String dn) {
		this.dn = dn;
	}
	
	public Attributes getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public boolean isCoach() {
		return coach;
	}

	public void setCoach(boolean coach) {
		this.coach = coach;
	}

	public boolean isAuthor() {
		return author;
	}

	public void setAuthor(boolean author) {
		this.author = author;
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

	public boolean isQpoolManager() {
		return qpoolManager;
	}

	public void setQpoolManager(boolean qpoolManager) {
		this.qpoolManager = qpoolManager;
	}

	public boolean isCurriculumManager() {
		return curriculumManager;
	}

	public void setCurriculumManager(boolean curriculumManager) {
		this.curriculumManager = curriculumManager;
	}

	public boolean isLearningResourceManager() {
		return learningResourceManager;
	}

	public void setLearningResourceManager(boolean learningResourceManager) {
		this.learningResourceManager = learningResourceManager;
	}

	public List<String> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<String> groupIds) {
		this.groupIds = groupIds;
	}

	public List<String> getCoachedGroupIds() {
		return coachedGroupIds;
	}

	public void setCoachedGroupIds(List<String> coachedGroupIds) {
		this.coachedGroupIds = coachedGroupIds;
	}

	public IdentityRef getCachedIdentity() {
		return cachedIdentity;
	}

	public void setCachedIdentity(IdentityRef cachedIdentity) {
		this.cachedIdentity = cachedIdentity;
	}
	
	public boolean isNotFound() {
		return notFound;
	}

	public void setNotFound(boolean notFound) {
		this.notFound = notFound;
	}
	
	public List<OrganisationRoles> getRoles() {
		List<OrganisationRoles> roles = new ArrayList<>();
		if(isAuthor()) {
			roles.add(OrganisationRoles.author);
		}
		if(isUserManager()) {
			roles.add(OrganisationRoles.usermanager);
		}
		if(isGroupManager()) {
			roles.add(OrganisationRoles.groupmanager);
		}
		if(isQpoolManager()) {
			roles.add(OrganisationRoles.poolmanager);
		}
		if(isCurriculumManager()) {
			roles.add(OrganisationRoles.curriculummanager);
		}
		if(isLearningResourceManager()) {
			roles.add(OrganisationRoles.learnresourcemanager);
		}
		return roles;
	}

	public void addRole(OrganisationRoles role) {
		if(role == null) return;
		
		switch(role) {
			case author:
				setAuthor(true);
				break;
			case usermanager:
				setUserManager(true);
				break;
			case groupmanager:
				setGroupManager(true);
				break;
			case poolmanager:
				setQpoolManager(true);
				break;
			case curriculummanager:
				setCurriculumManager(true);
				break;
			case learnresourcemanager:
				setLearningResourceManager(true);
				break;
			default:
				break;
		}
	}

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void addOrganisation(Organisation organisation) {
		if(organisations == null) {
			organisations = new ArrayList<>(3);
		}
		organisations.add(organisation);
	}

	@Override
	public int hashCode() {
		return dn == null ? 76789 : dn.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof LDAPUser) {
			LDAPUser user = (LDAPUser)obj;
			return dn != null && dn.equals(user.getDn());
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "ldapUser[dn=" + dn + "]" + super.toString();
	}
}
