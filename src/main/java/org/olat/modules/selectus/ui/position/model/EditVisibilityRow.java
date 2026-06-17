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
package org.olat.modules.selectus.ui.position.model;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;

/**
 * 
 * Initial date: 18 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditVisibilityRow {
	
	private final Object userObject;
	private final String title;
	private final MultipleSelectionElement expertsEl;
	private final MultipleSelectionElement refereesEl;
	private final MultipleSelectionElement comparativeExpertsEl;
	private final MultipleSelectionElement[] facultyMembersEls;
	private final MultipleSelectionElement publicFeedbackEl;
	
	public EditVisibilityRow(Object userObject, String title,
			MultipleSelectionElement expertsEl, MultipleSelectionElement refereesEl,
			MultipleSelectionElement comparativeExpertsEl, MultipleSelectionElement[] facultyMembersEls,
			MultipleSelectionElement publicFeedbackEl) {
		this.userObject = userObject;
		this.title = title;
		this.expertsEl = expertsEl;
		this.refereesEl = refereesEl;
		this.comparativeExpertsEl = comparativeExpertsEl;
		this.facultyMembersEls = facultyMembersEls;
		this.publicFeedbackEl = publicFeedbackEl;
	}
	
	public Object getUserObject() {
		return userObject;
	}

	public String getTitle() {
		return title;
	}

	public MultipleSelectionElement expertsEl() {
		return expertsEl;
	}
	
	public boolean isExpertsSelected() {
		return isSelected(expertsEl);
	}

	public MultipleSelectionElement refereesEl() {
		return refereesEl;
	}
	
	public boolean isRefereesElSelected() {
		return isSelected(refereesEl);
	}

	public MultipleSelectionElement comparativeExpertsEl() {
		return comparativeExpertsEl;
	}
	
	public boolean isComparativeExpertsElSelected() {
		return isSelected(comparativeExpertsEl);
	}
	
	public MultipleSelectionElement[] facultyMembersEls() {
		return facultyMembersEls;
	}
	
	public boolean isFacultyMembersElsSelected(int i) {
		if(facultyMembersEls != null && i>= 0 && i< facultyMembersEls.length) {
			return isSelected(facultyMembersEls[i]);
		}
		return false;
	}
	
	public MultipleSelectionElement publicFeedbackEl() {
		return publicFeedbackEl;
	}
	
	private boolean isSelected(MultipleSelectionElement el) {
		return el != null && el.isAtLeastSelected(1);
	}
}
