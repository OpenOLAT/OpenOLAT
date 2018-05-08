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
package org.olat.core.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;

/**
 * 
 * Initial date: 25 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RolesByOrganisation implements Serializable {

	private static final long serialVersionUID = -8830925452353030770L;
	
	private final OrganisationRef organisation;
	private final OrganisationRoles[] roles;
	
	public RolesByOrganisation(OrganisationRef organisation, OrganisationRoles[] roles) {
		this.organisation = organisation;
		this.roles = roles;
	}
	
	public RolesByOrganisation(OrganisationRef organisation, List<OrganisationRoles> roles) {
		this.organisation = organisation;
		this.roles = roles == null ? new OrganisationRoles[0] : roles.toArray(new OrganisationRoles[roles.size()]);
	}
	
	public static RolesByOrganisation roles(OrganisationRef org, boolean guest, boolean invitee,
			boolean user, boolean coach, boolean author,
			boolean groupManager, boolean poolManager, boolean curriculummanager,
			boolean usermanager, boolean learnresourcemanager, boolean admin) {
		
		List<OrganisationRoles> roleList = new ArrayList<>();
		if(guest) {
			roleList.add(OrganisationRoles.guest);
		} else {
			if(user) {
				roleList.add(OrganisationRoles.user);
			}
			if(invitee) {
				roleList.add(OrganisationRoles.invitee);
			}
			if(coach) {
				roleList.add(OrganisationRoles.coach);
			}
			if(groupManager) {
				roleList.add(OrganisationRoles.groupmanager);
			}
			if(poolManager) {
				roleList.add(OrganisationRoles.poolmanager);
			}
			if(curriculummanager) {
				roleList.add(OrganisationRoles.curriculummanager);
			}
			if(author) {
				roleList.add(OrganisationRoles.author);
			}
			if(usermanager) {
				roleList.add(OrganisationRoles.usermanager);
			}
			if(learnresourcemanager) {
				roleList.add(OrganisationRoles.learnresourcemanager);
			}
			if(admin) {
				roleList.add(OrganisationRoles.administrator);
			}
		}
		return new RolesByOrganisation(org, roleList.toArray(new OrganisationRoles[roleList.size()]));
	}
	
	public OrganisationRef getOrganisation() {
		return organisation;
	}
	
	public boolean matchOrganisation(OrganisationRef org) {
		return organisation.getKey().equals(org.getKey());
	}
	
	public boolean matchOrganisationOrItsParents(Organisation org) {
		if(organisation.getKey().equals(org.getKey())) {
			return true;
		}
		
		for(OrganisationRef parent:org.getParentLine()) {
			if(parent.getKey().equals(organisation.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isGuestOnly() {
		return hasRole(OrganisationRoles.guest);
	}
	
	public boolean isInvitee() {
		return hasRole(OrganisationRoles.invitee);
	}
	
	public boolean isUser() {
		return hasRole(OrganisationRoles.user);
	}
	
	public boolean isCoach() {
		return hasRole(OrganisationRoles.coach);
	}
	
	public boolean isAuthor() {
		return hasRole(OrganisationRoles.author);
	}
	
	public boolean isGroupManager() {
		return hasRole(OrganisationRoles.groupmanager);
	}
	
	public boolean isUserManager() {
		return hasRole(OrganisationRoles.usermanager);
	}
	
	public boolean isPoolManager() {
		return hasRole(OrganisationRoles.poolmanager);
	}
	
	public boolean isCurriculumManager() {
		return hasRole(OrganisationRoles.curriculummanager);
	}
	
	public boolean isLearnResourceManager() {
		return hasRole(OrganisationRoles.learnresourcemanager);
	}
	
	public boolean isAdministrator() {
		return hasRole(OrganisationRoles.administrator);
	}
	
	public boolean hasRole(OrganisationRoles role) {
		boolean hasRole = false;
		if(roles != null && roles.length > 0) {
			for(int i=roles.length; i-->0; ) {
				if(roles[i] == role) {
					hasRole = true;
				}
			}
		}
		return hasRole;
	}

}
