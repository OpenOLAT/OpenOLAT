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
package org.olat.course.member;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupMembership;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryMembership;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PermissionHelper {
	
	public static final RepoPermission getPermission(RepositoryEntry re, Identity id, List<RepositoryEntryMembership> memberships) {
		RepoPermission p = new RepoPermission();
		if(id != null && memberships != null && !memberships.isEmpty()) {
			for(RepositoryEntryMembership membership:memberships) {
				if(membership.isOwner() && re.getKey().equals(membership.getRepoKey())) {
					p.setOwner(true);
				}
				if(membership.isCoach() && re.getKey().equals(membership.getRepoKey())) {
					p.setTutor(true);
				}
				if(membership.isParticipant() && re.getKey().equals(membership.getRepoKey())) {
					p.setParticipant(true);
				}
			}
		}
		return p;
	}
	
	public static final RepoPermission getPermission(CurriculumElement element, Identity id, List<CurriculumElementMembership> memberships) {
		RepoPermission p = new RepoPermission();
		if(id != null && memberships != null && !memberships.isEmpty()) {
			for(CurriculumElementMembership membership:memberships) {
				if(membership.isRepositoryEntryOwner() && element.getKey().equals(membership.getCurriculumElementKey())) {
					p.setOwner(true);
				}
				if(membership.isCoach() && element.getKey().equals(membership.getCurriculumElementKey())) {
					p.setTutor(true);
				}
				if(membership.isParticipant() && element.getKey().equals(membership.getCurriculumElementKey())) {
					p.setParticipant(true);
				}
				if(membership.isMasterCoach() && element.getKey().equals(membership.getCurriculumElementKey())) {
					p.setMasterCoach(true);
				}
				if(membership.isCurriculumElementOwner() && element.getKey().equals(membership.getCurriculumElementKey())) {
					p.setCurriculumElementOwner(true);
				}
			}
		}
		return p;
	}
	
	public static final BGPermission getPermission(Long groupkey, Identity id, List<BusinessGroupMembership> memberships) {
		BGPermission p = new BGPermission();
		if(id != null && memberships != null && !memberships.isEmpty()) {
			for(BusinessGroupMembership membership:memberships) {
				if(membership.getGroupKey().equals(groupkey)) {
					if(membership.isOwner()) {
						p.setTutor(true);
					}
					if(membership.isParticipant()) {
						p.setParticipant(true);
					}
					if(membership.isWaiting()) {
						p.setWaitingList(true);
					}
				}
			}
		}
		return p;
	}
	
	public static class RepoPermission {
		private boolean owner = false;
		private boolean tutor = false;
		private boolean participant = false;
		private boolean masterCoach = false;
		private boolean curriculumElementOwner = false;

		public boolean isOwner() {
			return owner;
		}
		public void setOwner(boolean owner) {
			this.owner = owner;
		}
		
		public boolean isTutor() {
			return tutor;
		}
		public void setTutor(boolean tutor) {
			this.tutor = tutor;
		}
		
		public boolean isParticipant() {
			return participant;
		}
		public void setParticipant(boolean participant) {
			this.participant = participant;
		}
		
		public boolean isMasterCoach() {
			return masterCoach;
		}
		public void setMasterCoach(boolean masterCoach) {
			this.masterCoach = masterCoach;
		}
		
		public boolean isCurriculumElementOwner() {
			return curriculumElementOwner;
		}
		public void setCurriculumElementOwner(boolean curriculumElementOwner) {
			this.curriculumElementOwner = curriculumElementOwner;
		}
	}
	
	public static class BGPermission {
		boolean tutor = false;
		boolean participant = false;
		boolean waitingList = false;

		public boolean isTutor() {
			return tutor;
		}
		public void setTutor(boolean tutor) {
			this.tutor = tutor;
		}
		
		public boolean isParticipant() {
			return participant;
		}
		public void setParticipant(boolean participant) {
			this.participant = participant;
		}
		
		public boolean isWaitingList() {
			return waitingList;
		}
		public void setWaitingList(boolean waitingList) {
			this.waitingList = waitingList;
		}
	}
}