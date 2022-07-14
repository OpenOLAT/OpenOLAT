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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.OrganisationRoles;

/**
*  Description:<br>
* @author Felix Jost
*/
public class Roles implements Serializable {
	private static final long serialVersionUID = 4726449291059674346L;

	private final boolean isGuestOnly;
	
	private List<RolesByOrganisation> rolesByOrganisations;

	private Roles(List<RolesByOrganisation> rolesByOrganisations, boolean isGuestOnly) {
		this.rolesByOrganisations = rolesByOrganisations;
		this.isGuestOnly = isGuestOnly;
	}
	
	public static final Roles guestRoles() {
		RolesByOrganisation guest = new RolesByOrganisation(null, new OrganisationRoles[] {  OrganisationRoles.guest });
		return new Roles(Collections.singletonList(guest), true);
	}
	
	/**
	 * The roles of a standard user without special permissions.
	 * 
	 * @return The roles object
	 */
	public static final Roles userRoles() {
		RolesByOrganisation lrm = new RolesByOrganisation(null, new OrganisationRoles[] {  OrganisationRoles.user });
		return new Roles(Collections.singletonList(lrm), false);
	}
	
	public static final Roles authorRoles() {
		RolesByOrganisation lrm = new RolesByOrganisation(null, new OrganisationRoles[] {  OrganisationRoles.user, OrganisationRoles.author });
		return new Roles(Collections.singletonList(lrm), false);
	}
	
	public static final Roles administratorRoles() {
		RolesByOrganisation lrm = new RolesByOrganisation(null, new OrganisationRoles[] {  OrganisationRoles.user, OrganisationRoles.administrator });
		return new Roles(Collections.singletonList(lrm), false);
	}
	
	public static final Roles administratorAndManagersRoles() {
		RolesByOrganisation lrm = new RolesByOrganisation(null, new OrganisationRoles[] {
				OrganisationRoles.user,
				OrganisationRoles.administrator,
				OrganisationRoles.usermanager,
				OrganisationRoles.poolmanager,
				OrganisationRoles.groupmanager,
				OrganisationRoles.curriculummanager,
				OrganisationRoles.learnresourcemanager	
		});
		return new Roles(Collections.singletonList(lrm), false);
	}
	
	public static final Roles learnResourceManagerRoles() {
		RolesByOrganisation lrm = new RolesByOrganisation(null, new OrganisationRoles[] {  OrganisationRoles.user, OrganisationRoles.learnresourcemanager });
		return new Roles(Collections.singletonList(lrm), false);
	}
	
	public static final Roles valueOf(List<RolesByOrganisation> rolesByOrganisations, boolean isGuestOnly) {
		return new Roles(new ArrayList<>(rolesByOrganisations), isGuestOnly);
	}
	
