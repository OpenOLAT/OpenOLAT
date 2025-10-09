/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.member;

import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.resource.accesscontrol.ConfirmationByEnum;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberRolesDetailsRow extends AbstractCurriculumElementRow {
	
	private ModificationStatusSummary modificationSummary;
	
	private final List<CurriculumRoles> roles;
	private final EnumMap<CurriculumRoles,RoleDetails> details = new EnumMap<>(CurriculumRoles.class);
	
	private MemberRolesDetailsRow parent;
	
	public MemberRolesDetailsRow(CurriculumElement curriculumElement, List<CurriculumRoles> roles) {
		super(curriculumElement);
		this.roles = roles;
	}

	public List<CurriculumRoles> getRoles() {
		return roles;
	}
	
	public GroupMembershipStatus getStatus(CurriculumRoles role) {
		RoleDetails d = details.get(role);
		return d == null ? null : d.getRolesStatus();
	}
		
	public void addStatus(CurriculumRoles role, GroupMembershipStatus status) {
		computeIfAbsent(role).setRolesStatus(status);
	}
	
	public ConfirmationByEnum getConfirmationBy(CurriculumRoles role) {
		RoleDetails d = details.get(role);
		return d == null ? null : d.getConfirmationBy();
	}
	
	public void addConfirmationBy(CurriculumRoles role, ConfirmationByEnum by) {
		computeIfAbsent(role).setConfirmationBy(by);
	}
	
	public Date getConfirmationUntil(CurriculumRoles role) {
		RoleDetails d = details.get(role);
		return d == null ? null : d.getConfirmationUntil();
	}
	
	public void addConfirmationUntil(CurriculumRoles role, Date date) {
		computeIfAbsent(role).setConfirmationUntil(date);
	}
	
	public boolean hasModifications() {
		for(Map.Entry<CurriculumRoles,RoleDetails> entry:details.entrySet()) {
			if(entry.getValue().getModification() != null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasReservations() {
		for(Map.Entry<CurriculumRoles,RoleDetails> entry:details.entrySet()) {
			if(GroupMembershipStatus.reservation.equals(entry.getValue().getRolesStatus())) {
				return true;
			}
		}
		return false;
	}
	
	public GroupMembershipStatus getModificationStatus(CurriculumRoles role) {
		RoleDetails d = details.get(role);
		return d == null || d.getModification() == null ? null : d.getModification().nextStatus();
	}
	
	public ConfirmationByEnum getModificationConfirmationBy(CurriculumRoles role) {
		RoleDetails d = details.get(role);
		return d == null || d.getModification() == null ? null : d.getModification().confirmationBy();
	}
	
	public Date getModificationConfirmationUntil(CurriculumRoles role) {
		RoleDetails d = details.get(role);
		return d == null || d.getModification() == null ? null : d.getModification().confirmUntil();
	}
	
	public void addModification(CurriculumRoles role, MembershipModification modification) {
		computeIfAbsent(role).setModification(modification);
	}
	
	public RoleDetails computeIfAbsent(CurriculumRoles role) {
		return details.computeIfAbsent(role, r -> new RoleDetails());
	}
	
	public ModificationStatusSummary getModificationSummary() {
		return modificationSummary;
	}

	public void setModificationSummary(ModificationStatusSummary modificationSummary) {
		this.modificationSummary = modificationSummary;
	}

	@Override
	public MemberRolesDetailsRow getParent() {
		return parent;
	}

	public void setParent(MemberRolesDetailsRow parent) {
		this.parent = parent;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MemberRolesDetailsRow detailsRow) {
			return getKey().equals(detailsRow.getKey());
		}
		return false;
	}
	
	private static final class RoleDetails {
		
		private GroupMembershipStatus rolesStatus;
		private MembershipModification modification;
		private ConfirmationByEnum confirmationBy;
		private Date confirmationUntil;
		
		public RoleDetails() {
			//
		}

		public GroupMembershipStatus getRolesStatus() {
			return rolesStatus;
		}

		public void setRolesStatus(GroupMembershipStatus rolesStatus) {
			this.rolesStatus = rolesStatus;
		}

		public MembershipModification getModification() {
			return modification;
		}

		public void setModification(MembershipModification modification) {
			this.modification = modification;
		}

		public ConfirmationByEnum getConfirmationBy() {
			return confirmationBy;
		}

		public void setConfirmationBy(ConfirmationByEnum confirmationBy) {
			this.confirmationBy = confirmationBy;
		}

		public Date getConfirmationUntil() {
			return confirmationUntil;
		}

		public void setConfirmationUntil(Date confirmationUntil) {
			this.confirmationUntil = confirmationUntil;
		}
	}
}
