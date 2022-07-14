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
package org.olat.group.ui.main;

import org.olat.basesecurity.GroupRoles;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseMembership {
	
	private boolean repositoryEntryOwner;
	private boolean repositoryEntryCoach;
	private boolean repositoryEntryParticipant;
	
	private boolean businessGroupCoach;
	private boolean businessGroupParticipant;
	private boolean businessGroupWaiting;
	
	private boolean curriculumElementCoach;
	private boolean curriculumElementParticipant;
	private boolean curriculumElementOwner;
	private boolean curriculumElementMasterCoach;
	
	private boolean pending;
	private boolean externalUser;
	private boolean managedMembersRepo;
	
	public CourseMembership() {
		//
	}

	public boolean isOwner() {
		return repositoryEntryOwner || curriculumElementOwner;
	}
	
	public boolean isCoach() {
		return repositoryEntryCoach || businessGroupCoach || curriculumElementCoach;
	}
	
	public boolean isParticipant() {
		return repositoryEntryParticipant || businessGroupParticipant || curriculumElementParticipant;
	}
	
	public boolean isRepositoryEntryMember() {
		return repositoryEntryOwner || repositoryEntryCoach || repositoryEntryParticipant;
	}
	
	public boolean isBusinessGroupMember() {
		return businessGroupCoach || businessGroupParticipant || businessGroupWaiting;
	}

	public boolean isWaiting() {
		return businessGroupWaiting;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public boolean isExternalUser() {
		return externalUser;
	}

	public void setExternalUser(boolean externalUser) {
		this.externalUser = externalUser;
	}

	public boolean isManagedMembersRepo() {
		return managedMembersRepo;
	}

	public void setManagedMembersRepo(boolean managedMembersRepo) {
		this.managedMembersRepo = managedMembersRepo;
	}

	public boolean isRepositoryEntryOwner() {
		return repositoryEntryOwner;
	}

	public void setRepositoryEntryOwner(boolean repositoryEntryOwner) {
		this.repositoryEntryOwner = repositoryEntryOwner;
	}

	public boolean isRepositoryEntryCoach() {
		return repositoryEntryCoach;
	}

	public void setRepositoryEntryCoach(boolean repositoryEntryCoach) {
		this.repositoryEntryCoach = repositoryEntryCoach;
	}

	public boolean isRepositoryEntryParticipant() {
		return repositoryEntryParticipant;
	}

	public void setRepositoryEntryParticipant(boolean repositoryEntryParticipant) {
		this.repositoryEntryParticipant = repositoryEntryParticipant;
	}
	
	public void setRepositoryEntryRole(String role) {
		if(GroupRoles.participant.name().equals(role)) {
			setRepositoryEntryParticipant(true);
		} else if(GroupRoles.coach.name().equals(role)) {
			setRepositoryEntryCoach(true);
		} else if(GroupRoles.owner.name().equals(role)) {
			setRepositoryEntryOwner(true);
		}
	}

	public boolean isBusinessGroupCoach() {
		return businessGroupCoach;
	}

	public void setBusinessGroupCoach(boolean businessGroupCoach) {
		this.businessGroupCoach = businessGroupCoach;
	}

	public boolean isBusinessGroupParticipant() {
		return businessGroupParticipant;
	}

	public void setBusinessGroupParticipant(boolean businessGroupParticipant) {
		this.businessGroupParticipant = businessGroupParticipant;
	}

	public boolean isBusinessGroupWaiting() {
		return businessGroupWaiting;
	}

	public void setBusinessGroupWaiting(boolean businessGroupWaiting) {
		this.businessGroupWaiting = businessGroupWaiting;
	}
	
	public void setBusinessGroupRole(String role) {
		if(GroupRoles.participant.name().equals(role)) {
			setBusinessGroupParticipant(true);
		} else if(GroupRoles.coach.name().equals(role)) {
			setBusinessGroupCoach(true);
		} else if(GroupRoles.waiting.name().equals(role)) {
			setBusinessGroupWaiting(true);
		}
	}

	public boolean isCurriculumElementOwner() {
		return curriculumElementOwner;
	}

	public void setCurriculumElementOwner(boolean curriculumElementOwner) {
		this.curriculumElementOwner = curriculumElementOwner;
	}

	public boolean isCurriculumElementCoach() {
		return curriculumElementCoach;
	}

	public void setCurriculumElementCoach(boolean curriculumElementCoach) {
		this.curriculumElementCoach = curriculumElementCoach;
	}

	public boolean isCurriculumElementMasterCoach() {
		return curriculumElementMasterCoach;
	}

	public void setCurriculumElementMasterCoach(boolean curriculumElementMasterCoach) {
		this.curriculumElementMasterCoach = curriculumElementMasterCoach;
	}

	public boolean isCurriculumElementParticipant() {
		return curriculumElementParticipant;
	}

	public void setCurriculumElementParticipant(boolean curriculumElementParticipant) {
		this.curriculumElementParticipant = curriculumElementParticipant;
	}
	
	public void setCurriculumElementRole(String role) {
		if(CurriculumRoles.participant.name().equals(role)) {
			setCurriculumElementParticipant(true);
		} else if(CurriculumRoles.coach.name().equals(role)) {
			setCurriculumElementCoach(true);
		} else if(CurriculumRoles.owner.name().equals(role)) {
			setRepositoryEntryOwner(true);
		} else if(CurriculumRoles.curriculumelementowner.name().equals(role)) {
			setCurriculumElementOwner(true);
		} else if(CurriculumRoles.mastercoach.name().equals(role)) {
			setCurriculumElementMasterCoach(true);
		}
	}
}