	public RolesByOrganisation getRoles(OrganisationRef organisation) {
		RolesByOrganisation setOfRoles = null;
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).matchOrganisation(organisation)) {
					setOfRoles = rolesByOrganisations.get(i);
				}
			}
		}
		return setOfRoles;
	}
	
	/**
	 * All the organizations 
	 * 
	 * @return
	 */
	public List<OrganisationRef> getOrganisations() {
		Set<OrganisationRef> organisations = new HashSet<>();
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				organisations.add(rolesByOrganisations.get(i).getOrganisation());
			}
		}
		return new ArrayList<>(organisations);
	}
	
	public List<OrganisationRef> getOrganisationsWithRole(OrganisationRoles role) {
		List<OrganisationRef> organisations = new ArrayList<>();
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).hasRole(role)
						&& rolesByOrganisations.get(i).getOrganisation() != null) {
					organisations.add(rolesByOrganisations.get(i).getOrganisation());
				}
			}
		}
		return organisations;
	}
	
	public List<OrganisationRef> getOrganisationsWithRoles(OrganisationRoles... roles) {
		List<OrganisationRef> organisations = new ArrayList<>();
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).hasSomeRoles(roles)
						&& rolesByOrganisations.get(i).getOrganisation() != null) {
					organisations.add(rolesByOrganisations.get(i).getOrganisation());
				}
			}
		}
		return organisations;
	}
	
	public boolean hasRole(OrganisationRoles role) {
		boolean foundRole = false;
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).hasRole(role)) {
					foundRole = true;
				}
			}
		}
		return foundRole;
	}
	
	public boolean hasRole(OrganisationRef organisation, OrganisationRoles role) {
		boolean foundRole = false;
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).matchOrganisation(organisation) && rolesByOrganisations.get(i).hasRole(role)) {
					foundRole = true;
				}
			}
		}
		return foundRole;
	}
	
	public boolean hasSomeRoles(OrganisationRef organisation, OrganisationRoles... roles) {
		boolean foundRole = false;
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).matchOrganisation(organisation) && rolesByOrganisations.get(i).hasSomeRoles(roles)) {
					foundRole = true;
				}
			}
		}
		return foundRole;
	}
	
	public boolean hasRoleInParentLine(Organisation organisation, OrganisationRoles role) {
		boolean foundRole = false;
		if(rolesByOrganisations != null) {
			for(int i=rolesByOrganisations.size(); i--> 0; ) {
				if(rolesByOrganisations.get(i).matchOrganisationOrItsParents(organisation) && rolesByOrganisations.get(i).hasRole(role)) {
					foundRole = true;
				}
			}
		}
		return foundRole;
	}
	
	public boolean hasRole(List<? extends OrganisationRef> organisations, OrganisationRoles role) {
		if(rolesByOrganisations != null) {
			for(OrganisationRef organisation: organisations) {
				for(int i=rolesByOrganisations.size(); i--> 0; ) {
					if(rolesByOrganisations.get(i).matchOrganisation(organisation) && rolesByOrganisations.get(i).hasRole(role)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isSystemAdmin() {
		return hasRole(OrganisationRoles.sysadmin);
	}
	
	public boolean isAdministrator() {
		return hasRole(OrganisationRoles.administrator);
	}
	
	public boolean isPrincipal() {
		return hasRole(OrganisationRoles.principal);
	}

	public boolean isAuthor() {
		return hasRole(OrganisationRoles.author);
	}
	
	public boolean isGroupManager() {
		return hasRole(OrganisationRoles.groupmanager);
	}
	
	public boolean isRolesManager() {
		return hasRole(OrganisationRoles.rolesmanager);
	}

	public boolean isUserManager() {
		return hasRole(OrganisationRoles.usermanager);
	}
	
	public boolean isLearnResourceManager() {
		return hasRole(OrganisationRoles.learnresourcemanager);
	}
	
	public boolean isPoolManager() {
		return hasRole(OrganisationRoles.poolmanager);
	}
	
	public boolean isCurriculumManager() {
		return hasRole(OrganisationRoles.curriculummanager);
	}
	
	public boolean isLectureManager() {
		return hasRole(OrganisationRoles.lecturemanager);
	}
	
	public boolean isQualityManager() {
		return hasRole(OrganisationRoles.qualitymanager);
	}
	
	public boolean isLineManager() {
		return hasRole(OrganisationRoles.linemanager);
	}
	
	public boolean isManager() {
		return isCurriculumManager()
				|| isGroupManager()
				|| isLearnResourceManager()
				|| isLectureManager()
				|| isLineManager()
				|| isPoolManager()
				|| isQualityManager()
				|| isRolesManager()
				|| isUserManager();
	}

	public boolean isInvitee() {
		return hasRole(OrganisationRoles.invitee);
	}
	
	public boolean isInviteeOnly() {
		return hasRole(OrganisationRoles.invitee) && !hasRole(OrganisationRoles.user);
	}

	public boolean isGuestOnly() {
		return hasRole(OrganisationRoles.guest) || isGuestOnly;
	}
	
	public boolean isManagerOf(OrganisationRoles role, Roles targetRoles) {
		List<OrganisationRef> targetOrganisations = targetRoles.getOrganisationsWithRole(OrganisationRoles.user);
		return hasRole(targetOrganisations, role);
	}
	
	public boolean isInviteeOf(OrganisationRoles role, Roles targetRoles) {
		List<OrganisationRef> targetOrganisations = targetRoles.getOrganisationsWithRole(OrganisationRoles.invitee);
		return hasRole(targetOrganisations, role);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isGuestOnly ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Roles) {
			Roles roles = (Roles) obj;
			if(roles.isGuestOnly != isGuestOnly) {
				return false;
			}
			if((roles.rolesByOrganisations == null || roles.rolesByOrganisations.isEmpty())
					&& (rolesByOrganisations == null || rolesByOrganisations.isEmpty())) {
				return true;
			}
			return roles.rolesByOrganisations != null && rolesByOrganisations != null
					&& roles.rolesByOrganisations.size() == rolesByOrganisations.size()
					&& roles.rolesByOrganisations.containsAll(rolesByOrganisations)
					&& rolesByOrganisations.containsAll(roles.rolesByOrganisations);
		}
		return false;
	}
}
