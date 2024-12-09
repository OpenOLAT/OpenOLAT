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

import java.util.EnumMap;
import java.util.List;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 11 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberRolesDetailsRow extends AbstractCurriculumElementRow {
	
	private final List<CurriculumRoles> roles;
	private final EnumMap<CurriculumRoles,GroupMembershipStatus> rolesStatus = new EnumMap<>(CurriculumRoles.class);
	private final EnumMap<CurriculumRoles,MembershipModification> modifications = new EnumMap<>(CurriculumRoles.class);
	
	private MemberRolesDetailsRow parent;
	
	public MemberRolesDetailsRow(CurriculumElement curriculumElement, List<CurriculumRoles> roles) {
		super(curriculumElement);
		this.roles = roles;
	}

	public List<CurriculumRoles> getRoles() {
		return roles;
	}
	
	public void addStatus(CurriculumRoles role, GroupMembershipStatus status) {
		rolesStatus.put(role, status);
	}
	
	public GroupMembershipStatus getStatus(CurriculumRoles role) {
		return rolesStatus.get(role);
	}
	
	public GroupMembershipStatus getModificationStatus(CurriculumRoles role) {
		MembershipModification modification = modifications.get(role);
		return modification == null ? null : modification.nextStatus();
	}
	
	public void addModification(CurriculumRoles role, MembershipModification modification) {
		modifications.put(role, modification);
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
}
