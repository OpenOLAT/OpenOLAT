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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.EditMemberController.Modification;

/**
 * 
 * Initial date: 12 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditMemberCurriculumElementRow extends AbstractCurriculumElementRow {
	
	private EditMemberCurriculumElementRow parent;
	private final EnumMap<CurriculumRoles,FormLink> rolesChangeLinks = new EnumMap<>(CurriculumRoles.class);
	private final EnumMap<CurriculumRoles,Modification> modifications = new EnumMap<>(CurriculumRoles.class);
	
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

	public void addButton(CurriculumRoles role, FormLink button) {
		rolesChangeLinks.put(role, button);
	}
	
	public FormLink getButton(CurriculumRoles role) {
		return rolesChangeLinks.get(role);
	}
	
	public void addModification(CurriculumRoles role, Modification modification) {
		modifications.put(role, modification);
	}
	
	public Modification getModification(CurriculumRoles role) {
		return modifications.get(role);
	}
	
	public void resetModification() {
		modifications.clear();
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
