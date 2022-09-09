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
package org.olat.repository.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.repository.RepositoryEntrySecurity;

/**
 * 
 * Initial date: 2 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleRoleRepositoryEntrySecurity implements RepositoryEntrySecurity {
	
	public enum Role { 
		owner("role.owner", "o_icon_owner"),
		administrator("role.administrator", "o_icon_administrator"),
		learningResourceManager("role.learning.resource.manager", "o_icon_lrm"),
		coach("role.coach", "o_icon_coach"),
		principal("role.principal", "o_icon_principal"),
		masterCoach("role.master.coach", "o_icon_master_coach"),
		participant("role.participant", "o_icon_user"),
		fakeParticipant("role.fake.participant", "o_icon_fake_participant");
		
		private final String i18nKey;
		private final String iconCssClass;
		
		private Role(String i18nKey, String iconCssClass) {
			this.i18nKey = i18nKey;
			this.iconCssClass = iconCssClass;
		}

		public String getI18nKey() {
			return i18nKey;
		}

		public String getIconCssClass() {
			return iconCssClass;
		}
		
	}
	
	private Role currentRole;
	private Role previousRole;
	private RepositoryEntrySecurity wrappedSecurity;
	
	public SingleRoleRepositoryEntrySecurity(RepositoryEntrySecurity wrappedSecurity) {
		this.wrappedSecurity = wrappedSecurity;
		this.currentRole = getDefaultRole();
	}

	private Role getDefaultRole() {
		if (wrappedSecurity.isOwner()) {
			return Role.owner;
		} else if (wrappedSecurity.isAdministrator()) {
			return Role.administrator;
		} else if (wrappedSecurity.isLearnResourceManager()) {
			return Role.learningResourceManager;
		} else if (wrappedSecurity.isCoach()) {
			return Role.coach;
		} else if (wrappedSecurity.isPrincipal()) {
			return Role.principal;
		} else if (wrappedSecurity.isMasterCoach()) {
			return Role.masterCoach;
		}
		return Role.participant;
	}

	public Role getCurrentRole() {
		return currentRole;
	}

	public void setCurrentRole(Role role) {
		previousRole = currentRole;
		currentRole = role;
	}

	public Role getPreviousRole() {
		return previousRole;
	}

	public RepositoryEntrySecurity getWrappedSecurity() {
		return wrappedSecurity;
	}

	public void setWrappedSecurity(RepositoryEntrySecurity wrappedSecurity) {
		this.wrappedSecurity = wrappedSecurity;
		// current role no longer possible
		if ((Role.owner == currentRole && !wrappedSecurity.isOwner())
			|| (Role.administrator == currentRole && !wrappedSecurity.isAdministrator())
			|| (Role.learningResourceManager == currentRole && !wrappedSecurity.isLearnResourceManager())
			|| (Role.coach == currentRole && !wrappedSecurity.isCoach())
			|| (Role.principal == currentRole && !wrappedSecurity.isPrincipal())
			|| (Role.masterCoach == currentRole && !wrappedSecurity.isMasterCoach())
			|| (Role.participant == currentRole && !wrappedSecurity.isParticipant())
			|| (Role.fakeParticipant == currentRole && !isFakeParticipantAvailable())) {
			setCurrentRole(getDefaultRole());
		}
	}
	
	public Collection<Role> getOtherRoles() {
		List<Role> otherRoles = new ArrayList<>(Role.values().length);
		if (Role.owner != currentRole && wrappedSecurity.isOwner()) {
			otherRoles.add(Role.owner);
		}
		if (Role.administrator != currentRole && wrappedSecurity.isAdministrator()) {
			otherRoles.add(Role.administrator);
		}
		if (Role.learningResourceManager != currentRole && wrappedSecurity.isLearnResourceManager()) {
			otherRoles.add(Role.learningResourceManager);
		}
		if (Role.coach != currentRole && wrappedSecurity.isCoach()) {
			otherRoles.add(Role.coach);
		}
		if (Role.principal != currentRole && wrappedSecurity.isPrincipal()) {
			otherRoles.add(Role.principal);
		}
		if (Role.masterCoach != currentRole && wrappedSecurity.isMasterCoach()) {
			otherRoles.add(Role.masterCoach);
		}
		if (Role.participant != currentRole && wrappedSecurity.isParticipant()) {
			otherRoles.add(Role.participant);
		}
		if (Role.fakeParticipant != currentRole && isFakeParticipantAvailable()) {
			otherRoles.add(Role.fakeParticipant);
		}
		return otherRoles;
	}
	
	private boolean isFakeParticipantAvailable() {
		if (wrappedSecurity.isOwner() || wrappedSecurity.isCoach() || wrappedSecurity.isMasterCoach()) {
			if (!wrappedSecurity.isParticipant()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isOwner() {
		return Role.owner == currentRole;
	}

	@Override
	public boolean isCoach() {
		return Role.coach == currentRole;
	}

	@Override
	public boolean isParticipant() {
		return Role.participant == currentRole || Role.fakeParticipant == currentRole;
	}

	@Override
	public boolean isMasterCoach() {
		return Role.masterCoach == currentRole;
	}

	@Override
	public boolean isEntryAdmin() {
		return Role.owner == currentRole || Role.administrator == currentRole || Role.learningResourceManager == currentRole;
	}

	@Override
	public boolean canLaunch() {
		return wrappedSecurity.canLaunch();
	}

	@Override
	public boolean isReadOnly() {
		return wrappedSecurity.isReadOnly();
	}

	@Override
	public boolean isCourseParticipant() {
		return Role.participant.equals(currentRole) && wrappedSecurity.isCourseParticipant();
	}

	@Override
	public boolean isCourseCoach() {
		return Role.coach.equals(currentRole) && wrappedSecurity.isCourseCoach();
	}

	@Override
	public boolean isGroupParticipant() {
		return Role.participant.equals(currentRole) && wrappedSecurity.isGroupParticipant();
	}

	@Override
	public boolean isGroupCoach() {
		return Role.coach.equals(currentRole) && wrappedSecurity.isGroupCoach();
	}

	@Override
	public boolean isGroupWaiting() {
		return wrappedSecurity.isGroupWaiting();
	}

	@Override
	public boolean isCurriculumParticipant() {
		return Role.participant.equals(currentRole) && wrappedSecurity.isCurriculumParticipant();
	}

	@Override
	public boolean isCurriculumCoach() {
		return Role.coach.equals(currentRole) && wrappedSecurity.isCurriculumCoach();
	}

	@Override
	public boolean isMember() {
		return wrappedSecurity.isMember();
	}

	@Override
	public boolean isAuthor() {
		return wrappedSecurity.isAuthor();
	}

	@Override
	public boolean isPrincipal() {
		return Role.principal == currentRole;
	}

	@Override
	public boolean isAdministrator() {
		return Role.administrator == currentRole;
	}

	@Override
	public boolean isLearnResourceManager() {
		return Role.learningResourceManager == currentRole;
	}

	@Override
	public boolean isOnlyPrincipal() {
		return Role.principal == currentRole && wrappedSecurity.isOnlyPrincipal();
	}

	@Override
	public boolean isOnlyMasterCoach() {
		return Role.masterCoach == currentRole && wrappedSecurity.isOnlyMasterCoach();
	}
	
}
