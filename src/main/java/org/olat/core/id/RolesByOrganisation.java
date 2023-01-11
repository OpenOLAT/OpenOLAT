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
import java.util.Arrays;
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
		if(roles != null && roles.length > 1) {
			Arrays.sort(roles);
		}
		this.roles = roles == null ? OrganisationRoles.EMPTY_ROLES : roles;
	}
	
	public RolesByOrganisation(OrganisationRef organisation, List<OrganisationRoles> roles) {
		this(organisation, roles == null ? OrganisationRoles.EMPTY_ROLES : roles.toArray(new OrganisationRoles[roles.size()]));
	}
	
	public static RolesByOrganisation empty(OrganisationRef organisation) {
		return new RolesByOrganisation(organisation, OrganisationRoles.EMPTY_ROLES);
	}
	
	public static RolesByOrganisation enhance(RolesByOrganisation original, List<OrganisationRoles> rolesToAdd, List<OrganisationRoles> rolesToRemove) {
		List<OrganisationRoles> roles = new ArrayList<>();
		for(OrganisationRoles role:original.roles) {
			roles.add(role);
		}
		if(rolesToAdd != null) {
			for(OrganisationRoles roleToAdd:rolesToAdd) {
				if(!roles.contains(roleToAdd)) {
					roles.add(roleToAdd);
				}
			}
		}
		if(rolesToRemove != null) {
			for(OrganisationRoles roleToRemove:rolesToRemove) {
				roles.remove(roleToRemove);
			}
		}
		return new RolesByOrganisation(original.getOrganisation(), roles);
	}
	
	public static RolesByOrganisation roles(OrganisationRef org,
			boolean guest, boolean invitee, boolean user, boolean author,
			boolean groupManager, boolean poolManager, boolean curriculummanager,
			boolean usermanager, boolean learnresourcemanager,
			boolean admin, boolean sysAdmin) {
		
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
			if(sysAdmin) {
				roleList.add(OrganisationRoles.sysadmin);
			}
		}
		return new RolesByOrganisation(org, roleList.toArray(new OrganisationRoles[roleList.size()]));
	}
	
	public OrganisationRef getOrganisation() {
		return organisation;
	}
	
	public boolean matchOrganisation(OrganisationRef org) {
		if(organisation == null) return true;
		return organisation.getKey().equals(org.getKey());
	}
	
	public boolean matchOrganisationOrItsParents(Organisation org) {
		if(organisation == null) return true;
		
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
	
	public boolean isAuthor() {
		return hasRole(OrganisationRoles.author);
	}
	
	public boolean isGroupManager() {
		return hasRole(OrganisationRoles.groupmanager);
	}
	
	public boolean isUserManager() {
		return hasRole(OrganisationRoles.usermanager);
	}
	
	public boolean isRolesManager() {
		return hasRole(OrganisationRoles.rolesmanager);
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
	
	public boolean isSystemAdministrator() {
		return hasRole(OrganisationRoles.sysadmin);
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
	
	/**
	 * Check if the roles in this organization match at least one
	 * of the specified wanted roles.
	 * 
	 * @param wantedRoles A list of roles to match (partially)
	 * @return true if role matches
	 */
	public boolean hasSomeRoles(OrganisationRoles... wantedRoles) {
		if(roles != null && roles.length > 0) {
			for(int i=roles.length; i-->0; ) {
				for(int j=wantedRoles.length; j-->0; ) {
					if(roles[i] == wantedRoles[j]) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hashCode = 31;
		if(roles != null) {
			for(OrganisationRoles role:roles) {
				hashCode += role.hashCode();
			}
		}
		if(organisation != null && organisation.getKey() != null) {
			hashCode += organisation.hashCode();
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RolesByOrganisation) {
			RolesByOrganisation r = (RolesByOrganisation)obj;
			if((r.organisation == null && organisation == null)
					|| (r.organisation != null && organisation != null && r.organisation.getKey().equals(organisation.getKey()))) {
				return Arrays.equals(r.roles, roles);
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("organisation[key=").append(organisation == null ? "NULL" : organisation.getKey()).append("]")
		  .append(roles == null ? "[]" : roles.toString());
		return sb.toString();
	}
}
