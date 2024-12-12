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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditMemberCurriculumElementRow extends AbstractCurriculumElementRow {
	
	private EditMemberCurriculumElementRow parent;
	private final EnumMap<CurriculumRoles,FormLink> rolesModelLinks = new EnumMap<>(CurriculumRoles.class);
	private final EnumMap<CurriculumRoles,MembershipModification> modifications = new EnumMap<>(CurriculumRoles.class);
	private final EnumMap<CurriculumRoles,ResourceReservation> rolesReservation = new EnumMap<>(CurriculumRoles.class);
	private final EnumMap<CurriculumRoles,GroupMembershipStatus> rolesStatus = new EnumMap<>(CurriculumRoles.class);
	
	public EditMemberCurriculumElementRow(CurriculumElement curriculumElement) {
		super(curriculumElement);
	}

	@Override
	public EditMemberCurriculumElementRow getParent() {
		return parent;
	}
	
	public void setParent(EditMemberCurriculumElementRow parent) {
		this.parent = parent;
	}

	public ResourceReservation getReservation(CurriculumRoles role) {
		return rolesReservation.get(role);
	}

	public void addReservation(CurriculumRoles role, ResourceReservation reservation) {
		rolesReservation.put(role, reservation);
	}
	
	public GroupMembershipStatus getStatus(CurriculumRoles role) {
		return rolesStatus.get(role);
	}
	
	public void setStatus(CurriculumRoles role, GroupMembershipStatus status) {
		if(status == null) {
			rolesStatus.remove(role);
		} else {
			rolesStatus.put(role, status);
		}
	}

	public void addButton(CurriculumRoles role, FormLink button) {
		rolesModelLinks.put(role, button);
	}
	
	public FormLink getButton(CurriculumRoles role) {
		return rolesModelLinks.get(role);
	}
	
	public void addModification(CurriculumRoles role, MembershipModification modification) {
		modifications.put(role, modification);
	}
	
	public MembershipModification getModification(CurriculumRoles role) {
		return modifications.get(role);
	}
	
	public List<MembershipModification> getModifications() {
		return new ArrayList<>(modifications.values());
	}
	
	public void resetModification() {
		modifications.clear();
	}
	
	public void removeModification(CurriculumRoles role) {
		modifications.remove(role);
	}
	
	public boolean hasModifications() {
		return !modifications.isEmpty();
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
		if(obj instanceof EditMemberCurriculumElementRow detailsRow) {
			return getKey().equals(detailsRow.getKey());
		}
		return false;
	}
}
