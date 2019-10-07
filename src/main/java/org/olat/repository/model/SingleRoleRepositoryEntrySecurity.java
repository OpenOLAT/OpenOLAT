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

import java.util.HashSet;
import java.util.Set;

import org.olat.repository.RepositoryEntrySecurity;

/**
 * 
 * Initial date: 2 Oct 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SingleRoleRepositoryEntrySecurity implements RepositoryEntrySecurity {

	public enum Role { owner, coach, participant };
	
	private Role currentRole;
	private RepositoryEntrySecurity wrappedSecurity;
	
	public SingleRoleRepositoryEntrySecurity(RepositoryEntrySecurity wrappedSecurity) {
		this.wrappedSecurity = wrappedSecurity;
		this.currentRole = getDefaultRole();
	}

	private Role getDefaultRole() {
		if (wrappedSecurity.isParticipant()) {
			return Role.participant;
		} else if (wrappedSecurity.isCoach() || wrappedSecurity.isMasterCoach()) {
			return Role.coach;
		} if (wrappedSecurity.isEntryAdmin() || wrappedSecurity.isPrincipal() || wrappedSecurity.isMasterCoach()) {
			return Role.owner;
		}
		return Role.participant;
	}

	public Role getCurrentRole() {
		return currentRole;
	}

	public void setCurrentRole(Role role) {
		this.currentRole = role;
	}

	public RepositoryEntrySecurity getWrappedSecurity() {
		return wrappedSecurity;
	}

	public void setWrappedSecurity(RepositoryEntrySecurity wrappedSecurity) {
		this.wrappedSecurity = wrappedSecurity;
	}
	
	public Set<Role> getOtherRoles() {
		Set<Role> otherRoles = new HashSet<>();
		if (!Role.participant.equals(currentRole) && wrappedSecurity.isParticipant()) {
			otherRoles.add(Role.participant);
		}
		if (!Role.coach.equals(currentRole) && (wrappedSecurity.isCoach() || wrappedSecurity.isMasterCoach())) {
			otherRoles.add(Role.coach);
		}
		if (!Role.owner.equals(currentRole) && (wrappedSecurity.isEntryAdmin() || wrappedSecurity.isPrincipal() || wrappedSecurity.isMasterCoach())) {
			otherRoles.add(Role.owner);
		}
		return otherRoles;
	}

	@Override
	public boolean isOwner() {
		return Role.owner.equals(currentRole) && wrappedSecurity.isOwner();
	}

	@Override
	public boolean isCoach() {
		return Role.coach.equals(currentRole) && wrappedSecurity.isCoach();
	}

	@Override
	public boolean isParticipant() {
		return Role.participant.equals(currentRole) && wrappedSecurity.isParticipant();
	}

	@Override
	public boolean isMasterCoach() {
		return Role.coach.equals(currentRole) && wrappedSecurity.isMasterCoach();
	}

	@Override
	public boolean isEntryAdmin() {
		return Role.owner.equals(currentRole) && wrappedSecurity.isEntryAdmin();
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
		return Role.owner.equals(currentRole) && wrappedSecurity.isPrincipal();
	}

	@Override
	public boolean isOnlyPrincipal() {
		return Role.owner.equals(currentRole) && wrappedSecurity.isOnlyPrincipal();
	}

	@Override
	public boolean isOnlyMasterCoach() {
		return Role.coach.equals(currentRole) && wrappedSecurity.isOnlyMasterCoach();
	}
	
}
