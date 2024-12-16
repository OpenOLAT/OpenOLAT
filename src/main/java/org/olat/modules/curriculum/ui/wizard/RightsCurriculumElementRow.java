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
package org.olat.modules.curriculum.ui.wizard;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.member.AbstractCurriculumElementRow;
import org.olat.modules.curriculum.ui.member.MembershipModification;

/**
 * 
 * Initial date: 9 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RightsCurriculumElementRow extends AbstractCurriculumElementRow {

	private FormLink addButton;
	private FormLink noteButton;
	private RightsCurriculumElementRow parent;
	private MembershipModification modification;
	
	public RightsCurriculumElementRow(CurriculumElement curriculumElement) {
		super(curriculumElement);
	}

	@Override
	public RightsCurriculumElementRow getParent() {
		return parent;
	}
	
	public void setParent(RightsCurriculumElementRow parent) {
		this.parent = parent;
	}
	
	public FormLink getAddButton() {
		return addButton;
	}

	public void setAddButton(FormLink button) {
		this.addButton = button;
	}
	
	public FormLink getNoteButton() {
		return noteButton;
	}

	public void setNoteButton(FormLink noteButton) {
		this.noteButton = noteButton;
	}

	public MembershipModification getModification() {
		return modification;
	}
	
	public void setModification(MembershipModification modification) {
		this.modification = modification;
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
		if(obj instanceof RightsCurriculumElementRow detailsRow) {
			return getKey().equals(detailsRow.getKey());
		}
		return false;
	}
}
